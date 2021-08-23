package com.txznet.txz.component.poi.txz;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.CenterSearchParam;
import com.txz.ui.equipment.UiEquipment.GpsInfo;
import com.txz.ui.equipment.UiEquipment.NearSearchParam;
import com.txz.ui.equipment.UiEquipment.Req_TXZPoiSearch;
import com.txz.ui.equipment.UiEquipment.Resp_TXZPoiSearch;
import com.txz.ui.equipment.UiEquipment.TXZPoiDetailInfo;
import com.txz.ui.equipment.UiEquipment.TXZPoiInfo;
import com.txz.ui.equipment.UiEquipment.TXZPoiSearchParam;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.TxzPoi;
import com.txznet.sdk.bean.TxzPoi.GeoDetail;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.NavManager.IRespTxzPoiSearch;
import com.txznet.txz.ui.win.nav.BDLocationUtil;

public class PoiSearchToolTxzPoiImpl implements
		TXZPoiSearchManager.PoiSearchTool, IRespTxzPoiSearch {

	// 搜索类型
	public static final int POI_SEARCH_TYPE_CENTER = 1; // 中心搜索
	public static final int POI_SEARCH_TYPE_NEAR = 2; // 附近搜索
	public static final int POI_SEARCH_TYPE_RANGE = 3; // 区域搜索，封闭区域，和行政区不一样
	public static final int POI_SEARCH_TYPE_DST = 4; // 目的地搜索，目标一定会带上gps信息
	public static final int POI_SEARCH_TYPE_CITY = 5; // 行政区搜索
	// POI 搜索类型
	public static final int POI_SORT_TYPE_DISTANCE = 1; // 距离排序
	public static final int POI_SORT_TYPE_PRICE = 2; // 价格排序
	public static final int POI_SORT_TYPE_SCORE = 3; // 评分排序

	/** 后台POI数据请求id，防止搜索结果 */
	private static volatile int mRequstId = 0;
	private int mRetryCount = -1;
	/** POI搜索超时 */
	private int mTimeOut = TXZPoiSearchManager.DEFAULT_SEARCH_TIMEOUT;
	private Req_TXZPoiSearch mPoiSearchReq = null;
	private int mType = -1;
	private PoiSearchResultListener mListener = null;
	private byte[] mCenterKeyWord;
	private PoiSearchOption mOption = null;
	private long mSearchTime = 0;
	private boolean mIsBussiness = false;
	private boolean mNeedShowEVCard = false;

	public PoiSearchToolTxzPoiImpl(){
		this(false);
	}

	public PoiSearchToolTxzPoiImpl(boolean isBusiness) {
		mIsBussiness = isBusiness;
	}

	SearchReq mSearchReq = new SearchReq() {

		@Override
		public void cancel() {
			AppLogic.removeBackGroundCallback(mTimeOutRunnable);
			NavManager.getInstance().unRegisterTxzPoiSeachCallback();
			if (mOption != null) {
				mOption.getSearchInfo().setTxzPoiToolComplete(true);
			}
			mListener = null;
		}
	};
	SearchReq mSearchReqNull = new SearchReq() {

		@Override
		public void cancel() {

		}
	};

	Runnable mTimeOutRunnable = new Runnable() {

		@Override
		public void run() {
			if (mListener != null) {
				JNIHelper.logd("POISearchLog:" + getClass().toString()
						+ " mRetryCount=" + mRetryCount);
				if (mRetryCount > 0) {
					mRetryCount--;
					JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
							UiEquipment.SUBEVENT_REQ_TXZ_POI_SEARCH,
							mPoiSearchReq);
					AppLogic.runOnBackGround(mTimeOutRunnable, mTimeOut);
					return;
				}
				JNIHelper.logd("TXZPoiSearchTool timeOut");
				NavManager.getInstance().unRegisterTxzPoiSeachCallback();
				mOption.getSearchInfo().setTxzPoiToolComplete(true);
				mListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT,
						"");
				mListener = null;
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_TXZPOI);
			}
		}
	};

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		if (mType != POI_SEARCH_TYPE_CENTER) {
			mType = POI_SEARCH_TYPE_CITY;
		}
		return search(option, listener);
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		if (mType != POI_SEARCH_TYPE_CENTER) {
			mType = POI_SEARCH_TYPE_NEAR;
		}

		return search(option, listener);
	}

	public void setCenterSearch(String keyword) {
		if (!TextUtils.isEmpty(keyword)) {
			mType = POI_SEARCH_TYPE_CENTER;
			mCenterKeyWord = keyword.getBytes();
		}
	}

	private SearchReq search(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		JNIHelper.logd("TXZPoiSearchTool start");
		mOption = option;
		mListener = listener;
		mTimeOut = option.getTimeout();
		mRetryCount = option.getSearchInfo().getPoiRetryCount();

		mPoiSearchReq = new Req_TXZPoiSearch();
		TXZPoiSearchParam param = new TXZPoiSearchParam();
		mPoiSearchReq.txzPoiSearchParam = param;
		LocationInfo lastLocation = LocationManager.getInstance()
				.getLastLocation();
		if (lastLocation != null && lastLocation.msgGpsInfo != null
				&& lastLocation.msgGpsInfo.dblLat != null
				&& lastLocation.msgGpsInfo.dblLng != null) {
			mPoiSearchReq.gpsInfo = new GpsInfo();
			mPoiSearchReq.gpsInfo.dblLat = lastLocation.msgGpsInfo.dblLat;
			JNIHelper.logd("TXZPoiSearchTool mPoiSearchReq.gpsInfo.dblLat="+mPoiSearchReq.gpsInfo.dblLat);			
			mPoiSearchReq.gpsInfo.dblLng = lastLocation.msgGpsInfo.dblLng;
			JNIHelper.logd("TXZPoiSearchTool mPoiSearchReq.gpsInfo.dblLng="+mPoiSearchReq.gpsInfo.dblLng);
		} else {
			mListener.onError(TXZPoiSearchManager.ERROR_CODE_UNKNOW, "定位失败");
			mListener = null;
			return mSearchReqNull;
		}
		if (lastLocation != null && lastLocation.msgGeoInfo != null
				&& !TextUtils.isEmpty(lastLocation.msgGeoInfo.strCity)) {
			mPoiSearchReq.strCity = lastLocation.msgGeoInfo.strCity.getBytes();
			JNIHelper.logd("TXZPoiSearchTool mPoiSearchReq.strCity="+lastLocation.msgGeoInfo.strCity);
		}

		mRequstId++;
		mPoiSearchReq.uint32SessionId = mRequstId;
		JNIHelper.logd("TXZPoiSearchTool mPoiSearchReq.uint32SessionId="+mPoiSearchReq.uint32SessionId);
		param.rptKeywords = new byte[][] { option.getKeywords().getBytes() };
		JNIHelper.logd("TXZPoiSearchTool rptKeywords="+ option.getKeywords());
		JNIHelper.logd("TXZPoiSearchTool strTargetCity="+ option.getCity());
		if(!TextUtils.isEmpty(option.getCity())){
			param.strTargetCity = option.getCity().getBytes();
		}		
		param.uint32ReqCount = option.getNum();
		JNIHelper.logd("TXZPoiSearchTool uint32ReqCount="+param.uint32ReqCount);
		param.txzPoiSearchType = POI_SEARCH_TYPE_NEAR;
		JNIHelper.logd("TXZPoiSearchTool txzPoiSearchType="+param.txzPoiSearchType);
		mNeedShowEVCard = true;
		if (mType == POI_SEARCH_TYPE_CENTER) {
			param.certerSearchParam = new CenterSearchParam();
			param.certerSearchParam.rptCenterKeywords = mCenterKeyWord;
			JNIHelper.logd("TXZPoiSearchTool rptCenterKeywords="+param.certerSearchParam.rptCenterKeywords);
		} else if (mType == POI_SEARCH_TYPE_NEAR) {
			if (option instanceof NearbyPoiSearchOption) {
				NearbyPoiSearchOption nearOption = (NearbyPoiSearchOption) option;
				if(!TextUtils.equals("网点",option.getKeywords())){
					mNeedShowEVCard = false;
				}
				// 不用管原始设计方案，后台没有支持，客户端也没有支持这些
				mPoiSearchReq.gpsInfo.dblLat = nearOption.getCenterLat();
				mPoiSearchReq.gpsInfo.dblLng = nearOption.getCenterLng();
				param.nearSearchParam = new NearSearchParam();
				param.nearSearchParam.gpsInfo = new GpsInfo();
				param.nearSearchParam.gpsInfo.dblLat = mPoiSearchReq.gpsInfo.dblLat;
				param.nearSearchParam.gpsInfo.dblLng = mPoiSearchReq.gpsInfo.dblLng;
				JNIHelper.logd("TXZPoiSearchTool param.nearSearchParam.gpsInfo.dblLat="+param.nearSearchParam.gpsInfo.dblLat);
				JNIHelper.logd("TXZPoiSearchTool param.nearSearchParam.gpsInfo.dblLng="+param.nearSearchParam.gpsInfo.dblLng);
				param.nearSearchParam.uint32Radius = nearOption.getRadius();
			}
		}
		NavManager.getInstance().registerTxzPoiSeachCallback(this);
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ALL,MonitorUtil.POISEARCH_ENTER_TXZPOI);
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_TXZ_POI_SEARCH, mPoiSearchReq);
		JNIHelper.logd("TXZPoiSearchTool send event");
		mSearchTime = SystemClock.elapsedRealtime();
		AppLogic.runOnBackGround(mTimeOutRunnable, mTimeOut);
		return mSearchReq;
	}

	@Override
	public void onResult(Resp_TXZPoiSearch poi) {
		JNIHelper.logd("TXZPoiSearchTool onResult");
		if (mListener == null || mOption == null) {
			JNIHelper.logw("TXZPoiSearchTool no listener");
			return;
		}
		if (poi == null) {
			JNIHelper.logd("TXZPoiSearchTool onResult == null");
			AppLogic.removeBackGroundCallback(mTimeOutRunnable);
			mOption.getSearchInfo().setTxzPoiToolComplete(true);
			mListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mListener = null;
			return;
		}
		JNIHelper.logd("TXZPoiSearchTool resp_SessionId = " + poi.uint32SessionId + " req_SessionId = " + mPoiSearchReq.uint32SessionId);
		if (poi.uint32SessionId != mPoiSearchReq.uint32SessionId) {
			return;
		}
		JNIHelper.logd("TXZPoiSearchTool uint32DisabledEngine = "+poi.uint32DisabledEngine);
		if(poi.uint32DisabledEngine != null){
			mOption.getSearchInfo().setDisShowEngine(poi.uint32DisabledEngine);
		}
		if (poi.rptPoiInfo == null || poi.rptPoiInfo.length == 0) {
			JNIHelper.logd("TXZPoiSearchTool onResult is empty");
			AppLogic.removeBackGroundCallback(mTimeOutRunnable);
			mOption.getSearchInfo().setTxzPoiToolComplete(true);
			mListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mListener = null;
			return;
		}
		
		ArrayList<Poi> resultList = new ArrayList<Poi>(poi.rptPoiInfo.length);
		JNIHelper.logd("TXZPoiSearchTool rptPoiInfo length = "+poi.rptPoiInfo.length);
		for(int i = 0;i < poi.rptPoiInfo.length ; i++){
			try {
				addPoi(resultList, poi.rptPoiInfo[i]);
			} catch (Exception e) {
				e.printStackTrace();
				JNIHelper.logd("TXZPoiSearchTool poiInfo error " + e.toString());
			}
		}
		mSearchTime = SystemClock.elapsedRealtime()-mSearchTime;
		mOption.getSearchInfo().setTxzPoiToolComplete(true);
		if(resultList.size()>0){
			JNIHelper.logd("TXZPoiSearchTool resultList size=" + resultList.size());
			NavManager.getInstance().setNomCityList(resultList);
			PoiSearchToolGaodeWebImpl.getPoisCity(mOption.getTimeout()-mSearchTime,mListener,resultList);
//						mListener.onResult(resultList);
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_TXZPOI);
		}else{
			JNIHelper.logd("TXZPoiSearchTool onResust isEvoemt");
			mListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_TXZPOI);
		}
		NavManager.reportBack("txzpoi", mSearchTime);
		AppLogic.removeBackGroundCallback(mTimeOutRunnable);
		mListener = null;
	}

	private void addPoi(ArrayList<Poi> resultList, TXZPoiInfo info) {
		if (info == null) {
			return;
		}
		Poi poi = null;
		if(info.poiType != null) {
			switch (info.poiType) {
			case UiEquipment.TXZ_POI_TYPE_BUSINESS: // 商圈类型
				try {
					JSONObject jsonAddtion = new JSONObject(new String(info.strJsonAddtion));
					// 存在附加poi数据类型
					poi = getBusinessPoiFromResult(jsonAddtion);
					if (poi == null || !setPoiParams(poi, info)) {
						return;
					}
					JNIHelper.logd("TXZPoiSearchTool business poi = " + poi.toString() );
					resultList.add(poi);
				} catch (Exception e) {
					e.printStackTrace();
					JNIHelper.loge("TXZPoiSearchTool " + e.toString());
				}
				return;
				// break;
			default:
				break;
			}
		}
		TxzPoi poiTmp= new TxzPoi();
		if (!setPoiParams(poiTmp, info)) {
			return;
		}
		boolean isNeedSort = false;
		if(info.strJsonAddtion != null&& info.strJsonAddtion.length >0){
			poiTmp.setExtraStr(new String(info.strJsonAddtion));
			try {
				JSONObject jsonAddtion = new JSONObject(new String(info.strJsonAddtion));
				if(jsonAddtion.has("b_need_client_sort")){
					isNeedSort = jsonAddtion.getBoolean("b_need_client_sort");
				}
				LogUtil.logd("TXZPoiSearchTool Addtion = " + jsonAddtion.toString());
			} catch (Exception e) {
			}
		}
		poiTmp.isTop = (info.bTop == null ? false : info.bTop);
		if(info.rptKeywords!=null&&info.rptKeywords.length>0){
			String[] key = new String[info.rptKeywords.length];
			for(int ii = 0; ii<info.rptKeywords.length; ii++){
				key[ii] =new String( info.rptKeywords[ii]);
			}
			poiTmp.setKeyWords(key);
		}
		if( info.bTop != null){
			poiTmp.isTop = info.bTop;
		}
		if(info.poiDetailInfo!=null){
			try{
				TXZPoiDetailInfo detai =  info.poiDetailInfo;
				GeoDetail geoDetail = new TxzPoi.GeoDetail();
				poiTmp.setGeoDetail(geoDetail);
				geoDetail.country = new String(detai.strContry);
				geoDetail.province =new String( detai.strProvince);
				geoDetail.town = new String(detai.strTown);
				geoDetail.area =new String( detai.strArea);
				geoDetail.street = new String(detai.strStreet);
				geoDetail.building = new String(detai.strBinding);
				geoDetail.number = new String(detai.strNumber);
				geoDetail.room =new String( detai.strRoot);					
			}catch(Exception e ){			
			}
		}
		if(info.poiType != null){
			poiTmp.setPoiShowType(info.poiType);
		}
		
		JNIHelper.logd("TXZPoiSearchTool toString poi = "+poiTmp.toString() );
		JNIHelper.logd("TXZPoiSearchTool isNeedSort= "+isNeedSort );
		if((!mNeedShowEVCard || mIsBussiness) && isEvCard(info)){
			// 在商圈搜索时移除Evcard搜索结果
			LogUtil.logd("TXZPoiSearchTool business no evcard remove " + poiTmp.getName());
			return;
		}
		if(isNeedSort){
			int index = 0;
			for (;index < resultList.size(); index++) {
				TxzPoi poi2 = (TxzPoi) resultList.get(index);
				JNIHelper.logd("TXZPoiSearchTool poi2 name = "+poi2.getName()+" isTop= "+poi2.isTop+" distance = "+poi2.getDistance() );
				JNIHelper.logd("TXZPoiSearchTool poiTmp name = "+poiTmp.getName()+" isTop= "+poiTmp.isTop+" distance = "+poiTmp.getDistance() );
				if (poiTmp.isTop && !poi2.isTop) {
					break;
				}else{
					if (!poiTmp.isTop && poi2.isTop){
						continue;
					}
				}
				if(poiTmp.getDistance()  > poi2.getDistance()){
					continue;
				}else{
					break;
				}
			}
			JNIHelper.logd("TXZPoiSearchTool index= "+index );
			resultList.add(index, poiTmp);
		}else{
			resultList.add(poiTmp);
		}
	}
	
	/**
	 * 设置基本poi信息
	 * @param poi
	 * @param info
	 */
	private boolean setPoiParams(Poi poi, TXZPoiInfo info) {
		if (info.gpsInfo == null || info.gpsInfo.dblLat == null || info.gpsInfo.dblLng == null) {
			JNIHelper.logw("TXZPoiSearchTool no GPS info");
			return false;
		}
		poi.setLat(info.gpsInfo.dblLat);
		poi.setLng(info.gpsInfo.dblLng);
		poi.setDistance(BDLocationUtil.calDistance(poi.getLat(), poi.getLng()));
		poi.setSourceType(info.sourceType == null ? Poi.POI_SOURCE_TXZ_POI : info.sourceType);
		
		if(info.strName != null&&info.strName.length>0){
			poi.setName(new String(info.strName));
		}
		if(info.strGeoAddr != null&&info.strGeoAddr.length>0){
			poi.setGeoinfo(new String(info.strGeoAddr));
		}
		if(info.rptAlias!=null&&info.rptAlias.length>0){
			String[] key = new String[info.rptAlias.length];
			for(int ii = 0; ii<info.rptAlias.length; ii++){
				key[ii] = new String(info.rptAlias[ii]);
			}
			poi.setAlias(key);
		}
		return true;
	}

	/**
	 * 判断是否是EVCard网点的poi
	 * @return
	 * @param info
	 */
	private boolean isEvCard(TXZPoiInfo info) {
		if(info.strJsonAddtion == null){
			return false;
		}
		try {
			JSONObject json = new JSONObject(new String(info.strJsonAddtion));
			if(json.has("shop_seq")){
				return true;
			}
		} catch (JSONException e) {
		}
		return false;
	}

	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if((disShowPoiType &  1<<(Poi.POI_SOURCE_TXZ_POI-1)) != 0 && mListener != null){
			mListener.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			mListener = null;
		}
	}
	@Override
	public int getPoiSearchType() {
		return Poi.POI_SOURCE_TXZ_POI;
	}
	
	private BusinessPoiDetail getBusinessPoiFromResult(JSONObject json) {
		JNIHelper.logd("TXZPoiSearchTool business json:" + json.toString());
		JSONBuilder data = new JSONBuilder(json);
		BusinessPoiDetail poi = new BusinessPoiDetail();
		poi.setName(data.getVal("name", String.class)); // POI名称
		Double lat = data.getVal("lat", Double.class);
		if (lat != null)
			poi.setLat(lat); // 纬度，gcj02坐标系
		Double lng = data.getVal("lng", Double.class);
		if (lng != null)
			poi.setLng(lng); // 经度，gcj02坐标系
		poi.setDistance(BDLocationUtil.calDistance(lat, lng)); // 距离，单位米
		poi.setCity(data.getVal("city", String.class)); // 城市
		poi.setProvince(data.getVal("province", String.class)); // 所属省
		poi.setBranchName(data.getVal("branchName", String.class)); // 分店信息
		poi.setAvgPrice(data.getVal("avgPrice", Integer.class, 0)); // 人均价格 ，单位元
		poi.setDealCount(data.getVal("dealCount", Integer.class, 0)); // 团购数量
		poi.setHasDeal(data.getVal("hasDeal", Boolean.class, false)); // 是否有团购
		poi.setGeoinfo(data.getVal("geo", String.class)); // 位置信息
		poi.setHasCoupon(data.getVal("hasCoupon", Boolean.class, false)); // 是否有优惠券
		poi.setHasPark(data.getVal("hasPark", Boolean.class, false)); // 是否有停车场
		poi.setHasWifi(data.getVal("hasWifi", Boolean.class, false)); // 是否有wifi
		poi.setPhotoUrl(data.getVal("photoUrl", String.class)); // 照片地址
		poi.setReviewCount(data.getVal("review_count", Integer.class, 0)); // 点评次数
		poi.setScore(data.getVal("score", Float.class, 0F)); // 星级评分 ，10分制
		poi.setScoreDecoration(data.getVal("scoreDecoration", Float.class, 0F)); // 环境评分 ，10分制
		poi.setScoreService(data.getVal("scoreServer", Float.class, 0F)); // 服务评分 ，10分制
		poi.setScoreProduct(data.getVal("scoreProduct", Float.class, 0F)); // 产品评分 ，10分制
		poi.setTelephone(data.getVal("telephone", String.class)); // 电话号码
		poi.setWebsite(data.getVal("business_url", String.class)); // 网址
		poi.setRegions(data.getVal("regions", String[].class)); // 区域信息
		poi.setAlias(data.getVal("alias", String[].class)); // 别名
		poi.setCategories(data.getVal("categories", String[].class)); // 分类信息
			// postcode 邮政编码 只有baidu使用
		return poi;
	}
}
