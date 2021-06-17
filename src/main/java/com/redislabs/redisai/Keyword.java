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
  TIMEOUT,
  BACKENDSPATH,
  LOADBACKEND,
  LOAD,
  PERSIST,
  PIPE("|>");

  private final byte[] raw;

  Keyword() {
    raw = SafeEncoder.encode(this.name());
  }

  Keyword(String encodeStr) {
    raw = SafeEncoder.encode(encodeStr);
  }

  public byte[] getRaw() {
    return raw;
  }
}
