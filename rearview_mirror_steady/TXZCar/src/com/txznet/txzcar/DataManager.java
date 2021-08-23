package com.txznet.txzcar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.baidu.nplatform.comapi.basestruct.GeoPoint;
import com.baidu.nplatform.comapi.map.OverlayItem;
import com.baidu.nplatform.comapi.map.OverlayItem.CoordType;
import com.baidu.nplatform.comjni.tools.JNITools;
import com.txznet.txzcar.data.GeoData;
import com.txznet.txzcar.util.ResUtil;

public class DataManager {
	private static final String TAG = "DataManager";
	
	Map<String, OverlayItem> oliMap = new HashMap<String,OverlayItem>();
	
	Map<String, GeoData> mGpList = new HashMap<String,GeoData>();
	
//	List<OverlayItem> needRefreshItems = new ArrayList<OverlayItem>();
	
	List<String> mExistUserId = new ArrayList<String>();
	
	private static DataManager instance = new DataManager();
	
	public static DataManager getInstance(){
		return instance;
	}
	
	/**
	 * 解析用户列表
	 */
	void parseGeoDatas(List<GeoData> geoDatas){
		mExistUserId.clear();
		mGpList.clear();
		for(GeoData gd:geoDatas){
			megerToOverlayItems(gd);
			mExistUserId.add(gd.userId);
			mGpList.put(gd.userId, gd);
		}
		
		// 通知刷新覆盖物
		List<String> uList = OverlayManager.getInstance().getAllUserId();
		for(String uid:uList){
			if(mExistUserId.contains(uid)){
				// 表示存在覆盖物，为更新操作
				GeoData gData = mGpList.get(uid);
				OverlayManager.getInstance().updateOverlayItem(uid,gData.lng,gData.lat);
			}else {
				OverlayManager.getInstance().removeOverlayItem(uid);
			}
		}
		
		for(String uid:mExistUserId){
			if(uList.contains(uid)){
				// 跳过
			}else {
				OverlayItem item = oliMap.get(uid);
				OverlayManager.getInstance().addOverlayItem(uid, item);
			}
		}
	}
	
	/**
	 * 将数据转化为覆盖物
	 * @param gd
	 */
	void megerToOverlayItems(GeoData gd){
		Log.d(TAG, TAG + " -- >" + gd.toString());
		String userId = gd.userId;
		boolean isContain = oliMap.containsKey(userId);
		if(isContain){
			OverlayItem item = oliMap.get(userId);
			GeoPoint eGeoPoint = item.getPoint();
			
			Bundle bundle = JNITools.LL2MC(gd.lng, gd.lat);
			if (bundle == null) {
				return;
			}
			eGeoPoint.setLongitudeE6(bundle.getInt("MCx"));
			eGeoPoint.setLatitudeE6(bundle.getInt("MCy"));
			item.setGeoPoint(eGeoPoint);
			item.setMarker(ResUtil.getInstance().getDrawableByUid(userId, ""));
			
		}else {
			if(TextUtils.isEmpty(userId)){
				return;
			}
			
			Bundle bundle = JNITools.LL2MC(gd.lng, gd.lat);
			if (bundle == null) {
				return;
			}
			GeoPoint point = new GeoPoint(bundle.getInt("MCx"), bundle.getInt("MCy"));
			Drawable markDrawable = ResUtil.getInstance().getDrawableByUid(gd.userId,gd.imagePath);
			MyOverlayItem overlayItem = new MyOverlayItem(point, "", "");
			overlayItem.setUserId(gd.userId);
			overlayItem.setCoordType(CoordType.CoordType_BD09);
			overlayItem.setAnchor(OverlayItem.ALIGN_TOP);
			if(markDrawable == null){
				markDrawable = ResUtil.getInstance().getDefaultMark();
			}
			overlayItem.setMarker(markDrawable);
			oliMap.put(userId, overlayItem);
		}
	}
	
	/**
	 * 添加一个用户
	 * @param gd
	 */
	void addGeoDatas(GeoData gd){
		megerToOverlayItems(gd);
		OverlayItem item = oliMap.get(gd.userId);
		OverlayManager.getInstance().addOverlayItem(gd.userId, item);
	}
	
	/**
	 * 删除一个用户
	 * @param uid
	 */
	void removeGeoData(String uid){
		OverlayManager.getInstance().removeOverlayItem(uid);
	}
	
	public static class MyOverlayItem extends OverlayItem{
		
		String userId;
		boolean hasSetDrawable;

		public MyOverlayItem(GeoPoint point, String title, String snippet) {
			super(point, title, snippet);
		}
		
		public void setUserId(String uid){
			userId = uid;
		}
		
		public String getUserId(){
			return userId;
		}
		
		public void setHasSetDrawable(boolean hasSet){
			hasSetDrawable = hasSet;
		}
		
		public boolean isSetDrawable(){
			return hasSetDrawable;
		}
		
		@Override
		public boolean equals(Object o) {
			return userId.equals(((MyOverlayItem)o).getUserId());
		}
	}
}
