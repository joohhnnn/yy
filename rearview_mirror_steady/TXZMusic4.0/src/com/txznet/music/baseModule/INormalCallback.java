package com.txznet.music.baseModule;

public interface INormalCallback<T> {
    void onSuccess(T t);

    void onError();
}
