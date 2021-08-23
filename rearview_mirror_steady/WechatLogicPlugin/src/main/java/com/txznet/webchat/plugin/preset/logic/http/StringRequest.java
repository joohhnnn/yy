package com.txznet.webchat.plugin.preset.logic.http;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ASUS User on 2015/7/15.
 */
public class StringRequest extends com.android.volley.toolbox.StringRequest {
    private Map<String, String> mParams;

    public StringRequest(int method, String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(method, url, listener, errorListener);
    }

    public StringRequest(String url, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(url, listener, errorListener);
    }

    public StringRequest(String url, Map<String, String> params, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        this.mParams = params;
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        HashMap<String, String> headers = new HashMap<String, String>();
//        headers.put("Accept", "application/json, text/plain, */*");
        headers.put("Content-Type", "text/html;charset=UTF-8");
//        headers.put("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
//        headers.put("Cache-Control", "max-age=0");
//        headers.put("Connection", "keep-alive");
//        headers.put("DNT", "1");
        headers.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0");
        headers.put("Referer", "https://wx2.qq.com/?&lang=zh_CN");
//        headers.put("Referer", "https://wx.qq.com/");
        return headers;
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return mParams;
    }
}
