package com.redislabs.redisai;

import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class ChunkTest {

  private static final int SMALL_CHUNK_SIZE = 8 * 1024; // 8KB

  @BeforeClass
  public static void prepare() {
    System.setProperty(Model.BLOB_CHUNK_SIZE_PROPERTY, Integer.toString(SMALL_CHUNK_SIZE));
  }

  @AfterClass
  public static void cleanUp() {
    System.clearProperty(Model.BLOB_CHUNK_SIZE_PROPERTY);
  }

  /**
   * @throws java.net.URISyntaxException
   * @throws java.io.IOException
   * @see ModelTest#argumentsWithoutChunking()
   */
  @Test
  public void argumentsWithChunking() throws URISyntaxException, IOException {
    Model model =
        new Model(
            Backend.ONNX,
            Device.GPU,
            getClass().getClassLoader().getResource("test_data/mnist.onnx").toURI());

    Assert.assertEquals(8, model.getModelStoreCommandArgs("key").size());
  }

  @Test
  public void commandWithChunking() throws IOException, URISyntaxException {
    Model model =
        new Model(
            Backend.ONNX,
            Device.CPU,
            getClass().getClassLoader().getResource("test_data/mnist.onnx").toURI());

    try (RedisAI ai = new RedisAI()) {
      Assert.assertTrue(ai.storeModel("model-chunk", model));
      Assert.assertNotNull(ai.getModel("model-chunk"));
    }
  }
}
