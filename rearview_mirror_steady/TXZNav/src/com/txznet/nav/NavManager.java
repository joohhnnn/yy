package com.txznet.nav;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.baidu.navisdk.ui.routeguide.BNavigator;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.im.UiIm.ActionRoomMemberList_Resp;
import com.txz.ui.im.UiIm.RoomMember;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LocationUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.nav.multinav.MultiNavManager;
import com.txznet.nav.multinav.MultiNavManager.LocationData;
import com.txznet.nav.ui.CheckMapActivity;
import com.txznet.nav.ui.NavStartActivity;
import com.txznet.nav.ui.PreviewActivity;
import com.txznet.nav.ui.SetLocationActivity;
import com.txznet.nav.util.BDLocationUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;

public class NavManager {

	public static final int LOCATION_COMPANY = 2;
	public static final int LOCATION_HOME = 1;
	public static final int LOCATION_NONE = 0;

	private static NavManager mInstance = new NavManager();
	private NavService mNavService;
	private boolean mIsNavi;
	private boolean mIsMultiNav;
	
	private NavigateInfo mNavigateInfo;

	public static NavManager getInstance() {
		return mInstance;
	}

	private NavManager() {
		mNavService = NavService.getInstance();
		notifyNavStatus();
	}

	public LocationInfo getLocationInfo() {
		return mNavService.getLocationInfo();
	}

	public void NavigateHome() {
		NavigateTo(mNavService.getHome());
	}

	public void NavigateCompany() {
		NavigateTo(mNavService.getCompany());
	}

	public void NavigateTo(NavigateInfo info) {
		if (info == null || info.strTargetName == null
				|| info.strTargetAddress == null || info.msgGpsInfo == null
				|| info.msgGpsInfo.dblLat == null
				|| info.msgGpsInfo.dblLng == null
				|| info.msgGpsInfo.uint32GpsType == null)
			return;
		
		initForMultiNav(info);
		
		Log.d("MultiNav", "MultiNav -------- > startPreview," + "dbLat is:"+info.msgGpsInfo.dblLat+",dblLng is:"+info.msgGpsInfo.dblLng);
		startPreview(info.strTargetName, info.strTargetAddress,
				info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng,
				info.msgGpsInfo.uint32GpsType);
	}
	
	private void initForMultiNav(NavigateInfo info){
		if(info.msgServerPushInfo != null){
			if(info.msgServerPushInfo.uint32Type == UiMap.NT_MULTI_NATIGATION){
				setIsMultiNav(true);
			}else {
				setIsMultiNav(false);
			}
			
			mNavigateInfo = info;
		}
	}

	public void setHome(String name, String address, double lat, double lng,
			int gpsType) {
		NavigateInfo home = new NavigateInfo();
		home.strTargetName = name;
		home.strTargetAddress = address;
		home.msgGpsInfo = new GpsInfo();
		if (UiMap.GPS_TYPE_BD09 == gpsType) {
			double xy[] = BDLocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			lat = xy[0];
			lng = xy[1];
		}

		home.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;

		home.msgGpsInfo.dblLat = lat;
		home.msgGpsInfo.dblLng = lng;
		LocationUtil.setHome(home);
	}

	public NavigateInfo getHome() {
		return mNavService.getHome();
	}

	/**
	 * 向同行者设置家
	 * 
	 * @param name
	 * @param address
	 * @param lat
	 * @param lng
	 * @param gpsType
	 */
	public void setCompany(String name, String address, double lat, double lng,
			int gpsType) {
		NavigateInfo company = new NavigateInfo();
		company.strTargetName = name;
		company.strTargetAddress = address;
		company.msgGpsInfo = new GpsInfo();
		if (UiMap.GPS_TYPE_BD09 == gpsType) {
			double xy[] = BDLocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			lat = xy[0];
			lng = xy[1];
		}

		company.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		company.msgGpsInfo.dblLat = lat;
		company.msgGpsInfo.dblLng = lng;
		LocationUtil.setCompany(company);
	}

	public NavigateInfo getCompany() {
		return mNavService.getCompany();
	}

	public void setHistory(String name, String address, double lat, double lng,
			int gpsType) {
		LogUtil.logd("addHistory: name=" + name + ", address=" + address
				+ ", lat=" + lat + ", lng=" + lng + ", type=" + gpsType);
		mNavService.setHistory(name, address, lat, lng, gpsType);
	}

	public NavigateInfoList getHistoryList() {
		return mNavService.getHistoryList();
	}

