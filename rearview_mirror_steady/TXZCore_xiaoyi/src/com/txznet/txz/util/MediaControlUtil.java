package com.txznet.txz.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.view.KeyEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.music.MusicManager;

public class MediaControlUtil {
	private static AudioManager sAudioManager;

	public static void play() {
		MusicManager.getInstance().releaseAudioFocusImmediately();
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PLAY);
	}

	public static void stop() {
		MusicManager.getInstance().releaseAudioFocusImmediately();
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_STOP);
	}

	public static void pause() {
		MusicManager.getInstance().releaseAudioFocusImmediately();
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PAUSE);
	}

	public static void next() {
		MusicManager.getInstance().releaseAudioFocusImmediately();
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_NEXT);
	}

	public static void prev() {
		MusicManager.getInstance().releaseAudioFocusImmediately();
		sendKeyEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
	}

	@SuppressLint("NewApi")
	private static void sendKeyEvent(final int keycode) {

		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				if (Build.VERSION.SDK_INT >= 19) {
					if (null == sAudioManager) {
						sAudioManager = (AudioManager) GlobalContext.get()
								.getSystemService(Context.AUDIO_SERVICE);
					}
					sAudioManager.dispatchMediaKeyEvent(new KeyEvent(
							KeyEvent.ACTION_DOWN, keycode));
					sAudioManager.dispatchMediaKeyEvent(new KeyEvent(
							KeyEvent.ACTION_UP, keycode));
				} else {
					dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN,
							keycode));
					dispatchMediaKeyEvent(new KeyEvent(KeyEvent.ACTION_UP,
							keycode));
				}

			}
		}, 1000);
	}

	/**
	 * AudioManager.dispatchMediaKeyEvent需要API19, pre-api19设备通过反射调用
	 * 
	 * @param keyEvent
	 */
	public static void dispatchMediaKeyEvent(KeyEvent keyEvent) {
		try {
			/*
			 * 参考AudioManager.dispatchMediaKeyEvent实现 相当于调用以下代码
			 * IAudioService audioService = IAudioService.Stub.asInterface(b);
			 * audioService.dispatchMediaKeyEvent(keyEvent);
			 */
			IBinder iBinder = (IBinder) Class
					.forName("android.os.ServiceManager")
					.getDeclaredMethod("checkService", String.class)
					.invoke(null, Context.AUDIO_SERVICE);

			Object audioService = Class
					.forName("android.media.IAudioService$Stub")
					.getDeclaredMethod("asInterface", IBinder.class)
					.invoke(null, iBinder);

			Class.forName("android.media.IAudioService")
					.getDeclaredMethod("dispatchMediaKeyEvent", KeyEvent.class)
					.invoke(audioService, keyEvent);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
