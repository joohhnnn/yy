package com.txznet.nav.multinav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import android.graphics.Color;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.baidu.mapapi.model.LatLng;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.im.UiIm.ActionRoomIn_Resp;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.nav.NavManager;
import com.txznet.nav.ui.widget.DirectionCiv;
import com.txznet.nav.util.BDLocationUtil;
import com.txznet.nav.util.PreferenceUtil;

public class MultiNavManager {
	
	private static final boolean isTestEv = false;
	private static final boolean isDebug = false;

	private volatile boolean isMultiNav;
	private volatile boolean isNeedReloadDc = true;

	private volatile int mNavType;           // 来源（1：微信群; 2：公众帐号）
	private volatile long mRoomId;           // 房间ID
	private String mCurrentUserId;
	
	// 最新的UserID
	private Set<String> mNewUserId = new HashSet<String>();
	private Map<String, LocationData> mNewLd = new HashMap<String, LocationData>();
	
	// 记录当前的用户所有的ID
	private Set<String> mAllUserId = new HashSet<String>();
	private Map<String, LocationData> mCacheLd = new HashMap<String, LocationData>();
	private Map<String, DirectionCiv> mDciv = new HashMap<String, DirectionCiv>();

	private NavigateInfo mNavigateInfo;
	private static MultiNavManager mInstance = new MultiNavManager();

	private MultiNavManager(){ 
		initThread();
	}
	
	public static MultiNavManager getInstance(){
		return mInstance;
	}
	
	public void setNavigateInfo(NavigateInfo info){
		if(mNavigateInfo != null){
			NavManager.getInstance().stopNavi();
		}
		
		this.mNavigateInfo = info;
		setNavType(mNavigateInfo.msgServerPushInfo.uint32Type);
		setRoomId(mNavigateInfo.msgServerPushInfo.uint64RoomId);
		mCurrentUserId = String.valueOf(mNavigateInfo.msgServerPushInfo.uint64FromUid);
		
		LocationData ld = new LocationData();
		ld.imagePath = info.msgServerPushInfo.strToFaceUrl;
		ld.isGps = false;
		ld.userId = mCurrentUserId;
		
		LatLng latLng = BDLocationUtil.getLocation(info.msgGpsInfo);
		int[] itude = convertToInteger(latLng.latitude, latLng.longitude);
		ld.latitudeE6 = itude[0];
		ld.longitudeE6 = itude[1];
		ld.rotateDegress = 0;
		
		updateLocationData(mCurrentUserId, ld);
	}
	
	public static class LocationData{
		public int latitudeE6;
		public int longitudeE6;
		public String userId;
		public float rotateDegress;
		public String imagePath;
		public boolean isGps;
		
		@Override
		public String toString() {
			return "["+"lat:"+latitudeE6+",long:"+longitudeE6+",userId:"+userId+",imagePath:"+imagePath+"]";
		}
	}
	
	public NavigateInfo getNavigateInfo(){
		return mNavigateInfo;
	}
	
	public boolean isTestEnv(){
		if(NavManager.getInstance().isMultiNav()){
			return false;
		}
		
		return isTestEv;
	}
	
	public static boolean isDebug(){
		return isDebug;
	}
	
	public void setIsMultiNav(boolean isMulti){
		isMultiNav = isMulti;
	}
	
	public boolean isMultiNav(){
		return isMultiNav;
	}
	
	/**
	 * 处理进入房间
	 * @param data
	 */
	public void handleRoomIn(byte[] data){
		try {
			ActionRoomIn_Resp arir = ActionRoomIn_Resp.parseFrom(data);
			if (arir != null) {
				setNavType(arir.uint32FromType);
				Log.d("MultiNav", "MultiNav -------> handleRoomin --> RoomId is:"+arir.uint64Rid);
				setRoomId(arir.uint64Rid);
			}

			Log.d("MultiNav", "MultiNav -------> 本设备进入多车同行");
			
			long roomId = getRoomId();
			if (roomId != 0 && roomId != -1) {
				setIsMultiNav(true);
				begin();
				
				// 开始获取联系人列表
				NavManager.getInstance().getMultiDetailMemInfoList(getRoomId());
			}
		} catch (InvalidProtocolBufferNanoException e) {
		}
	}
	
