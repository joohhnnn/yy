package com.txznet.music.net;

import android.util.SparseArray;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by brainBear on 2017/11/6.
 * HttpURLConnection的工具类
 */

public class HttpUtils {
    private static final String TAG = "HttpUtils:";
    private static int CONNECT_TIMEOUT = 5000;
    private static int READ_TIMEOUT = 5000;

    /**
     * 最大队列长度
     */
    private static final int MAX_QUEUE_LENGTH = 27;

    /**
     * 常驻内在线程数
     */
    private static final int ALIVE_THREAD_SIZE = 1;

    /**
     * 最大活动线程数
     */
    private static final int MAX_THREAD_SIZE = 3;

    /**
     * 线程空置多长时间销毁
     */
    private static final int THREAD_ALIVE_SECONDS = 60;


    private static final ThreadPoolExecutor executorService = new ThreadPoolExecutor(ALIVE_THREAD_SIZE,
            MAX_THREAD_SIZE, THREAD_ALIVE_SECONDS, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(MAX_QUEUE_LENGTH),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private HttpUtils() {

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
            httpURLConnection = (HttpURLConnection) _url.openConnection();
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
                fileOutputStream.flush();
                return 0;
            } else {
                LogUtil.e(TAG, "response code:" + responseCode + ", url:" + url);
                return -1;
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.toString());
            return -2;
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.toString());
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


    private static void safelyClose(Closeable closeable) {
        try {
            closeable.close();
        } catch (IOException e) {
            e.printStackTrace();
            LogUtil.e(TAG, e.toString());
        }
    }

    public static void sendGetRequest(final String url, Map<String, Object> headers, final HttpCallbackListener listener) {
        sendGetRequest(url, headers, CONNECT_TIMEOUT, listener);
    }

    static SparseArray<Future> futures = new SparseArray<>();

    public static void sendGetRequest(final String url, Map<String, Object> headers, final int timeout, final HttpCallbackListener listener) {
        sendRequest("GET", url, headers, null, timeout, listener);
    }

    public static void sendPostRequest(final String url, Map<String, Object> headers, final int timeout, final HttpCallbackListener listener) {
        sendRequest("POST", url, headers, null, timeout, listener);
    }

    public static void sendPostRequest(final String url, Map<String, Object> headers, String body, final int timeout, final HttpCallbackListener listener) {
        sendRequest("POST", url, headers, body, timeout, listener);
    }

    private static void sendRequest(String method, final String url, Map<String, Object> headers, String body, final int timeout, final HttpCallbackListener listener) {
        Future<?> submit = executorService.submit(new Runnable() {
            @Override
            public void run() {
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
                    if (body != null) {
                        httpURLConnection.setDoOutput(true);
                    }
                    if (headers != null) {
                        for (String key : headers.keySet()) {
                            httpURLConnection.setRequestProperty(key, "" + headers.get(key));
                        }
                    }
                    httpURLConnection.connect();
                    if (body != null) {
                        httpURLConnection.getOutputStream().write(body.getBytes());
                        httpURLConnection.getOutputStream().flush();
                    }
                    int responseCode = httpURLConnection.getResponseCode();
                    if (responseCode / 100 == 2) {
                        callSuccess(processStream(httpURLConnection.getInputStream()), listener);
                    } else {
                        callError(responseCode, listener);
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                    Logger.e(TAG, e.toString());
                    callError(-1, listener);
                } catch (IOException e) {
                    e.printStackTrace();
                    Logger.e(TAG, e.toString());
                    callError(-2, listener);
                } finally {
                    if (null != httpURLConnection) {
                        httpURLConnection.disconnect();
                    }
                    futures.remove(listener.hashCode());
                }
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


    private static String processStream(InputStream inputStream) throws IOException {
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
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(response);
                }
            });
        }
    }

    private static void callError(final int errorCode, final HttpCallbackListener listener) {
        if (null != listener) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    listener.onError(errorCode);
                }
            });
        }
    }

    public interface HttpCallbackListener {

        void onSuccess(String response);


        void onError(int errorCode);

    }

}
