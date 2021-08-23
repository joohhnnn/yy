package com.txznet.music.utils;

import com.txznet.comm.remote.GlobalContext;

import android.widget.Toast;

/**
 * Toast统一管理类
 */
public class ToastUtils {

	public static boolean isShow = true;
	private static Toast toast = null;

	private ToastUtils() {
		/* cannot be instantiated */
		throw new UnsupportedOperationException("cannot be instantiated");
	}

	/**
	 * 短时间显示Toast
	 *
	 * @param message
	 */
	public static void showShort(CharSequence message) {
		if (isShow) {
			if (toast != null) {
				toast.cancel();
			}
			toast = Toast.makeText(GlobalContext.get().getApplicationContext(),
					message, Toast.LENGTH_SHORT);
			toast.show();
		}
	}

	/**
	 * 短时间显示Toast
	 *
	 * @param message
	 */
	public static void showShort(int message) {
		if (isShow)
			Toast.makeText(GlobalContext.get().getApplicationContext(),
					message, Toast.LENGTH_SHORT).show();
	}

	/**
	 * 长时间显示Toast
	 *
	 * @param message
	 */
	public static void showLong(CharSequence message) {
		if (isShow)
			Toast.makeText(GlobalContext.get().getApplicationContext(),
					message, Toast.LENGTH_LONG).show();
	}

	/**
	 * 长时间显示Toast
	 *
	 * @param message
	 */
	public static void showLong(int message) {
		if (isShow)
			Toast.makeText(GlobalContext.get().getApplicationContext(),
					message, Toast.LENGTH_LONG).show();
	}

	/**
	 * 网络连接不给力
	 */

	public static void showNetNotFrce() {
		ToastUtils.showShort("网络连接不给力");
	}

	/**
	 * 显示没有更多消息的toast
	 */
	public static void showNoMoreData() {
		ToastUtils.showShort("没有更多消息");
	}
}
