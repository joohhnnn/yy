package com.txznet.txz.component.poi.gaode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.text.TextUtils;

import com.amap.api.services.core.LatLonPoint;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.Option;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.PoiQueryResultListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.txz.module.nav.NavManager;

public class PoiSearchToolGDLocalImpl implements PoiSearchTool {
	final static int KWS_SEARCH_TYPE = 10023;
	final static int NEAR_SEARCH_TYPE = 10024;
	final static int KWS_RESULT_TYPE = 10042;
	final static int NEAR_RESULT_TYPE = 10043;

	private int mSearchRaduis;
	private Option mSeqOption;
	private PoiSearchResultListener mPoiSearchResutlListener;
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	private long mSearchTime= 0;
	PoiQueryResultListener mInnerListener = new PoiQueryResultListener() {

		@Override
		public void onResult(String strData) {
			mSearchTime=SystemClock.elapsedRealtime()-mSearchTime;
//			NavManager.getInstance().reportNetStatus("gaodeLoc", "end");
			NavManager.reportBack("gaodeLoc",mSearchTime);
			onSearchResultListener(strData);
		}
	};
	
	SearchSession mSearchReq = new SearchSession() {

		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mSearchTimeOut);
			PoiQuery.getInstance().cancel(mSeqOption);
			mGiveUpResult = true;
		}
	};

	public abstract class SearchSession implements SearchReq {
		boolean mGiveUpResult;

		public boolean isGiveUpResult() {
			return mGiveUpResult;
		}
	}

	Runnable mSearchTimeOut = new Runnable() {

		@Override
		public void run() {
			JNIHelper.loge("gdlocal searchTimeOut");
			if (mSearchReq != null) {
				mSearchReq.cancel();
			}

			if (mPoiSearchResutlListener != null) {
				JNIHelper.logd("POISearchLog:"+getClass().toString()+" mRetryCount="+mRetryCount);
				if(mRetryCount>0){
					mRetryCount--;
					mSearchReq.mGiveUpResult = false;
					if(isCitySearch){
						searchInCity((CityPoiSearchOption)mOption, mPoiSearchResutlListener);
					}else{
						searchNearby((NearbyPoiSearchOption)mOption, mPoiSearchResutlListener);
					}
					return;
				}
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE_OFFLINE);
				mPoiSearchResutlListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
//				NavManager.getInstance().reportNetStatus("gaodeLoc", "timeout");
				mPoiSearchResutlListener = null;
			}
		}
	};

	private SearchReq search(int searchType, String kws,String city, int num, LatLonPoint myPoint,
			LatLonPoint centerPoi) {

		JNIHelper.logd("start query...");
//		NavManager.getInstance().reportNetStatus("gaodeLoc", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		
		Option option = new Option();
		option.searchType = searchType;
		option.myPoint = myPoint;
		option.searchRaduis = mSearchRaduis;
		option.kws=kws;
		option.city = city;
		option.num = num;
		option.centerPoi = centerPoi;
		option.mResultListener = mInnerListener;
		mSeqOption = option;
		NavAmapValueService.getInstance().checkAutoIsAlive(searchType);
		PoiQuery.getInstance().startQuery(option);
		return mSearchReq;
	}
	
	@Override
	public SearchReq searchInCity(CityPoiSearchOption option, PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_LOCAL)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeLocal is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_GAODE_OFFLINE);

		mPoiSearchResutlListener = listener;
		mSearchRaduis = -1;
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		AppLogic.runOnBackGround(mSearchTimeOut, option.getTimeout());
		isCitySearch=true;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		return search(KWS_SEARCH_TYPE, keyword,option.getCity(), option.getNum(), getMyLatLon(), null);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option, PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_LOCAL)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeLocal is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_GAODE_OFFLINE);
		mPoiSearchResutlListener = listener;
		mSearchRaduis = option.getRadius();
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		AppLogic.runOnBackGround(mSearchTimeOut, option.getTimeout());
		isCitySearch=false;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		return search(NEAR_SEARCH_TYPE,keyword,option.getCity(), option.getNum(), getMyLatLon(),
				new LatLonPoint(option.getCenterLat(), option.getCenterLng()));
	}

	private LatLonPoint getMyLatLon() {
		LocationInfo mInfo = LocationManager.getInstance().getLastLocation();
		if (mInfo == null) {
			mInfo = new LocationInfo();
			return null;
		}
		if (mInfo.msgGpsInfo == null) {
			mInfo.msgGpsInfo = new GpsInfo();
			return null;
		}
		if (mInfo.msgGpsInfo.dblLat == null || mInfo.msgGpsInfo.dblLng == null) {
			return null;
		}

		return new LatLonPoint(mInfo.msgGpsInfo.dblLat, mInfo.msgGpsInfo.dblLng);
	}

	private void onSearchResultListener(String jsonDatas) {
		AppLogic.removeBackGroundCallback(mSearchTimeOut);
		if (mPoiSearchResutlListener == null) {
			return;
		}

		if (mSearchReq != null && mSearchReq.isGiveUpResult()) {
			JNIHelper.logd("POISearchLog:PoiSearchToolGDLocalImpl ERROR_CODE_TIMEOUT isGiveUpResult");
			doError(MonitorUtil.POISEARCH_EMPTY_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
					"give up search result");
			return;
		}

		if (TextUtils.isEmpty(jsonDatas)) {
			JNIHelper.logd("POISearchLog:PoiSearchToolGDLocalImpl ERROR_CODE_EMPTY");
			doError(MonitorUtil.POISEARCH_EMPTY_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return;
		}

		ArrayList<Poi> pois = null;
		ArrayList<String> cityList = new ArrayList<String>();
		try {
			pois = getPoiFromJson(jsonDatas,cityList);
		} catch (JSONException e) {
			JNIHelper.logd("POISearchLog:PoiSearchToolGDLocalImpl ERROR_CODE_UNKNOW");
			doError(MonitorUtil.POISEARCH_ERROR_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_UNKNOW, "JSONException");
		}

		if (pois != null && pois.size() > 0) {
			JNIHelper.logd("POISearchLog:PoiSearchToolGDLocalImpl pois.size() > 0");
			JNIHelper.logd("POISearchLog: PoiSearchToolGDLocalImpl time =  "+(mOption.getTimeout()-mSearchTime));
			PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout()-mSearchTime,mPoiSearchResutlListener,pois);
			return;
//		}else if(cityList.size()  > 0){
//			MonitorUtil
//				.monitorCumulant(MonitorUtil.POISEARCH_SUGGEST_ALL);
//			JNIHelper.logd("POISearchLog: gaodeLoc suggestCityList = "+cityList);
//			List<String> keyWord =  new ArrayList<String>();
//			keyWord.add(mOption.getKeywords());
//			mPoiSearchResutlListener.onSuggestion(
//				new SearchPoiSuggestion().setCity(cityList).setKeywrods(keyWord));
//			return;
		}

		doError(MonitorUtil.POISEARCH_EMPTY_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
	}

	private void doError(String monitCode, int errCode, String errDesc) {
		MonitorUtil.monitorCumulant(monitCode);
		if (mPoiSearchResutlListener != null) {
			mPoiSearchResutlListener.onError(errCode, errDesc);
			mPoiSearchResutlListener = null;
		}
	}

	private ArrayList<Poi> getPoiFromJson(String jsonResult, ArrayList<String> cityList) throws JSONException {
		ArrayList<Poi> pois = new ArrayList<Poi>();
		JSONArray jsonArray = new JSONArray(jsonResult);
		
		for (int i = 0; i < jsonArray.length(); i++) {
			JSONObject jo = (JSONObject) jsonArray.get(i);
			Poi poi = new PoiDetail();
			if (jo.has("name")) {
				poi.setName(jo.getString("name"));
			}
			if (jo.has("address")) {
				poi.setGeoinfo(jo.getString("address"));
			}
			if (jo.has("latitude")) {
				poi.setLat(jo.getDouble("latitude"));
			}
			if (jo.has("longitude")) {
				poi.setLng(jo.getDouble("longitude"));
			}
			if (jo.has("distance")) {
				poi.setDistance((int) jo.getDouble("distance"));
			}
			if(jo.has("poideepinfo")){
				poi.setExtraStr(jo.getString("poideepinfo"));
			}
			if(poi.getLat() == 0 && poi.getLng() == 0 && jo.has("city_suggestion_name")){
				String city = jo.getString("city_suggestion_name");
				if(! TextUtils.isEmpty(city)){
					cityList.add(city);
				}		
				continue;
			}
			if (jo.has("tel")) {

			}
			poi.setSourceType(Poi.POI_SOURCE_GAODE_LOCAL);
			pois.add(poi);
		}
		return pois;
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_GAODE_LOCAL-1)) != 0 && mPoiSearchResutlListener != null){
			mPoiSearchResutlListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mPoiSearchResutlListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_GAODE_LOCAL;
	}
}
