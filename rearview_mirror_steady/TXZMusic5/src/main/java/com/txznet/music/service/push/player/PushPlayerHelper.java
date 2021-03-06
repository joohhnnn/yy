package com.txznet.music.service.push.player;

import android.media.AudioManager;
import android.text.TextUtils;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.audio.player.core.ijk.IjkMediaPlayer;
import com.txznet.audio.player.entity.Audio;
import com.txznet.audio.player.queue.PlayQueue;
import com.txznet.comm.err.Error;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.data.http.NetRequestManager;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.source.TXZMusicDataSource;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.TXZUri;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.proxy.ProxyParam;
import com.txznet.proxy.ProxyUtils;
import com.txznet.proxy.SessionManager;
import com.txznet.proxy.cache.LocalBuffer;
import com.txznet.proxy.util.StorageUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.File;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.music.service.push.player.IConstant.KEY_AUDIO_ARTIST;
import static com.txznet.music.service.push.player.IConstant.KEY_AUDIO_ID;
import static com.txznet.music.service.push.player.IConstant.KEY_AUDIO_NAME;
import static com.txznet.music.service.push.player.IConstant.LISTENER_AUDIO;
import static com.txznet.music.service.push.player.IConstant.LISTENER_KEY_DURATION;
import static com.txznet.music.service.push.player.IConstant.LISTENER_KEY_ORDER;
import static com.txznet.music.service.push.player.IConstant.LISTENER_KEY_PROGRESS;
import static com.txznet.music.service.push.player.IConstant.LISTENER_KEY_STATE;
import static com.txznet.music.service.push.player.IConstant.LISTENER_PLAYER_PROGRESS;
import static com.txznet.music.service.push.player.IConstant.LISTENER_PLAYER_QUEUE_END;
import static com.txznet.music.service.push.player.IConstant.LISTENER_PLAYER_STATE;

/**
 * @author telen
 * @date 2018/12/27,14:40
 */
public class PushPlayerHelper {

    /**
     * ????????????
     */
    private volatile static PushPlayerHelper singleton;

    private PushPlayerHelper() {
    }

    public static PushPlayerHelper getInstance() {
        if (singleton == null) {
            synchronized (PushPlayerHelper.class) {
                if (singleton == null) {
                    singleton = new PushPlayerHelper();
                }
            }
        }
        return singleton;
    }

    /**
     * @param temp ???????????????????????????
     */
    public AudioPlayer createPlayer(String packageName, boolean temp) {
        AudioPlayer audioPlayer = null;
        if (temp) {
//??????
            audioPlayer = AudioPlayer.newInstance();
            audioPlayer.setConfig(new AudioPlayer.Config.Builder().autoPlay(true).setPlayerImplClass(IjkMediaPlayer.class).build());
        } else {
            audioPlayer = AudioPlayer.getDefault();
        }

        initAudioFocusHandler(audioPlayer);
        //2019???1???26???10:52:48?????????????????????????????????
//        initPlayerListener(packageName, audioPlayer);
        initPlayUrlProvider(audioPlayer, true);

        //??????????????????
        audioPlayer.useQueuePlay();

//        initQueueInterceptor(packageName, audioPlayer);


        return audioPlayer;
    }

    private void initQueueInterceptor(String packageName, AudioPlayer audioPlayer) {
        //??????
        audioPlayer.setAudioPlayerQueueInterceptor(new AudioPlayer.AudioPlayerQueueInterceptor() {
            @Override
            public void pickPrevItem(PlayQueue queue, Audio oriAudio, Callback callback) {
                if (oriAudio == null) {
                    //??????????????????
                    notifyQueueEnd(packageName, queue.getFirstItem(), 0);
                } else {
                    callback.onPickResult(oriAudio);
                }
            }

            @Override
            public void pickNextItem(PlayQueue queue, Audio oriAudio, boolean fromUser, Callback callback) {
                if (oriAudio == null) {
                    //??????????????????
                    notifyQueueEnd(packageName, queue.getLastItem(), 1);
                } else {
                    callback.onPickResult(oriAudio);
                }
            }
        });

    }

