package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

    @Test
    public void getSetTag() {
        Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
        String tag = model.getTag();
        Assert.assertEquals(tag, "");
        model.setTag("tagExample");
        tag = model.getTag();
        Assert.assertEquals(tag, "tagExample");
    }

    @Test
    public void getBlob() {
    }

    @Test
    public void setBlob() {
    }

    @Test
    public void getSetOutputs() {
        Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
        String[] outputs = model.getOutputs();
        Assert.assertArrayEquals(outputs, new String[0]);
        model.setOutputs(new String[]{"out1"});
        outputs = model.getOutputs();
        Assert.assertArrayEquals(outputs, new String[]{"out1"});
    }


    @Test
    public void getSetInputs() {
        Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
        String[] inputs = model.getInputs();
        Assert.assertArrayEquals(inputs, new String[0]);
        model.setInputs(new String[]{"in1"});
        inputs = model.getInputs();
        Assert.assertArrayEquals(inputs, new String[]{"in1"});
    }


    @Test
    public void getSetDevice() {
        Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
        Device device = model.getDevice();
        Assert.assertEquals(Device.GPU, device);
        model.setDevice(Device.CPU);
        device = model.getDevice();
        Assert.assertEquals(Device.CPU, device);
    }


    @Test
    public void getSetBackend() {
        Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
        Backend backend = model.getBackend();
        Assert.assertEquals(Backend.ONNX, backend);
        model.setBackend(Backend.TF);
        backend = model.getBackend();
        Assert.assertEquals(Backend.TF, backend);
    }

}
