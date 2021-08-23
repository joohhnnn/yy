package com.txznet.nav.manager;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.NaviInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.helper.OverlayHelper;
import com.txznet.nav.ui.AMapConfig;
import com.txznet.nav.ui.NaviCustomView;
import com.txznet.nav.ui.NaviListener;
import com.txznet.txz.util.runnables.Runnable1;

public class AMapProcessor {

	private static AMapProcessor sProcessor = new AMapProcessor();

	private AMap mAMap;

	public static AMapProcessor getInstance() {
		return sProcessor;
	}

	public void assignAMap(AMap aMap) {
		this.mAMap = aMap;
	}

	// ////////////////////////////////////////////////////////////////////////
	/**
	 * 间隔6000获取一次摄像头的倾斜角度
	 */
	public void procGetCameraPositionLoop() {
		mCpRun.run();
	}

	Runnable mCpRun = new Runnable() {

		@Override
		public void run() {
			resumeCameraPosition();
			AppLogic.removeUiGroundCallback(mCpRun);
			AppLogic.runOnUiGround(mCpRun, 6000);
		}
	};

	public void resumeCameraPosition() {
		ensureAMapNotNull();
		if (AMapConfig.mNaviViewSupport3D) {
			mAMap.animateCamera(CameraUpdateFactory.changeTilt(45));
		} else {
			mAMap.animateCamera(CameraUpdateFactory.changeTilt(0));
		}
	}

	private void ensureAMapNotNull() {
		if (mAMap == null) {
			throw new NullPointerException("AMap should not be null!");
		}
	}

	// //////////////////////////////////////////////////////////////////////
	private static final int CHECK_TIME = 30000;

	public void checkProcessResumeQueue(long delay) {
		AppLogic.removeBackGroundCallback(mCheckRunnable);
		AppLogic.runOnBackGround(mCheckRunnable, delay <= 0 ? CHECK_TIME : delay);
	}

	public void removeCheckResumeNaviRunnable() {
		AppLogic.removeBackGroundCallback(mCheckRunnable);
	}

	Runnable mCheckRunnable = new Runnable() {

		@Override
		public void run() {
			if (!NavManager.getInstance().isNavi()) {
				AppLogic.removeBackGroundCallback(mCheckRunnable);
				return;
			}

			if (NaviListener.getInstance().mCarLock) {
				NaviCustomView.getInstance().resumeNavi();
			}

			AppLogic.removeBackGroundCallback(mCheckRunnable);
			AppLogic.runOnBackGround(mCheckRunnable, CHECK_TIME);
		}
	};

	// ///////////////////////////////////////////////////////////////////////////
	public void procOnNaviInfoUpdate(NaviInfo naviInfo) {
		LogUtil.logd("onNaviInfoUpdate:" + naviInfo.m_NextRoadName + "," + naviInfo.m_RouteRemainDis + ","
				+ naviInfo.m_RouteRemainTime);
		NaviDataManager.getInstance().updateNaviInfo(naviInfo);
		NaviCustomView.getInstance().updateNaviInfo(naviInfo);
	}

	public void onNaviInfoUpdated(AMapNaviInfo naviInfo) {
		int distance = naviInfo.getPathRemainDistance();
		int time = naviInfo.getPathRemainTime();
		NaviDataManager.getInstance().updateRemainValue(distance, time);
	}

	// ///////////////////////////////////////////////////////////////////////////////
	/**
	 * 开始导航的回调处理
	 */
	public void procOnStartNavi(int mode) {
		if (!NavManager.getInstance().isNavi()) {
			NavManager.getInstance().setIsNav(true);
		}

		ensureAMapNotNull();
		// 开始检查导航中的倾斜角度
		procGetCameraPositionLoop();
		resumeCameraPosition();
		AppLogic.runOnBackGround(new Runnable1<AMap>(mAMap) {

			@Override
			public void run() {
				if (NavManager.getInstance().isMultiNav()) {
					OverlayManager.getInstance().init(mP1);
					OverlayHelper.getInstance().init(mP1);
					// MultiNavManager.getInstance().startNavigate(
					// NavManager.getInstance().getNavigateInfo());
				}
			}
		}, 0);
	}

	/**
	 * 处理导航结束
	 */
	public void procOnEndNavi() {
		NavManager.getInstance().setIsNav(false);
		AMapProcessor.getInstance().removeCheckResumeNaviRunnable();
	}

	// /////////////////////////////////////////////////////////////////////
	/**
	 * 处理路况放大图
	 */
	public void procShowCross(AMapNaviCross anc) {
		// if (AMapConfig.mCrossDisplayShow) {
		// NaviCustomView.getInstance().showCross(anc);
		// }
	}

	public void procHideCross() {
		// if (AMapConfig.mCrossDisplayShow) {
		// NaviCustomView.getInstance().hideCross();
		// }
	}

	// //////////////////////////////////////////////////////////////////////
	public void procShowLaneInfo() {

	}

	public void procHideLaneInfo() {

	}
}