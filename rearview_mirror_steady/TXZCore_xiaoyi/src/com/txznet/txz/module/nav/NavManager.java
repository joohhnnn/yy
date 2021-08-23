package com.txznet.txz.module.nav;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSON;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.google.protobuf.nano.MessageNano;
import com.qihu.mobile.lbs.appfactory.QHAppFactory;
import com.txz.ui.data.UiData;
import com.txz.ui.data.UiData.UserConfig;
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
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.remote.util.TtsUtil.ITtsCallback;
import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.dialog.WinConfirmAsr;
import com.txznet.comm.ui.dialog.WinNotice;
import com.txznet.comm.ui.dialog.WinProcessing;
import com.txznet.comm.util.DatasUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZConfigManager.AsrMode;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.BusinessPoiDetail;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.nav.INav;
import com.txznet.txz.component.nav.INav.IInitCallback;
import com.txznet.txz.component.nav.INav.NavPlanType;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.cld.NavCldImpl;
import com.txznet.txz.component.nav.daodaotong.NavDdtImpl;
import com.txznet.txz.component.nav.gaode.NavAmapAutoNavImpl;
import com.txznet.txz.component.nav.gaode.NavAmapImpl;
import com.txznet.txz.component.nav.gaode.NavAutoNavImpl;
import com.txznet.txz.component.nav.mx.NavMXImpl;
import com.txznet.txz.component.nav.qihoo.NavQihooImpl;
import com.txznet.txz.component.nav.txz.NavTxzImpl;
import com.txznet.txz.component.poi.dzdp.BussinessPoiSearchDzdpImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGDLocalImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeImpl;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeWebImpl;
import com.txznet.txz.component.poi.mx.PoiSearchToolMXImpl;
import com.txznet.txz.component.poi.qihoo.PoiSearchToolQihooImpl;
import com.txznet.txz.component.poi.txz.CenterPoiSearchResultListener;
import com.txznet.txz.component.poi.txz.CenterPoiSearchResultListener.NextStepListener;
import com.txznet.txz.component.poi.txz.ChaosPoiSearchTXZImpl;
import com.txznet.txz.component.poi.txz.CityPoiSearchToolTXZimpl;
import com.txznet.txz.component.poi.txz.MaxRaduisNearPoiSearchToolTXZimpl;
import com.txznet.txz.component.poi.txz.NearToCityPoiSearchToolTXZimpl;
import com.txznet.txz.component.poi.txz.PoiSearchToolTXZimpl;
import com.txznet.txz.component.selector.SelectorHelper;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.cmd.CmdManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.nav.BDLocationUtil;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.KeywordsParser;
import com.txznet.txz.util.LocationUtil;
import com.txznet.txz.util.PreferenceUtil;

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

	static NavManager sModuleInstance = new NavManager();

	List<String> mInnerSuppNavs = new ArrayList<String>();
	Map<String, NavThirdApp> mNavAppMap = new HashMap<String, NavThirdApp>();
	NavTxzImpl mNavTxzImpl;
	NavCldImpl mNavCldImpl;

	private boolean mIsPass;
	private boolean mIsInited;
	public boolean mSearchByEdit;
	private long mPoiActivityDismissDelay;
	private long mPoiActivtiyStartNavDismissDelay = -1;

	private NavManager() {
//		final NavBaiduMapImpl sNavBaiduMapImpl = new NavBaiduMapImpl();
//		sNavBaiduMapImpl.initialize(new IInitCallback() {
//			@Override
//			public void onInit(boolean bSuccess) {
//				if (bSuccess) {
//					synchronized (mNavAppMap) {
//						mNavAppMap.put(sNavBaiduMapImpl.getPackageName(),
//								sNavBaiduMapImpl);
//					}
//				}
//			}
//		});
//		final NavBaiduNavImpl sNavBaiduNavImpl = new NavBaiduNavImpl();
//		sNavBaiduNavImpl.initialize(new IInitCallback() {
//			@Override
//			public void onInit(boolean bSuccess) {
//				if (bSuccess) {
//					synchronized (mNavAppMap) {
//						mNavAppMap.put(sNavBaiduNavImpl.getPackageName(),
//								sNavBaiduNavImpl);
//					}
//				}
//			}
//		});
//		final NavBaiduDeepImpl sNavBaiduDeepImpl = new NavBaiduDeepImpl();
//		sNavBaiduDeepImpl.initialize(new IInitCallback() {
//
//			@Override
//			public void onInit(boolean bSuccess) {
//				if (bSuccess) {
//					synchronized (mNavAppMap) {
//						mNavAppMap.put(sNavBaiduDeepImpl.getPackageName(), sNavBaiduDeepImpl);
//					}
//				}
//			}
//		});

//		final NavBaiduNavHDImpl sNavBaiduNavHDImpl = new NavBaiduNavHDImpl();
//		sNavBaiduNavHDImpl.initialize(new IInitCallback() {
//			@Override
//			public void onInit(boolean bSuccess) {
//				if (bSuccess) {
//					synchronized (mNavAppMap) {
//						mNavAppMap.put(sNavBaiduNavHDImpl.getPackageName(),
//								sNavBaiduNavHDImpl);
//					}
//				}
//			}
//		});

		final NavAmapImpl sNavAmapImpl = new NavAmapImpl();
		sNavAmapImpl.initialize(new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(sNavAmapImpl.getPackageName(),
								sNavAmapImpl);
					}
				}
			}
		});
		final NavAutoNavImpl sNavAutoNavImpl = new NavAutoNavImpl();
		sNavAutoNavImpl.initialize(new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(sNavAutoNavImpl.getPackageName(),
								sNavAutoNavImpl);
					}
				}
			}
		});
		final NavAmapAutoNavImpl sNavAmapAutoNavImpl = new NavAmapAutoNavImpl();
		sNavAmapAutoNavImpl.initialize(new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(NavAmapAutoNavImpl.PACKAGE_PREFIX, sNavAmapAutoNavImpl);
					}
				}
			}
		});
//		final NavAmapCarNavImpl sNavAmapCarNavImpl = new NavAmapCarNavImpl();
//		sNavAmapCarNavImpl.initialize(new IInitCallback() {
//
//			@Override
//			public void onInit(boolean bSuccess) {
//				if (bSuccess) {
//					synchronized (mNavAppMap) {
//						mNavAppMap.put(sNavAmapCarNavImpl.getPackageName(), sNavAmapCarNavImpl);
//					}
//				}
//			}
//		});
		mNavCldImpl = new NavCldImpl();
		mNavCldImpl.initialize(new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
//						mNavAppMap.put(mNavCldImpl.getPackageName(),
//								mNavCldImpl);
						mNavAppMap.put(NavCldImpl.PACKAGE_NAME_REG, mNavCldImpl);
					}
				}
			}
		});

		mNavTxzImpl = new NavTxzImpl();
		mNavTxzImpl.initialize(new IInitCallback() {
			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(ServiceManager.NAV, mNavTxzImpl);
					}
				}
			}
		});

		final NavMXImpl mMxNavImpl = new NavMXImpl();
		mMxNavImpl.initialize(new IInitCallback() {

			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(NavMXImpl.MX_PACKAGE_NAME, mMxNavImpl);
					}
				}
			}
		});

		final NavDdtImpl mNavDdtImpl = new NavDdtImpl();
		mNavDdtImpl.initialize(new IInitCallback() {

			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(NavDdtImpl.DDT_PACKAGE_NAME, mNavDdtImpl);
					}
				}
			}
		});

		final NavQihooImpl mQihooImpl = new NavQihooImpl();
		mQihooImpl.initialize(new IInitCallback() {

			@Override
			public void onInit(boolean bSuccess) {
				if (bSuccess) {
					synchronized (mNavAppMap) {
						mNavAppMap.put(mQihooImpl.getPackageName(), mQihooImpl);
					}
				}
			}
		});
		
		mInnerSuppNavs.add(ServiceManager.NAV);
		mInnerSuppNavs.add(NavAmapAutoNavImpl.PACKAGE_NAME);
		mInnerSuppNavs.add(NavAmapAutoNavImpl.PACKAGE_NAME_LITE);
		mInnerSuppNavs.add(sNavAutoNavImpl.getPackageName());
