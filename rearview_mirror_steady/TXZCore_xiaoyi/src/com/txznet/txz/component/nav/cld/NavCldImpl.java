package com.txznet.txz.component.nav.cld;

import java.util.List;

import com.example.startcld.StartCld;
import com.txz.ui.app.UiApp.AppInfo;
import com.txz.ui.app.UiApp.AppInfoList;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.LocationUtil;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.util.runnables.Runnable4;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.text.TextUtils;

public class NavCldImpl extends NavThirdComplexApp {
	public static final int CLD_VOICE_ORDER_HOME = 1;
	public static final int CLD_VOICE_ORDER_COM = 2;
	public static final int CLD_VOICE_ORDER_REPEAT = 19;
	public static final int CLD_VOICE_ORDER_CANCEL = 20;
	public static final int CLD_ZOOM_IN = 26;
	public static final int CLD_ZOOM_OUT = 27;
	public static final int CLD_PLAN_XITONG = 36;
	public static final int CLD_PLAN_ZUIDUAN = 37;
	public static final int CLD_PLAN_SHAOZOUGAOSU = 38;
	public static final int CLD_PLAN_GAOSU = 39;
	public static final int CLD_VOICE_ORDER_EXIT = 40;
	public static final int CLD_VOICE_ORDER_SET_PASS_KCODE = 66; // 导航以K码设置经由地
	public static final int CLD_VOICE_ORDER_SET_AVOID_KCODE = 67; // 导航以K码设置回避地
	public static final int AUTO_CLD_IS_NAVI = 85;
	public static final int CLD_OPEN_TRAC = 89;
	public static final int CLD_CLOSE_TRAC = 90;
	public static final int CLD_VOICE_SET_HOME_ADDRESS = 132;
	public static final int CLD_VOICE_SET_COM_ADDRESS = 133;
	public static final int CLD_IS_NAVIPAGER = 136;
	public static final int CLD_CURRENT_PLAN = 137;
	public static final int CLD_IS_NAVI = 138;
	public static final int CLD_PLAN_START_END = 139;
	public static final int CLD_VOICE_GETCURRENT_MODE = 140;
	public static final int CLD_VOICE_ORDER_START = 141;
	public static final int CLD_VOICE_ORDER_CHANGEROUTEPLANCON = 142;
	public static final int CLD_ASK_REMAIN_DISTANCE = 143;
	public static final int CLD_VOICE_ORDER_BTN_OK = 144;
	public static final int CLD_VOICE_EXIT_DIRECT = 145;
	public static final int CLD_VOICE_QUXIAO = 146;
	public static final int CLD_VOICE_ORDER_SENDKCODEANDADDRNAME = 147;
	public static final int CLD_VOICE_ORDER_NAVIUPGRADE = 148;

	public static final int CLD_VOICE_ORDER_MAINORAUX = 149; // 主道还是辅道 1:主道 2:辅道
	public static final int CLD_VOICE_ORDER_CONTINUE_LASTNAVI = 150; // 续航提示 1:是
																		// 2:否
	public static final int CLD_VOICE_ORDER_DRIVEMODE = 151; // 进入行车模式
	public static final int CLD_VOICE_ORDER_SPANMODE = 152;
	public static final int CLD_VOICE_ORDER_FULLMODE = 153;
	public static final int CLD_VOICE_ORDER_SPEECH_TEXT = 154;

	private static String PACKAGE_NAME = "cld.navi.c3027.mainframe";
	private static String PACKAGE_NAME_VIP = "cld.navi.k3618.mainframe";

	public static String PACKAGE_NAME_REG = "cld.navi.";

	private int mNextPlan;
	private int mCurrentFlag;

	private boolean mHasSaveAddr;
	private boolean mIsBtOpen;
	private boolean mIsNaviUpGrade = false;

	// 凯立德330阈值为-3.65f
	public static float sAsrWakeupThresHold = WakeupManager.getInstance().getWakeupThreshhold();

	public NavCldImpl() {
		AppLogic.removeUiGroundCallback(mRefreshPackageNameRunnable);
		AppLogic.runOnUiGround(mRefreshPackageNameRunnable, 0);
		addStatusListener();
	}

