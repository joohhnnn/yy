package com.txznet.txz.component.nav.gaode;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.amap.api.maps.AMapUtils;
import com.amap.api.maps.model.LatLng;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ProcessUtil;
import com.txznet.loader.AppLogic;
import com.txznet.loader.AppLogicBase;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.TXZNavManager.PathInfo.WayInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.IMapInterface.PlanStyle;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.gaode.NavAmapControl.IAmapNavContants;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.RoadInfo;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.ConfigFileHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavInterceptTransponder;
import com.txznet.txz.module.net.NetworkUtil;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.LocationUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class NavAmapAutoNavImpl extends NavThirdComplexApp implements IAmapNavContants {
	public static final String PACKAGE_PREFIX = "com.autonavi.amap";
	public static final String PACKAGE_NAME = "com.autonavi.amapauto";
	public static final String PACKAGE_NAME_LITE = "com.autonavi.amapautolite";

	NavigateInfo mNavigateInfo;
	List<Poi> mTJPois = new ArrayList<Poi>();
	protected NavAmapControl mNavAmapControl;
	BroadcastReceiver mRecv;
	
	boolean mIsNorth;

	private volatile int mMapSize = -1;//地图尺寸是否达到最大
	private static final int MAP_SIZE_ALREADY_MAX = 1;
	private static final int MAP_SIZE_ALREADY_MIN = 0;
	private static final int MAP_SIZE_ALREADY_DEFAULT = -1;
	private static final int DELAY_RUNNING_TASK = 2000;

	public NavAmapAutoNavImpl() {
	}

	private void init() {
		mNavAmapControl = new NavAmapControl(this, null);
		try {
		mRecv = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				handleIntent(intent);
			}
		};
		GlobalContext.get().registerReceiver(mRecv, new IntentFilter(IAmapNavContants.RECV_ACTION));
		initProcess();
	} catch (Exception e) {
	}
}

	@Override
	public int initialize(IInitCallback oRun) {
		init();
		NavAmapValueService.getInstance().queryNavFocus();
		NavAmapValueService.getInstance().queryNavStatus();
		return super.initialize(oRun);
	}

	@Override
	public void release() {
		super.release();
		if (mRecv != null) {
			GlobalContext.get().unregisterReceiver(mRecv);
		}
	}

	private void initProcess() {
		if (Build.VERSION.SDK_INT < 21) {
			if (ProcessUtil.isForeground(getPackageName())) {
				// 前台
				onResume();
			}
		}
	}
	
	@Override
	public void handleIntent(Intent intent) {
		int keyType = intent.getIntExtra("KEY_TYPE", -1);
		int state = intent.getIntExtra("EXTRA_STATE", -1);
		JNIHelper.logd("onReceive recv keyType:" + keyType + ",state:" + state);

		AppLogic.runOnBackGround(new Runnable1<Intent>(intent) {

			@Override
			public void run() {
				doReceive(mP1);
				NavAmapValueService.getInstance().notifyReceive(mP1);
			}
		}, 0);
	}

	@Override
	public void onPause() {
		super.onPause();
		// 地图退到后台，打断当前的TTS
		NavAmapValueService.getInstance().cancelTts();
	}

	private void doReceive(Intent intent) {
		int key_type = intent.getIntExtra("KEY_TYPE", -1);
		if (key_type == 10019) {
			// 前后台切换
			int state = intent.getIntExtra("EXTRA_STATE", -1);
			if (state == -1) {
				state = intent.getIntExtra("type", -1);
			}

			JNIHelper.logd("amapauto state:" + state);

			switch (state) {
				case CREATE_MAP:
					// onResume(); 创建地图不再认为导航前台，根据FRONT来定
					mNavAmapControl.queryMapStatus();
					break;

				case EXIT_APP:
					onPause();
					onEnd(false);
					NavAmapValueService.getInstance().destoryAllSelectAsrs();
					NavAmapValueService.getInstance().clearAllJingYou();
					onExitApp();
					NavAmapValueService.getInstance().mRoadInfo = null;
					NavAmapValueService.getInstance().setEnableAutoPupUp(true);//退出导航时恢复续航状态
					break;

				case ACTIVITY_GET_FOCUS:
					if (getMapCode() >= 400) {
						break;
					}
				case FRONT:
					if (NavInterceptTransponder.getInstance().interceptGroundIntent(this, intent, true)) {
						LogUtil.logd("amap FRONT is intercepted！");
						return;
					}
					onResume();
					NavAmapValueService.getInstance().onStateChange(true);
					break;

				case ACTIVITY_LOSE_FOCUS:
					if (getMapCode() >= 400) {
					break;
				}
				case BGROUND:
					if (NavInterceptTransponder.getInstance().interceptGroundIntent(this, intent,
							false)) {
						LogUtil.logd("amap BGROUND is intercepted！");
						return;
					}
					onPause();
					NavAmapValueService.getInstance().onStateChange(false);
					break;

				case PLAN_START:
					break;

				case PLAN_SUC:
//					NavAmapValueService.getInstance().cancelAutoPopUp(false);
					onPlanComplete();
					break;

				case PLAN_FAIL:
					break;

				case START_NAVI:
					NavAmapValueService.getInstance().setNaving(true);
					NavAmapValueService.getInstance().removeAutoPopUpCallback();
					AppLogic.runOnBackGround(new Runnable() {
						@Override
						public void run() {
							NavAmapValueService.getInstance().setEnableAutoPupUp(true);
						}
					}, 5000);
					onStart();

					break;

				case END_NAVI:
					NavAmapValueService.getInstance().destroyPlanningSelect();
					NavAmapValueService.getInstance().setNaving(false);
					NavAmapValueService.getInstance().mRoadInfo = null;
					onEnd(false);
					break;

				case 10:// 开始模拟导航
					onStart();
					break;
				case 11:// 暂停模拟导航
					onEnd(false);
					break;
				case 12:// 停止模拟导航
					onEnd(false);
					break;

				case TTS_START:
					JNIHelper.logd("navi tts start");
					TXZMediaFocusManager.getInstance().requestFocus();
					break;

				case TTS_END:
					JNIHelper.logd("navi tts end");
					TXZMediaFocusManager.getInstance().releaseFocus();
					break;
				case MAP_ZOOM_IN:
				case MAP_ZOOM_OUT:
					mMapSize = MAP_SIZE_ALREADY_DEFAULT;
					break;
				case MAP_SIZE_MAX:
					mMapSize = MAP_SIZE_ALREADY_MAX;
					break;
				case MAP_SIZE_MIN:
					mMapSize = MAP_SIZE_ALREADY_MIN;
					break;
				case HOME_CHANGE:
					NavAmapValueService.getInstance().querySet(1);
					break;

				case COMPANY_CHANGE:
					NavAmapValueService.getInstance().querySet(2);
					break;

				case SENDTOCARD_DISMISS:
					NavAmapValueService.getInstance().destorySelectAsrTask(NavAmapValueService.TASK_RECV_PLAN_ID);
					break;

				case STOP_CAR_DISMISS:
					NavAmapValueService.getInstance().destorySelectAsrTask(NavAmapValueService.TASK_PARKER_ID);
					break;

				case CONTNAVI_DISMISS:
					NavAmapValueService.getInstance().cancelAutoPopUp(false);
					NavAmapValueService.getInstance().destorySelectAsrTask(NavAmapValueService.TASK_CONTINUE_NAVI_ID);
					break;

				case PLANFAIL_DISMISS:
					NavAmapValueService.getInstance().destorySelectAsrTask(NavAmapValueService.TASK_PLAN_FAIL);
					break;

				case ALL_EXIT_APP:
					onExitApp();
					NavAmapValueService.getInstance().mRoadInfo = null;
					break;

				default:
					break;
			}
		}
		if (key_type == 10001) {
			Bundle bundle = intent.getExtras();
			if (mNavInfo == null) {
				mNavInfo = new NavInfo();
			}

			mNavInfo.reset();
			if (bundle == null) {
				return;
			}

			try {
				mNavInfo.parseAmapAutoNav(bundle, getPackageName());
				if (NavAmapValueService.getInstance().mRoadInfo != null) {
					mNavInfo.destName = NavAmapValueService.getInstance().mRoadInfo.toPoiName;
					mNavInfo.destAddress = NavAmapValueService.getInstance().mRoadInfo.toPoiAddr;
				}
				broadNaviInfo(mNavInfo.toJson());
			} catch (Exception e) {
			}
		}
		if (key_type == 10041) {
			try {
				String version = intent.getStringExtra("VERSION_NUM");
				JNIHelper.logd("recv amap version:" + version);
				mNavAmapControl.updateNavAmapVersion(version);
			} catch (Exception e) {
			}
		}
		if (key_type == 10056) {
			// 当前道路信息通知
			parseCurrInfo(intent);
		}
	}
	
	// TODO 途经点规划路线进入导航有几率并没有收到开始导航的通知
	private void parseCurrInfo(Intent intent) {
		try {
//			NavAmapValueService.getInstance().doKeyType10056(intent);
			RoadInfo roadInfo = NavAmapValueService.getInstance().mRoadInfo;
			if (roadInfo != null) {
				if (mNavigateInfo == null) {
					mNavigateInfo = new NavigateInfo();
					mNavigateInfo.msgGpsInfo = new GpsInfo();
				}
				JNIHelper.logd("currInfo:" + roadInfo.toPoiName);
				// 比较两次目的地，如果不相同，则清空途经点
				if (mNavigateInfo.msgGpsInfo.dblLat == null
						|| mNavigateInfo.msgGpsInfo.dblLat != roadInfo.toPoiLat
						|| mNavigateInfo.msgGpsInfo.dblLng == null
						|| mNavigateInfo.msgGpsInfo.dblLng != roadInfo.toPoiLng) {
					JNIHelper.logd("clear tjpois");
					mTJPois.clear();
				}

				// TODO 手动设置的途经点无法获取，可能造成数据丢失
				mNavigateInfo.msgGpsInfo.dblLat = roadInfo.toPoiLat;
				mNavigateInfo.msgGpsInfo.dblLng = roadInfo.toPoiLng;
				mNavigateInfo.strTargetName = roadInfo.toPoiName;
				mNavigateInfo.strTargetAddress = roadInfo.toPoiAddr;
			}
		} catch (Exception e) {
		}
	}
	
