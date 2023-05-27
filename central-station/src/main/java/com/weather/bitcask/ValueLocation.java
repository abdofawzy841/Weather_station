package com.weather.bitcask;

public class ValueLocation {
    String fileName;
    int offset;
    int size;

    public ValueLocation(String fileName, int offset, int size) {
        this.fileName = fileName;
        this.offset = offset;
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this)
            return true;
        if (!(o instanceof ValueLocation)) {
            return false;
        }
        ValueLocation valueLocation = (ValueLocation) o;
        return fileName.equals(valueLocation.fileName) && offset == valueLocation.offset && size == valueLocation.size;
    }
}

