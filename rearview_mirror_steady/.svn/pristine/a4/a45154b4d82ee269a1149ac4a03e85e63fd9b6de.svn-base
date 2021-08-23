package com.txznet.music.playerModule.logic.focus;

import android.content.Context;
import android.media.AudioManager;

import com.txznet.audio.player.TXZAudioPlayer;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.audio.player.audio.PlayerAudio;
import com.txznet.audio.player.audio.QQMusicAudio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.utils.SharedPreferencesUtils;

import java.lang.ref.WeakReference;

/**
 * Created by ASUS User on 2016/12/5.
 */

public class MyQuickPlayerFocusListener implements AudioManager.OnAudioFocusChangeListener {

    private static final String TAG = "music:focus:";
    private boolean isNeedRequest = true;
    static AudioManager audioManager;
    private WeakReference<TXZAudioPlayer> playerWeakReference;
    private int currentFocusInt = AudioManager.AUDIOFOCUS_GAIN;
    private int oldFocusInt = currentFocusInt;
    public static boolean quickInFocus = false;//1表示在焦点上,0表示不再焦点上.


    public int getCurrentFocusInt() {
        return currentFocusInt;
    }

    static {
        audioManager = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);
    }

    public MyQuickPlayerFocusListener(TXZAudioPlayer player) {
        playerWeakReference = new WeakReference<TXZAudioPlayer>(player);
    }

    @Override
    public void onAudioFocusChange(int focusChange) {
        LogUtil.logd(TAG + "MyFocusListener::focusChange::" + focusChange);
        currentFocusInt = focusChange;
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                LogUtil.logd(TAG + "currentState:" + playerWeakReference.get().getCurrentState());
                if (playerWeakReference.get() != null && oldFocusInt != currentFocusInt) {
                    playerWeakReference.get().start();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                if (playerWeakReference.get() != null/*&&playerWeakReference.get().getCurrentState() == PLAYSTATE*/) {
                    playerWeakReference.get().stop();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                if (playerWeakReference.get() != null) {
                    playerWeakReference.get().pause();
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                if (playerWeakReference.get() != null) {
                    playerWeakReference.get().pause();
                }
//                PlayEngineFactory.getEngine().setVolume(EnumState.OperState.auto, 0.2f);
                break;
        }
        oldFocusInt = currentFocusInt;
    }

    public boolean isNeedRequestFocus() {
        return isNeedRequest;
    }

    /**
     * @param durationHint 可选值AudioManager.AUDIOFOCUS_GAIN
     */
    public void requestAudioFocus(int durationHint) {
        if (isNeedRequestFocus()) {
            LogUtil.logd(TAG + "requestAudioFocus,type=" + durationHint);
            int i = audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, durationHint);
            if (i > 0) {
                currentFocusInt = i;
                isNeedRequest = false;
                quickInFocus = true;
            }
        }
    }

    public void abandonAudioFocus() {
        if (!isNeedRequestFocus()) {
            LogUtil.logd(TAG + "abandonAudioFocus");
            int i = audioManager.abandonAudioFocus(this);
            if (i > 0) {
                currentFocusInt = 0;
                isNeedRequest = true;
                quickInFocus = false;
            }
        }
    }
}
