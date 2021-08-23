package com.txznet.txz.module.sys;

import android.text.TextUtils;

import com.spreada.utils.chinese.ZHConverter;
import com.txz.ui.app.UiApp.AppInfo;
import com.txz.ui.app.UiApp.AppInfoList;
import com.txz.ui.carcontrol.CarControlData;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;

public class SysTool {
	private static final String CMD_HANDLE_SCREEN = "handle_screen";
	private static final String CMD_BACKNAVI = "backNavi";
	private static final String CMD_BACKHOME = "backHome";
	private static final String CMD_GOTOSLEEP = "goToSleep";

	public static final String CUSTOM_CMD_OPEN_SCREEN = "CUSTOM_CMD_OPEN_SCREEN";
	public static final String CUSTOM_CMD_CLOSE_SCREEN = "CUSTOM_CMD_CLOSE_SCREEN";
	public static final String CUSTOM_CMD_BACK_HOME = "CUSTOM_CMD_BACK_HOME";
	public static final String CUSTOM_CMD_BACK_NAVI = "CUSTOM_CMD_BACK_NAVI";
	
	public static final String VOLUME = "tool.volume";
	public static final String WAKE_LOCK = "tool.wakelock";
	public static final String APP_MGR = "tool.appmgr";
	public static final String SCREEN_SLEEP = "tool.screensleep";
	public static final String MUTEALL = "tool.muteall";
	public static final String SCREEN_LIGHT = "tool.light";
	public static final String COMMCMDS = "tool.ccw";
	private static String mRemoteVolumeToolImpl = null;
	private static String mRemoteWakeLockToolImpl = null;
	private static String mRemoteAppMgrToolImpl = null;
	private static String mRemoteScreenSleepImpl = null;
	private static String mRemoteScreenLightImpl = null;
	private static String mRemoteMuteAllToolImpl = null;
	private static String mRemoteCommCmdsToolImpl = null;

	private SysTool() {
	}

	public static boolean hasRemoteVolumeTool() {
		return !TextUtils.isEmpty(mRemoteVolumeToolImpl);
	}

	public static boolean hasRemoteWakeLockTool() {
		return !TextUtils.isEmpty(mRemoteWakeLockToolImpl);
	}

	public static boolean hasRemoteAppMgrTool() {
		return !TextUtils.isEmpty(mRemoteAppMgrToolImpl);
	}

	public static boolean hasRemoteScreenSleep() {
		return !TextUtils.isEmpty(mRemoteScreenSleepImpl);
	}

	public static boolean hasRemoteScreenLightTool() {
		return !TextUtils.isEmpty(mRemoteScreenLightImpl);
	}

	public static String getRemoteVolumeTool(){
		return mRemoteVolumeToolImpl;
	}
	
	public static String getRemoteScreenLightTool(){
		return mRemoteScreenLightImpl;
	}
	
