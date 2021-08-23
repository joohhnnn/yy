package com.txznet.txz.component.poi.qihoo;

import java.util.ArrayList;
import java.util.List;

import com.qihu.mobile.lbs.search.Search;
import com.qihu.mobile.lbs.search.Search.SearchListener;
import com.qihu.mobile.lbs.search.SearchResult;
import com.qihu.mobile.lbs.search.SearchResult.CitySuggestion;
import com.qihu.mobile.lbs.search.SearchResult.PoiInfo;
import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.CoordType;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

import android.os.SystemClock;
import android.text.TextUtils;

public class PoiSearchToolQihooImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	public final static int MAX_NEARBY_RADIUS = 5000;
	private long mSearchTime=0;
	private Search mSearch;
	private PoiSearchResultListener mResultListener;
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	private Runnable mRunnableSearchTimeout = new Runnable() {
		@Override
		public void run() {
//			NavManager.getInstance().reportNetStatus("qihoo", "timeout");
			mSearch.cancelSearch();
			mSearch.cancelSearchNearby();
			mSearch.release();
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
						.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_QIHOO);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
				mResultListener = null;
			}
		}
	};

	SearchReq mSearchReq = new SearchReq() {
		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
			if (mSearch != null) {
				mSearch.cancelSearch();
				mSearch.cancelSearchNearby();
				mSearch.release();
			}
		}
	};

	private boolean checkSearchResult(SearchResult result) {
		switch (result.getStatus()) {
		case 0:
			break;
		case 1:
			int respCode = result.getResponseCode();
			if (respCode >= 1000) {
				MonitorUtil
						.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_QIHOO);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
			} else {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_QIHOO);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY,
						"");
			}
			return false;
		default:
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_QIHOO);
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
			return false;
		}
		if (result.getResponseCode() != 200) {
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_QIHOO);
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, ""); // 服务器异常
			return false;
		}
		return true;
	}

	private static Poi convertPoi(PoiInfo info) {
		PoiDetail poi = new PoiDetail();
		poi.setLat(info.y);
		poi.setLng(info.x);
		poi.setCity(info.city);
		poi.setProvince(info.province);
		poi.setCoordType(CoordType.GCJ02);
		// poi.setDistance((int) info.distance);
		poi.setName(info.name);
		poi.setGeoinfo(info.address);
		poi.setDistance(BDLocationUtil.calDistance(info.y, info.x));
//		poi.setHasPark(info.parking != null && !info.parking.isEmpty());
		poi.setTelephone(info.telephone);
		poi.setSourceType(Poi.POI_SOURCE_QIHOO);
		return poi;
	}

	SearchListener mSearchListener = new SearchListener() {
		@Override
		public void onSearchSuggestion(SearchResult result) {
			AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
			if (!checkSearchResult(result)) {
				return;
			}
			SearchPoiSuggestion suggestion = new SearchPoiSuggestion();
			List<CitySuggestion> citySuggestions = result.getCitySuggestion();
			if (citySuggestions != null && citySuggestions.size() > 0) {
				List<String> strCitySuggs = new ArrayList<String>(
						citySuggestions.size());
				for (CitySuggestion citySuggestion : citySuggestions) {
					strCitySuggs.add(citySuggestion.name);
				}
				suggestion.setCity(strCitySuggs);
			}
			if (!TextUtils.isEmpty(result.getKeyword())) {
				List<String> kws = new ArrayList<String>(1);
				kws.add(result.getKeyword());
				suggestion.setKeywrods(kws);
			}
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_SUGGEST_ALL);
			mResultListener.onSuggestion(suggestion);
		}

		@Override
		public void onSearchResult(SearchResult result) {
			doResult(result);
		}

		@Override
		public void onSearchPoi(SearchResult result) {
		}

		@Override
		public void onSearchNearby(SearchResult result) {
			doResult(result);
		}

		@Override
		public void onSearchMapPoi(SearchResult result) {

		}

		@Override
		public void onSearchBus(SearchResult result) {

		}
	};

	private void doResult(SearchResult result) {
		System.out.println(result);
		AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
//		NavManager.getInstance().reportNetStatus("qihoo", "end");
		mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
		NavManager.reportBack("qihoo",mSearchTime);
		if (!checkSearchResult(result)) {
			return;
		}
		List<PoiInfo> poiInfos = result.getList();
		if (poiInfos != null && !poiInfos.isEmpty()) {
			List<Poi> res = new ArrayList<Poi>(poiInfos.size());
			for (PoiInfo poiInfo : poiInfos) {
				Poi poi = convertPoi(poiInfo);
				res.add(poi);
			}
			if (mResultListener != null) {
				MonitorUtil
						.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_QIHOO);
				JNIHelper.logd("POISearchLog: PoiSearchToolQihooImpl return Poi count= "+res.size());
				NavManager.getInstance().setNomCityList(res);
				PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout()-mSearchTime,mResultListener,res);
			}
		} else {
			if (mResultListener != null) {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_QIHOO);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY,
						"");
			}
		}
	}

	public PoiSearchToolQihooImpl() {
		if (mSearch != null) {
			mSearch.release();
		}
		mSearch = new Search(mSearchListener);
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_QIHOO)){
			JNIHelper.logd("POISearchLog:PoiSearToolQihoo is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		mSearch.cancelSearch();
		mSearch.cancelSearchNearby();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=true;
		String city = option.getCity();
		if (!TextUtils.isEmpty(city)) {
			mSearch.setCityName(city);
		}
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
//		NavManager.getInstance().reportNetStatus("qihoo", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		mSearch.search(keyword, 0, option.getNum());
		return mSearchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_QIHOO)){
			JNIHelper.logd("POISearchLog:PoiSearToolQihoo is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		mSearch.cancelSearch();
		mSearch.cancelSearchNearby();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=false;
		String city = option.getCity();
		if (!TextUtils.isEmpty(city)) {
			mSearch.setCityName(city);
		}
		int radius = option.getRadius();
		if (radius > MAX_NEARBY_RADIUS) {
			radius = MAX_NEARBY_RADIUS;
		} else if (radius <= 0) {
			radius = TXZPoiSearchManager.DEFAULT_NEARBY_RADIUS;
		}
		mSearch.setLocation(option.getCenterLat(), option.getCenterLng()); // 这里实际入参是lat,
																			// lng
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
//		NavManager.getInstance().reportNetStatus("qihoo", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		mSearch.searchNearby(keyword, radius, 0, option.getNum());
		return mSearchReq;
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_QIHOO-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_QIHOO;
	}
}
