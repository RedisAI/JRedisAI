package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Device implements ProtocolCommand{
  CPU, GPU;

  private final byte[] raw;

  Device() {
    raw = SafeEncoder.encode(this.name());
  }

  public byte[] getRaw() {
    return raw;
  } 
}
