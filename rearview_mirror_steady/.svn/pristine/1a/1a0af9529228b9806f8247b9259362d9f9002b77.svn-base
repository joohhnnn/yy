package com.txznet.txz.service;

import java.util.HashSet;
import java.util.Set;

import org.json.JSONObject;

import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.txz.ui.data.UiData.UserConfig;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txz.ui.platform.UiPlatform;
import com.txznet.comm.base.BaseForegroundService;
import com.txznet.comm.remote.ServiceHandler;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager.AsrMode;
import com.txznet.sdk.TXZConfigManager.AsrServiceMode;
import com.txznet.txz.cfg.ImplCfg;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.asr.remote.AsrRemoteImpl;
import com.txznet.txz.component.selector.Selector;
import com.txznet.txz.component.text.IText.ITextCallBack;
import com.txznet.txz.component.tts.remote.TtsRemoteImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.ModuleManager;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.audio.AudioManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.camera.CameraManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.contact.ContactManager;
import com.txznet.txz.module.fm.FmManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.netdata.NetDataManager;
import com.txznet.txz.module.record.RecordManager;
import com.txznet.txz.module.resource.ResourceManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sys.SysTool;
import com.txznet.txz.module.text.TextManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.version.LicenseManager;
import com.txznet.txz.module.volume.VolumeManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.notification.NotificationManager;
import com.txznet.txz.ui.widget.SDKFloatView;
import com.txznet.txz.ui.win.help.HelpMsgManager;
import com.txznet.txz.ui.win.record.RecordInvokeFactory;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DeviceInfo;

