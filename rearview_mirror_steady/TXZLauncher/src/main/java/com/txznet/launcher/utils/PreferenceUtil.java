package com.txznet.launcher.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.txznet.comm.remote.GlobalContext;

import java.util.Set;

/**
 * 操作名字为com.txznet.launcher.sp的sp文件的封装。
 * 也定义了一些会用到的key name，方便其他类调用。（这个没有限制说只能添加这些，我们还是可以添加其他的，不过建议还是别乱加）
 */
public class PreferenceUtil {
	private static final String NAME = "com.txznet.launcher.sp";
	public static final String KEY_WECHAT_USER_ID = "key_wechat_user_id";// 登录的微信账号的id
	public static final String KEY_WECHAT_USER_NICK = "key_wechat_user_nick";// 登录的微信的昵称
	public static final String KEY_WECHAT_QR_TIMEOUT = "key_wechat_qr_timeout";//微信扫码界面超时时间
	public static final String KEY_WECHAT_BIND_QR_TIMEOUT = "key_wechat_bind_qr_timeout";//远程控制绑定扫码界面超时时间
	public static final String KEY_SELECT_LIST_TIMEOUT = "key_select_list_timeout";//列表超时时间
	public static final String KEY_LAUNCH_TIME = "key_launch_time"; // 应用启动日期
	public static final String KEY_TODAY_NOTICE_STATE = "key_today_notice_state";//今日数据开关状态
	public static final String KEY_WELCOME_STATE = "key_welcome_state";//欢迎开关
	public static final String KEY_SMART_TRAFFIC_STATE = "key_smart_traffic_state";//路况早晚报开关

	public static final String KEY_WIFI_AP_NAME = "key_wifi_ap_name";//WIFI热点
	public static final String KEY_WIFI_AP_PSD = "key_wifi_ap_psd";//WIFI热点

	public static final String KEY_SETTINGS_VOLUME = "key_settings_volume";//设备音量

	// 安吉星账户信息
	public static final String KEY_ANJIXING_LOGIN = "key_anjixing_login";
	public static final String KEY_ANJIXING_ACC_NAME = "key_anjixing_acc_name"; // 账号名/昵称
	public static final String KEY_ANJIXING_ACC_BIRTHDAY = "key_anjixing_acc_birthday"; // 生日
	public static final String KEY_ANJIXING_ACC_VEHICLE_LICENSE = "key_anjixing_acc_vehicle_license"; // 车牌号

	public static final String KEY_ELAPSED_REAL_TIME = "elapsed_real_time"; // 从设备开机到APP启动的时间，SystemClock.elapsedRealtime

	// ota升级的缓存
	public static final String KEY_OTA_UPGRADE_VERSION="key_ota_upgrade_version";// 当前系统升级的版本
	public static final String KEY_OTA_UPGRADE_TIP_NEW_COUNT ="key_ota_upgrade_tip_new_count";// 当前系统提示有新版可以升级的次数
	public static final String KEY_OTA_UPGRADE_TIP_CONTINUE_COUNT="key_ota_upgrade_tip_continue_count";// 当前系统提示继续升级的次数
	public static final String KEY_OTA_UPGRADE_DOWNLOAD="key_ota_upgrade_download";// 当前系统升级是否执行过下载。针对是否下载过会有不同的文字提示

	private static PreferenceUtil mInstance = null;
	private SharedPreferences preferences = null;
	private Editor editor = null;
	private Context mContext;


	public static final long DEFAULT_WECHAT_QR_TIMEOUT = 2 * 60 * 1000;//2分钟
	public static final long DEFAULT_SELECT_LIST_TIMEOUT = 20 * 1000;//20s
	public static final long DEFAULT_WECHAT_BIND_QR_TIMEOUT = 2 * 60 * 1000;//2分钟

	public static final String DEFAULT_WIFI_AP_NAME = "OnStar";
	public static final String DEFAULT_WIFI_AP_PSD = "anjixing1";


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

