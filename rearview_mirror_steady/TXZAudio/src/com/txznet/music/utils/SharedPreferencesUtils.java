package com.txznet.music.utils;

import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.txznet.comm.remote.GlobalContext;

public class SharedPreferencesUtils {

	private static final String PRE_NAME_TXZ = "txz_music";

	private class SharePreferenceKeyParams {

		public static final String KEY_CLOSEVOLUME = "key_closevolume";
		public static final String KEY_NOTAPPPNAME = "key_notappname";
		public static final String KEY_LOCALPATHS = "key_local_paths";
		public static final String KEY_SEARCHSIZE = "key_searchsize";
		public static final String KEY_CONFIG = "key_config";
		public static final String KEY_PLAY_MODE = "key_play_mode";
		public static final String KEY_CURRENT_ALBUM_ID = "key_current_album_id";
		public static final String KEY_IS_FIRST = "key_is_first";
		public static final String KEY_AUDIO_SOUCRE = "key_audio_soucre";
		public static final String KEY_IS_PLAY = "key_is_play";
		public static final String KEY_IS_FIRST_TIP = "key_is_fist_tip";
		public static final String KEY_APPFIRSTPLAY = "key_appfirstplay";
		public static final String KEY_FATALEXIT = "key_fatalexit";
		public  static final String KEY_APP_NEED_ASR="key_app_need_asr";
		public static final String KEY_RELEASE_AUDIO_FOCUS = "key_release_audio_focus";
		public static final String KEY_WAKEUP_PLAY = "key_wakeup_play";
		public static final String KEY_Full_SCREEN = "key_full_screen";
	}

	public static void setWakeupPlay(boolean isPlay) {
		put(SharePreferenceKeyParams.KEY_WAKEUP_PLAY, isPlay);
	}

	public static boolean getWakeupPlay() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_WAKEUP_PLAY, true);
	}
	public static void setFullScreen(boolean full) {
		put(SharePreferenceKeyParams.KEY_Full_SCREEN, full);
	}
	
	public static boolean getFullScreen() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_Full_SCREEN, false);
	}
	
	public static void setReleaseAudioFocus(boolean isRelease) {
		put(SharePreferenceKeyParams.KEY_RELEASE_AUDIO_FOCUS, isRelease);
	}

	public static boolean isReleaseAudioFocus() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_RELEASE_AUDIO_FOCUS, true);
	}
	
	public static boolean isCloseVolume() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_CLOSEVOLUME, true);
	}

	public static void setCloseVolume(boolean value) {
		put(SharePreferenceKeyParams.KEY_CLOSEVOLUME, value);
	}

	public static String getNotOpenAppPName() {
		return (String) get(SharePreferenceKeyParams.KEY_NOTAPPPNAME, "");
	}

	public static void setNotOpenAppPName(String pNames) {
		put(SharePreferenceKeyParams.KEY_NOTAPPPNAME, pNames);
	}
	public static String getLocalPaths() {
		return (String) get(SharePreferenceKeyParams.KEY_LOCALPATHS, "");
	}
	
	public static void setLocalPaths(String paths) {
		put(SharePreferenceKeyParams.KEY_LOCALPATHS, paths);
	}

	public static Long getSearchSize() {
		return (Long) get(SharePreferenceKeyParams.KEY_SEARCHSIZE, 200 * 1024L);
	}

	public static void setSearchSize(Long searchSize) {
		put(SharePreferenceKeyParams.KEY_SEARCHSIZE, searchSize);
	}

	public static String getConfig() {
		return (String) get(SharePreferenceKeyParams.KEY_CONFIG, "");
	}

	public static void setConfig(String config) {
		put(SharePreferenceKeyParams.KEY_CONFIG, config);
	}

	public static int getPlayMode() {
		return (Integer) get(SharePreferenceKeyParams.KEY_PLAY_MODE, 0);
	}

	public static void setPlayMode(int mode) {
		put(SharePreferenceKeyParams.KEY_PLAY_MODE, mode);
	}

