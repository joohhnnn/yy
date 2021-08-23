package com.txznet.audio.player;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.baseModule.Constant;

import java.util.ArrayList;
import java.util.List;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

public class IjkAudioPlayer extends TXZAudioPlayer {
    public static final String TAG = "IjkAudioPlayer ";

    public static final int DEFAULT_DATA_PIECE_SIZE = 6 * PREPARE_BUFFER_DATA_TIME; // 每毫秒大概6个字节//预加载PREPARE_BUFFER_DATA_TIME这么多时间的数据

    private int mSteamType;
    private String mDataUrl;
    private IMediaPlayer mMediaPlayer;

    private boolean isError = false;
    private float mBufferingPercent = 0;
    private boolean mReleased = false;
    private long mdataPiceSize;

    protected IjkAudioPlayer(SessionInfo sess, int streamtype, String path) {
        super(sess, streamtype);
        mSteamType = streamtype;
        mDataUrl = path;
        init();
    }

    private void init() {
        mMediaPlayer = new IjkMediaPlayer();
        mMediaPlayer.setAudioStreamType(mSteamType);
        mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
        mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
        mMediaPlayer.setOnErrorListener(mOnErrorListener);

        try {
            mMediaPlayer.setDataSource(mDataUrl);
            LogUtil.logd("url=" + mDataUrl);
        } catch (Exception e) {
            LogUtil.loge("url=" + mDataUrl);
            e.printStackTrace();
        }
    }