	public static boolean procByRemoteTool(String tool, String command,
			Object... data) {
		String remoteService = null;
		String remoteCmd = null;
		byte[] remoteData = null;
		if (tool.equals(VOLUME) && !TextUtils.isEmpty(mRemoteVolumeToolImpl)) {
			remoteService = mRemoteVolumeToolImpl;
			remoteCmd = tool + "." + command;
			if (command.equals("mute")) {
				if (data != null && data.length > 0) {
					JSONBuilder doc = new JSONBuilder();
					doc.put("enable", data[0]);
					remoteData = doc.toBytes();
				}
			} else if (command.equals("setVolume")) {
				if (data != null && data.length > 0) {
					JSONBuilder doc = new JSONBuilder();
					doc.put("data", data[0]);
					remoteData = doc.toBytes();
				}
			} else if (command.equals("decVolume")) {
				if (data != null && data.length > 0) {
					JSONBuilder doc = new JSONBuilder();
					doc.put("data", data[0]);
					remoteData = doc.toBytes();
				}
			}else if (command.equals("incVolume")) {
				if (data != null && data.length > 0) {
					JSONBuilder doc = new JSONBuilder();
					doc.put("data", data[0]);
					remoteData = doc.toBytes();
				}
			}
		} else if (tool.equals(WAKE_LOCK)
				&& !TextUtils.isEmpty(mRemoteWakeLockToolImpl)) {
			remoteService = mRemoteWakeLockToolImpl;
			remoteCmd = tool + "." + command;
		} else if (tool.equals(APP_MGR)
				&& !TextUtils.isEmpty(mRemoteAppMgrToolImpl)) {
			remoteService = mRemoteAppMgrToolImpl;
			remoteCmd = tool + "." + command;
			if (command.equals("closeApp") || command.equals("openApp")) {
				if (data != null && data.length > 0) {
					JSONBuilder doc = new JSONBuilder();
					doc.put("pkgName", data[0]);
					remoteData = doc.toBytes();
				}
			}
		} else if (tool.equals(SCREEN_SLEEP)
				&& !TextUtils.isEmpty(mRemoteScreenSleepImpl)) {
			remoteService = mRemoteScreenSleepImpl;
			remoteCmd = tool + "." + command;
		} else if (tool.equals(MUTEALL)
				&& !TextUtils.isEmpty(mRemoteMuteAllToolImpl)) {
			remoteService = mRemoteMuteAllToolImpl;
			remoteCmd = tool + "." + command;
		} else if (tool.equals(SCREEN_LIGHT)
				&& !TextUtils.isEmpty(mRemoteScreenLightImpl)) {
			remoteService = mRemoteScreenLightImpl;
			remoteCmd = tool + "." + command;
		}
		if (remoteService != null) {
			ServiceManager.getInstance().sendInvoke(remoteService, remoteCmd,
					remoteData, null);
			JNIHelper.logd("core:music:remoteSysTool:"+remoteService+"/"+remoteCmd);
			ReportUtil.doReport(new ReportUtil.Report.Builder().setType("procTool")
					.putExtra("cmd", remoteCmd).setSessionId().buildCommReport());
			return true;
		} else {
			JNIHelper.logd("core:music:remoteSysTool:none");
			return false;
		}
	}

	public static boolean procByRemoteToolSync(String tool, String command,
			byte[] data) {
		String remoteService = null;
		String remoteCmd = null;
		byte[] remoteData = null;
		if (tool.equals(COMMCMDS)
				&& !TextUtils.isEmpty(mRemoteCommCmdsToolImpl)) {
			remoteService = mRemoteCommCmdsToolImpl;
			remoteCmd = tool + "." + command;
			remoteData = data;
		}
		if (remoteService != null) {
			ServiceData sd = ServiceManager.getInstance().sendInvokeSync(
					remoteService, remoteCmd, remoteData);
			if (sd != null) {
				return sd.getBoolean();
			}
		}
		return false;
	}

