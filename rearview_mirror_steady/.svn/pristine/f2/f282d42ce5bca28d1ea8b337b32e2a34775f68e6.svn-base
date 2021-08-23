package com.txznet.record.view;

import java.util.ArrayList;
import java.util.List;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;




//import com.amap.api.location.AMapLocation;
//import com.amap.api.location.AMapLocationClient;
//import com.amap.api.location.AMapLocationClientOption;
//import com.amap.api.location.AMapLocationClientOption.AMapLocationMode;
//import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.LocationSource.OnLocationChangedListener;
import com.amap.api.maps.MapView;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.LatLngBounds.Builder;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.adapter.ChatPoiAdapter;
import com.txznet.record.adapter.ChatPoiAdapter.PoiItem;
import com.txznet.record.adapter.ChatPoiAdapter.TextClickListener;
import com.txznet.record.bean.ChatMessage;
import com.txznet.record.bean.PoiMsg;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.lib.R;
import com.txznet.record.view.DisplayLvEx;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;

public class WinPoiShow  {
	public static WinPoiShow mIntance = new WinPoiShow();
	public View mViewRoot= null;
	public View mViewRootBase= null;
	TextureMapView mapView = null;
	AMap mAMap = null;
	LinearLayout mPoiLayout = null;
	LinearLayout mMapLoadingLayout= null;
	DisplayLvEx mPoiList =null;
	FrameLayout  mButtonLayout = null;
	FrameLayout  mFlMapView = null;
	PoiMsg  mPoiMsg = null;
	ImageView  mIvMapLoading= null;
	int mShowCount = -1;
	boolean isVerticalScreen = false;
	boolean mIsBusiness =false;
	boolean mIsHistory =false;
	LatLng mDestinationlatLng = null;
	LatLng mLocationlatLng = null;
	Boolean mIsList  = null;
	public void  setData(final ChatMessage msg ) {
		mPoiMsg = (PoiMsg)  msg;
		if(mPoiMsg.mItemList == null || mPoiMsg.mItemList.size() < 1){
			return;
		}
		Integer showCount = mPoiMsg.mShowCount;
		mIsBusiness= mPoiMsg.mIsBusiness;
		Boolean isJingYou= mPoiMsg.action.equals(PoiAction.ACTION_JINGYOU);	
		mIsList = mPoiMsg.mIsListModel;
		if(mIsList == null){
			mIsList = true;
		}
		mIsHistory = mPoiMsg.action.equals(PoiAction.ACTION_NAV_HISTORY) ;
		if (showCount != null && showCount != 0) {
			mShowCount = showCount;
		}
			if(mPoiMsg.mDestinationLat !=null && mPoiMsg.mDestinationLng !=null){
				mDestinationlatLng = new LatLng(mPoiMsg.mDestinationLat,mPoiMsg.mDestinationLng);
		}else{
			mDestinationlatLng = null;
		}
		if(mPoiMsg.mLocationLat !=null && mPoiMsg.mLocationLng !=null){
			mLocationlatLng = new LatLng(mPoiMsg.mLocationLat,mPoiMsg.mLocationLng);
		}		
		
//		 if(mViewRootBase !=null && mViewRootBase.getParent() ==null){
//			 JNIHelper.logd("zsbin mViewRootBase is remove");
//				if(mapView != null){
//					mapView.onDestroy();
//					mapView = null;
//				}
//				if(mViewRoot != null){
//					mViewRoot = null;
//				}
//				isLoaded = false;
//				isListLoaded = false;
//				mChatPoiAdapter = null;
//				mViewRootBase = null;
//				mMapInit = false;
//		 }
		
//		
//		if(mViewRoot == null){
//			creatView(poiMsg);
//			return mViewRootBase;
//		}else{
//			updateView(poiMsg);
//			return mViewRootBase;
//		}
	}
	public void changePoiShowMode(final boolean isList){
		mIsList =isList;
		if(isList){
			mPoiList.setBackgroundColor(Color.argb(38,255,255,255));
			mFlMapView.setVisibility(View.GONE);
			mButtonLayout.setVisibility(View.GONE);
			if (mapView != null) {
				mapView.setVisibility(View.GONE);
			}
//					TtsManager.getInstance().speakText("已为您切换到列表模式");
			
		}else{
			if(!mMapInit){
				mapInit();
			}
			if (mapView != null) {
				mapView.setVisibility(View.VISIBLE);
			}
			mPoiList.setBackgroundColor(Color.argb(204,0,0,0));
			mFlMapView.setVisibility(View.VISIBLE);
			mButtonLayout.setVisibility(View.VISIBLE);
//					TtsManager.getInstance().speakText("已为您切换到地图模式");
		}
		updateView();
	}
	
	
	
