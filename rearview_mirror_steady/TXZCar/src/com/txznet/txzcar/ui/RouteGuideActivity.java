package com.txznet.txzcar.ui;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.navisdk.adapter.BNRouteGuideManager;
import com.baidu.navisdk.adapter.BNRouteGuideManager.CustomizedLayerItem;
import com.baidu.navisdk.adapter.BNRouteGuideManager.OnNavigationListener;
import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.adapter.BaiduNaviManager;
import com.baidu.navisdk.comapi.mapcontrol.BNMapViewFactory;
import com.baidu.navisdk.model.datastruct.LocData;
import com.baidu.navisdk.model.datastruct.SensorData;
import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.baidu.navisdk.ui.routeguide.BNavigator.OnNaviBeginListener;
import com.baidu.navisdk.ui.routeguide.IBNavigatorListener;
import com.baidu.nplatform.comapi.MapItem;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.baidu.nplatform.comapi.map.MapObj;
import com.baidu.nplatform.comapi.map.MapViewListener;
import com.txznet.comm.base.StackActivity;
import com.txznet.comm.ui.dialog.WinConfirm;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txzcar.MultiNavManager;
import com.txznet.txzcar.MyApplication;
import com.txznet.txzcar.NavManager;
import com.txznet.txzcar.R;

public class RouteGuideActivity extends StackActivity{
	
	private View mMapView;
	
	private static final String KEY_EXTRA_BUNDLE = "bundle";
	
	public static void navigateTo(Context context,Bundle bundle){
		Intent intent = new Intent(context,RouteGuideActivity.class);
		intent.putExtra(KEY_EXTRA_BUNDLE, bundle);
		context.startActivity(intent);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		processIntent();

		init();
		initNavigator();
		createRouteMap();
		
		if(mMapView == null){
			finish();
			return ;
		}
		
		setContentView(mMapView);
	}
	
	private void processIntent(){
		Bundle mBundle = null;
		Intent intent = getIntent();
		if(intent != null){
			mBundle = intent.getBundleExtra(KEY_EXTRA_BUNDLE);
		}
	}
	
	private void createRouteMap(){
		mMapView = BNRouteGuideManager.getInstance().onCreate(this, new OnNavigationListener() {
			
			@Override
			public void onNaviGuideEnd() {
				
			}
			
			@Override
			public void notifyOtherAction(int arg0, int arg1, int arg2, Object arg3) {
			}
		});

		BNRouteGuideManager.getInstance().showCustomizedLayer(true);
		
		MyApplication.getApp().runOnUiGround(new Runnable() {
			
			@Override
			public void run() {
				if(mMapView != null){
					checkViewGroup(mMapView);
				}
			}
		}, 10000);
	}
	
	private void init(){
		mConfirmExitWin = new WinConfirm() {
			@Override
			public void onClickOk() {
				NavManager.getInstance().stopNavi();
				finish();
			}
		}.setMessage("确定退出导航？");
	}
	
	private void initNavigator(){
		BNavigator.getInstance().setListener(mIbNavigatorListener);
		BNavigator.getInstance().addOnNaviBeginListener(mOnNaviBeginListener);
		BNavigator.getInstance().setNavigationListener(mOnNavigationListener);
	}
	
	private void checkViewGroup(View view){
		if(view instanceof ViewGroup){
			ViewGroup v = (ViewGroup)view;
			Rect outRect = new Rect();
			v.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view ViewGroup childCount is:"+v.getChildCount()+",id is:"+v.getId()+",Rect is:"+outRect + ",name is:"+v.getClass().getName());
			for(int i = 0;i<v.getChildCount();i++){
				checkViewGroup(v.getChildAt(i));
			}
		}else if(view instanceof TextView){
			Rect outRect = new Rect();
			view.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view TextView rect is:"+outRect.toShortString()+",text is:"+((TextView)view).getText().toString()+",id is:"+view.getId() + ",name is:"+view.getClass().getName());
		}else if(view instanceof ImageView){
			Rect outRect = new Rect();
			view.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view ImageView rect is:"+outRect.toShortString()+",id is:"+view.getId() + ",name is:"+view.getClass().getName());
		}else {
			Rect outRect = new Rect();
			view.getHitRect(outRect);
			Log.d("NavView", "checkViewGroup view rect is:"+outRect.toShortString() + ",name is:"+view.getClass().getName());
		}
	}
	
	@Override
	protected void onResume() {
		BNRouteGuideManager.getInstance().onResume();
		super.onResume();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		BNRouteGuideManager.getInstance().onStop();
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		mConfirmExitWin.dismiss();
		BaiduNaviManager.getInstance().uninit();
		BNRouteGuideManager.getInstance().onDestroy();
	}
	
	@Override
	public void onBackPressed() {
		if (!mConfirmExitWin.isShowing()) {
			mConfirmExitWin.show();
		} else {
			mConfirmExitWin.dismiss();
		}
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		BNRouteGuideManager.getInstance().onConfigurationChanged(newConfig);
		super.onConfigurationChanged(newConfig);
	}
	
