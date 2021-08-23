package com.txznet.audio.player;

import android.media.AudioTrack;
import android.os.HandlerThread;

import com.txznet.audio.codec.TXZAudioDecoder;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZHandler;
import com.txznet.txz.util.runnables.Runnable4;

public class AudioCodecTrack {
	public final static int DECODE_PIECE_SIZE = 1 * 1024;
	public final static int READ_RUNNALE_QUEUE_SIZE = 2;

	private TXZAudioDecoder mTXZAudioDecoder;
	private HandlerThread mDecodeThread = null;
	private TXZHandler mDecodeHandler = null;
	private HandlerThread mWriteThread = null;
	private TXZHandler mWriteHandler = null;

	AudioTrack mAudioTrack;

	private synchronized void createHandler() {
		if (mDecodeThread == null) {
			mDecodeThread = new HandlerThread("ACTDecode");
			mDecodeThread.start();
			mDecodeHandler = new TXZHandler(mDecodeThread.getLooper());
		}
		if (mWriteThread == null) {
			mWriteThread = new HandlerThread("ACTWrite");
			mWriteThread.start();
			mWriteHandler = new TXZHandler(mWriteThread.getLooper());
		}
	}

	private synchronized void releaseHandler() {
		if (mDecodeThread != null) {
			mDecodeThread.quit();
			mDecodeThread = null;
			mDecodeHandler = null;
		}
		if (mWriteThread != null) {
			mWriteThread.quit();
			mWriteThread = null;
			mWriteHandler = null;
		}
	}

	int mPassTime = 0;

	protected void createAudioTrack() {
		int sampleRateInHz = mTXZAudioDecoder.getSampleRate();
		int channelConfig = mTXZAudioDecoder.getChannel();
		int audioFormat = mTXZAudioDecoder.getAudioFormat();
		mAudioTrack = new AudioTrack(mStreamType, sampleRateInHz, channelConfig, audioFormat, AudioTrack.getMinBufferSize(sampleRateInHz, channelConfig, audioFormat), AudioTrack.MODE_STREAM);
		// mAudioTrack
		// .setPositionNotificationPeriod(TXZAudioPlayer.PLAY_PROGRESS_NOTIFY_INTERVAL
		// * sampleRateInHz / 1000);
		// mAudioTrack
		// .setPlaybackPositionUpdateListener(new
		// OnPlaybackPositionUpdateListener() {
		// @Override
		// public void onPeriodicNotification(AudioTrack track) {
		// mPassTime += TXZAudioPlayer.PLAY_PROGRESS_NOTIFY_INTERVAL;
		// }
		//
		// @Override
		// public void onMarkerReached(AudioTrack track) {
		// }
		// });
	}

	int mStreamType;

	protected AudioCodecTrack(int streamType) {
		mStreamType = streamType;
		createHandler();
	}

	public static AudioCodecTrack createAudioTrack(int streamType, String url, byte[] data, long total) {
		TXZAudioDecoder decoder = TXZAudioDecoder.createDecoder(url, data, 0, data.length, total);
		if (decoder == null) {
			return null;
		}

		AudioCodecTrack ret = new AudioCodecTrack(streamType);
		ret.mTXZAudioDecoder = decoder;
		ret.createAudioTrack();
		return ret;
	}

	public void release() {
		synchronized (AudioCodecTrack.this) {
			if (null != mDecodeHandler) {
				mDecodeHandler.removeCallbacksAndMessages(null);
				mDecodeHandler.post(new Runnable() {
					@Override
					public void run() {
						if (mTXZAudioDecoder != null) {
							mTXZAudioDecoder.release();
						}
						releaseHandler();
						mAudioTrack.release();
					}
				});
			}
			if (null != mWriteHandler) {
				mWriteHandler.removeCallbacksAndMessages(null);
			}
			if (mTrackSession != null) {
				mTrackSession.release();
				mTrackSession = null;
			}
		}

	}

	public boolean isComplete() {
		synchronized (AudioCodecTrack.this) {
			return mTrackSession == null || mTrackSession.mEnd;
		}
	}

	// private int mReadDecodeDataSeq = 0;
	// private int mDecodeCount = 0;

	private class TrackSession {
		int mReadDecodeDataSeq = 0;
		int mDecodeCount = 0;
		int mLastReadSeq = 0;

		int mLastWriteCount = 0;
		int mLastReadCount = 0;
		byte[] mLastReadData = null;

		boolean mEnd = false;
		boolean mReleased = false;

