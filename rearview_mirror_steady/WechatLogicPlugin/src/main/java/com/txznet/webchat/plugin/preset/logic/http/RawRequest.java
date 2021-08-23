package com.txznet.webchat.plugin.preset.logic.http;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import java.io.UnsupportedEncodingException;

/**
 * Created by ASUS User on 2015/7/15.
 */
public class RawRequest extends Request<byte[]> {

    private final Response.Listener<byte[]> mListener;
    private String mRequestBody;

    public RawRequest(int method, String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    public RawRequest(int method, String url, String requestBody, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        this(method, url, listener, errorListener);
        this.mRequestBody = requestBody;
    }

    public RawRequest(String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @Override
    public byte[] getBody() {
        try {
            return this.mRequestBody == null ? null : this.mRequestBody.getBytes("utf-8");
        } catch (UnsupportedEncodingException var2) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", new Object[]{this.mRequestBody, "utf-8"});
            return null;
        }
    }

    protected void deliverResponse(byte[] data) {
        this.mListener.onResponse(data);
    }

    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}
