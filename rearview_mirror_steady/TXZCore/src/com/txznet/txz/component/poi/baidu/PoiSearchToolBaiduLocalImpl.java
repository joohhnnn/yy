package com.txznet.txz.component.poi.baidu;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
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
import com.txznet.txz.component.nav.baidu.BDConstants;
import com.txznet.txz.component.nav.baidu.BDConstants.BDHelper;
import com.txznet.txz.component.nav.baidu.BaiduVersion;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor.ExecuteCallBack;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor.ExecuteReq;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor.ExecuteTask;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

import android.os.SystemClock;
import android.text.TextUtils;

import static com.txznet.txz.component.nav.baidu.BDConstants.GCJ_BD_RATE;

public class PoiSearchToolBaiduLocalImpl implements PoiSearchTool {

	private PoiSearchResultListener mResultListener;
	boolean isCitySearch=false;
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	private long mSearchTime= 0;
	Runnable mTimeOutRunnable = new Runnable() {

		@Override
		public void run() {
			if (mResultListener != null) {
				JNIHelper.logd("POISearchLog:BaiduLocal mRetryCount="+mRetryCount);
				if(mRetryCount>0){
					mRetryCount--;
					if(isCitySearch){
						searchInCity((CityPoiSearchOption)mOption, mResultListener);
					}else{
						searchNearby((NearbyPoiSearchOption)mOption, mResultListener);
					}
					return;
				}
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_BAIDU_OFFLINE);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
//				NavManager.getInstance().reportNetStatus("baiduloc", "timeout");
				mResultListener = null;
			}
		}
	};

	SearchReq mSearchReq = new SearchReq() {

		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mTimeOutRunnable);
			mResultListener = null;
		}
	};

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option, PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_BAIDU_LOCAL)){
			JNIHelper.logd("POISearchLog:BaiduLocal is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		mSearchReq.cancel();
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=true;
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		search(keyword, option.getTimeout());
		return mSearchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option, PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_BAIDU_LOCAL)){
			JNIHelper.logd("POISearchLog:BaiduLocal is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		mSearchReq.cancel();
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=false;
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		search(keyword,option.getTimeout());
		return mSearchReq;
	}

	private void search(final String keywords,long delay) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_BAIDU_OFFLINE);
//		NavManager.getInstance().reportNetStatus("baiduloc", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		if (!PackageManager.getInstance().checkAppExist(BaiduVersion.getCurPackageName())) {
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_OFFLINE);
			if (mResultListener != null) {
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			}
			return;
		}

		AppLogic.runOnBackGround(mTimeOutRunnable, delay);
		final String reqId = BDHelper.getRequestId();
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.queryOfflinePois(reqId, keywords);
				return reqId.equals(rId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
				AppLogic.removeBackGroundCallback(mTimeOutRunnable);
				mSearchTime=SystemClock.elapsedRealtime()-mSearchTime;
//				NavManager.getInstance().reportNetStatus("baiduloc", "end");
				NavManager.reportBack("baiduloc",mSearchTime);
				JNIHelper.logd("POISearchLog:BaiduLocal:bSucc " + bSucc + "," + params);
				if (bSucc) {
					if (!TextUtils.isEmpty(params)) {
						try {
							List<Poi> pois = new ArrayList<Poi>();
							JSONBuilder jBuilder = new JSONBuilder(params);
							JSONArray jsonArray = jBuilder.getVal("res", JSONArray.class);
							if (jsonArray == null) {
								jsonArray = jBuilder.getVal("result", JSONArray.class);
							}
							for (int i = 0; i < jsonArray.length(); i++) {
								Poi poi = convertPoiFromJsonObject((JSONObject) jsonArray.get(i));
								if (poi != null) {
									pois.add(poi);
								}
							}
							if (mResultListener != null) {
								MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_BAIDU_OFFLINE);
								JNIHelper.logd("POISearchLog:BaiduLocal return Poi count= "+pois.size());
								NavManager.getInstance().setNomCityList(pois);
								PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout() - mSearchTime, mResultListener, pois);
							}
						} catch (JSONException e) {
							if (mResultListener != null) {
								MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_OFFLINE);
								mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "JSONException");
							}
						}
						return;
					}
					if (mResultListener != null) {
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_OFFLINE);
						mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
					}
				} else {
					onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				}
			}

			@Override
			public void onError(int error, String des) {
				AppLogic.removeBackGroundCallback(mTimeOutRunnable);
				JNIHelper.logd("POISearchLog:BaiduLocal:error " + error + "," + des);
				if (mResultListener != null) {
					if (error == ExecuteCallBack.ERROR_TIME_OUT) {
						if(NavAppManager.getInstance().isAlreadyExitNav()){
							error = TXZPoiSearchManager.ERROR_CODE_NAVICLOSE;
						}else {
							error = TXZPoiSearchManager.ERROR_CODE_TIMEOUT;
						}
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_BAIDU_OFFLINE);
					} else if (error == ExecuteCallBack.ERROR_NULL) {
						error = TXZPoiSearchManager.ERROR_CODE_EMPTY;
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_OFFLINE);
					} else if (error == ExecuteCallBack.ERROR_EXEC) {
						error = TXZPoiSearchManager.ERROR_CODE_EMPTY;
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_OFFLINE);
					} else {
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_BAIDU_OFFLINE);
					}

					mResultListener.onError(error, des);
				}
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, BDConstants.FUN_NAVI_OFFLINE_POI, eTask, callBack);
	}

	private Poi convertPoiFromJsonObject(JSONObject jsonObject) {
		try {
			JNIHelper.logd("POISearchLog:BaiduLocal poiJson="+jsonObject);
			Poi poi = new PoiDetail();
			double lat = jsonObject.optDouble("lat");
			double lng = jsonObject.optDouble("lng");
			String name = jsonObject.optString("name");
			String addr = jsonObject.optString("address");
			if (addr.equals("")){
				addr = jsonObject.optString("addr");
			}
			int distance = jsonObject.optInt("distance");
			double dLat;
			if (lat / GCJ_BD_RATE >= 1){
				dLat= BDHelper.convertDouble((int) lat);
			}else {
				dLat=lat;
			}

			double dLng;
			if (lat / GCJ_BD_RATE >= 1){
				dLng= BDHelper.convertDouble((int) lng);
			}else {
				dLng=lng;
			}
			if (distance == -1 || distance == 0) {
				distance = BDLocationUtil.calDistance(dLat, dLng);
			}
			poi.setSourceType(Poi.POI_SOURCE_BAIDU_LOCAL);
			poi.setLat(dLat);
			poi.setLng(dLng);
			poi.setName(name);
			poi.setDistance(distance);
			poi.setGeoinfo(addr);
			return poi;
		} catch (Exception e) {
			return null;
		}
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_BAIDU_LOCAL-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_BAIDU_LOCAL;
	}
}