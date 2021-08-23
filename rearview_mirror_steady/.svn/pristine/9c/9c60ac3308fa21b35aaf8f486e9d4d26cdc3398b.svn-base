package com.txznet.txz.module.nav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.alibaba.fastjson.JSON;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.baidu.mapapi.SDKInitializer;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.qihu.mobile.lbs.appfactory.QHAppFactory;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.UserConfig;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.equipment.UiEquipment.Destination;
import com.txz.ui.equipment.UiEquipment.Req_Destination;
import com.txz.ui.equipment.UiEquipment.Resp_CommonCity;
import com.txz.ui.equipment.UiEquipment.Resp_Destination;
import com.txz.ui.equipment.UiEquipment.Resp_POI_Parse;
import com.txz.ui.equipment.UiEquipment.Resp_TXZPoiSearch;
import com.txz.ui.event.UiEvent;
import com.txz.ui.im.UiIm;
import com.txz.ui.im.UiIm.ActionRoomIn_Req;
import com.txz.ui.im.UiIm.ActionRoomMemberList_Req;
import com.txz.ui.im.UiIm.ActionRoomOut_Req;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txz.ui.map.UiMap.NearbySearchInfo;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.AsrUtil.IAsrRegCmdCallBack;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.ReportUtil.Report.Builder;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.comm.ui.dialog2.WinNotice;
import com.txznet.comm.ui.dialog2.WinProcessing;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMapPoiListView;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMapPoiListView.MapPoiConTrol;
import com.txznet.comm.util.DatasUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager.AsrMode;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchInfo;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.sdk.bean.TxzPoi;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.choice.list.PoiWorkChoice.PoisData;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.nav.INav.NavPlanType;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.baidu.BDConstants.BDHelper;
import com.txznet.txz.component.nav.baidu.NavBaiduDeepImpl;
import com.txznet.txz.component.nav.cld.NavCldImpl;
import com.txznet.txz.component.nav.gaode.NavAmapAutoNavImpl;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.component.nav.mx.NavMXImpl;
import com.txznet.txz.component.nav.qihoo.NavQihooImpl;
import com.txznet.txz.component.nav.remote.RemoteNavImpl;
import com.txznet.txz.component.nav.txz.NavTxzImpl;
import com.txznet.txz.component.poi.baidu.PoiSearchToolBaiduImpl;
import com.txznet.txz.component.poi.baidu.PoiSearchToolBaiduLocalImpl;
import com.txznet.txz.component.poi.dzdp.BussinessPoiSearchDzdpImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGDLocalImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeOnWay;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.component.poi.mx.PoiSearchToolMXImpl;
import com.txznet.txz.component.poi.qihoo.PoiSearchToolQihooImpl;
import com.txznet.txz.component.poi.txz.CenterPoiSearchResultListener;
import com.txznet.txz.component.poi.txz.CenterPoiSearchResultListener.NextStepListener;
import com.txznet.txz.component.poi.txz.ChaosPoiSearchTXZImpl;
import com.txznet.txz.component.poi.txz.CityPoiSearchToolTXZimpl;
import com.txznet.txz.component.poi.txz.MaxRaduisNearPoiSearchToolTXZimpl;
import com.txznet.txz.component.poi.txz.NearToCityPoiSearchToolTXZimpl;
import com.txznet.txz.component.poi.txz.PoiSearchToolConTXZImpl;
import com.txznet.txz.component.poi.txz.PoiSearchToolTxzPoiImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.fake.FakeReqManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.nav.tool.NavAppManager.NavAppBean;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.net.NetworkUtil;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.module.userconf.ConfigData;
import com.txznet.txz.module.userconf.UserConf;
import com.txznet.txz.module.version.VisualUpgradeManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.plugin.PluginManager.CommandProcessor;
import com.txznet.txz.ui.win.nav.BDLocationUtil;
import com.txznet.txz.ui.win.nav.MapPoiView;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.ui.win.record.RecorderWin.StatusObervable.StatusObserver;
import com.txznet.txz.util.BeepPlayer;
import com.txznet.txz.util.KeywordsParser;
import com.txznet.txz.util.LocationUtil;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;

/**
 * 导航管理模块，负责导航状态处理，导航事件处理
 * 
 * @author bihongpi
 *
 */
public class NavManager extends IModule {
	public static final int LOCATION_NONE = 0;
	public static final int LOCATION_HOME = 1;
	public static final int LOCATION_COMPANY = 2;
	public static final int LOCATION_JINGYOU = 3;
	public static final int LOCATION_END = 4;
	public static final int REQUEST_DESTINATION_TIME_OUT = 5 * 1000;	

	public static final int POIMAP_MINSWITCH = 800;
	public static final int POIMAP_LISTMODE = 1;
	public static final int POIMAP_MAPMODE = 2;
	public static int sSearchCount = 8;
	static NavManager sModuleInstance = new NavManager();

	private boolean mIsPass;
	public boolean mSearchByEdit;
	public boolean mSearchBySelect;
	private long mPoiActivityDismissDelay;
	private long mPoiActivtiyStartNavDismissDelay = -1;
	public int mIsAddOnWay=0;
	private boolean mIsEnd=false;
	private static final int NO_ONWAY=1;
	private static final int SHOW_LIST_ONWAY=2;
	private static final int NO_SHOW_ONWAY=3;
	
	//默认的前置禁用引擎配置，默认禁用同行者后台搜索
	private static final int DEFAULT_SEARCH_CONFIG=(~1);
	
	// 默认的单次超时时间
	private static final int DEFAULT_SEARCH_TIMEOUT = 10 * 1000;
	// 默认的重试次数
	private static final int DEFAULT_SEARCH_RETRYCOUNT = 0;
	// 保护的单次超时时间最小值
	private static final int MIN_SEARCH_TIMEOUT = 500;
	// 保护的单次超时时间范围
	private static final int MAX_SEARCH_TIMEOUT = 20 * 1000;
	// 保护的最大重试次数
	private static final int MAX_SEARCH_RETRY = 10;

	private Integer mPoisearchConfig = DEFAULT_SEARCH_CONFIG;
	private Integer mPoisearRetryCount = 0;
	private Integer mPoisearTimeCost = 10000;

	// 是否总是询问选择导航工具
	private boolean mAlwayAsk = false;
	// 是否启用多导航功能
	private boolean mEnableMulti = false;
	// 是否支持设置默认导航
	public boolean mSupportDefaultNav = false;
	// 仅针对高德去掉所有弹框
	public boolean mRemoveNavDialog = false;
    private boolean isCloseWhenSetHcAddr = false;
	
	private NavManager() {
	}

	public static NavManager getInstance() {
		return sModuleInstance;
	}

    public boolean isCloseWhenSetHcAddr() {
        return isCloseWhenSetHcAddr;
    }

	public boolean isEnablePoiToolSearch(PoiSearchInfo info, int engine) {
		// 搜索工具是否可用，搜索工具ID位运算，不等于零表示可用
		boolean enable = ((info.getPoiSourceConf() & engine) != 0);
		JNIHelper.logd("POISearchLog:engine id = " + engine);
		JNIHelper.logd("POISearchLog:getPoiSourceConf id is ="+ info.getPoiSourceConf());
		JNIHelper.logd("POISearchLog:isEnableTxiPoiSearch = " + enable);
		return enable;
	}
	//根据后台来过滤POI的结果
	private String mTargetCity = null;
	private List<Poi> mTargetCityPoiList = new ArrayList<Poi>(); 
	public void filterPoiResult( PoiSearchInfo info,  List<Poi> poiList ) {
		int disShowEngine=info.getDisShowEngine();
		JNIHelper.logd("POISearchLog:info getDisShowEngine ="+disShowEngine);
		JNIHelper.logd("POISearchLog:return POIs Type is ="+poiList.get(0).getSourceType()+" count is "+poiList.size() );
		if(disShowEngine==0){
			if( mSearchBySelect && !TextUtils.isEmpty(mTargetCity) ){
				for(int i=0;i<poiList.size();i++){
					if( poiList.get(i).getCity().equals(mTargetCity) && !checkSamePoiForList(mTargetCityPoiList, poiList.get(i)) ){
						mTargetCityPoiList.add(poiList.get(i));
					}					
				}
			}
			return;
		}
			
		for(int i=0;i<poiList.size();){
			
			if( ( disShowEngine & ( 1<<(poiList.get(i).getSourceType()-1) ))!=0){
				JNIHelper.logd("POISearchLog:this Poi type is "+(poiList.get(i).getSourceType())+" remove");
				poiList.remove(i);
				continue;	
			}
			i++;
		}
	}

	public long getFinishDelayMillis() {
		return mPoiActivityDismissDelay;
	}

	public long getStartNavDelayMillis() {
		return mPoiActivtiyStartNavDismissDelay;
	}

	/**
	 * 
	 * @param name
	 * @param address
	 * @param lat
	 *            BD09
	 * @param lng
	 *            BD09
	 */
	public void setHomeLocation(String name, String address, double lat,
			double lng, int gpsType) {
		setHomeLocation(name, address, lat, lng, gpsType, true);
	}

