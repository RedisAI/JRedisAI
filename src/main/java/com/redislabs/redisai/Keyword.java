package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Keyword implements ProtocolCommand{

    TENSOR, INPUTS, OUTPUTS, META, VALUES, BLOB, SOURCE, RESETSTAT, BACKENDSPATH, LOADBACKEND;
    
    private final byte[] raw;

    Keyword() {
        raw = SafeEncoder.encode(this.name());
    }

    public byte[] getRaw() {
        return raw;
    }
}