package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.util.SafeEncoder;

public class ScriptTest {

  private final JedisPool pool = new JedisPool();
  private final RedisAI redisaiClient = new RedisAI(pool);

  @Before
  public void flushAI() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }

  @Test
  public void setGetDevice() {
    Script script = new Script(Device.GPU);
    Device device = script.getDevice();
    Assert.assertEquals(Device.GPU, device);
    script.setDevice(Device.CPU);
    device = script.getDevice();
    Assert.assertEquals(Device.CPU, device);
  }

  @Test
  public void setGetSource() {
    Script script = new Script(Device.GPU);
    String source = script.getSource();
    Assert.assertEquals("", source);
    script.setSource("def func a:");
    source = script.getSource();
    Assert.assertEquals("def func a:", source);
  }

  @Test
  public void setGetTag() {
    Script script = new Script(Device.GPU);
    String tag = script.getTag();
    Assert.assertEquals(null, tag);
    script.setTag("tagExample");
    tag = script.getTag();
    Assert.assertEquals("tagExample", tag);
  }

  @Test
  public void constructorsTest() throws IOException {
    ClassLoader classLoader = getClass().getClassLoader();
    String pathString = classLoader.getResource("test_data/script.txt").getFile();
    Path scriptFilePath = Paths.get(pathString);
    String scriptSource =
        Files.readAllLines(scriptFilePath, StandardCharsets.UTF_8).stream()
                .collect(Collectors.joining("\n"))
            + "\n";

    // Default constructor
    Script script = new Script(Device.GPU);
    script.readSourceFromFile(pathString);

    // Overloaded constructor 1
    Script scriptC1 = new Script(Device.GPU, scriptSource);
    Assert.assertEquals(script.getSource(), scriptC1.getSource());

    // Overloaded constructor 2
    Script scriptC2 = new Script(Device.GPU, scriptFilePath);
    Assert.assertEquals(script.getSource(), scriptC2.getSource());
    Assert.assertEquals(scriptC1.getSource(), scriptC2.getSource());
  }

  @Test
  public void createScriptFromRespReply() {
    // negative testing
    try {
      Jedis conn = pool.getResource();
      BinaryClient vanillaClient = conn.getClient();
      String key = "negativeTest:parser:script";
      String script = "def bar(a, b):\n" + "    return a + b\n";
      Assert.assertTrue(redisaiClient.setScript(key, Device.CPU, script));

      vanillaClient.sendCommand(Command.SCRIPT_GET, SafeEncoder.encode(key), Keyword.META.getRaw());
      List<?> reply = vanillaClient.getObjectMultiBulkReply();
      Script.createScriptFromRespReply(reply);
      Assert.fail("Should throw JRedisAIRunTimeException or JedisDataException");
    } catch (JRedisAIRunTimeException e) {
      Assert.assertEquals(
          "AI.SCRIPTGET reply did not contained all elements to build the script", e.getMessage());
    }
  }
}
