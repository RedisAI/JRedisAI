package com.redislabs.redisai;

import redis.clients.jedis.exceptions.JedisDataException;

public class RedisAIException extends RuntimeException{
  
  public RedisAIException(JedisDataException cause) {
    super(cause);
  }

}
