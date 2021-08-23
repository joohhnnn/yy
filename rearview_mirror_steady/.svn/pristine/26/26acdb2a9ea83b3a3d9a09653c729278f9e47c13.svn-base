package com.txznet.audio.player;

import com.txznet.audio.player.AudioDirectCodecTrack.OnStateListener;
import com.txznet.audio.player.AudioDirectCodecTrack.State;
import com.txznet.audio.player.SessionManager.SessionInfo;

public class DirectAudioPlayer extends TXZAudioPlayer {
	AudioDirectCodecTrack mAudioDirectCodecTrack = null;
	private OnPreparedListener mOnPreparedListener;
	private OnCompletionListener mOnCompletionListener;
	State state;

	protected DirectAudioPlayer(SessionInfo sess, int streamtype, String path) {
		super(sess, streamtype);
		mAudioDirectCodecTrack = AudioDirectCodecTrack.createAudioTrack(
				streamtype, path);
		mAudioDirectCodecTrack.setOnStateListener(new OnStateListener() {

			@Override
			public void onState(State state) {
				DirectAudioPlayer.this.state = state;
				switch (state) {
				case played:
					if (null != mOnPreparedListener) {
						mOnPreparedListener.onPrepared(DirectAudioPlayer.this);
					}
					break;
				case buffered:
					break;
				case inited:
					break;
				default:
					break;
				}
			}

			@Override
			public void onError(int errcode, String errDesc) {
				// 向上层抛错误
				mOnErrorListenerSet.onError(DirectAudioPlayer.this,
						new MediaError(errcode, errDesc, "error"));
			}
		});
	}

	@Override
	public int getDuration() {
		return 0;
	}

	@Override
	public float getPlayPercent() {
		return 0;
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
		return false;
	}

	@Override
	public long getDataPieceSize() {
		return 0;
	}

	@Override
	public void setVolume(float leftVolume, float rightVolume) {
		mAudioDirectCodecTrack.setStereoVolume(leftVolume, rightVolume);
	}

	@Override
	public void prepareAsync() {
		// TODO 缓冲一定的数据量

	}

	@Override
	public void start() {
		// if (DirectAudioPlayer.this.state != State.played) {
		mAudioDirectCodecTrack.start();
		// }
	}

	@Override
	public void pause() {
		mAudioDirectCodecTrack.release();
	}

	@Override
	public void stop() {
		mAudioDirectCodecTrack.release();
	}

	@Override
	public void release() {
		mAudioDirectCodecTrack.release();
		super.release();
	}

	@Override
	public void seekTo(float percent) {
		// 不支持

	}

	@Override
	public void setOnPreparedListener(OnPreparedListener listener) {
		this.mOnPreparedListener = listener;

	}

	@Override
	public void setOnCompletionListener(OnCompletionListener listener) {

	}

	@Override
	public void setOnSeekCompleteListener(OnSeekCompleteListener listener) {
		// 不支持

	}

	@Override
	public void setOnPlayProgressListener(OnPlayProgressListener listener) {

	}

	@Override
	public void forceNeedMoreData(boolean isForce) {
		//TODO:强制拉取更多。
	}

}