	// 设置家的地址，并通知导航工具
	public void setHomeLocation(final String name, final String address, final double lat, final double lng, final int gpsType, final boolean needSync) {
		JNIHelper.logd("setHomeLocation:" + name + ",address:" + address);
		if (NavInscriber.getInstance().needReseveSalName(name)
				|| NavInscriber.getInstance().needReseveSalName(address)) {
			LocationManager.getInstance().reverseGeoCode(lat, lng, new OnGeocodeSearchListener() {

				@Override
				public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
					LogUtil.logd("onRegeocodeSearched:" + arg0 + ",arg1:" + arg1);
					if (arg0 == null) {
						return;
					}
					RegeocodeAddress regeocodeAddress = arg0.getRegeocodeAddress();
					if (regeocodeAddress != null) {
						String geoInfo = regeocodeAddress.getFormatAddress();
						LogUtil.logd("setHomeLocation onRegeocodeSearched geoInfo:" + geoInfo);
						String poiName = geoInfo;
						if (regeocodeAddress.getProvince() != null) {
							poiName = poiName.replace(regeocodeAddress.getProvince(), "");
						}
						if (regeocodeAddress.getCity() != null) {
							poiName = poiName.replace(regeocodeAddress.getCity(), "");
						}
						if (regeocodeAddress.getDistrict() != null) {
							poiName = poiName.replace(regeocodeAddress.getDistrict(), "");
						}
						if (regeocodeAddress.getTownship() != null) {
							poiName = poiName.replace(regeocodeAddress.getTownship(), "");
						}
						if (!NavInscriber.getInstance().needReseveSalName(name)) {
							poiName = name;
						}
						if (!NavInscriber.getInstance().needReseveSalName(address)) {
							geoInfo = address;
						}

						AppLogic.removeBackGroundCallback(mTimeoutTask);
						setHomeLocationInner(poiName, geoInfo, lat, lng, gpsType, needSync);
					}
				}

				@Override
				public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
				}
			});
			mTimeoutTask = new Runnable() {
				
				@Override
				public void run() {
					setHomeLocationInner(name, address, lat, lng, gpsType, needSync);
				}
			};
			AppLogic.runOnBackGround(mTimeoutTask, 5000);
		} else {
			setHomeLocationInner(name, address, lat, lng, gpsType, needSync);
		}
	}

	Runnable mTimeoutTask = null;

	////////////////////////////先暂时缓存，等待1~2秒后再覆盖，以解决同时设置不生效的问题////////////////////////
	UserConfig mCurrUserConfig = null;
	Runnable mDelayCoverTask = new Runnable() {
		
		@Override
		public void run() {
			if (mCurrUserConfig != null) {
				JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG, UiData.DATA_ID_CONFIG_USER,
						UserConfig.toByteArray(mCurrUserConfig));
				mCurrUserConfig = null;
				LogUtil.logd("delayCover run");
			}
		}
	};
	//////////////////////////////////////////////////////////
	
	private void setHomeLocationInner(String name, String address, double lat, double lng, int gpsType,
			boolean needSync) {
		if (name == null) {
			name = "";
		}
		if (address == null) {
			address = "";
		}
		NavigateInfo navigateInfo = new NavigateInfo();
		navigateInfo.uint32Time = NativeData.getServerTime();
		navigateInfo.strTargetName = name;
		navigateInfo.strTargetAddress = address;
		navigateInfo.msgGpsInfo = new GpsInfo();
		if (UiMap.GPS_TYPE_BD09 == gpsType) {
			double xy[] = LocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			lat = xy[0];
			lng = xy[1];
		}

		navigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;

		navigateInfo.msgGpsInfo.dblLat = lat;
		navigateInfo.msgGpsInfo.dblLng = lng;
		UserConfig userConfig = mCurrUserConfig;
		if (userConfig == null) {
			userConfig = NativeData.getCurUserConfig();
		}
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();

		boolean notNeedUpload = isEqualsAddr(lat, lng, name, address, userConfig.msgNetCfgInfo.msgHomeLoc);
		LogUtil.logd("setHomeLocation notNeedUpload:" + notNeedUpload);

		userConfig.msgNetCfgInfo.msgHomeLoc = navigateInfo;
		mCurrUserConfig = userConfig;
		AppLogic.removeBackGroundCallback(mDelayCoverTask);
		AppLogic.runOnBackGround(mDelayCoverTask, 1 * 1000);

		if (!notNeedUpload) {
			CollectLocsPlugin.uploadHomeLoc(mCurrUserConfig);
		}

		// 通知导航应用
		ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
				"txz.nav.updateHomeLocation",
				NavigateInfo.toByteArray(navigateInfo), null);

		NavThirdApp navTool = NavAppManager.getInstance().getInnerNavTool();
		if (needSync && navTool != null) {
			if (isImmediateApply(navTool)) {
				navTool.updateHomeLocation(navigateInfo);
				mTmphAddr = null;
				PreferenceUtil.getInstance().clearSyncAddress(true);
			} else {
				saveHcAddress(navigateInfo, true, navTool);
			}
		}
	}
	
	/**
	 * 是否立即将地址传给导航
	 * @param tool
	 * @return
	 */
	private boolean isImmediateApply(NavThirdApp tool) {
		if (tool == null) {
			return false;
		}
		if (!tool.enableWorkWithoutResume()) {
			return true;
		}
		if (!isJustModifyAddress()) {
			return true;
		}

		return tool.isInFocus() && !WinManager.getInstance().isActivityDialog();
	}
	
	private NavigateInfo mTmphAddr;
	private NavigateInfo mTmpcAddr;
	
	public void applyHcAddressToNav(NavThirdApp navtool, boolean coverUser) {
		LogUtil.logd("applyHcAddressToNav");
		if (navtool == null) {
			return;
		}

		if (mTmphAddr == null) {
			mTmphAddr = PreferenceUtil.getInstance().getSyncHcAddress(true);
		}
		if (mTmpcAddr == null) {
			mTmpcAddr = PreferenceUtil.getInstance().getSyncHcAddress(false);
		}

		if (mTmphAddr != null) {
			if (coverUser) {
				String name = mTmphAddr.strTargetName;
				String address = mTmphAddr.strTargetAddress;
				double lat = mTmphAddr.msgGpsInfo.dblLat;
				double lng = mTmphAddr.msgGpsInfo.dblLng;
				setHomeLocationInner(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02, true);
			} else {
				navtool.updateHomeLocation(mTmphAddr);
				mTmphAddr = null;
				PreferenceUtil.getInstance().clearSyncAddress(true);
			}
		}
		if (mTmpcAddr != null) {
			if (coverUser) {
				String name = mTmpcAddr.strTargetName;
				String address = mTmpcAddr.strTargetAddress;
				double lat = mTmpcAddr.msgGpsInfo.dblLat;
				double lng = mTmpcAddr.msgGpsInfo.dblLng;
				setCompanyLocationInner(name, address, lat, lng, UiMap.GPS_TYPE_GCJ02, true);
			} else {
				navtool.updateCompanyLocation(mTmpcAddr);
				mTmpcAddr = null;
				PreferenceUtil.getInstance().clearSyncAddress(false);
			}
		}
	}
	
	/**
	 * 是否需要同步地址
	 * @return
	 */
	public boolean needSyncAddress() {
		if (mTmphAddr == null) {
			mTmphAddr = PreferenceUtil.getInstance().getSyncHcAddress(true);
		}
		if (mTmpcAddr == null) {
			mTmpcAddr = PreferenceUtil.getInstance().getSyncHcAddress(false);
		}

		if (mTmphAddr == null && mTmpcAddr == null) {
			return false;
		}
		return true;
	}
	
	private void saveHcAddress(NavigateInfo navigateInfo, boolean isHome, final NavThirdApp navtool) {
		if (isHome) {
			mTmphAddr = navigateInfo;
		} else {
			mTmpcAddr = navigateInfo;
		}
		PreferenceUtil.getInstance().setSyncHomeCompanyAddress(isHome, navigateInfo);
		RecorderWin.addCloseRunnable(new Runnable() {

			@Override
			public void run() {
				if (navtool.isInFocus() && WinManager.getInstance().isActivityDialog()) {
					applyHcAddressToNav(navtool, false);
				}
			}
		});
	}

	/**
	 * 
	 * @param lat
	 * @param lng
	 * @param name
	 * @param addr
	 * @param 对比的地址
	 * @return
	 */
	private boolean isEqualsAddr(double lat, double lng, String name, String addr, NavigateInfo info) {
		if (info == null || info.msgGpsInfo == null) {
			if (lat == 0 || lng == 0) {
				return true;
			}
			return false;
		}

		if (!info.strTargetName.equals(name)) {
			return false;
		}
		if (!info.strTargetAddress.equals(addr)) {
			return false;
		}
		double dblat = info.msgGpsInfo.dblLat.doubleValue();
		double dblng = info.msgGpsInfo.dblLng.doubleValue();
		int distance = Math.abs(BDLocationUtil.calDistance(dblat, dblng, lat, lng));
		JNIHelper.logd("calDistance:" + distance);
		if (distance > 10) {
			return false;
		}

		return true;
	}

	/**
	 * 
	 * @param name
	 * @param address
	 * @param lat
	 *            BD09
	 * @param lng
	 *            BD09
	 */
	public void setCompanyLocation(String name, String address, double lat,
			double lng, int gpsType) {
		setCompanyLocation(name, address, lat, lng, gpsType, true);
	}

	// 设置公司地址并通知导航应用
	public void setCompanyLocation(final String name, final String address, final double lat, final double lng, final int gpsType, final boolean needSync) {
		JNIHelper.logd("setCompanyLocation:" + name + ",address:" + address);
		if (NavInscriber.getInstance().needReseveSalName(name)
				|| NavInscriber.getInstance().needReseveSalName(address)) {
			LocationManager.getInstance().reverseGeoCode(lat, lng, new OnGeocodeSearchListener() {

				@Override
				public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
					LogUtil.logd("setCompanyLocation onRegeocodeSearched:" + arg0 + ",arg1:" + arg1);
					if (arg0 == null) {
						return;
					}
					RegeocodeAddress regeocodeAddress = arg0.getRegeocodeAddress();
					if (regeocodeAddress != null) {
						String geoInfo = regeocodeAddress.getFormatAddress();
						LogUtil.logd("setCompanyLocation onRegeocodeSearched geoInfo:" + geoInfo);
						String poiName = geoInfo;
						if (regeocodeAddress.getProvince() != null) {
							poiName = poiName.replace(regeocodeAddress.getProvince(), "");
						}
						if (regeocodeAddress.getCity() != null) {
							poiName = poiName.replace(regeocodeAddress.getCity(), "");
						}
						if (regeocodeAddress.getDistrict() != null) {
							poiName = poiName.replace(regeocodeAddress.getDistrict(), "");
						}
						if (regeocodeAddress.getTownship() != null) {
							poiName = poiName.replace(regeocodeAddress.getTownship(), "");
						}
						if (!NavInscriber.getInstance().needReseveSalName(name)) {
							poiName = name;
						}
						if (!NavInscriber.getInstance().needReseveSalName(address)) {
							geoInfo = address;
						}

						AppLogic.removeBackGroundCallback(mTimeoutTask);
						setCompanyLocationInner(poiName, geoInfo, lat, lng, gpsType, needSync);
					}
				}

				@Override
				public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
				}
			});
			mTimeoutTask = new Runnable() {
				
				@Override
				public void run() {
					setCompanyLocationInner(name, address, lat, lng, gpsType, needSync);
				}
			};
			AppLogic.runOnBackGround(mTimeoutTask, 5000);
		} else {
			setCompanyLocationInner(name, address, lat, lng, gpsType, needSync);
		}
	}
	
	private void setCompanyLocationInner(String name, String address, double lat, double lng, int gpsType, boolean needSync){
		if (name == null) {
			name = "";
		}
		if (address == null) {
			address = "";
		}
		NavigateInfo navigateInfo = new NavigateInfo();
		navigateInfo.uint32Time = NativeData.getServerTime();
		navigateInfo.strTargetName = name;
		navigateInfo.strTargetAddress = address;

		navigateInfo.msgGpsInfo = new GpsInfo();
		if (UiMap.GPS_TYPE_BD09 == gpsType) {
			double xy[] = LocationUtil.Convert_BD09_To_GCJ02(lat, lng);
			lat = xy[0];
			lng = xy[1];
		}

		navigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		navigateInfo.msgGpsInfo.dblLat = lat;
		navigateInfo.msgGpsInfo.dblLng = lng;
		UserConfig userConfig = mCurrUserConfig;
		if (userConfig == null) {
			userConfig = NativeData.getCurUserConfig();
		}
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();

		boolean notNeedUpload = isEqualsAddr(lat, lng, name, address, userConfig.msgNetCfgInfo.msgCompanyLoc);
		LogUtil.logd("setCompanyLocation notNeedUpload:" + notNeedUpload);

		userConfig.msgNetCfgInfo.msgCompanyLoc = navigateInfo;
		mCurrUserConfig = userConfig;
		AppLogic.removeBackGroundCallback(mDelayCoverTask);
		AppLogic.runOnBackGround(mDelayCoverTask, 1 * 1000);

		if (!notNeedUpload) {
			CollectLocsPlugin.uploadCompanyLoc(mCurrUserConfig);
		}

		// 通知导航应用
		ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
				"txz.nav.updateCompanyLocation",
				NavigateInfo.toByteArray(navigateInfo), null);

		NavThirdApp navTool = NavAppManager.getInstance().getInnerNavTool();
		if (needSync && navTool != null) {
			if (isImmediateApply(navTool)) {
				navTool.updateCompanyLocation(navigateInfo);
				mTmpcAddr = null;
				PreferenceUtil.getInstance().clearSyncAddress(false);
			} else {
				saveHcAddress(navigateInfo, false, navTool);
			}
		}
	}
	
	//清理家的信息
	public void clearHomeLocation() {
		JNIHelper.loge("clearHomeLocation");
		UserConfig userConfig = mCurrUserConfig;
		if (userConfig == null) {
			userConfig = mCurrUserConfig = NativeData.getCurUserConfig();
		}
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		if (userConfig.msgNetCfgInfo.msgHomeLoc != null) {
			NavigateInfo invInfo = new NavigateInfo();
			invInfo.uint32Time = NativeData.getServerTime();
			userConfig.msgNetCfgInfo.msgHomeLoc = invInfo;
			CollectLocsPlugin.uploadNavigateInfo(null, null, null, null, UiMap.HOME, NativeData.getServerTime(),
					mCurrUserConfig);
		}
		AppLogic.removeBackGroundCallback(mDelayCoverTask);
		AppLogic.runOnBackGround(mDelayCoverTask, 1 * 1000);
		NavAppManager.getInstance().clearHomeLocation();
	}

	// 清理公司信息
	public void clearCompanyLocation() {
		JNIHelper.loge("clearCompanyLocation");
		UserConfig userConfig = mCurrUserConfig;
		if (userConfig == null) {
			userConfig = mCurrUserConfig = NativeData.getCurUserConfig();
		}
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		if (userConfig.msgNetCfgInfo.msgCompanyLoc != null) {
			NavigateInfo invInfo = new NavigateInfo();
			invInfo.uint32Time = NativeData.getServerTime();
			userConfig.msgNetCfgInfo.msgCompanyLoc = invInfo;
			CollectLocsPlugin.uploadNavigateInfo(null, null, null, null, UiMap.COMPANY, NativeData.getServerTime(),
					mCurrUserConfig);
		}
		AppLogic.removeBackGroundCallback(mDelayCoverTask);
		AppLogic.runOnBackGround(mDelayCoverTask, 1 * 1000);
		NavAppManager.getInstance().clearCompanyLocation();
	}

	/**
	 * notify nav app to update home location
	 */
	public void notifyUpdateHomeLocation(NavigateInfo navigateInfo) {
		NavThirdApp navThirdApp = getLocalNavImpl();
		if (navThirdApp != null && navThirdApp instanceof NavThirdComplexApp) {
			((NavThirdComplexApp)navThirdApp).updateHomeLocation(navigateInfo);
		}
	}

	/**
	 * notify nav app to update company location
	 */
	public void notifyUpdateCompanyLocation(NavigateInfo navigateInfo) {
		NavThirdApp navThirdApp = getLocalNavImpl();
		if (navThirdApp != null && navThirdApp instanceof NavThirdComplexApp) {
			((NavThirdComplexApp)navThirdApp).updateCompanyLocation(navigateInfo);
		}
	}

	public void setHistroyLocation(NavigateInfo info, boolean delete) {
		if (info == null) {
			return;
		}

		UserConfig userConfig = NativeData.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		if (userConfig.msgNetCfgInfo.msgHistoryLocs == null)
			userConfig.msgNetCfgInfo.msgHistoryLocs = new NavigateInfoList();
		if (userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem.length == 0) {
			userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem = new NavigateInfo[1];
			userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem[0] = info;
		} else {
			// 数组转换list
			List<NavigateInfo> navilist = new ArrayList<NavigateInfo>(
					Arrays.asList(userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem));
			// 查找相同项
			for (int i = 0; i < navilist.size(); i++) {
				NavigateInfo naviInfo = navilist.get(i);
				if (info.msgGpsInfo.dblLat == naviInfo.msgGpsInfo.dblLat
						.doubleValue()
						&& info.msgGpsInfo.dblLng == naviInfo.msgGpsInfo.dblLng
								.doubleValue()) {
					// 删除相同项
					navilist.remove(i);
					break;
				}
			}

			if (!delete) {
				navilist.add(0, info);
			}

			userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem = new NavigateInfo[navilist
					.size()];
			navilist.toArray(userConfig.msgNetCfgInfo.msgHistoryLocs.rptMsgItem);
		}

		// 通知导航历史记录改变
		ServiceManager.getInstance().sendInvoke(
				ServiceManager.NAV,
				"txz.location.historylistchange",
				NavigateInfoList
						.toByteArray(userConfig.msgNetCfgInfo.msgHistoryLocs),
				null);
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG,
				UiData.DATA_ID_CONFIG_USER, UserConfig.toByteArray(userConfig));
	}
	
	public static final int MODIFY_HC_ADDRESS = 1 << 0;

	private int mFlag = MODIFY_HC_ADDRESS;

	public boolean isJustModifyAddress() {
		return (mFlag & MODIFY_HC_ADDRESS) == MODIFY_HC_ADDRESS;
	}

	public void setHcFlag(int flag) {
		LogUtil.logd("setHcFlag:" + flag);
		this.mFlag = flag;
	}

	/**
	 * action 取值区分我要回家和修改家
	 * @param action
	 */
	public void ModifyHome(final int busyType) {
		mFlag = busyType;
		mReq_Task = new Req_Task() {

			@Override
			public void onGetPoisData(Destination[] destinations) {
				PoisData poisData = new PoisData();
				poisData.action = PoiAction.ACTION_RECOMM_HOME;
				poisData.isBus = false;
				poisData.mPois = new ArrayList<Poi>();
				for (Destination d : destinations) {
					poisData.mPois.add(convDestinationToPoi(d, PoiAction.ACTION_RECOMM_HOME));
				}
				CompentOption<Poi> option = new CompentOption<Poi>();
				option.setTtsText(NativeData.getResString("RS_VOICE_WHERE_IS_YOUR_HOME_RECOMMAND"));
				option.setCanSure(false);
				option.setProgressDelay(0);
				ChoiceManager.getInstance().showPoiList(poisData, option);
			}
		};
		mReq_Task.mTargetRunTask = new Runnable() {

			@Override
			public void run() {
				mReq_Task = null;
				String txt = NativeData.getResString("RS_VOICE_WHERE_IS_YOUR_HOME");
				RecorderWin.open(txt, VoiceData.GRAMMAR_SENCE_SET_HOME, new Runnable() {
					@Override
					public void run() {
						AsrManager.getInstance().mSenceRepeateCount = 0;
					}
				});
			}
		};
		requestRecommand(UiEquipment.DESTINATION_TYPE_HOME);
		if (mReq_Task.mTargetRunTask != null) {
			AppLogic.removeBackGroundCallback(mReq_Task.mTargetRunTask);
			AppLogic.runOnBackGround(mReq_Task.mTargetRunTask, REQUEST_DESTINATION_TIME_OUT);
		}
	}
	
	public void NavigateHome() {
		// if (!TextUtils.isEmpty(mNavToolServiceName)) {
		// ServiceManager.getInstance().sendInvoke(mNavToolServiceName,
		// "tool.nav.navHome", null, null);
		// return;
		// }
		String txt = NativeData.getResString("RS_VOICE_WILL_GO_HOME");
		NavThirdApp nta = getLocalNavImpl();
		if (nta instanceof NavCldImpl/** && !hasRemoteProcTool()**/) {
			boolean isNav = ((NavCldImpl) nta).naviHome(txt);
			if (isNav) {
				return;
			} else {
				ModifyHome(0);
			}
			return;
		}

		final com.txz.ui.data.UiData.UserConfig userConfig = NativeData
				.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null
				|| userConfig.msgNetCfgInfo.msgHomeLoc == null
				||TextUtils.isEmpty(userConfig.msgNetCfgInfo.msgHomeLoc.strTargetName)) {
			ModifyHome(0);
			return;
		}
		if (needAsrSelectNav()) {
			NavigateTo(userConfig.msgNetCfgInfo.msgHomeLoc, PoiAction.ACTION_HOME);
			return;
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(txt, new Runnable() {
			@Override
			public void run() {
				NavigateTo(userConfig.msgNetCfgInfo.msgHomeLoc, PoiAction.ACTION_HOME);
			}
		});
	}

	/**
	 * 获取保存的家的地址
	 * @return
	 */
	public NavigateInfo getHomeNavigateInfo() {
		UserConfig userConfig = mCurrUserConfig;
		if (userConfig == null) {
			userConfig = NativeData.getCurUserConfig();
		}
		if (userConfig == null) {
			return null;
		}
		if (userConfig.msgNetCfgInfo == null) {
			return null;
		}
		return userConfig.msgNetCfgInfo.msgHomeLoc;
	}

	/**
	 * 获取保存的公司的地址
	 * @return
	 */
	public NavigateInfo getCompanyNavigateInfo() {
		UserConfig userConfig = mCurrUserConfig;
		if (userConfig == null) {
			userConfig = NativeData.getCurUserConfig();
		}

		if (userConfig == null) {
			return null;
		}
		if (userConfig.msgNetCfgInfo == null) {
			return null;
		}
		return userConfig.msgNetCfgInfo.msgCompanyLoc;
	}

	public boolean isLegalNavigateInfo(NavigateInfo info) {
		if (info == null || info.msgGpsInfo == null) {
			return false;
		}

		GpsInfo gpsInfo = info.msgGpsInfo;
		if (gpsInfo.dblLat == null || gpsInfo.dblLng == null || gpsInfo.dblLat == 0 || gpsInfo.dblLng == 0) {
			return false;
		}
		return true;
	}

	/**
	 * action 取值区分我要回家和修改家
	 * @param action
	 */
	public void ModifyCompany(final int busyType) {
		mFlag = busyType;
		mReq_Task = new Req_Task() {

			@Override
			public void onGetPoisData(Destination[] destinations) {
				PoisData poisData = new PoisData();
				poisData.action = PoiAction.ACTION_RECOMM_COMPANY;
				poisData.isBus = false;
				poisData.mPois = new ArrayList<Poi>();
				for (Destination d : destinations) {
					poisData.mPois.add(convDestinationToPoi(d, PoiAction.ACTION_RECOMM_COMPANY));
				}
				CompentOption<Poi> option = new CompentOption<Poi>();
				option.setTtsText(NativeData.getResString("RS_VOICE_WHERE_IS_YOUR_COMPANY_RECOMMAND"));
				option.setCanSure(false);
				option.setProgressDelay(0);
				ChoiceManager.getInstance().showPoiList(poisData, option);
			}
		};
		mReq_Task.mTargetRunTask = new Runnable() {

			@Override
			public void run() {
				mReq_Task = null;
				String txt = NativeData.getResString("RS_VOICE_WHERE_IS_YOUR_COMPANY");
				RecorderWin.open(txt, VoiceData.GRAMMAR_SENCE_SET_COMPANY, new Runnable() {
					@Override
					public void run() {
						AsrManager.getInstance().mSenceRepeateCount = 0;
					}
				});
			}
		};
		requestRecommand(UiEquipment.DESTINATION_TYPE_COMPANY);
		if (mReq_Task.mTargetRunTask != null) {
			AppLogic.removeBackGroundCallback(mReq_Task.mTargetRunTask);
			AppLogic.runOnBackGround(mReq_Task.mTargetRunTask, REQUEST_DESTINATION_TIME_OUT);
		}
	}

	public void NavigateCompany() {
		// if (!TextUtils.isEmpty(mNavToolServiceName)) {
		// ServiceManager.getInstance().sendInvoke(mNavToolServiceName,
		// "tool.nav.navCompany", null, null);
		// return;
		// }
		String txt = NativeData.getResString("RS_VOICE_WILL_GO_COMPANY");
		NavThirdApp nta = getLocalNavImpl();
		if (nta instanceof NavCldImpl /**&& !hasRemoteProcTool()**/) {
			boolean isNavi = ((NavCldImpl) nta).naviCompany(txt);
			if (isNavi) {
				return;
			} else {
				ModifyCompany(0);
			}
			return;
		}

		final com.txz.ui.data.UiData.UserConfig userConfig = NativeData
				.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null
				|| userConfig.msgNetCfgInfo.msgCompanyLoc == null
				||TextUtils.isEmpty(userConfig.msgNetCfgInfo.msgCompanyLoc.strTargetName)) {
			ModifyCompany(0);
			return;
		}
		if (needAsrSelectNav()) {
			NavigateTo(userConfig.msgNetCfgInfo.msgCompanyLoc, PoiAction.ACTION_COMPANY);
			return;
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(txt, new Runnable() {
			@Override
			public void run() {
				NavigateTo(userConfig.msgNetCfgInfo.msgCompanyLoc, PoiAction.ACTION_COMPANY);
			}
		});
	}

	/**
	 * 分享的POI
	 */
	public Poi mSharePoi;
	
	/**
	 * cmd: wx.session.share_poi
		params:
		id: String 目标联系人id, 通过声控界面选择
		lat: double 经度
		lng: double 纬度
		addr: String 地址信息
	 * @param poi
	 */
	public void sharePoiToWx(Poi poi) {
		mSharePoi = poi;
		AsrManager.getInstance().cancel();
		TtsManager.getInstance().cancelSpeak(TtsManager.getInstance().getCurTaskId());
		if (!WeixinManager.getInstance().checkEnabled()) {
			WeixinManager.getInstance().requestLogin();
			return;
		}
		RecorderWin.show();
		String spk = NativeData.getResString("RS_WX_SEARCH_CONVERSATION");
		RecorderWin.addSystemMsg(spk);
		AsrManager.getInstance().setCloseRecordWinWhenProcEnd(false);
		WeixinManager.getInstance().mWeChatChoice = WeixinManager.ON_SHARE_POI;
		WeixinManager.getInstance().requestRecentSession();
	}

	public void NavigateTo(Poi p) {
		// 如果是空值，则填为导航
		if (TextUtils.isEmpty(p.getAction())) {
			p.setAction(PoiAction.ACTION_NAVI);
		}
		
		if (p.getSourceType() == Poi.POI_SOURCE_MEIXING && !TextUtils.isEmpty(p.getExtraStr())) {
			if (PackageManager.getInstance().checkAppExist(NavMXImpl.MX_PACKAGE_NAME)) {
				NavThirdApp nta = NavAppManager.getInstance().getNavToolByName(NavMXImpl.MX_PACKAGE_NAME);
				if (nta != null) {
					NavMXImpl nm = (NavMXImpl) nta;
					nm.NavigateByMCode(p.getName(), p.getExtraStr());
					return;
				}
			}
		}
		
		NavigateInfo navigateInfo = new NavigateInfo();
		navigateInfo.msgGpsInfo = new GpsInfo();
		navigateInfo.strTargetCity = p.getCity();
		navigateInfo.strTargetName = p.getName();
		navigateInfo.strTargetAddress = p.getGeoinfo();
		navigateInfo.msgGpsInfo.dblLat = p.getLat();
		navigateInfo.msgGpsInfo.dblLng = p.getLng();
		navigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		navigateInfo.uint32NavType = 1;// 1为语音发起，默认都是语音发起
		navigateInfo.strExtraInfo = p.getExtraStr();
		UiEquipment.Req_POI_Parse parse = new UiEquipment.Req_POI_Parse();
		parse.addr = navigateInfo.strTargetAddress.getBytes();
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
				UiEquipment.SUBEVENT_REQ_POI_PARSE, parse);

		mNavTargetPoi = p;
		NavAmapValueService.getInstance().setEnableAutoPupUp(false);
		NavigateTo(navigateInfo, p.getAction());
	}

	public void NavigateTo(final NavigateInfo info, final String action) {
		MtjModule.getInstance().event(MtjModule.EVENTID_NAV);
		if (info == null
				|| info.strTargetName == null
//				|| info.strTargetAddress == null
				|| info.msgGpsInfo == null
				|| info.msgGpsInfo.uint32GpsType == null
				|| info.msgGpsInfo.dblLat == null
				|| info.msgGpsInfo.dblLng == null) {
			JNIHelper.loge("wrong parameter");
			return;
		}

		// 转国测
		if (UiMap.GPS_TYPE_BD09 == info.msgGpsInfo.uint32GpsType) {
			double xy[] = BDLocationUtil.Convert_BD09_To_GCJ02(
					info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng);
			info.msgGpsInfo.dblLat = xy[0];
			info.msgGpsInfo.dblLng = xy[1];
			info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
		}

		if (needSelectAction(action)) {
			NavAppManager.getInstance().showNavTool(false, NativeData.getResString("RS_NAVTOOL_LIST_SPK"),
					new com.txznet.txz.component.choice.OnItemSelectListener<NavAppManager.NavAppBean>() {

						@Override
						public boolean onItemSelected(boolean isPreSelect, NavAppBean v, boolean fromPage, int idx,
								String fromVoice) {
							NavThirdApp nta = NavAppManager.getInstance().getNavToolByName(v.strPackageName);
							if (nta != null) {
								naviInner(nta, info, action);
							}
							return true;
						}
					});
			return;
		}
		if(mNavTargetPoi == null){
			mNavTargetPoi =new Poi();
		}
		mNavTargetPoi.setLat(info.msgGpsInfo.dblLat );
		mNavTargetPoi.setLat(info.msgGpsInfo.dblLng);
		mNavTargetPoi.setCity(mNavTargetPoi.getCity());
		naviInner(getLocalNavImpl(), info, action);
	}
	
	public boolean needSelectAction(String action) {
		if (!needAsrSelectNav()) {
			return false;
		}
		if (PoiAction.ACTION_NAVI.equals(action) || PoiAction.ACTION_HOME.equals(action)
				|| PoiAction.ACTION_COMPANY.equals(action) || PoiAction.ACTION_RECOMM_HOME.equals(action)
				|| PoiAction.ACTION_RECOMM_COMPANY.equals(action)
				|| PoiAction.ACTION_NAV_RECOMMAND.equals(action)) {
			return true;
		}
		return false;
	}
	
	/**
	 * 导航的时候是否提示选择导航工具
	 * 
	 * @param action
	 * @return
	 */
	public boolean needAsrSelectNav() {
		if (NavAppManager.getInstance().getNavAppsCount() <= 1) {
			return false;
		}
		if (!mEnableMulti) {
			return false;
		}

		if (mAlwayAsk) {
			return true;
		}

		return false;
	}
	
	private void naviInner(NavThirdApp nav,NavigateInfo info,String action){
		boolean navExists = false;
		if (nav != null) {
//			if((mIsAddOnWay==SHOW_LIST_ONWAY)||
//					(mIsAddOnWay==NO_SHOW_ONWAY)){
//				mIsAddOnWay=NO_ONWAY;
//				if(nav instanceof NavThirdComplexApp){
//					((NavThirdComplexApp)nav).procJingYouPoi(mNavTargetPoi);
//				}
//				return ;
//			}
			if (!PackageManager.getInstance().checkAppExist(
					nav.getPackageName())) {
			    navExists = false;
			} else {
			    navExists = true;
                NavAppManager.getInstance().notifyStartNav(info, nav);

                if (nav instanceof RemoteNavImpl) {
                    RemoteNavImpl.sNavAction = action;
                }
                nav.NavigateTo(NavPlanType.NAV_PLAN_TYPE_RECOMMEND, info);
            }
		}
		if (!navExists) {
			preInvokeWhenNavNotExists(new Runnable() {
				@Override
				public void run() {
					TtsManager.getInstance().speakText(NativeData.getResString("RS_VOICE_NO_NAV_TOOL"));
				}
			});
        }
		if (RecorderWin.isOpened()) {
			LogUtil.d("MapView RecorderWin.close() ");
			RecorderWin.close();
		}
	}
	
	@Override
	public int initialize_addPluginCommandProcessor() {
		PluginManager.addCommandProcessor("txz.nav.", new CommandProcessor() {
			
			@Override
			public Object invoke(String command, Object[] args) {
				try {
					if("navHome".equals(command)){
						NavigateHome();
					}else if("navCompany".equals(command)){
						NavigateCompany();
					}else if("navTo".equals(command)){
						if(!(args[0] instanceof UiMap.NavigateInfo)){
							return false;
						}
						UiMap.NavigateInfo navigateInfo = (NavigateInfo) args[0];  
						NavigateTo(navigateInfo, PoiAction.ACTION_NAVI);
						return true;
					} else if ("modifyHome".equals(command)) {// 修改家的地址
						if (!(args[0] instanceof String)
								|| !(args[1] instanceof String)
								|| !(args[2] instanceof Double)
								|| !(args[3] instanceof Double)
								|| !(args[4] instanceof Integer)) {
							return false;
						}
						String name = (String) args[0];
						String address = (String) args[1];
						double lat = (Double) args[2];
						double lng = (Double) args[3];
						int gpsType = (Integer) args[4];
						setHomeLocation(name, address, lat, lng, gpsType);
						return true;
					} else if ("modifyCompany".equals(command)) {// 修改公司的地址
						if (!(args[0] instanceof String)
								|| !(args[1] instanceof String)
								|| !(args[2] instanceof Double)
								|| !(args[3] instanceof Double)
								|| !(args[4] instanceof Integer)) {
							return false;
						}
						String name = (String) args[0];
						String address = (String) args[1];
						double lat = (Double) args[2];
						double lng = (Double) args[3];
						int gpsType = (Integer) args[4];
						setCompanyLocation(name, address, lat, lng, gpsType);
						return true;
					} else if ("invokeTXZNav".equals(command)) {
						if(!(args[0] instanceof String)){
							return false;
						}
						String navCommand = (String) args[0];
						byte[] data = "".getBytes();
						if (args.length > 1 && (args[1] instanceof byte[])) {
							data = (byte[]) args[1];
						}
						return invokeTXZNav("", navCommand, data);
					} else if ("tool.packageName".equals(command)) {
						NavThirdApp nta = getLocalNavImpl();
						if (nta != null) {
							return nta.getPackageName();
						}
					}
				} catch (Exception e) {
				}
				return null;
			}
		});

		return super.initialize_addPluginCommandProcessor();
	}


	class TXZSearchResultListener {
		Object searchTool;
		NearbyPoiSearchOption next_option;
		Object next_search_tool;
		CityPoiSearchOption option;
		boolean manual = false;
		boolean exactCity = false; // 确切的城市搜索
		String action = PoiAction.ACTION_NAVI;

		public void reset() {
			exactCity = true;
			manual = false;
			action = PoiAction.ACTION_NAVI;
			option = null;
			next_option = null;
			next_search_tool = null;
		}

		public void setAction(String a) {
			action = a;
		}

		public void setManual(boolean b) {
			manual = b;
		}

		public void setExactCity(boolean b) {
			exactCity = b;
		}

		public void setOption(CityPoiSearchOption opt) {
			option = opt;
		}

		public void setNextStep(NearbyPoiSearchOption opt, Object tool) {
			next_option = opt;
			next_search_tool = tool;
		}

		public void onSuggestion(SearchPoiSuggestion suggestion) {
			JNIHelper.logw("PoiSearch onSuggestion");
			// if (searchTool instanceof PoiSearchToolGaodeImpl || searchTool
			// instanceof PoiSearchToolGaodeWebImpl) {
			// // 如果百度搜索结果为空，改成高德搜索
			// searchTool = new PoiSearchToolGaodeImpl();
			// if (!(option instanceof NearbyPoiSearchOption) &&
			// TextUtils.isEmpty(option.getCity())
			// && !(suggestion.getCity().isEmpty())) {
			// // TODO 多个搜索建议目前默认采用第一个城市
			// if (suggestion.getCity().size() > 0) {
			// option.setCity(suggestion.getCity().get(0));
			// } else {
			// LocationInfo myLocation =
			// LocationManager.getInstance().getLastLocation();
			// if (myLocation != null && myLocation.msgGeoInfo != null
			// && !TextUtils.isEmpty(myLocation.msgGeoInfo.strCity)) {
			// option.setCity(myLocation.msgGeoInfo.strCity);
			// }
			// }
			// if (!(TextUtils.isEmpty(option.getCity()))) {
			// JNIHelper.logd("PoiSearch change gaode tool to current city
			// search: " + option.getCity());
			// mSearchReqLastPoiSearch = new
			// PoiSearchToolGaodeImpl().searchInCity(option,
			// mTXZPoiSearchResultListener);
			// return;
			// }
			// }
			// }
			this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
		}

		public void onResult(final List result, final boolean bussiness) {
			JNIHelper.logd("PoiSearch onResult size=" + result.size());
			if( result!=null){
				for(int i=0;i< result.size();i++){
					Poi poi= (Poi)result.get(i);
					JNIHelper.logd("Poi ["+i+"]"+"type is ="+poi.getSourceType());
				}
			}
			if(mIsAddOnWay==NO_SHOW_ONWAY){		
				if (result != null && result.size() > 0) {
					Poi poi=(Poi)result.get(0);
					poi.setAction(action);
					naviByPoiAction(poi);				
				}else{
					RecorderWin.speakTextWithClose("没查找到相关结果", null);
				}
				return ;
			}
			switch (result.size()) {
			case 0:
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_ALL);
				break;
			case 1:
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_RESULT_ONLY);
				break;
			case 2:
			case 3:
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_RESULT_LE3);
				break;
			default:
				break;
			}

			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_SUCCESS_ALL);

			AsrManager.getInstance().mSenceRepeateCount = -1;

			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					mRunnableSearchResult = new Runnable() {
						@Override
						public void run() {
							JNIHelper
									.logd("show result: TtsId=" + mSearchTtsId);
							AppLogic.removeUiGroundCallback(mRunnableSearchResult);
							mRunnableSearchResult = null;
							if (result == null || result.size() == 0) {

							}else {
								if (mSearchTtsId != TtsManager.INVALID_TTS_TASK_ID) {
									TtsManager.getInstance().cancelSpeak(
											mSearchTtsId);
								}
							}

							cancelAllPoiSearch();
							if (!RecorderWin.isOpened() && !manual) {
								cancelAllPoiSearchIncludeTts();
								return;
							}

							if (preInvokePoiSearchResult(option.getCity(),
									option.getKeywords(), action, result,
									bussiness)) {
								return;
							}
						}
					};
					if (mSearchTtsId == TtsManager.INVALID_TTS_TASK_ID) {
						mRunnableSearchResult.run();
						mRunnableSearchResult = null;
					} else {
						AppLogic.runOnUiGround(mRunnableSearchResult, 8000);
					}
				}
			}, 0);

		}

		public void showSearchError(final String hint) {
			if (manual) {
				new WinNotice(
						(WinNotice.WinNoticeBuildData)new WinNotice.WinNoticeBuildData()
						.setMessageText(hint)
						.setHintTts(hint)
						.setSystemDialog(true)){
					@Override
					public void onClickOk() {

					}

					@Override
					public String getReportDialogId() {
						return "nav_search_error_manual";
					}
				}.show();//WinNotice.showNotice(hint, true, true, null);

			} else {
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						// 遇到多次无法识别的文字，判断是否安装了宏祥人工语音导航
						String spk = NativeData.getResString("RS_MAP_ARITIFICIAL_SERVICE");
						if (PoiAction.ACTION_NAVI.equals(action)
								&& PackageManager.getInstance().checkAppExist("com.glsx.autonavi")) {
							WinConfirmAsr.WinConfirmAsrBuildData buildData = new WinConfirmAsr.WinConfirmAsrBuildData();
							buildData.setMessageText(spk);
							buildData.setSureText("是", new String[] { "是", "确定", "好的" });
							buildData.setCancelText("否", new String[] { "否", "取消", "不要" });
							buildData.setHintTts(hint + "," + spk);
							WinConfirmAsr win = new WinConfirmAsr(buildData) {
								@Override
								public void onClickOk() {
									JNIHelper.logd("start com.glsx.autonavi");
									Intent autoNaviIntent = new Intent();
									autoNaviIntent.setClassName("com.glsx.autonavi",
											"com.glsx.autonavi.ui.MainActivity");
									autoNaviIntent.putExtra("autonaviType", 1);
									autoNaviIntent.addFlags(
											Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_NEW_TASK
													| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
									GlobalContext.get().startActivity(autoNaviIntent);
								}

								@Override
								public String getReportDialogId() {
									return "nav_error_aritificial_service";
								}
							};
							win.show();
							RecorderWin.close();
							return;
						}

						AsrManager
								.getInstance()
								.setNeedCloseRecord(
										AsrManager.getInstance().mAsrMode == AsrMode.ASR_MODE_SINGLE);
						final Runnable runError = new Runnable() {
							@Override
							public void run() {
								JNIHelper.logd("show result: TtsId="
										+ mSearchTtsId);
								AppLogic.removeUiGroundCallback(mRunnableSearchResult);
								mRunnableSearchResult = null;
								if (mSearchTtsId != TtsManager.INVALID_TTS_TASK_ID) {
									TtsManager.getInstance().cancelSpeak(
											mSearchTtsId);
								}
								if (AsrManager.getInstance().mSenceRepeateCount >= 0) {
									AsrManager.getInstance().mSenceRepeateCount++;
									if (AsrManager.getInstance().mSenceRepeateCount < 2) {
										int grammar = AsrManager.getInstance()
												.getCurrentGrammarId();
										String txt = hint;
										if (PoiAction.ACTION_NAVI
												.equals(action)) {
											grammar = VoiceData.GRAMMAR_SENCE_NAVIGATE;
											txt = NativeData
													.getResPlaceholderString(
															"RS_MAP_DESTINATION_AGAIN",
															"%CMD%", hint);
										} else if (PoiAction.ACTION_HOME
												.equals(action)) {
											grammar = VoiceData.GRAMMAR_SENCE_SET_HOME;
											txt = NativeData
													.getResPlaceholderString(
															"RS_MAP_HOME_AGAIN",
															"%CMD%", hint);
										} else if (PoiAction.ACTION_COMPANY
												.equals(action)) {
											grammar = VoiceData.GRAMMAR_SENCE_SET_COMPANY;
											txt = NativeData
													.getResPlaceholderString(
															"RS_MAP_CONMANY_AGAIN",
															"%CMD%", hint);
										}
										RecorderWin.open(txt, grammar);

										return;
									}
								}
								RecorderWin.speakTextWithClose(hint, null);
							}
						};
						if (mSearchTtsId != TtsManager.INVALID_TTS_TASK_ID) {
							mRunnableSearchResult = runError;
							AppLogic.runOnUiGround(mRunnableSearchResult, 8000);
						} else {
							mRunnableSearchResult = null;
							runError.run();
						}
					}
				}, 0);
			}
		}

		public void onError(int errCode, String errDesc) {
			mHaveResult = false;
			JNIHelper.loge("PoiSearch onError err=" + errCode);
			cancelAllPoiSearch();

			if (searchTool instanceof BussinessPoiSearchDzdpImpl) {
				// 大众点评搜索失败，改用百度搜索
				searchTool = new PoiSearchToolBaiduImpl();
				if (option instanceof NearbyPoiSearchOption) {
					JNIHelper
							.logd("PoiSearch change baidu tool to nearby search");
					option.setNum(sSearchCount);
					mSearchReqLastPoiSearch = new PoiSearchToolBaiduImpl()
							.searchNearby((NearbyPoiSearchOption) option,
									mTXZPoiSearchResultListener);
				} else {
					JNIHelper
							.logd("PoiSearch change baidu tool to city search");
					option.setNum(sSearchCount);
					mSearchReqLastPoiSearch = new PoiSearchToolBaiduImpl()
							.searchInCity(option, mTXZPoiSearchResultListener);
				}
				return;
			} else if (searchTool instanceof PoiSearchToolBaiduImpl) {
				// 如果百度搜索结果为空，改成高德搜索
				searchTool = new PoiSearchToolGaodeImpl();
				if (option instanceof NearbyPoiSearchOption) {
					JNIHelper
							.logd("PoiSearch change gaode tool to nearby search");
					option.setNum(sSearchCount);
					mSearchReqLastPoiSearch = new PoiSearchToolGaodeImpl()
							.searchNearby((NearbyPoiSearchOption) option,
									mTXZPoiSearchResultListener);
				} else {
					JNIHelper
							.logd("PoiSearch change gaode tool to city search");
					option.setNum(sSearchCount);
					mSearchReqLastPoiSearch = new PoiSearchToolGaodeImpl()
							.searchInCity(option, mTXZPoiSearchResultListener);
				}
				return;
			} else if (searchTool instanceof PoiSearchToolGaodeImpl
					|| searchTool instanceof PoiSearchToolGaodeWebImpl) {
				// 如果百度搜索结果为空，改成高德搜索
				searchTool = new PoiSearchToolGaodeImpl();
				if (option instanceof NearbyPoiSearchOption) {
					JNIHelper
							.logd("PoiSearch change gaode tool to city search");
					option.setNum(sSearchCount);
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity(option.getCity())
							.setKeywords(option.getKeywords())
							.setNum(sSearchCount);
					option = opt;
					mSearchReqLastPoiSearch = new PoiSearchToolGaodeImpl()
							.searchInCity(option, mTXZPoiSearchResultListener);
					return;
				} else if (searchTool instanceof PoiSearchToolQihooImpl) {
					// 奇虎360搜索失败不处理
				} else if (!exactCity) {
					JNIHelper
							.logd("PoiSearch change gaode tool to empty city search");
					exactCity = true;
					option.setNum(sSearchCount);
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity("").setKeywords(option.getKeywords())
							.setNum(sSearchCount);
					option = opt;
					mSearchReqLastPoiSearch = new PoiSearchToolGaodeImpl()
							.searchInCity(option, mTXZPoiSearchResultListener);
					return;
				}
			}

			switch (errCode) {
			case TXZPoiSearchManager.ERROR_CODE_EMPTY: {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_EMPTY_ALL);

				if (PoiAction.ACTION_NAVI.equals(action)
						&& PackageManager.getInstance().checkAppExist(
								ServiceManager.WEBCHAT)) {
					AppLogic.runOnUiGround(new Runnable() {
						@Override
						public void run() {
							mRunnableSearchResult = new Runnable() {
								@Override
								public void run() {
									JNIHelper.logd("show result: TtsId="
											+ mSearchTtsId);
									AppLogic.removeUiGroundCallback(mRunnableSearchResult);
									mRunnableSearchResult = null;
									if (mSearchTtsId != TtsManager.INVALID_TTS_TASK_ID) {
										TtsManager.getInstance().cancelSpeak(
												mSearchTtsId);
									}

									// 声控界面展示没有结果
									if (mSearchByEdit || !manual) {
										if (preInvokePoiSearchResult(option.getCity(), option.getKeywords(), action,
												new ArrayList<Poi>(), true)) {
											return;
										}
									}

									// 搜索框没有结果展示
									if (!mSearchByEdit && manual) {
										String spk = NativeData
												.getResPlaceholderString(
														"RS_MAP_NOT_FOUND",
														"%KEYWORDS%",
														option.getKeywords());
										showSearchError(spk);
										return;
									}
								}
							};
							if (mSearchTtsId == TtsManager.INVALID_TTS_TASK_ID) {
								mRunnableSearchResult.run();
								mRunnableSearchResult = null;
							} else {
								AppLogic.runOnUiGround(mRunnableSearchResult,
										8000);
							}
						}
					}, 0);
					return;
				}

				if (mSearchByEdit || !manual) {
					mSearchByEdit = false;
					preInvokePoiSearchResult(option.getCity(),
							option.getKeywords(), action, new ArrayList<Poi>(),
							true);
					TtsManager.getInstance().cancelSpeak(mSearchTtsId);
					break;
				}
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_NOT_FOUND", "%KEYWORDS%", option.getKeywords());
				showSearchError(spk);
				break;
			}
			case TXZPoiSearchManager.ERROR_CODE_TIMEOUT: {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_ALL);

				AsrManager.getInstance().mSenceRepeateCount = -1;
				String hint = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCH_TIMEOUT", "%KEYWORDS%", option.getKeywords());
				if (procByNetNotWork(hint, manual)) {
					break;
				}
				showSearchError(hint);
				break;
			}
			default: {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_ALL);
				AsrManager.getInstance().mSenceRepeateCount = -1;
				String hintTxt = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCH_ERROR", "%KEYWORDS%", option.getKeywords());
				if (procByNetNotWork(hintTxt, manual)) {
					break;
				}
				showSearchError(hintTxt);
				break;
			}
			}
		}
	};

	class TXZPoiSearchResultListener implements PoiSearchResultListener {
		public void setOption(CityPoiSearchOption opt) {
			mTXZSearchResultListener.setOption(opt);
		}

		public void setNextStep(NearbyPoiSearchOption opt, Object tool) {
			mTXZSearchResultListener.setNextStep(opt, tool);
		}

		@Override
		public void onSuggestion(SearchPoiSuggestion suggestion) {
			cancelAllPoiSearch();
			mTXZSearchResultListener.onSuggestion(suggestion);
		}

		@Override
		public void onResult(List<Poi> result) {
			cancelAllPoiSearch();
			boolean bussiness = false;
			if (!result.isEmpty()) {
				int i = 0;
				while (i < result.size()) {
					if (result.get(i) instanceof BusinessPoiDetail) {
						bussiness = true;
						break;
					}
					i++;
				}
			}
			if(mTargetCityPoiList.size() > 0){
				List<Poi> list = new ArrayList<Poi>(mTargetCityPoiList);
				mTargetCityPoiList.clear();
				mTXZSearchResultListener.onResult(list, bussiness);
			}else{
				mTXZSearchResultListener.onResult(result, bussiness);
			}
			
		}

		@Override
		public void onError(int errCode, String errDesc) {
			cancelAllPoiSearch();
			mTXZSearchResultListener.onError(errCode, errDesc);
		}
	};

	// TODO PoiSearchTool new PoiSearchToolGaodeImpl() = new
	// PoiSearchToolGaodeImpl();
	// TODO PoiSearchTool new PoiSearchToolBaiduImpl() = new
	// PoiSearchToolBaiduImpl();
	// TODO BussinessPoiSearchTool new BussinessPoiSearchDzdpImpl() = new
	// BussinessPoiSearchDzdpImpl();
	TXZSearchResultListener mTXZSearchResultListener = new TXZSearchResultListener();
	TXZPoiSearchResultListener mTXZPoiSearchResultListener = new TXZPoiSearchResultListener();
	WinProcessing mWinProcessingPoiSearch;
	SearchReq mSearchReqLastPoiSearch;
	int mSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;
	int mRoughSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;

	ITtsCallback mPoiSearchTtsCallback = new ITtsCallback() {
		public void onEnd() {
			AppLogic.removeUiGroundCallback(mRunnableSearchResult);
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					mSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;
					if (mRunnableSearchResult != null) {
						mRunnableSearchResult.run();
						mRunnableSearchResult = null;
					} else {
						BeepPlayer.playWaitMusic();
					}
				}
			}, 0);

		};
	};

	Runnable mRunnableSearchResult = null;
	Runnable  mRunnableSearchTimeout=new Runnable() {
		@Override
		public void run() {
			JNIHelper.logd("POISearchLog:this time poi search timeout no back");
			cancelAllPoiSearch();
			mTXZSearchResultListener.onError(TXZPoiSearchManager.ERROR_CODE_TIMEOUT, "");
		}
	};
	public void cancelAllPoiSearchIncludeTts() {
		cancelAllPoiSearch();
		TtsManager.getInstance().cancelSpeak(mSearchTtsId);
		mSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;
	}
	public void disMiss(){
		mIsAddOnWay=NO_ONWAY;
		cancelAllPoiSearch();
	}
	public void cancelAllPoiSearch() {
		JNIHelper.logd("cancelAllPoiSearch");
		AppLogic.removeUiGroundCallback(mRunnableSearchTimeout);
		AppLogic.removeUiGroundCallback(mRunnableSearchResult);
		mRunnableSearchResult = null;
		if (mSearchReqLastPoiSearch != null) {
			mSearchReqLastPoiSearch.cancel();
			mSearchReqLastPoiSearch = null;
		}
		if (mWinProcessingPoiSearch != null) {
			mWinProcessingPoiSearch.dismiss("cancel all poi search");
			mWinProcessingPoiSearch = null;
		}
	}

	/**
	 * 检查当前本地导航是否支持途经点功能
	 * 
	 * @return
	 */
	public String disableSetJTPoi() {
		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		if (nta != null && nta.isReachable() && nta.isInNav()) {
			return ((NavThirdComplexApp) nta).disableProcJingYouPoi();
		}

		nta = getLocalNavImpl();
		if (nta != null) {
			if (nta instanceof NavThirdComplexApp && nta.isInNav()) {
				return ((NavThirdComplexApp) nta).disableProcJingYouPoi();
			}
		}
		return NativeData.getResString("RS_NAV_NOT_SUPPORT_THROUGH");
	}

	/**
	 * 执行途经点插入
	 * @param poi
	 * @return
	 */
	public boolean procJingYouPoi(Poi poi) {
		poi.setAction(PoiAction.ACTION_JINGYOU);

		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		if (nta == null) {
			nta = getLocalNavImpl();
		}
		if (nta != null) {
			if (nta instanceof NavThirdComplexApp && nta.isInNav()) {
				return ((NavThirdComplexApp)nta).procJingYouPoi(poi);
			}
		}
		return false;
	}
	
	/**
	 * 播报当前限速信息
	 */
	public boolean broadLimitSpeech() {
		NavThirdApp nta = getLocalNavImpl();
		if (nta != null && nta instanceof NavThirdComplexApp) {
			boolean bSucc = ((NavThirdComplexApp) nta).speakLimitSpeech();
			if (bSucc) {
				return true;
			}
		}
		return false;
	}

	public void naviFavorsPoi(final Poi poi, String kws) {
		String hint = NativeData.getResString("RS_NAV_PATH_PLAN").replace("%PATH%", kws);
		String ttsHint = NativeData.getResString("RS_MAP_PATH_FAIL");

		if (PoiAction.ACTION_JINGYOU.equals(poi.getAction())) {
			hint = NativeData.getResString("RS_NAV_PATH_THROUGH").replace("%POINT%", kws);
			ttsHint = NativeData.getResString("RS_NAV_THROUGH_POINT_FAIL");
			if (NavManager.getInstance().isNavi()) {
				hint = NativeData.getResString("RS_NAV_PATH_REPLANNING").replace("%POINT%", kws);
			}
		}
		NavManager.getInstance().setSpeechAfterPlanError(true, ttsHint);
		RecorderWin.addSystemMsg(hint);
		final int speechId = TtsManager.getInstance().speakText(hint, new ITtsCallback() {

			@Override
			public void onSuccess() {
				naviByPoiAction(poi);
			}
		});
		RecorderWin.addCloseRunnable(new Runnable() {

			@Override
			public void run() {
				TtsManager.getInstance().cancelSpeak(speechId);
			}
		});
	}

	public void naviByPoiAction(final Poi mPoi) {
		String action = PoiAction.ACTION_NAVI;
		if (!TextUtils.isEmpty(mPoi.getAction())) {
			action = mPoi.getAction();
		}

		if (PoiAction.ACTION_NAVI.equals(action)) {
		} else if (PoiAction.ACTION_HOME.equals(action)) {
			NavManager.getInstance().setHomeLocation(mPoi.getName(), mPoi.getGeoinfo(), mPoi.getLat(), mPoi.getLng(),
					UiMap.GPS_TYPE_GCJ02);
		} else if (PoiAction.ACTION_COMPANY.equals(action)) {
			NavManager.getInstance().setCompanyLocation(mPoi.getName(), mPoi.getGeoinfo(), mPoi.getLat(), mPoi.getLng(),
					UiMap.GPS_TYPE_GCJ02);
		} else if (PoiAction.ACTION_JINGYOU.equals(action)) {
			boolean bSucc = NavManager.getInstance().procJingYouPoi(mPoi);
			if (bSucc) {
				RecorderWin.close();
				return;
			} else {
				String spk = NativeData.getResString("RS_MAP_THROUGH_POINT_FAIL_NAV");
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						NavigateTo(mPoi);
					}
				});
				return;
			}
		}

		RecorderWin.close();
		NavigateTo(mPoi);

		if (NetworkManager.getInstance().checkLeastFlow()) {
			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
			TtsManager.getInstance().speakText(resText);
		}
	}

	public boolean preInvokePoiSearchResult(final String city, String keywords,
			final String action, List<? extends Poi> pois, boolean isBussiness) {
		int count = pois != null ? pois.size() : 0;
		JSONBuilder jObj = new JSONBuilder();
		jObj.put("type", 2);
		jObj.put("keywords", keywords);
		jObj.put("city", city);
		jObj.put("action", action);
		if (isBussiness) {
			jObj.put("poitype", "business");
		}
		if (count > sSearchCount) {
			count = sSearchCount;
		}

		jObj.put("count", count);
		JSONArray jPois = new JSONArray();
		for (int i = 0; i < count; i++) {
			JSONBuilder poi = new JSONBuilder(pois.get(i).toString());
			poi.put("asr_addr_kws", KeywordsParser.splitAddressKeywords(pois
					.get(i).getGeoinfo()));
			poi.put("asr_name_kws",
					KeywordsParser.splitKeywords(pois.get(i).getName()));
			jPois.put(poi.getJSONObject());
			JNIHelper.logd("POISearchLog:the show POI :"+(i+1)+" type is "+pois.get(i).getSourceType());
			if (pois.get(i) instanceof TxzPoi) {
				TxzPoi tmp = (TxzPoi) pois.get(i);
				JNIHelper.logd("POISearchLog:"+(tmp.isTop() ? "is" : "isn't") + " top");
			}
		}
		jObj.put("pois", jPois);
		if (SenceManager.getInstance().noneedProcSence("poi_choice",
				jObj.toBytes())) {
			return true;
		}
		if (count <= 0) {
			mHaveResult = false;
			final String roughWords = getRoughWords(keywords);
			if (roughWords != null && roughWords.length() > 1) {
				String hint = NativeData
						.getResString("RS_MAP_NOT_FOUND_SEARCH")
						.replace("%SRC%", keywords).replace("%DES%", roughWords);
				RecorderWin.addSystemMsg(hint);
				mRoughSearchTtsId = TtsManager.getInstance().speakText(hint,
						PreemptType.PREEMPT_TYPE_NEXT, new ITtsCallback() {
							@Override
							public void onSuccess() {
								NavigateInfo info = new NavigateInfo();
								info.strTargetName = roughWords;
								info.strTargetCity = city;
								navigateByName(info, false, action, true);
								super.onEnd();
							}
						});
				RecorderWin.addCloseRunnable(new Runnable() {
					@Override
					public void run() {
						if (mRoughSearchTtsId != TtsManager.INVALID_TTS_TASK_ID) {
							TtsManager.getInstance().cancelSpeak(
									mRoughSearchTtsId);
							mRoughSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;
						}
					}
				});
				return true;
			}
		}else{
			mHaveResult = true;
		}

		// 完整的Poi数据
		List<Poi> poiList = new ArrayList<Poi>();
		for (int i = 0; i < count; i++) {
			poiList.add(pois.get(i));
		}
		setTargetCity(poiList);
		PoisData poisData = new PoisData();
		poisData.action = action;
		if(mSearchBySelect && !TextUtils.isEmpty(mTargetCity)){
			poisData.city = mTargetCity;
		}else{
			poisData.city = city;
		}
		if(poiList.size() > 0 && poiList.get(0).getSourceType() ==  Poi.POI_SOURCE_DZDP){
			poisData.isBus = true;
		}else{
			poisData.isBus = false;
		}
		poisData.keywords = keywords;
		poisData.mPois = poiList;
		JNIHelper.logd("zsbin: preInvokePoiSearchResult mCurrPoisData.city="+poisData.city);
		setTipCityStr(poisData);
		ChoiceManager.getInstance().showPoiList(poisData,null);
		return true;
	}
	private static int NEARBY_MAX_DISTANCE = 10000;
	private String mTipCityString = null;
	public String getTipCityString(){
		return mTipCityString;
	}
	
	public void setTipCityStr( PoisData mPoisData){
		boolean isNeatBy = false;
		if((mSearchType & SEARCH_TYPE_BUSSINESS_MASK  ) !=0 ||
		    (mSearchType & SEARCH_TYPE_NEARBY_MASK ) !=0 ){
			isNeatBy = true;
			LogUtil.logd("setTipCityStr isNearBy:" + isNeatBy);
		}
		List<String> cityList = null;
		if (isNeatBy) {
			for (Poi poi : mPoisData.mPois) {
				int calDistance = poi.getDistance();
				if (calDistance > NEARBY_MAX_DISTANCE) {
					isNeatBy = false;
					break;
				}
			}
		}
		mTipCityString = mPoisData.city;
		cityList = NavManager.getInstance().getTargetCity();
		if(isNeatBy){
			mTipCityString = "附近";
			return ;
		}
		if(cityList == null || cityList.size() == 0){
			return ;
		}
		if(cityList.size() >1){
			mTipCityString = "多个城市";
			return ;
		}else if(cityList.size() == 1){
			mTipCityString = cityList.get(0);
			return ;
		}		
	}
		
	private boolean mHaveResult = false;
	public boolean getIsHaveResultPre(){
		return mHaveResult;
	}
	
	public final static Pattern PATTERN_SPLIT_AREA = Pattern
			.compile("^((.{2,8}(自治)?(区|州))|(.{2,5}(省|市|区|州|县|盟|乡|镇|旗)))$");

	private boolean isOnlyRough(NavigateInfo navigateInfo, String address) {
		if (address == null || address.length() <= 1)
			return false;
		if (address.equals(navigateInfo.strCountry))
			return true;
		if (address.equals(navigateInfo.strProvince))
			return true;
		if (address.equals(navigateInfo.strArea))
			return true;
		if (address.equals(navigateInfo.strRegion))
			return true;
		Matcher m = PATTERN_SPLIT_AREA.matcher(address);
		if (m != null && m.find()) {
			return true;
		}
		return false;
	}

	public final static Pattern PATTERN_SPLIT_ADDRESS_END = Pattern
			.compile("^(.+" + KeywordsParser.EXPR_ADDRESS_END_KEYWORDS
					+ ").+?$");

	private String getRoughWords(String keywords) {
		if (keywords == null || keywords.length() <= 0) {
			return null;
		}
		// 尝试去掉英文和数字搜索
		Pattern re = Pattern.compile("([0-9a-zA-Z]+)(号|栋|座|层|室|区)?");
		Matcher r = re.matcher(keywords);
		if (r != null) {
			String kw = replaceAllExceptPhrase(r, " ");
			if (!keywords.equals(kw) && !TextUtils.isEmpty(kw)) {
				return kw;
			}
		}
		// 得到大致位置，如：南山区中科大厦，可以被大致定位为南山区
		return KeywordsParser.getNextNavAddress(keywords);
	}

	/**
	 * 替换掉除了某些常用语之外的所有匹配项，例如：4S店中的4S 
	 */
	private static String replaceAllExceptPhrase(Matcher matcher,String replacement){
        matcher.reset();
        boolean result = matcher.find();
        if (result) {
            StringBuffer sb = new StringBuffer();
            do {
				// 4S店不进行替换
				if (("4S".equals(matcher.group()) || ("4s".equals(matcher.group())))) {
					result = matcher.find();
					continue;
				}
				matcher.appendReplacement(sb, replacement);
				result = matcher.find();
			} while (result);
            matcher.appendTail(sb);
            return sb.toString();
        }
        return null;
	}

	private boolean procByNetNotWork(String hint, boolean manual) {
		if (mSearchByEdit || !manual) {
			mSearchByEdit = false;
			if (RecorderWin.isOpened()) {
				RecorderWin.addSystemMsg(hint);
				RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
				mSearchTtsId = TtsManager.getInstance().speakText(hint,
						new ITtsCallback() {

							@Override
							public void onEnd() {
								AsrManager.getInstance().start();
							}

							@Override
							public boolean isNeedStartAsr() {
								return true;
							}
						});
			} else {
				RecorderWin.open(hint);
			}
			return true;
		}
		return false;
	}

	public void NavigateToByTXZ(NavigateInfo info) {
		if (info == null || info.strTargetName == null
				|| info.strTargetAddress == null || info.msgGpsInfo == null
				|| info.msgGpsInfo.uint32GpsType == null
				|| info.msgGpsInfo.dblLat == null
				|| info.msgGpsInfo.dblLng == null) {
			JNIHelper.loge("参数为空!");
			return;
		}
		JNIHelper.logd("use txz to navigate");
		NavThirdApp navTxzImpl = NavAppManager.getInstance().getNavToolByName(ServiceManager.NAV);
		if (navTxzImpl != null) {
			NavAppManager.getInstance().invokeRemoteStatus("status.nav.enter", navTxzImpl.getPackageName().getBytes());
			navTxzImpl.NavigateTo(NavPlanType.NAV_PLAN_TYPE_RECOMMEND, info);
		}
	}

	private boolean deleteHistoryNavigateInfo(NavigateInfo info) {
		setHistroyLocation(info, true);
		return true;
	}

	/**
	 * 判断是不是同一个城市
	 * @param city1
	 * @param city2
	 * @return
	 */
	public static boolean isSameCity(String city1, String city2) {
		if (city1.length() > city2.length()) {
			city1 = city1.replace("市", "");
		} else if (city1.length() > city2.length()) {
			city2 = city2.replace("市", "");
		}
		return city1.equals(city2);
	}

	public boolean isNearbyName(String name) {
		int i = 0;
		for (;;) {
			String s = NativeData.getResString("NEARBY_SEARCH_CONVER_LIST", i);
			if (s == null || s.length() == 0)
				return false;
			if (s.equals(name))
				return true;
			++i;
		}
	}

	public boolean isBussinessName(String name) {
		int i = 0;
		for (;;) {
			String s = NativeData.getResString("BUSSINESS_SEARCH_CONVER_LIST",
					i);
			if (s == null || s.length() == 0)
				return false;
			if (s.equals(name))
				return true;
			++i;
		}
	}

	// /////////////////////////////////////////////////////////////////////////

	@Override
	public int initialize_BeforeStartJni() {
		regEvent(UiEvent.EVENT_SYSTEM_MAP);
		regEvent(UiEvent.EVENT_ACTION_IM, UiIm.SUBEVENT_ACTION_ROOM_IN_RESP);
		regEvent(UiEvent.EVENT_ACTION_IM, UiIm.SUBEVENT_ACTION_ROOM_MEMBER_LIST_RESP);
		regEvent(UiEvent.EVENT_ACTION_IM, UiIm.SUBEVENT_ACTION_ROOM_OUT_RESP);
		regEvent(UiEvent.EVENT_ACTION_IM, UiIm.SUBEVENT_ACTION_ROOM_UPDATE_NOTIFY);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_POI_SEARCH);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_TXZ_POI_SEARCH);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_COMMON_CITY_INFO);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_POI_PARSE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_REPORT_NAV_INFO);
		regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_DESTINATION_RECOMMEND);

		regCustomCmd();
		return super.initialize_BeforeStartJni();
	}
	
	private void regSetDefaultNavCmd() {
		AppLogic.removeBackGroundCallback(mLoginRegCmdRunnable);
		AppLogic.runOnBackGround(mLoginRegCmdRunnable, 3 * 1000);
	}
	
	Runnable mLoginRegCmdRunnable = new Runnable() {

		@Override
		public void run() {
			regAfterLoginConfigCmds();
		}
	};
	
	private void regAfterLoginConfigCmds() {
		if (mSupportDefaultNav) {
			regCommand("NAV_CMD_SWITCH_DEFAULT_TOOL");
		}
	}

	private void regCustomCmd() {
		regCommandWithResult("NAV_CMD_ENTER");
		regCommandWithResult("NAV_FRONT_TRAFFIC");
		regCommandWithResult("NAV_WAY_POI_CMD_GAS");
		regCommandWithResult("NAV_WAY_POI_CMD_BANK");
		regCommandWithResult("NAV_WAY_POI_CMD_TOILET");
		regCommandWithResult("NAV_WAY_POI_CMD_SPOTS");
		regCommandWithResult("NAV_WAY_POI_CMD_RESTAURANT");
		regCommandWithResult("NAV_WAY_POI_CMD_HOTEL");
		regCommandWithResult("NAV_WAY_POI_CMD_SERVICE");
		regCommandWithResult("NAV_WAY_POI_CMD_PARK");
		regCommand("NAV_REVERSAL_CMD");
		regCommand("NAV_TRAFFIC_CMD_REPORT");
		regCommand("NAV_TRAFFIC_EVENT_REPORT_ACCIDENT");
		regCommand("NAV_TRAFFIC_EVENT_REPORT_CONGESTION");
		regCommand("NAV_TRAFFIC_EVENT_REPORT_CONSTRUCTION");
		regCommand("NAV_TRAFFIC_EVENT_REPORT_SEEPER");
		regCommand("NAV_TRAFFIC_EVENT_REPORT_ROADCLOSE");
	}
	public int mExactEvent= -1;
	@Override
	public int onCommand(String cmd) {
		if (!TextUtils.isEmpty(getDisableResaon())) { // 没有导航工具
			if (preInvokeWhenNavNotExists(null)) {
				return 0;
			}
		}

		if ("NAV_REVERSAL_CMD".equals(cmd)) {
			queryLocalNavReversal();
			return 0;
		}
		if ("NAV_CMD_SWITCH_TOOL".equals(cmd)) {
			if (NavAppManager.getInstance().isAlreadyExitNav()) {
				RecorderWin.speakText(NativeData.getResString("RS_VOICE_ANSWER_OPEN_NAV"), null);
				return 0;
			}
			NavAppManager.getInstance().handleSelectNavApp();
			return 0;
		}
		if ("NAV_CMD_SWITCH_DEFAULT_TOOL".equals(cmd)) {
			handleSetDefaultNavTool();
			return 0;
		}
		if("NAV_TRAFFIC_CMD_REPORT".equals(cmd)){
			reportTraffic(0);
		}
		if(cmd.startsWith("NAV_TRAFFIC_EVENT_REPORT")){
			if(cmd.equals("NAV_TRAFFIC_EVENT_REPORT_ACCIDENT")){
				reportTraffic(1);
			}
			if(cmd.equals("NAV_TRAFFIC_EVENT_REPORT_CONGESTION")){
				reportTraffic(2);
			}
			if(cmd.equals("NAV_TRAFFIC_EVENT_REPORT_CONSTRUCTION")){
				reportTraffic(3);
			}
			if(cmd.equals("NAV_TRAFFIC_EVENT_REPORT_SEEPER")){
				reportTraffic(5);
			}
			if(cmd.equals("NAV_TRAFFIC_EVENT_REPORT_ROADCLOSE")){
				reportTraffic(4);
			}			
		}
		return 0;
	}

	/**
	 * EVCard 附近的网点
	 */
	private void navEVCardNearbyPoint(int num) {
		PoiSearchTool poiSearchTool = new ChaosPoiSearchTXZImpl(false)
				.addPoiSearchTool(new PoiSearchToolTxzPoiImpl(false), true);
		NearbyPoiSearchOption nearbyPoiSearchOption = new NearbyPoiSearchOption();
		LocationInfo loc = LocationManager.getInstance().getLastLocation();
		nearbyPoiSearchOption.setKeywords("网点").setNum(num);
		if(loc != null){
			if(loc.msgGeoInfo != null && !TextUtils.isEmpty(loc.msgGeoInfo.strCity)){
				nearbyPoiSearchOption.setCity(loc.msgGeoInfo.strCity);
			}
			if(loc.msgGpsInfo != null && loc.msgGpsInfo.dblLat != null && loc.msgGpsInfo.dblLng != null){
				nearbyPoiSearchOption.setCenterLat(loc.msgGpsInfo.dblLat.doubleValue())
						.setCenterLng(loc.msgGpsInfo.dblLng.doubleValue());
			}
		}else{
			String spk = NativeData
					.getResString("RS_MAP_LOC_ERROR");
			mTXZSearchResultListener.showSearchError(spk);
			return;
		}
		mTXZSearchResultListener.setOption(nearbyPoiSearchOption);
		poiSearchTool.searchNearby(nearbyPoiSearchOption, mTXZPoiSearchResultListener);
		return;
	}

	public int onCommand(String cmd, String keywords, String voiceString) {
		if (!TextUtils.isEmpty(getDisableResaon())) { // 没有导航工具
			if (preInvokeWhenNavNotExists(null)) {
				return 0;
			}
		}

		if ("NAV_CMD_ENTER".equals(cmd)) {
			// 特殊指令分开
			if ("2D模式".equals(keywords)
					||"2d模式".equals(keywords)
					||"3D模式".equals(keywords)
					||"3d模式".equals(keywords)){
				NavThirdApp nta = getLocalNavImpl();
				if (nta instanceof NavAmapAutoNavImpl) {
					String pkn = nta.getPackageName();
					if (NavAmapAutoNavImpl.PACKAGE_NAME_LITE.equals(pkn)) {
						// 车镜版，去掉2/3D模式
						RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE_2"), null);
						return 0;
					}
				}
			}
			
			String tts = "RS_VOICE_ANSWER_OPEN_NAV";
			if (!NavAppManager.getInstance().isAlreadyExitNav()) {
				tts = "RS_VOICE_ANSWER_BACK_NAV";
				if (isNavFocus()) {
					tts = "RS_VOICE_ANSWER_TO_NAV";
					RecorderWin.open(NativeData.getResString(tts), VoiceData.GRAMMAR_SENCE_NAVIGATE);
					return 0;
				}
			}

			RecorderWin.speakText(NativeData.getResString(tts), null);
			return 0;
		}
		if (cmd.startsWith("NAV_WAY_POI_CMD")) {
			NavThirdApp nta = getLocalNavImpl();
			if (nta != null && nta instanceof NavBaiduDeepImpl && nta.isInNav()) {
				nta.enterNav();
				((NavBaiduDeepImpl) nta).onWayPoiCommand(cmd, keywords);
				return 0;
			}
			String answer = NativeData
					.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
			AsrManager.getInstance().setNeedCloseRecord(false);
			RecorderWin
					.speakTextWithClose(
							answer,	null);
			return 0;
		}
		return onCommand(cmd);
	}
	
	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		// 发送初始化需要触发的事件
		try {
			SDKInitializer.initialize(GlobalContext.get());
		} catch (Exception e) {
			LogUtil.loge("Baidu SDKInitializer error:" + e.getMessage());
		}

		NavAppManager.getInstance().init(true);
		CollectLocsPlugin.init();
		NavInscriber.getInstance().init(GlobalContext.get());
		getPoiShowIsList();
		MapPoiConTrol mapPoiControl = MapPoiView.getInstance().getMapPoiControl();
		DefaultMapPoiListView.getInstance().setMapPoiContril(mapPoiControl);
		
		RecorderWin.OBSERVABLE.registerObserver( new StatusObserver() {
			@Override
			public void onShow() {
				Map<Integer, ArrayList<String>> planningWakeUp = NavAmapValueService.getInstance().getPlanningWakeUp();
				if(planningWakeUp.size() >0){
					 NavAmapValueService.getInstance().destroyPlanningSelect();
					 Set<Integer> keySet = planningWakeUp.keySet();
					 ArrayList<String> keyWordList = new ArrayList<String>();
					 for(Integer key : keySet){
						 keyWordList.addAll(planningWakeUp.get(key));
					 }
					 AsrUtil.regCmd( (String[]) keyWordList.toArray(new String[0]), "CMD_PLANNING_WAKEUP",
								new IAsrRegCmdCallBack() {
									@Override
									public void notify(String text, byte[] data) {
										Map<Integer, ArrayList<String>> planningWakeUp = NavAmapValueService.getInstance().getPlanningWakeUp();
										Set<Integer> keySet = planningWakeUp.keySet();
										for(Integer key : keySet){
											int index = planningWakeUp.get(key).indexOf(text);
											if(index != -1){
												NavAmapValueService.getInstance().choicePlanRoad(key);
											}
										}
									}
								});		
				}
			}
			@Override
			public void onDismiss() {
				Map<Integer, ArrayList<String>> planningWakeUp = NavAmapValueService.getInstance().getPlanningWakeUp();
				if(planningWakeUp.size() >0){
					 NavAmapValueService.getInstance().buildPlanningSelect(true);
					 Set<Integer> keySet = planningWakeUp.keySet();
					 ArrayList<String> keyWordList = new ArrayList<String>();
					 for(Integer key : keySet){
						 keyWordList.addAll(planningWakeUp.get(key));
					 }
					 AsrUtil.unregCmd((String[]) keyWordList.toArray(new String[0]));
				}
			}			
		});
		return super.initialize_AfterInitSuccess();
	}
	
	public void loginSuccess() {
		FakeReqManager.getInstance().triggleUploadGps();
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_COMMON_CITY_INFO);
	}
	
	/**
	 * 执行导航历史
	 */
	public void queryLocalHistory() {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				int totalRecord = NavInscriber.getInstance().getCache().toggleDestination(true).getTotalSize(); // 获取导航记录的总数
				if (totalRecord > 0) {
					ChoiceManager.getInstance().showNavHistory(totalRecord);
				} else {
					String tts = NativeData.getResString("RS_NAV_HISTORY_NO_FOUND");
					RecorderWin.open(tts, VoiceData.GRAMMAR_SENCE_NAVIGATE);
				}
			}
		});
	}
	
	/**
	 * 执行返航目的地
	 */
	private void queryLocalNavReversal() {
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				int totalRecord = NavInscriber.getInstance().getCache().toggleDestination(false).getTotalSize(); // 获取导航记录的总数
				if (totalRecord > 0) {
					ChoiceManager.getInstance().showNavReversal(totalRecord);
				} else {
					String tts = NativeData.getResString("RS_NAV_REVERSAL_NO_FOUND");
					RecorderWin.open(tts, VoiceData.GRAMMAR_SENCE_NAVIGATE);
				}
			}
		});
	}
	
	private void handleSetDefaultNavTool() {
		AppLogic.runOnBackGround(new Runnable() {
			
			@Override
			public void run() {
				String ttsSpk = NativeData.getResString("RS_NAVTOOL_LIST_SPK");
				NavAppManager.getInstance().showNavTool(false, ttsSpk,
						new OnItemSelectListener<NavAppManager.NavAppBean>() {

					@Override
					public boolean onItemSelected(boolean isPreSelect, NavAppBean v, boolean fromPage, int idx,
							String fromVoice) {
						if (v != null) {
									NavAppManager.getInstance().setDefaultNavType(true, v.strPackageName);
							String tts = NativeData.getResString("RS_VOICE_NAV_SET_DEFAULT").replace("%NAVTOOL%",
									v.strAppName);
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(tts, null);
						}
						return true;
					}
				});
			}
		});
	}

	boolean sUseQihooSearchTool = false;
	String sPoiSearchToolType = null;

	{
		// 检测是否安装了好搜地图，如果安装了则切换到奇虎360搜索
		checkPoiResApp();
	}
	public void reportTraffic(int event){
		mExactEvent = event;
		boolean  result = getLocalNavImpl().reportTraffic(event);
		if( !result ){
			String answer = NativeData
					.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
			AsrManager.getInstance().setNeedCloseRecord(false);
			RecorderWin.speakTextWithClose(
							answer,	null);
		} else {
			NavAmapValueService.getInstance().setEnableAutoPupUp(false);
			RecorderWin.dismiss();
		}
	}
	public void checkPoiResApp() {
		boolean bSetQihooSearchTool = false;
		// 用户指定类型优先度最高
		if (sPoiSearchToolType != null) {
			if ("TXZ".equals(sPoiSearchToolType)) {
				bSetQihooSearchTool = false;
			}
			if ("QIHOO".equals(sPoiSearchToolType)) {
				bSetQihooSearchTool = true;
			}
		} else {
			if (PackageManager.getInstance().checkAppExist(NavQihooImpl.PACKAGE_NAME)) {
				bSetQihooSearchTool = true;
			} else {
				bSetQihooSearchTool = false;
			}
		}
		if (bSetQihooSearchTool && bSetQihooSearchTool != sUseQihooSearchTool) {
			QHAppFactory.init(GlobalContext.get());
		}
		sUseQihooSearchTool = bSetQihooSearchTool;
	}

	public void reportNetStatus(String poiName,String status){
//
//		boolean hasNet = NetworkManager.getInstance().hasNet();
//		Integer netType=null;
//		Integer rssi=null;
//		if(hasNet){
//			netType= NetworkManager.getInstance().getNetType();
//			if(UiData.NETWORK_STATUS_WIFI==netType){
//				WifiInfo currentWifiConnectionInfo = DeviceInfo.getCurrentWifiConnectionInfo();
//				rssi=currentWifiConnectionInfo.getRssi();
//			}else{
//				TraceCellInfoList cellInfo = DeviceInfo.getCellInfo();
//				if(cellInfo!=null&&cellInfo.rptMsgCellInfo!=null){
//					for(TraceCellInfo info :cellInfo.rptMsgCellInfo){
//						if(rssi==null||rssi<info.int32Rssi){
//							rssi=info.int32Rssi;
//						}
//					}
//				}
//			}
//		}else{
//			netType=UiData.NETWORK_STATUS_NONE;
//		}
//		Builder builder = new ReportUtil.Report.Builder();
//		builder.setType("navi");
//		builder.setAction("net");
//		builder.putExtra("type", netType);
//		builder.putExtra("name", poiName);
//		builder.putExtra("status", status);
//		if(rssi!=null)
//			builder.putExtra("rssi", rssi);
//		ReportUtil.doReport(builder.buildCommReport());
	}
	
	public static void reportBack(String name,long time){
		Builder builder = new ReportUtil.Report.Builder();
		builder.setType("navi");
		builder.setAction("back");
		builder.putExtra("name", name);
		builder.putExtra("time", time);
		ReportUtil.doReport(builder.buildCommReport());
	}
	
	private boolean enableOfflineSearch() {
		return !(NetworkManager.getInstance().hasNet() || NetworkUtil.isConnectedOrConnecting(GlobalContext.get()) );
	}
	//添加沿途搜索
	private PoiSearchTool dealToolBeforeSearch(PoiSearchTool tool,PoiSearchInfo info,String str,boolean isCenter,boolean isBussiness,boolean isOffline,String keyword){
		if(!isCenter&&isOnWaySearch(str)&&!mIsEnd){
			PoiSearchTool onway = null;
			if(getLocalNavImpl().getOnWaySearchToolCode(keyword) == ConfigFileHelper.POI_SEARCH_CODE_GAODE){
				onway = new PoiSearchToolGaodeOnWay();
			}
			tool=new ChaosPoiSearchTXZImpl(isBussiness)
				.addPoiSearchTool(onway, false)
				.addPoiSearchTool(tool, false);
		}
		//如果是纯离线搜索和指定奇虎搜索，则不使用后台搜索
		if (isEnablePoiToolSearch(info,UiEquipment.POI_SOURCE_TXZ_POI) && !isOffline
				&& !sUseQihooSearchTool) {
			tool = new ChaosPoiSearchTXZImpl(isBussiness)
						.addPoiSearchTool(new PoiSearchToolTxzPoiImpl(isBussiness), false)
						.addPoiSearchTool(tool, false);
		}	
		return tool;
	}
	
	// 周边商圈搜索
	public void navigateBussinessNearby(NearbyPoiSearchOption opt,
			boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_BUSSINESS_NEARBY);
		mTXZSearchResultListener.setAction(action);
		PoiSearchTool poiSearchTool;
		PoiSearchInfo info = opt.getSearchInfo();
		info.setPoiSourceConf(mPoisearchConfig);
		info.setPoiRetryCount(mPoisearRetryCount);
		opt.setTimeout(mPoisearTimeCost);
		boolean offline = enableOfflineSearch();
		if (!offline&&sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
		} else {
			poiSearchTool =creatPoiSearchTool(SEARCH_TYPE_BUSSINESSNEARBY, null, null,offline,opt.getKeywords());

		}


		poiSearchTool=dealToolBeforeSearch(poiSearchTool,info,opt.getKeywords(),false,true,offline,opt.getKeywords());
		JNIHelper.logd("POISearchLog:txz poi search begin navigateBussinessNearby ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchNearby(opt,
				mTXZPoiSearchResultListener);
	}
	//附近商圈中心搜索
	public void navigateBussinessNearbyCenter(CityPoiSearchOption centerOpt,
			NearbyPoiSearchOption nextOpt, boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_CENTER);
		mTXZSearchResultListener.setAction(action);
		PoiSearchInfo info = centerOpt.getSearchInfo();
		PoiSearchInfo nextInfo = nextOpt.getSearchInfo();
		info.setPoiSourceConf(mPoisearchConfig);
		info.setPoiRetryCount(mPoisearRetryCount);
		nextInfo.setPoiSourceConf(mPoisearchConfig);
		nextInfo.setPoiRetryCount(mPoisearRetryCount);
		centerOpt.setTimeout(mPoisearTimeCost);
		nextOpt.setTimeout(mPoisearTimeCost);
		PoiSearchTool poiSearchToolCenter;
		PoiSearchTool poiSearchToolNext;
		boolean offline = enableOfflineSearch();
		if (!offline&&sUseQihooSearchTool) {
			poiSearchToolCenter = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
			poiSearchToolNext = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
		} else {
			// 中心采用高德搜索 
			poiSearchToolCenter = creatCenterPoiSearchTool(offline);
			poiSearchToolNext =creatPoiSearchTool(SEARCH_TYPE_BUSSINESSNEARBYCENTER, null, null,offline,nextOpt.getKeywords());
		}

		poiSearchToolCenter=dealToolBeforeSearch(poiSearchToolCenter,info,centerOpt.getKeywords(),true,true,offline,nextOpt.getKeywords());	
		poiSearchToolNext=dealToolBeforeSearch(poiSearchToolNext,info,nextOpt.getKeywords(),true,true,offline,nextOpt.getKeywords());
		JNIHelper.logd("POISearchLog:txz poi search begin navigateBussinessNearbyCenter ["
				+ centerOpt.getKeywords() + "] in city [" + centerOpt.getCity()
				+ "] with tool " + poiSearchToolCenter.toString());
		mSearchReqLastPoiSearch = poiSearchToolCenter.searchInCity(centerOpt,
				new CenterPoiSearchResultListener(nextOpt, poiSearchToolNext,
						mTXZPoiSearchResultListener, new NextStepListener() {
							public void onBegin(SearchReq req) {
								mSearchReqLastPoiSearch = req;
							};
						}));
	}

	// 城市商圈搜索
	public void navigateBussinessCity(CityPoiSearchOption opt, boolean manual,
			String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_BUSSINESS_CITY);
		mTXZSearchResultListener.setAction(action);
		PoiSearchInfo info = opt.getSearchInfo();
		info.setPoiSourceConf(mPoisearchConfig);
		info.setPoiRetryCount(mPoisearRetryCount);
		opt.setTimeout(mPoisearTimeCost);
		PoiSearchTool poiSearchTool;
		boolean offline = enableOfflineSearch();
		if (!offline && sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
		} else {
			poiSearchTool =creatPoiSearchTool(SEARCH_TYPE_BUSSINESSCITY, null, null,offline,opt.getKeywords());
		}


		poiSearchTool=dealToolBeforeSearch(poiSearchTool,info,opt.getKeywords(),false,true,offline,opt.getKeywords());	
		JNIHelper.logd("POISearchLog:txz poi search begin navigateBussinessCity ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchInCity(opt,
				mTXZPoiSearchResultListener);
	}

	// 周边搜索
	public void navigateNearby(NearbyPoiSearchOption opt, boolean manual,
			String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_NEARBY);
		mTXZSearchResultListener.setAction(action);
		PoiSearchInfo info = opt.getSearchInfo();
		info.setPoiSourceConf(mPoisearchConfig);
		info.setPoiRetryCount(mPoisearRetryCount);
		opt.setTimeout(mPoisearTimeCost);
		PoiSearchTool poiSearchTool;
		boolean offline = enableOfflineSearch();
		if (!offline && sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
		} else {
			poiSearchTool =creatPoiSearchTool(SEARCH_TYPE_NEARBY, null, null,offline,opt.getKeywords());
		}

		poiSearchTool=dealToolBeforeSearch(poiSearchTool,info,opt.getKeywords(),false,false,offline,opt.getKeywords());	
		JNIHelper.logd("POISearchLog:txz poi search begin navigateNearby ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchNearby(opt,
				mTXZPoiSearchResultListener);
	}

	public void navigateNearbyCenter(CityPoiSearchOption centerOpt,
			NearbyPoiSearchOption nextOpt, boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_CENTER);
		mTXZSearchResultListener.setAction(action);
		PoiSearchInfo info = centerOpt.getSearchInfo();
		PoiSearchInfo nextInfo = nextOpt.getSearchInfo();
		info.setPoiSourceConf(mPoisearchConfig);
		info.setPoiRetryCount(mPoisearRetryCount);
		nextInfo.setPoiSourceConf(mPoisearchConfig);
		nextInfo.setPoiRetryCount(mPoisearRetryCount);
		centerOpt.setTimeout(mPoisearTimeCost);
		nextOpt.setTimeout(mPoisearTimeCost);
		PoiSearchTool poiSearchToolCenter;
		PoiSearchTool poiSearchToolNext;
		boolean offline = enableOfflineSearch();
		if (!offline && sUseQihooSearchTool) {
			poiSearchToolCenter = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
			poiSearchToolNext = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new PoiSearchToolQihooImpl(), false);
		} else {
			// 中心使用高德城市搜索	
			poiSearchToolCenter = creatCenterPoiSearchTool(offline);
			poiSearchToolNext =creatPoiSearchTool(SEARCH_TYPE_NEARBYCENTER, null, null,offline,nextOpt.getKeywords());
		}

		poiSearchToolCenter=dealToolBeforeSearch(poiSearchToolCenter,info,centerOpt.getKeywords(),true,false,offline,nextOpt.getKeywords());
		poiSearchToolNext=dealToolBeforeSearch(poiSearchToolNext,info,nextOpt.getKeywords(),true,true,offline,nextOpt.getKeywords());
		JNIHelper.logd("POISearchLog:txz poi search begin navigateNearbyCenter ["
				+ centerOpt.getKeywords() + "] in city [" + centerOpt.getCity()
				+ "] with tool " + poiSearchToolCenter.toString());
		mSearchReqLastPoiSearch = poiSearchToolCenter.searchInCity(centerOpt,
				new CenterPoiSearchResultListener(nextOpt, poiSearchToolNext,
						mTXZPoiSearchResultListener, new NextStepListener() {
							public void onBegin(SearchReq req) {
								mSearchReqLastPoiSearch = req;
							};
						}));
	}

	// 城市搜索
	public void navigateCity(NavigateInfo navigateInfo,
			CityPoiSearchOption opt, boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_CITY);
		mTXZSearchResultListener.setAction(action);
		PoiSearchInfo info = opt.getSearchInfo();
		info.setPoiSourceConf(mPoisearchConfig);
		info.setPoiRetryCount(mPoisearRetryCount);
		opt.setTimeout(mPoisearTimeCost);
		if (!TextUtils.isEmpty(opt.getCity())) {
			LocationInfo loc = LocationManager.getInstance().getLastLocation();
			if (loc != null && loc.msgGeoInfo != null) {
				String strCity = loc.msgGeoInfo.strCity;
				if (strCity == null) {
					strCity = "";
				}
				if (!opt.getCity().startsWith(strCity)
						&& !TextUtils.isEmpty(strCity)
						&& !strCity.startsWith(opt.getCity())) {
					MonitorUtil
							.monitorCumulant(MonitorUtil.POISEARCH_ENTER_CROSS_CITY);
				}
			}
		} else {
			opt.setCity("");
		}

		PoiSearchTool poiSearchTool;
		boolean offline = enableOfflineSearch();
