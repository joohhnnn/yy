package com.txznet.audio.player;

import java.util.List;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.audio.player.SessionManager.SessionInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;

public abstract class TXZAudioPlayer {
	protected static final String TAG = "[MUSIC][AudioPlayer] ";
	public static final int PLAY_PROGRESS_NOTIFY_INTERVAL = 1000; // 通知播放进度时间间隔500ms
	public static final int PREPARE_BUFFER_DATA_TIME = 30000; // 预缓冲数据时长，决定碎片大小，默认30s
	public static final int NEED_BUFFER_DATA_TIME = 2 * 60 * 1000 /* 15000 */; // 需要开始缓冲的数据时长，默认15s

	protected TXZAudioPlayer(SessionInfo sess, int streamtype) {
		mSessionInfo = sess;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	public abstract int getDuration();

	public abstract float getPlayPercent();

	public abstract float getBufferingPercent();

	public abstract boolean isPlaying();

	public abstract boolean isBuffering();

	public abstract boolean needMoreData();

	public abstract long getDataPieceSize();

	/**
	 * 是否可以seek，没有的不要显示进度条
	 * 
	 * @return
	 */
	public boolean seekable() {
		return true;
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	public abstract void setVolume(float leftVolume, float rightVolume);

	public void setVolume(float volume) {
		setVolume(volume, volume);
	}

	/**
	 * 强制需要更多的数据
	 */
	public abstract void forceNeedMoreData(boolean isForce);

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	public abstract void prepareAsync();

	public abstract void start();

	public abstract void pause();

	public abstract void stop();

	public abstract void seekTo(float percent);

	public void release() {
		if (mSessionInfo != null) {
			if (Constant.ISTESTDATA) {
				LogUtil.logd("close socket.release");
			}
			mSessionInfo.cancelAllResponse();
			SessionManager.getInstance().removeSessionInfo(mSessionInfo.hashCode());
		}
	}

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	public interface OnPreparedListener {
		void onPrepared(TXZAudioPlayer ap);
	}

	public abstract void setOnPreparedListener(OnPreparedListener listener);

	public interface OnCompletionListener {
		void onCompletion(TXZAudioPlayer ap);
	}

	public abstract void setOnCompletionListener(OnCompletionListener listener);

	public interface OnBufferingUpdateListener {
		void onBufferingUpdate(TXZAudioPlayer ap, float percent);

		void onDownloading(TXZAudioPlayer ap, List<LocalBuffer> buffers);
	}

	protected OnBufferingUpdateListener mOnBufferingUpdateListenerSet = null;

	protected void onBufferingUpdate(float percent) {
		if (mOnBufferingUpdateListenerSet != null) {
			mOnBufferingUpdateListenerSet.onBufferingUpdate(this, percent);
		}
	}

	public void setOnBufferingUpdateListener(OnBufferingUpdateListener listener) {
		mOnBufferingUpdateListenerSet = listener;
	}

	public void notifyDownloading(List<LocalBuffer> buffers) {
		if (mOnBufferingUpdateListenerSet != null) {
			mOnBufferingUpdateListenerSet.onDownloading(this, buffers);
			if (buffers != null && buffers.size() > 0) {
				this.onBufferingUpdate(buffers.get(buffers.size() - 1).getToP());
			}
		}
	}

	public interface OnSeekCompleteListener {
		public void onSeekComplete(TXZAudioPlayer ap);
	}

	public abstract void setOnSeekCompleteListener(
			OnSeekCompleteListener listener);

	public interface OnErrorListener {
		boolean onError(TXZAudioPlayer ap, MediaError err);
	}

	protected OnErrorListener mOnErrorListenerSet = null;

	public void setOnErrorListener(OnErrorListener listener) {
		mOnErrorListenerSet = listener;
	}

	public void notifyError(MediaError err) {
		if (mOnErrorListenerSet != null) {
			mOnErrorListenerSet.onError(this, err);
		}
	}

	public interface OnPlayProgressListener {
		boolean onPlayProgress(TXZAudioPlayer ap, float percent);
	}

	public abstract void setOnPlayProgressListener(
			OnPlayProgressListener listener);

	// //////////////////////////////////////////////////////////////////////////////////////////////////////
	protected SessionInfo mSessionInfo = null;

	public SessionInfo getSessionInfo() {
		return mSessionInfo;
	}
}
