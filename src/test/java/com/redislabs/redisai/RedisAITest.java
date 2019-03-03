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
    Assert.assertTrue(client.setModel("model", Backend.TF, Device.CPU, new String[] {"input"}, new String[] {"target"}, model));
//    client.getModel("model");
  }

  @Test
  public void testRunModel() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("graph.pb").getFile();
    client.setModel("model", Backend.TF, Device.CPU, new String[] {"input"}, new String[] {"target"}, model);
    client.setTensor("input", new int[]{1}, new int[] {1});

    Assert.assertTrue(client.runModel("model", new String[] {"input"}, new String[] {"target"}));
  }
  
  
  @Test
  public void testSeScript() {
    ClassLoader classLoader = getClass().getClassLoader();
    String script = classLoader.getResource("script.txt").getFile();
    Assert.assertTrue(client.setScript("script", Device.CPU, script));
//    client.getScript("script");
  }
  
  @Test
  public void testRunScript() {
    ClassLoader classLoader = getClass().getClassLoader();
    String script = classLoader.getResource("script.txt").getFile();
    client.setScript("script", Device.CPU, script);
    client.setTensor("input", new int[]{1}, new int[] {1});

    Assert.assertTrue(client.runScript("model", new String[] {"input"}, new String[] {"target"}));
  }
}
