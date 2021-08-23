package com.txznet.audio.player.core.ffmpeg;

import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.txznet.audio.ErrCode;
import com.txznet.audio.codec.ITXZAudioDecoder;
import com.txznet.audio.codec.TXZAudioDecoder;
import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.OnPlayerStateChangeListener;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.err.Error;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.NetworkUtil;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;


/**
 * Created by brainBear on 2017/9/1.
 * <p>
 * 注意AudioTrack 的两种状态：
 * AudioTrack.getPlayState()
 * AudioTrack.getState()
 * <p>
 * 2018/10/16 zackzhou
 * 解耦
 */
public class HandlerAudioTrack implements ICodecTrack, AudioTrack.OnPlaybackPositionUpdateListener, ITXZAudioDecoder.ITXZDecoderCallBack {

    public static int TIME_UNIT = 1000;//时间的单位，用于播放进度条等，因为威仕特设备有一些影响。

    private static final String TAG = "AudioDecoderTrack";
    private static final int READ_DECODER_TIMEOUT = 2000;
    private static final int MAX_BUFFER_DEFAULT_SIZE = 8192;

    private final TXZAudioDecoder mAudioDecoder;
    private final ReadWriteLock mRWLock = new ReentrantReadWriteLock();
    private int mStreamType;
    private String mPath;
    private long mSessionId;
    private HandlerThread mDecoderThread;
    private HandlerThread mReadThread;
    private Handler mDecoderHandler;
    private Handler mReadHandler;
    private Handler mMainHandler;
    private int[] mParams = new int[3];
    private byte[] mData = new byte[0];
    private int mOffset = 0;
    private int mChannelConfig = 0;// 通道数 1/2
    private int mSampleRateInHz = 0;// 采样率 44.1k/8K/
    private int mAudioFormat = 0;// 采样位数8/16(1/2字节)
    private int mBufferSize = 0;
    private boolean mFirst = true;
    private int mReadCount;
    private int mWriteCount;
    private int mRemainSize;
    private AudioTrack mAudioTrack;
    private float mLeftVolume = 1.0f;
    private float mRightVolume = 1.0f;
    private long mDuration = 0;//单位是Constant.TimeUnit
    private boolean mInSeek = false;
    private long mSeekTime;
    private boolean mIsPrepared = false;//是否准备好,即已经回调prepared了