    /**
     * ?????????????????????
     *
     * @param order ?????? 0 ???????????????, 1 ???????????????
     */
    private void notifyQueueEnd(String packageName, Audio audio, int order) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put(LISTENER_KEY_ORDER, order);
        jsonBuilder.put(KEY_AUDIO_ID, audio.id);
        notifyEvent(audio, packageName, LISTENER_PLAYER_QUEUE_END, jsonBuilder);
    }


    // ??????????????????
    private void initAudioFocusHandler(AudioPlayer audioPlayer) {
        audioPlayer.setAudioFocusHandler((player, focusChange) -> {

            int lostFlag;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN: // ??????????????????
                    if (TXZFileConfigUtil.getBooleanSingleConfig("focus_gain_play", true)) {
                        audioPlayer.setVolume(1, 1);
                        audioPlayer.start();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS: // ????????????
                    audioPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT: //  ??????????????????
                    audioPlayer.pause();
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK: // ????????????:
                    float duck = (float) TXZFileConfigUtil.getDoubleSingleConfig("focus_duck_play", 0.5);
                    audioPlayer.setVolume(duck, duck);
                    break;
                default:
                    break;
            }
        });
    }

    /**
     * ????????????????????????
     *
     * @param audio
     * @return
     */
    private boolean isValidEvent(Audio audio) {
        return audio != null;
    }

    // ????????????
    private void initPlayerListener(String packageName, AudioPlayer audioPlayer) {
        audioPlayer.setAudioPlayerStateChangeListener(new AudioPlayer.AudioPlayerStateChangeListener() {

            Audio mAudio = null;

            @Override
            public void onAudioChanged(Audio audio, boolean willEnd) {
                if (BuildConfig.DEBUG) {
                    Logger.d(Constant.LOG_TAG_PUSH, getClass().getSimpleName() + ",onAudioChanged:" + audio);
                }

                mAudio = audio;
                if (isValidEvent(mAudio)) {
                    JSONBuilder jsonBuilder = new JSONBuilder();
                    jsonBuilder.put(KEY_AUDIO_NAME, audio.name);
                    jsonBuilder.put(KEY_AUDIO_ARTIST, Arrays.deepToString(audio.artist));
                    notifyEvent(mAudio, packageName, LISTENER_AUDIO, jsonBuilder);
                }
            }


            @Override
            public void onQueuePlayEnd() {

            }

            @Override
            public void onPlayStateChanged(int state) {
                if (isValidEvent(mAudio)) {
                    JSONBuilder jsonBuilder = new JSONBuilder();
                    jsonBuilder.put(LISTENER_KEY_STATE, state);
                    jsonBuilder.put(KEY_AUDIO_ID, mAudio.id);
                    notifyEvent(mAudio, packageName, LISTENER_PLAYER_STATE, jsonBuilder);
                }

            }

            @Override
            public void onProgressChanged(long position, long duration) {
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put(LISTENER_KEY_PROGRESS, position);
                jsonBuilder.put(LISTENER_KEY_DURATION, duration);
                jsonBuilder.put(KEY_AUDIO_ID, mAudio.id);
                notifyEvent(mAudio, packageName, LISTENER_PLAYER_PROGRESS, jsonBuilder);
            }

            @Override
            public void onSeekComplete() {

            }

            @Override
            public void onCompletion() {

            }

            @Override
            public void onError(Error error) {

            }
        });
    }

    private void notifyEvent(Audio audio, String packageName, String cmd, JSONBuilder jsonBuilder) {
        if (BuildConfig.DEBUG) {
            Logger.d(Constant.LOG_TAG_PUSH, getClass().getSimpleName() + ",packageName=" + packageName + ",cmd= " + cmd + ",notify:" + jsonBuilder);
        }

        if (audio == null) {
            return;
        }

        // TODO: 2018/12/27 ??????
//        ServiceManager.getInstance().sendInvoke(packageName, cmd, jsonBuilder.toBytes(), null);
    }

    // ???????????????
    private void initPlayUrlProvider(AudioPlayer audioPlayer, boolean needProxy) {
        audioPlayer.setPlayUrlProvider((audio, urlCallback) -> {
            final List<String> oriUrl = new ArrayList<>();
            if (TextUtils.isEmpty(audio.sourceUrl)) {
                // ??????audio???????????????????????????????????????????????????????????????
            } else {
                if (audio.sourceUrl.startsWith("txz")) { // ??????
                    NetRequestManager.removeSameRequest("getTXZUrl");
                    TXZUri uri = TXZUri.parse(audio.sourceUrl);
                    if (TXZAudio.DOWNLOADTYPE_PROXY.equals(uri.downloadType)) { // qq ???????????????
                        Disposable disposable = TXZMusicDataSource.get().getAudioPlayUrls(AudioConverts.convert2Audio(audio))
                                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                                .subscribe(playUrlInfo -> {
                                    List<String> urls = new ArrayList<>();
                                    urls.add(playUrlInfo.strUrl);
                                    if (playUrlInfo.arrBackUpUrl != null) {
                                        urls.addAll(playUrlInfo.arrBackUpUrl);
                                    }
                                    urlCallback.onPlayUrlResp(getProxyUrl(audioPlayer, audio, urls.toArray(new String[0])));
                                }, throwable -> {
                                    urlCallback.onError();
                                });
                        NetRequestManager.addMonitor("getTXZUrl", disposable);
                        return;
                    } else { // ????????????downloadUrl??????
                        urlCallback.onPlayUrlResp(getProxyUrl(audioPlayer, audio, uri.downloadUrl));
                    }
                } else {
                    if (needProxy) {
                        urlCallback.onPlayUrlResp(getProxyUrl(audioPlayer, audio, audio.sourceUrl));
                    } else {
                        urlCallback.onPlayUrlResp(audio.sourceUrl);
                    }

                }
            }
        });
    }

    // ??????????????????
    private String getProxyUrl(AudioPlayer audioPlayer, Audio audio, String... oriUrl) {
//        if (!TXZFileConfigUtil.getBooleanSingleConfig(Configuration.Key.ENABLE_MEDIA_PROXY, Configuration.DefVal.ENABLE_MEDIA_PROXY)) { // ????????????????????????
//            return oriUrl[0];
//        }
        ProxyParam proxyParam = new ProxyParam();
        proxyParam.callback = new ProxyParam.IProxyCallback() {
            @Override
            public void onError(int errorCode, String desc, String hint) {
            }

            @Override
            public void onBufferingUpdate(List<LocalBuffer> buffers) {
            }

            @Override
            public void onDownloadComplete() {
            }

            @Override
            public float getPlayPercent() {
                return audioPlayer.getCurrentPosition() * 1f / audioPlayer.getDuration();
            }

            @Override
            public long getDuration() {
                return 0;
            }

            @Override
            public boolean isPlaying() {
                return true;
            }
        };
        proxyParam.cacheDir = new File(StorageUtil.getSongCacheDir());
        proxyParam.cacheId = calCacheId(oriUrl[0]);
        //????????????tmd??????
        proxyParam.finalFile = AudioUtils.getAudioTMDFile(audio);
        proxyParam.info = JsonHelper.toJson(AudioConverts.convert2TXZAudio(audio)).getBytes();
        proxyParam.tag = SessionManager.get().getTag(audio.sid, audio.id);
        return ProxyUtils.getProxyUrl(oriUrl, proxyParam);
    }

    private String calCacheId(String key) {
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

}
