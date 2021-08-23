package com.txznet.txz.module.location;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.os.SystemClock;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult.ERRORNO;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.google.protobuf.nano.MessageNano;
import com.qihu.mobile.lbs.geocoder.Geocoder.GeocoderResult;
import com.qihu.mobile.lbs.geocoder.Geocoder.QHAddress;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy;
import com.qihu.mobile.lbs.geocoder.GeocoderAsy.GeocoderListener;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GeoInfo;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.DebugCfg;
import com.txznet.txz.component.nav.qihoo.NavQihooImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DistanceUtil;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class LocationManager extends IModule {
	private final int LAST_LOCATION_LIST_COUNT= 5;
	static LocationManager sModuleInstance = new LocationManager();
	ILocationClient mLocationClient = null;
	SeqGeoSearchListener mSearchListener;
	public static boolean sUseAndroidSysGps = true;
	public static int DEFAULT_CHECK_DELAY = 60 * 1000;
	private static int locationFlag = UiEquipment.LOCATION_FLAG_DEFAULT;
	/** 定位时间间隔 */
	private int mTimeInterval = ILocationClient.LOCATION_DEFAULT_TIME_INTERVAL;
	
	/** 通知位置更新时间间隔 **/
	private int mNotifyInterval = 60 * 1000;

	
	static int sSpeechId;
	private LocationClientOfExternal mClientOfOutter;

	private LocationManager() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.DATE_CHANGED");
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				LogUtil.loge("SDKInitializer recv DATE_CHANGED");
				checkNet2InitBaiduSDK();
			}
		}, filter);
	}


	public static LocationManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		// 注册需要处理的事件
		mTimeInterval = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_GPS_INTERVAL_SPEED,3);
		if(mTimeInterval < 3){
			mTimeInterval = 3;
		}else if(mTimeInterval >= 10*60){
			mTimeInterval = 10 * 60;
		}
		
		mNotifyInterval = TXZFileConfigUtil.getIntSingleConfig(TXZFileConfigUtil.KEY_GPS_NOTIFY_INTERVAL,60 * 1000);
		return super.initialize_BeforeStartJni();
	}

	private int initCount;
	private static final int maxInitCount = 5;
	private boolean hasRegisterBRecv;
	private boolean isBaiduSdkInitSuccess;

	private BroadcastReceiver mRecv = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			int errorCode = intent.getIntExtra("error_code", -1);
			LogUtil.loge("SDKInitializer recv action:" + action + ",errorCode:" + errorCode);
			if (!SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK.equals(action) &&
					initCount < maxInitCount) {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						initCount++;
						checkNet2InitBaiduSDK();
					}
				}, 3 * 1000);
			}

			if (SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK.equals(action)) {
				isBaiduSdkInitSuccess = true;
			}
		}
	};

	
	private void initializeSDK() {
		if (isBaiduSdkInitSuccess) {
			LogUtil.logd("SDKInitializer had initializeSDK!");
			return;
		}
		if (System.currentTimeMillis() < 1512116110491L) {
			// 百度SDK鉴权会判断当前时间是否在鉴权范围内
			LogUtil.logd("SDKInitializer timeNoReady! " + System.currentTimeMillis());
			return;
		}

		try {
			if (!hasRegisterBRecv) {
				IntentFilter intentFilter = new IntentFilter();
				intentFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
				intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
				intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
				intentFilter.addAction(SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE);
				GlobalContext.get().registerReceiver(mRecv, intentFilter);
				hasRegisterBRecv = true;
			}
			init_SDK();
		} catch (Exception e) {
			LogUtil.loge("SDKInitializer initialize Exception:" + e.getMessage());
		}
	}

	private void init_SDK() {
		SDKInitializer.initialize(GlobalContext.get());
		LogUtil.logd("SDKInitializer initSDK");
	}

	/**
	 * 百度SDK是否已经鉴权成功
	 *
	 * @return
	 */
	public boolean isBaiduSDKReady() {
		return isBaiduSdkInitSuccess;
	}

	@Override
	public int initialize_AfterInitSuccess() {
		mClientOfOutter = new LocationClientOfExternal(new LocationClientOfExternal.LocOutterInitCallback() {

			@Override
			public void onInit(boolean bLink) {
				checkLocClient();
				quickLocation(true);
			}
		});
		checkLocClient();
		mLocationClient.setTimeInterval(mTimeInterval);
		JNIHelper.logd("mLocationClient:"+mLocationClient.getClass().getName());
		
		quickLocation(true);

		// 初始化时还没有定位到则获取最后的位置
		LocationInfo pbLocationInfo = getLastLocation();
		if (pbLocationInfo != null) {
			if (pbLocationInfo.msgGpsInfo != null) {
				JNIHelper.logw("get last location from client: lat="+pbLocationInfo.msgGpsInfo.dblLat+", lng="+pbLocationInfo.msgGpsInfo.dblLng);
			}
		}
		else {
			pbLocationInfo = PreferenceUtil.getInstance().getLocationInfo();
			synchronized (LocationManager.class) {
				if (null != pbLocationInfo) {
					if (pbLocationInfo.msgGpsInfo != null) {
						JNIHelper.logw("get last location from preference: lat="+pbLocationInfo.msgGpsInfo.dblLat+", lng="+pbLocationInfo.msgGpsInfo.dblLng);
					}
					mLocationClient.setLastLocation(pbLocationInfo);
				} else {
					pbLocationInfo = NativeData.getLocationInfo();
					if (null != pbLocationInfo) {
						if (pbLocationInfo.msgGpsInfo != null) {
							JNIHelper.logw("get last location from native: lat="+pbLocationInfo.msgGpsInfo.dblLat+", lng="+pbLocationInfo.msgGpsInfo.dblLng);
						}
						mLocationClient.setLastLocation(pbLocationInfo);
						PreferenceUtil.getInstance().setLocationInfo(pbLocationInfo);
					}
				}
			}
		}
		regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_LOCATION);
