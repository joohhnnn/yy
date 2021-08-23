package com.txznet.music.utils;

import android.content.Intent;
import android.view.KeyEvent;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.engine.MediaPlayerActivityEngine;

public class MediaReceiver {

	public static void mediabtn(Intent intent) {
		if (null == intent) {
			LogUtil.loge("receiver  mediabtn is null");
			return;
		}
		LogUtil.logd("Action::" + intent.getAction());
		String intentAction = intent.getAction();
		if (Intent.ACTION_MEDIA_BUTTON.equals(intentAction)) {
			// 获得KeyEvent对象
			KeyEvent keyEvent = (KeyEvent) intent
					.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
			if (null == keyEvent) {
				return;
			}
			int keyCode = keyEvent.getKeyCode();
			LogUtil.logd("keyCode::" + keyCode);
			// if (headSetListener != null) {
			// try {
			if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
				if (KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE == keyCode) {
					MediaPlayerActivityEngine.getInstance().playOrPause();
				}
				// if (KeyEvent.KEYCODE_HEADSETHOOK == keyCode) {
				// if (headSetListener != null) {
				// if (isTimerStart) {
				// myTimer.cancel();
				// isTimerStart = false;
				// headSetListener.onDoubleClick();
				// } else {
				// myTimer = new MyTimer();
				// timer.schedule(myTimer, 1000);
				// isTimerStart = true;
				// }
				// }
				// }
				if (KeyEvent.KEYCODE_MEDIA_PREVIOUS == keyCode) {
					MediaPlayerActivityEngine.getInstance().last();
				}
				if (KeyEvent.KEYCODE_MEDIA_STOP == keyCode) {
					MediaPlayerActivityEngine.getInstance().stop();
				}
				if (KeyEvent.KEYCODE_MEDIA_NEXT == keyCode) {
					MediaPlayerActivityEngine.getInstance().next();
				}
				if (KeyEvent.KEYCODE_MEDIA_PLAY == keyCode) {
					MediaPlayerActivityEngine.getInstance().play();
				}
				if (KeyEvent.KEYCODE_MEDIA_PAUSE == keyCode) {
					MediaPlayerActivityEngine.getInstance().pause();
				}

			}
			// } catch (Exception e) {
			// // TODO: handle exception
			// }
			// }
		} else if (Intent.ACTION_HEADSET_PLUG.equals(intentAction)) {
			if (intent.getIntExtra("state", 1) == 0) {
				MediaPlayerActivityEngine.getInstance().pause();
			}
		}
	}
}
