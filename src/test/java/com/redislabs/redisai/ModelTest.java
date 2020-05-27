package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Test;

public class ModelTest {

  @Test
  public void getSetTag() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    String tag = model.getTag();
    Assert.assertEquals(null, tag);
    model.setTag("tagExample");
    tag = model.getTag();
    Assert.assertEquals(tag, "tagExample");
  }

  @Test
  public void getSetBlob() {
    byte[] expected = new byte[0];
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], expected);
    byte[] blob = model.getBlob();
    Assert.assertEquals(blob, expected);
    byte[] expected2 = new byte[] {0x10};
    model.setBlob(expected2);
    blob = model.getBlob();
    Assert.assertEquals(blob, expected2);
  }

  @Test
  public void getSetOutputs() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    String[] outputs = model.getOutputs();
    Assert.assertArrayEquals(outputs, new String[0]);
    model.setOutputs(new String[] {"out1"});
    outputs = model.getOutputs();
    Assert.assertArrayEquals(outputs, new String[] {"out1"});
  }

  @Test
  public void getSetInputs() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    String[] inputs = model.getInputs();
    Assert.assertArrayEquals(inputs, new String[0]);
    model.setInputs(new String[] {"in1"});
    inputs = model.getInputs();
    Assert.assertArrayEquals(inputs, new String[] {"in1"});
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

  @Test
  public void getSetBatchSize() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    long batchsize = model.getBatchSize();
    Assert.assertEquals(0, batchsize);
    model.setBatchSize(10);
    batchsize = model.getBatchSize();
    Assert.assertEquals(10, batchsize);
  }

  @Test
  public void getSetMinBatchSize() {
    Model model = new Model(Backend.ONNX, Device.GPU, new String[0], new String[0], new byte[0]);
    long minbatchsize = model.getMinBatchSize();
    Assert.assertEquals(0, minbatchsize);
    model.setMinBatchSize(10);
    minbatchsize = model.getMinBatchSize();
    Assert.assertEquals(10, minbatchsize);
  }
}
