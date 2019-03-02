package com.redislabs.redisai;

import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum Command implements ProtocolCommand{

    TENSOR_GET("AI.TENSORGET"),
    TENSOR_SET("AI.TENSORSET"),
    MODEL_GET("AI.MODELGET"),
    MODEL_SET("AI.MODELSET"),
    MODEL_RUN("AI.MODELRUN"),
    SCRIPT_GET("AI.SCRIPTGET"),
    SCRIPT_SET("AI.SCRIPTSET"),
    SCRIPT_RUN("AI.SCRIPTRUN");

    
    private final byte[] raw;

    Command(String alt) {
        raw = SafeEncoder.encode(alt);
    }

    public byte[] getRaw() {
        return raw;
    }
}