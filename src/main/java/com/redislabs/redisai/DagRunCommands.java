package com.redislabs.redisai;

public interface DagRunCommands {
  public void setTensor(String key, Tensor tensor);

  public void getTensor(String key);

  public void runModel(String key, String[] inputs, String[] outputs);

  public void runScript(String key, String function, String[] inputs, String[] outputs);
}
