package com.txznet.txzsetting.util;

import android.util.Log;
import android.widget.Toast;

import com.txznet.sdk.TXZTtsManager;
import com.txznet.txzsetting.TXZApplication;

public class ToastUtil {

	private static Toast mLastToast = null;

	/**
	 * @param content
	 * @param tts 是否要TTS播报
	 */
	public static void showTips(final CharSequence content, boolean tts) {
		TXZApplication.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mLastToast != null) {
					mLastToast.cancel();
				}
				mLastToast = Toast.makeText(TXZApplication.getApp(), content,
						Toast.LENGTH_LONG);
				mLastToast.setGravity(0,0,0);
				mLastToast.show();
			}
		}, 0);
		try {
			if (tts) {
				TXZTtsManager.getInstance().speakText(content.toString());
			}
		} catch (Exception e) {
		}
	}

	public static void showTips(final CharSequence content) {
		showTips(content, false);
	}

}
