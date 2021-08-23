package com.txznet.txz.util.player;

import android.media.AudioTrack;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

import com.txznet.audio.codec.ITXZAudioDecoder;
import com.txznet.audio.codec.TXZAudioDecoder;
import com.txznet.comm.remote.util.LogUtil;

import java.lang.ref.WeakReference;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.txznet.txz.util.player.TXZAudioPlayer.BUFFERSTATE;

/**
 * Created by brainBear on 2017/9/1.
 */

public class HandlerAudioTrack implements ICodecTrack, AudioTrack.OnPlaybackPositionUpdateListener, ITXZAudioDecoder.ITXZDecoderCallBack {

    private static final String TAG = "AudioDecoderTrack";
    private static final int READ_DECODER_TIMEOUT = 2000;
    private static final int MAX_BUFFER_DEFAULT_SIZE = 8192;
    
    public static final int TIME_UNIT=1;//时间的单位，用于播放进度条等，因为威仕特设备有一些影响。

    private final WeakReference<TXZAudioPlayer> mPlayerReference;
    private final TXZAudioDecoder mAudioDecoder;
    private final ReadWriteLock mRWLock = new ReentrantReadWriteLock();
    private int mStreamType;
    private String mPath;
    private long mSessionId;
    private OnStateListener mOnStateListener;
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

    private Runnable mStartDecodeTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d(TAG, "start decode");
            int retCode = mAudioDecoder.startDecoder(mSessionId, mPath);
            if (retCode < 0) {
                LogUtil.e(TAG, "start decode ret:" + retCode);
                Error error = new Error(Error.SOURCE_DECODE, retCode, "read codec data error", "播放发生异常;" + retCode);
                notifyErrorListener(error);
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

                    Error error = new Error(Error.SOURCE_DECODE, result, "read decode error", "播放发生异常:" + result);
                    notifyErrorListener(error);
                    return;
                }

                if (isChangeAudioParam(mParams[0], mParams[1], mParams[2])) {
                    LogUtil.d(TAG, "old params:" + mChannelConfig + " " + mSampleRateInHz + " " + mAudioFormat);
                    mChannelConfig = mParams[0];
                    mSampleRateInHz = mParams[1];
                    mAudioFormat = mParams[2];
                    LogUtil.d(TAG, "new params:" + mChannelConfig + " " + mSampleRateInHz + " " + mAudioFormat);
                    mBufferSize = getMaxBufferSize(mChannelConfig, mSampleRateInHz, mAudioFormat);
                    mData = new byte[mBufferSize];
                    mOffset = 0;

                    mFirst = true;

                    boolean ret = createAudioTrack(mChannelConfig, mSampleRateInHz, mAudioFormat);
                    if (!ret) {
                        Error error = new Error(Error.SOURCE_DECODE, result, "create audioTrack error", "创建播放器发生异常:" + result);
                        notifyErrorListener(error);
                        return;
                    }
                }

                mReadCount = result;
                if (needFillBuffer()) {
                    mOffset += mReadCount;
                    mReadCount = 0;
                    continue;
                }