	public void changeMapZoom(boolean isUp){
		
		CameraUpdate cameraUpdate;
		if(mAMap != null){
	    	float maxZoom=mAMap.getMaxZoomLevel();
	    	float minZoom=mAMap.getMinZoomLevel();
	    	float nowzoom = mAMap.getCameraPosition().zoom;		
	    	final String hint ;
			if(isUp ){
				if( nowzoom < maxZoom){
					cameraUpdate = CameraUpdateFactory.zoomIn();
				}else{
					 RecordWin2Manager.mapActionResult(mPoiMsg.mMapAction, false);
					 mPoiMsg.mMapAction = null;
//					TtsManager.getInstance().speakText("当前为最大倍数无法进行放大");
					return ;
				}
				
			}else{
				 if(nowzoom > minZoom){
						cameraUpdate = CameraUpdateFactory.zoomOut();
				 }else{
					 RecordWin2Manager.mapActionResult(mPoiMsg.mMapAction, false);
					 mPoiMsg.mMapAction = null;
//					 TtsManager.getInstance().speakText("当前为最小倍数无法进行缩小");
					 return ;
				}	
			}	
			mAMap.animateCamera(cameraUpdate, new CancelableCallback() {
				
				@Override
				public void onFinish() {
					 RecordWin2Manager.mapActionResult(mPoiMsg.mMapAction, true);
					 mPoiMsg.mMapAction = null;
//					TtsManager.getInstance().speakText("已经为您"+hint+"地图");
					moveToCenter(mCurrentCenterLat, mCurrentCenterLng);
				}				
				@Override
				public void onCancel() {
					// TODO Auto-generated method stub
					
				}
			});
			
		}
		
	}
	
	public View creatView (){
		if(mViewRootBase !=null){
			return mViewRootBase;
		}
		mViewRootBase = View.inflate(GlobalContext.get(), R.layout.win_poi_show_base, null);
		final RelativeLayout  root = (RelativeLayout)mViewRootBase.findViewById(R.id.rlPoiShowBase);
		boolean isVertical = ScreenUtil.mListViewRectHeight > ScreenUtil.mListViewRectWidth;
		if(isVertical){
			mViewRoot =  View.inflate(GlobalContext.get(), R.layout.win_poi_show_vertical, null);
			isVerticalScreen = true;
		}else{
			mViewRoot = View.inflate(GlobalContext.get(), R.layout.win_poi_show_horizontal, null);
			isVerticalScreen = false; 
		}
		root.addView(mViewRoot);	
		Log.d("zsbin","creatView get mMapLoadingLayout");
		mMapLoadingLayout = (LinearLayout) mViewRoot.findViewById(R.id.ll_map_loading);
		Log.d("zsbin","creatView get mIvMapLoading");
		try {
			mIvMapLoading = (ImageView) mViewRoot.findViewById(R.id.iv_map_poi_loading);			
		} catch (Exception e) {
			Log.d("zsbin", e.toString());
		}
		
		Log.d("zsbin","creatView get mIvMapLoading end");		
		mPoiLayout = (LinearLayout) mViewRoot.findViewById(R.id.llPoi);
		LayoutParams lp = mPoiLayout.getLayoutParams();		
//		if(isVerticalScreen && !mIsList){
//			lp.height = ScreenUtil.mListViewRectHeight/2;
//		}else{
//			lp.height = ScreenUtil.mListViewRectHeight;
//		}
        lp.height = ScreenUtil.mListViewRectHeight;
		mPoiLayout.setLayoutParams(lp);
		
		mPoiList = (DisplayLvEx) mViewRoot.findViewById(R.id.list_ex);
		mButtonLayout = (FrameLayout) mViewRoot.findViewById(R.id.lyButton);
		
		mFlMapView = (FrameLayout) mViewRoot.findViewById(R.id.fm_map);
		
		if(!mIsList){
			mFlMapView.setVisibility(View.VISIBLE);
			mButtonLayout.setVisibility(View.VISIBLE);
			mapInit();
		}else{
			mFlMapView.setVisibility(View.GONE);
			mButtonLayout.setVisibility(View.GONE);
		}
		
		updateView();
		return mViewRootBase;
	}
	private boolean mMapInit =false;
	private void mapInit(){
		mapView = new TextureMapView(GlobalContext.get());
		mapView.setClickable(true);
		FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,FrameLayout.LayoutParams.MATCH_PARENT);
		if (!isVerticalScreen) {
			params.height = ScreenUtil.mListViewRectHeight;
		}
		mFlMapView.addView(mapView, params);
		mMapInit = true;
		mapView.onCreate((Bundle) null);
		mAMap = mapView.getMap();
		mAMap.getUiSettings().setScaleControlsEnabled(true);
		mAMap.setOnMapLoadedListener(new OnMapLoadedListener() {

			@Override
			public void onMapLoaded() {
				isLoaded = true;
					getshowBounds(mPoiMsg.mItemList);		
			}
		});		
	}
	
	OnLocationChangedListener mListener;
