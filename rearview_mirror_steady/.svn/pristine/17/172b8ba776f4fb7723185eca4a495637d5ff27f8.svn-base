package com.txznet.txz.component.poi.txz;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.util.Log;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.Resp_POI_Search;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.TxzPoi;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.NavManager.IRespPoiSearch;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolTxzNPLImpl implements
		TXZPoiSearchManager.PoiSearchTool, IRespPoiSearch {
	private PoiSearchResultListener mResultListener;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	public PoiSearchToolTxzNPLImpl(boolean bBusiness) {
		mPoiSearchReq.bBusiness = bBusiness;
		NavManager.getInstance().registerPoiSeachCallback(this);

	}
	private PoiSearchOption mOption;
	private long mSearchTime= 0;
	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {

		mSearchReq.cancel();
		mResultListener = listener;
		mPoiSearchReq.bNear = false;
		if(option.getCity()!=null){
			mPoiSearchReq.targetCity = option.getCity().getBytes();
		}else{
			mPoiSearchReq.targetCity=null;
		}
		isCitySearch=true;
		search(option);
		return mSearchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {

		mSearchReq.cancel();
		mResultListener = listener;
		mPoiSearchReq.bNear = true;
		isCitySearch=false;
		search(option);
		return mSearchReq;
	}

	SearchReq mSearchReq = new SearchReq() {

		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mTimeOutRunnable);
			if(mOption!=null)
				mOption.getSearchInfo().setTxzPoiToolComplete(true);
			mResultListener = null;
		}
	};

	Runnable mTimeOutRunnable = new Runnable() {

		@Override
		public void run() {
			if (mResultListener != null) {
				JNIHelper.logd("TxzNplSearch is end");
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
//				NavManager.getInstance().reportNetStatus("txz", "timeout");
				mOption.getSearchInfo().setTxzPoiToolComplete(true);
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_TXZ);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
				mResultListener = null;
			}
		}
	};

	private UiEquipment.Req_POI_Search mPoiSearchReq = new UiEquipment.Req_POI_Search();

	private void search(PoiSearchOption option) {
		JNIHelper.logd("TxzNplSearch is start");
		AppLogic.runOnBackGround(mTimeOutRunnable, option.getTimeout());
		mOption=option;
		option.getSearchInfo().setTxzPoiToolComplete(false);
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		mPoiSearchReq.keyWord = option.getKeywords().getBytes();
		mPoiSearchReq.searchCount = option.getNum();
		LocationInfo loc = LocationManager.getInstance().getLastLocation();
		if (loc != null && loc.msgGpsInfo != null) {
			mPoiSearchReq.lat = loc.msgGpsInfo.dblLat;
			mPoiSearchReq.lng = loc.msgGpsInfo.dblLng;
			mPoiSearchReq.currentCity = loc.msgGeoInfo.strCity.getBytes();
		}
//		NavManager.getInstance().reportNetStatus("txz", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_POI_SEARCH, mPoiSearchReq);

		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,
				MonitorUtil.POISEARCH_ENTER_TXZ);

	}

	@Override
	public void onResult(Resp_POI_Search respPoi) {
//		NavManager.getInstance().reportNetStatus("txz", "end");
		mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
		NavManager.reportBack("txz", mSearchTime);
		if (respPoi == null||respPoi.strJsonResult==null) {
			
			if(mResultListener!=null){
				mOption.getSearchInfo().setTxzPoiToolComplete(true);
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_TXZ);
				
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, ""
						+ "onResult==null");
			}else{
				JNIHelper.logd("PoiSearchToolTxz mResultListener is null");
			}
			return;
		}
		
		String jsonResult = new String(respPoi.strJsonResult);
		if(respPoi.disabledSearchEngine==null)
			respPoi.disabledSearchEngine=1;
		mOption.getSearchInfo().setDisShowEngine(respPoi.disabledSearchEngine);
		JNIHelper.logd("TxzServcer Poi search result setDisShowEngine =  " + respPoi.disabledSearchEngine);
		JNIHelper.logd("TxzServcer Poi search result is " + jsonResult);
		JSONObject json = new JSONBuilder(jsonResult).build();
		String action = null;
		try {
			action = json.getString("action");
			if (action.equals("company_navi")) {
				String companyString = json.getString("companies");
				List<Poi> companyPoiList = TxzPoi
						.getCompanyPoiForJson(companyString);
				if (companyPoiList != null && companyPoiList.size() > 0) {
					for (Poi poi : companyPoiList) {
						((TxzPoi) poi).setDistance(BDLocationUtil.calDistance(
								poi.getLat(), poi.getLng()));
						poi.setSourceType(Poi.POI_SOURCE_TXZ);
					}
				}
				if (companyPoiList == null || companyPoiList.isEmpty()) {
					if (mResultListener != null) {
						mOption.getSearchInfo().setTxzPoiToolComplete(true);
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_ERROR_TXZ);
						mResultListener.onError(
								TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
					}
				} else {
					if (mResultListener != null) {
						mOption.getSearchInfo().setTxzPoiToolComplete(true);
						MonitorUtil
								.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_TXZ);
						JNIHelper.logd("POISearchLog: PoiSearchToolTxzNPLImpl return Poi count= "+companyPoiList.size());
						NavManager.getInstance().setNomCityList(companyPoiList);
						PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout() - mSearchTime, mResultListener, companyPoiList);
					}
				}
			} else {
				if (mResultListener != null) {
					mOption.getSearchInfo().setTxzPoiToolComplete(true);
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_ERROR_TXZ);
					mResultListener.onError(
							TXZPoiSearchManager.ERROR_CODE_UNKNOW, "");
				}
			}
		} catch (JSONException e) {
			if (mResultListener != null) {
				mOption.getSearchInfo().setTxzPoiToolComplete(true);
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_TXZ);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW,
						"");
			}
		}
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_TXZ-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}

	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_TXZ;
	}

}
