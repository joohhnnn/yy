package com.txznet.nav.manager;

import java.util.List;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.ServerPushNavigateInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.ServiceRequest;
import com.txznet.nav.data.UserNaviGpsInfo;
import com.txznet.nav.helper.OverlayHelper;
import com.txznet.nav.helper.OverlayImageSelector;
import com.txznet.nav.ui.NavViewActivity;
import com.txznet.nav.util.AnimationUtil;

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

	volatile UserNaviGpsInfo mMyGeoData;

	private static MultiNavManager instance = new MultiNavManager();

	public static MultiNavManager getInstance() {
		return instance;
	}

	private MultiNavManager() {
	}

	private void ensureHandlerThread() {
		updateThread = new HandlerThread("update-thread");
		updateThread.start();
		updateHandler = new Handler(updateThread.getLooper());
	}

	public void startNavigate(NavigateInfo navigateInfo) {
		this.mNavigateInfo = navigateInfo;
		if (mNavigateInfo == null) {
			return;
		}

		ServerPushNavigateInfo spni = mNavigateInfo.msgServerPushInfo;
		if (spni == null) {
			return;
		}

		if (spni.uint32Type != 1) {
			return;
		}

		mUserId = spni.uint64FromUid;

		createMyGeoData(mNavigateInfo);

		reqBegin();
	}

	private void createMyGeoData(NavigateInfo navigateInfo) {
		if (mMyGeoData != null) {
			mMyGeoData = null;
		}

		mMyGeoData = new UserNaviGpsInfo();
		mMyGeoData.imagePath = navigateInfo.msgServerPushInfo.strToFaceUrl;
		mMyGeoData.nickName = navigateInfo.msgServerPushInfo.strToWxNick;
		mMyGeoData.userId = String.valueOf(mUserId);
		mMyGeoData.isGpsOnly = false;

		LocationInfo info = NavManager.getInstance().getLocationInfo();
		if (info == null) {
			return;
		}

		GpsInfo gps = info.msgGpsInfo;
		if (gps == null) {
			return;
		}

		double alt = 0;
		float dir = 0, rad = 0, spd = 0;

		if (gps.dblAltitude != null) {
			alt = gps.dblAltitude;
		}

		if (gps.fltDirection != null) {
			dir = gps.fltDirection;
		}

		if (gps.fltRadius != null) {
			rad = gps.fltRadius;
		}

		if (gps.fltSpeed != null) {
			spd = gps.fltSpeed;
		}

		mMyGeoData.lat = gps.dblLat;
		mMyGeoData.lng = gps.dblLng;
		mMyGeoData.altitude = alt;
		mMyGeoData.direction = dir;
		mMyGeoData.radius = rad;
		mMyGeoData.speed = spd;
	}

	public UserNaviGpsInfo getMyGeoData() {
		return mMyGeoData;
	}

	/**
	 * 设置多车同行的相关参数
	 * 
	 * @param roomId
	 * @param navType
	 */
	public void preBegin(long roomId, int navType) {
		this.mRoomId = roomId;
		this.mNavType = navType;
	}

	/**
	 * 开始多车同行
	 */
	public void begin() {
		if (mRoomId == 0 || mRoomId == -1) {
			return;
		}

		isNav = true;

		if (updateHandler == null) {
			ensureHandlerThread();
		}

		updateHandler.removeCallbacks(uploadRunnable);
		updateHandler.postDelayed(uploadRunnable, 5000);
	}

	/**
	 * 结束多车同行
	 */
	public void end() {
		if (!NavManager.getInstance().isMultiNav()) {
			return;
		}

		mRoomId = -1;
		mNavType = -1;
		isNav = false;
		
		if(updateHandler != null){
			updateHandler.removeCallbacks(uploadRunnable);
		}
		

		NaviDataManager.getInstance().destoryAll();
		AnimationUtil.getInstance().clear();
		OverlayImageSelector.getInstance().destory();
		OverlayManager.getInstance().destoryAllMarker();
		OverlayHelper.getInstance().destory();

		AppLogic.finishActivity(NavViewActivity.class);
		LogUtil.logd("结束多车同行，已清除相关数据");
	}

	/**
	 * @param gdList
	 */
	public void updateMemberList(List<UserNaviGpsInfo> gdList) {
		try {
			NaviDataManager.getInstance().parseGeoDatas(gdList);
		} catch (Exception e) {
			Log.e(TAG, TAG + " -- > 更新联系人列表发生异常！");
		}
	}

	/**
	 * 加入多车同行
	 * 
	 * @param geoData
	 */
	public void join(UserNaviGpsInfo geoData) {
		NaviDataManager.getInstance().addUser(geoData);
	}

	/**
	 * uid退出多车同行
	 * 
	 * @param uid
	 */
	public void quit(String uid) {
		NaviDataManager.getInstance().removeUser(uid);
	}

	/**
	 * 请求开始多车同行
	 */
	public void reqBegin() {
		if (mNavigateInfo == null) {
			Log.e(TAG, TAG + " -- > 请求开始失败，NavigateInfo 为空！");
			return;
		}

		ServiceRequest.sendBeginInvoke(mNavigateInfo);
	}

	/**
	 * 请求结束多车同行
	 */
	public void reqEnd() {
		if (!isNav) {
			LogUtil.logd("当前不处于多车同行！");
			return;
		}

		if (mNavigateInfo == null) {
			LogUtil.loge(" -- > 请求结束失败，NavigateInfo 为空！");
			return;
		}

		ServiceRequest.sendEndInvoke(mNavigateInfo);
	}

	/**
	 * 请求获取好友 param isGps 是否只包含GPS信息
	 */
	public void reqMemberList(boolean isGps) {
		if (!isNav || mRoomId == -1 || mRoomId == 0) {
			LogUtil.loge(" -- > 请求获取好友失败，没有开始导航！");
			return;
		}

		int time = NaviDataManager.getInstance().getRemainTime();
		int distance = NaviDataManager.getInstance().getRemainDistance();
		if (isGps) {
			ServiceRequest.sendGetMemberListInfo(mRoomId, 1, distance, time);
		} else {
			ServiceRequest.sendGetMemberListInfo(mRoomId, 0, distance, time);
		}
	}

	/**
	 * 更新我的位置
	 * 
	 * @param locData
	 */
	public void updateMyLoc(double lat, double lng, double acc, double alt,
			double bear) {
		if (!isNav) {
			return;
		}

		if (mMyGeoData == null) {
			createMyGeoData(mNavigateInfo);
		}

		mMyGeoData.altitude = alt;
		mMyGeoData.direction = bear;
		mMyGeoData.lat = lat;
		mMyGeoData.lng = lng;
	}

	Runnable uploadRunnable = new Runnable() {
		public void run() {
			int count = 0;
			reqMemberList(false);
			while (isNav) {
				try {
					Thread.sleep(1000);
					if (count % 15 == 0 && count != 0) {
						reqMemberList(false);
						count = 0;
					} else if (count % 3 == 0 && count != 0) {
						reqMemberList(true);
					}

					count++;
				} catch (InterruptedException e) {
				}
			}
		}
	};

	public NavigateInfo getNavigateInfo() {
		return mNavigateInfo;
	}

	public void setRoomId(long rid) {
		mRoomId = rid;
	}

	public long getRoomId() {
		return mRoomId;
	}

	public void setNavType(int type) {
		mNavType = type;
	}

	public int getNavType() {
		return mNavType;
	}

	public boolean isNav() {
		return isNav;
	}

	public long getMyId() {
		return mUserId;
	}
}