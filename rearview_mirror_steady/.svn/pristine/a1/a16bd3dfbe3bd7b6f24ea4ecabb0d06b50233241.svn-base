package com.txznet.audio.codec;

import java.io.FileOutputStream;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import android.media.AudioFormat;
import android.media.AudioTrack;
import android.text.TextUtils;

import com.txznet.audio.player.TXZAudioPlayer;

public class TXZAudioDecoder {
	private final static int DECODE_BUFFER_SIZE = 1000; // 预解码数据大小1000ms

	static {
		System.loadLibrary("TXZAudio");
	}

	// 构造解码器
	private native static Object nativeCreateDecoder(String url, byte[] data,
			long offset, long len, long total);

	// 解码
	private native int nativeDecode(long nativeCodec, byte[] data, long offset,
			long len);

	// 取消解码
	private native void nativeCancel(long nativeCodec);

	// 清理解码状态
	private native void nativeFlush(long nativeCodec);

	// 读取解码数据
	private native void nativeRead(long nativeCodec);

	// 释放
	private native void nativeRelease(long nativeCodec);

	// //////////////////////////////////////////////////////////////////////////////

	/**
	 * 通过url和透数据创建解码器
	 * 
	 * @param url
	 * @param data
	 * @param offset
	 * @param len
	 * @return
	 */
	public static TXZAudioDecoder createDecoder(String url, byte[] data,
			int offset, long len, long total) {
		return (TXZAudioDecoder) nativeCreateDecoder(url, data, offset, len,
				total);
	}

	// //////////////////////////////////////////////////////////////////////////////
	private long nativeCodec = 0;

	private TXZAudioDecoder(int sampleRate, int channels, int format) {
		mSampleRate = sampleRate;
		mChannel = channels;
		mAudioFormat = format;

		mLastDecodeData = new byte[(mSampleRate
				* TXZAudioPlayer.PLAY_PROGRESS_NOTIFY_INTERVAL / 1000)
				* mChannel * (mAudioFormat / 8)];
	}

	protected int mHeadSize = 0;// 文件头长度
	protected int mDurnation = 0; // 持续时间，单位ms
	protected int mChannel; // 通道数
	protected int mSampleRate; // 采样率:8K/16K/44.1K
	protected int mAudioFormat; // 采样位数:8/16
	protected byte[] mLastDecodeData;// 最后解码的数据
	protected int mLastDecodeDataSize = 0; // 最后解码的数据量
	protected int mLastDecodeRemainSize = 0; // 缓冲区中还剩余的数据量
	protected long mLastDecodeDataOffset = 0; // 当前读取的数据在源文件的偏移量

	ReadWriteLock mLockDecoder = new ReentrantReadWriteLock(false);

	public int getHeadSize() {
		return mHeadSize;
	}

	public int getDurnation() {
		return mDurnation;
	}

	public int getChannel() {
		return mChannel == 2 ? AudioFormat.CHANNEL_OUT_STEREO
				: AudioFormat.CHANNEL_OUT_MONO;
	}

	public int getSampleRate() {
		return mSampleRate;
	}

	public int getAudioFormat() {
		return mAudioFormat == 8 ? AudioFormat.ENCODING_PCM_8BIT
				: AudioFormat.ENCODING_PCM_16BIT;
	}

	// public int getDataPieceSize() {
	// return (mAudioFormat / 8) * mSampleRate * mChannel
	// * TXZAudioPlayer.PREPARE_BUFFER_DATA_TIME / 1000;
	// }

	/**
	 * 获取最后解码的数据
	 * 
	 * @return
	 */
	public byte[] getDecodeData() {
		return mLastDecodeData;
	}

	/**
	 * 获取解码数据的长度
	 * 
	 * @return
	 */
	public int getDecodeDataSize() {
		try {
			mLockDecoder.readLock().lock();
			nativeRead(nativeCodec);
			try {
				if (mDebugPcmFile != null) {
					mDebugPcmFile
							.write(mLastDecodeData, 0, mLastDecodeDataSize);
				}
			} catch (Exception e) {
			}
			// LogUtil.logd("read data: " + mLastDecodeDataSize + "/" +
			// mLastDecodeDataOffset);
			return mLastDecodeDataSize;
		} finally {
			mLockDecoder.readLock().unlock();
		}
	}

	/**
	 * 返回解码等待时间，<=0则不睡眠
	 * 
	 * @return
	 */
	public int getDecodeDelay() {
		if (mLastDecodeRemainSize > DECODE_BUFFER_SIZE * mAudioFormat
				* mSampleRate / 8 / 1000) {
			return DECODE_BUFFER_SIZE / 5;
		}
		return 0;
	}

	public long getDecodeDataOffset() {
		return mLastDecodeDataOffset;
	}

	public int decode(byte[] data, long offset, long len) {
		try {
			mLockDecoder.readLock().lock();
			if (nativeCodec == 0) {
				return -1;
			}
			return nativeDecode(nativeCodec, data, offset, len);
		} finally {
			mLockDecoder.readLock().unlock();
		}
	}

	public void cancel() {
		try {
			mLockDecoder.readLock().lock();
			if (nativeCodec == 0) {
				return;
			}
			nativeCancel(nativeCodec);
		} finally {
			mLockDecoder.readLock().unlock();
		}
	}

	public void flush() {
		try {
			mLockDecoder.readLock().lock();
			mLastDecodeDataSize = 0;
			mLastDecodeDataOffset = 0;
			if (nativeCodec == 0) {
				return;
			}
			nativeFlush(nativeCodec);
		} finally {
			mLockDecoder.readLock().unlock();
		}
	}

	public void release() {
		mLockDecoder.writeLock().lock();
		if (nativeCodec != 0) {
			nativeRelease(nativeCodec);
			nativeCodec = 0;
		}
		mLastDecodeDataSize = 0;
		mLastDecodeDataOffset = 0;
		mLockDecoder.writeLock().unlock();
	}

	AudioTrack mAudioTrack;

	public void test() {
		// if (mAudioTrack != null) {
		// mAudioTrack.flush();
		// } else {
		// mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC,
		// getSampleRate(), getChannel(), getAudioFormat(),
		// 100 * 1024, AudioTrack.MODE_STREAM);
		// }
		//
		// if (mAudioTrack.getState() == AudioTrack.STATE_INITIALIZED) {
		// mAudioTrack.play();
		// byte[] d = readDecodeData();
		// mAudioTrack.write(d, 0, d.length);
		// }
	}

	private static FileOutputStream mDebugPcmFile;

	public static void setDebugPcmFile(String url) {
		try {
			if (mDebugPcmFile != null) {
				mDebugPcmFile.close();
				mDebugPcmFile = null;
			}
			if (!TextUtils.isEmpty(url)) {
				mDebugPcmFile = new FileOutputStream(url);
			}
		} catch (Exception e) {
		}
	}
}
