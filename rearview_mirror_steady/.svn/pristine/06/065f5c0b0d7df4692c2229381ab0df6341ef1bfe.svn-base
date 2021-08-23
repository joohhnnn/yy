package com.txznet.audio.player.core.ffmpeg;

import android.media.AudioManager;

import com.txznet.audio.ErrCode;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.OnPlayerStateChangeListener;
import com.txznet.audio.player.util.HttpUtil;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.util.LogUtil;

import java.io.File;
import java.io.IOException;

/**
 * IMediaPlayer基于FFMPEG解码的实现
 */
public class FFMPEGMediaPlayer implements IMediaPlayer {
    private static final String TAG = "FFMPEGMediaPlayer";
    private ICodecTrack mCodecTrack;
    private String mPath;
    private int mStreamType = AudioManager.STREAM_MUSIC;
    private float mLeftVolume, mRightVolume;
    private boolean isPlaying;
    private @PlayState
    int mCurrPlayState = STATE_ON_IDLE;
    private long mResLen;

    private OnPlayerStateChangeListener mInnerOnPlayChangeListener = new OnPlayerStateChangeListener() {
        @Override
        public void onPlayStateChanged(int state) {
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onPlayStateChanged(state);
            }
        }

        @Override
        public void onProgressChanged(long position, long duration) {
            if (STATE_ON_STOPPED != mCurrPlayState && mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onProgressChanged(position, duration);
            }
        }

        @Override
        public void onSeekComplete() {
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onSeekComplete();
            }
        }

        @Override
        public void onCompletion() {
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onCompletion();
            }
            notifyPlayStateChange(STATE_ON_STOPPED);
            if (mCodecTrack != null) {
                mCodecTrack.setOnPlayStateChangeListener(null);
                mCodecTrack.release();
                initCodecTrack();
            }
        }

        @Override
        public void onError(Error error) {
            if (mOnPlayStateChangeListener != null) {
                mOnPlayStateChangeListener.onError(error);
            }
        }
    };

    public FFMPEGMediaPlayer() {
    }

    @Override
    public void setDataSource(String path) {
        mPath = path;
        initCodecTrack();
        notifyPlayStateChange(STATE_ON_INITIALIZED);
    }

    private void initCodecTrack() {
        mCodecTrack = new HandlerAudioTrack(mStreamType, mPath);
        mCodecTrack.setStereoVolume(mLeftVolume, mRightVolume);
        mCodecTrack.setOnPlayStateChangeListener(mInnerOnPlayChangeListener);
    }

    @Override
    public String getDataSource() {
        return mPath;
    }

    @Override
    public void prepareAsync() {
        if (mCodecTrack == null || mPath == null) {
            return;
        }
        mCodecTrack.request();
        notifyPlayStateChange(STATE_ON_PREPARING);
        new Thread() {
            @Override
            public void run() {
                if (mPath == null) {
                    return;
                }
                final String _path = mPath;
                mResLen = getResLen(_path);
                if (!_path.equals(mPath)) {
                    return;
                }
                if (mResLen == 0) {
                    if (mOnPlayStateChangeListener != null) {
                        mOnPlayStateChangeListener.onError(new Error(ErrCode.ERROR_MEDIA_NOT_FOUND, "dataSource len get failed", "资源访问失败"));
                    }
                    notifyPlayStateChange(STATE_ON_ERROR);
                }
            }
        }.start();
    }

    @Override
    public void start() {
        if (mCodecTrack != null) {
            mCodecTrack.play();
            isPlaying = true;
        }
    }

    @Override
    public void stop() {
        if (mCodecTrack != null) {
//            mCodecTrack.stop(); // 这个stop跟pause是一样的
            mCodecTrack.setOnPlayStateChangeListener(null);
            mCodecTrack.release();
            initCodecTrack();
            notifyPlayStateChange(STATE_ON_STOPPED);
            isPlaying = false;
        }
    }

    @Override
    public void pause() {
        if (mCodecTrack != null) {
            mCodecTrack.pause();
            isPlaying = false;
        }
    }

    @Override
    public boolean isPlaying() {
        return isPlaying;
    }

    @Override
    public void seekTo(long msec) {
        if (getDuration() > 0) {
            long seekPosition = (long) (msec * 1.0f / getDuration() * mResLen);
            LogUtil.e(TAG, " seek to " + msec + "/" + getDuration() + "/" + mResLen);
            mCodecTrack.seek(msec, seekPosition);
        } else {
            LogUtil.e(TAG, " seek occur error " + msec + "/" + getDuration());
        }
    }

    @Override
    public long getCurrentPosition() {
        if (mCodecTrack != null) {
            return mCodecTrack.getPosition();
        }
        return 0;
    }

    @Override
    public long getDuration() {
        if (mCodecTrack != null) {
            return mCodecTrack.getDuration();
        }
        return 0;
    }

    @Override
    public void release() {
        if (mCodecTrack != null) {
            mCodecTrack.setOnPlayStateChangeListener(null);
            mCodecTrack.release();
        }
        mCodecTrack = null;
        mPath = null;
        notifyPlayStateChange(STATE_ON_END);
        mOnPlayStateChangeListener = null;
        isPlaying = false;
    }

    @Override
    public void reset() {
        if (mCodecTrack != null) {
            mCodecTrack.setOnPlayStateChangeListener(null);
            mCodecTrack.release();
        }
        mCodecTrack = null;
        mPath = null;
        notifyPlayStateChange(STATE_ON_IDLE);
        isPlaying = false;
    }

    @Override
    public void setVolume(float leftVolume, float rightVolume) {
        mLeftVolume = leftVolume;
        mRightVolume = rightVolume;
        if (mCodecTrack != null) {
            mCodecTrack.setStereoVolume(leftVolume, rightVolume);
        }
    }

    @Override
    public void setAudioStreamType(int streamType) {
        mStreamType = streamType;
    }

    @Override
    public int getPlayState() {
        return mCurrPlayState;
    }

    private OnPlayerStateChangeListener mOnPlayStateChangeListener;

    @Override
    public void setOnPlayStateChangeListener(OnPlayerStateChangeListener listener) {
        mOnPlayStateChangeListener = listener;
    }

    private long getResLen(String url) {
        if (url == null) {
            return -1;
        }
        if (url.startsWith("http")) {
            try {
                return HttpUtil.getFileLength(url);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            File file = new File(url);
            if (file.exists()) {
                return file.length();
            }
        }
        return -1;
    }

    // 通知
    private void notifyPlayStateChange(@PlayState int state) {
        mCurrPlayState = state;
        if (mOnPlayStateChangeListener != null) {
            mOnPlayStateChangeListener.onPlayStateChanged(state);
        }
    }
}
