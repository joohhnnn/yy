package com.txznet.nav.util;

import com.txznet.nav.MyApplication;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class PreferenceUtil {

	private static final String FILE_NAME = "txz_nav_pref";
	private static final SharedPreferences PREFERENCE = MyApplication.getApp()
			.getSharedPreferences(FILE_NAME, Context.MODE_PRIVATE);
	private static final Editor mEditor = PREFERENCE.edit();

	private static final String KEY_IS_MULTINAV = "isMultiNav";
	private static final String KEY_ROOMID = "roomId";

	/**
	 * 保证当导航crash时使得用户可以退出多车同行
	 * 
	 * @param isMultiNav
	 */
	public static void setIsMultiNav(boolean isMultiNav) {
		putBoolean(KEY_IS_MULTINAV, isMultiNav);
		if (!isMultiNav) {
			saveRoomId(-1);
		}
	}

	public static boolean IsMultiNav() {
		boolean nav = getBoolean(KEY_IS_MULTINAV, false);
		if (!nav) {
			saveRoomId(-1);
		}

		return nav;
	}

	/**
	 * 保存房间号
	 * 
	 * @param roomId
	 */
	public static void saveRoomId(long roomId) {
		putLong(KEY_ROOMID, roomId);
	}

	public static long getRoomId() {
		return getLong(KEY_ROOMID, -1);
	}

	private static void putBoolean(String key, boolean value) {
		mEditor.putBoolean(key, value);
		mEditor.commit();
	}

	private static boolean getBoolean(String key, boolean defValue) {
		return PREFERENCE.getBoolean(key, defValue);
	}

	private static void putLong(String key, long value) {
		mEditor.putLong(key, value);
		mEditor.commit();
	}

	private static long getLong(String key, long defValue) {
		return PREFERENCE.getLong(key, defValue);
	}
}
