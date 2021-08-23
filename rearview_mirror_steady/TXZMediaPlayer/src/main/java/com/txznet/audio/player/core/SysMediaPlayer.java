package com.txznet.audio.player.core;

import android.media.MediaPlayer;

import com.txznet.audio.ErrCode;
import com.txznet.audio.player.core.base.BasePlayer;
import com.txznet.audio.player.util.PlayStateUtil;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.util.LogUtil;

import java.io.IOException;

public class SysMediaPlayer extends BasePlayer {
    private static final String TAG = "SysMediaPlayer";
    private MediaPlayer mMediaPlayer;
    private String mPath;

    public SysMediaPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (STATE_ON_ERROR == mCurrPlayState) {
                    return;
                }
                notifyCompletion();
                notifyPlayStateChange(STATE_ON_STOPPED);
            }
        });
        mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                notifyPlayStateChange(STATE_ON_PREPARED);
            }
        });
        mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                notifyError(new Error(ErrCode.ERROR_MEDIA_SYS_PLAYER, what + "-" + extra, "播放音频发生错误"));
                notifyPlayStateChange(STATE_ON_ERROR);
                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new MediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(MediaPlayer mp) {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onSeekComplete();
                }
            }
        });
    }

    @Override
    public void setDataSource(String path) {
        if (STATE_ON_IDLE != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", setDataSource illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        LogUtil.d(TAG, this.hashCode() + ", setDataSource path= " + path);
        mPath = path;
        try {
            mMediaPlayer.setDataSource(path);
            notifyPlayStateChange(STATE_ON_INITIALIZED);
        } catch (IOException e) {
            e.printStackTrace();
            notifyError(new Error(ErrCode.ERROR_MEDIA_NOT_FOUND, mPath + ": open failed (no such file or directory)", "文件不存在"));
        }
    }

    @Override
    public String getDataSource() {
        return mPath;
    }

    @Override
    public void prepareAsync() {
        if (STATE_ON_INITIALIZED != mCurrPlayState && STATE_ON_STOPPED != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", prepareAsync illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        LogUtil.d(TAG, this.hashCode() + ", prepareAsync");
        try {
            mMediaPlayer.prepareAsync();
            notifyPlayStateChange(STATE_ON_PREPARING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void start() {
        if (STATE_ON_PREPARED != mCurrPlayState && STATE_ON_PAUSED != mCurrPlayState && STATE_ON_PLAYING != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", start illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        LogUtil.d(TAG, this.hashCode() + ", start");
        super.start();
        try {
            mMediaPlayer.start();
            notifyPlayStateChange(STATE_ON_PLAYING);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() {
        if (STATE_ON_PLAYING != mCurrPlayState && STATE_ON_PAUSED != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", stop illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        super.stop();
        LogUtil.d(TAG, this.hashCode() + ", stop");
        try {
            mMediaPlayer.stop();
            notifyPlayStateChange(STATE_ON_STOPPED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void pause() {
        if (STATE_ON_PLAYING != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", pause illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        super.pause();
        LogUtil.d(TAG, this.hashCode() + ", pause");
        try {
            mMediaPlayer.pause();
            notifyPlayStateChange(STATE_ON_PAUSED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean isPlaying() {
        LogUtil.d(TAG, this.hashCode() + ", isPlaying");
        try {
            if (mMediaPlayer != null) {
                return mMediaPlayer.isPlaying();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void seekTo(long msec) {
        super.seekTo(msec);
        if (STATE_ON_PLAYING != mCurrPlayState && STATE_ON_PREPARED != mCurrPlayState && STATE_ON_PAUSED != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", seekTo illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        LogUtil.d(TAG, this.hashCode() + ", seekTo " + msec);
        try {
            mMediaPlayer.seekTo((int) msec);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public long getCurrentPosition() {
        if (STATE_ON_IDLE == mCurrPlayState
                || STATE_ON_INITIALIZED == mCurrPlayState
                || STATE_ON_PREPARING == mCurrPlayState
                || STATE_ON_END == mCurrPlayState
                || STATE_ON_ERROR == mCurrPlayState) {
            return 0;
        }
        try {
            return mMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (STATE_ON_IDLE == mCurrPlayState
                || STATE_ON_INITIALIZED == mCurrPlayState
                || STATE_ON_PREPARING == mCurrPlayState
                || STATE_ON_END == mCurrPlayState
                || STATE_ON_ERROR == mCurrPlayState) {
            return 0;
        }
        try {
            return mMediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void release() {
        super.release();
        LogUtil.d(TAG, this.hashCode() + ", release");
        try {
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnSeekCompleteListener(null);
            mMediaPlayer.setOnBufferingUpdateListener(null);
            mMediaPlayer.release();
            mMediaPlayer = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyPlayStateChange(STATE_ON_END);
    }

    @Override
    public void reset() {
        try {
            stop();
            mMediaPlayer.reset();
            mPath = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyPlayStateChange(STATE_ON_IDLE);
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public void setAudioStreamType(int streamtype) {
        if (mMediaPlayer != null) {
            mMediaPlayer.setAudioStreamType(streamtype);
        }
    }

    // 通知
    private void notifyPlayStateChange(@PlayState int state) {
        mCurrPlayState = state;
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(state);
        }
    }

    private void notifyCompletion() {
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onCompletion();
        }
    }

    private void notifyError(Error error) {
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onError(error);
        }
    }

    // 交给基类模拟进度值通知和缓冲通知
    @Override
    protected boolean supportProgressNotify() {
        return false;
    }

    @Override
    protected boolean supportBufferStateNotify() {
        return false;
    }
}
