package com.txznet.txz.module.config;

import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.ConfigUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.setting.ChangeCommandActivity;
import com.txznet.record.setting.MainActivity;
import com.txznet.record.ui.WinRecord;
import com.txznet.sdk.TXZConfigManager.ConfigJsonKey;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.nav.cld.NavCldImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.account.AccountManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.voiceprintrecognition.VoiceprintRecognitionManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.wakeup.WakeupPcmHelper;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.service.TXZService;
import com.txznet.txz.ui.win.help.WinHelpManager;
import com.txznet.txz.ui.win.nav.SearchEditManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.IntentUtil;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.recordcenter.RecordFile;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class ConfigManager extends IModule {
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
	

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_NOTIFY_RPT_DEVICE_STATUS);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_RESP_RPT_DEVICE_STATUS);
		regEvent(UiEvent.EVENT_NETWORK_CHANGE);

		return super.initialize_BeforeStartJni();
	}
	
	@Override
	public int initialize_AfterInitSuccess() {
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				loadConfigFromFile(GlobalContext.get().getApplicationInfo().dataDir
						+ "/data/nlp_config");
			}
		}, 0);
		return super.initialize_AfterInitSuccess();
	}

	private UiEquipment.ServerConfig m_pbServerConfig;
	
	public UiEquipment.ServerConfig getServerConfig() {
		return m_pbServerConfig;
	}
	
	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		switch (eventId) {
		case UiEvent.EVENT_ACTION_EQUIPMENT: {
			switch (subEventId) {
			case UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE: {
				try {
					m_pbServerConfig = UiEquipment.ServerConfig.parseFrom(data);
					if (m_pbServerConfig != null) {
						JNIHelper.logd("update server config: uint64Flags=" + m_pbServerConfig.uint64Flags);
						JNIHelper.logd("update server config: strServerEnvType=" + m_pbServerConfig.strServerEnvType);
						JNIHelper.logd("update server config: uint32TxzNlpLevel=" + m_pbServerConfig.uint32TxzNlpLevel);
						JNIHelper.logd("update server config: bTxzNlp=" + m_pbServerConfig.bTxzNlp);
						JNIHelper.logd("update server config: logInfo.recordTime=" + (m_pbServerConfig.msgLogInfo == null ? null:m_pbServerConfig.msgLogInfo.uint32VoiceRecordTime));
						JNIHelper.logd("update server config: b_data_partner = "+m_pbServerConfig.bDataPartner);
						
						if (m_pbServerConfig.uint64Flags != null){
							if ((m_pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_ENABLE_SLEEP_HEARTBEAT ) != 0){
								TXZPowerControl.bEnableSleepHeartHeat = true;
								TXZPowerControl.initSleepHeartBeatAlarm();
							}
						}
						
						
						if (m_pbServerConfig.msgSimInfo != null
								&& m_pbServerConfig.msgSimInfo.popMsg != null) {
							JNIHelper
									.logd("update server config: fltRecogRate="
											+ m_pbServerConfig.msgSimInfo.popMsg.fltRecogRate);
							JNIHelper
									.logd("update server config: uint32RecogDelay="
											+ m_pbServerConfig.msgSimInfo.popMsg.uint32RecogDelay);
							if (m_pbServerConfig.msgSimInfo.popMsg.fltRecogRate != null)
								SimManager.getInstance().mAsrPercent = m_pbServerConfig.msgSimInfo.popMsg.fltRecogRate;
							else
								SimManager.getInstance().mAsrPercent = -1;
							if (m_pbServerConfig.msgSimInfo.popMsg.uint32RecogDelay != null)
								SimManager.getInstance().mAsrDelay = m_pbServerConfig.msgSimInfo.popMsg.uint32RecogDelay;
							else
								SimManager.getInstance().mAsrDelay = -1;
						} else {
							SimManager.getInstance().mAsrPercent = -1;
							SimManager.getInstance().mAsrDelay = -1;
						}
						
						//账户系统配置项
						AccountManager.getInstance().onConfig(m_pbServerConfig.msgAccountInfo);
						if(m_pbServerConfig.msgEncryptKey != null && m_pbServerConfig.msgEncryptKey.strEncryptKey != null){
							RecordFile.setEncryptKey(new String(m_pbServerConfig.msgEncryptKey.strEncryptKey));
						}
						if(m_pbServerConfig.msgLogInfo != null && m_pbServerConfig.msgLogInfo.uint32VoiceRecordTime != null){
							WakeupPcmHelper.setRecordTime(m_pbServerConfig.msgLogInfo.uint32VoiceRecordTime);
						}
						if(m_pbServerConfig.locationFlag != null){
							LocationManager.setLocationFlag(m_pbServerConfig.locationFlag);
						}
						if(m_pbServerConfig.msgSimInfo != null){
							LogUtil.logd("update server config: uint32SimCarrier = "+m_pbServerConfig.msgSimInfo.uint32SimCarrier);
						}
						if (m_pbServerConfig.uint32TraceCollectTimeInterval != null) {
							// 定位时间间隔
							LocationManager.getInstance().setTimeInterval(m_pbServerConfig.uint32TraceCollectTimeInterval);
						}
						if (m_pbServerConfig.uint32TraceCollectDisInterval != null) {
							// 定位距离间隔
							LogUtil.logd("update server config: uint32TraceCollectDisInterval=" + m_pbServerConfig.uint32TraceCollectDisInterval);
						}

						if (m_pbServerConfig.voiceConfig != null && m_pbServerConfig.voiceConfig.bOnlineDisable != null) {
							LogUtil.logd("update server config: voiceConfig = " + m_pbServerConfig.voiceConfig.bOnlineDisable);
						}

						ProjectCfg.setVoiceConfig(m_pbServerConfig.voiceConfig);

						boolean lastUseUi2 = WinManager.getInstance().checkUseRecordWin2();
						boolean newUseUi2 = ProjectCfg.DEFAULT_USE_2_0;

						//强制关闭了ui2.0功能
						if (m_pbServerConfig.uint64Flags != null && (m_pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FALG_NOT_USR_SKIN ) != 0) {
							newUseUi2 = false;
						}

						PreferenceUtil.getInstance().setBoolean(PreferenceUtil.KEY_USE_UI_2_0, newUseUi2);
						VoiceprintRecognitionManager.getInstance().initializeComponent();
						//如果新选中的用皮肤包跟当前的不一致，需要重启来更新ui
						if (lastUseUi2 != newUseUi2) {
							if (WinManager.getInstance().checkUseRecordWin2() != lastUseUi2) {
								AppLogic.runOnUiGround(new Runnable2<Boolean, Boolean>(lastUseUi2, newUseUi2) {
									@Override
									public void run() {
										JNIHelper.loge("restart process: last use ui2.0 flag = " + mP1 + " ; new use ui2.0 flag = " + mP2);
										AppLogic.restartProcess();
									}
								}, 3000);
							}
						}

					}
				} catch (InvalidProtocolBufferNanoException e) {
				}
				break;
			}
			case com.txz.ui.equipment.UiEquipment.SUBEVENT_NOTIFY_RPT_DEVICE_STATUS: {
				try {
					PushManager.PushCmd_NotifyRptDeviceStatus rptDeviceStatus = PushManager.PushCmd_NotifyRptDeviceStatus.parseFrom(data);
					if (rptDeviceStatus != null && rptDeviceStatus.uint32Type != null) {
						if (rptDeviceStatus.uint32Type == PushManager.DEVICE_SLEEP_STATUS) {
							if (TXZPowerControl.hasReleased()) {
								ConfigManager.getInstance().reportDeviceStatus(ConfigManager.REPORT_NAME_SLEEP,ConfigManager.REPORT_ACTION_SLEEP_ENTER, NativeData.getServerTime());
							} else {
								ConfigManager.getInstance().reportDeviceStatus(ConfigManager.REPORT_NAME_SLEEP,ConfigManager.REPORT_ACTION_SLEEP_QUIT, NativeData.getServerTime());
							}
						}
					}
				} catch (InvalidProtocolBufferNanoException e) {
					e.printStackTrace();
				}
				break;
			}
			case com.txz.ui.equipment.UiEquipment.SUBEVENT_RESP_RPT_DEVICE_STATUS: {
				//上报设备状态回包
				break;
			}

			default: {
				break;
			}
			}

			break;
		}
		case UiEvent.EVENT_NETWORK_CHANGE:
			if (NetworkManager.getInstance().hasNet()) {
				processReportDeviceStatus(DELAY_NORMAL_PROCESS);
			}
			break;
		default: {
			break;
		}
		}
		return super.onEvent(eventId, subEventId, data);
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
			String[] wakeupKeywordsSdk = UserConf.getInstance().getFactoryConfigData().mWakeupWords;
			String[] wakeupKeywordsUser = WakeupManager.getInstance().getWakeupKeywords_User();
			String[] wakeupKeywordsSetting = UserConf.getInstance().getUserConfigData().mWakeupWords;
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
				
				if (wakeupKeywordsSetting != null) {
					for (int i = 0; i < wakeupKeywordsSetting.length; i++) {
						jKeywords.put(wakeupKeywordsSetting[i]);
					}
				}
				
				data.put("wakeupKeywords", jKeywords);
				data.put("wakeupSound", wakeupSound);
				data.put("floatTool", TXZService.getFloatToolType());
				data.put("coverDefaultKeywords", coverDefaultKeywords);
				JNIHelper.logd("notifyRemoteSync : " + data.toString());
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
	public boolean mCloseRecorderWin = true; // 命中远程命令字后是否关闭界面

	public byte[] invokeTXZConfig(String packageName, String command, byte[] data) {
		if (command.equals("requestSync")) {
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
//				Boolean needMap = jb.getVal(ConfigJsonKey.needPoiMap.name(), Boolean.class);
//				Boolean needAutoMusic = jb.getVal(ConfigJsonKey.autoPlayKuwo.name(), Boolean.class);
				Float asrThreshold = jb.getVal(ConfigJsonKey.asrThreshold.name(), Float.class);
				Boolean changeGpsStyle = jb.getVal(ConfigJsonKey.changeGpsStyle.name(), Boolean.class);
				Boolean needResetWav = jb.getVal(ConfigJsonKey.needResetWav.name(), Boolean.class);
				Boolean showOnWindowManager = jb.getVal(ConfigJsonKey.showOnWindowManager.name(), Boolean.class);
				Integer wmType = jb.getVal(ConfigJsonKey.wmType.name(), Integer.class);
//				if (needMap != null) {
//					JNIHelper.logd("needMap:" + needMap);
//					VersionPreference.NEED_POI_MAP = needMap;
//				}
//				if (needAutoMusic != null) {
//					VersionPreference.AUTO_PLAY_KUWO = needAutoMusic;
//				}
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
					AppLogic.runOnUiGround(new Runnable1<Integer>(wmType) {
						@Override
						public void run() {
							WinRecord.getInstance().updateDialogType(mP1);
							SearchEditManager.getInstance().updateDialogType(mP1);
						}
					});
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
		} else if (command.equals("winRecord.fullScreen")) {
			if (data == null) {
				return null;
			}

			Boolean isFull = Boolean.parseBoolean(new String(data));
			if (isFull != null) {
				LogUtil.logd("setIsFullScreen :" + isFull);
				WinHelpManager.getInstance().setEnableFullScreen(isFull);
				if (WinManager.getInstance().isRecordWin2()) {
					AppLogic.runOnUiGround(new Runnable1<Boolean>(isFull) {
						
						@Override
						public void run() {
							if (TextUtils.isEmpty(WinManager.getInstance().getThirdImpl2())) {
								RecordWin2.getInstance().setIsFullSreenDialog(mP1);
							}else {
								ServiceManager.getInstance().sendInvoke(WinManager.getInstance().getThirdImpl2(), "win.record2.fullScreen",
										(mP1+"").getBytes(), null);
							}
							SearchEditManager.getInstance().setIsFullSreenDialog(mP1);
						}
					}, 0);
				}else {
					AppLogic.runOnUiGround(new Runnable1<Boolean>(isFull) {
						@Override
						public void run() {
							WinRecord.getInstance().setIsFullSreenDialog(mP1);
							SearchEditManager.getInstance().setIsFullSreenDialog(mP1);
						}
					}, 0);
				}
			}
		} else if (command.equals("winRecord.close")){
			if(data == null){
				return null;
			}
			Boolean isClose = Boolean.parseBoolean(new String(data));
			if (isClose != null) {
				mCloseRecorderWin = isClose;
				JNIHelper.logd("set WinRecord close:" + mCloseRecorderWin);
			}
		} else if (command.equals("enable.ticket")) {
			if (data == null) {
				return null;
			}
			Boolean enable = Boolean.parseBoolean(new String(data));
			if (enable != null) {
				enableQueryTicket = enable;
				JNIHelper.logd("enableQueryTicket:" + enableQueryTicket);
			}
		} else if (command.equals("enableRecording")) {
			if(data==null){
				return null;
			}
			boolean mEnableRecording = Boolean.parseBoolean(new String(data));
			RecordManager.getInstance().setEnableRecording(mEnableRecording);
		} else if(command.equals("setInterruptTips")){
			if(data == null){
				return null;
			}
			String text = new String(data);
			LogUtil.logd("setInterruptTips text = " + text);
			WakeupManager.getInstance().setInterruptTips(new String[]{text});
		} else if (command.equals("setInterruptTipsArr")) {
			if(data == null){
				return null;
			}
			String text = new String(data);
			try {
				JSONArray jsonArray  = new JSONArray(text);
				LogUtil.logd("setInterruptTips text = " + text);
				String[] arr = new String[jsonArray.length()];
				for (int i = 0; i < jsonArray.length(); i++) {
					arr[i] = jsonArray.optString(i);
				}
				WakeupManager.getInstance().setInterruptTips(arr);
			} catch (Exception e) {
				LogUtil.loge("parse interruptTipsArr error " + e.getLocalizedMessage());
			}

		} else if (command.equals("enableInterruptTips")) {
			try {
				Boolean enableInterruptTips = Boolean.parseBoolean(new String(data));
				if (enableInterruptTips != null) {
					RecorderWin.enableInterruptTips_SDK = enableInterruptTips;
				}
			} catch (Exception e) {
				LogUtil.loge("enableInterruptTips error " + e.getLocalizedMessage());
			}
		} else if (command.equals("setNeedHelpFloat")) {
			if (data == null) {
				return null;
			}
			HelpGuideManager.getInstance().setNeedHelpFloat(packageName, Boolean.parseBoolean(new String(data)));
		} else if (command.equals("isNeedGuideAnim")) {
			HelpGuideManager.getInstance().setNeedGuideAnimFlag(Boolean.parseBoolean(new String(data)));
		} else if (command.equals("isNeedBlockSearchTts")) {
			isNeedBlockSearchTts = Boolean.parseBoolean(new String(data));
			LogUtil.logd("isNeedBlockSearchTts:" + isNeedBlockSearchTts);
			NavManager.getInstance().bNeedSearchingTip = isNeedBlockSearchTts;
		} else if ("getEnableSelfMarkting".equals(command)){
			return getEnableSelfMarkting();
		} else if("getHasDefaultWelcomeMessage".equals(command)) {
			return hasDefaultWelcomeMessage();
		} else if("isVoiceprintRecognitionEnable".equals(command)) {
			return isVoiceprintRecognitionEnable();
		} else if ("getLauncherEnableRegister".equals(command)) {
			return getLauncherEnableRegister();
		} else if ("getLauncherEnableModifyVehicleInfo".equals(command)) {
			return getLauncherEnableModifyVehicleInfo();
		} else if("enableShowHelpQRCode".equals(command)){
			if (data == null) {
				return null;
			}
			WinHelpManager.getInstance().setEnableShowHelpQRCode(Boolean.parseBoolean(new String(data)));
		} else if("needSetIntentPackage".equals(command)){
			Boolean flag = Boolean.parseBoolean(new String(data));
			if(flag != null){
				IntentUtil.getInstance().setNeedSetPackage(flag);
			}
		} else if("needShowOfflinePromote".equals(command)){
			if (data == null) {
				return null;
			}
			OfflinePromoteManager.getInstance().setNeedShowOfflinePromote(Boolean.parseBoolean(new String(data)));
		} else if ("isSupportInterruptTips".equals(command)) {
			return RecorderWin.isSupportInterruptTips()?"true".getBytes() : "false".getBytes();
		}
		return null;
	}

	private byte[] isVoiceprintRecognitionEnable() {
		UiEquipment.ServerConfig serverConfig = ConfigManager.getInstance().getServerConfig();
		boolean enable =  serverConfig != null && serverConfig.uint64Flags != null && (serverConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_ENABLE_VOICE_PRINT) != 0;
		return enable ? "true".getBytes() : "false".getBytes();
	}

	private byte[] hasDefaultWelcomeMessage() {
		String welcomeMessage = NativeData.getResString("RS_VOICE_ASR_START_HINT");
		if (UserConf.getInstance().getUserConfigData().mDeviceWelcomeMsg == null) {
			if (TextUtils.isEmpty(welcomeMessage)) {
				return "false".getBytes();
			} else {
				return "true".getBytes();
			}
		} else if("".equals(UserConf.getInstance().getUserConfigData().mDeviceWelcomeMsg.trim())){
			return "false".getBytes();
		} else {
			return "true".getBytes();
		}
	}

	private byte[] getEnableSelfMarkting() {
		if(m_pbServerConfig != null && m_pbServerConfig.uint64Flags != null && ((m_pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_ENABLE_SELF_MARKETING) != 0)){
			return "true".getBytes();
		}
		return "false".getBytes();
	}

	/**
	 * 判断launcher是否要开启注册功能
	 */
	private byte[] getLauncherEnableRegister() {
		if (m_pbServerConfig != null && m_pbServerConfig.uint64Flags != null && ((m_pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_LAUNCHER_ENABLE_REGISTER) != 0)) {
			return "true".getBytes();
		}
		return "false".getBytes();
	}

	/**
	 * 判断launcher是否要开启修改车辆信息的功能
	 */
	private byte[] getLauncherEnableModifyVehicleInfo() {
		if (m_pbServerConfig != null && m_pbServerConfig.uint64Flags != null
				&& ((m_pbServerConfig.uint64Flags & UiEquipment.SERVER_CONFIG_FLAG_LAUNCHER_ENABLE_MODIFY_VEHICLE_INFO) != 0)) {
			return "true".getBytes();
		}
		return "false".getBytes();
	}

	public boolean isNeedBlockSearchTts = false;
	
	public boolean enableQueryTicket = false;
	
	public void loadConfigFromFile(String fileName) {
		try {
			Properties properties = new Properties();
			File file = new File(fileName);
			if (!file.exists())
				return;
			FileInputStream fis = new FileInputStream(file);
			properties.load(fis);
			fis.close();

			Set<Object> keySet = properties.keySet();
			HashMap<String, String> bRegexps = new HashMap<String, String>();
			HashMap<String, String> aRegexps = new HashMap<String, String>();
			HashSet<String> serverRegexps = new HashSet<String>();

			if (keySet.contains("CMD_WORD")) {
				CmdManager.getInstance().regCmdString(
						properties.getProperty("CMD_WORD").split("\\|"));
			}
			String key;
			for (Object object : keySet) {
				key = object.toString();
				if (key.startsWith("B_REGEXP_")) {
					int divide = properties.getProperty(key).indexOf("===");
					if (divide > 0) {
						bRegexps.put(
								properties.getProperty(key)
										.substring(0, divide), properties
										.getProperty(key).substring(divide + 3));
					}
				} else if (key.startsWith("A_REGEXP_")) {
					int divide = properties.getProperty(key).indexOf("===");
					if (divide > 0) {
						aRegexps.put(
								properties.getProperty(key)
										.substring(0, divide), properties
										.getProperty(key).substring(divide + 3));
					}
				} else if (key.startsWith("SERVER_REGEXP_")) {
					serverRegexps.add(properties.getProperty(key));
				}
			}

			if (bRegexps.size() > 0)
				TextResultHandle.getInstance().setBeforeRegexps(bRegexps);
			if (aRegexps.size() > 0)
				TextResultHandle.getInstance().setAfterRegexps(aRegexps);
			if (serverRegexps.size() > 0)
				TextResultHandle.getInstance().setServerRegexps(serverRegexps);
		} catch (Exception e) {
		}
	}

	public static final String REPORT_NAME_SLEEP = "sleep";
	public static final int REPORT_ACTION_SLEEP_QUIT = 0;
	public static final int REPORT_ACTION_SLEEP_ENTER = 1;

	public static final String REPORT_NAME_STATUS = "status";

	private static final long DELAY_NORMAL_PROCESS = 50;
	private static final long DELAY_NOT_LOGIN = 10000;
	private final ArrayList<DeviceStatus> mDeviceStatuses = new ArrayList<DeviceStatus>();

	class DeviceStatus {
		String mName;
		int mStatus;
		int mTime;

		public DeviceStatus(String mName, int mStatus, int mTime) {
			this.mName = mName;
			this.mStatus = mStatus;
			this.mTime = mTime;
		}
	}

	/**
	 * status参考
	 * {@link UiEquipment#DEVICE_STATUS_POWER_ON}
	 * {@link UiEquipment#DEVICE_STATUS_BEFORE_SLEEP}
	 * {@link UiEquipment#DEVICE_STATUS_SLEEP}
	 * {@link UiEquipment#DEVICE_STATUS_WAKEUP}
	 * {@link UiEquipment#DEVICE_STATUS_SHOCK_WAKEUP}
	 * {@link UiEquipment#DEVICE_STATUS_ENTER_REVERSE}
	 * {@link UiEquipment#DEVICE_STATUS_QUIT_REVERSE}
	 * {@link UiEquipment#DEVICE_STATUS_BEFORE_POWER_OFF}
	 * {@link UiEquipment#DEVICE_STATUS_POWER_OFF}
	 * @param status
	 */
	public synchronized void reportDeviceStatus(int status) {
		addReportTask(new DeviceStatus(REPORT_NAME_STATUS, status, NativeData.getServerTime()));
	}

	/**
	 *
	 * @param name
	 * @param status
	 * @param time
	 */
	public synchronized void reportDeviceStatus(String name, int status, int time){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("name",name);
		jsonBuilder.put("action",status);
		jsonBuilder.put("time",time);//s
		UiEquipment.Req_RptDeviceStatus req_rptDeviceStatus = new UiEquipment.Req_RptDeviceStatus();
		req_rptDeviceStatus.strJson = jsonBuilder.toBytes();
		LogUtil.logd("device status : " + jsonBuilder.toString());
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_RPT_DEVICE_STATUS, req_rptDeviceStatus);
	}

	private synchronized void addReportTask(DeviceStatus deviceStatus){
		mDeviceStatuses.add(deviceStatus);
		processReportDeviceStatus(DELAY_NORMAL_PROCESS);
	}

	private void processReportDeviceStatus(long delay) {
		AppLogic.removeBackGroundCallback(reportDeviceStatusRunnable);
		AppLogic.runOnBackGround(reportDeviceStatusRunnable, delay);
	}

	/**
	 * 上报设备状态的队列<br>
	 * 设备登陆的情况，延迟{@link #DELAY_NORMAL_PROCESS}ms上报下一条状态<br>
	 * 设备未登录的情况，延迟{@link #DELAY_NOT_LOGIN}ms进行检测<br>
	 * 没有网络的情况下直接停掉队列，等网络变化触发<br>
	 */
	private Runnable reportDeviceStatusRunnable = new Runnable() {
		@Override
		public void run() {
			synchronized (mDeviceStatuses) {
				if (mDeviceStatuses.size() == 0) {
					AppLogic.removeBackGroundCallback(this);
					return;
				}
				if (NetworkManager.getInstance().hasNet()) {
					if (isLogin()) {
						DeviceStatus deviceStatus = mDeviceStatuses.remove(0);
						if (deviceStatus != null) {
							reportDeviceStatus(deviceStatus.mName, deviceStatus.mStatus, deviceStatus.mTime);
						}
						processReportDeviceStatus(DELAY_NORMAL_PROCESS);
					} else {
						processReportDeviceStatus(DELAY_NOT_LOGIN);
					}
				} else {
					AppLogic.removeBackGroundCallback(this);
				}
			}
		}
	};


	/**
	 * 是否登录了后台，省去了判断网络的状态，在外部判断了
	 * @return
	 */
	public boolean isLogin(){
		boolean bRet = false;
		try {
			byte[] byteData = NativeData.getNativeData(UiData.DATA_ID_CHECK_LOGIN);
			bRet = Integer.parseInt(new String(byteData)) != 0;
		} catch (Exception e) {
		}
		return bRet;
	}

}