	private WinConfirm mConfirmExitWin = null;
	
	
	private OnNaviBeginListener mOnNaviBeginListener = new OnNaviBeginListener() {
		
		@Override
		public void onNaviBegin(boolean nav) {
			if(!NavManager.getInstance().isNav()){
				NavManager.getInstance().setIsNav(true);
				MultiNavManager.getInstance().startNavigate(NavManager.getInstance().getNavigateInfo());
			}
			
			mMglsfv = BNMapViewFactory.getInstance().getMainMapView();
			mMglsfv.setMapViewListener(mMapViewListener);
//			Drawable markerDrawable = getResources().getDrawable(R.drawable.icon_car);
//			List<CustomizedLayerItem> list = new ArrayList<CustomizedLayerItem>();
//			list.add(new CustomizedLayerItem(113.949109,22.5339871,CoordinateType.GCJ02, markerDrawable, CustomizedLayerItem.ALIGN_CENTER));
//			list.add(new CustomizedLayerItem(113.957109,22.5419871,CoordinateType.GCJ02, markerDrawable, CustomizedLayerItem.ALIGN_CENTER));
//			list.add(new CustomizedLayerItem(113.953109,22.5392871,CoordinateType.GCJ02, markerDrawable, CustomizedLayerItem.ALIGN_CENTER));
//			list.add(new CustomizedLayerItem(113.934709,22.5521871,CoordinateType.GCJ02, markerDrawable, CustomizedLayerItem.ALIGN_CENTER));
//			BNRouteGuideManager.getInstance().setCustomizedLayerItems(list);
		}
	};
	
	private MapGLSurfaceView mMglsfv;
	
	private OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
		
		@Override
		public void onNaviGuideEnd() {
			
		}
		
		@Override
		public void notifyOtherAction(int arg0, int arg1, int arg2, Object arg3) {
		}
	};
	
	private IBNavigatorListener mIbNavigatorListener = new IBNavigatorListener() {
		
		@Override
		public void onYawingRequestSuccess() {
			Log.d("BNavigator", "BNavigator -- > onYawingRequestSuccess");
		}
		
		@Override
		public void onYawingRequestStart() {
			Log.d("BNavigator", "BNavigator -- > onYawingRequestStart");
		}
		
		@Override
		public void onPageJump(int arg0, Object arg1) {
			Log.d("BNavigator", "BNavigator -- > onPageJump");
		}
		
		@Override
		public void notifyViewModeChanged(int arg0) {
			Log.d("BNavigator", "BNavigator -- > notifyViewModeChanged");
		}
		
		@Override
		public void notifyStartNav() {
			Log.d("BNavigator", "BNavigator -- > notifyStartNav");
		}
		
		@Override
		public void notifySensorData(SensorData arg0) {
			Log.d("BNavigator", "BNavigator -- > notifySensorData");
		}
		
		@Override
		public void notifyOtherAction(int arg0, int arg1, int arg2, Object arg3) {
			Log.d("BNavigator", "BNavigator -- > notifyOtherAction");
		}
		
		@Override
		public void notifyNmeaData(String arg0) {
			Log.d("BNavigator", "BNavigator -- > notifyNmeaData");
		}
		
		@Override
		public void notifyLoacteData(LocData locData) {
			Log.d("BNavigator", "BNavigator -- > notifyLoacteData");
			MyApplication.getApp().runOnBackGround(new Runnable1<LocData>(locData) {

				@Override
				public void run() {
					if(MultiNavManager.getInstance().isNav()){
						MultiNavManager.getInstance().updateMyLoc(mP1);
					}
				}
			}, 0);
		}
		
		@Override
		public void notifyGPSStatusData(int arg0) {
			Log.d("BNavigator", "BNavigator -- > notifyGPSStatusData");
		}
	};
	
	private MapViewListener mMapViewListener = new MapViewListener() {
		
		@Override
		public void onMapObviousMove() {
			Log.d("MapViewListener", "MapViewListener -- > onMapObviousMove");
		}
		
		@Override
		public void onMapNetworkingChanged(boolean arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onMapNetworkingChanged");
		}
		
		@Override
		public void onMapAnimationFinish() {
			Log.d("MapViewListener", "MapViewListener -- > onMapAnimationFinish");
		}
		
		@Override
		public void onDoubleFingerZoom() {
			Log.d("MapViewListener", "MapViewListener -- > onDoubleFingerZoom");
		}
		
		@Override
		public void onDoubleFingerRotate() {
			Log.d("MapViewListener", "MapViewListener -- > onDoubleFingerRotate");
		}
		
		@Override
		public void onClickedStreetPopup(String arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedStreetPopup");
		}
		
		@Override
		public void onClickedStreetIndoorPoi(MapObj arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedStreetIndoorPoi");
		}
		
		@Override
		public void onClickedRouteSpecLayer(MapItem arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedRouteSpecLayer");
		}
		
		@Override
		public void onClickedPopupLayer() {
			Log.d("MapViewListener", "MapViewListener -- > onClickedPopupLayer");
		}
		
		@Override
		public void onClickedPOILayer(MapItem arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedPOILayer");
		}
		
		@Override
		public void onClickedPOIBkgLayer(MapItem arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedPOIBkgLayer");
		}
		
		@Override
		public void onClickedFavPoiLayer(MapItem arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedFavPoiLayer");
		}
		
		@Override
		public void onClickedCompassLayer() {
			Log.d("MapViewListener", "MapViewListener -- > onClickedCompassLayer");
		}
		
		@Override
		public void onClickedBasePOILayer(MapItem arg0) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedBasePOILayer");
		}
		
		@Override
		public void onClickedBaseLayer() {
			Log.d("MapViewListener", "MapViewListener -- > onClickedBaseLayer");
		}
		
		@Override
		public void onClickedBackground(int arg0, int arg1) {
			Log.d("MapViewListener", "MapViewListener -- > onClickedBackground");
		}
	};
}
