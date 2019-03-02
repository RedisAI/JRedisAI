package com.redislabs.redisai;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import redis.clients.jedis.Protocol;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum DataType implements ProtocolCommand{

  INT32 {
    @Override
    public List<byte[]> toByteArray(Object obj){
      int[] values = (int[])obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for(int value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      // TODO Auto-generated method stub
      return null;
    }
  }, 
  INT64 {
    @Override
    public List<byte[]> toByteArray(Object obj){
      long[] values = (long[])obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for(long value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      // TODO Auto-generated method stub
      return null;
    }
  }, 
  FLOAT {
    @Override
    public List<byte[]> toByteArray(Object obj){
      float[] values = (float[])obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for(float value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
//      float[] values = (float[])obj;
//      List<byte[]> res = new ArrayList<>(values.length);
//      for(byte[] value : data) {
//        res.add(Protocol.to(value));
//      }
//      return res;
      // TODO Auto-generated method stub
      return null;
    }
  }, 
  DOUBLE {
    @Override
    public List<byte[]> toByteArray(Object obj){
      double[] values = (double[])obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for(double value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      // TODO Auto-generated method stub
      return null;
    }
  }, 
  STRING {
    @Override
    public List<byte[]> toByteArray(Object obj){
      byte[] values = (byte[])obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for(byte value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      // TODO Auto-generated method stub
      return null;
    }
  }, 
  BOOL {
    @Override
    public List<byte[]> toByteArray(Object obj){
      boolean[] values = (boolean[])obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for(boolean value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      // TODO Auto-generated method stub
      return null;
    }
  };

  private final byte[] raw;

  private static final HashMap<Class<?>, DataType> classDataTypes = new HashMap<>();
  static {
    classDataTypes.put(int.class, DataType.INT32);
    classDataTypes.put(Integer.class, DataType.INT32);
    classDataTypes.put(long.class, DataType.INT64);
    classDataTypes.put(Long.class, DataType.INT64);
    classDataTypes.put(float.class, DataType.FLOAT);
    classDataTypes.put(Float.class, DataType.FLOAT);
    classDataTypes.put(double.class, DataType.DOUBLE);
    classDataTypes.put(Double.class, DataType.DOUBLE);
    classDataTypes.put(byte.class, DataType.STRING);
    classDataTypes.put(Byte.class, DataType.STRING);
    classDataTypes.put(boolean.class, DataType.BOOL);
    classDataTypes.put(Boolean.class, DataType.BOOL);
  }

  DataType() {
    raw = SafeEncoder.encode(this.name());
  }

  protected abstract List<byte[]> toByteArray(Object obj);
  protected abstract Object toObject(List<byte[]> data);

  public byte[] getRaw() {
    return raw;
  }

  public List<byte[]> toByteArray(Object obj, int[] dimensions){
    return toByteArray(obj, dimensions, 0, this);
  }

  /** The class for the data type to which Java object o corresponds. */
  public static DataType baseObjType(Object o) {
    Class<?> c = o.getClass();
    while (c.isArray()) {
      c = c.getComponentType();
    }
    DataType ret = classDataTypes.get(c);
    if (ret != null) {
      return ret;
    }
    throw new IllegalArgumentException("cannot create Tensors of type " + c.getName());  
  }
  
  private static List<byte[]> toByteArray(Object obj, int[] dimensions, int dim, DataType type) {
    ArrayList<byte[]> res = new ArrayList<>();
    if(dimensions.length > dim+1) {
      int dimension = dimensions[dim++];
      for(int i=0 ; i < dimension; ++i) {
        Object value = Array.get(obj, i);
        res.addAll(toByteArray(value, dimensions, dim, type));
      }
    } else {
      res.addAll(type.toByteArray(obj));
    }
    return res;
  }
}


