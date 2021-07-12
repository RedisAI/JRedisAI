package com.redislabs.redisai;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DagV2Test {
  private final JedisPool pool = new JedisPool();
  private final RedisAI client = new RedisAI(pool);

  @Before
  public void setUp() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }

  @After
  public void tearDown() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }

  private void setTfModel(RedisAI client) {
    ClassLoader classLoader = getClass().getClassLoader();
    String modelPath = classLoader.getResource("test_data/graph.pb").getFile();
    byte[] blob = null;
    try {
      blob = Files.readAllBytes(Paths.get(modelPath));
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    String[] inputs = new String[] {"a", "b"};
    String[] outputs = new String[] {"mul"};
    Model model = new Model(Backend.TF, Device.CPU, inputs, outputs, blob);
    Assert.assertTrue(client.storeModel("mul", model));
  }

  /** ai.dagrun simple modelrun tensorget positive testing with load clause */
  @Test
  public void dagrunWithLoadPersist() {
    setTfModel(client);
    Dag dag = new Dag();

    String keyA = "tensorA";
    String keyB = "tensorB";
    Tensor t1 = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Assert.assertTrue(client.setTensor(keyA, t1));
    Assert.assertTrue(client.setTensor(keyB, t1));

    dag.executeModel("mul", new String[] {"tensorA", "tensorB"}, new String[] {"tensorC"});
    dag.getTensor("tensorC");
    List<?> result =
        client.dagExecute(
            new String[] {"tensorA", "tensorB"},
            new String[] {"tensorC"},
            new String[] {"mul"},
            dag);
    float[] expected = new float[] {1, 4};

    Tensor tensorC = (Tensor) result.get(1);
    float[] values = (float[]) tensorC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);

    Tensor tensorCC = client.getTensor("tensorC");
    values = (float[]) tensorCC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);
  }

  @Test
  public void dagExecuteWithoutLoadKeysOrPersist() {
    setTfModel(client);
    Dag dag = new Dag();
    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.executeModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    dag.getTensor(keyC);
    Assert.assertThrows(
        "ERR AI.DAGEXECUTE and AI.DAGEXECUTE_RO commands must contain at least one out of KEYS, LOAD, PERSIST keywords",
        redis.clients.jedis.exceptions.JedisDataException.class,
        () -> client.dagExecute(null, null, null, dag));
  }

  @Test
  public void dagExecuteWithoutKeysOrPersist() {
    setTfModel(client);
    Dag dag = new Dag();
    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.executeModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    dag.getTensor(keyC);
    List<?> result = client.dagExecute(null, null, new String[] {"mul"}, dag);
    float[] expected = new float[] {2, 6};

    Tensor tensorC = (Tensor) result.get(3);
    float[] values = (float[]) tensorC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);
  }

  /**
   * ai.dagExecute simple tensorset modelExecute tensorget positive testing without load or persist
   * clauses
   */
  @Test
  public void dagExecuteReadOnlyWithoutLoad() {
    setTfModel(client);
    Dag dag = new Dag();

    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.executeModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    dag.getTensor(keyC);
    List<?> result = client.dagExecuteReadOnly(null, new String[] {"mul"}, dag);
    float[] expected = new float[] {2, 6};
    Tensor tensorC = (Tensor) result.get(3);
    float[] values = (float[]) tensorC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);
  }

  @Test
  // TODO: Last DAG statement with 'NA' response should work after RedisAI fix
  public void dagExecuteWithExeception() {
    setTfModel(client);
    Dag dag = new Dag();
    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {2, 3}, new float[][] {{1, 2, 3}, {4, 5, 6}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.executeModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    //    dag.getTensor(keyC);
    List<?> result = client.dagExecuteReadOnly(null, new String[] {"mul"}, dag);
    Assert.assertArrayEquals("OK".getBytes(), (byte[]) result.get(0));
    Assert.assertArrayEquals("OK".getBytes(), (byte[]) result.get(1));
    Assert.assertEquals(RedisAIException.class, result.get(2).getClass());
    //    Assert.assertArrayEquals("NA".getBytes(), (byte[]) result.get(3));
  }

  /** ai.dagrun test with tensorset modelrun tensorget scriptrun tensorget */
  // @Test
  // public void dagRunWithAllCommands() {
  //   Dag dag = new Dag();

  //   String keyA = "tensorA";
  //   String keyB = "tensorB";
  //   String keyC = "tensorC";
  //   String keyD = "tensorD";
  //   String keyE = "tensorE";
  //   Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
  //   Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
  //   Tensor tD = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{5, 5}});
  //   dag.setTensor(keyA, tA);
  //   dag.setTensor(keyB, tB);
  //   dag.setTensor(keyD, tD);
  //   dag.runModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
  //   dag.getTensor(keyC);
  //   dag.runScript("script", "bar", new String[] {keyC, keyD}, new String[] {keyE});
  //   dag.getTensor(keyE);
  //   List<?> result = client.dagRun(null, null, dag);
  //   float[] expected = new float[] {2, 6};
  //   Tensor tensorC = (Tensor) result.get(4);
  //   float[] values = (float[]) tensorC.getValues();
  //   Assert.assertEquals("Assert same shape of values", 2, values.length);
  //   Assert.assertArrayEquals(expected, values, (float) 0.1);

  //   float[] expectedSum = new float[] {7, 11};
  //   Tensor tensorE = (Tensor) result.get(6);
  //   float[] valuesSum = (float[]) tensorE.getValues();
  //   Assert.assertEquals("Assert same shape of values", 2, valuesSum.length);
  //   Assert.assertArrayEquals(expectedSum, valuesSum, (float) 0.1);
  // }

  /** ai.dagrun test with tensorset modelrun tensorget scriptrun tensorget */
  //   @Test
  //   public void dagRunWithAllCommandsChained() {
  //     ClassLoader classLoader = getClass().getClassLoader();
  //     String model = classLoader.getResource("test_data/graph.pb").getFile();
  //     String[] inputs = new String[] {"a", "b"};
  //     String[] outputs = new String[] {"mul"};
  //     Assert.assertTrue(client.setModel("mul", Backend.TF, Device.CPU, inputs, outputs, model));

  //     String scriptFile = classLoader.getResource("test_data/script.txt").getFile();
  //     Assert.assertTrue(client.setScriptFile("script", Device.CPU, scriptFile));
  //     Dag dag = new Dag();

  //     String keyA = "tensorA";
  //     String keyB = "tensorB";
  //     String keyC = "tensorC";
  //     String keyD = "tensorD";
  //     String keyE = "tensorE";
  //     Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
  //     Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
  //     Tensor tD = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{5, 5}});
  //     dag.setTensor(keyA, tA)
  //         .setTensor(keyB, tB)
  //         .setTensor(keyD, tD)
  //         .runModel("mul", new String[] {keyA, keyB}, new String[] {keyC})
  //         .getTensor(keyC)
  //         .runScript("script", "bar", new String[] {keyC, keyD}, new String[] {keyE})
  //         .getTensor(keyE);
  //     List<?> result = client.dagRun(null, null, dag);
  //     float[] expected = new float[] {2, 6};
  //     Tensor tensorC = (Tensor) result.get(4);
  //     float[] values = (float[]) tensorC.getValues();
  //     Assert.assertEquals("Assert same shape of values", 2, values.length);
  //     Assert.assertArrayEquals(expected, values, (float) 0.1);

  //     float[] expectedSum = new float[] {7, 11};
  //     Tensor tensorE = (Tensor) result.get(6);
  //     float[] valuesSum = (float[]) tensorE.getValues();
  //     Assert.assertEquals("Assert same shape of values", 2, valuesSum.length);
  //     Assert.assertArrayEquals(expectedSum, valuesSum, (float) 0.1);
  //   }
}
