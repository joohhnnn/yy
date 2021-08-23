package com.txznet.audio.player;

import android.media.AudioManager;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.bean.SessionInfo;
import com.txznet.audio.player.ICodecTrack.State;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.baseModule.bean.Error;

import java.util.List;

public class FFMPEGAudioPlayer extends TXZAudioPlayer {

    public static final int DEFAULT_DATA_PIECE_SIZE = 6 * PREPARE_BUFFER_DATA_TIME; // 每毫秒大概6个字节//预加载PREPARE_BUFFER_DATA_TIME这么多时间的数据

    ICodecTrack mCodecTrack = null;
    private OnPreparedListener mOnPreparedListener;
    private OnCompletionListener mOnCompletionListener;
    private OnSeekCompleteListener mOnSeekCompleteListener;
    private OnPlayProgressListener mOnPlayProgressListener;
    private OnBufferingUpdateListener mOnBufferingUpdateListener;
    //	private OnErrorListener mOnErrorListener;
    State state;
    float mPercent = 0;


    private boolean mReleased = false;
    private long mdataPiceSize;
    private String mUrl = "";

    @Override
    public void settDataPieceSize(long dataSize) {
        mdataPiceSize = dataSize;
    }

    public FFMPEGAudioPlayer(SessionInfo sess, int streamtype, final String path) {
        super(sess, streamtype);
        mUrl = path;
//        mCodecTrack = new AudioFFMpegCodecTrack(this, streamtype, path);
//        mCodecTrack = new AudioDecoderTrack(this, streamtype, path);
        mCodecTrack = new HandlerAudioTrack(this, streamtype, path);
        mCodecTrack.setOnStateListener(new OnStateListener() {

            @Override
            public void onPreparedListener() {
                LogUtil.logd(TAG + "FFMpegAudioPlayer:preparedListener");
                currentState = PREPAREDSTATE;
                if (mOnPreparedListener != null)
                    mOnPreparedListener.onPrepared(FFMPEGAudioPlayer.this);
            }

            @Override
            public void onCompletionListener() {
                if (mOnCompletionListener != null)
                    mOnCompletionListener.onCompletion(FFMPEGAudioPlayer.this);
            }

            @Override
            public void onSeekCompleteListener(long seekTime) {
                if (mOnSeekCompleteListener != null)
                    mOnSeekCompleteListener.onSeekComplete(FFMPEGAudioPlayer.this, seekTime);
            }

            @Override
            public void onPlayProgressListener(long position, long duration) {
                if (mOnPlayProgressListener != null) {
                    mPercent = position * 1.0f / duration;
                    mOnPlayProgressListener.onPlayProgress(FFMPEGAudioPlayer.this, position, duration);
                }
            }

            @Override
            public void onBufferingUpdateListener(List<LocalBuffer> buffers) {
                if (mOnBufferingUpdateListener != null)
                    mOnBufferingUpdateListener.onDownloading(FFMPEGAudioPlayer.this, buffers);
            }

            @Override
            public void onErrorListener(Error error) {
                currentState = ERRORSTATE;
                if (mOnErrorListenerSet != null) {
                    mOnErrorListenerSet.onError(FFMPEGAudioPlayer.this, error);
                } else {
                    LogUtil.logd(TAG + " don't set error callback");
                }
            }

            @Override
            public void onPlayStateListener() {
                currentState = PLAYSTATE;
                if (mOnPlayingListener != null) {
                    mOnPlayingListener.onPlayingListener(path, 0);
                }
            }

            @Override
            public void onPauseStateListener() {
                currentState = PAUSESTATE;
                if (mOnPausedCompleteListener != null) {
                    mOnPausedCompleteListener.onPausedCompleteListener(path);
                }
            }

            @Override
            public void onBufferingStateListener() {
                currentState = BUFFERSTATE;
                if (mOnPausedCompleteListener != null) {
                    mOnBufferingStatusListener.onBufferingStart("");
                }
            }

            @Override
            public void onBufferingEndStateListener() {

                if (mOnPausedCompleteListener != null) {
                    mOnBufferingStatusListener.onBufferingEnd();
                }
            }

            @Override
            public void onIdelListener() {
                currentState = IDELSTATE;
                if (mOnStoppedListener != null) {
                    mOnStoppedListener.onStoppedListener("");
                }
            }
        });

    }

