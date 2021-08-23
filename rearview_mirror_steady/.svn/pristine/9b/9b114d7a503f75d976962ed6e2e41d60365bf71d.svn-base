package com.txznet.audio.player.core.base;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.CallSuper;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.OnPlayerStateChangeListener;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Objects;

public abstract class BasePlayer implements IMediaPlayer {

    private Handler mMainHandler;
    protected OnPlayerStateChangeListener mOnPlayStateChangeListener;
    private long mLastPos;
    protected @PlayState
    int mCurrPlayState = STATE_ON_IDLE;

    protected BasePlayer() {
        if (!supportProgressNotify() || !supportBufferStateNotify()) {
            mMainHandler = new Handler(Looper.getMainLooper());
        }
    }

    private Runnable mSendDurationTask = new Runnable() {
        @Override
        public void run() {
            long currPos = getCurrentPosition();
            if (mOnPlayStateChangeListener != null) {
                if (currPos != getDuration()) {
                    mOnPlayStateChangeListener.onProgressChanged(currPos, getDuration());
                }
                if (!supportBufferStateNotify()) {
                    if (currPos == mLastPos && getPlayState() != STATE_ON_BUFFERING && getPlayState() != STATE_ON_PAUSED) {
                        mCurrPlayState = STATE_ON_BUFFERING;
                        mOnPlayStateChangeListener.onPlayStateChanged(STATE_ON_BUFFERING);
                    } else if (currPos != 0 && currPos != mLastPos && getPlayState() != STATE_ON_PLAYING) {
                        mCurrPlayState = STATE_ON_PLAYING;
                        mOnPlayStateChangeListener.onPlayStateChanged(STATE_ON_PLAYING);
                    }
                }
            }
            mLastPos = currPos;
            mMainHandler.removeCallbacks(mSendDurationTask);
            mMainHandler.postDelayed(mSendDurationTask, 1000);
        }
    };

    @CallSuper
    @Override
    public void start() {
        if (!supportProgressNotify() || !supportProgressNotify()) {
            if (mMainHandler == null) {
                mMainHandler = new Handler(Looper.getMainLooper());
            }
        }
        if (mMainHandler != null) {
            mMainHandler.postDelayed(mSendDurationTask, 1000);
        }
    }

    @CallSuper
    @Override
    public void release() {
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(mSendDurationTask);
        }
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(STATE_ON_END);
            mOnPlayStateChangeListener = null;
        }
    }

    @CallSuper
    @Override
    public void pause() {
        if (mMainHandler != null) {
            mSendDurationTask.run();
            mMainHandler.removeCallbacks(mSendDurationTask);
        }
    }

    @CallSuper
    @Override
    public void stop() {
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(mSendDurationTask);
        }
    }

    @Override
    public void reset() {
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(mSendDurationTask);
        }
    }

    @CallSuper
    @Override
    public void seekTo(long msec) {
        if (mMainHandler != null) {
            mMainHandler.removeCallbacks(mSendDurationTask);
        }
    }

    /**
     * 是否支持对外广播进度值，若不支持则采用模拟的进度值监听对外进行广播
     */
    protected boolean supportProgressNotify() {
        return true;
    }

    /**
     * 是否支持对外广播缓冲状态，若不支持则采用模拟的进度值监听，进度值未变化则判断为处于缓冲中
     */
    protected boolean supportBufferStateNotify() {
        return true;
    }

    @Override
    public void setOnPlayStateChangeListener(final OnPlayerStateChangeListener listener) {
        mOnPlayStateChangeListener = (OnPlayerStateChangeListener) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{OnPlayerStateChangeListener.class}, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                if ("onCompletion".equals(method.getName())) { // 播放完毕
                    if (mMainHandler != null) {
                        mMainHandler.removeCallbacks(mSendDurationTask);
                    }
                    if (!supportProgressNotify() && mOnPlayStateChangeListener != null) {
                        mOnPlayStateChangeListener.onProgressChanged(getDuration(), getDuration());
                    }
                } else if ("onPlayStateChanged".equals(method.getName())) {
                    if (args != null && Objects.equals(STATE_ON_ERROR, args[0])) {
                        if (mMainHandler != null) {
                            mMainHandler.removeCallbacks(mSendDurationTask);
                        }
                    }
                }
                return method.invoke(listener, args);
            }
        });
    }

    @Override
    public int getPlayState() {
        return mCurrPlayState;
    }
}