    // 开始解码
    private Runnable mStartDecodeTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "start decode");
            int retCode = mAudioDecoder.startDecoder(mSessionId, mPath);
            if (retCode < 0) {
                LogUtil.e(TAG, "start decode ret:" + retCode);
                Error error = new Error(ErrCode.SOURCE_PLAYER, retCode, "read codec data error", "播放发生异常;" + retCode);
                notifyErrorListener(error, true);
            }
            LogUtil.d(TAG, "end decode");
        }

        @Override
        public String toString() {
            return "start_decode";
        }
    };
    private Runnable mReadDecoderTimeoutTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "read decoder timeout");
            if (!NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                Error error = new Error(ErrCode.ERROR_NET_OFFLINE, "not connect to net:", "网络错误");
                notifyErrorListener(error, false);
            }
            notifyBufferedListener();
        }
    };
    private Runnable mReadDecodeTask = new Runnable() {
        @Override
        public void run() {
            while (true) {
                mMainHandler.postDelayed(mReadDecoderTimeoutTask, READ_DECODER_TIMEOUT);
                int result = mAudioDecoder.readDecoder(mSessionId, mParams, mData, mOffset);
                mMainHandler.removeCallbacks(mReadDecoderTimeoutTask);
                if (result < 0) {
                    LogUtil.e(TAG, "read decode error:" + result);
                    if (result == -13) {
                        notifyCompleteListener();
                        return;
                    }
                    Error error = new Error(ErrCode.SOURCE_PLAYER, result, "read decode error", "播放发生异常:" + result);
                    notifyErrorListener(error, true);
                    return;
                }

                if (isChangeAudioParam(mParams[0], mParams[1], mParams[2])) {
                    LogUtil.d(TAG, "old params:" + mChannelConfig + " " + mSampleRateInHz + " " + mAudioFormat);
                    mChannelConfig = mParams[0];
                    mSampleRateInHz = mParams[1];
                    mAudioFormat = mParams[2];
                    mBufferSize = getMaxBufferSize(mChannelConfig, mSampleRateInHz, mAudioFormat);
                    LogUtil.d(TAG, "new params:" + mChannelConfig + " " + mSampleRateInHz + " " + mAudioFormat + " , size:" + mBufferSize);
                    mData = new byte[mBufferSize];
                    mOffset = 0;

                    mFirst = true;

                    int retryCount = 0;
                    while (!createAudioTrack(mChannelConfig, mSampleRateInHz, mAudioFormat)) {
                        if (mAudioTrack != null) {
                            mAudioTrack.release();
                        }
                        mAudioTrack = null;
                        if (retryCount++ > 5) {
                            Error error = new Error(ErrCode.SOURCE_PLAYER, ErrCode.ERROR_AUDIO_TRACK_ERROR);
                            notifyErrorListener(error, true);
                            break;
                        }
                    }
                }
                mReadCount = result;
                if (needFillBuffer()) {
                    mOffset += mReadCount;
                    mReadCount = 0;
                    continue;
                }
                if (mFirst) {
                    notifyOnPreparedListenerMayNot();
                }
                mFirst = false;
                byteAligning();
                mReadHandler.post(mWriteTask);
                return;
            }
        }

        @Override
        public String toString() {
            return "read_decode";
        }
    };

    private Runnable mWriteTask = new Runnable() {
        @Override
        public void run() {
            if (mInSeek) {
                mOffset = 0;
                readDecode();
                return;
            }
            if (mWriteCount < mReadCount) {
                int size = mReadCount - mWriteCount > MAX_BUFFER_DEFAULT_SIZE ? MAX_BUFFER_DEFAULT_SIZE : mReadCount - mWriteCount;
                mRWLock.readLock().lock();
                try {
                    if (null != mAudioTrack) {
                        int writeResult = mAudioTrack.write(mData, mWriteCount, size);

                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.write(mData, mWriteCount, size);
                            } catch (IOException e) {
                            }
                        }

                        if (writeResult == 0) {
                            mReadHandler.postDelayed(mWriteTask, 100);
                            return;
                        } else if (writeResult < 0) {
                            LogUtil.e(TAG, "write error " + writeResult + " data size:" + mData.length + " offset:" + mWriteCount + " size:" + size);
                            return;
                        }

                        mWriteCount += writeResult;
                        mReadHandler.post(mWriteTask);

                        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
                            notifyPlayListener();
                        }
                    }
                } finally {
                    mRWLock.readLock().unlock();
                }

            } else {
                residualByteFill();

                mReadCount = 0;
                mWriteCount = 0;

                readDecode();
            }
        }

        @Override
        public String toString() {
            return "write_task";
        }
    };
    private Runnable mSeekTask = new Runnable() {
        @Override
        public void run() {
            mReadHandler.removeCallbacks(mReadDecodeTask);
            mReadHandler.removeCallbacks(mWriteTask);

            mInSeek = true;

            mReadHandler.post(mWriteTask);
        }
    };

    public HandlerAudioTrack(int streamType, final String path) {
        mStreamType = streamType;
        mPath = path;
        mAudioDecoder = new TXZAudioDecoder();
        long sessionId = mAudioDecoder.createDecoder(this);
        if (sessionId == 0) {
            LogUtil.e(TAG, "create decoder error:" + sessionId);
            return;
        }
        this.mSessionId = sessionId;
        createHandler();
    }

    private void createHandler() {
        mDecoderThread = new HandlerThread("Decoder");
        mDecoderThread.start();
        mDecoderHandler = new Handler(mDecoderThread.getLooper());

        mReadThread = new HandlerThread("Read");
        mReadThread.start();
        mReadHandler = new Handler(mReadThread.getLooper());

        mMainHandler = new Handler(Looper.getMainLooper());
    }

    private void residualByteFill() {
        for (int i = 0; i < mRemainSize; ++i) {
            mData[i] = mData[mReadCount + i];
        }
        mOffset = mRemainSize;
    }

    private boolean createAudioTrack(int channelConfig, int sampleRateInHz, int audioFormat) {
        if (0 == channelConfig || 0 == sampleRateInHz || 0 == audioFormat) {
            return false;
        }

        int channel = AudioFormatUtil.getChannel(channelConfig);
        int format = AudioFormatUtil.getAudioFormat(audioFormat);
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channel, format);
        if (AudioTrack.ERROR_BAD_VALUE == minBufferSize || AudioTrack.ERROR == minBufferSize) {
            return false;
        }
        mRWLock.writeLock().lock();
        try {
            mAudioTrack = new AudioTrack(mStreamType, sampleRateInHz, channel, format, minBufferSize, AudioTrack.MODE_STREAM);
            if (mAudioTrack.getState() != AudioTrack.STATE_INITIALIZED) {
                //TXZ-12787
                CrashCommonHandler.dumpExceptionToSDCard(Thread.currentThread(), new RuntimeException("audioTrack init is error by telenewbie :" + mAudioTrack.getState() + "/" + channelConfig + "/" + sampleRateInHz + "/" + audioFormat + "/" + minBufferSize));
                return false;
            }

            mAudioTrack.setPlaybackPositionUpdateListener(this);
            mAudioTrack.setPositionNotificationPeriod(sampleRateInHz);
        } finally {
            mRWLock.writeLock().unlock();
        }
        setStereoVolume(mLeftVolume, mRightVolume);

        return true;
    }

    private void byteAligning() {
        mReadCount += mOffset;
        mRemainSize = mReadCount % (mChannelConfig * mAudioFormat);
        mRemainSize -= mRemainSize;
    }

    private boolean needFillBuffer() {
        if (getDuration() > 10 * TIME_UNIT) {
            return mFirst && mOffset < mBufferSize;
        } else {
            return false;//少于10s则不需要预填满缓冲区
        }
    }

    private boolean isChangeAudioParam(int channelConfig, int sampleRateInHz, int audioFormat) {
        return this.mSampleRateInHz != sampleRateInHz || this.mChannelConfig != channelConfig || this.mAudioFormat != audioFormat;
    }


    private int getMaxBufferSize(int channelConfig, int sampleRateInHz, int audioFormat) {
        int max = sampleRateInHz * audioFormat * channelConfig * 3;
        return max > MAX_BUFFER_DEFAULT_SIZE ? max : MAX_BUFFER_DEFAULT_SIZE;
    }

    @Override
    public void onMarkerReached(AudioTrack track) {

    }

    @Override
    public void onPeriodicNotification(AudioTrack track) {
        if (track.getPlayState() != AudioTrack.PLAYSTATE_PLAYING) {
            return;
        }
        notifyPlayListener();
        if (mSampleRateInHz > 0) {
            long time = mSeekTime + track.getPlaybackHeadPosition() / mSampleRateInHz * TIME_UNIT;
            notifyProgressListener(time);
        }
    }

    @Override
    public void onGetDuration(long duration) {
        mDuration = duration * TIME_UNIT;
    }

    @Override
    public void onSeekCompleteListener(long seekTime) {
        LogUtil.d(TAG, "seek complete:" + seekTime);
        mInSeek = false;

        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.setPositionNotificationPeriod(mSampleRateInHz);
            }
        } finally {
            mRWLock.readLock().unlock();
        }
        seekTime = seekTime * TIME_UNIT;
        if (seekTime == 0) {
            mSeekTime = 0;
        } else if (seekTime > TIME_UNIT) {
            mSeekTime = seekTime + TIME_UNIT;
        } else {
            mSeekTime += TIME_UNIT;
        }
        notifySeekCompleteListener(mSeekTime);
    }

    @Override
    public void release() {
        if (mSessionId != 0) {
            mAudioDecoder.stopDecoder(mSessionId);

            mDecoderHandler.post(new Runnable() {
                @Override
                public void run() {
                    mAudioDecoder.destoryDecoder(mSessionId);
                    mSessionId = 0;

                    mDecoderThread.quit();
                }
            });
        }

        mReadHandler.postAtFrontOfQueue(new Runnable() {
            @Override
            public void run() {
                mRWLock.writeLock().lock();
                try {
                    if (null != mAudioTrack) {
                        mAudioTrack.flush();
                        mAudioTrack.release();
                        mAudioTrack = null;

                        mReadHandler.removeCallbacksAndMessages(null);
                        notifyReleaseListener();
                    }
                } finally {
                    mRWLock.writeLock().unlock();
                }
                mReadThread.quit();
            }
        });
        // FIXME
//        if (mPlayerReference.get() != null && mPlayerReference.get().mSessionInfo != null) {
//            SessionManager.getInstance().removeSessionInfo(mPlayerReference.get().mSessionInfo.hashCode());
//        } else {
//            if (mPlayerReference.get() == null) {
//                LogUtil.d(TAG + "oom:null");
//            } else if (mPlayerReference.get().mSessionInfo == null) {
//                LogUtil.d(TAG + "oom:sessionInfo:null");
//            } else {
//                LogUtil.d(TAG + "oom:" + mPlayerReference.get().mSessionInfo.getLogId());
//            }
//        }
    }

    @Override
    public void stop() {
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.pause();
                mReadHandler.removeCallbacks(mReadDecodeTask);
                mReadHandler.removeCallbacks(mWriteTask);

                notifyPlayStateChanged(IMediaPlayer.STATE_ON_STOPPED);
            }
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    @Override
    public boolean isComplete() {
        return false;
    }

    @Override
    public int write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
        return 0;
    }

    @Override
    public void flush(Runnable runAfterFlush) {

    }

    @Override
    public boolean isPlaying() {
        boolean result = false;
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                result = mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
            }
        } finally {
            mRWLock.readLock().unlock();
        }
        return result;
    }

    @Override
    public void setStereoVolume(float leftVolume, float rightVolume) {
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.setStereoVolume(leftVolume, rightVolume);
            }
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    @Override
    public void play() {
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                if (!mIsPrepared) {
                    notifyOnPreparedListener();
                    return;
                }
                mAudioTrack.play();
//                voiceTransition(mAudioTrack, 0, 1000, 500, null);
                mReadHandler.removeCallbacks(mReadDecodeTask);
                mReadHandler.removeCallbacks(mWriteTask);
                mReadHandler.post(mWriteTask);
            } else {
                //可能是由于AudioTrack在创建的时候没有创建成功
                LogUtil.d(TAG, "AudioTrack is not prepared ,maybe AudioTrack init failed");
            }
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    @Override
    public void pause() {
        LogUtil.d("qijian", "you will pause this music!");
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                //voiceTransition(mAudioTrack, 1000, 0, 500, new Runnable() {
                //    @Override
                //    public void run() {
                if (mAudioTrack != null && mAudioTrack.getPlayState() != AudioTrack.PLAYSTATE_STOPPED && mAudioTrack.getState() != AudioTrack.STATE_UNINITIALIZED) {
                    mAudioTrack.pause();
                }

                notifyPauseListener();
                mReadHandler.removeCallbacks(mReadDecodeTask);
                mReadHandler.removeCallbacks(mWriteTask);
                //     }
                // });
            }
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    @Override
    public void seek(long time, long position) {
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.pause();
                mAudioTrack.flush();
            }
        } finally {
            mRWLock.readLock().unlock();
        }
        notifyBufferedListener();
        int seekResult = mAudioDecoder.seekDecoder(mSessionId, time / TIME_UNIT, position);
        LogUtil.d(TAG, "seek result " + seekResult);
        mReadHandler.postAtFrontOfQueue(mSeekTask);
    }

    @Override
    public long getDuration() {
        return mDuration;
    }

    private long mPosition;

    @Override
    public long getPosition() {
        return mPosition;
    }

    private OnPlayerStateChangeListener mOnPlayStateChangeListener;

    @Override
    public void setOnPlayStateChangeListener(OnPlayerStateChangeListener listener) {
        mOnPlayStateChangeListener = listener;
    }

    FileOutputStream fileOutputStream = null;

    @Override
    public void request() {
        LogUtil.d(TAG, "request");
        mIsPrepared = false;
        mFirst = true;
        // FIXME
//        notifyBufferedListener();
        startDecode();
        readDecode();
    }

    private void startDecode() {
        mDecoderHandler.post(mStartDecodeTask);
    }

    private void readDecode() {
        mReadHandler.removeCallbacks(mReadDecodeTask);
        mReadHandler.post(mReadDecodeTask);
    }

    private void notifyOnPreparedListener() {
        notifyPlayStateChanged(IMediaPlayer.STATE_ON_PREPARED);
    }

    private void notifyOnPreparedListenerMayNot() {
        notifyOnPreparedListener();
    }

    private void notifyPauseListener() {
        notifyPlayStateChanged(IMediaPlayer.STATE_ON_PAUSED);
    }

    private void notifyPlayListener() {
        notifyPlayStateChanged(IMediaPlayer.STATE_ON_PLAYING);
    }

    private void notifyReleaseListener() {
        notifyPlayStateChanged(IMediaPlayer.STATE_ON_END);
    }

    private void notifyBufferedListener() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mIsPrepared && mOnPlayStateChangeListener != null) {
                    notifyPlayStateChanged(IMediaPlayer.STATE_ON_BUFFERING);
                }
            }
        });
    }

    private void notifyErrorListener(final Error error, final boolean isDead) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onError(error);
                    if (isDead) {
                        notifyPlayStateChanged(IMediaPlayer.STATE_ON_ERROR);
                        release();
                    }
                }
            }
        });
    }

    private void notifyCompleteListener() {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onCompletion();
                }
            }
        });
    }

    private @IMediaPlayer.PlayState
    int mLastSyncState;

    private void notifyPlayStateChanged(@IMediaPlayer.PlayState final int state) {
        if (IMediaPlayer.STATE_ON_PREPARED == state) {
            mIsPrepared = true;
        }
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnPlayStateChangeListener != null && mLastSyncState != state) {
                    mOnPlayStateChangeListener.onPlayStateChanged(state);
                    mLastSyncState = state;
                }
            }
        });
    }

    private void notifyProgressListener(final long pos) {
        mPosition = pos;
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onProgressChanged(mPosition, getDuration());
                }
            }
        });
    }

    private void notifySeekCompleteListener(long seektime) {
        mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                if (mOnPlayStateChangeListener != null) {
                    mOnPlayStateChangeListener.onSeekComplete();
                }
            }
        });
    }
}
