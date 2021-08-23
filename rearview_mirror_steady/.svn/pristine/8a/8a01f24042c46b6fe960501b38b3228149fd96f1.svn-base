package com.txznet.txz.component.poi.gaode;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.amap.api.services.core.LatLonPoint;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.PoiDetail;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.Option;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.PoiQueryResultListener;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;

import android.text.TextUtils;

public class PoiSearchToolGDLocalImpl implements PoiSearchTool {
	final static int KWS_SEARCH_TYPE = 10023;
	final static int NEAR_SEARCH_TYPE = 10024;
	final static int KWS_RESULT_TYPE = 10042;
	final static int NEAR_RESULT_TYPE = 10043;

	int mSearchRaduis;
	SearchSession mSearchReq;
	PoiSearchResultListener mPoiSearchResutlListener;

	PoiQueryResultListener mInnerListener = new PoiQueryResultListener() {

		@Override
		public void onResult(String strData) {
			onSearchResultListener(strData);
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
			JNIHelper.loge(getClass().getName() + " searchTimeOut");
			if (mSearchReq != null) {
				mSearchReq.cancel();
			}

			if (mPoiSearchResutlListener != null) {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE_OFFLINE);
				mPoiSearchResutlListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				mPoiSearchResutlListener = null;
			}
		}
	};

	private SearchReq search(int searchType, String kws, String city, int num, LatLonPoint myPoint,
			LatLonPoint centerPoi) {
		JNIHelper.logd("start query...");
		Option option = new Option();
		option.searchType = searchType;
		option.kws = kws;
		option.myPoint = myPoint;
		option.searchRaduis = mSearchRaduis;
		option.city = city;
		option.num = num;
		option.centerPoi = centerPoi;
		option.mResultListener = mInnerListener;
		PoiQuery.getInstance().startQuery(option);
		return mSearchReq = new SearchSession() {

			@Override
			public void cancel() {
				AppLogic.removeBackGroundCallback(mSearchTimeOut);
				PoiQuery.getInstance().cancel();
				mGiveUpResult = true;
			}
		};
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option, PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_GAODE_OFFLINE);

		mPoiSearchResutlListener = listener;
		mSearchRaduis = -1;
		AppLogic.runOnBackGround(mSearchTimeOut, option.getTimeout());
		return search(KWS_SEARCH_TYPE, option.getKeywords(), option.getCity(), option.getNum(), getMyLatLon(), null);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option, PoiSearchResultListener listener) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_GAODE_OFFLINE);
		mPoiSearchResutlListener = listener;
		mSearchRaduis = option.getRadius();
		AppLogic.runOnBackGround(mSearchTimeOut, option.getTimeout());
		return search(NEAR_SEARCH_TYPE, option.getKeywords(), option.getCity(), option.getNum(), getMyLatLon(),
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
			JNIHelper.logd("give up result");
			doError(MonitorUtil.POISEARCH_EMPTY_GAODE_OFFLINE, TXZPoiSearchManager.DEFAULT_SEARCH_TIMEOUT,
					"give up search result");
			return;
		}

		if (TextUtils.isEmpty(jsonDatas)) {
			doError(MonitorUtil.POISEARCH_EMPTY_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return;
		}

		ArrayList<Poi> pois = null;
		try {
			pois = getPoiFromJson(jsonDatas);
		} catch (JSONException e) {
			doError(MonitorUtil.POISEARCH_ERROR_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_UNKNOW, "JSONException");
		}

		if (pois != null && pois.size() > 0) {
			mPoiSearchResutlListener.onResult(pois);
			return;
		}

		doError(MonitorUtil.POISEARCH_EMPTY_GAODE_OFFLINE, TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
	}

	private void doError(String monitCode, int errCode, String errDesc) {
		MonitorUtil.monitorCumulant(monitCode);
		if (mPoiSearchResutlListener != null) {
			mPoiSearchResutlListener.onError(errCode, errDesc);
		}
	}

	private ArrayList<Poi> getPoiFromJson(String jsonResult) throws JSONException {
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
			if (jo.has("tel")) {
				// TODO
			}
			pois.add(poi);
		}
		return pois;
	}
}
