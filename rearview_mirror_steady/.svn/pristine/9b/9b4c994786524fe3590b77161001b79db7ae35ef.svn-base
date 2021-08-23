package com.txznet.nav.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;
import android.widget.TextView;

import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.overlay.DrivingRouteOverlay;
import com.amap.api.navi.AMapNavi;
import com.amap.api.navi.AMapNaviListener;
import com.amap.api.navi.model.AMapLaneInfo;
import com.amap.api.navi.model.AMapNaviCross;
import com.amap.api.navi.model.AMapNaviInfo;
import com.amap.api.navi.model.AMapNaviLocation;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.amap.api.navi.model.NaviLatLng;
import com.amap.api.navi.view.RouteOverLay;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.route.BusRouteResult;
import com.amap.api.services.route.DrivePath;
import com.amap.api.services.route.DriveRouteResult;
import com.amap.api.services.route.RouteSearch;
import com.amap.api.services.route.RouteSearch.DriveRouteQuery;
import com.amap.api.services.route.RouteSearch.FromAndTo;
import com.amap.api.services.route.RouteSearch.OnRouteSearchListener;
import com.amap.api.services.route.WalkRouteResult;
import com.autonavi.tbt.TrafficFacilityInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.ui.dialog.WinWaiting;
import com.txznet.loader.AppLogic;
import com.txznet.nav.R;
import com.txznet.nav.helper.OverlayHelper;
import com.txznet.nav.manager.NaviDataManager;
import com.txznet.nav.manager.NavManager;
import com.txznet.nav.manager.OverlayManager;
import com.txznet.nav.util.DateUtils;
import com.txznet.sdk.TXZTtsManager;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

/**
 * 路径规划的界面
 */
public class RoutePlanActivity extends BaseActivity {
	public static final String WAKEUP_TASK_ID = "RoutePlanActivity";
	public static final String ACTION_RECALCUTE = "routeplan.re_calcute";
	private static final String SECOND = "s";
	private static final String SPACE = "  ";

	private boolean mStopAutoNav;
	private boolean mIsMapLoaded;
	private boolean mIsAccessNavi;
	private boolean mIsPlanning;

	private int mTimeToBeginNav;
	private int mCurrentCalPolicy;

	private MapView mMapView;
	private Button mBtnStartNav;

	private TextView mTxtNameTv;
	private TextView mTxtGeoInfoTv;
	private ImageButton mZoomoutIb;
	private ImageButton mZoominIb;

	private AMap mAMap;
	private AMapNavi mAMapNavi;
	private RouteOverLay mRouteOverLay;

