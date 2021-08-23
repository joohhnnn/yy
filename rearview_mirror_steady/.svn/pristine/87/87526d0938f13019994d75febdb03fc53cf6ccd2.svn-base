package com.txznet.nav.multinav;

import android.util.Log;
import android.view.View;

import com.baidu.nplatform.comapi.MapItem;
import com.baidu.nplatform.comapi.map.MapObj;
import com.baidu.nplatform.comapi.map.MapViewListener;
import com.baidu.nplatform.comapi.streetscape.model.StreetscapeInfoModel;
import com.txznet.nav.MyApplication;
import com.txznet.nav.ui.widget.UserViewGroup;
import com.txznet.nav.util.BDLocationUtil;

public class MultiMapViewListener implements MapViewListener {
	
	UserViewGroup mUvg;
	
	public MultiMapViewListener(UserViewGroup uvg) {
		mUvg = uvg;
	}

	@Override
	public void onClickStreetArrow(StreetscapeInfoModel arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickStreetArrow");
	}

	@Override
	public void onClickedBackground(int arg0, int arg1) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedBackground");
	}

	@Override
	public void onClickedBaseLayer() {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedBaseLayer");
	}

	@Override
	public void onClickedBasePOILayer(MapItem arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedBasePOILayer");
		Log.d("MapViewListener", "MapViewListener ------------->lat:"+arg0.mLatitude+",lng:"+arg0.mLongitude);
		double[] map = BDLocationUtil.Convert_BD09_To_GCJ02(arg0.mLatitude, arg0.mLongitude);
		Log.d("MapViewListener", "MapViewListener --GCJ02-->lat:"+map[0]+",lng:"+map[1]);
	}

	@Override
	public void onClickedCompassLayer() {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedCompassLayer");
	}

	@Override
	public void onClickedFavPoiLayer(MapItem arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedFavPoiLayer");
	}

	@Override
	public void onClickedPOIBkgLayer(MapItem arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedPOIBkgLayer");
	}

	@Override
	public void onClickedPOILayer(MapItem arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedPOILayer");
	}

	@Override
	public void onClickedPopupLayer() {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedPopupLayer");
	}

	@Override
	public void onClickedStreetIndoorPoi(MapObj arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedStreetIndoorPoi");
	}

	@Override
	public void onClickedStreetPopup(String arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onClickedStreetPopup");
	}

	@Override
	public void onDoubleFingerRotate() {
		Log.d("MapViewListener", "MapViewListener -------------> onDoubleFingerRotate");
	}

	@Override
	public void onDoubleFingerZoom() {
		Log.d("MapViewListener", "MapViewListener -------------> onDoubleFingerZoom");
	}

	@Override
	public void onMapAnimationFinish() {
		Log.d("MapViewListener", "MapViewListener -------------> onMapAnimationFinish");
		if(mUvg != null){
			mUvg.setVisibility(View.VISIBLE);
		}
	}

	@Override
	public void onMapNetworkingChanged(boolean arg0) {
		Log.d("MapViewListener", "MapViewListener -------------> onMapNetworkingChanged");
	}

	@Override
	public void onMapObviousMove() {
		if(mUvg != null && mUvg.getVisibility() != View.GONE){
			mUvg.setVisibility(View.GONE);
		}
		
		MyApplication.getApp().removeUiGroundCallback(r);
		MyApplication.getApp().runOnUiGround(r, 100);
	}
	
	Runnable r = new Runnable() {
		
		@Override
		public void run() {
			onMapAnimationFinish();
		}
	};
}
