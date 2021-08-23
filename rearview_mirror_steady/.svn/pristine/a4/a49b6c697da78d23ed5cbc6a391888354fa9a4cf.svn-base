package com.txznet.txz.ui.win.help;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.txznet.comm.remote.GlobalContext;

import java.util.Set;

public class HelpPreferenceUtil {
	private static final String NAME = "help";
	public static final String KEY_SHOW_HELP_NEWS = "SHOW_HELP_NEWS";
	public static final String KEY_HELP_FILE_PATH = "HELP_FILE_PATH";
	public static final String KEY_HELP_DETAIL_NAME = "KEY_HELP_DETAIL_NAME";
	public static final String KEY_HELP_LAST_TIPS_TIME  = "KEY_HELP_LAST_TIPS_TIME";
	public static final String KEY_SHOW_HELP_TAG = "SHOW_HELP_TAG";
	public static final String KEY_QCORD_WX_BIND_URL = "KEY_QCORD_WX_BIND_URL";
	public static final String KEY_LAST_LAUNCH_TIME = "KEY_LAST_LAUNCH_TIME";
	public static final String KEY_LAUNCH_COUNT = "KEY_LAUNCH_COUNT";

	public static final String KEY_HIDE_HELP_TIPS = "KEY_HIDE_HELP_TIPS";
	public static final String KEY_NEED_HIDE_HELP_TIPS = "KEY_NEED_HIDE_HELP_TIPS";
	public static final String KEY_HIT_TO_TARGET_TIPS = "KEY_HIT_TO_TARGET_TIPS";
	public static final String KEY_HIT_NAV_TIPS = "win_help_daohang";
	public static final String KEY_HIT_WECHAT_TIPS = "win_help_wechat";
	public static final String KEY_HIT_MUSIC_TIPS = "win_help_music";
	public static final String KEY_HIT_RADIO_TIPS = "win_help_diantai";
	public static final String KEY_HIT_DIANHUA_TIPS = "win_help_dianhua";
	public static final String KEY_HIT_OTHER_TIPS = "win_help_other";
	public static final String KEY_HIT_CONTROL_TIPS = "win_help_control";
	public static final String KEY_HIT_CHAOLENGFAN_TIPS = "win_help_chaolengfan";
	public static final String KEY_HIGH_TIPS = "KEY_HIGH_TIPS";

	public static final String KEY_HELP_GUIDE_LAST_SHOW_TIME = "KEY_HELP_GUIDE_LAST_SHOW_TIME";//帮助引导界面上一次展示时间
	public static final String KEY_HELP_QRCODE_LAST_REQ_TIME = "KEY_HELP_QRCODE_LAST_REQ_TIME";//帮助界面上一次请求后台时间
	public static final String KEY_HELP_QRCODE_DATA = "KEY_HELP_QRCODE_DATA";//帮助界面二维码信息
	public static final String KEY_HELP_QRCODE_DATA_VERSION = "KEY_HELP_QRCODE_DATA_VERSION";//帮助界面二维码信息版本
	public static final String KEY_HELP_NEED_IMMEDIATELY_SHOW = "KEY_HELP_NEED_IMMEDIATELY_SHOW";//帮助界面引导是否需要立即显示
	public static final String KEY_HELP_QRCODE_IS_SHOW = "KEY_HELP_QRCODE_IS_SHOW";//帮助界面是否需要展示


	private static HelpPreferenceUtil mInstance = null;
	private SharedPreferences preferences = null;
	private Editor editor = null;
	private Context mContext;

	private HelpPreferenceUtil() {
		mContext = GlobalContext.get();
		preferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		editor = preferences.edit();
	}

	public static HelpPreferenceUtil getInstance() {
		if (mInstance == null)
			synchronized (HelpPreferenceUtil.class) {
				if (mInstance == null) {
					mInstance = new HelpPreferenceUtil();
				}
			}
		return mInstance;
	}
	
	public void setString(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}

	public String getString(String key, String defValue) {
		return preferences.getString(key, defValue);
	}
	
	public void setBoolean(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}

	public boolean getBoolean(String key, boolean defValue) {
		return preferences.getBoolean(key, defValue);
	}

	public long getLong(String key, long def) {
		return preferences.getLong(key,def);
	}

	public void setLong(String key, long value) {
		editor.putLong(key,value);
		editor.commit();
	}

	public void setStringSet(String key ,Set<String> values){
        editor.putStringSet(key,values);
        editor.commit();
	}

	public Set<String> getStringSet(String key,Set<String> def) {
		return preferences.getStringSet(key ,def);
	}

	public int getInt(String key,int def) {
		return preferences.getInt(key,def);
	}

	public void setInt(String key,int value) {
		editor.putInt(key,value);
		editor.commit();
	}

	public void remove(String key) {
		editor.remove(key);
		editor.commit();
	}
}