	/**
	 * 处理退出房间
	 */
	public void handleRoomOut(){
		mRoomId = 0;
		setIsMultiNav(false);
		end();
	}
	
	public void setCurrentUserId(String userId){
		mCurrentUserId = userId;
	}
	
	public String getCurrentUserId(){
		return mCurrentUserId;
	}
	
	public void setNavType(int type){
		mNavType = type;
	}
	
	public void setRoomId(long roomId){
		mRoomId = roomId;
	}
	
	public int getNavType(){
		return mNavType;
	}
	
	public long getRoomId(){
		return mRoomId;
	}
	
	public boolean isNeedReloadDc(){
		return isNeedReloadDc;
	}
	
	public void setNeedReload(boolean isReload){
		this.isNeedReloadDc = isReload;
	}
	
	public Set<String> getAllUserId(){
		return mAllUserId;
	}
	
	public static int[] convertToInteger(double dblLat,double dblLng){
		return new int[]{(int)(dblLat * 1e5),(int)(dblLng * 1e5)};
	}
	
	public synchronized void reset(){
		Log.d("MultiNav", "MultiNav ----------> DataManager reset <-----------");
		mNavType = 0;
		mRoomId = 0;
		mNavigateInfo = null;
		setIsMultiNav(false);
		setNeedReload(true);
		setCurrentUserId("");
		
		mDciv.clear();
		mNewLd.clear();
		mCacheLd.clear();
	}
	
	/**
	 * 更新所有的用户数据
	 * @param datas
	 */
	public void setUpLocationData(List<LocationData> datas){
		Log.d("MultiNav", "MultiNav ------- > DataManager setUpLocationData is:"+datas);
		
		boolean isNeed = false;
		synchronized (mNewLd) {
			mNewLd.clear();
		
			synchronized (mNewUserId) {
				mNewUserId.clear();

				synchronized (mAllUserId) {
					
					synchronized (mDciv) {

						if (datas != null) {
							for (LocationData ld : datas) {
								if (mAllUserId.contains(ld.userId)) {
									// 之前已经存在该用户的相对详细信息
									LocationData d = mCacheLd.get(ld.userId);
									if (ld.isGps) {
										// 仅仅是更新GPS信息
										d.latitudeE6 = ld.latitudeE6;
										d.longitudeE6 = ld.longitudeE6;
										d.rotateDegress = ld.rotateDegress;
									} else {
										d = ld;
										isNeed = true;
									}
									mNewLd.put(ld.userId, d);
								} else {
									isNeed = true;
									mNewLd.put(ld.userId, ld);
								}

								mNewUserId.add(ld.userId);

								// 刷新DirectionDiv数据
								synchronized (mDciv) {
									try {
										final DirectionCiv dc = mDciv.get(ld.userId);
										if (dc == null) {
											// 根据新增的id生成界面元素
											DirectionCiv d = WTDUtil.generateDc(Color.parseColor("#34bfff"), ld.rotateDegress, ld.imagePath,ld.userId);
											mDciv.put(ld.userId, d);
										} else {
//											WTDUtil.setDirectionDiv(dc, ld.imagePath);
										}
									} catch (Exception e) {
									}
								}
							}
						}
					}
					
				}
			}
		}
		
		// 同步最新的定位数据
		synchronized (mCacheLd) {
			mCacheLd.clear();
			synchronized (mNewLd) {
				Set<Entry<String, LocationData>> set = mNewLd.entrySet();
				for(Entry<String, LocationData> entry:set){
					mCacheLd.put(entry.getKey(), entry.getValue());
				}
			}
		}
		
		// 同步最新的用户ID
		synchronized (mAllUserId) {
			mAllUserId.clear();
			synchronized (mNewUserId) {
				mAllUserId.addAll(mNewUserId);
			}
		}
		
		if(isNeed){
			setNeedReload(true);
		}
	}
	
	/**
	 * 更新当前用户的位置
	 * @param locData
	 */
	public void updateCurrentUser(double latitude,double longitude,float direction){
		LocationData ld = new LocationData();
		ld.userId = mCurrentUserId;
		if(ld.userId == null || "".equals(ld.userId)){
			ld.userId = "1";
		}
		
		ld.isGps = true;
		int[] itude = convertToInteger(latitude, longitude);
		ld.latitudeE6 = itude[0];
		ld.longitudeE6 = itude[1];
		ld.rotateDegress = direction;
		
		try {
			updateLocationData(ld.userId, ld);
		} catch (Exception e) {
		}
	}
	
