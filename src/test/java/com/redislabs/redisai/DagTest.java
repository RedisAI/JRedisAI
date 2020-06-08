package com.redislabs.redisai;

import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

public class DagTest {
  private final JedisPool pool = new JedisPool();
  private final RedisAI client = new RedisAI(pool);

  @Before
  public void setUp() {
    try (Jedis conn = pool.getResource()) {
      conn.flushAll();
    }
  }
  //
  //    @After
  //    public void tearDown(){
  //        try (Jedis conn = pool.getResource()) {
  //            conn.flushAll();
  //        }
  //    }

  /** ai.dagrun simple modelrun tensorget positive testing with load clause */
  @Test
  public void dagrunWithLoadPersist() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    String[] inputs = new String[] {"a", "b"};
    String[] outputs = new String[] {"mul"};
    Assert.assertTrue(client.setModel("mul", Backend.TF, Device.CPU, inputs, outputs, model));
    Dag dag = new Dag();

    String keyA = "tensorA";
    String keyB = "tensorB";
    Tensor t1 = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Assert.assertTrue(client.setTensor(keyA, t1));
    Assert.assertTrue(client.setTensor(keyB, t1));

    dag.runModel("mul", new String[] {"tensorA", "tensorB"}, new String[] {"tensorC"});
    dag.getTensor("tensorC");
    List<?> result =
        client.dagRun(new String[] {"tensorA", "tensorB"}, new String[] {"tensorC"}, dag);
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

  /**
   * ai.dagrun simple tensorset modelrun tensorget positive testing without load or persist clauses
   */
  @Test
  public void dagrunWithoutLoadOrPersist() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    String[] inputs = new String[] {"a", "b"};
    String[] outputs = new String[] {"mul"};
    Assert.assertTrue(client.setModel("mul", Backend.TF, Device.CPU, inputs, outputs, model));
    Dag dag = new Dag();

    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.runModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    dag.getTensor(keyC);
    List<?> result = client.dagRun(null, null, dag);
    float[] expected = new float[] {2, 6};

    Tensor tensorC = (Tensor) result.get(3);
    float[] values = (float[]) tensorC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);
  }

  /**
   * ai.dagrun simple tensorset modelrun tensorget positive testing without load or persist clauses
   */
  @Test
  public void dagRunReadOnlyWithoutLoad() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    String[] inputs = new String[] {"a", "b"};
    String[] outputs = new String[] {"mul"};
    Assert.assertTrue(client.setModel("mul", Backend.TF, Device.CPU, inputs, outputs, model));
    Dag dag = new Dag();

    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.runModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    dag.getTensor(keyC);
    List<?> result = client.dagRunReadOnly(null, dag);
    float[] expected = new float[] {2, 6};
    Tensor tensorC = (Tensor) result.get(3);
    float[] values = (float[]) tensorC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);
  }

  /** ai.dagrun test with tensorset modelrun tensorget scriptrun tensorget */
  @Test
  public void dagRunWithAllCommands() {
    ClassLoader classLoader = getClass().getClassLoader();
    String model = classLoader.getResource("test_data/graph.pb").getFile();
    String[] inputs = new String[] {"a", "b"};
    String[] outputs = new String[] {"mul"};
    Assert.assertTrue(client.setModel("mul", Backend.TF, Device.CPU, inputs, outputs, model));

    String scriptFile = classLoader.getResource("test_data/script.txt").getFile();
    Assert.assertTrue(client.setScriptFile("script", Device.CPU, scriptFile));
    Dag dag = new Dag();

    String keyA = "tensorA";
    String keyB = "tensorB";
    String keyC = "tensorC";
    String keyD = "tensorD";
    String keyE = "tensorE";
    Tensor tA = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{1, 2}});
    Tensor tB = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{2, 3}});
    Tensor tD = new Tensor(DataType.FLOAT, new long[] {1, 2}, new float[][] {{5, 5}});
    dag.setTensor(keyA, tA);
    dag.setTensor(keyB, tB);
    dag.setTensor(keyD, tD);
    dag.runModel("mul", new String[] {keyA, keyB}, new String[] {keyC});
    dag.getTensor(keyC);
    dag.runScript("script", "bar", new String[] {keyC, keyD}, new String[] {keyE});
    dag.getTensor(keyE);
    List<?> result = client.dagRun(null, null, dag);
    float[] expected = new float[] {2, 6};
    Tensor tensorC = (Tensor) result.get(4);
    float[] values = (float[]) tensorC.getValues();
    Assert.assertEquals("Assert same shape of values", 2, values.length);
    Assert.assertArrayEquals(expected, values, (float) 0.1);

    float[] expectedSum = new float[] {7, 11};
    Tensor tensorE = (Tensor) result.get(6);
    float[] valuesSum = (float[]) tensorE.getValues();
    Assert.assertEquals("Assert same shape of values", 2, valuesSum.length);
    Assert.assertArrayEquals(expectedSum, valuesSum, (float) 0.1);
  }
}