		public TrackSession() {
			LogUtil.logd("codec session[" + this.hashCode() + "] create");
		}

		void release() {
			mWriteHandler.removeCallbacks(mCheckNeedRead);
			mReleased = true;
			LogUtil.logd("codec session[" + this.hashCode() + "] release");
			mReadDecodeDataSeq = -1;
			mDecodeCount = -1;
			mLastWriteCount = 0;
			mLastReadCount = 0;
			mLastReadData = null;
		}
		
		Runnable mCheckNeedRead = new Runnable() {
			@Override
			public void run() {
				if (!mReleased && isPlaying()) {
					createReadRunnable(false);
					mWriteHandler.postDelayed(mCheckNeedRead, 1000);
				}
			}
		};

		synchronized boolean createReadRunnable(boolean force) {
			mWriteHandler.removeCallbacks(mCheckNeedRead);
			mWriteHandler.postDelayed(mCheckNeedRead, 1000);
			if (!force && mReadDecodeDataSeq > mLastReadSeq + READ_RUNNALE_QUEUE_SIZE) {
				// 队列里已经有多个读的runnable了，则不构造新的
				return false;
			}
			++mReadDecodeDataSeq;
			LogUtil.logd("codec session[" + this.hashCode() + "] create new read runnable: " + mReadDecodeDataSeq);
			mWriteHandler.post(new ReadDecodeDataRunnable(this, mReadDecodeDataSeq));
			return true;
		}

		boolean isLastReadPack(int seq) {
			mLastReadSeq = seq;

			LogUtil.logd("codec session[" + this.hashCode() + "] isLastReadPack[" + seq + "/" + mReadDecodeDataSeq + "], decode=" + mDecodeCount);

			if ((mReadDecodeDataSeq < 0 || mDecodeCount < 0) && mTrackSession == this) {
				LogUtil.logw("codec session[" + this.hashCode() + "]  already release,mDecodeCount=" + mDecodeCount + ",mReadDecodeDataSeq=" + mReadDecodeDataSeq + "/" + seq);
			}
			if (seq == mReadDecodeDataSeq && mDecodeCount == 0) {
				mEnd = true;
				LogUtil.logd("codec session[" + this.hashCode() + "] complete");
				return true;
			}
			return false;
		}

		void incDecodeCount() {
			++mDecodeCount;
		}

		void decDecodeCount() {
			if (mDecodeCount <= 0) {
				LogUtil.logw("codec session[" + this.hashCode() + "] decode code error");
			}
			--mDecodeCount;

			if (mDecodeCount == 0) {
				LogUtil.logd("codec session[" + this.hashCode() + "] decode all end");
			}
		}

		void pauseSession() {
			mLastReadSeq = mReadDecodeDataSeq;
			mWriteHandler.removeCallbacksAndMessages(null);
		}

		void writeAudioData(int seq) {
			while (true) {
				if (false == AudioCodecTrack.this.isPlaying())
					return;
				if (mLastReadCount <= 0 || mLastReadData == null) {
					mLastReadCount = mTXZAudioDecoder.getDecodeDataSize();
					if (mLastReadCount <= 0)
						break;
					mLastReadData = mTXZAudioDecoder.getDecodeData();
					mLastWriteCount = 0;
				}
				try {
					while (mLastWriteCount < mLastReadCount) {
						mWriteHandler.heartbeat();
						int r = mAudioTrack.write(mLastReadData, mLastWriteCount, mLastReadCount - mLastWriteCount);
						if (r == 0) {
							if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED) {
								LogUtil.logd("codec session[" + this.hashCode() + "] writeAudioData paused");
								return;
							}
						}
						if (r < 0)
							break;
						mLastWriteCount += r;
					}
				} catch (Exception e) {
				}
				mLastWriteCount = 0;
				mLastReadCount = 0;
				mLastReadData = null;
				if (mOnCodecListener != null) {
					mOnCodecListener.onTrackWrite(mTXZAudioDecoder.getDecodeDataOffset());
				}
			}

