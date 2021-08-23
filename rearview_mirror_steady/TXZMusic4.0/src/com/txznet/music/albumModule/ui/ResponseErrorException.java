package com.txznet.music.albumModule.ui;

public class ResponseErrorException extends Exception {

    public static final int ERROR_SERVER_NULL = -1;//服务器返回数据为null
    public static final int ERROR_SERVER_EXCEPTION = -2;//发生crash
    public static final int ERROR_SERVER_NET_ERROR = -3;//无网
    public static final int ERROR_SERVER_TIMEOUT = -5;//请求超时

    //请求音频数据产生的问题
    public static final int ERROR_SERVER_MEASURE = -4;//请求章节会错误

    private int id;

    public ResponseErrorException(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
