package com.txznet.nav.manager;

import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.NaviInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.nav.ServiceRequest;
import com.txznet.nav.data.UserNaviGpsInfo;
import com.txznet.nav.helper.NavInfoCache;
import com.txznet.nav.helper.OverlayHelper;

/**
 * TODO Refactor
 *
 */
public class NaviDataManager {
	private static NaviDataManager instance = new NaviDataManager();

	private Vector<String> mLastExsitUserIdList = new Vector<String>();
	private Vector<String> mCurrentExsitUserIdList = new Vector<String>();

	private ConcurrentHashMap<String, String> mUserIdToUserName = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, String> mUserIdToUserImage = new ConcurrentHashMap<String, String>();
	private ConcurrentHashMap<String, UserNaviGpsInfo> mUserIdToNaviInfo = new ConcurrentHashMap<String, UserNaviGpsInfo>();

	private volatile int mRemainTime;
	private volatile int mRemainDistance;
	private volatile AMapNaviPath mNaviPath;

	private NaviDataManager() {
	}

	public static NaviDataManager getInstance() {
		return instance;
	}

	/**
	 * 解析获取联系人
	 * 
	 * @param geoDatas
	 */
	public void parseGeoDatas(List<UserNaviGpsInfo> geoDatas) {
		mCurrentExsitUserIdList.clear();
		for (UserNaviGpsInfo gd : geoDatas) {
			LogUtil.logd(" --->" + gd.toString());
			mCurrentExsitUserIdList.add(gd.userId);
			mUserIdToNaviInfo.put(gd.userId, gd);
			if (gd.nickName.contains("用户")) {
				continue;
			}

			if (mUserIdToUserName.containsKey(gd.userId)) {
				continue;
			}

			mUserIdToUserName.put(gd.userId, gd.nickName);
		}

		for (String uid : mLastExsitUserIdList) {
			if (mCurrentExsitUserIdList.contains(uid)) {
				invokeUpdateExistResource(uid);
			} else {
				invokeRemovedResource(uid);
			}
		}

		for (String uid : mCurrentExsitUserIdList) {
			if (mLastExsitUserIdList.contains(uid)) {
				continue;
			} else {
				invokeAddResource(uid);
			}
		}

		mLastExsitUserIdList.clear();
		mLastExsitUserIdList.addAll(mCurrentExsitUserIdList);
	}

	/**
	 * 更新本地存在的资源
	 * 
	 * @param uid
	 */
	private void invokeUpdateExistResource(String uid) {
		UserNaviGpsInfo d = mUserIdToNaviInfo.get(uid);
		if (d != null) {
			OverlayManager.getInstance().updateMarker(uid, d.imagePath, d.lat,
					d.lng, d.direction);

			// 更新用户的规划路径
			OverlayHelper.getInstance().addOverlay(d.naviPath);
		}
	}

	/**
	 * 移除当前不存在的资源
	 * 
	 * @param uid
	 */
	private void invokeRemovedResource(String uid) {
		boolean success = OverlayManager.getInstance().removeMarker(uid);
		if (success) {
			mLastExsitUserIdList.remove(uid);
		}

		// 删除用户的路径规划
		OverlayHelper.getInstance().removeUid(uid);
	}

	/**
	 * 添加新的资源
	 * 
	 * @param uid
	 */
	private void invokeAddResource(String uid) {
		UserNaviGpsInfo ungi = mUserIdToNaviInfo.get(uid);
		if (ungi != null) {
			OverlayManager.getInstance().addMarker(uid, ungi.imagePath,
					ungi.lat, ungi.lng, (float) ungi.direction);
			mUserIdToUserImage.put(uid, ungi.imagePath);

			// 增加一个用户的路径规划
			OverlayHelper.getInstance().addOverlay(ungi.naviPath);
		}
	}

	/**
	 * 增加一个用户
	 * 
	 * @param geoData
	 */
	public void addUser(UserNaviGpsInfo geoData) {
		if (mLastExsitUserIdList.contains(geoData.userId)) {
			OverlayManager.getInstance().updateMarker(geoData.userId, null,
					geoData.lat, geoData.lng, geoData.direction);
		} else {
			boolean success = OverlayManager.getInstance().addMarker(
					geoData.userId, geoData.imagePath, geoData.lat,
					geoData.lng, (float) geoData.direction);
			if (success) {
				mLastExsitUserIdList.add(geoData.userId);
			}
		}
	}

