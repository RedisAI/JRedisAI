package com.redislabs.redisai;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class RedisAITest {

  private final JedisPool pool = new JedisPool();
  private final RedisAI client = new RedisAI(pool);
  private final RedisAI clientDefaultConnection = new RedisAI();
  private final RedisAI clientHostPort = new RedisAI("localhost", 6379);
  private final RedisAI clientPoolSize = new RedisAI("localhost", 6379, 30000, 1);

  @Before
  public void testClient() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }

  @Test
  public void testClientDefaultConnectionConstructorSetTensorFLOAT() {
    Assert.assertTrue(
        clientDefaultConnection.setTensor(
            "ClientDefaultConnectionConstructor:tensor",
            new float[][] {{1, 2}, {3, 4}},
            new int[] {2, 2}));
  }

  @Test
  public void testClientHostPortConstructorSetTensorFLOAT() {
    Assert.assertTrue(
        clientHostPort.setTensor(
            "ClientHostPortConstructor:tensor", new float[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
  }

  @Test
  public void testClientPoolSizeConstructorSetTensorFLOAT() {
    Assert.assertTrue(
        clientPoolSize.setTensor(
            "ClientPoolSizeConstructor:tensor", new float[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
  }

  @Test
  public void testSetTensorNegative() {
    try {
      client.setTensor("t1", new float[] {1, 2}, new int[] {1});
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: ERR wrong number of arguments for 'AI.TENSORSET' command",
          e.getMessage());
    }

    try {
      Tensor t1 = new Tensor(DataType.FLOAT, new long[] {1}, new float[] {1, 2});
      client.setTensor("t1", t1);
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: ERR wrong number of arguments for 'AI.TENSORSET' command",
          e.getMessage());
    }
  }

  @Test
  public void testSetTensorFLOAT() {
    Assert.assertTrue(client.setTensor("t1", new float[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
    Tensor t1 = new Tensor(DataType.FLOAT, new long[] {2, 2}, new float[][] {{1, 2}, {3, 4}});
    Assert.assertTrue(client.setTensor("t2", t1));
    Tensor t2 = client.getTensor("t2");
    Assert.assertEquals(t1.getDataType(), t2.getDataType());
    Assert.assertArrayEquals(t1.getShape(), t2.getShape());
    Assert.assertArrayEquals(new float[][] {{1, 2}, {3, 4}}, (float[][]) t1.getValues());
    Assert.assertArrayEquals(new float[] {1, 2, 3, 4}, (float[]) t2.getValues(), (float) 0.1);
  }

  @Test
  public void testSetTensorDOUBLE() {
    Assert.assertTrue(client.setTensor("t1", new double[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
    Tensor t1 = new Tensor(DataType.DOUBLE, new long[] {2, 2}, new double[][] {{1, 2}, {3, 4}});
    Assert.assertTrue(client.setTensor("t2", t1));
  }

  @Test
  public void testSetTensorINT32() {
    Assert.assertTrue(client.setTensor("t1", new int[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
    Tensor t1 = new Tensor(DataType.INT32, new long[] {2, 2}, new int[][] {{1, 2}, {3, 4}});
    Assert.assertTrue(client.setTensor("t2", t1));
  }

  @Test
  public void testSetTensorINT64() {
    Assert.assertTrue(client.setTensor("t1", new long[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
    Tensor t1 = new Tensor(DataType.INT64, new long[] {2, 2}, new long[][] {{1, 2}, {3, 4}});
    Assert.assertTrue(client.setTensor("t2", t1));
  }

  @Test
  public void testConfig() {
    Assert.assertTrue(client.setBackendsPath("/usr/lib/redis/modules/backends/"));
    try {
      client.loadBackend(Backend.TF, "notexist/redisai_tensorflow.so");
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      // ERR error loading backend
    }
    try {
      client.loadBackend(Backend.TF, "redisai_tensorflow/redisai_tensorflow.so");
    } catch (RedisAIException e) {
      // will throw error if backend already loaded
    }
  }

  @Test
  public void testSetModel() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    String[] inputs = new String[] {"a", "b"};
    String[] outputs = new String[] {"mul"};
    Assert.assertTrue(client.setModel("model", Backend.TF, Device.CPU, inputs, outputs, model));
    Model m1 = client.getModel("model");
    Assert.assertEquals(Device.CPU, m1.getDevice());
    Assert.assertEquals(Backend.TF, m1.getBackend());
    Assert.assertArrayEquals(inputs, m1.getInputs());
    Assert.assertArrayEquals(outputs, m1.getOutputs());
    Assert.assertEquals(0, m1.getBatchSize());
    Assert.assertEquals(0, m1.getMinBatchSize());
    m1.setBatchSize(10);
    m1.setMinBatchSize(5);
    m1.setTag("test minbatching 5 batchsize 10");
    Assert.assertTrue(client.setModel("model:m1", m1));
    Model m1FromKeyspace = client.getModel("model:m1");
    Assert.assertEquals(10, m1FromKeyspace.getBatchSize());
    Assert.assertEquals(5, m1FromKeyspace.getMinBatchSize());
    Assert.assertEquals(Device.CPU, m1FromKeyspace.getDevice());
    Assert.assertEquals(Backend.TF, m1FromKeyspace.getBackend());
    Assert.assertArrayEquals(inputs, m1FromKeyspace.getInputs());
    Assert.assertArrayEquals(outputs, m1FromKeyspace.getOutputs());
    Assert.assertEquals("test minbatching 5 batchsize 10", m1FromKeyspace.getTag());
  }

  @Test
  public void testSetModelNegative() {
    // Wrong backend for the specified model
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      String model = classLoader.getResource("test_data/graph.pb").getFile();
      client.setModel("model", Backend.ONNX, Device.CPU, new String[0], new String[0], model);
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: No graph was found in the protobuf.",
          e.getMessage());
    }

    try {
      Model m1 = new Model(Backend.ONNX, Device.CPU, new String[0], new String[0], new byte[0]);
      client.setModel("m1", m1);
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: No graph was found in the protobuf.",
          e.getMessage());
    }
  }

  @Test
  public void testSetModelFromModelOnnx() {
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      String modelPath = classLoader.getResource("test_data/mnist.onnx").getFile();
      byte[] blob = Files.readAllBytes(Paths.get(modelPath));
      Model m1 = new Model(Backend.ONNX, Device.CPU, new String[] {}, new String[] {}, blob);
      Assert.assertTrue(client.setModel("mnist.onnx", m1));
      Model m2 = client.getModel("mnist.onnx");
      Assert.assertEquals(m1.getDevice(), m2.getDevice());
      Assert.assertEquals(m1.getBackend(), m2.getBackend());
      Assert.assertTrue(client.setModel("mnist.onnx.m2", m2));
      Model m3 = client.getModel("mnist.onnx.m2");
      Assert.assertEquals(m2.getDevice(), m3.getDevice());
      Assert.assertEquals(m2.getBackend(), m3.getBackend());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @org.junit.Ignore
  @Test
  public void testSetModelFromModelTFLite() {
    try {
      ClassLoader classLoader = getClass().getClassLoader();
      String modelPath = classLoader.getResource("test_data/mnist_model_quant.tflite").getFile();
      byte[] blob = Files.readAllBytes(Paths.get(modelPath));
      Model m1 = new Model(Backend.TFLITE, Device.CPU, new String[] {}, new String[] {}, blob);
      Assert.assertTrue(client.setModel("mnist.tflite", m1));
      Model m2 = client.getModel("mnist.tflite");
      Assert.assertEquals(m1.getDevice(), m2.getDevice());
      Assert.assertEquals(m1.getBackend(), m2.getBackend());
      Assert.assertTrue(client.setModel("mnist.tflite.m2", m2));
      Model m3 = client.getModel("mnist.tflite.m2");
      Assert.assertEquals(m2.getDevice(), m3.getDevice());
      Assert.assertEquals(m2.getBackend(), m3.getBackend());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  @Test
  public void testRunModel() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    client.setModel(
        "model", Backend.TF, Device.CPU, new String[] {"a", "b"}, new String[] {"mul"}, model);

    client.setTensor("a", new float[] {2, 3}, new int[] {2});
    client.setTensor("b", new float[] {3, 5}, new int[] {2});

    Assert.assertTrue(client.runModel("model", new String[] {"a", "b"}, new String[] {"c"}));
    Tensor tensor = client.getTensor("c");
    float[] values = (float[]) tensor.getValues();
    float[] expected = new float[] {6, 15};
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(values, expected, (float) 0.1);
  }

  @Test
  public void testRunModelNegative() {
    try {
      client.runModel("dont:exist", new String[] {"a", "b"}, new String[] {"c"});
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: ERR model key is empty",
          e.getMessage());
    }
  }

  @Test
  public void testSetScriptFile() {
    ClassLoader classLoader = getClass().getClassLoader();
    String scriptFile = classLoader.getResource("test_data/script.txt").getFile();
    Assert.assertTrue(client.setScriptFile("script", Device.CPU, scriptFile));
  }

  @Test
  public void testSetScript() {
    String script = "def bar(a, b):\n" + "    return a + b\n";
    Assert.assertTrue(client.setScript("script", Device.CPU, script));
  }

  @Test
  public void testSetScriptNegative() {
    try {
      Script script = new Script(Device.CPU, "bad function -----");
      client.setScript("script.error1", script);
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      // long error message
    }

    try {
      client.setScript("script.error1", Device.CPU, "bad function -----");
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      // long error message
    }
  }

  @Test
  public void testRunScript() {
    ClassLoader classLoader = getClass().getClassLoader();
    String script = classLoader.getResource("test_data/script.txt").getFile();
    client.setScriptFile("script", Device.CPU, script);

    client.setTensor("a1", new float[] {2, 3}, new int[] {2});
    client.setTensor("b1", new float[] {2, 3}, new int[] {2});

    Assert.assertTrue(
        client.runScript("script", "bar", new String[] {"a1", "b1"}, new String[] {"c1"}));
  }

  @Test
  public void testRunScriptNegative() {
    try {
      client.runScript("dont:exist", "bar", new String[] {"a1", "b1"}, new String[] {"c1"});
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: ERR script key is empty",
          e.getMessage());
    }
  }

  @Test
  public void runScriptVariadicInputs() {
    String script =
        "def addn(a, args : List[Tensor]):\n" + "    return a + torch.stack(args).sum()\n";
    client.setScript("var_in", new Script(Device.CPU, script));

    client.setTensor("t1", new float[] {40}, new int[] {1});
    client.setTensor("t2", new float[] {1}, new int[] {1});
    client.setTensor("t3", new float[] {1}, new int[] {1});

    Assert.assertTrue(
        client.runScript(
            "var_in", "addn", new String[] {"t1", "t2", "t3"}, true, new String[] {"r"}));
    Tensor result = client.getTensor("r");
    assertEquals(DataType.FLOAT, result.getDataType());
    assertArrayEquals(new long[] {1}, result.getShape());
    assertArrayEquals(new float[] {42}, (float[]) result.getValues(), 0f);
  }

  @Test
  public void testGetTensor() {
    Assert.assertTrue(client.setTensor("t1", new float[][] {{1, 2}, {3, 4}}, new int[] {2, 2}));
    Tensor tensor = client.getTensor("t1");
    float[] values = (float[]) tensor.getValues();
    Assert.assertEquals("Assert same shape of values", 4, values.length);
    float[] expected = new float[] {1, 2, 3, 4};
    Assert.assertArrayEquals(values, expected, (float) 0.1);
  }

  @Test
  public void testInfo() {
    String key = "test:info:script";
    String script = "def bar(a, b):\n" + "    return a + b\n";
    Assert.assertTrue(client.setScript(key, Device.CPU, script));

    // not exist
    Map<String, Object> infoMap;
    try {
      infoMap = client.getInfo("not:exist");
      Assert.fail("Should throw RedisAIException");
    } catch (RedisAIException e) {
      // ERR cannot find run info for key
    }

    // first inited info
    infoMap = client.getInfo(key);
    Assert.assertNotNull(infoMap);
    Assert.assertEquals(key, infoMap.get("key"));
    Assert.assertEquals(Device.CPU.name(), infoMap.get("device"));
    Assert.assertEquals(0L, infoMap.get("calls"));

    client.setTensor("a1", new float[] {2, 3}, new int[] {2});
    client.setTensor("b1", new float[] {2, 3}, new int[] {2});
    Assert.assertTrue(client.runScript(key, "bar", new String[] {"a1", "b1"}, new String[] {"c1"}));

    // one model runs
    infoMap = client.getInfo(key);
    Assert.assertEquals(1L, infoMap.get("calls"));

    // reset
    Assert.assertTrue(client.resetStat(key));
    infoMap = client.getInfo(key);
    Assert.assertEquals(0L, infoMap.get("calls"));

    try {
      client.resetStat("not:exist");
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      // ERR cannot find run info for key
    }
  }

  @Test
  public void delModel() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    Assert.assertTrue(
        client.setModel(
            "model", Backend.TF, Device.CPU, new String[] {"a", "b"}, new String[] {"mul"}, model));
    Assert.assertTrue(client.delModel("model"));
    try {
      Assert.assertTrue(client.delModel("model"));
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: ERR model key is empty",
          e.getMessage());
    }
  }

  @Test
  public void delScript() {
    ClassLoader classLoader = getClass().getClassLoader();
    String script = classLoader.getResource("test_data/script.txt").getFile();
    Assert.assertTrue(client.setScriptFile("script.test.del", Device.CPU, script));
    Assert.assertTrue(client.delScript("script.test.del"));
    try {
      Assert.assertTrue(client.delScript("script.test.del"));
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      Assert.assertEquals(
          "redis.clients.jedis.exceptions.JedisDataException: ERR script key is empty",
          e.getMessage());
    }
  }

  @Test
  public void getScript() {
    ClassLoader classLoader = getClass().getClassLoader();
    String scriptFilePath = classLoader.getResource("test_data/script.txt").getFile();
    Assert.assertTrue(client.setScriptFile("script.test.get", Device.CPU, scriptFilePath));
    Script script1 = client.getScript("script.test.get");
    Assert.assertTrue(client.setScript("script2.test.get", script1));
    Script script2 = client.getScript("script2.test.get");
    Assert.assertEquals(script1.getDevice(), script2.getDevice());
    Assert.assertEquals(script1.getTag(), script2.getTag());
    Assert.assertEquals(script1.getSource(), script2.getSource());
  }
}
