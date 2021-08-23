package com.txznet.comm.sp;

import java.util.Set;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

public class CommonSp {
	private SharedPreferences mSharePre;

	protected CommonSp(Context context, String spName) {
		mSharePre = context.getSharedPreferences(spName, Context.MODE_PRIVATE);
	}

	/* get and put String value */
	public void setValue(String key, String value) {
		mSharePre.edit().putString(key, value).commit();
	}

	public String getValue(String key, String defValue) {
		return mSharePre.getString(key, defValue);
	}

	/* get and put boolean value */
	public void setValue(String key, boolean value) {
		mSharePre.edit().putBoolean(key, value).commit();
	}

	public boolean getValue(String key, boolean defValue) {
		return mSharePre.getBoolean(key, defValue);
	}

	/* get and put int value */
	public void setValue(String key, int value) {
		mSharePre.edit().putInt(key, value).commit();
	}

	public int getValue(String key, int defValue) {
		return mSharePre.getInt(key, defValue);
	}

	/* get and put int value */
	public void setValue(String key, float value) {
		mSharePre.edit().putFloat(key, value).commit();
	}

	public float getValue(String key, float defValue) {
		return mSharePre.getFloat(key, defValue);
	}

	/* get and put long value */
	public void setValue(String key, long value) {
		mSharePre.edit().putLong(key, value).commit();
	}

	public long getValue(String key, long defValue) {
		return mSharePre.getLong(key, defValue);
	}

	/* get and put string set value */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public void setValue(String key, Set<String> value) {
		mSharePre.edit().putStringSet(key, value).commit();
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public Set<String> getValue(String key, Set<String> defValue) {
		return mSharePre.getStringSet(key, defValue);
	}

	public void registerSpChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		mSharePre.registerOnSharedPreferenceChangeListener(listener);
	}

	public void unregisterSpChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
		mSharePre.unregisterOnSharedPreferenceChangeListener(listener);
	}
	
	public void clear(){
		mSharePre.edit().clear().commit();
	}
}
