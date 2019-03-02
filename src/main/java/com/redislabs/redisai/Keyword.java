package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Keyword implements ProtocolCommand{

    TENSOR, VALUES, INPUTS, OUTPUTS;
    
    private final byte[] raw;

    Keyword() {
        raw = SafeEncoder.encode(this.name());
    }

    public byte[] getRaw() {
        return raw;
    }
}