package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

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
    client.setTensor("b", new float[] {2, 3}, new int[]{2});

    Assert.assertTrue(client.runModel("model", new String[] {"a", "b"}, new String[] {"c"}));
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
}