	/**
	 * 更新列表中的数据
	 * @param userId
	 * @param ld
	 */
	public void updateLocationData(String userId,LocationData ld){
		Log.d("MultiNav", "MultiNav ------------> DataManager updateLocationData userId:"+userId+",LocationData:"+ld);
		boolean needReload = false;
		synchronized (mAllUserId) {
			synchronized (mCacheLd) {

				if (userId != null && !"".equals(userId)) {
					if (ld == null) {
						// 退出多车同行
						if (mAllUserId.contains(userId)) {
							LocationData l = mCacheLd.get(userId);
							if (l != null) {
								synchronized (mCacheLd) {
									mCacheLd.remove(l);
								}
							}
							mAllUserId.remove(userId);
						}

						LocationData d = mCacheLd.get(userId);
						if (d != null) {
							mCacheLd.remove(d);
						}
						
						synchronized (mDciv) {
							DirectionCiv dc = mDciv.get(userId);
							if(dc != null){
								mDciv.remove(userId);
								needReload = true;
							}
						}
					} else {
						// 加入多车同行
						LocationData d = mCacheLd.get(userId);
						if (d != null) {
							// 之前存在，可能是更新操作
							mCacheLd.remove(d);
							if (ld.isGps) {
								d.latitudeE6 = ld.latitudeE6;
								d.longitudeE6 = ld.longitudeE6;
							} else {
								needReload = true;
								d = ld;
							}
						} else {
							needReload = true;
							d = ld;
						}

						mCacheLd.put(userId, d);
						mAllUserId.add(userId);
						
						synchronized (mDciv) {
							final DirectionCiv dc = mDciv.get(userId);
							if(dc != null){
								// 存在该用户
//								mDciv.remove(userId);
//								WTDUtil.setDirectionDiv(dc, ld.imagePath);
//								
//								mDciv.put(userId, dc);
							}else {
								DirectionCiv c = WTDUtil.generateDc(Color.parseColor("#34bfff"), ld.rotateDegress, ld.imagePath, ld.userId);
								mDciv.put(userId, c);
							}
						}
					}
				}
			}
		}
		
		if(needReload){
			setNeedReload(true);
		}
	}
	
	/**
	 * 获取所有的定位数据
	 * @return
	 */
	public List<LocationData> getLocationDataList(){
		Log.d("MultiNav", "MultiNav -------- > DataManager getLocationDataList -- > mCacheLd size is:"+mCacheLd.size());
		List<LocationData> ldList = new ArrayList<LocationData>();
		synchronized (mCacheLd) {
			Set<Entry<String, LocationData>> set = mCacheLd.entrySet();
			for(Entry<String, LocationData> entry:set){
				ldList.add(entry.getValue());
			}
		}
		
		return ldList;
	}
	
	/**
	 * 得到所有的界面对象
	 * @return
	 */
	public List<DirectionCiv> getAllDic(){
		List<DirectionCiv> dcList = new ArrayList<DirectionCiv>();
		synchronized (mDciv) {
			Set<Entry<String, DirectionCiv>> set = mDciv.entrySet();
			for(Entry<String, DirectionCiv> entry:set){
				dcList.add(entry.getValue());
			}
		}
		return dcList;
	}
	
	/**
	 * 根据用户id取得DC
	 * @param uid
	 * @return
	 */
	public DirectionCiv getDcByUserId(String uid){
		synchronized (mDciv) {
			return mDciv.get(uid);
		}
	}
	
