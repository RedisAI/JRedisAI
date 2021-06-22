package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.SafeEncoder;

public class ModelTest {

  private final JedisPool pool = new JedisPool();
  private final RedisAI redisaiClient = new RedisAI(pool);

  @Before
  public void flushAI() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }

  @Test
  public void getSetTag() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    String tag = model.getTag();
    Assert.assertEquals(null, tag);
    model.setTag("tagExample");
    tag = model.getTag();
    Assert.assertEquals("tagExample", tag);
  }

  @Test
  public void getSetBlob() {
    byte[] expected = new byte[0];
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], expected);
    byte[] blob = model.getBlob();
    Assert.assertSame(blob, expected);
    byte[] expected2 = new byte[] {0x10};
    model.setBlob(expected2);
    blob = model.getBlob();
    Assert.assertSame(blob, expected2);
  }

  @Test
  public void getSetOutputs() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    String[] outputs = model.getOutputs();
    Assert.assertArrayEquals(outputs, new String[0]);
    model.setOutputs(new String[] {"out1"});
    outputs = model.getOutputs();
    Assert.assertArrayEquals(outputs, new String[] {"out1"});
  }

  @Test
  public void getSetInputs() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    String[] inputs = model.getInputs();
    Assert.assertArrayEquals(inputs, new String[0]);
    model.setInputs(new String[] {"in1"});
    inputs = model.getInputs();
    Assert.assertArrayEquals(inputs, new String[] {"in1"});
  }

  @Test
  public void getSetDevice() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    Device device = model.getDevice();
    Assert.assertEquals(Device.GPU, device);
    model.setDevice(Device.CPU);
    device = model.getDevice();
    Assert.assertEquals(Device.CPU, device);
  }

  @Test
  public void getSetBackend() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    Backend backend = model.getBackend();
    Assert.assertEquals(Backend.ONNX, backend);
    model.setBackend(Backend.TF);
    backend = model.getBackend();
    Assert.assertEquals(Backend.TF, backend);
  }

  @Test
  public void getSetBatchSize() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    long batchsize = model.getBatchSize();
    Assert.assertEquals(0, batchsize);
    model.setBatchSize(10);
    batchsize = model.getBatchSize();
    Assert.assertEquals(10, batchsize);
  }

  @Test
  public void getSetMinBatchSize() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    long minbatchsize = model.getMinBatchSize();
    Assert.assertEquals(0, minbatchsize);
    model.setMinBatchSize(10);
    minbatchsize = model.getMinBatchSize();
    Assert.assertEquals(10, minbatchsize);
  }

  /**
   * @throws java.net.URISyntaxException
   * @throws java.io.IOException
   * @see ChunkTest#argumentsWithChunking()
   */
  @Test
  public void argumentsWithoutChunking() throws URISyntaxException, IOException {
    Model model =
        new Model(
            Backend.ONNX,
            Device.GPU,
            getClass().getClassLoader().getResource("test_data/mnist.onnx").toURI());

    Assert.assertEquals(5, model.getModelStoreCommandArgs("key").size());
  }

  @Test
  public void createModelFromRespReply() {
    // negative testing
    try {
      Jedis conn = pool.getResource();
      BinaryClient vanillaClient = conn.getClient();
      String key = "negativeTest:parser:model";
      ClassLoader classLoader = getClass().getClassLoader();
      String model = classLoader.getResource("test_data/graph.pb").getFile();
      Assert.assertTrue(
          redisaiClient.setModel(
              key, Backend.TF, Device.CPU, new String[] {"a", "b"}, new String[] {"mul"}, model));

      vanillaClient.sendCommand(Command.MODEL_GET, SafeEncoder.encode(key), Keyword.META.getRaw());
      List<?> reply = vanillaClient.getObjectMultiBulkReply();
      Model.createModelFromRespReply(reply);
      Assert.fail("Should throw JRedisAIRunTimeException or JedisDataException");
    } catch (JRedisAIRunTimeException e) {
      Assert.assertEquals(
          "AI.MODELGET reply did not contained all elements to build the model", e.getMessage());
    }
  }
}
