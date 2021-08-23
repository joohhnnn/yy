package com.txznet.proxy.server.response;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.proxy.BuildConfig;
import com.txznet.proxy.ErrCode;
import com.txznet.proxy.ProxySession;
import com.txznet.proxy.server.NanoHTTPD;
import com.txznet.txz.util.ThreadManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.txznet.proxy.ErrCode.ERROR_CLIENT_MEDIA_WRONG_URL;

public class HttpMediaResponse extends MediaResponseBase {
    protected int mCurPosition = 0;// 默认使用第一个地址
    protected URI mURI;
    protected String mUrl;
    protected long mCurPos;
    protected long mEndPos;
    private CacheInfo mCacheInfo;
    private int mRetryCount;
    private int mRetryCount_403;
    private MediaHttpClient mHttpClient;

    protected HttpMediaResponse(Socket socket, ProxySession sess, String method, long from, long to) {
        super(socket, sess, method, from, to);
    }

    @Override
    protected long getContentLength() throws IOException {
        // 初始化链接
        mUrl = mSess.oriUrls[0];
        try {
            mURI = new URI(mUrl);
        } catch (URISyntaxException e) {
            onHttpMediaError(ERROR_CLIENT_MEDIA_WRONG_URL, "wrong media url:" + mUrl, "播放地址错误:" + mUrl);
        }
        mCurPos = mFrom;
        if (mCurPos < 0) {
            mCurPos = 0;
        }
        waitDownloadUrl();
        // 从缓冲获取资源长度
        mLen = getCacheLength();
        printEmptyData();
        if (mLen > 0) {
            LogUtil.logd(TAG + "getLength:fromCache:" + mLen);
            return mLen;
        }
        mLen = getNetLengthWithData();
        if (mLen > 0) {
            LogUtil.logd(TAG + "getLength:fromNetLengthWithData:" + mLen);
            return mLen;
        }
        // 通过head请求获取资源长度
        mLen = getNetLength();
        LogUtil.logd(TAG + "getLength:fromNetLength:" + mLen);
        return mLen;
    }

    private long getCacheLength() throws IOException {
        mCacheInfo = CacheInfo.createCacheInfo(mSess, -1);
        if (mCacheInfo != null) {
            return mCacheInfo.getTotalSize();
        }
        return -1;
    }

