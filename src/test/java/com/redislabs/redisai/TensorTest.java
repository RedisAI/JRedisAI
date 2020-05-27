package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.SafeEncoder;

public class TensorTest {

  private final JedisPool pool = new JedisPool();
  private final RedisAI redisaiClient = new RedisAI(pool);

  @Before
  public void flushAI() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }

  @Test
  public void getValues() {
    Tensor tensor = new Tensor(DataType.INT32, new long[] {1, 2}, new int[] {3, 4});
    int[] values = (int[]) tensor.getValues();
    Assert.assertArrayEquals(values, new int[] {3, 4});
  }

  @Test
  public void setValues() {
    Tensor tensor = new Tensor(DataType.INT32, new long[] {1, 2}, new int[] {3, 4});
    int[] values = (int[]) tensor.getValues();
    Assert.assertArrayEquals(values, new int[] {3, 4});
    tensor.setValues(new int[] {6, 8});
    values = (int[]) tensor.getValues();
    Assert.assertArrayEquals(values, new int[] {6, 8});
  }

  @Test
  public void getShape() {
    Tensor tensor = new Tensor(DataType.INT32, new long[] {1, 2}, new int[] {3, 4});
    long[] values = tensor.getShape();
    Assert.assertArrayEquals(values, new long[] {1, 2});
  }

  @Test
  public void getDataType() {
    Tensor tensor = new Tensor(DataType.INT32, new long[] {1, 2}, new int[] {3, 4});
    DataType dtype = tensor.getDataType();
    Assert.assertEquals(dtype, DataType.INT32);
  }

  @Test
  public void setDataType() {
    Tensor tensor = new Tensor(DataType.INT32, new long[] {1, 2}, new int[] {3, 4});
    tensor.setDataType(DataType.INT64);
    DataType dtype = tensor.getDataType();
    Assert.assertEquals(dtype, DataType.INT64);
  }

  @Test
  public void createTensorFromRespReply() {
    // negative testing
    try {
      Jedis conn = pool.getResource();
      BinaryClient vanillaClient = conn.getClient();
      String key = "negativeTest:parser:tensor";
      Tensor t1 = new Tensor(DataType.FLOAT, new long[] {2, 2}, new float[][] {{1, 2}, {3, 4}});
      Assert.assertTrue(redisaiClient.setTensor(key, t1));

      vanillaClient.sendCommand(Command.TENSOR_GET, SafeEncoder.encode(key), Keyword.META.getRaw());
      List<?> reply = vanillaClient.getObjectMultiBulkReply();
      Tensor.createTensorFromRespReply(reply);
      Assert.fail("Should throw JRedisAIRunTimeException or JedisDataException");
    } catch (JRedisAIRunTimeException e) {
      Assert.assertEquals(
          "AI.TENSORGET reply did not contained all elements to build the tensor", e.getMessage());
    }
  }
}
