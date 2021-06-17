package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Command implements ProtocolCommand {
  TENSOR_GET("AI.TENSORGET"),
  TENSOR_SET("AI.TENSORSET"),
  MODEL_GET("AI.MODELGET"),
  MODEL_SET("AI.MODELSET"),
  MODEL_STORE("AI.MODELSTORE"),
  MODEL_DEL("AI.MODELDEL"),
  MODEL_RUN("AI.MODELRUN"),
  MODEL_EXECUTE("AI.MODELEXECUTE"),
  SCRIPT_SET("AI.SCRIPTSET"),
  SCRIPT_GET("AI.SCRIPTGET"),
  SCRIPT_DEL("AI.SCRIPTDEL"),
  SCRIPT_RUN("AI.SCRIPTRUN"),
  // TODO: support AI.DAGRUN
  DAGRUN("AI.DAGRUN"),
  // TODO: support AI.DAGRUN_RO
  DAGRUN_RO("AI.DAGRUN_RO"),
  INFO("AI.INFO"),
  CONFIG("AI.CONFIG");

  private final byte[] raw;

  Command(String alt) {
    raw = SafeEncoder.encode(alt);
  }

  @Override
  public byte[] getRaw() {
    return raw;
  }
}
