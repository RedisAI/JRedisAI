package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.util.Pool;
import redis.clients.jedis.util.SafeEncoder;

public class RedisAI implements AutoCloseable {

  private final Pool<Jedis> pool;

  private final int blobChunkSize;

  /** Create a new RedisAI client with default connection to local host */
  public RedisAI() {
    this("localhost", 6379);
  }

  /**
   * Create a new RedisAI client
   *
   * @param host the redis host
   * @param port the redis pot
   */
  public RedisAI(String host, int port) {
    this(host, port, 500, 100);
  }

  /**
   * Create a new RedisAI client
   *
   * @param host the redis host
   * @param port the redis pot
   */
  public RedisAI(String host, int port, int timeout, int poolSize) {
    this(host, port, timeout, poolSize, null);
  }

  /**
   * Create a new RedisAI client
   *
   * @param host the redis host
   * @param port the redis pot
   * @param password the password for authentication in a password protected Redis server
   */
  public RedisAI(String host, int port, int timeout, int poolSize, String password) {
    this(new JedisPool(initPoolConfig(poolSize), host, port, timeout, password));
  }

  /**
   * Create a new RedisAI client
   *
   * @param pool jedis connection pool
   */
  public RedisAI(Pool<Jedis> pool) {
    this.pool = pool;
    this.blobChunkSize = readBlobChunkSize(pool);
  }

  @Override
  public void close() {
    this.pool.close();
  }

  /**
   * Constructs JedisPoolConfig object.
   *
   * @param poolSize size of the JedisPool
   * @return {@link JedisPoolConfig} object with a few default settings
   */
  private static JedisPoolConfig initPoolConfig(int poolSize) {
    JedisPoolConfig conf = new JedisPoolConfig();
    conf.setMaxTotal(poolSize);
    conf.setTestOnBorrow(false);
    conf.setTestOnReturn(false);
    conf.setTestOnCreate(false);
    conf.setTestWhileIdle(false);
    conf.setMinEvictableIdleTimeMillis(60000);
    conf.setTimeBetweenEvictionRunsMillis(30000);
    conf.setNumTestsPerEvictionRun(-1);
    conf.setFairness(true);

    return conf;
  }

  private static int readBlobChunkSize(Pool<Jedis> pool) {
    final String chunkSizeKey = "proto-max-bulk-len";
    try (Jedis jedis = pool.getResource()) {
      List<String> configMap = jedis.configGet(chunkSizeKey);
      if (!configMap.isEmpty()) {
        return Integer.parseInt(configMap.get(1));
      }
    } catch (Exception e) {
      // swallow any exception
    }
    return 0;
  }
  /**
   * Direct mapping to AI.TENSORSET
   *
   * @param key name of key to store the Tensor
   * @param values multi-dimension numeric data
   * @param shape one or more dimensions, or the number of elements per axis, for the tensor
   * @return true if Tensor was properly set in RedisAI server
   */
  public boolean setTensor(String key, Object values, int[] shape) {
    DataType dataType = DataType.baseObjType(values);
    long[] shapeL = new long[shape.length];
    for (int i = 0; i < shape.length; i++) {
      shapeL[i] = shape[i];
    }
    Tensor tensor = new Tensor(dataType, shapeL, values);
    return setTensor(key, tensor);
  }

