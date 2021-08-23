package com.txznet.txz.component.poi.baidu;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import android.os.Environment;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.text.TextUtils;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.base.CrashCommonHandler;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.TXZHandler;

public class PoiSearchToolBaiduImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	public final static int MAX_NEARBY_RADIUS = 10000;
	private final static int MAX_THREAD_COUNT_WARNING = 5;
	private final static int MAX_THREAD_COUNT_ERROR = 20;
	
	private long mSearchTime= 0;
	PoiSearch mPoiSearch = null;
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	private static HashSet<HandlerThread> sAliveSearchThread = new HashSet<HandlerThread>();
	private HandlerThread mPoiThread;
	private TXZHandler mPoiHandler;
	SearchReq mReq = new SearchReq() {
		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
			if (mPoiSearch != null) {
				mPoiSearch.destroy();
				mPoiSearch = null;
			}
			releseThread(false);
		}
	};
	PoiSearchResultListener mResultListener = null;

	Runnable mRunnableSearchTimeout = new Runnable() {
		@Override
		public void run() {
			if (mResultListener != null) {
				JNIHelper.logd("POISearchLog:"+getClass().toString()+" mRetryCount="+mRetryCount);
				if(mRetryCount>0){
					mRetryCount--;
					if(isCitySearch){
						searchInCity((CityPoiSearchOption)mOption, mResultListener);
					}else{
						searchNearby((NearbyPoiSearchOption)mOption, mResultListener);
					}
					return;
				}
				MonitorUtil
						.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_BAIDU);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
//				NavManager.getInstance().reportNetStatus("baiduimp", "timeout");
				mResultListener = null;
			}
		}
	};

	OnGetPoiSearchResultListener mOnGetPoiSearchResultListener = new OnGetPoiSearchResultListener() {
		@Override
		public void onGetPoiDetailResult(PoiDetailResult arg0) {
			JNIHelper.logd("POISearchLog: baiduImp onGetPoiDetailResult");
			releseThread(true);
		}
		

		@Override
		public void onGetPoiIndoorResult(PoiIndoorResult arg0) {
			JNIHelper.logd("POISearchLog: baiduImp onGetPoiIndoorResult");
			releseThread(true);
		}

		@Override
		public void onGetPoiResult(PoiResult result) {
			JNIHelper.logd("POISearchLog: baiduImp onGetPoiResult");
			final PoiResult resultBack = result;
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					try {
						releseThread(true);
						mSearchTime = SystemClock.elapsedRealtime() - mSearchTime;
						NavManager.reportBack("baiduimp", mSearchTime);
						AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
						if (mPoiSearch == null || mResultListener == null)
							return;
						JNIHelper.logd("POISearchLog:PoiSearchToolBaiduImpl resultBack.error= "+resultBack.error);
						if (resultBack.error != null&& resultBack.error != ERRORNO.NO_ERROR) {
							if (resultBack.error == ERRORNO.RESULT_NOT_FOUND) {
								MonitorUtil
										.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU);
								mResultListener.onError(
										TXZPoiSearchManager.ERROR_CODE_EMPTY,
										"");
								return;
							}
							if (resultBack.error == ERRORNO.NETWORK_TIME_OUT) {
								MonitorUtil
										.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_BAIDU);
								mResultListener.onError(
										TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
										"");
								return;
							}
							if(resultBack.error == ERRORNO.PERMISSION_UNFINISHED){
								MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_BAIDU);
								mResultListener.onError(
										TXZPoiSearchManager.ERROR_CODE_EMPTY,
								"");
								return;
							}
							if(resultBack.error == ERRORNO.AMBIGUOUS_KEYWORD){
								MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_SUGGEST_ALL);
								List<CityInfo> suggestCityList = resultBack.getSuggestCityList();
								List<String> cityList = new ArrayList<String>();
								if(suggestCityList != null ){
									for(CityInfo info : suggestCityList){
										cityList.add(info.city);
									}									
								}
								JNIHelper.logd("POISearchLog: baiduImp suggestCityList = "+cityList);
								List<String> keyWord =  new ArrayList<String>();
								keyWord.add(mOption.getKeywords());
//								mResultListener.onError(
//										TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
//								return;
								mResultListener.onSuggestion(
										new SearchPoiSuggestion().setCity(cityList).setKeywrods(keyWord));
								return;								
							}

							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_ERROR_BAIDU);
							mResultListener.onError(
									TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
							return;
						}
						List<Poi> pois = new ArrayList<Poi>();
						if (resultBack.getAllPoi() != null) {
							for (PoiInfo p : resultBack.getAllPoi()) {
								Poi poi = getPoiFromPoiInfo(p);
								if (poi == null)
									continue;
								pois.add(poi);
							}
						}
						if (!pois.isEmpty()) {
							Collections.sort(pois, new Comparator<Poi>() {
								@Override
								public int compare(Poi lhs, Poi rhs) {
									if (lhs.getDistance() < rhs.getDistance())
										return -1;
									if (lhs.getDistance() > rhs.getDistance())
										return 1;
									return 0;
								}
							});
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_BAIDU);
							JNIHelper
									.logd("POISearchLog:PoiSearchToolBaiduImpl return Poi count= "
											+ pois.size());
							NavManager.getInstance().setNomCityList(pois);
							PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout() - mSearchTime, mResultListener, pois);
							return;
						}
						List<CityInfo> cityinfos = resultBack
								.getSuggestCityList();
						if (cityinfos != null) {
							List<String> cities = new ArrayList<String>();
							for (CityInfo city : cityinfos) {
								if (city == null
										|| TextUtils.isEmpty(city.city))
									continue;
								cities.add(city.city);
							}
							if (!cities.isEmpty()) {
								SearchPoiSuggestion suggestion = new SearchPoiSuggestion();
								suggestion.setCity(cities);
								MonitorUtil
										.monitorCumulant(MonitorUtil.POISEARCH_SUGGEST_ALL);
								mResultListener.onSuggestion(suggestion);
								return;
							}
						}
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU);
						mResultListener.onError(
								TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
					} catch (Exception e) {
						if (mResultListener != null)
							mResultListener.onError(
									TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
					}
				}
			}, 0);

		}


		@Override
		public void onGetPoiDetailResult(PoiDetailSearchResult arg0) {
			// TODO Auto-generated method stub
			
		}
	};

	private static Poi getPoiFromPoiInfo(PoiInfo item) {
		if (item == null)
			return null;
		PoiDetail p = new PoiDetail();
		p.setName(item.name);
		if (TextUtils.isEmpty(item.name))
			return null;
		if (item.location == null) {
			return null;
		}
		p.setCity(item.city);
		double[] xy = BDLocationUtil.Convert_BD09_To_GCJ02(
				item.location.latitude, item.location.longitude);
		p.setLat(xy[0]);
		p.setLng(xy[1]);
		p.setDistance(BDLocationUtil.calDistance(xy[0], xy[1]));
		if(!TextUtils.isEmpty(item.address) && item.address.startsWith("\"\\u")){
			StringBuilder build = new StringBuilder();
			String[] split = item.address.split(",");
			if(split != null && split.length>0 && !TextUtils.isEmpty(split[0])){
				split[0] = split[0].replace("\"", "");
				int length = split[0].length()/6;
				for(int i = 0;i<length;i++){
					build.append((char)Integer.parseInt(split[0].substring(6*i+2, 6*i+6), 16));
				}
				item.address = build.toString();
			}
		}
		p.setGeoinfo(item.address);
		p.setPostcode(item.postCode);
		p.setTelephone(item.phoneNum);
		p.setSourceType(Poi.POI_SOURCE_BAIDU_IMPL);
		return p;
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_BAIDU_IMPL)){
			JNIHelper.logd("POISearchLog:PoiSearToolBaiduImpl is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
//		NavManager.getInstance().reportNetStatus("baiduimp", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_BAIDU);
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		mReq.cancel();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=true;

		final CityPoiSearchOption optionTmp= option;
		final  String keywordTmp=keyword;
		
		checkAliveThreads();

		mPoiThread = new HandlerThread("PoiSearch");
		mPoiThread.start();
		mPoiHandler = new TXZHandler(mPoiThread.getLooper());
		synchronized (sAliveSearchThread) {
			sAliveSearchThread.add(mPoiThread);
		}
		mPoiHandler.post(new Runnable() {			
			@Override
			public void run() {
				try {
					mPoiSearch = PoiSearch.newInstance();
					mPoiSearch
							.setOnGetPoiSearchResultListener(mOnGetPoiSearchResultListener);
					mPoiSearch.searchInCity((new PoiCitySearchOption())
							.city(optionTmp.getCity()).keyword(keywordTmp)
							.pageNum(0).pageCapacity(optionTmp.getNum()));
				} catch (Exception e) {
					LogUtil.loge("PoiSearchToolBaiduImpl searchInCity Exception:" + e.getMessage());
					if (mResultListener != null) {
						mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
						mResultListener = null;
					}
				}
			}
		});
		return mReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_BAIDU_IMPL)){
			JNIHelper.logd("POISearchLog:PoiSearToolBaiduImpl is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
//		NavManager.getInstance().reportNetStatus("baiduimp", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_BAIDU);

		mReq.cancel();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=false;
		final double loc[] = BDLocationUtil.Convert_GCJ02_To_BD09(
				option.getCenterLat(), option.getCenterLng());
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			region=region+" "+keyword;
		}
		
		final CityPoiSearchOption optionTmp = option;
		final  String keywordTmp = keyword;
		
		checkAliveThreads();
		
		mPoiThread = new HandlerThread("PoiSearch");
		mPoiThread.start();
		mPoiHandler = new TXZHandler(mPoiThread.getLooper());
		synchronized (sAliveSearchThread) {
			sAliveSearchThread.add(mPoiThread);
		}
		mPoiHandler.post(new Runnable() {		
			@Override
			public void run() {
				try {
					mPoiSearch = PoiSearch.newInstance();
					mPoiSearch
							.setOnGetPoiSearchResultListener(mOnGetPoiSearchResultListener);
					int radius = ((NearbyPoiSearchOption) optionTmp).getRadius();
					if (radius > MAX_NEARBY_RADIUS) {
						radius = MAX_NEARBY_RADIUS;
					} else if (radius <= 0) {
						radius = MAX_NEARBY_RADIUS;
					}
					mPoiSearch.searchNearby(new PoiNearbySearchOption()
							.keyword(keywordTmp)
							.location(new LatLng(loc[0], loc[1])).radius(radius).pageNum(0)
							.pageCapacity(optionTmp.getNum()));
				} catch (Exception e) {
					LogUtil.loge("PoiSearchToolBaiduImpl searchInCity Exception:" + e.getMessage());
					if (mResultListener != null) {
						mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
						mResultListener = null;
					}
				}
			}
		});
		return mReq;
	}
	
	private void checkAliveThreads() {
		int n = 0;
		synchronized (sAliveSearchThread) {
			Iterator<HandlerThread> it = sAliveSearchThread.iterator();
			while (it.hasNext()) {
				HandlerThread th = it.next();
				if (th.isAlive()) {
					++n;
				} else {
					it.remove();
				}
			}
		}
		final int mHandlerMsgCount = n;
		if(mHandlerMsgCount > MAX_THREAD_COUNT_WARNING){
			JNIHelper.logd("POISearchLog: baiduImp PoiSearch Threnad count is warning -->count = "+mHandlerMsgCount);
			if(mHandlerMsgCount > MAX_THREAD_COUNT_ERROR ){
				JNIHelper.logd("POISearchLog: baiduImp PoiSearch Threnad count is error -->count = "+mHandlerMsgCount);
				RecorderWin.OBSERVABLE.registerObserver( new StatusObserver() {
					@Override
					public void onShow() {
					}
					@Override
					public void onDismiss() {
						JNIHelper.logd("POISearchLog: baiduImp PoiSearch Threnad count is error kill core ");
						RecorderWin.OBSERVABLE.unregisterObserver(this);
						CrashCommonHandler.dumpExceptionToSDCard(GlobalContext.get(),  Environment
								.getExternalStorageDirectory().getPath()
								+ "/txz/report/", null, new  TXZBaiDuSdkTheardException(mHandlerMsgCount));
						AppLogic.restartProcess();
					}
				});
				
			}
		}
	}
	
	private void releseThread(boolean isCancel){
		HandlerThread th = mPoiThread;
		if(th != null){
			th.quit();			
		}
		mPoiHandler=null;
		mPoiThread=null;
	}
	private class TXZBaiDuSdkTheardException extends RuntimeException {

		private static final long serialVersionUID = 2367467062456909149L;
		String  mThreadCount;
		public TXZBaiDuSdkTheardException(int count) {
			super("TXZInvokeTimeoutException");
			mThreadCount = ""+count;
		}
		
		@Override
		public void printStackTrace(PrintWriter err) {
			err.println("TXZBaiDuSdkThearException");
			err.println("mThreadCount=" + mThreadCount);
		}
		
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType & 1<<(Poi.POI_SOURCE_BAIDU_IMPL-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}

	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_BAIDU_IMPL;
	}


}