	/**
	 * 删除一个用户
	 * 
	 * @param uid
	 */
	public void removeUser(String uid) {
		boolean success = OverlayManager.getInstance().removeMarker(uid);
		if (success) {
			mLastExsitUserIdList.remove(uid);
		}
	}

	public void destoryAll() {
		mCurrentExsitUserIdList.clear();
		mLastExsitUserIdList.clear();
		mUserIdToNaviInfo.clear();
		mUserIdToUserName.clear();
		mUserIdToUserImage.clear();
	}

	private NaviInfo mNaviInfo;

	/**
	 * 更新NaviInfo信息
	 * 
	 * @param naviInfo
	 */
	public void updateNaviInfo(NaviInfo naviInfo) {
		this.mNaviInfo = naviInfo;
		int time = naviInfo.getPathRetainTime();
		int distance = naviInfo.getPathRetainDistance();
		updateRemainValue(distance, time);

		if (NavManager.getInstance().isAllowUpdateNaviInfo()) {
			ServiceRequest.sendNaviInfo(mNaviInfo);
		}
	}

	/**
	 * 超过30秒没用动静，则保存该剩余距离
	 * 
	 * @param distance
	 * @param time
	 */
	public void updateRemainValue(int distance, int time) {
		mRemainDistance = distance;
		mRemainTime = time;

		procLoop();
		if (mRemoveRemainTaskRunnable == null) {
			mRemoveRemainTaskRunnable = new Runnable() {

				@Override
				public void run() {
					AppLogic.removeBackGroundCallback(
							mSaveRemainTaskRunnable);
					mSaveRemainTaskRunnable = null;
				}
			};
		}

		AppLogic.removeBackGroundCallback(
				mRemoveRemainTaskRunnable);
		AppLogic
				.runOnBackGround(mRemoveRemainTaskRunnable, 30000);
	}

	public void updateNaviPath(AMapNaviPath naviPath) {
		this.mNaviPath = naviPath;
	}

	public AMapNaviPath getNaviPath() {
		return mNaviPath;
	}

	public int getRemainDistance() {
		return mRemainDistance;
	}

	public int getRemainTime() {
		return mRemainTime;
	}

	/**
	 * 获取当前所有的用户id
	 * 
	 * @return
	 */
	public List<String> getAllUserList() {
		return mCurrentExsitUserIdList;
	}

	/**
	 * 根据用户id取得剩余距离
	 * 
	 * @param uid
	 * @return
	 */
	public int getDistanceByUserId(String uid) {
		UserNaviGpsInfo data = mUserIdToNaviInfo.get(uid);
		if (data != null) {
			return data.distance;
		}

		return 0;
	}

	/**
	 * 根据用户Id取得剩余时间
	 * 
	 * @param uid
	 * @return
	 */
	public int getTimeByUserId(String uid) {
		UserNaviGpsInfo data = mUserIdToNaviInfo.get(uid);
		if (data != null) {
			return data.time;
		}

		return 0;
	}

	public String getUserNickName(String uid) {
		if (mUserIdToUserName.containsKey(uid)) {
			return mUserIdToUserName.get(uid);
		}
		return "";
	}

	public String getUserImagePath(String uid) {
		if (mUserIdToUserImage.containsKey(uid)) {
			return mUserIdToUserImage.get(uid);
		}
		return "";
	}

	Runnable mSaveRemainTaskRunnable = null;

	/**
	 * 保存剩余距离，异常恢复
	 */
	private void procLoop() {
		if (mSaveRemainTaskRunnable == null) {
			mSaveRemainTaskRunnable = new Runnable() {

				@Override
				public void run() {
					LogUtil.logd("procLoop setRemainDistance");
					NavInfoCache.getInstance().setRemainDistance(
							getRemainDistance());
					AppLogic.removeBackGroundCallback(
							mSaveRemainTaskRunnable);
					AppLogic.runOnBackGround(
							mSaveRemainTaskRunnable, 10000);
				}
			};
			AppLogic.runOnBackGround(mSaveRemainTaskRunnable, 0);
		}
	}

	Runnable mRemoveRemainTaskRunnable = null;
}