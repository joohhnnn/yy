package com.txznet.music.util;

import android.support.annotation.NonNull;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;

import java.util.Map;

public class VolleyHttpReq {
    public static final String TAG = Constant.LOG_TAG_NET + ":Volley";
    private static final int TIME_RETRY = 2000;
    private int TIME_OUT_REQ = 0;
    private final String mUrl;
    private ICallback mCallback;
    private boolean post;
    private Map<String, Object> headers;
    private Runnable mRetryTask = this::doRequest;

    private HttpUtils.HttpCallbackListener mHttpCallbackListener = new HttpUtils.HttpCallbackListener() {
        @Override
        public void onSuccess(String response) {
            AppLogic.removeUiGroundCallback(mRetryTask);
            if (needResponse()) {
                mRetryTask = null;
                mCallback.onResp(response);
                mCallback = null;
            }
        }

        @Override
        public void onError(int errorCode) {
            Logger.e(TAG, "req error:" + mUrl + ";" + needResponse() + ";" + errorCode);
            if (needResponse() && mRetryTask != null) {
                AppLogic.runOnUiGround(mRetryTask, TIME_RETRY);
            }
        }
    };

    private synchronized boolean needResponse() {
        return mCallback != null;
    }

    public interface ICallback {
        void onResp(String resp);
    }

    public VolleyHttpReq(@NonNull String url, @NonNull ICallback callback) {
        this(url, callback, false, null);
    }

    public VolleyHttpReq(@NonNull String url, @NonNull ICallback callback, boolean post, Map<String, Object> headers) {
        this.mUrl = url;
        this.mCallback = callback;
        this.post = post;
        this.headers = headers;
    }

    public void doRequest() {
        AppLogic.removeUiGroundCallback(mRetryTask);
        increaseData();

        if (!needResponse()) {
            //已经不需要请求了
            Logger.d(TAG, "success:" + needResponse() + ";  no need to req data");
            return;
        }

        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
            if (post) {
                HttpUtils.sendPostRequest(mUrl, headers, TIME_OUT_REQ, mHttpCallbackListener);
                HttpUtils.sendPostRequest(mUrl, headers, TIME_OUT_REQ, mHttpCallbackListener);
            } else {
                HttpUtils.sendGetRequest(mUrl, headers, TIME_OUT_REQ, mHttpCallbackListener);
                HttpUtils.sendGetRequest(mUrl, headers, TIME_OUT_REQ, mHttpCallbackListener);
            }
        } else {
            mHttpCallbackListener.onError(-2);
        }
    }

    private void increaseData() {
        if (TIME_OUT_REQ == 0) {
            TIME_OUT_REQ = 1000;
        } else {
            TIME_OUT_REQ += 1000;
        }
        if (TIME_OUT_REQ > 5000) {
            TIME_OUT_REQ = 5000;//最大值
        }
        Logger.d(TAG, "request:data:timeout:" + TIME_OUT_REQ);
    }
}
