package com.txznet.music.data.bean;


import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.data.DataErrorException;
import com.txznet.music.data.kaola.net.bean.RespParent;
import com.txznet.music.utils.CollectionUtils;

import java.util.Collection;

import io.reactivex.functions.Function;

public abstract class FunctionKaolaResp<T extends RespParent, R> implements Function<T, R> {
    @Override
    public R apply(T t) throws Exception {
        if (t.getErrcode() == null) {
            if (t.getResult() instanceof Collection) {
                //接口
                if (CollectionUtils.isEmpty((Collection) t.getResult())) {
                    onError("-110");
                } else {
                    return onSuccess(t);
                }
            } else {
                return onSuccess(t);
            }
        }
        onError(t.getErrcode());
        return null;
    }

    public abstract R onSuccess(T t);

    public void onError(String errorCode) throws DataErrorException {
        LogUtil.logd("考拉接口发生异常：（" + errorCode + ")");
        throw new DataErrorException(errorCode);
    }
}
