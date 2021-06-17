package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.util.ArrayList;
import java.util.List;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

/** Direct mapping to RedisAI Model */
public class Model {

  private Backend backend;
  private Device device;
  private String[] inputs;
  private String[] outputs;
  private byte[] blob;
  private String tag;
  private long batchSize;
  private long minBatchSize;
  private long minBatchTimeout;

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param blob - the Protobuf-serialized model
   */
  public Model(Backend backend, Device device, byte[] blob) {
    this.backend = backend;
    this.device = device;
    this.blob = blob;
  }

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param blob - the Protobuf-serialized model
   */
  public Model(Backend backend, Device device, String[] inputs, String[] outputs, byte[] blob) {
    this(backend, device, inputs, outputs, blob, 0, 0);
  }

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param blob - the Protobuf-serialized model
   * @param batchSize - when provided with an batchsize that is greater than 0, the engine will
   *     batch incoming requests from multiple clients that use the model with input tensors of the
   *     same shape.
   * @param minBatchSize - when provided with an minbatchsize that is greater than 0, the engine
   *     will postpone calls to AI.MODELRUN until the batch's size had reached minbatchsize
   */
  public Model(
      Backend backend,
      Device device,
      String[] inputs,
      String[] outputs,
      byte[] blob,
      long batchSize,
      long minBatchSize) {
    this.backend = backend;
    this.device = device;
    this.inputs = inputs;
    this.outputs = outputs;
    this.blob = blob;
    this.tag = null;
    this.batchSize = batchSize;
    this.minBatchSize = minBatchSize;
  }

  public static Model createModelFromRespReply(List<?> reply) {
    Backend backend = null;
    Device device = null;
    String tag = null;
    byte[] blob = null;
    long batchsize = 0;
    long minbatchsize = 0;
    long minbatchtimeout = 0;
    String[] inputs = new String[0];
    String[] outputs = new String[0];
    for (int i = 0; i < reply.size(); i += 2) {
      String arrayKey = SafeEncoder.encode((byte[]) reply.get(i));
      switch (arrayKey) {
        case "backend":
          String backendString = SafeEncoder.encode((byte[]) reply.get(i + 1));
          backend = Backend.valueOf(backendString);
          if (backend == null) {
            throw new JRedisAIRunTimeException("Unrecognized backend: " + backendString);
          }
          break;
        case "device":
          String deviceString = SafeEncoder.encode((byte[]) reply.get(i + 1));
          device = Device.valueOf(deviceString);
          if (device == null) {
            throw new JRedisAIRunTimeException("Unrecognized device: " + deviceString);
          }
          break;
        case "tag":
          tag = SafeEncoder.encode((byte[]) reply.get(i + 1));
          break;
        case "blob":
          blob = (byte[]) reply.get(i + 1);
          break;
        case "batchsize":
          batchsize = (Long) reply.get(i + 1);
          break;
        case "minbatchsize":
          minbatchsize = (Long) reply.get(i + 1);
          break;
        case "minbatchtimeout":
          minbatchtimeout = (Long) reply.get(i + 1);
          break;
        case "inputs":
          List<byte[]> inputsEncoded = (List<byte[]>) reply.get(i + 1);
          if (!inputsEncoded.isEmpty()) {
            inputs = new String[inputsEncoded.size()];
            for (int j = 0; j < inputsEncoded.size(); j++) {
              inputs[j] = SafeEncoder.encode(inputsEncoded.get(j));
            }
          }
          break;
        case "outputs":
          List<byte[]> outputsEncoded = (List<byte[]>) reply.get(i + 1);
          if (!outputsEncoded.isEmpty()) {
            outputs = new String[outputsEncoded.size()];
            for (int j = 0; j < outputsEncoded.size(); j++) {
              outputs[j] = SafeEncoder.encode(outputsEncoded.get(j));
            }
          }
          break;
        default:
          break;
      }
    }

    if (backend == null || device == null || blob == null) {
      throw new JRedisAIRunTimeException(
          "AI.MODELGET reply did not contained all elements to build the model");
    }
    return new Model(backend, device, blob)
        .setInputs(inputs)
        .setOutputs(outputs)
        .setBatchSize(batchsize)
        .setMinBatchSize(minbatchsize)
        .setMinBatchTimeout(minbatchtimeout)
        .setTag(tag);
  }

  public String getTag() {
    return tag;
  }

  public Model setTag(String tag) {
    this.tag = tag;
    return this;
  }

  public byte[] getBlob() {
    return blob;
  }

  public void setBlob(byte[] blob) {
    this.blob = blob;
  }

  public String[] getOutputs() {
    return outputs;
  }

  public Model setOutputs(String[] outputs) {
    this.outputs = outputs;
    return this;
  }

  public String[] getInputs() {
    return inputs;
  }

