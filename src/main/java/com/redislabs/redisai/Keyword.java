package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Keyword implements ProtocolCommand {
  INPUTS,
  OUTPUTS,
  META,
  VALUES,
  BLOB,
  SOURCE,
  RESETSTAT,
  TAG,
  BATCHSIZE,
  MINBATCHSIZE,
  BACKENDSPATH,
  LOADBACKEND,
  LOAD,
  PERSIST,
  PIPE;

  private final byte[] raw;

  Keyword() {
    if (this.name() == "PIPE") {
      raw = SafeEncoder.encode("|>");
    } else {
      raw = SafeEncoder.encode(this.name());
    }
  }

  public byte[] getRaw() {
    return raw;
  }
}
