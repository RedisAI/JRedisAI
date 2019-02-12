package com.redislabs.redisai;

import java.util.ArrayList;
import java.util.List;

import redis.clients.jedis.BinaryClient;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.exceptions.JedisDataException;
import redis.clients.jedis.util.Pool;
import redis.clients.jedis.util.SafeEncoder;

public class RedisAI {

 
  private final Pool<Jedis> pool;
  
  public RedisAI(Pool<Jedis> pool) {
    this.pool = pool;
  }
  
  /**
   * AI.SET TENSOR tensor_key data_type dim shape1..shapeN VALUES val1..valN
   */
  public boolean setTensor(String key, Object tensor, int[] dimensions){

    DataType type = DataType.baseObjType(tensor);
    
    try (Jedis conn = getConnection()) {

      ArrayList<byte[]> args = new ArrayList<>();
      args.add(Keyword.TENSOR.getRaw());
      args.add(SafeEncoder.encode(key));
      args.add(type.getRaw());
      for(int shape : dimensions) {
        args.add(Protocol.toByteArray(shape));
      }
      args.add(Keyword.VALUES.getRaw());
      args.addAll(type.toByteArray(tensor, dimensions));
      
      return sendCommand(conn, Command.SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply().equals("OK");
      
    } catch(JedisDataException ex ) {
      throw new RedisAIException(ex);
    }
  }
  
  /**
   * AI.SET TENSOR tensor_key data_type dim shape1..shapeN VALUES val1..valN
   */
  public Object getTensor(String key){

    try (Jedis conn = getConnection()) {
      List<Object> result = (List<Object>)sendCommand(conn, Command.GET, Keyword.TENSOR.getRaw(), SafeEncoder.encode(key), Keyword.VALUES.getRaw())
          .getObjectMultiBulkReply();
      
      DataType type = DataType.valueOf((String)result.get(0));
      
      return null;
      
      
    } catch(JedisDataException ex ) {
      throw new RedisAIException(ex);
    }
  }
  
  private Jedis getConnection() {
    return pool.getResource();
  }
  
  private BinaryClient sendCommand(Jedis conn, Command command, byte[]... args) {
    BinaryClient client = conn.getClient();
    client.sendCommand(command, args);
    return client;
  }
}