//	AMapLocationClient mlocationClient;
//	AMapLocationClientOption mLocationOption;
	double mLastLatitude= 0;
	double mLastLongitude= 0;
//	private void startLocation() {
//		mAMap.setLocationSource(new LocationSource() {
//			
//			@Override
//			public void deactivate() {
//				
//			}
//			
//			@Override
//			public void activate(OnLocationChangedListener listener) {
//			    mListener = listener;
//			    if (mlocationClient == null) {
//			        //初始化定位
//			        mlocationClient = new AMapLocationClient(GlobalContext.get());
//			        //初始化定位参数
//			        mLocationOption = new AMapLocationClientOption();
//			        //设置定位回调监听
//			        mlocationClient.setLocationListener(new AMapLocationListener() {
//						
//						@Override
//						public void onLocationChanged(AMapLocation arg0) {
//							mLastLatitude = arg0.getLatitude();
//							mLastLongitude = arg0.getLongitude();
//							String name = arg0.getPoiName();
//							if(arg0.getErrorCode() == AMapLocation.LOCATION_SUCCESS){
//								if(mLocationMarker !=null){
//									mLocationMarker.destroy();
//								}				
//								mLocationMarker=mAMap.addMarker(
//										new MarkerOptions().position(new LatLng(mLastLatitude, mLastLongitude)).title(name).snippet("DefaultMarker").icon(
//												BitmapDescriptorFactory.fromBitmap(
//														BitmapFactory.decodeResource(
//																GlobalContext.get().getResources(), R.drawable.current_point))));
//							}
//
//						}
//					});
//			        //设置为高精度定位模式
//			        mLocationOption.setLocationMode(AMapLocationMode.Hight_Accuracy);
//			        //设置定位参数
//			        mLocationOption.setInterval(2000);
//			        mlocationClient.setLocationOption(mLocationOption);
//			        mlocationClient.startLocation();//启动定位
//			    }
//			}
//		});
//		mAMap.setMyLocationEnabled(true);
//		mAMap.setMyLocationType(AMap.LOCATION_TYPE_LOCATE);
//	}

	private WinPoiShow(){
//		RecorderWin.OBSERVABLE.registerObserver(new StatusObserver() {
//
//			@Override
//			public void onShow() {
//			}
//
//			@Override
//			public void onDismiss() {
//				dismiss();
//			}              
//		});
	}
	private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			JSONBuilder jb = new JSONBuilder();
			jb.put("index", position);
			jb.put("lat",mPoiMsg.mItemList.get(position).mItem.getLat());
			jb.put("lng",mPoiMsg.mItemList.get(position).mItem.getLng());			
			jb.put("name",mPoiMsg.mItemList.get(position).mItem.getName());
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
					jb.toBytes(), null);
		}
	};

	public void dismiss() {
		if (mapView != null) {
			mapView.onDestroy();
			mapView = null;
		}
		if (mViewRoot != null) {
			mViewRoot = null;
		}
		if (mFlMapView != null) {
			mFlMapView.removeAllViews();
			mFlMapView = null;
		}
		isLoaded = false;
		isListLoaded = false;
		mChatPoiAdapter = null;
		mViewRootBase = null;
		mMapInit = false;

	}

	boolean isLoaded =false;
	boolean isListLoaded =false;
	double mCurrentCenterLat  ;
	double mCurrentCenterLng ;
	private void moveToCenter(double lat,double lng){
		mCurrentCenterLat = lat;
		mCurrentCenterLng = lng;
		LatLngBounds latLngBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
		double distanceLng =0;
		double distanceLat  = 0;
		if(isVerticalScreen){
			distanceLng= lng
					- (latLngBounds.northeast.longitude+ latLngBounds.southwest.longitude)/2;
			distanceLat = lat
					-( latLngBounds.southwest.latitude+(latLngBounds.northeast.latitude - latLngBounds.southwest.latitude)/4);			
		}else{
			distanceLng= lng
					-( latLngBounds.northeast.longitude-(latLngBounds.northeast.longitude - latLngBounds.southwest.longitude)/4);
			distanceLat = lat
					-( latLngBounds.northeast.latitude+ latLngBounds.southwest.latitude)/2;			
		}
		CameraUpdate changeLatLng = null;
		if(mPoiMsg.mItemList == null || mPoiMsg.mItemList.size() <1){
			changeLatLng =  CameraUpdateFactory.changeLatLng(new LatLng(mLastLatitude,mLastLongitude));
		}else{
			changeLatLng = CameraUpdateFactory.changeLatLng(new LatLng(
					 (latLngBounds.northeast.latitude+latLngBounds.southwest.latitude)/2+distanceLat, 
					 (latLngBounds.northeast.longitude+latLngBounds.southwest.longitude)/2+distanceLng) );			
		}

		mAMap.animateCamera(changeLatLng);
		latLngBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
	}
	
	private void changeViewShowZoom(){
		double distanceLat =  maxLat - minLat;
		double distanceLng = maxLng - minLng;
		double showMaxLng =0;
		double showMinLng =0;
		double showMinLat = 0;
		double showMaxLat =0;
		if( ! isVerticalScreen){
			showMaxLng= maxLng  + distanceLng/5;
			showMinLng =2*minLng-2*distanceLng/5 - showMaxLng;		
			showMinLat = minLat  - distanceLat/10;
			showMaxLat = maxLat  + distanceLat/10;				
		}else{
			showMaxLng= maxLng  + distanceLng/10;
			showMinLng =minLng-distanceLng/10;		
			showMinLat = minLat  - distanceLat/5;
			showMaxLat =2*maxLat+2*distanceLat/5 - showMinLat;			
		}

		
		Builder builder = LatLngBounds.builder();	
		builder.include(new LatLng(showMinLat, showMinLng));
		builder.include(new LatLng(showMinLat, showMaxLng));
		builder.include(new LatLng(showMaxLat, showMinLng));
		builder.include(new LatLng(showMaxLat, showMaxLng));
		LatLngBounds build = builder.build();

		LatLngBounds latLngBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;		
		CameraUpdate newLatLngBounds = CameraUpdateFactory.newLatLngBounds(build,5);
		mAMap.animateCamera(newLatLngBounds, new CancelableCallback() {
			
			@Override
			public void onFinish() {
				if(mPoiMsg.mItemList.size() == 1 && 
						!mPoiMsg.action.equals(PoiAction.ACTION_JINGYOU) && 
						!mPoiMsg.action.equals(PoiAction.ACTION_DEL_JINGYOU) ){
					onCheckPoi((maxLat+minLat)/2,(maxLng+minLng)/2);
				}else{
					moveToCenter((maxLat+minLat)/2,(maxLng+minLng)/2);
				}
				
			}
			
			@Override
			public void onCancel() {
				
			}
		});		
	}
	
	private void onCheckPoi(final double lat,final double lng){
		for(int i = 0;i < mPoiItemList.size(); i++){
			if(mPoiItemList.get(i).index == mPreClick){
				mPoiItemList.get(i).marker.setIcon(
						BitmapDescriptorFactory.fromBitmap(
							BitmapFactory.decodeResource(
									GlobalContext.get().getResources(), getMarKIconId(i, false))));
			}
			if(mPoiItemList.get(i).index == mCurrentClick){
				mPoiItemList.get(i).marker.setIcon(
						BitmapDescriptorFactory.fromBitmap(
							BitmapFactory.decodeResource(
									GlobalContext.get().getResources(), getMarKIconId(i, true))));
			}

		}
    	float newZoom=mAMap.getMaxZoomLevel()-4F;
    	float nowzoom = mAMap.getCameraPosition().zoom;
    	if(nowzoom >= newZoom){
    		moveToCenter(lat,lng);
    		return;
    	}
		CameraUpdate zoomTo = CameraUpdateFactory.zoomTo(newZoom);
		mAMap.animateCamera(zoomTo, new CancelableCallback() {
			
			@Override
			public void onFinish() {
				moveToCenter(lat,lng);
			}
			
			@Override
			public void onCancel() {
				
			}
		});		
	}
	ChatPoiAdapter mChatPoiAdapter = null;
	public void updateView(){
		mMapLoadingLayout.setVisibility(View.GONE);
		if(mPoiMsg.mMapAction != null){
			if(mPoiMsg.mMapAction == PoiMsg.MAP_ACTION_ENLARGE){
				changeMapZoom(true);
			}else if(mPoiMsg.mMapAction == PoiMsg.MAP_ACTION_NARROW){
				changeMapZoom(false);	
			}else if(mPoiMsg.mMapAction == PoiMsg.MAP_ACTION_LIST){
				mPoiMsg.mMapAction = null;
				changePoiShowMode(true);
			}else if(mPoiMsg.mMapAction == PoiMsg.MAP_ACTION_MAP){
				mPoiMsg.mMapAction = null;
				changePoiShowMode(false);		
			}else if(mPoiMsg.mMapAction == PoiMsg.MAP_ACTION_LOADING){
				mMapLoadingLayout.setVisibility(View.VISIBLE);
				mIvMapLoading.setImageResource(R.drawable.poimap_loading_anim);
		        AnimationDrawable animationDrawable1 = (AnimationDrawable) mIvMapLoading.getDrawable();		        		
		        animationDrawable1.start();
			}
			return;
		}
		if(mPoiMsg.mItemList == null || mPoiMsg.mItemList.size() < 1){
			noPoiListDeal();
		}

		mChatPoiAdapter = new ChatPoiAdapter(GlobalContext.getModified(), mPoiMsg.mItemList , mShowCount);
		mChatPoiAdapter.setIsList(mIsList);
		if(  mIsBusiness && !isVerticalScreen ){
			mChatPoiAdapter.setIsUseNewLayout(true);		
		}else{
			mChatPoiAdapter.setIsUseNewLayout(false);
		}
		if(isVerticalScreen && !mIsList){
			mChatPoiAdapter.setListViewHeight(ScreenUtil.mListViewRectHeight/2);
		}else{
			mChatPoiAdapter.setListViewHeight(ScreenUtil.mListViewRectHeight);
		}
		if(mIsHistory){
			mChatPoiAdapter.setIsHistoryLayout(mIsHistory);
		}
		if( !mIsList){
			mChatPoiAdapter.setNumberOnClickListener(new TextClickListener() {
				
				@Override
				public void onClick(int index) {
					checkPoiDeal(index);
				}
			});					
		}

		KeyEventManagerUI1.getInstance().updateListAdapter(mChatPoiAdapter);	
		mPoiList.setAdapter(mChatPoiAdapter);
		mPoiList.setOnItemClickListener(mOnItemClickListener);
		if(isLoaded && !mIsList){					
			getshowBounds(mPoiMsg.mItemList);				
		}
		
		if(mIsHistory){
			mChatPoiAdapter.setHistoryDelOnClickListener(new TextClickListener() {
				
				@Override
				public void onClick(int index) {			
					JSONBuilder jb = new JSONBuilder();
					jb.put("index", index);
					jb.put("action","delete");
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.ui.event.item.selected",
							jb.toBytes(), null);
				}
			});
		}
