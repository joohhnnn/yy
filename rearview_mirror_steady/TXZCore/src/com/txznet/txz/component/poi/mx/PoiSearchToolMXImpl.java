package com.txznet.txz.component.poi.mx;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.equipment.UiEquipment;
import com.txznet.comm.remote.GlobalContext;
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
import com.txznet.txz.component.nav.mx.NavMXImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Observable;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;

public class PoiSearchToolMXImpl implements PoiSearchTool {
	private static final String CMD_TYPE_ENUM = "CMD_TYPE_ENUM";
	private static final String CMD_TYPE_CONTENT = "COMMUNICATE_INFO_CONTENT";
	private static final String RECV_ACTION = "NAVI_TO_VC_COMMUNICATE_MESSAGE";
	PoiSearchOption mOption;
	private int mRetryCount=-1;
	boolean isCitySearch=false;
	public static class PoiResultObervable extends Observable<PoiResultObervable.PoiResultObserver> {

		public static interface PoiResultObserver {
			public void onReceiver(Intent intent);
		}

		public void notifyReceiver(Intent intent) {
			synchronized (mObservers) {
				for (int i = mObservers.size() - 1; i >= 0; i--) {
					mObservers.get(i).onReceiver(intent);
				}
			}
		}
	}

	static PoiResultObervable sObservable = new PoiResultObervable();

