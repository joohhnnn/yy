package com.txznet.webchat.helper;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.txznet.loader.AppLogic;

/**
 * 微信网络请求管理
 * Created by J on 2017/7/19.
 */

public class WxNetworkHelper {
    private RequestQueue mRequestQueue;

    // single instance
    private static WxNetworkHelper sInstance;

    public static WxNetworkHelper getInstance() {
        if (null == sInstance) {
            synchronized (WxNetworkHelper.class) {
                if (null == sInstance) {
                    sInstance = new WxNetworkHelper();
                }
            }
        }

        return sInstance;
    }

    private WxNetworkHelper() {
        // 初始化RequestQueue
        mRequestQueue = Volley.newRequestQueue(AppLogic.getApp());
    }
    // eof single instance

    public void doRequest(Request request) {
        mRequestQueue.add(request);
    }
}