//	@Override
//	public void enterNav() {
//		JNIHelper.logd("NavAmapAutoNavImpl enterNav isRecvVersion:" + mNavAmapControl.isRecvVersion());
//		mNavAmapControl.enterNav();
//	}

	@Override
	public void exitNav() {
		boolean isFocus = isInFocus();
		try {
			int delay = 1000;
			int mapCode = mNavAmapControl.getMapCode();
			if (mapCode >= 200) {
				// 2.0导航延时退出，防止退不了
				delay = 2000;
		}

			mNavAmapControl.naviExit();
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.appExit();
				}
			}, delay);
			JNIHelper.logd("exit amapauto");
		} catch (Exception e) {
		}

		// 退出了应用
		if (isFocus) {
			onExitApp();
		}
	}

	@Override
	public void onEnd(boolean arrive) {
		super.onEnd(arrive);
		// 退出导航后清空途经点列表
		mTJPois.clear();
	}

	@Override
	public void onPlanComplete() {
		super.onPlanComplete();
		onPlanFinishCheck(100);
	}

	Runnable mStartNaviRunnable = new Runnable() {

		@Override
		public void run() {
			if (mIsJingYouPlan) {
				mIsJingYouPlan = false;
				if (mIsStarted && mIsPlaned) {
					startNavByInner();
				} else {
					JNIHelper.loge("onPlanError,mIsStarted:" + mIsStarted + ",mIsPlaned:" + mIsPlaned);
				}
			}
		}
	};
	
	@Override
	public void startNavByInner() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10009);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		JNIHelper.logd("GDAuto:intent= "+intent);
		JNIHelper.logd("GDAuto:intent.getExtras()= "+intent.getExtras());
		GlobalContext.get().sendBroadcast(intent);
	}

	private void onPlanFinishCheck(long delay) {
		AppLogic.removeBackGroundCallback(mStartNaviRunnable);
		AppLogic.runOnBackGround(mStartNaviRunnable, delay);
	}

	@Override
	public void onNavCommand(boolean isWakeupResult, String type, String command) {
		if (AsrKeyType.EXIT_NAV.equals(type) || AsrKeyType.CLOSE_MAP.equals(type)) {
			JSONBuilder json = new JSONBuilder();
			json.put("scene", "nav");
			json.put("text", command);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}

		JNIHelper
				.logd("onNavCommSelect:[" + isWakeupResult + "," + type + "," + command + "," + getPackageName() + "]");

		if (AsrKeyType.ZOOM_IN.equals(type)) {
			if(mMapSize == MAP_SIZE_ALREADY_MAX){
				String text = NativeData.getResString("RS_VOICE_ALREADY_ZOOM_IN_MAX");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
				return;
			}
			doConfirmShow(type, command, "RS_MAP_ZOOMIN", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.zoomMap(true);
				}
			}, false);
		}
		if (AsrKeyType.ZOOM_OUT.equals(type)) {
			if(mMapSize == MAP_SIZE_ALREADY_MIN){
				String text = NativeData.getResString("RS_VOICE_ALREADY_ZOOM_OUT_MIN");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, null);
				return;
			}
			doConfirmShow(type, command, "RS_MAP_ZOOMOUT", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.zoomMap(false);
				}
			}, false);
		}
		if (AsrKeyType.NIGHT_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_NIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchLightNightMode(false);
				}
			}, false);
		}
		if (AsrKeyType.LIGHT_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_LIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchLightNightMode(true);
				}
			}, false);
		}
		if (AsrKeyType.AUTO_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_AUTO_MODE", new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent(IAmapNavContants.SEND_ACTION);
					intent.putExtra("KEY_TYPE", 10048);
					intent.putExtra("EXTRA_DAY_NIGHT_MODE", 0);
					JNIHelper.logd("GDAuto:intent= "+intent);
					JNIHelper.logd("GDAuto:intent.getExtras()= "+intent.getExtras());
					intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					GlobalContext.get().sendBroadcast(intent);
				}
			}, false);
			return;
		}
		if (AsrKeyType.OPEN_TRAFFIC.equals(type)) {
			if (!NetworkUtil.isConnectedOrConnecting(GlobalContext.get())) {
				RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_CHECK_NET"), null);
				return;
			}
			doConfirmShow(type, command, "RS_MAP_OPEN_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchTraffic(true);
				}
			}, false);
			return;
		}
		if (AsrKeyType.CLOSE_TRAFFIC.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_CLOSE_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchTraffic(false);
				}
			}, false);
			return;
		}
		if (AsrKeyType.TWO_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_TWO_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switch23D(true, mIsNorth ? 1 : 0);
				}
			}, false);
			return;
		}
		if (AsrKeyType.THREE_MODE.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_THREE_MODE", new Runnable() {

				@Override
				public void run() {
					mIsNorth = false;
					mNavAmapControl.switch23D(false, 0);
				}
			}, false);
		}
		if (AsrKeyType.CAR_DIRECT.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_CAR_DIRECT", new Runnable() {

				@Override
				public void run() {
					mIsNorth = false;
					mNavAmapControl.switchCarDirection();
				}
			},false);
			return;
		}
		if (AsrKeyType.NORTH_DIRECT.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_NORTH_DIRECT", new Runnable() {

				@Override
				public void run() {
					mIsNorth = true;
					mNavAmapControl.switchNorthDirection();
				}
			}, false);
			return;
		}
		if (AsrKeyType.VIEW_ALL.equals(type)) {
			String tts = NativeData.getResString("RS_MAP_VIEW_ALL");
			//tts = tts.replace("%COMMAND%", command);
			Runnable task = new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.zoomAll(new Runnable() {

						@Override
						public void run() {

						}
					});
				}
			};

			if (isWakeupResult) {
				task.run();
				//tts = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_VIEW_ALL").replace("%CMD%", tts);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, null);
			} else {
				//tts = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", tts);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(tts, task);
			}
		}
		if (AsrKeyType.LESS_MONEY.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_LESS_MONEY", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.DUOBISHOUFEI);
					JNIHelper.logd("NavAmapAutoNavImpl start LESS_MONEY");
				}
			});
			return;
		}
		if (AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION.equals(type)) {
			doRePlanWakeup(type, command, "RS_NAV_CMD_NAV_WAY_POI_CMD_GASTATION", new Runnable() {
				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.JIAYOUZHAN);
					JNIHelper.logd("NavAmapAutoNavImpl start NAV_WAY_POI_CMD_GO_GASTATION");
				}
			});
			return;
		}
		if (AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET.equals(type)) {
			doRePlanWakeup(type, command, "RS_NAV_CMD_NAV_WAY_POI_CMD_TOILET", new Runnable() {
				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.CESUO);
					JNIHelper.logd("NavAmapAutoNavImpl start NAV_WAY_POI_CMD_GO_TOILET");
				}
			});
			return;
		}
		if (AsrKeyType.NAV_WAY_POI_CMD_GO_REPAIR.equals(type)) {
			doRePlanWakeup(type, command, "RS_NAV_CMD_NAV_WAY_POI_CMD_REPAIR", new Runnable() {
				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.WEIXIUZHAN);
					JNIHelper.logd("NavAmapAutoNavImpl start NAV_WAY_POI_CMD_GO_REPAIR");
				}
			});
			return;
		}
		if (AsrKeyType.NAV_WAY_POI_CMD_GO_ATM.equals(type)) {
			doRePlanWakeup(type, command, "RS_NAV_CMD_NAV_WAY_POI_CMD_ATM", new Runnable() {
				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.ATM);
					JNIHelper.logd("NavAmapAutoNavImpl start NAV_WAY_POI_CMD_GO_ATM");
				}
			});
			return;
		}
		if (AsrKeyType.DUOBIYONGDU.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_DUOBIYONGDU", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.DUOBIYONGDU);
					JNIHelper.logd("NavAmapAutoNavImpl start DUOBIYONGDU");
				}
			});
			return;
		}
		if (AsrKeyType.BUZOUGAOSU.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_BUZOUGAOSU", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.BUZOUGAOSU);
					JNIHelper.logd("NavAmapAutoNavImpl start BUZOUGAOSU");
				}
			});
			return;
		}
		if (AsrKeyType.GAOSUYOUXIAN.equals(type)) {
			doRePlanWakeup(type, command, "RS_MAP_GAOSUYOUXIAN", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.GAOSUYOUXIAN);
					JNIHelper.logd("NavAmapAutoNavImpl start GAOSUYOUXIAN");
				}
			});
			return;
		}
		if (AsrKeyType.HOW_NAVI.equals(type)) {
			speakHowNavi(isWakeupResult);
			return;
		}
		if (AsrKeyType.ASK_REMAIN.equals(type)) {
			speakAskRemain(isWakeupResult);
			return;
		}
		if (AsrKeyType.LIMIT_SPEED.equals(type)) {
			queryLimitSpeed();
			return;
		}
		if (AsrKeyType.BACK_NAVI.equals(type)) {
			mNavAmapControl.backNavi();
			AsrManager.getInstance().setNeedCloseRecord(true);
			String tts = NativeData.getResString("RS_MAP_NAV_CONTINUE");
			String spk = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_BACK_NAV").replace("%CMD%", tts);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (AsrKeyType.START_NAVI.equals(type)) {
			startNavByInner();
			RecorderWin.close();
			return;
		}
		if (AsrKeyType.EXIT_NAV.equals(type)) {
			if(!isWakeupResult){
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
						NativeData.getResString("RS_MAP_NAV_EXIT"));
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
			doExitConfirm(type, NativeData.getResString("RS_MAP_NAV_EXIT"), new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}
		if (AsrKeyType.CANCEL_NAV.equals(type)) {
			if (preNavCancelCommand(isWakeupResult, command)) {
				return;
			}

			if (!isWakeupResult && !isInNav()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%",
						NativeData.getResString("RS_MAP_NAV_EXIT"));
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			doExitConfirm(type,
					isInNav() ? NativeData.getResString("RS_MAP_NAV_STOP") : NativeData.getResString("RS_MAP_NAV_EXIT"),
					new Runnable() {

						@Override
						public void run() {
							if (!isInNav()) {
								NavManager.getInstance().exitAllNavTool();
								return;
							}
							mNavAmapControl.naviExit();
						}
					});
			return;
		}
		if (AsrKeyType.CLOSE_MAP.equals(type)) {
			if (!isWakeupResult) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", command);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			doExitConfirm(type, command, new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}
		if(AsrKeyType.GUOYU_MM.equals(type)){
			switchRole(isWakeupResult, ROLE_GUOYU_MM);
			return;
		}
		if (AsrKeyType.GUOYU_GG.equals(type)) {
			switchRole(isWakeupResult, ROLE_GUOYU_GG);
			return;
		}
		if (AsrKeyType.ZHOUXINGXING.equals(type)) {
			switchRole(isWakeupResult, ROLE_ZHOUXINGXING);
			return;
		}
		if (AsrKeyType.GUANGDONGHUA.equals(type)) {
			switchRole(isWakeupResult, ROLE_GUANGDONGHUA);
			return;
		}
		if (AsrKeyType.LINZHILIN.equals(type)) {
			switchRole(isWakeupResult, ROLE_LINZHILIN);
			return;
		}
		if (AsrKeyType.GUODEGANG.equals(type)) {
			switchRole(isWakeupResult, ROLE_GUODEGANG);
			return;
		}
		if (AsrKeyType.DONGBEIHUA.equals(type)) {
			switchRole(isWakeupResult, ROLE_DONGBEIHUA);
			return;
		}
		if (AsrKeyType.HENANHUA.equals(type)) {
			switchRole(isWakeupResult, ROLE_HENANHUA);
			return;
		}
		if (AsrKeyType.HUNANHUA.equals(type)) {
			switchRole(isWakeupResult, ROLE_HUNANHUA);
			return;
		}
		if (AsrKeyType.SICHUANHUA.equals(type)) {
			switchRole(isWakeupResult, ROLE_SICHUANHUA);
			return;
		}
		if (AsrKeyType.TAIWANHUA.equals(type)) {
			switchRole(isWakeupResult, ROLE_TAIWANHUA);
			return;
		}
		if (AsrKeyType.SWITCH_ROLE.equals(type)) {
			int role = mRole + 1;
			if (role > 10) {
				role = 0;
			}
			switchRole(isWakeupResult, role);
			return;
		}

		if (AsrKeyType.FRONT_TRAFFIC.equals(type)) {
			String spk = NativeData.getResString("RS_VOICE_DOING_COMMAND");
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				@Override
				public void run() {
					mNavAmapControl.frontTraffic();
				}
			});
			return;
		}

		/*
		 * 2021-06-10
		 * 修改切换主辅路的交互
		 * 1.当收到主辅路切换的指令先播放好的
		 * 2.当高德反馈异常的时候就播报 暂不支持切换
		 */
		if (AsrKeyType.SWITCH_MAIN_ROAD.equals(type)) {
			mNavAmapControl.switchPlanStyle(PlanStyle.MAINROAD);
			String spk = NativeData.getResString("RS_MAP_SWITCH_ROAD");
			RecorderWin.speakText(spk,null);
            doTimeOutTask();
			return;
		}

		if (AsrKeyType.SWITCH_SIDE_ROAD.equals(type)) {
			mNavAmapControl.switchPlanStyle(PlanStyle.SIDEROAD);
			String spk = NativeData.getResString("RS_MAP_SWITCH_ROAD");
			RecorderWin.speakText(spk,null);
            doTimeOutTask();
			return;
		}

		if (AsrKeyType.REFRESH_PATH.equals(type)) {
			doConfirmShow(type, command, "RS_MAP_REFRESH_PATH", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.REFRESHPATH);
					return;
				}
			}, false);
		}

		if(AsrKeyType.QUERY_COLLECTION_POINT.equals(type)){
           	mNavAmapControl.queryCollectionPoint();
            doTimeOutTask();
            return;
        }

		if(AsrKeyType.OPEN_SIMPLE_MODE.equals(type)){
			doConfirmShow(type, command, "RS_MAP_OPEN_SIMPLE_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchSimpleMode(true);
					return;
				}
			},false);
		}

		if(AsrKeyType.CLOSE_SIMPLE_MODE.equals(type)){
			doConfirmShow(type, command, "RS_MAP_CLOSE_SIMPLE_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchSimpleMode(false);
					return;
				}
			}, false);
		}

		if(AsrKeyType.INTO_TEAM.equals(type)){
			mNavAmapControl.intoTeam();
			return;
		}
	}

	private Runnable mTimeOutTask = new Runnable() {
        @Override
        public void run() {
            AsrManager.getInstance().setNeedCloseRecord(true);
            String spk = NativeData.getResString("RS_VOICE_NAV_TIMEOUT");
            RecorderWin.speakTextWithClose(spk,null);
            isTimeOut = false;
        }
    };

	public void clearTimeOutTask(){
	    AppLogic.removeBackGroundCallback(mTimeOutTask);
    }

    int mTimeOut = 5000;
	private boolean isTimeOut = false;

    private void doTimeOutTask(){
        isTimeOut = false;
        AppLogic.removeBackGroundCallback(mTimeOutTask);
        AppLogic.runOnBackGround(mTimeOutTask,mTimeOut);
    }

    public boolean getTaskIsTimeOut(){
        return isTimeOut;
    }
	
	int mRole;
	
	private void switchRole(boolean isWakeup, int role) {
		if (mRole == role) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(mNavAmapControl.getTTS(role), null);
			return;
		}

		mRole = role;
		if (!isWakeup) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(mNavAmapControl.getSetTTS(role), new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchBroadcastRole(mRole);
				}
			});
			return;
		}
		
		mNavAmapControl.switchBroadcastRole(mRole);
	}
	
	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
