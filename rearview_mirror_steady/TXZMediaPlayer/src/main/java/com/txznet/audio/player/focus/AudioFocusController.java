package com.txznet.audio.player.focus;

import android.content.Context;
import android.media.AudioManager;
import android.support.annotation.IntDef;
import android.util.SparseBooleanArray;

import com.txznet.audio.player.AudioPlayer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

import static android.media.AudioManager.AUDIOFOCUS_GAIN;
import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE;
import static android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK;
import static android.media.AudioManager.AUDIOFOCUS_LOSS;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_FAILED;
import static android.media.AudioManager.AUDIOFOCUS_REQUEST_GRANTED;

/**
 * 音频焦点管理工具
 */
public class AudioFocusController {
    private static final String TAG = "AudioFocusController";

    @IntDef({
            AUDIOFOCUS_GAIN,
            AUDIOFOCUS_GAIN_TRANSIENT,
            AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK,
            AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DurationHint {
    }

    @IntDef({
            AUDIOFOCUS_REQUEST_FAILED,
            AUDIOFOCUS_REQUEST_GRANTED
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface RequestResult {
    }

    private @IAudioFocusHandler.AudioFocus
    int mCurrFocus = AUDIOFOCUS_LOSS;//默认焦点不再自己身上
    private IAudioFocusHandler mAudioFocusHandler;
    private AudioManager mAudioManager;
    private WeakReference<AudioPlayer> mPlayerRef;
    private static final SparseBooleanArray GAIN_STATES = new SparseBooleanArray(2);

    private AudioManager.OnAudioFocusChangeListener mOnAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            mCurrFocus = focusChange;
            synchronized (GAIN_STATES) {
                GAIN_STATES.put(AudioFocusController.this.hashCode(), isFocusInThere());
            }
            if (mAudioFocusHandler != null && mPlayerRef.get() != null) {
                mAudioFocusHandler.onAudioFocusChange(mPlayerRef.get(), focusChange);
            }
        }
    };

    public AudioFocusController(AudioPlayer player, IAudioFocusHandler audioFocusHandler) {
        mAudioManager = (AudioManager) GlobalContext.get().getSystemService(Context.AUDIO_SERVICE);
        mPlayerRef = new WeakReference<>(player);
        mAudioFocusHandler = audioFocusHandler;
    }

    /**
     * 申请焦点，已经获得焦点的不会重新申请
     */
    public @RequestResult
    int requestAudioFocus(int steamtype, @DurationHint int durationHint) {
        if (AUDIOFOCUS_GAIN == mCurrFocus) {
            return AUDIOFOCUS_REQUEST_GRANTED;
        }
        int i = mAudioManager.requestAudioFocus(mOnAudioFocusChangeListener, steamtype, durationHint);
        if (AUDIOFOCUS_REQUEST_GRANTED == i) {
            mCurrFocus = AUDIOFOCUS_GAIN;
            synchronized (GAIN_STATES) {
                GAIN_STATES.put(AudioFocusController.this.hashCode(), isFocusInThere());
            }
            LogUtil.d(TAG, "onAudioFocusChange focusChange=" + AUDIOFOCUS_GAIN);
        }
        LogUtil.d(TAG, "requestAudioFocus steamtype=" + steamtype + ", durationHint=" + durationHint + ", result=" + i);
        return i;
    }

    /**
     * 释放焦点，已经释放的不会重复释放
     */
    public @RequestResult
    int abandonAudioFocus() {
        if (AUDIOFOCUS_LOSS == mCurrFocus) {
            return AUDIOFOCUS_REQUEST_GRANTED;
        }
        int i = mAudioManager.abandonAudioFocus(mOnAudioFocusChangeListener);
        if (AUDIOFOCUS_REQUEST_GRANTED == i) {
            mCurrFocus = AUDIOFOCUS_LOSS;
            synchronized (GAIN_STATES) {
                GAIN_STATES.delete(AudioFocusController.this.hashCode());
            }
        }
        LogUtil.d(TAG, "abandonAudioFocus result=" + i);
        return i;
    }

    /**
     * 获取当前焦点
     */
    public @IAudioFocusHandler.AudioFocus
    int getCurrentFocus() {
        return mCurrFocus;
    }

    /**
     * 焦点是否在此播放器实例上,只限焦点为1的时候,(-1,-2,等)都视作没有焦点
     */
    public boolean isFocusInThere() {
        return AUDIOFOCUS_GAIN == mCurrFocus;
    }

    /**
     * 焦点是否在应用上
     */
    public static boolean isFocusInApp() {
        boolean result = false;
        synchronized (GAIN_STATES) {
            for (int i = 0; i < GAIN_STATES.size(); i++) {
                boolean isFocus = GAIN_STATES.get(GAIN_STATES.keyAt(i));
                result |= isFocus;
            }
        }
        return result;
    }
}
