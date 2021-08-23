package com.txznet.music.net;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.SystemClock;
import android.support.annotation.NonNull;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.audio.UiAudio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.net.request.ReqAlbumAudio;
import com.txznet.music.albumModule.logic.net.response.ResponseSearchAlbum;
import com.txznet.music.data.http.req.ReqCategory;
import com.txznet.music.albumModule.logic.net.request.ReqInterestTag;
import com.txznet.music.albumModule.logic.net.request.ReqSearchAlbum;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.logic.ServiceEngine;
import com.txznet.music.net.rx.RxNet;
import com.txznet.music.playerModule.logic.net.request.ReqProcessing;
import com.txznet.music.playerModule.logic.net.request.ReqThirdSearch;
import com.txznet.music.playerModule.logic.net.response.RespThirdSearch;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.logic.FackLogic;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;
import com.txznet.music.soundControlModule.logic.net.request.ReqHistorySearch;
import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
import com.txznet.music.soundControlModule.logic.net.response.ResponseHistorySearch;
import com.txznet.music.soundControlModule.logic.net.response.ResponseSearch;
import com.txznet.music.ui.net.request.ReqCheck;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.JsonHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

import io.reactivex.Observable;

/**
 * Created by brainBear on 2017/6/15.
 */

public class NetManager {

    public static final String TAG = "Music:Net:";
    private static final int CHECK_TIME_OUT_INTERVAL = 1000;
    private static int sRequestId = new Random().nextInt();
    private static NetManager sInstance;
    private ConcurrentHashMap<Integer, RequestWrapper> mRequestArray = new ConcurrentHashMap<Integer, RequestWrapper>();


    /**
     * 检测请求队列中超时的任务
     */
    private Runnable mCheckTimeoutTask = new Runnable() {
        @Override
        public void run() {
            AppLogic.removeUiGroundCallback(this);
            Iterator<Integer> iterator = mRequestArray.keySet().iterator();
            while (iterator.hasNext()) {
                int key = iterator.next();
                RequestWrapper requestWrapper = mRequestArray.get(key);
                if (requestWrapper == null) {
                    mRequestArray.remove(key);
                    continue;
                }
                if (SystemClock.elapsedRealtime() - requestWrapper.getRequestTime() > requestWrapper.getTimeOut()) {
                    LogUtil.e(TAG + "time out " + requestWrapper.getDataInterface().strCmd + " id:" + requestWrapper.getDataInterface().uint32Seq);
                    mRequestArray.remove(key);
                    RequestRawCallBack callBack = requestWrapper.getCallBack();
                    if (null != callBack) {
                        callBack.onError(requestWrapper.getDataInterface().strCmd,
                                new Error(Error.ERROR_CLIENT_NET_TIMEOUT));
                    }
                }
            }
            AppLogic.runOnUiGround(this, CHECK_TIME_OUT_INTERVAL);
        }
    };
    /**
     * 上次请求audio的id，用于取消上次请求
     */
    private int mLastRequestAudioId;

    private NetManager() {
        AppLogic.runOnUiGround(mCheckTimeoutTask, CHECK_TIME_OUT_INTERVAL);
    }

    /**
     * 判断当前网络连接状态
     *
     * @return 有网络连接返回true 否则返回false
     */
    public static boolean isNetworkConnected() {
        boolean netStatus = false;
        Context context = GlobalContext.get();
        ConnectivityManager connectivity = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivity.getActiveNetworkInfo();
        if (networkInfo != null) {
            netStatus = networkInfo.isAvailable();
        }
        return netStatus;
    }

    public static NetManager getInstance() {
        if (null == sInstance) {
            synchronized (NetManager.class) {
                if (null == sInstance) {
                    sInstance = new NetManager();
                }
            }
        }
        return sInstance;
    }

    private int getNewRequestId() {
        return ++sRequestId;
    }

    public int sendRequestToCore(String url, Object obj, RequestRawCallBack callBack) {
        String json = JsonHelper.toJson(obj);
        return sendRequestToCore(url, json.getBytes(), callBack);
    }

