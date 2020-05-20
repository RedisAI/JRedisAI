package com.redislabs.redisai;

import com.redislabs.redisai.exceptions.JRedisAIRunTimeException;
import redis.clients.jedis.util.SafeEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * Direct mapping to RedisAI Model
 */
public class Model {
    private Backend backend;
    private Device device;
    private String[] inputs;
    private String[] outputs;
    private byte[] blob;
    private String tag;

    /**
     * @param backend - the backend for the model. can be one of TF, TFLITE, TORCH or ONNX
     * @param device  - the device that will execute the model. can be of CPU or GPU
     * @param inputs  - one or more names of the model's input nodes (applicable only for TensorFlow models)
     * @param outputs - one or more names of the model's output nodes (applicable only for TensorFlow models)
     * @param blob    - the Protobuf-serialized model
     */
    public Model(Backend backend, Device device, String[] inputs, String[] outputs, byte[] blob) {
        this.backend = backend;
        this.device = device;
        this.inputs = inputs;
        this.outputs = outputs;
        this.blob = blob;
        this.tag = "";
    }

    public static Model createModelFromRespReply(List<?> reply) {
        Model model = null;
        Backend backend = null;
        Device device = null;
        String tag = null;
        byte[] blob = null;
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
                default:
                    break;
            }
        }
        if (backend != null && device != null && blob != null) {
            model = new Model(backend,
                    device,
                    new String[0], new String[0], blob);
            if (tag != null) {
                model.setTag(tag);
            }
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
        args.add(Keyword.INPUTS.getRaw());
        for (String input : inputs) {
            args.add(SafeEncoder.encode(input));
        }
        args.add(Keyword.OUTPUTS.getRaw());
        for (String output : outputs) {
            args.add(SafeEncoder.encode(output));
        }
        args.add(Keyword.BLOB.getRaw());
        args.add(blob);
        return args;
    }

}
