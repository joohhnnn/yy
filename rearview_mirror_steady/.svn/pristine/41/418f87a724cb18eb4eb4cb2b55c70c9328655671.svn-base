package com.txznet.webchat.plugin.preset.logic.api;

import com.android.volley.Request;
import com.android.volley.RequestQueue;

/**
 * Request包装类，根据休眠等状态拦截网络请求
 * Created by J on 2017/3/8.
 */

public class TXZRequestQueue {
    private boolean bEnableRequest = true; // 是否允许网络请求

    private RequestQueue mRequestQueue;

    public TXZRequestQueue(RequestQueue queue) {
        mRequestQueue = queue;
    }

    public void setEnableRequest(boolean enable) {
        bEnableRequest = enable;
    }

    public <T> Request<T> add(Request<T> request) {
        if (bEnableRequest) {
            return mRequestQueue.add(request);
        }

        return null;
    }

    public void start() {
        mRequestQueue.start();
    }

    public void stop() {
        mRequestQueue.stop();
    }

    public void cancelAll(Object tag) {
        mRequestQueue.cancelAll(tag);
    }

    public void cancelAll(RequestQueue.RequestFilter filter) {
        mRequestQueue.cancelAll(filter);
    }
}
