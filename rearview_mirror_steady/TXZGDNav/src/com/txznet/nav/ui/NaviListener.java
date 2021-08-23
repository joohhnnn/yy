package com.txznet.nav.ui;

import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.AMapNaviViewListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.manager.AMapProcessor;
import com.txznet.nav.manager.NaviDataManager;
import com.txznet.nav.manager.NavManager;
import com.txznet.txz.util.runnables.Runnable1;

public class NaviListener implements AMapNaviViewListener, AMapNaviListener {

	private int mCurrentSpeakTaskId;

	public boolean mCarLock = true;
	public boolean mIsRePlanByHand = false;

	private static NaviListener mNaviListener;

	private NaviListener() {
	}

	public static NaviListener getInstance() {
		if (mNaviListener == null) {
			synchronized (NaviListener.class) {
				if (mNaviListener == null) {
					mNaviListener = new NaviListener();
				}
			}
		}

		return mNaviListener;
	}

	@Override
	public void onLockMap(boolean arg0) {
		LogUtil.logd("onLockMap");
		NaviCustomView mNcv = NaviCustomView.getInstance();
		if (mNcv != null) {
			if (!arg0) {
				mNcv.setCustomView(true);
			} else {
				mNcv.setCustomView(false);
			}
		}
	}

	@Override
	public void onNaviCancel() {
		NavManager.getInstance().stopNavi(true);
	}

	@Override
	public void onNaviMapMode(int arg0) {
	}

	@Override
	public void onNaviSetting() {
	}

	@Override
	public void onNaviTurnClick() {
	}

	@Override
	public void onNextRoadClick() {
	}

	@Override
	public void onScanViewButtonClick() {
	}

	@Override
	public void onArriveDestination() {
		NavManager.getInstance().stopNavi(true);
	}

	@Override
	public void onArrivedWayPoint(int arg0) {
	}

	@Override
	public void onCalculateRouteFailure(int arg0) {
		String calculateResult = "";
		if (arg0 == 2) {
			calculateResult = "路径规划失败，请检查网络连接是否正常";
		}

		TtsUtil.speakText(calculateResult);
		if (mIsRePlanByHand) {
			mIsRePlanByHand = false;
		}
	}

	@Override
	public void onCalculateRouteSuccess() {
		if (mIsRePlanByHand) {
			AMapNaviPath naviPath = AMapNavi
					.getInstance(AppLogic.getApp()).getNaviPath();
			if (naviPath != null) {
				NaviDataManager.getInstance().updateNaviPath(naviPath);
				NaviCustomView.getInstance().lookAllPath(true, false);
			}
			mIsRePlanByHand = false;
		}
	}

	@Override
	public void onEndEmulatorNavi() {
	}

	@Override
	public void onGetNavigationText(int arg0, String arg1) {
		mCurrentSpeakTaskId = TtsUtil.speakText(arg1);
	}

	@Override
	public void onGpsOpenStatus(boolean arg0) {
	}

	@Override
	public void onInitNaviFailure() {

	}

	@Override
	public void onInitNaviSuccess() {

	}

	@Override
	public void onLocationChange(AMapNaviLocation arg0) {

	}

	@Override
	public void onNaviInfoUpdate(NaviInfo arg0) {
		AMapProcessor.getInstance().checkProcessResumeQueue(0);

		if (arg0 == null) {
			return;
		}

		AppLogic.runOnBackGround(new Runnable1<NaviInfo>(arg0) {

			@Override
			public void run() {
				AMapProcessor.getInstance().procOnNaviInfoUpdate(mP1);
			}
		}, 0);
	}

	@Override
	public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
		AMapProcessor.getInstance().checkProcessResumeQueue(0);

		if (naviInfo == null) {
			return;
		}

		AppLogic.runOnBackGround(
				new Runnable1<AMapNaviInfo>(naviInfo) {

					@Override
					public void run() {
						AMapProcessor.getInstance().onNaviInfoUpdated(mP1);
					}
				}, 0);
	}

	@Override
	public void onReCalculateRouteForTrafficJam() {
		TtsUtil.speakText("前方路线拥堵，路线重新规划");
	}

	@Override
	public void onReCalculateRouteForYaw() {
		TtsUtil.speakText("您已偏航");
	}

	@Override
	public void onStartNavi(int arg0) {
		AMapProcessor.getInstance().procOnStartNavi(arg0);
	}

	@Override
	public void onTrafficStatusUpdate() {
	}

	public void onDestory() {
		TtsUtil.cancelSpeak(mCurrentSpeakTaskId);
		mNaviListener = null;
	}

	@Override
	public boolean onNaviBackClick() {
		return false;
	}

	@Override
	public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
	}

	@Override
	public void hideCross() {
		AMapProcessor.getInstance().procHideCross();
	}

	@Override
	public void hideLaneInfo() {
	}

	@Override
	public void showCross(AMapNaviCross arg0) {
		AMapProcessor.getInstance().procShowCross(arg0);
	}

	@Override
	public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
	}
}