  public Model setInputs(String[] inputs) {
    this.inputs = inputs;
    return this;
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public Backend getBackend() {
    return backend;
  }

  public void setBackend(Backend backend) {
    this.backend = backend;
  }

  public long getBatchSize() {
    return batchSize;
  }

  public Model setBatchSize(long batchsize) {
    this.batchSize = batchsize;
    return this;
  }

  public long getMinBatchSize() {
    return minBatchSize;
  }

  public Model setMinBatchSize(long minbatchsize) {
    this.minBatchSize = minbatchsize;
    return this;
  }

  public long getMinBatchTimeout() {
    return minBatchTimeout;
  }

  public Model setMinBatchTimeout(long minBatchTimeout) {
    this.minBatchTimeout = minBatchTimeout;
    return this;
  }

  /**
   * Encodes the current model properties into an AI.MODELSET command to be store in RedisAI Server
   *
   * @param key name of key to store the Model
   * @return
   */
  protected List<byte[]> getModelSetCommandBytes(String key) {
    List<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(key));
    args.add(backend.getRaw());
    args.add(device.getRaw());
    if (tag != null) {
      args.add(Keyword.TAG.getRaw());
      args.add(SafeEncoder.encode(tag));
    }
    if (batchSize > 0) {
      args.add(Keyword.BATCHSIZE.getRaw());
      args.add(Protocol.toByteArray(batchSize));
      if (minBatchSize > 0) {
        args.add(Keyword.MINBATCHSIZE.getRaw());
        args.add(Protocol.toByteArray(minBatchSize));
      }
    }
    args.add(Keyword.INPUTS.getRaw());
    for (String input : inputs) {
      args.add(SafeEncoder.encode(input));
    }
    args.add(Keyword.OUTPUTS.getRaw());
    for (String output : outputs) {
      args.add(SafeEncoder.encode(output));
    }
    args.add(Keyword.BLOB.getRaw());
    args.add(blob);
    return args;
  }

  /**
   * Encodes the current model properties into an AI.MODELSTORE command to store in RedisAI Server.
   *
   * @param key
   * @return
   */
  protected List<byte[]> getModelStoreCommandArgs(String key) {

    List<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(key));

    args.add(backend.getRaw());
    args.add(device.getRaw());

    if (tag != null) {
      args.add(Keyword.TAG.getRaw());
      args.add(SafeEncoder.encode(tag));
    }

    if (batchSize > 0) {
      args.add(Keyword.BATCHSIZE.getRaw());
      args.add(Protocol.toByteArray(batchSize));

      args.add(Keyword.MINBATCHSIZE.getRaw());
      args.add(Protocol.toByteArray(minBatchSize));

      args.add(Keyword.MINBATCHTIMEOUT.getRaw());
      args.add(Protocol.toByteArray(minBatchTimeout));
    }

    if (inputs != null && inputs.length > 0) {
      args.add(Keyword.INPUTS.getRaw());
      args.add(Protocol.toByteArray(inputs.length));
      for (String input : inputs) {
        args.add(SafeEncoder.encode(input));
      }
    }

    if (outputs != null && outputs.length > 0) {
      args.add(Keyword.OUTPUTS.getRaw());
      args.add(Protocol.toByteArray(outputs.length));
      for (String output : outputs) {
        args.add(SafeEncoder.encode(output));
      }
    }

    args.add(Keyword.BLOB.getRaw());
    args.add(blob);

    return args;
  }

  protected static List<byte[]> modelRunFlatArgs(
      String key, String[] inputs, String[] outputs, boolean includeCommandName) {
    List<byte[]> args = new ArrayList<>();
    if (includeCommandName) {
      args.add(Command.MODEL_RUN.getRaw());
    }
    args.add(SafeEncoder.encode(key));

    args.add(Keyword.INPUTS.getRaw());
    for (String input : inputs) {
      args.add(SafeEncoder.encode(input));
    }

    args.add(Keyword.OUTPUTS.getRaw());
    for (String output : outputs) {
      args.add(SafeEncoder.encode(output));
    }
    return args;
  }

  protected static List<byte[]> modelExecuteCommandArgs(
      String key, String[] inputs, String[] outputs, long timeout, boolean includeCommandName) {

    List<byte[]> args = new ArrayList<>();
    if (includeCommandName) {
      args.add(Command.MODEL_EXECUTE.getRaw());
    }
    args.add(SafeEncoder.encode(key));

    args.add(Keyword.INPUTS.getRaw());
    args.add(Protocol.toByteArray(inputs.length));
    for (String input : inputs) {
      args.add(SafeEncoder.encode(input));
    }

    args.add(Keyword.OUTPUTS.getRaw());
    args.add(Protocol.toByteArray(outputs.length));
    for (String output : outputs) {
      args.add(SafeEncoder.encode(output));
    }

    if (timeout >= 0) {
      args.add(Keyword.TIMEOUT.getRaw());
      args.add(Protocol.toByteArray(timeout));
    }
    return args;
  }
}
