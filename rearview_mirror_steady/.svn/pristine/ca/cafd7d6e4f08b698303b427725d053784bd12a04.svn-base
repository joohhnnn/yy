package com.txznet.txz.component.nav.gaode;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.ui.dialog2.WinConfirmAsr.WinConfirmAsrBuildData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.nav.INavHighLevelInterface;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.n.INavHighLevel;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.LocationUtil;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.text.TextUtils;

import java.util.List;

public class NavAmapImpl extends NavThirdApp implements INavHighLevelInterface,INavHighLevel {
	public final static String PACKAGE_NAME = "com.autonavi.minimap";

	private NavInfo mNavInfo;

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		try {
			double[] origin = LocationUtil.getGCJ02(LocationManager.getInstance().getLastLocation().msgGpsInfo);
			double[] dest = LocationUtil.getGCJ02(info.msgGpsInfo);

			int strategy = 0;
			switch (plan) {
			case NAV_PLAN_TYPE_AVOID_JAMS:
				strategy = 4;
				break;
			case NAV_PLAN_TYPE_LEAST_COST:
				strategy = 1;
				break;
			case NAV_PLAN_TYPE_LEAST_DISTANCE:
				strategy = 2;
				break;
			case NAV_PLAN_TYPE_LEAST_TIME:
				strategy = 0;
				break;
			case NAV_PLAN_TYPE_RECOMMEND:
			default:
				strategy = 0;
				break;
			}

			Intent intent = new Intent();

			intent.setData(Uri.parse("androidamap://navi?sourceApplication=" + GlobalContext.get().getPackageName()
					+ "&poiname=" + info.strTargetName + "&lat=" + dest[0] + "&lon=" + dest[1] + "&dev=0&style=2"));
			intent.setAction("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public void exitNav() {
		try {
			if (mIsCustomVersion) {
				dismiss();

				Intent mIntent = new Intent("com.autonavi.minimap");
				mIntent.putExtra("NAVI", "NAVI_EXIT");
				GlobalContext.get().sendBroadcast(mIntent);

				Intent intent = new Intent("com.autonavi.minimap");
				intent.putExtra("NAVI", "APP_EXIT");
				GlobalContext.get().sendBroadcast(intent);
			} else {
				super.exitNav();
			}
		} catch (Exception e) {
		}
		
		// 退出了应用
		onExitApp();
	}

	public NavAmapImpl() {
		super();
		addStatusListener();
	}

	boolean mIsCustomVersion = false; // 是否为订制版本
	boolean mIsStarted = false; // 是否启动了导航
	boolean mIsPlaned = false; // 是否规划了路线
	boolean mIsOnFocus = false; // 地图是否处于焦点

	private final static String SEND_ACTION = "com.autonavi.minimap.carmode.send";

	private final static String SEND_CONTENT = "SEND_CONTENT";
	private final static String SEND_BUSINESS_ACTION = "send_business_action";
	private final static String SEND_BUSINESS_DATA = "send_business_data";

	private final static String SEND_LAUNCH_APP = "LAUNCH_APP";
	private final static String SEND_EXIT_APP = "EXIT_APP";
	private final static String SEND_OPEN_NAVI = "OPEN_NAVI";
	private final static String SEND_CLOSE_NAVI = "CLOSE_NAVI";
	private final static String SEND_PATH_FAIL = "PATH_FAIL";
	private final static String SEND_APP_FOREGROUND = "APP_FOREGROUND";
	private final static String SEND_APP_BACKGROUND = "APP_BACKGROUND";
	private final static String SEND_NAVI_INFO = "NAVI_INFO";

	@Override
	public void addStatusListener() {
		// TODO 增加状态监听
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				try {
					String action = intent.getAction();
					if (!SEND_ACTION.equals(action))
						return;
					mIsCustomVersion = true;
					String content = intent.getStringExtra(SEND_CONTENT);
					JNIHelper.logd("recv gaode status action: " + content);

					if (content == null) {
						content = intent.getStringExtra(SEND_BUSINESS_ACTION);
					}

					if (SEND_LAUNCH_APP.equals(content)) {
						// 程序启动
					} else if (SEND_EXIT_APP.equals(content)) {
						// 程序退出
						onEnd(false);
					} else if (SEND_OPEN_NAVI.equals(content)) {
						// 打开导航。附带数据为终点数据（EPOI类型）
						onStart();
						onPlanComplete();
						onResume();
					} else if (SEND_CLOSE_NAVI.equals(content)) {
						// 关闭导航
						onEnd(false);
					} else if (SEND_PATH_FAIL.equals(content)) {
						// 规划路径失败
						onPlanError(0, "");
					} else if (SEND_APP_FOREGROUND.equals(content)) {
						// 程序切换到前台
						onResume();
					} else if (SEND_APP_BACKGROUND.equals(content)) {
						// 程序切换到后台
						onPause();
					} else if (SEND_NAVI_INFO.equals(content)) {
						// 导航数据（ENaviInfo类型）
						String sData = intent.getStringExtra(SEND_BUSINESS_DATA);
						LogUtil.logd("ENaviInfo:" + sData);
						broadNaviInfo(sData);
					}
				} catch (Exception e) {
				}

			}
		}, new IntentFilter(SEND_ACTION));
		// TODO 请求最后的状态
	}
	
	Intent broadIntent = null;

	@Override
	public void broadNaviInfo(String navJson) {
		if (mNavInfo == null) {
			mNavInfo = new NavInfo();
		}

		mNavInfo.parseGDNavInfo(navJson, getPackageName());
		if (broadIntent == null) {
			broadIntent = new Intent(NAVI_INFO_ACTION);
		}
		broadIntent.putExtra(EXTRA_KEY_NAVI_INFO, mNavInfo.toJson());
		GlobalContext.get().sendBroadcast(broadIntent);
	}

	@Override
	public void onStart() {
		mIsStarted = true;
		checkInNav();
	}

	@Override
	public void onEnd(boolean arrive) {
		mIsPlaned = false;
		mIsStarted = false;
		checkInNav();
	}

	@Override
	public void onPlanComplete() {
		mIsStarted = true;
		mIsPlaned = true;
		checkInNav();
	}

	@Override
	public void onPlanError(int errCode, String errDesc) {
		mIsPlaned = false;
		mIsStarted = false;
		checkInNav();
	}

	@Override
	public void onResume() {
		mIsOnFocus = true;
		checkInNav();
		initAmapParams();
	}

	@Override
	public void onPause() {
		mIsOnFocus = false;
		checkInNav();
	}

	@Override
	public void onNavCommand(boolean fromWakeup, String cmd, String speech) {
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
		
		if ("ZOOM_IN".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String zoomIn = NativeData.getResString("RS_MAP_ZOOMIN");
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", zoomIn);
			RecorderWin.speakTextWithClose(spk, new Runnable() {

				@Override
				public void run() {
					Intent mIntent = new Intent("com.autonavi.minimap");
					mIntent.putExtra("NAVI", "MAP_ZOOM_IN");
					GlobalContext.get().sendBroadcast(mIntent);
				}
			});
			return;
		}
		if ("ZOOM_OUT".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String zoomOut = NativeData.getResString("RS_MAP_ZOOMOUT");
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", zoomOut);
			RecorderWin.speakTextWithClose(spk, new Runnable() {

				@Override
				public void run() {
					Intent mIntent = new Intent("com.autonavi.minimap");
					mIntent.putExtra("NAVI", "MAP_ZOOM_OUT");
					GlobalContext.get().sendBroadcast(mIntent);
				}
			});
			return;
		}
		if ("ASK_REMAIN".equals(cmd)) {
			if (!isInNav() || mNavInfo == null) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("", null);
				return;
			}
			Long remainTime = mNavInfo.remainTime;
			Long remainDistance = mNavInfo.remainDistance;

			String rt = getRemainTime(remainTime);
			String rd = getRemainDistance(remainDistance);
			String hint = "";
			if (TextUtils.isEmpty(rt) && TextUtils.isEmpty(rd)) {
				hint = "";
			}
			if (!TextUtils.isEmpty(rt) && !TextUtils.isEmpty(rd)) {
				hint = NativeData.getResString("RS_MAP_DESTINATION_ABOUT")
						.replace("%DISTANCE%", rd).replace("%TIME%", rt);
			} else if (!TextUtils.isEmpty(rd)) {
				hint = NativeData.getResPlaceholderString(
						"RS_MAP_DESTINATION_DIS", "%DISTANCE%", rd);
			} else if (!TextUtils.isEmpty(rt)) {
				hint = NativeData.getResPlaceholderString(
						"RS_MAP_DESTINATION_TIME", "%TIME%", rt);
			}

			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, null);
			return;
		}
		if ("ASK_HOW".equals(cmd)) {
			if (!isInNav() || mNavInfo == null) {
				if (RecorderWin.isOpened()) {
					RecorderWin.close();
				}
				return;
			}

			String dirTxt = mNavInfo.getDirectionDes();
			if (TextUtils.isEmpty(dirTxt)) {
				if (RecorderWin.isOpened()) {
					RecorderWin.close();
				}
				return;
			}

			long remainDistance = mNavInfo.remainDistance;
			String distance = "";
			if (remainDistance > 1000) {
				distance = (Math.round(remainDistance / 100.0) / 10.0) + "公里";
			} else if (remainDistance > 0) {
				distance = remainDistance + "米";
			} else {
				if (RecorderWin.isOpened()) {
					RecorderWin.close();
				}
				return;
			}

			String nextRoad = mNavInfo.nextRoadName;
			String hint = "前方" + distance + dirTxt;
			if (!TextUtils.isEmpty(nextRoad)) {
				hint += ",进入" + nextRoad;
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, null);
			return;
		}
		if ("NOW_TRAFFIC".equals(cmd)) {
			sendIntent("androidamap://showTraffic?lat=" + mNavInfo.latitude + "&lon=" + mNavInfo.longitude
					+ "&level=14&dev=0&sourceApplication=txz&pkg=com.autonavi.minimap&voicebroadcast=yes");
			return;
		}
		if ("OPEN_TRAFFIC".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, new Runnable() {

				@Override
				public void run() {
					openTraffic();
				}
			});
			return;
		}
		if ("CLOSE_TRAFFIC".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", speech);
			RecorderWin.speakTextWithClose(spk, new Runnable() {

				@Override
				public void run() {
					closeTraffic();
				}
			});
			return;
		}
		if ("LIMIT_SPEED".equals(cmd)) {
			if (!isInNav() || mNavInfo == null) {
				if (RecorderWin.isOpened()) {
					RecorderWin.close();
				}
				return;
			}
			long speed = mNavInfo.currentLimitedSpeed;
			if (speed < 1) {
				if (RecorderWin.isOpened()) {
					RecorderWin.close();
				}
				return;
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResPlaceholderString(
					"RS_MAP_SPEEDLIMIT", "%SPEED%", speed+"");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if ("EXIT_NAV".equals(cmd)) {
			if (!fromWakeup) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", exitNav);
				RecorderWin.speakTextWithClose(spk, new Runnable() {
					@Override
					public void run() {
						exitNav();
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
					String exitNav = NativeData.getResString("RS_MAP_NAV_EXIT");
					final String sureSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_EXIT_SURE").replace("%COMMAND%",
							exitNav);
					final String hintSpk = NativeData.getResString(
							"RS_MAP_CONFIRM_EXIT_HINT").replace("%COMMAND%",
							exitNav);
					WinConfirmAsrBuildData data = new WinConfirmAsrBuildData();
					data.setSureText("确定", new String[] { "确定", "退出" });
					data.setCancelText("取消", new String[] { "取消" });
					data.setHintTts(hintSpk);
					data.setMessageText(hintSpk);
					mWinConfirmAsr = new WinConfirmAsr(data) {
						
						@Override
						public String getReportDialogId() {
							return "NavAmapImpl_dialog";
						}
						
						@Override
						public void onClickOk() {
							TtsManager.getInstance().speakText(sureSpk, new TtsUtil.ITtsCallback() {
								@Override
								public void onEnd() {
									exitNav();
								}
							});
						}
					};
					mWinConfirmAsr.show();
				}
			}, 0);
			return;
		}
	}

	@Override
	public boolean isInNav() {
		return mIsStarted && mIsPlaned;
	}

	private void checkInNav() {
		if (mIsStarted && mIsPlaned && mIsOnFocus) {
			onEnterNavUi();
		} else {
			onLeaveNavUi();
		}
	}

	// 进入导航界面
	public void onEnterNavUi() {
		regNavUiCommands();
	}

	// 离开导航界面
	public void onLeaveNavUi() {
		unregNavUiCommands();
	}

	// 注册唤醒指令
	public void regNavUiCommands() {
		AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {
			@Override
			public boolean needAsrState() {
				return false;
			}

			@Override
			public String getTaskId() {
				return "NAV_CTRL#" + PACKAGE_NAME;
			}

			public void onCommandSelected(String type, String command) {
				onNavCommand(this.isWakeupResult(), type, command);
			};

		}.addCommand("ZOOM_IN", "放大").addCommand("ZOOM_OUT", "缩小").addCommand("VIEW_ALL", "查看全程", "全览")
				.addCommand("OPEN_TRAFFIC", "打开路况").addCommand("CLOSE_TRAFFIC", "关闭路况")
//				.addCommand("LIMIT_SPEED", "限速", "当前限速", "道路限速")
				.addCommand("ASK_REMAIN", "还有多久", "多久到", "还要多久", "还有多远")
				.addCommand("ASK_HOW", "怎么走", "往哪走", "哪条路");
		if (ProjectCfg.mEnableAEC) {
			callback.addCommand("EXIT_NAV", "退出导航", "放弃导航", "关闭导航", "离开导航");
		}
		WakeupManager.getInstance().useWakeupAsAsr(callback);
	}

	private void sendIntent(String url) {
		Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
		intent.setPackage(getPackageName());
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

	private void initAmapParams() {
		openTraffic();
		openVoiceNavi();
	}

	/**
	 * 开启导航语音
	 */
	private void openVoiceNavi() {
		Intent mIntent = new Intent("com.autonavi.minimap");
		mIntent.putExtra("NAVI", "OPEN_VOICE");
		GlobalContext.get().sendBroadcast(mIntent);
	}

	/**
	 * 关闭导航语音
	 */
	private void closeVoiceNavi() {
		Intent mIntent = new Intent("com.autonavi.minimap");
		mIntent.putExtra("NAVI", "CLOSE_VOICE");
		GlobalContext.get().sendBroadcast(mIntent);
	}

	/**
	 * 打开路况
	 */
	private void openTraffic() {
		Intent mIntent = new Intent("com.autonavi.minimap");
		mIntent.putExtra("NAVI", "OPEN_TRAFFIC_BROADCAST");
		GlobalContext.get().sendBroadcast(mIntent);
	}

	/**
	 * 关闭路况
	 */
	private void closeTraffic() {
		Intent mIntent = new Intent("com.autonavi.minimap");
		mIntent.putExtra("NAVI", "CLOSE_TRAFFIC_BROADCAST");
		GlobalContext.get().sendBroadcast(mIntent);
	}

	// 反注册唤醒指令
	public void unregNavUiCommands() {
		WakeupManager.getInstance().recoverWakeupFromAsr("NAV_CTRL#" + PACKAGE_NAME);
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPackageName(String packageName) {
		
	}

	@Override
	public boolean procJingYouPoi(Poi... pois) {
		return false;
	}

	@Override
	public boolean deleteJingYou(Poi poi) {
		return false;
	}

	@Override
	public List<Poi> getJingYouPois() {
		return null;
	}

	@Override
	public String disableProcJingYouPoi() {
		return null;
	}

	@Override
	public String disableDeleteJingYou() {
		return NativeData.getResString("RS_NAV_NOT_SUPPORT_THROUGH");
	}
}
