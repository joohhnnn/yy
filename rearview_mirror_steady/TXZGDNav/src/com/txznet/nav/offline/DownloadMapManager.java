package com.txznet.nav.offline;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amap.api.maps.AMapException;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapManager.OfflineMapDownloadListener;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.util.runnables.Runnable3;

public class DownloadMapManager implements IOffline {

	// 一级列表
	private List<OfflineMapProvince> mOfflineMapProvinces;
	// 二级目录
	private HashMap<Object, List<OfflineMapCity>> mExpendListCityMap = new HashMap<Object, List<OfflineMapCity>>();

	private OfflineMapManager mOfflineMapManager;
	private OfflineMapDownloadListener mListener;

	// 通过手工下载的城市
	private List<String> mDownloadingCityNameList = new ArrayList<String>();
	// 城市对应的group位置
	private Map<String, Integer> mCityToProvMap = new HashMap<String, Integer>();

	// 通过手工下载的省份
	private List<String> mDownloadingProvNameList = new ArrayList<String>();

	private HashMap<String, DownloadListener> mCityDownloadHashMap = new HashMap<String, IOffline.DownloadListener>();
	private HashMap<String, DownloadListener> mProvDownloadHashMap = new HashMap<String, IOffline.DownloadListener>();

	private static List<OnRefreshListener> mListeners = new ArrayList<DownloadMapManager.OnRefreshListener>();
	private static List<OnInitListener> mOnInitListeners = new ArrayList<DownloadMapManager.OnInitListener>();

	private static DownloadMapManager instance = null;

	public boolean mHasInited;

	private DownloadMapManager() {
		initialize();
	}

	private void reset() {
		mDownloadingCityNameList.clear();
		mCityToProvMap.clear();
		mDownloadingProvNameList.clear();
		mCityDownloadHashMap.clear();
		mProvDownloadHashMap.clear();
		mExpendListCityMap.clear();
		mHasInited = false;
		mOfflineMapManager = null;
		mListener = null;
	}

	public static DownloadMapManager getInstance() {
		if (instance == null) {
			synchronized (DownloadMapManager.class) {
				if (instance == null) {
					instance = new DownloadMapManager();
				}
			}
		}

		return instance;
	}

	public void destory() {
		instance = null;
	}

