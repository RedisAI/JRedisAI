package com.redislabs.redisai.exceptions;

import redis.clients.jedis.exceptions.JedisDataException;

/**
 * An instance of JRedisAIRunTimeException is thrown when RedisAI encounters a runtime error during
 * command execution.
 */
public class JRedisAIRunTimeException extends JedisDataException {
  public JRedisAIRunTimeException(String message) {
    super(message);
  }

  public JRedisAIRunTimeException(Throwable cause) {
    super(cause);
  }

  public JRedisAIRunTimeException(String message, Throwable cause) {
    super(message, cause);
  }
}
