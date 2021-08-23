package com.txznet.txz.module.nav;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.report_manager.ReportManager.Req_ReportNavInfoList;
import com.txz.report_manager.ReportManager.Resp_ReportNavInfoList;
import com.txz.ui.data.UiData.UserConfig;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavInfo;
import com.txz.ui.map.UiMap.NavInfoList;
import com.txz.ui.map.UiMap.NavPointInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.txz.db.DBInfo;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;
import com.txznet.txz.util.DatabaseCache;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.text.TextUtils;
import android.util.SparseArray;

/**
 * 与后台实现导航历史数据的同步
 */
public class NavInscriber {
	private class HandleTask {
		public int hideStatus = 0;
		public NavInfo navInfo;
	}

	// 放在内存中等待更新状态的数据
	private List<HandleTask> mWaitHandleList = new ArrayList<HandleTask>();
	private List<HandleTask> mWaitUploadList = new ArrayList<HandleTask>();

	private NavInfoCache mNavInfoCache;
	private static NavInscriber sSyncor = new NavInscriber();

	public static NavInscriber getInstance() {
		return sSyncor;
	}

	public void init(Context context, int size) {
		if (mNavInfoCache == null) {
			mNavInfoCache = new NavInfoCache(context, size);
		}
	}

	public NavInfoCache rebuildCache(Context context, int size) {
		init(context, size);
		mNavInfoCache.clear();
		mNavInfoCache.setNumSize(size);
		mNavInfoCache.autoRecompute();
		return mNavInfoCache;
	}

	public NavInfoCache getCache() {
		return mNavInfoCache;
	}

	public void handleData(byte[] data) {
		if (data == null) {
			return;
		}

		try {
			Resp_ReportNavInfoList resp = Resp_ReportNavInfoList.parseFrom(data);
			if (resp == null) {
				LogUtil.loge("handleData resp:" + resp);
				return;
			}
			handleInner(resp);
		} catch (InvalidProtocolBufferNanoException e) {
			e.printStackTrace();
		}
	}

	private void handleInner(Resp_ReportNavInfoList resp) {
		removeUpdateDatabase();

		Integer syncNo = resp.uint32SyncNo;
		Boolean hasMore = resp.boolHasMore;
		NavInfoList infoList = resp.msgNavInfoList;
		LogUtil.logd("syncNo:" + syncNo + ",hasMore:" + hasMore + ",infoList:" + infoList);

		updateServerList(infoList);
		if (hasMore != null && hasMore) {
			requestFromServer();
		}

		if (syncNo != null) {
			PreferenceUtil.getInstance().setNavHistorySyncNo(syncNo);
			onUploadRespone(true);
		} else {
			onUploadRespone(false);
		}

		notifyUpdateDatabase(0);
	}

	private void updateServerList(NavInfoList list) {
		if (list == null || list.rptNavInfo == null) {
			return;
		}

		NavInfo[] navInfos = list.rptNavInfo;
		synchronized (mWaitHandleList) {
			for (NavInfo info : navInfos) {
				printNavInfo(info);
				if (!correctNavInfo(info)) {
					continue;
				}
				HandleTask task = new HandleTask();
				task.navInfo = info;
				mWaitHandleList.add(task);
			}
		}
	}

	private boolean correctNavInfo(NavInfo navInfo) {
		NavPointInfo bInfo = navInfo.msgBeginAddress;
		NavPointInfo eInfo = navInfo.msgEndAddress;
		if (bInfo == null || eInfo == null) {
			LogUtil.loge("updateServerList bInfo:" + bInfo + ",eInfo:" + eInfo);
			return false;
		}
		if (bInfo.msgGpsInfo == null || eInfo.msgGpsInfo == null) {
			LogUtil.loge(
					"updateServerList bInfo.msgGpsInfo:" + bInfo.msgGpsInfo + ",eInfo.msgGpsInfo:" + eInfo.msgGpsInfo);
			return false;
		}
		if (bInfo.msgGpsInfo.dblLat == null
				|| bInfo.msgGpsInfo.dblLat == 0
				|| eInfo.msgGpsInfo.dblLng == null
				|| eInfo.msgGpsInfo.dblLng == 0) {
			LogUtil.loge("updateServerList bInfo.msgGpsInfo.dblLat:" + bInfo.msgGpsInfo.dblLat
					+ ",eInfo.msgGpsInfo.dblLng:" + eInfo.msgGpsInfo.dblLng);
			return false;
		}
		return true;
	}

	private void printNavInfo(NavInfo navInfo) {
		LogUtil.logd("add Server record:" + navInfo.uint32Time + "," + navInfo.uint32Status + ","
				+ navInfo.msgBeginAddress.strPoiName + "," + navInfo.msgBeginAddress.strPoiAddress + ","
				+ navInfo.msgEndAddress.strPoiName + "," + navInfo.msgEndAddress.strPoiAddress);
	}

	private List<DbNavInfo> mCacheInfos = null;

	public List<DbNavInfo> getNavHistorys(int size) {
		if (mNavInfoCache != null) {
			mCacheInfos = mNavInfoCache.queryTopRecord(size);
			return mCacheInfos;
		}
		return null;
	}

	public void removeNavHistory(List<String> filters) {
		if (mCacheInfos == null || mCacheInfos.isEmpty() || filters == null || filters.isEmpty()) {
			return;
		}
		for (String id : filters) {
			for (DbNavInfo info : mCacheInfos) {
				if (id.equals(info.beginSameField + info.endSameField)) {
					removeDestRecord(info);
				}
			}
		}
	}

	/**
	 * 上报后根据返回是否成功写入数据库
	 *
	 * @param bSucc
	 */
	private void onUploadRespone(boolean bSucc) {
		if (bSucc) {
			synchronized (mWaitHandleList) {
				synchronized (mWaitUploadList) {
					for (HandleTask hTask : mWaitUploadList) {
						if (mWaitHandleList.contains(hTask)) {
						} else {
							mWaitHandleList.add(hTask);
						}
					}

					mWaitUploadList.clear();
				}

				for (HandleTask task : mWaitHandleList) {
					// 成功后本地标记位已同步
					task.navInfo.uint32Status &= ~UiMap.ADDED;
				}
			}
		}
	}

