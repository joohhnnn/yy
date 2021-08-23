package com.txznet.txz.component.nav.baidu;

import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNEnlargeRoad;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGAssistant;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCarFreeStatus;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCarInfo;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCarTunelInfo;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCruiseEnd;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCruiseStart;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCurShapeIndexUpdate;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGCurrentRoad;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGDestInfo;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGGPSLost;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGGPSNormal;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGManeuver;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGNaviEnd;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGNaviStart;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGNearByCameraInfo;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGNextRoad;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGRPYawComplete;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGRPYawing;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGRemainInfo;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGRouteInfo;
import com.baidu.navisdk.hudsdk.BNRemoteMessage.BNRGServiceArea;
import com.baidu.navisdk.hudsdk.client.HUDConstants;
import com.baidu.navisdk.hudsdk.client.HUDSDkEventCallback.OnRGInfoEventCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.NavInfo;
import com.txznet.txz.component.nav.baidu.info.StringUtils;
import com.txznet.txz.component.nav.baidu.info.StringUtils.UnitLangEnum;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.runnables.Runnable1;

import android.text.TextUtils;

public class BaiduHudInfo implements OnRGInfoEventCallback {
	public static final boolean ENABLE_LOG = true;
	private NavInfo mNavInfo = new NavInfo();

	private NavBaiduDeepImpl mDeepImpl;

	public BaiduHudInfo(NavBaiduDeepImpl navImpl) {
		this.mDeepImpl = navImpl;
	}

	public NavInfo getCurrNavInfo() {
		return mNavInfo;
	}

	@Override
	public void onAssistant(BNRGAssistant arg0) {
		logBDHudInfo("onAssistant");
		if (arg0 == null) {
			return;
		}
		if (getCurrNavInfo() != null) {
			String assistantTips = "";
			String assistantTypeS = "合流";
			if (arg0.getAssistantDistance() > 0) {
				switch (arg0.getAssistantType()) {
				case HUDConstants.AssistantType.JOINT:
					assistantTypeS = "合流";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.TUNNEL:
					assistantTypeS = "隧道";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.BRIDGE:
					assistantTypeS = "桥梁";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.RAILWAY:
					assistantTypeS = "铁路";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.BLIND_BEND:
					assistantTypeS = "急转弯";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.BLIND_SLOPE:
					assistantTypeS = "陡坡";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.ROCKFALL:
					assistantTypeS = "落石";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.ACCIDENT:
					assistantTypeS = "事故多发区";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.SPEED_CAMERA:
					assistantTypeS = "测速摄像";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS + "限速： "
							+ arg0.getAssistantLimitedSpeed();
					break;
				case HUDConstants.AssistantType.TRAFFIC_LIGHT_CAMERA:
					assistantTypeS = "交通信号灯摄像";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.INTERVAL_CAMERA:
					assistantTypeS = "区间测速";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.CHILDREN:
					assistantTypeS = "注意儿童";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.UNEVEN:
					assistantTypeS = "路面不平";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.NARROW:
					assistantTypeS = "道路变窄";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.VILLAGE:
					assistantTypeS = "前面村庄";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.SLIP:
					assistantTypeS = "路面易滑";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.OVER_TAKE_FORBIDEN:
					assistantTypeS = "禁止超车";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				case HUDConstants.AssistantType.HONK:
					assistantTypeS = "请铭喇叭";
					assistantTips = "前方" + getFormatAfterMeters(arg0.getAssistantDistance()) + assistantTypeS;
					break;
				default:
					break;
				}
			}

			final String assistantTipstr = assistantTips;
			if (getCurrNavInfo() != null) {
				mNavInfo.dirDistance = (long) arg0.getAssistantDistance();
				mNavInfo.currentLimitedSpeed = (long) arg0.getAssistantLimitedSpeed();
			}
		}

		sendNavInfo();
	}

	public static final String RG_SG_AFTER_METERS = "<Data><![CDATA[%1$s]]></Data>";

	/**
	 * 根据剩余距离获取格式化的字符串，如 200米后
	 * 
	 * @param nextRemainDist
	 * @return
	 */
	private String getFormatAfterMeters(int nextRemainDist) {
		StringBuffer distance = new StringBuffer();
		StringUtils.formatDistance(nextRemainDist, UnitLangEnum.ZH, distance);
		return String.format(RG_SG_AFTER_METERS, distance);
	}

	@Override
	public void onCarFreeStatus(BNRGCarFreeStatus arg0) {
		// TODO Auto-generated method stub
		logBDHudInfo("onCarFreeStatus");

	}

	@Override
	public void onCarInfo(BNRGCarInfo arg0) {
		logBDHudInfo("onCarInfo");
		if (arg0 == null) {
			return;
		}
		int curSpeed = arg0.getCurSpeed();
		if (getCurrNavInfo() != null) {
			mNavInfo.currentSpeed = (long) curSpeed;
		}

		sendNavInfo();
	}

	@Override
	public void onCarTunelInfo(BNRGCarTunelInfo arg0) {
		logBDHudInfo("onCarTunelInfo");
		// TODO Auto-generated method stub

	}

	@Override
	public void onCruiseEnd(BNRGCruiseEnd arg0) {
		logBDHudInfo("onCruiseEnd");
		// TODO Auto-generated method stub

	}

