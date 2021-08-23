package com.txznet.feedback.volley;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;

/**
 * Created by ASUS User on 2015/7/15.
 */
public class RawRequest extends Request<byte[]> {

    private final Response.Listener<byte[]> mListener;

    public RawRequest(int method, String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        super(method, url, errorListener);
        this.mListener = listener;
    }

    public RawRequest(String url, Response.Listener<byte[]> listener, Response.ErrorListener errorListener) {
        this(Method.GET, url, listener, errorListener);
    }

    @SuppressWarnings("deprecation")
	@Override
    public byte[] getPostBody() throws AuthFailureError {
        return super.getPostBody();
    }

    protected void deliverResponse(byte[] data) {
        this.mListener.onResponse(data);
    }

    protected Response<byte[]> parseNetworkResponse(NetworkResponse response) {
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response));
    }
}