//		mAMap.animateCamera(CameraUpdateFactory.newLatLngBounds(arg0, arg1));
		if(mPoiMsg.mItemList == null || mPoiMsg.mItemList.size() <1){
			mPoiLayout.setVisibility(View.GONE);
		}else{
			if(mIsList){
				mPoiList.setBackgroundColor(Color.argb(38,255,255,255));
			}else{
				mPoiList.setBackgroundColor(Color.argb(204,0,0,0));
			}
			mPoiList.setVisibility(View.VISIBLE);
		}
	}
	private void noPoiListDeal() {
    	float newZoom=mAMap.getMaxZoomLevel()-4F;
    	float nowzoom = mAMap.getCameraPosition().zoom;
		CameraUpdate zoomTo = CameraUpdateFactory.zoomTo(newZoom);
		mAMap.animateCamera(zoomTo, new CancelableCallback() {
			
			@Override
			public void onFinish() {
				moveToCenter(mLastLatitude,mLastLongitude);
			}
			
			@Override
			public void onCancel() {
				
			}
		});			
	}
	public class MarkerInfo{
		int index;
		Poi poi;
		Marker marker;
	}
	List<MarkerInfo> mPoiItemList = new ArrayList<MarkerInfo>();
	Marker mLocationMarker;
	Marker mDestinationMarker;
	Marker mQueryMarker;
	double maxLat=0,maxLng=0,minLat=0,minLng=0;
	private void getshowBounds(List<PoiItem> poiList) {
		if(poiList ==null || poiList.size() < 1){
			return ;
		}
		for(MarkerInfo info :mPoiItemList){
			info.marker.destroy();
		}
		if(mLocationMarker != null){
			mLocationMarker.destroy();
			mLocationMarker = null;			
		}
		if(mDestinationMarker != null){
			mDestinationMarker.destroy();
			mDestinationMarker = null;		
		}
		mPoiItemList.clear();
		mCurrentClick = -1;
		mPreClick = -1;
		maxLat = minLat = poiList.get(0).mItem.getLat();
		maxLng = minLng = poiList.get(0).mItem.getLng();
		int index = poiList.size()-1;
		for (int i = 0; i < poiList.size(); i++) {
			Poi item = poiList.get(index-i).mItem;
			
			MarkerInfo markerInfo = new MarkerInfo();
			markerInfo.poi = item;	
			markerInfo.index = index-i;
			double lat = item.getLat();
			double lng = item.getLng();
			LatLng latLng = new LatLng(lat,lng);
			markerInfo.marker = mAMap.addMarker(
					new MarkerOptions().position(latLng).draggable(true).title(item.getName()).snippet("DefaultMarker").icon(
							BitmapDescriptorFactory.fromBitmap(
									BitmapFactory.decodeResource(
											GlobalContext.get().getResources(), getMarKIconId(index-i, false)))));
			mPoiItemList.add(0,markerInfo);
			
			maxLat = lat > maxLat ? lat : maxLat;
			minLat = lat < minLat ? lat : minLat;
			maxLng = lng > maxLng ? lng : maxLng;
			minLng = lng < minLng ? lng : minLng;
		}
		
		double lat =0;
		double lng =0;
		if(mLocationlatLng !=null){
			mLocationMarker=mAMap.addMarker(
					new MarkerOptions().position(mLocationlatLng).title("location").snippet("DefaultMarker").icon(
							BitmapDescriptorFactory.fromBitmap(
									BitmapFactory.decodeResource(
											GlobalContext.get().getResources(), R.drawable.win_poi_mark_location))));
			
			if(mPoiMsg.action.equals(PoiAction.ACTION_JINGYOU) || mPoiMsg.action.equals(PoiAction.ACTION_DEL_JINGYOU) ){
				maxLat = mLocationlatLng.latitude > maxLat ? mLocationlatLng.latitude  : maxLat;
				minLat = mLocationlatLng.latitude  < minLat ? mLocationlatLng.latitude  : minLat;
				maxLng = mLocationlatLng.longitude > maxLng ? mLocationlatLng.longitude : maxLng;
				minLng = mLocationlatLng.longitude < minLng ? mLocationlatLng.longitude : minLng;				
			}
		}
		
		if(mDestinationlatLng != null){
			mDestinationMarker = mAMap.addMarker(
					new MarkerOptions().position(mDestinationlatLng).draggable(true).title("end").snippet("DefaultMarker").icon(
							BitmapDescriptorFactory.fromBitmap(
									BitmapFactory.decodeResource(
											GlobalContext.get().getResources(), R.drawable.win_poi_end))));
			lat = mDestinationlatLng.latitude;
			lng = mDestinationlatLng.longitude;
			maxLat = lat > maxLat ? lat : maxLat;
			minLat = lat < minLat ? lat : minLat;
			maxLng = lng > maxLng ? lng : maxLng;
			minLng = lng < minLng ? lng : minLng;
		}

		if(isLoaded){
			changeViewShowZoom();	
		}
		
		AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {

		    @Override
		    public boolean onMarkerClick(Marker marker) {
		    	for(int i = 0; i< mPoiItemList.size() ;i++){
		    		Poi poi = mPoiItemList.get(i).poi;
		    		LatLng position = marker.getPosition();
		    		if(poi.getName().equals(marker.getTitle())){
		    			checkPoiDeal(i);
		    		}
		    	}
		        return true;
		    }
		};
		mAMap.setOnMarkerClickListener(markerClickListener);
	}
	private int mCurrentClick = -1;
	private int mPreClick = -1;
	private void checkPoiDeal(int index){
		if(index < mPoiMsg.mItemList.size()){
			mPreClick = mCurrentClick;
			mCurrentClick = index;
			if (mChatPoiAdapter != null) {
                mChatPoiAdapter.update(mCurrentClick, mPoiList, mPreClick);
                onCheckPoi(mPoiMsg.mItemList.get(index).mItem.getLat(),mPoiMsg.mItemList.get(index).mItem.getLng());
            }
		}
	}
	
	private int getMarKIconId(int index,boolean isClick) {
		int icon = isClick?index*2:index*2+1;
		int iconId = -1;
		switch (icon) {
		case 0:
			return R.drawable.win_poi_mark_1_click;
		case 1:
			return R.drawable.win_poi_mark_1_unclick;
		case 2:
			return R.drawable.win_poi_mark_2_click;
		case 3:
			return R.drawable.win_poi_mark_2_unclick;
		case 4:
			return R.drawable.win_poi_mark_3_click;
		case 5:
			return R.drawable.win_poi_mark_3_unclick;
		case 6:
			return R.drawable.win_poi_mark_4_click;
		case 7:
			return R.drawable.win_poi_mark_4_unclick;
		case 8:
			return R.drawable.win_poi_mark_5_click;
		case 9:
			return R.drawable.win_poi_mark_5_unclick;
		case 10:
			return R.drawable.win_poi_mark_6_click;
		case 11:
			return R.drawable.win_poi_mark_6_unclick;
		case 12:
			return R.drawable.win_poi_mark_7_click;
		case 13:
			return R.drawable.win_poi_mark_7_unclick;
		case 14:
			return R.drawable.win_poi_mark_8_click;
		case 15:
			return R.drawable.win_poi_mark_8_unclick;
		default:
			return -1;
		}
	}
	
	public static WinPoiShow getIntance(){
		return mIntance;
	}
}