	@Override
	public void onCruiseStart(BNRGCruiseStart arg0) {
		logBDHudInfo("onCruiseStart");
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurShapeIndexUpdate(BNRGCurShapeIndexUpdate arg0) {
		logBDHudInfo("onCurShapeIndexUpdate");
		// TODO Auto-generated method stub

	}

	@Override
	public void onCurrentRoad(BNRGCurrentRoad arg0) {
		logBDHudInfo("onCurrentRoad");
		if (arg0 == null) {
			return;
		}
		if (getCurrNavInfo() != null) {
			mNavInfo.currentRoadName = arg0.getCurrentRoadName();
		}

		sendNavInfo();
	}

	@Override
	public void onDestInfo(BNRGDestInfo arg0) {
		logBDHudInfo("onDestInfo");
		// TODO Auto-generated method stub

	}

	@Override
	public void onEnlargeRoad(BNEnlargeRoad arg0) {
		if (arg0 == null) {
			return;
		}

		logBDHudInfo("onEnlargeRoad");
		String roadName = arg0.getRoadName();
		int totalDist = arg0.getTotalDist();
		int remDist = arg0.getRemainDist();
		if (getCurrNavInfo() != null) {
			mNavInfo.currentRoadName = roadName;
			mNavInfo.totalDistance = (long) totalDist;
			mNavInfo.remainDistance = (long) remDist;
		}

		sendNavInfo();
	}

	@Override
	public void onGPSLost(BNRGGPSLost arg0) {
		logBDHudInfo("onGPSLost");
		// TODO Auto-generated method stub

	}

	@Override
	public void onGPSNormal(BNRGGPSNormal arg0) {
		logBDHudInfo("onGPSNormal");
	}

	@Override
	public void onManeuver(BNRGManeuver arg0) {
		logBDHudInfo("onManeuver");
		if (arg0 == null) {
			return;
		}

		if (getCurrNavInfo() != null) {
			mNavInfo.dirDistance = (long) arg0.getManeuverDistance();
			mNavInfo.nextRoadName = arg0.getNextRoadName();
			String dir = arg0.name;
			if (!TextUtils.isEmpty(dir)) {
				if ("turn_front".equals(dir)) {
					mNavInfo.dirDes = "直行";
				} else if ("turn_right_front".equals(dir)) {
					mNavInfo.dirDes = "右前方转弯";
				} else if ("turn_right".equals(dir)) {
					mNavInfo.dirDes = "右转";
				} else if ("turn_right_back".equals(dir)) {
					mNavInfo.dirDes = "右后方转弯";
				} else if ("turn_back".equals(dir)) {
					mNavInfo.dirDes = "掉头";
				} else if ("turn_left_back".equals(dir)) {
					mNavInfo.dirDes = "左后方转弯";
				} else if ("turn_left".equals(dir)) {
					mNavInfo.dirDes = "左转";
				} else if ("turn_left_front".equals(dir)) {
					mNavInfo.dirDes = "左前方转弯";
				}
			}
		}

		sendNavInfo();
	}

	@Override
	public void onNaviEnd(BNRGNaviEnd arg0) {
		logBDHudInfo("onNaviEnd");
		if (getCurrNavInfo() != null) {
			getCurrNavInfo().reset();
		}

		getCurrNavInfo().toolPKN = BaiduVersion.mCurrNavPackageName;
		sendNavInfo();
	}

	@Override
	public void onNaviStart(BNRGNaviStart arg0) {
		logBDHudInfo("onNaviStart");
		if (arg0 == null) {
			return;
		}

		if (getCurrNavInfo() == null) {
			mNavInfo = new NavInfo();
		}
		mNavInfo.toolPKN = BaiduVersion.getCurPackageName();
	}

	@Override
	public void onNearByCamera(BNRGNearByCameraInfo arg0) {
		logBDHudInfo("onNearByCamera");
		// TODO Auto-generated method stub

	}

	@Override
	public void onNextRoad(BNRGNextRoad arg0) {
		logBDHudInfo("onNextRoad");
		if (arg0 == null) {
			return;
		}

		if (getCurrNavInfo() != null) {
			mNavInfo.nextRoadName = arg0.getNextRoadName();
		}

		sendNavInfo();
	}

	@Override
	public void onRemainInfo(BNRGRemainInfo arg0) {
		logBDHudInfo("onRemainInfo");
		if (arg0 == null) {
			return;
		}
		int remainTime = arg0.getRemainTime();
		int remainDistance = arg0.getRemainDistance();
		if (getCurrNavInfo() != null) {
			mNavInfo.remainTime = (long) remainTime;
			mNavInfo.remainDistance = (long) remainDistance;
		}

		sendNavInfo();
	}

	@Override
	public void onRouteInfo(BNRGRouteInfo arg0) {
		logBDHudInfo("onRouteInfo");
		// TODO Auto-generated method stub

	}

	@Override
	public void onRoutePlanYawComplete(BNRGRPYawComplete arg0) {
		logBDHudInfo("onRoutePlanYawComplete");
		// TODO Auto-generated method stub

	}

	@Override
	public void onRoutePlanYawing(BNRGRPYawing arg0) {
		// TODO Auto-generated method stub
		logBDHudInfo("onRoutePlanYawing");
	}

	@Override
	public void onServiceArea(BNRGServiceArea arg0) {
		logBDHudInfo("onServiceArea");
	}

	private void logBDHudInfo(String msg) {
		if (ENABLE_LOG) {
			JNIHelper.logd(msg);
		}
	}

	Runnable mHudInfoTask = new Runnable() {

		@Override
		public void run() {
			try {
				if (mNavInfo != null) {
					String navJson = mNavInfo.toJson();
					mDeepImpl.broadNaviInfo(navJson);
					JNIHelper.logi("navJson:" + navJson);
				}
			} catch (Exception e) {
			}
		}
	};

	private void sendNavInfo() {
		AppLogic.removeBackGroundCallback(mHudInfoTask);
		AppLogic.runOnBackGround(mHudInfoTask, 50);
	}
}