	public static byte[] invokeTXZSysTool(final String packageName,
			String command, final byte[] data) {
		synchronized (SysTool.class) {
			JNIHelper.logd("core:music:SysTool : " + command + " from package : " + packageName);
			if (command.equals("volume.settool")) {
				mRemoteVolumeToolImpl = packageName;
			} else if (command.equals("volume.cleartool")) {
				mRemoteVolumeToolImpl = null;
			} else if (command.equals("wakelock.settool")) {
				mRemoteWakeLockToolImpl = packageName;
			} else if (command.equals("wakelock.cleartool")) {
				mRemoteWakeLockToolImpl = null;
			} else if (command.equals("appmgr.settool")) {
				mRemoteAppMgrToolImpl = packageName;
			} else if (command.equals("appmgr.cleartool")) {
				mRemoteAppMgrToolImpl = null;
			} else if (command.equals("screensleep.settool")) {
				mRemoteScreenSleepImpl = packageName;
				regCommScreenSleepAsr();
			} else if (command.equals("screenlight.settool")) {
				mRemoteScreenLightImpl = packageName;
			} else if (command.equals("screensleep.cleartool")) {
				mRemoteScreenSleepImpl = null;
				unregCommScreenSleepAsr();
			} else if (command.equals("wakelock.acquire")) {
				if (TextUtils.isEmpty(mRemoteWakeLockToolImpl)) {
					return "false".getBytes();
				} else {
					procByRemoteTool(WAKE_LOCK, "acquire");
					return "true".getBytes();
				}
			} else if (command.equals("wakelock.release")) {
				if (TextUtils.isEmpty(mRemoteWakeLockToolImpl)) {
					return "false".getBytes();
				} else {
					procByRemoteTool(WAKE_LOCK, "release");
					return "true".getBytes();
				}
			} else if (command.equals("pkg.sync")) {
				new Thread() {
					@Override
					public void run() {
						try {
							JSONBuilder doc = new JSONBuilder(data);
							JSONArray jInfos = doc.getVal("infos", JSONArray.class);
							AppInfo[] infos = new AppInfo[jInfos.length()];
							for (int i = 0; i < jInfos.length(); i++) {
								AppInfo info = new AppInfo();
								info.strAppName = ZHConverter.convert(jInfos
										.getJSONObject(i).getString("strAppName"),
										ZHConverter.SIMPLIFIED);
								info.strPackageName = jInfos.getJSONObject(i)
										.getString("strPackageName");
								infos[i] = info;
							}
							synchronized (SysTool.class) {
								mSyncAppInfoList = new AppInfoList();
								mSyncAppInfoList.rptMsgApps = infos;
							}
							JNIHelper.logd("core:music:sync:" + "app"
									+ jInfos.toString());
						} catch (Exception e) {
						}
						// 通知刷新应用列表
						PackageManager.getInstance().sendAppList();
					};
				}.start();
			} else if (command.equals("muteall.settool")) {
				mRemoteMuteAllToolImpl = packageName;
			} else if (command.equals("muteall.cleartool")) {
				mRemoteMuteAllToolImpl = null;
			} else if (command.equals("commcmds.settool")) {
				JNIHelper.logd("commoncmds settool:" + packageName);
				mRemoteCommCmdsToolImpl = packageName;
				// 注册常用命令字
				registerCommonCmdsWakeup();
			} else if (command.equals("commcmds.cleartool")) {
				mRemoteCommCmdsToolImpl = null;
			} else if (command.equals("screenlight.cleartool")) {
				mRemoteScreenLightImpl = null;
			} else if (command.equals("volume.setvolumedistance")) {
				if (data == null) {
					return null;
				}
				try {
					String strJson = new String(data);
					JSONBuilder json = new JSONBuilder(strJson);
					int minValue = json.getVal("minVal", Integer.class);
					int maxValue = json.getVal("maxVal", Integer.class);
					setVolumeDistance(minValue, maxValue);

				} catch (Exception e) {

				}
			} else if ("openGuideAnim".equals(command)) {
				HelpGuideManager.getInstance().startGuideAnimFromOuter();
			}
		}
		return null;
	}

	private static Integer mMinTempValue;
	private static Integer mMaxTempValue;
	private static void setVolumeDistance(int minValue, int maxValue) {
		mMaxTempValue = maxValue;
		mMinTempValue = minValue;
		CarControlData.VolumeSettingData volumeSettingData = new CarControlData.VolumeSettingData();
		volumeSettingData.uint32MaxValue = mMaxTempValue;
		volumeSettingData.uint32MinValue = mMinTempValue;
		JNIHelper.sendEvent(UiEvent.EVENT_CAR_CONTROL, CarControlData.SUBEVENT_VOLUME_SETTING, volumeSettingData);
	}

	private static AppInfoList mSyncAppInfoList;

	public synchronized static AppInfoList getSyncAppInfoList() {
		return mSyncAppInfoList;
	}
	
