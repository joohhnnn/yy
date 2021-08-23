/**
 * 
 */
package com.txznet.fm.bean;

import java.util.Locale;
import java.util.ResourceBundle;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.utils.StringUtils;

/**
 * 参数配置
 */
public class Configuration {
	
	public static final String TXZ_VERSION = "version";
	public static final String TXZ_Search_VERSION = "search_version";
	public static final String TXZ_Category_VERSION = "category_version";
	public static final String TXZ_Audio_VERSION = "audio_version";
	public static final String TXZ_SKIN = "skin";// 皮肤
	public static final String TXZ_TEST = "isTest";// 皮肤
	public static final String TXZ_WIDGET = "widget";

	private static ResourceBundle resourceBundle;

	private static final Configuration INSTANCE = new Configuration();
	private static final String TAG = "[MUSIC][Config]";

	private Configuration() {
		try {
			resourceBundle = ResourceBundle.getBundle("TXZAudio", Locale.getDefault());
		} catch (Exception e) {
			LogUtil.loge(TAG+"[error]config" ,e);
		}
	}

	public static Configuration getInstance() {
		return INSTANCE;
	}

	public String getString(String key) {
		return resourceBundle.getString(key);
	}

	public int getInteger(String key) {
		Object number = getObject(key);
		if (number instanceof Integer) {
			return (Integer) number;
		}
		if (number instanceof String) {
			String s = (String) number;
			if (StringUtils.isNumeric(s)) {
				return Integer.parseInt(s);
			}
		}
		return 0;
	}
	public boolean getBoolean(String key) {
		String  isTest=getString(key);
		try {
			return Boolean.parseBoolean(isTest);
		} catch (Exception e) {
			return false;
		}
	}

	public Object getObject(String key) {
		return resourceBundle.getObject(key);
	}
}
