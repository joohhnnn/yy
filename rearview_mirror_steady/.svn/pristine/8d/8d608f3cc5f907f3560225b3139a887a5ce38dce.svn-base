package com.txznet.cldfm.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.txznet.comm.remote.GlobalContext;

public class FmPreferenceUtil {
	private static final String NAME = "fminfo";

	// 频率数值
	private static final String KEY_OF_FREQUENCY = "fminfo_frequency";
	// 关闭状态
	private static final String KEY_OF_OPEN_STATUS = "open_status";

	private static final float DEFAULT_FREQUENCY = 99.0f;

	private static FmPreferenceUtil mInstance = null;
	private SharedPreferences preferences = null;
	private Editor editor = null;
	private Context mContext;

	private FmPreferenceUtil() {
		mContext = GlobalContext.get();
		preferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		editor = preferences.edit();
	}

	public static FmPreferenceUtil getInstance() {
		if (mInstance == null)
			synchronized (FmPreferenceUtil.class) {
				if (mInstance == null) {
					mInstance = new FmPreferenceUtil();
				}
			}
		return mInstance;
	}

	public void setFrequency(float frequency) {
		setFloat(KEY_OF_FREQUENCY, frequency);
	}

	public float getFrequency() {
		return getFloat(KEY_OF_FREQUENCY, DEFAULT_FREQUENCY);
	}

	public void setFMOpenStatus(boolean isOpen) {
		setBoolean(KEY_OF_OPEN_STATUS, isOpen);
	}

	public boolean getFMOpenStatus() {
		return getBoolean(KEY_OF_OPEN_STATUS, false);
	}

	private void setFloat(String name, float value) {
		editor.putFloat(name, value);
		editor.commit();
	}

	private float getFloat(String name, float defValue) {
		return preferences.getFloat(name, defValue);
	}

	private void setInt(String name, int value) {
		editor.putInt(name, value);
		editor.commit();
	}

	private int getInt(String name, int defValue) {
		return preferences.getInt(name, defValue);
	}

	private void setBoolean(String name, boolean value) {
		editor.putBoolean(name, value);
		editor.commit();
	}

	private boolean getBoolean(String key, boolean defValue) {
		return preferences.getBoolean(key, defValue);
	}
}
