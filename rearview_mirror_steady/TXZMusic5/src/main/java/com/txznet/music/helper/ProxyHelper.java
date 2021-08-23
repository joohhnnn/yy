package com.txznet.music.helper;

import android.util.Log;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.err.Error;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.config.Configuration;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.proxy.ProxyParam;
import com.txznet.proxy.ProxyUtils;
import com.txznet.proxy.SessionManager;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.rxflux.Dispatcher;
import com.txznet.rxflux.RxAction;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;
import java.security.MessageDigest;
import java.util.Iterator;
import java.util.List;

/**
 * @author zackzhou
 * @date 2019/1/24,16:51
 */

public class ProxyHelper {

    private static final String TAG = Constant.LOG_TAG_PROXY;

    private ProxyHelper() {
    }

    // 获取代理链接
    public static String getProxyUrl(Audio audio, String... oriUrl) {
        if (!TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.ENABLE_MEDIA_PROXY, Configuration.DefVal.ENABLE_MEDIA_PROXY)) { // 开启本地代理服务
            return oriUrl[0];
        }
        return getProxyUrl(audio, new ProxyParam.IProxyCallback() {
            final Object lock = new Object();
            boolean hasSetSyncTask;
            private Runnable syncBuffTask = new Runnable() {
                @Override
                public void run() {
                    synchronized (lock) {
                        AudioV5 currAudio = PlayHelper.get().mCurrAudio;
                        if (currAudio != null && currAudio.sid == audio.sid && currAudio.id == audio.id) {
                            Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_BUFFERING_UPDATE).bundle(Constant.PlayConstant.KEY_PLAY_BUFFER, PlayHelper.get().lastBuffs).build());
                        }
                        AppLogic.removeUiGroundCallback(syncBuffTask);
                        hasSetSyncTask = false;
                    }
                }
            };

            private void wakeupSyncTask() {
                if (!hasSetSyncTask) {
                    AppLogic.runOnUiGround(syncBuffTask, 1000);
                    hasSetSyncTask = true;
                }
            }

            @Override
            public void onError(int errorCode, String desc, String hint) {
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_ERROR).bundle(Constant.ProxyConstant.KEY_ERROR, new Error(errorCode, desc, hint)).build());
            }

            @Override
            public void onBufferingUpdate(List<LocalBuffer> buffers) {
                synchronized (lock) {
                    // FIXME: 2019/4/9 隐藏末端127字节的缓冲
                    Iterator<LocalBuffer> iterator = buffers.iterator();
                    while (iterator.hasNext()) {
                        LocalBuffer buffer = iterator.next();
                        if (buffer != null && (buffer.getTo() - buffer.getFrom()) <= 127) {
                            iterator.remove();
                        }
                    }
                    AppLogic.runOnUiGround(() -> {
                        AudioV5 currAudio = PlayHelper.get().mCurrAudio;
                        if (currAudio == null || (currAudio.sid == audio.sid && currAudio.id == audio.id)) {
                            PlayHelper.get().lastBuffs = buffers;
                            if (BuildConfig.DEBUG) {
                                Log.d(Constant.LOG_TAG_PROXY, "raw buff=" + buffers);
                            }
                            wakeupSyncTask();
                        }
                    });
                }
            }

            @Override
            public void onDownloadComplete() {
                Logger.d(TAG, "onDownloadComplete");
                Dispatcher.get().postAction(RxAction.type(ActionType.ACTION_PROXY_DOWNLOAD_COMPLETE)
                        .bundle(Constant.ProxyConstant.KEY_AUDIO, AudioConverts.convert2Audio(audio))
                        .build());
            }

            @Override
            public float getPlayPercent() {
                if (AudioPlayer.getDefault().getDuration() == 0) {
                    return 0;
                }
                return AudioPlayer.getDefault().getCurrentPosition() * 1f / AudioPlayer.getDefault().getDuration();
            }

            @Override
            public long getDuration() {
                long duration = AudioPlayer.getDefault().getDuration();
                return duration > 0 ? duration : duration;
            }

            @Override
            public boolean isPlaying() {
                return AudioPlayer.getDefault().getCurrPlayState() != IMediaPlayer.STATE_ON_PAUSED;
            }
        }, oriUrl);
    }

    // 获取代理链接
    public static String getProxyUrl(Audio audio, ProxyParam.IProxyCallback callback, String... oriUrl) {
        if (!TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.ENABLE_MEDIA_PROXY, Configuration.DefVal.ENABLE_MEDIA_PROXY)) { // 开启本地代理服务
            return oriUrl[0];
        }
        ProxyParam proxyParam = new ProxyParam();
        proxyParam.callback = callback;
        proxyParam.cacheDir = new File(AudioUtils.isSong(audio.sid) ? StorageUtil.getSongCacheDir() : StorageUtil.getOtherCacheDir());
        proxyParam.cacheId = calCacheId(oriUrl[0]);
        proxyParam.finalFile = AudioUtils.getAudioTMDFile(audio);
        proxyParam.info = JsonHelper.toJson(AudioConverts.convert2TXZAudio(audio)).getBytes();
        proxyParam.tag = SessionManager.get().getTag(audio.sid, audio.id);
        return ProxyUtils.getProxyUrl(oriUrl, proxyParam);
    }

    private static String calCacheId(String key) {
        try {
            MessageDigest mdInst = MessageDigest.getInstance("MD5");
            byte[] md5Bytes = mdInst.digest(key.getBytes());
            StringBuilder hexValue = new StringBuilder();
            for (byte md5Byte : md5Bytes) {
                int val = ((int) md5Byte) & 0xff;
                if (val < 16) {
                    hexValue.append("0");
                }
                hexValue.append(Integer.toHexString(val));
            }
            return hexValue.toString();
        } catch (Exception e) {
            return null;
        }
    }

    public static void releaseProxyRequest(int sid, long id) {
        AppLogic.runOnBackGround(() -> {
            SessionManager.get().removeSession(SessionManager.get().getTag(sid, id));
        });
    }
}
