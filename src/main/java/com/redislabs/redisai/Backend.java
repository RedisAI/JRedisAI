package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Backend implements ProtocolCommand {
  TF,
  TORCH;

  private final byte[] raw;

  Backend() {
    raw = SafeEncoder.encode(this.name());
  }

  public byte[] getRaw() {
    return raw;
  }
}
