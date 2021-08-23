package com.txznet.txz.module.config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.version.VersionPreference;
import com.txznet.record.setting.ChangeCommandActivity;
import com.txznet.record.setting.MainActivity;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZConfigManager.ConfigJsonKey;
import com.txznet.txz.component.nav.cld.NavCldImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.help.WinHelpDetail;
import com.txznet.txz.ui.win.help.WinHelpDetailTops;
import com.txznet.txz.ui.win.help.WinHelpTops;
import com.txznet.txz.ui.win.nav.SearchEditDialog;

import android.text.TextUtils;

public class ConfigManager {
	private Boolean mNeedCloseDelay = true;
	private static ConfigManager sInstance;

	private ConfigManager() {
	}

	public static ConfigManager getInstance() {
		if (sInstance == null) {
			synchronized (ConfigManager.class) {
				if (sInstance == null) {
					sInstance = new ConfigManager();
				}
			}
		}
		return sInstance;
	}

	public boolean needCloseDelay() {
		return mNeedCloseDelay;
	}

	// 要求同步数据
	public void notifyRemoteSync() {
		ServiceManager.getInstance().removeServiceThreadCallback(mRemoteSyncTask);
		ServiceManager.getInstance().runOnServiceThread(mRemoteSyncTask, 50);
	}