	private NavigateInfo mNavigateInfo;
	private AutoNavRunnable mAutoNavRunnable;
	private Map<Integer, RouteOverLay> aproMap = new HashMap<Integer, RouteOverLay>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		AppLogic.addActivity(this);
		init();
		setContentView(R.layout.activity_cal);
		initView(savedInstanceState);
		setListener();
		initNavigateInfo();
	}

	private void init() {
		AMapNavi.getInstance(AppLogic.getApp()).setAMapNaviListener(
				mAMapNaviListener);

		mAutoNavRunnable = new AutoNavRunnable();
		mStopAutoNav = false;
		mTimeToBeginNav = 8;
		mIsPlanning = false;
	}

	private void setNightView() {
		mZoominIb.setImageResource(R.drawable.nav_view_zoom_out_n);
		mZoomoutIb.setImageResource(R.drawable.nav_view_zoom_in_n);
	}

	private void initView(Bundle savedInstanceState) {
		mBtnStartNav = (Button) findViewById(R.id.btnStartNav);
		mMapView = (MapView) findViewById(R.id.routemap);
		mTxtNameTv = (TextView) findViewById(R.id.txt_name_tv);
		mTxtGeoInfoTv = (TextView) findViewById(R.id.txt_geoinfo_tv);
		mMapView.onCreate(savedInstanceState);
		mZoomoutIb = (ImageButton) findViewById(R.id.zoom_out_ib);
		mZoominIb = (ImageButton) findViewById(R.id.zoom_in_ib);
		mAMap = mMapView.getMap();

		if (DateUtils.isNight() && mAMap.getMapType() != AMap.MAP_TYPE_NIGHT) {
			mAMap.setMapType(AMap.MAP_TYPE_NIGHT);
			setNightView();
		}

		mZoomoutIb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomout();
			}
		});

		mZoominIb.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				zoomin();
			}
		});

		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				mAMap.setOnMapLoadedListener(new OnMapLoadedListener() {

					@Override
					public void onMapLoaded() {
						mIsMapLoaded = true;
						if (mRouteOverLay != null) {
							mRouteOverLay.zoomToSpan();
						}
					}
				});

				mAMap.setTrafficEnabled(true);
				preStartPlanRoute();

				if (NavManager.getInstance().isMultiNav()) {
					OverlayManager.getInstance().init(mAMap);
					OverlayHelper.getInstance().init(mAMap);
				}
			}
		}, 0);

		UiSettings us = mAMap.getUiSettings();
		if (us == null) {
			return;
		}

		us.setMyLocationButtonEnabled(false);
		us.setZoomControlsEnabled(false);

		// mPathPlanManager = new PathPlanManager();
		// MyApplication.getApp().removeBackGroundCallback(mSyncSearchDriveRoute);
		// MyApplication.getApp().runOnBackGround(mSyncSearchDriveRoute, 0);
	}

	private void zoomout() {
		mAMap.animateCamera(CameraUpdateFactory.zoomOut());
	}

	private void zoomin() {
		mAMap.animateCamera(CameraUpdateFactory.zoomIn());
	}

	private void initNavigateInfo() {
		NavigateInfo info = NavManager.getInstance().getNavigateInfo();
		if (info != null) {
			String strName = info.strTargetName;
			String strAddr = info.strTargetAddress;
			if (TextUtils.isEmpty(strName)) {
				strName = "地图选点";
			}
			if (TextUtils.isEmpty(strAddr)) {
				mTxtGeoInfoTv.setVisibility(View.GONE);
			}

			mTxtNameTv.setText(strName);
			mTxtGeoInfoTv.setText(strAddr);
		}
	}

	private void initNavi() {
		mAMapNavi = AMapNavi.getInstance(AppLogic.getApp());
		AMapNaviPath naviPath = mAMapNavi.getNaviPath();
		if (naviPath == null) {
			return;
		}

		NaviDataManager.getInstance().updateNaviPath(naviPath);
		if (mRouteOverLay != null) {
			mRouteOverLay.removeFromMap();
			mRouteOverLay.destroy();
			mRouteOverLay = null;
		}

		mRouteOverLay = new RouteOverLay(mAMap, naviPath, AppLogic.getApp());

		// 获取路径规划线路，显示到地图上
		mRouteOverLay.addToMap();
		mRouteOverLay.setTrafficLine(true);
		if (mIsMapLoaded) {
			mRouteOverLay.zoomToSpan();
		}

		AppLogic.runOnUiGround(mAutoNavRunnable, 1000);
	}

	AsrComplexSelectCallback mAsrCommands = new AsrComplexSelectCallback() {
		@Override
		public String getTaskId() {
			return WAKEUP_TASK_ID;
		}

		@Override
		public boolean needAsrState() {
			return false;
		}

		public void onCommandSelected(String type, String command) {
			LogUtil.logd("trigger wakeup command: " + type + "-" + command);
			if (type.equals("ZOOM_OUT")) {
				mAMap.animateCamera(CameraUpdateFactory.zoomOut());
				return;
			}
			if (type.equals("ZOOM_IN")) {
				mAMap.animateCamera(CameraUpdateFactory.zoomIn());
				return;
			}
			if (type.equals("LESS_DISTANCE")) {
				if (AMapNavi.getInstance(AppLogic.getApp()).getNaviPath()
						.getStrategy() == AMapNavi.DrivingShortDistance) {
					TtsUtil.speakText("已经为您选择了少路程路线");
					return;
				}

				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						((RadioButton) findViewById(R.id.distance))
								.performClick();
					}
				}, 0);
				return;
			}
			if (type.equals("LESS_MONEY")) {
				if (AMapNavi.getInstance(AppLogic.getApp()).getNaviPath()
						.getStrategy() == AMapNavi.DrivingSaveMoney) {
					TtsUtil.speakText("已经为您选择了少收费路线");
					return;
				}

				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						((RadioButton) findViewById(R.id.toll)).performClick();
					}
				}, 0);
				return;
			}
			if (type.equals("AVOID")) {
				if (AMapNavi.getInstance(AppLogic.getApp()).getNaviPath()
						.getStrategy() == AMapNavi.DrivingFastestTime) {
					TtsUtil.speakText("已经为您选择了躲避拥堵路线");
					return;
				}

				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						((RadioButton) findViewById(R.id.recommend))
								.performClick();
					}
				}, 0);
				return;
			}
			if (type.equals("BEGIN")) {
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						mBtnStartNav.performClick();
					}
				}, 0);
				return;
			}
			if (type.equals("EXIT")) {
				RoutePlanActivity.this.onBackPressed();
				return;
			}
		};
	}.addCommand("ZOOM_OUT", "缩小").addCommand("ZOOM_IN", "放大")
			.addCommand("AVOID", "躲避拥堵", "最佳路线")
			.addCommand("LESS_MONEY", "少收费").addCommand("BEGIN", "开始")
			.addCommand("LESS_DISTANCE", "少路程")
			.addCommand("EXIT", "结束", "退出", "关闭", "取消", "返回");

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {
			stopAutoNav();
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);
	}

	@Override
	public void onLoseFocus() {
		AsrUtil.recoverWakeupFromAsr(WAKEUP_TASK_ID);
		super.onLoseFocus();
	}

	@Override
	public void onGetFocus() {
		AsrUtil.useWakeupAsAsr(mAsrCommands);
		super.onGetFocus();
	}

	@Override
	protected void onStart() {
		super.onStart();
		register();
		showStartButtonText();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (mMapView != null) {
			mMapView.onResume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (mMapView != null) {
			mMapView.onPause();
		}
		stopAutoNav();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unRegister();
		if (mMapView != null) {
			mMapView.onDestroy();
		}
		AMapNavi.getInstance(AppLogic.getApp()).removeAMapNaviListener(
				mAMapNaviListener);
	}

	@Override
	public void onBackPressed() {
		NavManager.getInstance().stopNavi(true);
		super.onBackPressed();
	}

	private void setListener() {
		mBtnStartNav.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mBtnStartNav.setEnabled(false);
				mBtnStartNav.setBackgroundColor(getResources().getColor(
						R.color.win_nav_preview_start_button_bg_press));
				AppLogic.runOnUiGround(new Runnable() {

					@Override
					public void run() {
						mBtnStartNav.setEnabled(true);
						mBtnStartNav
								.setBackgroundColor(getResources()
										.getColor(
												R.color.win_nav_preview_start_button_bg_normal));
					}
				}, 5000);

				if (mIsPlanning) {
					return;
				}

				if (!mIsAccessNavi) {
					calcuteRoute(mCurrentCalPolicy);
					return;
				}

				finish();
				// 保存历史
				saveHistory();

				Intent intent = new Intent(RoutePlanActivity.this,
						NavViewActivity.class);
				startActivity(intent);
			}
		});

		RadioGroup rg = (RadioGroup) findViewById(R.id.path_select);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.toll) {
					mCurrentCalPolicy = AMapNavi.DrivingSaveMoney;
					calcuteRoute(AMapNavi.DrivingSaveMoney);
					stopAutoNav();
				} else if (checkedId == R.id.distance) {
					mCurrentCalPolicy = AMapNavi.DrivingShortDistance;
					calcuteRoute(AMapNavi.DrivingShortDistance);
					stopAutoNav();
				} else if (checkedId == R.id.recommend) {
					mCurrentCalPolicy = AMapNavi.DrivingFastestTime;
					calcuteRoute(AMapNavi.DrivingFastestTime);
				}
			}
		});

		// 触发路径规划
		((RadioButton) findViewById(R.id.recommend)).setChecked(true);
	}

	public void stopAutoNav() {
		AppLogic.removeUiGroundCallback(mAutoNavRunnable);
		mStopAutoNav = true;
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				showStartButtonText();
			}
		}, 0);
	}

	/**
	 * 根据策略计算线路
	 * 
	 * @param strategy
	 */
	private void calcuteRoute(int strategy) {
		AppLogic.runOnBackGround(new Runnable1<Integer>(strategy) {

			@Override
			public void run() {
				preStartPlanRoute();
				mIsPlanning = true;
				mCurrentCalPolicy = mP1;
				double mLat = 0, mLng = 0;
				double eLat = 0, eLng = 0;

				LocationInfo myLocationInfo = NavManager.getInstance()
						.getLocationInfo();
				if (myLocationInfo == null) {
					LogUtil.loge("myLocationInfo is null！");
					return;
				}

				if (myLocationInfo.msgGpsInfo == null) {
					LogUtil.loge("myLocationInfo is null！");
					return;
				}

				mLat = myLocationInfo.msgGpsInfo.dblLat;
				mLng = myLocationInfo.msgGpsInfo.dblLng;

				mNavigateInfo = NavManager.getInstance().getNavigateInfo();

				if (mNavigateInfo == null) {
					LogUtil.loge("mNavigateInfo 为空！");
					return;
				}

				if (mNavigateInfo.msgGpsInfo == null) {
					LogUtil.loge("mNavigateInfo.msgGpsInfo 为空！");
					return;
				}

				eLat = mNavigateInfo.msgGpsInfo.dblLat;
				eLng = mNavigateInfo.msgGpsInfo.dblLng;

				LogUtil.logd("终点算路 -- 》 eLat:" + eLat + ",eLng:" + eLng);

				NaviLatLng mNaviStart = new NaviLatLng(mLat, mLng);
				NaviLatLng mNaviEnd = new NaviLatLng(eLat, eLng);

				List<NaviLatLng> toList = new ArrayList<NaviLatLng>();
				List<NaviLatLng> startList = new ArrayList<NaviLatLng>();

				startList.add(mNaviStart);
				toList.add(mNaviEnd);

				AMapNavi.getInstance(AppLogic.getApp()).calculateDriveRoute(
						startList, toList, null, mP1);
			}
		}, 0);
	}

	private OnAMapNaviListener mAMapNaviListener = new OnAMapNaviListener();

	private class OnAMapNaviListener implements AMapNaviListener {
		int retry = 0;

		@Override
		public void onArriveDestination() {
		}

		@Override
		public void onArrivedWayPoint(int arg0) {
		}

		private String getErrorResult(int errorCode) {
			switch (errorCode) {
			case 2:
				return "网络操作失败";
			case 3:
				return "起点错误";
			case 4:
				return "协议解析错误";
			case 6:
				return "终点错误";
			case 10:
				return "起点没有找到道路";
			case 11:
				return "终点没有找到道路";
			case 12:
				return "途经点没有找到道路";
			case 13:
				return "用户Key非法或过期";
			case 14:
				return "请求服务不存在";
			case 15:
				return "请求服务响应错误";
			case 16:
				return "无权限访问此服务";
			case 17:
				return "请求超出配额";
			case 18:
				return "请求参数非法";
			case 19:
				return "未知错误";
			default:
				return "未知错误" + errorCode;
			}
		}

		@Override
		public void onCalculateRouteFailure(int arg0) {
			AppLogic.removeUiGroundCallback(mRoutePlanTimeout);
			dismissRoutePlanDialog(0);
			String calculateResult = "路径规划失败";
			mIsAccessNavi = false;
			mIsPlanning = false;
			if (retry < 1) {
				switch (arg0) {
				case 2:
					calculateResult = "路径规划失败，请检查网络连接是否正常";
					break;

				default:
					calculateResult = "路径规划失败，即将为您重新规划路径";
					break;
				}

				retry++;
				AppLogic.runOnUiGround(new Runnable2<String, Integer>(
						calculateResult, arg0) {

					@Override
					public void run() {
						TtsUtil.speakText(mP1, new ITtsCallback() {
							@Override
							public void onSuccess() {
								super.onSuccess();
								if (mP2 == 2) {
									return;
								}

								calcuteRoute(mCurrentCalPolicy);
							}
						});
					}
				}, 0);
				return;
			}

			if (retry == 1) {
				switch (arg0) {
				case 2:
					calculateResult = "路径规划失败，请检查网络连接是否正常";
					break;

				default:
					calculateResult = "路径规划失败，请行驶到开阔地带后再试";
					break;
				}
			}

			LogUtil.loge("算路失败 -- 》" + calculateResult + "，错误码："
					+ getErrorResult(arg0));
			WinNotice.showNotice(calculateResult, true, true, null);
		}

		@Override
		public void onCalculateRouteSuccess() {
			AppLogic.removeUiGroundCallback(mRoutePlanTimeout);
			dismissRoutePlanDialog(0);
			retry = 0;
			String calculateResult = "路径规划成功";
			TtsUtil.speakText(calculateResult);
			mIsPlanning = false;
			mIsAccessNavi = true;

			NavManager.getInstance().setStrategy(mCurrentCalPolicy);
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					initNavi();
				}
			}, 0);
		}

		@Override
		public void onEndEmulatorNavi() {
		}

		@Override
		public void onGetNavigationText(int arg0, String arg1) {
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
		}

		@Override
		public void onNaviInfoUpdated(AMapNaviInfo arg0) {
		}

		@Override
		public void onReCalculateRouteForTrafficJam() {
		}

		@Override
		public void onReCalculateRouteForYaw() {
		}

		@Override
		public void onStartNavi(int arg0) {
		}

		@Override
		public void onTrafficStatusUpdate() {
		}

		@Override
		public void OnUpdateTrafficFacility(TrafficFacilityInfo arg0) {
		}

		@Override
		public void hideCross() {
		}

		@Override
		public void hideLaneInfo() {
		}

		@Override
		public void showCross(AMapNaviCross arg0) {
		}

		@Override
		public void showLaneInfo(AMapLaneInfo[] arg0, byte[] arg1, byte[] arg2) {
		}
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
				AppLogic.runOnUiGround(mAutoNavRunnable, 1000);
				showStartButtonText();
			}
		}
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
			NavManager.getInstance().setHistory(mNavigateInfo);
		} catch (Exception e) {
			LogUtil.loge("save history error!");
		}
	}

	private WinWaiting mWinWaiting;

	private void preStartPlanRoute() {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				try {
					if (mWinWaiting != null && mWinWaiting.isShowing()) {
						return;
					}

					if (mWinWaiting == null) {
						mWinWaiting = new WinWaiting("正在规划路径");
					}

					mWinWaiting.show();
					AppLogic.removeUiGroundCallback(mRoutePlanTimeout);
					AppLogic.runOnUiGround(mRoutePlanTimeout, TIME_OUT);
				} catch (Exception e) {
					LogUtil.loge("showDialog Exception！");
				}
			}
		}, 0);
	}

	private final int TIME_OUT = 30000;

	Runnable mRoutePlanTimeout = new Runnable() {

		@Override
		public void run() {
			dismissRoutePlanDialog(0);
			TXZTtsManager.getInstance().speakText("路径规划发生超时");
		}
	};

	private void dismissRoutePlanDialog(long delay) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mWinWaiting != null && mWinWaiting.isShowing()) {
					mWinWaiting.dismiss();
				}
			}
		}, delay);
	}

	private ReCalReceiver mReCalReceiver;

	private void register() {
		if (mReCalReceiver == null) {
			mReCalReceiver = new ReCalReceiver();
		}

		IntentFilter filter = new IntentFilter();
		filter.addAction(ACTION_RECALCUTE);
		registerReceiver(mReCalReceiver, filter);
	}

	private void unRegister() {
		unregisterReceiver(mReCalReceiver);
	}

	private class ReCalReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			calcuteRoute(mCurrentCalPolicy);
			mStopAutoNav = false;
		}
	}

	Runnable mSyncSearchDriveRoute = new Runnable() {

		@Override
		public void run() {
			if (mPathPlanManager == null) {
				mPathPlanManager = new PathPlanManager();
			}
			startSearch(RouteSearch.DrivingSaveMoney);
			startSearch(RouteSearch.DrivingShortDistance);
			startSearch(RouteSearch.DrivingAvoidCongestion);
		}
	};

	List<Runnable> mSearchQueue = new ArrayList<Runnable>();
	boolean mIsBusy;

	private void procQueue() {
		mIsBusy = false;
		synchronized (mSearchQueue) {
			if (!mSearchQueue.isEmpty()) {
				Runnable r = mSearchQueue.remove(0);
				AppLogic.removeBackGroundCallback(mProcQueueRunnable);
				AppLogic.removeBackGroundCallback(r);
				AppLogic.runOnBackGround(r, 0);
			}
		}
	}

	Runnable mProcQueueRunnable = new Runnable() {

		@Override
		public void run() {
			procQueue();
		}
	};

	private void startSearch(int driveMode) {
		Runnable1 r = new Runnable1<Integer>(driveMode) {

			@Override
			public void run() {
				mIsBusy = true;
				double mLat = 0, mLng = 0;
				double eLat = 0, eLng = 0;

				LocationInfo myLocationInfo = NavManager.getInstance()
						.getLocationInfo();
				if (myLocationInfo == null) {
					LogUtil.loge("myLocationInfo is null！");
					return;
				}

				if (myLocationInfo.msgGpsInfo == null) {
					LogUtil.loge("myLocationInfo is null！");
					return;
				}

				mLat = myLocationInfo.msgGpsInfo.dblLat;
				mLng = myLocationInfo.msgGpsInfo.dblLng;

				mNavigateInfo = NavManager.getInstance().getNavigateInfo();

				if (mNavigateInfo == null) {
					LogUtil.loge("mNavigateInfo 为空！");
					return;
				}

				if (mNavigateInfo.msgGpsInfo == null) {
					LogUtil.loge("mNavigateInfo.msgGpsInfo 为空！");
					return;
				}

				eLat = mNavigateInfo.msgGpsInfo.dblLat;
				eLng = mNavigateInfo.msgGpsInfo.dblLng;

				LatLonPoint startPoint = new LatLonPoint(mLat, mLng);
				LatLonPoint endPoint = new LatLonPoint(eLat, eLng);
				mPathPlanManager.startSearchRoute(startPoint, endPoint, mP1,
						false);
			}
		};

		if (mIsBusy) {
			synchronized (mSearchQueue) {
				mSearchQueue.add(r);
			}
		} else {
			AppLogic.runOnBackGround(r, 0);
			AppLogic.runOnBackGround(mProcQueueRunnable, 5000);
		}
	}

	private PathPlanManager mPathPlanManager = null;
	private List<DrivingRouteOverlay> mDrivingRouteOverlays = new ArrayList<DrivingRouteOverlay>();

	private class PathPlanManager {
		private RouteSearch mRouteSearch;
		private DriveRouteQuery mDriveRouteQuery;

		public void startSearchRoute(LatLonPoint startPoint,
				LatLonPoint endPoint, int driveMode, final boolean isClear) {
			if (mRouteSearch == null) {
				mRouteSearch = new RouteSearch(AppLogic.getApp());
				mRouteSearch
						.setRouteSearchListener(new OnRouteSearchListener() {

							@Override
							public void onWalkRouteSearched(
									WalkRouteResult arg0, int arg1) {
							}

							@Override
							public void onDriveRouteSearched(
									DriveRouteResult arg0, int arg1) {
								if (isClear) {
									mDrivingRouteOverlays.clear();
								}

								List<DrivePath> mDrivePaths = arg0.getPaths();
								if (mDrivePaths == null) {
									return;
								}

								for (DrivePath path : mDrivePaths) {
									DrivingRouteOverlay mOverlay = new DrivingRouteOverlay(
											AppLogic.getApp(), mAMap, path,
											arg0.getStartPos(), arg0
													.getTargetPos());
									mOverlay.removeFromMap();
									mOverlay.addToMap();
									mOverlay.zoomToSpan();
									mDrivingRouteOverlays.add(mOverlay);
								}

								procQueue();
							}

							@Override
							public void onBusRouteSearched(BusRouteResult arg0,
									int arg1) {
							}
						});
			}

			if (mDriveRouteQuery != null) {
				mDriveRouteQuery = null;
			}

			FromAndTo mFromAndTo = new FromAndTo(startPoint, endPoint);
			mDriveRouteQuery = new DriveRouteQuery(mFromAndTo, driveMode, null,
					null, "");

			mRouteSearch.calculateDriveRouteAsyn(mDriveRouteQuery);
		}
	}
}