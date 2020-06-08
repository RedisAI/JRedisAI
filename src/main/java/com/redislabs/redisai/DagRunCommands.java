package com.redislabs.redisai;

interface DagRunCommands {
  void setTensor(String key, Tensor tensor);

  void getTensor(String key);

  void runModel(String key, String[] inputs, String[] outputs);

  void runScript(String key, String function, String[] inputs, String[] outputs);
}
