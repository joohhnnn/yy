package com.txznet.music.playerModule.logic;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.HttpUtils;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.playerModule.logic.net.request.ReqProcessing;
import com.txznet.music.playerModule.logic.net.response.ResponseURL;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.runnables.Runnable2;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by brainBear on 2017/11/6.
 */

public class PlayItemManager {

    private static final String TAG = "PlayItem:";
    /**
     * 由于后台返回的请求时间和记录的系统的时间戳有误差，需要排除这个误差的影响，单位秒
     */
    private static final long TIME_DEVIATION = 60;
    private static int TIME_OUT_REQ = 0;
    private static boolean reqJsonDataSuccess = false;// 请求Json是否正确返回了
    private static int TIME_RETRY = 1000;
    private static PlayItemManager sInstance;
    private Runnable2<Audio, IPlayItemListener> mRetryTask;
    private Runnable mRetryRequestQQurlTask;

    private PlayItemManager() {

    }

    public static PlayItemManager getInstance() {
        if (null == sInstance) {
            synchronized (PlayItemManager.class) {
                if (null == sInstance) {
                    sInstance = new PlayItemManager();
                }
            }
        }

        return sInstance;
    }

    public void createPlayItem(Audio audio, IPlayItemListener listener) {
//        PlayEngineFactory.getEngine().setState(PlayerInfo.PLAYER_STATUS_BUFFER);

        if (Utils.isLocalSong(audio.getSid())) {
            if (FileUtils.isExist(audio.getStrDownloadUrl())) {
                createLocalPlayItem(audio, listener);
            } else {
                listener.onError(new Error(Error.ERROR_CLIENT_MEDIA_NOT_FOUND, "该文件不存在:" + audio.getStrDownloadUrl(), "该文件已被删除"));
            }
            return;
        }

        File audioTMDFile = Utils.getAudioTMDFile(audio);
        if (audioTMDFile != null) {
            if (audioTMDFile.exists()) {
                createCachePlayItem(audio, listener);
                return;
            }
        }

        if (audio.getStrDownloadUrl().endsWith(".tmd")) {
            if (new File(audio.getStrDownloadUrl()).exists()) {
                createCachePlayItem(audio, listener);
            } else {
                listener.onError(new Error(Error.ERROR_CLIENT_MEDIA_NOT_FOUND, "该文件不存在:" + audio.getStrDownloadUrl(), "该文件已被删除"));
            }
            return;
        }
//
//        File audioTMDFile = Utils.getAudioTMDFile(audio);
//        if (null != audioTMDFile) {
//            if (new File(audio.getStrDownloadUrl()).exists()) {
//                //TODO:
//            }
//
//            if (audioTMDFile.exists()) {
//                createCachePlayItem(audio, listener);
//            } else {
//                listener.onError(new Error(Error.ERROR_CLIENT_MEDIA_NOT_FOUND, "该文件不存在:" + audio.getStrDownloadUrl(), "该文件已被删除"));
//            }
//            return;
//        }

        if (TextUtils.equals(audio.getDownloadType(), "1")) {
            createQQPlayItem(audio, listener);
        } else {
            createNetPlayItem(audio, listener);
        }
    }

    private void createCachePlayItem(Audio audio, IPlayItemListener listener) {
        Logger.d(TAG, "createCachePlayItem");
        File audioTMDFile = Utils.getAudioTMDFile(audio);
        if (null == audioTMDFile || !audioTMDFile.exists()) {
            Logger.e(TAG, "create cache playItem error:file not exist");
            return;
        }

        PlayItem playItem = new PlayItem();
        playItem.setType(PlayItem.TYPE_FILE);
        playItem.setId(audio.getId());
        playItem.setSid(audio.getSid());
        playItem.setName(audio.getName());

        List<String> urls = new ArrayList<>();
        urls.add(audioTMDFile.getAbsolutePath());
        playItem.setUrls(urls);

        listener.onSuccess(playItem);
    }

