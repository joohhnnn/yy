package com.txznet.webchat.plugin.preset.logic.http;

import com.android.volley.NoConnectionError;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.txznet.webchat.plugin.preset.logic.api.WeChatClient;

/**
 * Created by ASUS User on 2015/12/10.
 */
public class SimpleErrorListener implements Response.ErrorListener {
    public static final int CONNECTION_ERROR = 1001;
    private WeChatClient.WeChatResp mResp;

    public SimpleErrorListener(WeChatClient.WeChatResp resp) {
        mResp = resp;
    }

    public SimpleErrorListener() {

    }

    @Override
    public void onErrorResponse(VolleyError volleyError) {
        int statusCode = volleyError.networkResponse == null ? -1 : volleyError.networkResponse.statusCode;
        if (volleyError instanceof NoConnectionError || volleyError instanceof TimeoutError) {
            statusCode = CONNECTION_ERROR;
        }
        mResp.onError(statusCode, volleyError.getClass() + "::" + volleyError.getCause() + "::" + volleyError.getMessage());
    }
}
