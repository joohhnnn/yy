package com.txznet.audio.server.response;

import android.text.TextUtils;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.audio.NetAudio;
import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.data.dao.DaoManager;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

public class HttpMediaResponse extends MediaResponseBase {
    private final int DATA_PIECE_SIZE_MIN = 1024 * 4 * 8 * 2;//原本32k
    //    protected List<String> mBackUrl = new ArrayList<String>();// 备用请求地址
//    protected int mCurPosition = -1;// 默认使用第一个地址
    private final NetAudio mNetAudio;
    protected URI mURI = null;
    protected String mUrl = null;
    protected long mExpTime;
    protected long mCurPos = 0;
    protected boolean isNetPoor = false;
    CacheInfo mCacheInfo = null;
    int mRetryCount = 0;
    MediaHttpClient<HttpMediaResponse> mHttpClient;

    protected HttpMediaResponse(Socket out, SessionInfo sess, long from, long to) {
        super(out, sess, from, to);

        mNetAudio = (NetAudio) mSess.audio;
        mUrl = mNetAudio.getUrl();
        mExpTime = mNetAudio.getExpTime();
        try {
            mURI = new URI(mUrl);
        } catch (URISyntaxException e) {
            HttpMediaResponse.this.onHttpMediaError(new Error(Error.ERROR_CLIENT_MEDIA_WRONG_URL
                    , "wrong media url:" + mUrl, "播放地址错误:" + mUrl));
        }
    }

    public void notifyUrlChangeListener() {

    }

    public void onHttpMediaError(Error err) {
        LogUtil.loge("media session[" + mSess.getLogId() + "]media error: "
                + err.getErrorCode() + " " + err.getDesc());

        mSess.player.notifyError(err);
    }

    @Override
    public void cancel() {
        super.cancel();

        if (mHttpClient != null) {
            mHttpClient.cancel();
        }
//        if (mCacheInfo != null) {
//            mCacheInfo = null;
//        }
    }

