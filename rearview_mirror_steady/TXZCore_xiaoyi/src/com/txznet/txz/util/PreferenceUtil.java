package com.txznet.txz.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Base64;

import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;

public class PreferenceUtil {
	private static final String NAME = "txz";

	// sdcard路径
	private static final String SDCARD_PATH = "sdcard_path";
	public static final String KEY_USER_WAKEUP_KEYWORDS = "USER_WAKEUP_KEYWORDS";
	public static final String KEY_VOICE_STYLE = "VOICE_STYLE";
	public static final String KEY_WIFI_MAC = "WIFI_MAC";

	private static PreferenceUtil mInstance = null;
	private SharedPreferences preferences = null;
	private Editor editor = null;
	private Context mContext;

	private PreferenceUtil() {
		mContext = GlobalContext.get();
		preferences = mContext.getSharedPreferences(NAME, Context.MODE_PRIVATE);
		editor = preferences.edit();
	}

	public static PreferenceUtil getInstance() {
		if (mInstance == null)
			synchronized (PreferenceUtil.class) {
				if (mInstance == null) {
					mInstance = new PreferenceUtil();
				}
			}
		return mInstance;
	}
	
	public String getSDCardPath() {
		return getString(SDCARD_PATH, SDCardUtil.DEFAULT_SDCARD_PATH);
	}

	public void setSDCardPath(String path) {
		setString(SDCARD_PATH, path);
	}
	
	public LocationInfo getLocationInfo(){
//		LocationInfo location
		String sLocationInfoBase64 = getString("location_info", "null");
		if ( !sLocationInfoBase64.equals("null") )
		{
			try {
				return  LocationInfo.parseFrom(Base64.decode(sLocationInfoBase64, Base64.DEFAULT));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
	public void setLocationInfo(LocationInfo location){
		setString("location_info", Base64.encodeToString(MessageNano.toByteArray(location), Base64.DEFAULT));
	}
	
	public void setString(String key, String value) {
		editor.putString(key, value);
		editor.commit();
	}

	public String getString(String key, String defValue) {
		return preferences.getString(key, defValue);
	}
}