	@Override
	public String getPackageName() {
		if (!TextUtils.isEmpty(mRemotePackageName)) {
			return mRemotePackageName;
		}

		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME_VIP)) {
			return PACKAGE_NAME_VIP;
		}
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME)) {
			return PACKAGE_NAME;
		}
		mRefreshPackageNameRunnable.run();
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME_VIP)) {
			return PACKAGE_NAME_VIP;
		}
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME)) {
			return PACKAGE_NAME;
		}
		return "";
	}

	@Override
	public boolean willNavAfterSet() {
		return true;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, final NavigateInfo info) {
		// 选路方式(1:系统推荐， 2:高速优先，3:最短路径， 4:最少收费)
		try {
			if (getPackageName() == PACKAGE_NAME_VIP) {
				Runnable2<NavPlanType, NavigateInfo> mRunnable = new Runnable2<NavPlanType, NavigateInfo>(plan, info) {

					@Override
					public void run() {
						int strategy = 1;
						switch (mP1) {
						case NAV_PLAN_TYPE_AVOID_JAMS:
							strategy = 1;
							break;
						case NAV_PLAN_TYPE_LEAST_COST:
							strategy = 4;
							break;
						case NAV_PLAN_TYPE_LEAST_DISTANCE:
							strategy = 3;
							break;
						case NAV_PLAN_TYPE_LEAST_TIME:
							strategy = 1;
							break;
						case NAV_PLAN_TYPE_RECOMMEND:
						default:
							strategy = 1;
							break;
						}

						if (mP2 == null) {
							throw new NullPointerException("NavigateInfo null！");
						}

						if (mEnableSave) {
							if (mPlanStyle != -1) {
								strategy = mPlanStyle;
							}
						}

						double lat = mP2.msgGpsInfo.dblLat;
						double lng = mP2.msgGpsInfo.dblLng;
						String name = mP2.strTargetName;
						naviDirect(strategy, lat, lng, name);
					}
				};

				AppLogic.runOnUiGround(mRunnable, 200);
				return true;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			PackageManager.getInstance().openApp(PACKAGE_NAME);
			Runnable mStartNav = new Runnable() {
				@Override
				public void run() {
					if (!PackageManager.getInstance().isAppRunning(PACKAGE_NAME)) {
						JNIHelper.logd("StartCld waiting");
						AppLogic.runOnUiGround(this, 200);
						return;
					}

					double[] dest = LocationUtil.getGCJ02(info.msgGpsInfo);
					StartCld navi = new StartCld(dest[0], dest[1], GlobalContext.get());
					navi.startNavi();
					JNIHelper.logd("StartCld end: " + dest[0] + "," + dest[1]);
				}
			};
			AppLogic.runOnUiGround(mStartNav, 200);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public void naviDirect(int type, double lat, double lng, String name) {
		JNIHelper.logd("NavCldImpl naviDirect");
		StringBuilder sb = new StringBuilder();
		sb.append("(TNC1");
		sb.append(type + ",");
		sb.append("D" + lat);
		sb.append("," + lng);
		sb.append("," + name);
		sb.append(")");
		Intent i = new Intent("android.NaviOne.CldStdTncReceiver");
		i.putExtra("CLDTNC", sb.toString());
		i.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		GlobalContext.get().sendBroadcast(i);
	}

	@Override
	public void addStatusListener() {
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					onProcReplayByCldBroadCast(intent);
				} catch (Exception e) {
				}
			}

		}, new IntentFilter("CLD.NAVI.MSG.VOICEORDER_REPLAY"));

		GlobalContext.get().registerReceiver(new NaviGuidanceReceiver() {

			@Override
			public void onNavInfoUpdate(CldDataStore cds) {
				procCldNaviInfo(cds);
			}
		}, new IntentFilter("CLD.NAVI.MSG.GUIDANCEINFO"));

		IntentFilter iFilter = new IntentFilter();
		iFilter.addAction("com.android.bt.connected");
		iFilter.addAction("com.android.bt.disconnected");
		iFilter.addAction("android.NaviOne.voiceprotocol");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				if (action.equals("com.android.bt.connected")) {
					mIsBtOpen = true;
				} else if (action.equals("com.android.bt.disconnected")) {
					mIsBtOpen = false;
				} else if ("android.NaviOne.voiceprotocol".equals(action)) {
					String voiceTxt = intent.getStringExtra("VOICEPROTOCOL");
					if (ProjectCfg.mEnableAEC) {
						return;
					}

					if (!TextUtils.isEmpty(voiceTxt)) {
						if (mIsTTs && "stop".equals(voiceTxt)) {
							mIsTTs = false;
							checkNaviTts();
							JNIHelper.logd("recv navi tts:" + voiceTxt);
						} else if (!mIsTTs && "play".equals(voiceTxt)) {
							mIsTTs = true;
							checkNaviTts();
							JNIHelper.logd("recv navi tts:" + voiceTxt);
						}
					}
				}
			}
		}, iFilter);

		checkNaviStatus();
		sendVoiceOrderMessage(CLD_IS_NAVIPAGER, null);
	}

	private boolean mIsTTs = false;
	private boolean mIsSupportWakeup = false;

	private void checkNaviTts() {
		if (mIsTTs) {
			mIsSupportWakeup = WakeupManager.getInstance().mEnableWakeup;
			JNIHelper.logd("isSupportWakeup:" + mIsSupportWakeup);
			WakeupManager.getInstance().enableWakeup(false);
		} else {
			WakeupManager.getInstance().enableWakeup(mIsSupportWakeup);
		}
	}

	private NavInfo mNavInfo;

	private void procCldNaviInfo(CldDataStore cds) {
		try {
			if (mNavInfo == null) {
				mNavInfo = new NavInfo();
			}

			mNavInfo.parseCldNavInfo(cds, getPackageName());
			broadNaviInfo(mNavInfo.toJson());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void enterNav() {
		if (mIsNaviUpGrade) {
			String spk = NativeData.getResString("RS_MAP_NAV_UPDATE");
			TtsUtil.speakText(spk);
			return;
		}
		super.enterNav();
	}

	public void checkNaviStatus() {
		if (enableWakeupExitNav) {
			sendVoiceOrderMessage(AUTO_CLD_IS_NAVI, null);
		}
	}

	@Override
	public void exitNav() {
		try {
			JNIHelper.logd("exitNav enableWakeupExitNav");
			if (getPackageName().equals("cld.navi.k3618.mainframe")) {
				sendVoiceOrderMessage(CLD_VOICE_EXIT_DIRECT, null);
			} else {
				if (!enableWakeupExitNav) {
					// TODO 取消导航路径，并退回主界面
					try {
						sendVoiceOrderMessage(CLD_VOICE_ORDER_CANCEL, null);
						backToSys(0, "back");
					} catch (Exception ep) {
					}
				} else {
					sendVoiceOrderMessage(CLD_VOICE_ORDER_EXIT, null);
					super.exitNav();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onNavCommand(final boolean fromWakeup, String type, final String speech) {
		if ("EXIT".equals(type)) {
			JSONBuilder json = new JSONBuilder();
			json.put("sence", "nav");
			json.put("text", speech);
			// json.put("keywords", keywords);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}

		JNIHelper.loge("NavCldImpl onNavCommand:[" + type + ":" + speech + "]");
		if (!enableWakeupExitNav && (("CANCEL".equals(type) || "QUXIAO".equals(type)) || "EXIT".equals(type))) { // 凯立德希望不允许退出导航
			do {
				if ("CANCEL".equals(type)) {
					break;
				}

				if (!fromWakeup) {
					String answer = NativeData.getResString("RS_VOICE_CAN_NOT_PROC_RESULT");
					RecorderWin.speakTextWithClose(
							answer.replace("%CONTENT%", NativeData.getResString("RS_VOICE_USUAL_SPEAK_GRAMMAR")), null);
					return;
				}

				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("", null);
				return;
			} while (false);
		}
		if (mIsNaviUpGrade) {
			String spk = NativeData.getResString("RS_MAP_NAV_UPDATE");
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk, null);
				return;
			} else {
				TtsManager.getInstance().speakText(spk);
			}
			return;
		}
		if (type.equals("ZOOM_OUT")) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String zoomOut = NativeData.getResString("RS_MAP_ZOOMOUT");
				String spk = NativeData.getResPlaceholderString(
						"RS_VOICE_WILL_DO_COMMAND", "%CMD%", zoomOut);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_ZOOM_OUT, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_ZOOM_OUT, null);
			}
			return;
		}
		if (type.equals("ZOOM_IN")) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String zoomIn = NativeData.getResString("RS_MAP_ZOOMIN");
				String spk = NativeData
						.getResString("RS_VOICE_WILL_DO_COMMAND").replace(
								"%CMD%", zoomIn);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_ZOOM_IN, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_ZOOM_IN, null);
			}
			return;
		}
		if (type.equals("ASK_ROUTE")) {
			if (!fromWakeup) {
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_VOICE_ORDER_REPEAT, null);
			return;
		}
		if (type.equals("XITONG")) {
			if (!mIsStarted) {
				planHintTTS(fromWakeup);
				return;
			}

			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String xiTong = NativeData.getResString("RS_PATH_XITONG");
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_SWITCH_PATH", "%CMD%", xiTong);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						mNextPlan = 1;
						sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
						return;
					}
				});
			} else {
				mNextPlan = 1;
				sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
			}
			return;
		}
		if (type.equals("LESS_DISTANCE")) {
			if (!mIsStarted) {
				planHintTTS(fromWakeup);
				return;
			}

			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String lessDistance = NativeData
						.getResString("RS_PATH_LESS_DISTANCE");
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_SWITCH_PATH", "%CMD%", lessDistance);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						mNextPlan = 8;
						sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
						return;
					}
				});
			} else {
				mNextPlan = 8;
				sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
			}
			return;
		}
		if (type.equals("LESS_MONEY")) {
			if (!mIsStarted) {
				planHintTTS(fromWakeup);
				return;
			}

			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String lessMoney = NativeData
						.getResString("RS_PATH_LESS_MONEY");
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_SWITCH_PATH", "%CMD%", lessMoney);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						mNextPlan = 16;
						sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
						return;
					}
				});
			} else {
				mNextPlan = 16;
				sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
			}
			return;
		}
		if (type.equals("GAOSU")) {
			if (!mIsStarted) {
				planHintTTS(fromWakeup);
				return;
			}

			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String gaosu = NativeData.getResString("RS_MAP_GAOSUYOUXIAN");
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_SWITCH_PATH", "%CMD%", gaosu);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						mNextPlan = 2;
						sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
						return;
					}
				});
			} else {
				mNextPlan = 2;
				sendVoiceOrderMessage(CLD_CURRENT_PLAN, null);
			}
			return;
		}
		if (type.equals("OPEN_TRAC")) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				RecorderWin.speakTextWithClose(text, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_OPEN_TRAC, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_OPEN_TRAC, null);
				String openTraffic = NativeData
						.getResString("RS_MAP_OPEN_TRAFFIC");
				String text = NativeData.getResPlaceholderString(
						"RS_VOICE_ALREAD_DO_COMMAND", "%CMD%", openTraffic);
				TtsManager.getInstance().speakText(text);
			}
			return;
		}
		if (type.equals("CLOSE_TRAC")) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String text = NativeData.getResString(
						"RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				RecorderWin.speakTextWithClose(text, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_CLOSE_TRAC, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_CLOSE_TRAC, null);
				String closeTraffic = NativeData
						.getResString("RS_MAP_CLOSE_TRAFFIC");
				String text = NativeData.getResPlaceholderString(
						"RS_VOICE_ALREAD_DO_COMMAND", "%CMD%", closeTraffic);
				TtsManager.getInstance().speakText(text);
			}
			return;
		}
		if (type.equals("YIJIANTONG")) {
			String hint = "";
			if (mIsBtOpen) {
				hint = NativeData.getResPlaceholderString(
						"RS_VOICE_WILL_DO_COMMAND", "%CMD%", speech);
			} else {
				if (!CallManager.getInstance().checkMakeCall()) {
					hint = NativeData.getResString("RS_BULETOOTH_NOT_CONNECT");
				}
			}

			if (!fromWakeup) {
				if (!enableWakeupExitNav) {
					String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
					RecorderWin.speakTextWithClose(text, new Runnable() {

						@Override
						public void run() {
							Intent intent = new Intent("CLD.NAVI.MSG.OKH.REGISTER");
							GlobalContext.get().sendBroadcast(intent);
						}
					});
					return;
				}

				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(hint, new Runnable() {

					@Override
					public void run() {
						if (!mIsBtOpen || !CallManager.getInstance().checkMakeCall()) {
							return;
						}

						Intent intent = new Intent("CLD.NAVI.MSG.OKH.REGISTER");
						GlobalContext.get().sendBroadcast(intent);
						return;
					}
				});
			} else {
				if (!enableWakeupExitNav) {
					Intent intent = new Intent("CLD.NAVI.MSG.OKH.REGISTER");
					GlobalContext.get().sendBroadcast(intent);
					return;
				}

				if (!mIsBtOpen && !CallManager.getInstance().checkMakeCall()) {
					String hintTxt = NativeData.getResString("RS_BULETOOTH_NOT_CONNECT");
					TtsUtil.speakText(hintTxt);
					return;
				}
				String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {

					public void onSuccess() {
						Intent intent = new Intent("CLD.NAVI.MSG.OKH.REGISTER");
						GlobalContext.get().sendBroadcast(intent);
					}
				});
			}
			return;
		}
		if (type.equals("ASK_REMAIN")) {
			if (!mIsStarted) {
				planHintTTS(fromWakeup);
				return;
			}

			if (!fromWakeup) {
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_ASK_REMAIN_DISTANCE, null);
			return;
		}
		if (type.equals("START_NAVI")) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_MAP_NAV_START");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_VOICE_ORDER_START, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_VOICE_ORDER_START, null);
			}
			return;
		}
		if (type.equals("CHANGE_ROUTE")) {
			if (!mIsStarted) {
				planHintTTS(fromWakeup);
				return;
			}

			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				RecorderWin.speakTextWithClose(text, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_VOICE_ORDER_CHANGEROUTEPLANCON, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_VOICE_ORDER_CHANGEROUTEPLANCON, null);
			}
			return;
		}
		if (type.equals("GOHOME")) {
			if (!fromWakeup) {
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_VOICE_ORDER_HOME, null);
			return;
		}
		if (type.equals("GOCOMPANY")) {
			if (!fromWakeup) {
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_VOICE_ORDER_COM, null);
			return;
		}
		if (type.equals("CANCEL") /* && !mIsNavi */) {
			// if (!mIsStarted) {
			// planHintTTS(fromWakeup);
			// return;
			// }
			if (!mIsFocus) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("", null);
				return;
			}

			if (!fromWakeup) {
				String hint = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(hint, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_VOICE_ORDER_CANCEL, null);
						if (!enableWakeupExitNav) {
							try {
								backToSys(1000, "back");
							} catch (Exception ep) {
							}
						}
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_VOICE_ORDER_CANCEL, null);
				if (!enableWakeupExitNav) {
					try {
						backToSys(0, "back");
					} catch (Exception ep) {
					} finally {
						String text = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", speech);
						TtsManager.getInstance().speakText(text);
					}
				}
			}
			return;
		}
		if (type.equals("QUXIAO")) {
			if (!fromWakeup) {
				String hint = NativeData.getResString("RS_MAP_CONTINUTE_SERVICE");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(hint, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_VOICE_QUXIAO, null);
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_VOICE_QUXIAO, null);
			}
			return;
		}
		if (type.equals("EXIT")) {
			if (!fromWakeup) {
				String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
				String hint = NativeData.getResPlaceholderString(
						"RS_VOICE_WILL_DO_COMMAND", "%CMD%", exitNav);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(hint, new Runnable() {

					@Override
					public void run() {
						exitNav();
					}
				});
			} else {
				exitNav();
			}
			return;
		}
		if (type.equals("OK_DIALOG")) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
				String spk = NativeData.getResPlaceholderString(
						"RS_VOICE_WILL_DO_COMMAND", "%CMD%", exitNav);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						sendVoiceOrderMessage(CLD_VOICE_ORDER_BTN_OK, null);
						return;
					}
				});
			} else {
				sendVoiceOrderMessage(CLD_VOICE_ORDER_BTN_OK, null);
			}
			return;
		}
		if ("NOT_SURE".equals(type)) {
			if (!fromWakeup) {
				RecorderWin.close();
			}
			return;
		}
		if ("CAR_MODE".equals(type)) {
			if (!fromWakeup) {
				backToSys(0, "Dismiss");
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_VOICE_ORDER_DRIVEMODE, null);
			return;
		}
		if ("SPAN_MODE".equals(type)) {
			if (!fromWakeup) {
				backToSys(0, "Dismiss");
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_VOICE_ORDER_SPANMODE, null);
			return;
		}
		if ("FULL_MODE".equals(type)) {
			if (!fromWakeup) {
				backToSys(0, "Dismiss");
				RecorderWin.close();
			}
			sendVoiceOrderMessage(CLD_VOICE_ORDER_FULLMODE, null);
			return;
		}
	}

	public static void sendVoiceOrderMessage(int actiontype, String[] param) {
		JNIHelper.logd("sendVoiceOrderMessage:" + actiontype);
		Bundle bundle = new Bundle();
		bundle.putInt("VOICEORDER_ACTION_TYPE", actiontype);
		if (param != null) {
			bundle.putStringArray("VOICEORDER_ARRAY_PARAM", param);
		}
		Intent intent = new Intent("CLD.NAVI.MSG.VOICEORDER");
		intent.putExtras(bundle);
		GlobalContext.get().sendBroadcast(intent);

		if (RecorderWin.isOpened()) {
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					RecorderWin.close();
				}
			}, 0);
		}
	}

	private void checkPlan() {
		switch (mNextPlan) {
		case 1:
			mPlanStyle = 1;
			sendVoiceOrderMessage(CLD_PLAN_XITONG, null);
			break;

		case 2:
			mPlanStyle = 2;
			sendVoiceOrderMessage(CLD_PLAN_GAOSU, null);
			break;

		case 8:
			mPlanStyle = 3;
			sendVoiceOrderMessage(CLD_PLAN_ZUIDUAN, null);
			break;

		case 16:
			mPlanStyle = 4;
			sendVoiceOrderMessage(CLD_PLAN_SHAOZOUGAOSU, null);
			break;
		}

		if (!enableWakeupExitNav && mCurrentFlag != 0) {
			String replan = NativeData.getResString("RS_MAP_PATH_REPLAN");
			String text = NativeData.getResPlaceholderString(
					"RS_VOICE_ALREAD_DO_COMMAND", "%CMD%", replan);
			TtsManager.getInstance().speakText(text);
		}
	}

	private void onProcReplayByCldBroadCast(Intent intent) {
		String action = intent.getAction();
		if (action.equals("CLD.NAVI.MSG.VOICEORDER_REPLAY")) {
			Bundle bundle = intent.getExtras();
			if (bundle == null) {
				return;
			}

			int actionType = bundle.getInt("VOICEORDER_ACTION_TYPE");
			String[] params = bundle.getStringArray("VOICEORDER_ARRAY_PARAM");
			try {
				switch (actionType) {
				case CLD_IS_NAVI:
					if (params != null) {
						int flag = Integer.parseInt(params[1]);
						if (flag == 0) {
							// 处于导航中
							onStart();
						} else {
							// 不处于导航中
							Long distance = CldDataStore.getInstance().lRemainDistance;
							if (distance != null && distance < 500) {
								onEnd(true);
							} else {
								onEnd(false);
							}
						}
						JNIHelper.loge("NavCldImpl mIsNavi:" + mIsStarted);
					}
					break;
				case AUTO_CLD_IS_NAVI:
					if (params != null) {
						int flag = Integer.parseInt(params[1]);
						if (flag == 1) {
							// 处于导航中
							onStart();
						} else {
							// 不处于导航中
							Long distance = CldDataStore.getInstance().lRemainDistance;
							if (distance != null && distance < 500) {
								onEnd(true);
							} else {
								onEnd(false);
							}
						}
						JNIHelper.loge("NavCldImpl mIsNavi:" + mIsStarted);
					}
					break;

				case CLD_IS_NAVIPAGER:
					if (params != null) {
						int flag = Integer.parseInt(params[1]);
						if (flag == 0) {
							// 处于导航可见
							onResume();
						} else {
							// 不处于导航可见
							onPause();
						}
						JNIHelper.loge("NavCldImpl mIsNaviPager:" + mIsFocus);
					}
					break;

				case CLD_CURRENT_PLAN:
					if (params != null) {
						int flag = Integer.parseInt(params[1]);
						JNIHelper.logd("NavCldImpl mCurrentPlan:" + flag);
						mCurrentFlag = flag;
						checkPlan();
					}
					break;

				case CLD_PLAN_START_END:
					break;

				case CLD_VOICE_GETCURRENT_MODE:
					if (params != null) {
						String mode = params[1];
						if ("B03".equals(mode)) {
							// 开始导航
							sendVoiceOrderMessage(CLD_VOICE_ORDER_START, null);
						}
					}
					break;

				case CLD_VOICE_ORDER_SENDKCODEANDADDRNAME:
					if (params != null) {
						String type = params[1];
						String act = params[2];
						String kcode = params[3];
						String addrName = params[4];
						mHasSaveAddr = true;
						if (type.equals("0")) {
							if (act.equals("1")) {
								clearShare("HomeAddr");
								return;
							}
							updateNaviInfo("HomeAddr", null, null, addrName, kcode);
						} else if (type.equals("1")) {
							if (act.equals("1")) {
								clearShare("CompanyAddr");
								return;
							}
							updateNaviInfo("CompanyAddr", null, null, addrName, kcode);
						}
						mHasSaveAddr = false;
					}
					break;

				case CLD_VOICE_ORDER_NAVIUPGRADE:
					if (params != null) {
						String cmd = params[1];
						if ("1".equals(cmd)) {
							// 开始升级
							mIsNaviUpGrade = true;
							JNIHelper.logd("receiver naviUpgrade");
						} else if ("2".equals(cmd)) {
							// 结束升级
							mIsNaviUpGrade = false;
							JNIHelper.logd("receiver finish naviUpgrade");
						}
					}
					break;

				case CLD_VOICE_ORDER_MAINORAUX:
					if (params != null && params.length > 1) {
						String showHide = params[1];
						JNIHelper.logd("149 order showHide:" + showHide);
						if ("1".equals(showHide)) {
							// 显示
							if (mNavDialogAsr == null) {
								mNavDialogAsr = new NavDialogAsr("TASK_ROAD_ID");
							}
							mNavDialogAsr.updateTaskId("TASK_ROAD_ID");
							mNavDialogAsr.onShow();
						} else {
							// 隐藏
							if (mNavDialogAsr != null) {
								mNavDialogAsr.onDismiss();
							}
						}
					}
					break;

				case CLD_VOICE_ORDER_CONTINUE_LASTNAVI:
					if (params != null && params.length > 1) {
						String showHide = params[1];
						JNIHelper.logd("150 order showHide:" + showHide);
						if ("1".equals(showHide)) {
							// 显示
							if (mNavDialogAsr == null) {
								mNavDialogAsr = new NavDialogAsr("TASK_LASTNAVI_ID");
							}
							mNavDialogAsr.updateTaskId("TASK_LASTNAVI_ID");
							mNavDialogAsr.onShow();
						} else {
							// 隐藏
							if (mNavDialogAsr != null) {
								mNavDialogAsr.onDismiss();
							}
						}
					}
					break;

				case CLD_VOICE_ORDER_SPEECH_TEXT:
					if (params != null && params.length > 1) {
						String spTxt = params[1];
						JNIHelper.logd("spTxt:" + spTxt);
						if (!TextUtils.isEmpty(spTxt)) {
							TtsManager.getInstance().speakText(spTxt);
						}
					}
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}

			checkInNav();
		}
	}

	private void planHintTTS(boolean fromWakeup) {
		String hint = NativeData.getResString("RS_MAP_PLAN_FIRST");
		if (!fromWakeup) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, new Runnable() {

				@Override
				public void run() {
				}
			});
		} else {
			TtsManager.getInstance().speakText(hint);
		}
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		if (navigateInfo == null || navigateInfo.msgGpsInfo == null) {
			return;
		}

		double lat = navigateInfo.msgGpsInfo.dblLat;
		double lng = navigateInfo.msgGpsInfo.dblLng;
		String name = navigateInfo.strTargetName;
		saveAddr("HomeAddr", "", name, lat, lng);
		if (mHasSaveAddr || PackageManager.getInstance().isAppRunning(getPackageName())) {
			sendNavigatePoiInfo(CLD_VOICE_SET_HOME_ADDRESS, lat, lng, name);
		}
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		if (navigateInfo == null || navigateInfo.msgGpsInfo == null) {
			return;
		}

		double lat = navigateInfo.msgGpsInfo.dblLat;
		double lng = navigateInfo.msgGpsInfo.dblLng;
		String name = navigateInfo.strTargetName;
		saveAddr("CompanyAddr", "", name, lat, lng);
		if (mHasSaveAddr || PackageManager.getInstance().isAppRunning(getPackageName())) {
			sendNavigatePoiInfo(CLD_VOICE_SET_COM_ADDRESS, lat, lng, name);
		}
	}

	Runnable4<Integer, Double, Double, String> mSetDestRunnable = null;

	public void sendNavigatePoiInfo(int actionType, double lat, double lng, String name) {
		String[] params = new String[4];
		params[0] = "1.0";
		params[1] = name;
		params[2] = String.valueOf(lat);
		params[3] = String.valueOf(lng);
		sendVoiceOrderMessage(actionType, params);
	}

	private void saveAddr(String preName, String kcode, String name, double lat, double lng) {
		SharedPreferences mPreferences = AppLogic.getApp().getSharedPreferences(preName, Context.MODE_WORLD_READABLE);
		if (mPreferences != null) {
			Editor mEditor = mPreferences.edit();
			mEditor.clear();
			if (kcode != null && !kcode.equals("") && !kcode.equals("null")) {
				mEditor.putString("kcode", kcode);
			} else {
				mEditor.putString("kcode", "null");
			}

			mEditor.putString("lat", String.valueOf(lat));
			mEditor.putString("lng", String.valueOf(lng));
			mEditor.putString("addrName", name);

			mEditor.commit();
		}
	}

	public boolean naviHome(String hint) {
		return naviShared("HomeAddr", hint);
	}

	public boolean naviCompany(String hint) {
		return naviShared("CompanyAddr", hint);
	}

	private boolean naviShared(String preName, String hint) {
		SharedPreferences shared = GlobalContext.get().getSharedPreferences(preName, Context.MODE_WORLD_READABLE);
		final String kcode = shared.getString("kcode", "");
		final String addr = shared.getString("addrName", "");
		if (kcode == null || kcode.equals("") || kcode.equals("null")) {
			final String lat = shared.getString("lat", "");
			final String lng = shared.getString("lng", "");
			if (lat != null && lng != null && !lat.equals("") && !lng.equals("") && !lat.equals("null")
					&& !lng.equals("null")) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(hint, new Runnable() {
					@Override
					public void run() {
						naviDirect(1, Double.parseDouble(lat), Double.parseDouble(lng), addr);
					}
				});
				return true;
			} else {
			}

			return false;
		} else {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, new Runnable() {
				@Override
				public void run() {
					naviByKCode(1, kcode, addr);
				}
			});
			return true;
		}
	}

	private void updateNaviInfo(String preName, String lat, String lng, String name, String kcode) {
		JNIHelper.loge("updateNaviInfo:" + preName + ",addrName:" + name + ",kcode:" + kcode);
		SharedPreferences preferences = GlobalContext.get().getSharedPreferences(preName, Context.MODE_WORLD_READABLE);
		if (preferences != null) {
			Editor editor = preferences.edit();
			if (editor != null) {
				if (kcode != null && !kcode.equals("") && !kcode.equals("null")) {
					editor.putString("kcode", kcode);
				} else {
					editor.putString("kcode", "");
				}

				editor.putString("lat", lat);
				editor.putString("lng", lng);
				editor.putString("addrName", name);
				editor.commit();
				JNIHelper.loge("updateNaviInfo: commit");
			} else {
				JNIHelper.loge("getEditor fail!");
			}
		} else {
			JNIHelper.loge("getSharedPreferences fail!");
		}
	}

	private void clearShare(String preName) {
		JNIHelper.logd("clearShare preName:" + preName);
		SharedPreferences preferences = GlobalContext.get().getSharedPreferences(preName, Context.MODE_WORLD_READABLE);
		if (preferences != null) {
			Editor editor = preferences.edit();
			if (editor != null) {
				editor.clear();
				editor.commit();
			}
		}

		if (preName.equals("HomeAddr")) {
			NavManager.getInstance().clearHomeLocation();
		} else if (preName.equals("CompanyAddr")) {
			NavManager.getInstance().clearCompanyLocation();
		}
	}

	private void naviByKCode(int type, String kCode, String name) {
		JNIHelper.loge("naviByKCode");
		StringBuilder sb = new StringBuilder();
		sb.append("(TNC,");
		sb.append("002,");
		sb.append(type + ",");
		sb.append(kCode + ",");
		sb.append(name + ",,,");
		sb.append("A,H)");
		Intent i = new Intent("android.NaviOne.CldStdTncReceiver");
		i.putExtra("CLDTNC", sb.toString());
		GlobalContext.get().sendBroadcast(i);
	}

	//////////////////////// 凯立德需求
	static Runnable mRefreshPackageNameRunnable = new Runnable() {

		@Override
		public void run() {
			AppInfoList infoList = PackageManager.getInstance().getAppList();
			AppInfo[] appInfos = infoList.rptMsgApps;
			for (AppInfo info : appInfos) {
				if (info.strPackageName.matches("cld.navi.c\\d+.mainframe")
						&& info.strPackageName.equals("cld.navi.c3027.mainframe")) {
					PACKAGE_NAME = info.strPackageName;
					break;
				}

				if (info.strPackageName.matches("cld.navi.k\\d+.mainframe")
						|| info.strPackageName.matches("cld.navi.p\\d+.mainframe")
						|| info.strPackageName.matches("cld.navi.m\\d+.mainframe")
						|| info.strPackageName.matches("cld.navi.c\\d+.mainframe")) {
					PACKAGE_NAME_VIP = info.strPackageName;
					break;
				}
			}
		}
	};

	public boolean enableExitNav() {
		return enableWakeupExitNav;
	}

	@Override
	public byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if (command.equals("cldpkn")) {
			try {
				setRemotePackageName(new String(data));
			} catch (Exception e) {
			}
			return null;
		}

		return super.invokeTXZNav(packageName, command, data);
	}

	private NavDialogAsr mNavDialogAsr;

	public class NavDialogAsr {
		boolean mIsShow;
		String mNavDialogAsrId;

		public NavDialogAsr(String taskId) {
			this.mNavDialogAsrId = taskId;
		}

		public void updateTaskId(String taskId) {
			if (!TextUtils.isEmpty(mNavDialogAsrId) && !mNavDialogAsrId.equals(taskId)) {
				onDismiss();
			}

			mNavDialogAsrId = taskId;
		}

		public void onShow() {
			if (mIsShow) {
				return;
			}

			mIsShow = true;
			AsrComplexSelectCallback acsc = new AsrComplexSelectCallback() {

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
					JNIHelper.logd("NavDialogAsr:" + type);
					if ("CMD_OK".equals(type)) {
						String[] param = new String[2];
						param[0] = new String("1.0");
						param[1] = new String("1");
						sendVoiceOrderMessage(CLD_VOICE_ORDER_CONTINUE_LASTNAVI, param);
					}
					if ("CMD_NO".equals(type)) {
						String[] param = new String[2];
						param[0] = new String("1.0");
						param[1] = new String("2");
						sendVoiceOrderMessage(CLD_VOICE_ORDER_CONTINUE_LASTNAVI, param);
					}
					if ("CMD_MAIN".equals(type)) {
						String[] param = new String[2];
						param[0] = new String("1.0");
						param[1] = new String("1");
						sendVoiceOrderMessage(CLD_VOICE_ORDER_MAINORAUX, param);
					}
					if ("CMD_SECO".equals(type)) {
						String[] param = new String[2];
						param[0] = new String("1.0");
						param[1] = new String("2");
						sendVoiceOrderMessage(CLD_VOICE_ORDER_MAINORAUX, param);
					}
				}
			};

			if ("TASK_LASTNAVI_ID".equals(mNavDialogAsrId)) {
				acsc.addCommand("CMD_OK", "继续");
				acsc.addCommand("CMD_NO", "取消");
			}

			if ("TASK_ROAD_ID".equals(mNavDialogAsrId)) {
				acsc.addCommand("CMD_MAIN", "主道");
				acsc.addCommand("CMD_SECO", "辅道");
			}

			WakeupManager.getInstance().useWakeupAsAsr(acsc);
		}

		public void onDismiss() {
			mIsShow = false;
			WakeupManager.getInstance().recoverWakeupFromAsr(mNavDialogAsrId);
		}
	}

	private void backToSys(long delay, String cmd) {
		JNIHelper.logd("backToSys");
		Intent intent = new Intent("com.txznet.txz.ACTION");
		intent.putExtra("CMD", cmd);
		intent.putExtra("Delay", delay);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public List<String> getBanCmds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] getSupportCmds() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int onWakeupRegister(AsrComplexSelectCallback acsc) {
		int count = 0;
		if (enableWakeupExitNav) {
			addWakeupCommand(acsc, "ZOOM_OUT", new String[] { "缩小" });
			addWakeupCommand(acsc, "ZOOM_IN", new String[] { "放大" });
			addWakeupCommand(acsc, "ASK_ROUTE", new String[] { "前面怎么走", "前面哪条路", "前面往哪走" });
			addWakeupCommand(acsc, "OPEN_TRAC", new String[] { "打开路况" });
			addWakeupCommand(acsc, "CLOSE_TRAC", new String[] { "关闭路况" });
			addWakeupCommand(acsc, "START_NAVI", new String[] { "开始导航" });
			addWakeupCommand(acsc, "XITONG", new String[] { "系统推荐", "推荐路线", "推荐路径" });
			addWakeupCommand(acsc, "LESS_DISTANCE", new String[] { "少路程", "短路程", "距离最近", "距离优先" });
			addWakeupCommand(acsc, "CHANGE_ROUTE", new String[] { "换一条路" });
			addWakeupCommand(acsc, "LESS_MONEY", new String[] { "少收费", "少走高速", "躲避高速" });
			addWakeupCommand(acsc, "GAOSU", new String[] { "高速优先", "走高速", "选择高速" });
			addWakeupCommand(acsc, "ASK_REMAIN", new String[] { "还有多远", "还有多久", "还要多久" });
			addWakeupCommand(acsc, "YIJIANTONG",
					new String[] { "呼叫一键通", "拨打一键通", "拨打一键导航", "拨打一键通客服", "一键通导航", "客服导航" });
			addWakeupCommand(acsc, "NOT_SURE", new String[] { "不确定", "不取消" });
			addWakeupCommand(acsc, "OK_DIALOG", new String[] { "确定" });
			addWakeupCommand(acsc, "CANCEL", new String[] { "结束导航", "停止导航" });
			addWakeupCommand(acsc, "QUXIAO", new String[] { "取消" });
			addWakeupCommand(acsc, "EXIT", new String[] { "退出导航", "关闭导航" });
			count = 18;
		} else {
			addWakeupCommand(acsc, "ZOOM_OUT", new String[] { "缩小", "缩小地图", "地图缩小" });
			addWakeupCommand(acsc, "ZOOM_IN", new String[] { "放大", "放大地图", "地图放大" });
			addWakeupCommand(acsc, "OPEN_TRAC", new String[] { "打开路况" });
			addWakeupCommand(acsc, "CLOSE_TRAC", new String[] { "关闭路况" });
			addWakeupCommand(acsc, "START_NAVI", new String[] { "开始导航" });
			addWakeupCommand(acsc, "XITONG", new String[] { "推荐路线", "推荐路径" });
			addWakeupCommand(acsc, "LESS_DISTANCE", new String[] { "距离优先" });
			addWakeupCommand(acsc, "CHANGE_ROUTE", new String[] { "换条路", "换一条路" });
			addWakeupCommand(acsc, "LESS_MONEY", new String[] { "少收费", "少走高速" });
			addWakeupCommand(acsc, "GAOSU", new String[] { "高速优先" });
			addWakeupCommand(acsc, "ASK_REMAIN", new String[] { "还有多远", "还要多久到" });
			addWakeupCommand(acsc, "YIJIANTONG", new String[] { "呼叫一键通", "拨打一键通" });
			addWakeupCommand(acsc, "CANCEL", new String[] { "结束导航", "停止导航", "退出导航", "关闭导航" });
			addWakeupCommand(acsc, "CAR_MODE", new String[] { "行车模式" });
			addWakeupCommand(acsc, "SPAN_MODE", new String[] { "分屏模式" });
			addWakeupCommand(acsc, "FULL_MODE", new String[] { "全屏模式" });
			count = 16;
		}

		// K3618导航场景最优阈值由外部设置
		if (getPackageName().equals("cld.navi.k3618.mainframe")) {
			WakeupManager.getInstance().setWakeupThreshhold(sAsrWakeupThresHold);
		}

		return count;
	}

	@Override
	protected void onUnRegisterCmds() {
		// K3618非导航场景最优阈值
		if (getPackageName().equals("cld.navi.k3618.mainframe")) {
			WakeupManager.getInstance().setWakeupThreshhold(-3.1f);
		}
	}
}