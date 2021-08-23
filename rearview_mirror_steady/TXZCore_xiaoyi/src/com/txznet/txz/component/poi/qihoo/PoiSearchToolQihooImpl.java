package com.txznet.txz.component.poi.qihoo;

import java.util.ArrayList;
import java.util.List;

import com.qihu.mobile.lbs.search.Search;
import com.qihu.mobile.lbs.search.Search.SearchListener;
import com.qihu.mobile.lbs.search.SearchResult;
import com.qihu.mobile.lbs.search.SearchResult.CitySuggestion;
import com.qihu.mobile.lbs.search.SearchResult.PoiInfo;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.CoordType;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

import android.text.TextUtils;

public class PoiSearchToolQihooImpl implements
		TXZPoiSearchManager.PoiSearchTool {
	public final static int MAX_NEARBY_RADIUS = 5000;

	private Search mSearch;
	private PoiSearchResultListener mResultListener;
	private Runnable mRunnableSearchTimeout = new Runnable() {
		@Override
		public void run() {
			mSearch.cancelSearch();
			mSearch.cancelSearchNearby();
			mSearch.release();
			if (mResultListener != null) {
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
				mResultListener.onResult(res);
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
		mSearch.cancelSearch();
		mSearch.cancelSearchNearby();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		mResultListener = listener;
		String city = option.getCity();
		if (!TextUtils.isEmpty(city)) {
			mSearch.setCityName(city);
		}
		mSearch.search(option.getKeywords(), 0, option.getNum());
		return mSearchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		mSearch.cancelSearch();
		mSearch.cancelSearchNearby();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		mResultListener = listener;
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
		mSearch.searchNearby(option.getKeywords(), radius, 0, option.getNum());
		return mSearchReq;
	}
}
