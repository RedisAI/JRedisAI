package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.util.ArrayList;
import java.util.List;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

public class Tensor {
  private DataType dataType;
  private long[] shape;
  private Object values;

  /**
   * @param dataType
   * @param shape
   * @param values
   */
  public Tensor(DataType dataType, long[] shape, Object values) {
    this.shape = shape;
    this.values = values;
    this.dataType = dataType;
  }

  /**
   * Given a RESP reply from RedisAI this method will create a new Tensor
   *
   * @param reply reply from RedisAI Server
   * @return Tensor Object
   */
  protected static Tensor createTensorFromRespReply(List<?> reply) {
    DataType dtype = null;
    long[] shape = null;
    Object values = null;
    Tensor tensor = null;
    for (int i = 0; i < reply.size(); i += 2) {
      String arrayKey = SafeEncoder.encode((byte[]) reply.get(i));
      switch (arrayKey) {
        case "dtype":
          String dtypeString = SafeEncoder.encode((byte[]) reply.get(i + 1));
          dtype = DataType.getDataTypefromString(dtypeString);
          if (dtype == null) {
            throw new JRedisAIRunTimeException("Unrecognized datatype: " + dtypeString);
          }
          break;
        case "shape":
          List<Long> shapeResp = (List<Long>) reply.get(i + 1);
          shape = new long[shapeResp.size()];
          for (int j = 0; j < shapeResp.size(); j++) {
            shape[j] = shapeResp.get(j);
          }
          break;
        case "values":
          if (dtype == null) {
            throw new JRedisAIRunTimeException(
                "Trying to decode values array without previous datatype info");
          }
          List<byte[]> valuesEncoded = (List<byte[]>) reply.get(i + 1);
          values = dtype.toObject(valuesEncoded);
          break;
        default:
          break;
      }
    }
    if (dtype != null && shape != null && values != null) {
      tensor = new Tensor(dtype, shape, values);
    }
    return tensor;
  }

  public Object getValues() {
    return values;
  }

  public void setValues(Object values) {
    this.values = values;
  }

  public long[] getShape() {
    return shape;
  }

  public DataType getDataType() {
    return dataType;
  }

  public void setDataType(DataType dataType) {
    this.dataType = dataType;
  }

  /**
   * Encodes the current model properties into an AI.MODELSET command to be store in RedisAI Server
   *
   * @param key name of key to store the Model
   * @return
   */
  protected List<byte[]> getTensorSetCommandBytes(String key) {
    List<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(key));
    args.add(dataType.getRaw());
    for (long shapeDimension : shape) {
      args.add(Protocol.toByteArray(shapeDimension));
    }
    args.add(Keyword.VALUES.getRaw());
    args.addAll(dataType.toByteArray(values, shape));
    return args;
  }
}
