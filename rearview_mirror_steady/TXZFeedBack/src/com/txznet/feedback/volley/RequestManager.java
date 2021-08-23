package com.txznet.feedback.volley;

import java.security.KeyStore;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpVersion;
import org.apache.http.client.CookieStore;
import org.apache.http.client.params.HttpClientParams;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.protocol.HTTP;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.RetryPolicy;
import com.android.volley.toolbox.HttpClientStack;
import com.android.volley.toolbox.Volley;
import com.txznet.feedback.AppLogic;

public class RequestManager {
    private DefaultHttpClient getHttpClient(CookieStore cookieStore) {
        final HttpParams httpParams = new BasicHttpParams();
        httpParams.setBooleanParameter("http.protocol.handle-redirects", false);
        ConnManagerParams.setTimeout(httpParams, 1000);
        HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
        HttpConnectionParams.setSoTimeout(httpParams, 5000);
        ConnManagerParams.setMaxConnectionsPerRoute(httpParams, new ConnPerRouteBean(8));
        ConnManagerParams.setMaxTotalConnections(httpParams, 24);
        HttpProtocolParams.setUseExpectContinue(httpParams, true);
        HttpConnectionParams.setStaleCheckingEnabled(httpParams, false);
        HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setContentCharset(httpParams, HTTP.UTF_8);
        HttpClientParams.setRedirecting(httpParams, false);
        String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0";
        HttpProtocolParams.setUserAgent(httpParams, userAgent);
        HttpConnectionParams.setTcpNoDelay(httpParams, true);
        HttpConnectionParams.setSocketBufferSize(httpParams, 8192);
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));

        KeyStore trustStore = null;
        try {
            trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            trustStore.load(null, null);
            SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
            sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            schemeRegistry.register(new Scheme("https", sf, 443));
        } catch (Exception e) {
            e.printStackTrace();
        }
        ClientConnectionManager manager = new ThreadSafeClientConnManager(httpParams, schemeRegistry);
        DefaultHttpClient httpClient = new DefaultHttpClient(manager, httpParams);
        if (cookieStore != null) {
            httpClient.setCookieStore(cookieStore);
        }
        return httpClient;
    }

    private RequestQueue mRequestQueue;
    private PersistentCookieStore mCookieStore;

    public RequestManager(Context context) {
        mCookieStore = new PersistentCookieStore(context);
        mCookieStore.clear();
        mRequestQueue = Volley.newRequestQueue(context, new HttpClientStack(getHttpClient(mCookieStore)));
        start();
    }

    private static RequestManager mInstance;

    public static RequestManager getInstance() {
        if (mInstance == null) {
            synchronized (RequestManager.class) {
                mInstance = new RequestManager(AppLogic.getApp());
            }
        }
        return mInstance;
    }

    public void start() {
        mRequestQueue.start();
    }

    public void stop() {
        mRequestQueue.stop();
    }

    public void addRequest(Request<?> request) {
        addRequest(request, null);
    }

    public void addRequest(Request<?> request, RetryPolicy retryPolicy) {
        if (request == null) {
            return;
        }
        if (retryPolicy == null) {
            retryPolicy = RequestManager.newDefaultRetryPolicy();
        }
        request.setRetryPolicy(retryPolicy);
        mRequestQueue.add(request);
    }

    public void cancelAll() {
        mRequestQueue.cancelAll(new RequestQueue.RequestFilter() {
            @Override
            public boolean apply(Request<?> request) {
                return true;
            }
        });
    }

    public String getCookieVal(String name) {
        List<Cookie> cookies = mCookieStore.getCookies();
        for (int i = 0; i < cookies.size(); i++) {
            if (cookies.get(i).getName().equals(name)) {
                return cookies.get(i).getValue();
            }
        }
        return null;
    }


    public static DefaultRetryPolicy newDefaultRetryPolicy() {
        return new DefaultRetryPolicy();
    }

    public static DefaultRetryPolicy newLongRetryPolicy() {
        return new DefaultRetryPolicy(1000 * 60, 1, 0);
    }

    public void reset() {
        cancelAll();
        mCurVersion = "";
        mCookieStore.clear();
    }

//    public JSONObject getBaseRequest() {
//        JSONObject BaseRequest = new JSONObject();
//        JSONObject ret = new JSONObject();
//        try {
//            BaseRequest.put("Uin", Long.parseLong(getWxUin()));
//            BaseRequest.put("Sid", "" + getWxSid());
//            BaseRequest.put("Skey", "" + getWxSkey());
//            BaseRequest.put("DeviceID", "e" + genStr(15));
//            ret.put("BaseRequest", BaseRequest);
//        } catch (JSONException e) {
//        }
//        return ret;
//    }

    private String mCurVersion = "";

    public void setCurVersion(String curVersion) {
        mCurVersion = curVersion;
    }

    public String getCurVersion() {
        return mCurVersion;
    }

    public static String genStr(int length) {
        StringBuilder random = new StringBuilder();
        Random r = new Random();
        for (int i = 0; i < length; ++i) {
            random.append(r.nextInt(10));
        }
        return random.toString();
    }
}
