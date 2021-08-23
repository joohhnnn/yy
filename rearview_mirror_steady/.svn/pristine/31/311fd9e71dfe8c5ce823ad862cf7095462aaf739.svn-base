package com.txznet.music.playerModule.logic;

import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;
import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.data.netease.NeteaseSDK;
import com.txznet.music.data.netease.net.bean.NeteaseUrl;
import com.txznet.music.data.utils.OnGetData;
import com.txznet.music.net.HttpUtils;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.playerModule.logic.net.request.ReqProcessing;
import com.txznet.music.playerModule.logic.net.response.ResponseURL;
import com.txznet.music.utils.NetworkUtil;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.runnables.Runnable2;

import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.txznet.music.baseModule.bean.Error.ERROR_CLIENT_REQUEST_TIMEOUT;
import static com.txznet.music.baseModule.bean.Error.ERROR_CLIENT_REQUEST_URL;

/**
 * Created by brainBear on 2017/11/6.
 */

public class PlayItemManager {

    private static final String TAG = "PlayItem:";
    /**
     * 由于后台返回的请求时间和记录的系统的时间戳有误差，需要排除这个误差的影响，单位秒
     */
    private static final long TIME_DEVIATION = 60;
    private static final int TIME_RETRY = 5 * 1000;
    private static PlayItemManager sInstance;
    private Runnable2<Audio, IPlayItemListener> mRetryTask;

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
            createLocalPlayItem(audio, listener);
            return;
        }

        File audioTMDFile = Utils.getAudioTMDFile(audio);
        if (null != audioTMDFile && audioTMDFile.exists()) {
            createCachePlayItem(audio, listener);
            return;
        }

        if (Utils.isNetSong(audio.getSid()) && TextUtils.equals(audio.getDownloadType(), "1")) {
            createQQPlayItem(audio, listener);
        } else if (TextUtils.equals(audio.getDownloadType(), "3")) {
            createNeteaseItem(audio, listener);
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
                getQQPlayItemFromNet(audio, listener);
            }
        });
    }

    private void createNeteaseItem(final Audio audio, final IPlayItemListener listener) {
        getNeteaseItemFromCache(audio, new IPlayItemListener() {
            @Override
            public void onSuccess(final PlayItem playItem) {
                listener.onSuccess(playItem);
            }

            @Override
            public void onError(Error error) {
                //在缓存中获取失败了才去换链接
                getNeteaseItemFromNet(audio, listener);
            }
        });
    }

    private void getNeteaseItemFromNet(final Audio audio, final IPlayItemListener listener) {
        NeteaseSDK.getInstance().getUrl(audio.getStrId(), NeteaseSDK.getInstance().getBitrate(), new OnGetData<NeteaseUrl>() {
            @Override
            public void success(NeteaseUrl neteaseUrl) {
                Logger.d(TAG, "createNetPlayItem:" + audio.getName());
                final PlayItem playItem = new PlayItem();
                playItem.setType(PlayItem.TYPE_NET);
                playItem.setId(audio.getId());
                playItem.setSid(audio.getSid());
                playItem.setName(audio.getName());

                List<String> urls = new ArrayList<>();
                urls.add(neteaseUrl.getData().getUrl());
                playItem.setUrls(urls);

                listener.onSuccess(playItem);
                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        DBManager.getInstance().savePlayItem(playItem);
                    }
                });
            }

            @Override
            public void failed(int errorCode) {
                if (errorCode == -2) {
                    listener.onError(new Error(ERROR_CLIENT_REQUEST_TIMEOUT, "请求网易云资源发生异常", "歌曲失效"));
                } else {
                    listener.onError(new Error(ERROR_CLIENT_REQUEST_URL, "请求网易云资源发生异常", "歌曲失效"));
                }

            }
        });
    }

    private void getNeteaseItemFromCache(final Audio audio, final IPlayItemListener listener) {
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                PlayItem playItem = DBManager.getInstance().findPlayItem(audio);

                boolean isExpired = false;
                if (playItem == null) {
                    isExpired = true;
                } else {
                    if (playItem.getUrls() == null || playItem.getUrls().size() < 1) {
                        isExpired = true;
                    } else {
                        if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                            // 尝试一下header请求是不是200
                            try {
                                HttpURLConnection conn = (HttpURLConnection) new URL(playItem.getUrls().get(0)).openConnection();
                                conn.setRequestMethod("HEAD");
                                conn.setConnectTimeout(1000 * 3);
                                conn.connect();
                                if (conn.getResponseCode() != 200) {
                                    LogUtil.d(TAG, "get netease PlayItem expired, url=" + playItem.getUrls().get(0));
                                    isExpired = true;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
                if (isExpired) {
                    try {
                        DBManager.getInstance().removeLocalAudios(audio);
                    } catch (Exception e) {
                    }
                    listener.onError(new Error(Error.ERROR_GET_PLAY_ITEM_TIMEOUT));
                } else {
                    LogUtil.d(TAG, "get netease PlayItem from cache:" + playItem.toString());
                    listener.onSuccess(playItem);
                }
            }
        });
    }

    private void requestQQUrl(final Audio audio, String ticket, final IPlayItemListener listener) {
        ReqProcessing reqProcessing = new ReqProcessing();
        reqProcessing.setStrDownloadUrl(audio.getStrDownloadUrl());
        reqProcessing.setStrProcessingUrl(audio.getStrProcessingUrl());
        reqProcessing.setProcessingContent(ticket);
        reqProcessing.setSid(audio.getSid());
        reqProcessing.setAudioId(audio.getId());

        Logger.d("requestQQUrl", reqProcessing.toString());

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

    public void getQQPlayItemFromNet(final Audio audio, final IPlayItemListener listener) {
        Logger.d(TAG, "getQQPlayItemFromNet");
        if (null != mRetryTask) {
            AppLogic.removeUiGroundCallback(mRetryTask);
            mRetryTask = null;
        }
        HttpUtils.sendGetRequest(audio.getStrProcessingUrl(), new HttpUtils.HttpCallbackListener() {
            @Override
            public void onSuccess(String response) {
                LogUtil.d(TAG, "success:" + response);
                requestQQUrl(audio, response, listener);
            }

            @Override
            public void onError(int errorCode) {
                LogUtil.e(TAG, "getQQPlayItemFromNet error:" + errorCode);
                mRetryTask = new Runnable2<Audio, IPlayItemListener>(audio, listener) {
                    @Override
                    public void run() {
                        PlayItemManager.getInstance().getQQPlayItemFromNet(mP1, mP2);
                    }
                };
                AppLogic.runOnUiGround(mRetryTask, TIME_RETRY);
            }
        });
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
