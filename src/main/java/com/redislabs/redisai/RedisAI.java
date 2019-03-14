package com.redislabs.redisai;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.stream.Collectors;

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
   * AI.TENSORSET tensor_key data_type shape1 shape2 ... [BLOB data | VALUES val1 val2 ...]
   */
  public boolean setTensor(String key, Object tensor, int[] dimensions){

    DataType type = DataType.baseObjType(tensor);
    
    try (Jedis conn = getConnection()) {

      ArrayList<byte[]> args = new ArrayList<>();
      args.add(SafeEncoder.encode(key));
      args.add(type.getRaw());
      for(int shape : dimensions) {
        args.add(Protocol.toByteArray(shape));
      }
      args.add(Keyword.VALUES.getRaw());
      args.addAll(type.toByteArray(tensor, dimensions));
      
      return sendCommand(conn, Command.TENSOR_SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply().equals("OK");
      
    } catch(JedisDataException ex ) {
      throw new RedisAIException(ex);
    }
  }
  
  /**
   * AI.MODELSET model_key backend device [INPUTS name1 name2 ... OUTPUTS name1 name2 ...] model_blob
   */
  public boolean setModel(String key, Backend backend, Device devive, String[] inputs, String[] outputs, String modelPath){

    try (Jedis conn = getConnection()) {

      ArrayList<byte[]> args = new ArrayList<>();
      args.add(SafeEncoder.encode(key));
      args.add(backend.getRaw());
      args.add(devive.getRaw());
      
      args.add(Keyword.INPUTS.getRaw());
      for(String input: inputs) {
        args.add(SafeEncoder.encode(input));
      }
      
      args.add(Keyword.OUTPUTS.getRaw());
      for(String output: outputs) {
        args.add(SafeEncoder.encode(output));
      }
      
      args.add(Files.readAllBytes(Paths.get(modelPath)));
      
      return sendCommand(conn, Command.MODEL_SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply().equals("OK");
      
    } catch(JedisDataException | IOException ex ) {
      throw new RedisAIException(ex);
    }
  }
  
  /**
   * AI.SCRIPTSET script_key device script_source
   */
  public boolean setScriptFile(String key, Device device, String scriptFile){
    try {
      
      String script = Files.readAllLines(Paths.get(scriptFile), StandardCharsets.UTF_8)
          .stream()
          .collect(Collectors.joining("\n")) + "\n";
      
      return setScript(key, device, script);
      
    } catch(IOException ex ) {
      throw new RedisAIException(ex);
    }
  }
  
  /**
   * AI.SCRIPTSET script_key device script_source
   */
  public boolean setScript(String key, Device device, String script){

    try (Jedis conn = getConnection()) {

      ArrayList<byte[]> args = new ArrayList<>();
      args.add(SafeEncoder.encode(key));
      args.add(device.getRaw());
      
      args.add(SafeEncoder.encode(script));
      
      return sendCommand(conn, Command.SCRIPT_SET, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply().equals("OK");
      
    } catch(JedisDataException ex ) {
      throw new RedisAIException(ex);
    }
  }
  
  /**
   * AI.MODELRUN model_key INPUTS input_key1 ... OUTPUTS output_key1 ...
   */
  public boolean runModel(String key, String[] inputs, String[] outputs){

    try (Jedis conn = getConnection()) {

      ArrayList<byte[]> args = new ArrayList<>();
      args.add(SafeEncoder.encode(key));
      
      args.add(Keyword.INPUTS.getRaw());
      for(String input: inputs) {
        args.add(SafeEncoder.encode(input));
      }
      
      args.add(Keyword.OUTPUTS.getRaw());
      for(String output: outputs) {
        args.add(SafeEncoder.encode(output));
      }
      
      return sendCommand(conn, Command.MODEL_RUN, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply().equals("OK");
      
    } catch(JedisDataException ex) {
      throw new RedisAIException(ex);
    }
  }
  

  /**
   * AI.SCRIPTRUN script_key fn_name INPUTS input_key1 ... OUTPUTS output_key1 ...
   */
  public boolean runScript(String key, String function, String[] inputs, String[] outputs) {

    try (Jedis conn = getConnection()) {

      ArrayList<byte[]> args = new ArrayList<>();
      args.add(SafeEncoder.encode(key));
      args.add(SafeEncoder.encode(function));
      
      args.add(Keyword.INPUTS.getRaw());
      for(String input: inputs) {
        args.add(SafeEncoder.encode(input));
      }
      
      args.add(Keyword.OUTPUTS.getRaw());
      for(String output: outputs) {
        args.add(SafeEncoder.encode(output));
      }
      
      return sendCommand(conn, Command.SCRIPT_RUN, args.toArray(new byte[args.size()][]))
          .getStatusCodeReply().equals("OK");
      
    } catch(JedisDataException ex) {
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
