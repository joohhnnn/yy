package com.txznet.txz.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Base64;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class PreferenceUtil {
	private static final String NAME = "txz";

	// sdcard路径
	private static final String SDCARD_PATH = "sdcard_path";
	public static final String KEY_USER_WAKEUP_KEYWORDS = "USER_WAKEUP_KEYWORDS";
	public static final String KEY_VOICE_STYLE = "VOICE_STYLE";
	public static final String KEY_WIFI_MAC = "WIFI_MAC";
	public static final String KEY_GSENSOR_COLLECT_TIME = "KEY_GSENSOR_COLLECT_TIME";
	public static final String KEY_GSENSOR_REPORT_MAX_COUNT = "KEY_GSENSOR_REPORT_MAX_COUNT";
	public static final String KEY_GSENSOR_SAVE_MAX_COUNT = "KEY_GSENSOR_SAVE_MAX_COUNT";
	public static final String KEY_NAVHISTORY_SYNC_NO = "KEY_NAVHISTORY_SYNC_NO";
	public static final String KEY_DEFAULT_NAV_TOOL = "KEY_DEFAULT_NAV_TOOL";
	public static final String KEY_INTERRUPT_TIPS_COUNT = "KEY_INTERRUPT_TIPS_COUNT";
	public static final String KEY_FLOW_EMPTY_FLAG = "KEY_FLOW_EMPTY_FLAG";
	public static final String KEY_SYNC_HOME_ADDRESS = "KEY_SYNC_HOME_ADDRESS";
	public static final String KEY_SYNC_COMPANY_ADDRESS = "KEY_SYNC_COMPANY_ADDRESS";
	public static final String KEY_TRAFFIC_NOTIFY_TOAST = "KEY_TRAFFIC_NOTIFY_TOAST";
	public static final String KEY_OPEN_CLOSE_NEWSPAPER = "KEY_OPEN_CLOSE_NEWSPAPER";
	public static final String KEY_NEED_PLAY_GUIDE_ANIM = "KEY_NEED_PLAY_GUIDE_ANIM";
	public static final String KEY_WAKEUP_COUNT = "KEY_WAKEUP_COUNT";
	public static final String KEY_OPEN_NAV_COUNT = "KEY_OPEN_NAV_COUNT";
	public static final String KEY_OPEN_MUSIC_COUNT = "KEY_OPEN_MUSIC_COUNT";
	public static final String KEY_LICENSE_ACTIVE_TIME = "KEY_LICENSE_ACTIVE_TIME";
	public static final String KEY_LAST_USE_TIME = "KEY_LAST_USE_TIME";
	public static final String KEY_IS_FIRST_START = "KEY_IS_FIRST_START";
	public static final String KEY_SIM_AUTH_CANCEL_TIME = "KEY_SIM_AUTH_CANCEL_TIME";
	public static final String KEY_SIM_AUTH_CANCEL_LIMIT = "KEY_SIM_AUTH_CANCEL_LIMIT";
	public static final String KEY_SIM_AUTH_SUCCESS_NUM = "KEY_SIM_AUTH_SUCCESS_NUM";//实名认证成功过
	public static final String KEY_VISUAL_UPGRADE_TASK_LIST = "KEY_VISUAL_UPGRADE_TASK_LIST"; // 可视化升级任务队列
	public static final String KEY_VISUAL_UPGRADE_CUR_PROCESS = "KEY_VISUAL_UPGRADE_CUR_PROCESS"; // 当前执行中可视化升级任务
	public static final String KEY_UPGRADE_TASK_LIST = "KEY_UPGRADE_TASK_LIST"; // 升级任务队列数据保存
	public static final String KEY_USE_UI_2_0 = "KEY_USE_UI_2_0"; // 使用ui2.0
	public static final String KEY_FANGDE_TOKEN = "KEY_FANGDE_TOKEN";
	public static final String KEY_HISTORICAL_LOCAL_ASR_ENGINE_TYPE = "KEY_HISTORY_LOCAL_ASR_ENGINE_TYPE";
	//////////////////////声纹识别///////////////////////
	public static final String KEY_VOICE_RECOGNITION_CORE_GROUP_ID = "KEY_VOICE_RECOGNITION_CORE_GROUP_ID";
	public static final String KEY_VOICE_RECOGNITION_ACCESS_TOKEN = "KEY_VOICE_RECOGNITION_ACCESS_TOKEN";
	public static final String KEY_VOICE_RECOGNITION_EXPIRED_TIME = "KEY_VOICE_RECOGNITION_EXPIRED_TIME";
	public static final String KEY_VOICE_RECOGNITION_HAD_SHOW_TIPS  = "KEY_VOICE_RECOGNITION_HAD_SHOW_TIPS";
	//////////////////////声纹识别///////////////////////

	//////////////////////离线促活///////////////////////
	public static final String KEY_OFFLINE_CONTENT_SHOW_TIME = "KEY_OFFLINE_CONTENT_SHOW_TIME";//内容的展示时间
	public static final String KEY_DEVICE_ACTIVE_TIME = "KEY_DEVICE_ACTIVE_TIME";//设备激活的时间
	public static final String KEY_OFFLINE_FLOAT_WINDOW_SHOW_NUMBER = "KEY_OFFLINE_FLOAT_WINDOW_SHOW_NUMBER";//记录悬浮图标显示的次数
	public static final String KEY_OFFLINE_WX_IS_BIND = "KEY_OFFLINE_WX_IS_BIND";//保存上一次联网获取到的微信绑定状态
	public static final String KEY_OFFLINE_NEED_SHOW = "KEY_OFFLINE_NEED_SHOW";//配置是否需要展示(可以通过关注公众号删除或者适配)
	public static final String KEY_OFFLINE_INPUT_ERROR_NUMBER = "KEY_OFFLINE_INPUT_ERROR_NUMBER";//二维码输入的错误次数
	public static final String KEY_OFFLINE_INPUT_ERROR_TIME = "KEY_OFFLINE_INPUT_ERROR_TIME";//输入达到最大次数后，记录下时间
	public static final String KEY_OFFLINE_REQ_CONTENT = "KEY_OFFLINE_REQ_CONTENT";

	public static final String KEY_OFFLINE_RECENT_DAY_DATA = "KEY_OFFLINE_RECENT_DAY_DATA";//最近10天的联网状态
	public static final String KEY_OFFLINE_RECENT_TIME_DATA = "KEY_OFFLINE_RECENT_TIME_DATA";//最近30次运行的联网状态
	public static final String KEY_OFFLINE_UNINTERESTED_TIME = "KEY_OFFLINE_UNINTERESTED_TIME";//不感兴趣的时间
	public static final String KEY_OFFLINE_RECORD_RUNNING_TIME = "KEY_OFFLINE_RECORD_RUNNING_TIME";//记录软件已经运行了多长时间，方便清空条件后计算
	public static final String KEY_OFFLINE_RECORD_DRIVING_DISTANCE = "KEY_OFFLINE_RECORD_DRIVING_DISTANCE";//记录软件已经驾驶了多少路程，方便清空条件后计算
	public static final String KEY_OFFLINE_RECORD_BOOT_NUMBER = "KEY_OFFLINE_RECORD_BOOT_NUMBER";//记录开机次数
	public static final String KEY_OFFLINE_RECORD_AUTO_CORE_DISABLE_TIME = "KEY_OFFLINE_RECORD_AUTO_CORE_DISABLE_TIME";//记录不能显示时，当前的总运行时间


	//////////////////////离线促活///////////////////////

	public static final String KEY_TOTAL_RUNNING_TIME = "KEY_TOTAL_RUNNING_TIME";//运行的总时长
	public static final String KEY_CURRENT_RUNNING_TIME = "KEY_CURRENT_RUNNING_TIME";//本次开机运行的时长
	public static final String KEY_TOTAL_DRIVING_DISTANCE = "KEY_TOTAL_DRIVING_DISTANCE";//驾驶的距离
	public static final String KEY_CURRENT_DRIVING_DISTANCE = "KEY_CURRENT_DRIVING_DISTANCE";//驾驶的距离



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

	public long getTotalRunningTime() {
		return getLong(KEY_TOTAL_RUNNING_TIME, 0);
	}

	public void setTotalRunningTime(long time) {
		setLong(KEY_TOTAL_RUNNING_TIME, time);
	}

	public long getCurrentRunningTime() {
		return getLong(KEY_CURRENT_RUNNING_TIME, 0);
	}

	public void setCurrentRunningTime(long time) {
		setLong(KEY_CURRENT_RUNNING_TIME, time);
	}

	public long getTotalDrivingDistance() {
		return getLong(KEY_TOTAL_DRIVING_DISTANCE, 0);
	}

	public void setTotalDrivingDistance(long distance) {
		setLong(KEY_TOTAL_DRIVING_DISTANCE, distance);
	}

	public long getCurrentDrivingDistance() {
		return getLong(KEY_CURRENT_DRIVING_DISTANCE, 0);
	}

	public void setCurrentDrivingDistance(long distance) {
		setLong(KEY_CURRENT_DRIVING_DISTANCE, distance);
	}

	public String getOfflineContentShowTime() {
		return getString(KEY_OFFLINE_CONTENT_SHOW_TIME, "");
	}

    public void setOfflineContentShowTime(String time) {
        setString(KEY_OFFLINE_CONTENT_SHOW_TIME, time);
    }

	public String getDeviceActiveTime(){
		return getString(KEY_DEVICE_ACTIVE_TIME,"");
	}

	public void setDeviceActiveTime(String time){
		setString(KEY_DEVICE_ACTIVE_TIME,time);
	}

	public int getOfflineFloatShowNumber() {
		return getInt(KEY_OFFLINE_FLOAT_WINDOW_SHOW_NUMBER, 3);
	}

	public void setOfflineFloatShowNumber(int number) {
		setInt(KEY_OFFLINE_FLOAT_WINDOW_SHOW_NUMBER, number);
	}

	public boolean getOfflineWXIsBind(){
		return getBoolean(KEY_OFFLINE_WX_IS_BIND,false);
	}

	public void setOfflineWXIsBind(boolean isBind){
		setBoolean(KEY_OFFLINE_WX_IS_BIND,isBind);
	}

	public long getOfflineUninterestedTime() {
		return getLong(KEY_OFFLINE_UNINTERESTED_TIME, 0);
	}

	public void setOfflineUninterestedTime(long time) {
		setLong(KEY_OFFLINE_UNINTERESTED_TIME, time);
	}

	public long getOfflineRecordRunningTime() {
		return getLong(KEY_OFFLINE_RECORD_RUNNING_TIME, 0);
	}

	public void setOfflineRecordRunningTime(long time) {
		setLong(KEY_OFFLINE_RECORD_RUNNING_TIME, time);
	}

	public long getOfflineRecordDrivingDistance() {
		return getLong(KEY_OFFLINE_RECORD_DRIVING_DISTANCE, 0);
	}

	public void setOfflineRecordDrivingDistance(long distance) {
		setLong(KEY_OFFLINE_RECORD_DRIVING_DISTANCE, distance);
	}

	public int getOfflineRecordBootNumber() {
		return getInt(KEY_OFFLINE_RECORD_BOOT_NUMBER, 0);
	}

	public void setOfflineRecordBootNumber(int number) {
		setInt(KEY_OFFLINE_RECORD_BOOT_NUMBER, number);
	}

	public long getOfflineDisableRunnigTime(){
		return getLong(KEY_OFFLINE_RECORD_AUTO_CORE_DISABLE_TIME,0);
	}

	public void setOfflineDisableRunningTime(long time){
		setLong(KEY_OFFLINE_RECORD_AUTO_CORE_DISABLE_TIME,time);
	}

	public boolean getOfflineNeedShow(){
		return getBoolean(KEY_OFFLINE_NEED_SHOW,true);
	}

	public void setOfflineNeedShow(boolean needShow){
		setBoolean(KEY_OFFLINE_NEED_SHOW,needShow);
	}

	public int getOfflineInputErrorNumber() {
		return getInt(KEY_OFFLINE_INPUT_ERROR_NUMBER, 0);
	}

	public void setOfflineInputErrorNumber(int number) {
		setInt(KEY_OFFLINE_INPUT_ERROR_NUMBER, number);
	}

	public long getOfflineInputErrorTime() {
		return getLong(KEY_OFFLINE_INPUT_ERROR_TIME, 0);
	}

	public void setOfflineInputErrorTime(long time) {
		setLong(KEY_OFFLINE_INPUT_ERROR_TIME, time);
	}

	public String getOfflineReqContent(){
	    return getString(KEY_OFFLINE_REQ_CONTENT,"");
    }

    public void setOfflineReqContent(String content){
	    setString(KEY_OFFLINE_REQ_CONTENT,content);
    }

	public String getOfflineRecentDayData(){
		return getString(KEY_OFFLINE_RECENT_DAY_DATA,"");
	}

	public void setOfflineRecentDayData(String data){
		setString(KEY_OFFLINE_RECENT_DAY_DATA,data);
	}

	public String getOfflineRecentTimeData(){
		return getString(KEY_OFFLINE_RECENT_TIME_DATA,"");
	}

	public void setOfflineRecentTimeData(String data){
		setString(KEY_OFFLINE_RECENT_TIME_DATA,data);
	}

	public String getVoiceRecognitionCoreGroupId() {
		return getString(KEY_VOICE_RECOGNITION_CORE_GROUP_ID, "");
	}

	public void setVoiceRecognitionCoreGroupId(String groupId) {
		setString(KEY_VOICE_RECOGNITION_CORE_GROUP_ID, groupId);
	}

	public String getVoiceRecognitionAccessToken() {
		return getString(KEY_VOICE_RECOGNITION_ACCESS_TOKEN, "");
	}

	public void setVoiceRecognitionAccessToken(String accessToken) {
		setString(KEY_VOICE_RECOGNITION_ACCESS_TOKEN, accessToken);
	}

	public Long getVoiceRecognitionExpiredTime() {
		return getLong(KEY_VOICE_RECOGNITION_EXPIRED_TIME, 0);
	}

	public void setVoiceRecognitionExpiredTime(long expiredTime) {
		setLong(KEY_VOICE_RECOGNITION_EXPIRED_TIME, expiredTime);
	}

	public boolean getVoiceRecognitionHadShowTips() {
		return getBoolean(KEY_VOICE_RECOGNITION_HAD_SHOW_TIPS, false);
	}

	public void setVoiceRecognitionHadShowTips(boolean hadShowTips) {
		setBoolean(KEY_VOICE_RECOGNITION_HAD_SHOW_TIPS, hadShowTips);
	}
	
	public String getSDCardPath() {
		return getString(SDCARD_PATH, SDCardUtil.DEFAULT_SDCARD_PATH);
	}

	public void setSDCardPath(String path) {
		setString(SDCARD_PATH, path);
	}
	
	public void setDefaultNavTool(String navTool) {
		setString(KEY_DEFAULT_NAV_TOOL, navTool);
	}

	public String getDefaultNavTool() {
		return getString(KEY_DEFAULT_NAV_TOOL, "");
	}
	
	public void setNavHistorySyncNo(int syncNo) {
		setInt(KEY_NAVHISTORY_SYNC_NO, syncNo);
	}

	public int getNavHistorySyncNo() {
		return getInt(KEY_NAVHISTORY_SYNC_NO, 0);
	}
	
	public void setSyncHomeCompanyAddress(boolean isHome, NavigateInfo info) {
		String key = isHome ? KEY_SYNC_HOME_ADDRESS : KEY_SYNC_COMPANY_ADDRESS;
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("lat", info.msgGpsInfo.dblLat);
		jsonBuilder.put("lng", info.msgGpsInfo.dblLng);
		jsonBuilder.put("name", info.strTargetName);
		jsonBuilder.put("address", info.strTargetAddress);
		setString(key, jsonBuilder.toString());
	}
	
	public NavigateInfo getSyncHcAddress(boolean isHome) {
		String key = isHome ? KEY_SYNC_HOME_ADDRESS : KEY_SYNC_COMPANY_ADDRESS;
		String strData = getString(key, null);
		if (!TextUtils.isEmpty(strData)) {
			JSONBuilder jsonBuilder = new JSONBuilder(strData);
			NavigateInfo navigateInfo = new NavigateInfo();
			navigateInfo.msgGpsInfo = new GpsInfo();
			navigateInfo.msgGpsInfo.dblLat = jsonBuilder.getVal("lat", Double.class);
			navigateInfo.msgGpsInfo.dblLng = jsonBuilder.getVal("lng", Double.class);
			navigateInfo.strTargetName = jsonBuilder.getVal("name", String.class);
			navigateInfo.strTargetAddress = jsonBuilder.getVal("address", String.class);
			return navigateInfo;
		}
		return null;
	}
	
	public void clearSyncAddress(boolean isHome) {
		if (isHome) {
			setString(KEY_SYNC_HOME_ADDRESS, "");
		} else {
			setString(KEY_SYNC_COMPANY_ADDRESS, "");
		}
	}
	
	public void setTrafficToastCount(int count) {
		setInt(KEY_TRAFFIC_NOTIFY_TOAST, count);
	}
	
	public int getTrafficToastCount() {
		return getInt(KEY_TRAFFIC_NOTIFY_TOAST, 0);
	}
	
	public void setNavNewspaperable(boolean enable) {
		setBoolean(KEY_OPEN_CLOSE_NEWSPAPER, enable);
	}
	
	public boolean isEnableNavNewspaperable() {
		return getBoolean(KEY_OPEN_CLOSE_NEWSPAPER, true);
	}

	/**
	 * 当时间为0时，表示需要引导
	 * 
	 * @param need
	 */
	public void setNeedPlayGuideAnim(long need) {
		setLong(KEY_NEED_PLAY_GUIDE_ANIM, need);
	}

	public long needPlayGuideAnim() {
		return getLong(KEY_NEED_PLAY_GUIDE_ANIM, 0);
	}
	
	public void setWakeupCount(int count) {
		setInt(KEY_WAKEUP_COUNT, count);
	}
	
	// 最多纪录3次，超过3次后将不再纪录
	public int getWakeupCount() {
		return getInt(KEY_WAKEUP_COUNT, 0);
	}
	
	public void setOpenNavCount(int count) {
		setInt(KEY_OPEN_NAV_COUNT, count);
	}

	// 最多纪录3次
	public int getOpenNavCount() {
		return getInt(KEY_OPEN_NAV_COUNT, 0);
	}
	
	public void setOpenMusicCount(int count){
		setInt(KEY_OPEN_MUSIC_COUNT, count);
	}

	// 最多纪录3次
	public int getOpenMusicCount() {
		return getInt(KEY_OPEN_MUSIC_COUNT, 0);
	}
	
	public void setActiveTime(String time) {
		setString(KEY_LICENSE_ACTIVE_TIME, time);
	}

	public String getActiveTime() {
		return getString(KEY_LICENSE_ACTIVE_TIME, "");
	}

	public void setLastUseTime(long time) {
		setLong(KEY_LAST_USE_TIME, time);
	}

	public long getLastUseTime() {
		return getLong(KEY_LAST_USE_TIME, 0);
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
	
	public void setInt(String key, int value) {
		editor.putInt(key, value);
		editor.commit();
	}
	
	public int getInt(String key, int defValue) {
		return preferences.getInt(key, defValue);
	}
	
	public void setLong(String key, long value) {
		editor.putLong(key, value);
		editor.commit();
	}
	
	public long getLong(String key, long defValue) {
		return preferences.getLong(key, defValue);
	}
	
	public void setBoolean(String key, boolean value) {
		editor.putBoolean(key, value);
		editor.commit();
	}
	
	public boolean getBoolean(String key, boolean defVal) {
		return preferences.getBoolean(key, defVal);
	}

	/**
	 * 用于保存集合
	 *
	 * @param key key
	 * @param map map数据
	 * @return 保存结果
	 */
	public <K, V> boolean putHashMapData(String key, Map<K, V> map) {
		boolean result;
		try {
			Gson gson = new Gson();
			String json = gson.toJson(map);
			editor.putString(key, json);
			result = true;
		} catch (Exception e) {
			result = false;
			e.printStackTrace();
		}
		editor.commit();
		return result;
	}

	/**
	 * 用于保存集合
	 *
	 * @param key key
	 * @return HashMap
	 */
	public <V> HashMap<String, V> getHashMapData(String key, Class<V> clsV) {
		String json = preferences.getString(key, "");
		HashMap<String, V> map = new HashMap<String, V>();
		Gson gson = new Gson();
		try {
			JsonObject obj = new com.google.gson.JsonParser().parse(json).getAsJsonObject();
			Set<Map.Entry<String, JsonElement>> entrySet = obj.entrySet();
			for (Map.Entry<String, JsonElement> entry : entrySet) {
				String entryKey = entry.getKey();
				LogUtil.loge(entryKey);
				JsonObject value = (JsonObject) entry.getValue();
				LogUtil.loge(value.toString());
				map.put(entryKey, gson.fromJson(value, clsV));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return map;
	}
}
