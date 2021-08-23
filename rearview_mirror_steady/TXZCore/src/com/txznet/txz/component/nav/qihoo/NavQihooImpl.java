package com.txznet.txz.component.nav.qihoo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.json.JSONObject;

import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.ui.dialog2.WinConfirmAsr.WinConfirmAsrBuildData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.txz.component.nav.IMapInterface.PlanStyle;
import com.txznet.txz.component.nav.INavHighLevelInterface;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.LocationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

public class NavQihooImpl extends NavThirdComplexApp implements INavHighLevelInterface {
	public static final String ACTION_NAVI_START = "com.qihoo.map.action.navistart";
	public static final String ACTION_NAVI_END = "com.qihoo.map.action.naviend";
	public static final String ACTION_PATH_START = "com.qihoo.map.action.getpathstart";
	public static final String ACTION_PATH_SUCC = "com.qihoo.map.action.getpathsuccesful";
	public static final String ACTION_PATH_FAIL = "com.qihoo.map.action.getpathfail";
	public static final String ACTION_MAP_ACTIVE = "com.qihoo.map.action.active";
	public static final String ACTION_MAP_PAUSE = "com.qihoo.map.action.suspend";
	public static String PACKAGE_NAME = "com.qihoo.map360.auto";

	public static final String ACTION_COMMON_CHANGED = "com.qihoo.map.action.commonaddresschanged";

	private QihooMapInterface mQihooMap;
	protected boolean mIsNav;
	
	private boolean mNorthDirect;
	private boolean mNowZoomIn;

	public NavQihooImpl() {
		addStatusListener();
	}

