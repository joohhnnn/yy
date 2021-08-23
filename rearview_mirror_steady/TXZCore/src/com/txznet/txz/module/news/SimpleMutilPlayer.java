package com.txznet.txz.module.news;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnPreparedListener;

import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.txz.module.tts.TtsManager;

public class SimpleMutilPlayer implements IMutilPlayer{
	private IMutilPlayer mPlayer = null;
	
	@Override
	public void play(Model model, IPlayCallBack cb) {
		switch(model.type){
			case Model.TYPE_TEXT_TTS:
				mPlayer = new TextPlayer();
				break;
			case Model.TYPE_NET_AUDIO:
				mPlayer = new AudioPlayer();
				break;
		}
		
		IMutilPlayer player = mPlayer;
		if (player != null){
			player.play(model, cb);
		}
	}

	@Override
	public void pause() {
		IMutilPlayer player = mPlayer;
		if (player != null){
			player.pause();
		}
	}

	@Override
	public void stop() {
		IMutilPlayer player = mPlayer;
		if (player != null){
			player.stop();
		}
	}

	@Override
	public boolean isBusy() {
		IMutilPlayer player = mPlayer;
		if (player != null){
			return player.isBusy();
		}
		return false;
	}
	
	private class TextPlayer implements IMutilPlayer{
		private IPlayCallBack mPlayCallBack = null;
		private int mCurrTaskid = 0;

		@Override
		public void play(Model model, IPlayCallBack cb) {
			String sText = model.text;
			mPlayCallBack = cb;
			mCurrTaskid = TtsManager.getInstance().speakText(sText, new ITtsCallback() {
				@Override
				public void onEnd() {
					final IPlayCallBack cb = mPlayCallBack;
					mPlayCallBack = null;
					if (cb != null) {
						mCurrTaskid = 0;
						cb.onEnd();
					}
				}
			});
		}

		@Override
		public void pause() {
			
		}

		/*
		 * 主动调用stop，不会有回调
		 * @see com.txznet.txz.module.news.IMutilPlayer#stop()
		 */
		@Override
		public void stop() {
			if (mCurrTaskid != 0){
				final int taskid = mCurrTaskid;
				mCurrTaskid = 0;
				mPlayCallBack = null;
				TtsManager.getInstance().cancelSpeak(taskid);
			}
		}

		@Override
		public boolean isBusy() {
			return mCurrTaskid != 0;
		}
		
	}
	
	private class AudioPlayer implements IMutilPlayer{
		private int sTaskId = 0;
		private IPlayCallBack mPlayCallBack = null;
		private int mCurrTaskid = 0;
		private MediaPlayer mPlayer = null;
		
		public AudioPlayer() {
			mPlayer = new MediaPlayer();
		}
		
		@Override
		public void play(Model model, IPlayCallBack cb) {
			sTaskId++;
			mCurrTaskid = sTaskId;
			
			try {
				mPlayer.setDataSource(model.url);
				mPlayer.prepareAsync(); 
				mPlayer.setOnCompletionListener(new OnCompletionListener() {
					
					@Override
					public void onCompletion(MediaPlayer mp) {
						onEnd(0);
					}
				});
				
				mPlayer.setOnPreparedListener(new OnPreparedListener() {
					@Override
					public void onPrepared(MediaPlayer mp) {
						mPlayer.start();
					}
				});


			} catch (Exception e) {
				onEnd(-1);
			}
			
		}

		@Override
		public void pause() {
		}

		@Override
		public void stop() {
			mCurrTaskid = 0;
			mPlayCallBack = null;
			mPlayer.stop();
		}

		@Override
		public boolean isBusy() {
			return mCurrTaskid != 0;
		}
		
		private void onEnd(int retCode){
			final IPlayCallBack cb = mPlayCallBack;
			mPlayCallBack = null;
			if (cb != null) {
				mCurrTaskid = 0;
				cb.onEnd();
			}
		}
	}

}
