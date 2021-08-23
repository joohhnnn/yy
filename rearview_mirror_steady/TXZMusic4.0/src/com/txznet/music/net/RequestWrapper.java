package com.txznet.music.net;

import android.os.SystemClock;

import com.txz.ui.audio.UiAudio;

/**
 * Created by brainBear on 2017/6/16.
 */

public class RequestWrapper {

    public static long DEFAULT_TIME_OUT = 10 * 1000;

    private UiAudio.Req_DataInterface dataInterface;
    private long requestTime;
    private long timeOut;
    private RequestRawCallBack callBack;


    public RequestWrapper(UiAudio.Req_DataInterface dataInterface) {
        this(dataInterface, null);
    }

    public RequestWrapper(UiAudio.Req_DataInterface dataInterface, RequestRawCallBack callBack) {
        this(dataInterface, DEFAULT_TIME_OUT, callBack);
    }

    public RequestWrapper(UiAudio.Req_DataInterface dataInterface, long timeOut, RequestRawCallBack callBack) {
        this(dataInterface, timeOut, callBack, SystemClock.elapsedRealtime());
    }

    public RequestWrapper(UiAudio.Req_DataInterface dataInterface, long timeOut, RequestRawCallBack callBack, long requestTime) {
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

    public RequestRawCallBack getCallBack() {
        return callBack;
    }

    public void setCallBack(RequestCallBack callBack) {
        this.callBack = callBack;
    }

    public long getTimeOut() {
        return timeOut;
    }

    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }
}
