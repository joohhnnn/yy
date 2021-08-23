package com.txznet.music.data.utils;

/**
 * Created by telenewbie on 2018/2/8.
 */
public interface OnGetData<T> {
    void success(T t);

    void failed(int errorCode);
}