    private void createLocalPlayItem(Audio audio, IPlayItemListener listener) {
        Logger.d(TAG, "createLocalPlayItem");
        PlayItem playItem = new PlayItem();
        playItem.setType(PlayItem.TYPE_FILE);
        playItem.setId(audio.getId());
        playItem.setSid(audio.getSid());
        playItem.setName(audio.getName());

        List<String> urls = new ArrayList<>();
        urls.add(audio.getStrDownloadUrl());
        playItem.setUrls(urls);

        listener.onSuccess(playItem);
    }

    private void createNetPlayItem(Audio audio, IPlayItemListener listener) {
        Logger.d(TAG, "createNetPlayItem:" + audio.getName());
        PlayItem playItem = new PlayItem();
        playItem.setType(PlayItem.TYPE_NET);
        playItem.setId(audio.getId());
        playItem.setSid(audio.getSid());
        playItem.setName(audio.getName());

        List<String> urls = new ArrayList<>();
        urls.add(audio.getStrDownloadUrl());
//        urls.add("http://image.kaolafm.net/mz/aac_32/201807/140052d5-7092-4e43-9a4e-198800b30ace.aac");
        playItem.setUrls(urls);

        listener.onSuccess(playItem);
    }

    private void createQQPlayItem(final Audio audio, final IPlayItemListener listener) {
        getQQPlayItemFromCache(audio, new IPlayItemListener() {
            @Override
            public void onSuccess(final PlayItem playItem) {
                listener.onSuccess(playItem);
            }

            @Override
            public void onError(Error error) {
                //在缓存中获取失败了才去换链接
                initData();
                initReqData();
                getQQPlayItemFromNet(audio, listener);
            }
        });
    }

