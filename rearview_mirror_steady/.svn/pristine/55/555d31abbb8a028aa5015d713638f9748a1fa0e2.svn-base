package com.txznet.comm.remote.util;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZTtsManager;

public class ConfigUtil {

	static Boolean showHelpInfos = null; // 是否显示帮助图标
	static Boolean showSettings = null; // 是否打开设置入口
	static Boolean showCloseIcon = false; // 是否显示关闭图标
	static Boolean showHelpNewTag = null;//是否显示有新的帮助信息

	static Boolean mShowCloseIcon = showCloseIcon;//外部更新的状态

	public static boolean isShowHelpInfos() {
		return showHelpInfos == null || showHelpInfos == true;
	}

	public static boolean isShowSettings() {
		return showSettings != null && showSettings == true;
	}
	
	public static boolean isShowCloseIcon(){
		return showCloseIcon != null && showCloseIcon;
	}
	
	public static boolean isCoverDefaultKeywords() {
		return coverDefaultKeywords==null || coverDefaultKeywords==true;
	}
	
	public static boolean isShowHelpNewTag(){
		return showHelpNewTag !=null && showHelpNewTag;
	}

	private static JSONObject defaultConfig = null;

	public static void setDefaultConfig(JSONObject mDefaultDoc) {
		ConfigUtil.defaultConfig = mDefaultDoc;
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.config.default.set", null, null);
	}

	public static void setShowHelpInfos(boolean showHelpInfos) {
		ConfigUtil.showHelpInfos = showHelpInfos;
		notifyIconStateChanged(IconStateChangeListener.TYPE_SHOW_HELP, showHelpInfos);
	}

	public static void setShowSettings(boolean showSettings) {
		ConfigUtil.showSettings = showSettings;
		notifyIconStateChanged(IconStateChangeListener.TYPE_SHOW_SETTINGS, showSettings);
	}
	
	public static void setShowCloseIcon(boolean showClose){
		ConfigUtil.mShowCloseIcon = showClose;
		ConfigUtil.showCloseIcon = showClose;
		notifyIconStateChanged(IconStateChangeListener.TYPE_SHOW_CLOSE, showClose);
		
	}

	public static void updateCloseIconState(boolean showClose) {
		ConfigUtil.showCloseIcon = showClose;
		notifyIconStateChanged(IconStateChangeListener.TYPE_SHOW_CLOSE, showClose);
	}

	public static void resetCloseIconState(){
		if (ConfigUtil.mShowCloseIcon != null) {
			setShowCloseIcon(mShowCloseIcon);
		} else {
			setShowCloseIcon(false);
		}
	}
	
	public static void setShowHelpNewTag(boolean showHelpNewTag){
		if (ConfigUtil.showHelpNewTag == null || ConfigUtil.showHelpNewTag != showHelpNewTag) {
			ConfigUtil.showHelpNewTag = showHelpNewTag;
			notifyIconStateChanged(IconStateChangeListener.TYPE_SHOW_HELP_NEW_TAG, showHelpNewTag);
		}
	}
	
	private static Boolean coverDefaultKeywords = null;
	public static void setCoverDefaultKeywords(boolean coverDefault){
		ConfigUtil.coverDefaultKeywords = coverDefault;
	}

