package com.txznet.txz.module.download;

import java.util.Map;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.HttpHeaderParser;


public class RawRequest extends Request<NetworkResponse> {

	private final Response.Listener<NetworkResponse> mListener;
	private byte[] mRequestBody;
	private Map<String, String> mHeaders;
	private NetworkResponse mResponse;

	public RawRequest(int method, String url,
			Response.Listener<NetworkResponse> listener,
			Response.ErrorListener errorListener) {
		super(method, url, errorListener);
		this.mListener = listener;
	}

	public RawRequest(int method, String url, byte[] requestBody,
			Response.Listener<NetworkResponse> listener,
			Response.ErrorListener errorListener) {
		this(method, url, listener, errorListener);
		this.mRequestBody = requestBody;
	}

	public RawRequest(String url, Response.Listener<NetworkResponse> listener,
			Response.ErrorListener errorListener) {
		this(Method.GET, url, listener, errorListener);
	}

	@Override
	public byte[] getBody() {
		return this.mRequestBody;
	}
	
	public RawRequest setHeaders(Map<String, String> headers) {
		mHeaders = headers;
		return this;
	}
	
	@Override
	public Map<String, String> getHeaders() throws AuthFailureError {
		if (mHeaders != null) {
			return mHeaders;
		}
		return super.getHeaders();
	}

	@Override
	protected void deliverResponse(NetworkResponse res) {
		this.mListener.onResponse(res);
	}

	protected Response<NetworkResponse> parseNetworkResponse(NetworkResponse response) {
		mResponse = response;
		return Response.success(response,
				HttpHeaderParser.parseCacheHeaders(response));
	}
}
