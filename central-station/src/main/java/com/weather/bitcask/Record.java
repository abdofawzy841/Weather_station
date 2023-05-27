package com.weather.bitcask;

import java.io.FileOutputStream;
import java.nio.ByteBuffer;

public class Record {
    int keySize;
    int valSize;
    byte[] key;
    byte[] value;

    public Record(long key, String value) {
        this.key = ByteBuffer.allocate(8).putLong(key).array();
        this.value = value.getBytes();
        this.keySize = this.key.length;
        this.valSize = this.value.length;
    }

    public byte[] getRecord(){
        byte[] record = new byte[getLength()];
        System.arraycopy(ByteBuffer.allocate(4).putInt(keySize).array(),0,record,0,4);
        System.arraycopy(ByteBuffer.allocate(4).putInt(valSize).array(),0,record,4,4);
        System.arraycopy(key, 0, record, 8, keySize);
        System.arraycopy(value, 0, record, 8+keySize, valSize);
        return record;
    }
    public int getLength(){
        return 8 + keySize + valSize;
    }

    public void writeRecord(String filePath){
        try {
            FileOutputStream toWrite = new FileOutputStream(filePath,true);
            toWrite.write(this.getRecord());
            toWrite.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
