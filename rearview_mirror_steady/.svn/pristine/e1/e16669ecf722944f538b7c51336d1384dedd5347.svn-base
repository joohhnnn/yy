package com.txznet.music.data.http;


import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.data.http.resp.BaseResponse;
import com.txznet.music.net.rx.NetErrorException;

import io.reactivex.functions.Function;

public abstract class TXZFunction<T extends BaseResponse, R> implements Function<T, R> {
    @Override
    public R apply(T t) throws Exception {
        if (t.getErrCode() == 0) {
            return handleData(t);
        }
        //抛出异常
        throw new NetErrorException("request error:", new Error(t.getErrCode()));
    }

    public abstract R handleData(T t);

}
