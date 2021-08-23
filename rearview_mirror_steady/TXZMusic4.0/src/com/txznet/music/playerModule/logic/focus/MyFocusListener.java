package com.txznet.music.playerModule.logic.focus;

import android.content.Context;
import android.media.AudioManager;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.Utils;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * Created by ASUS User on 2016/12/5.
 */

public class MyFocusListener implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "music:focus:";
    private boolean isNeedRequest = true;
    private static AudioManager audioManager;
    private int currentFocusInt = AudioManager.AUDIOFOCUS_LOSS;//默认焦点不再自己身上
    public static boolean playerInFocus = false;//1表示在焦点上,0表示不再焦点上.
    private boolean isManualPlay = false;//手动调用了播放,表示用户主动行为,

    static {
        audioManager = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);
    }


    //##创建一个单例类##
    private volatile static MyFocusListener singleton;

    private MyFocusListener() {
    }

    public static MyFocusListener getInstance() {
        if (singleton == null) {
            synchronized (MyFocusListener.class) {
                if (singleton == null) {
                    singleton = new MyFocusListener();
                }
            }
        }
        return singleton;
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogUtil.d(TAG + "MyFocusListener::focusChange::" + focusChange);
        currentFocusInt = focusChange;

        ReportEvent.reportFocusChange(PlayInfoManager.getInstance().getCurrentAudio(), focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                if (needPlay()) {
                    PlayEngineFactory.getEngine().setVolume(EnumState.Operation.auto, 1.0f);
                    PlayEngineFactory.getEngine().play(EnumState.Operation.auto);
                    ReportEvent.reportFocusPlay(PlayInfoManager.getInstance().getCurrentAudio());
                }
                //如果音频回来的时候,判断丢失焦点之前的情况
                notifyInFocus();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                int lost = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS, 1);
                Logger.d(TAG, "AUDIOFOCUS_LOSS:" + lost + "," + SharedPreferencesUtils.isReleaseAudioFocus());
                if (lost == 1 && SharedPreferencesUtils.isReleaseAudioFocus()) {
                    abandonAudioFocus();
                    //因为是pause所以不需要释放媒体按键焦点，否则下次在播放的时候，不响应媒体按键，除非切歌才响应
//                    HeadSetHelper.getInstance().close(GlobalContext.get());
//                    PlayEngineFactory.getEngine().release(EnumState.Operation.auto);
                    pause();
                } else if (lost == 0) {
                    //不处理
                } else {
                    pause();
                }

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                pause();
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                Audio currentAudio = PlayInfoManager.getInstance().getCurrentAudio();
                if (currentAudio != null) {
                    int lostMusic = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_M, 1);
                    int lostRadio = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_R, 2);
                    float lostFactor = Float.parseFloat(FileConfigUtil.getStringConfig(TXZFileConfigUtil.KEY_MUSIC_LOSS_TRANSIENT_FACTOR, "0.5"));
                    lostFactor = Math.min(Math.max(0f, lostFactor), 1.0f);//[0.0-- 1.0]
                    Logger.d(TAG, "AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:music=" + lostMusic + ",radio:" + lostRadio + ",factor:" + lostFactor);
                    if (Utils.isSong(currentAudio.getSid())) {
                        if (lostMusic == 0) {
                        } else if (lostMusic == 1) {
                            PlayEngineFactory.getEngine().setVolume(EnumState.Operation.auto, lostFactor);//修改bug:TXZ-9508
                        } else {
                            pause();
                        }
                    } else {
                        if (lostRadio == 0) {
                        } else if (lostRadio == 2) {
                            pause();
                        } else {
                            PlayEngineFactory.getEngine().setVolume(EnumState.Operation.auto, lostFactor);//修改bug:TXZ-9508
                        }
                    }
                }
                break;
        }

    }

    private void pause() {
        PlayEngineFactory.getEngine().pause(EnumState.Operation.temp);
        ReportEvent.reportFocusPause(PlayInfoManager.getInstance().getCurrentAudio());
    }

    /**
     * 焦点是否在自己身上,只限焦点为1的时候,(-1,-2,等)都视作没有焦点
     *
     * @return
     */
    public boolean focusInThere() {
        return currentFocusInt == AudioManager.AUDIOFOCUS_GAIN;
    }

    /**
     * 判断是否需要播放
     * ,用户主动行为+内部状态+音频焦点状态
     *
     * @return
     */
    private boolean needPlay() {
        if (isManualPlay()) {
            return true;
        }
        return false;
    }

    public boolean isNeedRequestFocus() {
        return isNeedRequest;
    }

    /**
     * @param durationHint 可选值AudioManager.AUDIOFOCUS_GAIN
     */
    public void requestAudioFocus(int durationHint) {
        if (isNeedRequestFocus()) {
            int i = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, durationHint);
            LogUtil.d(TAG + "requestAudioFocus,type=" + durationHint + ",i=" + i);
            if (i > 0) {
                currentFocusInt = i;
                isNeedRequest = false;
                playerInFocus = true;
            }
        }
    }

    /**
     * @param durationHint 可选值AudioManager.AUDIOFOCUS_GAIN
     */
    public void requestAudioFocusImmediately(int durationHint) {
        if (currentFocusInt != AudioManager.AUDIOFOCUS_GAIN) {
            int i = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, durationHint);
            LogUtil.d(TAG + "requestAudioFocusImmediately,type=" + durationHint + ",i=" + i);
            if (i > 0) {
                currentFocusInt = i;
                isNeedRequest = false;
                playerInFocus = true;
            }
        }
    }

    public void abandonAudioFocus() {
        if (!isNeedRequestFocus()) {
            LogUtil.d(TAG + "abandonAudioFocus");
            int i = audioManager.abandonAudioFocus(this);
            if (i > 0) {
                currentFocusInt = 0;
                isNeedRequest = true;
                playerInFocus = false;
                notifyInFocus();
            }
        }
    }

    public void notifyInFocus() {
        synchronized (this) {
            if (currentFocusInt == AudioManager.AUDIOFOCUS_GAIN) {
                notifyAll();
            }
        }
    }

    public void notifyLossFocus() {

    }

    public boolean isManualPlay() {
        return isManualPlay;
    }

    /**
     * 分别在,play,pause,prepared,seekto,和release的时候调用
     *
     * @param manualPlay
     */
    public void setManualPlay(boolean manualPlay) {
        isManualPlay = manualPlay;
    }
}
