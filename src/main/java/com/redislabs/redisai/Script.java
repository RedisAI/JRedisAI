package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import redis.clients.jedis.util.SafeEncoder;

public class Script {

  /** the device that will execute the model. can be of CPU or GPU */
  private Device device; // TODO: final

  /** a string containing TorchScript source code */
  private String source; // TODO: final

  /**
   * tag is an optional string for tagging the model such as a version number or any arbitrary
   * identifier
   */
  private String tag = null; // TODO: final

  /** @param device the device that will execute the model. can be of CPU or GPU */
  @Deprecated
  public Script(Device device) {
    this(device, null, "");
  }

  /**
   * @param device the device that will execute the model. can be of CPU or GPU
   * @param source a string containing TorchScript source code
   */
  public Script(Device device, String source) {
    this(device, null, source);
  }

  public Script(Device device, String tag, String source) {
    this.device = device;
    this.tag = tag;
    this.source = source;
  }

  /**
   * Constructor given the device string and the Path containing the script
   *
   * @param device the device that will execute the model. can be of CPU or GPU
   * @param filePath file path to load the script from
   * @throws java.io.IOException
   */
  public Script(Device device, Path filePath) throws IOException {
    this(device, fileContent(filePath));
  }

  private static String fileContent(Path filePath) throws IOException {
    return Files.readAllLines(filePath, StandardCharsets.UTF_8).stream()
            .collect(Collectors.joining("\n"))
        + "\n";
  }

  public static Script createScriptFromRespReply(List<?> reply) {
    Device device = null;
    String tag = null;
    String source = null;
    for (int i = 0; i < reply.size(); i += 2) {
      String arrayKey = SafeEncoder.encode((byte[]) reply.get(i));
      switch (arrayKey) {
        case "source":
          source = SafeEncoder.encode((byte[]) reply.get(i + 1));
          break;
        case "device":
          String deviceString = SafeEncoder.encode((byte[]) reply.get(i + 1));
          device = Device.valueOf(deviceString);
          break;
        case "tag":
          tag = SafeEncoder.encode((byte[]) reply.get(i + 1));
          break;
        default:
          break;
      }
    }
    if (device != null && source != null) {
      return new Script(device, tag, source);
    }
    throw new JRedisAIRunTimeException(
        "AI.SCRIPTGET reply did not contained all elements to build the script");
  }

  public Device getDevice() {
    return device;
  }

  @Deprecated
  public void setDevice(Device device) {
    this.device = device;
  }

  public String getSource() {
    return source;
  }

  @Deprecated
  public void setSource(String source) {
    this.source = source;
  }

  public String getTag() {
    return tag;
  }

  @Deprecated
  public void setTag(String tag) {
    this.tag = tag;
  }

  /**
   * Encodes the current script into an AI.SCRIPTSET command to be store in RedisAI Server
   *
   * @param key name of key to store the Script
   * @return
   */
  protected List<byte[]> getScriptSetCommandBytes(String key) {
    List<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(key));
    args.add(device.getRaw());
    if (tag != null) {
      args.add(Keyword.TAG.getRaw());
      args.add(SafeEncoder.encode(tag));
    }
    args.add(Keyword.SOURCE.getRaw());
    args.add(SafeEncoder.encode(source));
    return args;
  }

  /**
   * sets the Script source give a filePath
   *
   * @param filePath
   * @throws IOException
   * @deprecated Use {@link #Script(com.redislabs.redisai.Device, java.nio.file.Path)}.
   */
  @Deprecated
  public void readSourceFromFile(String filePath) throws IOException {
    this.source = fileContent(Paths.get(filePath));
  }

  protected static List<byte[]> scriptRunFlatArgs(
      String key, String function, String[] inputs, String[] outputs, boolean includeCommandName) {
    List<byte[]> args = new ArrayList<>();
    if (includeCommandName) {
      args.add(Command.SCRIPT_RUN.getRaw());
    }
    args.add(SafeEncoder.encode(key));
    args.add(SafeEncoder.encode(function));
    args.add(Keyword.INPUTS.getRaw());
    for (String input : inputs) {
      args.add(SafeEncoder.encode(input));
    }

    args.add(Keyword.OUTPUTS.getRaw());
    for (String output : outputs) {
      args.add(SafeEncoder.encode(output));
    }
    return args;
  }
}
