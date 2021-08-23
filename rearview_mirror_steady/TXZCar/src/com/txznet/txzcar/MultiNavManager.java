package com.txznet.txzcar;

import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.baidu.navisdk.adapter.BNRoutePlanNode.CoordinateType;
import com.baidu.navisdk.model.datastruct.LocData;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.ServerPushNavigateInfo;
import com.txznet.txzcar.data.GeoData;
import com.txznet.txzcar.util.BDLocationUtil;

public class MultiNavManager {
	private static final String TAG = "MultiNavManager";
	
	private long mRoomId;
	private int mNavType;
	
	private long mUserId;
	
	// 当前是否开始了多车同行
	private boolean isNav;
	
	NavigateInfo mNavigateInfo;
	HandlerThread updateThread;
	Handler updateHandler;
	
	volatile GeoData mMyGeoData;
	
	private static MultiNavManager instance = new MultiNavManager();
	
	public static MultiNavManager getInstance(){
		return instance;
	}
	
	private MultiNavManager(){
		initThread();
	}
	
	private void initThread(){
		updateThread = new HandlerThread("update-thread");
		updateThread.start();
		updateHandler = new Handler(updateThread.getLooper());
	}
	
	public void startNavigate(NavigateInfo navigateInfo){
		this.mNavigateInfo = navigateInfo;
		if(mNavigateInfo == null){
			return;
		}
		
		ServerPushNavigateInfo spni = mNavigateInfo.msgServerPushInfo;
		if(spni == null){
			return ;
		}
		mUserId = spni.uint64FromUid;
		
		createMyGeoData(mNavigateInfo);
		
		reqBegin();
	}
	
	private void createMyGeoData(NavigateInfo navigateInfo){
		if(mMyGeoData != null){
			mMyGeoData = null;
		}
		
		mMyGeoData = new GeoData();
		mMyGeoData.imagePath = navigateInfo.msgServerPushInfo.strToFaceUrl;
		mMyGeoData.userId = String.valueOf(mUserId);
		mMyGeoData.isGpsOnly = false;
		
		LocationInfo info = NavManager.getInstance().getLocationInfo();
		if(info == null){
			return;
		}
		
		GpsInfo gps = info.msgGpsInfo;
		double[] latlng = BDLocationUtil.getGCJ02(gps);
		if(latlng == null){
			return;
		}
		
		double alt = 0;
		float dir = 0,rad = 0,spd = 0;
		
		if(gps.dblAltitude != null){
			alt = gps.dblAltitude;
		}
		
		if(gps.fltDirection != null){
			dir = gps.fltDirection;
		}

		if(gps.fltRadius != null){
			rad = gps.fltRadius;
		}
		
		if(gps.fltSpeed != null){
			spd = gps.fltSpeed;
		}
		
		mMyGeoData.lat = latlng[0];
		mMyGeoData.lng = latlng[1];
		mMyGeoData.altitude = alt;
		mMyGeoData.direction = dir;
		mMyGeoData.radius = rad;
		mMyGeoData.speed = spd;
		mMyGeoData.type = CoordinateType.GCJ02;
	}
	
	public GeoData getMyGeoData(){
		return mMyGeoData;
	}

	/**
	 * 设置多车同行的相关参数
	 * @param roomId
	 * @param navType
	 */
	public void preBegin(long roomId,int navType){
		this.mRoomId = roomId;
		this.mNavType = navType;
	}
	
	/**
	 * 开始多车同行
	 */
	public void begin(){
		if(mRoomId == 0 || mRoomId == -1){
			return;
		}
		
		isNav = true;
		
		updateHandler.removeCallbacks(update);
		updateHandler.postDelayed(update, 5000);
	}
	
	/**
	 * 结束多车同行
	 */
	public void end(){
		mRoomId = -1;
		mNavType = -1;
		isNav = false;
		updateHandler.removeCallbacks(update);
	}
	
	/**
	 * @param gdList
	 */
	public void updateMemberList(List<GeoData> gdList){
		try {
			DataManager.getInstance().parseGeoDatas(gdList);
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, TAG + " -- > 更新联系人列表发生异常！");
		}
	}
	
	/**
	 * 加入多车同行
	 * @param geoData
	 */
	public void join(GeoData geoData){
		DataManager.getInstance().addGeoDatas(geoData);
	}
	
	/**
	 * uid退出多车同行
	 * @param uid
	 */
	public void quit(String uid){
		DataManager.getInstance().removeGeoData(uid);
	}
	
	/**
	 * 请求开始多车同行
	 */
	public void reqBegin(){
		if(mNavigateInfo == null){
			Log.e(TAG, TAG + " -- > 请求开始失败，NavigateInfo 为空！");
			return;
		}
		
		ServiceRequest.sendBeginInvoke(mNavigateInfo);
	}
	
	/**
	 * 请求结束多车同行
	 */
	public void reqEnd(){
		if(!isNav){
			Log.e(TAG, TAG + " -- > 请求结束失败，当前不处于多车同行！");
			return;
		}
		
		if(mNavigateInfo == null){
			Log.e(TAG, TAG + " -- > 请求结束失败，NavigateInfo 为空！");
			return;
		}
		
		ServiceRequest.sendEndInvoke(mNavigateInfo);
	}
	
	/**
	 * 请求获取好友
	 * param isGps 是否只包含GPS信息
	 */
	public void reqMemberList(boolean isGps){
		if(!isNav || mRoomId == -1 || mRoomId == 0){
			Log.e(TAG, TAG + " -- > 请求获取好友失败，没有开始导航！");
			return;
		}
	
		if(isGps){
			ServiceRequest.sendGetGPSMemInfoList(mRoomId);
		}else {
			ServiceRequest.sendGetDetailMemberListInfo(mRoomId);
		}
	}
	
	/**
	 * 更新我的位置
	 * @param locData
	 */
	public void updateMyLoc(LocData locData){
		if(!isNav){
			return;
		}
		
		if(mMyGeoData == null){
			createMyGeoData(mNavigateInfo);
		}
		
		mMyGeoData.altitude = locData.altitude;
		mMyGeoData.direction = locData.direction;
		mMyGeoData.lat = locData.latitude;
		mMyGeoData.lng = locData.longitude;
		mMyGeoData.speed = locData.speed;
	}
	
	Runnable update = new Runnable() {
		public void run() {
			int count = 0;
			reqMemberList(false);
			if (isNav) {
				try {
					
					Thread.sleep(1000);

					if(count % 15 == 0 & count != 0){
						reqMemberList(false);
						count = 0;
					}else if(count % 5 == 0 && count != 0){
						reqMemberList(true);
					}
					
					count ++;
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	};
	
	public NavigateInfo getNavigateInfo(){
		return mNavigateInfo;
	}
	
	public void setRoomId(long rid){
		mRoomId = rid;
	}
	
	public long getRoomId(){
		return mRoomId;
	}
	
	public void setNavType(int type){
		mNavType = type;
	}
	
	public int getNavType(){
		return mNavType;
	}
	
	public boolean isNav(){
		return isNav;
	}
}
