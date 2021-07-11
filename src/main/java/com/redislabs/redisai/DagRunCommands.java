package com.redislabs.redisai;

import java.util.List;

interface DagRunCommands<T> {
  T setTensor(String key, Tensor tensor);

  T getTensor(String key);

  T runModel(String key, String[] inputs, String[] outputs);

  T executeModel(String key, String[] inputs, String[] outputs);

  T runScript(String key, String function, String[] inputs, String[] outputs);

  T executeScript(
      String key,
      String function,
      List<String> keys,
      List<String> inputs,
      List<String> args,
      List<String> outputs);
}
