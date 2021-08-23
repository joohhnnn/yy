package com.txznet.music.helper;

import android.os.SystemClock;

import com.txz.ui.audio.UiAudio;
import com.txznet.music.config.Configuration;

/**
 * Created by brainBear on 2017/6/16.
 */

public class RequestWrapper {

    private UiAudio.Req_DataInterface dataInterface;
    private long requestTime;
    private long timeOut;
    private TXZNetRequest.RequestRawCallBack callBack;


    public RequestWrapper(UiAudio.Req_DataInterface dataInterface) {
        this(dataInterface, null);
    }

    public RequestWrapper(UiAudio.Req_DataInterface dataInterface, TXZNetRequest.RequestRawCallBack callBack) {
        this(dataInterface, Configuration.DefVal.DEFAULT_TIME_OUT, callBack);
    }

    public RequestWrapper(UiAudio.Req_DataInterface dataInterface, long timeOut, TXZNetRequest.RequestRawCallBack callBack) {
        this(dataInterface, timeOut, callBack, SystemClock.elapsedRealtime());
    }

    public RequestWrapper(UiAudio.Req_DataInterface dataInterface, long timeOut, TXZNetRequest.RequestRawCallBack callBack, long requestTime) {
        this.dataInterface = dataInterface;
        this.timeOut = timeOut;
        this.callBack = callBack;
        this.requestTime = requestTime;
    }

    public UiAudio.Req_DataInterface getDataInterface() {
        return dataInterface;
    }

    public void setDataInterface(UiAudio.Req_DataInterface dataInterface) {
        this.dataInterface = dataInterface;
    }

    public long getRequestTime() {
        return requestTime;
    }

    public void setRequestTime(long requestTime) {
        this.requestTime = requestTime;
    }

    public TXZNetRequest.RequestRawCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(TXZNetRequest.RequestCallBack callBack) {
        this.callBack = callBack;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}
