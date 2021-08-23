package com.txznet.txz.module.launch;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;

import com.txznet.txz.jni.JNIHelper;

public class MediaButtonReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		// 获得Action
		String intentAction = intent.getAction();
		// 获得KeyEvent对象
		KeyEvent keyEvent = (KeyEvent) intent
				.getParcelableExtra(Intent.EXTRA_KEY_EVENT);

		JNIHelper.logd("Action ---->" + intentAction + "  KeyEvent----->"
				+ keyEvent.toString());
		boolean activateVoice = false;
		// 获得按键字节码
		int keyCode = keyEvent.getKeyCode();
		// 按下 / 松开 按钮
		int keyAction = keyEvent.getAction();
		// 获得事件的时间
		// long downtime = keyEvent.getEventTime();

		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)
				&& keyAction == KeyEvent.ACTION_UP) {
			// //这些都是可能的按键码 ， 打印出来用户按下的键
			if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
				// mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE,
				// AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			}
			if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
				// mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER,
				// AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
			}
			// 说明：当我们按下MEDIA_BUTTON中间按钮时，实际出发的是 KEYCODE_HEADSETHOOK 而不是
			// KEYCODE_MEDIA_PLAY_PAUSE
			if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
				activateVoice = true;
			}
			if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
				activateVoice = true;
			}
			if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
				activateVoice = true;
			}
			if (KeyEvent.KEYCODE_MEDIA_PLAY == keyCode) {
				activateVoice = true;
			}
			if (KeyEvent.KEYCODE_MEDIA_PAUSE == keyCode) {
				activateVoice = true;
			}
		}
		if (activateVoice) {
			JNIHelper.logd("MediaButtonReceiver doLaunch: " + keyCode);
			// LaunchManager.getInstance().launchWithRecord();
		}
	}

}