    // --- inner listener
    private IMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new IMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(IMediaPlayer iMediaPlayer, int percent) {
            if (mBufferingPercent >= percent) {
                return;
            }
            LogUtil.logd(TAG + "onBufferingUpdate, percent=" + percent);
            IjkAudioPlayer.this.onBufferingUpdate(percent);
        }
    };

    private IMediaPlayer.OnPreparedListener mOnPreparedListener = new IMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(IMediaPlayer iMediaPlayer) {
            mIsBuffering = false;
            iMediaPlayer.pause();
            if (mOuterOnPreparedListener != null) {
                LogUtil.logd(TAG + "onPrepared");
                mOuterOnPreparedListener.onPrepared(IjkAudioPlayer.this);
            }
        }
    };

    private IMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new IMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(IMediaPlayer iMediaPlayer) {
            mIsBuffering = false;
            LogUtil.logd(TAG + "onSeekComplete");
            if (mOuterOnSeekCompleteListener != null) {
                mOuterOnSeekCompleteListener.onSeekComplete(IjkAudioPlayer.this, iMediaPlayer.getCurrentPosition());
            }
        }
    };

    private IMediaPlayer.OnCompletionListener mOnCompletionListener = new IMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(IMediaPlayer iMediaPlayer) {
            LogUtil.logd(TAG + "onCompletion");
            if (!isError && mOuterOnCompletionListener != null) {
                mOuterOnCompletionListener.onCompletion(IjkAudioPlayer.this);
            }
            if (mOnPausedCompleteListener != null) {
                mOnPausedCompleteListener.onPausedCompleteListener("");
            }
        }
    };

    private IMediaPlayer.OnErrorListener mOnErrorListener = new IMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(IMediaPlayer iMediaPlayer, int i, int i1) {
            return false;
        }
    };

    @Override
    public int getDuration() {
        try {
            return (int) (mMediaPlayer.getDuration() / 1000);
        } catch (Exception e) {
            if (null != mOnErrorListener) {
                mOnErrorListener.onError(mMediaPlayer, -20, 20);
            }
        }
        return 0;
    }

    @Override
    public float getPlayPercent() {
        float r = 0f;
        try {
            r = mMediaPlayer.getCurrentPosition() * 1.0F
                    / mMediaPlayer.getDuration();
        } catch (Exception e) {
        }
        return r;
    }

    private float getPlayPercentInner() {
        float r = 0f;
        try {
            r = mMediaPlayer.getCurrentPosition() * 1.0F
                    / mMediaPlayer.getDuration();
        } catch (Exception e) {
        }
        return r;
    }

    @Override
    public float getBufferingPercent() {
        float r = getPlayPercentInner();
        float b = getBufferingPercent();
        LogUtil.logd(TAG + "playPercent:" + r + ",bufferPercent:" + b);
        if (r > b) {
            return b;
        }
        return r;
    }

    @Override
    public boolean isPlaying() {
        LogUtil.logd(TAG + "isPlaying");
        if (mIsBuffering) {
            return false;
        }
        try {
            return mMediaPlayer.isPlaying();
        } catch (Exception e) {
            LogUtil.loge(TAG + "[exception]", e);
            // java.lang.Exception
        }
        return false;
    }

    private boolean mIsBuffering = true;
    private boolean mIsForceNeedMoreData = false;

    @Override
    public boolean isBuffering() {
        return mIsBuffering || getBufferingPercent() < getPlayPercentInner();
    }

    @Override
    public boolean needMoreData() {
        LogUtil.logd(TAG + "[decide] needMoreData release?" + mReleased);
        if (mReleased) {
            throw new RuntimeException("player alreay released");
        }
        return mIsForceNeedMoreData;

//        if (mIsBuffering) {
//            return true;
//        }
//        if (mIsForceNeedMoreData) {
//            mIsForceNeedMoreData = false;
//            return true;
//        }
//        if (isPlaying()) {
//            try {
//                if (Constant.ISNEED) {
//                    LogUtil.logd(TAG + "[variable]" + mBufferingPercent + ",duration" + mMediaPlayer.getDuration() + ",currentPosition:" + mMediaPlayer.getCurrentPosition());
//                }
//                return (mBufferingPercent * mMediaPlayer.getDuration() / 100.0)
//                        - mMediaPlayer.getCurrentPosition() < NEED_BUFFER_DATA_TIME;
//            } catch (Exception e) {
//                LogUtil.loge(TAG + "[exception] ", e);
//            }
//        }
//        return false;
    }

    @Override
    public void settDataPieceSize(long dataSize) {
        mdataPiceSize = dataSize;
    }

    @Override
    public long getDataPieceSize() {
//        if (mdataPiceSize != 0) {
//            return mdataPiceSize;
//        }
//        return DEFAULT_DATA_PIECE_SIZE;
        return Long.MAX_VALUE;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setDataSource(String url) {

    }

    @Override
    public void release() {
        LogUtil.logd(TAG + "release");
        super.release();
        AppLogic.removeUiGroundCallback(mRunnableRefreshPlayProgress);
        mMediaPlayer.release();
        mReleased = true;
    }

    @Override
    public String getUrl() {
        return mDataUrl;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mMediaPlayer.setVolume(leftVolume, rightVolume);
    }

    @Override
    public void forceNeedMoreData(boolean isForce) {
        mIsForceNeedMoreData = isForce;
    }

    @Override
    public void prepareAsync() {
        LogUtil.logd(TAG + "prepareAsync");
        mIsBuffering = true;
        try {
            mMediaPlayer.prepareAsync();
            currentState = BUFFERSTATE;
        } catch (Exception e) {
            if (null != mOnErrorListener) {
                mOnErrorListener.onError(mMediaPlayer, 1, 520);//
            }
        }
    }

    @Override
    public void prepareAsyncSub() {
        prepareAsync();
    }

    @Override
    public void start() {
        LogUtil.logd(TAG + "start");
        AppLogic.runOnUiGround(mRunnableRefreshPlayProgress, 0);
        try {
            mMediaPlayer.start();
            if (mOnPlayingListener != null) {
                mOnPlayingListener.onPlayingListener("", 0);
            }
//			mRunnableRefreshPlayProgress.run();
        } catch (Exception e) {
            LogUtil.loge(TAG + e.getMessage());
        }
    }

    @Override
    public void pause() {
        LogUtil.logd(TAG + "pause");
        try {
            mMediaPlayer.pause();

            if (mOnPausedCompleteListener != null) {
                mOnPausedCompleteListener.onPausedCompleteListener("");
            }

            currentState = PAUSESTATE;
        } catch (Exception e) {
            if (mOnErrorListener != null) {
                mOnErrorListener.onError(mMediaPlayer, -20, 20);
            }
        }
    }

    @Override
    public void stop() {
        LogUtil.logd(TAG + "stop");
        if (mMediaPlayer != null) {
            mMediaPlayer.stop();
            currentState = IDELSTATE;
        }
    }

    @Override
    public void seekTo(long position) {
        LogUtil.logd(TAG + "media session[" + mSessionInfo.getLogId() + "] seekTo: " + (position * 1000f / Constant.TIME_UNIT));
        if (mMediaPlayer != null) {
            mIsBuffering = true;
            mMediaPlayer.seekTo((long) (position * 1000f / Constant.TIME_UNIT));
        }
    }

    private OnPreparedListener mOuterOnPreparedListener;

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        mOuterOnPreparedListener = listener;
    }

    private OnCompletionListener mOuterOnCompletionListener;

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOuterOnCompletionListener = listener;
    }

    private OnSeekCompleteListener mOuterOnSeekCompleteListener;

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOuterOnSeekCompleteListener = listener;
    }

    private OnPlayProgressListener mOuterOnPlayProgressListener;
    private float mLastPlayPercent = -1;

    @Override
    public void setOnPlayProgressListener(OnPlayProgressListener listener) {
        mOuterOnPlayProgressListener = listener;
        AppLogic.runOnUiGround(mRunnableRefreshPlayProgress, 0);
    }

    private Runnable mRunnableRefreshPlayProgress = new Runnable() {
        @Override
        public void run() {
            if (mOuterOnPlayProgressListener != null && isPlaying()) {
                float p = getPlayPercent();
                if (p != mLastPlayPercent) {
                    mOuterOnPlayProgressListener.onPlayProgress(IjkAudioPlayer.this, (long) (getPlayPercent() * getDuration()), getDuration());
                    mLastPlayPercent = p;
                } else {
                    LogUtil.logd(TAG + " can't excute :" + p);
                }
            }
            AppLogic.runOnUiGround(this, PLAY_PROGRESS_NOTIFY_INTERVAL);

            if (mOnBufferingUpdateListenerSet != null
                    && mSessionInfo.audio instanceof FileAudio) {
                List<LocalBuffer> lst = new ArrayList<LocalBuffer>();
                lst.add(LocalBuffer.buildFull(100));
                notifyDownloading(lst);
            }
        }
    };
}
