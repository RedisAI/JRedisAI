package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
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
        Assert.assertTrue(client.setTensor("t1", new float[][]{{1, 2}, {3, 4}}, new int[]{2, 2}));
//    client.getTensor("t1");
    }

    @Test
    public void testSetModel() {
        ClassLoader classLoader = getClass().getClassLoader();
        String model = classLoader.getResource("test_data/graph.pb").getFile();
        Assert.assertTrue(client.setModel("model", Backend.TF, Device.CPU, new String[]{"a", "b"}, new String[]{"mul"}, model));
        Model m1 = client.getModel("model");
        Assert.assertEquals(Device.CPU, m1.getDevice());
        Assert.assertEquals(Backend.TF, m1.getBackend());
    }

    @Test
    public void testSetModelFromModelOnnx() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String modelPath = classLoader.getResource("test_data/mnist.onnx").getFile();
            byte[] blob = Files.readAllBytes(Paths.get(modelPath));
            Model m1 = new Model(Backend.ONNX, Device.CPU, new String[]{}, new String[]{}, blob);
            Assert.assertTrue(client.setModel("mnist.onnx", m1));
            Model m2 = client.getModel("mnist.onnx");
            Assert.assertEquals(m1.getDevice(), m2.getDevice());
            Assert.assertEquals(m1.getBackend(), m2.getBackend());
            Assert.assertTrue(client.setModel("mnist.onnx.m2", m2));
            Model m3 = client.getModel("mnist.onnx.m2");
            Assert.assertEquals(m2.getDevice(), m3.getDevice());
            Assert.assertEquals(m2.getBackend(), m3.getBackend());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testSetModelFromModelTFLite() {
        try {
            ClassLoader classLoader = getClass().getClassLoader();
            String modelPath = classLoader.getResource("test_data/mnist_model_quant.tflite").getFile();
            byte[] blob = Files.readAllBytes(Paths.get(modelPath));
            Model m1 = new Model(Backend.TFLITE, Device.CPU, new String[]{}, new String[]{}, blob);
            Assert.assertTrue(client.setModel("mnist.tflite", m1));
            Model m2 = client.getModel("mnist.tflite");
            Assert.assertEquals(m1.getDevice(), m2.getDevice());
            Assert.assertEquals(m1.getBackend(), m2.getBackend());
            Assert.assertTrue(client.setModel("mnist.tflite.m2", m2));
            Model m3 = client.getModel("mnist.tflite.m2");
            Assert.assertEquals(m2.getDevice(), m3.getDevice());
            Assert.assertEquals(m2.getBackend(), m3.getBackend());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRunModel() {
        ClassLoader classLoader = getClass().getClassLoader();
        String model = classLoader.getResource("test_data/graph.pb").getFile();
        client.setModel("model", Backend.TF, Device.CPU, new String[]{"a", "b"}, new String[]{"mul"}, model);

        client.setTensor("a", new float[]{2, 3}, new int[]{2});
        client.setTensor("b", new float[]{3, 5}, new int[]{2});

        Assert.assertTrue(client.runModel("model", new String[]{"a", "b"}, new String[]{"c"}));
        Tensor tensor = client.getTensor("c");
        float[] values = (float[]) tensor.getValues();
        float[] expected = new float[]{6, 15};
        Assert.assertTrue("Assert same shape of values", values.length == 2);
        Assert.assertArrayEquals(values, expected, (float) 0.1);
    }

    @Test
    public void testSeScriptFile() {
        ClassLoader classLoader = getClass().getClassLoader();
        String scriptFile = classLoader.getResource("test_data/script.txt").getFile();
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
        String script = classLoader.getResource("test_data/script.txt").getFile();
        client.setScriptFile("script", Device.CPU, script);

        client.setTensor("a1", new float[]{2, 3}, new int[]{2});
        client.setTensor("b1", new float[]{2, 3}, new int[]{2});

        Assert.assertTrue(client.runScript("script", "bar", new String[]{"a1", "b1"}, new String[]{"c1"}));
    }

    @Test
    public void testGetTensor() {
        Assert.assertTrue(client.setTensor("t1", new float[][]{{1, 2}, {3, 4}}, new int[]{2, 2}));
        Tensor tensor = client.getTensor("t1");
        float[] values = (float[]) tensor.getValues();
        Assert.assertTrue("Assert same shape of values", values.length == 4);
        float[] expected = new float[]{1, 2, 3, 4};
        Assert.assertArrayEquals(values, expected, (float) 0.1);
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

        client.setTensor("a1", new float[]{2, 3}, new int[]{2});
        client.setTensor("b1", new float[]{2, 3}, new int[]{2});
        Assert.assertTrue(client.runScript(key, "bar", new String[]{"a1", "b1"}, new String[]{"c1"}));

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
    public void delModel() {
        ClassLoader classLoader = getClass().getClassLoader();
        String model = classLoader.getResource("test_data/graph.pb").getFile();
        Assert.assertTrue(client.setModel("model", Backend.TF, Device.CPU, new String[]{"a", "b"}, new String[]{"mul"}, model));
        Assert.assertTrue(client.delModel("model"));
        try {
            Assert.assertTrue(client.delModel("model"));
            Assert.fail("Should throw JedisDataException");
        } catch (RedisAIException e) {
            Assert.assertEquals("redis.clients.jedis.exceptions.JedisDataException: ERR model key is empty", e.getMessage());
        }
    }

    @Test
    public void delScript() {
        ClassLoader classLoader = getClass().getClassLoader();
        String script = classLoader.getResource("test_data/script.txt").getFile();
        Assert.assertTrue(client.setScriptFile("script.test.del", Device.CPU, script));
        Assert.assertTrue(client.delScript("script.test.del"));
        try {
            Assert.assertTrue(client.delScript("script.test.del"));
            Assert.fail("Should throw JedisDataException");
        } catch (RedisAIException e) {
            Assert.assertEquals("redis.clients.jedis.exceptions.JedisDataException: ERR script key is empty", e.getMessage());
        }
    }

    @Test
    public void getScript() {
        ClassLoader classLoader = getClass().getClassLoader();
        String scriptFilePath = classLoader.getResource("test_data/script.txt").getFile();
        Assert.assertTrue(client.setScriptFile("script.test.get", Device.CPU, scriptFilePath));
        Script script1 = client.getScript("script.test.get");
        Assert.assertTrue(client.setScript("script2.test.get", script1));
        Script script2 = client.getScript("script2.test.get");
        Assert.assertEquals(script1.getDevice(), script2.getDevice());
        Assert.assertEquals(script1.getTag(), script2.getTag());
        Assert.assertEquals(script1.getSource(), script2.getSource());
    }
}
