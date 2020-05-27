package com.redislabs.redisai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Collectors;
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
}