    @Override
    public int getDuration() {
        if (mCodecTrack != null) {
            return (int) mCodecTrack.getDuration();
        }
        return 0;
    }

    @Override
    public float getPlayPercent() {
        return mPercent;
    }

    @Override
    public float getBufferingPercent() {
        return 0;
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public boolean isBuffering() {
        return false;
    }

    @Override
    public boolean needMoreData() {
        if (mReleased) {
            throw new RuntimeException("player alreay released");
        }
        return mIsForceNeedMoreData;
//        if (mIsForceNeedMoreData) {
//            mIsForceNeedMoreData = false;
//            return true;
//        }
//        return false;
    }

    @Override
    public long getDataPieceSize() {
        if (mdataPiceSize != 0) {
            return mdataPiceSize;
        }

//        if (getDuration() != 0) {
//            return getDuration() * -1;
//        }
        return DEFAULT_DATA_PIECE_SIZE;
    }

    @Override
    public void reset() {

    }

    @Override
    public void setDataSource(String url) {

    }

    @Override
    public String getUrl() {
        return mUrl;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mCodecTrack.setStereoVolume(leftVolume, rightVolume);
    }

    @Override
    public void prepareAsync() {
        // TODO 缓冲一定的数据量
        // 开始网络请求：
        mCodecTrack.request();

    }

    @Override
    public void prepareAsyncSub() {
        prepareAsync();
    }

    @Override
    public void start() {
        // if (DirectAudioPlayer.this.state != State.played) {
//        LogUtil.logd(TAG + "start::error::" + currentFocusInt);
        mCodecTrack.play();

        // }
    }

    @Override
    public void pause() {
        if (mCodecTrack != null) {
            mCodecTrack.pause();
        }
    }

    @Override
    public void stop() {
//        abandonAudioFocus();
        if (mCodecTrack != null) {
            mCodecTrack.stop();
        }
    }

    @Override
    public void release() {
        mReleased = true;
        mCodecTrack.release();
//        mCodecTrack.setOnStateListener(null);
        mOnPreparedListener = null;
        mOnCompletionListener = null;
        mOnSeekCompleteListener = null;
        mOnPlayProgressListener = null;
        mOnBufferingUpdateListener = null;
        mCodecTrack = null;
        super.release();
    }

    @Override
    public void seekTo(long time) {
        if (getDuration() > 0) {
            long seekPosition = (long) (time * 1.0f / getDuration() * mSessionInfo.len);
            LogUtil.loge(TAG + "seek to" + time + "/" + getDuration() + "/" + mSessionInfo.len);
            mCodecTrack.seek(time, seekPosition);
        } else {
            LogUtil.loge(TAG + "seek occur error " + time + "/" + getDuration());
        }

    }

    @Override
    public void setOnPreparedListener(OnPreparedListener listener) {
        this.mOnPreparedListener = listener;
    }

    @Override
    public void setOnCompletionListener(OnCompletionListener listener) {
        mOnCompletionListener = listener;
    }

    @Override
    public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
        mOnSeekCompleteListener = listener;

    }

    @Override
    public void setOnPlayProgressListener(OnPlayProgressListener listener) {
        mOnPlayProgressListener = listener;
    }

    //
    // @Override
    // public void setOnBufferingUpdateListener(OnBufferingUpdateListener
    // listener) {
    // mOnBufferingUpdateListener = listener;
    // }
    //
    // @Override
    // public void setOnErrorListener(OnErrorListener listener) {
    // mOnErrorListener = listener;
    // }

    boolean mIsForceNeedMoreData = false;

    @Override
    public void forceNeedMoreData(boolean isForce) {
        // TODO:强制拉取更多。
        mIsForceNeedMoreData = isForce;
    }
}