    // 等待
    protected void waitDownloadUrl(OutputStream out) throws IOException {
//        if (mLen <= 0 && isNetPoor) {
//            HttpMediaResponse.this.onHttpMediaError(new Error(Error.ERROR_CLIENT_NET_OFFLINE
//                    , "not connect to net:" + mUrl, "网络错误:" + (mUrl!=null?mUrl:"")));
//        }

        while (TextUtils.isEmpty(mUrl) || mURI == null) {
            printEmptyData(out);
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
            }
            printEmptyData(out);
        }
    }

    @Override
    protected long getLength(final OutputStream out) throws IOException {
        mCurPos = mFrom;
        if (mCurPos < 0) {
            mCurPos = 0;
        }
        waitDownloadUrl(out);
        mLen = getCacheLength(out);
        printEmptyData(out);
        if (mLen > 0) {
            LogUtil.logd(TAG + "getLength:fromCache:" + mLen);
            return mLen;
        }
        mLen = getNetLengthWithData(out);
        if (mLen > 0) {
//            if (mSess.audio instanceof QQMusicAudio) {
//                if (mCurPosition > 0) {
//                    notifyUrlChangeListener();
//                }
//                //保存数据库
//                QQMusicAudio qqMusicAudio = (QQMusicAudio) mSess.audio;
//                Audio audio = (qqMusicAudio.getAudio());
//
//                QQTicketTable qqTicketTable = new QQTicketTable(audio.getId(), audio.getSid(), mUrl, qqMusicAudio.getUrlHashCode(), mExpTime);
//                LogUtil.logd("test:play:error:saveDB:" + qqTicketTable.toString());
//                DaoManager.getInstance().saveTicketUrl(qqTicketTable);
//
//            }
            LogUtil.logd(TAG + "getLength:fromNetLengthWithData:" + mLen);
            return mLen;
        }
        mLen = getNetLength(out);
        LogUtil.logd(TAG + "getLength:fromNetLength:" + mLen);
        return mLen;
    }

    protected long getCacheLength(final OutputStream out) throws IOException {
        mCacheInfo = CacheInfo.createCacheInfo(mSess, -1);
        if (mCacheInfo != null) {
            return mCacheInfo.getTotalSize();
        }
        return -1;
    }

    protected long getNetLength(final OutputStream out) throws IOException {
        mRetryCount = 0;

        mHttpClient = new MediaHttpClient<HttpMediaResponse>(
                HttpMediaResponse.this) {
            @Override
            public void onMediaError(Error err) throws IOException {
                HttpMediaResponse.this.onHttpMediaError(err);
            }

            @Override
            public void onResponse(int statusCode, String statusLine)
                    throws IOException {
                processResponseStatusLine(this, statusCode);
            }

            @Override
            public void onGetInfo(Map<String, String> headers, String mimeType,
                                  long contentLength) throws IOException {
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get media length: " + contentLength);
                mLen = contentLength;
            }

            @Override
            public void onReadData(byte[] data, int offset, int len)
                    throws IOException {
            }

            @Override
            public void onIdle() throws IOException {
                HttpMediaResponse.super.printEmptyData(out);
            }

            @Override
            public void onConnectTimout() throws IOException {
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get length onConnectTimout");
            }

            @Override
            public void onReadTimout() throws IOException {
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get length onReadTimout");
            }
        };

//        do {
//            waitDownloadUrl(out);
//            try {
//                if (!mBackUrl.isEmpty()) {
//                    LogUtil.logd("mBackUrl =" + mBackUrl.size());
//                    mCurPosition = ++mCurPosition % mBackUrl.size();
//                    mURI = new URI(mBackUrl.get(mCurPosition));
//                }
//                LogUtil.logd("media session[" + mSess.getLogId()
//                        + "] request url=" + mURI.toString());
//            } catch (Exception e) {
//                LogUtil.loge("media session[" + mSess.getLogId()
//                        + "]get url error,url is " + mBackUrl.get(mCurPosition));
//                mURI = null;
//                continue;
//            }
//
//            mHttpClient.headMedia(mURI);
//
//            printEmptyData(out);
//            int delay = mHttpClient.getSuggestRetryDelay();
//            retry(delay, out);
//        } while (mHttpClient.getSuggestRetryDelay() > 0);

        do {
            mURI = mHttpClient.headMedia(mURI);
            printEmptyData(out);
            int retry = mHttpClient.getSuggestRetryDelay();
            retry(retry, out);

            if (retry > 0) {
                mUrl = mNetAudio.switchUrl();
                try {
                    mURI = new URI(mUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "switch url:" + e.toString());
                }
            }
        } while (mHttpClient.getSuggestRetryDelay() > 0);

        MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_SUCCESS);
        mHttpClient.cancel();

        mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);

        return mLen;
    }

    private void processResponseStatusLine(MediaHttpClient<HttpMediaResponse> response, int statusCode) {
        if (statusCode / 100 == 4) {
            //只有遇到4XX的才为不可播放地址
            MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_ERROR);
        }

        if (statusCode / 100 != 2) {
            LogUtil.logw("media session[" + mSess.getLogId()
                    + "]get data url[" + statusCode + "]: "
                    + mHttpClient.mUri.toString() + "");
            if (statusCode == 302) {
                return;
            }
        }

        if (statusCode == 400) {
            HttpMediaResponse.this.onHttpMediaError(new Error(
                    Error.ERROR_CLIENT_MEDIA_BAD_REQUEST,
                    "uri media bad request: " + mUrl, "音频已下架"));
            response.cancel();
            return;
        }
        if (statusCode == 403) {
            HttpMediaResponse.this.onHttpMediaError(new Error(
                    Error.ERROR_CLIENT_MEDIA_FILE_FORBIDDEN,
                    "uri media forbidden: " + mUrl, "音频已下架"));
            response.cancel();
            return;
        }
        if (statusCode == 404) {
            HttpMediaResponse.this.onHttpMediaError(new Error(
                    Error.ERROR_CLIENT_MEDIA_NOT_FOUND,
                    "uri media not found: " + mUrl, "音频不存在"));
            response.cancel();
            return;
        }


        if (statusCode != 206) {
            response.cancel();
            return;
        }
        //表示这个音频连接可以访问得到,
