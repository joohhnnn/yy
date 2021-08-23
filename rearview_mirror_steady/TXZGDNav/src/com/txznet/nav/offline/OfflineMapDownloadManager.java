package com.txznet.nav.offline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.text.TextUtils;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;

public class OfflineMapDownloadManager {
	// 一级列表
	private List<OfflineMapProvince> mOfflineMapProvinces;
	// 二级目录
	private HashMap<Object, List<OfflineMapCity>> mExpendListCityMap = new HashMap<Object, List<OfflineMapCity>>();

	private Map<String, String> mCityToProvMap = new HashMap<String, String>();

	private OfflineMapManager mMapInnerManager;

	private static OfflineMapDownloadManager instance;

	public static OfflineMapDownloadManager getInstance() {
		if (instance == null) {
			synchronized (OfflineMapDownloadManager.class) {
				if (instance == null) {
					instance = new OfflineMapDownloadManager();
				}
			}
		}
		return instance;
	}

	public class DownloadItem {
		public int state;
		public String name;
		public int completeCode;
	}

	private String mCurrentDownloadItemName;

	OfflineMapDownloadListener mOfflineMapDownloadListener = new OfflineMapDownloadListener() {

		@Override
		public void onRemove(boolean arg0, String arg1, String arg2) {
			requestExecQueryAgain(false);
		}

		@Override
		public void onDownload(int arg0, int arg1, String arg2) {
			LogUtil.logd("state:" + arg0 + ",completeCode:" + arg1 + ",name:"
					+ arg2);
			// if (mCurrentDownloadItem == null) {
			// mCurrentDownloadItem = new DownloadItem();
			// }
			if (TextUtils.isEmpty(mCurrentDownloadItemName)) {
				mCurrentDownloadItemName = arg2;
				requestExecQueryAgain(false);
			} else if (!mCurrentDownloadItemName.equals(arg2)) {
				mCurrentDownloadItemName = arg2;
				requestExecQueryAgain(false);
			} else {

				// }

				// if (!mCurrentDownloadItem.name.equals(arg2)) {
				// mCurrentDownloadItem.name = arg2;
				// mCurrentDownloadItem.state = arg0;
				// mCurrentDownloadItem.completeCode = arg1;
				// requestExecQueryAgain(false);
				// } else {
				// 不执行重新拉去列表，更新既可
				invokeOnNotifyListener();
			}

			// if(mOnNotifyListener != null){
			// mOnNotifyListener.onNotify();
			// }
		}

		@Override
		public void onCheckUpdate(boolean arg0, String arg1) {
		}
	};

	private void checkForRestartDownTask() {
		AppLogic.removeBackGroundCallback(mCheckRestartRunnable);
		AppLogic.runOnBackGround(mCheckRestartRunnable, 10000);
	}

	/**
	 * 检测是否还在运行download，主要是为了检测是否停止了
	 */
	Runnable mCheckRestartRunnable = new Runnable() {

		@Override
		public void run() {
			List<OfflineMapCity> omcs = mMapInnerManager
					.getDownloadingCityList();
			List<OfflineMapProvince> omps = mMapInnerManager
					.getDownloadingProvinceList();
			if ((omcs != null && omcs.size() > 0)
					|| (omps != null && omps.size() > 0)) {
				// 需要restart一次
				setOfflineStopFlag();
				mMapInnerManager.restart();
				checkForRestartDownTask();
			}
		}
	};

	public void cancelAllBackgroundRunnable() {
		AppLogic.removeBackGroundCallback(mCheckRestartRunnable);
	}

	private void setOfflineStopFlag() {
		try {
			Field field = OfflineMapManager.class.getDeclaredField("mIsStart");
			field.set(mMapInnerManager, (Boolean) false);
		} catch (Exception e) {
		}
	}

	public void invokeOnNotifyListener() {
		for (OnNotifyListener listener : mOnNotifyListeners) {
			listener.onNotify();
		}
	}

	public void init() {
		AppLogic.removeBackGroundCallback(mInitRunnable);
		AppLogic.runOnBackGround(mInitRunnable, 0);
	}

	Runnable mInitRunnable = new Runnable() {

		@Override
		public void run() {
			mMapInnerManager = new OfflineMapManager(AppLogic.getApp(),
					mOfflineMapDownloadListener);
			requestExecQueryAgain(true);
		}
	};

	private OfflineMapDownloadManager() {
	}

