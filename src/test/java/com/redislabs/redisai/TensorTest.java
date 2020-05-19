package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Test;

public class TensorTest {

    @Test
    public void getValues() {
        Tensor tensor = new Tensor(DataType.INT32, new long[]{1, 2}, new int[]{3, 4});
        int[] values = (int[]) tensor.getValues();
        Assert.assertArrayEquals(values, new int[]{3, 4});
    }

    @Test
    public void setValues() {
        Tensor tensor = new Tensor(DataType.INT32, new long[]{1, 2}, new int[]{3, 4});
        int[] values = (int[]) tensor.getValues();
        Assert.assertArrayEquals(values, new int[]{3, 4});
        tensor.setValues(new int[]{6, 8});
        values = (int[]) tensor.getValues();
        Assert.assertArrayEquals(values, new int[]{6, 8});
    }

    @Test
    public void getShape() {
        Tensor tensor = new Tensor(DataType.INT32, new long[]{1, 2}, new int[]{3, 4});
        long[] values = tensor.getShape();
        Assert.assertArrayEquals(values, new long[]{1, 2});
    }

    @Test
    public void getDataType() {
        Tensor tensor = new Tensor(DataType.INT32, new long[]{1, 2}, new int[]{3, 4});
        DataType dtype = tensor.getDataType();
        Assert.assertEquals(dtype, DataType.INT32);
    }

    @Test
    public void setDataType() {
        Tensor tensor = new Tensor(DataType.INT32, new long[]{1, 2}, new int[]{3, 4});
        tensor.setDataType(DataType.INT64);
        DataType dtype = tensor.getDataType();
        Assert.assertEquals(dtype, DataType.INT64);
    }
}