//        mBackUrl.clear();//清空掉备份地址
    }

    protected long getNetLengthWithData(final OutputStream out)
            throws IOException {
        mRetryCount = 0;

        mHttpClient = new MediaHttpClient<HttpMediaResponse>(
                HttpMediaResponse.this) {
            @Override
            public void onMediaError(Error err) throws IOException {
                HttpMediaResponse.this.onHttpMediaError(err);
            }

            @Override
            public void onReadEmpty(long endPos) {
                LogUtil.logd(TAG + "onReadEmpty:length:" + endPos);
//                mCurPos += endPos;
//                setSuggestRetryDelay(mSuggestRetryDelay);
                cancel();
            }

            @Override
            public void onResponse(int statusCode, String statusLine)
                    throws IOException {
                processResponseStatusLine(this, statusCode);
                mNetAudio.setConnectSuccess();
            }


            @Override
            public void onGetInfo(Map<String, String> headers, String mimeType,
                                  long contentLength) throws IOException {
                String contentRange = headers.get("Content-Range");
                try {
                    mLen = Long.parseLong(contentRange.split("/")[1]);
                    LogUtil.logw("media session[" + mSess.getLogId()
                            + "]get media length with data: " + mLen);
                } catch (Exception e) {
                    mLen = -1;
                    LogUtil.logw("media session[" + mSess.getLogId()
                            + "]get media length with data error: "
                            + e.getMessage());
                    cancel();
                }
                printResponseHeader(out);
            }

            @Override
            public void onReadData(byte[] data, int offset, int len)
                    throws IOException {
                if (mCacheInfo == null) {
                    mCacheInfo = CacheInfo.createCacheInfo(mSess, mLen);
                }

                IOException exp = null;
                try {
                    printData(out, data, offset, len);
                } catch (IOException e) {
                    exp = e;
                }
                if (null != mCacheInfo) {
                    mCacheInfo.addCacheBlock(HttpMediaResponse.this.mCurPos, data,
                            offset, len);
                    mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());
                }
                HttpMediaResponse.this.mCurPos += len;
                if (exp != null) {
                    throw exp;
                }
            }

            @Override
            public void onIdle() throws IOException {
                HttpMediaResponse.super.printEmptyData(out);
            }

            @Override
            public void onConnectTimout() throws IOException {
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get length onConnectTimout");
            }

            @Override
            public void onReadTimout() throws IOException {
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get length onReadTimout");
            }
        };

        long need = mSess.player.getDataPieceSize();

        LogUtil.logd("media session[" + mSess.getLogId() + "]need data size="
                + need + ", now=" + mCurPos + ", to=" + mTo + ", min="
                + DATA_PIECE_SIZE_MIN);

        if (need < DATA_PIECE_SIZE_MIN) {
            need = DATA_PIECE_SIZE_MIN;
        }

        long endPos = mCurPos + need - 1;