	/**
	 * 同步后台数据
	 *
	 * @return
	 */
	private List<DbNavInfo> requestFromServer() {
		upload_inner(null, PreferenceUtil.getInstance().getNavHistorySyncNo());
		return null;
	}

	/**
	 * 用来标识微信发来的导航
	 */
	private PathInfo mWxPathInfo;

	public void addRecordFromWx(PathInfo navInfo) {
		this.mWxPathInfo = navInfo;
		addRecord(mWxPathInfo);
	}

	/**
	 * 新增一条历史记录
	 *
	 * @param navInfo
	 */
	public void addRecord(PathInfo navInfo) {
		addRecord(navInfo, true);
	}

	public boolean needReseveSalName(String name) {
		if (TextUtils.isEmpty(name)) {
			return true;
		}
		String[] resevesal_list = NativeData.getResStringArray("RS_POIINFO_RESEVESAL_LIST");
		if (resevesal_list == null) {
			return false;
		}
		for (String revName : resevesal_list) {
			if (revName.equals(name)) {
				return true;
			}
		}
		return false;
	}

	private Runnable mAddTask = null;

	/**
	 * 用于过滤微信发来地址和导航通知导航开始的地址
	 *
	 * @param info1
	 * @param info2
	 * @return
	 */
	private boolean isSamePathInfo(PathInfo info1, PathInfo info2) {
		int distance = BDLocationUtil.calDistance(info1.toPoiLat, info1.toPoiLng, info2.toPoiLat, info2.toPoiLng);
		if (distance < 100) {
			return true;
		}
		return false;
	}

	public boolean isSameDestinationLatLng(double sLat, double sLng, double dLat, double dLng) {
		int distance = BDLocationUtil.calDistance(sLat, sLng, dLat, dLng);
		LogUtil.logd("isSameDestination distance:" + distance);
		if (distance < 100) {
			return true;
		}
		return false;
	}

	/**
	 * 新增一条历史记录
	 *
	 * @param navInfo
	 * @param upLoad
	 */
	public void addRecord(final PathInfo navInfo, final boolean upLoad) {
		LogUtil.logd("addHistoryRecord:" + navInfo);
		if (navInfo == null) {
			return;
		}

		if (mWxPathInfo != null && mWxPathInfo != navInfo) {
			if (isSamePathInfo(mWxPathInfo, navInfo)) {
				mWxPathInfo = null;
				LogUtil.loge("filter same PathInfo！");
				return;
			}
		}

		mAddTask = new Runnable() {
			private boolean hasRun;

			@Override
			public void run() {
				AppLogic.removeBackGroundCallback(mAddTask);
				mAddTask = null;
				LogUtil.logd("addTask hasRun");
				if (hasRun) {
					return;
				}
				hasRun = true;

				// 起点
				GpsInfo sGpsInfo = new GpsInfo();
				sGpsInfo.dblLat = navInfo.fromPoiLat;
				sGpsInfo.dblLng = navInfo.fromPoiLng;
				NavPointInfo sPointInfo = new NavPointInfo();
				sPointInfo.strPoiAddress = navInfo.fromPoiAddr;
				sPointInfo.strPoiName = navInfo.fromPoiName;
				sPointInfo.msgGpsInfo = sGpsInfo;

				// 终点
				GpsInfo eGpsInfo = new GpsInfo();
				eGpsInfo.dblLat = navInfo.toPoiLat;
				eGpsInfo.dblLng = navInfo.toPoiLng;
				NavPointInfo ePointInfo = new NavPointInfo();
				ePointInfo.strPoiAddress = navInfo.toPoiAddr;
				ePointInfo.strPoiName = navInfo.toPoiName;
				ePointInfo.msgGpsInfo = eGpsInfo;

				NavInfo sInfo = new NavInfo();
				sInfo.msgBeginAddress = sPointInfo;
				sInfo.msgEndAddress = ePointInfo;
				sInfo.uint32Time = NativeData.getServerTime();
				sInfo.uint32Status = UiMap.ADDED;

				HandleTask task = new HandleTask();
				task.navInfo = sInfo;
				LogUtil.logd("will upload fromName:" + navInfo.fromPoiName + ",fromAddr:" + navInfo.fromPoiAddr
						+ ",toName:" + navInfo.toPoiName + ",toAddress:" + navInfo.toPoiAddr);

				addHandleRecord(task);
				if (upLoad) {
					uploadToServer(0);
				}
			}
		};
		if (navInfo.fromPoiLat == 0 && navInfo.fromPoiLng == 0 && navInfo.toPoiLat == 0 && navInfo.toPoiLng == 0) {
			JNIHelper.loge("ignore invalid navInfo！");
			return;
		}

		boolean bWait = reverseGeocode(navInfo, mAddTask);
		if (bWait) {
			// 5S还查不出来的话将数据保存
			AppLogic.runOnBackGround(mAddTask, 5 * 1000);
		} else {
			if (mAddTask != null) {
				mAddTask.run();
				mAddTask = null;
			}
		}
	}

	/**
	 * 替换GPS的别名
	 *
	 * @param lat
	 * @param lng
	 * @return
	 */
	private String getGPSPoiName(double lat, double lng) {
		UserConfig userConfig = NativeData.getCurUserConfig();
		if (userConfig == null || userConfig.msgNetCfgInfo == null) {
			return "";
		}

		NavigateInfo navigateInfo = userConfig.msgNetCfgInfo.msgHomeLoc;
		if (navigateInfo != null && navigateInfo.msgGpsInfo != null) {
			if (isSameDestinationLatLng(lat, lng, navigateInfo.msgGpsInfo.dblLat, navigateInfo.msgGpsInfo.dblLng)) {
				return "家";
			}
		}

		navigateInfo = userConfig.msgNetCfgInfo.msgCompanyLoc;
		if (navigateInfo != null && navigateInfo.msgGpsInfo != null) {
			if (isSameDestinationLatLng(lat, lng, navigateInfo.msgGpsInfo.dblLat, navigateInfo.msgGpsInfo.dblLng)) {
				return "公司";
			}
		}

		if (userConfig.msgNetCfgInfo.msgFavouriteLocs == null
				|| userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem == null
				|| userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem.length < 1) {
			return "";
		}
		NavigateInfo[] infos = userConfig.msgNetCfgInfo.msgFavouriteLocs.rptMsgItem;
		for (NavigateInfo info : infos) {
			if (info != null && info.msgGpsInfo != null) {
				if (isSameDestinationLatLng(lat, lng, info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng)) {
					return info.strBakName;
				}
			}
		}
		return "";
	}

