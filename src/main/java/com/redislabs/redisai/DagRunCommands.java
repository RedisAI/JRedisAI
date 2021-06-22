package com.redislabs.redisai;

interface DagRunCommands<T> {
  T setTensor(String key, Tensor tensor);

  T getTensor(String key);

  T runModel(String key, String[] inputs, String[] outputs);

  T executeModel(String key, String[] inputs, String[] outputs);

  T runScript(String key, String function, String[] inputs, String[] outputs);
}
