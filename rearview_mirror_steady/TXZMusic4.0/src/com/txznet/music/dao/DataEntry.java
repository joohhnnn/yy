package com.txznet.music.dao;

/**
 * Created by brainBear on 2017/9/27.
 */

public class DataEntry {

    private String key;

    private String value;

    private int type;


    public DataEntry(String key, String value, int type) {
        this.key = key;
        this.value = value;
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