//		NavAmapValueService.getInstance().setEnableAutoPupUp(false);
		try {
			int delay = 0;
			if (isInNav()) {
				delay = 1000;
				mNavAmapControl.naviExit();
			}
			mNavRunnable.update(info, plan);
			AppLogic.removeBackGroundCallback(mNavRunnable);
			AppLogic.runOnBackGround(mNavRunnable, delay);
			return true;
		} catch (Exception e) {
			JNIHelper.loge(e.toString());
			return false;
		}
	}

	Runnable2<NavigateInfo, NavPlanType> mNavRunnable = new Runnable2<NavigateInfo, NavPlanType>(null, null) {

		@Override
		public void run() {
			if (mP1 == null) {
				return;
			}
			NavigateInfo info = mP1;

			mTJPois.clear();
			// STYLE (0 速度快; 1 费用少; 2 路程短; 3 不走高速；4 躲避拥堵；
			// 5 不走高速且避免收费；6 不走高速且躲避拥堵；7 躲避收费和拥堵；8 不走高速躲避收费和拥堵)
			int strategy = -1;
			if (mP2 != null) {
				if (mP2 == NavPlanType.NAV_PLAN_TYPE_AVOID_JAMS) {
					strategy = 4;
				} else if (mP2 == NavPlanType.NAV_PLAN_TYPE_LEAST_COST) {
					strategy = 1;
				} else if (mP2 == NavPlanType.NAV_PLAN_TYPE_BZGS) {
					strategy = 3;
				} else if (mP2 == NavPlanType.NAV_PLAN_TYPE_GSYX) {
					strategy = 20;
				} else if (mP2 == NavPlanType.NAV_PLAN_TYPE_LEAST_TIME) {
					strategy = 0;
				}
			}
			mNavigateInfo = info;
			double[] dest = LocationUtil.getGCJ02(info.msgGpsInfo);
			if (mEnableSave) {
				if (mPlanStyle != -1) {
					strategy = mPlanStyle;
				}
			}
			NavAmapValueService.getInstance().setEnableAutoPupUp(false);
			mNavAmapControl.navigateTo(info.strTargetName, dest[0], dest[1], strategy);
		}
	};
	
	@Override
	public boolean navigateWithWayPois(Poi startPoi, Poi endPoi, List<WayInfo> pois) {
		if (endPoi == null) {
			throw new NullPointerException("endPoi is null！");
		}
		final Intent intent = new Intent();
		intent.setAction("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10032);
		if (startPoi != null) {
			intent.putExtra("EXTRA_SNAME", startPoi.getName());
			intent.putExtra("EXTRA_SLON", startPoi.getLng());
			intent.putExtra("EXTRA_SLAT", startPoi.getLat());
		}

		if (endPoi != null) {
			intent.putExtra("EXTRA_DNAME", endPoi.getName());
			intent.putExtra("EXTRA_DLON", endPoi.getLng());
			intent.putExtra("EXTRA_DLAT", endPoi.getLat());
		}

		if (pois != null) {
			do {
				if (pois.size() > 0) {
					intent.putExtra("EXTRA_FMIDNAME", pois.get(0).name);
					intent.putExtra("EXTRA_FMIDLON", pois.get(0).lng);
					intent.putExtra("EXTRA_FMIDLAT", pois.get(0).lat);
				}
				if (pois.size() > 1) {
					intent.putExtra("EXTRA_SMIDNAME", pois.get(1).name);
					intent.putExtra("EXTRA_SMIDLON", pois.get(1).lng);
					intent.putExtra("EXTRA_SMIDLAT", pois.get(1).lat);
				}
				if (pois.size() > 2) {
					intent.putExtra("EXTRA_TMIDNAME", pois.get(2).name);
					intent.putExtra("EXTRA_TMIDLON", pois.get(2).lng);
					intent.putExtra("EXTRA_TMIDLAT", pois.get(2).lat);
				}
			} while (false);
		}
		int delay = 0;
		if (!PackageManager.getInstance().isAppRunning(getPackageName())) {
			Intent openIntent = GlobalContext.get().getPackageManager().getLaunchIntentForPackage(getPackageName());
			if (openIntent != null) {
				openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				openIntent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				GlobalContext.get().startActivity(openIntent);
			}
			delay = 3000;
			LogUtil.logd("navigateTo delay:" + delay);
		}

		intent.putExtra("EXTRA_DEV", 0);
		intent.putExtra("EXTRA_M", -1);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		AppLogic.runOnBackGround(new Runnable() {
			@Override
			public void run() {
				GlobalContext.get().sendBroadcast(intent);
			}
		},delay);
		return true;
	}
	
	@Override
	public List<String> getBanCmds() {
		List<String> cmds = new ArrayList<String>();;
		int mapPrefix = mNavAmapControl.getMapCode();
		int mapDetail = mNavAmapControl.getMapDetCode();
		JNIHelper.logd("mapPrefix code:" + mapPrefix + ",mapDetail:" + mapDetail);
		if (mapPrefix < 142 &&PackageManager.getInstance().getVerionCode(getPackageName()) < 541) {
			// cmds.add(AsrKeyType.DONGBEIHUA);o
			// cmds.add(AsrKeyType.HENANHUA);
			// cmds.add(AsrKeyType.HUNANHUA);
			// cmds.add(AsrKeyType.SICHUANHUA);
			// cmds.add(AsrKeyType.TAIWANHUA);
			cmds.add(AsrKeyType.AUTO_MODE);
		}

		if (mapPrefix >= 143 && mapDetail < 1432227 && !mNavAmapControl.isAmapautoLite()) {
			cmds.add(AsrKeyType.GAOSUYOUXIAN);
		}

		if (mapPrefix >= 200 && mNavAmapControl.isAmapautoLite()) {
			cmds.add(AsrKeyType.TWO_MODE);
			cmds.add(AsrKeyType.THREE_MODE);
		}

		if (mapPrefix < 294) {
			cmds.add(AsrKeyType.SWITCH_MAIN_ROAD);
			cmds.add(AsrKeyType.SWITCH_SIDE_ROAD);
			cmds.add(AsrKeyType.REFRESH_PATH);
		}

		if (mapPrefix < 260) {
			cmds.add(AsrKeyType.FRONT_TRAFFIC);
		}

		return cmds;
	}

	@Override
	public String[] getSupportCmds() {
		return new String[] { AsrKeyType.ZOOM_IN, 
				AsrKeyType.ZOOM_OUT, 
				AsrKeyType.NIGHT_MODE, 
				AsrKeyType.LIGHT_MODE,
				AsrKeyType.AUTO_MODE, 
				AsrKeyType.EXIT_NAV, 
				AsrKeyType.CANCEL_NAV,
				AsrKeyType.CLOSE_MAP, 
				AsrKeyType.VIEW_ALL,
				AsrKeyType.DUOBIYONGDU, 
				AsrKeyType.BUZOUGAOSU, 
				AsrKeyType.GAOSUYOUXIAN, 
				AsrKeyType.LESS_MONEY,
				AsrKeyType.LESS_DISTANCE, 
				AsrKeyType.HOW_NAVI, 
				AsrKeyType.ASK_REMAIN, 
//				AsrKeyType.LIMIT_SPEED,
				AsrKeyType.BACK_NAVI, 
				AsrKeyType.START_NAVI, 
				AsrKeyType.OPEN_TRAFFIC, 
				AsrKeyType.CLOSE_TRAFFIC,
				AsrKeyType.TWO_MODE, 
				AsrKeyType.THREE_MODE,
				AsrKeyType.CAR_DIRECT, 
				AsrKeyType.NORTH_DIRECT,
				AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION,
				AsrKeyType.FRONT_TRAFFIC,
//				AsrKeyType.SWITCH_MAIN_ROAD,
//				AsrKeyType.SWITCH_SIDE_ROAD,
				AsrKeyType.REFRESH_PATH
//				AsrKeyType.NAV_WAY_POI_CMD_GO_ATM,
//				AsrKeyType.NAV_WAY_POI_CMD_GO_REPAIR,
//				AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET
//				AsrKeyType.SWITCH_ROLE, 
//				AsrKeyType.GUOYU_MM, 
//				AsrKeyType.GUOYU_GG, 
//				AsrKeyType.ZHOUXINGXING,
//				AsrKeyType.GUANGDONGHUA, 
//				AsrKeyType.LINZHILIN, 
//				AsrKeyType.GUODEGANG, 
//				AsrKeyType.DONGBEIHUA,
//				AsrKeyType.HENANHUA, 
//				AsrKeyType.HUNANHUA, 
//				AsrKeyType.SICHUANHUA, 
//				AsrKeyType.TAIWANHUA,
//				AsrKeyType.BACK_HOME, 
//				AsrKeyType.GO_COMPANY 
				};
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
		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION);
		cmds.add(AsrKeyType.FRONT_TRAFFIC);
		cmds.add(AsrKeyType.SWITCH_MAIN_ROAD);
		cmds.add(AsrKeyType.SWITCH_SIDE_ROAD);
		cmds.add(AsrKeyType.REFRESH_PATH);
//		cmds.add(AsrKeyType.SWITCH_ROLE);
//		cmds.add(AsrKeyType.GUOYU_MM);
//		cmds.add(AsrKeyType.GUOYU_GG);
//		cmds.add(AsrKeyType.ZHOUXINGXING);
//		cmds.add(AsrKeyType.GUANGDONGHUA);
//		cmds.add(AsrKeyType.LINZHILIN);
//		cmds.add(AsrKeyType.GUODEGANG);
//		cmds.add(AsrKeyType.DONGBEIHUA);
//		cmds.add(AsrKeyType.HENANHUA);
//		cmds.add(AsrKeyType.HUNANHUA);
//		cmds.add(AsrKeyType.SICHUANHUA);
//		cmds.add(AsrKeyType.TAIWANHUA);
		return cmds;
	}

	@Override
	public List<Poi> getJingYouPois(){
		mTJPois= NavAmapValueService.getInstance().getTjPoiList();
		LocationInfo lastLocation = LocationManager.getInstance().getLastLocation();
		if(lastLocation!=null&&lastLocation.msgGpsInfo!=null){
			for(Poi poi:mTJPois){
				poi.setDistance(
						(int)AMapUtils.calculateLineDistance(
								new LatLng(poi.getLat(), poi.getLng()),
								new LatLng(lastLocation.msgGpsInfo.dblLat,lastLocation.msgGpsInfo.dblLng)));
			}			
		}

		return mTJPois;
	}

	@Override
	public String disableNavWithWayPoi() {
		return "";
	}

	@Override
	public String disableNavWithFromPoi() {
		return "";
	}

	@Override
	public String disableDeleteJingYou() {
		return "";
	}

	@Override
	public String disableProcJingYouPoi() {
		getJingYouPois();
		if(mTJPois.size() >= 3){
			return NativeData.getResString("RS_MAP_POINT_TOO_MUCH");
		}		
		return "";
	}

	public int getMapCode(){
		JNIHelper.logd("getMapCode= "+mNavAmapControl.getMapCode());
		return mNavAmapControl.getMapCode();
	}
	public int getMapDetCode(){
		JNIHelper.logd("getMapDetCode= "+mNavAmapControl.getMapDetCode());
		return mNavAmapControl.getMapDetCode();
	}
	@Override
	public boolean procJingYouPoi(Poi... poi) {
		super.procJingYouPoi(poi);
        NavAmapValueService.getInstance().proJingYouPoiSpeakTask(poi[0].getName());
		if (mNavAmapControl.getMapCode() >= 210) {
			JNIHelper.logd("getMapCode()>210");
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 12104);
			intent.putExtra("EXTRA_NAVI_VIA_MODIFY", 1);
			intent.putExtra("EXTRA_MIDNAME", poi[0].getName());
			intent.putExtra("EXTRA_MIDLAT", poi[0].getLat());
			intent.putExtra("EXTRA_MIDLON", poi[0].getLng());
			// mTJPois.add(poi[0]);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			GlobalContext.get().sendBroadcast(intent);
			return true;
		}
		if (mNavigateInfo == null) {
			return false;
		}
        if(mNavAmapControl.getMapCode() >= 210){
        	JNIHelper.logd("getMapCode()>210");
  			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
  			intent.putExtra("KEY_TYPE", 12104);
  			intent.putExtra("EXTRA_NAVI_VIA_MODIFY", 1);
  			intent.putExtra("EXTRA_MIDNAME", poi[0].getName());
  			intent.putExtra("EXTRA_MIDLAT", poi[0].getLat());
  			intent.putExtra("EXTRA_MIDLON", poi[0].getLng());
//  			mTJPois.add(poi[0]);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
  			GlobalContext.get().sendBroadcast(intent);
  			return true;
        }else if (mNavAmapControl.getMapCode() < 142) {
        	JNIHelper.logd("getMapCode()<142");
			if (PackageManager.getInstance().getVerionCode(getPackageName()) < 541) {
				return false;
			}
		}
		
		if (mTJPois.size() >= 3) {
			return true;
		}
		mTJPois.add(poi[0]);
		{
			// 按照距离排途经点
			Collections.sort(mTJPois, new Comparator<Poi>() {

				@Override
				public int compare(Poi lhs, Poi rhs) {
					return lhs.getDistance() - rhs.getDistance();
				}
			});
		}
		JNIHelper.logd("procJingYouPoi:" + poi[0].toString());
		// 途经点重新发起导航
		startNavByWayPois(-1);
		
		mIsJingYouPlan = true;
		return true;
	}
	
	void startNavByWayPois(int style) {
		JNIHelper.logd("startNavByWayPois style:" + style);
		
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10032);

		for (int i = 0; i < mTJPois.size(); i++) {
			if (i == 0) {
				intent.putExtra("EXTRA_FMIDLAT", mTJPois.get(0).getLat());
				intent.putExtra("EXTRA_FMIDLON", mTJPois.get(0).getLng());
				intent.putExtra("EXTRA_FMIDNAME", mTJPois.get(0).getName());
			}

			if (i == 1) {
				intent.putExtra("EXTRA_SMIDLAT", mTJPois.get(1).getLat());
				intent.putExtra("EXTRA_SMIDLON", mTJPois.get(1).getLng());
				intent.putExtra("EXTRA_SMIDNAME", mTJPois.get(1).getName());
			}

			if (i == 2) {
				intent.putExtra("EXTRA_TMIDLAT", mTJPois.get(2).getLat());
				intent.putExtra("EXTRA_TMIDLON", mTJPois.get(2).getLng());
				intent.putExtra("EXTRA_TMIDNAME", mTJPois.get(2).getName());
			}
		}

		if (mNavigateInfo != null) {
			if (mNavigateInfo.msgGpsInfo != null) {
				intent.putExtra("EXTRA_DLAT", mNavigateInfo.msgGpsInfo.dblLat);
				intent.putExtra("EXTRA_DLON", mNavigateInfo.msgGpsInfo.dblLng);
			}
			intent.putExtra("EXTRA_DNAME", mNavigateInfo.strTargetName);
		}

		intent.putExtra("EXTRA_DEV", 0);
		/**
		 * EXTRA_M =0（速度快）=1（费用少） =2（路程短）=3 不走高速 =4（躲避拥堵） EXTRA_M =5（不走高速且避免收费）
		 * =6（不走高速且躲避拥堵） EXTRA_M =7（躲避收费和拥堵） =8（不走高速躲避收费和拥堵） EXTRA_M =20 （高速优先）
		 * =24（高速优先且躲避拥堵） EXTRA_M =-1（地图内部设置默认规则）
		 */
		intent.putExtra("EXTRA_M", style);
		JNIHelper.logd("GDAuto:intent= "+intent);
		JNIHelper.logd("GDAuto:intent.getExtras()= "+intent.getExtras());
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	@Override
	public boolean speakLimitSpeech() {
		queryLimitSpeed();
		return true;
	}
	
	@Override
	public PathInfo getCurrentPathInfo() {
		return convertPathInfo(NavAmapValueService.getInstance().mRoadInfo);
	}
	
	private PathInfo convertPathInfo(RoadInfo info) {
		PathInfo pathInfo = null;
		if (info != null) {
			pathInfo = new PathInfo();
			pathInfo.fromPoiAddr = info.fromPoiAddr;
			pathInfo.fromPoiLat = info.fromPoiLat;
			pathInfo.fromPoiLng = info.fromPoiLng;
			pathInfo.fromPoiName = info.fromPoiName;
			pathInfo.toCity = info.toCity;
			pathInfo.toPoiAddr = info.toPoiAddr;
			pathInfo.toPoiLat = info.toPoiLat;
			pathInfo.toPoiLng = info.toPoiLng;
			pathInfo.toPoiName = info.toPoiName;
		}
		return pathInfo;
	}

	private String mRealPackageName;
	
	@Override
	public void setPackageName(String packageName) {
		mRealPackageName = packageName;
	}

	@Override
	public String getPackageName() {
		if (!TextUtils.isEmpty(mRealPackageName) && PackageManager.getInstance().checkAppExist(mRealPackageName)) {
			return mRealPackageName;
		}
		
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME_LITE)) {
			return PACKAGE_NAME_LITE;
		}
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME)) {
			return PACKAGE_NAME;
		}
		return PACKAGE_NAME;
	}

	@Override
	public boolean showTraffic(String city, String addr) {
		try {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("androidauto://viewGeo?sourceApplication=txz&addr=" + city + addr));
			GlobalContext.get().startActivity(intent);

			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchTraffic(true);
				}
			}, 2000);
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public boolean showTraffic(final double lat, final double lng) {
		try {
			PackageManager.getInstance().openApp(getPackageName());
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
					intent.putExtra("KEY_TYPE", 10013);
					intent.putExtra("EXTRA_LAT", lat);
					intent.putExtra("EXTRA_LON", lng);
					intent.putExtra("EXTRA_DEV", 0);
					intent.putExtra("SOURCE_APP", "txz");
					JNIHelper.logd("GDAuto:intent= "+intent);
					JNIHelper.logd("GDAuto:intent.getExtras()= "+intent.getExtras());
					intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					GlobalContext.get().sendBroadcast(intent);
					mNavAmapControl.switchTraffic(true);
				}
			}, 2000);
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public boolean isInNav() {
		return mIsStarted /* && mIsPlaned */;
	}
	
	@Override
	public void queryHomeCompanyAddr() {
		NavAmapValueService.getInstance().startQueryHomeCompany();
	}
	
	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		setNavigateInfo(true, navigateInfo);
	}
	
	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		setNavigateInfo(false, navigateInfo);
	}

	protected void setNavigateInfo(final boolean isHome, final NavigateInfo navigateInfo) {
		JNIHelper.logd("amapauto set " + (isHome ? "home" : "company"));
		if (!isInFocus()) {
			PackageManager.getInstance().openApp(getPackageName());
		}
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				intent.putExtra("KEY_TYPE", 10058);
				intent.putExtra("POINAME", navigateInfo.strTargetName);
				if (mNavAmapControl.getMapDetCode() >= 1432227
						|| mNavAmapControl.isAmapautoLite()) {
					intent.putExtra("LON", navigateInfo.msgGpsInfo.dblLng);
					intent.putExtra("LAT", navigateInfo.msgGpsInfo.dblLat);
				} else {
					intent.putExtra("LON", String.valueOf(navigateInfo.msgGpsInfo.dblLng));
					intent.putExtra("LAT", String.valueOf(navigateInfo.msgGpsInfo.dblLat));
				}
				intent.putExtra("ADDRESS", navigateInfo.strTargetAddress);
				intent.putExtra("EXTRA_TYPE", isHome ? 1 : 2);
				intent.putExtra("DEV", 0);
				JNIHelper.logd("GDAuto:intent= "+intent);
				JNIHelper.logd("GDAuto:intent.getExtras()= "+intent.getExtras());
				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 3000);
	}

	@Override
	public boolean willNavAfterSet() {
		return true;
	}

	@Override
	public double[] getDestinationLatlng() {
		RoadInfo roadInfo = NavAmapValueService.getInstance().mRoadInfo;

		if (roadInfo != null ) {
			double[] latlng = { roadInfo.toPoiLat, roadInfo.toPoiLng };
			return latlng;
		}
		return null;
	}

	@Override
	public String getDestinationCity() {
		RoadInfo roadInfo = NavAmapValueService.getInstance().mRoadInfo;

		if (roadInfo != null ) {
			String  citty =roadInfo.toCity;
			return citty;
		}
		return null;
	}

	@Override
	public  int getOnWaySearchToolCode(String keyword) {
		int value = ConfigFileHelper.getInstance(GlobalContext.get()).ConfigValue(
				ConfigFileHelper.ONWAY_SEARCH, ConfigFileHelper.NAV_IMP_GAODE, keyword, getMapDetCode(),-1);
		return -1==value?-1:ConfigFileHelper.POI_SEARCH_CODE_GAODE;
	}
	public boolean isDeepSearch(String keyword) {
		return  ConfigFileHelper.getInstance(GlobalContext.get()).ConfigValue(
				ConfigFileHelper.DEEP_SEARCH, ConfigFileHelper.NAV_IMP_GAODE, keyword, getMapDetCode());
	}

	@Override
	public boolean deleteJingYou(Poi poi){
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 12104);
		intent.putExtra("EXTRA_NAVI_VIA_MODIFY", 2);
		intent.putExtra("EXTRA_MIDNAME", poi.getName());
		intent.putExtra("EXTRA_MIDLAT", poi.getLat());
		intent.putExtra("EXTRA_MIDLON", poi.getLng());
		JNIHelper.logd("GDAuto:intent= "+intent);
		JNIHelper.logd("GDAuto:intent.getExtras()= "+intent.getExtras());
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		GlobalContext.get().sendBroadcast(intent);
		AppLogic.runOnBackGround(new Runnable() {
			
			@Override
			public void run() {
				mNavAmapControl.zoomAll(new Runnable() {		
					@Override
					public void run() {
						
					}
				});
			}
		}, 2000);
		return true;
	}
	@Override
	public boolean reportTraffic(int event) {
		if (getMapCode() >= 215 && mNavAmapControl.isAmapautoLite()) {
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			int reportType = 1;
			if(event>0){
				reportType = 2;
			}
			intent.putExtra("KEY_TYPE", 12108);
			intent.putExtra("REPORT_OPERATE_TYPE", 1);
			intent.putExtra("REPORT_TYPE", reportType);
			if(reportType == 2 ){
				intent.putExtra("REPORT_EVENT_TYPE", event);
			}
			intent.setFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			GlobalContext.get().sendBroadcast(intent);
			return true;
		} else {
			return super.reportTraffic(event);
		}
	}
}