    protected long getNetLengthWithData() throws IOException {
        mRetryCount = 0;
        mHttpClient = new MediaHttpClient() {
            @Override
            public void onMediaError(int err, String desc, String hint) throws IOException {
                onHttpMediaError(err, desc, hint);
            }

            @Override
            public void onResponse(int statusCode, String statusLine) throws IOException {
                processResponseStatusLine(this, statusCode, false);
            }

            @Override
            public void onGetInfo(Map<String, String> headers, String mimeType, long contentLength) throws IOException {
                String contentRange = headers.get("Content-Range");
                try {
                    mLen = Long.parseLong(contentRange.split("/")[1]);
                    LogUtil.logw("media session[" + mSess.getLogId() + "]get media length with data: " + mLen);
                } catch (Exception e) {
                    mLen = -1;
                    LogUtil.logw("media session[" + mSess.getLogId() + "]get media length with data error: " + e.getMessage());
                    cancel();
                }
            }

            @Override
            public void onReadData(byte[] data, int offset, int len) throws IOException {
                // 合并到缓存中
                if (mCacheInfo == null) {
                    mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);
                    mCacheInfo.addCacheBlock(mCurPos, data, offset, len);
                    mSess.param.callback.onBufferingUpdate(mCacheInfo.getCacheBlocks());
                }
            }

            @Override
            public void onIdle() throws IOException {
                printEmptyData();
            }

            @Override
            public void onConnectTimeout() throws IOException {
                LogUtil.logw("media session[" + mSess + "]get length onConnectTimeout");
            }

            @Override
            public void onReadTimeout() throws IOException {
                LogUtil.logw("media session[" + mSess + "]get length onReadTimeout");
            }
        };
        mEndPos = mCurPos + MAX_CACHE_SIZE - 1;
        do {
            mURI = mHttpClient.getMedia(mURI, mCurPos, mEndPos);
            printEmptyData();
            int retry = mHttpClient.getSuggestRetryDelay();
            retry(retry);
            if (retry > 0) {
                changeOtherURI();
            }
        } while (mHttpClient.getSuggestRetryDelay() > 0);
        mHttpClient.cancel();
        return mLen;
    }

    private long getNetLength() throws IOException {
        mRetryCount = 0;
        mHttpClient = new MediaHttpClient() {
            @Override
            public void onMediaError(int err, String desc, String hint) throws IOException {
                onHttpMediaError(err, desc, hint);
            }

            @Override
            public void onResponse(int statusCode, String statusLine) throws IOException {
                processResponseStatusLine(this, statusCode, true);
            }

            @Override
            public void onGetInfo(Map<String, String> headers, String mimeType, long contentLength) throws IOException {
                LogUtil.logw("media session[" + mSess + "]get media length: " + contentLength);
                mLen = contentLength;
            }

            @Override
            public void onReadData(byte[] data, int offset, int len) throws IOException {
            }

            @Override
            public void onIdle() throws IOException {
                printEmptyData();
            }

            @Override
            public void onConnectTimeout() throws IOException {
                LogUtil.logw("media session[" + mSess + "]get length onConnectTimeout");
            }

            @Override
            public void onReadTimeout() throws IOException {
                LogUtil.logw("media session[" + mSess + "]get length onReadTimeout");
            }
        };

        do {
            LogUtil.logw("media session[" + mSess + "]get media length begin " + mURI);
            mURI = mHttpClient.headMedia(mURI);
            LogUtil.logw("media session[" + mSess + "]get media length end");
            printEmptyData();
            int retry = mHttpClient.getSuggestRetryDelay();
            retry(retry);
            if (retry > 0) {
                changeOtherURI();
            }
            printEmptyData();
        } while (mHttpClient.getSuggestRetryDelay() > 0);
        mHttpClient.cancel();
        mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);
        return mLen;
    }


    private void changeOtherURI() {
        // TODO: 2018/11/7 如果这个链接错误，获取不到数据，需要这边去通知错误出去
        mUrl = mSess.oriUrls[++mCurPosition % mSess.oriUrls.length];
        try {
            mURI = new URI(mUrl);
        } catch (URISyntaxException e) {
            LogUtil.e(TAG, "switch url:" + mUrl + "," + e.toString());
        }
    }

    private void processResponseStatusLine(MediaHttpClient response, int statusCode, boolean isHeadReq) {
        if (statusCode / 100 == 4) {
            //只有遇到4XX的才为不可播放地址
//            MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_ERROR);
        }
        if (statusCode / 100 != 2) {
            LogUtil.logw("media session[" + mSess + "]get data url[" + statusCode + "]: " + mHttpClient.getUri());
            if (statusCode == 302) {
                return;
            }
        }
        if (statusCode == 400) {
            HttpMediaResponse.this.onHttpMediaError(ErrCode.ERROR_CLIENT_MEDIA_BAD_REQUEST, "uri media bad request: " + mUrl, "音频已下架");
            response.cancel();
            return;
        }
        if (statusCode == 403) {
            ++mRetryCount_403;
            if (mRetryCount_403 < 5) {
                LogUtil.logw("uri media forbidden, try again, current_retry=" + mRetryCount_403);
                try {
                    mURI = mHttpClient.getMedia(mURI, mCurPos, mEndPos);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                HttpMediaResponse.this.onHttpMediaError(ErrCode.ERROR_CLIENT_MEDIA_FILE_FORBIDDEN, "uri media forbidden: " + mUrl, "音频已下架");
                response.cancel();
            }
            return;
        }
        if (statusCode == 404) {
            HttpMediaResponse.this.onHttpMediaError(ErrCode.ERROR_CLIENT_MEDIA_NOT_FOUND, "uri media not found: " + mUrl, "音频不存在");
            response.cancel();
            return;
        }
        if (statusCode != 206 && (isHeadReq && statusCode != 200)) {
            response.cancel();
        } else {
            mRetryCount_403 = 0;
            //表示这个音频连接可以访问得到,
//        mBackUrl.clear();//清空掉备份地址
        }
    }

    @Override
    protected NanoHTTPD.InputStreamWrapper getData() throws IOException {
        return new NanoHTTPD.InputStreamWrapper() {
            @Override
            public void printData(OutputStream os) throws IOException {
                getDataInner();
            }
        };
    }

    private void getDataInner() throws IOException {
        if (mCacheInfo == null) {
            if (mLen != 0) {
                mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);
            }
            if (mCacheInfo == null) {
                LogUtil.loge("mCacheInfo is null on getData, have error");
                cancel();//存储空间不足，
            }
        }
        if (mHttpClient != null) {
            mHttpClient.cancel();
            mHttpClient = null;
        }
        printEmptyData();
        mRetryCount = 0;
        try {
            // 通知当前缓冲情况
            mSess.param.callback.onBufferingUpdate(mCacheInfo.getCacheBlocks());
            CacheInfo.CacheData cacheData = new CacheInfo.CacheData();
            long total = mFrom;
            // 计算需要的数据量
            long need = MAX_CACHE_SIZE;
            while (true) {
                mEndPos = mCurPos + need - 1;
                if (mEndPos > mTo) {
                    mEndPos = mTo;
                }
                // 尝试从缓存读取数据
                mCacheInfo.getCacheDataOri(cacheData, mCurPos, mEndPos, false);
                if (cacheData.data != null && cacheData.len > 0) {
                    // 写入缓存数据
                    printData(cacheData.data, 0, cacheData.len);
                    mCurPos += cacheData.len;
                    total += cacheData.len;
                    LogUtil.logd("media session[" + mSess + "]write cache data size=" + cacheData.len);
                }
                // 缓存不足
                if (mEndPos > mCurPos) {
                    printEmptyData();
                    invokeDownloadTask();
                    // 等待下载
                    SystemClock.sleep(WAIT_DATA_TIME);
                }
                if (total >= mTo) {
                    LogUtil.logi("media session[" + mSess + "]write file data complete");
                    break;
                }
                // 限制单位时间内的写入数据量，避免内存暴涨
                if (!waitNeedData()) {
                    return;
                }
                LogUtil.logd("media session[" + mSess + "]need data size=" + need + ", now=" + total + ", to=" + mTo);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
            LogUtil.logd("media session[" + mSess + "]need data exception, e=" + e);
        } finally {
            cancel();
        }
    }

    private boolean isTaskRunning;

    // 下载资源任务
    private void invokeDownloadTask() {
        if (isTaskRunning) {
            return;
        }
        isTaskRunning = true;
        ThreadManager.getPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    mDownloadCurPos = mCurPos;
                    // 计算需要的数据量
                    while (true) {
                        if (mHttpClient == null) {
                            createGetDataClient();
                        }
                        if (!waitNeedDownload()) {
                            return;
                        }
                        try {
                            do {
                                waitDownloadUrl();
                                long need = mDownloadCurPos + MAX_CACHE_SIZE;
                                if (need > mTo + 1) {
                                    need = mTo + 1;
                                }
                                LogUtil.logd(TAG + "media session[" + mSess + "]get net data[" + mDownloadCurPos + "~" + need + "]|[" + mFrom + "~" + mTo + "]");
                                printEmptyData();
                                mURI = mHttpClient.getMedia(mURI, mDownloadCurPos, need);
                                printEmptyData();
                                int delay = mHttpClient.getSuggestRetryDelay();
                                retry(delay);
                            } while (mHttpClient.getSuggestRetryDelay() > 0);
                        } catch (IOException e) {
                            if (mHttpClient != null) {
                                mHttpClient.cancel();
                            }
                        }
                        if (mDownloadCurPos >= mTo) {
                            LogUtil.logi("media session[" + mSess.getLogId() + "]write net data complete");
                            isTaskRunning = false;
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (mHttpClient != null) {
                        mHttpClient.cancel();
                    }
                }
            }
        });
    }

    private boolean waitNeedDownload() throws IOException {
        while (true) {
            // TODO: 2018/11/7  限流的算法？
            try {
                if (mSocket == null || mSocket.isClosed()) {
                    throw new IOException("socket is closed");
                }
                // 强制打开限流，直接下载
                if (mSess.param.needMoreData) {
                    break;
                }
                // FIXME: 2019/7/27 ID3v1协议会直接请求末端128字节，此时播放进度为0
                if (mDownloadCurPos + 128 >= mLen) {
                    break;
                }
                // 开放前一片的下载
                if (mDownloadCurPos < MAX_CACHE_SIZE) {
                    break;
                }
                long wTotal = mDownloadCurPos;
                float wPercent = wTotal * 1f / mLen;
                float pPercent = mSess.param.callback.getPlayPercent();
                // 写入量小于播放量, 如seekTo操作，放行
                if (wPercent < pPercent) {
                    break;
                }
                // 写入量小于播放量 + 20%
                if (wPercent - pPercent < 0.20) {
                    break;
                }
                // 有写入量，但解码器并没有解析出文件信息
                if (wPercent > 0 && mSess.param.callback.getDuration() == 0) {
                    break;
                }
                try {
                    if (BuildConfig.DEBUG) {
                        LogUtil.logd("media session[" + mSess + "] need more net data wait (" + pPercent + "/" + wPercent + ", " + mDownloadCurPos + "/" + mLen + ")");
                    }
                    printEmptyData();
                    Thread.sleep(WAIT_DATA_TIME);
                    printEmptyData();
                } catch (InterruptedException e) {
                }
            } catch (Exception e) {
                return false;
            }
        }
        printEmptyData();
        return true;
    }

    private void retry(int delay) throws IOException {
        try {
            if (delay > 0) {
                ++mRetryCount;
                LogUtil.logw("media session[" + mSess + "]get data need retry: " + mRetryCount);
            }
            while (delay > 0) {
                Thread.sleep(delay >= WAIT_DATA_TIME ? WAIT_DATA_TIME : delay);
                printEmptyData();
                delay -= WAIT_DATA_TIME;
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private long mDownloadCurPos; // 当前下载位置

    private void createGetDataClient() {
        mHttpClient = new MediaHttpClient() {
            @Override
            public void onReadEmpty(long endPos) {
                LogUtil.logd(TAG + "onReadEmpty:data:" + endPos);
                setSuggestRetryDelay(mSuggestRetryDelay);
                cancel();
            }

            @Override
            public void onMediaError(int errorCode, String desc, String hint) throws IOException {
                onHttpMediaError(errorCode, desc, hint);
            }

            @Override
            public void onResponse(int statusCode, String statusLine) throws IOException {
                if (statusCode == 416) {
                    onHttpMediaError(ErrCode.ERROR_CLIENT_MEDIA_URL_CHANGE, "播放路径发生变化", "为您重新加载中");
                    cancel();
                    return;
                }
                processResponseStatusLine(this, statusCode, false);
            }

            @Override
            public void onGetInfo(Map<String, String> headers, String mimeType, long contentLength) throws IOException {
                String contentRange = headers.get("Content-Range");
                long lenTmp = 0;
                try {
                    lenTmp = Long.parseLong(contentRange.split("/")[1]);
                } catch (Exception e) {
                }
                if (lenTmp != mLen) {
                    LogUtil.loge("test:play:error:content_length:" + mSess + "[" + lenTmp + "/" + mLen + "]" + mUrl);
                    mLen = lenTmp;
                    onHttpMediaError(ErrCode.ERROR_CLIENT_MEDIA_URL_CHANGE, "播放路径发生变化", "为您重新加载中");
                }
            }

            @Override
            public void onReadData(byte[] data, int offset, int len) throws IOException {
                setSuggestRetryDelay(0);
                // 合并到缓存中
                if (mCacheInfo != null) {
                    mCacheInfo.addCacheBlock(mDownloadCurPos, data, offset, len);
                    mSess.param.callback.onBufferingUpdate(mCacheInfo.getCacheBlocks());
                }
                LogUtil.logi("media session[" + mSess.getLogId() + "]write net data [" + mDownloadCurPos + "/" + (mDownloadCurPos + len - 1) + "]");
                mDownloadCurPos += len;
            }

            @Override
            public void onIdle() throws IOException {
                printEmptyData();
            }

            @Override
            public void onConnectTimeout() throws IOException {
                LogUtil.logw("media session[" + mSess + "]get data onConnectTimeout");
            }
        };
    }

    private void onHttpMediaError(int errorCode, String desc, String hint) {
        LogUtil.loge("media session[" + mSess + "]media error: " + errorCode + " " + desc);
        mSess.param.callback.onError(errorCode, desc, hint);
    }

    @Override
    public void cancel() {
        super.cancel();
        if (mHttpClient != null) {
            mHttpClient.cancel();
        }
    }

    // 等待
    private void waitDownloadUrl() throws IOException {
        printEmptyData();
        while (TextUtils.isEmpty(mUrl) || mURI == null) {
            printEmptyData();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            printEmptyData();
        }
    }
}
