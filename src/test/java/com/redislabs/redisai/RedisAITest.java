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
  public void testSet() {
    Assert.assertTrue(client.setTensor("source", new int[][] {{1,2},{3,4}}, new int[] {2,2}));
    Assert.assertNotNull(client.getTensor("source"));
  }


}
