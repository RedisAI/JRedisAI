package com.redislabs.redisai;

public class RedisAIException extends RuntimeException {

  public RedisAIException(Exception cause) {
    super(cause);
  }

  public RedisAIException(String message, Exception cause) {
    super(message, cause);
  }
}