	/**
	 * 启动查地图界面
	 * 
	 * @param where
	 *            SetLocationActivity.LOCATION_COMPANY，SetLocationActivity.
	 *            LOCATION_HOME，SetLocationActivity.LOCATION_NONE
	 */
	public void startCheckMap(int where) {
		MyApplication.getInstance().finishActivity(NavStartActivity.class);
		MyApplication.getInstance().finishActivity(PreviewActivity.class);
		Intent intent = new Intent(MyApplication.getApp(),
				CheckMapActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("where", where);
		MyApplication.getApp().startActivity(intent);
	}

	/**
	 * 启动搜索界面
	 * 
	 * @param strDest
	 * @param city
	 * @param isNearbySearch
	 * @param where
	 *            SetLocationActivity.LOCATION_COMPANY，SetLocationActivity.
	 *            LOCATION_HOME，SetLocationActivity.LOCATION_NONE
	 */
	public void startSearch(String strDest, String city,
			boolean isNearbySearch, int where) {
		if (!isInit()) {
			TtsUtil.speakText("导航初始化中，请稍后...");
			return;
		}

		JSONObject json = new JSONObject();
		try {
			json.put("keywords", strDest);
			json.put("city", city);
			json.put("isNearbySearch", isNearbySearch);
			json.put("where", where);
		} catch (Exception e) {
		}

		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.inner.poiSearch", json.toString().getBytes(), null);
	}

	/**
	 * 启动设置地址界面
	 * 
	 * @param where
	 */
	public void startSetLocation(int where) {
		Intent intent = new Intent(MyApplication.getApp(),
				SetLocationActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("where", where);
		MyApplication.getApp().startActivity(intent);
	}

	/**
	 * 
	 * @param name
	 * @param address
	 * @param lat
	 * @param lng
	 * @param type
	 */
	public void startPreview(String name, String address, double lat,
			double lng, int type) {
		Intent intent = new Intent(MyApplication.getApp(),
				PreviewActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.putExtra("name", name);
		intent.putExtra("address", address);
		intent.putExtra("lat", lat);
		intent.putExtra("lng", lng);
		intent.putExtra("type", type);
		Log.d("PreView", "startPreview lat:"+lat + ",lng:"+lng);
		
		MyApplication.getInstance().runOnUiGround(
				new Runnable1<Intent>(intent) {

					@Override
					public void run() {
						stopNavi();
						MyApplication.getInstance().runOnUiGround(
								new Runnable1<Intent>(mP1) {

									@Override
									public void run() {
										MyApplication.getApp().startActivity(
												mP1);
									}
								}, 0);
					}
				}, 0);
	}

	/**
	 * 暂停导航预览时自动倒数
	 */
	public void stopPreviewCount() {
		Activity a = MyApplication.getInstance().getActivity(
				PreviewActivity.class);
		if (a == null)
			return;
		PreviewActivity pa = (PreviewActivity) a;
		pa.stopAutoNav();
	}

	/**
	 * 退出导航
	 */
	public void stopNavi() {
		if(NavManager.getInstance().isMultiNav()){
			if(MultiNavManager.getInstance().isMultiNav()){
				MultiNavManager.getInstance().reset();
			}
			
			endMultiNav();
		}
		
		MyApplication.getInstance().finishActivity(NavStartActivity.class);
		MyApplication.getInstance().finishActivity(PreviewActivity.class);
		if (isNavi()) {
			BNavigator.getInstance().onExitDialogConfirm();
			BNavigator.destory();
			NavManager.getInstance().setIsNavi(false);
			NavManager.getInstance().setIsMultiNav(false);
		}
	}
	
	public void quickLocation(boolean b) {
		mNavService.quickLocation(b);
	}

	public boolean isNavi() {
		return mIsNavi;
	}
	
	public boolean isMultiNav(){
		return mIsMultiNav;
	}

	public void setIsNavi(boolean isNavi) {
		if (mIsNavi != isNavi) {
			mIsNavi = isNavi;

			notifyNavStatus();
		}
	}
	
	private void notifyNavStatus(){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
				"txz.nav.inner.notifyNavStatus", ("" + mIsNavi).getBytes(),
				null);
	}
	
	public void setIsMultiNav(boolean isMultiNav){
		this.mIsMultiNav = isMultiNav;
	}
	
	/**
	 * 开始多车同行
	 * @param info
	 */
	public void beginMultiNav(){
		if(mNavigateInfo == null){
			return;
		}
		
		Log.d("MultiNav", "MultiNav -------------> NavManager beginMultinav <-----------");
		try {
			MultiNavManager.getInstance().setIsMultiNav(false);
			MultiNavManager.getInstance().setNavigateInfo(mNavigateInfo);
			MyApplication.getApp().runOnBackGround(new Runnable2<NavigateInfo,Long>(mNavigateInfo,mNavigateInfo.msgServerPushInfo.uint64RoomId) {
				
				@Override
				public void run() {
					Log.d("MultiNav", "MultiNav -------------> NavManager beginMultinav sendInvoke<-----------");
					ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.beginMultiNav", MessageNano.toByteArray(mP1), null);
					getMultiDetailMemInfoList(mP2);
					
				}
			}, 0);
			
		} catch (Exception e) {
			LogUtil.loge("MultiNav error!");
		}
	}
	
	/**
	 *  结束多车同行
	 * @param info
	 */
	public void endMultiNav(){
		Log.d("MultiNav", "MultiNav -------------> NavManager endMultiNav <-----------");
		try {
			if(MultiNavManager.getInstance().getNavigateInfo() != null){
				sendEndMultiNavInvoke(MultiNavManager.getInstance().getNavigateInfo());
			}
			
			if(mNavigateInfo == null){
				return;
			}
			
			sendEndMultiNavInvoke(mNavigateInfo);
		} catch (Exception e) {
			LogUtil.loge("endMultiNav error!");
		}
	}
	
	private void sendEndMultiNavInvoke(NavigateInfo info){
		MyApplication.getApp().runOnBackGround(new Runnable1<NavigateInfo>(info) {
			
			@Override
			public void run() {
				ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.endMultiNav", MessageNano.toByteArray(mP1), null);
			}
		}, 0);
	}
	
	/**
	 * 获取成员列表
	 * @param info
	 */
	public void getMultiDetailMemInfoList(long roomId){
		Log.d("MultiNav", "MultiNav-------------> NavManager getMultiDetailMemInfoList sendInvoke <-----------");
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.getMultiNavMemList.detail", String.valueOf(roomId).getBytes(), null);
	}
	
	/**
	 * 获取用户的GPS信息
	 * @param info
	 */
	public void getMultiGPSMemInfoList(long roomId){
		Log.d("MultiNav", "MultiNav-------------> NavManager getMultiGPSMemInfoList <-----------");
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.multi.nav.inner.getMultiNavMemList.gps", String.valueOf(roomId).getBytes(), null);
	}
	
	/**
	 * 刷新当前用户信息的列表
	 * @param data
	 */
	public void refreshMemberList(byte[] data){
		Log.d("MultiNav", "MultiNav ------------- > NavManager refreshMemberList <--------- data[]:"+data);
		try {
			if(!MultiNavManager.getInstance().isMultiNav()){
				MultiNavManager.getInstance().setIsMultiNav(true);
			}
			ActionRoomMemberList_Resp armlr = ActionRoomMemberList_Resp.parseFrom(data);
			if( armlr == null ){
				Log.d("MultiNav", "MultiNav ------------- > NavManager refreshMemberList ActionRoomMemverList_Resp is null");
				return;
			}
			if( armlr.uint64Rid != MultiNavManager.getInstance().getRoomId() ){
				Log.d("MultiNav", "MultiNav ------------- > NavManager refreshMemberList RoomId not equals,action is:"+armlr.uint64Rid+",Data is:"+MultiNavManager.getInstance().getRoomId());
				return;
			}
			
			List<LocationData> ldList = new ArrayList<LocationData>();
			RoomMember[] rms = armlr.rptMsgMemberList;
			Log.d("MultiNav", "MultiNav----------------> refreshMemberList RoomMember array:"+rms);
			if (rms != null) {
				for (RoomMember rm : rms) {
					LocationData ld = new LocationData();
					if (rm.msgInfo != null) {
						ld.imagePath = rm.msgInfo.strFaceUrl;
					}else {
						ld.isGps = true;
					}
					
					if (rm.msgInfo.uint64Uid != null && !"".equals(rm.msgInfo.uint64Uid)) {
						ld.userId = String.valueOf(rm.msgInfo.uint64Uid);
					}
					
					if (rm.msgGps != null) {
//						LatLng latLng = BDLocationUtil.getLocation(rm.msgGps);
//						int[] itude = MultiNavManager.convertToInteger(latLng.latitude, latLng.longitude);
						int[] itude = MultiNavManager.convertToInteger(rm.msgGps.dblLat, rm.msgGps.dblLng);
						ld.latitudeE6 = itude[0];
						ld.longitudeE6 = itude[1];
						ld.rotateDegress = rm.msgGps.fltDirection;
					}

					ldList.add(ld);
					Log.d("Nav", "NavManager ------ > " + ld.toString());
				}
				
				// 开始加入数据
				MultiNavManager.getInstance().setUpLocationData(ldList);
			}
		} catch (InvalidProtocolBufferNanoException e) {
			LogUtil.loge("InvalidProtocolBufferNanoException error!");
		}
	}

	public boolean isInit() {
		return mNavService.isInit();
	}
}