//		boolean offline = true;
		JNIHelper.logd("POISearchLog:offline= "+offline);	
		if (!offline && sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolConTXZImpl().
					addPoiSearchTool(new CityPoiSearchToolTXZimpl(
							new PoiSearchToolQihooImpl(),
							new PoiSearchToolQihooImpl(),
							new PoiSearchToolQihooImpl()), false);
		} else {
				poiSearchTool = creatPoiSearchTool(SEARCH_TYPE_CITY, navigateInfo, opt,offline,opt.getKeywords());
		}

		poiSearchTool=dealToolBeforeSearch(poiSearchTool,info,opt.getKeywords(),false,false,offline,opt.getKeywords());	
		JNIHelper.logd("POISearchLog:txz poi search begin navigateCity ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchInCity(opt,
				mTXZPoiSearchResultListener);
	}
	
	public static final int SEARCH_TYPE_NEARBY_MASK=0x1;
	public static final int SEARCH_TYPE_CITY_MASK=0x4;
	public static final int SEARCH_TYPE_BUSSINESS_MASK=0x2;
	public static final int SEARCH_TYPE_CENTER_MASK=0x8;
	
	public static final int SEARCH_TYPE_BUSSINESSNEARBY =
								SEARCH_TYPE_BUSSINESS_MASK|SEARCH_TYPE_NEARBY_MASK;

	public static final int SEARCH_TYPE_BUSSINESSNEARBYCENTER =
								SEARCH_TYPE_BUSSINESS_MASK|SEARCH_TYPE_CENTER_MASK;
	
	public static final int SEARCH_TYPE_BUSSINESSCITY=
								SEARCH_TYPE_CITY_MASK|SEARCH_TYPE_BUSSINESS_MASK;
	
	public static final int SEARCH_TYPE_NEARBY =SEARCH_TYPE_NEARBY_MASK;
	
	public static final int SEARCH_TYPE_NEARBYCENTER=SEARCH_TYPE_CENTER_MASK|SEARCH_TYPE_NEARBY_MASK;
	
	public static final int SEARCH_TYPE_CITY =SEARCH_TYPE_CITY_MASK;
	
	private PoiSearchTool creatCenterPoiSearchTool(boolean isOffline){
		PoiSearchToolConTXZImpl tool= new PoiSearchToolConTXZImpl();
		tool.addPoiSearchTool(
				new CityPoiSearchToolTXZimpl(
					new PoiSearchToolGaodeImpl(),
					new PoiSearchToolGaodeImpl(),
					new PoiSearchToolGaodeImpl()), isOffline);
		tool.addPoiSearchTool(
				new CityPoiSearchToolTXZimpl(
					new PoiSearchToolBaiduImpl(),
					new PoiSearchToolBaiduImpl(),
					new PoiSearchToolBaiduImpl()), isOffline);
		tool.addPoiSearchTool(
				new CityPoiSearchToolTXZimpl(
						new PoiSearchToolGDLocalImpl(),
						new PoiSearchToolGDLocalImpl(),
						new PoiSearchToolGDLocalImpl()), !isOffline);
		tool.addPoiSearchTool(
				new CityPoiSearchToolTXZimpl(
						new PoiSearchToolBaiduLocalImpl(),
						new PoiSearchToolBaiduLocalImpl(),
						new PoiSearchToolBaiduLocalImpl()), !isOffline);
		tool.addPoiSearchTool(
				new CityPoiSearchToolTXZimpl(
						new PoiSearchToolMXImpl(),
						new PoiSearchToolMXImpl(),
						new PoiSearchToolMXImpl()),!isOffline);
		return tool;
	}
	private int mSearchType = 0;
	private PoiSearchTool creatPoiSearchTool(int searchType,NavigateInfo navigateInfo,
			CityPoiSearchOption opt,boolean isOffline,String keyWord){
		mSearchType = searchType;
		boolean isBussiness=false;
		boolean isCity=false;
		boolean isCenter=false;
		boolean isAutoNewVersion=false;
		NavThirdApp localNavImpl = getLocalNavImpl();
		if(localNavImpl instanceof NavAmapAutoNavImpl){
			isAutoNewVersion = (((NavAmapAutoNavImpl)localNavImpl).getMapCode() >= 205)&&
					(((NavAmapAutoNavImpl)localNavImpl).isDeepSearch(keyWord));
		}
		if((searchType&SEARCH_TYPE_CITY_MASK)!=0)
			isCity=true;
		if((searchType&SEARCH_TYPE_BUSSINESS_MASK)!=0)
			isBussiness=true;
		if((searchType&SEARCH_TYPE_CENTER_MASK)!=0)
			isCenter=true;
		PoiSearchTool resultTool;
		ChaosPoiSearchTXZImpl 	 chaoTool= null;
		PoiSearchToolConTXZImpl conTool= null;
		if(!isBussiness&&isCity){
			conTool=new PoiSearchToolConTXZImpl();
			resultTool=conTool;
		}else{
			chaoTool=new ChaosPoiSearchTXZImpl(isBussiness);
			resultTool=chaoTool;
		}
		
		if(isBussiness&&!isCity){//添加大众点评
			chaoTool.addPoiSearchTool(
					new PoiSearchToolConTXZImpl().
						addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
								new BussinessPoiSearchDzdpImpl(),
								new BussinessPoiSearchDzdpImpl()), isOffline)
					,isOffline);
		}
		
		if(!isCity){
			chaoTool.addPoiSearchTool(//添加高德百度附近搜索
					new PoiSearchToolConTXZImpl().
						addPoiSearchTool(isAutoNewVersion?new MaxRaduisNearPoiSearchToolTXZimpl(
							new PoiSearchToolGDLocalImpl(),
							new PoiSearchToolGDLocalImpl()):null,isOffline).
						addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
								new PoiSearchToolBaiduImpl(),
								new PoiSearchToolBaiduImpl()),isOffline).
						addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
								new PoiSearchToolGaodeImpl(),
								new PoiSearchToolGaodeImpl()),isOffline),
					isOffline);
			if(isCenter&&!isBussiness){//添加离线附近搜索
				chaoTool.addPoiSearchTool(
						new PoiSearchToolConTXZImpl().
							addPoiSearchTool(isAutoNewVersion?null:new MaxRaduisNearPoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolGDLocalImpl()),!isOffline).
							addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl(),
									new PoiSearchToolMXImpl()), !isOffline).
							addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
									new PoiSearchToolBaiduLocalImpl(),
									new PoiSearchToolBaiduLocalImpl()),!isOffline),		
					!isOffline);
			}else{
				chaoTool.addPoiSearchTool(
						new PoiSearchToolConTXZImpl().
							addPoiSearchTool(isAutoNewVersion?null:new MaxRaduisNearPoiSearchToolTXZimpl(
								new PoiSearchToolGDLocalImpl(),
								new PoiSearchToolGDLocalImpl()),!isOffline).
							addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
									new PoiSearchToolBaiduLocalImpl(),
									new PoiSearchToolBaiduLocalImpl()),!isOffline).
							addPoiSearchTool(new MaxRaduisNearPoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl(),
									new PoiSearchToolMXImpl()), !isOffline),
					!isOffline);
			}
		}
		//添加城市搜索
		if(isCity){
			if(isBussiness){
				chaoTool.addPoiSearchTool(
					new PoiSearchToolConTXZImpl().
						addPoiSearchTool(new BussinessPoiSearchDzdpImpl(), isOffline),isOffline);			
				chaoTool.addPoiSearchTool(
						new PoiSearchToolConTXZImpl().
							addPoiSearchTool(isAutoNewVersion?new CityPoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolGDLocalImpl()):null,isOffline).
							addPoiSearchTool(new PoiSearchToolBaiduImpl(),isOffline).
							addPoiSearchTool(new CityPoiSearchToolTXZimpl(
									new PoiSearchToolGaodeImpl(),
									new PoiSearchToolGaodeImpl(),
									new PoiSearchToolGaodeImpl()),isOffline),
					isOffline);
			}else{
				boolean isOnlyTough=false;
				if (navigateInfo != null
						&& isOnlyRough(navigateInfo, opt.getKeywords())){
					isOnlyTough=true;
				}
				CityPoiSearchToolTXZimpl cityPoi1 = new CityPoiSearchToolTXZimpl(
																				new PoiSearchToolBaiduImpl(),
																				new PoiSearchToolBaiduImpl(),
																				new PoiSearchToolBaiduImpl());
				CityPoiSearchToolTXZimpl cityPoi2 = new CityPoiSearchToolTXZimpl(
																				new PoiSearchToolGaodeImpl(),
																				new PoiSearchToolGaodeImpl(),
																				new PoiSearchToolGaodeImpl());
				conTool.addPoiSearchTool(isAutoNewVersion?
																new CityPoiSearchToolTXZimpl(
																	new PoiSearchToolGDLocalImpl(),
																	new PoiSearchToolGDLocalImpl(),
																	new PoiSearchToolGDLocalImpl())
																:null,isOffline);
				if(isOnlyTough){//添加城市
					conTool.addPoiSearchTool(cityPoi1,isOffline);
					conTool.addPoiSearchTool(cityPoi2,isOffline);
				}else{
					conTool.addPoiSearchTool(cityPoi2,isOffline);
					conTool.addPoiSearchTool(cityPoi1,isOffline);
				}
			}		
		}else{
			if(isBussiness){
				chaoTool.addPoiSearchTool(//大众点评城市
						new NearToCityPoiSearchToolTXZimpl(
								new BussinessPoiSearchDzdpImpl()),
						isOffline);
			}
			chaoTool.addPoiSearchTool(//添加城市
					isAutoNewVersion?new NearToCityPoiSearchToolTXZimpl(
							new CityPoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolGDLocalImpl())):null,
					isOffline);
			chaoTool.addPoiSearchTool(//添加城市
					new NearToCityPoiSearchToolTXZimpl(
							new CityPoiSearchToolTXZimpl(
									new PoiSearchToolGaodeImpl(),
									new PoiSearchToolGaodeImpl(),
									new PoiSearchToolGaodeImpl())),
					isOffline);
			chaoTool.addPoiSearchTool(//添加城市
					new NearToCityPoiSearchToolTXZimpl(
							new CityPoiSearchToolTXZimpl(
									new PoiSearchToolBaiduImpl(),
									new PoiSearchToolBaiduImpl(),
									new PoiSearchToolBaiduImpl())),
					isOffline);

		}
		
		
		//添加离线
		if(isCity){
			if(isBussiness){
				chaoTool.addPoiSearchTool(
						new PoiSearchToolConTXZImpl().
							addPoiSearchTool(isAutoNewVersion?null:new PoiSearchToolGDLocalImpl(),!isOffline).
							addPoiSearchTool(new PoiSearchToolMXImpl(), !isOffline).
							addPoiSearchTool(new PoiSearchToolBaiduLocalImpl(), !isOffline),
						!isOffline);		
			}else{
				PoiSearchToolConTXZImpl offlineTool = new PoiSearchToolConTXZImpl();
				if(!isAutoNewVersion){
					offlineTool.addPoiSearchTool(new CityPoiSearchToolTXZimpl(
							new PoiSearchToolGDLocalImpl(),
							new PoiSearchToolGDLocalImpl(),
							new PoiSearchToolGDLocalImpl()), !isOffline);
				}
				offlineTool.addPoiSearchTool(new CityPoiSearchToolTXZimpl(
						new PoiSearchToolBaiduLocalImpl(),
						new PoiSearchToolBaiduLocalImpl(),
						new PoiSearchToolBaiduLocalImpl()), !isOffline);
				offlineTool.addPoiSearchTool(new CityPoiSearchToolTXZimpl(
						new PoiSearchToolMXImpl(),
						new PoiSearchToolMXImpl(),
						new PoiSearchToolMXImpl()), !isOffline);
				conTool.addPoiSearchTool(offlineTool , !isOffline);				
			}
		}else{
			chaoTool.addPoiSearchTool(
					new NearToCityPoiSearchToolTXZimpl(
							new PoiSearchToolConTXZImpl().
							addPoiSearchTool(isAutoNewVersion?null:new PoiSearchToolGDLocalImpl(),!isOffline).
							addPoiSearchTool(new PoiSearchToolBaiduLocalImpl(), !isOffline).
							addPoiSearchTool(new PoiSearchToolMXImpl(), !isOffline)),
					!isOffline);
		}
		mNomCityList.clear();
		JNIHelper.logd("POISearchLog: Poi search max time is "+(mPoisearRetryCount+1)*mPoisearTimeCost*3);
		AppLogic.runOnUiGround(mRunnableSearchTimeout, (mPoisearRetryCount+1)*mPoisearTimeCost*3);	
		return resultTool;
	}

	public interface IRespPoiSearch {
		public void onResult(UiEquipment.Resp_POI_Search poi);
	}

	public IRespPoiSearch mPoiSearchCallBack;

	public void registerPoiSeachCallback(IRespPoiSearch callback) {
		mPoiSearchCallBack = callback;
	}
	
	public interface IRespTxzPoiSearch {
		public void onResult(UiEquipment.Resp_TXZPoiSearch poi);
	}

	public IRespTxzPoiSearch mTxzPoiSearchCallBack;

	public void registerTxzPoiSeachCallback(IRespTxzPoiSearch callback) {
		mTxzPoiSearchCallBack = callback;
	}
	public void unRegisterTxzPoiSeachCallback() {
		mTxzPoiSearchCallBack = null;
	}
	
	/**
	 * 获取推荐目的地后台回应
	 * @param data
	 */
	private void onGetRecommandDest(byte[] data) {
		LogUtil.logd("onGetRecommandDest:" + data + ",mReq_Task:" + mReq_Task);
		if (mReq_Task == null) {
			return;
		}

		if (mReq_Task.mTargetRunTask != null) {
			AppLogic.removeBackGroundCallback(mReq_Task.mTargetRunTask);
		}
		if (data != null) {
			try {
				Resp_Destination resp = Resp_Destination.parseFrom(data);
				if (resp == null) {
					if (mReq_Task.mTargetRunTask != null) {
						mReq_Task.mTargetRunTask.run();
					}
					LogUtil.logd("onGetRecommandDest empty Resp_Destination");
					return;
				}

				Destination[] destinations = resp.rptDestination;
				if (destinations == null || destinations.length <= 0) {
					LogUtil.logd("onGetRecommandDest empty destination");
					if (mReq_Task.mTargetRunTask != null) {
						mReq_Task.mTargetRunTask.run();
					}
					return;
				}

				if (mReq_Task != null) {
					mReq_Task.onGetPoisData(destinations);
				}
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
				if (mReq_Task.mTargetRunTask != null) {
					mReq_Task.mTargetRunTask.run();
				}
				LogUtil.logd("onGetRecommandDest InvalidProtocolBufferNanoException");
			}
		} else {
			if (mReq_Task.mTargetRunTask != null) {
				mReq_Task.mTargetRunTask.run();
			}
		}
	}
	
	/**
	 * 获取推荐目的地
	 * 
	 * @param data
	 */
	public void handleRecommandDest(byte[] data) {
		mReq_Task = new Req_Task() {

			@Override
			public void onGetPoisData(Destination[] destinations) {
				PoisData poisData = new PoisData();
				poisData.action = PoiAction.ACTION_NAV_RECOMMAND;
				poisData.isBus = false;
				poisData.mPois = new ArrayList<Poi>();
				for (Destination d : destinations) {
					poisData.mPois.add(convDestinationToPoi(d,PoiAction.ACTION_NAV_RECOMMAND));
				}
				CompentOption<Poi> option = new CompentOption<Poi>();
				if (poisData.mPois.size() == 1) {
					option.setTtsText(
							NativeData.getResString("RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE_RECOMMAND_SINGLE_RESULT"));
				} else {
					option.setTtsText(NativeData.getResString("RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE_RECOMMAND"));
				}
				option.setCanSure(false);
				option.setProgressDelay(0);
				ChoiceManager.getInstance().showPoiList(poisData, option);
			}
		};
		mReq_Task.mTargetRunTask = new Runnable() {

			@Override
			public void run() {
				mReq_Task = null;
				RecorderWin.open(NativeData.getResString("RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE"),
						VoiceData.GRAMMAR_SENCE_NAVIGATE);
			}
		};
		requestRecommand(UiEquipment.DESTINATION_TYPE_DEFAULT);
		if (mReq_Task.mTargetRunTask != null) {
			AppLogic.removeBackGroundCallback(mReq_Task.mTargetRunTask);
			AppLogic.runOnBackGround(mReq_Task.mTargetRunTask, REQUEST_DESTINATION_TIME_OUT);
		}
	}
	
	private Poi convDestinationToPoi(Destination d, String action) {
		Poi poi = new Poi();
		poi.setLat(d.lat);
		poi.setLng(d.lng);
		poi.setName(d.poiName);
		poi.setGeoinfo(d.poiAddress);
		poi.setDistance(BDLocationUtil.calDistance(d.lat, d.lng));
		poi.setAction(action);
		return poi;
	}
	
	private Req_Task mReq_Task = null;

	private abstract class Req_Task {
		public Runnable mTargetRunTask = null;

		public abstract void onGetPoisData(Destination[] pData);
	}
	
	private void requestRecommand(int req_type) {
		LocationInfo info = LocationManager.getInstance().getLastLocation();
		Req_Destination req = new Req_Destination();
		if (info != null && info.msgGpsInfo != null) {
			req.currentLat = info.msgGpsInfo.dblLat;
			req.currentLng = info.msgGpsInfo.dblLng;
		}
		req.uint32ExpectType = req_type;
		LogUtil.logd("requestRecommand:" + req_type);

		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_DESTINATION_RECOMMEND, req);
	}
	public void navigateByName(final NavigateInfo navigateInfo, boolean manual,
			String action, boolean avoidAnnoy) {
		cancelAllPoiSearch();
		// TODO 走非手动逻辑
		// manual = false;
		mTXZSearchResultListener.setManual(manual);
		mTXZSearchResultListener.setAction(action);
		mTXZSearchResultListener.setExactCity(!TextUtils
				.isEmpty(navigateInfo.strTargetCity));

		if (null == navigateInfo || null == navigateInfo.strTargetName
				|| navigateInfo.strTargetName.length() == 0)
			return;

		String navTarName = null;
		if (navigateInfo.strArea != null && !navigateInfo.strArea.equals("")
				&& !navigateInfo.strArea.equals(navigateInfo.strTargetName)) {
			navTarName = navigateInfo.strArea + navigateInfo.strTargetName;
		} else {
			navTarName = navigateInfo.strTargetName;
		}

		boolean bBussiness = isBussinessName(navTarName);
		boolean bNear = (!bBussiness) && isNearbyName(navTarName);
		boolean bSameCity = true;

		LocationInfo myLocation = LocationManager.getInstance()
				.getLastLocation();
		if (!TextUtils.isEmpty(navigateInfo.strTargetCity)) {
			bSameCity = false;

			if (myLocation != null && myLocation.msgGeoInfo != null
					&& !TextUtils.isEmpty(myLocation.msgGeoInfo.strCity)) {
				if (navigateInfo.strTargetCity
						.startsWith(myLocation.msgGeoInfo.strCity)
						|| myLocation.msgGeoInfo.strCity
								.startsWith(navigateInfo.strTargetCity)) {
					bSameCity = true;
				}
			}
		} else {
			if (myLocation != null && myLocation.msgGeoInfo != null) {
				if(TextUtils.isEmpty(myLocation.msgGeoInfo.strCity)){
					myLocation.msgGeoInfo.strCity = "";
				}
				navigateInfo.strTargetCity = myLocation.msgGeoInfo.strCity;
			} else {
				navigateInfo.strTargetCity = "";
			}
		}

		if (!bSameCity)
			bNear = false;

		// 商圈搜索
		if (bBussiness) {
			// 城市商圈搜索
			if (!bSameCity) {
				CityPoiSearchOption opt = new CityPoiSearchOption()
						.setCity(navigateInfo.strTargetCity)
						.setKeywords(navTarName)
						.setNum(sSearchCount);
				opt.setRegion(navigateInfo.strRegion);
				mTXZPoiSearchResultListener.setOption(opt);
				playNameSearchInfo(manual,navigateInfo.strTargetAddress,avoidAnnoy,navTarName/*,navigateInfo.strRegion*/);
				navigateBussinessCity(opt, manual, action);
				return;
			}
			LocationInfo loc = LocationManager.getInstance().getLastLocation();
			if (loc == null || loc.msgGpsInfo == null
					|| loc.msgGpsInfo.dblLat == null
					|| loc.msgGpsInfo.dblLng == null) {
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCH_FAIL", "%TARGET%", navTarName);
				mTXZSearchResultListener.showSearchError(spk);
				return;
			}

			// 周边商圈搜索
			NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
					.setCenterLat(loc.msgGpsInfo.dblLat)
					.setCenterLng(loc.msgGpsInfo.dblLng)
					.setCity(navigateInfo.strTargetCity)
					.setKeywords(navTarName)
					.setNum(sSearchCount);
			opt.setRegion(navigateInfo.strRegion);
			mTXZPoiSearchResultListener.setOption(opt);
			playNameSearchInfo(manual,navigateInfo.strTargetAddress,avoidAnnoy,navTarName/*,navigateInfo.strRegion*/);
			navigateBussinessNearby(opt, manual, action);
		} else if (bNear) {
			// 周边搜索
			LocationInfo loc = LocationManager.getInstance().getLastLocation();
			if (loc == null || loc.msgGpsInfo == null
					|| loc.msgGpsInfo.dblLat == null
					|| loc.msgGpsInfo.dblLng == null) {
				String spk = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCH_FAIL", "%TARGET%", navTarName);
				mTXZSearchResultListener.showSearchError(spk);
				return;
			}
			NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
					.setCenterLat(loc.msgGpsInfo.dblLat)
					.setCenterLng(loc.msgGpsInfo.dblLng)
					.setCity(navigateInfo.strTargetCity)
					.setKeywords(navTarName)
					.setNum(sSearchCount);
			opt.setRegion(navigateInfo.strRegion);
			mTXZPoiSearchResultListener.setOption(opt);
			playNameSearchInfo(manual,navigateInfo.strTargetAddress,avoidAnnoy,navTarName/*,navigateInfo.strRegion*/);
			navigateNearby(opt, manual, action);
		} else {
			// 城市搜索
			CityPoiSearchOption opt = new CityPoiSearchOption()
					.setCity(navigateInfo.strTargetCity)
					.setKeywords(navTarName)
					.setNum(sSearchCount);
			opt.setRegion(navigateInfo.strRegion);
			mTXZPoiSearchResultListener.setOption(opt);
			playNameSearchInfo(manual,navigateInfo.strTargetAddress,avoidAnnoy,navTarName/*,navigateInfo.strRegion*/);
			navigateCity(navigateInfo, opt, manual, action);
		}

	}

	private void playNameSearchInfo(boolean manual, final String name, boolean avoidAnnoy, String navTarName/*,final String strRegion*/) {
		if( !mPlayNavInfo ){
			mPlayNavInfo = true;
			return;
		}	
		if (manual && !mSearchByEdit) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
/*					String spk = null;
					if(!TextUtils.isEmpty(strRegion)){
						spk = NativeData.getResString("RS_MAP_SEARCH_NEAR")
								.replace("%POI%", strRegion)
								.replace("%KEY%", name);
					}else{
						spk = NativeData.getResPlaceholderString(
								"RS_MAP_SEARCHING", "%TARGET%",name);			
					}*/
					String spk = NativeData.getResPlaceholderString(
							"RS_MAP_SEARCHING", "%TARGET%",name);
					mWinProcessingPoiSearch = new WinProcessing(spk, true) {
						@Override
						public void onCancelProcess() {
							cancelAllPoiSearch();
						}

						@Override
						public String getReportDialogId() {
							return "nav_poi_searching";
						}
					};
					mWinProcessingPoiSearch.show();
				}
			}, 0);
		} else {
			TtsManager.getInstance().cancelSpeak(mSearchTtsId);
			mSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;
			AsrManager.getInstance().cancel();
			if (!RecorderWin.isOpened()) {
				RecorderWin.show();
			}
			RecorderWin.refreshState(RecorderWin.STATE_NORMAL);
/*			String spk = null;
			if(!TextUtils.isEmpty(strRegion)){
				spk = NativeData.getResString("RS_MAP_SEARCH_NEAR")
						.replace("%POI%", strRegion)
						.replace("%KEY%", navTarName);
			}else{
				spk = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCHING", "%TARGET%",navTarName);			
			}	*/
			String spk = NativeData.getResPlaceholderString("RS_MAP_SEARCHING", "%TARGET%", navTarName);
			if(TextUtils.isEmpty(spk)){
				return;
			}
			String hint = avoidAnnoy ? "" : spk;
			if (!avoidAnnoy)
				RecorderWin.addSystemMsg(spk);
			mSearchTtsId = TtsManager.getInstance().speakText(hint,
					mPoiSearchTtsCallback);
		}
		RecorderWin.addCloseRunnable(new Runnable() {
			@Override
			public void run() {
				cancelAllPoiSearchIncludeTts();
			}
		});
	}

	public byte[] preNavigateByName(JSONObject json, String action) {
		try {
			NavigateInfo navigateInfo = new NavigateInfo();
			boolean  isEnd = false;
			if(action.equals(PoiAction.ACTION_NAVI_END)){
				isEnd = true;
			}
			if (json.has("city")){
				navigateInfo.strTargetCity = json.getString("city");
				if(mSearchBySelect){
					mTargetCity = navigateInfo.strTargetCity ;
				}
			}
				
			navigateInfo.strTargetName = json.getString("keywords");
			if (isBussinessName(navigateInfo.strTargetName)) {
				NearbySearchInfo info = new NearbySearchInfo();
				info.strCenterCity = navigateInfo.strTargetCity;
				info.strKeywords = navigateInfo.strTargetName;
				if(isEnd){
					info.strCenterPoi = "END_POI";
				}
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
						UiMap.SUBEVENT_MAP_NAVIGATE_BUSSINESS, info);
				return null;
			}
			if (isNearbyName(navigateInfo.strTargetName) || isEnd) {
				NearbySearchInfo info = new NearbySearchInfo();
				info.strCenterCity = navigateInfo.strTargetCity;
				info.strKeywords = navigateInfo.strTargetName;
				if(isEnd){
					info.strCenterPoi = "END_POI";
				}
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
						UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, info);
				return null;
			}
			navigateByName(navigateInfo, true, action, false);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int onEvent(int eventId, int subEventId, byte[] data) {

		if (UiEvent.EVENT_ACTION_IM == eventId) {
			switch (subEventId) {
			case com.txz.ui.im.UiIm.SUBEVENT_ACTION_ROOM_IN_RESP: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
						"nav.multi.roomin", data, null);
				break;
			}
			case com.txz.ui.im.UiIm.SUBEVENT_ACTION_ROOM_OUT_RESP: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
						"nav.multi.roomout", data, null);
				break;
			}
			case com.txz.ui.im.UiIm.SUBEVENT_ACTION_ROOM_MEMBER_LIST_RESP: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
						"nav.multi.memlist", data, null);
				break;
			}
			case com.txz.ui.im.UiIm.SUBEVENT_ACTION_ROOM_UPDATE_NOTIFY: {
				ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
						"nav.multi.update", data, null);
				break;
			}
			}
			
			return 0;
		}
		if (UiEvent.EVENT_ACTION_EQUIPMENT == eventId) {
			switch (subEventId) {
			case UiEquipment.SUBEVENT_RESP_POI_SEARCH: {
				JNIHelper.logd("SUBEVENT_RESP_POI_SEARCH");
				UiEquipment.Resp_POI_Search result;
				try {
					if(data==null||data.length==0){
						result=null;
						JNIHelper.logd("SUBEVENT_RESP_POI_SEARCH data=null");
					}else{
						result = UiEquipment.Resp_POI_Search.parseFrom(data);
					}
					if (mPoiSearchCallBack != null) {
						mPoiSearchCallBack.onResult(result);
					}
				} catch (Exception e) {
				}
	
				break;
			}
			case UiEquipment.SUBEVENT_RESP_POI_PARSE: {
				if(data==null||data.length==0){
					break;
				}
				Resp_POI_Parse parse=null;
				try {
					parse = Resp_POI_Parse.parseFrom(data);
					mAddressDetail =new String(parse.floorMsg);
					JNIHelper.logd("SUBEVENT_RESP_POI_PARSE mAddressDetail= "+mAddressDetail);
				} catch (Exception e) {
				}
	
				break;
			}
			case UiEquipment.SUBEVENT_NOTIFY_SERVER_CONFIG_UPDATE :{
				try {
					UiEquipment.ServerConfig pbServerConfig = UiEquipment.ServerConfig
							.parseFrom(data);
					mPoisearRetryCount = null;
					mPoisearTimeCost = null;
					if (pbServerConfig != null) {
						if (pbServerConfig.msgPoiSearchConf != null) {
							mPoisearRetryCount = pbServerConfig.msgPoiSearchConf.uint32RetryCnt;
							mPoisearTimeCost = pbServerConfig.msgPoiSearchConf.uint32Timeout;
						}
						if (pbServerConfig.uint32PoiSourceConf != null) {
							mPoisearchConfig = pbServerConfig.uint32PoiSourceConf;
						}
						if (pbServerConfig.uint32NavHisInfoHash != null) {
							NavInscriber.getInstance().reSyncHistory(pbServerConfig.uint32NavHisInfoHash);
						}
						if ((pbServerConfig.uint64Flags
								& UiEquipment.SERVER_CONFIG_FLAG_SUPPORT_DEFAULT_NAV) == UiEquipment.SERVER_CONFIG_FLAG_SUPPORT_DEFAULT_NAV) {
							mSupportDefaultNav = true;
							LogUtil.logd("supportDefaultNav");
							regSetDefaultNavCmd();
						} else {
							mSupportDefaultNav = false;
							LogUtil.logd("not supportDefaultNav");
						}
					}
					if (mPoisearRetryCount == null //
							|| mPoisearRetryCount < 0 || mPoisearRetryCount > MAX_SEARCH_RETRY // 范围保护
					) {
						JNIHelper.logw("illegal retry count config: " + mPoisearRetryCount);
						mPoisearRetryCount = DEFAULT_SEARCH_RETRYCOUNT;
					}
					if (mPoisearTimeCost == null //
							|| mPoisearTimeCost > MAX_SEARCH_TIMEOUT || mPoisearTimeCost < MIN_SEARCH_TIMEOUT // 范围保护
					) {
						JNIHelper.logw("illegal retry count config: " + mPoisearTimeCost);
						mPoisearTimeCost = DEFAULT_SEARCH_TIMEOUT;
					}
					
					if (mPoisearchConfig == null) {
						mPoisearchConfig = DEFAULT_SEARCH_CONFIG;
					}
					JNIHelper.logd("POISearchLog:getConfig  retryCount = "
							+ mPoisearRetryCount + " timeoutCost= "
							+ mPoisearTimeCost + ", searchConfig="
							+ mPoisearchConfig);
					
					boolean enable = true;
					if ((pbServerConfig.uint64Flags
							& UiEquipment.SERVER_CONFIG_FLAG_SMART_TRAVEL) == UiEquipment.SERVER_CONFIG_FLAG_SMART_TRAVEL) {
						enable = true;
					} else {
						enable = false;
					}
					FakeReqManager.getInstance().setSmartTrafficSetting(enable, true);
				} catch (Exception e) {
					
				}
			}
				break;
			case UiEquipment.SUBEVENT_RESP_TXZ_POI_SEARCH:{
				JNIHelper.logd("SUBEVENT_RESP_TXZ_POI_SEARCH");
				Resp_TXZPoiSearch result;
				try {
					if(data==null||data.length==0){
						result=null;
						JNIHelper.logd("SUBEVENT_RESP_POI_SEARCH data=null");
					}else{
						result = UiEquipment.Resp_TXZPoiSearch.parseFrom(data);
					}
					if (mTxzPoiSearchCallBack != null) {
						mTxzPoiSearchCallBack.onResult(result);
					}
				} catch (Exception e) {
					
				}
			}
				break;
			case UiEquipment.SUBEVENT_RESP_COMMON_CITY_INFO :{
				JNIHelper.logd("SUBEVENT_RESP_COMMON_CITY_INFO");
					Resp_CommonCity result;
					try {
						if(data == null  || data.length ==0 ){
							result = null;
							JNIHelper.logd("SUBEVENT_RESP_COMMON_CITY_INFO data=null");
						}else{
							result=UiEquipment.Resp_CommonCity.parseFrom(data);
							if(result != null){
								UiEquipment.CommonCityInfo[] info = result.rptCommonCity;
								if(info != null && info.length >0){
									for(UiEquipment.CommonCityInfo item : info){
										if( !TextUtils.isEmpty(item.strCityName) ){
											mCommonCityList.add(item.strCityName);
										}
									}
								}
							}
						}
					} catch (Exception e) {
					}
			}
				break;
			case UiEquipment.SUBEVENT_RESP_REPORT_NAV_INFO:
				NavInscriber.getInstance().handleData(data);
				break;
			case UiEquipment.SUBEVENT_RESP_DESTINATION_RECOMMEND:
				onGetRecommandDest(data);
				break;
			default:
				break;
			}
		}
