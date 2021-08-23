package com.txznet.record.bean;

public class MiHomeInfo {

    public static final int STATE_NULL = -1;

    public static final int STATE_QUERYING = 2;
    public static final int STATE_QUERYED = 3;
    public static final int STATE_EXECUTING = 4;
    public static final int STATE_EXECUTED = 5;
    public static final int STATE_QUERY_FAIL = -2;


    public static final int DEVICE_STATE_OPEN = 0;
    public static final int DEVICE_STATE_CLOSE = 1;

    public String deviceArea;
    public String deviceLocation;
    public int state;



    public static class Device extends MiHomeInfo {
        public String deviceId;
        public String deviceName;
        public int isOnline;
        public int deviceState;
    }

    public static class TemperatureHumiditySensorDevice extends Device {
        public int temperature;
        public int humidity;
    }

    public static class AreaDevice extends MiHomeInfo {
        public String deviceTypeName;
        public String openNum;
        public String closeNum;
    }
}
