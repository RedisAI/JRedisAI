package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Keyword implements ProtocolCommand {
  INPUTS("INPUTS"),
  OUTPUTS("OUTPUTS"),
  META("META"),
  VALUES("VALUES"),
  BLOB("BLOB"),
  SOURCE("SOURCE"),
  RESETSTAT("RESETSTAT"),
  TAG("TAG"),
  BATCHSIZE("BATCHSIZE"),
  MINBATCHSIZE("MINBATCHSIZE"),
  BACKENDSPATH("BACKENDSPATH"),
  LOADBACKEND("LOADBACKEND"),
  LOAD("LOAD"),
  PERSIST("PERSIST"),
  PIPE("|>");

  private final byte[] raw;

  Keyword(String s) {
    raw = SafeEncoder.encode(s);
  }

  public byte[] getRaw() {
    return raw;
  }
}
