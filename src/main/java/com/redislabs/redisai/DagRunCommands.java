package com.redislabs.redisai;

interface DagRunCommands<T> {
  T setTensor(String key, Tensor tensor);

  T getTensor(String key);

  T runModel(String key, String[] inputs, String[] outputs);

  default T executeModel(String key, String[] inputs, String[] outputs) {
    return executeModel(key, inputs, outputs, -1L);
  }

  T executeModel(String key, String[] inputs, String[] outputs, long timeout);

  T runScript(String key, String function, String[] inputs, String[] outputs);
}
