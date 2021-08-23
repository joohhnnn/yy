package com.txznet.nav.ui;

import java.util.ArrayList;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.navisdk.BaiduNaviManager;
import com.baidu.navisdk.CommonParams.Const.ModelName;
import com.baidu.navisdk.CommonParams.NL_Net_Mode;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.mapcontrol.MapParams.Const.LayerMode;
import com.baidu.navisdk.comapi.routeguide.RouteGuideParams.RGLocationMode;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.comapi.routeplan.IRouteResultObserver;
import com.baidu.navisdk.comapi.routeplan.RoutePlanParams.NE_RoutePlan_Mode;
import com.baidu.navisdk.comapi.tts.BNavigatorTTSPlayer;
import com.baidu.navisdk.model.NaviDataEngine;
import com.baidu.navisdk.model.RoutePlanModel;
import com.baidu.navisdk.model.datastruct.RoutePlanNode;
import com.baidu.navisdk.ui.routeguide.BNavConfig;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.ui.widget.RoutePlanObserver;
import com.baidu.navisdk.util.common.ScreenUtil;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.nav.MyApplication;
import com.txznet.nav.NavManager;
import com.txznet.nav.R;
import com.txznet.nav.util.BDLocationUtil;

public class PreviewActivity extends BaseActivity {

	private Button mBtnStartNav;
	private static final String SECOND = "s";
	private static final String SPACE = "  ";
	private int mTimeToBeginNav;
	private boolean mStopAutoNav;

	private MapGLSurfaceView mMapView;
	private RoutePlanModel mRoutePlanModel;
	private AutoNavRunnable mAutoNavRunnable;
	private NavTtsPlayer mNavTtsPlayer;
	private IRouteResultObserver mRouteResultObserver;
	private NavigateInfo mNavigateInfo;
	private boolean mStartNavi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		processIntent();

		System.loadLibrary("app_BaiduNaviApplib_v1_0_0");
		System.loadLibrary("BaiduMapSDK_v3_2_0_15");
		System.loadLibrary("locnaviSDK");
		System.loadLibrary("locSDK5");
		System.loadLibrary("msc");

