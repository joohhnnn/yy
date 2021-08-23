package com.txznet.txz.component.poi.gaode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.gaode.NavAmapAutoNavImpl;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.WayPoiData;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.Option;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.PoiQueryResultListener;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.WayPoiData.WayPoi;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGDLocalImpl.SearchSession;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.ConfigFileHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolGaodeOnWay implements TXZPoiSearchManager.PoiSearchTool {
	
	final static int ONWAY_SEARCH_TYPE = 10057;
	static String TAG="";
	private PoiSearchResultListener mPoiSearchResutlListener;
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	private long mSearchTime=0;
	PoiQueryResultListener mInnerListener = new PoiQueryResultListener() {

		@Override
		public void onResult(String strData) {
//			NavManager.getInstance().reportNetStatus("gaodeloc", "end");
			mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
			NavManager.reportBack("gaodeloc",mSearchTime );
			AppLogic.removeBackGroundCallback(mSearchTimeOut);
//			onSearchResultListener(strData);
			ArrayList<Poi> poiList = getPoiFromJsonString(strData);
			if(mPoiSearchResutlListener!=null){
				if(poiList!=null&&poiList.size()>0){
					JNIHelper.logd("POISearchLog: PoiSearchToolGaodeOnWay return Poi count= "+poiList.size());
					NavManager.getInstance().setNomCityList(poiList);
					JNIHelper.logd("POISearchLog: PoiSearchToolGaodeOnWay time =  "+(mOption.getTimeout()-mSearchTime));
					PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout()-mSearchTime,mPoiSearchResutlListener,poiList);
				}else{
					mPoiSearchResutlListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				}
			}
		}
	};

	SearchReq mSearchReq = new SearchReq() {
		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mSearchTimeOut);
			PoiQuery.getInstance().cancel(mSeqOption);
		}
	};
	Runnable mSearchTimeOut = new Runnable() {

		@Override
		public void run() {
//			NavManager.getInstance().reportNetStatus("gaodeloc", "timeout");
			JNIHelper.loge("gdonway searchTimeOut");
			if (mSearchReq != null) {
				mSearchReq.cancel();
			}

			if (mPoiSearchResutlListener != null) {
				JNIHelper.logd("POISearchLog:"+getClass().toString()+" mRetryCount="+mRetryCount);
				if(mRetryCount>0){
					mRetryCount--;
					if(isCitySearch){
						searchInCity((CityPoiSearchOption)mOption, mPoiSearchResutlListener);
					}else{
						searchNearby((NearbyPoiSearchOption)mOption, mPoiSearchResutlListener);
					}
					return;
				}
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_GAODE_OFFLINE);
				mPoiSearchResutlListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
				mPoiSearchResutlListener = null;
			}
		}
	};
	private Option mSeqOption;
	
	
	
	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_LOCAL)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeLocal is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		NavThirdApp localNavImpl = NavManager.getInstance().getLocalNavImpl();
		if(localNavImpl instanceof NavAmapAutoNavImpl){
			if(((NavAmapAutoNavImpl)localNavImpl).getMapCode()<210 ){
				listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				return new SearchReq() {
					@Override
					public void cancel() {
					
					}
				};				
			}
		}
		AppLogic.runOnBackGround(mSearchTimeOut, option.getTimeout());
		isCitySearch=true;
		return search(option,listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_GAODE_LOCAL)){
			JNIHelper.logd("POISearchLog:PoiSearToolGaodeLocal is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		NavThirdApp localNavImpl = NavManager.getInstance().getLocalNavImpl();
		if(localNavImpl instanceof NavAmapAutoNavImpl){
			if(((NavAmapAutoNavImpl)localNavImpl).getMapCode()<210 ){
				listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				return new SearchReq() {
					@Override
					public void cancel() {
					
					}
				};				
			}
		}
		AppLogic.runOnBackGround(mSearchTimeOut, option.getTimeout());
		isCitySearch=false;
		return search(option,listener);
	}
	
	private SearchReq search(PoiSearchOption searchOption,PoiSearchResultListener listene){
		if(DebugCfg.POISEARCH_ONWAY_TAG){
			TAG="TXZ";
		}
//		NavManager.getInstance().reportNetStatus("gaodeloc", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		mPoiSearchResutlListener=listene;
		mOption=searchOption;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		Option option = new Option();
		option.searchType = ONWAY_SEARCH_TYPE;
		NavThirdApp localNavImpl = NavManager.getInstance().getLocalNavImpl();
		if(localNavImpl instanceof NavAmapAutoNavImpl){
			option.kws=ConfigFileHelper.getInstance(GlobalContext.get()).ConfigValue(
					ConfigFileHelper.ONWAY_SEARCH, ConfigFileHelper.NAV_IMP_GAODE, searchOption.getKeywords(),
					((NavAmapAutoNavImpl)localNavImpl).getMapDetCode(),"");		
		}
		JNIHelper.logd("option.kws = "+option.kws);
		option.mResultListener = mInnerListener;
		mSeqOption = option;
		NavAmapValueService.getInstance().checkAutoIsAlive(ONWAY_SEARCH_TYPE);
		PoiQuery.getInstance().startQuery(option);
		
		return mSearchReq;		
	}
	
	private ArrayList<Poi> getPoiFromJsonString(String jsonResult){
		ArrayList<Poi> poiList = new ArrayList<Poi>();
		try{
			if (!TextUtils.isEmpty(jsonResult)) {
				int count = 0;
				int type = 0;
				
				JSONObject jo = new JSONObject(jsonResult);
				if (jo.has("search_result_size")) {
					count = jo.getInt("search_result_size");
				}
				if (jo.has("poi_info")) {
					
					JSONArray jsonArray = (JSONArray) jo.get("poi_info");
					for (int i = 0; i < count; i++) {
						Poi wayPoi = new Poi();
						JSONObject jObj = (JSONObject) jsonArray.get(i);
						if (jObj != null) {
							if (jObj.has("poi_Longitude")) {
								String lng = jObj.getString("poi_Longitude");
								if (!TextUtils.isEmpty(lng)) {
									wayPoi.setLng(Double.parseDouble(lng));
								} else {
									wayPoi.setLng(jObj.getDouble("poi_Longitude"));
								}
							}
							if (jObj.has("poi_Latitude")) {
								String lat = jObj.getString("poi_Latitude");
								if (!TextUtils.isEmpty(lat)) {
									wayPoi.setLat(Double.parseDouble(lat));
								} else {
									wayPoi.setLat(jObj.getDouble("poi_Latitude"));
								}
							}

							if (jObj.has("poi_addr")) {
								wayPoi.setGeoinfo(jObj.getString("poi_addr"));
							}
							if (jObj.has("poi_name")) {
								wayPoi.setName( TAG+jObj.getString("poi_name"));
							}
							wayPoi.setDistance(BDLocationUtil.calDistance(
									wayPoi.getLat(), wayPoi.getLng()));
							wayPoi.setSourceType(Poi.POI_SOURCE_GAODE_LOCAL);
							int distance=wayPoi.getDistance();
							for(int ii =0;ii<poiList.size();ii++){
								if(distance<poiList.get(ii).getDistance()){
									poiList.add(ii, wayPoi);
									wayPoi=null;
									break;
								}
							}
							if(wayPoi!=null)
								poiList.add(wayPoi);
						}
					}
					return poiList;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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