public class TXZService extends BaseForegroundService {
	private class TxzBinder extends IService.Stub {
		@Override
		public byte[] sendInvoke(String packageName, String command, byte[] data) throws RemoteException {
			if (TextUtils.isEmpty(command))
				return null;
			if (command.equals("comm.exitTXZ")) {
				// 通知所有调用程序已结束
				ServiceManager.getInstance().broadInvoke("comm.exitTXZ.exited", null);
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						AppLogic.exit();
					}
				}, 2000);
				return null;
			}
			if (command.startsWith("txz.sdk.")) {
				return invokeSDK(packageName, command, data);
			}
			if (command.startsWith("txz.record.win.")) {
				return RecordInvokeFactory.invokeRecordWin(packageName, command.substring("txz.record.win.".length()),
						data);
			}
			if (AppLogic.isInited() == false) {
				return null;
			}
			// 需要先执行初始化
			// if (!command.equals("comm.log"))
			// JNIHelper.logd("packageName=" + packageName + ", command="
			// + command + ", data=" + data);
			byte[] ret = ServiceHandler.preInvoke(packageName, command, data);
			if ("comm.log".equals(command)) { //这个日志接口是相等
				return invokeCommLog(packageName, command, data);
			}
			if (command.startsWith("comm.report.type.")) {
				try {
					JNIHelper.doReport(Integer.parseInt(command.substring("comm.report.type.".length())), data);
				}catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			if (command.equals("comm.monitor")) {
				try {
					JSONBuilder json = new JSONBuilder(data);
					JNIHelper.monitor(json.getVal("type", Integer.class), json.getVal("val", Integer.class), json.getVal("attrs", String[].class));
				}catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}
			if (command.startsWith("comm.tts.")) {
				return invokeCommTts(packageName, command, data);
			}
			if (command.startsWith("comm.location")) {
				return invokeCommLocation(packageName, command, data);
			}
			if (command.startsWith("comm.asr.")) {
				return AsrManager.getInstance().invokeCommAsr(packageName, command, data);
			}
			if (command.startsWith("comm.record")) {
				return RecordManager.getInstance().invokeCommRecord(packageName, command, data);
			}
			if (command.startsWith("comm.status.")) {
				return invokeCommStatus(packageName, command, data);
			}
			if (command.startsWith("comm.power.")) {
				return invokeCommPower(packageName, command.substring("comm.power.".length()), data);
			}
			if (command.startsWith("wx.")) {
				return invokeWebChat(packageName, command, data);
			}
			if (command.startsWith("wakeup.")){
				return invokeWakeup(packageName, command, data);
			}
			if (command.startsWith("team.")) {
				return invokeTeam(packageName, command, data);
			}
			if (command.startsWith("txz.music.")) {
				command = command.substring("txz.music.".length());
				if (command.equals("play")) {
					command += ".extra";
				}
				return MusicManager.getInstance().invokeTXZMusic(packageName,
						command, data);
			}
			if (command.startsWith("txz.audio.")) {
				return AudioManager.getInstance().invokeTXZAudio(packageName, command.substring("txz.audio.".length()),
						data);
			}
			if (command.startsWith("txz.call.")) {
				return CallManager.getInstance().invokeTXZCall(packageName, command.substring("txz.call.".length()),
						data);
			}
			if (command.startsWith("txz.config.")) {
				return ConfigManager.getInstance().invokeTXZConfig(packageName,
						command.substring("txz.config.".length()), data);
			}
			if (command.startsWith("txz.record.ui.")) {
				return RecorderWin.dealRecorderUIEvent(command, data);
			}
			if (command.startsWith("txz.nav.")) {
				return NavManager.getInstance().invokeTXZNav(packageName, command.substring("txz.nav.".length()), data);
			}
			if (command.startsWith("txz.multi.nav.")) {
				return NavManager.getInstance().invokeMultiNav(packageName,
						command.substring("txz.multi.nav.".length()), data);
			}
			if (command.startsWith("txz.sence.")) {
				return SenceManager.getInstance().invokeTXZSence(packageName, command.substring("txz.sence.".length()),
						data);
			}
			if (command.startsWith("txz.camera.")) {
				return CameraManager.getInstance().invokeTXZCamera(packageName,
						command.substring("txz.camera.".length()), data);
			}
			if (command.startsWith("txz.wakeup.")) {
				return WakeupManager.getInstance().invokeTXZWakeup(packageName,
						command.substring("txz.wakeup.".length()), data);
			}
			if (command.startsWith("txz.resource.")) {
				return ResourceManager.getInstance().invokeTXZResource(packageName,
						command.substring("txz.resource.".length()), data);
			}
			if (command.startsWith("comm.text")) {
				return invokeCommText(packageName, command, data);
			}
			if (command.startsWith("txz.notification")) {
				return NotificationManager.getInstance().invokeTXZNotification(packageName, command, data);
			}
			if (command.startsWith("txz.help")) {
				return HelpMsgManager.invokeTXZHelpMsg(packageName, command, data);
			}
			if (command.startsWith("txz.loc.")) {
				return LocationManager.getInstance().processRemoteCommand(packageName,
						command.substring("txz.loc.".length()), data);
			}
			if (command.startsWith("txz.tool.tts.")) {
				return TtsRemoteImpl.procRemoteResponse(packageName, command.substring("txz.tool.tts.".length()), data);
			}
			if (command.startsWith("txz.tool.asr.")) {
				return AsrRemoteImpl.procRemoteResponse(packageName, command.substring("txz.tool.asr.".length()), data);
			}
			if (command.startsWith("txz.sys.")) {
				return SysTool.invokeTXZSysTool(packageName, command.substring("txz.sys.".length()), data);
			}
			if (command.startsWith("txz.fm.")) {
				return FmManager.getInstance().invokeTXZFM(packageName, command.substring("txz.fm.".length()), data);
			}
			if (command.startsWith("txz.am.")) {
				return FmManager.getInstance().invokeTXZAM(packageName,
						command.substring("txz.am.".length()), data);
			}
			if (command.startsWith("txz.selector.")) {
//				if (command.startsWith("txz.selector.poi.")) {
//					return Selector.getSelector() != null ? Selector
//							.getSelector().procInvoke(
//									packageName,
//									command.substring("txz.selector.poi."
//											.length()), data) : null;
//				} else if (command.startsWith("txz.selector.audio.")) {
//					return Selector.getSelector() != null ? Selector
//							.getSelector().procInvoke(
//									packageName,
//									command.substring("txz.selector.audio."
//											.length()), data) : null;
//				} else {
//					return ListSelector.procStaticInvoke(packageName, command,
//							data);
//				}
				return Selector.procInvoke(packageName, command, data);
			}
			if (command.startsWith("txz.package.")) {
				return PackageManager.getInstance().processInvoke(packageName,
						command.substring("txz.package.".length()), data);
			}
			if (command.startsWith("comm.netdata.req.")) {
				return NetDataManager.getInstance().processInvoke(packageName,
						command.substring("comm.netdata.req.".length()), data);
			}
			if (command.startsWith("txz.poi.")) {
				return NavManager.getInstance().processInvoke(packageName, command, data);
			}
			return ret;
		}

		private byte[] invokeWakeup(String packageName, String command,
				byte[] data) {
			WakeupManager.getInstance().invokeWakeup(packageName, command, data);
			return null;
		}
	}

	private byte[] invokeCommText(final String packageName, String command, byte[] data) {
		if (command.equals("comm.text.parse")) {
			if (data == null) {
				return null;
			}
			try {
				JSONObject json = new JSONObject(new String(data));
				String text = json.getString("text");
				TextManager.getInstance().parseText(text, new ITextCallBack() {
					@Override
					public void onResult(String jsonResult) {
						ServiceManager.getInstance().sendInvoke(packageName, "comm.text.event.result",
								jsonResult.getBytes(), null);
					}

					@Override
					public void onError(int errorCode) {
						String data = "" + errorCode;
						ServiceManager.getInstance().sendInvoke(packageName, "comm.text.event.error", data.getBytes(),
								null);
					}
				});
			} catch (Exception e) {

			}
		}
		return null;
	}

	private byte[] invokeCommStatus(final String packageName, String command, byte[] data) {
		if (command.equals("comm.status.get")) {
			JSONObject json = new JSONObject();
			try {
				json = new JSONObject(new String(data));
				if (json.has("asr"))
					json.put("asr", AsrManager.getInstance().isBusy() || RecordManager.getInstance().isBusy());
				if (json.has("tts"))
					json.put("tts", TtsManager.getInstance().isBusy());
				if (json.has("call"))
					json.put("call", CallManager.getInstance().isIdle() == false);
				return json.toString().getBytes();
			} catch (Exception e) {
				return json.toString().getBytes();
			}
		}
		return null;
	}

	private long mLastShockWakeupTime = 0;

	private byte[] invokeCommPower(final String packageName, String command, byte[] data) {
		if (command.equals("POWER_ON")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_POWER_ON);
			return null;
		}
		if (command.equals("BEFORE_SLEEP")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_BEFORE_SLEEP);
			WakeupManager.getInstance().stopComplete();
			LocationManager.getInstance().quickLocation(false);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
			return null;
		}
		if (command.equals("SLEEP")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_SLEEP);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
			return null;
		}
		if (command.equals("WAKEUP")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_WAKEUP);
			WakeupManager.getInstance().start();
			LocationManager.getInstance().quickLocation(true);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.wakeup", null, null);
			return null;
		}
		if (command.equals("BEFORE_POWER_OFF")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_BEFORE_POWER_OFF);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
			return null;
		}
		if (command.equals("POWER_OFF")) {
			JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_POWER_OFF);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.exit", null, null);