//		mInnerSuppNavs.add(sNavBaiduNavHDImpl.getPackageName());
//		mInnerSuppNavs.add(sNavBaiduDeepImpl.getPackageName());
//		mInnerSuppNavs.add(sNavBaiduMapImpl.getPackageName());
		mInnerSuppNavs.add(sNavAmapImpl.getPackageName());
		mInnerSuppNavs.add(mMxNavImpl.getPackageName());
		mInnerSuppNavs.add(mNavCldImpl.getPackageName());
		mInnerSuppNavs.add(mQihooImpl.getPackageName());
	}

	public static NavManager getInstance() {
		return sModuleInstance;
	}

	// /////////////////////////////////////////////////////////////////////////

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

	public void setHomeLocation(String name, String address, double lat,
			double lng, int gpsType, boolean needSync) {
		JNIHelper.logd("setHomeLocation:" + name);
		NavigateInfo navigateInfo = new NavigateInfo();
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
		UserConfig userConfig = NativeData.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		userConfig.msgNetCfgInfo.msgHomeLoc = navigateInfo;
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG,
				UiData.DATA_ID_CONFIG_USER, UserConfig.toByteArray(userConfig));

		// 通知导航应用
		ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
				"txz.nav.updateHomeLocation",
				NavigateInfo.toByteArray(navigateInfo), null);
		if (hasRemoteProcTool()) {
			Poi hPoi = new Poi();
			hPoi.setName(name);
			hPoi.setGeoinfo(address);
			hPoi.setLat(lat);
			hPoi.setLng(lng);
			ServiceManager.getInstance().sendInvoke(mNavToolServiceName, "tool.nav.setHomeLoc",
					hPoi.toString().getBytes(), null);
			return;
		}

		NavThirdApp navTool = getLocalNavImpl();
		if (needSync && navTool != null) {
			navTool.updateHomeLocation(navigateInfo);
		}
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

	public void setCompanyLocation(String name, String address, double lat,
			double lng, int gpsType, boolean needSync) {
		JNIHelper.logd("setCompanyLocation:" + name);
		NavigateInfo navigateInfo = new NavigateInfo();
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
		UserConfig userConfig = NativeData.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		userConfig.msgNetCfgInfo.msgCompanyLoc = navigateInfo;
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG,
				UiData.DATA_ID_CONFIG_USER, UserConfig.toByteArray(userConfig));

		// 通知导航应用
		ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
				"txz.nav.updateCompanyLocation",
				NavigateInfo.toByteArray(navigateInfo), null);
		if (hasRemoteProcTool()) {
			Poi cPoi = new Poi();
			cPoi.setName(name);
			cPoi.setLat(lat);
			cPoi.setLng(lng);
			cPoi.setGeoinfo(address);
			ServiceManager.getInstance().sendInvoke(mNavToolServiceName, "tool.nav.setCompanyLoc",
					cPoi.toString().getBytes(), null);
			return;
		}

		NavThirdApp navTool = getLocalNavImpl();
		if (needSync && navTool != null) {
			navTool.updateCompanyLocation(navigateInfo);
		}
	}

	public void clearHomeLocation() {
		UserConfig userConfig = NativeData.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		userConfig.msgNetCfgInfo.msgHomeLoc = null;
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG, UiData.DATA_ID_CONFIG_USER,
				UserConfig.toByteArray(userConfig));
		if (hasRemoteProcTool()) {
			ServiceManager.getInstance().sendInvoke(mNavToolServiceName, "tool.nav.setHomeLoc", null, null);
			return;
		}
	}

	public void clearCompanyLocation() {
		UserConfig userConfig = NativeData.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null)
			userConfig.msgNetCfgInfo = new com.txz.ui.contact.ContactData.UserNetConfigInfo();
		userConfig.msgNetCfgInfo.msgCompanyLoc = null;
		JNIHelper.sendEvent(com.txz.ui.event.UiEvent.EVENT_CONFIG,
				UiData.DATA_ID_CONFIG_USER, UserConfig.toByteArray(userConfig));
		if (hasRemoteProcTool()) {
			ServiceManager.getInstance().sendInvoke(mNavToolServiceName, "tool.nav.setCompanyLoc", null, null);
			return;
		}
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

	public void ModifyHome() {
		String txt = NativeData.getResString("RS_VOICE_WHERE_IS_YOUR_HOME");
		RecorderWin.open(txt, VoiceData.GRAMMAR_SENCE_SET_HOME, new Runnable() {
			@Override
			public void run() {
				AsrManager.getInstance().mSenceRepeateCount = 0;
			}
		});
	}

	public void NavigateHome() {
		// if (!TextUtils.isEmpty(mNavToolServiceName)) {
		// ServiceManager.getInstance().sendInvoke(mNavToolServiceName,
		// "tool.nav.navHome", null, null);
		// return;
		// }
		String txt = NativeData.getResString("RS_VOICE_WILL_GO_HOME");
		NavThirdApp nta = getLocalNavImpl();
		if (nta instanceof NavCldImpl) {
			boolean isNav = ((NavCldImpl) nta).naviHome(txt);
			if (isNav) {
				return;
			} else {
				ModifyHome();
			}
			return;
		}

		final com.txz.ui.data.UiData.UserConfig userConfig = NativeData
				.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null
				|| userConfig.msgNetCfgInfo.msgHomeLoc == null) {
			ModifyHome();
			return;
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(txt, new Runnable() {
			@Override
			public void run() {
				NavigateTo(userConfig.msgNetCfgInfo.msgHomeLoc);
			}
		});
	}

	public void ModifyCompany() {
		String txt = NativeData.getResString("RS_VOICE_WHERE_IS_YOUR_COMPANY");
		RecorderWin.open(txt, VoiceData.GRAMMAR_SENCE_SET_COMPANY,
				new Runnable() {
					@Override
					public void run() {
						AsrManager.getInstance().mSenceRepeateCount = 0;
					}
				});
	}

	public void NavigateCompany() {
		// if (!TextUtils.isEmpty(mNavToolServiceName)) {
		// ServiceManager.getInstance().sendInvoke(mNavToolServiceName,
		// "tool.nav.navCompany", null, null);
		// return;
		// }
		String txt = NativeData.getResString("RS_VOICE_WILL_GO_COMPANY");
		NavThirdApp nta = getLocalNavImpl();
		if (nta instanceof NavCldImpl) {
			boolean isNavi = ((NavCldImpl) nta).naviCompany(txt);
			if (isNavi) {
				return;
			} else {
				ModifyCompany();
			}
			return;
		}

		final com.txz.ui.data.UiData.UserConfig userConfig = NativeData
				.getCurUserConfig();
		if (userConfig.msgNetCfgInfo == null
				|| userConfig.msgNetCfgInfo.msgCompanyLoc == null) {
			ModifyCompany();
			return;
		}
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(txt, new Runnable() {
			@Override
			public void run() {
				NavigateTo(userConfig.msgNetCfgInfo.msgCompanyLoc);
			}
		});
	}

	public void NavigateTo(Poi p) {
		if (p.getSourceType() == Poi.POI_SOURCE_MEIXING && !TextUtils.isEmpty(p.getExtraStr())) {
			if (PackageManager.getInstance().checkAppExist(NavMXImpl.MX_PACKAGE_NAME)) {
				NavThirdApp nta = mNavAppMap.get(NavMXImpl.MX_PACKAGE_NAME);
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
		NavigateTo(navigateInfo);
	}

	public void NavigateTo(NavigateInfo info) {
		if (info == null || info.strTargetName == null
				|| info.strTargetAddress == null || info.msgGpsInfo == null
				|| info.msgGpsInfo.uint32GpsType == null
				|| info.msgGpsInfo.dblLat == null
				|| info.msgGpsInfo.dblLng == null) {
			JNIHelper.loge("wrong parameter");
			return;
		}

		if (hasRemoteProcTool()) {
			try {
				JSONObject json = new JSONObject();
				json.put("lat", info.msgGpsInfo.dblLat);
				json.put("lng", info.msgGpsInfo.dblLng);
				json.put("city", info.strTargetCity);
				json.put("name", info.strTargetName);
				if (info.strTargetAddress != null)
					json.put("geo", info.strTargetAddress);
				ServiceManager.getInstance().sendInvoke(mNavToolServiceName,
						"tool.nav.navTo", json.toString().getBytes(), null);
			} catch (Exception e) {
			}
			return;
		}

		NavThirdApp nav = getLocalNavImpl();
		if (nav != null) {
			if (!PackageManager.getInstance().checkAppExist(
					nav.getPackageName())) {
				TtsManager.getInstance().speakText(
						NativeData.getResString("RS_VOICE_NO_NAV_TOOL"));
			}
			if (hasStatusListener()) {
				ServiceManager.getInstance().sendInvoke(
						getStatusListenerServiceName(), "status.nav.enter",
						null, null);
			}
			nav.NavigateTo(NavPlanType.NAV_PLAN_TYPE_RECOMMEND, info);
		} else
			TtsManager.getInstance().speakText(
					NativeData.getResString("RS_VOICE_NO_NAV_TOOL"));
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
							if (!RecorderWin.isOpened()) {
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
			if (manual)
				WinNotice.showNotice(hint, true, true, null);
			else {
				AppLogic.runOnUiGround(new Runnable() {
					@Override
					public void run() {
						// 遇到多次无法识别的文字，判断是否安装了宏祥人工语音导航
						String spk = NativeData.getResString("RS_MAP_ARITIFICIAL_SERVICE");
						if (PoiAction.ACTION_NAVI.equals(action)
								&& PackageManager.getInstance().checkAppExist(
										"com.glsx.autonavi")) {
							WinConfirmAsr win = new WinConfirmAsr() {
								@Override
								public void onClickOk() {
									JNIHelper.logd("start com.glsx.autonavi");
									Intent autoNaviIntent = new Intent();
									autoNaviIntent
											.setClassName("com.glsx.autonavi",
													"com.glsx.autonavi.ui.MainActivity");
									autoNaviIntent.putExtra("autonaviType", 1);
									autoNaviIntent
											.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
													| Intent.FLAG_ACTIVITY_NEW_TASK
													| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
									GlobalContext.get().startActivity(
											autoNaviIntent);
								}
							}.setHintTts(hint + ","+spk)
									.setMessage(spk)
									.setSureText("是",
											new String[] { "是", "确定", "好的" })
									.setCancelText("否",
											new String[] { "否", "取消", "不要" });
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
											txt = NativeData.
													getResPlaceholderString(
															"RS_MAP_DESTINATION", 
															"%CMD%", hint);
										} else if (PoiAction.ACTION_HOME
												.equals(action)) {
											grammar = VoiceData.GRAMMAR_SENCE_SET_HOME;
											txt = NativeData
													.getResPlaceholderString(
															"RS_MAP_HOME", 
															"%CMD%", hint);
										} else if (PoiAction.ACTION_COMPANY
												.equals(action)) {
											grammar = VoiceData.GRAMMAR_SENCE_SET_COMPANY;
											txt = NativeData
													.getResPlaceholderString(
															"RS_MAP_CONMANY", 
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
			JNIHelper.loge("PoiSearch onError err=" + errCode);
			cancelAllPoiSearch();

			if (searchTool instanceof BussinessPoiSearchDzdpImpl) {
				// 如果大众点评搜索结果为空，改成高德搜索
				searchTool = new PoiSearchToolGaodeImpl();
				if (option instanceof NearbyPoiSearchOption) {
					JNIHelper
							.logd("PoiSearch change gaode tool to nearby search");
					option.setNum(SelectorHelper.sSearchCount);
					mSearchReqLastPoiSearch = new PoiSearchToolGaodeImpl()
							.searchNearby((NearbyPoiSearchOption) option,
									mTXZPoiSearchResultListener);
				} else {
					JNIHelper
							.logd("PoiSearch change gaode tool to city search");
					option.setNum(SelectorHelper.sSearchCount);
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
					option.setNum(SelectorHelper.sSearchCount);
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity(option.getCity())
							.setKeywords(option.getKeywords())
							.setNum(SelectorHelper.sSearchCount);
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
					option.setNum(SelectorHelper.sSearchCount);
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity("").setKeywords(option.getKeywords())
							.setNum(SelectorHelper.sSearchCount);
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
												.getResPlaceholderString("RS_MAP_NOT_FOUND", 
														"%CMD%", option.getKeywords());
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
						"RS_MAP_NOT_FOUND", "%CMD%", option.getKeywords());
				showSearchError(spk);
				break;
			}
			case TXZPoiSearchManager.ERROR_CODE_TIMEOUT: {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_TIMEOUT_ALL);

				AsrManager.getInstance().mSenceRepeateCount = -1;
				String hint = NativeData
						.getResPlaceholderString(
								"RS_MAP_SEARCH_TIMEOUT", 
								"%CMD%", option.getKeywords());
				if (procByNetNotWork(hint)) {
					break;
				}
				showSearchError(hint);
				break;
			}
			default: {
				MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ERROR_ALL);
				AsrManager.getInstance().mSenceRepeateCount = -1;
				String hintTxt = NativeData
						.getResPlaceholderString(
								"RS_MAP_SEARCH_ERROR", 
								"%CMD%", option.getKeywords());
				if (procByNetNotWork(hintTxt)) {
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
			mTXZSearchResultListener.onSuggestion(suggestion);
		}

		@Override
		public void onResult(List<Poi> result) {
			boolean bussiness = false;
			if (!result.isEmpty() && result.get(0) instanceof BusinessPoiDetail) {
				bussiness = true;
			}
			mTXZSearchResultListener.onResult(result, bussiness);
		}

		@Override
		public void onError(int errCode, String errDesc) {
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
					}
				}
			}, 0);

		};
	};

	Runnable mRunnableSearchResult = null;

	public void cancelAllPoiSearchIncludeTts() {
		cancelAllPoiSearch();
		TtsManager.getInstance().cancelSpeak(mSearchTtsId);
		mSearchTtsId = TtsManager.INVALID_TTS_TASK_ID;
	}

	public void cancelAllPoiSearch() {
		JNIHelper.logd("cancelAllPoiSearch");
		AppLogic.removeUiGroundCallback(mRunnableSearchResult);
		mRunnableSearchResult = null;
		if (mSearchReqLastPoiSearch != null) {
			mSearchReqLastPoiSearch.cancel();
			mSearchReqLastPoiSearch = null;
		}
		if (mWinProcessingPoiSearch != null) {
			mWinProcessingPoiSearch.cancel();
			mWinProcessingPoiSearch = null;
		}
	}

	public String disableSetJTPoi() {
		NavThirdApp nta = getLocalNavImpl();
		if (nta != null) {
			if (nta instanceof NavThirdComplexApp && nta.isInNav()) {
				return ((NavThirdComplexApp)nta).disableProcJingYouPoi();
			}
		}
		return "";
	}

	public boolean procJingYouPoi(Poi poi) {
		NavThirdApp nta = getLocalNavImpl();
		if (nta != null) {
			if (nta instanceof NavThirdComplexApp && nta.isInNav()) {
				return ((NavThirdComplexApp)nta).procJingYouPoi(poi);
			}
		}
		return false;
	}
	
	public boolean preInvokePoiSearchResult(final String city, String keywords,
			final String action, List<? extends Poi> pois, boolean isBussiness) {

		JSONBuilder jObj = new JSONBuilder();
		jObj.put("type", 2);
		jObj.put("keywords", keywords);
		jObj.put("city", city);
		jObj.put("action", action);
		if (isBussiness) {
			jObj.put("poitype", "business");
		}
		int count = pois != null ? pois.size() : 0;
		if (count > SelectorHelper.sSearchCount) {
			count = SelectorHelper.sSearchCount;
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
		}
		jObj.put("pois", jPois);
		if (SenceManager.getInstance().noneedProcSence("poi_choice",
				jObj.toBytes())) {
			return true;
		}

		if (count <= 0) {
			final String roughWords = getRoughWords(keywords);
			if (roughWords != null && roughWords.length() > 1) {
				String hint = NativeData
						.getResString("RS_MAP_NOT_FOUND_SEARCH")
						.replace("%SRC%", keywords)
						.replace("%DES%", getRoughWords(keywords));
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
		}

		// 完整的Poi数据
		List<Poi> poiList = new ArrayList<Poi>();
		for (int i = 0; i < count; i++) {
			poiList.add(pois.get(i));
		}

		SelectorHelper.entryPoisSelector(poiList, keywords, isBussiness, action, city);
		return true;
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
		Pattern re = Pattern.compile("([1-9a-zA-Z]+)(号|栋|座|层|室|区)?");
		Matcher r = re.matcher(keywords);
		if (r != null) {
			String kw = r.replaceAll("");
			if (!keywords.equals(kw) && !TextUtils.isEmpty(kw)) {
				return kw;
			}
		}
		// 得到大致位置，如：南山区中科大厦，可以被大致定位为南山区
		return KeywordsParser.getNextNavAddress(keywords);
	}

	private boolean procByNetNotWork(String hint) {
		if (mSearchByEdit) {
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
		if (hasStatusListener()) {
			ServiceManager.getInstance().sendInvoke(
					getStatusListenerServiceName(), "status.nav.enter", null,
					null);
		}
		mNavTxzImpl.NavigateTo(NavPlanType.NAV_PLAN_TYPE_RECOMMEND, info);
	}

	private boolean deleteHistoryNavigateInfo(NavigateInfo info) {
		setHistroyLocation(info, true);
		return true;
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
		regEvent(UiEvent.EVENT_ACTION_IM,
				UiIm.SUBEVENT_ACTION_ROOM_MEMBER_LIST_RESP);
		regEvent(UiEvent.EVENT_ACTION_IM, UiIm.SUBEVENT_ACTION_ROOM_OUT_RESP);
		regEvent(UiEvent.EVENT_ACTION_IM,
				UiIm.SUBEVENT_ACTION_ROOM_UPDATE_NOTIFY);
		
//		regCommand("NAV_CMD_ENTER");
		return super.initialize_BeforeStartJni();
	}
	
	@Override
	public int onCommand(String cmd) {
		if ("NAV_CMD_ENTER".equals(cmd)) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_NAV_OPEN");
			RecorderWin.speakTextWithClose(spk, new Runnable() {
				public void run() {
					NavThirdApp nta = getLocalNavImpl();
					if (nta != null) {
						nta.enterNav();
						AppLogic.runOnBackGround(new Runnable() {
							
							@Override
							public void run() {
							}
						}, 200);
					}
				}
			});
		}
		return 0;
	}
	
	@Override
	public int initialize_AfterStartJni() {
		// 发送初始化需要触发的事件
		return super.initialize_AfterStartJni();
	}

	@Override
	public int initialize_AfterInitSuccess() {
		// 发送初始化需要触发的事件
//		try {
//			SDKInitializer.initialize(GlobalContext.get());
//		} catch (Exception e) {
//			LogUtil.loge("Baidu SDKInitializer error:" + e.getMessage());
//		}
		AppLogic.removeBackGroundCallback(mQueryAddr);
		AppLogic.runOnBackGround(mQueryAddr, 5000);

		return super.initialize_AfterInitSuccess();
	}
	
	Runnable mQueryAddr = new Runnable() {

		@Override
		public void run() {
			NavThirdApp nta = getLocalNavImpl();
			if (nta != null) {
				JNIHelper.logd("NavManager queryHomeCompanyAddr");
				nta.queryHomeCompanyAddr();
			}
		}
	};

	boolean sUseQihooSearchTool = false;
	String sPoiSearchToolType = null;

	{
		// 检测是否安装了好搜地图，如果安装了则切换到奇虎360搜索
		checkPoiResApp();
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
	
	private boolean enableOfflineSearch(){
		return false;
	}

	// 周边商圈搜索
	public void navigateBussinessNearby(NearbyPoiSearchOption opt,
			boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_BUSSINESS_NEARBY);

		PoiSearchTool poiSearchTool;
		if (enableOfflineSearch()) {
			poiSearchTool = new ChaosPoiSearchTXZImpl(true)
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl())
							, false)
					.addPoiSearchTool(
							new PoiSearchToolGDLocalImpl(), 
							false);
		} else {
			if (sUseQihooSearchTool) {
				poiSearchTool = new PoiSearchToolTXZimpl(
						new PoiSearchToolQihooImpl());
			} else {
				poiSearchTool = new ChaosPoiSearchTXZImpl(true)
						// 优先大众点评周边搜索
						.addPoiSearchTool(
								new MaxRaduisNearPoiSearchToolTXZimpl(
										new BussinessPoiSearchDzdpImpl(),
										new BussinessPoiSearchDzdpImpl()), false)
						// 补齐百度+高德周边搜索
						.addPoiSearchTool(
								new PoiSearchToolTXZimpl(
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new PoiSearchToolGaodeImpl(),
												new PoiSearchToolGaodeImpl())),
								false)
						// 补齐离线周边搜索
						.addPoiSearchTool(
								new PoiSearchToolTXZimpl(
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new PoiSearchToolGDLocalImpl(), 
												new PoiSearchToolGDLocalImpl()),
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new PoiSearchToolMXImpl(), 
												new PoiSearchToolMXImpl())),
								false)
						// 补齐大众点评城市搜索
						.addPoiSearchTool(
								new NearToCityPoiSearchToolTXZimpl(
										new BussinessPoiSearchDzdpImpl()), false)
						// 补齐高德城市搜索
						.addPoiSearchTool(
								new NearToCityPoiSearchToolTXZimpl(
										new CityPoiSearchToolTXZimpl(
												new PoiSearchToolGaodeImpl(),
												new PoiSearchToolGaodeImpl(),
												new PoiSearchToolGaodeImpl())),
								false)
						// 补齐离线城市搜索
						.addPoiSearchTool(
								new NearToCityPoiSearchToolTXZimpl(
										new PoiSearchToolTXZimpl(
												new PoiSearchToolGDLocalImpl(), 
												new PoiSearchToolMXImpl())),
								false);
			}
		}
		JNIHelper.logd("txz poi search begin navigateBussinessNearby ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchNearby(opt,
				mTXZPoiSearchResultListener);
	}

	public void navigateBussinessNearbyCenter(CityPoiSearchOption centerOpt,
			NearbyPoiSearchOption nextOpt, boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_CENTER);

		PoiSearchTool poiSearchToolCenter;
		PoiSearchTool poiSearchToolNext;
		if (enableOfflineSearch()) {
			poiSearchToolCenter = new ChaosPoiSearchTXZImpl(true)
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl())
							, false)
					.addPoiSearchTool(
							new PoiSearchToolGDLocalImpl(), 
							false);
			poiSearchToolNext = new ChaosPoiSearchTXZImpl(true)
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl())
							, false)
					.addPoiSearchTool(
							new PoiSearchToolGDLocalImpl(), 
							false);
		} else {
			if (sUseQihooSearchTool) {
				poiSearchToolCenter = new PoiSearchToolTXZimpl(
						new PoiSearchToolQihooImpl());
				poiSearchToolNext = new PoiSearchToolTXZimpl(
						new PoiSearchToolQihooImpl());
			} else {
				//中心采用高德搜索
				poiSearchToolCenter = new CityPoiSearchToolTXZimpl(
						new PoiSearchToolGaodeImpl(), new PoiSearchToolGaodeImpl(),
						new PoiSearchToolGaodeImpl());
				poiSearchToolNext = new ChaosPoiSearchTXZImpl(true)
						// 优先采用大众点评
						.addPoiSearchTool(
								new PoiSearchToolTXZimpl(
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new BussinessPoiSearchDzdpImpl(),
												new BussinessPoiSearchDzdpImpl())),
								false)
						// 补齐百度+高德周边
						.addPoiSearchTool(
								new PoiSearchToolTXZimpl(
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new PoiSearchToolGaodeImpl(),
												new PoiSearchToolGaodeImpl())),
								false)
						// 补齐离线周边搜索
						.addPoiSearchTool(
								new PoiSearchToolTXZimpl(
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new PoiSearchToolGDLocalImpl(),
												new PoiSearchToolGDLocalImpl()),
										new MaxRaduisNearPoiSearchToolTXZimpl(
												new PoiSearchToolMXImpl(),
												new PoiSearchToolMXImpl())),
								false)
						// 补齐大众点评城市搜索
						.addPoiSearchTool(
								new NearToCityPoiSearchToolTXZimpl(
										new BussinessPoiSearchDzdpImpl()), false)
						// 补齐高德城市搜索
						.addPoiSearchTool(
								new NearToCityPoiSearchToolTXZimpl(
										new CityPoiSearchToolTXZimpl(
												new PoiSearchToolGaodeImpl(),
												new PoiSearchToolGaodeImpl(),
												new PoiSearchToolGaodeImpl())),
								false)
						// 补齐离线城市搜索
						.addPoiSearchTool(
								new NearToCityPoiSearchToolTXZimpl(
										new PoiSearchToolTXZimpl(
												new PoiSearchToolGDLocalImpl(),
												new PoiSearchToolMXImpl())), 
								false);
			}
		}
		JNIHelper.logd("txz poi search begin navigateBussinessNearbyCenter ["
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

		PoiSearchTool poiSearchTool;
		if (enableOfflineSearch()) {
			poiSearchTool = new ChaosPoiSearchTXZImpl(true)
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl())
							, false)
					.addPoiSearchTool(
							new PoiSearchToolGDLocalImpl(), 
							false);
		} else
		if (sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolTXZimpl(
					new PoiSearchToolQihooImpl());
		} else {
			poiSearchTool = new ChaosPoiSearchTXZImpl(true)
			// 优先大众点评城市搜索
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new BussinessPoiSearchDzdpImpl()), false)
					// 补齐高德城市搜索
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new CityPoiSearchToolTXZimpl(
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl())),
							false)
					// 补齐离线搜索
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolMXImpl()), 
							false);
		}
		JNIHelper.logd("txz poi search begin navigateBussinessCity ["
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

		PoiSearchTool poiSearchTool;
		if (enableOfflineSearch()) {
			poiSearchTool = new ChaosPoiSearchTXZImpl(false)
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolMXImpl())
							, false)
					.addPoiSearchTool(
							new PoiSearchToolGDLocalImpl(), 
							false);
		} else
		if (sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolTXZimpl(
					new PoiSearchToolQihooImpl());
		} else {
			poiSearchTool = new ChaosPoiSearchTXZImpl(false)
			// 优先百度+高德周边
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new MaxRaduisNearPoiSearchToolTXZimpl(
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl())),
							false)
					// 补齐离线周边搜索
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new MaxRaduisNearPoiSearchToolTXZimpl(
											new PoiSearchToolGDLocalImpl(), 
											new PoiSearchToolGDLocalImpl()),
									new MaxRaduisNearPoiSearchToolTXZimpl(
											new PoiSearchToolMXImpl(), 
											new PoiSearchToolMXImpl())), 
							false)
					// 补齐高德城市结果
					.addPoiSearchTool(
							new NearToCityPoiSearchToolTXZimpl(
									new CityPoiSearchToolTXZimpl(
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl())),
							false)
					// 补齐离线城市搜索
					.addPoiSearchTool(
							new NearToCityPoiSearchToolTXZimpl(
									new PoiSearchToolTXZimpl(
											new PoiSearchToolGDLocalImpl(),
											new PoiSearchToolMXImpl())), 
							false);
		}
		JNIHelper.logd("txz poi search begin navigateNearby ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchNearby(opt,
				mTXZPoiSearchResultListener);
	}

	public void navigateNearbyCenter(CityPoiSearchOption centerOpt,
			NearbyPoiSearchOption nextOpt, boolean manual, String action) {
		MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_ACTION,
				MonitorUtil.POISEARCH_ENTER_CENTER);

		PoiSearchTool poiSearchToolCenter;
		PoiSearchTool poiSearchToolNext;
		if(enableOfflineSearch()){
			poiSearchToolCenter = new PoiSearchToolTXZimpl(
					new PoiSearchToolGDLocalImpl(), 
					new PoiSearchToolMXImpl());
			poiSearchToolNext = new PoiSearchToolTXZimpl(
					new PoiSearchToolGDLocalImpl(), 
					new PoiSearchToolMXImpl());
		} else 
		if (sUseQihooSearchTool) {
			poiSearchToolCenter = new PoiSearchToolTXZimpl(
					new PoiSearchToolQihooImpl());
			poiSearchToolNext = new PoiSearchToolTXZimpl(
					new PoiSearchToolQihooImpl());
		} else {
			//中心使用高德城市搜索
			poiSearchToolCenter = new CityPoiSearchToolTXZimpl(
					new PoiSearchToolGaodeImpl(),
					new PoiSearchToolGaodeImpl(),
					new PoiSearchToolGaodeImpl());
			poiSearchToolNext = new ChaosPoiSearchTXZImpl(false)
			// 优先百度+高德周边
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new MaxRaduisNearPoiSearchToolTXZimpl(
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl())),
							false)
					// 补齐离线周边搜索
					.addPoiSearchTool(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(),
									new PoiSearchToolMXImpl()), 
							false)
					// 补齐高德城市结果
					.addPoiSearchTool(
							new NearToCityPoiSearchToolTXZimpl(
									new CityPoiSearchToolTXZimpl(
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl(),
											new PoiSearchToolGaodeImpl())),
							false)
					// 补齐离线城市搜索
					.addPoiSearchTool(
							new NearToCityPoiSearchToolTXZimpl(
									new PoiSearchToolTXZimpl(
											new PoiSearchToolGDLocalImpl(),
											new PoiSearchToolMXImpl())), 
							false);
		}

		JNIHelper.logd("txz poi search navigateNearbyCenter ["
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
		if (enableOfflineSearch()) {
			poiSearchTool = new PoiSearchToolTXZimpl(
					new CityPoiSearchToolTXZimpl(
							new PoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(),  
									new PoiSearchToolMXImpl()),
							new PoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(), 
									new PoiSearchToolMXImpl()),
							new PoiSearchToolTXZimpl(
									new PoiSearchToolGDLocalImpl(), 
									new PoiSearchToolMXImpl())
							)
					);
		} else 
		if (sUseQihooSearchTool) {
			poiSearchTool = new PoiSearchToolTXZimpl(
					new CityPoiSearchToolTXZimpl(new PoiSearchToolQihooImpl(),
							new PoiSearchToolQihooImpl(),
							new PoiSearchToolQihooImpl()));
		} else {
			if (navigateInfo != null
					&& isOnlyRough(navigateInfo, opt.getKeywords())) {
				poiSearchTool = new PoiSearchToolTXZimpl(
						new CityPoiSearchToolTXZimpl(
								new PoiSearchToolGaodeImpl(),
								new PoiSearchToolGaodeImpl(),
								new PoiSearchToolGaodeImpl()),
						// 离线搜索
						new CityPoiSearchToolTXZimpl(
								new PoiSearchToolTXZimpl(
										new PoiSearchToolGDLocalImpl(), 
										new PoiSearchToolMXImpl()),
								new PoiSearchToolTXZimpl(
										new PoiSearchToolGDLocalImpl(), 
										new PoiSearchToolMXImpl()),
								new PoiSearchToolTXZimpl(
										new PoiSearchToolGDLocalImpl(), 
										new PoiSearchToolMXImpl())
								));
			} else {
				poiSearchTool = new PoiSearchToolTXZimpl(
						new CityPoiSearchToolTXZimpl(
								new PoiSearchToolGaodeImpl(),
								new PoiSearchToolGaodeImpl(),
								new PoiSearchToolGaodeImpl()),
						// 离线搜索
						new CityPoiSearchToolTXZimpl(
								new PoiSearchToolTXZimpl(
										new PoiSearchToolGDLocalImpl(), 
										new PoiSearchToolMXImpl()),
								new PoiSearchToolTXZimpl(
										new PoiSearchToolGDLocalImpl(), 
										new PoiSearchToolMXImpl()),
								new PoiSearchToolTXZimpl(
										new PoiSearchToolGDLocalImpl(), 
										new PoiSearchToolMXImpl())
								));
			}
		}
		JNIHelper.logd("txz poi search begin navigateCity ["
				+ opt.getKeywords() + "] in city [" + opt.getCity()
				+ "] with tool " + poiSearchTool.toString());
		mSearchReqLastPoiSearch = poiSearchTool.searchInCity(opt,
				mTXZPoiSearchResultListener);
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
						.setNum(SelectorHelper.sSearchCount);
				mTXZPoiSearchResultListener.setOption(opt);
				navigateBussinessCity(opt, manual, action);
				return;
			}
			LocationInfo loc = LocationManager.getInstance().getLastLocation();
			if (loc == null || loc.msgGpsInfo == null
					|| loc.msgGpsInfo.dblLat == null
					|| loc.msgGpsInfo.dblLng == null) {
				String spk = NativeData
						.getResPlaceholderString(
								"RS_MAP_SEARCH_FAIL", "%CMD%", navTarName);
				mTXZSearchResultListener.showSearchError(spk);
				return;
			}

			// 周边商圈搜索
			NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
					.setCenterLat(loc.msgGpsInfo.dblLat)
					.setCenterLng(loc.msgGpsInfo.dblLng)
					.setCity(navigateInfo.strTargetCity)
					.setKeywords(navTarName)
					.setNum(SelectorHelper.sSearchCount);
			mTXZPoiSearchResultListener.setOption(opt);
			navigateBussinessNearby(opt, manual, action);
		} else if (bNear) {
			// 周边搜索
			LocationInfo loc = LocationManager.getInstance().getLastLocation();
			if (loc == null || loc.msgGpsInfo == null
					|| loc.msgGpsInfo.dblLat == null
					|| loc.msgGpsInfo.dblLng == null) {
				String spk = NativeData
						.getResPlaceholderString(
								"RS_MAP_SEARCH_FAIL", "%CMD%", navTarName);
				mTXZSearchResultListener.showSearchError(spk);
				return;
			}
			NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
					.setCenterLat(loc.msgGpsInfo.dblLat)
					.setCenterLng(loc.msgGpsInfo.dblLng)
					.setCity(navigateInfo.strTargetCity)
					.setKeywords(navTarName)
					.setNum(SelectorHelper.sSearchCount);
			mTXZPoiSearchResultListener.setOption(opt);
			navigateNearby(opt, manual, action);
		} else {
			// 城市搜索
			CityPoiSearchOption opt = new CityPoiSearchOption()
					.setCity(navigateInfo.strTargetCity)
					.setKeywords(navTarName)
					.setNum(SelectorHelper.sSearchCount);
			mTXZPoiSearchResultListener.setOption(opt);
			navigateCity(navigateInfo, opt, manual, action);
		}

		if (manual && !mSearchByEdit) {
			AppLogic.runOnUiGround(new Runnable() {
				@Override
				public void run() {
					String spk = NativeData
							.getResPlaceholderString(
									"RS_MAP_SEARCHING", 
									"%CMD%", navigateInfo.strTargetName);
					mWinProcessingPoiSearch = new WinProcessing(spk, true) {
						@Override
						public void onCancelProcess() {
							cancelAllPoiSearch();
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
			String spk = NativeData
					.getResPlaceholderString(
							"RS_MAP_SEARCHING", "%CMD%", navTarName);
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

	private byte[] preNavigateByName(JSONObject json, String action) {
		try {
			NavigateInfo navigateInfo = new NavigateInfo();
			if (json.has("city"))
				navigateInfo.strTargetCity = json.getString("city");
			navigateInfo.strTargetName = json.getString("keywords");
			if (isBussinessName(navigateInfo.strTargetName)) {
				NearbySearchInfo info = new NearbySearchInfo();
				info.strCenterCity = navigateInfo.strTargetCity;
				info.strKeywords = navigateInfo.strTargetName;
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
						UiMap.SUBEVENT_MAP_NAVIGATE_BUSSINESS, info);
				return null;
			}
			if (isNearbyName(navigateInfo.strTargetName)) {
				NearbySearchInfo info = new NearbySearchInfo();
				info.strCenterCity = navigateInfo.strTargetCity;
				info.strKeywords = navigateInfo.strTargetName;
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
		}

		if (UiEvent.EVENT_SYSTEM_MAP != eventId)
			return 0;

		switch (subEventId) {
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_HOME: {
			NavigateInfo navigateInfo;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
				if (!TextUtils.isEmpty(navigateInfo.strTargetName)) {
					navigateByName(navigateInfo, false, PoiAction.ACTION_HOME,
							false);
					break;
				}
			} catch (InvalidProtocolBufferNanoException e) {
			}
			NavigateHome();
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_COMPANY: {
			NavigateInfo navigateInfo;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
				if (!TextUtils.isEmpty(navigateInfo.strTargetName)) {
					navigateByName(navigateInfo, false,
							PoiAction.ACTION_COMPANY, false);
					break;
				}
			} catch (InvalidProtocolBufferNanoException e) {
			}
			NavigateCompany();
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_PASS_BY_NAME: {
			JNIHelper.logd("recv pass...");
			if (TextUtils.isEmpty(disableSetJTPoi())
					&& NavManager.getInstance().isNavi()) {
				mIsPass = true;
			} else {
				if (NavManager.getInstance().isNavi()) {
					AsrManager.getInstance().setNeedCloseRecord(false);
					RecorderWin.speakTextWithClose(disableSetJTPoi(), null);
					String spk = NativeData.getResString("RS_MAP_ANY_HELP");
					RecorderWin.speakTextWithClose(spk, null);
				} else {
					String answer = NativeData
							.getResString("RS_VOICE_CAN_NOT_PROC_RESULT");
					AsrManager.getInstance().setNeedCloseRecord(false);
					RecorderWin
							.speakTextWithClose(
									answer.replace(
											"%CONTENT%",
											NativeData
													.getResString("RS_VOICE_USUAL_SPEAK_GRAMMAR")),
									null);
				}
				break;
			}
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_BY_NAME: {
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

			NavigateInfo navigateInfo;
			try {
				navigateInfo = NavigateInfo.parseFrom(data);
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
				break;
			}

			if ("CURRENT_LOC".equals(navigateInfo.strTargetName)) {
				RecorderWin.open(NativeData.getResString("RS_VOICE_WHERE_DO_YOU_WANT_TO_NAVIGATE"),
						VoiceData.GRAMMAR_SENCE_NAVIGATE);
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
			mTXZSearchResultListener.reset();

			cancelAllPoiSearch();

			try {
				NearbySearchInfo info = NearbySearchInfo.parseFrom(data);
				int radius = 0;
				if (info.uint32Radius != null)
					radius = info.uint32Radius;
				String hint = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCHING", "%CMD%", info.strKeywords);
				if (radius == 0 && !TextUtils.isEmpty(info.strCenterCity)
						&& TextUtils.isEmpty(info.strCenterPoi)) {
					// 城市商圈搜索
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity(info.strCenterCity)
							.setKeywords(info.strKeywords)
							.setNum(SelectorHelper.sSearchCount);
					mTXZPoiSearchResultListener.setOption(opt);
					navigateBussinessCity(opt, false, null);
				} else {
					// 周边商圈搜索
					NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
							.setKeywords(info.strKeywords).setRadius(radius)
							.setNum(SelectorHelper.sSearchCount);
					// 判断要先进行中心搜索
					if (!TextUtils.isEmpty(info.strCenterPoi)) {
						String centerCity = info.strCenterCity;
						if (TextUtils.isEmpty(centerCity)) {
							LocationInfo loc = LocationManager.getInstance()
									.getLastLocation();
							if (loc == null || loc.msgGpsInfo == null
									|| loc.msgGeoInfo == null
									|| loc.msgGpsInfo.dblLat == null
									|| loc.msgGpsInfo.dblLng == null) {
								String spk = NativeData.getResString("RS_MAP_LOC_ERROR");
								mTXZSearchResultListener
										.showSearchError(spk);
								break;
							}
							centerCity = loc.msgGeoInfo.strCity;
						}
						CityPoiSearchOption center_opt = new CityPoiSearchOption()
								.setCity(centerCity)
								.setKeywords(info.strCenterPoi)
								.setNum(SelectorHelper.sSearchCount);
						hint = NativeData.getResString("RS_MAP_SEARCH_NEAR")
								.replace("%POI%", info.strCenterPoi)
								.replace("%KEY%", info.strKeywords);
						mTXZPoiSearchResultListener.setOption(center_opt);
						navigateBussinessNearbyCenter(center_opt, opt, false,
								null);
					} else {
						LocationInfo loc = LocationManager.getInstance()
								.getLastLocation();
						if (loc == null || loc.msgGpsInfo == null
								|| loc.msgGeoInfo == null
								|| loc.msgGpsInfo.dblLat == null
								|| loc.msgGpsInfo.dblLng == null) {
							String spk = NativeData.getResString("RS_MAP_LOC_ERROR");
							mTXZSearchResultListener.showSearchError(spk);
							break;
						}
						opt.setCenterLat(loc.msgGpsInfo.dblLat)
								.setCenterLng(loc.msgGpsInfo.dblLng)
								.setCity(loc.msgGeoInfo.strCity != null ? loc.msgGeoInfo.strCity:"");
						mTXZPoiSearchResultListener.setOption(opt);
						navigateBussinessNearby(opt, false, null);
					}
				}

				RecorderWin.addSystemMsg(hint);
				mSearchTtsId = TtsManager.getInstance().speakText(hint,
						mPoiSearchTtsCallback);
				RecorderWin.addCloseRunnable(new Runnable() {
					@Override
					public void run() {
						cancelAllPoiSearchIncludeTts();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY: {
			mTXZSearchResultListener.reset();

			cancelAllPoiSearch();

			try {
				NearbySearchInfo info = NearbySearchInfo.parseFrom(data);
				int radius = 0;
				if (info.uint32Radius != null)
					radius = info.uint32Radius;
				String hint = NativeData.getResPlaceholderString(
						"RS_MAP_SEARCHING", "%CMD%", info.strKeywords);
				if (radius == 0 && !TextUtils.isEmpty(info.strCenterCity)
						&& TextUtils.isEmpty(info.strCenterPoi)) {
					// 周边搜索
					CityPoiSearchOption opt = new CityPoiSearchOption()
							.setCity(info.strCenterCity)
							.setKeywords(info.strKeywords)
							.setNum(SelectorHelper.sSearchCount);
					mTXZPoiSearchResultListener.setOption(opt);
					navigateCity(null, opt, false, null);
				} else {
					NearbyPoiSearchOption opt = new NearbyPoiSearchOption()
							.setRadius(radius).setKeywords(info.strKeywords)
							.setNum(SelectorHelper.sSearchCount);
					// 判断要先进行中心搜索
					if (!TextUtils.isEmpty(info.strCenterPoi)) {
						CityPoiSearchOption center_opt = new CityPoiSearchOption()
								.setCity(info.strCenterCity)
								.setKeywords(info.strCenterPoi)
								.setNum(SelectorHelper.sSearchCount);
						hint = NativeData.getResString("RS_MAP_SEARCH_NEAR")
								.replace("%POI%", info.strCenterPoi)
								.replace("%KEY%", info.strKeywords);
						mTXZPoiSearchResultListener.setOption(center_opt);
						navigateNearbyCenter(center_opt, opt, false, null);
					} else {
						LocationInfo loc = LocationManager.getInstance()
								.getLastLocation();
						if (loc == null || loc.msgGpsInfo == null
								|| loc.msgGeoInfo == null
								|| loc.msgGpsInfo.dblLat == null
								|| loc.msgGpsInfo.dblLng == null) {
							String spk = NativeData.getResString("RS_MAP_LOC_ERROR");
							mTXZSearchResultListener.showSearchError(spk);
							break;
						}
						opt.setCenterLat(loc.msgGpsInfo.dblLat)
								.setCenterLng(loc.msgGpsInfo.dblLng)
								.setCity(loc.msgGeoInfo.strCity);
						mTXZPoiSearchResultListener.setOption(opt);
						navigateNearby(opt, false, null);
					}
				}

				RecorderWin.addSystemMsg(hint);
				mSearchTtsId = TtsManager.getInstance().speakText(hint,
						mPoiSearchTtsCallback);
				RecorderWin.addCloseRunnable(new Runnable() {
					@Override
					public void run() {
						cancelAllPoiSearchIncludeTts();
					}
				});
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_MODIFY_HOME: {
			ModifyHome();
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_MODIFY_COMPANY: {
			ModifyCompany();
			break;
		}
		case com.txz.ui.map.UiMap.SUBEVENT_MAP_NAVIGATE_BY_GPS: {
			try {
				NavigateTo(NavigateInfo.parseFrom(data));
			} catch (InvalidProtocolBufferNanoException e) {
				e.printStackTrace();
			}
			break;
		}
		}

		return super.onEvent(eventId, subEventId, data);
	}

	public boolean isNavi() {
		if (hasRemoteProcTool()) {
			return mRemoteNavToolisInNav;
		}
		INav nav = getLocalNavImpl();
		if (nav != null)
			return nav.isInNav();
		return false;
	}

	public boolean isInited() {
		if (hasRemoteProcTool()) {
			return true;
		}
		INav nav = getLocalNavImpl();
		if (nav != null && nav instanceof NavTxzImpl)
			return mIsInited;
		return true;
	}

	public boolean hasStatusListener() {
		return TextUtils.isEmpty(mNavStatusListenerServiceName) ? false : true;
	}

	public String getStatusListenerServiceName() {
		return mNavStatusListenerServiceName;
	}

	private String mNavToolServiceName;
	private String mNavStatusListenerServiceName;
	private String mLocalNavType = null;
	private boolean mRemoteNavToolisInNav;
	private boolean mEnableExitAllNav = true;

	private String mLastNavTool;
	public static String sDefaultNavTool;
	
	public NavThirdApp getLocalNavImpl() {
		String type = mLocalNavType;
		
		{ // 设置默认的导航工具不为空优先使用
			String navTool = ProjectCfg.getDefaultNavTool();
			if (!TextUtils.isEmpty(navTool) && navTool.equals(mLastNavTool) && !TextUtils.isEmpty(sDefaultNavTool)) {
				type = sDefaultNavTool;
			} else {
				if (!TextUtils.isEmpty(navTool)) {
					sDefaultNavTool = getNavToolByType(navTool);
					if (!TextUtils.isEmpty(sDefaultNavTool)) {
						type = sDefaultNavTool;
					}
				} else {
					sDefaultNavTool = "";
				}

				JNIHelper.logd("fixCallFunction defaultNavTool:" + sDefaultNavTool);
			}
			mLastNavTool = navTool;
		}
		
		if (!TextUtils.isEmpty(type) && !PackageManager.getInstance().checkAppExist(type)) {
			type = null;
			
		}
		
		if(TextUtils.isEmpty(type)){
			for(String nav:mInnerSuppNavs){
				if(PackageManager.getInstance().checkAppExist(nav)){
					type = nav;
					break;
				}
			}
		}
		if (!TextUtils.isEmpty(type)) {
			if (type.startsWith(NavCldImpl.PACKAGE_NAME_REG)) {
				type = NavCldImpl.PACKAGE_NAME_REG;
			} else if (type.startsWith(NavAmapAutoNavImpl.PACKAGE_PREFIX)) {
				type = NavAmapAutoNavImpl.PACKAGE_PREFIX;
			}
		}
		JNIHelper.logd(" NavThirdApp type:" + type);

		synchronized (mNavAppMap) {
			return mNavAppMap.get(type);
		}
	}

	public boolean showTraffic(final String city, final String addr) {
		final NavThirdApp nta = getLocalNavImpl();
		if (nta != null 
				&& (nta instanceof NavAmapAutoNavImpl)) {
			if (!nta.isInNav() || !nta.isInFocus()) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				String spk = NativeData.getResString("RS_MAP_OPEN");
				RecorderWin.speakTextWithClose(spk, new Runnable() {

					@Override
					public void run() {
//						if(nta instanceof NavBaiduDeepImpl){
//							nta.showTraffic(city, addr);
//							return;
//						}
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
		return !TextUtils.isEmpty(mNavToolServiceName) && !ProjectCfg.isFixCallFunction();
	}

	public String getDisableResaon() {
		if (hasRemoteProcTool())
			return "";
		NavThirdApp nav = NavManager.getInstance().getLocalNavImpl();
		if (nav != null
				&& PackageManager.getInstance().checkAppExist(
						nav.getPackageName()))
			return "";
		return NativeData.getResString("RS_VOICE_NO_NAV_TOOL");
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
			if (serviceName.equals(mNavToolServiceName)) {
				invokeTXZNav(null, "settool", "TXZ".getBytes());
			}
			if (serviceName.equals(mNavStatusListenerServiceName)) {
				invokeTXZNav(null, "setStatusListener", "TXZ".getBytes());
			}
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

	public void exitAllNavTool() {
		if (hasRemoteProcTool()) {
			NavManager.getInstance().invokeTXZNav(null, "exitNav", null);
			// return; 应退出所有打开的导航
			if(!mEnableExitAllNav){
				return;
			}
		}
		if (hasStatusListener()) {
			ServiceManager.getInstance().sendInvoke(
					getStatusListenerServiceName(), "status.nav.exit", null,
					null);
		}
		synchronized (mNavAppMap) {
			// 退出同行者导航
			mNavTxzImpl.exitNav();
			// 退出所有导航工具
			try {
				JNIHelper.logd("===start check exitAll navTool");
				for (NavThirdApp tool : mNavAppMap.values()) {
					if (!PackageManager.getInstance().checkAppExist(
							tool.getPackageName())) {
						continue;
					}
					tool.exitNav();

					JNIHelper.logd("exit " + tool.getPackageName());
				}
			} catch (Exception e) {

			}
		}
	}
	
	private byte[] invokeTxzNavOption(String packageName, String command, byte[] data) {
		Set<Entry<String, NavThirdApp>> navSets = mNavAppMap.entrySet();
		for (Entry<String, NavThirdApp> entry : navSets) {
//			if (PackageManager.getInstance().checkAppExist(entry.getKey())) {
			entry.getValue().invokeTXZNav(packageName, command, data);
//			}
		}
		return null;
	}
	
	private String getNavToolByType(String type) {
		String navType = "";
		if (type.equals("BAIDU_MAP")) {
//			navType = NavBaiduMapImpl.PACKAGE_NAME;
		}
		if (type.equals("BAIDU_NAV")) {
			// mLocalNavType = NavBaiduNavImpl.PACKAGE_NAME;
//			navType = BaiduVersion.refeshPackageName();
		}
		if (type.equals("BAIDU_NAV_HD")) {
//			navType = NavBaiduNavHDImpl.PACKAGE_NAME;
		}
		if (type.equals("GAODE_MAP")) {
			navType = NavAmapImpl.PACKAGE_NAME;
		}
		if (type.equals("GAODE_MAP_CAR")) {
			navType = NavAmapAutoNavImpl.PACKAGE_NAME;
			if (!PackageManager.getInstance().checkAppExist(mLocalNavType)) {
				navType = NavAmapAutoNavImpl.PACKAGE_NAME_LITE;
			}
		}
		if (type.equals("GAODE_NAV")) {
			navType = NavAutoNavImpl.PACKAGE_NAME;
			if (!PackageManager.getInstance().checkAppExist(mLocalNavType)) {
				navType = NavAmapAutoNavImpl.PACKAGE_NAME;
			}
		}
		if (type.equals("KAILIDE_NAV")) {
			// mLocalNavType = NavCldImpl.PACKAGE_NAME;
			navType = mNavCldImpl.getPackageName();
		}
		if (type.equals("TXZ")) {
			navType = ServiceManager.NAV;
		}
		if (type.equals("MX_NAV")) {
			navType = NavMXImpl.MX_PACKAGE_NAME;
		}
		if (type.equals("DDT_NAV")) {
			navType = NavDdtImpl.DDT_PACKAGE_NAME;
		}
		if (type.equals("QIHOO_NAV")) {
			navType = NavQihooImpl.PACKAGE_NAME;
		}
		if (type.equals("TX_NAV")) {
//			navType = NavTXNavImpl.PACKAGE_NAME;
		}
		return navType;
	}

	public byte[] invokeTXZNav(final String packageName, String command,
			byte[] data) {
		if (command.startsWith("asr.key.")) {
			return invokeTxzNavOption(packageName, command.substring("asr.key.".length()), data);
		}
		if (command.startsWith("remote.")) {
			return invokeTxzNavOption(packageName, command.substring("remote.".length()), data);
		}
		if (command.startsWith("app.")) {
			return invokeTxzNavOption(packageName, command.substring("app.".length()), data);
		}
		if (command.startsWith("enablecmd") 
				|| command.equals("enableWakeupExit") 
				|| command.equals("forceRegister")
				|| command.equals("enableWakeupNav")) {
			return invokeTxzNavOption(packageName, command, data);
		}
		if (command.equals("isInNav")) {
			return ("" + isNavi()).getBytes();
		}
		if (command.equals("notifyNavStatus")) {
			mRemoteNavToolisInNav = Boolean.parseBoolean(new String(data));
			return null;
		}
		if (command.equals("notifyExitAllNav")) {
			mEnableExitAllNav = Boolean.parseBoolean(new String(data));
			JNIHelper.logd("mEnableExitAllNav:" + mEnableExitAllNav);
			return null;
		}
		if (command.equals("inner.notifyNavStatus")) {
			boolean b = Boolean.parseBoolean(new String(data));
			NavTxzImpl.setInNav(b);
			return null;
		}
		if (command.equals("inner.notifyInitStatus")) {
			mIsInited = Boolean.parseBoolean(new String(data));
			return null;
		}
		if (command.equals("poi.finish")) {
			try {
				mPoiActivityDismissDelay = Long.parseLong(new String(data));
				SelectorHelper.updateAutoDismissDelay(mPoiActivityDismissDelay);
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
			mNavToolServiceName = null;
			ServiceManager.getInstance().removeConnectionListener(
					mConnectionListener);
			if (data != null) {
				String type = new String(data);
				JNIHelper.logd(packageName + " set nav tool type: " + type);
				String navtool = getNavToolByType(type);
				if (!TextUtils.isEmpty(navtool)) {
					mLocalNavType = navtool;
					// 查询该导航的家和公司的地址
					NavThirdApp nta = getLocalNavImpl();
					if (nta != null) {
						nta.queryHomeCompanyAddr();
					}
					return null;
				}
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
									mNavToolServiceName = packageName;
							}
						});
			}
			return null;
		}
		if (command.equals("setStatusListener")) {
			mNavStatusListenerServiceName = null;
			ServiceManager.getInstance().removeConnectionListener(
					mConnectionListener);
			ServiceManager.getInstance().addConnectionListener(
					mConnectionListener);
			ServiceManager.getInstance().sendInvoke(packageName, "", null,
					new GetDataCallback() {
						@Override
						public void onGetInvokeResponse(ServiceData data) {
							// 记录工具
							if (data != null)
								mNavStatusListenerServiceName = packageName;
						}
					});
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
							"RS_MAP_NAV_RECEICE", "%CMD%", info.strTargetName)+ "("
							+ info.strTargetAddress + ")";
				}
				if (TextUtils.isEmpty(hint)) {
					hint = NativeData.getResPlaceholderString("RS_MAP_NAV_CANCEL", "%CMD%", message);
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
		
		if (packageName == null || !ProjectCfg.isFixCallFunction()) {
			if (!TextUtils.isEmpty(mNavToolServiceName)) {
				ServiceManager.getInstance().sendInvoke(mNavToolServiceName,
						"tool.nav." + command, data, null);
				return null;
			}
		}
		
		if (command.equals("navTo")) {
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
				NavigateTo(info);
			} catch (Exception e) {
			}
			return null;
		}

		if (command.equals("enterNav")) {
			NavThirdApp nav = getLocalNavImpl();
			if (nav != null) {
				if (PackageManager.getInstance().checkAppExist(
						nav.getPackageName())) {
					if (hasStatusListener()) {
						ServiceManager.getInstance().sendInvoke(
								getStatusListenerServiceName(),
								"status.nav.enter", null, null);
					}
					nav.enterNav();
					return null;
				}
			}
			TtsManager.getInstance().speakText(
					NativeData.getResString("RS_VOICE_NO_NAV_TOOL"));
			return null;
		}

		if (command.equals("exitNav")) {
			NavThirdApp nav = getLocalNavImpl();
			if (nav != null) {
				if (PackageManager.getInstance().checkAppExist(
						nav.getPackageName())) {
					if (hasStatusListener()) {
						ServiceManager.getInstance().sendInvoke(
								getStatusListenerServiceName(),
								"status.nav.exit", null, null);
					}
					nav.exitNav();
					return null;
				}
			}
			return null;
		}

		return null;
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
		}
		return null;
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
}