	@Override
	public void addStatusListener() {
		mQihooMap = new QihooMapInterface();
		mQihooMap.initialize();
		mQihooMap.setPackageName(PACKAGE_NAME);

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_NAVI_START);
		filter.addAction(ACTION_NAVI_END);
		filter.addAction(ACTION_PATH_START);
		filter.addAction(ACTION_PATH_SUCC);
		filter.addAction(ACTION_PATH_FAIL);
		filter.addAction(ACTION_MAP_ACTIVE);
		filter.addAction(ACTION_MAP_PAUSE);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				JNIHelper.logd("Qihoo action:" + action);
				if (ACTION_NAVI_START.equals(action)) {
					onStart();
				} else if (ACTION_NAVI_END.equals(action)) {
					onEnd(false);
				} else if (ACTION_PATH_START.equals(action)) {

				} else if (ACTION_PATH_SUCC.equals(action)) {
					onPlanComplete();
				} else if (ACTION_PATH_FAIL.equals(action)) {
					onPlanError(0, "");
				} else if (ACTION_MAP_ACTIVE.equals(action)) {
					onResume();
				} else if (ACTION_MAP_PAUSE.equals(action)) {
					onPause();
				}
			}
		}, filter);

		IntentFilter commAddrFilter = new IntentFilter();
		commAddrFilter.addAction(ACTION_COMMON_CHANGED);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				JNIHelper.logd("Qihoo action:" + action);
				if (ACTION_COMMON_CHANGED.equals(action)) {
					String actionvalue = intent.getStringExtra("actionvalue");
					if (!TextUtils.isEmpty(actionvalue)) {
						try {
							JSONObject jObj = new JSONObject(actionvalue);
							int status = jObj.optInt("status");
							int commonaddress = jObj.optInt("commonaddress");
							if (status == 0) {
								if (commonaddress == 0) {
									NavManager.getInstance().clearHomeLocation();
								} else if (commonaddress == 1) {
									NavManager.getInstance().clearCompanyLocation();
								}
							} else {
								String poi_name = jObj.optString("poi_name");
								double poi_lat = jObj.optDouble("poi_lat");
								double poi_lng = jObj.optDouble("poi_lng");
								if (commonaddress == 0) {
									NavManager.getInstance().setHomeLocation(poi_name, poi_name, poi_lat, poi_lng,
											UiMap.GPS_TYPE_GCJ02, false);
								} else if (commonaddress == 1) {
									NavManager.getInstance().setCompanyLocation(poi_name, poi_name, poi_lat, poi_lng,
											UiMap.GPS_TYPE_GCJ02, false);
								}
							}
						} catch (Exception e) {

						}
					}
				}
			}
		}, commAddrFilter);
	}

	@Override
	public void onStart() {
		mIsStarted = true;
		mIsNav = true;
		// mIsOnFocus = true;
		checkInNav();
	}

	@Override
	public void onEnd(boolean arrive) {
		mIsStarted = false;
		mIsNav = false;
		// mIsOnFocus = false;
		checkInNav();
	}

	@Override
	public void onPlanComplete() {
		mIsStarted = true;
		checkInNav();
	}

	@Override
	public void onPlanError(int errCode, String errDesc) {
		mIsStarted = false;
		checkInNav();
	}

	@Override
	public void onResume() {
		mIsFocus = true;
		checkInNav();
	}

	@Override
	public void onPause() {
		mIsFocus = false;
		checkInNav();
	}

	@Override
	public boolean isInNav() {
		return mIsStarted && mIsFocus;
	}

	@Override
	public void onNavCommand(boolean fromWakeup, String cmd, final String speech) {
		if ("EXIT_NAV".equals(cmd)) {
			JSONBuilder json = new JSONBuilder();
			json.put("scene", "nav");
			json.put("text", speech);
			// json.put("keywords", keywords);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}
		
		JNIHelper.logd("NavAmapAutoNavImpl cmd:" + cmd);
		if ("ZOOM_IN".equals(cmd)) {
			mQihooMap.zoomMap(true);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("ZOOM_OUT".equals(cmd)) {
			mQihooMap.zoomMap(false);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("NIGHT_MODE".equals(cmd)) {
			mQihooMap.switchLightNightMode(false);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String night = NativeData.getResString("RS_MAP_NIGHT_MODE");
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", night);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("LIGHT_MODE".equals(cmd)) {
			mQihooMap.switchLightNightMode(true);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String light = NativeData.getResString("RS_MAP_LIGHT_MODE");
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", light);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("OPEN_TRAFFIC".equals(cmd)) {
			mQihooMap.switchTraffic(true);
			JNIHelper.logd("NavAmapAutoNavImpl start OPEN_TRAFFIC");
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("CLOSE_TRAFFIC".equals(cmd)) {
			mQihooMap.switchTraffic(false);
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("TWO_MODE".equals(cmd)) {
			try {
				int sv = 0;
				if (mNorthDirect) {
					sv = 1;
				} else {
					sv = 0;
				}
				mQihooMap.switch23D(true, sv);
			} catch (Exception e) {
			}

			AsrManager.getInstance().setNeedCloseRecord(true);
			String two = NativeData.getResString("RS_MAP_TWO_MODE");
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", two);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("THREE_MODE".equals(cmd)) {
			try {
				mNorthDirect = false;
				mQihooMap.switch23D(false, 0);
			} catch (Exception e) {
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			String three = NativeData.getResString("RS_MAP_THREE_MODE");
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", three);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("CAR_DIRECT".equals(cmd)) {
			try {
				mNorthDirect = false;
				mQihooMap.switchCarDirection();
			} catch (Exception e) {
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			String carDirect = NativeData.getResString("RS_MAP_CAR_DIRECT");
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", carDirect);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("NORTH_DIRECT".equals(cmd)) {
			try {
				mNorthDirect = true;
				mQihooMap.switchNorthDirection();
			} catch (Exception e) {
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			String northDirect = NativeData.getResString("RS_MAP_NORTH_DIRECT");
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", northDirect);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("VIEW_ALL".equals(cmd)) {
			mNowZoomIn = true;
			mQihooMap.zoomAll(new Runnable() {

				@Override
				public void run() {
					mNowZoomIn = false;
				}
			});
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResPlaceholderString(
					"RS_MAP_SWITCH_PATH_DONE", "%PATH%", speech);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		
		if ("PLAY_FORWARE_VIDEO".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String openVideo = NativeData.getResString("RS_MAP_OPEN_VIDEO");
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND")
					.replace("%CMD%", openVideo);
			RecorderWin.speakTextWithClose(spk, null);
			mQihooMap.playforwardvideo(true);
			return;
		}
		
		if ("CLOSE_FORWARE_VIDEO".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String closeVideo = NativeData.getResString("RS_MAP_CLOSE_VIDEO");
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND")
					.replace("%CMD%", closeVideo);
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				@Override
				public void run() {
					mQihooMap.playforwardvideo(false);
				}
			});
			return;
		}

		if ("LESS_MONEY".equals(cmd)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String lessMoney = NativeData
							.getResString("RS_MAP_LESS_MONEY");
					final String sureSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
							lessMoney);
					final String hintSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
							lessMoney);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定"});
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						@Override
						public String getReportDialogId() {
							return "NavQihooImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk);
							mQihooMap.switchPlanStyle(PlanStyle.DUOBISHOUFEI);
							JNIHelper.logd("NavAmapAutoNavImpl start LESS_MONEY");
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if ("DUOBIYONGDU".equals(cmd)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String duoBiYongDu = NativeData
							.getResString("RS_MAP_DUOBIYONGDU");
					final String sureSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
							duoBiYongDu);
					final String hintSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
							duoBiYongDu);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定"});
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						@Override
						public String getReportDialogId() {
							return "NavQihooImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk);
							mQihooMap.switchPlanStyle(PlanStyle.DUOBIYONGDU);
							JNIHelper.logd("NavAmapAutoNavImpl start DUOBIYONGDU");
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if ("BUZOUGAOSU".equals(cmd)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String buZouGaoSu = NativeData
							.getResString("RS_MAP_BUZOUGAOSU");
					final String sureSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
							buZouGaoSu);
					final String hintSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
							buZouGaoSu);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定"});
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						@Override
						public String getReportDialogId() {
							return "NavQihooImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk);
							mQihooMap.switchPlanStyle(PlanStyle.BUZOUGAOSU);
							JNIHelper.logd("NavAmapAutoNavImpl start BUZOUGAOSU");
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if ("GAOSUYOUXIAN".equals(cmd)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String gaoSu = NativeData
							.getResString("RS_MAP_GAOSUYOUXIAN");
					final String sureSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_SURE_SPK").replace("%COMMAND%",
							gaoSu);
					final String hintSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_HINT_SPK").replace("%COMMAND%",
							gaoSu);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定"});
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						@Override
						public String getReportDialogId() {
							return "NavQihooImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk);
							mQihooMap.switchPlanStyle(PlanStyle.GAOSUYOUXIAN);
							JNIHelper.logd("NavAmapAutoNavImpl start GAOSUYOUXIAN");
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if ("ASK_REMAIN".equals(cmd)) {
			if (!isInNav()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("", null);
				return;
			}
			((QihooMapInterface) mQihooMap).onSpeechRemain();
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose("", null);
			return;
		}

		if ("EXIT_NAV".equals(cmd)) {
			if (!fromWakeup) {

				JSONBuilder json = new JSONBuilder();
				json.put("scene", "nav");
				json.put("text", speech);
				json.put("action", "exit");
				if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
					return ;
				}
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("将为您" + speech, new Runnable() {

					@Override
					public void run() {
						if (!mIsNav) {
							return;
						}
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}

			if (!enableWakeupExitNav || !mIsNav) {
				return;
			}

			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE").replace("%COMMAND%", speech);
					final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_HINT").replace("%COMMAND%", speech);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定", "退出" });
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						@Override
						public String getReportDialogId() {
							return "NavQihooImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {
								@Override
								public void onEnd() {
									NavManager.getInstance().exitAllNavTool();
								};
							});
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if ("CLOSE_MAP".equals(cmd)) {
			if (!fromWakeup) {

				JSONBuilder json = new JSONBuilder();
				json.put("scene", "nav");
				json.put("text", speech);
				json.put("action", "exit");
				if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
					return ;
				}
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("将为您" + speech, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				return;
			}

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					final String sureSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_SURE").replace("%COMMAND%", speech);
					final String hintSpk = NativeData.getResString("RS_MAP_CONFIRM_EXIT_HINT").replace("%COMMAND%", speech);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定", "退出" });
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						@Override
						public String getReportDialogId() {
							return "NavQihooImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk, new ITtsCallback() {
								@Override
								public void onEnd() {
									NavManager.getInstance().exitAllNavTool();
								};
							});
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}

		if (RecorderWin.isOpened()) {
			RecorderWin.close();
		}
	}

	@Override
	public void setPackageName(String packageName) {
		PACKAGE_NAME = packageName;
		if (mQihooMap != null) {
			mQihooMap.setPackageName(PACKAGE_NAME);
		}
	}

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		try {
			double[] dest = LocationUtil.getGCJ02(info.msgGpsInfo);
			mQihooMap.navigateTo(info.strTargetName, dest[0], dest[1], 0);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void exitNav() {
		mQihooMap.naviExit();
		// 退出了应用
		onExitApp();
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		mQihooMap.updateHomeLocation(navigateInfo.strTargetName, navigateInfo.msgGpsInfo.dblLat, navigateInfo.msgGpsInfo.dblLng);
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		mQihooMap.updateCompanyLocation(navigateInfo.strTargetName, navigateInfo.msgGpsInfo.dblLat, navigateInfo.msgGpsInfo.dblLng);
	}

	@Override
	public boolean willNavAfterSet() {
		return false;
	}
	
/*	void addQihooAsr(AsrComplexSelectCallback acsc){
		if(acsc == null && cmds != null){
			unregCmdCommand();
		}
		
		checkSupportAsrKeys();
		
		List<String> cmdList = new ArrayList<String>();
		Map<String, Set<String>> asrCmds = mAsrCmdsSrc.getAsrCmdsHashSet();
		if (asrCmds != null) {
			Set<Entry<String, Set<String>>> cmdsSet = asrCmds.entrySet();
			for (Entry<String, Set<String>> entry : cmdsSet) {
				if (acsc != null) {
					acsc.addCommand(entry.getKey(), entry.getValue().toArray(new String[entry.getValue().size()]));
				} else {
					String[] regcmds = entry.getValue().toArray(new String[entry.getValue().size()]);
					AsrUtil.regCmd(regcmds, entry.getKey(), iccb);

					for (String key : regcmds) {
						cmdList.add(key);
					}
				}
			}
		}
		
		// 独有支持的命令字
		if (mIsStarted) {
			for (String key : mCustomCmdList.keySet()) {
				if (acsc != null) {
					acsc.addCommand(key, mCustomCmdList.get(key));
				} else {
					AsrUtil.regCmd(mCustomCmdList.get(key), key, iccb);
					for (String cmd : mCustomCmdList.get(key)) {
						cmdList.add(cmd);
					}
				}
			}
		}

		if (cmdList != null) {
			mHasRegisterCmds = true;
			cmds = cmdList.toArray(new String[cmdList.size()]);
		}
	}
	
	
	private Map<String, String[]> mCustomCmdList = new HashMap<String, String[]>();
	{
		mCustomCmdList.put("PLAY_FORWARE_VIDEO", new String[] { "打开视频" });
		mCustomCmdList.put("CLOSE_FORWARE_VIDEO", new String[] { "关闭视频" });
	}*/
	
	@Override
	public int onWakeupRegister(AsrComplexSelectCallback acsc) {
		acsc.addCommand("PLAY_FORWARE_VIDEO", new String[] { "打开视频" });
		acsc.addCommand("CLOSE_FORWARE_VIDEO", new String[] { "关闭视频" });
		return 2;
	}
	
	@Override
	public Collection<String> onAsrCmdsRegister(IAsrRegCmdCallBack mICCB) {
		List<String> cmds = new ArrayList<String>();
		cmds.add("PLAY_FORWARE_VIDEO");
		cmds.add("CLOSE_FORWARE_VIDEO");
		AsrUtil.regCmd( new String[] { "打开视频" }, "PLAY_FORWARE_VIDEO", mICCB);
		AsrUtil.regCmd(new String[] { "关闭视频" }, "CLOSE_FORWARE_VIDEO", mICCB);
		return cmds;
	}
	
	@Override
	public List<String> getBanCmds() {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public List<String> getCmdNavOnly() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(AsrKeyType.VIEW_ALL);
		cmds.add(AsrKeyType.DUOBIYONGDU);
		cmds.add(AsrKeyType.BUZOUGAOSU);
		cmds.add(AsrKeyType.GAOSUYOUXIAN);
		cmds.add(AsrKeyType.LESS_MONEY);
		cmds.add(AsrKeyType.HOW_NAVI);
		cmds.add(AsrKeyType.ASK_REMAIN);
//		cmds.add(AsrKeyType.LIMIT_SPEED);
		cmds.add(AsrKeyType.BACK_NAVI);
		return cmds;
	}

	@Override
	public String[] getSupportCmds() {
		return new String[]{
			AsrKeyType.ZOOM_IN,
			AsrKeyType.ZOOM_OUT,
			AsrKeyType.NIGHT_MODE,
			AsrKeyType.LIGHT_MODE,
			AsrKeyType.EXIT_NAV,
			AsrKeyType.VIEW_ALL,
			AsrKeyType.DUOBIYONGDU,
			AsrKeyType.BUZOUGAOSU,
			AsrKeyType.GAOSUYOUXIAN,
			AsrKeyType.LESS_MONEY,
			AsrKeyType.ASK_REMAIN,
			AsrKeyType.OPEN_TRAFFIC,
			AsrKeyType.CLOSE_TRAFFIC,
			AsrKeyType.TWO_MODE,
			AsrKeyType.THREE_MODE,
			AsrKeyType.CAR_DIRECT,
			AsrKeyType.NORTH_DIRECT
		};
	}
}
