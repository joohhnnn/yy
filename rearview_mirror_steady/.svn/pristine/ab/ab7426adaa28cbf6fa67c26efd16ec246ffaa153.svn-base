package com.txznet.txz.component.poi.gaode;

import java.util.ArrayList;
import java.util.List;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

@SuppressWarnings("deprecation")
public class PoiSearchToolGaodeImpl implements
		TXZPoiSearchManager.PoiSearchTool {

	public final static int MAX_NEARBY_RADIUS = 10000;

	private static Poi getPoiFromPoiItem(PoiItem item) {
		PoiDetail poi = new PoiDetail();
		poi.setCity(item.getCityName());
		double lat, lng;
		if (item.getEnter() != null) {
			lat = item.getEnter().getLatitude();
			lng = item.getEnter().getLongitude();
		} else if (item.getLatLonPoint() != null) {
			lat = item.getLatLonPoint().getLatitude();
			lng = item.getLatLonPoint().getLongitude();
		} else {
			return null;
		}
		poi.setLat(lat);
		poi.setLng(lng);
		poi.setGeoinfo(item.getSnippet());
		// poi.setDistance(item.getDistance());
		poi.setDistance(BDLocationUtil.calDistance(lat, lng));
		poi.setName(item.getTitle());
		poi.setTelephone(item.getTel());
		poi.setWebsite(item.getWebsite());
		poi.setSourceType(Poi.POI_SOURCE_GAODE_IMPL);
		return poi;
	}

	PoiSearch mPoiSearch = null;
	SearchReq mSearchReq = new SearchReq() {
		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
			if (mPoiSearch != null) {
				mPoiSearch.setOnPoiSearchListener(mCancelListener);
				mPoiSearch = null;
			}
		}
	};

	OnPoiSearchListener mCancelListener = new OnPoiSearchListener() {
		@Override
		public void onPoiItemDetailSearched(PoiItemDetail arg0, int arg1) {
		}

		@Override
		public void onPoiSearched(PoiResult arg0, int arg1) {
		}
	};

	PoiSearchResultListener mResultListener = null;

	Runnable mRunnableSearchTimeout = new Runnable() {
		@Override
		public void run() {
			mSearchReq.cancel();
			if (mResultListener != null) {
				MonitorUtil
						.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
				mResultListener = null;
			}
		}
	};

	private SearchReq searchPoi(PoiSearch poi, PoiSearchResultListener listener) {
		mPoiSearch = poi;
		mResultListener = listener;

		try {
			poi.setOnPoiSearchListener(new OnPoiSearchListener() {
				@Override
				public void onPoiSearched(PoiResult poiResult, int rCode) {
					mPoiSearch = null;
					AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);

					if (rCode != 0) {
						// 出错
						switch (rCode) {
						case 23:
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE);
							mResultListener.onError(
									TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
							break;
						case 22:
						case 27:
						case 28:
						case 29:
						case 30:
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE);
							mResultListener.onError(
									TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
							break;
						case 31:
						default:
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_GAODE);
							mResultListener.onError(
									TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
							break;
						}
						return;
					}

					ArrayList<PoiItem> pois = poiResult.getPois();

					if (pois != null && !pois.isEmpty()) {
						List<Poi> res = new ArrayList<Poi>();
						for (PoiItem item : pois) {
							Poi p = getPoiFromPoiItem(item);
							if (p == null)
								continue;
							res.add(p);
						}
						// 返回结果
						if (!res.isEmpty()) {
							// Collections.sort(res, new
							// PoiComparator_Distance());
							MonitorUtil
									.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_GAODE);
							mResultListener.onResult(res);
							return;
						}
					}

					List<String> lstKws = poiResult
							.getSearchSuggestionKeywords();
					List<SuggestionCity> lstSuggestionCities = poiResult
							.getSearchSuggestionCitys();
					List<String> lstCities = new ArrayList<String>();
					if (lstSuggestionCities != null) {
						for (SuggestionCity city : lstSuggestionCities) {
							lstCities.add(city.getCityName());
						}
					}
					// 搜索建议
					if ((lstKws != null && !lstKws.isEmpty())
							|| (lstCities != null && !lstCities.isEmpty())) {
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_SUGGEST_ALL);
						mResultListener.onSuggestion(new SearchPoiSuggestion()
								.setCity(lstCities).setKeywrods(lstKws));
						return;
					}
					// 结果为空
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_GAODE);
					mResultListener.onError(
							TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				}

				@Override
				public void onPoiItemDetailSearched(
						PoiItemDetail poiItemDetail, int rCode) {
				}
			});
			poi.searchPOIAsyn();
			return mSearchReq;
		} catch (Exception e) {
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_GAODE);
			listener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
			return null;
		}
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE);

		mSearchReq.cancel();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());

		Query query = new Query(option.getKeywords(), null, option.getCity());
		query.setPageNum(0);
		query.setPageSize(option.getNum());
		PoiSearch poi = new PoiSearch(GlobalContext.get(), query);
		return searchPoi(poi, listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE);

		mSearchReq.cancel();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());

		Query query = new Query(option.getKeywords(), option.getCity());
		query.setPageNum(0);
		query.setPageSize(option.getNum());
		PoiSearch poi = new PoiSearch(GlobalContext.get(), query);
		int radius = option.getRadius();
		if (radius > MAX_NEARBY_RADIUS) {
			radius = MAX_NEARBY_RADIUS;
		} else if (radius <= 0) {
			radius = TXZPoiSearchManager.DEFAULT_NEARBY_RADIUS;
		}

		poi.setBound(new SearchBound(new LatLonPoint(option.getCenterLat(),
				option.getCenterLng()), radius));
		return searchPoi(poi, listener);
	}
}