//        do {
//            waitDownloadUrl(out);
//
//            LogUtil.logd("media session[" + mSess.getLogId()
//                    + "]get length with data[" + mCurPos + "~" + endPos + "]|["
//                    + mFrom + "~" + mTo + "]");
//
//            try {
//                if (!mBackUrl.isEmpty()) {
//                    LogUtil.logd("mBackUrl is not null,size " + mBackUrl.size()
//                            + ",curPosition=" + mCurPosition);
//                    mCurPosition = ++mCurPosition % mBackUrl.size();
//                    mURI = new URI(mBackUrl.get(mCurPosition));
//                }
//                if (Constant.ISTESTDATA) {
//                    LogUtil.logd("media session[" + mSess.getLogId()
//                            + "] request backurl " + mCurPosition + " url="
//                            + mURI.toString());
//                }
//            } catch (Exception e) {
//                LogUtil.loge("media session[" + mSess.getLogId()
//                        + "]get url error,url is " + mBackUrl.get(mCurPosition < 0 ? 0 : mCurPosition));
//                mURI = null;
//                continue;
//            }
//            mHttpClient.getMedia(mURI, mCurPos, endPos);
//            printEmptyData(out);
//
//            int delay = mHttpClient.getSuggestRetryDelay();
//            retry(delay, out);
//
//        } while (mHttpClient.getSuggestRetryDelay() > 0);


        do {
            mURI = mHttpClient.getMedia(mURI, mCurPos, endPos);
            printEmptyData(out);
            int retry = mHttpClient.getSuggestRetryDelay();
            retry(retry, out);

            if (retry > 0) {
                mUrl = mNetAudio.switchUrl();
                try {
                    mURI = new URI(mUrl);
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG, "switch url:" + e.toString());
                }
            }
        } while (mHttpClient.getSuggestRetryDelay() > 0);

        MonitorUtil.monitorCumulant(Constant.M_URL_PLAY_SUCCESS);

        mHttpClient.cancel();

        return mLen;
    }

    protected void createGetDataClient(final OutputStream out) {
        mHttpClient = new MediaHttpClient<HttpMediaResponse>(
                HttpMediaResponse.this) {
            @Override
            public void onMediaError(Error err) throws IOException {
                HttpMediaResponse.this.onHttpMediaError(err);
            }

            @Override
            public void onReadEmpty(long endPos) {
                LogUtil.logd(TAG + "onReadEmpty:data:" + endPos);
//                mCurPos += endPos;
                setSuggestRetryDelay(mSuggestRetryDelay);
                cancel();
            }

            @Override
            public void onResponse(int statusCode, String statusLine)
                    throws IOException {
                if (statusCode == 416) {
                    if (mSess.audio instanceof QQMusicAudio) {
                        QQMusicAudio qqMusicAudio = (QQMusicAudio) mSess.audio;
                        Audio audio = (qqMusicAudio.getAudio());
                        DaoManager.getInstance().removeTicketUrl(audio.getId(), audio.getSid());
                    }
                    HttpMediaResponse.this.onHttpMediaError(new Error(Error.ERROR_CLIENT_MEDIA_URL_CHANGE, "播放路径发生变化", "为您重新加载中"));
                    this.cancel();
                    return;
                }
                processResponseStatusLine(this, statusCode);
            }

            @Override
            public void onGetInfo(Map<String, String> headers, String mimeType,
                                  long contentLength) throws IOException {
                String contentRange = headers.get("Content-Range");

                long lenTmp = 0;
                try {
                    lenTmp = Long.parseLong(contentRange.split("/")[1]);
                } catch (Exception e) {
                }
                if (lenTmp != mLen) {
                    LogUtil.loge("test:play:error:content_length:" + mSess.getLogId() + "[" + lenTmp + "/" + mLen + "]" + mUrl);
                    mLen = lenTmp;
                    if (mSess.audio instanceof QQMusicAudio) {
                        QQMusicAudio qqMusicAudio = (QQMusicAudio) mSess.audio;
                        Audio audio = (qqMusicAudio.getAudio());
                        DaoManager.getInstance().removeTicketUrl(audio.getId(), audio.getSid());
                    }
                    HttpMediaResponse.this.onHttpMediaError(new Error(Error.ERROR_CLIENT_MEDIA_URL_CHANGE, "播放路径发生变化", "为您重新加载中"));
                }
            }

            @Override
            public void onReadData(byte[] data, int offset, int len)
                    throws IOException {
                setSuggestRetryDelay(0);

                IOException exp = null;
                try {
                    printData(out, data, offset, len);
                } catch (IOException e) {
                    exp = e;
                }
                //TODO:这里会进行IO操作，会有一定的时耗，是否可以开一个线程进行IO操作，加快seek的时候网络请求的耗时
                if (mCacheInfo != null) {
                    mCacheInfo.addCacheBlock(HttpMediaResponse.this.mCurPos, data, offset, len);
                    mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());
                }
//                addCacheBlock(HttpMediaResponse.this.mCurPos, data, offset, len);
                HttpMediaResponse.this.mCurPos += len;
                if (exp != null) {
                    throw exp;
                }
            }

            @Override
            public void onIdle() throws IOException {
                HttpMediaResponse.super.printEmptyData(out);
            }

            @Override
            public void onConnectTimout() throws IOException {
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get data onConnectTimout");
            }
        };
    }

    private void addCacheBlock(final long from, final byte[] data, final int offset, final int len) {
        AppLogic.runOnSlowGround(new Runnable() {
            @Override
            public void run() {
                if (mCacheInfo != null) {
                    //这里可能引发多线程问题，导致mCacheinfo为null
                    mCacheInfo.addCacheBlock(from, data, offset, len);
                    mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());
                }
            }
        });
    }

    @Override
    protected void getData(final OutputStream out) throws IOException {
        LogUtil.logd(TAG + " getData ");
        if (mCacheInfo == null) {
            LogUtil.loge("mCacheInfo is null on getData ,have error");
            cancel();//存储空间不足，
        }
        mHttpClient = null;

        printEmptyData(out);

        mRetryCount = 0;

        List<LocalBuffer> buffers = mCacheInfo.getCacheBlocks();
        mSess.player.notifyDownloading(buffers);
        CacheInfo.CacheData cacheData = new CacheInfo.CacheData();

        while (true) {
            // 等待需要更多的数据
            if (mCurPos != 0 && mCurPos != mFrom) {
                LogUtil.logd(TAG + " waitNeedData");
                if (!waitNeedData(out)) {
                    LogUtil.logw(TAG + " can't exit");
                    return;
                }
            }

            // 计算需要的数据量
            long need = mSess.player.getDataPieceSize();

            LogUtil.logd(TAG + "media session[" + mSess.getLogId()
                    + "]need data size=" + need + ", now=" + mCurPos + ", to="
                    + mTo + ", min=" + DATA_PIECE_SIZE_MIN);

            if (need < DATA_PIECE_SIZE_MIN) {
                need = DATA_PIECE_SIZE_MIN;
            }
            long endPos = mCurPos + need - 1;
            if (endPos > mTo) {
                endPos = mTo;
            }

            if (endPos < mCurPos) {
                LogUtil.logi("media session[" + mSess.getLogId()
                        + "]all data response complete: [" + mCurPos + "~"
                        + endPos + "]|[" + mFrom + "~" + mTo + "]");
                printEmptyData(out);
                return;
            }

            // 尝试从缓存读取数据
            mCacheInfo.getCacheData(cacheData, mCurPos, endPos, true);
            if (cacheData.data != null && cacheData.len > 0) {
                printData(out, cacheData.data, 0, cacheData.len);
                mCurPos += cacheData.len;
                LogUtil.logd(TAG + "media session[" + mSess.getLogId()
                        + "]write cache data size=" + cacheData.len + ",curPos=" + mCurPos);
            }
            if (endPos > mCurPos) {
                if (mHttpClient == null) {
                    createGetDataClient(out);
                }

                do {
                    waitDownloadUrl(out);

                    LogUtil.logd(TAG + "media session[" + mSess.getLogId()
                            + "]get data[" + mCurPos + "~" + endPos + "]|["
                            + mFrom + "~" + mTo + "]");

                    mURI = mHttpClient.getMedia(mURI, mCurPos, endPos);
                    mSess.player.notifyDownloading(mCacheInfo.getCacheBlocks());

                    LogUtil.logd("media session[" + mSess.getLogId()
                            + "]write net data size="
                            + mHttpClient.getReadCount());

                    printEmptyData(out);
                    int delay = mHttpClient.getSuggestRetryDelay();
                    retry(delay, out);
                } while (mHttpClient.getSuggestRetryDelay() > 0);

                mHttpClient.cancel();
            }
        }
    }

    private void retry(int delay, OutputStream out) throws IOException {
        try {
            if (delay > 0) {
                ++mRetryCount;
                LogUtil.logw("media session[" + mSess.getLogId()
                        + "]get data need retry: " + mRetryCount);
                if (/*mRetryCount == 3 | mRetryCount == 7 | */mRetryCount == 15) {
                    // TtsUtil.speakText(Constant.SPEAK_NET_POOR);
                }
            }

            while (delay > 0) {
                Thread.sleep(delay >= WAIT_DATA_TIME ? WAIT_DATA_TIME : delay);
                printEmptyData(out);
                delay -= WAIT_DATA_TIME;
            }
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
