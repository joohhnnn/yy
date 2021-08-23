package com.txznet.music.albumModule.ui;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.data.http.resp.BaseResponse;
import com.txznet.music.net.NetManager;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public abstract class TXZObserver<T extends BaseResponse> implements Observer<T> {

    private static final String TAG = "music:net:response:error:";

    @Override
    public void onSubscribe(Disposable d) {

        //判断是否有网络
        if (!NetManager.isNetworkConnected()) {
            onError(new ResponseErrorException(ResponseErrorException.ERROR_SERVER_NET_ERROR));
        }


    }

    @Override
    public void onNext(T o) {

        if (null != o) {
            if (o.getErrCode() == 0) {
                //成功
                onResponse(o);
            } else {
                onError(new ResponseErrorException(o.getErrCode()));
            }
        } else {
            onError(new ResponseErrorException(ResponseErrorException.ERROR_SERVER_NULL));
        }
    }

    @Override
    public void onError(Throwable e) {
        do {
            if (e instanceof ResponseErrorException) {
                if (((ResponseErrorException) e).getId() == ResponseErrorException.ERROR_SERVER_NULL) {
                    if (showOtherException(ResponseErrorException.ERROR_SERVER_NULL)) break;
                }
                if (((ResponseErrorException) e).getId() == ResponseErrorException.ERROR_SERVER_NET_ERROR) {
                    if (showNetErrorException()) break;
                }
                if (((ResponseErrorException) e).getId() > 0) {
                    if (showTimeoutException()) break;
                }
            }
            showOtherException(ResponseErrorException.ERROR_SERVER_EXCEPTION);
        } while (false);
        LogUtil.loge(TAG, e);
    }

    @Override
    public void onComplete() {

    }

    public abstract void onResponse(T data);

    public boolean showTimeoutException() {
        return false;
    }

    public boolean showNetErrorException() {
        return false;
    }

    public boolean showOtherException(int code) {
        return false;
    }

}
