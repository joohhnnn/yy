package com.txznet.music.utils;

import java.io.IOException;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;

public class MediaPlayerUtils {

	static MediaPlayer mediaPlayer;

	public MediaPlayerUtils() {
		mediaPlayer = new MediaPlayer();
	}

	public static class NewInstance {
		private static final MediaPlayerUtils instance = new MediaPlayerUtils();
	}

	public static MediaPlayerUtils getInstance() {
		return NewInstance.instance;
	}

//	public void play() {
//		mediaPlayer.reset();
//		// context.getResource().getAssert().open("your media");
//		AssetFileDescriptor afd;
//		try {
//			afd = GlobalContext.get().getAssets().openFd("loading.wav");
//			mediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
//			mediaPlayer.prepare();// prepared state
//			mediaPlayer.start();// started state
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	/**
	 * 播放
	 */
	public void play(String httpurl) {
		try {
			// mediaPlayer.create(context, resid)//-->prepared state
			mediaPlayer.setDataSource(httpurl);// --> Initialized state
			mediaPlayer.prepare();// prepared state
			mediaPlayer.start();// started state
			mediaPlayer.setOnErrorListener(new OnErrorListener() {// 错误的时候就会跑到这里面//-->会变成Error
																	// state

						@Override
						public boolean onError(MediaPlayer mp, int what, int extra) {
							mediaPlayer.reset();// -->idel state
							return false;
						}
					});
			// 进入started state
			mediaPlayer.setOnBufferingUpdateListener(new OnBufferingUpdateListener() {

				@Override
				public void onBufferingUpdate(MediaPlayer mp, int percent) {

				}
			});

		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void pause() {
		if (mediaPlayer.isPlaying()) {// 如果正在播放
			mediaPlayer.pause();// -->paused state
		}
	}

//	public void stop() {
//		if (mediaPlayer.isPlaying()) {
//			mediaPlayer.stop();// -->stoped state
//		}
//	}
}
