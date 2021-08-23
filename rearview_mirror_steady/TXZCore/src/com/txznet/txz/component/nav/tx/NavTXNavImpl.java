package com.txznet.txz.component.nav.tx;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.tx.internal.ExternalDefaultBroadcastKey;
import com.txznet.txz.component.nav.tx.internal.SRActionDispatcher;
import com.txznet.txz.component.nav.tx.internal.TNBroadcastManager;
import com.txznet.txz.component.nav.tx.internal.TNBroadcastReceiver;
import com.txznet.txz.component.nav.tx.internal.TNBroadcastReceiver.OnNaviMsgListener;
import com.txznet.txz.component.nav.tx.internal.TNBroadcastSender;
import com.txznet.txz.component.nav.tx.internal.TNFeedbackListener;
import com.txznet.txz.component.nav.tx.internal.TNLatLng;
import com.txznet.txz.component.nav.tx.internal.TNPoi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.MyInstallReceiver;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NavTXNavImpl extends NavThirdComplexApp {
	public static final String PACKAGE_NAME = "com.tencent.wecarnavi";
	
	private String mTmpSpeech;
	
	@Override
	public int initialize(IInitCallback oRun) {
		init();
		return super.initialize(oRun);
	}

	private void init() {
		TNBroadcastManager.getInstance().init(GlobalContext.get());
		TNBroadcastSender.getInstance().initParent(this);
		TNBroadcastReceiver.getInstance().assignParent(this);
//		SRActionDispatcher.getInstance().mTimeoutDelay = 3 * 1000;
		// 不需要等待结果，与高德一样直接播报
		SRActionDispatcher.getInstance().mTimeoutDelay = -1;
		TNBroadcastManager.getInstance().setMsgListener(new OnNaviMsgListener() {

			@Override
			public void onForeground() {
				onResume();
			}

			@Override
			public void onBackground() {
				onPause();
			}

			@Override
			public void onNavRecv(Intent intent) {
				try {
					if (procNavCmdsSender(intent)) {
						return;
					}
					if (procHomeCompanyAddr(intent)) {
						return;
					}
				} catch (Exception e) {
				}
			}

			@Override
			public void onStartNav() {
				onStart();
			}

			@Override
			public void onEndNav() {
				onEnd(false);
			}

			@Override
			public void onNavExit() {
				onPause();
				onExitApp();
			}
		});

		TNBroadcastManager.getInstance().setOnFeedbackListener(new TNFeedbackListener() {

			@Override
			public void onTimeOut() {
				if (RecorderWin.isOpened()) {
					speakWordsWithClose("发送腾讯地图指令发生超时");
				}
			}

			@Override
			public void onRevWhereAmI(int errorCode, String address, String strTtsWording) {
				JNIHelper.loge(
						"onRevWhereAmI errCode:" + errorCode + ",address:" + address + ",strTtsWord:" + strTtsWording);
				if (!TextUtils.isEmpty(strTtsWording)) {
					speakWordsWithClose(strTtsWording);
				}
			}

			@Override
			public void onRevRemainTime(int errorCode, int timeAsSecond, String strTtsWording) {
				JNIHelper.loge("onRevRemainTime errCode:" + errorCode + ",timeAsSecond:" + timeAsSecond + ",strTtsWord:"
						+ strTtsWording);
				if (!TextUtils.isEmpty(strTtsWording)) {
					speakWordsWithClose(strTtsWording);
				}
			}

			@Override
			public void onRevRemainDistance(int errorCode, int distanceAsMeter, String strTtsWording) {
				JNIHelper.loge("onRevRemainDistance errCode:" + errorCode + ",distanceAsMeter:" + distanceAsMeter
						+ ",strTtsWord:" + strTtsWording);
				if (!TextUtils.isEmpty(strTtsWording)) {
					speakWordsWithClose(strTtsWording);
				}
			}

			@Override
			public void onFeedback(int errorCode, Bundle data, String strTtsWording) {
				JNIHelper.loge("onFeedback errCode:" + errorCode + ",data:" + data.toString() + ",strTtsWord:"
						+ strTtsWording);
				if (!TextUtils.isEmpty(strTtsWording)) {
//					speakWordsWithClose(strTtsWording);
				}
				if (data.containsKey(ExternalDefaultBroadcastKey.KEY.TAG)) {
					String sessionId = data.getString(ExternalDefaultBroadcastKey.KEY.TAG);
					// 查看全程使用导航的反馈语
					if (ExternalDefaultBroadcastKey.FB_SESSION_DEFAULT.VIEW_ALL_SESSION.equals(sessionId)) {
						String speechText = strTtsWording;
						if (TextUtils.isEmpty(strTtsWording)) {
							speechText = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
						} else {
							speechText = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%",
									NativeData.getResString("RS_MAP_VIEW_ALL").replace("%COMMAND%", mTmpSpeech));
						}
						speakWordsWithClose(speechText);
					}
				}
			}
		});

		// 注册apk安装和卸载监听
		MyInstallReceiver.SINSTALL_OBSERVABLE
				.registerObserver(new MyInstallReceiver.InstallObservable.InstallObserver() {

					@Override
					public void onApkUnInstall(String packageName) {
						onPause();
					}

					@Override
					public void onApkInstall(String packageName) {
					}
				});
	}

	private int mSpeechId = TtsManager.INVALID_TTS_TASK_ID;

	private boolean procNavCmdsSender(Intent intent) {
		Bundle data = intent.getExtras();
		int keyType = data.getInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE);
		if (keyType == ExternalDefaultBroadcastKey.TYPE.NAVI_CONFIRM_DIALOG_SHOW) {// 对话框显示
			createConfirmDialog(data, ExternalDefaultBroadcastKey.TYPE.NAVI_CONFIRM_DIALOG_ORDER);
			return true;
		} else if (keyType == ExternalDefaultBroadcastKey.TYPE.NAVI_CONFIRM_DIALOG_DISMISS) {// 对话框消失
			dismissConfirmDialog(data);
			return true;
		} else if (keyType == ExternalDefaultBroadcastKey.TYPE.NAVI_PUSH_WXPOI_DIALOG_SHOW) {// 微信我的车显示
			createConfirmDialog(data, ExternalDefaultBroadcastKey.TYPE.NAVI_PUSH_WXPOI_DIALOG_ORDER);
			return true;
		} else if (keyType == ExternalDefaultBroadcastKey.TYPE.NAVI_PUSH_WXPOI_DIALOG_HIDE) {// 微信我的车对话框消失
			dismissConfirmDialog(data);
			return true;
		}
		return false;
	}

	private void createConfirmDialog(Bundle data,final int okActionType) {
		String speech = data.getString("WORD");
		String cmd = data.getString("CMDS");
		String[] wks = cmd.split(";");
		if (wks == null) {
			return;
		}
		final Map<String, String> cmdMap = new HashMap<String, String>();
		NavDialogAsr nda = new NavDialogAsr(cmd) {

			@Override
			public void onSelect(String type) {
				final String order = cmdMap.get(type);
				Bundle bundle = new Bundle();
				bundle.putString("CMD", order);
				bundle.putBoolean("notimeout", true);
				TNBroadcastSender.getInstance()
						.sendBroadcast(okActionType, bundle);
				JNIHelper.logd("send nav cmd:" + order);
			}
		};

		for (String wk : wks) {
			String[] asr = wk.split(":");
			if (asr != null) {
				nda.addCmds(asr[0], asr[0]);
				cmdMap.put(asr[0], asr[1]);
			}
		}
		nda.onShow();

		NavSenderCycler.getInstance().addNavSender(nda);
		if (RecorderWin.isOpened()) {
			// 关闭界面
			RecorderWin.close();
		}

		TtsManager.getInstance().cancelSpeak(mSpeechId);
		mSpeechId = TtsManager.getInstance().speakVoice(speech, TtsManager.BEEP_VOICE_URL);
	}

	// 消失对话框没有id，认为是退掉顶层
	private void dismissConfirmDialog(Bundle data) {
		JNIHelper.logd("NAVI_CONFIRM_DIALOG_DISMISS");
		NavSenderCycler.getInstance().popTop();
		TtsManager.getInstance().cancelSpeak(mSpeechId);
	}

	private boolean procHomeCompanyAddr(Intent intent) {
		Bundle data = intent.getExtras();
		int keyType = data.getInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE);
		if (keyType == ExternalDefaultBroadcastKey.TYPE.NAVI_SEND_HOME_COMPANY_ADDR) {
			String poiname = data.getString(ExternalDefaultBroadcastKey.KEY.POINAME);
			double lng = data.getDouble(ExternalDefaultBroadcastKey.KEY.LON);
			double lat = data.getDouble(ExternalDefaultBroadcastKey.KEY.LAT);
			int distance = data.getInt(ExternalDefaultBroadcastKey.KEY.DISTANCE);
			String addr = data.getString(ExternalDefaultBroadcastKey.KEY.ADDRESS);
			int type = data.getInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE);
			int isExist = data.getInt(ExternalDefaultBroadcastKey.KEY.RST);
			JNIHelper.logd("procHomeComAddr poiName:" + poiname + ",isExist:" + isExist);

			if (isExist == 0) { // 导航没有家和公司的地址
				if (type == 0) {
					NavManager.getInstance().clearHomeLocation();
				} else if (type == 1) {
					NavManager.getInstance().clearCompanyLocation();
				}
				return true;
			}

			if (type == 0) { // 家
				NavManager.getInstance().setHomeLocation(poiname, addr, lat, lng, UiMap.GPS_TYPE_GCJ02, false);
			} else if (type == 1) {
				NavManager.getInstance().setCompanyLocation(poiname, addr, lat, lng, UiMap.GPS_TYPE_GCJ02, false);
			}
			return true;
		}
		return false;
	}

	private void speakWordsWithClose(String words) {
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(words, null);
	}
	
	@Override
	public PathInfo getCurrentPathInfo() {
		// TODO 
		return null;
	}
	
	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		try {
			GpsInfo gpsInfo = info.msgGpsInfo;
			double lat = gpsInfo.dblLat;
			double lng = gpsInfo.dblLng;

			TNPoi poi = new TNPoi();
			TNLatLng tll = new TNLatLng(lat, lng);
			tll.setCoordinateSystem(TNLatLng.COORDINATE_SYSTEM_GCJ02);
			poi.naviCoordinate = tll;
			poi.address = info.strTargetAddress;
			poi.poiName = info.strTargetName;
			TNBroadcastSender.getInstance().naviTo(poi);
			return true;
		} catch (Exception e) {
		}
		return false;
	}

	@Override
	public void exitNav() {
		AppLogic.runOnBackGround(mExitNav, 0);
		// 隔一秒关闭导航APP
		AppLogic.runOnBackGround(mExitNavApp, 1000);
		// 反注册一下唤醒词
		onPause();
		
		// 退出了应用
		onExitApp();
	}

	@Override
	public void enterNav() {
		TNBroadcastSender.getInstance().openNaviApp(false);
	}

	Runnable mExitNav = new Runnable() {

		@Override
		public void run() {
			TNBroadcastSender.getInstance().closeNavi(1, false);
		}
	};

	Runnable mExitNavApp = new Runnable() {

		@Override
		public void run() {
			TNBroadcastSender.getInstance().exitNaviApp(false);
		}
	};

	@Override
	public void onResume() {
		NavSenderCycler.getInstance().onResume();
		super.onResume();
	}

	@Override
	public void onPause() {
		NavSenderCycler.getInstance().onPause();
		super.onPause();
	}
	
	@Override
	public void handleIntent(Intent intent) {
		TNBroadcastReceiver.getInstance().handleIntent(intent);
	}

	@Override
	public void onNavCommand(boolean fromWakeup, String type, String speech) {
		// TODO 之前比较老的写法
		if (AsrKeyType.EXIT_NAV.equals(type) || AsrKeyType.CLOSE_MAP.equals(type)) {
			JSONBuilder json = new JSONBuilder();
			json.put("scene", "nav");
			json.put("text", speech);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}

		JNIHelper.logd(getClass().getSimpleName() + " onNavCommand:[" + fromWakeup + "," + type + "," + speech + "]");

		if (AsrKeyType.ZOOM_IN.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_ZOOMIN", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().zoomMap(false);
				}
			}, false);
			return;
		}
		if (AsrKeyType.ZOOM_OUT.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_ZOOMOUT", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().zoomMap(true);
				}
			}, false);
			return;
		}
		if (AsrKeyType.NIGHT_MODE.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_NIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setStyle(false);
				}
			}, false);
			return;
		}
		if (AsrKeyType.LIGHT_MODE.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_LIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setStyle(true);
				}
			}, false);
			return;
		}
		if (AsrKeyType.OPEN_TRAFFIC.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_OPEN_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setTraffic(true);
				}
			}, false);
			return;
		}
		if (AsrKeyType.CLOSE_TRAFFIC.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_CLOSE_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setTraffic(false);
				}
			}, false);
			return;
		}
		if (AsrKeyType.TWO_MODE.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_TWO_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setCar(0);
				}
			}, false);
			return;
		}
		if (AsrKeyType.THREE_MODE.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_THREE_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setCar(1);
				}
			}, false);
			return;
		}
		if (AsrKeyType.CAR_DIRECT.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_CAR_DIRECT", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setCar(1);
				}
			},false);
			return;
		}
		if (AsrKeyType.NORTH_DIRECT.equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_NORTH_DIRECT", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setCar(0);
				}
			}, false);
			return;
		}
		if (AsrKeyType.VIEW_ALL.equals(type)) {
			mTmpSpeech = speech;
			String tts = NativeData.getResString("RS_MAP_VIEW_ALL");
			tts = tts.replace("%COMMAND%", speech);
			Runnable task = new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().showOverviewMap(true);
				}
			};
			
			// 版本号小于33用导航反馈
			if (getMapVersion() < 33) {
				task.run();
				return;
			}

			if (fromWakeup) {
				task.run();
				tts = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_VIEW_ALL");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, null);
			} else {
				tts = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_VIEW_ALL");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, task);
			}
			return;
		}
		if (AsrKeyType.HOW_NAVI.equals(type)) {
			// TODO 同时获取剩余时间和距离
			// TNBroadcastSender.getInstance().requestRemianTime();
			TNBroadcastSender.getInstance().requestRemianDistance();
			return;
		}
		if (AsrKeyType.ASK_REMAIN.equals(type)) {
			// TODO 同时获取剩余时间和距离
			// TNBroadcastSender.getInstance().requestRemianTime();
			TNBroadcastSender.getInstance().requestRemianDistance();
			return;
		}
		if ("AUTO_MODE".equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_AUTO_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setAutoStyle();
				}
			}, false);
			return;
		}
		if ("MEADWAR_MODE".equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_MEADWAR_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setSimpleStyle();
				}
			}, false);
			return;
		}
		if ("EXPORT_MODE".equals(type)) {
			doConfirmShow(type, speech, "RS_MAP_EXPERT_MODE", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().setDetailStyle();
				}
			}, false);
			return;
		}
		if (AsrKeyType.BACK_NAVI.equals(type)) {
			mTmpSpeech = speech;
			TNBroadcastSender.getInstance().showOverviewMap(false);
			// 版本号小于33用导航反馈
			if (getMapVersion() < 33) {
				return;
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_BACK_NAV");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (AsrKeyType.START_NAVI.equals(type)) {
			JNIHelper.logd("startNavi with close win");
			TNBroadcastSender.getInstance().startNavi();
			RecorderWin.close();
			return;
		}
		if (AsrKeyType.DUOBIYONGDU.equals(type)) {
			doRePlanWakeup(type, speech, "RS_MAP_DUOBIYONGDU", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().replanNaviStrategy(0);
					LogUtil.logd("NavTXNavImpl start DUOBIYONGDU");
				}
			});
			return;
		}
		if (AsrKeyType.GAOSUYOUXIAN.equals(type)) {
			doRePlanWakeup(type, speech, "RS_MAP_GAOSUYOUXIAN", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().replanNaviStrategy(3);
					LogUtil.logd("NavTXNavImpl start GAOSUYOUXIAN");
				}
			});
			return;
		}
		if (AsrKeyType.BUZOUGAOSU.equals(type)) {
			doRePlanWakeup(type, speech, "RS_MAP_BUZOUGAOSU", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().replanNaviStrategy(1);
					LogUtil.logd("NavTXNavImpl start BUZOUGAOSU");
				}
			});
			return;
		}
		if (AsrKeyType.LESS_MONEY.equals(type)) {
			doRePlanWakeup(type, speech, "RS_MAP_LESS_MONEY", new Runnable() {

				@Override
				public void run() {
					TNBroadcastSender.getInstance().replanNaviStrategy(2);
					LogUtil.logd("NavTXNavImpl start LESS_MONEY");
				}
			});
			return;
		}
		if (AsrKeyType.EXIT_NAV.equals(type)) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", exitNav);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}

			if (!enableWakeupExitNav) {
				return;
			}
			closeNav(type, speech);
			return;
		}
		if (AsrKeyType.CANCEL_NAV.equals(type)) {
			if (preNavCancelCommand(fromWakeup, speech)) {
				return;
			}
			if (!fromWakeup && !isInNav()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", exitNav);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						// NavManager.getInstance().exitAllNavTool();
						TNBroadcastSender.getInstance().closeNavi(1, false);
					}
				});
				return;
			}

			// closeNav(type, speech);
			endNav(type);
			return;
		}
		if (AsrKeyType.CLOSE_MAP.equals(type)) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			closeNav(type, speech);
			return;
		}
	}
	
	private void endNav(String type) {
		boolean needConfirm = true;
		if (processRemoteIsConfirm(type)) {
			needConfirm = false;
		}
		if (needConfirm) {
			TNBroadcastSender.getInstance().closeNavi(0, true);
		} else {
			TNBroadcastSender.getInstance().closeNavi(1, true);
		}
		// doExitConfirm(type, NativeData.getResString("RS_MAP_NAV_STOP"), new
		// Runnable() {
		//
		// @Override
		// public void run() {
		// TNBroadcastSender.getInstance().closeNavi(1, false);
		// }
		// });
	}

	private void closeNav(String type, String speech) {
		boolean needConfirm = true;
		if (processRemoteIsConfirm(type)) {
			needConfirm = false;
		}

		if (needConfirm) {
			TNBroadcastSender.getInstance().closeNavi(0, true);
		} else {
			// TODO 使用导航发过来的文本
			NavManager.getInstance().exitAllNavTool();
		}
	}

	@Override
	public List<String> getBanCmds() {
		int mapVersion = getMapVersion();
		List<String> bans = new ArrayList<String>();
		if (mapVersion >= 50) {// 对应腾讯导航版本2.1.0
			return bans;
		}
		bans.add(AsrKeyType.GAOSUYOUXIAN);
		bans.add(AsrKeyType.DUOBIYONGDU);
		bans.add(AsrKeyType.LESS_MONEY);
		bans.add(AsrKeyType.BUZOUGAOSU);
		return bans;
	}
	
	@Override
	public List<String> getCmdNavOnly() {
		int mapVersion = getMapVersion();
		LogUtil.logd("mapVersion:" + mapVersion);
		List<String> cmds = new ArrayList<String>();
		// 版本号这个版本以上才支持导航状态通知
		if (mapVersion >= 33) {
			cmds.add(AsrKeyType.VIEW_ALL);
		}
		if (mapVersion >= 50) {
			cmds.add(AsrKeyType.GAOSUYOUXIAN);
			cmds.add(AsrKeyType.DUOBIYONGDU);
			cmds.add(AsrKeyType.LESS_MONEY);
			cmds.add(AsrKeyType.BUZOUGAOSU);
		}
		return cmds;
	}

	@Override
	public String[] getSupportCmds() {
		return new String[] { 
				AsrKeyType.ZOOM_IN, 
				AsrKeyType.ZOOM_OUT, 
				AsrKeyType.NIGHT_MODE, 
				AsrKeyType.LIGHT_MODE,
				AsrKeyType.EXIT_NAV, 
				AsrKeyType.CANCEL_NAV, 
				AsrKeyType.CLOSE_MAP, 
				AsrKeyType.VIEW_ALL,
				AsrKeyType.DUOBIYONGDU, 
				AsrKeyType.BUZOUGAOSU, 
				AsrKeyType.GAOSUYOUXIAN, 
				AsrKeyType.LESS_MONEY,
				AsrKeyType.ASK_REMAIN,
				AsrKeyType.BACK_NAVI, 
				AsrKeyType.START_NAVI,
				AsrKeyType.OPEN_TRAFFIC, 
				AsrKeyType.CLOSE_TRAFFIC, 
				AsrKeyType.TWO_MODE, 
				AsrKeyType.THREE_MODE,
				AsrKeyType.CAR_DIRECT, 
				AsrKeyType.NORTH_DIRECT
				};
	}
	
	@Override
	public int onWakeupRegister(AsrComplexSelectCallback acsc) {
		if (acsc != null) {
			acsc.addCommand("AUTO_MODE", NativeData.getResStringArray("RS_NAV_TX_AUTO_MODE"));
			acsc.addCommand("MEADWAR_MODE", NativeData.getResStringArray("RS_NAV_TX_MEADWAR_MODE"));
			acsc.addCommand("EXPORT_MODE", NativeData.getResStringArray("RS_NAV_TX_EXPORT_MODE"));
		}
		return 5;
	}

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public void queryHomeCompanyAddr() {
		TNBroadcastSender.getInstance().queryAddr();
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		if (navigateInfo == null || navigateInfo.msgGpsInfo == null) {
			return;
		}
		String name = navigateInfo.strTargetName;
		String addr = navigateInfo.strTargetAddress;
		double lat = navigateInfo.msgGpsInfo.dblLat;
		double lng = navigateInfo.msgGpsInfo.dblLng;
		TNBroadcastSender.getInstance().setHomeAddr(name, lat, lng, addr);
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		if (navigateInfo == null || navigateInfo.msgGpsInfo == null) {
			return;
		}
		String name = navigateInfo.strTargetName;
		String addr = navigateInfo.strTargetAddress;
		double lat = navigateInfo.msgGpsInfo.dblLat;
		double lng = navigateInfo.msgGpsInfo.dblLng;
		TNBroadcastSender.getInstance().setCompanyAddr(name, lat, lng, addr);
	}

	private static class NavSenderCycler {
		List<NavDialogAsr> mNavAsrs = new ArrayList<NavTXNavImpl.NavDialogAsr>();

		private static NavSenderCycler sCycler = new NavSenderCycler();

		private NavSenderCycler() {
		}

		public static NavSenderCycler getInstance() {
			return sCycler;
		}

		public void onPause() {
			synchronized (mNavAsrs) {
				for (NavDialogAsr nda : mNavAsrs) {
					WakeupManager.getInstance().recoverWakeupFromAsr(nda.mNavDialogAsrId);
				}
			}
		}

		public void onResume() {
			synchronized (mNavAsrs) {
				for (NavDialogAsr nda : mNavAsrs) {
					WakeupManager.getInstance().useWakeupAsAsr(nda);
				}
			}
		}

		public void removeAll() {
			synchronized (mNavAsrs) {
				for (NavDialogAsr nda : mNavAsrs) {
					nda.onDismiss();
					mNavAsrs.remove(nda);
				}
			}
		}

		public void popTop() {
			synchronized (mNavAsrs) {
				if (!mNavAsrs.isEmpty()) {
					NavDialogAsr nda = mNavAsrs.remove(mNavAsrs.size() - 1);
					if (nda != null) {
						nda.onDismiss();
					}
				}
			}
		}

		public void dismiss(String taskId) {
			synchronized (mNavAsrs) {
				for (NavDialogAsr nda : mNavAsrs) {
					if (taskId.equals(nda.getTaskId())) {
						nda.onDismiss();
						mNavAsrs.remove(nda);
						break;
					}
				}
			}
		}

		public void addNavSender(NavDialogAsr nda) {
			synchronized (mNavAsrs) {
				for (NavDialogAsr asr : mNavAsrs) {
					if (asr.getTaskId().equals(nda.getTaskId())) {
						int pos = mNavAsrs.indexOf(asr);
						mNavAsrs.remove(pos);
						mNavAsrs.add(pos, nda);
						JNIHelper.logd("NavTX update Sender:" + nda.getTaskId());
						return;
					}
				}

				mNavAsrs.add(nda);
				JNIHelper.logd("NavTX add Sender:" + nda.getTaskId());
			}
		}
	}

	protected abstract class NavDialogAsr extends AsrComplexSelectCallback {
		boolean mIsShow;
		// String[]最后一个代表命令
		public String mNavDialogAsrId;

		public NavDialogAsr(String taskId) {
			this.mNavDialogAsrId = taskId;
		}

		public void addCmds(String type, String... keywords) {
			addCommand(type, keywords);
		}

		public void onShow() {
			if (mIsShow) {
				return;
			}

			mIsShow = true;
			WakeupManager.getInstance().useWakeupAsAsr(this);
		}

		public void onDismiss() {
			if (mIsShow) {
				mIsShow = false;
				WakeupManager.getInstance().recoverWakeupFromAsr(mNavDialogAsrId);
			}
		}

		@Override
		public boolean needAsrState() {
			return false;
		}

		@Override
		public String getTaskId() {
			return mNavDialogAsrId;
		}

		@Override
		public void onCommandSelected(String type, String command) {
			JNIHelper.logd("tx onCommandSelected :" + type);
			onSelect(type);
		}

		public abstract void onSelect(String type);
	}
}
