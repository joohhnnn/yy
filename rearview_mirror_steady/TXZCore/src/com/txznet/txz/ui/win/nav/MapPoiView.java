package com.txznet.txz.ui.win.nav;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.RecordWin2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.MapPoiListViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatMapView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMapPoiListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMapPoiListView.MapPoiConTrol;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMapPoiListView.TextClickListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.jni.JNIHelper;
import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdate;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.AMap.CancelableCallback;
import com.amap.api.maps.AMap.OnMapLoadedListener;
import com.amap.api.maps.TextureMapView;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.LatLngBounds.Builder;

public class MapPoiView extends IChatMapView {
	public static int MAP_ACTION_LOADING = 1;
	public static int MAP_ACTION_ENLARGE= 2;
	public static int MAP_ACTION_NARROW = 3;
	public static int MAP_ACTION_MAP= 4;
	public static int MAP_ACTION_LIST = 5;
	
	TextureMapView mMapView = null;
	AMap mAMap = null;
	MapPoiListViewData mViewData  = null;
	LatLng mDestinationlatLng = null;
	LatLng mLocationlatLng = null;
	boolean isLoaded = false;
	double mCurrentCenterLat  ;
	double mCurrentCenterLng ;
	boolean isVerticalScreen;
	private static final MapPoiView sInstance = new MapPoiView();
	private  MapPoiView() {
		
	}
	public static MapPoiView getInstance(){
		return sInstance;
	}
	@Override
	public ViewAdapter getView(ViewData data) {
		setScreenModel(ScreenUtil.isVerticalDevice());
		setData(data);
		
		mMapView = new  TextureMapView(GlobalContext.get());
		mMapView.onCreate((Bundle) null);
		mAMap = mMapView.getMap();
		mAMap.getUiSettings().setScaleControlsEnabled(true);
		mAMap.setOnMapLoadedListener(new OnMapLoadedListener() {

			@Override
			public void onMapLoaded() {
				isLoaded = true;
				getshowBounds(mViewData.getData());		
			}
		});	
		ViewAdapter adapter = new ViewAdapter();
		adapter.flags = null;
		adapter.object =null;
		adapter.type = -1;
		adapter.view = mMapView; 
		return adapter;
	}
	private void setData(ViewData data){
		mViewData =(MapPoiListViewData) data;
		if(mViewData.mLocationLat != null && mViewData.mLocationLng != null){
			mLocationlatLng = new LatLng(mViewData.mLocationLat, mViewData.mLocationLng);
		}
		if(mViewData.mDestinationLat != null && mViewData.mDestinationLng != null){
			mDestinationlatLng = new LatLng(mViewData.mDestinationLat, mViewData.mDestinationLng);
		}else{
			mDestinationlatLng = null;
		}
	}
	public void setScreenModel(boolean isVertical){
		isVerticalScreen = isVertical;
	}
	public class MarkerInfo{
		int index;
		Poi poi;
		Marker marker;
	}
	List<MarkerInfo> mPoiItemList = new ArrayList<MarkerInfo>();
	Marker mLocationMarker;
	Marker mDestinationMarker;
	int mCurrentClick = -1;
	int mPreClick = -1;
	double maxLat=0,maxLng=0,minLat=0,minLng=0;
	protected void getshowBounds(ArrayList<Poi> poiList) {
		if (poiList == null || poiList.size() < 1) {
			return;
		}
		for (MarkerInfo info : mPoiItemList) {
			info.marker.destroy();
		}
		if (mLocationMarker != null) {
			mLocationMarker.destroy();
			mLocationMarker = null;
		}
		if (mDestinationMarker != null) {
			mDestinationMarker.destroy();
			mDestinationMarker = null;
		}
		mPoiItemList.clear();
		mCurrentClick = -1;
		mPreClick = -1;
		maxLat = minLat = poiList.get(0).getLat();
		maxLng = minLng = poiList.get(0).getLng();
		
		int index = poiList.size()-1;
		for (int i = 0; i < poiList.size(); i++) {
			Poi item = poiList.get(index-i);

			MarkerInfo markerInfo = new MarkerInfo();
			markerInfo.poi = item;
			markerInfo.index =index-i;
			double lat = item.getLat();
			double lng = item.getLng();
			LatLng latLng = new LatLng(lat, lng);
			markerInfo.marker = mAMap.addMarker(new MarkerOptions()
					.position(latLng)
					.draggable(true)
					.title(item.getName())
					.snippet("DefaultMarker")				
					.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getMarKIconId(index-i, false)))));
			mPoiItemList.add(0,markerInfo);

			maxLat = lat > maxLat ? lat : maxLat;
			minLat = lat < minLat ? lat : minLat;
			maxLng = lng > maxLng ? lng : maxLng;
			minLng = lng < minLng ? lng : minLng;
		}

		double lat = 0;
		double lng = 0;
		if (mLocationlatLng != null) {
			mLocationMarker = mAMap.addMarker(new MarkerOptions()
					.position(mLocationlatLng)
					.title("location")
					.snippet("DefaultMarker")
					.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(LayouUtil.getDrawable("win_poi_mark_location")))));
			
			if(mViewData.mAction != null &&
					(mViewData.mAction.equals(PoiAction.ACTION_JINGYOU)
					|| mViewData.mAction.equals(PoiAction.ACTION_DEL_JINGYOU) )) {
				maxLat = mLocationlatLng.latitude > maxLat ? mLocationlatLng.latitude
						: maxLat;
				minLat = mLocationlatLng.latitude < minLat ? mLocationlatLng.latitude
						: minLat;
				maxLng = mLocationlatLng.longitude > maxLng ? mLocationlatLng.longitude
						: maxLng;
				minLng = mLocationlatLng.longitude < minLng ? mLocationlatLng.longitude
						: minLng;
			}
		}

		if (mDestinationlatLng != null) {
			mDestinationMarker = mAMap.addMarker(new MarkerOptions()
					.position(mDestinationlatLng)
					.draggable(true)
					.title("end")
					.snippet("DefaultMarker")
					.icon(BitmapDescriptorFactory.fromBitmap(drawableToBitmap(LayouUtil.getDrawable("win_poi_end")))));

			lat = mDestinationlatLng.latitude;
			lng = mDestinationlatLng.longitude;
			maxLat = lat > maxLat ? lat : maxLat;
			minLat = lat < minLat ? lat : minLat;
			maxLng = lng > maxLng ? lng : maxLng;
			minLng = lng < minLng ? lng : minLng;
		}

		if (isLoaded) {
			changeViewShowZoom();
		}

		AMap.OnMarkerClickListener markerClickListener = new AMap.OnMarkerClickListener() {

			@Override
			public boolean onMarkerClick(Marker marker) {
				for (int i = 0; i < mPoiItemList.size(); i++) {
					Poi poi = mPoiItemList.get(i).poi;
					LatLng position = marker.getPosition();
					if (poi.getName().equals(marker.getTitle())) {
						if(mTextClickListener != null){
							mTextClickListener.onClick(i);
						}
						checkPoiDeal(i);
					}
				}
				return true;
			}
		};
		mAMap.setOnMarkerClickListener(markerClickListener);
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
				if(mViewData.getData().size() == 1 && 
						!mViewData.mAction.equals(PoiAction.ACTION_JINGYOU) && 
						!mViewData.mAction.equals(PoiAction.ACTION_DEL_JINGYOU) ){
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
	public void checkPoiDeal(int index){
		if(index < mViewData.getData().size()){
			mPreClick = mCurrentClick;
			mCurrentClick = index;
			onCheckPoi(mViewData.getData().get(index).getLat(),mViewData.getData().get(index).getLng());
		}
	}
	private void onCheckPoi(final double lat,final double lng){
		for(int i = 0;i < mPoiItemList.size(); i++){
			if(mPoiItemList.get(i).index == mPreClick){
				mPoiItemList.get(i).marker.setIcon(
						BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getMarKIconId(i, false))));
			}
			if(mPoiItemList.get(i).index == mCurrentClick){
				mPoiItemList.get(i).marker.setIcon(
						BitmapDescriptorFactory.fromBitmap(drawableToBitmap(getMarKIconId(i, true))));
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
		if(mViewData== null || mViewData.getData().size() <1){
			changeLatLng =  CameraUpdateFactory.changeLatLng(new LatLng(0,0));
		}else{
			changeLatLng = CameraUpdateFactory.changeLatLng(new LatLng(
					 (latLngBounds.northeast.latitude+latLngBounds.southwest.latitude)/2+distanceLat, 
					 (latLngBounds.northeast.longitude+latLngBounds.southwest.longitude)/2+distanceLng) );			
		}

		mAMap.animateCamera(changeLatLng);
		latLngBounds = mAMap.getProjection().getVisibleRegion().latLngBounds;
	}
	private Drawable getMarKIconId(int index,boolean isClick) {
		int icon = isClick?index*2:index*2+1;
		int iconId = -1;
		switch (icon) {
		case 0:
			return LayouUtil.getDrawable("win_poi_mark_1_click");
		case 1:
			return LayouUtil.getDrawable("win_poi_mark_1_unclick");
		case 2:
			return LayouUtil.getDrawable("win_poi_mark_2_click");
		case 3:
			return LayouUtil.getDrawable("win_poi_mark_2_unclick");
		case 4:
			return LayouUtil.getDrawable("win_poi_mark_3_click");
		case 5:
			return LayouUtil.getDrawable("win_poi_mark_3_unclick");
		case 6:
			return LayouUtil.getDrawable("win_poi_mark_4_click");
		case 7:
			return LayouUtil.getDrawable("win_poi_mark_4_unclick");
		case 8:
			return LayouUtil.getDrawable("win_poi_mark_5_click");
		case 9:
			return LayouUtil.getDrawable("win_poi_mark_5_unclick");
		case 10:
			return LayouUtil.getDrawable("win_poi_mark_6_click");
		case 11:
			return LayouUtil.getDrawable("win_poi_mark_6_unclick");
		case 12:
			return LayouUtil.getDrawable("win_poi_mark_7_click");
		case 13:
			return LayouUtil.getDrawable("win_poi_mark_7_unclick");
		case 14:
			return LayouUtil.getDrawable("win_poi_mark_8_click");
		case 15:
			return LayouUtil.getDrawable("win_poi_mark_8_unclick");
		default:
			return null;
		}
	}
	private  Bitmap drawableToBitmap(Drawable drawable) {
        // 取 drawable 的长宽
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();

        // 取 drawable 的颜色格式
        Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
                : Bitmap.Config.RGB_565;
        // 建立对应 bitmap
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        // 建立对应 bitmap 的画布
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        // 把 drawable 内容画到画布中
        drawable.draw(canvas);
        return bitmap;
    }
	private DefaultMapPoiListView.TextClickListener  mTextClickListener; 
	public void setNumberOnClickListener(DefaultMapPoiListView.TextClickListener listener){
		mTextClickListener = listener;
	}
	public void changeMapZoom(boolean isUp){
		
		CameraUpdate cameraUpdate;
		if(mAMap != null){
	    	float maxZoom=mAMap.getMaxZoomLevel();
	    	float minZoom=mAMap.getMinZoomLevel();
	    	float nowzoom = mAMap.getCameraPosition().zoom;		
			if(isUp ){
				if( nowzoom < maxZoom){
					cameraUpdate = CameraUpdateFactory.zoomIn();
				}else{
					RecordWin2Manager.mapActionResult(mViewData.mMapAction,false);
					return ;
				}
				
			}else{
				 if(nowzoom > minZoom){
						cameraUpdate = CameraUpdateFactory.zoomOut();
				 }else{
					 RecordWin2Manager.mapActionResult(mViewData.mMapAction,false);
					 return ;
				}	
			}	
			mAMap.animateCamera(cameraUpdate, new CancelableCallback() {
				
				@Override
				public void onFinish() {
					RecordWin2Manager.mapActionResult(mViewData.mMapAction,true);
					moveToCenter(mCurrentCenterLat, mCurrentCenterLng);
				}				
				@Override
				public void onCancel() {
					
				}
			});		
		}
	}

	public void dismiss() {
		LogUtil.d(" MapPoi View dismiss mMapView = "+mMapView);
		if (mMapView != null) {
			try {
				mMapView.onDestroy();
				mMapView = null;
			} catch (Exception e) {
				LogUtil.loge("releaseMap",e);
				e.printStackTrace();
			}
		}
	}
	
	public void updata(ViewData data) {
		setData(data);
		Integer mapAction = mViewData.mMapAction;
		if(!isLoaded){
			return;
		}
		if( mapAction == null){
			getshowBounds(mViewData.getData());
		}else if(mapAction == MAP_ACTION_ENLARGE){
			changeMapZoom(true);
		}else if(mapAction == MAP_ACTION_NARROW){
			changeMapZoom(false);
		}
	}
	
	public MapPoiConTrol getMapPoiControl(){
		return mMapPoiControl;
	}
	
	private MapPoiConTrol mMapPoiControl = new MapPoiConTrol() {
		
		@Override
		public void updata(ViewData data) {
			MapPoiView.this.updata(data);
		}
		
		@Override
		public void setNumberOnClickListener(TextClickListener listener) {
			MapPoiView.this.setNumberOnClickListener(listener);
		}
		
		@Override
		public ViewAdapter getView(MapPoiListViewData data) {
			return MapPoiView.this.getView(data);
		}
		
		@Override
		public void dismiss() {
			MapPoiView.this.dismiss();
		}
		
		@Override
		public void checkPoiDeal(int index) {
			MapPoiView.this.checkPoiDeal(index);			
		}
	};
	
}
