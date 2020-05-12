package com.redislabs.redisai;

public class Tensor {
    private DataType dataType;
    private long[] shape;
    private Object values;

    public Tensor(DataType dataType, long[] shape, Object values ) {
        this.shape = shape;
        this.values = values;
        this.dataType = dataType;
    }

    public Object getValues() {
        return values;
    }

    public void setValues(Object values) {
        this.values = values;
    }

    public long[] getShape() {
        return shape;
    }

    public DataType getDataType() {
        return dataType;
    }

    public void setDataType(DataType dataType) {
        this.dataType = dataType;
    }


}
