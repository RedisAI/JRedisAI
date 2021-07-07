package com.redislabs.redisai.manual;

import com.redislabs.redisai.Backend;
import com.redislabs.redisai.Device;
import com.redislabs.redisai.Model;
import com.redislabs.redisai.RedisAI;
import java.io.File;
import java.io.IOException;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.junit.Assert;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Protocol;

public class LargeModelTest {

  public static void main(String[] args) throws Exception {
    bert();
  }

  /**
   * First generate a large model. E.g:
   * https://gist.github.com/lantiga/eea9db320f81de6031c261ead1382464
   *
   * @throws IOException
   */
  private static void bert() throws IOException {
    final String modelPath = "/path/to/traced_bert_qa.pt";
    final String key = "bert";
    JedisPool pool =
        new JedisPool(
            new GenericObjectPoolConfig<>(),
            new HostAndPort(Protocol.DEFAULT_HOST, Protocol.DEFAULT_PORT),
            DefaultJedisClientConfig.builder().socketTimeoutMillis(5000).build());
    delKey(pool, key);
    RedisAI ai = new RedisAI(pool);
    Model model = new Model(Backend.TORCH, Device.CPU, new File(modelPath).toURI());
    Assert.assertTrue(ai.storeModel(modelPath, model));
    delKey(pool, key);
    pool.close();
  }

  private static void delKey(JedisPool pool, String key) {
    try (Jedis j = pool.getResource()) {
      j.del(key);
    }
  }
}