	/**
	 * 逆地理编码
	 *
	 * @param pathInfo
	 * @param endRun
	 */
	public boolean reverseGeocode(final PathInfo pathInfo, final Runnable endRun) {
		boolean bWait = false;
		if (pathInfo != null) {
			String poiName = getGPSPoiName(pathInfo.fromPoiLat, pathInfo.fromPoiLng);
			if (!TextUtils.isEmpty(poiName)) {
				pathInfo.fromPoiName = poiName;
				LogUtil.logd("fromPoiName:" + poiName);
			}

			poiName = getGPSPoiName(pathInfo.toPoiLat, pathInfo.toPoiLng);
			if (!TextUtils.isEmpty(poiName)) {
				pathInfo.toPoiName = poiName;
				LogUtil.logd("toPoiName:" + poiName);
			}
		}
		if (needReseveSalName(pathInfo.fromPoiName) || needReseveSalName(pathInfo.fromPoiAddr)) {
			LogUtil.logd("reseveSal pathInfo.fromPoiLat" + pathInfo.fromPoiLat + ",pathInfo.fromPoiLng" + pathInfo.fromPoiLng);
			LogUtil.logd("reseveSal pathInfo.fromPoiName:"+pathInfo.fromPoiName+",pathInfo.fromPoiAddr:" + pathInfo.fromPoiAddr);
			LocationManager.getInstance().reverseGeoCode(pathInfo.fromPoiLat, pathInfo.fromPoiLng,
					new OnGeocodeSearchListener() {

						@Override
						public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
							LogUtil.logd("fromPoi onRegeocodeSearched arg0:" + arg0 + ",arg1:" + arg1); // 32KEY鉴权失败
							if (arg0 == null) {
								return;
							}
							RegeocodeAddress regeocodeAddress = arg0.getRegeocodeAddress();
							if (regeocodeAddress != null) {
								String geoInfo = regeocodeAddress.getFormatAddress();
								LogUtil.logd("fromPoi onRegeocodeSearched geoInfo:" + geoInfo);
								String poiName = geoInfo;
								if (regeocodeAddress.getProvince() != null) {
									poiName = poiName.replace(regeocodeAddress.getProvince(), "");
								}
								if (regeocodeAddress.getCity() != null) {
									poiName = poiName.replace(regeocodeAddress.getCity(), "");
								}
								if (regeocodeAddress.getDistrict() != null) {
									poiName = poiName.replace(regeocodeAddress.getDistrict(), "");
								}
								if (regeocodeAddress.getTownship() != null) {
									poiName = poiName.replace(regeocodeAddress.getTownship(), "");
								}
								if (needReseveSalName(pathInfo.fromPoiAddr)) {
									pathInfo.fromPoiAddr = geoInfo;
								}
								if (needReseveSalName(pathInfo.fromPoiName)) {
									pathInfo.fromPoiName = poiName;
								}

								if (needReseveSalName(pathInfo.toPoiName) || needReseveSalName(pathInfo.toPoiAddr)) {
									// 还未查询完
								} else {
									LogUtil.logd("reverseGeocode endRun:" + endRun);
									if (endRun != null) {
										endRun.run();
									}
								}
							}
						}

						@Override
						public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
						}
					});
			bWait = true;
		}

		if (needReseveSalName(pathInfo.toPoiName) || needReseveSalName(pathInfo.toPoiAddr)) {
			LogUtil.logd("reseveSal pathInfo.toPoiLat" + pathInfo.toPoiLat + ",pathInfo.toPoiLng" + pathInfo.toPoiLng);
			LogUtil.logd("reseveSal pathInfo.toPoiName:"+pathInfo.toPoiName+",pathInfo.toPoiAddr:" + pathInfo.toPoiAddr);
			LocationManager.getInstance().reverseGeoCode(pathInfo.toPoiLat, pathInfo.toPoiLng,
					new OnGeocodeSearchListener() {

						@Override
						public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
							LogUtil.logd("toPoi onRegeocodeSearched arg0:" + arg0 + ",arg1:" + arg1);
							if (arg0 == null) {
								return;
							}
							RegeocodeAddress regeocodeAddress = arg0.getRegeocodeAddress();
							if (regeocodeAddress != null) {
								String geoInfo = regeocodeAddress.getFormatAddress();
								LogUtil.logd("toPoi onRegeocodeSearched geoInfo:" + geoInfo);
								String poiName = geoInfo;
								if (regeocodeAddress.getProvince() != null) {
									poiName = poiName.replace(regeocodeAddress.getProvince(), "");
								}
								if (regeocodeAddress.getCity() != null) {
									poiName = poiName.replace(regeocodeAddress.getCity(), "");
								}
								if (regeocodeAddress.getDistrict() != null) {
									poiName = poiName.replace(regeocodeAddress.getDistrict(), "");
								}
								if (regeocodeAddress.getTownship() != null) {
									poiName = poiName.replace(regeocodeAddress.getTownship(), "");
								}
								if (needReseveSalName(pathInfo.toPoiAddr)) {
									pathInfo.toPoiAddr = geoInfo;
								}
								if (needReseveSalName(pathInfo.toPoiName)) {
									pathInfo.toPoiName = poiName;
								}
								if (needReseveSalName(pathInfo.toCity)) {
									pathInfo.toCity = regeocodeAddress.getCity();
								}
								if (needReseveSalName(pathInfo.fromPoiName)
										|| needReseveSalName(pathInfo.fromPoiAddr)) {
									// 未完
								} else {
									LogUtil.logd("reverseGeocode endRun:" + endRun);
									if (endRun != null) {
										endRun.run();
									}
								}
							}
						}

						@Override
						public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
						}
					});
			bWait = true;
		}
		return bWait;
	}

	private void removeUpdateDatabase() {
		AppLogic.removeBackGroundCallback(mStoreDbTask);
	}

	private void notifyUpdateDatabase(long delay) {
		AppLogic.removeBackGroundCallback(mStoreDbTask);
		AppLogic.runOnBackGround(mStoreDbTask, delay);
	}

	Runnable mStoreDbTask = new Runnable() {

		@Override
		public void run() {
			store_inner();
		}
	};

	/**
	 * 保存到数据库
	 */
	private void store_inner() {
		LogUtil.logd("store_inner");
		NavInfoCache cache = mNavInfoCache;
		if (cache != null) {
			synchronized (mWaitHandleList) {
				if (!mWaitHandleList.isEmpty()) {
					LogUtil.logd("store_inner begin... size:" + mWaitHandleList.size());
					List<DbNavInfo> dbInfos = new ArrayList<DbNavInfo>();
					for (HandleTask task : mWaitHandleList) {
						NavInfo navInfo = task.navInfo;
						DbNavInfo info = new DbNavInfo();
						info.beginSameField = navInfo.msgBeginAddress.strPoiName
								+ navInfo.msgBeginAddress.strPoiAddress;
						info.endSameField = navInfo.msgEndAddress.strPoiName + navInfo.msgEndAddress.strPoiAddress;
						info.navInfo = task.navInfo;
						info.hideStatus = task.hideStatus;
						dbInfos.add(info);
					}

					if (!dbInfos.isEmpty()) {
						cache.store(dbInfos);
					}

					autoSyncRecord();

					LogUtil.logd("store_inner end");
					mWaitHandleList.clear();
				} else {
					LogUtil.logd("mWaitStoreList isEmpty");
				}
			}
		}
	}

	private void addHandleRecord(HandleTask sNavInfo) {
		if (sNavInfo != null && sNavInfo.navInfo != null) {
			LogUtil.logd("addHandleRecord:" + sNavInfo.navInfo.toString());
		}
		synchronized (mWaitHandleList) {
			mWaitHandleList.add(sNavInfo);
		}
		// 等待3S，没响应则记录到数据库中
		notifyUpdateDatabase(3000);
	}

	public void removeDestRecord(DbNavInfo navInfo) {
		clearActiveField(navInfo, UiMap.HIDE_END_ADDRESS);
		removeRecord(navInfo, true);
	}

	public void removeFromRecord(DbNavInfo navInfo) {
		clearActiveField(navInfo, UiMap.HIDE_BEGIN_ADDRESS);
		removeRecord(navInfo, true);
	}

	private void clearActiveField(DbNavInfo info, int activeStatus) {
		info.hideStatus = activeStatus;
		info.navInfo.uint32Status |= activeStatus;
		info.navInfo.uint32Status |= UiMap.ADDED;
	}

	/**
	 * 删除一条记录
	 *
	 * @param navInfo
	 * @param upLoad
	 */
	private void removeRecord(DbNavInfo navInfo, boolean upLoad) {
		HandleTask task = new HandleTask();
		task.navInfo = navInfo.navInfo;
		task.hideStatus = navInfo.hideStatus;
		synchronized (mWaitHandleList) {
			mWaitHandleList.add(task);
		}

		synchronized (mWaitUploadList) {
			mWaitUploadList.add(task);
		}
		LogUtil.logd("removeNavRecord:" + navInfo.navInfo.uint32Time + ",hideStatus:" + navInfo.hideStatus);

		if (upLoad) {
			upload_run();
		}

		store_inner();
	}

	Boolean mRemoveAll;

	/**
	 * 提交到后台
	 */
	private void uploadToServer(long delay) {
		AppLogic.removeBackGroundCallback(mUploadTask);
		AppLogic.runOnBackGround(mUploadTask, delay);
	}

	Runnable mUploadTask = new Runnable() {

		@Override
		public void run() {
			upload_run();
		}
	};

	private void upload_run() {
		synchronized (mWaitHandleList) {
			NavInfoList list = new NavInfoList();
			NavInfo[] navInfos = new NavInfo[mWaitHandleList.size()];
			for (int i = 0; i < navInfos.length; i++) {
				navInfos[0] = mWaitHandleList.get(i).navInfo;
			}
			list.rptNavInfo = navInfos;
			upload_inner(list, PreferenceUtil.getInstance().getNavHistorySyncNo());
		}
	}

	private void uploadNavInfoList(List<NavInfo> navInfos) {
		NavInfoList list = new NavInfoList();
		if (navInfos != null) {
			list.rptNavInfo = navInfos.toArray(new NavInfo[navInfos.size()]);
		}
		upload_inner(list, PreferenceUtil.getInstance().getNavHistorySyncNo());
	}

	/**
	 * 上报后台
	 *
	 * @param list
	 * @param syncNo
	 */
	private void upload_inner(NavInfoList list, int syncNo) {
		Req_ReportNavInfoList req = new Req_ReportNavInfoList();
		req.msgNavInfoList = list;
		req.uint32SyncNo = syncNo;

		int listSize = req.msgNavInfoList != null
				? req.msgNavInfoList.rptNavInfo != null ? req.msgNavInfoList.rptNavInfo.length : 0 : 0;
		LogUtil.logd("uploadToServer size:" + listSize + " syncNo:" + req.uint32SyncNo);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_REPORT_NAV_INFO, req);
	}

	/**
	 * 从数据库中取出没有准备好的数据上报
	 */
	public void reSyncHistory(int syncNo) {
		LogUtil.logd("reSyncNo:" + syncNo);
		mReSyncTask.update(syncNo);
		AppLogic.removeBackGroundCallback(mReSyncTask);
		AppLogic.runOnBackGround(mReSyncTask, 2 * 1000);
	}

	Runnable1<Integer> mReSyncTask = new Runnable1<Integer>(null) {

		@Override
		public void run() {
			reSync_Inner(mP1);
		}
	};

	private void reSync_Inner(Integer syncNo) {
		int sNo = PreferenceUtil.getInstance().getNavHistorySyncNo();
		if (syncNo == null || syncNo == sNo) {
			LogUtil.logw("reSync_Inner error mP1:" + syncNo + ",sNo:" + sNo);
			return;
		}

		LogUtil.logd("reSync_Inner:" + syncNo);
		reupload(true);
	}

	/**
	 * 重新同步没有同步的数据
	 */
	private void reupload(boolean emptyReq) {
		if (mNavInfoCache == null) {
			return;
		}
		LogUtil.logd("reupload nav history..." + emptyReq);
		List<DbNavInfo> navInfos = mNavInfoCache.queryNoReadys(UiMap.ADDED);
		if (navInfos != null) {
			List<NavInfo> upInfos = new ArrayList<NavInfo>();
			synchronized (mWaitUploadList) {
				for (DbNavInfo dbInfo : navInfos) {
					NavInfo info = dbInfo.navInfo;
					HandleTask task = new HandleTask();
					task.navInfo = info;
					mWaitUploadList.add(task);
					upInfos.add(info);
				}
			}

			uploadNavInfoList(upInfos);
		} else if (emptyReq) {
			requestFromServer();
		}
	}

	Runnable mAutoSyncTask = new Runnable() {

		@Override
		public void run() {
			LogUtil.logd("autoSync...");
			reupload(false);
		}
	};

	/**
	 * 自动同步没有提交的数据
	 */
	private void autoSyncRecord() {
		AppLogic.removeSlowGroundCallback(mAutoSyncTask);
		AppLogic.runOnSlowGround(mAutoSyncTask, 1 * 1000);
	}

	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

	/**
	 * 分页的数据库
	 *
	 * @param <T>
	 */

	public static abstract class PageCache<T> extends DatabaseCache<T> {
		protected String tableName;
		protected int mNumSize;
		protected int mCurrPage;
		protected int mPageSize;
		// 当前的起始位置
		protected int mCurrIndex;
		protected boolean mLastIsDestination = true;

		public PageCache(SQLiteCallback callback, PersistableResource<T> resource, int pageSize, String tableName) {
			super(callback, resource);
			this.mNumSize = pageSize;
			this.tableName = tableName;
		}

		public PageCache<T> toggleDestination(boolean isDestination) {
			LogUtil.logd("toggleDestination:" + isDestination);
			mLastIsDestination = isDestination;
			clear();
			return this;
		}

		protected void setNumSize(int numSize) {
			this.mNumSize = numSize;
		}

		protected void reset() {
			mCurrPage = 0;
			int tmp = getRecordSize(tableName) - mNumSize;
			mCurrIndex = Math.max(0, tmp);
		}

		/**
		 * 重新调整当前页
		 */
		protected void autoRecompute() {
			int totalSize = getRecordSize(tableName);
			// 得到总数的页数
			int pageSize = totalSize / mNumSize;
			int rSize = totalSize % mNumSize;
			if (rSize != 0) {
				pageSize++;
			}
			mPageSize = pageSize;
			mCurrPage = Math.min(mCurrPage, pageSize);
		}

		public boolean nextPage() {
			int totalSize = getRecordSize(tableName);
			if (mCurrIndex >= (totalSize - mNumSize)) {
				return false;
			}
			if (totalSize <= 0) {
				return false;
			}

			mCurrPage++;
			// 得到总数的页数
			int pageSize = totalSize / mNumSize;
			int rSize = totalSize % mNumSize;
			if (rSize != 0) {
				pageSize++;
			}
			mPageSize = pageSize;
			if (mCurrPage >= pageSize) {
				mCurrPage--;
				return false;
			}
			LogUtil.logd("nextPage:" + mCurrPage);
			return true;
		}

		public boolean lastPage() {
			if (mCurrIndex <= 0) {
				// 已经查到顶了，不能再继续查
				return false;
			}
			mCurrPage--;
			if (mCurrPage < 0) {
				mCurrPage = 0;
				return false;
			}
			LogUtil.logd("lastPage:" + mCurrPage);
			return true;
		}

		/**
		 * 获取当前页的数量
		 *
		 * @return
		 */
		public int getCurrPageNumSize() {
			int totalSize = getRecordSize(tableName);
			if (totalSize <= 0) {
				return 0;
			}

			// 得到总数的页数
			int pageSize = totalSize / mNumSize;
			int rSize = totalSize % mNumSize;
			if (rSize != 0) {
				pageSize++;
			}
			mPageSize = pageSize;
			if (mCurrPage == pageSize - 1) {
				// 最后一页
				return totalSize - mCurrPage * mNumSize;
			}

			return mNumSize;
		}

		/**
		 * 获取当前页
		 *
		 * @return
		 */
		public int getCurrPage() {
			return mCurrPage;
		}

		/**
		 *
		 * @param page
		 *            从1开始
		 * @return
		 */
		public boolean selectPage(int page) {
			page--;
			if (page < 0) {
				return false;
			}
			int totalSize = getRecordSize(tableName);
			if (totalSize <= 0) {
				return false;
			}
			// 得到总数的页数
			int pageSize = totalSize / mNumSize;
			int rSize = totalSize % mNumSize;
			if (rSize != 0) {
				pageSize++;
			}
			mPageSize = pageSize;
			if (page >= pageSize) {
				return false;
			}

			mCurrPage = page;
			LogUtil.logd("selectPage:" + mCurrPage);
			return true;
		}

		/**
		 * 保存数据
		 *
		 * @param navInfos
		 */
		public void store(List<T> navInfos) {
			mPersistableResource.store(getWritable(), getReadable(), navInfos);
		}

		/**
		 * 根据时间戳删除记录
		 *
		 * @param ids
		 * @return
		 */
		protected boolean delete_Sql(int... ids) {
			if (ids == null || ids.length <= 0) {
				return false;
			}

			List<String> whereArgs = new ArrayList<String>();
			StringBuilder whereBuilder = new StringBuilder();
			whereBuilder.append(DBInfo.Table.NavHistory.KEY_OPERA_TIME);
			whereBuilder.append(" IN (");
			for (int id : ids) {
				whereBuilder.append("?,");
				whereArgs.add(id + "");
			}
			whereBuilder.deleteCharAt(whereBuilder.length() - 1);
			whereBuilder.append(")");

			return mPersistableResource.delete(getWritable(), whereBuilder.toString(),
					whereArgs.toArray(new String[whereArgs.size()]));
		}

		public void clear() {
			reset();
		}


		/**
		 * 获取当前的分页总数
		 * @return
		 */
		public int getPageSize(){
			return mPageSize;
		}

		/**
		 * 获取数据库总数
		 *
		 * @return
		 */
		public int getTotalSize() {
			return getRecordSize(tableName);
		}

		/**
		 * 获取表中数据的总数（没有被删除的记录）
		 *
		 * @param tableName
		 * @return
		 */
		protected int getRecordSize(String tableName) {
			String where = "";
			if (mLastIsDestination) {
				where = " WHERE " + DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "&" + UiMap.HIDE_END_ADDRESS
						+ "=0 GROUP BY " + DBInfo.Table.NavHistory.KEY_SAME_FIELD_END;
			} else {
				where = " WHERE " + DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "&" + UiMap.HIDE_BEGIN_ADDRESS
						+ "=0 GROUP BY " + DBInfo.Table.NavHistory.KEY_SAME_FIELD_BEGIN;
			}
			int size = getRecordSizeByStatus(tableName, where);
			return size > 100 ? 100 : size;
		}

		protected int getRecordSizeByStatus(String tableName, String where) {
			int size = 0;
			SQLiteDatabase database = getReadable();
			if (database != null) {
				Cursor cursor = null;
				try {
					String sql = "select count(*) as c from " + tableName;
					if (!TextUtils.isEmpty(where)) {
						sql += where;
					}
					cursor = database.rawQuery(sql, null);
					if (cursor != null) {
						if (cursor.moveToFirst()) {
							size = cursor.getCount();
							LogUtil.logd("cursor getCount:" + size);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					if (cursor != null && !cursor.isClosed()) {
						cursor.close();
					}
				}
			}
			return size;
		}

		private int[] getIndexs() {
			int totalSize = getRecordSize(tableName);
			int len = mNumSize;

			// 倒序后的index
			int sIdx = mCurrPage * mNumSize;
			int rSize = totalSize - sIdx;
			if (rSize < mNumSize) {
				len = rSize;
			}

			int[] idxs = new int[2];
			idxs[0] = sIdx;
			idxs[1] = len;
			return idxs;
		}

		/**
		 * 先从内存中取，取不到再到数据库中取， 暂不考虑mNavInfoMap的容量，后续有问题，可限制缓存页数
		 *
		 * @return
		 */
		protected List<T> queryByCurrPage(String sql) {
			int[] idxs = getIndexs();
			// 记录当前的起始位置
			mCurrIndex = idxs[0];
			LogUtil.logd("queryByCurrPage idx[0]:" + idxs[0] + ",idx[1]:" + idxs[1]);
			List<T> data = getCurrPageFromCache(mCurrPage);
			if (data != null && !data.isEmpty()) {
				return data;
			}

			data = queryByRawSql(sql, idxs[1], idxs[0]);
			storeToCache(mCurrPage, data);
			return data;
		}

		public abstract List<T> getCurrPageFromCache(int currPage);

		public abstract void storeToCache(int currPage, List<T> data);

		protected List<T> queryByRawSql(String sql, int limit, int offset) {
			SQLiteDatabase readableDatabase = getReadable();
			Cursor cursor = null;
			try {
				cursor = readableDatabase.rawQuery(sql + " LIMIT ? OFFSET ?", new String[] { "" + limit, "" + offset });
				if (cursor != null) {
					if (!cursor.moveToFirst()) {
						return null;
					}
					List<T> cached = new ArrayList<T>();
					do
						cached.add(mPersistableResource.loadFrom(cursor));
					while (cursor.moveToNext());
					return cached;
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
			return null;
		}
	}

	public static class DbNavInfo {
		public int hideStatus = 0;
		public String beginSameField;
		public String endSameField;
		public NavInfo navInfo;
	}

	public class NavInfoCache extends PageCache<DbNavInfo> {
		private static final String NAME = "txz_nav_history.db";
		private static final int VERSION = 1;
		private static final int MAX_EXIST_SIZE = 100;

		/**
		 * 保存当前内存中的数据
		 */
		private SparseArray<List<DbNavInfo>> mNavInfoArrays = new SparseArray<List<DbNavInfo>>();

		public NavInfoCache(Context context, int pageSize) {
			super(new SQLiteCallback() {

				@Override
				public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
					LogUtil.logd("NavHistory database onUpdate");
					db.execSQL(DBInfo.Table.NavHistory.DROP_TABLE);
					onCreate(db);
				}

				@Override
				public void onCreate(SQLiteDatabase db) {
					LogUtil.logd("NavHistory database onCreate");
					db.execSQL(DBInfo.Table.NavHistory.CREATE_TABLE);
				}

				@Override
				public int getVersion() {
					return VERSION;
				}

				@Override
				public String getName() {
					return NAME;
				}
			}, new PersistableResource<DbNavInfo>() {

				@Override
				public Cursor getCursor(SQLiteDatabase readableDatabase, String selection, String[] selectArgs) {
					SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
					builder.setTables(DBInfo.Table.NavHistory.TABLE_NAME);
					return builder.query(readableDatabase,
							new String[] { DBInfo.Table.NavHistory.KEY_OPERA_TIME,
									DBInfo.Table.NavHistory.KEY_OPERA_STATUS,
									DBInfo.Table.NavHistory.KEY_SAME_FIELD_BEGIN,
									DBInfo.Table.NavHistory.KEY_SAME_FIELD_END, DBInfo.Table.NavHistory.KEY_START_INFO,
									DBInfo.Table.NavHistory.KEY_END_INFO },
							selection, selectArgs, null, null, null);
				}

				@Override
				public DbNavInfo loadFrom(Cursor cursor) {
					DbNavInfo dbNavInfo = new DbNavInfo();
					NavInfo navInfo = new NavInfo();
					navInfo.uint32Time = cursor.getInt(0);
					navInfo.uint32Status = cursor.getInt(1);
					dbNavInfo.beginSameField = cursor.getString(2);
					dbNavInfo.endSameField = cursor.getString(3);
					navInfo.msgBeginAddress = convFromJson(cursor.getString(4));
					navInfo.msgEndAddress = convFromJson(cursor.getString(5));
					dbNavInfo.navInfo = navInfo;
					return dbNavInfo;
				}

				@Override
				public void store(SQLiteDatabase writableDatabase, SQLiteDatabase readableDatabase,
								  List<DbNavInfo> items) {
					if (items == null || items.isEmpty()) {
						return;
					}

					LogUtil.logd("store begin...");
					ContentValues values = new ContentValues();
					for (DbNavInfo dbNavInfo : items) {
						NavInfo navInfo = dbNavInfo.navInfo;
						int uTime = navInfo.uint32Time;
						int uStatus = navInfo.uint32Status;
						int clearStatus = dbNavInfo.hideStatus;
						String beginField = dbNavInfo.beginSameField;
						String endField = dbNavInfo.endSameField;
						String startInfo = convFromPointInfo(navInfo.msgBeginAddress);
						String endInfo = convFromPointInfo(navInfo.msgEndAddress);
						LogUtil.logd("store info:" + uTime + "," + uStatus + ",clearStatus:" + clearStatus + ","
								+ beginField + "," + endField + "," + startInfo + "," + endInfo);
						values.clear();
						values.put(DBInfo.Table.NavHistory.KEY_OPERA_TIME, uTime);
						values.put(DBInfo.Table.NavHistory.KEY_OPERA_STATUS, uStatus);
						values.put(DBInfo.Table.NavHistory.KEY_SAME_FIELD_BEGIN, beginField);
						values.put(DBInfo.Table.NavHistory.KEY_SAME_FIELD_END, endField);
						values.put(DBInfo.Table.NavHistory.KEY_START_INFO, startInfo);
						values.put(DBInfo.Table.NavHistory.KEY_END_INFO, endInfo);
						long rawId = writableDatabase.replace(DBInfo.Table.NavHistory.TABLE_NAME, null, values);
						LogUtil.logd("rawId:" + rawId);

						if ((clearStatus & UiMap.HIDE_BEGIN_ADDRESS) == 0
								&& (clearStatus & UiMap.HIDE_END_ADDRESS) == 0) {
							continue;
						}

						// 同时将当前的关键字更新到所有相同关键字的属性ActiveStatus中
						String keyArgs = "";
						if ((clearStatus & UiMap.HIDE_BEGIN_ADDRESS) == UiMap.HIDE_BEGIN_ADDRESS) {
							keyArgs = " WHERE " + DBInfo.Table.NavHistory.KEY_SAME_FIELD_BEGIN + "='" + beginField + "'"
									+ " AND " + DBInfo.Table.NavHistory.KEY_OPERA_TIME + "!=" + uTime;
							String sql = "UPDATE " + DBInfo.Table.NavHistory.TABLE_NAME + " SET "
									+ DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "="
									+ DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "|" + UiMap.HIDE_BEGIN_ADDRESS + "|"
									+ UiMap.ADDED + " " + keyArgs;
							LogUtil.logd("execSQL HIDE_BEGIN_ADDRESS update:" + sql);
							writableDatabase.execSQL(sql);
						}

						if ((clearStatus & UiMap.HIDE_END_ADDRESS) == UiMap.HIDE_END_ADDRESS) {
							keyArgs = " WHERE " + DBInfo.Table.NavHistory.KEY_SAME_FIELD_END + "='" + endField + "'"
									+ " AND " + DBInfo.Table.NavHistory.KEY_OPERA_TIME + "!=" + uTime;
							String sql = "UPDATE " + DBInfo.Table.NavHistory.TABLE_NAME + " SET "
									+ DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "="
									+ DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "|" + UiMap.HIDE_END_ADDRESS + "|"
									+ UiMap.ADDED + " " + keyArgs;
							LogUtil.logd("execSQL HIDE_END_ADDRESS update:" + sql);
							writableDatabase.execSQL(sql);
						}
					}
					LogUtil.logd("store end...");
				}

				public boolean delete(SQLiteDatabase writableDatabase, String where, String[] whereArgs) {
					int iRet = writableDatabase.delete(DBInfo.Table.NavHistory.TABLE_NAME, where, whereArgs);
					return iRet > 0 ? true : false;
				}

				private NavPointInfo convFromJson(String json) {
					JSONBuilder jsonBuilder = new JSONBuilder(json);
					NavPointInfo navPointInfo = new NavPointInfo();
					navPointInfo.strPoiAddress = jsonBuilder.getVal("strPoiAddress", String.class);
					navPointInfo.strPoiName = jsonBuilder.getVal("strPoiName", String.class);
					GpsInfo gpsInfo = new GpsInfo();
					gpsInfo.dblAltitude = jsonBuilder.getVal("dblAltitude", Double.class);
					gpsInfo.dblLat = jsonBuilder.getVal("dblLat", Double.class);
					gpsInfo.dblLng = jsonBuilder.getVal("dblLng", Double.class);
					gpsInfo.fltDirection = jsonBuilder.getVal("fltDirection", Float.class);
					gpsInfo.fltRadius = jsonBuilder.getVal("fltRadius", Float.class);
					gpsInfo.fltSpeed = jsonBuilder.getVal("fltSpeed", Float.class);
					gpsInfo.uint32GpsType = jsonBuilder.getVal("uint32GpsType", Integer.class);
					gpsInfo.uint32SatellitesNum = jsonBuilder.getVal("uint32SatellitesNum", Integer.class);
					navPointInfo.msgGpsInfo = gpsInfo;
					return navPointInfo;
				}

				private String convFromPointInfo(NavPointInfo info) {
					JSONBuilder jsonBuilder = new JSONBuilder();
					if (info.msgGpsInfo.dblAltitude != null)
						jsonBuilder.put("dblAltitude", info.msgGpsInfo.dblAltitude);
					if (info.msgGpsInfo.dblLat != null)
						jsonBuilder.put("dblLat", info.msgGpsInfo.dblLat);
					if (info.msgGpsInfo.dblLng != null)
						jsonBuilder.put("dblLng", info.msgGpsInfo.dblLng);
					if (info.msgGpsInfo.fltDirection != null)
						jsonBuilder.put("fltDirection", info.msgGpsInfo.fltDirection);
					if (info.msgGpsInfo.fltRadius != null)
						jsonBuilder.put("fltRadius", info.msgGpsInfo.fltRadius);
					if (info.msgGpsInfo.fltSpeed != null)
						jsonBuilder.put("fltSpeed", info.msgGpsInfo.fltSpeed);
					if (info.msgGpsInfo.uint32GpsType != null)
						jsonBuilder.put("uint32GpsType", info.msgGpsInfo.uint32GpsType);
					if (info.msgGpsInfo.uint32SatellitesNum != null)
						jsonBuilder.put("uint32SatellitesNum", info.msgGpsInfo.uint32SatellitesNum);
					if (info.strPoiAddress != null)
						jsonBuilder.put("strPoiAddress", info.strPoiAddress);
					if (info.strPoiName != null)
						jsonBuilder.put("strPoiName", info.strPoiName);
					return jsonBuilder.toString();
				}
			}, pageSize, DBInfo.Table.NavHistory.TABLE_NAME);
			init(context);

			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					createAutoLimitTrigger(MAX_EXIST_SIZE);
				}
			});
		}

		@Override
		public void store(List<DbNavInfo> navInfos) {
			mNavInfoArrays.clear();
			super.store(navInfos);
		}

		@Override
		public void clear() {
			super.clear();
			mNavInfoArrays.clear();
		}

		/**
		 * 查询没有被标记删除的数据
		 *
		 * @return
		 */
		public List<DbNavInfo> queryByCurrPage() {
			String whereArgs = "";
			if (mLastIsDestination) {
				whereArgs = DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "&" + UiMap.HIDE_END_ADDRESS + "=0 GROUP BY "
						+ DBInfo.Table.NavHistory.KEY_SAME_FIELD_END;
			} else {
				whereArgs = DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "&" + UiMap.HIDE_BEGIN_ADDRESS + "=0 GROUP BY "
						+ DBInfo.Table.NavHistory.KEY_SAME_FIELD_BEGIN;
			}
			return queryByCurrPage(whereArgs);
		}

		// TODO
		public List<DbNavInfo> queryTopRecord(int size) {
			String whereArgs = DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "&" + UiMap.HIDE_END_ADDRESS + "=0 GROUP BY "
					+ DBInfo.Table.NavHistory.KEY_SAME_FIELD_END;
			return queryByRawSql(DBInfo.Table.NavHistory.QUERY_SQL.replace("whereArgs", whereArgs), size, 0);
		}

		@Override
		public List<DbNavInfo> queryByCurrPage(String whereArgs) {
			String sql = DBInfo.Table.NavHistory.QUERY_SQL.replace("whereArgs", whereArgs);
			return super.queryByCurrPage(sql);
		}

		@Override
		public List<DbNavInfo> getCurrPageFromCache(int currPage) {
			List<DbNavInfo> navInfos = mNavInfoArrays.get(currPage);
			if (navInfos != null && !navInfos.isEmpty()) {
				return navInfos;
			}
			mNavInfoArrays.remove(currPage);
			return null;
		}

		@Override
		public void storeToCache(int currPage, List<DbNavInfo> data) {
			if (data != null && !data.isEmpty()) {
				mNavInfoArrays.put(currPage, data);
			}
		}

		/**
		 * 查询所有没有同步的数据
		 *
		 * @return
		 */
		public List<DbNavInfo> queryNoReadys(int UnlessArgs) {
			SQLiteDatabase readableDatabase = getReadable();
			Cursor cursor = null;
			try {
				String whereArgs = DBInfo.Table.NavHistory.KEY_OPERA_STATUS + " & " + UnlessArgs + "=" + UnlessArgs;
				String sql = "SELECT " + DBInfo.Table.NavHistory.KEY_OPERA_TIME + ","
						+ DBInfo.Table.NavHistory.KEY_OPERA_STATUS + "," + DBInfo.Table.NavHistory.KEY_SAME_FIELD_BEGIN
						+ "," + DBInfo.Table.NavHistory.KEY_SAME_FIELD_END + ","
						+ DBInfo.Table.NavHistory.KEY_START_INFO + "," + DBInfo.Table.NavHistory.KEY_END_INFO + " FROM "
						+ DBInfo.Table.NavHistory.TABLE_NAME + " WHERE " + whereArgs;
				cursor = readableDatabase.rawQuery(sql, null);
				if (cursor != null) {
					if (!cursor.moveToFirst()) {
						return null;
					}
					List<DbNavInfo> cached = new ArrayList<DbNavInfo>();
					do {
						cached.add(mPersistableResource.loadFrom(cursor));
					} while (cursor.moveToNext());
					return cached;
				}
			} catch (Exception exception) {
				exception.printStackTrace();
			} finally {
				if (cursor != null && !cursor.isClosed()) {
					cursor.close();
				}
			}
			return null;
		}

		/**
		 * 当数据超过size的时候触发删除最旧一条
		 *
		 * @param size
		 */
		private void createAutoLimitTrigger(int size) {
			SQLiteDatabase database = getWritable();
			try {
				database.execSQL(DBInfo.Table.NavHistory.INNSERT_LIMIT_TRIGGER.replace("%SIZE%", size + "").replace("?",
						UiMap.DELETED + ""));
			} catch (Exception e) {
			} finally {
				if (database != null) {
					database.close();
				}
			}
		}
	}
}