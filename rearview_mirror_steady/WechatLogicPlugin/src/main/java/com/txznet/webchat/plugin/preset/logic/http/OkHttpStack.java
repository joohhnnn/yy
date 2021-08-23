package com.txznet.webchat.plugin.preset.logic.http;

import com.android.volley.toolbox.HurlStack;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.OkUrlFactory;
import com.txznet.webchat.comm.plugin.utils.PluginLogUtil;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class OkHttpStack extends HurlStack {
    // 微信上传网络请求消息头分隔符
    public static final String WECHAT_UPLOAD_BOUNDRY = "-------------------------acebdf13572468";

    private OkHttpClient okHttpClient;

    /**
     * Create a OkHttpStack with default OkHttpClient.
     */
    public OkHttpStack() {
        this(new OkHttpClient());
        init();
    }

    /**
     * Create a OkHttpStack with a custom OkHttpClient
     *
     * @param okHttpClient Custom OkHttpClient, NonNull
     */
    public OkHttpStack(OkHttpClient okHttpClient) {
        this.okHttpClient = okHttpClient;
        init();
    }

    OkUrlFactory okUrlFactory;
    SSLContext sslContext;

    private void init() {
        // modified by J on 2018/7/13
        // 强制信任https证书的逻辑不能随意去掉, 某些场景下(例如设备时间不正确)默认逻辑会导致校验不通过,
        // 影响网络请求
        try {
            TrustManager tm = new X509TrustManager() {
                public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{tm}, null);
            okHttpClient.setSslSocketFactory(sslContext.getSocketFactory());
        } catch (Exception e) {
            //throw new AssertionError(e);
            PluginLogUtil.e("init http stack encountered error: " + e.toString());
        }
        okUrlFactory = new OkUrlFactory(okHttpClient);
    }

    @Override
    protected HttpURLConnection createConnection(URL url) throws IOException {
        OkUrlFactory okUrlFactory = new OkUrlFactory(okHttpClient);
        HttpURLConnection urlConnection = okUrlFactory.open(url);
        urlConnection.addRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:42.0) Gecko/20100101 Firefox/42.0");
        urlConnection.addRequestProperty("Referer", "https://wx.qq.com/?&lang=zh_CN");
        urlConnection.addRequestProperty("Accept", "*/*");
//        urlConnection.addRequestProperty("Accept-Encoding", "gzip, deflate");
        urlConnection.addRequestProperty("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3");
        urlConnection.addRequestProperty("Connection", "keep-alive");
        urlConnection.addRequestProperty("DNT", "1");
        // added for image uploading
        urlConnection.setRequestProperty("User-Agent", "QMO Uploader");
        urlConnection.setRequestProperty("Pragma", "no-cache");
        urlConnection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + WECHAT_UPLOAD_BOUNDRY);
        //urlConnection.setRequestProperty("Content-Length", "1563");

        urlConnection.setInstanceFollowRedirects(true);
        return urlConnection;
    }
}