//		if(!DebugCfg.ROADTRAFFIC_ENABLE_DEBUG){
//			regEvent(UiEvent.EVENT_VOICE, VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC);
//		}
		synchronized (LocationManager.class){
			mLastLocationList=new ArrayList<UiMap.GpsInfo>();
			GpsInfo gpsInit= new GpsInfo();
			for(int  i= 0;i<LAST_LOCATION_LIST_COUNT;i++){
				if(pbLocationInfo!=null){
					gpsInit.dblLat= pbLocationInfo.msgGpsInfo.dblLat;
					gpsInit.dblLng=pbLocationInfo.msgGpsInfo.dblLng;
				}
				mLastLocationList.add(gpsInit);
			}
		}

		checkNet2InitBaiduSDK();
		return super.initialize_AfterInitSuccess();
	}

	public void checkNet2InitBaiduSDK() {
//		NetworkManager.getInstance().checkNetConnect(3 * 1000, new Runnable() {
//			@Override
//			public void run() {
				initializeSDK();
//			}
//		}, new Runnable() {
//			@Override
//			public void run() {
//
//			}
//		});
	}
	
	private void checkLocClient() {
		if (mLocationClient != null) {
			mLocationClient.release();
			mLocationClient = null;
		}
		
		if (mClientOfOutter.isAvailable()) {
			mLocationClient = mClientOfOutter;
			LogUtil.logd("locationClient:" + mLocationClient.getClass().getName());
			return;
		}
		
		if (PackageManager.getInstance().checkAppExist(NavQihooImpl.PACKAGE_NAME)) {
			mLocationClient = new LocationClientOfQihoo();
		} else if (locationFlag == UiEquipment.LOCATION_FLAG_TENCENT) {
			mLocationClient = new LocationClientOfTencent();
		} else if (locationFlag == UiEquipment.LOCATION_FLAG_AMAP) {
			mLocationClient = new LocationClientOfAMap();
		} else if (locationFlag == UiEquipment.LOCATION_FLAG_QIHOO) {
			mLocationClient = new LocationClientOfQihoo();
		} else if (locationFlag == UiEquipment.LOCATION_FLAG_BAIDU) {
			mLocationClient = new LocationClientOfBaidu();
		} else if (PackageManager.getInstance().manifestHasMeta(GlobalContext.get().getPackageName(),
				"txz.core.hasamap")) {
			mLocationClient = new LocationClientOfAMap();
		} else {
			mLocationClient = new LocationClientOfBaidu();
		}
		
		LogUtil.logd("locationClient:" + mLocationClient.getClass().getName());
	}

	@Override
	public int initialize_addPluginCommandProcessor() {
		this.addPluginCommandProcessor();
		return super.initialize_addPluginCommandProcessor();
	}
	
	
	private void addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.location.", new CommandProcessor() {
			@Override
			public Object invoke(String arg0, Object[] arg1) {
				if ("getLocation".equals(arg0)) {
					return getLastLocation();
				}
				return null;
			}
		});
	}
	
	
	GeoCoder mGeoCoder = null;

	public void cancelReverseGeo() {
		if (mGeoCoder != null) {
			mGeoCoder.destroy();
			mGeoCoder = null;
		}
	}

	public void cancelQueryTraffic() {
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {
		// 处理事件
		switch (eventId) {
		case UiEvent.EVENT_VOICE:
			switch (subEventId) {
			case VoiceData.SUBEVENT_VOICE_SHOW_ROAD_TRAFFIC: {
				if(NetworkManager.getInstance().checkLeastFlow()){
					String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
					TtsManager.getInstance().speakText(resText);
				}
				
				String city = "";
				String addr = "";
				// 情景预处理
				try {
					final VoiceData.RoadTrafficQueryInfo info = VoiceData.RoadTrafficQueryInfo.parseFrom(data);
					city = info.strCity;
					addr = info.strKeywords;
					JSONObject jData = new JSONObject().put("strCity", info.strCity)
							.put("strDirection", info.strDirection).put("strKeywords", info.strKeywords);
					if (SenceManager.getInstance().noneedProcSence("traffic", jData.toString().getBytes())) {
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				if (TextUtils.isEmpty(city) && "附近".equals(addr)) {
					LocationInfo li = getLastLocation();
					if (li != null && li.msgGeoInfo != null) {
						city = li.msgGeoInfo.strCity;
						addr = li.msgGeoInfo.strAddr;
					}
				}

				// TODO 到地图界面展示路况信息
				boolean isShow = NavManager.getInstance().showTraffic(city, addr);
				if (isShow) {
					break;
				}

				String spk = NativeData.getResString("RS_LOCATION_UNSUPPORT");
				RecorderWin.speakTextWithClose(spk, null);
					
				break;
			}
			case VoiceData.SUBEVENT_VOICE_SHOW_LOCATION:
				final LocationInfo location = getLastLocation();
				// if (location != null && location.msgGeoInfo != null
				// && !TextUtils.isEmpty(location.msgGeoInfo.strAddr)) {
				// RecorderWin.speakTextWithClose(
				// "您当前的位置是："
				// + location.msgGeoInfo.strAddr
				// // + "，东经"
				// // + String.format("%.6f",
				// // location.msgGpsInfo.dblLng)
				// // + "，北纬"
				// // + String.format("%.6f",
				// // location.msgGpsInfo.dblLat)
				// , null);
				// break;
				// }
				if (location != null && location.msgGpsInfo != null && location.msgGpsInfo.dblLat != null
						&& location.msgGpsInfo.dblLng != null) {
					JNIHelper.logd("POI: location lat:" + location.msgGpsInfo.dblLat + " lng:" + location.msgGpsInfo.dblLng);
					String spk = NativeData.getResString("RS_LOCATION_QUERYING");
					String hint = NativeData.getResString("RS_LOCATION_HINT_QUERYING");
//					RecorderWin.speakTextNotEqualsDisplay(spk, hint);
					RecorderWin.addSystemMsg(hint);
					sSpeechId = TtsManager.getInstance().speakText(spk, new TtsUtil.ITtsCallback() {
						@Override
						public void onCancel() {
							super.onCancel();
							mLastSearchSeq = 0;
							AppLogic.removeBackGroundCallback(searchTimerOutTask);
							TtsManager.getInstance().cancelSpeak(mTtsTaskId);
						}
					});
					if (mLocationClient instanceof LocationClientOfQihoo) {
						reverseQihooGeoCode(location);
					} else {
						reverseAmapGeoCode(location);
					}
					break;
				}
				String spk = NativeData.getResString("RS_LOCATION_SITE_ERROR");
				RecorderWin.speakTextWithClose(spk, null);
				break;
			}
			break;
		}
		return super.onEvent(eventId, subEventId, data);
	}
	
	/**
	 * 如果定位操作DEFAULT_CHECK_DELAY后不更新重启定位工具
	 */
	public void reinitLocationClientDelay() {
		AppLogic.removeBackGroundCallback(mReinitRunnable);
		AppLogic.runOnBackGround(mReinitRunnable, LocationManager.DEFAULT_CHECK_DELAY);
	}

	/**
	 * 
	 */
	public void removeReinitDelayRunnable() {
		AppLogic.removeBackGroundCallback(mReinitRunnable);
	}

	Runnable mReinitRunnable = new Runnable() {

		@Override
		public void run() {
			JNIHelper.logd("reinit locationClient:" + mLocationClient);
			release_DelayRelease();
			reinit();
		}
	};

	public void reverseBaiduGeoCode(final LocationInfo location) {
		if (mGeoCoder == null) {
			mGeoCoder = GeoCoder.newInstance();
		}
		mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				if (mGeoCoder == null) {
					RecorderWin.close();
					return;
				}
				mGeoCoder.destroy();
				mGeoCoder = null;

				if (result.error == ERRORNO.NO_ERROR){
					String spk = NativeData.getResPlaceholderString(
							"RS_LOCATION_IS", "%LOCATION%", 
							result.getAddress());
					RecorderWin.speakTextWithClose(spk, null);
				} else{
					String spk = NativeData
							.getResString("RS_LOCATION_NOW_UNKNOWN")
							.replace(
									"%LNG%",
									String.format("%.6f",
											location.msgGpsInfo.dblLng))
							.replace(
									"%LAT%",
									String.format("%.6f",
											location.msgGpsInfo.dblLat));
					RecorderWin.speakTextWithClose(spk, null);
				}
			}

			@Override
			public void onGetGeoCodeResult(GeoCodeResult result) {
			}
		});
		mGeoCoder.reverseGeoCode(new ReverseGeoCodeOption()
				.location(new LatLng(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng)));
	}
	
	static class SeqGeoSearchListener implements OnGeocodeSearchListener {
		int seq = 0;

		public SeqGeoSearchListener(int seq) {
			this.seq = seq;
		}

		public void cancelRequest() {
			seq = 0;
		}

		public void onRegeocodeSearched(RegeocodeResult result, int arg1, int seq) {

		}

		@Override
		public void onGeocodeSearched(GeocodeResult arg0, int arg1) {

		}

		@Override
		public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
			if (seq == 0) {
				JNIHelper.logd("onRegeocodeSearched cancel");
				return;
			}

			onRegeocodeSearched(arg0, arg1, seq);
		}
	}
	
	GeocoderAsy mGeocoderAsy;
	public void reverseQihooGeoCode(final LocationInfo location) {
		if (mGeocoderAsy == null) {
			mGeocoderAsy = new GeocoderAsy(GlobalContext.get());
		}
		// 创建逆地理编码和地理编码检索监听者
		GeocoderListener listener = new GeocoderListener() {
			// 地理编码监听函数
			@Override
			public void onGeocodeResult(GeocoderResult result) {
			}

			// 逆地理编码监听函数，输出经纬度点对应的地址信息
			@Override
			public void onRegeoCodeResult(GeocoderResult result, String description) {
				if (result.code != 0) {
					String spk = NativeData
							.getResString("RS_LOCATION_NOW_UNKNOWN")
							.replace(
									"%LNG%",
									String.format("%.6f",
											location.msgGpsInfo.dblLng))
							.replace(
									"%LAT%",
									String.format("%.6f",
											location.msgGpsInfo.dblLat));
					RecorderWin.speakTextWithClose(spk, null);
					return;
				}
				List<QHAddress> list = result.address;
				if (list != null && list.size() > 0) {
					QHAddress address = list.get(0);
					GeoInfo geoInfo = new GeoInfo();
					geoInfo.strAddr = address.getFormatedAddress();
					geoInfo.strCity = address.getCity();
					geoInfo.strDistrict = address.getDistrict();
					geoInfo.strProvice = address.getProvince();
					geoInfo.strStreet = address.getStreet();
					location.msgGeoInfo = geoInfo;
					String spk = NativeData.getResPlaceholderString(
							"RS_LOCATION_IS", "%LOCATION%",
							address.getFormatedAddress());
					RecorderWin.speakTextWithClose(spk, null);
				}
			}
		};
		mGeocoderAsy.regeocode(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng, listener);
	}

	GeocodeSearch mSearch;
	int mSearchSeq = new Random().nextInt();
	int mLastSearchSeq = 0;

	int getNextSearchSeq() {
		++mSearchSeq;
		if (mSearchSeq == 0)
			++mSearchSeq;
		return mSearchSeq;
	}

	public void cancelRequestGeoCode() {
		if (mSearchListener != null) {
			mSearchListener.cancelRequest();
		}
	}
	
	/***直辖市和省辖县需要特殊处理，因为地址转码结果中city为空*****/
	
	//直辖市
	public final static String[] MUNICIPALITIES = {"北京市", "上海市", "重庆市", "天津市", "北京", "上海", "重庆", "天津"};
	
	public boolean isMunicipalities(String strProvince){
			boolean bRet = false;
			for (String s : MUNICIPALITIES){
				if (TextUtils.equals(strProvince, s)){
					bRet = true;
					break;
				}
			}
			return bRet;
	}
	
	//省辖县(或者省辖县级市)
	public final static String[] MUNI_DISTRICT = {"济源市",
		"仙桃市","潜江市","天门市","神农架林区",
		"五指山市","文昌市","琼海市","万宁市","东方市","定安县",
		"屯昌县","澄迈县","临高县","琼中黎族苗族自治县","保亭黎族苗族自治县",
		"白沙黎族自治县","昌江黎族自治县","乐东黎族自治县","陵水黎族自治县",
		"石河子市","阿拉尔市",
		"图木舒克市","五家渠市","北屯市","铁门关市",
		"双河市","可克达拉市","昆玉市"};
	
	public boolean isMuniDistrict(String strDistrict){
			boolean bRet = false;
			for (String s : MUNI_DISTRICT){
				if (TextUtils.equals(strDistrict, s)){
					bRet = true;
					break;
				}
			}
			return bRet;
	}
	
	public final static String[] PROVINCES = {"河北省","山西省","辽宁省","吉林省","黑龙江省","江苏省",
																			"浙江省","安徽省","福建省","江西省","山东省","河南省","湖北省",
																			"湖南省","广东省","海南省","四川省","贵州省","云南省","陕西省",
																			"甘肃省","青海省","台湾省","内蒙古自治区","广西壮族自治区",
																			"西藏自治区","宁夏回族自治区","新疆维吾尔自治区"};
	
	//判断是否是省或者自治区(不包括直辖市和特别行政区)
	public boolean isProvince(String strProvince){
		boolean bRet = false;
		for (String s : PROVINCES){
			if (TextUtils.equals(strProvince, s)){
				bRet = true;
				break;
			}
		}
		return bRet;
	}
	
	/***直辖市和省辖县需要特殊处理，因为地址转码结果中city为空*****/
	
	
	/**
	 * 使用高德逆地理编码
	 * @param lat
	 * @param lng
	 * @param listener
	 */
	public void reverseGeoCode(double lat, double lng, final OnGeocodeSearchListener listener) {
		GeocodeSearch gs = new GeocodeSearch(GlobalContext.get());
		gs.setOnGeocodeSearchListener(new OnGetGeocodeListener(gs) {

			@Override
			public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
				/********直辖市或者省辖县或者省辖县级市/自治区的city字段为空需要特殊处理 AndyZhao 2018/07/31*********/
				do{
					if (arg0 == null) {
						break;
					}
					RegeocodeAddress rAddress = arg0.getRegeocodeAddress();
					if (rAddress == null) {
						break;
					}
					
					String strProvince = rAddress.getProvince();// 省级(省或者直辖市或者自治区)
					String strCity = rAddress.getCity();// 地级市
					String strDistrict = rAddress.getDistrict();// 区、县
					//LogUtil.logd("reverseGeoCode strProvince:" + strProvince + ", strCity:" + strCity + ", strDistrict :" + strDistrict);
					
					if (!TextUtils.isEmpty(strCity)) {
						break;
					}
					
					//直辖市
					if (isMunicipalities(strProvince)){
						rAddress.setCity(strProvince);
						break;
					}
					//省辖县或者县级市
					if (isMuniDistrict(strDistrict)){
						rAddress.setCity(strDistrict);
						break;
					}
					
				}while(false);
				/********直辖市或者省辖县或者省辖县级市/自治区的city字段为空需要特殊处理*********/
				
				if (listener != null) {
					listener.onRegeocodeSearched(arg0, arg1);
				}
				if (gSearch != null) {
					gSearch = null;
				}
			}

			@Override
			public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
				if (listener != null) {
					listener.onGeocodeSearched(arg0, arg1);
				}
				if (gSearch != null) {
					gSearch = null;
				}
			}
		});
		RegeocodeQuery rq = new RegeocodeQuery(new LatLonPoint(lat, lng), 200, GeocodeSearch.AMAP);
		gs.getFromLocationAsyn(rq);
	}
	
	private abstract class OnGetGeocodeListener implements OnGeocodeSearchListener {
		protected GeocodeSearch gSearch;

		public OnGetGeocodeListener(GeocodeSearch gs) {
			gSearch = gs;
		}
	}

	public void reverseAmapGeoCode(final LocationInfo location) {
		if (mSearch == null) {
			mSearch = new GeocodeSearch(GlobalContext.get());
		}
		int seq = mLastSearchSeq = getNextSearchSeq();
		AppLogic.removeBackGroundCallback(searchTimerOutTask);
		AppLogic.runOnBackGround(searchTimerOutTask, SEARCH_TIME_EXCEED);

		if (mSearchListener != null) {
			mSearchListener.cancelRequest();
		}

		mSearch.setOnGeocodeSearchListener(mSearchListener = new SeqGeoSearchListener(seq) {
			@Override
			public void onRegeocodeSearched(RegeocodeResult result, int argq, int seq) {
				if (seq != mLastSearchSeq) {
					return;
				}
				if (mSearch == null) {
					RecorderWin.close();
					return;
				}
				mSearch = null;
				RegeocodeAddress ra = result.getRegeocodeAddress();
				AppLogic.removeBackGroundCallback(searchTimerOutTask);
				if (ra != null) {
					String spk = NativeData.getResPlaceholderString(
							"RS_LOCATION_IS", "%LOCATION%", ra.getFormatAddress());
					mTtsTaskId = RecorderWin.speakTextWithClose(spk, null);
				} else {
					String spk = NativeData
							.getResString("RS_LOCATION_NOW_UNKNOWN")
							.replace(
									"%LNG%",
									String.format("%.6f",
											location.msgGpsInfo.dblLng))
							.replace(
									"%LAT%",
									String.format("%.6f",
											location.msgGpsInfo.dblLat));
					mTtsTaskId = RecorderWin.speakTextWithClose(spk, null);
				}
			}
		});
		LatLonPoint llp = new LatLonPoint(location.msgGpsInfo.dblLat, location.msgGpsInfo.dblLng);
		RegeocodeQuery rq = new RegeocodeQuery(llp, 200, GeocodeSearch.AMAP);
		mSearch.getFromLocationAsyn(rq);
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				mLastSearchSeq = 0;
				TtsManager.getInstance().cancelSpeak(sSpeechId);
				AppLogic.removeBackGroundCallback(searchTimerOutTask);
			}
		});
	}

	private static final int SEARCH_TIME_EXCEED = 4000;
	Runnable searchTimerOutTask = new Runnable() {

		@Override
		public void run() {
			mLastSearchSeq = 0;
			AppLogic.removeBackGroundCallback(searchTimerOutTask);
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String spk = NativeData.getResString("RS_LOCATION_GET_FAIL");
					RecorderWin.speakTextWithClose(spk, null);
				}
			}, 0);
		}
	};
	
	private boolean mReleased = false; 
	private int mTtsTaskId;
	
	@Override
	public void release_DelayRelease() {
		synchronized (LocationManager.class) {
			if (mReleased == false) {
				if (mLocationClient != null) {
					mLocationClient.release();
				}
			}
			mReleased = true;
		}
	}

	@Override
	public void reinit() {
		synchronized (LocationManager.class) {
			if (mReleased == false) {
				return;
			}
			checkLocResApp();
			mReleased = false;
		}
	}
	
	public void setLastLocation(LocationInfo location) {
		synchronized (LocationManager.class) {
			mLocationClient.setLastLocation(location);
		}
		notifyUpdatedLocation();
	}
	
	public LocationInfo getLastLocation() {
		synchronized (LocationManager.class) {
			if(null == mLocationClient){
				return null;
			}
			return mLocationClient.getLastLocation();
		}
	}

	boolean mQuick = false;

	public void quickLocation(boolean bQuick) {
		// 诺威达定位特殊处理
		mQuick = bQuick;
		synchronized (LocationManager.class) {
			if (mReleased == false && null != mLocationClient) {
				mLocationClient.quickLocation(bQuick);
			}
		}
	}

	int mLocSuccessCount = 0;
	
	private List<GpsInfo> mLastLocationList;
	
	public List<GpsInfo> getLastLocationList(){
		synchronized (LocationManager.class) {
			return mLastLocationList;
		}
	
	}
	
	private GpsStatus mGpsStatus;
	
	private Integer mLastSatellitesNum;
		
	public void notifyUpdatedLocation() {
		LocationInfo locationInfo = getLastLocation();
		if (locationInfo != null) {
			try {
				android.location.LocationManager locationManager = (android.location.LocationManager) GlobalContext
						.get().getSystemService(Context.LOCATION_SERVICE);
				GpsStatus status = locationManager.getGpsStatus(mGpsStatus);
				int max = status.getMaxSatellites();
				int count = 0;
				Iterator<GpsSatellite> it = status.getSatellites().iterator();
				while (it.hasNext() && count < max) {
					GpsSatellite satellite = it.next();
					// LogUtil.logd(" getSatellites: usedInFix=" + satellite.usedInFix() + ", getSnr="+satellite.getSnr() + ", hasAlmanac="+satellite.hasAlmanac());
					if (satellite.usedInFix()) {
						++count;
					}
				}
				if (locationInfo.msgGpsInfo != null) {
					locationInfo.msgGpsInfo.uint32SatellitesNum = count;
					FakeReqManager.getInstance().uploadCurrentGPS(locationInfo.msgGpsInfo.dblLat,
							locationInfo.msgGpsInfo.dblLng);

					//记录驾驶距离
					if (mLocationClient instanceof LocationClientOfAMap) {
						LocationClientOfAMap clientOfAMap = (LocationClientOfAMap) mLocationClient;
						if (clientOfAMap.getLocationType() == AMapLocation.LOCATION_TYPE_GPS && clientOfAMap.getSatellitesNum() >= 3) {
							recordDrivingDistance(locationInfo.msgGpsInfo.dblLat, locationInfo.msgGpsInfo.dblLng);
						} else {
							clearRecordGps();
						}
					} else if (locationInfo.msgGpsInfo.uint32SatellitesNum >= 3) {
						recordDrivingDistance(locationInfo.msgGpsInfo.dblLat, locationInfo.msgGpsInfo.dblLng);
					} else {
						clearRecordGps();
					}
				}
				
				if (mLastSatellitesNum == null || (mLastSatellitesNum < 5 && count >= 5) || mLastSatellitesNum >= 5 && count < 5) {
					mLastSatellitesNum = count;
					LogUtil.logw("getSatellites count=" + count);
				}
			} catch (Exception e) {
			}
			
			++mLocSuccessCount;
			if (DebugCfg.ENABLE_TRACE_GPS &&  locationInfo.msgGpsInfo != null) {
				JNIHelper.logd("update location: lat="+locationInfo.msgGpsInfo.dblLat+", lng="+locationInfo.msgGpsInfo.dblLng+ ", satellites=" + locationInfo.msgGpsInfo.uint32SatellitesNum + ", client="+mLocationClient);
			}

			do {
				if (mLocationClient == mClientOfOutter) {
					break;
				}
				
				synchronized (LocationManager.class) {
					// 指定定位工具为系统或没有指定定位工具，使用高德/百度定位到10次后切换到系统的gps定位
					if((locationFlag == UiEquipment.LOCATION_FLAG_ANDROID || locationFlag == UiEquipment.LOCATION_FLAG_DEFAULT)
							&& mLocSuccessCount > 10 && (mLocationClient instanceof LocationClientOfBaidu || mLocationClient instanceof LocationClientOfAMap)){
//					if (mLocSuccessCount > 10 && !(mLocationClient instanceof LocationClientOfQihoo) && !(mLocationClient instanceof LocationClientOfTencent)
//							&& !(mLocationClient instanceof LocationClientOfAndroidSystem)) {
						if (sUseAndroidSysGps) {
							JNIHelper.logd("switch to android gps location system");
							mLocationClient.release();
							mLocationClient = new LocationClientOfAndroidSystem();
							mLocationClient.setTimeInterval(mTimeInterval);
						}
						mLocationClient.setLastLocation(locationInfo);
						mLocationClient.quickLocation(mQuick);
					}
				}
			} while (false);
			
			synchronized (LocationManager.class){
				GpsInfo gps = new GpsInfo();
				gps.dblLat=locationInfo.msgGpsInfo.dblLat;
				gps.dblLng=locationInfo.msgGpsInfo.dblLng;
				mLastLocationList.remove(0);
				mLastLocationList.add(gps);
			}
			boolean bReportTrace = true;
			try{
				bReportTrace = TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_REPORT_GPS_CFG,true);
			}catch (Exception e){
			}
			if(bReportTrace){
				JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_LOCATION_CHANGE, 0,
					MessageNano.toByteArray(locationInfo));
			}
			PreferenceUtil.getInstance().setLocationInfo(locationInfo);
			// 通知导航应用
			ServiceManager.getInstance().sendInvoke(ServiceManager.NAV, "txz.nav.updateMylocation",
					LocationInfo.toByteArray(locationInfo), null);
		}
		
		if((SystemClock.elapsedRealtime() - lastUpdateLoc) > mNotifyInterval) {
			lastUpdateLoc = SystemClock.elapsedRealtime();
			ServiceManager.getInstance().broadInvoke("tool.loc.updateLoc",
				locationInfo != null ? MessageNano.toByteArray(locationInfo) : null);
		}
				
		reinitLocationClientDelay();
	}
	
	private long lastUpdateLoc = 0;

	public void checkLocResApp() {
		synchronized (LocationManager.class) {
			try {
				if (mLocationClient != null && mLocationClient == mClientOfOutter && mClientOfOutter.isAvailable()) {
					return;
				}
				if (mClientOfOutter.isAvailable()) {
					if (mLocationClient != null && mLocationClient != mClientOfOutter) {
						mLocationClient.release();
					}
					mLocationClient = mClientOfOutter;
					LogUtil.logd("checkLocResApp use mClientOfOutter");
					quickLocation(mQuick);
					return;
				}
				
				Class<? extends ILocationClient> locClazz = null;

				if (locationFlag == UiEquipment.LOCATION_FLAG_DEFAULT && mLocToolType != null) {
					mLocSuccessCount = 0;
					if ("TXZ".equals(mLocToolType)) {
						if (mLocSuccessCount > 10) {
							locClazz = LocationClientOfAndroidSystem.class;
						} else {
							locClazz = LocationClientOfBaidu.class;
						}
					} else if ("QIHOO".equals(mLocToolType)) {
						locClazz = LocationClientOfQihoo.class;
					}else if("AMAP".equals(mLocToolType)) {
						if(mLocSuccessCount > 10) {
							locClazz = LocationClientOfAndroidSystem.class;
						} else {
							locClazz = LocationClientOfAMap.class;
						}
					}
				} else {
					if (PackageManager.getInstance().checkAppExist(NavQihooImpl.PACKAGE_NAME)) {
						locClazz = LocationClientOfQihoo.class;
					} else if (locationFlag == UiEquipment.LOCATION_FLAG_TENCENT) {
						locClazz = LocationClientOfTencent.class;
					} else if(locationFlag == UiEquipment.LOCATION_FLAG_AMAP) {
						locClazz = LocationClientOfAMap.class;
					} else if(locationFlag == UiEquipment.LOCATION_FLAG_BAIDU) {
						locClazz = LocationClientOfBaidu.class;
					} else if(locationFlag == UiEquipment.LOCATION_FLAG_QIHOO) {
						locClazz = LocationClientOfQihoo.class;
					} else {
						if (mLocSuccessCount > 10) {
							locClazz = LocationClientOfAndroidSystem.class;
						} else {
							if (PackageManager.getInstance().manifestHasMeta(GlobalContext.get().getPackageName(),"txz.core.hasamap")) {
								locClazz = LocationClientOfAMap.class;
							}else{
								locClazz = LocationClientOfBaidu.class;
							}
						}
					}
				}
				LocationInfo locationInfo = getLastLocation();
//				if (mLocationClient != null && mLocationClient.getClass().equals(locClazz)) {
//					return;
//				}
				if (mLocationClient != null) {
					mLocationClient.release();
					if (mLocationClient instanceof LocationClientOfQihoo) {
						ActivityManager activityManager = (ActivityManager) GlobalContext.get()
								.getSystemService(Context.ACTIVITY_SERVICE);
						List<RunningAppProcessInfo> appProcessInfos = activityManager.getRunningAppProcesses();
						for (RunningAppProcessInfo info : appProcessInfos) {
							if ((ServiceManager.TXZ + ":qh_loc").equals(info.processName)) {
								android.os.Process.killProcess(info.pid);
							}
						}
					}
				}
				JNIHelper.logd("create new location client: " + locClazz.getName());
				mLocationClient = locClazz.newInstance();
				mLocationClient.setTimeInterval(mTimeInterval);
				mLocationClient.setLastLocation(locationInfo);
				mLocationClient.quickLocation(mQuick);
			} catch (Exception e) {
				JNIHelper.loge(e.getMessage());
			}
		}
	}
	
	private String mLocToolType = null;
	
	public byte[] processRemoteCommand(String packageName, String command, byte[] data) {
		if (command.equals("getLocation")) {
			LocationInfo lastLocation = getLastLocation();
			if(null == lastLocation){
				return null;
			}
			return LocationInfo.toByteArray(lastLocation);
		} else if (command.equals("cleartool")) {
			mLocToolType = null;
			checkLocResApp();
		} else if (command.equals("setInnerTool")) {
			mLocToolType = null;
			String toolType = new String(data);
			mLocToolType = toolType;
			checkLocResApp();
		}
		return null;
	}
	
	/**
	 * 设置定位工具
	 * @param flag
	 */
	public static void setLocationFlag(int flag){
		LogUtil.logd("setLocationFlag "+flag);
		if(flag == UiEquipment.LOCATION_FLAG_AMAP && !PackageManager.getInstance().manifestHasMeta(GlobalContext.get().getPackageName(),"txz.core.hasamap")){
			flag = UiEquipment.LOCATION_FLAG_ANDROID;
		}
		locationFlag = flag;
	}
		
	/** 设置定位的时间间隔 */
	public void setTimeInterval(int timeInterval) {
		LogUtil.logd("setTimeInterval="+timeInterval);
		if (timeInterval <= 0) {
			LogUtil.logw("setTimeInterval invalid parameter");
			return;
		}
		mTimeInterval = timeInterval;
		if (mLocationClient != null) {
			removeReinitDelayRunnable();
			mReinitRunnable.run();
		}
	}
	
	private double mLastLat = 0;
	private double mLastLng = 0;
	private long mLastDrivingDistance = 0;
	private long mDrivingDistance = 0;
	private long mLastUpdateTime;
	private long mUpdateInterval = 60 * 1000;
	/**
	 * 记录驾驶的总里程数，本次驾驶里程数
	 * 单位/m
	 * @param lat
	 * @param lng
	 */
	private void recordDrivingDistance(double lat, double lng) {
		if (mLastLat == 0 && mLastLng == 0) {
			mLastLat = lat;
			mLastLng = lng;
			return;
		}

		double distance = DistanceUtil.getDistanceFromLngLat(mLastLat, mLastLng, lat, lng);
		double speed = distance / mTimeInterval;
		if (speed > 60.0) {//时速>216KM/h无效
			return;
		}

		long CurrentTime = SystemClock.elapsedRealtime();
		if (CurrentTime - mLastUpdateTime >= mUpdateInterval) {
			mLastUpdateTime = CurrentTime;
			mDrivingDistance += distance;
			PreferenceUtil.getInstance().setCurrentDrivingDistance(mDrivingDistance);
			long totalDrivingDistance = mDrivingDistance - mLastDrivingDistance + PreferenceUtil.getInstance().getTotalDrivingDistance();
			PreferenceUtil.getInstance().setTotalDrivingDistance(totalDrivingDistance);
			mLastDrivingDistance = mDrivingDistance;
		} else {
			mDrivingDistance += distance;
		}
		mLastLat = lat;
		mLastLng = lng;
	}

	/**
	 * 轨迹点卫星数，定位类型不满足的时候，清空基准点
	 */
	private void clearRecordGps(){
		mLastLat = 0;
		mLastLng = 0;
	}
}
