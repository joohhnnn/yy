package com.txznet.audio.player.core.ijk;

import com.txznet.audio.ErrCode;
import com.txznet.audio.player.core.base.BasePlayer;
import com.txznet.audio.player.util.PlayStateUtil;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.util.LogUtil;

import java.io.IOException;

public class IjkMediaPlayer extends BasePlayer {
    private static final String TAG = "IjkMediaPlayer";
    private tv.danmaku.ijk.media.player.IMediaPlayer mMediaPlayer;
    private String mPath;
    private boolean isSeeking;
    private long mLastSeekPos;

    private boolean hasInit;

    public IjkMediaPlayer() {

    }

    private void init() {
        hasInit = true;
        mMediaPlayer = new tv.danmaku.ijk.media.player.IjkMediaPlayer();
        mMediaPlayer.setOnCompletionListener(new tv.danmaku.ijk.media.player.IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
                if (STATE_ON_ERROR == mCurrPlayState) {
                    return;
                }
                notifyCompletion();
                notifyPlayStateChange(STATE_ON_STOPPED);
            }
        });
        mMediaPlayer.setOnPreparedListener(new tv.danmaku.ijk.media.player.IMediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
                LogUtil.d(TAG, IjkMediaPlayer.this.hashCode() + ", onPrepared");
                mp.pause();
                if (isSeeking) {
                    mp.seekTo(mLastSeekPos);
                } else {
                    notifyPlayStateChange(STATE_ON_PREPARED);
                }
            }
        });
        mMediaPlayer.setOnErrorListener(new tv.danmaku.ijk.media.player.IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(tv.danmaku.ijk.media.player.IMediaPlayer mp, int what, int extra) {
                LogUtil.d(TAG, IjkMediaPlayer.this.hashCode() + ", onError what=" + what + ", extra=" + extra);
                if (what == -10000 && extra == 0) {
                    LogUtil.e(TAG, IjkMediaPlayer.this.hashCode() + ", throw -10000-0");
                    if (STATE_ON_PREPARING == mCurrPlayState) {
                        notifyPlayStateChange(STATE_ON_PAUSED);
                    }
                    if (isSeeking) {
                        if (getDuration() == 0 || STATE_ON_PAUSED == mCurrPlayState) {
                            // FIXME: 2019/5/22 概率性duration=0，若此时强行seekTo，则会无限报错
                            LogUtil.w(TAG, IjkMediaPlayer.this.hashCode() + ", wrong seek duration=" + getDuration() + ", state=" + PlayStateUtil.convert2Str(mCurrPlayState));
                            extra = 1;
                        } else {
                            seekTo(mLastSeekPos);
                            return true;
                        }
                    }
                }
                isSeeking = false;
                notifyError(new Error(ErrCode.ERROR_MEDIA_SYS_PLAYER, what + "-" + extra, "播放音频发生错误"));
                notifyPlayStateChange(STATE_ON_ERROR);
                return false;
            }
        });
        mMediaPlayer.setOnSeekCompleteListener(new tv.danmaku.ijk.media.player.IMediaPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete(tv.danmaku.ijk.media.player.IMediaPlayer mp) {
                LogUtil.d(TAG, IjkMediaPlayer.this.hashCode() + ", onSeekComplete");
                // FIXME: 2019/4/24 ijkplayer的seekTo不是覆盖式的，在onSeekComplete之前继续seekTo，还是seekTo之前的地方
                if (Math.abs(mp.getCurrentPosition() - mLastSeekPos) < 1000) {
                    isSeeking = false;
                    if (mOnPlayStateChangeListener != null) {
                        mOnPlayStateChangeListener.onSeekComplete();
                    }
                } else {
                    seekTo(mLastSeekPos);
                }
            }
        });
    }

    @Override
    public synchronized void setDataSource(String path) {
        if (!hasInit) {
            init();
        }
        if (STATE_ON_IDLE != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", setDataSource illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        isSeeking = false;
        LogUtil.d(TAG, this.hashCode() + ", setDataSource path= " + path);
        mPath = path;
        try {
            mMediaPlayer.setDataSource(path);
            notifyPlayStateChange(STATE_ON_INITIALIZED);
        } catch (IOException e) {
            e.printStackTrace();
            notifyError(new Error(ErrCode.SOURCE_PLAYER, ErrCode.ERROR_MEDIA_NOT_FOUND, mPath + ": open failed (no such file or directory)", "文件不存在"));
        }
    }

    @Override
    public synchronized String getDataSource() {
        return mPath;
    }

    @Override
    public synchronized void prepareAsync() {
        if (!hasInit) {
            init();
        }
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
    public synchronized void start() {
        if (!hasInit) {
            init();
        }
        if (STATE_ON_PREPARED != mCurrPlayState && STATE_ON_PAUSED != mCurrPlayState && STATE_ON_STOPPED != mCurrPlayState
                && STATE_ON_BUFFERING != mCurrPlayState && STATE_ON_PLAYING != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", start illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));

            if (STATE_ON_IDLE == mCurrPlayState) {
                notifyPlayStateChange(STATE_ON_BUFFERING);
            }

            return;
        }
        if (STATE_ON_PLAYING == mCurrPlayState) {
            try {
                mMediaPlayer.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return;
        }

        if (isSeeking) {
            LogUtil.e(TAG, this.hashCode() + ", start illegal isSeeking=true");
            if (STATE_ON_BUFFERING != mCurrPlayState) {
                notifyPlayStateChange(STATE_ON_BUFFERING);
            }
            seekTo(mLastSeekPos);
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
    public synchronized void stop() {
        if (!hasInit) {
            init();
        }
        if (STATE_ON_PLAYING != mCurrPlayState && STATE_ON_PAUSED != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", stop illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        super.stop();
        isSeeking = false;
        LogUtil.d(TAG, this.hashCode() + ", stop");
        try {
            mMediaPlayer.stop();
            notifyPlayStateChange(STATE_ON_STOPPED);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void pause() {
        if (!hasInit) {
            init();
        }
        if (STATE_ON_PLAYING != mCurrPlayState && STATE_ON_BUFFERING != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", pause illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        super.pause();
        notifyPlayStateChange(STATE_ON_PAUSED);
        LogUtil.d(TAG, this.hashCode() + ", pause");
        try {
            mMediaPlayer.pause();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized boolean isPlaying() {
//        LogUtil.d(TAG, this.hashCode() + ", isPlaying");
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
    public synchronized void seekTo(long msec) {
        if (!hasInit) {
            init();
        }
        super.seekTo(msec);

        if (STATE_ON_PLAYING != mCurrPlayState && STATE_ON_PREPARED != mCurrPlayState && STATE_ON_PAUSED != mCurrPlayState
                && STATE_ON_BUFFERING != mCurrPlayState
                && STATE_ON_STOPPED != mCurrPlayState) {
            LogUtil.e(TAG, this.hashCode() + ", seekTo illegal on " + PlayStateUtil.convert2Str(mCurrPlayState));
            return;
        }
        long duration = getDuration();
        if (duration == 0) {
            LogUtil.e(TAG, this.hashCode() + ", seekTo illegal on duration get failed");
            return;
        }
        LogUtil.d(TAG, this.hashCode() + ", seekTo " + msec + "/" + duration + "__" + PlayStateUtil.convert2Str(mCurrPlayState));
        try {
            notifyPlayStateChange(STATE_ON_BUFFERING);
            mMediaPlayer.pause();
            mMediaPlayer.stop();
            mLastSeekPos = msec;
            if (msec > 0 && msec == duration) {
                notifyCompletion();
                notifyPlayStateChange(STATE_ON_STOPPED);
                return;
            }
            isSeeking = true;
            mMediaPlayer.reset();
            mMediaPlayer.setDataSource(mPath);
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized long getCurrentPosition() {
        try {
            return mMediaPlayer.getCurrentPosition();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public synchronized long getDuration() {
        try {
            return mMediaPlayer.getDuration();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public synchronized void release() {
        if (!hasInit) {
            init();
        }
        isSeeking = false;
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
    public synchronized void reset() {
        if (!hasInit) {
            init();
        }

        isSeeking = false;

        if (mPath == null) {
            super.stop();
            notifyPlayStateChange(STATE_ON_IDLE);
            return;
        }

        try {
            super.stop();
            mMediaPlayer.stop();
            mMediaPlayer.reset();
            mPath = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        notifyPlayStateChange(STATE_ON_IDLE);
    }

    @Override
    public synchronized void setVolume(float leftVolume, float rightVolume) {
        if (!hasInit) {
            init();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public synchronized void setAudioStreamType(int streamtype) {
        if (!hasInit) {
            init();
        }
        if (mMediaPlayer != null) {
            mMediaPlayer.setAudioStreamType(streamtype);
        }
    }

    // 通知
    private synchronized void notifyPlayStateChange(@PlayState int state) {
        mCurrPlayState = state;
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(state);
        }
    }

    private synchronized void notifyCompletion() {
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onCompletion();
        }
    }

    private synchronized void notifyError(Error error) {
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onError(error);
        }
    }

    @Override
    protected boolean supportProgressNotify() {
        return false;
    }

    @Override
    protected boolean supportBufferStateNotify() {
        return false;
    }
}
