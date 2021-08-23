package com.txznet.music.model;

public interface INormalCallback<T> {
    void onSuccess(T t);

    void onError();
}
