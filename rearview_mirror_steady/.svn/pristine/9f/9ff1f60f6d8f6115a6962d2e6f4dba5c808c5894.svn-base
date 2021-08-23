package com.txznet.music.net.rx;

public class TongtingException extends Exception {

    public static final int ERROR_CODE_EMPTY = 1;//后台返回内容为空
    public static final int ERROR_CODE_DATA = 2;//后台返回内容不为空，但是数据不是预期的值，比方说少了个标志位

    private int errorCode;

    public TongtingException(String message, int errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public int getErrorCode() {
        return errorCode;
    }
}