//	public static long getCurrentAlbumID() {
//		return (Long) get(SharePreferenceKeyParams.KEY_CURRENT_ALBUM_ID, 0L);
//	}
//
//	public static void setCurrentAlbumID(long albumID) {
//		put(SharePreferenceKeyParams.KEY_CURRENT_ALBUM_ID, albumID);
//	}

	public static int getAudioSource() {
		return (Integer) get(SharePreferenceKeyParams.KEY_AUDIO_SOUCRE, 0);
	}

	public static void setAudioSource(int source) {
		put(SharePreferenceKeyParams.KEY_AUDIO_SOUCRE, source);
	}

	public static boolean isFirst() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_IS_FIRST, true);
	}

	public static void setIsFirst(boolean value) {
		put(SharePreferenceKeyParams.KEY_IS_FIRST, value);
	}

	public static boolean getIsPlay() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_IS_PLAY, false);
	}

	public static void setIsPlay(boolean b) {
		put(SharePreferenceKeyParams.KEY_IS_PLAY, b);
	}

	public static boolean getShouldTip() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_IS_FIRST_TIP, false);
	}

	public static void setShouldTip(boolean b) {
		put(SharePreferenceKeyParams.KEY_IS_FIRST_TIP, b);
	}

	public static boolean getAppFirstPlay() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_APPFIRSTPLAY, false);
	}

	public static void setAppFirstPlay(boolean b) {
		put(SharePreferenceKeyParams.KEY_APPFIRSTPLAY, b);
	}
	
	public static boolean getFatalExit() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_FATALEXIT, false);
	}
	
	public static void setFatalExit(boolean b) {
		put(SharePreferenceKeyParams.KEY_FATALEXIT, b);
	}

	public static boolean getNeedAsr() {
		return (Boolean) get(SharePreferenceKeyParams.KEY_APP_NEED_ASR, true);
	}

	// 设置是否需要全局唤醒词
	public static void setNeedAsr(boolean b) {
		put(SharePreferenceKeyParams.KEY_APP_NEED_ASR, b);
	}

	/**
	 * 保存数据的方法，我们需要拿到保存数据的具体类型，然后根据类型调用不同的保存方法
	 *
	 * @param context
	 * @param key
	 * @param object
	 */
	public static void put(String key, Object object) {
		SharedPreferences sp = getSharedPreferences(GlobalContext.get(),
				SharedPreferenceType.LOCAL);
		SharedPreferences.Editor editor = sp.edit();
		if (object instanceof String) {
			editor.putString(key, (String) object);
		} else if (object instanceof Integer) {
			editor.putInt(key, (Integer) object);
		} else if (object instanceof Boolean) {
			editor.putBoolean(key, (Boolean) object);
		} else if (object instanceof Float) {
			editor.putFloat(key, (Float) object);
		} else if (object instanceof Long) {
			editor.putLong(key, (Long) object);
		} else {
			editor.putString(key, object.toString());
		}
		editor.commit();
		// SharedPreferencesCompat.apply(editor);
	}

	/**
	 * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
	 *
	 * @param context
	 * @param key
	 * @param defaultObject
	 * @return
	 */
	public static Object get(String key, Object defaultObject) {
		SharedPreferences sp = getSharedPreferences(GlobalContext.get(),
				SharedPreferenceType.LOCAL);
		if (defaultObject instanceof String) {
			return sp.getString(key, (String) defaultObject);
		} else if (defaultObject instanceof Integer) {
			return sp.getInt(key, (Integer) defaultObject);
		} else if (defaultObject instanceof Boolean) {
			return sp.getBoolean(key, (Boolean) defaultObject);
		} else if (defaultObject instanceof Float) {
			return sp.getFloat(key, (Float) defaultObject);
		} else if (defaultObject instanceof Long) {
			return sp.getLong(key, (Long) defaultObject);
		}

		return null;
	}

	/**
	 * the flag for clear data
	 */
	public enum SharedPreferenceType {
		LOCAL, PROCESS
	}

	/**
	 * clear all sharePreference data
	 *
	 * @param clearFlag
	 */
	public static void clear(Context mContext, SharedPreferenceType clearFlag) {
		SharedPreferences sp = null;
		switch (clearFlag) {
		case LOCAL:
			sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
					Activity.MODE_PRIVATE);
			break;
		case PROCESS:
			sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
					Activity.MODE_MULTI_PROCESS);
			break;
		}
		SharedPreferences.Editor editor = sp.edit();
		editor.clear().commit();
	}

	/**
	 * 根据
	 *
	 * @param clearFlag
	 * @return
	 */
	public static SharedPreferences getSharedPreferences(Context mContext,
			SharedPreferenceType clearFlag) {
		SharedPreferences sp = null;
		switch (clearFlag) {
		case LOCAL:
			sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
					Activity.MODE_PRIVATE);
			return sp;
		case PROCESS:
			sp = mContext.getSharedPreferences(PRE_NAME_TXZ,
					Activity.MODE_MULTI_PROCESS);
			return sp;
		default:
			return sp;
		}
	}

	/**
	 * 查询某个key是否已经存在
	 *
	 * @param key
	 * @return
	 */
	public static boolean contains(Context mContext, String key,
			SharedPreferenceType sharePreferenceType) {
		SharedPreferences sp = getSharedPreferences(mContext,
				sharePreferenceType);
		return sp.contains(key);
	}

	/**
	 * 返回所有的键值对
	 *
	 * @return
	 */
	public static Map<String, ?> getAll(Context mContext,
			SharedPreferenceType flag) {
		SharedPreferences sp = getSharedPreferences(mContext, flag);
		return sp.getAll();
	}

}