	private Runnable mRemoteSyncTask = new Runnable() {
		@Override
		public void run() {
			float wakeupThreshold = WakeupManager.getInstance().getWakeupThreshhold();
			String[] wakeupKeywordsSdk = WakeupManager.getInstance().getWakeupKeywords_Sdk();
			String[] wakeupKeywordsUser = WakeupManager.getInstance().getWakeupKeywords_User();
			int voiceSpeed = TtsManager.getInstance().getVoiceSpeed();
			boolean wakeupSound = WakeupManager.getInstance().mEnableWakeup;
			boolean coverDefaultKeywords = WakeupManager.getInstance().mEnableCoverDefaultKeywords;

			JSONObject data = new JSONObject();
			try {
				data.put("wakeupThreshold", wakeupThreshold);
				data.put("voiceSpeed", voiceSpeed);
				JSONArray jKeywords = new JSONArray();
				if (wakeupKeywordsSdk != null) {
					for (int i = 0; i < wakeupKeywordsSdk.length; i++) {
						jKeywords.put(wakeupKeywordsSdk[i]);
					}
				}
				if (wakeupKeywordsUser != null) {
					for (int i = 0; i < wakeupKeywordsUser.length; i++) {
						jKeywords.put(wakeupKeywordsUser[i]);
					}
				}
				data.put("wakeupKeywords", jKeywords);
				data.put("wakeupSound", wakeupSound);
				data.put("floatTool", TXZService.getFloatToolType());
				data.put("coverDefaultKeywords", coverDefaultKeywords);
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// 本地同步
			ConfigUtil.notifyConfigChanged(data.toString());
			ServiceManager.getInstance().broadInvoke("comm.config.syncData", data.toString().getBytes());
			// 如果安装了TXZReocrd，强制同步一份到TXZRecord
			if (PackageManager.getInstance().mInstalledRecord
					&& ServiceManager.getInstance().getService(ServiceManager.RECORD) == null) {
				ServiceManager.getInstance().sendInvoke(ServiceManager.RECORD, "comm.config.syncData",
						data.toString().getBytes(), null);
			}
		}
	};

	private String mRemoteDefault; // 设置了默认值的远程应用

	public byte[] invokeTXZConfig(String packageName, String command, byte[] data) {
		if (command.equals("end.close")) {
			try {
				mNeedCloseDelay = Boolean.parseBoolean(new String(data));
			} catch (Exception e) {
			}
		} else if (command.equals("requestSync")) {
			notifyRemoteSync();
		} else if (command.equals("default.set")) {
			mRemoteDefault = packageName;
		} else if (command.equals("restore")) {
			if (!TextUtils.isEmpty(mRemoteDefault)) {
				ServiceManager.getInstance().sendInvoke(mRemoteDefault, "comm.config.restore", null, null);
			} else {
				// 还原同行者的默认值
				Float wakeupThreshhold = ConfigUtil.getConfigWakeupThreshhold(ConfigUtil.DEFAULT_CONFIG);
				if (wakeupThreshhold != null) {
					WakeupManager.getInstance().setWakeupThreshhold(wakeupThreshhold);
				}
				Integer speedVoice = ConfigUtil.getConfigSpeedVoice(ConfigUtil.DEFAULT_CONFIG);
				if (speedVoice != null) {
					TtsManager.getInstance().setVoiceSpeed(speedVoice);
				}
				String[] wakeupKeywords = ConfigUtil.getConfigWakeupKeywords(ConfigUtil.DEFAULT_CONFIG);
				if (wakeupKeywords != null) {
					WakeupManager.getInstance().updateWakupKeywords_Sdk(wakeupKeywords);
				}
				Boolean wakeupSound = ConfigUtil.getConfigWakeupSound(ConfigUtil.DEFAULT_CONFIG);
				if (wakeupSound != null) {
					WakeupManager.getInstance().enableWakeup(wakeupSound);
				}
				String floatToolType = ConfigUtil.getConfigFloatTool(ConfigUtil.DEFAULT_CONFIG);
				if (floatToolType != null) {
					TXZService.setFloatToolType(floatToolType);
				}
			}
		} else if (command.equals("version.prefer")) {
			try {
				// TODO 后续可增加
				JSONBuilder jb = new JSONBuilder(new String(data));
				Boolean needMap = jb.getVal(ConfigJsonKey.needPoiMap.name(), Boolean.class);
				Boolean needAutoMusic = jb.getVal(ConfigJsonKey.autoPlayKuwo.name(), Boolean.class);
				Float asrThreshold = jb.getVal(ConfigJsonKey.asrThreshold.name(), Float.class);
				Boolean changeGpsStyle = jb.getVal(ConfigJsonKey.changeGpsStyle.name(), Boolean.class);
				Boolean needResetWav = jb.getVal(ConfigJsonKey.needResetWav.name(), Boolean.class);
				Boolean showOnWindowManager = jb.getVal(ConfigJsonKey.showOnWindowManager.name(), Boolean.class);
				Integer wmType = jb.getVal(ConfigJsonKey.wmType.name(), Integer.class);
				if (needMap != null) {
					JNIHelper.logd("needMap:" + needMap);
					VersionPreference.NEED_POI_MAP = needMap;
				}
				if (needAutoMusic != null) {
					VersionPreference.AUTO_PLAY_KUWO = needAutoMusic;
				}
				if (asrThreshold != null) {
					JNIHelper.logd("NavCldImpl AsrThresHold:" + asrThreshold);
					NavCldImpl.sAsrWakeupThresHold = asrThreshold;
				}
				if (changeGpsStyle != null) {
					LocationManager.sUseAndroidSysGps = changeGpsStyle;
				}
				if (needResetWav != null) {
					AsrManager.getInstance().setNeedStopWavAfterPlayTips(needResetWav);
				}
				if (showOnWindowManager != null) {
					JNIHelper.logd("USE_WINDOWMANAGER:" + showOnWindowManager);
				}
				if (wmType != null) {
					WinRecord.getInstance().updateDialogType(wmType);
					SearchEditDialog.getInstance().updateDialogType(wmType);
					WinHelpDetail.getInstance().updateDialogType(wmType);
					WinHelpDetailTops.getInstance().updateDialogType(wmType);
					WinHelpTops.getInstance().updateDialogType(wmType);
				}

				if (packageName.startsWith("com.zhonghong.")) {
					mNeedCloseDelay = false;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else if (command.equals("setStyleBindWithWakeupKeywords")) {
			try {
				JSONBuilder json = new JSONBuilder(data);
				WakeupManager.getInstance().mBindStyleWithWakeup = json.getVal("bind", Boolean.class, false);
			} catch (Exception e) {
			}
		} else if (command.equals("disableChangeWakeupKeywordsStyle")) {
			try {
				JSONBuilder json = new JSONBuilder(data);
				String[] ss = json.getVal("style", String[].class);
				for (String s : ss) {
					JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
							VoiceData.SUBEVENT_VOICE_DISABLE_CHANGE_WAKEUP_KEYWORDS_STYLE, s);
				}
			} catch (Exception e) {
			}
		} else if (command.equals("setting.hideOptions")) {
			if(data==null){
				return null;
			}
			int mHideOptions = Integer.parseInt(new String(data));
			MainActivity.hideOptions(mHideOptions);
		} else if (command.equals("setting.wkwordsEditable")) {
			if(data==null){
				return null;
			}
			Boolean mWkWordsEditable = Boolean.parseBoolean(new String(data));
			ChangeCommandActivity.enableWkWordsEditable(mWkWordsEditable);
		}
		return null;
	}
}