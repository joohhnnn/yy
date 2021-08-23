package com.txznet.audio.player;

import java.util.ArrayList;
import java.util.List;

import android.media.AudioManager;
import android.media.MediaPlayer;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.audio.player.audio.FileAudio;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

public class SysAudioPlayer extends TXZAudioPlayer {
	public static final int DEFAULT_DATA_PIECE_SIZE = 500 * 1024; // 默认500KB碎片大小，差不多30s

	private static android.media.MediaPlayer mMediaPlayer;
	private boolean isError = false;

	private float mBufferingPercent = 0;
	private android.media.MediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new android.media.MediaPlayer.OnBufferingUpdateListener() {
		@Override
		public void onBufferingUpdate(android.media.MediaPlayer mp, int percent) {
			if (mBufferingPercent >= percent) {
				return;
			}
			SysAudioPlayer.this.onBufferingUpdate(percent / 100.0f);
		}
	};

	protected void onBufferingUpdate(float percent) {
		mBufferingPercent = percent * 100;
		super.onBufferingUpdate(percent);
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	private OnSeekCompleteListener mOnSeekCompleteListenerSet = null;
	private android.media.MediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new android.media.MediaPlayer.OnSeekCompleteListener() {
		@Override
		public void onSeekComplete(android.media.MediaPlayer mp) {
			LogUtil.logd("mediaplayer:-38:=" + mMediaPlayer + ", mp=" + mp + ", mOnSeekCompleteListener =" + mOnErrorListenerSet);
			mIsBuffering = false;
			if (mOnSeekCompleteListenerSet != null) {
				mOnSeekCompleteListenerSet.onSeekComplete(SysAudioPlayer.this);
			}
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	private OnPreparedListener mOnPreparedListenerSet = null;
	private android.media.MediaPlayer.OnPreparedListener mOnPreparedListener = new android.media.MediaPlayer.OnPreparedListener() {
		@Override
		public void onPrepared(android.media.MediaPlayer mp) {
			LogUtil.logd("mediaplayer:-38:=" + mMediaPlayer + ", mp=" + mp + ", mOnPreparedListener =" + mOnErrorListenerSet);
			mIsBuffering = false;
			if (mOnPreparedListenerSet != null) {
				mOnPreparedListenerSet.onPrepared(SysAudioPlayer.this);
			}
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	private android.media.MediaPlayer.OnErrorListener mOnErrorListener = new android.media.MediaPlayer.OnErrorListener() {
		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			LogUtil.logd("mediaplayer:-38:=" + mMediaPlayer + ", mp=" + mp + ", mOnErrorListener =" + mOnErrorListenerSet);
			if (mOnErrorListenerSet != null) {
				if (mMediaPlayer != null) {
					if (mMediaPlayer != mp)  {
						LogUtil.logw("audio error not TXZAudio audioPlayer=" + mMediaPlayer + ", mp=" + mp);
						return false;
					}
				}
				isError = true;
				return mOnErrorListenerSet.onError(SysAudioPlayer.this,
						new MediaError(MediaError.ERR_SYS_PLAYER, what + "-"
								+ extra, "播放音频发生错误"));
			}
			return true;
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////
	private OnCompletionListener mOnCompletionListenerSet = null;
	private android.media.MediaPlayer.OnCompletionListener mOnCompletionListener = new android.media.MediaPlayer.OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mp) {
			LogUtil.logd("mediaplayer:-38:=" + mMediaPlayer + ", mp=" + mp + ", mOnCompletionListener =" + mOnErrorListenerSet);
			if (!isError && mOnCompletionListenerSet != null) {
				mOnCompletionListenerSet.onCompletion(SysAudioPlayer.this);
			}
		}
	};

	// //////////////////////////////////////////////////////////////////////////////////////////////////////////

	protected void init(int streamtype, String url) {
		// mMediaPlayer.reset();
		mMediaPlayer=new MediaPlayer();
		LogUtil.logd("mediaplayer:-38:setAudioStreamType:begin");
		mMediaPlayer.setAudioStreamType(streamtype);
		LogUtil.logd("mediaplayer:-38:setAudioStreamType:end");
		mMediaPlayer.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
		mMediaPlayer.setOnPreparedListener(mOnPreparedListener);
		mMediaPlayer.setOnSeekCompleteListener(mOnSeekCompleteListener);
		mMediaPlayer.setOnCompletionListener(mOnCompletionListener);
		mMediaPlayer.setOnErrorListener(mOnErrorListener);

		try {
			mMediaPlayer.setDataSource(url);
		} catch (Exception e) {
			LogUtil.loge("url=" + url);
			e.printStackTrace();
		}
	}

	public SysAudioPlayer(SessionInfo sess, int streamtype, String url) {
		super(sess, streamtype);
		init(streamtype, url);
	}

	public SysAudioPlayer(SessionInfo sess, String url) {
		super(sess, AudioManager.STREAM_MUSIC);
		init(AudioManager.STREAM_MUSIC, url);
	}

	@Override
	public int getDuration() {
		try {
			return mMediaPlayer.getDuration();
		} catch (Exception e) {
			if (null != mOnErrorListener) {
				mOnErrorListener.onError(mMediaPlayer, -20, 20);
			}
		}
		return 0;
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
	public float getPlayPercent() {
		float r = getPlayPercentInner();
		float b = getBufferingPercent();
		if (r > b) {
			return b;
		}
		return r;
	}

	@Override
	public float getBufferingPercent() {
		return mBufferingPercent / 100.0f;
	}

	@Override
	public boolean isPlaying() {
		LogUtil.logd(TAG+"isPlaying");
		if (mIsBuffering) {
			return false;
		}
		try {
			return mMediaPlayer.isPlaying();
		} catch (Exception e) {
			LogUtil.loge(TAG+"[exception]",e);
			// java.lang.Exception
		}
		return false;
	}

	boolean mIsBuffering = true;
	boolean mIsForceNeedMoreData = false;

	@Override
	public boolean isBuffering() {
		return mIsBuffering || getBufferingPercent() < getPlayPercentInner();
	}

	@Override
	public long getDataPieceSize() {
		// TODO 根据码率计算碎片大小
		return DEFAULT_DATA_PIECE_SIZE;
	}

	@Override
	public boolean needMoreData() {
		LogUtil.logd(TAG+"[decide] needMoreData release?"+mReleased);
		if (mReleased) {
			throw new RuntimeException("player alreay released");
		}
		if (mIsBuffering) {
			return true;
		}
		if (mIsForceNeedMoreData) {
			mIsForceNeedMoreData=false;
			return true;
		}
		if (isPlaying()) {
			try {
//				if (Constant.ISNEED) {
//					LogUtil.logd(TAG+"[variable]"+mBufferingPercent+",duration"+ mMediaPlayer.getDuration()+",currentPosition:"+mMediaPlayer.getCurrentPosition());
//				}
				return (mBufferingPercent * mMediaPlayer.getDuration() / 100.0)
						- mMediaPlayer.getCurrentPosition() < NEED_BUFFER_DATA_TIME;
			} catch (Exception e) {
				LogUtil.loge(TAG+"[exception] ", e);
			}
		}
		return false;
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		LogUtil.logd("mediaplayer:-38:setVolume:begin");
		mMediaPlayer.setVolume(leftVolume, rightVolume);
		LogUtil.logd("mediaplayer:-38:setVolume:end");
	}

	@Override
	public void prepareAsync() {
		mIsBuffering = true;
		try {
			LogUtil.logd("mediaplayer:-38:prepareAsync:begin");
			mMediaPlayer.prepareAsync();
			LogUtil.logd("mediaplayer:-38:prepareAsync:end");
		} catch (Exception e) {
			if (null != mOnErrorListener) {
				mOnErrorListener.onError(mMediaPlayer, 1, 520);//
			}
		}
	}

	@Override
	public void start() {
		try {
			LogUtil.logd("mediaplayer:-38:start:begin");
			mMediaPlayer.start();
			LogUtil.logd("mediaplayer:-38:start:end");
			AppLogic.runOnUiGround(mRunnableRefreshPlayProgress, 0);
//			mRunnableRefreshPlayProgress.run();
		} catch (Exception e) {
			LogUtil.loge(TAG + e.getMessage());
		}
	}

	@Override
	public void pause() {
		try {
			LogUtil.logd("mediaplayer:-38:pause:begin");
			mMediaPlayer.pause();
			LogUtil.logd("mediaplayer:-38:pause:end");
		} catch (Exception e) {
			if (mOnErrorListener != null) {
				mOnErrorListener.onError(mMediaPlayer, -20, 20);
			}
		}
	}

	@Override
	public void stop() {
		LogUtil.logd("mediaplayer:-38:stop:begin");
		mMediaPlayer.stop();
		LogUtil.logd("mediaplayer:-38:stop:end");
	}

	private boolean mReleased = false;

	@Override
	public void release() {
		//为了使调用方的线程和该置空的线程放在同一个线程，避免空指针。
		AppLogic.runOnUiGround(new Runnable() {
			
			@Override
			public void run() {
				mOnPlayProgressListener = null;
				mOnBufferingUpdateListenerSet = null;
				mOnCompletionListenerSet = null;
				mOnPreparedListenerSet = null;
				mOnSeekCompleteListenerSet = null;
			}
		}, 0);
		AppLogic.runOnSlowGround(kellMyself, 1000);
		mMediaPlayer.release();
//		mMediaPlayer.reset();
		AppLogic.removeSlowGroundCallback(kellMyself);
		mReleased = true;

		super.release();
	}

	private Runnable kellMyself = new Runnable() {

		@Override
		public void run() {
			android.os.Process.killProcess(android.os.Process.myPid());
		}
	};

	@Override
	public void seekTo(float percent) {
		LogUtil.logd("media session[" + mSessionInfo.getLogId() + "] seekTo: "
				+ ((int) (getDuration() * percent)) + "|" + percent);
		mIsBuffering = true;
		LogUtil.logd("mediaplayer:-38:seek:begin");
		mMediaPlayer.seekTo((int) (getDuration() * percent));
		LogUtil.logd("mediaplayer:-38:seek:end");
	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		mOnPreparedListenerSet = listener;
	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {
		mOnCompletionListenerSet = listener;
	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		mOnSeekCompleteListenerSet = listener;
	}

	OnPlayProgressListener mOnPlayProgressListener = null;
	float mLastPlayPercent = -1;

	Runnable mRunnableRefreshPlayProgress = new Runnable() {
		@Override
		public void run() {
			if (mOnPlayProgressListener != null
					&& SysAudioPlayer.this.isPlaying()) {
				float p = getPlayPercent();
				if (p != mLastPlayPercent) {
					mOnPlayProgressListener.onPlayProgress(SysAudioPlayer.this,
							p);
					mLastPlayPercent = p;
				}else{
					LogUtil.logd(TAG+" can't excute :"+p);
				}
				AppLogic.runOnUiGround(this, PLAY_PROGRESS_NOTIFY_INTERVAL);
			}

			if (mOnBufferingUpdateListenerSet != null
					&& mSessionInfo.audio instanceof FileAudio) {
				List<LocalBuffer> lst = new ArrayList<LocalBuffer>();
				lst.add(LocalBuffer.buildFull(100));
				notifyDownloading(lst);
			}
		}
	};

	@Override
	public void setOnPlayProgressListener(OnPlayProgressListener listener) {
		mOnPlayProgressListener = listener;
		AppLogic.runOnUiGround(mRunnableRefreshPlayProgress, 0);
//		mRunnableRefreshPlayProgress.run();
	}

	@Override
	public void forceNeedMoreData(boolean isForce) {
		mIsForceNeedMoreData=isForce;
	}

}
