package com.txznet.txzcar.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.baidu.navisdk.adapter.BNRoutePlanNode;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanListener;
import com.baidu.navisdk.adapter.BaiduNaviManager.RoutePlanPreference;
import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.mapcontrol.BNMapViewFactory;
import com.baidu.navisdk.comapi.mapcontrol.MapParams.Const.LayerMode;
import com.baidu.navisdk.comapi.routeplan.BNRoutePlaner;
import com.baidu.navisdk.util.common.ScreenUtil;
import com.baidu.nplatform.comapi.basestruct.GeoPoint;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.base.StackActivity;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txzcar.MyApplication;
import com.txznet.txzcar.NavManager;
import com.txznet.txzcar.R;

/**
 * 算路Activity
 */
public class RoutePlanActivity extends StackActivity{
	static final String TAG = "RoutePlanActivity";
	private static final String SECOND = "s";
	private static final String SPACE = "  ";
	
	private int mTimeToBeginNav;
	private boolean mStopAutoNav;
	private boolean mStartNavi = false;
	private boolean mIsAccessNav = false;

	private Button mBtnStartNav;
	private MapGLSurfaceView mMapView;
	private NavigateInfo mNavigateInfo;
	private AutoNavRunnable mAutoNavRunnable;
	
	public static void navigateTo(Context context){
		Intent intent = new Intent(context,RoutePlanActivity.class);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		processIntent();
		
		init();
		setContentView(R.layout.activity_preview);
		initMapView();
		initOnClick();
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
		super.onPause();
		BNMapController.getInstance().onPause();
		stopAutoNav();
	}
	
	@Override
	protected void onDestroy() {
		if (!mStartNavi) {
			if (mMapView != null) {
				ViewGroup viewGroup = (ViewGroup) (findViewById(R.id.mapview_layout));
				viewGroup.removeView(mMapView);
				mMapView = null;
			}
			
			destoryMapView();
		}
		
		super.onDestroy();
	}
	
	private void processIntent() {
		Intent intent = getIntent();
		mNavigateInfo = new NavigateInfo();
		mNavigateInfo.strTargetName = intent.getStringExtra("name");
		mNavigateInfo.strTargetAddress = intent.getStringExtra("address");
		mNavigateInfo.msgGpsInfo = new GpsInfo(); 
		mNavigateInfo.msgGpsInfo.dblLat = intent.getDoubleExtra("lat", 40.05087);
		mNavigateInfo.msgGpsInfo.dblLng = intent.getDoubleExtra("lng", 116.30142);
		mNavigateInfo.msgGpsInfo.uint32GpsType = intent.getIntExtra("type", 2);
	}
	
	private void init(){
		mAutoNavRunnable = new AutoNavRunnable();
		mStopAutoNav = false;
		mTimeToBeginNav = 8;
	}
	
