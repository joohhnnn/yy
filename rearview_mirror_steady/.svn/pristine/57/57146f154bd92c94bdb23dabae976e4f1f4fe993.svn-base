package com.txznet.audio.player;

import com.txznet.audio.client.TXZMediaClient;
import com.txznet.audio.player.AudioCodecTrack.OnCodecListener;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.comm.remote.util.LogUtil;

import android.media.AudioManager;

public class CodecAudioPlayer extends TXZAudioPlayer {
	public static final int DEFAULT_DATA_PIECE_SIZE = 20 * 1024; // 默认20KB碎片大小，差不多30s

	TXZMediaClient mTXZMediaClient;
	OnCodecListener mOnCodecListener = new OnCodecListener() {
		@Override
		public void onTrackWrite(long total) {
			if (mOnPlayProgressListener != null) {
				mOnPlayProgressListener.onPlayProgress(CodecAudioPlayer.this, CodecAudioPlayer.this.getPlayPercent());
			}
		}

		@Override
		public void onDecode(long offset) {
			// TODO 解码进度处理
			mIsBuffering = false;
		}

		@Override
		public void onError(int errCode, String errDesc) {
			// TODO 解码错误处理
		}

		@Override
		public void onCodecEnd() {
			checkComplete();
		}
	};

	private void checkComplete() {
		if (mAudioTrack.isComplete()) {
			if (mTXZMediaClient.getDownloadSize() >= mTXZMediaClient.getTotalSize()) {
				if (mOnCompletionListener != null) {
					mOnCompletionListener.onCompletion(CodecAudioPlayer.this);
				}
			} else {
				mIsBuffering = true;
				if (mOnPlayProgressListener != null) {
					mOnPlayProgressListener.onPlayProgress(CodecAudioPlayer.this, CodecAudioPlayer.this.getPlayPercent());
				}
			}
		}
	}

	private byte[] mDataRemain = null;
	TXZMediaClient.OnResponseListener mTXZMediaClientResponseListener = new TXZMediaClient.OnResponseListener() {
		@Override
		public void onRecive(final float percent, final byte[] _data) {
			byte[] data = _data;
			if (mDataRemain != null) {
				byte[] newdata = new byte[mDataRemain.length + data.length];
				System.arraycopy(mDataRemain, 0, newdata, 0, mDataRemain.length);
				System.arraycopy(data, 0, newdata, mDataRemain.length, data.length);
				data = newdata;
				mDataRemain = null;
			}
			if (mAudioTrack == null) {
				try {
					mAudioTrack = AudioCodecTrack.createAudioTrack(mStreamType, mTXZMediaClient.getUrl(), data, mTXZMediaClient.getTotalSize());
					// 还没有收全解码头，记录数据下次继续解码
					if (mAudioTrack == null) {
						mDataRemain = data;
						return;
					}
				} catch (Exception e) {
					// 创建解码器发生异常
					onError(new MediaError(MediaError.ERR_CODEC, "create codec error", "创建解码器发生异常"));
					return;
				}
				if (mAudioTrack != null) {
					mAudioTrack.setOnCodecListener(mOnCodecListener);
					mIsBuffering = false;
					if (mOnPreparedListener != null) {
						mOnPreparedListener.onPrepared(CodecAudioPlayer.this);
					}
					data = null;
				}
			}
			mIsBuffering = false;
			if (mAudioTrack != null) {
				if (data != null) {
					mAudioTrack.write(data, 0, data.length);
				}
			}
			CodecAudioPlayer.this.onBufferingUpdate(percent);
//			if (mOnBufferingUpdateListenerSet != null) {
//				mOnBufferingUpdateListenerSet.onBufferingUpdate(CodecAudioPlayer.this, percent);
//			}
		}

		@Override
		public void onGetInfo() {
		}

		@Override
		public void onSeek() {
			if (mOnSeekCompleteListener != null) {
//				CodecAudioPlayer.this.start();
				mOnSeekCompleteListener.onSeekComplete(CodecAudioPlayer.this);
			}
		}

		@Override
		public void onEnd() {

		}

		@Override
		public void onError(MediaError err) {
			mTXZMediaClient.cancel();
			if (mOnErrorListenerSet != null) {
				mOnErrorListenerSet.onError(CodecAudioPlayer.this, err);
			}
		}
	};
	AudioCodecTrack mAudioTrack;
	int mStreamType;

	protected void init(int streamtype, TXZMediaClient client, Integer durnation) {
		mStreamType = streamtype;

		mTXZMediaClient = client;
		mTXZMediaClient.setOnResponseListener(mTXZMediaClientResponseListener);

		mDurnation = durnation;
	}

	Integer mDurnation = null;

	private boolean mIsForceNeedMoreData=false;

	public CodecAudioPlayer(SessionInfo sess, int streamtype, TXZMediaClient client, Integer durnation) {
		super(sess, streamtype);
		init(streamtype, client, durnation);
	}

	public CodecAudioPlayer(SessionInfo sess, TXZMediaClient client, Integer durnation) {
		super(sess, AudioManager.STREAM_MUSIC);
		init(AudioManager.STREAM_MUSIC, client, null);
	}

	public CodecAudioPlayer(SessionInfo sess, TXZMediaClient client) {
		super(sess, AudioManager.STREAM_MUSIC);
		init(AudioManager.STREAM_MUSIC, client, null);
	}