    private void requestQQUrl(final Audio audio, final String ticket, final IPlayItemListener listener) {
        ReqProcessing reqProcessing = new ReqProcessing();
        reqProcessing.setStrDownloadUrl(audio.getStrDownloadUrl());
        reqProcessing.setStrProcessingUrl(audio.getStrProcessingUrl());
        reqProcessing.setProcessingContent(ticket);
        reqProcessing.setSid(audio.getSid());
        reqProcessing.setAudioId(audio.getId());

        Logger.d("requestQQUrl", reqProcessing.toString());

        if (mRetryRequestQQurlTask != null) {
            AppLogic.removeUiGroundCallback(mRetryRequestQQurlTask);
        }

        increaseData();

        NetManager.getInstance().requestProcessing(reqProcessing, new RequestCallBack<ResponseURL>(ResponseURL.class) {
            @Override
            public void onResponse(ResponseURL data) {
                LogUtil.d(TAG, "data:" + data.toString());
                final PlayItem playItem = new PlayItem();
                playItem.setType(PlayItem.TYPE_QQ);
                playItem.setId(audio.getId());
                playItem.setSid(audio.getSid());
                playItem.setName(audio.getName());
                List<String> urls = new ArrayList<>();
                urls.add(data.getStrUrl());
                urls.addAll(data.getArrBackUpUrl());
                playItem.setUrls(urls);
                playItem.setExpTime(data.getiExpTime());

                LogUtil.d(TAG, "get qq PlayItem from net:" + playItem.toString());
                listener.onSuccess(playItem);

                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        DBManager.getInstance().savePlayItem(playItem);
                    }
                });
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.e(TAG, error.toString());

                if (mRetryRequestQQurlTask == null) {
                    mRetryRequestQQurlTask = new Runnable() {
                        @Override
                        public void run() {
                            requestQQUrl(audio, ticket, listener);
                        }
                    };
                }


                AppLogic.runOnUiGround(mRetryRequestQQurlTask, TIME_RETRY);
            }
        });
    }

    private boolean isPlayItemTimeOut(PlayItem playItem) {
        long effectiveTime = TimeManager.getInstance().getEffectiveTime();
        LogUtil.d(TAG, "effectiveTime:" + effectiveTime + " " + (null == playItem));

        if (effectiveTime < 0 || null == playItem) {
            return true;
        }
        if (effectiveTime + TIME_DEVIATION > playItem.getExpTime()) {
            return true;
        }
        return false;
    }

    private void getQQPlayItemFromCache(final Audio audio, final IPlayItemListener listener) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                PlayItem playItem = DBManager.getInstance().findPlayItem(audio);
                if (isPlayItemTimeOut(playItem)) {
                    listener.onError(new Error(Error.ERROR_GET_PLAY_ITEM_TIMEOUT));
                } else {
                    LogUtil.d(TAG, "get qq PlayItem from cache:" + playItem.toString());
                    listener.onSuccess(playItem);
                }
            }
        });
    }

    public void initData() {
        TIME_OUT_REQ = 0;
    }

    private synchronized void initReqData() {
        reqJsonDataSuccess = false;
    }

    private synchronized void setReqJsonDataSuccess() {
        reqJsonDataSuccess = true;
    }

    private synchronized boolean needResponse() {
        return !reqJsonDataSuccess;
    }


    public void getQQPlayItemFromNet(final Audio audio, final IPlayItemListener listener) {
        Logger.d(TAG, "getQQPlayItemFromNet");
        if (null != mRetryTask) {
            AppLogic.removeUiGroundCallback(mRetryTask);
            mRetryTask = null;
        }

        increaseData();

        if (!needResponse()) {
            //已经不需要请求了
            LogUtil.d(TAG, "success:" + needResponse() + ";  no need to req data");
            return;
        }


        HttpUtils.HttpCallbackListener reqData = new HttpUtils.HttpCallbackListener() {
            @Override
            public void onSuccess(String response) {
                HttpUtils.shutdownRequest();//停止其他所有的请求
                LogUtil.d(TAG, "success:" + needResponse() + ";" + response);
                if (needResponse()) {
                    setReqJsonDataSuccess();
                    initData();
                    requestQQUrl(audio, response, listener);
                }
            }

            @Override
            public void onError(int errorCode) {
                LogUtil.e(TAG, "getQQPlayItemFromNet error:" + needResponse() + ";" + errorCode);
                if (needResponse()) {
                    AppLogic.removeUiGroundCallback(mRetryTask);
                    mRetryTask = new Runnable2<Audio, IPlayItemListener>(audio, listener) {
                        @Override
                        public void run() {
                            PlayItemManager.getInstance().getQQPlayItemFromNet(mP1, mP2);
                        }
                    };
                    AppLogic.runOnUiGround(mRetryTask, TIME_RETRY);
                }
            }
        };

        Map<String, Object> headers = audio.getProcessHeader();
        //多个请求一起发出
        if (audio.getProcessIsPost() == 1) {
            HttpUtils.sendPostRequest(audio.getStrProcessingUrl(), headers, TIME_OUT_REQ, reqData);
            HttpUtils.sendPostRequest(audio.getStrProcessingUrl(), headers, TIME_OUT_REQ, reqData);
            HttpUtils.sendPostRequest(audio.getStrProcessingUrl(), headers, TIME_OUT_REQ, reqData);
        } else {
            HttpUtils.sendGetRequest(audio.getStrProcessingUrl(), headers, TIME_OUT_REQ, reqData);
            HttpUtils.sendGetRequest(audio.getStrProcessingUrl(), headers, TIME_OUT_REQ, reqData);
            HttpUtils.sendGetRequest(audio.getStrProcessingUrl(), headers, TIME_OUT_REQ, reqData);
        }
    }

    private void increaseData() {
        if (TIME_OUT_REQ == 0) {
            TIME_OUT_REQ = 1000;
        } else {
            TIME_OUT_REQ += 1000;
        }
        if (TIME_OUT_REQ > 5000) {
            TIME_OUT_REQ = 5000;//最大值
        }
        Logger.d(TAG, "request:data:timeout:" + TIME_OUT_REQ);
    }

    public void cancelRetry() {
        if (null != mRetryTask) {
            AppLogic.removeUiGroundCallback(mRetryTask);
        }
    }


    public interface IPlayItemListener {

        void onSuccess(PlayItem playItem);


        void onError(Error error);
    }
}