// 去掉百度导航安装监测
//		if (UiEvent.EVENT_SYSTEM_APP == eventId) {
//			switch (subEventId) {
//			case UiApp.SUBEVENT_UPDATED_APP_LIST:
//				boolean navListHaveBaidu = false;
//				boolean appListHaveBaidu = false;
//				String baidu = "com.baidu.naviauto";
//				Set<String> keySet = mNavAppMap.keySet();
//				for (String key : keySet) {
//					NavThirdApp navThirdApp = mNavAppMap.get(key);
//					if (navThirdApp.getPackageName().equals(baidu))
//						navListHaveBaidu = true;
//				}
//				StringBuilder str = null;
//				try {
//					AppInfoList parseFrom = AppInfoList.parseFrom(data);
//					for (com.txz.ui.app.UiApp.AppInfo info : parseFrom.rptMsgApps) {
//						if (info.strPackageName.equals(baidu))
//							appListHaveBaidu = true;
//					}
//
//				} catch (InvalidProtocolBufferNanoException e) {
//					e.printStackTrace();
//				}
//				if (navListHaveBaidu != appListHaveBaidu) {
//					if (navListHaveBaidu) {
//						mNavAppMap.remove(baidu);
//					} else {
//						final NavBaiduDeepImpl sNavBaiduDeepImpl = new NavBaiduDeepImpl();
//						sNavBaiduDeepImpl.initialize(new IInitCallback() {
//							@Override
//							public void onInit(boolean bSuccess) {
//								if (bSuccess) {
//									synchronized (mNavAppMap) {
//										mNavAppMap.put(sNavBaiduDeepImpl.getPackageName(), sNavBaiduDeepImpl);
//									}
//								}
//							}
//						});
//					}
//				}
//			}
//		}
		
		if (UiEvent.EVENT_SYSTEM_MAP != eventId)
			return 0;
		mIsAddOnWay = NO_ONWAY;
		mIsEnd = false;
		switch (subEventId) {
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_HOME: {
			NavigateInfo navigateInfo = null;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
				if (!TextUtils.isEmpty(navigateInfo.strTargetName)) {
					navigateByName(navigateInfo, false, PoiAction.ACTION_HOME,
							false);
					break;
				}
			} catch (InvalidProtocolBufferNanoException e) {
			}
			if (CollectLocsPlugin.processNavigateInfo(navigateInfo, PoiAction.ACTION_NAVI)) {
				JNIHelper.logd("collect process navigate home");
				break;
			}
			NavigateHome();
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_COMPANY: {
			NavigateInfo navigateInfo = null;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
				if (!TextUtils.isEmpty(navigateInfo.strTargetName)) {
					navigateByName(navigateInfo, false,
							PoiAction.ACTION_COMPANY, false);
					break;
				}
			} catch (InvalidProtocolBufferNanoException e) {
			}
			if (CollectLocsPlugin.processNavigateInfo(navigateInfo, PoiAction.ACTION_NAVI)) {
				JNIHelper.logd("collect process navigate company");
				break;
			}
			NavigateCompany();
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_PASS_BY_NAME: {
			JNIHelper.logd("recv pass...");
			if (enableWayPoiSearch()) {
				mIsPass = true;
			} else {
				if (NavManager.getInstance().isNavi()) {
					AsrManager.getInstance().setNeedCloseRecord(false);
					RecorderWin.speakTextWithClose(disableSetJTPoi(), null);
//					String spk = NativeData.getResString("RS_MAP_ANY_HELP");
//					RecorderWin.speakTextWithClose(spk, null);
				} else {
					String answer = NativeData
							.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
					AsrManager.getInstance().setNeedCloseRecord(false);
					RecorderWin
							.speakTextWithClose(
									answer,	null);
				}
				break;
			}
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_BY_NAME: {
			JNIHelper.logd("POISearchLog:SUBEVENT_MAP_NAVIGATE_BY_NAME");
			NavigateInfo navigateInfo;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
				break;
			}

			if (CollectLocsPlugin.processNavigateInfo(navigateInfo,
					mIsPass ? PoiAction.ACTION_JINGYOU : PoiAction.ACTION_NAVI)) {
				JNIHelper.logd("process navigateInfo by collect plugin");
				break;
			}

			String action = PoiAction.ACTION_NAVI;
			switch (AsrManager.getInstance().getLastGrammarId()) {
			case VoiceData.GRAMMAR_SENCE_SET_HOME:
				action = PoiAction.ACTION_HOME;
				break;
			case VoiceData.GRAMMAR_SENCE_SET_COMPANY:
				action = PoiAction.ACTION_COMPANY;
				break;
			default:
				action = PoiAction.ACTION_NAVI;
				break;
			}
			if (mIsPass) {
				mIsPass = false;
				action = PoiAction.ACTION_JINGYOU;
			}

			mTXZSearchResultListener.reset();

			if ("CURRENT_LOC".equals(navigateInfo.strTargetName)) {
				handleRecommandDest(data);
				break;
			}

			if (!(navigateInfo.strTargetName.equals("那里"))
					&& navigateInfo.strTargetName.endsWith("那里")) {
				navigateInfo.strTargetName = navigateInfo.strTargetName
						.substring(0, navigateInfo.strTargetName.length() - 2);
			} else if (!(navigateInfo.strTargetName.equals("那"))
					&& navigateInfo.strTargetName.endsWith("那")) {
				navigateInfo.strTargetName = navigateInfo.strTargetName
						.substring(0, navigateInfo.strTargetName.length() - 1);
			}

			navigateByName(navigateInfo, false, action, false);

			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_BUSSINESS: {
			MtjModule.getInstance().event(MtjModule.EVENTID_NAV_NEARBY);
			JNIHelper.logd("POISearchLog:SUBEVENT_MAP_NAVIGATE_BUSSINESS");
			mTXZSearchResultListener.reset();
			
			cancelAllPoiSearch();

			try {
				NearbySearchInfo info = NearbySearchInfo.parseFrom(data);
				int radius = 0;
				if (info.uint32Radius != null)
					radius = info.uint32Radius;
				String hint = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCHING", "%TARGET%", info.strKeywords);
/*				String hint = null;
				if(!TextUtils.isEmpty(info.strRegion)){
					hint = NativeData.getResString("RS_MAP_SEARCH_NEAR")
							.replace("%POI%", info.strRegion)
							.replace("%KEY%", info.strKeywords);
				}else{
					hint = NativeData.getResPlaceholderString(
							"RS_MAP_SEARCHING", "%TARGET%", info.strKeywords);				
				}*/
				String action = null;
				if(info.strCenterPoi!=null&&info.strCenterPoi.equals("ON_WAY")){
					if (enableWayPoiSearch()) {
						action = PoiAction.ACTION_JINGYOU;
						if(RecorderWin.isOpened()){
							mIsAddOnWay=SHOW_LIST_ONWAY;
						}else{
							mIsAddOnWay=NO_SHOW_ONWAY;
						}
					} else {
						if (NavManager.getInstance().isNavi()) {
							AsrManager.getInstance().setNeedCloseRecord(false);
							RecorderWin.speakTextWithClose(disableSetJTPoi(), null);
//							String spk = NativeData.getResString("RS_MAP_ANY_HELP");
//							RecorderWin.speakTextWithClose(spk, null);
						} else {
							String answer = NativeData
									.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
							AsrManager.getInstance().setNeedCloseRecord(false);
							RecorderWin
									.speakTextWithClose(
											answer,	null);
						}
						break;
					}				
					info.strCenterPoi=null;
				}
				LocationInfo loc = LocationManager.getInstance()
						.getLastLocation();
				String locationCity = null;
				if(loc != null && loc.msgGeoInfo !=null 
						&& !TextUtils.isEmpty(loc.msgGeoInfo.strCity)){
					locationCity = loc.msgGeoInfo.strCity;
				}
				if (radius == 0 && !TextUtils.isEmpty(info.strCenterCity)
						&& TextUtils.isEmpty(info.strCenterPoi) 
						&& (TextUtils.isEmpty(locationCity)|| !locationCity.equals(info.strCenterCity))) {
					// 城市商圈搜索
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity(info.strCenterCity)
							.setKeywords(info.strKeywords)
							.setNum(sSearchCount).
							setRegion(info.strRegion);
					mTXZPoiSearchResultListener.setOption(opt);

					String target = "";
					if (!TextUtils.isEmpty(info.strRegion)) {
						target += info.strRegion + "附近的" + info.strKeywords;
					} else {
						target = info.strKeywords;
					}
					hint = NativeData.getResPlaceholderString(
							"RS_MAP_SEARCHING", "%TARGET%", target);
					playSearchInfo(hint);
					navigateBussinessCity(opt, false, action);
				} else {
					// 周边商圈搜索
					NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
							.setKeywords(info.strKeywords).setRadius(radius)
							.setNum(sSearchCount);
					// 去罗湖区的肯德基，此时CenterPoi为空，将Region字段覆盖给CenterPoi
					if (TextUtils.isEmpty(info.strCenterPoi)) {
						if (!TextUtils.isEmpty(info.strRegion)) {
							info.strCenterPoi = info.strRegion;
						}
					}
					// 判断要先进行中心搜索
					if (!TextUtils.isEmpty(info.strCenterPoi)) {
						String centerCity = info.strCenterCity;
						if(!endPoiSearch(info, true)){
							if (TextUtils.isEmpty(centerCity)) {
								if (loc == null || loc.msgGpsInfo == null
									|| loc.msgGeoInfo == null
									|| loc.msgGpsInfo.dblLat == null
									|| loc.msgGpsInfo.dblLng == null) {
									String spk = NativeData
											.getResString("RS_MAP_LOC_ERROR_NO_SEARCH");
									mTXZSearchResultListener
										.showSearchError(spk);
									break;
								}
								centerCity = loc.msgGeoInfo.strCity;
							}
							CityPoiSearchOption center_opt = new CityPoiSearchOption()
								.setCity(centerCity)
								.setKeywords(info.strCenterPoi)
								.setNum(sSearchCount)
								.setRegion(info.strRegion);
							hint = NativeData.getResString("RS_MAP_SEARCH_NEAR")
								.replace("%POI%", info.strCenterPoi)
								.replace("%KEY%", info.strKeywords);
							mTXZPoiSearchResultListener.setOption(opt);
							playSearchInfo(hint);
							navigateBussinessNearbyCenter(center_opt, opt, false,
									action);						
						}
					} else {
						if (loc == null || loc.msgGpsInfo == null
								|| loc.msgGeoInfo == null
								|| loc.msgGpsInfo.dblLat == null
								|| loc.msgGpsInfo.dblLng == null) {
							String spk = NativeData.getResString("RS_MAP_LOC_ERROR_NO_SEARCH");
							mTXZSearchResultListener.showSearchError(spk);
							break;
						}
						LogUtil.logd("loc.msgGpsInfo.dblLat:" + loc.msgGpsInfo.dblLat);
						LogUtil.logd("loc.msgGpsInfo.dblLng:" + loc.msgGpsInfo.dblLng);
						LogUtil.logd("loc.msgGeoInfo.strCity:" + loc.msgGeoInfo.strCity);
						opt.setCenterLat(loc.msgGpsInfo.dblLat)
								.setCenterLng(loc.msgGpsInfo.dblLng)
								.setCity(loc.msgGeoInfo.strCity != null ? loc.msgGeoInfo.strCity:"")
								.setRegion(info.strRegion);
						mTXZPoiSearchResultListener.setOption(opt);
						playSearchInfo(hint);
						navigateBussinessNearby(opt, false, action);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY: {
			MtjModule.getInstance().event(MtjModule.EVENTID_NAV_NEARBY);
			JNIHelper.logd("POISearchLog:SUBEVENT_MAP_NAVIGATE_NEARBY");
			mTXZSearchResultListener.reset();

			cancelAllPoiSearch();
			try {
				NearbySearchInfo info = NearbySearchInfo.parseFrom(data);
				JNIHelper.logd("POISearchLog: SUBEVENT_MAP_NAVIGATE_NEARBY strCenterPoi:" + info.strCenterPoi);
				JNIHelper.logd("POISearchLog: SUBEVENT_MAP_NAVIGATE_NEARBY strCenterCity:" + info.strCenterCity);
				JNIHelper.logd("POISearchLog: SUBEVENT_MAP_NAVIGATE_NEARBY strKeywords:" + info.strKeywords);
				int radius = 0;
				if (info.uint32Radius != null)
					radius = info.uint32Radius;
				String hint = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCHING", "%TARGET%", info.strKeywords);
/*				String hint =null;
				if(!TextUtils.isEmpty(info.strRegion)){
					hint = NativeData.getResString("RS_MAP_SEARCH_NEAR")
							.replace("%POI%", info.strRegion)
							.replace("%KEY%", info.strKeywords);
				}else{
					hint = NativeData.getResPlaceholderString(
							"RS_MAP_SEARCHING", "%TARGET%", info.strKeywords);				
				}
*/
				String action = null;
				if(info.strCenterPoi!=null&&info.strCenterPoi.equals("ON_WAY")){
					if (enableWayPoiSearch()) {
						action = PoiAction.ACTION_JINGYOU;
						if(RecorderWin.isOpened()){
							mIsAddOnWay=SHOW_LIST_ONWAY;
						}else{
							mIsAddOnWay=NO_SHOW_ONWAY;
						}
					} else {
						if (NavManager.getInstance().isNavi()) {
							AsrManager.getInstance().setNeedCloseRecord(false);
							RecorderWin.speakTextWithClose(disableSetJTPoi(), null);
//							String spk = NativeData.getResString("RS_MAP_ANY_HELP");
//							RecorderWin.speakTextWithClose(spk, null);
						} else {
							String answer = NativeData
									.getResString("RS_VOICE_UNSUPPORT_OPERATE_2");
							AsrManager.getInstance().setNeedCloseRecord(false);
							RecorderWin
									.speakTextWithClose(
											answer,	null);
						}
						break;
					}
					info.strCenterPoi=null;
				}
				if(!endPoiSearch(info, false)){
					LocationInfo loc = LocationManager.getInstance()
							.getLastLocation();
						if (loc == null || loc.msgGpsInfo == null
								|| loc.msgGeoInfo == null
								|| loc.msgGpsInfo.dblLat == null
								|| loc.msgGpsInfo.dblLng == null) {
							loc = null;
						}
						boolean isDiffCity = false;
						if(loc != null && !TextUtils.isEmpty(info.strCenterCity) 
							&& !isSameCity(info.strCenterCity, loc.msgGeoInfo.strCity)) {
							isDiffCity = true;
						}
						if( (radius == 0 && isDiffCity && TextUtils.isEmpty(info.strCenterPoi))) {
								// 城市搜索
							CityPoiSearchOption opt = new CityPoiSearchOption()
								.setCity(info.strCenterCity)
								.setKeywords(info.strKeywords)
								.setNum(sSearchCount)
								.setRegion(info.strRegion);
							mTXZPoiSearchResultListener.setOption(opt);
							String target = "";
							if (!TextUtils.isEmpty(info.strRegion)) {
								target += info.strRegion + "附近的" + info.strKeywords;
							} else {
								target = info.strKeywords;
							}
							hint = NativeData.getResPlaceholderString(
									"RS_MAP_SEARCHING", "%TARGET%", target);
							playSearchInfo(hint);
							navigateCity(null, opt, false, action);
						} else {
							NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
								.setRadius(radius).setKeywords(info.strKeywords)
								.setNum(sSearchCount);
							// 判断要先进行中心搜索
							if (!TextUtils.isEmpty(info.strCenterPoi)) {
								CityPoiSearchOption center_opt = new CityPoiSearchOption()
									.setCity(info.strCenterCity)
									.setKeywords(info.strCenterPoi)
									.setNum(sSearchCount)
									.setRegion(info.strRegion);
								hint = NativeData.getResString("RS_MAP_SEARCH_NEAR")
										.replace("%POI%", info.strCenterPoi)
										.replace("%KEY%", info.strKeywords);
								mTXZPoiSearchResultListener.setOption(opt);
								playSearchInfo(hint);
								navigateNearbyCenter(center_opt, opt, false, action);
							} else {
								if (loc == null) {
									String spk = NativeData
											.getResString("RS_MAP_LOC_ERROR");
									mTXZSearchResultListener.showSearchError(spk);
									break;
								}
								opt.setCenterLat(loc.msgGpsInfo.dblLat)
									.setCenterLng(loc.msgGpsInfo.dblLng)
									.setCity(loc.msgGeoInfo.strCity)
									.setRegion(info.strRegion);
								mTXZPoiSearchResultListener.setOption(opt);
								playSearchInfo(hint);
								navigateNearby(opt, false, action);
							}
						}
				}
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_MODIFY_HOME: {
			ModifyHome(MODIFY_HC_ADDRESS);
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_MODIFY_COMPANY: {
			ModifyCompany(MODIFY_HC_ADDRESS);
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_BY_GPS: {
			try {
				NavigateTo(NavigateInfo.parseFrom(data), PoiAction.ACTION_NAVI);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
			break;
		}
		case UiMap.SUBEVENT_MAP_NOTIFY_UPLOAD_ADDR:
			CollectLocsPlugin.handleServerReportData(data);
			break;
		case 	UiMap.SUBEVENT_MAP_NAVIGATE_PASS_DEL_BY_NAME:{
			JNIHelper.logd("POISearchLog: SUBEVENT_MAP_NAVIGATE_PASS_DEL_BY_NAME");
			NavigateInfo navigateInfo = null;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
			} catch (Exception e) {
			}
			if(navigateInfo != null){
				showJingYou(navigateInfo.strTargetName);
			}
		}
			break;
		}

		return super.onEvent(eventId, subEventId, data);
	}
	
	private void showJingYou(String keyWord){
		JNIHelper.logd("POISearchLog: 删除添加点 "+keyWord);
		List<Poi> tjPoi =null;
		List<Poi> showPoi =new ArrayList<Poi>();
		NavThirdApp localNavImpl = getLocalNavImpl();
		if( ! (localNavImpl instanceof NavAmapAutoNavImpl) ||
				((NavAmapAutoNavImpl)localNavImpl).getMapCode() < 210 ){
			AsrManager.getInstance().setNeedCloseRecord(false);
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),null);
			return;
		}
		if(localNavImpl instanceof NavAmapAutoNavImpl){
			tjPoi= ((NavAmapAutoNavImpl)localNavImpl).upDataJingYouPoi();
		}
		for(int i = 0; i<tjPoi.size();i++){
			if(TextUtils.isEmpty(keyWord)){
				showPoi.add(tjPoi.get(i));
			}else if(tjPoi.get(i).getName().contains(keyWord)){
				showPoi.add(tjPoi.get(i));
			}
		}
		if(showPoi.size()==0){
			String speak = NativeData.getResString("RS_MAP_SEARCH_JINTYOU_EMPTY");
			mTXZSearchResultListener.showSearchError(speak);
			return ;
		}
		
		PoisData poisData = new PoisData();
		poisData.mPois = showPoi;
		poisData.city = showPoi.get(0).getCity();
		poisData.isBus = false;
		poisData.keywords = null;
		poisData.action = PoiAction.ACTION_DEL_JINGYOU;
		ChoiceManager.getInstance().showPoiList(poisData);
	}
	public boolean deleteJingYou(Poi poi){
		NavThirdApp localNavImpl = getLocalNavImpl();
		if(localNavImpl instanceof NavAmapAutoNavImpl){
			return ((NavAmapAutoNavImpl)localNavImpl).deleteJingYou(poi);
		}
		return false;
	}

	private void playSearchInfo(String speak) {
		if( !mPlayNavInfo ){
			mPlayNavInfo = true;
			return;
		}
		if (TextUtils.isEmpty(speak)) {
			return;
		}
		if(mIsAddOnWay!=NO_SHOW_ONWAY){
			RecorderWin.addSystemMsg(speak);
			mSearchTtsId = TtsManager.getInstance().speakText(speak,
					mPoiSearchTtsCallback);
			RecorderWin.addCloseRunnable(new Runnable() {
				@Override
				public void run() {
					cancelAllPoiSearchIncludeTts();
				}
			});					
		}
	}
	public boolean isNavi() {
		return NavAppManager.getInstance().isInNav();
	}

	public boolean isInited() {
		return NavAppManager.getInstance().isInit();
	}

	public static String sDefaultNavTool;
	
	private Boolean mBanNavTool;

	// 注：这个方法包含了远程导航，不需要再判断远程导航
	public NavThirdApp getLocalNavImpl() {
		if (mBanNavTool != null && mBanNavTool) {
			return null;
		}
		return NavAppManager.getInstance().getCurrNavTool();
	}
	
	public boolean showTraffic(final String city, final String addr) {
		final NavThirdApp nta = getLocalNavImpl();
		if (nta != null && (nta instanceof NavAmapAutoNavImpl || nta instanceof NavBaiduDeepImpl)) {
			if (!nta.isInNav() || !nta.isInFocus()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_MAP_OPEN_WITH_TRAFFIC");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
						if (nta instanceof NavBaiduDeepImpl && !BDHelper.isNewSDKVersion()) {
							nta.showTraffic(city, addr);
							return;
						}

						if (nta.isInNav()) {
							PackageManager.getInstance().openApp(nta.getPackageName());
							AppLogic.runOnBackGround(new Runnable() {
								
								@Override
								public void run() {
									String spk = NativeData.getResString("RS_MAP_ROAD_OPEN");
									((NavThirdComplexApp) nta).onNavCommand(true, "OPEN_TRAFFIC", spk);
								}
							}, 2000);
							return;
						}

						if (addr.startsWith("前方") || addr.startsWith("前面")) {
							LocationInfo li = LocationManager.getInstance()
									.getLastLocation();
							if (li != null) {
								GpsInfo gps = li.msgGpsInfo;
								if (gps != null) {
									nta.showTraffic(gps.dblLat, gps.dblLng);
									return;
								}
							}
						}
						nta.showTraffic(city, addr);
					}
				});
				return true;
			} else {
				String spk = NativeData.getResString("RS_MAP_ROAD_OPEN");
				((NavThirdComplexApp)nta).onNavCommand(true, "OPEN_TRAFFIC", spk);
				return true;
			}
		}
		return false;
	}

	/**
	 * 路径规划失败后是否有TTS反馈
	 * @param isSpeech
	 * @param speechTxt
	 */
	public void setSpeechAfterPlanError(boolean isSpeech, String speechTxt) {
		NavThirdApp nta = getLocalNavImpl();
		if (nta != null && nta instanceof NavThirdComplexApp) {
			((NavThirdComplexApp) nta).setSpeechAfterPlan(isSpeech);
			((NavThirdComplexApp) nta).setSpeechTTSText(speechTxt);
		}
	}
	
	/**
	 * 添加路径规划成功的回调, 只允许一次操作,Runnable执行后会清空
	 * @param r
	 */
	public void addNavThirdAppPlanEndRunnable(Runnable r){
		NavThirdApp nta = getLocalNavImpl();
		if (nta != null && nta instanceof NavThirdComplexApp) {
			NavThirdComplexApp ntca = (NavThirdComplexApp) nta;
			ntca.addPlanEndRunnable(r);
		}
	}

	public void showMyLocation() {

	}

	public boolean hasRemoteProcTool() {
		// 远程工具是否生效的原则：没有固定功能，并且没有设置默认导航工具
		// return !TextUtils.isEmpty(mNavToolServiceName) &&
		// !ProjectCfg.isFixCallFunction()
		// && TextUtils.isEmpty(ProjectCfg.getDefaultNavTool());
		// return false;
		return NavAppManager.getInstance().hasRemoteNavTool();
	}

	/**
	 * 是否支持途经点搜索
	 * 
	 * @return
	 */
	public boolean enableWayPoiSearch() {
		return TextUtils.isEmpty(disableSetJTPoi()) && isNavi();
	}

	public String getDisableResaon() {
		NavThirdApp nav = NavManager.getInstance().getLocalNavImpl();
		if (nav != null && nav.isReachable())
			return "";
		return NativeData.getResString("RS_VOICE_NO_NAV_TOOL");
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
		}
	};

	private void beginMultiNavigate(NavigateInfo info) {
		if (!filterNavigateInfo(info)) {
			return;
		}

		ActionRoomIn_Req arreq = new ActionRoomIn_Req();
		arreq.uint32FromType = info.msgServerPushInfo.uint32RoomFromType;
		arreq.uint64Rid = info.msgServerPushInfo.uint64RoomId;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_IM,
				UiIm.SUBEVENT_ACTION_ROOM_IN_REQ, arreq);
	}

	private void endMultiNavgate(NavigateInfo info) {
		if (!filterNavigateInfo(info)) {
			return;
		}

		ActionRoomOut_Req aoreq = new ActionRoomOut_Req();
		aoreq.uint32FromType = info.msgServerPushInfo.uint32RoomFromType;
		aoreq.uint64Rid = info.msgServerPushInfo.uint64RoomId;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_IM,
				UiIm.SUBEVENT_ACTION_ROOM_OUT_REQ, aoreq);
	}

	private void getMultiNavMemList(long roomId, long type, long distance,
			long time) {
		ActionRoomMemberList_Req armlr = new ActionRoomMemberList_Req();
		armlr.uint64Rid = roomId;
		armlr.uint32Type = (int) type;
		armlr.uint32TargetDistance = (int) distance;
		armlr.uint32TargetTime = (int) time;
		JNIHelper.sendEvent(UiEvent.EVENT_ACTION_IM,
				UiIm.SUBEVENT_ACTION_ROOM_MEMBER_LIST_REQ, armlr);
	}
	//处理目的地附近搜索相关的请求	
	private boolean endPoiSearch(NearbySearchInfo info,boolean isBussiness){
		String centerPoi = info.strCenterPoi;
		int radius=0;
		if (info.uint32Radius != null)
			radius = info.uint32Radius;
		NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
		.setKeywords(info.strKeywords).setRadius(radius)
		.setNum(sSearchCount);
		
		String hint = NativeData.getResPlaceholderString(
				"RS_MAP_SEARCHING", "%TARGET%", info.strKeywords);	
		
		if(centerPoi != null && centerPoi.equals("END_POI") ){
			if(getLocalNavImpl().isInNav() && getLocalNavImpl().getDestinationLatlng()!=null){
				String action = PoiAction.ACTION_NAVI_END;
				JNIHelper.logd("POISearchLog: 搜索目的地附近的"+ info.strKeywords);
				JNIHelper.logd("POISearchLog: toPoiLat "+getLocalNavImpl().getDestinationLatlng()[0]);
				JNIHelper.logd("POISearchLog: toPoiLng "+getLocalNavImpl().getDestinationLatlng()[1]);
				JNIHelper.logd("POISearchLog: toPoiCity "+getLocalNavImpl().getDestinationCity());	
				opt.setCenterLat(getLocalNavImpl().getDestinationLatlng()[0])
					.setCenterLng(getLocalNavImpl().getDestinationLatlng()[1]);
				if (NavAmapValueService.getInstance().mRoadInfo != null
						&&!TextUtils.isEmpty(NavAmapValueService.getInstance().mRoadInfo.toCity)) {
					opt.setCity(getLocalNavImpl().getDestinationCity());
				}
				mTXZPoiSearchResultListener.setOption(opt);
				mIsEnd=true;
				if(isBussiness){
					navigateBussinessNearby(opt, true, action);
				}else{
					navigateNearby(opt, false, action);
				}
				playSearchInfo(hint);
				return true;
			}else{
				info.strCenterPoi = "";
				JNIHelper.logd("POISearchLog: 搜索附近的"+ info.strKeywords);
				LocationInfo loc = LocationManager.getInstance()
						.getLastLocation();
				if (loc == null || loc.msgGpsInfo == null
					|| loc.msgGeoInfo == null
					|| loc.msgGpsInfo.dblLat == null
					|| loc.msgGpsInfo.dblLng == null) {
					String spk = NativeData
						.getResString("RS_MAP_LOC_ERROR");
					mTXZSearchResultListener.showSearchError(spk);
					return true;
				}
				opt.setCenterLat(loc.msgGpsInfo.dblLat)
				.setCenterLng(loc.msgGpsInfo.dblLng)
				.setCity(loc.msgGeoInfo.strCity);
				mTXZPoiSearchResultListener.setOption(opt);
				if(isBussiness){
					navigateBussinessNearby(opt, true, null);	
				}else{
					navigateNearby(opt, false, null);
				}
				playSearchInfo(hint);
				return true;
			}
		}
		return  false;
	}
	
	private boolean isOnWaySearch(String str){
		if(getNavStatue()){
			return getLocalNavImpl().getOnWaySearchToolCode(str) != -1;
		}
		return  false;
	}
	
		
	private boolean filterNavigateInfo(NavigateInfo info) {
		if (info == null) {
			return false;
		}

		if (info.msgServerPushInfo == null) {
			return false;
		}

		if (info.msgServerPushInfo.uint32Type != UiMap.NT_MULTI_NATIGATION) {
			return false;
		}

		return true;
	}
	
	/**
	 * 判断导航是否处于焦点
	 * @return
	 */
	public boolean isNavFocus() {
		return NavAppManager.getInstance().isInFocus();
	}

	public void exitAllNavTool() {
		NavThirdApp nta = NavAppManager.getInstance().getCurrActiveTool();
		if (nta == null || !nta.isReachable()) {
			nta = NavAppManager.getInstance().getCurrNavTool();
		}

		if (nta != null) {
			nta.exitNav();
			NavAppManager.getInstance().invokeRemoteStatus("status.nav.exit", nta.getPackageName().getBytes());
		}
	}
	
	/**
	 * 判断是否是返回导航
	 * @param isBackNav
	 * @return
	 */
	private boolean needSelectNav(boolean needSelect, boolean isBackNav) {
		if (!isBackNav) {
			return true;
		}
		NavThirdApp nta = null;
		if (needSelect && isBackNav) {
			nta = NavAppManager.getInstance().getCurrActiveTool();
		}
		if (nta == null) {
			nta = getLocalNavImpl();
		}
		if (nta != null) {
			if (isBackNav && nta.hasBeenOpen()) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 执行打开导航
	 */
	public void openNav(boolean isBackNav) {
		boolean needSelect = needAsrSelectNav();
		if (needSelect && needSelectNav(needSelect, isBackNav)) {
			NavAppManager.getInstance().showNavTool(false, NativeData.getResString("RS_NAVTOOL_LIST_SPK"),
					new com.txznet.txz.component.choice.OnItemSelectListener<NavAppManager.NavAppBean>() {

						@Override
						public boolean onItemSelected(boolean isPreSelect, NavAppBean v, boolean fromPage, int idx,
								String fromVoice) {
							NavThirdApp nta = NavAppManager.getInstance().getNavToolByName(v.strPackageName);
							if (nta != null) {
								openNavInner(nta);
							}
							return true;
						}
					});
			return;
		}

		NavThirdApp nta = null;
		if (needSelect && isBackNav) {
			nta = NavAppManager.getInstance().getCurrActiveTool();
		}
		if (nta == null) {
			nta = getLocalNavImpl();
		}
		
		String spk = NativeData.getResString("RS_CMD_OPEN_NAV");
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk, new Runnable1<NavThirdApp>(nta) {
			@Override
			public void run() {
				NavThirdApp nta = mP1;
				if (nta instanceof NavAmapAutoNavImpl) {
					JSONBuilder jsonBuilder = new JSONBuilder();
					jsonBuilder.put("action", "open");
					jsonBuilder.put("origin", ((NavAmapAutoNavImpl) nta).getMapDetCode());
					if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
						return;
					}
				}

				openNavInner(mP1);
			}
		});

		if (NetworkManager.getInstance().checkLeastFlow()) {
			String resText = NativeData.getResString("RS_VOICE_SIM_WITHOUT_FLOW_TIP");
			TtsManager.getInstance().speakText(resText);
		}
	}
	
	private void openNavInner(NavThirdApp nta) {
		NavThirdApp nav = nta;
		if (nav != null) {
			if (nav.isReachable()) {
				NavAppManager.getInstance().invokeRemoteStatus("status.nav.enter", nav.getPackageName().getBytes());
				nav.enterNav();
				RecorderWin.close();
				return;
			}
		}
		AsrManager.getInstance().setNeedCloseRecord(true);

		preInvokeWhenNavNotExists(new Runnable() {
			@Override
			public void run() {
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_NAV_TOOL"), null);
			}
		});
	}

	private boolean mPlayNavInfo = true;
	public byte[] invokeTXZNav(final String packageName, String command,
			byte[] data) {
		LogUtil.logd("invokeTXZNav packageName:" + packageName + ",command:" + command);
		if (command.startsWith("asr.key.")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.startsWith("remote.")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.startsWith("app.")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.startsWith("enablecmd")
				|| command.equals("enableWakeupExit")
				|| command.equals("forceRegister")
				|| command.equals("enableWakeupNav")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("isInNav")) {
			return ("" + isNavi()).getBytes();
		}
		if (command.equals("setRemoteFlag")) {
			RemoteNavImpl.setRemoteNavFlag(Integer.parseInt(new String(data)));
			return null;
		}
		if (command.equals("notifyNavStatus")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("notifyIsFocus")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("notifyPathInfo")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("notifyExitApp")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("autoNaviDelay")) {
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("useActiveNav")){
			return NavAppManager.getInstance().invokeNav(packageName, command, data);
		}
		if (command.equals("notifyExitAllNav")) {
			return null;
		}
        if (command.equals("isCloseWhenSetHcAddr")) {
            isCloseWhenSetHcAddr = Boolean.parseBoolean(new String(data));
            LogUtil.logd("isCloseWhenSetHcAddr:" + isCloseWhenSetHcAddr);
            return null;
        }
		if (command.equals("inner.notifyNavStatus")) {
			boolean b = Boolean.parseBoolean(new String(data));
			NavTxzImpl.setInNav(b);
			return null;
		}
		if (command.equals("inner.notifyInitStatus")) {
			return null;
		}
		if (command.equals("cutNavDialog")) {
			try {
				mRemoveNavDialog = Boolean.parseBoolean(new String(data));
				LogUtil.logd("mRemoveNavDialog:" + mRemoveNavDialog);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("banNavTool")) {
			try {
				mBanNavTool = Boolean.parseBoolean(new String(data));
				LogUtil.logd("mBanNavTool:" + mBanNavTool);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("alwayAsk")) {
			try {
				mAlwayAsk = Boolean.parseBoolean(new String(data));
				LogUtil.logd("mAlwayAsk:" + mAlwayAsk);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("multinav")) {
			try {
				mEnableMulti = Boolean.parseBoolean(new String(data));
				LogUtil.logd("mEnableMulti:" + mEnableMulti);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("poi.finish")) {
			try {
				mPoiActivityDismissDelay = Long.parseLong(new String(data));
				ChoiceManager.getInstance().setTimeoutDelay(mPoiActivityDismissDelay);
			} catch (NumberFormatException e) {
				LogUtil.loge(e.toString());
			}
		}
		if (command.equals("poi.afterStartNav.finish")) {
			try {
				mPoiActivtiyStartNavDismissDelay = Long.parseLong(new String(
						data));
			} catch (NumberFormatException e) {
				LogUtil.loge(e.toString());
			}
		}
		if (command.equals("wx.dismiss")) {
			try {
				long delay = Long.parseLong(new String(data));
				WeixinManager.getInstance().setConfirmDismissDelay(delay);
			} catch (NumberFormatException e) {
				e.printStackTrace();
			}
		}
		if (command.equals("inner.poiSearch")) {
			try {
				if (packageName != null) {
					mSearchByEdit = false;
				}

				if(mSearchByEdit && mHaveResult){
					mPlayNavInfo = false;
				}
				
				JSONObject json = new JSONObject(new String(data));
				int where = LOCATION_NONE;
				if (json.has("where"))
					where = json.getInt("where");
				String action;
				switch (where) {
				case LOCATION_HOME:
					action = PoiAction.ACTION_HOME;
					break;
				case LOCATION_COMPANY:
					action = PoiAction.ACTION_COMPANY;
					break;
				case LOCATION_JINGYOU:
					action = PoiAction.ACTION_JINGYOU;
					break;
				case LOCATION_END:
					action = PoiAction.ACTION_NAVI_END;
					break;
				default:
					action = PoiAction.ACTION_NAVI;
					break;
				}
				preNavigateByName(json, action);
			} catch (Exception e) {
				LogUtil.loge(e.toString());
			}
			return null;
		}
		if (command.equals("inner.updateLocation")) {
			try {
				// LocationInfo m = LocationInfo.parseFrom(data);
				// LocationManager.getInstance().setLastLocation(m);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}
		if (command.equals("getLocationInfo")) {
			return MessageNano.toByteArray(PreferenceUtil.getInstance()
					.getLocationInfo());
		}
		if (command.equals("delete.history")) {
			try {
				NavigateInfo info = NavigateInfo.parseFrom(data);
				return (deleteHistoryNavigateInfo(info) + "").getBytes();
			} catch (InvalidProtocolBufferNanoException e) {
				LogUtil.loge(e.toString());
			}
		}
		if (command.equals("settool")) {
			NavAppManager.getInstance().setRemoteNavService(null);
			ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
			if (data != null) {
				NavAppManager.getInstance().invokeNav(packageName, command, data);
			} else {
				JNIHelper.logd(packageName + " set nav tool object");
				ServiceManager.getInstance().addConnectionListener(
						mConnectionListener);
				ServiceManager.getInstance().sendInvoke(packageName, "", null,
						new GetDataCallback() {

					@Override
					public void onGetInvokeResponse(ServiceData data) {
						// 记录工具
						if (data != null)
							NavAppManager.getInstance().setRemoteNavService(packageName);
					}
				});
			}
			return null;
		}
		if (command.equals("clearDefaultNav")) {
			NavAppManager.getInstance().invokeNav(packageName, command, data);
			return null;
		}
		if (command.equals("setDefaultNav")) {
			NavAppManager.getInstance().invokeNav(packageName, command, data);
			return null;
		}
		if (command.equals("setStatusListener")) {
			NavAppManager.getInstance().invokeNav(packageName, command, data);
			return null;
		}

		if (command.equals("navToLocWithHint")) {
			try {
				NavigateInfo info = new NavigateInfo();
				JSONObject json = new JSONObject(new String(data));
				info.msgGpsInfo = new GpsInfo();
				info.msgGpsInfo.dblLat = json.getDouble("lat");
				info.msgGpsInfo.dblLng = json.getDouble("lng");
				info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
				if (json.has("city"))
					info.strTargetCity = json.getString("city");
				if (json.has("name")) {
					info.strTargetName = json.getString("name");
					info.strTargetAddress = json.getString("name");
				}
				if (json.has("geo"))
					info.strTargetAddress = json.getString("geo");
				String message = json.optString("text");
				String hint = json.optString("tts");
				if (TextUtils.isEmpty(message)) {
					message = NativeData.getResPlaceholderString(
							"RS_MAP_NAV_RECEICE", "%TARGET%", info.strTargetName
									+ "(" + info.strTargetAddress + ")");
				}
				if (TextUtils.isEmpty(hint)) {
					hint = NativeData.getResPlaceholderString(
							"RS_MAP_NAV_CANCEL", "%CMD%", message);
				}
				WeixinManager.getInstance().navigateConfirm(message, hint,
						null, info);
			} catch (Exception e) {
			}
			return null;
		}

		if (command.equals("getHomeLocation")) {
			com.txz.ui.data.UiData.UserConfig userConfig = NativeData
					.getCurUserConfig();
			if (userConfig.msgNetCfgInfo == null
					|| userConfig.msgNetCfgInfo.msgHomeLoc == null
					|| userConfig.msgNetCfgInfo.msgHomeLoc.msgGpsInfo == null) {
				return null;
			}
			try {
				JSONObject json = new JSONObject();
				json.put("lat",
						userConfig.msgNetCfgInfo.msgHomeLoc.msgGpsInfo.dblLat);
				json.put("lng",
						userConfig.msgNetCfgInfo.msgHomeLoc.msgGpsInfo.dblLng);
				json.put("city",
						userConfig.msgNetCfgInfo.msgHomeLoc.strTargetCity);
				json.put("name",
						userConfig.msgNetCfgInfo.msgHomeLoc.strTargetName);
				json.put("geo",
						userConfig.msgNetCfgInfo.msgHomeLoc.strTargetAddress);
				return json.toString().getBytes();
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("setHomeLocation")) {
			try {
				NavigateInfo info = new NavigateInfo();
				JSONObject json = new JSONObject(new String(data));
				info.msgGpsInfo = new GpsInfo();
				info.msgGpsInfo.dblLat = json.getDouble("lat");
				info.msgGpsInfo.dblLng = json.getDouble("lng");
				info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
				if (json.has("city"))
					info.strTargetCity = json.getString("city");
				if (json.has("name"))
					info.strTargetName = json.getString("name");
				if (json.has("geo"))
					info.strTargetAddress = json.getString("geo");
				setHomeLocation(info.strTargetName, info.strTargetAddress,
						info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng,
						UiMap.GPS_TYPE_GCJ02);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("clearHomeLocation")) {
			clearHomeLocation();
			return null;
		}
		if (command.equals("getCompanyLocation")) {
			com.txz.ui.data.UiData.UserConfig userConfig = NativeData
					.getCurUserConfig();
			if (userConfig.msgNetCfgInfo == null
					|| userConfig.msgNetCfgInfo.msgCompanyLoc == null
					|| userConfig.msgNetCfgInfo.msgCompanyLoc.msgGpsInfo == null) {
				return null;
			}
			try {
				JSONObject json = new JSONObject();
				json.put(
						"lat",
						userConfig.msgNetCfgInfo.msgCompanyLoc.msgGpsInfo.dblLat);
				json.put(
						"lng",
						userConfig.msgNetCfgInfo.msgCompanyLoc.msgGpsInfo.dblLng);
				json.put("city",
						userConfig.msgNetCfgInfo.msgCompanyLoc.strTargetCity);
				json.put("name",
						userConfig.msgNetCfgInfo.msgCompanyLoc.strTargetName);
				json.put("geo",
						userConfig.msgNetCfgInfo.msgCompanyLoc.strTargetAddress);
				return json.toString().getBytes();
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("setCompanyLocation")) {
			try {
				NavigateInfo info = new NavigateInfo();
				JSONObject json = new JSONObject(new String(data));
				info.msgGpsInfo = new GpsInfo();
				info.msgGpsInfo.dblLat = json.getDouble("lat");
				info.msgGpsInfo.dblLng = json.getDouble("lng");
				info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
				if (json.has("city"))
					info.strTargetCity = json.getString("city");
				if (json.has("name"))
					info.strTargetName = json.getString("name");
				if (json.has("geo"))
					info.strTargetAddress = json.getString("geo");
				setCompanyLocation(info.strTargetName, info.strTargetAddress,
						info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng,
						UiMap.GPS_TYPE_GCJ02);
			} catch (Exception e) {
			}
			return null;
		}
		if (command.equals("clearCompanyLocation")) {
			clearCompanyLocation();
			return null;
		}
		if (command.equals("updateHomeLocation")) {
			NavigateInfo info = JSON.parseObject(new String(data),
					NavigateInfo.class);
			notifyUpdateHomeLocation(info);
			return null;
		}
		if (command.equals("updateCompanyLocation")) {
			NavigateInfo info = JSON.parseObject(new String(data),
					NavigateInfo.class);
			notifyUpdateCompanyLocation(info);
			return null;
		}
		if (command.equals("navHome")) {
			NavigateHome();
			return null;
		}
		if (command.equals("navCompany")) {
			NavigateCompany();
			return null;
		}

//		if (packageName == null) {
//			NavThirdApp nta = NavAppManager.getInstance().getRemoteNav();
//			if (nta != null) {
//				((RemoteNavImpl) nta).sendInvokeCmd(command, data);
//				return null;
//			}
//		}

		if (command.equals("navTo")) {
			try {
				NavigateInfo info = new NavigateInfo();
				JSONObject json = new JSONObject(new String(data));
				info.msgGpsInfo = new GpsInfo();
				info.msgGpsInfo.dblLat = json.getDouble("lat");
				info.msgGpsInfo.dblLng = json.getDouble("lng");
				info.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
				info.uint32NavType = 1;// 1为语音发起
				if (ServiceManager.WEBCHAT.equals(packageName)) {
					info.uint32NavType = 2;// 2为微信发起
				}
				String action = PoiAction.ACTION_NAVI;
				if (json.has("city"))
					info.strTargetCity = json.getString("city");
				if (json.has("name")) {
					info.strTargetName = json.getString("name");
					info.strTargetAddress = json.getString("name");
				}
				if (json.has("geo"))
					info.strTargetAddress = json.getString("geo");
				if (json.has("action")) {
					action = json.getString("action");
				}
				NavigateTo(info, action);
			} catch (Exception e) {
			}
			return null;
		}

		if (command.equals("enterNav")) {
			NavThirdApp nav = getLocalNavImpl();
			if (nav != null) {
				if (nav.isReachable()) {
					NavAppManager.getInstance().invokeRemoteStatus("status.nav.enter", nav.getPackageName().getBytes());
					nav.enterNav();
					return "true".getBytes();
				}
			}
			AsrManager.getInstance().setNeedCloseRecord(true);

			preInvokeWhenNavNotExists(new Runnable() {
				@Override
				public void run() {
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_NAV_TOOL"), null);
				}
			});
			return "false".getBytes();
		}

		if (command.equals("exitNav")) {
			NavThirdApp nav = getLocalNavImpl();
			if (nav != null && nav.isReachable()) {
				NavAppManager.getInstance().invokeRemoteStatus("status.nav.exit", nav.getPackageName().getBytes());
				nav.exitNav();
				return null;
			}
			return null;
		}

		return null;
	}

	boolean isPlayRoom = false;
	Poi mNavTargetPoi = null;
	String mAddressDetail = null;
	boolean isInNav=false;
	
	public boolean getNavStatue(){
		return isInNav;
	}
	
	public void dealWithNavStatus(String navStatus) {
		JNIHelper.logd("naviStatus " + navStatus);
		if (navStatus.equals("navEnd")) {
			isInNav=false;
			if (isPlayRoom) {
				isPlayRoom = false;
				playRoom();
			}
		} else if (navStatus.equals("navStart")) {
			isPlayRoom = true;
			isInNav=true;
		}
	}

	private void playRoom() {
		if (mAddressDetail == null || mNavTargetPoi == null) {
			mAddressDetail = null;
			return;
		}
		int distance = BDLocationUtil.calDistance(mNavTargetPoi.getLat(),
				mNavTargetPoi.getLng());
		if (distance > 20) {
			mAddressDetail = null;
			return;
		}

		String roomTTS = NativeData.getResString("RS_VOICE_CAN_NOT_PROC_RESULT");
		roomTTS.replace("%KEYWORD%", mNavTargetPoi.getName());
		roomTTS.replace("%ROOM%", mAddressDetail);

		TtsManager.getInstance().speakText(
				NativeData.getResString(roomTTS),
				PreemptType.PREEMPT_TYPE_IMMEADIATELY);
		mAddressDetail = null;
	}

	public byte[] processInvoke(final String packageName, String command,
			byte[] data) {
		if (command.equals("txz.poi.setInnerTool")) {
			sPoiSearchToolType = null;
			String toolType = new String(data);
			sPoiSearchToolType = toolType;
			checkPoiResApp();
		} else if (command.equals("txz.poi.cleartool")) {
			sPoiSearchToolType = null;
			checkPoiResApp();
		} else if(command.equals("txz.poi.setShowModel")){
			if(data == null || data.length <=0){
				return null;
			}
			try {
				JSONObject jb = new JSONObject(new String(data));
				if(jb != null){
					boolean isList = jb.getBoolean("isList");
					setPoiShowIsList(isList);
				}				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else if(command.equals("txz.poi.setGaoDeAutoPlanningRoute")){
			if(data == null || data.length <=0){
				return null;
			}
			try {
				JSONObject jb = new JSONObject(new String(data));
				if(jb != null){
					boolean isPlanning= jb.getBoolean("isPlanning");
					setGaoDeAutoPlanningRoute(isPlanning);
				}				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if(command.equals("txz.poi.setPoiPlayTipTts")){
			if(data == null || data.length <=0){
				return null;
			}
			try {
				JSONObject jb = new JSONObject(new String(data));
				if(jb != null){
					boolean isPlayPoiTip= jb.getBoolean("isPlayPoiTip");
					setPoiPlayTipTts(isPlayPoiTip);
				}				
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}else if(command.equals("txz.poi.stopMapPoiViewModle")){
			JSONObject jb;
			try {
				jb = new JSONObject(new String(data));
				if(jb != null){
					boolean isEnable= jb.getBoolean("isEnable");
					ConfigData configData = UserConf.getInstance().getUserConfigData();
					configData.mEnableMapMode = isEnable;
					UserConf.getInstance().saveUserConfigData();
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}else if(command.equals("txz.poi.nearbyPoint")){//evcard附近的网点
			navEVCardNearbyPoint(3);
		}
		return null;
	}
	
	public boolean  isSupportMapModle(){
		ConfigData configData = UserConf.getInstance().getUserConfigData();
		if(configData.mEnableMapMode != null &&
				!configData.mEnableMapMode){
			return false;
		}
		if(!WinManager.getInstance().isSupportMapPoi()){
			return false;
		}
		return true;	
	}
	
	public boolean getPoiShowIsList(){
		ConfigData configData = UserConf.getInstance().getUserConfigData();
		if(configData.mPoiMapMode == null){
			configData = UserConf.getInstance().getFactoryConfigData();
		}
		if(configData.mEnableMapMode != null && 
				!configData.mEnableMapMode){
			return true;
		}
		if(configData.mPoiMapMode == null){
			int screenWidth = ScreenUtil.getScreenWidth();
			if(screenWidth > POIMAP_MINSWITCH){
				setPoiShowIsList(false);
				return false;
			}else{
				setPoiShowIsList(true);
				return true;
			}
		}
		
		if(configData.mPoiMapMode == POIMAP_LISTMODE){
			return true;
		}
		
		return false;
	}
	private boolean  mIsGaoDeAutoPlanningRoute = true;
	public void setGaoDeAutoPlanningRoute(boolean isPlanning){
		mIsGaoDeAutoPlanningRoute = isPlanning;
	}	
	public boolean getGaoDeAutoPlanningRoute(){
		return  mIsGaoDeAutoPlanningRoute;
	}
	private boolean  mPoiPlayTipTts = true;
	public void setPoiPlayTipTts(boolean isPlanning){
		mPoiPlayTipTts = isPlanning;
		JNIHelper.logd("zsbin:setPoiPlayTipTts = "+mPoiPlayTipTts);
	}	
	public boolean getPoiPlayTipTts(){
		JNIHelper.logd("zsbin:getPoiPlayTipTts= "+mPoiPlayTipTts);
		return  mPoiPlayTipTts;
	}
	
	public void setPoiShowIsList(boolean list){
		ConfigData configData = UserConf.getInstance().getUserConfigData();
		configData.mPoiMapMode = list?POIMAP_LISTMODE:POIMAP_MAPMODE;
		UserConf.getInstance().saveUserConfigData();
	}
	public byte[] invokeMultiNav(final String packageName, String command,
			byte[] data) {
		if (command.equals("inner.beginMultiNav")) {
			try {
				NavigateInfo info = NavigateInfo.parseFrom(data);
				beginMultiNavigate(info);
			} catch (InvalidProtocolBufferNanoException e) {
			}
		}
		if (command.equals("inner.endMultiNav")) {
			try {
				NavigateInfo info = NavigateInfo.parseFrom(data);
				endMultiNavgate(info);
			} catch (InvalidProtocolBufferNanoException e) {
			}
		}
		if (command.equals("inner.getMultiNavMemList")) {
			try {
				long[] values = DatasUtil.decodeBytes(data);
				getMultiNavMemList(values[0], values[1], values[2], values[3]);
			} catch (NumberFormatException e) {
				LogUtil.loge("解析byte[]发生异常！");
			}
		}

		return null;
	}
	private List<String> mNomCityList = new ArrayList<String>();
	public void setNomCityListForString(List<String> citys){		
		for(String city:citys){
			boolean isSame =false;
			for(String city2:mNomCityList){
				if( city.equals(city2) ){
					isSame = true;
					break;
				}
			}
			if(!isSame){
				mNomCityList.add(city);
			}
		}
	}	
	public void setNomCityList(List<? extends Poi> pois){
		for(final Poi poi:pois){
			if(poi != null && !TextUtils.isEmpty(poi.getCity())){
				boolean isSame =false;
				for(String city:mNomCityList){
					if( city.equals(poi.getCity()) ){
						isSame = true;
						break;
					}
				}
				if(!isSame){
					mNomCityList.add(poi.getCity());
				}			
			}else if(poi != null){
				LocationManager.getInstance().reverseGeoCode(poi.getLat(),poi.getLng(), new OnGeocodeSearchListener() {

					@Override
					public void onRegeocodeSearched(RegeocodeResult arg0, int arg1) {
						if (arg0 != null) {
							RegeocodeAddress ra = arg0.getRegeocodeAddress();
							String city = null;
							if (ra != null) {
								if(TextUtils.isEmpty( ra.getCity())){
									city =ra.getProvince();
								}else{
									city =ra.getCity();
								}
								if(TextUtils.isEmpty(city));
								boolean isSame =false;
								for(String city1:mNomCityList){
									if( city1.equals(city) ){
										isSame = true;
										break;
									}
								}
								if(!isSame){
									mNomCityList.add(city);
								}	
							}
						}
					}

					@Override
					public void onGeocodeSearched(GeocodeResult arg0, int arg1) {
					}
				});
			}
		}
	}
	public List<String> getNomCityList(){
		return mNomCityList;
	}
	
	private  List<String> mCommonCityList =new ArrayList<String>();
	public List<String> getCommonCity(){
		return mCommonCityList;
	}
	
	
	private  List<String> mTargetCityList =new ArrayList<String>();
	public List<String> getTargetCity(){
		return mTargetCityList;
	}
	public void setTargetCity(List<Poi> poiList){
		mTargetCityList.clear();
		for(Poi poi : poiList){
			boolean isHave = false;
			String city = poi.getCity();
			if(TextUtils.isEmpty(city)){
				continue;
			}
			if( !city.endsWith("市")){
				city += "市";
			}
			for(int i = 0; i<mTargetCityList.size() ; i++){
				String city2 = mTargetCityList.get(i);
				String str;
				
				if(!city2.endsWith("市")){
					str = city2+ "市";
				}else{
					str = city2;
				}
				if(str.equals(city)){
					if(poi.getCity().length() > city2.length()){
						mTargetCityList.remove(i);
						mTargetCityList.add(poi.getCity());
					}
					isHave = true;
					break;
				}	
			}
			if(!isHave){
				mTargetCityList.add(poi.getCity());
			}
	
		}
	}
	public boolean checkSameName(String s1, String s2) {
		if (s1.equals(s2))
			return true;
		Set<String> ss1 = KeywordsParser.splitKeywords(s1);
		Set<String> ss2 = KeywordsParser.splitKeywords(s2);
		Set<String> min, max;
		if (ss1.size() > ss2.size()) {
			min = ss2;
			max = ss1;
		} else {
			min = ss1;
			max = ss2;
		}
		int count = 0;
		for (String s : min) {
			if (max.contains(s)) {
				count++;
			} else {
				for (String t : max) {
					if (t.startsWith(s)
							|| (s.startsWith(t) && max.contains(s.substring(t
									.length())))) {
						count++;
						break;
					}
				}
			}
		}
		return count > min.size() / 2;
	}
	public boolean checkSamePoiForList(List<Poi> list,Poi p){
		for(Poi poi:list){
			if( checkSamePoi(poi,p) ){
				return true;
			}
		}
		return false;
	}
	public boolean checkSamePoi(Poi p1, Poi p2) {
		try {
			double d = BDLocationUtil.calDistance(p1.getLat(), p1.getLng(),
					p2.getLat(), p2.getLng());
			if (d > 100) {
				return false;
			}
			return checkSameName(p1.getName(), p2.getName());
		} catch (Exception e) {
			return false;
		}
	}

	/*
	 created zackzhou，当导航不存在时的预处理
	 若被拦截则返回true，否则执行whenPass
	 */
	public boolean preInvokeWhenNavNotExists(Runnable whenPass) {
		if (NetworkManager.getInstance().hasNet()
				&& TXZFileConfigUtil.getBooleanSingleConfig(TXZFileConfigUtil.KEY_APP_REMOTE_INSTALL, false)) {
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NAV_TOOL_NOT_INSTALL"), new Runnable() {
				@Override
				public void run() {
					VisualUpgradeManager.getInstance().reqAppDownload(UiEquipment.APP_TYPE_NAVI);
				}
			});
			return true;
		} else {
			if (whenPass != null) {
				whenPass.run();
			}
			return false;
		}
	}
}