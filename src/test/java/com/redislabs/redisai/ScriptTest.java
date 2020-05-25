package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Test;

public class ScriptTest {

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
        Assert.assertEquals(source, "");
        script.setSource("def func a:");
        source = script.getSource();
        Assert.assertEquals(source, "def func a:");
    }

    @Test
    public void setGetTag() {
        Script script = new Script(Device.GPU);
        String tag = script.getTag();
        Assert.assertEquals(null, tag);
        script.setTag("tagExample");
        tag = script.getTag();
        Assert.assertEquals(tag, "tagExample");
    }
}