		init();
		setContentView(R.layout.activity_preview);
		initMapView();
		initView();
		initOnClick();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		processIntent();
	}

	@Override
	protected void onStart() {
		super.onStart();
		showStartButtonText();
	}

	@Override
	protected void onResume() {
		super.onResume();
		BNMapController.getInstance().onResume();
	}

	@Override
	protected void onPause() {
		BNMapController.getInstance().onPause();
		stopAutoNav();
		mNavTtsPlayer.clear();
		super.onPause();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (!mStartNavi) {
			if (mMapView != null) {
				ViewGroup viewGroup = (ViewGroup) (findViewById(R.id.mapview_layout));
				viewGroup.removeView(mMapView);
				mMapView = null;
			}
			BaiduNaviManager.getInstance().destroyNMapView();
		}
		super.onDestroy();
	}

	private void processIntent() {
		Intent intent = getIntent();
		mNavigateInfo = new NavigateInfo();
		mNavigateInfo.strTargetName = intent.getStringExtra("name");
		mNavigateInfo.strTargetAddress = intent.getStringExtra("address");
		mNavigateInfo.msgGpsInfo = new GpsInfo();
		mNavigateInfo.msgGpsInfo.dblLat = intent.getDoubleExtra("lat", 0);
		mNavigateInfo.msgGpsInfo.dblLng = intent.getDoubleExtra("lng", 0);
		mNavigateInfo.msgGpsInfo.uint32GpsType = intent.getIntExtra("type", 0);
	}

	private void init() {
		mNavTtsPlayer = new NavTtsPlayer();
		mAutoNavRunnable = new AutoNavRunnable();
		mStopAutoNav = false;
		mTimeToBeginNav = 8;
		BNavigatorTTSPlayer.setTTSPlayerListener(mNavTtsPlayer);
		mRouteResultObserver = new RouteResultObserver();
	}

	private void initMapView() {
		if (Build.VERSION.SDK_INT < 14) {
			BaiduNaviManager.getInstance().destroyNMapView();
		}

		try {
			mMapView = BaiduNaviManager.getInstance().createNMapView(this);
		} catch (Exception e) {
			LogUtil.loge("createNMapView error!");
			return;
		}

		BNMapController.getInstance().setLevel(14);
		BNMapController.getInstance().setLayerMode(
				LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);

		LocationInfo mylocation = NavManager.getInstance().getLocationInfo();
		try {
			BNMapController.getInstance().locate(
					(int) (mylocation.msgGpsInfo.dblLat * 1e6),
					(int) (mylocation.msgGpsInfo.dblLng * 1e6));
		} catch (Exception e) {
			LogUtil.loge("set mylocation fail!");
		}

		updateCompassPosition();
	}

	private void initView() {
		mBtnStartNav = (Button) findViewById(R.id.btnStartNav);
		ViewGroup viewGroup = (ViewGroup) (findViewById(R.id.mapview_layout));

		viewGroup.removeAllViews();
		ViewParent parent = mMapView.getParent();
		if (parent != null) {
			((ViewGroup) parent).removeView(mMapView);
		}

		viewGroup.addView(mMapView);
	}

	private void initOnClick() {
		mBtnStartNav.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				startNavi();
			}
		});

		RadioGroup rg = (RadioGroup) findViewById(R.id.path_select);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (mNavTtsPlayer == null)
					return;
				mNavTtsPlayer.clear();
				if (checkedId == R.id.toll) {
					startCalcRoute(NL_Net_Mode.NL_Net_Mode_OnLine,
							NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_TOLL);
					stopAutoNav();
				} else if (checkedId == R.id.distance) {
					startCalcRoute(NL_Net_Mode.NL_Net_Mode_OnLine,
							NE_RoutePlan_Mode.ROUTE_PLAN_MOD_MIN_DIST);
					stopAutoNav();
				} else if (checkedId == R.id.recommend) {
					startCalcRoute(NL_Net_Mode.NL_Net_Mode_OnLine,
							NE_RoutePlan_Mode.ROUTE_PLAN_MOD_RECOMMEND);
				}
			}
		});

		// 触发路径规划
		((RadioButton) findViewById(R.id.recommend)).setChecked(true);
	}

	private class AutoNavRunnable implements Runnable {

		@Override
		public void run() {
			if (mStopAutoNav)
				return;
			if (mTimeToBeginNav <= 0) {
				mBtnStartNav.performClick();
			} else {
				mTimeToBeginNav--;
				MyApplication.getApp().runOnUiGround(mAutoNavRunnable, 1000);
				showStartButtonText();
			}
		}

	}

	/**
	 * 更新指南针位置
	 */
	private void updateCompassPosition() {
		int screenW = getResources().getDisplayMetrics().widthPixels;
		BNMapController.getInstance().resetCompassPosition(
				screenW - ScreenUtil.dip2px(this, 30),
				ScreenUtil.dip2px(this, 126), -1);
	}

	private class RouteResultObserver implements IRouteResultObserver {

		@Override
		public void onRoutePlanCanceled() {
		}

		@Override
		public void onRoutePlanFail() {
		}

		@Override
		public void onRoutePlanStart() {
		}

		@Override
		public void onRoutePlanSuccess() {
			BNMapController.getInstance().setLayerMode(
					LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
			mRoutePlanModel = (RoutePlanModel) NaviDataEngine.getInstance()
					.getModel(ModelName.ROUTE_PLAN);
			MyApplication.getApp().runOnUiGround(mAutoNavRunnable, 1000);
		}

		@Override
		public void onRoutePlanYawingFail() {
		}

		@Override
		public void onRoutePlanYawingSuccess() {
		}

	}

	private void startNavi() {
		if (mRoutePlanModel == null) {
			MyApplication.showToast("请先算路！");
			return;
		}
		// 获取路线规划结果起点
		RoutePlanNode startNode = mRoutePlanModel.getStartNode();
		// 获取路线规划结果终点
		RoutePlanNode endNode = mRoutePlanModel.getEndNode();
		if (null == startNode || null == endNode) {
			return;
		}

		mStartNavi = true;
		finish();

		// 停掉语音播报
		mNavTtsPlayer.clear();

		saveHistory();

		// 获取路线规划算路模式
		int calcMode = BNRoutePlaner.getInstance().getCalcMode();
		Bundle bundle = new Bundle();
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_VIEW_MODE,
				BNavigator.CONFIG_VIEW_MODE_INFLATE_MAP);
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_CALCROUTE_DONE,
				BNavigator.CONFIG_CLACROUTE_DONE);
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_START_X,
				startNode.getLongitudeE6());
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_START_Y,
				startNode.getLatitudeE6());
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_END_X, endNode.getLongitudeE6());
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_END_Y, endNode.getLatitudeE6());
		bundle.putString(BNavConfig.KEY_ROUTEGUIDE_START_NAME,
				mRoutePlanModel.getStartName(this, false));
		bundle.putString(BNavConfig.KEY_ROUTEGUIDE_END_NAME,
				mRoutePlanModel.getEndName(this, false));
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_CALCROUTE_MODE, calcMode);
		// GPS 导航
		bundle.putInt(BNavConfig.KEY_ROUTEGUIDE_LOCATE_MODE,
				RGLocationMode.NE_Locate_Mode_GPS);

		// 切换导航要暂停否侧异常
		((ViewGroup) (findViewById(R.id.mapview_layout))).removeView(mMapView);
		BaiduNaviManager.getInstance().destroyNMapView();

		Intent intent = new Intent(this, NavStartActivity.class);
		intent.putExtra("navi", bundle);
		startActivity(intent);
	}

	private void startCalcRoute(int netmode, int routeMode) {
		int sLat = 0, sLng = 0, eLat = 0, eLng = 0;

		if (mNavigateInfo == null) {
			MyApplication.showToast("mNavigateInfo is null");
			LogUtil.loge("mNavigateInfo is null");
			return;
		}

		LocationInfo mylocation = NavManager.getInstance().getLocationInfo();
		if (mylocation == null) {
			MyApplication.showToast("mylocation is null");
			LogUtil.loge("mylocation is null");
			return;
		}

		if (mNavigateInfo.msgGpsInfo == null) {
			// MyApplication.showToast("mNavigateInfo.msgGpsInfo is null");
			LogUtil.loge("mNavigateInfo.msgGpsInfo is null");
			return;
		}

		if (mylocation.msgGpsInfo == null) {
			// MyApplication.showToast("mNavigateInfo.msgGeoInfo is null");
			LogUtil.loge("mylocation.msgGpsInfo is null");
			return;
		}

		double latitude = mNavigateInfo.msgGpsInfo.dblLat;
		double longitude = mNavigateInfo.msgGpsInfo.dblLng;

		switch (mNavigateInfo.msgGpsInfo.uint32GpsType) {
		case UiMap.GPS_TYPE_BD09: {
			double[] xy = BDLocationUtil.Convert_BD09_To_GCJ02(latitude,
					longitude);
			latitude = xy[0];
			longitude = xy[1];
			break;
		}
		}

		double zoom = 1e5;
		sLat = (int) (mylocation.msgGpsInfo.dblLat * zoom);
		sLng = (int) (mylocation.msgGpsInfo.dblLng * zoom);
		eLat = (int) (latitude * zoom);
		eLng = (int) (longitude * zoom);

		String sAddr = "起始位置";
		if (mylocation.msgGeoInfo == null) {
			LogUtil.logw("mylocation.msgGeoInfo is null");
		} else {
			sAddr = mylocation.msgGeoInfo.strAddr;
		}

		// 起点
		RoutePlanNode startNode = new RoutePlanNode(sLat, sLng,
				RoutePlanNode.FROM_MAP_POINT, sAddr, sAddr);
		// 终点
		RoutePlanNode endNode = new RoutePlanNode(eLat, eLng,
				RoutePlanNode.FROM_MAP_POINT, mNavigateInfo.strTargetName,
				mNavigateInfo.strTargetAddress);

		// 将起终点添加到nodeList
		ArrayList<RoutePlanNode> nodeList = new ArrayList<RoutePlanNode>(2);
		nodeList.add(startNode);
		nodeList.add(endNode);

		RoutePlanObserver routePlanObserver = new RoutePlanObserver(this,
				new RoutePlanObserver.IJumpToDownloadListener() {
					@Override
					public void onJumpToDownloadOfflineData() {
						// 跳转至离线下载页面
					}
				});

		BNRoutePlaner.getInstance().setObserver(routePlanObserver);
		// 设置算路方式
		BNRoutePlaner.getInstance().setCalcMode(routeMode);
		// 设置算路结果回调
		BNRoutePlaner.getInstance()
				.setRouteResultObserver(mRouteResultObserver);
		BNRoutePlaner.getInstance().SetRouteSpec(true);
		// 设置起终点并算路
		boolean ret = BNRoutePlaner.getInstance().setPointsToCalcRoute(
				nodeList, NL_Net_Mode.NL_Net_Mode_OnLine);
		BNRoutePlaner.getInstance().zoomToRouteBound();

		if (!ret) {
			MyApplication.showToast("没有网络，将切换到离线导航!");
			BNRoutePlaner.getInstance().setPointsToCalcRoute(nodeList,
					NL_Net_Mode.NL_Net_Mode_OffLine);
			BNRoutePlaner.getInstance().zoomToRouteBound();
		}
	}

	public void stopAutoNav() {
		MyApplication.getApp().removeUiGroundCallback(mAutoNavRunnable);
		mStopAutoNav = true;
		showStartButtonText();
	}

	private void showStartButtonText() {
		if (mBtnStartNav == null)
			return;

		if (mStopAutoNav) {
			mBtnStartNav
					.setText(getString(R.string.activity_preview_start_nav_text));
		} else {
			mBtnStartNav
					.setText(getString(R.string.activity_preview_start_nav_text)
							+ SPACE + mTimeToBeginNav + SECOND);
		}
	}

	// 保存历史记录
	private void saveHistory() {
		if (mNavigateInfo == null)
			return;
		try {
			String name = mNavigateInfo.strTargetName;
			String address = mNavigateInfo.strTargetAddress;
			double lat = mNavigateInfo.msgGpsInfo.dblLat;
			double lng = mNavigateInfo.msgGpsInfo.dblLng;
			int type = mNavigateInfo.msgGpsInfo.uint32GpsType;

			NavManager.getInstance().setHistory(name, address, lat, lng, type);
		} catch (Exception e) {
			LogUtil.loge("save history error!");
		}
	}
}