	public void test(){
		
		LocationData ld1 = new LocationData();
		ld1.userId = "1001";
		ld1.isGps = false;
		ld1.latitudeE6 = 2253698;
		ld1.longitudeE6 = 11395635;
		ld1.rotateDegress = 0;
		ld1.imagePath = "http://imgq.duitang.com/uploads/item/201504/30/20150430124048_HsSdU.jpeg";
		LocationData ld2 = new LocationData();
		ld2.userId = "1002";
		ld2.isGps = false;
		ld2.latitudeE6 = 2253834;
		ld2.longitudeE6 = 11395595;
		ld2.rotateDegress = 0;
		ld2.imagePath = "http://img5.duitang.com/uploads/item/201502/17/20150217114935_hQZNk.jpeg";
		LocationData ld3 = new LocationData();
		ld3.userId = "1003";
		ld3.isGps = false;
		ld3.latitudeE6 = 22543645;
		ld3.longitudeE6 = 113452390;
		ld3.rotateDegress = 0;
		ld3.imagePath = "http://img5.duitang.com/uploads/item/201506/20/20150620220803_3uXnc.jpeg";
//		LocationData ld4 = new LocationData();
//		ld4.userId = "1004";
//		ld4.isGps = false;
//		ld4.latitudeE6 = 2253754;
//		ld4.longitudeE6 = 11395595;
//		ld4.rotateDegress = 0;
//		ld4.imagePath = "http://img5.duitang.com/uploads/item/201502/17/20150217114935_hQZNk.jpeg";
//		LocationData ld5 = new LocationData();
//		ld5.userId = "1005";
//		ld5.isGps = false;
//		ld5.latitudeE6 = 22543845;
//		ld5.longitudeE6 = 113952390;
//		ld5.rotateDegress = 0;
//		ld5.imagePath = "http://img5.duitang.com/uploads/item/201506/20/20150620220803_3uXnc.jpeg";
		
		final List<LocationData> l = new ArrayList<LocationData>();
		l.add(ld1);
		l.add(ld2);
		l.add(ld3);
//		l.add(ld4);
//		l.add(ld5);
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					setUpLocationData(l);
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
		
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				while (true) {
					for(int i = 0;i<l.size();i++){
						LocationData ld = l.get(i);
						updateLocationData(ld.userId,ld);
					}
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
					}
				}
			}
		}).start();
		
		NavManager.getInstance().setIsMultiNav(true);
	}
	
	// 更新用户信息
	Runnable updateDetail = new Runnable() {
		
		@Override
		public void run() {
			while(isMultiNav){
				Log.d("update", "update  ---- > Detail roomid is:"+mRoomId);
				try {
					NavManager.getInstance().getMultiDetailMemInfoList(mRoomId);
					Thread.sleep(10000);
				} catch (InterruptedException e) {
				}
			}
		}
	};
	
	// 更新GPS信息
	Runnable updateGps = new Runnable() {
		
		@Override
		public void run() {
			while(isMultiNav){
				Log.d("update", "update  ---- > Gps roomid is:"+mRoomId);
				try {
					NavManager.getInstance().getMultiGPSMemInfoList(mRoomId);
					Thread.sleep(1000);
				} catch (InterruptedException e) {
				}
			}
		}
	};
	
	/**
	 * 开始
	 */
	public void begin(){
		cancelBackground();
		beginRefreshDetail();
		beginRefreshGps();
		
		PreferenceUtil.setIsMultiNav(true);
		PreferenceUtil.saveRoomId(mRoomId);
	}
	
	/**
	 * 结束
	 */
	public void end(){
		cancelBackground();
		PreferenceUtil.setIsMultiNav(false);
	}
	
	private Handler updateHandler;
	private Handler gpsHandler;
	private HandlerThread update;
	private HandlerThread gps;
	
	private void initThread(){
		update = new HandlerThread("udpateThread");
		update.start();
		updateHandler = new Handler(update.getLooper());
		gps = new HandlerThread("gpsThread");
		gps.start();
		gpsHandler = new Handler(gps.getLooper());
	}
	
//	private void quit(){
//		gps.quit();
//		update.quit();
//		gps = null;
//		update = null;
//		gpsHandler = null;
//		updateHandler = null;
//	}
	
	private void beginRefreshDetail(){
//		MyApplication.getApp().runOnBackGround(updateDetail, 5000);
		updateHandler.postDelayed(updateDetail, 10000);
	}
	
	private void beginRefreshGps(){
//		MyApplication.getApp().runOnBackGround(updateGps, 100);
		gpsHandler.postDelayed(updateGps, 1000);
	}
	
	private void cancelBackground(){
//		MyApplication.getApp().removeBackGroundCallback(updateGps);
//		MyApplication.getApp().removeBackGroundCallback(updateDetail);
		updateHandler.removeCallbacks(updateDetail);
		gpsHandler.removeCallbacks(updateGps);
	}
}