package com.txznet.widget;

import android.util.Log;
import android.widget.Toast;

import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZTtsManager;

public class DebugUtil {
	/**
	 * 字符数组转换成字符串，并以、隔开
	 */
	public static String convertArrayToString(String[] array) {
		if (array == null || array.length == 0)
			return "";
		StringBuffer sb = new StringBuffer(array[0]);
		int length = array.length;
		for (int i = 1; i < length; i++) {
			sb.append("、" + array[i]);
		}
		return sb.toString();
	}

	private static Toast mLastToast = null;

	/**
	 * 
	 * @param context
	 * @param content
	 * @param tts
	 */
	public static void showTips(final CharSequence content, boolean tts) {
		Log.i("DebugUtil", "showTips: " + content);
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (mLastToast != null) {
					mLastToast.cancel();
				}
				mLastToast = Toast.makeText(AppLogic.getApp(), content,
						Toast.LENGTH_LONG);
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