//			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.client.sleep", null, null);
			return null;
		}
		if (command.equals("SHOCK_WAKEUP")) {
			if (System.currentTimeMillis() - mLastShockWakeupTime > 10 * 1000) {
				mLastShockWakeupTime = System.currentTimeMillis();
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_PLATFORM, UiPlatform.SUBEVENT_POWER_ACTION_SHOCK_WAKEUP);
			}
			return null;
		}
		return null;
	}

	private byte[] invokeCommLocation(final String packageName, String command, byte[] data) {
		if (command.equals("comm.location.gethome")) {
			UserConfig userConfig = NativeData.getCurUserConfig();
			if (userConfig.msgNetCfgInfo != null && userConfig.msgNetCfgInfo.msgHomeLoc != null) {
				NavigateInfo home = userConfig.msgNetCfgInfo.msgHomeLoc;
				return NavigateInfo.toByteArray(home);
			}
		} else if (command.equals("comm.location.getcompany")) {
			UserConfig userConfig = NativeData.getCurUserConfig();
			if (userConfig.msgNetCfgInfo != null && userConfig.msgNetCfgInfo.msgCompanyLoc != null) {
				NavigateInfo company = userConfig.msgNetCfgInfo.msgCompanyLoc;
				return NavigateInfo.toByteArray(company);
			}
		} else if (command.equals("comm.location.sethome")) {
			try {
				NavigateInfo home = NavigateInfo.parseFrom(data);
				NavManager.getInstance().setHomeLocation(home.strTargetName, home.strTargetAddress,
						home.msgGpsInfo.dblLat, home.msgGpsInfo.dblLng, home.msgGpsInfo.uint32GpsType);
			} catch (Exception e) {
				LogUtil.loge("set home fail!");
			}
		} else if (command.equals("comm.location.setcompany")) {
			try {
				NavigateInfo company = NavigateInfo.parseFrom(data);
				NavManager.getInstance().setCompanyLocation(company.strTargetName, company.strTargetAddress,
						company.msgGpsInfo.dblLat, company.msgGpsInfo.dblLng, company.msgGpsInfo.uint32GpsType);
			} catch (Exception e) {
				LogUtil.loge("set company fail!");
			}
		} else if (command.equals("comm.location.gethistorylist")) {
			UserConfig userConfig = NativeData.getCurUserConfig();
			if (userConfig.msgNetCfgInfo != null && userConfig.msgNetCfgInfo.msgHistoryLocs != null
					&& userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem != null) {
				NavigateInfoList history = userConfig.msgNetCfgInfo.msgHistoryLocs;
				return NavigateInfoList.toByteArray(history);
			}
		} else if (command.equals("comm.location.sethistory")) {
			try {
				NavigateInfo history = NavigateInfo.parseFrom(data);
				NavManager.getInstance().setHistroyLocation(history, false);
			} catch (Exception e) {
				LogUtil.loge("set history fail!");
			}
		}
		return null;
	}

	private byte[] invokeCommTts(final String packageName, String command, byte[] data) {
		try {
			if (command.equals("comm.tts.speak")) {
				JSONBuilder jsonDoc = new JSONBuilder(new String(data));
				int iStream = jsonDoc.getVal("iStream", Integer.class);
				String sText = jsonDoc.getVal("sText", String.class);
				String[] voiceUrls = jsonDoc.getVal("voiceUrls", String[].class);
				PreemptType bPreempt = PreemptType.valueOf(jsonDoc.getVal("bPreempt", String.class));
				int remoteTtsId = TtsManager.getInstance().speakVoice(iStream, sText, voiceUrls, bPreempt, true,
						new ITtsCallback() {
							@Override
							public void onCancel() {
								String data = new JSONBuilder().put("ttsId", mTaskId).toString();
								ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.cancel",
										data.getBytes(), null);
							}

							@Override
							public void onSuccess() {
								String data = new JSONBuilder().put("ttsId", mTaskId).toString();
								ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.success",
										data.getBytes(), null);
							}

							@Override
							public void onError(int iError) {
								String data = new JSONBuilder().put("ttsId", mTaskId).put("error", iError).toString();
								ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.error",
										data.getBytes(), null);
							}
						});
				return (remoteTtsId + "").getBytes();
			}
			if (command.equals("comm.tts.cancel")) {
				TtsManager.getInstance().cancelSpeak(Integer.parseInt(new String(data)));
				return null;
			}
			if (command.equals("comm.tts.speakTextOnRecordWin")) {
				JSONBuilder json = new JSONBuilder(data);
				AsrManager.getInstance().setNeedCloseRecord(json.getVal("close", Boolean.class, true));
				RecorderWin.speakTextWithClose(json.getVal("text", String.class),json.getVal("needAsr", Boolean.class, true), new Runnable() {
					@Override
					public void run() {
						ServiceManager.getInstance().sendInvoke(packageName, "comm.tts.event.speakTextOnRecordWin.end",
								null, null);
					}
				});
				return null;
			}
			if (command.equals("comm.tts.set.voicespeed")) {
				if (data == null) {
					return null;
				}
				int speed = Integer.parseInt(new String(data));
				TtsManager.getInstance().setVoiceSpeed(speed);
				return null;
			}
			if (command.equals("comm.tts.set.modelrole")) {
				if (data == null) {
					return null;
				}
				try {
					String strModel = new String(data);
					TtsManager.getInstance().setTtsModel(strModel);
				} catch (Exception e) {

				}
				return null;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public byte[] invokeCommLog(String packageName, String command, byte[] data) {
		JSONBuilder jsonDoc = new JSONBuilder(new String(data));
		int level = jsonDoc.getVal("level", Integer.class);
		String tag = jsonDoc.getVal("tag", String.class);
		String content = jsonDoc.getVal("content", String.class);
		JNIHelper._logRaw(packageName, level, tag, content);
		return null;
	}

	private byte[] invokeWebChat(final String packageName, String command, byte[] data) {
		if (command.equals("wx.subscribe.qrcode")) {
			// 通知服务器订阅模块事件
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_WX_URL);
			return null;
		}

		if (command.equals("wx.upload.voice")) {
			// 上传语音
			try {
				JSONObject json = new JSONObject(new String(data));
				UiEquipment.Req_UploadVoice req = new UiEquipment.Req_UploadVoice();
				req.strPath = json.getString("path");
				req.uint32VoiceTimeLength = json.getInt("length");
				JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_UPLOAD_VOICE, req);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("wx.contact.update")) {
			// 更新微信联系人
			WeixinManager.getInstance().updateWeChatContact(command, data);
			return null;
		}

		if (command.equals("wx.loginstate.update")) {
			// 更新微信登陆状态
			WeixinManager.getInstance().updateLoginState(command, data);
			WeixinManager.getInstance().doReportWxchatLoginState(data);
			return null;
		}

		if (command.equals("wx.contact.recentsession.update")) {
			WeixinManager.getInstance().updateWeChatRecentChat(command, data);
			return null;
		}
		
		if (command.equals("wx.contact.maskedsession.update")) {
			WeixinManager.getInstance().updateWeChatMaskedChat(command, data);
			return null;
		}
		
		if (command.equals("wx.cmd.proc.send")) {
			WeixinManager.getInstance().procSendMsgSence();
			return null;
		}
		return null;
	}

	private byte[] invokeTeam(final String packageName, String command, byte[] data) {
		if (command.equals("team.subscribe.qrcode")) {
			// 通知服务器订阅模块事件
			JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_BIND_CAR_TEAM_URL);
			return null;
		}

		return null;
	}

	boolean mSetOnlyByLauncher = true; // 只被launcher设置过初始化参数
	static boolean mFirstCheckInit = true;

	public static boolean checkSdkInitResult() {
		if (!TextUtils.isEmpty(ImplCfg.getAsrImplClass())) {
			if (!AsrManager.getInstance().isInited())
				return false;
			if (!AsrManager.getInstance().isInitSuccessed()) {
				ServiceManager.getInstance().broadInvoke("sdk.init.error.asr", null);
				return true;
			}
		}
		if (!TextUtils.isEmpty(ImplCfg.getTtsImplClass())) {
			if (!TtsManager.getInstance().isInited())
				return false;
			if (!TtsManager.getInstance().isInitSuccessed()) {
				ServiceManager.getInstance().broadInvoke("sdk.init.error.tts", null);
				return true;
			}
		}
		if (!WakeupManager.getInstance().isInited())
			return false;
		if (!WakeupManager.getInstance().isInitSuccessed()) {
			ServiceManager.getInstance().broadInvoke("sdk.init.error.wakeup", null);
			return true;
		}
		ServiceManager.getInstance().broadInvoke("sdk.init.success", null);
		setFloatToolTypeInner(mFloatToolType);
		setFloatToolUrlInner(mFloatToolUrl_N, mFloatToolUrl_P);
		if (mFloatToolClickInterval != null) {
			setFloatToolClickIntervalInner(mFloatToolClickInterval);
		}
		if (mFirstCheckInit) {
			mFirstCheckInit = false;
			// TtsManager.getInstance().speakText("语音助理初始化完成");
			JNIHelper.logd("txz app init ready");
			
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					ModuleManager.getInstance().initialize_AfterInitSuccess();
				}
			}, 0);
		}
		return true;
	}

	private static Runnable mHandlerTask = new Runnable() {
		@Override
		public void run() {
			if (PackageManager.getInstance().isHome()) {
				SDKFloatView.getInstance().open();
			} else {
				SDKFloatView.getInstance().close();
			}
			AppLogic.removeUiGroundCallback(mHandlerTask);
			AppLogic.runOnUiGround(mHandlerTask, 500);
		}
	};

	private static void setFloatToolTypeInner(String floatToolType) {
		AppLogic.removeUiGroundCallback(mHandlerTask);
		if (floatToolType.equals("FLOAT_TOP")) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					SDKFloatView.getInstance().open();
				}
			}, 0);
		} else if (floatToolType.equals("FLOAT_NORMAL")) {
			AppLogic.runOnUiGround(mHandlerTask, 0);
		} else if (floatToolType.equals("FLOAT_NONE")) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					SDKFloatView.getInstance().close();
				}
			}, 0);
		}
	}

	private static void setFloatToolUrlInner(final String floatToolUrl_N, final String floatToolUrl_P) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				SDKFloatView.getInstance().setImageBitmap(floatToolUrl_N, floatToolUrl_P);
			}
		}, 0);
	}
	
	private static void setFloatToolClickIntervalInner(final long interval) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				SDKFloatView.getInstance().setClickInteval(interval);
			}
		}, 0);
	}

	private static String mFloatToolType = "FLOAT_TOP";
	
	public static String getFloatToolType(){
		return mFloatToolType;
	}

	public static void setFloatToolType(String floatToolType) {
		mFloatToolType = floatToolType;
		if (AsrManager.getInstance().isInitSuccessed() && TtsManager.getInstance().isInitSuccessed()
				&& WakeupManager.getInstance().isInitSuccessed()) {
			setFloatToolTypeInner(mFloatToolType);
		}
		ConfigManager.getInstance().notifyRemoteSync();
	}

	private static String mFloatToolUrl_N = null;
	private static String mFloatToolUrl_P = null;

	public static void setFloatToolUrl(String floatToolUrl_N, String floatToolUrl_P) {
		mFloatToolUrl_N = floatToolUrl_N;
		mFloatToolUrl_P = floatToolUrl_P;
		if (AsrManager.getInstance().isInitSuccessed() && TtsManager.getInstance().isInitSuccessed()
				&& WakeupManager.getInstance().isInitSuccessed()) {
			setFloatToolUrlInner(mFloatToolUrl_N, mFloatToolUrl_P);
		}
	}
	
	private static Long mFloatToolClickInterval = null;
	
	public static void setFloatToolClickInterval(long interval) {
		mFloatToolClickInterval = interval;
		setFloatToolClickIntervalInner(interval);
	}

	private static Set<String> mSetNoVoiceService = new HashSet<String>();

	public static void notifyInited() {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (!AppLogic.isInited())
					return;
				for (String s : mSetNoVoiceService) {
					ServiceManager.getInstance().sendInvoke(s, "sdk.init.success", null, null);
				}
				mSetNoVoiceService.clear();
			}
		}, 0);
	}

	private byte[] invokeSDK(final String packageName, String command, final byte[] data) {
		if (command.equals("txz.sdk.init")) {
			JNIHelper.logd(packageName + " init sdk");
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					if (data == null) {
						if (AppLogic.isInited()) {
							ServiceManager.getInstance().sendInvoke(packageName, "sdk.init.success", null, null);
						} else {
							mSetNoVoiceService.add(packageName);
						}
						return;
					}
					ServiceManager.getInstance().sendInvoke(packageName, "", null, new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							checkSdkInitResult();
						}
					});
					if (!ServiceManager.LAUNCHER.equals(packageName))
						mSetOnlyByLauncher = false;
					if (mSetOnlyByLauncher == false && ServiceManager.LAUNCHER.equals(packageName)) {
						// 如果被其他非launcher程序初始化过，则不再执行launcher的初始化参数了
						return;
					}
					String appId = null;
					String appToken = null;
					String appCustomId = null;
					String uuid = null;
					String neverFormatRoot = null;
					String ttsType = null;
					String asrType = null;
					String ftType = null;
					String ftUrl_N = null;
					String ftUrl_P = null;
					Long ftInterval = null;
					String[] wakeupKeywords = null;
					Boolean enableServiceContact = null;
					Boolean fixCallFunction = null;
					String defaultNavTool = null;
					AsrMode asrMode = null;
					Boolean coexistAsrAndWakeup = null;
					Float wakeupThreshHold = null;
					Float asrWakeupThreshHold = null;
					Boolean autoRunAsr = null;
					Integer beepTimeOut = null;
					Integer filterNoiseType = null;
					AsrServiceMode asrServiceMode = null;
					Integer ttsVoiceSpeed = null;
					Integer maxAsrRecordTime = null;
					Boolean zeroVolToast = null;
					Integer txzStream = null;
					Boolean useExternalAudioSource = null;
					Boolean enableBlackHole = null;
					Boolean forceStopWkWhenTts = null;
					Integer audioSourceForRecord = null;
					Integer extAudioSourceType = null;
					JSONBuilder json = new JSONBuilder(data);
					try {
						JNIHelper.logd(
								packageName + " init sdk version: " + json.getVal("version", String.class, "UNKNOW"));
						appId = json.getVal("appId", String.class);
						appToken = json.getVal("appToken", String.class);
						appCustomId = json.getVal("appCustomId", String.class);
						uuid = json.getVal("uuid", String.class);
						neverFormatRoot = json.getVal("neverFormatRoot", String.class);
						ttsType = json.getVal("ttsType", String.class);
						asrType = json.getVal("asrType", String.class);
						ftType = json.getVal("ftType", String.class);
						ftUrl_N = json.getVal("ftUrl_N", String.class);
						ftUrl_P = json.getVal("ftUrl_P", String.class);
						ftInterval = json.getVal("ftInterval", Long.class);
						String strAsrMode = json.getVal("asrMode", String.class);
						if (strAsrMode != null) {
							asrMode = AsrMode.valueOf(strAsrMode);
						}
						wakeupKeywords = json.getVal("wakeupKeywords", String[].class);
						enableServiceContact = json.getVal("enableServiceContact", Boolean.class);
						fixCallFunction = json.getVal("fixCallFunction", Boolean.class);
						defaultNavTool = json.getVal("defaultNavTool", String.class);
						coexistAsrAndWakeup = json.getVal("coexistAsrAndWakeup", Boolean.class);
						wakeupThreshHold = json.getVal("wakeupThreshHold", Float.class);
						asrWakeupThreshHold = json.getVal("asrWakeupThreshHold", Float.class);
						autoRunAsr = json.getVal("autoRunAsr", Boolean.class);
						beepTimeOut = json.getVal("beepTimeOut", Integer.class);
						ttsVoiceSpeed = json.getVal("ttsVoiceSpeed", Integer.class);
						String strAsrSvrMode = json.getVal("asrServiceMode", String.class);
						if (strAsrSvrMode != null) {
							try {
								asrServiceMode = AsrServiceMode.valueOf(strAsrSvrMode);
							} catch (Exception e) {

							}
						}
						String strAsrChoiceMode = json.getVal("asrChoiceMode", String.class);
						if (strAsrChoiceMode != null) {
						}
						filterNoiseType = json.getVal("filterNoiseType", Integer.class);
						maxAsrRecordTime = json.getVal("maxAsrRecordTime", Integer.class);
						zeroVolToast = json.getVal("zeroVolToast", Boolean.class);
						txzStream = json.getVal("txzStream", Integer.class);
						useExternalAudioSource = json.getVal("useExternalAudioSource", Boolean.class);
						enableBlackHole = json.getVal("enableBlackHole", Boolean.class);
						audioSourceForRecord = json.getVal("audioSourceForRecord", Integer.class);
						forceStopWkWhenTts = json.getVal("forceStopWkWhenTts", Boolean.class);
						extAudioSourceType = json.getVal("extAudioSourceType", Integer.class);
					} catch (Exception e) {
					}
					
					if (appId != null && appToken != null) {
						AppLogic.initWhenStart();
					}
					else if (AppLogic.isInited() == false) {
						JNIHelper.logd(packageName
								+ " init sdk waiting for appId");
						return;
					}

					if (uuid != null) {
						DeviceInfo.setUUID(uuid);
					}
					if (neverFormatRoot != null) {
						DeviceInfo.setNeverFormatRoot(neverFormatRoot);
					}

					// 设置tts实现类
					if (ttsType != null) {
						if (ttsType.equals("NONE")) {
							ImplCfg.setTtsImplClass("");
						} else if (ttsType.equals("TTS_SYSTEM")) {
							ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.sys.TtsSysImpl");
						} else if (ttsType.equals("TTS_IFLY")) {
							ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.ifly.TtsIflyImpl");
						} else {
							// ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.yunzhisheng.TtsYunzhishengImpl");
							ImplCfg.setTtsImplClass("com.txznet.txz.component.tts.yunzhisheng_3_0.TtsYunzhishengImpl");
						}
					}

					// 设置asr实现类
					if (asrType != null) {
						if (asrType.equals("NONE")) {
							ImplCfg.setAsrImplClass("");
						} else if (asrType.equals("ASR_IFLY")) {
							ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.ifly.AsrIflyImpl");
						} else {
							// ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.yunzhisheng.AsrYunzhishengImpl");
							ImplCfg.setAsrImplClass("com.txznet.txz.component.asr.yunzhisheng_3_0.AsrYunzhishengImpl");
						}
					}

					// 设置悬浮工具类型
					if (ftType != null) {
						JNIHelper.logd("init setFloatToolType: " + ftType);
						setFloatToolType(ftType);
					}

					// 设置浮动工具图片
					JNIHelper.logd("init setFloatToolUrl: " + ftUrl_N + ", " + ftUrl_P);
					setFloatToolUrl(ftUrl_N, ftUrl_P);
					
					// 设置悬浮工具点击间隔
					if (ftInterval != null) {
						JNIHelper.logd("init setFloatToolClickInterval: " + ftInterval);
						setFloatToolClickIntervalInner(ftInterval);
					}

					// 设置唤醒词
					if (wakeupKeywords != null) {
						WakeupManager.getInstance().updateWakupKeywords_Sdk(wakeupKeywords);
					}

					// 设置唤醒阀值
					if (wakeupThreshHold != null) {
						WakeupManager.getInstance().setWakeupThreshhold(wakeupThreshHold);
					}

					// 设置识别唤醒阀值
					if (asrWakeupThreshHold != null) {
						WakeupManager.getInstance().setAsrWakeupThreshhold(asrWakeupThreshHold);
					}

					// 设置tts播报速度
					if (ttsVoiceSpeed != null) {
						TtsManager.getInstance().setVoiceSpeed(ttsVoiceSpeed);
					}

					if (autoRunAsr != null) {
						AsrManager.getInstance().enableAutoRun(autoRunAsr);
					}

					if (beepTimeOut != null) {
						AsrManager.getInstance().setBeepTimeout(beepTimeOut);
					}

					// TODO 设置是否允许服务号联系人
					if (enableServiceContact != null) {
						ContactManager.getInstance().mEnableServiceContact = enableServiceContact;
					}

					// 设置是否固定调用接口
					if (fixCallFunction != null) {
						ProjectCfg.setFixCallFunction(fixCallFunction);
					}
					
					if (defaultNavTool != null) {
						ProjectCfg.setDefaultNavTool(defaultNavTool);
					}

					// 设置识别模式
					if (asrMode != null) {
						AsrManager.getInstance().setAsrMode(asrMode);
					}

					// 设置引擎识别模式
					if (asrServiceMode != null) {
						AsrManager.getInstance().setAsrServiceMode(asrServiceMode);
					}

					if (maxAsrRecordTime != null) {
						AsrManager.getInstance().setKeySpeechTimeout(maxAsrRecordTime);
					}

					if (zeroVolToast != null) {
						VolumeManager.getInstance().enableZeroVolToast(zeroVolToast);
					}
					if (txzStream != null) {
						TtsUtil.DEFAULT_TTS_STREAM = txzStream;
					}
					// 设置声控和唤醒是否可以共存
					if (coexistAsrAndWakeup != null) {
						ProjectCfg.mCoexistAsrAndWakeup = coexistAsrAndWakeup;
					}
					if (ProjectCfg.mCoexistAsrAndWakeup) {
						ImplCfg.setWakeupImplClass(
								"com.txznet.txz.component.wakeup.yunzhishengremote.WakeupYunzhishengRemoteImpl");
					} else {
						if (PackageManager.getInstance().checkAppExist(ServiceManager.WAKEUP)){
							ImplCfg.setWakeupImplClass("com.txznet.txz.component.wakeup.txz.WakeupTxzImpl");
						} else {
							ImplCfg.setWakeupImplClass("com.txznet.txz.component.wakeup.yunzhisheng_3_0.WakeupYunzhishengImpl");
						}
					}

					if (filterNoiseType != null) {
						ProjectCfg.setFilterNoiseType(filterNoiseType);
					}
					if (useExternalAudioSource != null) {
						ProjectCfg.useExtAudioSource(useExternalAudioSource);
					}
					
					if (enableBlackHole != null){
						ProjectCfg.enableBlackHole(enableBlackHole);
					}
					
					if (audioSourceForRecord != null){
						ProjectCfg.setAudioSourceForRecord(audioSourceForRecord);
					}
					
					if (forceStopWkWhenTts != null){
						ProjectCfg.forceStopWkWhenTts(forceStopWkWhenTts);
					}
					
					if (extAudioSourceType != null){
						ProjectCfg.setExtAudioSourceType(extAudioSourceType);
					}
					
					// 获取license，并初始化声控引擎
					if (appId != null && appToken != null) {
						LicenseManager.getInstance().initSDK(packageName, appId, appToken, appCustomId);
					}
				}
			}, 0);
			return null;
		} else if (command.equals("txz.sdk.ft.status.type")) {
			String floatToolType = new JSONBuilder(new String(data)).getVal("floatToolType", String.class);
			JNIHelper.logd("config setFloatToolType: " + floatToolType);
			setFloatToolType(floatToolType);
		} else if (command.equals("txz.sdk.ft.status.icon")) {
			String floatToolUrl_N = new JSONBuilder(new String(data)).getVal("floatToolUrl_N", String.class);
			String floatToolUrl_P = new JSONBuilder(new String(data)).getVal("floatToolUrl_P", String.class);
			JNIHelper.logd("config setFloatToolUrl: " + floatToolUrl_N + ", " + floatToolUrl_P);
			setFloatToolUrl(floatToolUrl_N, floatToolUrl_P);
		} else if (command.equals("txz.sdk.ft.status.interval")) {
			Long ftInterval = new JSONBuilder(new String(data)).getVal("ftInterval", Long.class);
			JNIHelper.logd("config setFloatToolInterval: " + ftInterval);
			setFloatToolClickInterval(ftInterval);
		}
		return null;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return new TxzBinder();
	}

	@Override
	public void onCreate() {
		super.onCreate();
		// setForeground();
	}

	// private static final int TXZ_NOTIFICATION_ID = 0x11000001;
	// private Notification mTXZNotification;
	// private WakeupManager.WakeupWordsChangedListener mWakeupWordsListener;
	//
	// private void setForeground() {
	// mWakeupWordsListener = new WakeupWordsChangedListener(){
	// public void onWordsChanged(String [] words){
	// if(words != null){
	// startForeground(TXZ_NOTIFICATION_ID, buildNotification(words));
	// }else{
	// stopForeground(true);
	// }
	// }
	// };
	// WakeupManager.getInstance().addWakeupWordsListener(mWakeupWordsListener);
	// }

	// private Notification buildNotification(String[] wakeUpWords){
	// String title = "语音唤醒运行中";
	// String content = null;
	// if(wakeUpWords != null && wakeUpWords.length > 0){
	// StringBuilder wakeUpWordsStr = new StringBuilder();
	// for (int i = 0; i < wakeUpWords.length; i++) {
	// if(i > 0){
	// wakeUpWordsStr.append(", ");
	// }
	// wakeUpWordsStr.append(wakeUpWords[i]);
	// }
	// content = String.format("您可以说”%s“，来唤醒语音识别", wakeUpWordsStr.toString());
	// }
	//
	// // 模拟媒体按键
	// Intent intent = new Intent("txz.intent.action.BUTTON");
	// intent.putExtra("txz.intent.action.KEY_EVENT", 0xff00);
	// final PendingIntent pi = PendingIntent.getBroadcast(this, 0, intent, 0);
	//
	// if(mTXZNotification != null){
	// mTXZNotification.setLatestEventInfo(TXZService.this, title, content, pi);
	// return mTXZNotification;
	// }
	//
	// mTXZNotification = new NotificationCompat.Builder(this)
	// .setContentTitle(title)
	// .setContentText(content)
	// .setSmallIcon(R.drawable.widget_voice_assistant_normal)
	// .setContentIntent(pi)
	// .build();
	// return mTXZNotification;
	// }

	// @Override
	// public void onDestroy() {
	// if(mWakeupWordsListener != null){
	// WakeupManager.getInstance().delWakeupWordsListener(mWakeupWordsListener);
	// }
	// super.onDestroy();
	// }
}