	// 注册常用唤醒命令
	public static void registerCommonCmdsWakeup() {
		WakeupManager.getInstance().useWakeupAsAsr(new AsrUtil.AsrComplexSelectCallback() {

			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return "TASK_COMMON_COMMAND_WORD_ID";
			}

			@Override
			public void onCommandSelected(String type, String command) {
				invokeCmdSelect(isWakeupResult(), type, command);
			}

		}.addCommand("CUSTOM_CMD_OPEN_SCREEN", NativeData.getResStringArray("RS_COMMON_WAKEUP_OPEN_SCREEN"))
				.addCommand("CUSTOM_CMD_CLOSE_SCREEN", NativeData.getResStringArray("RS_COMMON_WAKEUP_CLOSE_SCREEN"))
				.addCommand("CUSTOM_CMD_BACK_HOME", NativeData.getResStringArray("RS_COMMON_WAKEUP_BACK_HOME"))
				.addCommand("CUSTOM_CMD_BACK_NAVI", NativeData.getResStringArray("RS_COMMON_WAKEUP_BACK_NAVI")));
	}
	
	public static void invokeCmdSelect(boolean isWakeupResult, String type, String command) {
		boolean bSucc = false;
		String ttsPrefixId = "RS_VOICE_WILL_DO_COMMAND";
		if (type.equals(CUSTOM_CMD_OPEN_SCREEN)) {
			bSucc = procByRemoteToolSync(COMMCMDS, CMD_HANDLE_SCREEN, "true".getBytes());
			ttsPrefixId = "RS_VOICE_ALREAD_DO_COMMAND";
		} else if (type.equals(CUSTOM_CMD_CLOSE_SCREEN)) {
			bSucc = procByRemoteToolSync(COMMCMDS, CMD_HANDLE_SCREEN, "false".getBytes());
			if (mRemoteScreenSleepImpl != null) {
				bSucc = true;
				procByRemoteTool(SCREEN_SLEEP, CMD_GOTOSLEEP);
			}
			ttsPrefixId = "RS_VOICE_ALREAD_DO_COMMAND";
		} else if (type.equals(CUSTOM_CMD_BACK_HOME)) {
			ttsPrefixId = "RS_VOICE_DOING_COMMAND";
			bSucc = procByRemoteToolSync(COMMCMDS, CMD_BACKHOME, null);
			if (!bSucc) {
				bSucc = true;
				PackageManager.getInstance().returnHome();
			}
		} else if (type.equals(CUSTOM_CMD_BACK_NAVI)) {
			command = "打开导航";
			bSucc = procByRemoteToolSync(COMMCMDS, CMD_BACKNAVI, null);
			if (!bSucc) {
				byte[] dat = NavManager.getInstance().invokeTXZNav(null, "enterNav", null);
				try {
					bSucc = Boolean.parseBoolean(new String(dat));
				} catch (Exception e) {
					// TODO: handle exception
				}
				if (!bSucc) {
					return;
				}
			}
		}

		if (!isWakeupResult) {
			String spkHint = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
			if (bSucc) {
				spkHint = NativeData.getResString(ttsPrefixId).replace("%CMD%", command);
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(spkHint, null);
		}
	}
	
	private static void regCommScreenSleepAsr() {
		AsrUtil.regCmd(new String[] { "关闭屏幕" }, "CMD_CLOSE_SCREEN",
				new IAsrRegCmdCallBack() {
					@Override
					public void notify(String text, byte[] data) {
						String spk = NativeData
								.getResString("RS_SYS_CLOSE_SCREEN");
						TtsManager.getInstance().speakText(spk,
								new TtsUtil.ITtsCallback() {
									@Override
									public void onEnd() {
										procByRemoteTool(SCREEN_SLEEP,
												"goToSleep");
									}
								});
					}
				});
	}

	private static void unregCommScreenSleepAsr() {
		AsrUtil.unregCmd(new String[] { "关闭屏幕" });
	}
}