	public static void sendConfigs() {
		if (showHelpInfos != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.config.showHelpInfos",
					("" + showHelpInfos).getBytes(), null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD,
					"comm.config.showHelpInfos",
					("" + showHelpInfos).getBytes(), null);
		}
		if (showSettings != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.config.showSettings", ("" + showSettings).getBytes(),
					null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD,
					"comm.config.showSettings", ("" + showSettings).getBytes(),
					null);
		}
		if (showCloseIcon != null) {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"comm.config.showCloseIcon", ("" + showCloseIcon).getBytes(),
					null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD,
					"comm.config.showCloseIcon", ("" + showCloseIcon).getBytes(),
					null);
		}
		if (defaultConfig != null) {
			setDefaultConfig(defaultConfig);
		}
	}
	
	public static interface IconStateChangeListener{
		public static int TYPE_SHOW_HELP = 1;
		public static int TYPE_SHOW_CLOSE = 2;
		public static int TYPE_SHOW_SETTINGS = 3;
		public static int TYPE_SHOW_HELP_NEW_TAG = 4;
		void onStateChanged(int type,boolean enable);
	}
	
	private static List<IconStateChangeListener> mIconStateChangeListeners = new ArrayList<ConfigUtil.IconStateChangeListener>(); 
	
	public static void registerIconStateChangeListener(IconStateChangeListener listener) {
		synchronized (mIconStateChangeListeners) {
			mIconStateChangeListeners.add(listener);
		}
	}
	
	public static void unregisterIconStateChangeListener(IconStateChangeListener listener) {
		synchronized (mIconStateChangeListeners) {
			mIconStateChangeListeners.remove(listener);
		}
	}
	
	public static void notifyIconStateChanged(int type,boolean enable) {
		synchronized (mIconStateChangeListeners) {
			for (int i = 0; i < mIconStateChangeListeners.size(); i++) {
				if (mIconStateChangeListeners.get(i) != null) {
					mIconStateChangeListeners.get(i).onStateChanged(type, enable);
				}
			}
		}
	}

	// 请求同步
	public static void requestSync() {
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.config.requestSync", null, null);
	}

	public static interface ConfigListener {
		void onConfigChanged(String data);
	}

	private static List<ConfigListener> mConfigListeners = new ArrayList<ConfigUtil.ConfigListener>();

	public static void registerConfigListener(ConfigListener listener) {
		synchronized (mConfigListeners) {
			mConfigListeners.add(listener);
		}
	}

	public static void unregisterConfigListener(ConfigListener listener) {
		synchronized (mConfigListeners) {
			mConfigListeners.remove(listener);
		}
	}

	static Float wakeupThreshold;
	static String[] wakeupKeywords;
	static Integer voiceSpeed;

	public static void setWakeupThreshold(Float wakeupThreshold) {
		ConfigUtil.wakeupThreshold = wakeupThreshold;
	}

	public static void setWakeupKeywords(String[] wakeupKeywords) {
		ConfigUtil.wakeupKeywords = wakeupKeywords;
	}

	public static void setVoiceSpeed(Integer voiceSpeed) {
		ConfigUtil.voiceSpeed = voiceSpeed;
	}

	public static Float getWakeupThreshold() {
		return wakeupThreshold;
	}

	public static String[] getWakeupKeywords() {
		return wakeupKeywords;
	}

	public static Integer getVoiceSpeed() {
		return voiceSpeed;
	}

	public static void notifyConfigChanged(final String data) {
		// 同步到本地
		boolean hasChanged = refreshConfig(data);
		if(hasChanged){
			synchronized (mConfigListeners) {
				for (int i = 0; i < mConfigListeners.size(); i++) {
					mConfigListeners.get(i).onConfigChanged(data);
				}
			}
		}
	}
	
	private static Boolean mWakeupSound = true;
	
	public static Boolean isWakeUpSound(){
		return mWakeupSound;
	}
	
	public static void setWakeUpSound(boolean b){
		ConfigUtil.mWakeupSound = b;
	}
	
	private static String mfloatTool = "FLOAT_NORMAL";
	public static void setfloatTool(String floatTool){
		mfloatTool = floatTool;
	}
	
	public static String getFloatTool(){
		return mfloatTool;
	}
	
	private static String mLastSyncData;

	// 刷新配置参数，返回是否有改变
	private static boolean refreshConfig(String data) {
		boolean hasChanged = true;
		if(data != null && mLastSyncData != null &&  data.equals(mLastSyncData)){
			hasChanged = false;
		}
		// 同步到本地
		try {
			JSONObject doc = new JSONObject(data);
			if (doc.has("wakeupThreshold")) {
				wakeupThreshold = (float) doc.getDouble("wakeupThreshold");
			}
			if (doc.has("voiceSpeed")) {
				voiceSpeed = doc.getInt("voiceSpeed");
			}
			if (doc.has("wakeupKeywords")) {
				JSONArray jKeywords = doc.getJSONArray("wakeupKeywords");
				wakeupKeywords = new String[jKeywords.length()];
				for (int j = 0; j < jKeywords.length(); j++) {
					wakeupKeywords[j] = jKeywords.getString(j);
				}
			}
			if (doc.has("wakeupSound")){
				mWakeupSound = doc.getBoolean("wakeupSound");
			}
			if (doc.has("floatTool")){
				mfloatTool = doc.getString("floatTool");
			}
			if(doc.has("coverDefaultKeywords")){
				coverDefaultKeywords = doc.getBoolean("coverDefaultKeywords");
			}
		} catch (Exception e) {
		}
		mLastSyncData = data;
		return hasChanged;
	}
	
	// 通知还原到默认设置
	public static void notifyRestoreToDefault(){
		if(defaultConfig != null){
			Float wakeupThreshhold = getConfigWakeupThreshhold(defaultConfig);
			if(wakeupThreshhold != null){
				TXZConfigManager.getInstance().setWakeupThreshhold(wakeupThreshhold);
			}
			Integer speedVoice = getConfigSpeedVoice(defaultConfig);
			if(speedVoice != null){
				TXZTtsManager.getInstance().setVoiceSpeed(speedVoice);
			}
			String[]  wakeupKeywords = getConfigWakeupKeywords(defaultConfig);
			if(wakeupKeywords != null){
				TXZConfigManager.getInstance().setWakeupKeywordsNew(wakeupKeywords);
			}
			Boolean wakeupSound = getConfigWakeupSound(defaultConfig);
			if(wakeupSound != null){
				TXZConfigManager.getInstance().enableWakeup(wakeupSound);
			}
			Boolean coverDefaultKeywords = getConfigCoverDefaultKeywords(defaultConfig);
			if(coverDefaultKeywords!=null){
				TXZConfigManager.getInstance().enableCoverDefaultKeywords(coverDefaultKeywords);
			}
		}
	}
	
	public static final JSONObject DEFAULT_CONFIG = new JSONObject();
	static {
		try {
			DEFAULT_CONFIG.put("wakeupThreshold", -3.1f);
			DEFAULT_CONFIG.put("voiceSpeed", 70);
			// String[] wakeupKeywords = new String[] { "你好小踢" };
			// 重置配置文件时，唤醒词置为空
			String[] wakeupKeywords = new String[0];
			JSONArray array = new JSONArray();
			for (String str : wakeupKeywords) {
				array.put(str);
			}
			DEFAULT_CONFIG.put("wakeupKeywords", array);
			DEFAULT_CONFIG.put("wakeupSound", true);
			DEFAULT_CONFIG.put("floatTool", "FLOAT_TOP");
			DEFAULT_CONFIG.put("coverDefaultKeywords", true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	// ===== 解析JSONObject的帮助方法 =====
	
	public static Boolean getConfigCoverDefaultKeywords(JSONObject config){
		if(config.has("coverDefaultKeywords")){
			try {
				return config.getBoolean("coverDefaultKeywords");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static String getConfigFloatTool(JSONObject config){
		if(config.has("floatTool")){
			try {
				return config.getString("floatTool");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Float getConfigWakeupThreshhold(JSONObject config){
		if(config.has("wakeupThreshold")){
			try {
				return (float) config.getDouble("wakeupThreshold");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Integer getConfigSpeedVoice(JSONObject config){
		if(config.has("voiceSpeed")){
			try {
				return config.getInt("voiceSpeed");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public static Boolean getConfigWakeupSound(JSONObject config){
		if(config.has("wakeupSound")){
			try {
				return config.getBoolean("wakeupSound");
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	
	public static String[] getConfigWakeupKeywords(JSONObject config){
		if(config.has("wakeupKeywords")){
			try {
				JSONArray jWakeupKW = config.getJSONArray("wakeupKeywords");
				String[] wakeupKW = new String[jWakeupKW.length()];
				for (int i = 0; i < jWakeupKW.length(); i++) {
					wakeupKW[i] = jWakeupKW.getString(i);
				}
				return wakeupKW;
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
}
