package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import redis.clients.jedis.Protocol;
import redis.clients.jedis.util.SafeEncoder;

/** Direct mapping to RedisAI Model */
public class Model {

  public static final int DEFAULT_BLOB_CHUNK_SIZE = 512 * 1024 * 1024; // 512MB

  private Backend backend;
  private Device device;
  private String[] inputs;
  private String[] outputs;
  private byte[] blob;
  private final int blobChunkSize;
  private String tag;
  private long batchSize;
  private long minBatchSize;

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param blob - the Protobuf-serialized model
   */
  public Model(Backend backend, Device device, String[] inputs, String[] outputs, byte[] blob) {
    this(backend, device, inputs, outputs, blob, 0, 0);
  }

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param blob - the Protobuf-serialized model
   * @param blobChunkSize
   */
  public Model(
      Backend backend,
      Device device,
      String[] inputs,
      String[] outputs,
      byte[] blob,
      int blobChunkSize) {
    this(backend, device, inputs, outputs, blob, blobChunkSize, 0, 0);
  }

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param blob - the Protobuf-serialized model
   * @param batchSize - when provided with an batchsize that is greater than 0, the engine will
   *     batch incoming requests from multiple clients that use the model with input tensors of the
   *     same shape.
   * @param minBatchSize - when provided with an minbatchsize that is greater than 0, the engine
   *     will postpone calls to AI.MODELRUN until the batch's size had reached minbatchsize
   */
  public Model(
      Backend backend,
      Device device,
      String[] inputs,
      String[] outputs,
      byte[] blob,
      long batchSize,
      long minBatchSize) {
    this(backend, device, inputs, outputs, blob, DEFAULT_BLOB_CHUNK_SIZE, batchSize, minBatchSize);
  }

  /**
   * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
   * @param device - the device that will execute the model. can be of CPU or GPU
   * @param inputs - one or more names of the model's input nodes (applicable only for TensorFlow
   *     models)
   * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow
   *     models)
   * @param blob - the Protobuf-serialized model
   * @param blobChunkSize
   * @param batchSize - when provided with an batchsize that is greater than 0, the engine will
   *     batch incoming requests from multiple clients that use the model with input tensors of the
   *     same shape.
   * @param minBatchSize - when provided with an minbatchsize that is greater than 0, the engine
   *     will postpone calls to AI.MODELRUN until the batch's size had reached minbatchsize
   */
  public Model(
      Backend backend,
      Device device,
      String[] inputs,
      String[] outputs,
      byte[] blob,
      int blobChunkSize,
      long batchSize,
      long minBatchSize) {
    this.backend = backend;
    this.device = device;
    this.inputs = inputs;
    this.outputs = outputs;
    this.blob = blob;
    this.tag = null;
    this.batchSize = batchSize;
    this.minBatchSize = minBatchSize;
    this.blobChunkSize = blobChunkSize;
  }

  public static Model createModelFromRespReply(List<?> reply) {
    Model model = null;
    Backend backend = null;
    Device device = null;
    String tag = null;
    byte[] blob = null;
    long batchsize = 0;
    long minbatchsize = 0;
    String[] inputs = new String[0];
    String[] outputs = new String[0];
    for (int i = 0; i < reply.size(); i += 2) {
      String arrayKey = SafeEncoder.encode((byte[]) reply.get(i));
      switch (arrayKey) {
        case "backend":
          String backendString = SafeEncoder.encode((byte[]) reply.get(i + 1));
          backend = Backend.valueOf(backendString);
          if (backend == null) {
            throw new JRedisAIRunTimeException("Unrecognized backend: " + backendString);
          }
          break;
        case "device":
          String deviceString = SafeEncoder.encode((byte[]) reply.get(i + 1));
          device = Device.valueOf(deviceString);
          if (device == null) {
            throw new JRedisAIRunTimeException("Unrecognized device: " + deviceString);
          }
          break;
        case "tag":
          tag = SafeEncoder.encode((byte[]) reply.get(i + 1));
          break;
        case "blob":
          blob = (byte[]) reply.get(i + 1);
          break;
        case "batchsize":
          batchsize = (Long) reply.get(i + 1);
          break;
        case "minbatchsize":
          minbatchsize = (Long) reply.get(i + 1);
          break;
        case "inputs":
          List<byte[]> inputsEncoded = (List<byte[]>) reply.get(i + 1);
          if (!inputsEncoded.isEmpty()) {
            inputs = new String[inputsEncoded.size()];
            for (int j = 0; j < inputsEncoded.size(); j++) {
              inputs[j] = SafeEncoder.encode(inputsEncoded.get(j));
            }
          }
          break;
        case "outputs":
          List<byte[]> outputsEncoded = (List<byte[]>) reply.get(i + 1);
          if (!outputsEncoded.isEmpty()) {
            outputs = new String[outputsEncoded.size()];
            for (int j = 0; j < outputsEncoded.size(); j++) {
              outputs[j] = SafeEncoder.encode(outputsEncoded.get(j));
            }
          }
          break;
        default:
          break;
      }
    }
    if (backend == null || device == null || blob == null) {
      throw new JRedisAIRunTimeException(
          "AI.MODELGET reply did not contained all elements to build the model");
    }
    model = new Model(backend, device, inputs, outputs, blob, batchsize, minbatchsize);
    if (tag != null) {
      model.setTag(tag);
    }
    return model;
  }