	private void initMapView(){
		createMapView();
		initView();
		
		BNMapController.getInstance().setLevel(14);
		BNMapController.getInstance().setLayerMode(LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
		LocationInfo myLocationInfo = NavManager.getInstance().getLocationInfo();
		try {
			GeoPoint point = new GeoPoint((int) (myLocationInfo.msgGpsInfo.dblLng * 1e5), (int) (myLocationInfo.msgGpsInfo.dblLat * 1e5));
			BNMapController.getInstance().initMapStatus(point);
		} catch (Exception e) {
			LogUtil.loge("set mylocation fail!");
		}
		
		updateCompassPosition();
	}
	
	/**
	 * 更新指南针位置
	 */
	private void updateCompassPosition() {
		int screenW = getResources().getDisplayMetrics().widthPixels;
		BNMapController.getInstance().resetCompassPosition(screenW - ScreenUtil.getInstance().dip2px(30),ScreenUtil.getInstance().dip2px(126), -1);
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
				if(!mIsAccessNav){
					return;
				}
				
				mStartNavi = true;
				destoryMapView();
				finish();
				
				RouteGuideActivity.navigateTo(RoutePlanActivity.this, null);
			}
		});

		RadioGroup rg = (RadioGroup) findViewById(R.id.path_select);
		rg.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == R.id.toll) {
					startCalNav(RoutePlanPreference.ROUTE_PLAN_MOD_MIN_TOLL);
					stopAutoNav();
				} else if (checkedId == R.id.distance) {
					startCalNav(RoutePlanPreference.ROUTE_PLAN_MOD_MIN_DIST);
					stopAutoNav();
				} else if (checkedId == R.id.recommend) {
					startCalNav(RoutePlanPreference.ROUTE_PLAN_MOD_RECOMMEND);
				}
			}
		});

		// 触发路径规划
		((RadioButton) findViewById(R.id.recommend)).setChecked(true);
	}
	
	public void stopAutoNav() {
		MyApplication.getApp().removeUiGroundCallback(mAutoNavRunnable);
		mStopAutoNav = true;
		showStartButtonText();
	}
	
	private void createMapView(){
		DisplayMetrics display = this.getResources().getDisplayMetrics();
		Bundle configBundle = new Bundle();
		configBundle.putInt("screen_width", display.widthPixels);
		configBundle.putInt("screen_height", display.heightPixels);
		mMapView = BNMapController.getInstance().initMapView(this, configBundle);
		BNMapViewFactory.getInstance().attachMapView(mMapView);
	}
	
	private void destoryMapView(){
		BNMapController.destory();
        BNMapViewFactory.getInstance().dettachMapView();
        mMapView = null;
	}

	private void startCalNav(int preference){
		double sLat = 0,sLng = 0,eLat = 0,eLng = 0;
		CoordinateType sType = CoordinateType.GCJ02,eType = CoordinateType.GCJ02;
		String sName = "",eName = "",sDescription = "",eDescription = "";
		
		mNavigateInfo = NavManager.getInstance().getNavigateInfo();
		if(mNavigateInfo == null){
			Log.e(TAG, "RoutePlanActivity -- > NavigateInfo为空！");
			return;
		}
		
		if(mNavigateInfo.msgGpsInfo == null){
			Log.e(TAG, "RoutePlanActivity -- > NavigateInfo.GpsInfo 为空！");
			return;
		}
		
		GpsInfo gpsInfo = mNavigateInfo.msgGpsInfo;
		eLat = gpsInfo.dblLat;
		eLng = gpsInfo.dblLng;
		eName = mNavigateInfo.strTargetName;
		eDescription = mNavigateInfo.strTargetAddress;
		switch (gpsInfo.uint32GpsType) {
		case 1:
			eType = CoordinateType.WGS84;
			break;

		case 2:
			eType = CoordinateType.GCJ02;
			break;
			
		case 3:
			eType = CoordinateType.BD09_MC;
			break;
		}
		
		LocationInfo info = NavManager.getInstance().getLocationInfo();
		if(info == null){
			Log.e(TAG, "RoutePlanActivity -- > LocationInfo 为空！");
			return;
		}
		
		if(info.msgGpsInfo == null){
			Log.e(TAG, "RoutePlanActivity -- > LocationInfo.GpsInfo 为空！");
			return;
		}
		
		switch (info.msgGpsInfo.uint32GpsType) {
		case 1:
			sType = CoordinateType.WGS84;
			break;

		case 2:
			sType = CoordinateType.GCJ02;
			break;
			
		case 3:
			sType = CoordinateType.BD09_MC;
			break;
		}
		
		sLat = info.msgGpsInfo.dblLat;
		sLng = info.msgGpsInfo.dblLng;
		sName = info.msgGeoInfo.strAddr;
		sDescription = info.msgGeoInfo.strAddr;
		
		BNRoutePlanNode startNode = new BNRoutePlanNode(sLng, sLat, sName, sDescription, sType);
		BNRoutePlanNode endNode = new BNRoutePlanNode(eLng, eLat, eName, eDescription, eType);
		
		List<BNRoutePlanNode> nodes = new ArrayList<BNRoutePlanNode>();
		nodes.add(startNode);
		nodes.add(endNode);
		
		BNRoutePlaner.getInstance().SetRouteSpec(true);
		BaiduNaviManager.getInstance().launchNavigator(this, nodes, preference, true, new RoutePlanListener() {
			
			@Override
			public void onRoutePlanFailed() {
				mIsAccessNav = false;
				Log.e(TAG, "RoutePlanActivity --> 算路失败！");
			}
			
			@Override
			public void onJumpToNavigator() {
				mIsAccessNav = true;
				Log.d(TAG, "RoutePlanActivity --> 算路成功！");
				successJumpToNavigator();
			}
		});
	}
	
	private void successJumpToNavigator(){
		// 算路成功后的操作
		BNMapController.getInstance().setLayerMode(LayerMode.MAP_LAYER_MODE_ROUTE_DETAIL);
		MyApplication.getApp().runOnUiGround(mAutoNavRunnable, 1000);
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
	
	private void showStartButtonText() {
		if (mBtnStartNav == null)
			return;

		if (mStopAutoNav) {
			mBtnStartNav.setText(getString(R.string.activity_preview_start_nav_text));
		} else {
			mBtnStartNav.setText(getString(R.string.activity_preview_start_nav_text) + SPACE + mTimeToBeginNav + SECOND);
		}
	}
}