	static {
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				sObservable.notifyReceiver(intent);
			}
		}, new IntentFilter(RECV_ACTION));
	}

	// 加油站
	public static final int CMD_SEARCH_AROUND_GAS_STATION = 34;
	// 停车场
	public static final int CMD_SEARCH_AROUND_CAR_PARK = 35;
	// 公园
	public static final int CMD_SEARCH_AROUND_PARK = 36;
	// 公厕
	public static final int CMD_SEARCH_AROUND_PUBLIC_TOILET = 37;
	// 餐饮
	public static final int CMD_SEARCH_AROUND_RECREATION = 38;
	// 酒店
	public static final int CMD_SEARCH_AROUND_HOTEL = 39;
	// 餐厅
	public static final int CMD_SEARCH_AROUND_RESTAURANT = 40;
	// 银行
	public static final int CMD_SEARCH_AROUND_BANK = 41;
	// 超市
	public static final int CMD_SEARCH_AROUND_MARKET = 42;
	// 医院
	public static final int CMD_SEARCH_AROUND_HOSPITAL = 43;
	// 电影院
	public static final int CMD_SEARCH_AROUND_CINEMA = 44;
	// KTV
	public static final int CMD_SEARCH_AROUND_KTV = 45;
	// 学校
	public static final int CMD_SEARCH_AROUND_SCHOOL = 77;

	// 周边检索
	public static final int CMD_SEARCH_AROUND = 68;
	// 关键字检索
	public static final int CMD_SEARCH_POI = 70;
	// 沿途检索
	public static final int CMD_SEARCH_ALONG_ROAD = 73;

	private long mSearchTime=0;
	PoiSearchResultListener mResultListener;

	PoiSearchToolMXImpl.PoiResultObervable.PoiResultObserver poiResultObserver = new PoiSearchToolMXImpl.PoiResultObervable.PoiResultObserver() {

		@Override
		public void onReceiver(Intent intent) {
			if(intent !=null){
				Bundle b = intent.getExtras();
				if (b != null) {
					int type = b.getInt(CMD_TYPE_ENUM);
					if (type == 70) {
						mAppBack.update(intent);
						AppLogic.removeBackGroundCallback(mAppBack);
						AppLogic.runOnBackGround(mAppBack, 0);
					}
				}				
			}	
		}
	};
	
	Runnable1<Intent> mAppBack = new Runnable1<Intent>(null) {
		
		@Override
		public void run() {
			doNavReceiver70(mP1);
		}
	};

	private void doNavReceiver70(Intent intent) {
		try {
//			NavManager.getInstance().reportNetStatus("mx", "end");
			mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
			NavManager.reportBack("mx",mSearchTime);
			String[] arrays = intent.getStringArrayExtra(CMD_TYPE_CONTENT);
			if (arrays != null) {
				String vc = arrays[0];
				String con = arrays[1];
				String type = arrays[2];
				if ("2".equals(type)) { // 检索的回执结果
					List<Poi> pois = new ArrayList<Poi>();
					JSONArray jsonArray = new JSONArray(con);
					for (int i = 0; i < jsonArray.length(); i++) {
						Poi poi = getPoiFromJsonObject((JSONObject) jsonArray.get(i));
						pois.add(poi);
					}
					AppLogic.removeBackGroundCallback(mTimeoutRunnable);
					if (mResultListener != null) {
						MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_MEIXING_OFFLINE);
						JNIHelper.logd("POISearchLog: PoiSearchToolMXImpl return Poi count= "+pois.size());
						NavManager.getInstance().setNomCityList(pois);
						PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout()-mSearchTime,mResultListener,pois);
						checkUnRegisterObserver();
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			if (mResultListener != null) {
				AppLogic.removeBackGroundCallback(mTimeoutRunnable);
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_MEIXING_OFFLINE);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "exception");
				checkUnRegisterObserver();
			}
		}
	}

	private Poi getPoiFromJsonObject(JSONObject jo) {
		Poi poi = new Poi();
		try {
			poi.setSourceType(Poi.POI_SOURCE_MEIXING);
			if (jo.has("Name")) {
				poi.setName(jo.getString("Name"));
			}
			if (jo.has("Address")) {
				poi.setGeoinfo(jo.getString("Address"));
			}
			if (jo.has("Telephone")) {
			}
			if (jo.has("Distance")) {
				poi.setDistance(jo.getInt("Distance"));
			}
			if (jo.has("Mode")) {
				poi.setExtraStr(jo.getString("Mode"));
			}
			return poi;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}

	SearchReq mSearchReq = new SearchReq() {

		@Override
		public void cancel() {
			JNIHelper.logd("cancel search");
			AppLogic.removeBackGroundCallback(mTimeoutRunnable);
			mResultListener = null;
			checkUnRegisterObserver();
		}
	};

	Runnable mTimeoutRunnable = new Runnable() {

		@Override
		public void run() {
//			NavManager.getInstance().reportNetStatus("mx", "timeout");
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
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_MEIXING_OFFLINE);
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
				mResultListener = null;
				checkUnRegisterObserver();
			}
		}
	};

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option, PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_MEIXING)){
			JNIHelper.logd("POISearchLog:PoiSearToolMX is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_MEIXING_OFFLINE);
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=true;
		checkRegisterObserver();
		
		AppLogic.runOnBackGround(mTimeoutRunnable, option.getTimeout());
		String kws = option.getKeywords();
		String region= option.getRegion();
		if(!TextUtils.isEmpty(region)){
			kws=region+" "+kws;
		}
		String city = option.getCity();
		if (TextUtils.isEmpty(city)) {
			city = "";
		}
		sendToNavi(CMD_SEARCH_POI, new JSONBuilder().put("name", kws).put("city", city).put("province", "").toString());
		return mSearchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option, PoiSearchResultListener listener) {
		if(!NavManager.getInstance().isEnablePoiToolSearch(option.getSearchInfo(),UiEquipment.POI_SOURCE_MEIXING)){
			JNIHelper.logd("POISearchLog:PoiSearToolMX is unenable");
			listener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return new SearchReq() {
				@Override
				public void cancel() {
				
				}
			};
		}
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL, MonitorUtil.POISEARCH_ENTER_MEIXING_OFFLINE);
		mResultListener = listener;
		mOption=option;
		if(mOption.getSearchInfo()!=null&&mRetryCount==-1)
			mRetryCount=mOption.getSearchInfo().getPoiRetryCount();

		isCitySearch=false;
		checkRegisterObserver();
		
		AppLogic.runOnBackGround(mTimeoutRunnable, option.getTimeout());
		String region= option.getRegion();
		String keyword=option.getKeywords();
		if(!TextUtils.isEmpty(region)){
			keyword=region+" "+keyword;
		}
		if (isConvertCitySearch(keyword)) {
			sendToNavi(CMD_SEARCH_POI, new JSONBuilder().put("name", keyword)
					.put("city", option.getCity() == null ? "" : option.getCity()).put("province", "").toString());
			return mSearchReq;
		}

		keyword = filterSearchKeywords(keyword);

		sendToNavi(CMD_SEARCH_AROUND, new JSONBuilder().put("name", keyword).toString());
		return mSearchReq;
	}

	private boolean isConvertCitySearch(String kws) {
		if (!TextUtils.isEmpty(kws)) {
			if (kws.contains("美食")) {
				return true;
			}
			if (kws.contains("服装")) {
				return true;
			}
			if (kws.contains("咖啡厅")) {
				return true;
			}
			if (kws.contains("洗车")) {
				return true;
			}
		}
		return false;
	}

	private String filterSearchKeywords(String kws) {
		if (!TextUtils.isEmpty(kws)) {
			if (kws.contains("取款")) {
				return "银行";
			}
		}
		return kws;
	}

	private void sendToNavi(int action, String paramJson) {
		if (!PackageManager.getInstance().checkAppExist(NavMXImpl.MX_PACKAGE_NAME)) {
			AppLogic.removeBackGroundCallback(mTimeoutRunnable);
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_MEIXING_OFFLINE);
			if(mResultListener!=null)
				mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
			checkUnRegisterObserver();
			return;
		}

		String[] params = new String[3];
		params[0] = "1.0";
		params[1] = paramJson;
		params[2] = "1";

		Bundle b = new Bundle();
		b.putInt(CMD_TYPE_ENUM, action);
		if (params != null) {
			b.putStringArray(CMD_TYPE_CONTENT, params);
		}
//		NavManager.getInstance().reportNetStatus("mx", "begin");
		mSearchTime=SystemClock.elapsedRealtime();
		Intent intent = new Intent("VC_TO_NAVI_COMMUNICATE_MESSAGE");
		intent.putExtras(b);
		GlobalContext.get().sendBroadcast(intent);
	}
	
	boolean mHasRegister;
	
	private synchronized void checkRegisterObserver() {
		try {
			if (!mHasRegister) {
				mHasRegister = true;
				sObservable.registerObserver(poiResultObserver);
			}
		} catch (Exception e) {
		}
	}
	
	private synchronized void checkUnRegisterObserver() {
		try {
			if (mHasRegister) {
				mHasRegister = false;
				sObservable.unregisterObserver(poiResultObserver);
			}
		} catch (Exception e) {
		}
	}
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_MEIXING-1)) != 0 && mResultListener != null){
			mResultListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mResultListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_MEIXING;
	}
}
