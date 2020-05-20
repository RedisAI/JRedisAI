package com.redislabs.redisai;

import org.junit.Assert;
import org.junit.Test;

public class DataTypeTest {


    @Test
    public void getDataTypefromString() {
        DataType dtypef = DataType.getDataTypefromString("FLOAT");
        Assert.assertEquals("FLOAT", dtypef.name());
        Assert.assertEquals(DataType.FLOAT.getRaw(), dtypef.getRaw());

        DataType dtyped = DataType.getDataTypefromString("DOUBLE");
        Assert.assertEquals("DOUBLE", dtyped.name());
        Assert.assertEquals(DataType.DOUBLE.getRaw(), dtyped.getRaw());

        DataType dtypei = DataType.getDataTypefromString("INT32");
        Assert.assertEquals("INT32", dtypei.name());
        Assert.assertEquals(DataType.INT32.getRaw(), dtypei.getRaw());

        DataType dtypel = DataType.getDataTypefromString("INT64");
        Assert.assertEquals("INT64", dtypel.name());
        Assert.assertEquals(DataType.INT64.getRaw(), dtypel.getRaw());
    }
}
