package com.txznet.txz.component.poi.gaode;

import java.util.ArrayList;
import java.util.List;

import android.os.SystemClock;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.poisearch.PoiItemDetail;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;
import com.amap.api.services.poisearch.PoiSearch.OnPoiSearchListener;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.GlobalContext;
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
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolGaodeImpl implements
		TXZPoiSearchManager.PoiSearchTool {

	public final static int MAX_NEARBY_RADIUS = 10000;
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	private long mSearchTime= 0;
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
		if(poi.getGeoinfo().equals("{}")){
			return null;
		}
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
						.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
//				NavManager.getInstance().reportNetStatus("gaodeImp", "timeout");
				mResultListener = null;
			}
		}
	};

	private SearchReq searchPoi(PoiSearch poi, PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_GAODE);
		mPoiSearch = poi;
		mResultListener = listener;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();
		mSearchTime=SystemClock.elapsedRealtime();
		try {
			poi.setOnPoiSearchListener(new OnPoiSearchListener() {
				@Override
				public void onPoiSearched(PoiResult poiResult, int rCode) {
					mSearchTime=SystemClock.elapsedRealtime()-mSearchTime;
					NavManager.reportBack("gaodeImp",mSearchTime);
					mPoiSearch = null;
					AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
					JNIHelper.logd("POISearchLog:gaodeimp onPoiSearched errorCode  = "+rCode );
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
							JNIHelper.logd("POISearchLog: PoiSearchToolGaodeImpl return Poi count= "+pois.size());
							NavManager.getInstance().setNomCityList(res);
							PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout() - mSearchTime, mResultListener, res);
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
					JNIHelper.logd("POISearchLog:gaodeimp onPoiItemDetailSearched errorCode  = "+rCode );
					
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
		PoiSearch poi = getPoiSearch(option);
		if(poi == null){
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		isCitySearch=true;
		return searchPoi(poi, listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		PoiSearch poi = getPoiSearch(option);
		if(poi == null){
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		int radius = option.getRadius();
		if (radius > MAX_NEARBY_RADIUS) {
			radius = MAX_NEARBY_RADIUS;
		} else if (radius <= 0) {
			radius = MAX_NEARBY_RADIUS;
		}

		poi.setBound(new SearchBound(new LatLonPoint(option.getCenterLat(),
				option.getCenterLng()), radius));
		isCitySearch=false;
		return searchPoi(poi, listener);
	}
	private PoiSearch getPoiSearch(CityPoiSearchOption option) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_IMPL)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeImpl is unenable");
			return null;
		}
		mSearchReq.cancel();
		AppLogic.runOnBackGround(mRunnableSearchTimeout, option.getTimeout());
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		Query query = new Query(keyword, "", option.getCity());
		query.setPageNum(0); //查找第几页数据，起始为0
		query.setPageSize(option.getNum()); // 每页结果条数
		PoiSearch poi = new PoiSearch(GlobalContext.get(), query);
		mOption = option;
		return poi;
	}

	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_GAODE_IMPL-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_GAODE_IMPL;
	}
}