	private void init() {
		reset();
		mListener = new OfflineMapDownloadListener() {

			@Override
			public void onRemove(boolean arg0, String arg1, String arg2) {
				LogUtil.logd("OfflineMapManager onRemove:" + arg0 + "," + arg1
						+ "," + arg2);
				invokeOnRefreshListener();
			}

			@Override
			public void onDownload(int status, int completeCode, String name) {
				LogUtil.logd("OfflineMapManager onDownload:" + status + ","
						+ completeCode + "," + name);
				if (mDownloadingCityNameList.contains(name)) {
					DownloadListener dl = mCityDownloadHashMap.get(name);
					if (dl != null) {
						dl.onCityStatus(status, completeCode, name);
					}
				} else {
					DownloadListener dl = mProvDownloadHashMap.get(name);
					if (dl != null) {
						dl.onCityStatus(status, completeCode, name);
					}
				}

				invokeOnRefreshListener();
				checkForRestartDownTask(5000);
			}

			@Override
			public void onCheckUpdate(boolean arg0, String arg1) {
				LogUtil.logd("OfflineMapManager onCheckUpdate:" + arg0 + ","
						+ arg1);
			}
		};

		mOfflineMapManager = new OfflineMapManager(AppLogic.getApp(), mListener);
		mOfflineMapProvinces = mOfflineMapManager.getOfflineMapProvinceList();

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
			AppLogic.runOnUiGround(
					new Runnable2<Integer, List<OfflineMapCity>>(i + 3, omcList) {

						@Override
						public void run() {
							mExpendListCityMap.put(mP1, mP2);
						}
					}, 0);
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

		AppLogic.runOnUiGround(
				new Runnable3<List<OfflineMapCity>, List<OfflineMapCity>, List<OfflineMapCity>>(
						gaiyaoList, mZhiXiaCityList, gangaoList) {

					@Override
					public void run() {
						mExpendListCityMap.put(0, mP1);
						mExpendListCityMap.put(1, mP2);
						mExpendListCityMap.put(2, mP3);
					}
				}, 0);

		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				invokeInitListener();
			}
		}, 0);
		mHasInited = true;
	}

	private void checkForRestartDownTask(long millis) {
		AppLogic.removeBackGroundCallback(mCheckRestartRunnable);
		AppLogic.runOnBackGround(mCheckRestartRunnable, millis);
	}

	/**
	 * 检测是否还在运行download，主要是为了检测是否停止了
	 */
	Runnable mCheckRestartRunnable = new Runnable() {

		@Override
		public void run() {
			List<OfflineMapCity> omcs = mOfflineMapManager
					.getDownloadingCityList();
			List<OfflineMapProvince> omps = mOfflineMapManager
					.getDownloadingProvinceList();
			if ((omcs != null && omcs.size() > 0)
					|| (omps != null && omps.size() > 0)) {
				// 需要restart一次
				setOfflineStopFlag();
				mOfflineMapManager.restart();
				checkForRestartDownTask(3000);
			}
		}
	};

	public void cancelAllBackgroundRunnable() {
		AppLogic.removeBackGroundCallback(mCheckRestartRunnable);
	}

	private void setOfflineStopFlag() {
		try {
			Field field = OfflineMapManager.class.getDeclaredField("mIsStart");
			field.set(mOfflineMapManager, (Boolean) false);
		} catch (Exception e) {
		}
	}

	/**
	 * 把一个省的对象转化为一个市的对象
	 */
	public OfflineMapCity provinceToCity(OfflineMapProvince aMapProvince) {
		OfflineMapCity aMapCity = new OfflineMapCity();
		aMapCity.setCity(aMapProvince.getProvinceName());
		aMapCity.setSize(aMapProvince.getSize());
		aMapCity.setCompleteCode(aMapProvince.getcompleteCode());
		aMapCity.setState(aMapProvince.getState());
		aMapCity.setUrl(aMapProvince.getUrl());
		return aMapCity;
	}

	@Override
	public void initialize() {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				init();
				invokeOnRefreshListener();
			}
		}, 0);
	}

	@Override
	public List<OfflineMapProvince> getProvinces() {
		return mOfflineMapProvinces;
	}

	@Override
	public List<OfflineMapCity> getOfflineMapCities(int groupPos) {
		return mExpendListCityMap.get(groupPos);
	}

	@Override
	public void downloadMapCity(int groupPos, String cityName,
			DownloadListener listener) {
		try {
			if (!mCityToProvMap.containsKey(cityName)) {
				if (groupPos != -1) {
					mCityToProvMap.put(cityName, groupPos);
				}
			}

			mDownloadingCityNameList.add(cityName);
			mCityDownloadHashMap.put(cityName, listener);
			mOfflineMapManager.downloadByCityName(cityName);
			checkForRestartDownTask(5000);
		} catch (AMapException e) {
			LogUtil.loge(e.toString());
		}
	}

	@Override
	public void downloadMapProvince(int groupPos, String provName,
			DownloadListener listener) {
		try {
			mDownloadingProvNameList.add(provName);
			mProvDownloadHashMap.put(provName, listener);
			mOfflineMapManager.downloadByProvinceName(provName);
			checkForRestartDownTask(5000);
		} catch (AMapException e) {
			LogUtil.loge(e.toString());
		}
	}

	@Override
	public void pause() {
		mOfflineMapManager.pause();
		checkForRestartDownTask(3000);
	}

	public void remove(String cityName) {
		mOfflineMapManager.remove(cityName);
		checkForRestartDownTask(3000);
	}

	@Override
	public void goon() {
	}

	@Override
	public OfflineMapManager getOfflineMapManager() {
		return mOfflineMapManager;
	}

	public static void addOnRefreshListener(OnRefreshListener listener) {
		if (listener == null) {
			return;
		}

		if (mListeners == null) {
			mListeners = new ArrayList<DownloadMapManager.OnRefreshListener>();
		}

		if (!mListeners.contains(listener)) {
			mListeners.add(listener);
		}
	}

	public static void removeOnRefreshListener(OnRefreshListener listener) {
		if (listener == null) {
			return;
		}

		if (mListeners == null) {
			return;
		}

		if (mListeners.contains(listener)) {
			mListeners.remove(listener);
		}
	}

	public static void invokeOnRefreshListener() {
		if (mListeners == null) {
			return;
		}

		for (OnRefreshListener listener : mListeners) {
			listener.onRefresh();
		}
	}

	public static void addOnInitListener(OnInitListener listener) {
		if (listener == null) {
			return;
		}

		if (mOnInitListeners.contains(listener)) {
			return;
		}

		mOnInitListeners.add(listener);
	}

	public static void removeOnInitListener(OnInitListener listener) {
		if (listener == null) {
			return;
		}

		if (mOnInitListeners.contains(listener)) {
			mOnInitListeners.remove(listener);
		}
	}

	public static void invokeInitListener() {
		for (OnInitListener listener : mOnInitListeners) {
			listener.initSuccess();
		}
	}

	/**
	 * 刷新的监听
	 */
	public interface OnRefreshListener {
		public void onRefresh();
	}

	public interface OnInitListener {
		public void initSuccess();
	}

	public class DownloadCityQueue {
		List<String> mCitys;

		public void addFirst(String city) {
			mCitys.add(0, city);
		}

		public void addLast(String city) {
			synchronized (mCitys) {
				mCitys.add(city);
			}
		}

		public void remove(String city) {
			synchronized (mCitys) {
				if (mCitys.contains(city)) {
					mCitys.remove(city);
				}
			}
		}

		public String getNextDownloadCity() {
			synchronized (mCitys) {
				if (!mCitys.isEmpty()) {
					return mCitys.get(0);
				}
				return "";
			}
		}

		public void moveToLast(String city) {
			synchronized (mCitys) {
				for (String c : mCitys) {
					if (c.equals(city)) {
						mCitys.remove(c);
						mCitys.add(city);
						break;
					}
				}
			}
		}
	}
}