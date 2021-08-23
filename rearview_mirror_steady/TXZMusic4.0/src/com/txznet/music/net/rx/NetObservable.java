package com.txznet.music.net.rx;

import android.os.Looper;

import com.txz.ui.audio.UiAudio;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestRawCallBack;
import com.txznet.music.utils.JsonHelper;

import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

/**
 * Created by brainBear on 2018/2/28.
 */

public class NetObservable<T> extends Observable<T> implements Disposable, RequestRawCallBack {

    private static final String TAG = "music:net:observer:";
    private final AtomicBoolean unSubscribed = new AtomicBoolean();
    private String url;
    private Object data;
    private Class<T> clazz;
    private Observer<? super T> observer;
    private int requestId;

    public NetObservable(String url, Object data, Class<T> clazz) {
        this.url = url;
        this.data = data;
        this.clazz = clazz;
    }


    @Override
    public void onResponse(UiAudio.Resp_DataInterface respDataInterface) {
        if (!isDisposed()) {
            String s = new String(respDataInterface.strData);
            try {
                T t = JsonHelper.toObject(clazz, s);
                observer.onNext(t);
                observer.onComplete();
            } catch (Throwable throwable) {
                observer.onError(throwable);
            }
        }
    }

    @Override
    public void onError(String cmd, Error error) {
        if (!isDisposed()) {
            observer.onError(new NetErrorException(cmd, error));
        }
    }

    @Override
    protected void subscribeActual(Observer<? super T> observer) {
        this.observer = observer;

        observer.onSubscribe(this);

        requestId = NetManager.getInstance().sendRequestToCore(url, data, this);
    }

    @Override
    public void dispose() {
        if (unSubscribed.compareAndSet(false, true)) {
            if (Looper.myLooper() == Looper.getMainLooper()) {
                onDispose();
            } else {
                AndroidSchedulers.mainThread().scheduleDirect(new Runnable() {
                    @Override
                    public void run() {
                        onDispose();
                    }
                });
            }
        }
    }

    @Override
    public boolean isDisposed() {
        return unSubscribed.get();
    }

    private void onDispose() {
        Logger.d(TAG,"onDispose");
        NetManager.getInstance().cancelRequest(requestId);
    }
}
