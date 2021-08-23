//package com.txznet.audio.server.response;
//
//import android.os.SystemClock;
//import android.text.TextUtils;
//
//import com.txznet.audio.bean.SessionInfo;
//import com.txznet.audio.player.audio.QQMusicAudio;
//import com.txznet.comm.remote.GlobalContext;
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.comm.remote.util.MonitorUtil;
//import com.txznet.comm.util.CollectionUtils;
//import com.txznet.comm.util.StringUtils;
//import com.txznet.loader.AppLogic;
//import com.txznet.music.albumModule.bean.Audio;
//import com.txznet.music.baseModule.Constant;
//import com.txznet.music.baseModule.bean.Error;
//import com.txznet.music.baseModule.dao.DBManager;
//import com.txznet.music.dao.DaoManager;
//import com.txznet.music.playerModule.bean.QQTicketTable;
//import com.txznet.music.playerModule.logic.net.request.ReqProcessing;
//import com.txznet.music.playerModule.logic.net.response.ResponseURL;
//import com.txznet.music.util.TimeUtils;
//import com.txznet.music.utils.DataInterfaceBroadcastHelper;
//import com.txznet.music.utils.JsonHelper;
//import com.txznet.music.utils.NetworkUtil;
//
//import java.io.IOException;
//import java.io.InputStream;
//import java.net.HttpURLConnection;
//import java.net.Socket;
//import java.net.URI;
//import java.net.URISyntaxException;
//import java.net.URL;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.List;
//import java.util.Map;
//import java.util.Random;
//
//public class QQMusicMediaResponse extends HttpMediaResponse {
//    protected final static int GET_URL_RETRY_TIME = 1000;
//    protected final static int TICKET_EXPIRED_TIME = 2 * 60 * 60 * 1000; // QQ音乐的ticket有效期
//    private Audio mAudio = null;
//    private QQMusicAudio mMusicAudio = null;
//    private static Map<Long, QQParamData> paramsData = new HashMap<>(2);//最多存放三个。
//    int mBadRequestRetryCount = 0;
//    int timeOutRetry = 0;// 超过10次就停止
//    private final static int DEF_TICKET_TIME_OUT = 3000;
//    private int mForbiddenRetry = 0;
//    private boolean isNewRequest = true;
//    private int timeout_delay_time_request = 1000;
//    private QQTicketTable mTicketTable;
//
//    @Override
//    public void notifyUrlChangeListener() {
//        if (paramsData.get(mAudio.getId()) != null) {
//            paramsData.get(mAudio.getId()).position = mCurPosition;
//        }
//    }
//
//    @Override
//    public void onHttpMediaError(Error err) {
//        // 400时1s后重试
//        LogUtil.logw("media session[" + mSess.getLogId()
//                + "]bad request access retry[" + mBadRequestRetryCount
//                + "]: " + err.getErrorCode() + "/" + mUrl);
//        if (err.getErrorCode() == Error.ERROR_CLIENT_MEDIA_BAD_REQUEST) {
//            ++mBadRequestRetryCount;
//
//            LogUtil.logw("media session[" + mSess.getLogId()
//                    + "]bad request access retry[" + mBadRequestRetryCount
//                    + "]: " + mUrl);
//
//            if (mBadRequestRetryCount > 3) {
//                mBadRequestRetryCount = 0;
//
//                paramsData.remove(mAudio.getId());
//
//                mUrl = null;
//                mURI = null;
//
//                refreshDownloadData();
//
//                mHttpClient.setSuggestRetryDelay(WAIT_URL_TIME);
//            } else {
//                mHttpClient.setSuggestRetryDelay(GET_URL_RETRY_TIME);
//            }
//
//            return;
//        }
//        // 出现403时重新换取下载地址
//        if (err.getErrorCode() == Error.ERROR_CLIENT_MEDIA_FILE_FORBIDDEN) {
//            LogUtil.logw("media session[" + mSess.getLogId()
//                    + "]forbidden access: " + mUrl);
//
//            paramsData.remove(mAudio.getId());
//
//            mUrl = null;
//            mURI = null;
//            if (mForbiddenRetry++ < 3) {
//                refreshDownloadData();
//                mHttpClient.setSuggestRetryDelay(WAIT_URL_TIME);
//                return;
//            }
//        }
//        if (err.getErrorCode() == Error.ERROR_CLIENT_MEDIA_REQ_TIMEOUT) {// 超时
//            LogUtil.logw("timeout:");
//        }
//        super.onHttpMediaError(err);
//    }
//
//    protected QQMusicMediaResponse(Socket out, SessionInfo sess, long from,
//                                   long to) {
//        super(out, sess, from, to);
//        mMusicAudio = (QQMusicAudio) mSess.audio;
//
//        mAudio = mMusicAudio.getAudio();
//        if (paramsData.containsKey(mAudio.getId())) {
//            isNewRequest = false;
//        } else {
//            isNewRequest = true;
//            //paramsData.remove(mAudio.getId());
//        }
//
//        mURI = null;
//        mForbiddenRetry = 0;
//        AppLogic.removeBackGroundCallback(timeOutRunnable);
//
//
//        mUrl = mMusicAudio.getFinalUrl();
//
//        if (StringUtils.isEmpty(mUrl)) {
//            QQTicketTable ticketTable = getUrlFromDb(mAudio.getId(), mAudio.getSid());
//            if (ticketTable != null) {
//                LogUtil.logd("test:play:error:getFromDB:" + ticketTable.toString());
//                mUrl = ticketTable.url;
//                mExpTime = ticketTable.iExpTime;
//                mMusicAudio.setUrlHashCode(ticketTable.getHashcode());
//            }
//        }
//
//        if (StringUtils.isNotEmpty(mUrl)) {
//            try {
//                mURI = new URI(mUrl);
//            } catch (Exception e) {
//                mUrl = null;
//            }
//        }
//        LogUtil.logi(Constant.SPEND_TAG + "media session[" + mSess.getLogId() + "] receive QQ request " + from + "/" + to);
//        if (StringUtils.isEmpty(mUrl) || mURI == null) {
//            refreshDownloadData();
//        }
//    }
//
//    public QQTicketTable getUrlFromDb(long id, int sid) {
//        mTicketTable = DaoManager.getInstance().findTicketUrl(id, sid);
//        if (mTicketTable != null) {
//            long currentTime = System.currentTimeMillis() / 1000;
//            LogUtil.logd(TAG + "ticket :server expTime is " + mTicketTable.getIExpTime() + ",device expTime is " + currentTime);
//            //FIXME 这里的超时时间计算不对,但是没有一种合适的方式
//            if (mTicketTable.getIExpTime() - currentTime > 0) {//没有超时
//                return mTicketTable;
//            }
//        }
//        return null;
//
//    }
//
//
//    @Override
//    public void cancel() {
//        synchronized (QQMusicMediaResponse.this) {
//            AppLogic.removeBackGroundCallback(timeOutRunnable);
//        }
//
//        mMusicAudio = null;
//
//        super.cancel();
//    }
//
//    private static final int MAXIMUM_REDIRECTS = 5;
//    private static final int TIMEOUT = 5000;
//    private HttpURLConnection urlConnection;
//    private InputStream stream;
//
//    private void loadDataWithRedirects(URL url, int redirects, URL lastUrl,
//                                       Map<String, String> headers) throws IOException {
//        if (redirects >= MAXIMUM_REDIRECTS) {
//            throw new com.txznet.audio.server.response.HttpException("Too many (> " + MAXIMUM_REDIRECTS + ") redirects!");
//        } else {
//            // Comparing the URLs using .equals performs additional network I/O and is generally broken.
//            // See http://michaelscharf.blogspot.com/2006/11/javaneturlequals-and-hashcode-make.html.
//            try {
//                if (lastUrl != null && url.toURI().equals(lastUrl.toURI())) {
//                    throw new com.txznet.audio.server.response.HttpException("In re-direct loop");
//
//                }
//            } catch (URISyntaxException e) {
//                // Do nothing, this is best effort.
//            }
//        }
//
//        urlConnection = (HttpURLConnection) url.openConnection();
//        if (headers != null) {
//            for (Map.Entry<String, String> headerEntry : headers.entrySet()) {
//                urlConnection.addRequestProperty(headerEntry.getKey(), headerEntry.getValue());
//            }
//        }
//        urlConnection.setConnectTimeout(TIMEOUT);
//        urlConnection.setReadTimeout(TIMEOUT);
//        urlConnection.setUseCaches(false);
//        urlConnection.setDoInput(true);
//
//        // Stop the urlConnection instance of HttpUrlConnection from following redirects so that
//        // redirects will be handled by recursive calls to this method, loadDataWithRedirects.
//        urlConnection.setInstanceFollowRedirects(false);
//
//        // Connect explicitly to avoid errors in decoders if connection fails.
//        urlConnection.connect();
//        // Set the stream so that it's closed in cleanup to avoid resource leaks. See #2352.
//        stream = urlConnection.getInputStream();
//        final int statusCode = urlConnection.getResponseCode();
//        if (statusCode / 100 == 2) {
//            processStream(stream);
//            return;
//        } else if (statusCode / 100 == 3) {
//            String redirectUrlString = urlConnection.getHeaderField("Location");
//            if (TextUtils.isEmpty(redirectUrlString)) {
//                throw new com.txznet.audio.server.response.HttpException("Received empty or null redirect url");
//            }
//            URL redirectUrl = new URL(url, redirectUrlString);
//            loadDataWithRedirects(redirectUrl, redirects + 1, url, headers);
//            return;
//        } else if (statusCode == -1) {
//            throw new com.txznet.audio.server.response.HttpException(statusCode);
//        } else {
//            throw new com.txznet.audio.server.response.HttpException(urlConnection.getResponseMessage(), statusCode);
//        }
//    }
//
//    private void processStream(InputStream inputStream) throws IOException {
//        byte[] content = new byte[10 * 1024];
//        int length = 0;
//        StringBuffer sb = new StringBuffer();
//        while ((length = inputStream.read(content)) >= 0) {
//            sb.append(new String(content, 0, length));
//        }
//        QQParamData qqParamData = new QQParamData();
//        qqParamData.mQQMusicTicketData = sb.toString();
//        qqParamData.mQQMusicTicketExpiredTime = SystemClock.elapsedRealtime() + TICKET_EXPIRED_TIME;
//        synchronized (paramsData) {
//            if (paramsData != null && paramsData.size() > 2) {
//                Iterator<Long> iterator = paramsData.keySet().iterator();
//                boolean needDel = true;
//                while (needDel && iterator.hasNext()) {
//                    Long key = iterator.next();
//                    LogUtil.logd(TAG + "remove cache param data," + key);
//                    paramsData.remove(key);
//                    if (paramsData.size() <= 2) {
//                        needDel = false;
//                    }
//                }
//            }
//            LogUtil.logd(TAG + "save cache param data," + mAudio.getId());
//            paramsData.put(mAudio.getId(), qqParamData);
//        }
//        refreshDownloadUrl();
//    }
//
//
//    /**
//     * 获取QQMusic的ticket
//     */
//    public void refreshDownloadData() {
//        if (paramsData.get(mAudio.getId()) != null && paramsData.get(mAudio.getId()).mQQMusicTicketData != null
//                && SystemClock.elapsedRealtime() < paramsData.get(mAudio.getId()).mQQMusicTicketExpiredTime) {
//            LogUtil.logi(Constant.SPEND_TAG + "media session[" + mSess.getLogId() + "] refreshDownloadUrl cache");
//            QQMusicMediaResponse.this.refreshDownloadUrl();
//            return;
//        }
//
//        synchronized (QQMusicMediaResponse.this) {
//            if (paramsData.get(mAudio.getId()) == null || paramsData.get(mAudio.getId()).mQQMusicTicketData == null || SystemClock.elapsedRealtime() >= paramsData.get(mAudio.getId()).mQQMusicTicketExpiredTime) {
//                LogUtil.logi(Constant.SPEND_TAG + "media session[" + mSess.getLogId() + "] request QQ preData ");
////                if (isNewRequest) {
//                if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
//                    isNetPoor = true;
//                }
//                new Thread(new Runnable() {
//                    @Override
//                    public void run() {
//                        try {
//                            loadDataWithRedirects(new URL(mAudio.getStrProcessingUrl()), 0, null, null);
//                        } catch (IOException e) {
//                            AppLogic.runOnBackGround(new Runnable() {
//                                @Override
//                                public void run() {
//                                    QQMusicMediaResponse.this.refreshDownloadData();
//                                }
//                            }, GET_URL_RETRY_TIME);
//                        }
//                    }
//                }).start();
//            } else {
//                LogUtil.logi(Constant.SPEND_TAG + "media session[" + mSess.getLogId() + "] refreshDownloadUrl");
//                refreshDownloadUrl();
//            }
//
//        }
//    }
//
//
//    public void refreshDownloadUrl() {
//
//        final QQParamData qqParamData = paramsData.get(mAudio.getId());
//        if (qqParamData == null || qqParamData.mQQMusicTicketData == null
//                || SystemClock.elapsedRealtime() >= qqParamData.mQQMusicTicketExpiredTime) {
//            LogUtil.loge("media session[" + mSess.getLogId() + "]refreshDownloadData  ticketData="
//                    + (qqParamData == null ? "null" : qqParamData.mQQMusicTicketData));
//            QQMusicMediaResponse.this.refreshDownloadData();
//            return;
//        }
//
//        if (qqParamData != null && SystemClock.elapsedRealtime() <= qqParamData.mQQMusicTicketExpiredTime && !TextUtils.isEmpty(qqParamData.url)) {
//            initUrl(qqParamData.url, qqParamData.backUrl);
//            return;
//        }
//        //TODO:这里不能用SystemClock.elapsedRealtime()
//        LogUtil.logi(Constant.SPEND_TAG + "media session[" + mSess.getLogId() + "] request QQ url " + ",currenttime=" + SystemClock.elapsedRealtime() + ",qq ticket expired time=" + qqParamData.mQQMusicTicketExpiredTime);
//        ReqProcessing reqData = new ReqProcessing();
//        reqData.setStrDownloadUrl(mAudio.getStrDownloadUrl());
//        reqData.setProcessingContent(qqParamData.mQQMusicTicketData);
//        reqData.setStrProcessingUrl(mAudio.getStrProcessingUrl());
////		reqData.setSid(6);
//        reqData.setSid(mAudio.getSid());
//        reqData.setAudioId(mAudio.getId());
//        LogUtil.loge(TAG + reqData.toString() + "/" + "reqData：/" + JsonHelper.toJson(reqData));
//
//        //TODO：请求以便返回，真实路径
//
////        HttpURLConnection httpURLConnection=
//        TimeUtils.startTime("media session[" + mSess.getLogId() + "]requestQQRealUrl");
//        DataInterfaceBroadcastHelper.sendDataInterfaceReq(
//                Constant.GET_PROCESSING, JsonHelper.toJson(reqData).getBytes(), mAudio.getId(),
//                new DataInterfaceBroadcastHelper.RemoteNetListener() {
//                    @Override
//                    public int response(int code, byte[] data) {
//                        TimeUtils.endTime("media session[" + mSess.getLogId() + "]requestQQRealUrl");
//                        LogUtil.logi(Constant.SPEND_TAG + "media session[" + mSess.getLogId() + "]receive  QQ url ");
//                        AppLogic.removeBackGroundCallback(timeOutRunnable);
//                        if (Constant.ISTESTDATA) {
//                            LogUtil.logd("request refresh download ="
//                                    + new String(data));
//                        }
//                        if (code != 0) {
//                            LogUtil.loge("media session[" + mSess.getLogId()
//                                    + "]GET_PROCESSING error: " + code);
//                            MonitorUtil
//                                    .monitorCumulant(Constant.M_GETTICKETERROR);
//                            AppLogic.runOnBackGround(timeOutRunnable, 0);
//                            return CODE_FAILURE;
//                        }
//                        ResponseURL responseURL = null;
//                        try {
//                            responseURL = JsonHelper.toObject(ResponseURL.class, new String(data));
//                        } catch (Exception e1) {
//                            LogUtil.loge(TAG + "[Exception]" + new String(data), e1);
//                        }
//                        if (Constant.ISTEST) {
//                            LogUtil.logd("media session[" + mSess.getLogId() + "]responseURL: " + responseURL);
//                        }
//                        if (responseURL == null || responseURL.getErrCode() != 0) {
//                            LogUtil.loge("media session[" + mSess.getLogId()
//                                    + "]GET_PROCESSING error: " + code);
//                            MonitorUtil.monitorCumulant(Constant.M_GETTICKETERROR);
//                            AppLogic.runOnBackGround(timeOutRunnable, 0);
//                            return CODE_FAILURE;
//                        }
//                        mExpTime = responseURL.getiExpTime();
//                        if (!initUrl(responseURL.getStrUrl(), responseURL.getArrBackUpUrl())) {
//                            return CODE_FAILURE;
//                        }
//                        qqParamData.url = mUrl;
//                        qqParamData.backUrl = mBackUrl;
//                        return CODE_SUCCESS;
//                    }
//                });
//        AppLogic.removeBackGroundCallback(timeOutRunnable);
//        AppLogic.runOnBackGround(timeOutRunnable, DEF_TICKET_TIME_OUT);// 超过3s就重试
//        isNewRequest = false;
//    }
//
//    Runnable timeOutRunnable = new Runnable() {
//        @Override
//        public void run() {
//            timeOutRetry++;
//            LogUtil.loge("media session[" + mSess.getLogId() + "]timeout ticket and retry");
//            AppLogic.removeBackGroundCallback(timeOutRunnable);
//            //XXX:bug如果打开注释，请确认：在线歌曲缓冲未完，从历史播放列表中进入继续播放播放到未缓冲处连上网络能继续播放。
////			if (timeOutRetry >= 10) {
////				timeOutRetry = 0;
////				QQMusicMediaResponse.this.onHttpMediaError(new MediaError(MediaError.ERR_REQ_TIMEOUT, "request timeOut", "当前网络不佳"));
////			} else {
//            QQMusicMediaResponse.this.refreshDownloadUrl();
////			}
//        }
//    };
//
//    public boolean initUrl(String url, List<String> backUrls) {
//        LogUtil.logd("media session[" + mSess.getLogId() + "]initURL:" + url);
//        //如果回来的数据包含和原来相同的hashcode则使用之前的hashcode的值
//
//
//        mBackUrl.clear();
//        if (CollectionUtils.isNotEmpty(backUrls)) {
//            mBackUrl.add(url);
//            mBackUrl.addAll(backUrls);
//        }
//
//        if (mTicketTable != null) {
//            for (String urlTemp : mBackUrl) {
//                if (mMusicAudio != null && urlTemp.endsWith(mTicketTable.getHashcode())) {
//                    mMusicAudio.setUrlHashCode(mTicketTable.getHashcode());
//                    mUrl = urlTemp;
//                    //更新数据库
//                    QQTicketTable qqTicketTable = new QQTicketTable(mAudio.getId(), mAudio.getSid(), mUrl, mTicketTable.getHashcode(), mExpTime);
//                    LogUtil.logd("test:play:error:updateDB:" + qqTicketTable.toString());
//                    DaoManager.getInstance().saveTicketUrl(qqTicketTable);
//                    break;
//                }
//            }
//        }
//
//        //没有从之前的取出相同的则使用第一个
//        if (StringUtils.isEmpty(mUrl)) {
//            QQParamData qqParamData = paramsData.get(mAudio.getId());
//            if (qqParamData != null && CollectionUtils.isNotEmpty(mBackUrl) && mBackUrl.size() > qqParamData.position) {
//                mUrl = mBackUrl.get(qqParamData.position);
//            } else {
//                mUrl = url;
//            }
//        }
//
//        if (Constant.ISTEST) {
//            if (!mBackUrl.isEmpty()) {
//                LogUtil.loge("mBackUrl is" + mBackUrl);
//            }
//        }
//
//        try {
//            mURI = new URI(mUrl);
//        } catch (Exception e) {
//            LogUtil.loge("media session[" + mSess.getLogId() + "]responseURL :", e);
//            QQMusicMediaResponse.this
//                    .onHttpMediaError(new Error(
//                            Error.ERROR_CLIENT_MEDIA_WRONG_URL,
//                            "wrong media url: " + mUrl,
//                            "播放地址错误"));
//            return false;
//        }
//        if (mMusicAudio != null) {
//            mMusicAudio.setFinalUrl(mUrl);
//        }
//
//        return true;
//    }
//}
