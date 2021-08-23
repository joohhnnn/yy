package com.txznet.txz.module.sys;

import org.json.JSONArray;

import com.spreada.utils.chinese.ZHConverter;
import com.txz.ui.app.UiApp.AppInfo;
import com.txz.ui.app.UiApp.AppInfoList;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.tts.TtsManager;

import android.text.TextUtils;

public class SysTool {
	public static final String VOLUME = "tool.volume";
	public static final String WAKE_LOCK = "tool.wakelock";
	public static final String APP_MGR = "tool.appmgr";
	public static final String SCREEN_SLEEP = "tool.screensleep";
	public static final String MUTEALL = "tool.muteall";
	private static String mRemoteVolumeToolImpl = null;
	private static String mRemoteWakeLockToolImpl = null;
	private static String mRemoteAppMgrToolImpl = null;
	private static String mRemoteScreenSleepImpl = null;
	private static String mRemoteMuteAllToolImpl = null;

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
		}
		if (remoteService != null) {
			ServiceManager.getInstance().sendInvoke(remoteService, remoteCmd,
					remoteData, null);
			return true;
		} else {
			return false;
		}
	}

	public static byte[] invokeTXZSysTool(final String packageName,
			String command, byte[] data) {
		synchronized (SysTool.class) {
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
				} catch (Exception e) {
				}
				// 通知刷新应用列表
				PackageManager.getInstance().sendAppList();
			} else if (command.equals("muteall.settool")) {
				mRemoteMuteAllToolImpl = packageName;
			} else if (command.equals("muteall.cleartool")) {
				mRemoteMuteAllToolImpl = null;
			}
		}
		return null;
	}

	private static AppInfoList mSyncAppInfoList;

	public synchronized static AppInfoList getSyncAppInfoList() {
		return mSyncAppInfoList;
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
