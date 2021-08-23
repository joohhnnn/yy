package com.txznet.nav.ui;

import android.graphics.Color;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.UiSettings;
import com.amap.api.navi.AMapNaviView;
import com.amap.api.navi.AMapNaviViewOptions;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.util.DateUtils;

public class AMapConfig {
	// 配置信息
	public static final boolean mShowLeftSpace = false;

	// 实时交通图层
	public static final boolean mTrafficLayerEnabled = false;

	// 设置是否绘制显示交通路况的线路（彩虹线），拥堵-红色，畅通-绿色，缓慢-黄色，未知-蓝色
	public static final boolean mTrafficLine = true;

	// 设置是否自动改变缩放等级
	public static final boolean mAutoChangeZoom = true;

	// 设置是否自动画路
	public static final boolean mAutoDrawRoute = true;

	// 设置摄像头播报是否打开（只适用于驾车导航）
	public static final boolean mCameraInfoUpdateEnabled = true;

	// 设置指南针图标否在导航界面显示，默认显示
	public static final boolean mCompassEnabled = false;

	// 设置是否开启路口放大图功能
	public static final boolean mCrossDisplayEnabled = true;

	// 设置是否显示路口放大图
	public static final boolean mCrossDisplayShow = true;

	// 设置是否显示道路信息view
	public static final boolean mLaneInfoShow = true;

	// 设置锁定地图延迟毫秒数
	public static final int mLockMapDelayed = 8000;

	// 设置摄像头监控图标是否显示（只适用于驾车导航）
	public static final boolean mMonitorCameraEnabled = false;

	// 设置导航界面的颜色主题
	public static final int mNaviViewTopic = AMapNaviViewOptions.DEFAULT_COLOR_TOPIC;

	// 是否显示牵引线
	public static final boolean mLeaderLineEnabled = true;

	// 前方拥堵时是否重新计算路径（只适用于驾车导航，需要联网）
	public static final boolean mReCalculateRouteForTrafficJam = false;

	// 偏航时是否重新计算路径(计算路径需要联网）
	public static final boolean nReCalculateRouteForYaw = true;

	// 设置导航界面是否显示路线全览按钮
	public static final boolean mRouteListButtonShow = true;

	// 设置导航状态下屏幕是否一直开启
	public static final boolean mScreenAlwaysBright = true;

	// 设置菜单按钮是否在导航界面显示
	public static final boolean mSettingMenuEnable = false;

	// 设置路况光柱条是否显示（只适用于驾车导航，需要联网）
	public static final boolean mTrafficBarEnable = true;

	// 设置交通播报是否打开（只适用于驾车导航，需要联网）
	public static final boolean mTrafficInfoUpdateEnable = true;

	// 地图显示模式
	public static boolean mNaviViewSupport3D = true;

	private AMapNaviView mAMapNaviView;
	private static AMapConfig mConfig;

	private AMapConfig() {
	}

	public static AMapConfig getInstance() {
		if (mConfig == null) {
			synchronized (AMapConfig.class) {
				if (mConfig == null) {
					mConfig = new AMapConfig();
				}
			}
		}
		return mConfig;
	}

	public void initAMapNaviView(AMapNaviView naviView) {
		mAMapNaviView = naviView;
		initAMapNaviViewOptions();
	}

	private void initAMapNaviViewOptions() {
		AMapNaviViewOptions mAMapNaviViewOptions = mAMapNaviView
				.getViewOptions();

		if (mAMapNaviViewOptions != null) {
			mAMapNaviViewOptions.setTrafficLayerEnabled(mTrafficLayerEnabled);
			mAMapNaviViewOptions.setAutoChangeZoom(mAutoChangeZoom);
			mAMapNaviViewOptions.setAutoDrawRoute(mAutoDrawRoute);
			mAMapNaviViewOptions
					.setCameraInfoUpdateEnabled(mCameraInfoUpdateEnabled);
			mAMapNaviViewOptions.setCompassEnabled(mCompassEnabled);
			mAMapNaviViewOptions.setCrossDisplayEnabled(mCrossDisplayEnabled);
			mAMapNaviViewOptions.setCrossDisplayShow(mCrossDisplayShow);
			mAMapNaviViewOptions.setLaneInfoShow(mLaneInfoShow);
			mAMapNaviViewOptions.setLockMapDelayed(mLockMapDelayed);
			mAMapNaviViewOptions.setMonitorCameraEnabled(mMonitorCameraEnabled);
			mAMapNaviViewOptions.setNaviViewTopic(mNaviViewTopic);
			if (mLeaderLineEnabled) {
				mAMapNaviViewOptions.setLeaderLineEnabled(Color.RED);
			}
			mAMapNaviViewOptions
					.setReCalculateRouteForTrafficJam(mReCalculateRouteForTrafficJam);
			mAMapNaviViewOptions
					.setReCalculateRouteForYaw(nReCalculateRouteForYaw);
			mAMapNaviViewOptions.setRouteListButtonShow(mRouteListButtonShow);
			mAMapNaviViewOptions.setScreenAlwaysBright(mScreenAlwaysBright);
			mAMapNaviViewOptions.setSettingMenuEnabled(mSettingMenuEnable);
			mAMapNaviViewOptions.setTrafficBarEnabled(mTrafficBarEnable);
			mAMapNaviViewOptions
					.setTrafficInfoUpdateEnabled(mTrafficInfoUpdateEnable);
			mAMapNaviViewOptions.setTrafficLine(mTrafficLine);
			mAMapNaviViewOptions.setLayoutVisible(false);
			if (DateUtils.isNight()) {
				setNaviNight(true);
			} else {
				setNaviNight(false);
			}

			mAMapNaviView.setViewOptions(mAMapNaviViewOptions);
		}

		AMap aMap = mAMapNaviView.getMap();
		if (aMap != null) {
			aMap.setTrafficEnabled(true);
		}
	}

	// /////////////////////////////////////////////////////////////////////////////////
	public static void initAMapOption(AMap map) {
		UiSettings us = map.getUiSettings();
		us.setScrollGesturesEnabled(true);
		us.setTiltGesturesEnabled(true);
		if (mNaviViewSupport3D) {
			map.animateCamera(CameraUpdateFactory.changeTilt(45.0f));
		} else {
			map.animateCamera(CameraUpdateFactory.changeTilt(0.0f));
		}
	}

	public void setTrafficLineEnable(boolean enable) {
		AMapNaviViewOptions anvo = mAMapNaviView.getViewOptions();
		anvo.setTrafficLine(enable);
		anvo.setTrafficLayerEnabled(enable);
		mAMapNaviView.setViewOptions(anvo);
		AMap aMap = mAMapNaviView.getMap();
		if (aMap != null) {
			aMap.setTrafficEnabled(enable);
		}
	}

	public void setNaviNight(boolean isNight) {
		if (mAMapNaviView == null) {
			return;
		}

		if (isNaviNight() == isNight) {
			return;
		}

		LogUtil.logd("切换导航模式");
		AMapNaviViewOptions mAMapNaviViewOptions = mAMapNaviView
				.getViewOptions();
		mAMapNaviViewOptions.setNaviNight(isNight);
		mAMapNaviView.setViewOptions(mAMapNaviViewOptions);
		NaviCustomView.getInstance().updateNaviNight(isNight);
	}

	public boolean isNaviNight() {
		if (mAMapNaviView == null) {
			return false;
		}

		return mAMapNaviView.getViewOptions().isNaviNight();
	}

	public void onDestory() {
		mConfig = null;
	}
}