  /**
   * Direct mapping to AI.TENSORSET
   *
   * @param key name of key to store the Tensor
   * @param tensor Tensor object
   * @return true if Tensor was properly set in RedisAI server
   */
  public boolean setTensor(String key, Tensor tensor) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = tensor.tensorSetFlatArgs(key, false);
      return sendCommand(conn, Command.TENSOR_SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");

    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.TENSORGET
   *
   * @param key name of key to get the Tensor from
   * @return Tensor
   * @throws JRedisAIRunTimeException
   */
  public Tensor getTensor(String key) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = Tensor.tensorGetFlatArgs(key, false);
      List<?> reply =
          sendCommand(conn, Command.TENSOR_GET, args.toArray(new byte[args.size()][]))
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return Tensor.createTensorFromRespReply(reply);
    }
  }

  /**
   * Direct mapping to AI.MODELSET
   *
   * @param key name of key to store the Model
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param modelPath - the file path for the Protobuf-serialized model
   * @return true if Model was properly set in RedisAI server
   */
  public boolean setModel(
      String key,
      Backend backend,
      Device device,
      String[] inputs,
      String[] outputs,
      String modelPath) {

    try {
      byte[] blob = Files.readAllBytes(Paths.get(modelPath));
      Model model = new Model(backend, device, inputs, outputs, blob);
      return setModel(key, model);
    } catch (IOException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.MODELSET
   *
   * @param key name of key to store the Model
   * @param model Model object
   * @return true if Model was properly set in RedisAI server
   */
  public boolean setModel(String key, Model model) {

    try (Jedis conn = getConnection()) {
      List<byte[]> args = model.getModelSetCommandBytes(key);
      return sendCommand(conn, Command.MODEL_SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.MODELSTORE command.
   *
   * <p>{@code AI.MODELSTORE <key> <backend> <device> [TAG tag] [BATCHSIZE n [MINBATCHSIZE m]]
   * [INPUTS <input_count> <name> ...] [OUTPUTS <output_count> <name> ...] BLOB <model>}
   *
   * @param key name of key to store the Model
   * @param model Model object
   * @return true if Model was properly stored in RedisAI server
   */
  public boolean storeModel(String key, Model model) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = model.getModelStoreCommandArgs(key, blobChunkSize);
      return sendCommand(conn, Command.MODEL_STORE, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex.getMessage(), ex);
    }
  }

  /**
   * Direct mapping to AI.MODELGET
   *
   * @param key name of key to get the Model from RedisAI server
   * @return Model
   * @throws JRedisAIRunTimeException
   */
  public Model getModel(String key) {
    try (Jedis conn = getConnection()) {
      List<?> reply =
          sendCommand(
                  conn,
                  Command.MODEL_GET,
                  SafeEncoder.encode(key),
                  Keyword.META.getRaw(),
                  Keyword.BLOB.getRaw())
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return Model.createModelFromRespReply(reply);
    }
  }

  /**
   * Direct mapping to AI.MODELDEL
   *
   * @param key name of key to delete the Model
   * @return true if Model was properly delete in RedisAI server
   */
  public boolean delModel(String key) {

    try (Jedis conn = getConnection()) {
      return sendCommand(conn, Command.MODEL_DEL, SafeEncoder.encode(key))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.SCRIPTSET
   *
   * @param key name of key to store the Script in RedisAI server
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param scriptFile - the file path for the script source code
   * @return true if Script was properly set in RedisAI server
   */
  public boolean setScriptFile(String key, Device device, String scriptFile) {
    try {
      Script script = new Script(device, Paths.get(scriptFile));
      return setScript(key, script);
    } catch (IOException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.SCRIPTSET
   *
   * @param key name of key to store the Script in RedisAI server
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param source - the script source code
   * @return true if Script was properly set in RedisAI server
   */
  public boolean setScript(String key, Device device, String source) {
    Script script = new Script(device, source);
    return setScript(key, script);
  }

  /**
   * Direct mapping to AI.SCRIPTSET
   *
   * @param key name of key to store the Script in RedisAI server
   * @param script the Script Object
   * @return true if Script was properly set in RedisAI server
   */
  public boolean setScript(String key, Script script) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = script.getScriptSetCommandBytes(key);
      return sendCommand(conn, Command.SCRIPT_SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");

    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.SCRIPTGET
   *
   * @param key name of key to get the Script from RedisAI server
   * @return Script
   * @throws JRedisAIRunTimeException
   */
  public Script getScript(String key) {
    try (Jedis conn = getConnection()) {
      List<?> reply =
          sendCommand(
                  conn,
                  Command.SCRIPT_GET,
                  SafeEncoder.encode(key),
                  Keyword.META.getRaw(),
                  Keyword.SOURCE.getRaw())
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return Script.createScriptFromRespReply(reply);
    }
  }

  /**
   * Direct mapping to AI.SCRIPTDEL
   *
   * @param key name of key to delete the Script
   * @return true if Script was properly delete in RedisAI server
   */
  public boolean delScript(String key) {

    try (Jedis conn = getConnection()) {
      return sendCommand(conn, Command.SCRIPT_DEL, SafeEncoder.encode(key))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /** AI.MODELRUN model_key INPUTS input_key1 ... OUTPUTS output_key1 ... */
  public boolean runModel(String key, String[] inputs, String[] outputs) {

    try (Jedis conn = getConnection()) {
      List<byte[]> args = Model.modelRunFlatArgs(key, inputs, outputs, false);
      return sendCommand(conn, Command.MODEL_RUN, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");

    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.MODELEXECUTE command.
   *
   * <p>{@code AI.MODELEXECUTE <key> INPUTS <input_count> <input> [input ...] OUTPUTS <output_count>
   * <output> [output ...]}
   *
   * @param key
   * @param inputs
   * @param outputs
   * @return
   */
  public boolean executeModel(String key, String[] inputs, String[] outputs) {
    return executeModel(key, inputs, outputs, -1L);
  }

  /**
   * Direct mapping to AI.MODELEXECUTE command.
   *
   * <p>{@code AI.MODELEXECUTE <key> INPUTS <input_count> <input> [input ...] OUTPUTS <output_count>
   * <output> [output ...] [TIMEOUT t]}
   *
   * @param key
   * @param inputs
   * @param outputs
   * @param timeout timeout in ms
   * @return
   */
  public boolean executeModel(String key, String[] inputs, String[] outputs, long timeout) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = Model.modelExecuteCommandArgs(key, inputs, outputs, timeout, false);
      return sendCommand(conn, Command.MODEL_EXECUTE, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex.getMessage(), ex);
    }
  }

  /** AI.SCRIPTRUN script_key fn_name INPUTS input_key1 ... OUTPUTS output_key1 ... */
  public boolean runScript(String key, String function, String[] inputs, String[] outputs) {

    try (Jedis conn = getConnection()) {
      List<byte[]> args = Script.scriptRunFlatArgs(key, function, inputs, outputs, false);
      return sendCommand(conn, Command.SCRIPT_RUN, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply()
          .equals("OK");

    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * Direct mapping to AI.DAGRUN specifies a direct acyclic graph of operations to run within
   * RedisAI
   *
   * @param loadKeys
   * @param persistKeys
   * @param dag
   * @return
   */
  public List<?> dagRun(String[] loadKeys, String[] persistKeys, Dag dag) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = dag.dagRunFlatArgs(loadKeys, persistKeys);
      List<?> reply =
          sendCommand(conn, Command.DAGRUN, args.toArray(new byte[args.size()][]))
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return dag.processDagReply(reply);
    }
  }

  /**
   * Direct mapping to AI.DAGRUN_RO specifies a Read Only direct acyclic graph of operations to run
   * within RedisAI
   *
   * @param loadKeys
   * @param dag
   * @return
   */
  public List<?> dagRunReadOnly(String[] loadKeys, Dag dag) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = dag.dagRunFlatArgs(loadKeys, null);
      List<?> reply =
          sendCommand(conn, Command.DAGRUN_RO, args.toArray(new byte[args.size()][]))
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return dag.processDagReply(reply);
    }
  }

  /**
   * Direct mapping to AI.DAGEXECUTE specifies a direct acyclic graph of operations to run within
   * RedisAI
   *
   * @param loadTensors
   * @param persistTensors
   * @param dag
   * @return
   */
  public List<?> dagExecute(
      String[] loadTensors, String[] persistTensors, String[] keysArg, Dag dag) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = dag.dagExecuteFlatArgs(loadTensors, persistTensors, keysArg);
      List<?> reply =
          sendCommand(conn, Command.DAGEXECUTE, args.toArray(new byte[args.size()][]))
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return dag.processDagReply(reply);
    }
  }

  /**
   * Direct mapping to AI.DAGEXECUTE specifies a direct acyclic graph of operations to run within
   * RedisAI
   *
   * @param loadKeys
   * @param dag
   * @return
   */
  public List<?> dagExecuteReadOnly(String[] loadKeys, String[] keysArg, Dag dag) {
    try (Jedis conn = getConnection()) {
      List<byte[]> args = dag.dagExecuteFlatArgs(loadKeys, null, keysArg);
      List<?> reply =
          sendCommand(conn, Command.DAGEXECUTE_RO, args.toArray(new byte[args.size()][]))
              .getObjectMultiBulkReply();
      if (reply.isEmpty()) {
        return null;
      }
      return dag.processDagReply(reply);
    }
  }

  /**
   * AI.INFO <key> [RESETSTAT]
   *
   * @param key the key name of a model or script
   * @return a map of attributes for the given model or script
   */
  public Map<String, Object> getInfo(String key) {
    try (Jedis conn = getConnection()) {
      List<Object> values =
          sendCommand(conn, Command.INFO, SafeEncoder.encode(key)).getObjectMultiBulkReply();

      Map<String, Object> infoMap = new HashMap<>(values.size());
      for (int i = 0; i < values.size(); i += 2) {
        Object val = values.get(i + 1);
        if (val instanceof byte[]) {
          val = SafeEncoder.encode((byte[]) val);
        }
        infoMap.put(SafeEncoder.encode((byte[]) values.get(i)), val);
      }
      return infoMap;
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * AI.INFO <key> RESETSTAT resets all statistics associated with the key
   *
   * @param key the key name of a model or script
   * @return
   */
  public boolean resetStat(String key) {
    try (Jedis conn = getConnection()) {
      return sendCommand(conn, Command.INFO, SafeEncoder.encode(key), Keyword.RESETSTAT.getRaw())
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  private Jedis getConnection() {
    return pool.getResource();
  }

  private BinaryClient sendCommand(Jedis conn, Command command, byte[]... args) {
    BinaryClient client = conn.getClient();
    client.sendCommand(command, args);
    return client;
  }

  /**
   * AI.CONFIG <BACKENDSPATH <path>>
   *
   * @return
   */
  public boolean setBackendsPath(String path) {
    try (Jedis conn = getConnection()) {
      return sendCommand(
              conn, Command.CONFIG, Keyword.BACKENDSPATH.getRaw(), SafeEncoder.encode(path))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }

  /**
   * AI.CONFIG <LOADBACKEND <backend> <path>>
   *
   * @return
   */
  public boolean loadBackend(Backend backEnd, String path) {
    try (Jedis conn = getConnection()) {
      return sendCommand(
              conn,
              Command.CONFIG,
              Keyword.LOADBACKEND.getRaw(),
              backEnd.getRaw(),
              SafeEncoder.encode(path))
          .getStatusCodeReply()
          .equals("OK");
    } catch (JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }
}
