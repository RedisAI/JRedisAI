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
  ENTRY_POINTS,
  BATCHSIZE,
  MINBATCHSIZE,
  MINBATCHTIMEOUT,
  TIMEOUT,
  BACKENDSPATH,
  LOADBACKEND,
  LOAD,
  PERSIST,
  KEYS,
  ROUTING,
  ARGS,
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
