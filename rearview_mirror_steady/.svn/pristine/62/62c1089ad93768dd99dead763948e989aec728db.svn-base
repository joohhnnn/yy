package com.txznet.txz.component.nav.gaode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.ProcessUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.nav.IMapInterface.PlanStyle;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.gaode.NavAmapControl.IAmapNavContants;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.NavAmapRecvListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.LocationUtil;

public class NavAmapAutoNavImpl extends NavThirdComplexApp implements IAmapNavContants {
	public static final String PACKAGE_PREFIX = "com.autonavi.amap";
	public static final String PACKAGE_NAME = "com.autonavi.amapauto";
	public static final String PACKAGE_NAME_LITE = "com.autonavi.amapautolite";

	NavInfo mNavInfo;
	NavigateInfo mNavigateInfo;
	List<Poi> mTJPois = new ArrayList<Poi>();
	protected NavAmapControl mNavAmapControl;

	boolean mIsNorth;
	boolean mNowZoomIn;
	
	public static boolean mHadEnterNav;

	public NavAmapAutoNavImpl() {
		mNavAmapControl = new NavAmapControl(this, null);
		try {
			NavAmapValueService.getInstance().addRecvListener(new NavAmapRecvListener() {

				@Override
				public void onReceive(Intent intent) {
					doReceive(intent);
				}
			});
			initProcess();
		} catch (Exception e) {
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
				onResume();
				if (!mHadEnterNav) {
					mHadEnterNav = true;
					mIsForceRegister = true;
					// 强制注册导航唤醒词
					mNavCmdControl.resetSession();
					checkInNav();
				}
				break;

			case EXIT_APP:
				onPause();
				onEnd(false);
				NavAmapValueService.getInstance().destoryAllSelectAsrs();
				break;

			case FRONT:
				onResume();
				NavAmapValueService.getInstance().onStateChange(true);
				break;

			case BGROUND:
				onPause();
				NavAmapValueService.getInstance().onStateChange(false);
				break;

			case PLAN_SUC:
				onPlanComplete();
				break;

			case PLAN_FAIL:
				break;

			case START_NAVI:
				onStart();

				break;

			case END_NAVI:
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
				NavAmapValueService.getInstance().destorySelectAsrTask(NavAmapValueService.TASK_CONTINUE_NAVI_ID);
				break;

			case PLANFAIL_DISMISS:
				NavAmapValueService.getInstance().destorySelectAsrTask(NavAmapValueService.TASK_PLAN_FAIL);
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
		NavAmapValueService.getInstance().enableWakeup(mIsRegisterWakeup);
	}

	@Override
	public void exitNav() {
		try {
			mNavAmapControl.naviExit();
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.appExit();
				}
			}, 1000);
			JNIHelper.logd("exit amapauto");
		} catch (Exception e) {
		}

		mNavCmdControl.unRegisterCmds();
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
			json.put("sence", "nav");
			json.put("text", command);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}

		JNIHelper.logd("onNavCommSelect:" + isWakeupResult + "," + type + "," + command);
		if (!mIsFocus && (!AsrKeyType.EXIT_NAV.equals(type) && !AsrKeyType.CLOSE_MAP.equals(type))) {
			enterNav();
		}

		if (AsrKeyType.ZOOM_IN.equals(type)) {
			onCommTypeSelect(isWakeupResult, "RS_MAP_ZOOMIN", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.zoomMap(true);
				}
			});
		}
		if (AsrKeyType.ZOOM_OUT.equals(type)) {
			onCommTypeSelect(isWakeupResult, "RS_MAP_ZOOMOUT", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.zoomMap(false);
				}
			});
		}
		if (AsrKeyType.NIGHT_MODE.equals(type)) {
			onCommTypeSelect(true, "RS_MAP_NIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchLightNightMode(false);
				}
			});
		}
		if (AsrKeyType.LIGHT_MODE.equals(type)) {
			onCommTypeSelect(true, "RS_MAP_LIGHT_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchLightNightMode(true);
				}
			});
		}
		if(AsrKeyType.AUTO_MODE.equals(type)){
			onCommTypeSelect(true, "RS_MAP_AUTO_MODE", new Runnable() {
				
				@Override
				public void run() {
					Intent intent = new Intent(IAmapNavContants.SEND_ACTION);
					intent.putExtra("KEY_TYPE", 10048);
					intent.putExtra("EXTRA_DAY_NIGHT_MODE", 0);
					GlobalContext.get().sendBroadcast(intent);
				}
			});
			return;
		}
		if (AsrKeyType.OPEN_TRAFFIC.equals(type)) {
			onCommTypeSelect(true, "RS_MAP_OPEN_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchTraffic(true);
				}
			});
		}
		if (AsrKeyType.CLOSE_TRAFFIC.equals(type)) {
			onCommTypeSelect(true, "RS_MAP_CLOSE_TRAFFIC", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchTraffic(false);
				}
			});
		}
		if (AsrKeyType.TWO_MODE.equals(type)) {
			onCommTypeSelect(isWakeupResult, "RS_MAP_TWO_MODE", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switch23D(true, mIsNorth ? 1 : 0);
				}
			});
		}
		if (AsrKeyType.THREE_MODE.equals(type)) {
			onCommTypeSelect(isWakeupResult, "RS_MAP_THREE_MODE", new Runnable() {

				@Override
				public void run() {
					mIsNorth = false;
					mNavAmapControl.switch23D(false, 0);
				}
			});
		}
		if (AsrKeyType.CAR_DIRECT.equals(type)) {
			onDialogShow(getConfirmRes("RS_MAP_CAR_DIRECT", command, "已为您"), new Runnable() {

				@Override
				public void run() {
					mIsNorth = false;
					mNavAmapControl.switchCarDirection();
				}
			});
			return;
		}
		if (AsrKeyType.NORTH_DIRECT.equals(type)) {
			onDialogShow(getConfirmRes("RS_MAP_NORTH_DIRECT", command, "已为您"), new Runnable() {

				@Override
				public void run() {
					mIsNorth = true;
					mNavAmapControl.switchNorthDirection();
				}
			});
			return;
		}
		if (AsrKeyType.VIEW_ALL.equals(type)) {
			String tts = NativeData.getResString("RS_MAP_VIEW_ALL");
			tts = tts.replace("%COMMAND%", command);
			Runnable pR = null;
			Runnable aR = null;
			Runnable task = new Runnable() {

				@Override
				public void run() {
					mNowZoomIn = true;
					mNavAmapControl.zoomAll(new Runnable() {

						@Override
						public void run() {
							mNowZoomIn = false;
						}
					});
				}
			};

			if (isWakeupResult) {
				pR = task;
				tts = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", tts);
			} else {
				aR = task;
				tts = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", tts);
			}
			onCommTypeSelect(tts, pR, aR);
		}
		if (AsrKeyType.LESS_MONEY.equals(type)) {
			onConfirmDialogShow(command, "RS_MAP_LESS_MONEY", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.DUOBISHOUFEI);
					JNIHelper.logd("NavAmapAutoNavImpl start LESS_MONEY");
				}
			});
			return;
		}
		if (AsrKeyType.DUOBIYONGDU.equals(type)) {
			onConfirmDialogShow(command, "RS_MAP_DUOBIYONGDU", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.DUOBIYONGDU);
					JNIHelper.logd("NavAmapAutoNavImpl start DUOBIYONGDU");
				}
			});
			return;
		}
		if (AsrKeyType.BUZOUGAOSU.equals(type)) {
			onConfirmDialogShow(command, "RS_MAP_BUZOUGAOSU", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.BUZOUGAOSU);
					JNIHelper.logd("NavAmapAutoNavImpl start BUZOUGAOSU");
				}
			});
			return;
		}
		if (AsrKeyType.GAOSUYOUXIAN.equals(type)) {
			onConfirmDialogShow(command, "RS_MAP_GAOSUYOUXIAN", new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchPlanStyle(PlanStyle.GAOSUYOUXIAN);
					JNIHelper.logd("NavAmapAutoNavImpl start GAOSUYOUXIAN");
				}
			});
			return;
		}
		if (AsrKeyType.HOW_NAVI.equals(type)) {
			if (!isInNav() || mNavInfo == null) {
				if (!isWakeupResult) {
					RecorderWin.close();
				}
				return;
			}

			String dirTxt = mNavInfo.getDirectionDes();
			if (TextUtils.isEmpty(dirTxt)) {
				if (!isWakeupResult) {
					RecorderWin.close();
				}
				return;
			}

			long remainDistance = mNavInfo.dirDistance;
			String distance = getRemainDistance(remainDistance);

			String nextRoad = mNavInfo.nextRoadName;
			String hint = NativeData.getResPlaceholderString("RS_MAP_FRONT",
					"%DISTANCE%", distance + dirTxt);
			if (!TextUtils.isEmpty(nextRoad)) {
				hint = NativeData.getResString("RS_MAP_FRONT_INTO")
						.replace("%DISTANCE%", distance + dirTxt)
						.replace("%ROAD%", nextRoad);
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(hint, null);
			return;
		}
		if (AsrKeyType.ASK_REMAIN.equals(type)) {
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
		if (AsrKeyType.LIMIT_SPEED.equals(type)) {
			if (!isInNav() || mNavInfo == null) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_MAP_NO_SPEEDLIMIT");
				RecorderWin.speakTextWithClose(spk, null);
				return;
			}
			long speed = mNavInfo.currentLimitedSpeed;
			if (speed < 1) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_MAP_NO_SPEEDLIMIT");
				RecorderWin.speakTextWithClose(spk, null);
				return;
			}

			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResPlaceholderString(
					"RS_MAP_SPEEDLIMIT", "%CMD%", speed + "");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (AsrKeyType.BACK_NAVI.equals(type)) {
			if (!mNowZoomIn) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose("", null);
				return;
			}
			mNowZoomIn = false;
			mNavAmapControl.backNavi();
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_VOICE_ALREAD_DO_COMMAND").replace("%CMD%", command);
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		if (AsrKeyType.START_NAVI.equals(type)) {
			startNavByInner();
			RecorderWin.close();
			return;
		}
		if (AsrKeyType.EXIT_NAV.equals(type)) {
			if (!isWakeupResult) {
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
			onConfirmDialogShow(command, "", new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
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
			onConfirmDialogShow(command, "", new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}
		if (AsrKeyType.GUOYU_MM.equals(type)) {
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
	}

	int mRole;

	private void switchRole(boolean isWakeup, int role) {
		if (mRole == role) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(getTTS(role), null);
			return;
		}

		mRole = role;
		if (!isWakeup) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(getSetTTS(role), new Runnable() {

				@Override
				public void run() {
					mNavAmapControl.switchBroadcastRole(mRole);
				}
			});
			return;
		}

		mNavAmapControl.switchBroadcastRole(mRole);
	}

	private String getTTS(int role) {
		switch (role) {
		case 0:
			return HINT_IS_GUOYU_MM;
		case 1:
			return HINT_IS_GUOYU_GG;
		case 2:
			return HINT_IS_ZHOUXINGXING;
		case 3:
			return HINT_IS_GUANGDONGHUA;
		case 4:
			return HINT_IS_LINZHILIN;
		case 5:
			return HINT_IS_GUODEGANG;
		case 6:
			return HINT_IS_DONGBEIHUA;
		case 7:
			return HINT_IS_HENANHUA;
		case 8:
			return HINT_IS_HUNANHUA;
		case 9:
			return HINT_IS_SICHUANHUA;
		case 10:
			return HINT_IS_TAIWANHUA;
		}
		return "";
	}

	private String getSetTTS(int role) {
		switch (role) {
		case 0:
			return HINT_GUOYU_MM;
		case 1:
			return HINT_GUOYU_GG;
		case 2:
			return HINT_ZHOUXINGXING;
		case 3:
			return HINT_GUANGDONGHUA;
		case 4:
			return HINT_LINZHILIN;
		case 5:
			return HINT_GUODEGANG;
		case 6:
			return HINT_DONGBEIHUA;
		case 7:
			return HINT_HENANHUA;
		case 8:
			return HINT_HUNANHUA;
		case 9:
			return HINT_SICHUANHUA;
		case 10:
			return HINT_TAIWANHUA;
		}
		return "";
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		try {
			mTJPois.clear();
			int strategy = -1;
			mNavigateInfo = info;
			double[] dest = LocationUtil.getGCJ02(info.msgGpsInfo);
			if (mEnableSave) {
				if (mPlanStyle != -1) {
					strategy = mPlanStyle;
				}
			}

			mNavAmapControl.navigateTo(info.strTargetName, dest[0], dest[1], strategy);
			return true;
		} catch (Exception e) {
			JNIHelper.loge(e.toString());
			return false;
		}
	}

	@Override
	public List<String> getBanCmds() {
		List<String> cmds = null;
		int mapPrefix = mNavAmapControl.getMapCode();
		int mapDetail = mNavAmapControl.getMapDetCode();
		JNIHelper.logd("mapPrefix code:" + mapPrefix + ",mapDetail:" + mapDetail);
		if (mapPrefix < 142 && PackageManager.getInstance().getVerionCode(getPackageName()) < 541) {
			if (cmds == null) {
				cmds = new ArrayList<String>();
			}
			// cmds.add(AsrKeyType.DONGBEIHUA);
			// cmds.add(AsrKeyType.HENANHUA);
			// cmds.add(AsrKeyType.HUNANHUA);
			// cmds.add(AsrKeyType.SICHUANHUA);
			// cmds.add(AsrKeyType.TAIWANHUA);
			cmds.add(AsrKeyType.AUTO_MODE);
		}

		if (mapPrefix >= 143 && mapDetail < 1432227 && !mNavAmapControl.isAmapautoLite()) {
			if (cmds == null) {
				cmds = new ArrayList<String>();
			}
			cmds.add(AsrKeyType.GAOSUYOUXIAN);
		}

		if (mapPrefix >= 200 && mNavAmapControl.isAmapautoLite()) {
			if (cmds == null) {
				cmds = new ArrayList<String>();
			}
			cmds.add(AsrKeyType.TWO_MODE);
			cmds.add(AsrKeyType.THREE_MODE);
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
				AsrKeyType.AUTO_MODE,
				AsrKeyType.EXIT_NAV, 
				AsrKeyType.CLOSE_MAP, 
				AsrKeyType.VIEW_ALL, 
				AsrKeyType.DUOBIYONGDU,
				AsrKeyType.BUZOUGAOSU, 
				AsrKeyType.GAOSUYOUXIAN, 
				AsrKeyType.LESS_MONEY, 
				AsrKeyType.LESS_DISTANCE,
				AsrKeyType.HOW_NAVI, 
				AsrKeyType.ASK_REMAIN, 
				AsrKeyType.LIMIT_SPEED, 
				AsrKeyType.BACK_NAVI,
				AsrKeyType.START_NAVI, 
				AsrKeyType.OPEN_TRAFFIC, 
				AsrKeyType.CLOSE_TRAFFIC, 
				AsrKeyType.TWO_MODE,
				AsrKeyType.THREE_MODE,
				AsrKeyType.CAR_DIRECT, 
				AsrKeyType.NORTH_DIRECT, 
				AsrKeyType.SWITCH_ROLE,
				AsrKeyType.GUOYU_MM, 
				AsrKeyType.GUOYU_GG, 
				AsrKeyType.ZHOUXINGXING, 
				AsrKeyType.GUANGDONGHUA,
				AsrKeyType.LINZHILIN, 
				AsrKeyType.GUODEGANG, 
				AsrKeyType.DONGBEIHUA, 
				AsrKeyType.HENANHUA,
				AsrKeyType.HUNANHUA, 
				AsrKeyType.SICHUANHUA, 
				AsrKeyType.TAIWANHUA,
				AsrKeyType.BACK_HOME,
				AsrKeyType.GO_COMPANY
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
		cmds.add(AsrKeyType.LIMIT_SPEED);
		cmds.add(AsrKeyType.BACK_NAVI);
		cmds.add(AsrKeyType.SWITCH_ROLE);
		cmds.add(AsrKeyType.GUOYU_MM);
		cmds.add(AsrKeyType.GUOYU_GG);
		cmds.add(AsrKeyType.ZHOUXINGXING);
		cmds.add(AsrKeyType.GUANGDONGHUA);
		cmds.add(AsrKeyType.LINZHILIN);
		cmds.add(AsrKeyType.GUODEGANG);
		cmds.add(AsrKeyType.DONGBEIHUA);
		cmds.add(AsrKeyType.HENANHUA);
		cmds.add(AsrKeyType.HUNANHUA);
		cmds.add(AsrKeyType.SICHUANHUA);
		cmds.add(AsrKeyType.TAIWANHUA);
		return cmds;
	}

	@Override
	public String disableProcJingYouPoi() {
		if (mTJPois.size() >= 3) {
			return NativeData.getResString("RS_MAP_POINT_TOO_MUCH");
		}
		return "";
	}

	@Override
	public boolean procJingYouPoi(Poi... poi) {
		if (mNavigateInfo == null) {
			return false;
		}

		if (mNavAmapControl.getMapCode() < 142) {
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

		intent.putExtra("EXTRA_DLAT", mNavigateInfo.msgGpsInfo.dblLat);
		intent.putExtra("EXTRA_DLON", mNavigateInfo.msgGpsInfo.dblLng);
		intent.putExtra("EXTRA_DNAME", mNavigateInfo.strTargetName);

		intent.putExtra("EXTRA_DEV", 0);
		// 0（速度快）=1（费用少） =2（路程短）=3 不走高速 =4
		intent.putExtra("EXTRA_M", 0);
		GlobalContext.get().sendBroadcast(intent);
		mIsJingYouPlan = true;
		return true;
	}

	@Override
	public String getPackageName() {
		if (!TextUtils.isEmpty(mRemotePackageName)) {
			return mRemotePackageName;
		}
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME)) {
			return PACKAGE_NAME;
		}
		if (PackageManager.getInstance().checkAppExist(PACKAGE_NAME_LITE)) {
			return PACKAGE_NAME_LITE;
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
		return mIsStarted && mIsPlaned;
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
		PackageManager.getInstance().openApp(getPackageName());
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
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 3000);
	}

	@Override
	public boolean willNavAfterSet() {
		return true;
	}
}