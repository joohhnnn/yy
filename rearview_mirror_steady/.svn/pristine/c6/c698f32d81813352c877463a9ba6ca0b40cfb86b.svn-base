package com.txznet.music.util;

import android.util.SparseArray;

import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * Created by brainBear on 2017/11/6.
 * HttpURLConnection的工具类
 */

public class HttpUtils {

    private static final String TAG = Constant.LOG_TAG_UTILS + ":HttpUtils";
    private static int CONNECT_TIMEOUT = 5000;
    private static int READ_TIMEOUT = 5000;

    private HttpUtils() {
    }

    /**
     * 获取网络文件大小
     */
    public static long getFileLength(String downloadUrl) throws IOException {
        if (downloadUrl == null || "".equals(downloadUrl)) {
            return 0L;
        }
        URL url = new URL(downloadUrl);
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("HEAD");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            conn.setRequestProperty("Accept-Encoding", "gzip, deflate");
            conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7,ja;q=0.6");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Mobile Safari/537.36");
            return (long) conn.getContentLength();
        } catch (IOException e) {
            return 0L;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public static int downloadFile(String url, String path) {
        File file = new File(path);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

        HttpURLConnection httpURLConnection = null;
        FileOutputStream fileOutputStream = null;
        try {
            URL _url = new URL(url);
            if (_url.getProtocol().toLowerCase().equals("https")) {
                trustAllHosts();
                httpURLConnection = (HttpsURLConnection) _url.openConnection();
                ((HttpsURLConnection) httpURLConnection).setHostnameVerifier((hostname, session) -> true);
            } else {
                httpURLConnection = (HttpURLConnection) _url.openConnection();
            }
            httpURLConnection.setConnectTimeout(CONNECT_TIMEOUT);
            httpURLConnection.setReadTimeout(READ_TIMEOUT);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setInstanceFollowRedirects(false);
            httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
            httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7,ja;q=0.6");
            httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Mobile Safari/537.36");

            httpURLConnection.connect();

            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode / 100 == 2) {
                InputStream inputStream = httpURLConnection.getInputStream();
                fileOutputStream = new FileOutputStream(path);
                byte[] bytes = new byte[4 * 1024];
                int length = 0;
                while ((length = inputStream.read(bytes)) != -1) {
                    fileOutputStream.write(bytes, 0, length);
                }
//                fileOutputStream.flush();
                return 0;
            } else {
                Logger.e(TAG, "response code:" + responseCode);
                return -1;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            Logger.e(TAG, e.toString());
            return -2;
        } catch (SSLHandshakeException e) {
            return downloadFile(url.replace("https", "http"), path);
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, e.toString());
            return -3;
        } finally {
            if (null != fileOutputStream) {
                safelyClose(fileOutputStream);
            }
            if (null != httpURLConnection) {
                httpURLConnection.disconnect();
            }
        }
    }

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safelyClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
            Logger.e(TAG, e.toString());
        }
    }

    public static void sendGetRequest(final String url, Map<String, Object> headers, final HttpCallbackListener listener) {
        sendGetRequest(url, headers, CONNECT_TIMEOUT, listener);
    }

    private static SparseArray<Future> futures = new SparseArray<>();

    public static void sendGetRequest(final String url, Map<String, Object> headers, final int timeout, final HttpCallbackListener listener) {
        sendRequest("GET", url, headers, timeout, listener);
    }

    public static void sendPostRequest(final String url, Map<String, Object> headers, final int timeout, final HttpCallbackListener listener) {
        sendRequest("POST", url, headers, timeout, listener);
    }

    private static void sendRequest(String method, final String url, Map<String, Object> headers, final int timeout, final HttpCallbackListener listener) {
        Future<?> submit = ThreadManager.getPool().submit(() -> {
            Logger.d(TAG, "request real url:%s", url);
            HttpURLConnection httpURLConnection = null;
            try {
                URL _url = new URL(url);
                httpURLConnection = (HttpURLConnection) _url.openConnection();
                httpURLConnection.setConnectTimeout(timeout);
                httpURLConnection.setReadTimeout(timeout);
                httpURLConnection.setRequestMethod(method);
                httpURLConnection.setUseCaches(false);
                httpURLConnection.setDoInput(true);
                httpURLConnection.setInstanceFollowRedirects(false);
                httpURLConnection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
                httpURLConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
                httpURLConnection.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8,zh-TW;q=0.7,ja;q=0.6");
                httpURLConnection.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; Android 6.0; Nexus 5 Build/MRA58N) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Mobile Safari/537.36");
                if (headers != null) {
                    for (String key : headers.keySet()) {
                        httpURLConnection.setRequestProperty(key, "" + headers.get(key));
                    }
                }
                httpURLConnection.connect();

                int responseCode = httpURLConnection.getResponseCode();
                if (responseCode / 100 == 2) {
                    callSuccess(processStream(httpURLConnection.getInputStream(), httpURLConnection.getContentEncoding()), listener);
                } else {
                    callError(responseCode, listener);
                }
            } catch (MalformedURLException e) {
                Logger.e(TAG, e.toString());
                callError(-1, listener);
            } catch (IOException e) {
                Logger.e(TAG, e.toString());
                callError(-2, listener);
            } catch (Exception e) {
                Logger.e(TAG, e.toString());
            } finally {
                if (null != httpURLConnection) {
                    httpURLConnection.disconnect();
                }
                futures.remove(listener.hashCode());
            }
        });
        futures.put(listener.hashCode(), submit);
    }

    public static void shutdownRequest() {
        if (futures != null) {
            for (int i = 0, len = futures.size(); i < len; i++) {
                Future future = futures.get(futures.keyAt(i));
                if (future != null) {
                    future.cancel(true);
                }

            }
            futures.clear();
        }
    }

    private static String processStream(InputStream inputStream, String encoding) throws IOException {
        if ("gzip".equals(encoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }
        byte[] bytes = new byte[10 * 1024];
        int length = 0;
        StringBuilder sb = new StringBuilder();
        while ((length = inputStream.read(bytes)) >= 0) {
            sb.append(new String(bytes, 0, length));
        }
        return sb.toString();
    }


    private static void callSuccess(final String response, final HttpCallbackListener listener) {
        if (null != listener) {
            AppLogic.runOnUiGround(() -> listener.onSuccess(response));
        }
    }

    private static void callError(final int errorCode, final HttpCallbackListener listener) {
        if (null != listener) {
            AppLogic.runOnUiGround(() -> listener.onError(errorCode));
        }
    }

    public interface HttpCallbackListener {

        void onSuccess(String response);


        void onError(int errorCode);

    }

}
