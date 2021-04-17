package com.redislabs.redisai;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

public enum DataType implements ProtocolCommand {
  INT32 {
    @Override
    public List<byte[]> toByteArray(Object obj) {
      int[] values = (int[]) obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for (int value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      int[] values = new int[data.size()];
      for (int i = 0; i < data.size(); i++) {
        values[i] = Integer.parseInt(SafeEncoder.encode(data.get(i)));
      }
      return values;
    }
  },
  INT64 {
    @Override
    public List<byte[]> toByteArray(Object obj) {
      long[] values = (long[]) obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for (long value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      long[] values = new long[data.size()];
      for (int i = 0; i < data.size(); i++) {
        values[i] = Long.parseLong(SafeEncoder.encode(data.get(i)));
      }
      return values;
    }
  },
  FLOAT {
    @Override
    public List<byte[]> toByteArray(Object obj) {
      float[] values = (float[]) obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for (float value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      float[] values = new float[data.size()];
      for (int i = 0; i < data.size(); i++) {
        values[i] = Float.parseFloat(SafeEncoder.encode(data.get(i)));
      }
      return values;
    }
  },
  DOUBLE {
    @Override
    public List<byte[]> toByteArray(Object obj) {
      double[] values = (double[]) obj;
      List<byte[]> res = new ArrayList<>(values.length);
      for (double value : values) {
        res.add(Protocol.toByteArray(value));
      }
      return res;
    }

    @Override
    protected Object toObject(List<byte[]> data) {
      double[] values = new double[data.size()];
      for (int i = 0; i < data.size(); i++) {
        values[i] = Double.parseDouble(SafeEncoder.encode(data.get(i)));
      }
      return values;
    }
  };

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
  }

  private final byte[] raw;

  DataType() {
    raw = SafeEncoder.encode(this.name());
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

  private static List<byte[]> toByteArray(Object obj, long[] dimensions, int dim, DataType type) {
    ArrayList<byte[]> res = new ArrayList<>();
    if (dimensions.length - 1 > dim) {
      long dimension = dimensions[dim++];
      for (int i = 0; i < dimension; ++i) {
        Object value = Array.get(obj, i);
        res.addAll(toByteArray(value, dimensions, dim, type));
      }
    } else {
      res.addAll(type.toByteArray(obj));
    }
    return res;
  }

  protected abstract List<byte[]> toByteArray(Object obj);

  protected abstract Object toObject(List<byte[]> data);

  public byte[] getRaw() {
    return raw;
  }

  public List<byte[]> toByteArray(Object obj, long[] dimensions) {
    return toByteArray(obj, dimensions, 0, this);
  }
}