	@Override
	public int getDuration() {
		int d = 0;
		try {
			d = mAudioTrack.getDurnation();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (d > 0)
			return d;
		if (mDurnation != null)
			return mDurnation;
		return d;
	}

	@Override
	public long getDataPieceSize() {
		if (mAudioTrack != null) {
			int d = mAudioTrack.getDurnation();
			if (d != 0) {
				if (d < 0) {
					d = -d;
				}
				return mTXZMediaClient.getTotalSize() * PREPARE_BUFFER_DATA_TIME / d;
			}
		}
		return DEFAULT_DATA_PIECE_SIZE;
	}

	@Override
	public boolean needMoreData() {
		if (mReleased) {
			throw new RuntimeException("player alreay released");
		}
		if(mIsForceNeedMoreData){
			mIsForceNeedMoreData=false;
			return true;
		}
		
		return (mTXZMediaClient.getTotalSize() * (getBufferingPercent() - getPlayPercent())) // 缓冲好未播放的数据
		< (getDataPieceSize() * NEED_BUFFER_DATA_TIME / PREPARE_BUFFER_DATA_TIME); // 需要准备的数据
	}

	@Override
	public float getPlayPercent() {
		float percent = 0.0F;
		try {
			percent = mPlayStartPosition + (mAudioTrack.getDecodeDataOffset() * 1.0F / mTXZMediaClient.getTotalSize());
			if (percent >= 1.0F) {
				percent = 1.0F;
			}
			return percent;
		} catch (Exception e) {
			return 0.0F;
		}
	}

	@Override
	public float getBufferingPercent() {
		return mTXZMediaClient.getDownloadSize() * 1.0F / mTXZMediaClient.getTotalSize();
	}

	@Override
	public boolean isPlaying() {
		if (mAudioTrack == null) {
			return false;
		}
		return mAudioTrack.isPlaying();
	}

	private boolean mIsBuffering = false;

	@Override
	public boolean isBuffering() {
		return mIsBuffering;
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		if (mAudioTrack != null) {
			mAudioTrack.setStereoVolume(leftVolume, rightVolume);
		}
	}

	@Override
	public void prepareAsync() {
		mIsBuffering = true;
		mTXZMediaClient.request();
	}

	@Override
	public void start() {
		if (mAudioTrack != null) {
			if (mStoped) {
				// 如果之前停止过，则重新开始寻址
				final OnSeekCompleteListener listener = mOnSeekCompleteListener;
				mOnSeekCompleteListener = new OnSeekCompleteListener() {
					@Override
					public void onSeekComplete(TXZAudioPlayer ap) {
						mAudioTrack.play();
						mOnSeekCompleteListener = listener;
					}
				};
				seekTo(0.0F);
				mStoped = false;
				return;
			}
			mAudioTrack.play();
		}
	}

	@Override
	public void pause() {
		if (mAudioTrack != null) {
			mAudioTrack.pause();
		}
	}

	boolean mStoped = false;

	@Override
	public void stop() {
		LogUtil.logi("------------stop::cancel");
		mTXZMediaClient.cancel();
		if (mAudioTrack != null) {
			mStoped = true;
			mAudioTrack.flush(null);
		}
	}

	protected float mPlayStartPosition = 0;

	@Override
	public void seekTo(float percent) {
		LogUtil.logd("media session[" + mSessionInfo.getLogId() + "] seekTo: " + ((int) (getDuration() * percent)) + "|" + percent);

		if (mAudioTrack == null) {
			// 解码器未准备就绪，无法seek
			return;
		}
		mPlayStartPosition = percent;
		mTXZMediaClient.cancel();
		mIsBuffering = true;
		if (mAudioTrack != null) {
			try {
				mAudioTrack.flush(new Runnable() {
					@Override
					public void run() {
						long position = mAudioTrack.getSeekPosition(mTXZMediaClient.getTotalSize(), mPlayStartPosition);
						LogUtil.logd("seek to position: " + position);
						if (position >= mTXZMediaClient.getTotalSize()) {
							if (mOnCompletionListener != null) {
								mOnCompletionListener.onCompletion(CodecAudioPlayer.this);
							}
						} else {
							mTXZMediaClient.seek(position);
						}
					}
				});
			} catch (Exception e) {
				if (mOnErrorListenerSet != null) {
					mOnErrorListenerSet.onError(this, new MediaError(MediaError.ERR_UNKNOW, e.getMessage(), "发生未知错误"));
				}
			}
		}
	}

	private boolean mReleased = false;

	@Override
	public void release() {
		LogUtil.logd("media session[" + mSessionInfo.getLogId() + "] release ");
		mTXZMediaClient.cancel();
		mOnPreparedListener = null;
		mOnPlayProgressListener = null;
		mOnSeekCompleteListener = null;
		if (mAudioTrack != null) {
			mAudioTrack.release();
		}
		mReleased = true;
		
		super.release();
	}

	protected OnPreparedListener mOnPreparedListener;

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListener = listener;
	}

	protected OnCompletionListener mOnCompletionListener;

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListener = listener;
	}

	protected OnSeekCompleteListener mOnSeekCompleteListener;

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListener = listener;
	}

	protected OnPlayProgressListener mOnPlayProgressListener;

	@Override
	public void setOnPlayProgressListener(OnPlayProgressListener listener) {
		mOnPlayProgressListener = listener;
	}

	@Override
	public void forceNeedMoreData(boolean isForce) {
		mIsForceNeedMoreData=isForce;
	}
}