                if (mFirst) {
                    notifyOnPreparedListener();
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

    public HandlerAudioTrack(TXZAudioPlayer player, int streamType, String path) {
        this.mStreamType = streamType;
        this.mPath = path;
        mPlayerReference = new WeakReference<TXZAudioPlayer>(player);

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

        int channel = CreateParamFactory.getChannel(channelConfig);
        int format = CreateParamFactory.getAudioFormat(audioFormat);
        int minBufferSize = AudioTrack.getMinBufferSize(sampleRateInHz, channel, format);
        mRWLock.writeLock().lock();
        try {
            mAudioTrack = new AudioTrack(mStreamType, sampleRateInHz, channel, format, minBufferSize, AudioTrack.MODE_STREAM);
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
		if (getDuration() > 5 * TIME_UNIT) {
			return mFirst && mOffset < mBufferSize;
		} else {
			return false;
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
    }

    @Override
    public void stop() {
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.pause();
                mReadHandler.removeCallbacks(mReadDecodeTask);
                mReadHandler.removeCallbacks(mWriteTask);

                notifyIdelListener();
            }
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    private void notifyIdelListener() {
        LogUtil.d(TAG + "state:notify:Idel");
        if (null != mOnStateListener) {
            mOnStateListener.onIdelListener();
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
                mAudioTrack.play();
                mReadHandler.removeCallbacks(mReadDecodeTask);
                mReadHandler.removeCallbacks(mWriteTask);
                mReadHandler.post(mWriteTask);
            }
        } finally {
            mRWLock.readLock().unlock();
        }
    }

    @Override
    public void pause() {
        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.pause();
                mReadHandler.removeCallbacks(mReadDecodeTask);
                mReadHandler.removeCallbacks(mWriteTask);

                notifyPauseListener();
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

    @Override
    public void setOnStateListener(OnStateListener listener) {
        this.mOnStateListener = listener;
    }



    @Override
    public void request() {
        LogUtil.d(TAG, "request");
        notifyBufferedListener();
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

    private void notifyBufferedListener() {
        LogUtil.d(TAG + "state:notify:BufferStart");
        if (mPlayerReference.get() != null && mPlayerReference.get().getCurrentState() != BUFFERSTATE) {
            if (null != mOnStateListener) {
                mOnStateListener.onBufferingStateListener();
            }
        }
    }

    private void notifyErrorListener(Error error) {
        LogUtil.d(TAG + "state:notify:ErrorStart");
        // 抛错误码
        if (null != mOnStateListener) {
            mOnStateListener.onErrorListener(error);
        }

    }

    private void notifyCompleteListener() {
        LogUtil.d(TAG + "state:notify:CompleteStart");
        if (null != mOnStateListener) {
            mOnStateListener.onCompletionListener();
        }

    }

    private void notifyOnPreparedListener() {
        LogUtil.d(TAG + "state:notify:onPrepared");
        if (null != mOnStateListener) {
            mOnStateListener.onPreparedListener();
        }

    }


    private void notifyPauseListener() {
        LogUtil.d(TAG + "state:notify:Pause");
        if (null != mOnStateListener) {
            mOnStateListener.onPauseStateListener();
        }

    }

    private void notifyPlayListener() {
        if (mPlayerReference.get() != null &&
                mPlayerReference.get().getCurrentState() != TXZAudioPlayer.PLAYSTATE) {
            LogUtil.d(TAG + "state:notify:Play");
            if (mOnStateListener != null) {
                mOnStateListener.onPlayStateListener();
            }
        }
    }

    private void notifyReleaseListener() {
        LogUtil.d(TAG + "state:notify:Release");
        if (null != mOnStateListener) {
            mOnStateListener.onIdelListener();
        }
    }

    private void notifyProgressListener(final long testTime) {
        if (null != mOnStateListener) {
            mOnStateListener.onPlayProgressListener(testTime, getDuration());
        }
    }

    private void notifySeekCompleteListener(long seektime) {
        if (null != mOnStateListener) {
            mOnStateListener.onSeekCompleteListener(seektime);
        }
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
        mDuration = duration;
    }

    @Override
    public void onSeekCompleteListener(long seekTime) {
        LogUtil.d(TAG, "seek complete");
        mInSeek = false;

        mRWLock.readLock().lock();
        try {
            if (null != mAudioTrack) {
                mAudioTrack.setPositionNotificationPeriod(mSampleRateInHz);
            }
        } finally {
            mRWLock.readLock().unlock();
        }

        if (seekTime > TIME_UNIT) {
            mSeekTime = seekTime + TIME_UNIT;
        } else {
            mSeekTime += TIME_UNIT;
        }
        notifySeekCompleteListener(mSeekTime);
    }
}