	private void execQueryOffline() {
		mOfflineMapProvinces = mMapInnerManager.getOfflineMapProvinceList();
		List<OfflineMapProvince> mZhiXiaProvList = new ArrayList<OfflineMapProvince>();
		List<OfflineMapCity> mZhiXiaCityList = new ArrayList<OfflineMapCity>();

		for (int i = 0; i < mOfflineMapProvinces.size(); i++) {
			OfflineMapProvince omp = mOfflineMapProvinces.get(i);
			// 将省对象转为城市对象来下载
			OfflineMapCity omc = provinceToCity(omp);
			List<OfflineMapCity> omcList = new ArrayList<OfflineMapCity>();
			if (omp.getCityList().size() != 1) {
				omcList.add(omc);
				omcList.addAll(omp.getCityList());
			} else {
				// 直辖市
				mZhiXiaCityList.add(omc);
				mZhiXiaProvList.add(omp);
			}
			mExpendListCityMap.put(i + 3, omcList);
			if (!mCityToProvMap.containsKey(omc.getCity())) {
				mCityToProvMap.put(omc.getCity(), omp.getProvinceName());
			}
		}

		// 添加
		OfflineMapProvince omp = new OfflineMapProvince();
		omp.setProvinceName("概要图");
		mOfflineMapProvinces.add(0, omp);
		omp = new OfflineMapProvince();
		omp.setProvinceName("直辖市");
		mOfflineMapProvinces.add(1, omp);
		omp = new OfflineMapProvince();
		omp.setProvinceName("港澳");
		mOfflineMapProvinces.add(2, omp);

		mOfflineMapProvinces.removeAll(mZhiXiaProvList);
		List<OfflineMapCity> gangaoList = new ArrayList<OfflineMapCity>();
		List<OfflineMapCity> gaiyaoList = new ArrayList<OfflineMapCity>();
		for (OfflineMapProvince mp : mZhiXiaProvList) {
			if (mp.getProvinceName().contains("香港")
					|| mp.getProvinceName().contains("澳门")) {
				gangaoList.add(provinceToCity(mp));
			} else if (mp.getProvinceName().contains("全国")) {
				gaiyaoList.add(provinceToCity(mp));
			}
		}

		try {
			// 删除重复的港澳概要数据
			mZhiXiaCityList.remove(4);
			mZhiXiaCityList.remove(4);
			mZhiXiaCityList.remove(4);
		} catch (Throwable e) {
			LogUtil.loge("cityList throwIndexOutOfBoundsException");
		}

		mExpendListCityMap.put(0, gaiyaoList);
		mExpendListCityMap.put(1, mZhiXiaCityList);
		mExpendListCityMap.put(2, gangaoList);

		if (mInnerOnQueryDoneListener != null) {
			mInnerOnQueryDoneListener.onInitDone();
		}
	}

	public List<OfflineMapProvince> getOfflineMapProvinces() {
		return mOfflineMapProvinces;
	}

	public List<OfflineMapCity> getOfflineMapCitiesByGroupPos(int pos) {
		if (mExpendListCityMap.size() <= pos || pos < 0) {
			return null;
		}

		return mExpendListCityMap.get(pos);
	}

	Runnable mQueryOfflineRunnable = new Runnable() {

		@Override
		public void run() {
			execQueryOffline();
		}
	};

	private OnQueryDoneListener mInnerOnQueryDoneListener = new OnQueryDoneListener() {

		@Override
		public void onInitDone() {
			if (mOnQueryDoneListener != null) {
				mOnQueryDoneListener.onInitDone();
			}

			if (!mRequestRefreshAll) {
				return;
			}

			AppLogic
					.removeBackGroundCallback(mExecUpdateRunnable);
			AppLogic.runOnBackGround(mExecUpdateRunnable, 0);
		}
	};

	boolean mRequestRefreshAll;
	boolean mIsRefreshing;

	public void requestExecQueryAgain(boolean isNeedOffline) {
		if (mIsRefreshing) {
			return;
		}
		mRequestRefreshAll = true;
		mIsRefreshing = true;
		if (isNeedOffline) {
			AppLogic.removeBackGroundCallback(
					mQueryOfflineRunnable);
			AppLogic.runOnBackGround(mQueryOfflineRunnable, 0);
		} else {
			mInnerOnQueryDoneListener.onInitDone();
		}
	}

	public OfflineMapCity provinceToCity(OfflineMapProvince aMapProvince) {
		OfflineMapCity aMapCity = new OfflineMapCity();
		aMapCity.setCity(aMapProvince.getProvinceName());
		aMapCity.setSize(aMapProvince.getSize());
		aMapCity.setCompleteCode(aMapProvince.getcompleteCode());
		aMapCity.setState(aMapProvince.getState());
		aMapCity.setUrl(aMapProvince.getUrl());
		return aMapCity;
	}

	List<OfflineMapCity> mUpdateCacheCities = new ArrayList<OfflineMapCity>();
	List<OfflineMapProvince> mUpdateCacheProvs = new ArrayList<OfflineMapProvince>();

	public OfflineMapManager getOfflineMapManager() {
		return mMapInnerManager;
	}

	public void pause() {
		mMapInnerManager.pause();
	}

	public void remove(String city) {
		mMapInnerManager.remove(city);
	}

	public void downloadMapCity(String city) {
		try {
			mMapInnerManager.downloadByCityName(city);
			requestExecQueryAgain(false);
		} catch (AMapException e) {
			e.printStackTrace();
		}
	}