    public int sendRequestToCore(final String url, byte[] reqData, final RequestRawCallBack callBack) {
        int newRequestId = getNewRequestId();

        UiAudio.Req_DataInterface reqDataInterface = new UiAudio.Req_DataInterface();
        reqDataInterface.strCmd = url;
        reqDataInterface.strData = reqData;
        reqDataInterface.uint32Seq = newRequestId;
        LogUtil.e(TAG + " url=" + url + ",request id:" + newRequestId + ",params=" + (reqData == null ? "null" : new String(reqData, 0, reqData.length > 500 ? 500 : reqData.length)));
        if (!isNetworkConnected() && null != callBack) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    callBack.onError(url, new Error(Error.ERROR_CLIENT_NET_OFFLINE));
                }
            });
            return newRequestId;
        }

        RequestWrapper requestWrapper = new RequestWrapper(reqDataInterface, callBack);
        mRequestArray.put(newRequestId, requestWrapper);

        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.music.dataInterface", MessageNano.toByteArray(reqDataInterface), null);
        return newRequestId;
    }

    public byte[] handleDataInterface(byte[] data) {
        UiAudio.Resp_DataInterface dataInterface = null;
        try {
            dataInterface = UiAudio.Resp_DataInterface.parseFrom(data);
        } catch (InvalidProtocolBufferNanoException e) {
            LogUtil.loge(TAG, e);
        }

        if (null == dataInterface || null == dataInterface.uint32Seq) {
            // TODO: 2017/6/16 网络请求出错
            LogUtil.d(TAG + "handle data interface error:data or seq id empty");
            return null;
        }

        dispatchResponse(dataInterface);

        return null;
    }

    private void dispatchResponse(final UiAudio.Resp_DataInterface dataInterface) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                String strData = new String(dataInterface.strData);
                int retCode = 0;
                try {
                    JSONObject jsonObject = new JSONObject(strData);
                    if (jsonObject.has("errCode")) {
                        retCode = jsonObject.getInt("errCode");
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG + e.toString());
                }

                RequestWrapper requestWrapper = mRequestArray.get(dataInterface.uint32Seq);
                LogUtil.d(TAG + dataInterface.strCmd + " request wrapper is null?" + (requestWrapper == null) + " id:" + dataInterface.uint32Seq);
                RequestRawCallBack callBack = null;
                if (null != requestWrapper) {
                    callBack = requestWrapper.getCallBack();
                    mRequestArray.remove(dataInterface.uint32Seq);
                } else {
                    LogUtil.i(TAG + " id:" + dataInterface.uint32Seq + " cmd:" + dataInterface.strCmd + " request wrapper is null");
                }

                if (0 != dataInterface.uint32ErrCode || 0 != retCode) {
                    LogUtil.e(TAG + " cmd:" + dataInterface.strCmd + " errorCode:" + dataInterface.uint32ErrCode + " retCode:" + retCode);
                }

                if (null != callBack) {
                    if (dataInterface.uint32ErrCode != 0) {
                        callBack.onError(dataInterface.strCmd, new Error(Error.SOURCE_CORE, dataInterface.uint32ErrCode));
                    } else if (retCode != 0) {
                        callBack.onError(dataInterface.strCmd, new Error(Error.SOURCE_SERVER, retCode));
                    } else {
                        LogUtil.e(TAG + "response url=" + dataInterface.strCmd + ",result=" + new String(dataInterface.strData));
                        callBack.onResponse(dataInterface);
                    }
                } else {
                    LogUtil.d(TAG + " id:" + dataInterface.uint32Seq + " cmd:" + dataInterface.strCmd + " callback is null");
                }
            }
        }, 0);
    }

    public int requestCategory(RequestCallBack callBack) {
        ReqCategory reqCategory = new ReqCategory();
        reqCategory.setbAll(1);

        return NetCacheManager.getInstance().requestCache(Constant.GET_CATEGORY, reqCategory, callBack);
    }

    /**
     * 获取categoryID分类下的专辑列表
     *
     * @param categoryID
     * @param pageOff
     * @param callBack
     * @return
     */
    public int requestAlbum(long categoryID, int pageOff, RequestCallBack callBack) {
        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(pageOff);
        reqSearchAlbum.setCategoryId(categoryID);
        return NetCacheManager.getInstance().requestCache(Constant.GET_SEARCH_LIST, reqSearchAlbum, false, callBack);
    }

    public Observable<ResponseSearchAlbum> requestAlbumEx(long categoryID, int pageOff) {
        ReqSearchAlbum reqSearchAlbum = new ReqSearchAlbum();
        reqSearchAlbum.setPageId(pageOff);
        reqSearchAlbum.setCategoryId(categoryID);
        return RxNet.request(Constant.GET_SEARCH_LIST, reqSearchAlbum, ResponseSearchAlbum.class);
    }

    public int requestSetUserInterestTag(String url,List<Integer> tagIds, RequestCallBack callBack){
        ReqInterestTag reqInterestTag = new ReqInterestTag();
        reqInterestTag.setTagIds(tagIds);
        reqInterestTag.setAction(Constant.ACTION_SET);
        return NetManager.getInstance().sendRequestToCore(url, reqInterestTag,callBack);
    }

    public int requestSkipInterestTag(String url, RequestCallBack callBack) {
        ReqInterestTag reqInterestTag = new ReqInterestTag();
        reqInterestTag.setAction(Constant.ACTION_SKIP);
        return NetManager.getInstance().sendRequestToCore(url, reqInterestTag, callBack);
    }