  public String getTag() {
    return tag;
  }

  public void setTag(String tag) {
    this.tag = tag;
  }

  public byte[] getBlob() {
    return blob;
  }

  public void setBlob(byte[] blob) {
    this.blob = blob;
  }

  public String[] getOutputs() {
    return outputs;
  }

  public void setOutputs(String[] outputs) {
    this.outputs = outputs;
  }

  public String[] getInputs() {
    return inputs;
  }

  public void setInputs(String[] inputs) {
    this.inputs = inputs;
  }

  public Device getDevice() {
    return device;
  }

  public void setDevice(Device device) {
    this.device = device;
  }

  public Backend getBackend() {
    return backend;
  }

  public void setBackend(Backend backend) {
    this.backend = backend;
  }

  public long getBatchSize() {
    return batchSize;
  }

  public void setBatchSize(long batchsize) {
    this.batchSize = batchsize;
  }

  public long getMinBatchSize() {
    return minBatchSize;
  }

  public void setMinBatchSize(long minbatchsize) {
    this.minBatchSize = minbatchsize;
  }

  /**
   * Encodes the current model properties into an AI.MODELSET command to be store in RedisAI Server
   *
   * @param key name of key to store the Model
   * @return
   */
  protected List<byte[]> getModelSetCommandBytes(String key) {
    List<byte[]> args = new ArrayList<>();
    args.add(SafeEncoder.encode(key));
    args.add(backend.getRaw());
    args.add(device.getRaw());
    if (tag != null) {
      args.add(Keyword.TAG.getRaw());
      args.add(SafeEncoder.encode(tag));
    }
    if (batchSize > 0) {
      args.add(Keyword.BATCHSIZE.getRaw());
      args.add(Protocol.toByteArray(batchSize));
      if (minBatchSize > 0) {
        args.add(Keyword.MINBATCHSIZE.getRaw());
        args.add(Protocol.toByteArray(minBatchSize));
      }
    }
    args.add(Keyword.INPUTS.getRaw());
    for (String input : inputs) {
      args.add(SafeEncoder.encode(input));
    }
    args.add(Keyword.OUTPUTS.getRaw());
    for (String output : outputs) {
      args.add(SafeEncoder.encode(output));
    }
    args.add(Keyword.BLOB.getRaw());
    chunk(args, blob, blobChunkSize);
    return args;
  }

  private static void chunk(List<byte[]> collector, byte[] array, int chunkSize) {
    if (array.length <= chunkSize) {
      collector.add(array);
      return;
    }
    int from = 0;
    while (from < array.length) {
      int copySize = Math.min(array.length - from, chunkSize);
      collector.add(Arrays.copyOfRange(array, from, from + copySize));
      from += copySize;
    }
  }

  protected static List<byte[]> modelRunFlatArgs(
      String key, String[] inputs, String[] outputs, boolean includeCommandName) {
    List<byte[]> args = new ArrayList<>();
    if (includeCommandName) {
      args.add(Command.MODEL_RUN.getRaw());
    }
    args.add(SafeEncoder.encode(key));

    args.add(Keyword.INPUTS.getRaw());
    for (String input : inputs) {
      args.add(SafeEncoder.encode(input));
    }

    args.add(Keyword.OUTPUTS.getRaw());
    for (String output : outputs) {
      args.add(SafeEncoder.encode(output));
    }
    return args;
  }
}
