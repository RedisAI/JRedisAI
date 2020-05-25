package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.Map;

public class RedisAITest {

  private final JedisPool pool = new JedisPool();
  private final RedisAI client = new RedisAI(pool); 
  
  @Before
  public void testClient() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }      
  }
  
  @Test
  public void testSetTensor() {
    Assert.assertTrue(client.setTensor("t1", new float[][] {{1,2},{3,4}}, new int[] {2,2}));
//    client.getTensor("t1");
  }

  @Test
  public void testSetModel() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("graph.pb").getFile();
    Assert.assertTrue(client.setModel("model", Backend.TF, Device.CPU, new String[] {"a", "b"}, new String[] {"mul"}, model));
//    client.getModel("model");
  }

  @Test
  public void testRunModel() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("graph.pb").getFile();
    client.setModel("model", Backend.TF, Device.CPU, new String[] {"a", "b"}, new String[] {"mul"}, model);
    
    client.setTensor("a", new float[] {2, 3}, new int[]{2});
    client.setTensor("b", new float[] {3, 5}, new int[]{2});

    Assert.assertTrue(client.runModel("model", new String[] {"a", "b"}, new String[] {"c"}));
    Tensor tensor = client.getTensor("c");
    float[] values = (float[]) tensor.getValues();
    float[] expected =  new float[] {6, 15};
    Assert.assertTrue("Assert same shape of values", values.length==2);
    Assert.assertArrayEquals(values,expected, (float) 0.1);
  }

  @Test
  public void testSeScriptFile() {
    ClassLoader classLoader = getClass().getClassLoader();
    String scriptFile = classLoader.getResource("script.txt").getFile();
    Assert.assertTrue(client.setScriptFile("script", Device.CPU, scriptFile));
  }
  
  @Test
  public void testSeScript() {
    String script = "def bar(a, b):\n" + 
    "    return a + b\n";
    Assert.assertTrue(client.setScript("script", Device.CPU, script));
//    client.getScript("script");
  }
  
  @Test
  public void testRunScript() {
    ClassLoader classLoader = getClass().getClassLoader();
    String script = classLoader.getResource("script.txt").getFile();
    client.setScriptFile("script", Device.CPU, script);
    
    client.setTensor("a1", new float[] {2, 3}, new int[]{2});
    client.setTensor("b1", new float[] {2, 3}, new int[]{2});
    
    Assert.assertTrue(client.runScript("script", "bar", new String[] {"a1", "b1"}, new String[] {"c1"}));
  }

  @Test
  public void testGetTensor() {
    Assert.assertTrue(client.setTensor("t1", new float[][] {{1,2},{3,4}}, new int[] {2,2}));
    Tensor tensor = client.getTensor("t1");
    float[] values = (float[]) tensor.getValues();
    Assert.assertTrue("Assert same shape of values", values.length==4);
    float[] expected =  new float[] {1,2,3,4};
    Assert.assertArrayEquals(values,expected, (float) 0.1);
  }

  @Test
  public void testInfo() {
    String key = "test:info:script";
    String script = "def bar(a, b):\n" +
            "    return a + b\n";
    Assert.assertTrue(client.setScript(key, Device.CPU, script));

    // not exist
    Map<String, Object> infoMap = null;
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

    client.setTensor("a1", new float[] {2, 3}, new int[]{2});
    client.setTensor("b1", new float[] {2, 3}, new int[]{2});
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
  public void testConfig() {
    Assert.assertTrue(client.setBackendsPath("/usr/lib/redis/modules/backends/"));
    try {
      client.loadBackend(Backend.TF, "notexist/redisai_tensorflow.so");
      Assert.fail("Should throw JedisDataException");
    } catch (RedisAIException e) {
      // ERR error loading backend
    }
    // will throw error if backend already loaded
    Assert.assertTrue(client.loadBackend(Backend.TF, "redisai_tensorflow/redisai_tensorflow.so"));
  }
}