//    public int requestIsSetInterestTag(String url, RequestCallBack callBack){
//        ReqInterestTag reqInterestTag = new ReqInterestTag();
//        reqInterestTag.setAction(Constant.ACTION_IS_SET);
//        return NetManager.getInstance().sendRequestToCore(url, reqInterestTag, callBack);
//    }

    public int requestQueryInterestTag(String url, RequestCallBack callBack) {
        ReqInterestTag reqInterestTag = new ReqInterestTag();
        reqInterestTag.setAction(Constant.ACTION_GET);
        return NetManager.getInstance().sendRequestToCore(url, reqInterestTag, callBack);
    }

    public int requestProcessing(ReqProcessing req, RequestRawCallBack callBack) {
        return sendRequestToCore(Constant.GET_PROCESSING, req, callBack);
    }

    /**
     * 搜索音频数据用于返回给Core
     *
     * @return
     */
    public int searchAudio(ReqAlbumAudio reqAlbumAudio) {

        return NetManager.getInstance().sendRequestToCore(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, new RequestCallBack<ResponseAlbumAudio>(ResponseAlbumAudio.class) {
            @Override
            public void onResponse(ResponseAlbumAudio data) {
                SearchEngine.getInstance().handleAlbumAudioSearch(data);
            }

            @Override
            public void onError(String cmd, Error error) {
                SearchEngine.getInstance().handleAudioSearchError(error.getErrorCode());
            }
        });
    }

    public int requestAudio(final EnumState.Operation operation, Album album, Audio audio, boolean isNext, long categoryId, List<ReqChapter> chapters, @NonNull RequestCallBack callback) {

        return requestAudio(operation, album, audio, isNext, categoryId, chapters, callback, false);//默认不从缓存中获取
    }

    public int requestAudio(final EnumState.Operation operation, Album album, Audio audio, boolean isNext, long categoryId, List<ReqChapter> chapters, @NonNull RequestCallBack callback, boolean needCache) {
        cancelRequest(mLastRequestAudioId);
        ReqAlbumAudio reqAlbumAudio = new ReqAlbumAudio();
        reqAlbumAudio.setSid(album.getSid());
        reqAlbumAudio.setId(album.getId());
        reqAlbumAudio.setSvrData(album.getSvrData());
        if (audio != null) {
            reqAlbumAudio.setAudioId(audio.getId());
            reqAlbumAudio.setAudioSid(audio.getSid());
        }

        int order = 1;
        if (isNext) {
            order = 0;
        }
        reqAlbumAudio.setUp(order);
        reqAlbumAudio.setCategoryId(categoryId);
        reqAlbumAudio.setOffset(Constant.PAGECOUNT);
        if (CollectionUtils.isNotEmpty(chapters)) {
            reqAlbumAudio.setArrMeasure(chapters);
        }
        if (needCache) {
            mLastRequestAudioId = NetCacheManager.getInstance().requestCache(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, needCache, callback);
        } else {
            mLastRequestAudioId = sendRequestToCore(Constant.GET_ALBUM_AUDIO, reqAlbumAudio, callback);
        }
        return mLastRequestAudioId;
    }


    public int requestTag(RequestCallBack callBack) {
        ReqCheck reqCheck = new ReqCheck();
        reqCheck.setLogoTag(0);
        return NetCacheManager.getInstance().requestCache(Constant.GET_TAG, reqCheck, callBack);
    }


    public boolean cancelRequest(int requestId) {
        LogUtil.d(TAG + "cancel request " + requestId);
        RequestWrapper requestWrapper = mRequestArray.get(requestId);
        if (null != requestWrapper) {
            mRequestArray.remove(requestId);
            return true;
        }
        return false;
    }


    public void fakeRequest(ReqThirdSearch reqThirdSearch) {
        sendRequestToCore(Constant.GET_FAKE_SEARCH, reqThirdSearch, new RequestCallBack<RespThirdSearch>(RespThirdSearch.class) {
            @Override
            public void onResponse(RespThirdSearch data) {
                FackLogic.getInstance().doFakeReq(data);
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.e(TAG + "fake request error;" + error.getErrorCode());
                FackLogic.getInstance().handleError();
            }
        });
    }


    public int requestHistory(ReqHistorySearch reqHistorySearch) {
        LogUtil.d("reqHistorySearch  start");
        return sendRequestToCore(Constant.GET_HISTORY, reqHistorySearch, new RequestCallBack<ResponseHistorySearch>(ResponseHistorySearch.class) {
            @Override
            public void onResponse(ResponseHistorySearch data) {
                LogUtil.d("reqHistorySearch :" + data);
                SearchEngine.getInstance().handleHistorySearch(data);
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.e(TAG + "request search error:" + error.getErrorCode());
                SearchEngine.getInstance().handleError();
            }
        });
    }



    public void requestTime(RequestRawCallBack callBack) {
        sendRequestToCore(Constant.GET_TIME, null, callBack);
    }


    public void requestPushData(String service, Object data, RequestRawCallBack callBack) {
        sendRequestToCore(service, data, callBack);
    }
}