			if (isLastReadPack(seq)) {
				if (mOnCodecListener != null) {
					mOnCodecListener.onCodecEnd();
				}
			}
		}
	}

	TrackSession mTrackSession = new TrackSession();

	private class ReadDecodeDataRunnable implements Runnable {
		int seq;
		TrackSession sess;

		public ReadDecodeDataRunnable(TrackSession sess, int seq) {
			this.sess = sess;
			this.seq = seq;
		}

		@Override
		public void run() {
			sess.writeAudioData(seq);
		}
	};

	public int write(byte[] audioData, int offsetInBytes, int sizeInBytes) {
		synchronized (AudioCodecTrack.this) {
			// createHandler();
			if (mTrackSession != null) {
				Runnable r = new Runnable4<byte[], Integer, Integer, TrackSession>(audioData, offsetInBytes, sizeInBytes, mTrackSession) {
					@Override
					public void run() {
						TrackSession sess = mP4;
						if (sess != null) {
							byte[] audioData = mP1;
							int offsetInBytes = mP2;
							int sizeInBytes = mP3;
							while (sizeInBytes > 0 && sess.mReleased == false) {
								int decodeLen = sizeInBytes < DECODE_PIECE_SIZE ? sizeInBytes : DECODE_PIECE_SIZE;
								mDecodeHandler.heartbeat();
								mTXZAudioDecoder.decode(audioData, offsetInBytes, decodeLen);
								sizeInBytes -= decodeLen;
								offsetInBytes += decodeLen;
								sess.createReadRunnable(false);
								while (sess.mReleased == false) {
									int delay = mTXZAudioDecoder.getDecodeDelay();
									if (delay <= 0)
										break;
									try {
										mDecodeHandler.heartbeat();
										Thread.sleep(delay);
										mDecodeHandler.heartbeat();
									} catch (InterruptedException e) {
									}
								}
								if (mOnCodecListener != null) {
									mOnCodecListener.onDecode(mTXZAudioDecoder.getDecodeDataOffset());
								}
							}
							sess.decDecodeCount();
						}
					}
				};
				mTrackSession.incDecodeCount();
				mDecodeHandler.post(r);
			}
		}

		return sizeInBytes;
	}

	public void flush(final Runnable runAfterFlush) {
		mPassTime = 0;
		mAudioTrack.stop();
		try {
			synchronized (AudioCodecTrack.this) {
				mDecodeHandler.removeCallbacksAndMessages(null);
				mWriteHandler.removeCallbacksAndMessages(null);
				mTXZAudioDecoder.cancel();
				if (mTrackSession != null) {
					mTrackSession.release();
				}
				mTrackSession = new TrackSession();
				mDecodeHandler.post(new Runnable() {
					@Override
					public void run() {
						mTXZAudioDecoder.flush();
						mAudioTrack.flush();
						mPassTime = 0;
						if (null == runAfterFlush) {
							return;
						}
						runAfterFlush.run();
					}
				});
			}
		} catch (Exception e) {
			throw new RuntimeException("flush error::" + e.getMessage());
		}
	}

	// ////////////////////////////////////////////////////////////////////////////////////////////////////////
	public static interface OnCodecListener {
		public void onTrackWrite(long total);

		public void onDecode(long total);

		public void onError(int errCode, String errDesc);

		public void onCodecEnd();
	}

	OnCodecListener mOnCodecListener;

	/**
	 * 设置解码错误回调监听器
	 * 
	 * @param listener
	 */
	public void setOnCodecListener(OnCodecListener listener) {
		mOnCodecListener = listener;
	}

	/**
	 * 获取解码头的字节数
	 * 
	 * @return
	 */
	public long getSeekPosition(long total, float percent) {
		return mTXZAudioDecoder.getHeadSize() + ((long) ((total - mTXZAudioDecoder.getHeadSize()) * percent));
	}

	protected TXZAudioDecoder getAudioDecoder() {
		return mTXZAudioDecoder;
	}

	protected int getDurnation() {
		return mTXZAudioDecoder.getDurnation();
	}

	// public int getDataPieceSize() {
	// return mTXZAudioDecoder.getDataPieceSize();
	// }

	/**
	 * 获取解码后正在播放的数据偏移量
	 * 
	 * @return
	 */
	protected long getDecodeDataOffset() {
		return mTXZAudioDecoder.getDecodeDataOffset();
	}

	public boolean isPlaying() {
		return mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING;
	}

	public void setStereoVolume(float leftVolume, float rightVolume) {
		mAudioTrack.setStereoVolume(leftVolume, rightVolume);
	}

	public void play() {
		mAudioTrack.play();
		if (mWriteHandler != null) {
			synchronized (AudioCodecTrack.this) {
				if (mTrackSession != null) {
					mTrackSession.createReadRunnable(true);
				}
			}
		}
	}

	public void pause() {
		try {
			mAudioTrack.pause();
			synchronized (AudioCodecTrack.this) {
				if (mTrackSession != null) {
					mTrackSession.pauseSession();
				}
			}
		} catch (Exception e) {
		}
	}
}
