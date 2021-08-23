package com.txznet.txzcar;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;

import com.baidu.navisdk.comapi.mapcontrol.BNMapController;
import com.baidu.navisdk.comapi.mapcontrol.BNMapItemizedOverlay;
import com.baidu.navisdk.comapi.mapcontrol.BNMapViewFactory;
import com.baidu.nplatform.comapi.basestruct.GeoPoint;
import com.baidu.nplatform.comapi.basestruct.Point;
import com.baidu.nplatform.comapi.map.MapGLSurfaceView;
import com.baidu.nplatform.comapi.map.OverlayItem;
import com.baidu.nplatform.comjni.tools.JNITools;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txzcar.DataManager.MyOverlayItem;

public class OverlayManager {
	
	List<String> overlayUId = new ArrayList<String>();
	
	static OverlayManager instance = new OverlayManager();
	
	GeoPoint mGeoPoint = new GeoPoint();
	Rect mRect = new Rect();
	
	private OverlayManager(){
		udpateRect();
	}
	
	public static OverlayManager getInstance(){
		return instance;
	}
	
	private void udpateRect(){
		MapGLSurfaceView msfv = BNMapViewFactory.getInstance().getMainMapView();
		if(msfv != null){
			msfv.getGlobalVisibleRect(mRect);
		}
		
		if(mRect == null){
			mRect = new Rect();
		}
	}
	
	private boolean isShowPoint(double lat,double lng){
		mGeoPoint.setLatitudeE6((int)(lat * 1e5));
		mGeoPoint.setLongitudeE6((int)(lng * 1e5));
		try {
			Point point = BNMapController.getInstance().getScreenPosByGeoPos(mGeoPoint);
			
			if(mRect == null || (mRect.left == 0 && mRect.top == 0 && mRect.right == 0 && mRect.bottom == 0)){
				udpateRect();
			}
			
			if(!mRect.contains(point.x, point.y)){
				// 屏幕外不更新
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			Log.d("OverlayManager", "OverlayManager -- > 判断是否显示点发生异常！");
		}
		
		return true;
		
	}
	
	/**
	 * 更新
	 * @param data
	 */
	public void updateOverlayItem(String uid,double lng,double lat){
		OverlayItem oli = getOverlayByUserId(uid);
		if(!isShowPoint(lat, lng)){
			return;
		}
		
		if(oli != null){
			GeoPoint eGeoPoint = oli.getPoint();
			Bundle bundle = JNITools.LL2MC(lng, lat);
			if (bundle == null) {
				return;
			}
			eGeoPoint.setLongitudeE6(bundle.getInt("MCx"));
			eGeoPoint.setLatitudeE6(bundle.getInt("MCy"));
			
			MyApplication.getApp().runOnBackGround(new Runnable1<OverlayItem>(oli) {
				
				@Override
				public void run() {
					BNMapItemizedOverlay.getInstance().updateItem(mP1);
					BNMapViewFactory.getInstance().getMainMapView().refresh(BNMapItemizedOverlay.getInstance());
				}
			}, 0);
		}
	}
	
	/**
	 * 更新覆盖物的头像
	 * @param uid
	 * @param drawable
	 */
	public void updateOverlayDrawable(String uid,Drawable drawable){
		OverlayItem oli = getOverlayByUserId(uid);
		if(oli != null){
			oli.setMarker(drawable);
			MyApplication.getApp().runOnBackGround(new Runnable1<OverlayItem>(oli) {
				
				@Override
				public void run() {
					BNMapItemizedOverlay.getInstance().updateItem(mP1);
					BNMapViewFactory.getInstance().getMainMapView().refresh(BNMapItemizedOverlay.getInstance());
				}
			}, 0);
		}
	}
	
	/**
	 * 加入
	 * @param data
	 */
	public void addOverlayItem(String uid,OverlayItem item){
		removeOverlayItem(uid);
		MyApplication.getApp().runOnBackGround(new Runnable1<OverlayItem>(item) {
			
			@Override
			public void run() {
				BNMapItemizedOverlay.getInstance().addItem(mP1);
				BNMapViewFactory.getInstance().getMainMapView().refresh(BNMapItemizedOverlay.getInstance());
			}
		}, 0);
		
		overlayUId.add(uid);
	}
	
	/**
	 * 删除
	 * @param uid
	 */
	public void removeOverlayItem(String uid){
		OverlayItem item = getOverlayByUserId(uid);
		if(item != null){
			MyApplication.getApp().runOnBackGround(new Runnable1<OverlayItem>(item) {
				
				@Override
				public void run() {
					BNMapItemizedOverlay.getInstance().removeItem(mP1);
					BNMapViewFactory.getInstance().getMainMapView().refresh(BNMapItemizedOverlay.getInstance());
				}
			}, 0);
			
			overlayUId.remove(uid);
		}
	}
	
	/**
	 * 通过id取得覆盖物
	 * @param uid
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public OverlayItem getOverlayByUserId(String uid){
		ArrayList<OverlayItem> oliList = BNMapItemizedOverlay.getInstance().getAllItem();
		for(OverlayItem item:oliList){
			boolean equals = ((MyOverlayItem)item).getUserId().equals(uid);
			if(equals){
				return item;
			}
		}
		
		return null;
	}
	
	public List<String> getAllUserId(){
		return overlayUId;
	}
}