	public void cancelDownload(String city) {
		mMapInnerManager.remove(city);
	}

	public void downloadMapProvince(String provName) {
		try {
			mMapInnerManager.downloadByProvinceName(provName);
			requestExecQueryAgain(false);
		} catch (AMapException e) {
			e.printStackTrace();
		}
	}

	Runnable mUpdateCurrCityRunnable = new Runnable() {

		@Override
		public void run() {
			mUpdateCacheCities.clear();
			List<OfflineMapCity> omc = mMapInnerManager
					.getDownloadingCityList();
			if (omc == null) {
				omc = new ArrayList<OfflineMapCity>();
			}
			List<OfflineMapCity> omced = mMapInnerManager
					.getDownloadOfflineMapCityList();
			if (omc != null) {
				mUpdateCacheCities.addAll(omc);
			}
			if (omced != null) {
				mUpdateCacheCities.addAll(omced);
			}
		}
	};

	Runnable mUpdateCurrProvRunnable = new Runnable() {

		@Override
		public void run() {
			mUpdateCacheProvs.clear();
			List<OfflineMapProvince> omp = mMapInnerManager
					.getDownloadingProvinceList();
			if (omp == null) {
				omp = new ArrayList<OfflineMapProvince>();
			}
			List<OfflineMapProvince> omped = mMapInnerManager
					.getDownloadOfflineMapProvinceList();
			if (omp != null) {
				mUpdateCacheProvs.addAll(omp);
			}
			if (omped != null) {
				mUpdateCacheProvs.addAll(omped);
			}
		}
	};

	Runnable mAnalysisLoadingRunnable = new Runnable() {

		@Override
		public void run() {
			mDownloadWinUseList.clear();
			mNameList.clear();
			for (OfflineMapCity o : mUpdateCacheCities) {
				mDownloadWinUseList.add(o);
				mNameList.add(o.getCity());
			}

			for (OfflineMapProvince o : mUpdateCacheProvs) {
				if (mNameList.contains(o.getProvinceName())) {
					continue;
				}

				mDownloadWinUseList.add(DownloadMapManager.getInstance()
						.provinceToCity(o));
				mNameList.add(o.getProvinceName());
			}
			mParseProvListByCities.run();

			mRequestRefreshAll = false;
			mIsRefreshing = false;
			if (mOnPrepareDoneListener != null) {
				mOnPrepareDoneListener.onprepared(mDownloadWinUseList);
			}
		}
	};

	Runnable mParseProvListByCities = new Runnable() {

		@Override
		public void run() {
			mNameList.clear();
			mNameToCity.clear();
			for (OfflineMapCity omc : mDownloadWinUseList) {
				String provName = mCityToProvMap.get(omc.getCity());
				if (mNameToCity.containsKey(provName)) {
					List<OfflineMapCity> omcList = mNameToCity.get(provName);
					omcList.add(omc);
				} else {
					List<OfflineMapCity> omcList = new ArrayList<OfflineMapCity>();
					omcList.add(omc);
					mNameToCity.put(provName, omcList);
				}

				if (!mNameList.contains(provName)) {
					mNameList.add(provName);
				}
			}
		}
	};

	// 取一次数据判断当前状态
	Runnable mExecUpdateRunnable = new Runnable() {

		@Override
		public void run() {
			mUpdateCurrCityRunnable.run();
			mUpdateCurrProvRunnable.run();
			mAnalysisLoadingRunnable.run();
		}
	};

	List<OfflineMapCity> mDownloadWinUseList = new ArrayList<OfflineMapCity>();

	// 当前下载城市对应的省份
	List<String> mNameList = new ArrayList<String>();

	Map<String, List<OfflineMapCity>> mNameToCity = new HashMap<String, List<OfflineMapCity>>();

	public List<String> getDownloadProvList() {
		return mNameList;
	}

	public List<OfflineMapCity> getCitiesByProv(String provName) {
		return mNameToCity.get(provName);
	}

	public interface OnPrepareDoneListener {
		public void onprepared(Object object);
	}

	OnPrepareDoneListener mOnPrepareDoneListener;

	public void registerDownloadDoneListener(OnPrepareDoneListener listener) {
		this.mOnPrepareDoneListener = listener;
	}

	OnQueryDoneListener mOnQueryDoneListener;

	public void registerInitDoneListener(OnQueryDoneListener listener) {
		this.mOnQueryDoneListener = listener;
	}

	public interface OnQueryDoneListener {
		public void onInitDone();
	}

	List<OnNotifyListener> mOnNotifyListeners = new ArrayList<OfflineMapDownloadManager.OnNotifyListener>();

	public void registerNotifyListener(OnNotifyListener listener) {
		if (listener == null) {
			return;
		}
		if (mOnNotifyListeners.contains(listener)) {
			return;
		}
		mOnNotifyListeners.add(listener);
	}

	public interface OnNotifyListener {
		public void onNotify();
	}
}