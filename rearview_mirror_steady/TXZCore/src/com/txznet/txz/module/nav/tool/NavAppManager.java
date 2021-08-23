package com.txznet.txz.module.nav.tool;

import android.os.SystemClock;
import android.text.TextUtils;

import com.txz.report_manager.ReportManager;
import com.txz.ui.app.UiApp;
import com.txz.ui.app.UiApp.AppInfoList;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.ConnectionListener;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.remote.util.ReportUtil.Report;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager.NavStatusListener;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.choice.list.PoiWorkChoice;
import com.txznet.txz.component.nav.INav.IInitCallback;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.remote.RemoteNavImpl;
import com.txznet.txz.component.nav.txz.NavCommImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.MyInstallReceiver;
import com.txznet.txz.module.app.MyInstallReceiver.InstallObservable.InstallObserver;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.help.HelpGuideManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavInscriber;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable2;
import com.txznet.txz.util.runnables.Runnable3;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class NavAppManager {
	public static final int START_AND_FINISH_TIMEOUT = 20 * 1000;
	private static final boolean DEBUG = true;

	private static class LayzerHolder {
		public final static NavAppManager sInstance = new NavAppManager();
	}

	public static class BrandNavObj {
		public int arrayIndex;
		public String brandName;
		public List<String> appIds;
		public String appClsInstance;
		public String truePkgName;
		public boolean hasInited;
	}

	public static class NavAppBean {
		public static final int TYPE_LOCAL_NAV = 0;
		public static final int TYPE_REMOTE_NAV = 1;

		/**
		 * 导航类型
		 */
		public int navType = TYPE_LOCAL_NAV;
		/**
		 * 可选, 应用入口的Activity名
		 */
		private String strActivityName;
		/**
		 * 应用的可读名(Label), 必填
		 */
		public String strAppName;
		/**
		 * 应用的包名, 必填
		 */
		public String strPackageName;
		/**
		 * 可选
		 */
		private String strParams;
	}

	// 是否已经初始化完所有导航
	private volatile boolean mInited;
	private int mInitCount = 0;
	// 是否启用当前导航
	private boolean mUseActiveNav = false;

	// 当前正在使用的导航
	private String mCurrActivePkn = null;
	private String mSetNavType = null;
	// 远程导航工具
	private RemoteNavImpl mRemoteServiceNav = null;
	private Object mLockObj = new Object();
	private List<String> mNavToolStatusServiceNames = null;
	// 导航命令字注册工具
	private NavCmdRegister mNavCmdRegister = new NavCmdRegister();

	/** 集合 **/
	private List<NavAppBean> mNavApps = new ArrayList<NavAppBean>();
	private List<String> mAppsLevelList = new ArrayList<String>();
	private List<Runnable> mDelayTaskList = new ArrayList<Runnable>();
	private List<NavThirdApp> mTmpApps = new ArrayList<NavThirdApp>();
	private List<BrandNavObj> mTmpBrandNavObjs = new ArrayList<BrandNavObj>();
	private Map<String, NavThirdApp> mAppsMap = new HashMap<String, NavThirdApp>();

	public static NavAppManager getInstance() {
		return LayzerHolder.sInstance;
	}

	public void setMCurrActivePkn (String mCurrActivePkn){
		this.mCurrActivePkn = mCurrActivePkn;
	}

	private NavStatusListener mStatusListener = new NavStatusListener() {

		@Override
		public void onPlanSucc(String navPkn) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onPlanSucc:" + navPkn);
			}
//			onNavPlanSucc(navPkn);

			invokeRemoteStatus("status.nav.planSucc", navPkn.getBytes());
		}

		@Override
		public void onPlanFail(String navPkn, int errorCode, String reason) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onPlanFail:" + navPkn);
			}
//			onNavPlanError(navPkn);

			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("navPkn", navPkn);
			jsonBuilder.put("errorCode", errorCode);
			jsonBuilder.put("reason", reason);

			invokeRemoteStatus("status.nav.planFail", jsonBuilder.toBytes());
		}

		@Override
		public void onStart(String navPkg) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onStart:" + navPkg);
			}
			mCurrActivePkn = navPkg;
			if (mNavCmdRegister != null) {
				mNavCmdRegister.onStart(navPkg);
			}

			onNavStart(navPkg);

			invokeRemoteStatus("status.nav.start", navPkg.getBytes());
		}

		@Override
		public void onForeground(String pkn, boolean isForeground) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onForeground pkn:" + pkn + ",isForeground:" + isForeground);
			}
			if (isForeground) {
				mCurrActivePkn = pkn;
				if (mNavCmdRegister != null) {
					mNavCmdRegister.onForeground(pkn);
				}
				onNavForeground(pkn);
				invokeRemoteStatus("status.nav.foreground", pkn.getBytes());
			} else {
				if (mNavCmdRegister != null) {
					mNavCmdRegister.onBackground(pkn);
				}
				invokeRemoteStatus("status.nav.background", pkn.getBytes());
			}
		}

		@Override
		public void onExit(String pkn) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onExit:" + pkn);
			}
			mCurrActivePkn = null;
			if (mNavCmdRegister != null) {
				mNavCmdRegister.onExitApp(pkn);
			}
			// 导航计数
			HelpGuideManager.getInstance().notifyCloseNav();

			onNavEnd(pkn);
		}

		@Override
		public void onEnter(String pkn) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onEnter:" + pkn);
			}
		}

		@Override
		public void onEnd(String navPkg) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onEnd:" + navPkg);
			}
			if (mNavCmdRegister != null) {
				mNavCmdRegister.onEnd(navPkg);
			}

			onNavEnd(navPkg);

			invokeRemoteStatus("status.nav.end", navPkg.getBytes());
		}

		@Override
		public void onBeginNav(String pkn, Poi poi) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onBeginNav:" + pkn);
			}
		}

		@Override
		public void onStatusUpdate(String pkn) {
			if (mNavCmdRegister != null) {
				mNavCmdRegister.onStatusUpdate(pkn);
			}
		}

		public void onDefaultNavHasSeted(String pkn) {
		}

		@Override
		public void onTtsStartOrEnd(String pkn, boolean isTts) {
			if (DEBUG) {
				LogUtil.logd("NavAppManager onTtsStartOrEnd:" + pkn+",isTts:"+isTts);
			}

			if (isTts) {
				mCurrActivePkn = pkn;
				if (mNavCmdRegister != null) {
					mNavCmdRegister.onTtsStart(pkn);
				}
				invokeRemoteStatus("status.nav.ttsBegin", pkn.getBytes());
			} else {
				if (mNavCmdRegister != null) {
					mNavCmdRegister.onTtsEnd(pkn);
				}
				invokeRemoteStatus("status.nav.ttsEnd", pkn.getBytes());
			}
		}


	};

	Runnable mInitAppTask = new Runnable() {

		@Override
		public void run() {
			reset();
			createAppsInfo();
			initializeApps();
		}
	};

	public void init(boolean isForce) {
		if (isInit() && !isForce) {
			return;
		}
		registerObserver();
		AppLogic.removeBackGroundCallback(mInitAppTask);
		AppLogic.runOnBackGround(mInitAppTask, 0);
	}
	
	private void registerObserver() {
		try {
			MyInstallReceiver.SINSTALL_OBSERVABLE.registerObserver(new InstallObserver() {

				@Override
				public void onApkUnInstall(String packageName) {
					onApkInstallable(false, packageName);
				}

				@Override
				public void onApkInstall(String packageName) {
					onApkInstallable(true, packageName);
				}
			});
		} catch (Exception e) {
		}
	}

	private void onApkInstallable(boolean isInstall, String navPkn) {
		BrandNavObj navObj = null;
		for (int i = 0; i < mTmpBrandNavObjs.size(); i++) {
			BrandNavObj obj = mTmpBrandNavObjs.get(i);
			List<String> ids = obj.appIds;
			boolean found = false;
			for (String id : ids) {
				if (navPkn.equals(id) || Pattern.matches(id, navPkn)) {
					found = true;
					navObj = obj;
					break;
				}
			}
			if (found) {
				break;
			}
		}
		if (isInstall) {
			if (navObj != null) {
				try {
					final BrandNavObj brandNavObj = navObj;
					final NavThirdApp nta = (NavThirdApp) Class.forName(navObj.appClsInstance).newInstance();
					nta.setNavStatusListener(mStatusListener);
					nta.setPackageName(navObj.truePkgName);
					nta.initialize(new IInitCallback() {

						@Override
						public void onInit(boolean bSuccess) {
							if (bSuccess) {
								String navPkn = nta.getPackageName();
								synchronized (mAppsMap) {
									mAppsMap.put(navPkn, nta);
									if (DEBUG) {
										LogUtil.logd("installobserver NavAppManager appMap put:" + navPkn);
									}

									nta.setNavStatusListener(mStatusListener);
								}
								mAppsLevelList.add(navPkn);
								brandNavObj.truePkgName = navPkn;
								brandNavObj.hasInited = true;
							}
						}
					});
				} catch (Exception e) {
				}
			}
			if (mCurrThirdNavPkn != null && mCurrThirdNavPkn.equals(navPkn)){
				if (DEBUG) {
					LogUtil.logd("installobserver NavAppManager appMa:" + navPkn);
				}
				installTXZCommNav();
			}
		} else {
			if (navObj != null) {
				navObj.truePkgName = null;
				navObj.hasInited = false;
			}
			synchronized (mAppsMap) {
				NavThirdApp nta = mAppsMap.remove(navPkn);
				if (nta != null) {
					nta.release();
				}
				LogUtil.logd("remove navPkn:" + navPkn + "," + nta);
			}
			mAppsLevelList.remove(navPkn);
		}
	}

	/**
	 * 加载导航配置信息到内存
	 */
	private void createAppsInfo() {
		List<BrandNavObj> navObjs = loadAndExtractBean();
		if (DEBUG) {
			LogUtil.logd("NavAppManager load size:" + navObjs.size());
		}

		synchronized (mTmpBrandNavObjs) {
			mTmpBrandNavObjs.addAll(navObjs);
			Collections.sort(mTmpBrandNavObjs, new Comparator<BrandNavObj>() {

				@Override
				public int compare(BrandNavObj lhs, BrandNavObj rhs) {
					if (lhs.arrayIndex < rhs.arrayIndex) {
						return -1;
					} else if (lhs.arrayIndex > rhs.arrayIndex) {
						return 1;
					}
					return 0;
				}
			});
		}
	}

	/**
	 * 重置
	 */
	private void reset() {
		mInited = false;
		mInitCount = 0;
		mTmpApps.clear();
		mAppsLevelList.clear();
		mTmpBrandNavObjs.clear();
	}

	//当前支持标准导航协议的第三方导航工具包名
	private String mCurrThirdNavPkn = null;

	// 加入同行者外部导航工具
	private void installTXZCommNav() {
		final NavThirdApp nta = new NavCommImpl();
		nta.initialize(new IInitCallback() {

			@Override
			public void onInit(boolean bSuccess) {
				String pkn = nta.getPackageName();
				mCurrThirdNavPkn = pkn;
				LogUtil.logd("installTXZCommNav onInit:" + bSuccess + "," + pkn);
				synchronized (mAppsMap) {
					mAppsMap.put(pkn, nta);
					if (DEBUG) {
						LogUtil.logd("NavAppManager appMap put:" + pkn);
					}

					nta.setNavStatusListener(mStatusListener);
				}

				synchronized (mTmpBrandNavObjs) {
					for (BrandNavObj obj : mTmpBrandNavObjs) {
						if ("TXZ_COMM".equals(obj.brandName)) {
							obj.truePkgName = pkn;
							obj.hasInited = true;
							mAppsLevelList.add(pkn);
							break;
						}
					}
				}
			}
		});
	}

	private void initializeApps() {
		if (mInited) {
			return;
		}

		final AppInfoList apps = PackageManager.getInstance().getAppList();
		for (BrandNavObj navObj : mTmpBrandNavObjs) {
			List<String> ids = navObj.appIds;
			if (ids == null || ids.isEmpty()) {
				continue;
			}

			boolean found = false;
			for (String id : ids) {
				for (UiApp.AppInfo info : apps.rptMsgApps) {
					if (info.strPackageName.equals(id) || Pattern.matches(id, info.strPackageName)) {
						LogUtil.logd("NavAppManager packageName:" + info.strPackageName + " hasInit");
						found = true;
						navObj.truePkgName = info.strPackageName;
						break;
					}
				}

				if (found) {
					try {
						NavThirdApp nta = (NavThirdApp) Class.forName(navObj.appClsInstance).newInstance();
						nta.setNavStatusListener(mStatusListener);
						// 设置真实的包名
						nta.setPackageName(navObj.truePkgName);
						mTmpApps.add(nta);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				}
			}
		}
		
		for (final NavThirdApp nta : mTmpApps) {
			if (nta != null) {
				nta.initialize(new IInitCallback() {

					@Override
					public void onInit(final boolean bSuccess) {
						AppLogic.runOnBackGround(new Runnable() {

							@Override
							public void run() {
								tool_init_end(bSuccess, nta);
								checkInitComplete();
							}
						}, 0);
					}
				});
			}
		}
		if (mTmpApps.size() == 0) {
			checkInitComplete();
		}
	}
	
	private void onNavStart(String navPkn) {
		// 记录导航开始
		startRecordANav(navPkn);
	}

	private void onNavEnd(String navPkn) {
		endRecordANav(navPkn);
	}
	
	private void onNavPlanSucc(String navPkn) {
		// 路径规划成功重新倒计时
		setLastVoiceDestination(mLastNavigateInfo);
	}

	private void onNavPlanError(String navPkn) {

	}

	////////////////////////////////////////////////导航行为///////////////////////////////////////////
	
	private class NavRecord {
		public static final String SOURCE_NAV = "nav";
		public static final String SOURCE_VOICE = "voice";
		// 标记目的地信息是来源导航还是发起导航的目的地
		public String originalFrom = SOURCE_NAV;
		public long navStartTime;
		public PathInfo pathInfo;
	}

	Map<String, NavRecord> mNavRecords = new HashMap<String, NavRecord>();
	
	private Runnable mNavRecordTask = null;
	
	private void copyFromNavigateInfo(PathInfo info, NavigateInfo info2) {
		info.toPoiLat = info2.msgGpsInfo.dblLat;
		info.toPoiLng = info2.msgGpsInfo.dblLng;
		info.toCity = info2.strTargetCity;
		info.toPoiName = info2.strTargetName;
		info.toPoiAddr = info2.strTargetAddress;
		LocationInfo info3 = LocationManager.getInstance().getLastLocation();
		if (info3 != null) {
			if (info3.msgGpsInfo != null) {
				info.fromPoiLat = info3.msgGpsInfo.dblLat;
				info.fromPoiLng = info3.msgGpsInfo.dblLng;
			}
			if (info3.msgGeoInfo != null) {
				info.fromPoiAddr = info3.msgGeoInfo.strAddr;
			}
		}
	}
	
	public void startRecordANav(String navPkn) {
		LogUtil.logd("startRecordANav:" + navPkn);
		NavRecord navRecord = new NavRecord();
		navRecord.navStartTime = NativeData.getMilleServerTime().uint64Time;
		NavThirdApp nta = getNavToolByName(navPkn);
		if(nta == null){
			LogUtil.logd("getNavToolByName == null");
			return;
		}
		navRecord.pathInfo = nta.getCurrentPathInfo();
		if (navRecord.pathInfo == null) {
			if (mLastNavigateInfo == null || mLastNavigateInfo.msgGpsInfo == null) {
				LogUtil.logd("navRecord.pathInfo == null");
				return;
			}

			navRecord.pathInfo = new PathInfo();
			navRecord.originalFrom = NavRecord.SOURCE_VOICE;
			copyFromNavigateInfo(navRecord.pathInfo, mLastNavigateInfo);
			LogUtil.logd("use voice destination");
		}
		
		boolean voiceSame = false;
		if (mLastNavigateInfo != null) {
			double sLat = mLastNavigateInfo.msgGpsInfo.dblLat;
			double sLng = mLastNavigateInfo.msgGpsInfo.dblLng;
			double dLat = navRecord.pathInfo.toPoiLat;
			double dLng = navRecord.pathInfo.toPoiLng;
			voiceSame = NavInscriber.getInstance().isSameDestinationLatLng(sLat, sLng, dLat, dLng);
			if (voiceSame) {
				// 覆盖其中一些字段，防止导航给出的字段是空值，需要重新逆地理编码
				if (TextUtils.isEmpty(navRecord.pathInfo.toPoiName)) {
					navRecord.pathInfo.toPoiName = mLastNavigateInfo.strTargetName;
					navRecord.pathInfo.toCity = mLastNavigateInfo.strTargetCity;
					navRecord.pathInfo.toPoiAddr = mLastNavigateInfo.strTargetAddress;
				}
			}
			setLastVoiceDestination(null);
		}
		
		// 覆盖可能出现的空值
		if (navRecord.pathInfo != null) {
			do {
				LocationInfo info = LocationManager.getInstance().getLastLocation();
				if (info == null) {
					break;
				}
				if (info.msgGeoInfo != null) {
					if (TextUtils.isEmpty(navRecord.pathInfo.fromPoiAddr)) {
						navRecord.pathInfo.fromPoiAddr = info.msgGeoInfo.strAddr;
					}
					if (TextUtils.isEmpty(navRecord.pathInfo.fromPoiName)) {
						String poiName = info.msgGeoInfo.strAddr;
						if (info.msgGeoInfo.strProvice != null) {
							poiName = poiName.replace(info.msgGeoInfo.strProvice, "");
						}
						if (info.msgGeoInfo.strCity != null) {
							poiName = poiName.replace(info.msgGeoInfo.strCity, "");
						}
						if (info.msgGeoInfo.strDistrict != null) {
							poiName = poiName.replace(info.msgGeoInfo.strDistrict, "");
						}
						if (info.msgGeoInfo.strStreet != null) {
							poiName = poiName.replace(info.msgGeoInfo.strStreet, "");
						}
						navRecord.pathInfo.fromPoiName = poiName;
					}
				}
				if (info.msgGpsInfo == null) {
					break;
				}
				if (navRecord.pathInfo.fromPoiLng == 0) {
					navRecord.pathInfo.fromPoiLng = info.msgGpsInfo.dblLng;
				}
				if (navRecord.pathInfo.fromPoiLat == 0) {
					navRecord.pathInfo.fromPoiLat = info.msgGpsInfo.dblLat;
				}
			} while (false);
		}

		mNavRecordTask = new Runnable3<NavRecord, String, Boolean>(navRecord, navPkn, voiceSame) {
			private boolean hasRun;

			@Override
			public void run() {
				AppLogic.removeBackGroundCallback(mNavRecordTask);
				mNavRecordTask = null;
				if (hasRun) {
					return;
				}
				hasRun = true;

				synchronized (mNavRecords) {
					if (mNavRecords.containsKey(mP2)) {
						mNavRecords.remove(mP2);
					}

					mNavRecords.put(mP2, mP1);
					LogUtil.logd(mP2 + " put record " + mP1);
					final JSONBuilder jsonBuilder = new JSONBuilder();
					jsonBuilder.put("scene", "nav");
					jsonBuilder.put("action", "startNav");
					jsonBuilder.put("startTime", mP1.navStartTime);
					jsonBuilder.put("pathInfo", getStartPathInfo(mP1.pathInfo));
					jsonBuilder.put("originalFrom", mP1.originalFrom);
					jsonBuilder.put("navPkn", mP2);
					if (mP3) {
						jsonBuilder.put("origin", "voice");
						jsonBuilder.put("sessionId", ReportUtil.mSessionId);
					} else {
						jsonBuilder.put("origin", "touch");
					}
					doReport(jsonBuilder);
					LogUtil.logd("nav start doReport:" + jsonBuilder.toString());

					NavInscriber.getInstance().addRecord(mP1.pathInfo);
				}
			}
		};
		boolean bWait = NavInscriber.getInstance().reverseGeocode(navRecord.pathInfo, mNavRecordTask);

		if (bWait) {
			AppLogic.runOnBackGround(mNavRecordTask, 5 * 1000);
		} else {
			mNavRecordTask.run();
		}
	}
	
	private String getStartPathInfo(PathInfo info) {
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("toPoiLat", info.toPoiLat);
			jsonObject.put("toPoiLng", info.toPoiLng);
			jsonObject.put("toPoiAddr", info.toPoiAddr);
			jsonObject.put("toPoiName", info.toPoiName);
			jsonObject.put("toCity", info.toCity);
			jsonObject.put("totalDistance", info.totalDistance);
			jsonObject.put("totalTime", info.totalTime);
		} catch (Exception e) {
		}
		return jsonObject.toString();
	}
	
	private NavigateInfo mLastNavigateInfo;

	/**
	 * 记录从NavManager发起的导航，用于区分语音和手动发起的导航
	 * 
	 * @param info
	 */
	public void setLastVoiceDestination(NavigateInfo info) {
		mLastNavigateInfo = info;
		AppLogic.removeBackGroundCallback(mRemoveVoiceNav);
		AppLogic.runOnBackGround(mRemoveVoiceNav, START_AND_FINISH_TIMEOUT);// 10S没有规划成功的话，应该属于手动发起的导航了
	}
	
	Runnable mRemoveVoiceNav = new Runnable() {

		@Override
		public void run() {
			LogUtil.logd("mLastNavigateInfo set null");
			mLastNavigateInfo = null;
		}
	};
	
	/**
	 * 获取发起导航前所做的选择
	 * 
	 * @return
	 */
	private String getVoiceNavParams() {
		JSONBuilder jsonBuilder = ChoiceManager.getInstance().getLastReport();
		if (jsonBuilder != null) {
			String id = jsonBuilder.getVal(PoiWorkChoice.KEY_TYPE, String.class);
			if (TextUtils.isEmpty(id)) {
				return null;
			}

			String bTime = jsonBuilder.getVal(PoiWorkChoice.KEY_REPORT_TIME, String.class);
			if (!TextUtils.isEmpty(bTime)) {
				long t = SystemClock.elapsedRealtime();
				long beginTime = Long.parseLong(bTime);
				LogUtil.logd("t:" + t + ",beginTime:" + beginTime);
				if (t - beginTime <= START_AND_FINISH_TIMEOUT) {
					// 认为是声控发起的导航
					return jsonBuilder.toString();
				}
			}
		}
		return null;
	}
	
	public void endRecordANav(String navPkn) {
		synchronized (mNavRecords) {
			NavRecord navRecord = mNavRecords.remove(navPkn);
			if (navRecord != null) {
				long st = navRecord.navStartTime;
				long t = NativeData.getMilleServerTime().uint64Time;
				final JSONBuilder jsonBuilder = new JSONBuilder();
				jsonBuilder.put("scene", "nav");
				jsonBuilder.put("action", "endNav");
				jsonBuilder.put("startTime", st);
				jsonBuilder.put("endTime", t);
				jsonBuilder.put("navPkn", navPkn);
				jsonBuilder.put("sessionId", ReportUtil.mSessionId);
				if (navRecord.pathInfo != null) {
					jsonBuilder.put("pathInfo", navRecord.pathInfo.toString());
				}
				doReport(jsonBuilder);
				LogUtil.logd("nav end doReport:" + jsonBuilder.toString());
			}
		}
	}
	
	private void doReport(final JSONBuilder jsonBuilder) {
		if (jsonBuilder != null) {
			LocationInfo info = LocationManager.getInstance().getLastLocation();
			if (info != null && info.msgGpsInfo != null) {
				jsonBuilder.put("currAddr", info.msgGeoInfo.strAddr);
				jsonBuilder.put("currLat", info.msgGpsInfo.dblLat);
				jsonBuilder.put("currLng", info.msgGpsInfo.dblLng);
			}
		}
		ReportUtil.doReport(new Report() {

			@Override
			public int getType() {
				return ReportManager.UAT_COMMON;
			}

			@Override
			public String getData() {
				return jsonBuilder.toString();
			}
		});
	}
	
	/**
	 * 是否已经初始化完
	 * 
	 * @return
	 */
	public boolean isInit() {
		return mInited;
	}

	private volatile boolean mNeedUpdateCmds = true;

	public void setNeedUpdateCmd(boolean update) {
		LogUtil.logd("setNeedUpdateCmd:" + update);
		this.mNeedUpdateCmds = update;
	}

	/**
	 * 是否需要更新导航的唤醒词
	 * 
	 * @return
	 */
	public boolean needUpdateCmds() {
		return mNeedUpdateCmds;
	}

	/**
	 * 导航指令是否注册为唤醒方式
	 * 
	 * @return
	 */
	public boolean enableWakeup() {
		if (mNavCmdRegister != null) {
			return mNavCmdRegister.enableWakeup();
		}
		return true;
	}

	/**
	 * 插入初始化回调等待队列
	 * 
	 * @param task
	 */
	public void addDelayTask(Runnable task) {
		if (isInit()) {
			task.run();
			return;
		}
		synchronized (mDelayTaskList) {
			mDelayTaskList.add(task);
		}
	}

	public void setUseActiveNav(boolean isUse) {
		mUseActiveNav = isUse;
		LogUtil.logd("setUseActiveNav:" + isUse);
	}

	public boolean useActiveNav() {
		return mUseActiveNav;
	}

	private static List<BrandNavObj> loadAndExtractBean() {
		List<BrandNavObj> navObjs = new ArrayList<BrandNavObj>();
		JSONObject obj = loadConfigJson();
		if (DEBUG) {
			LogUtil.logd("NavAppManager loadConfigJson:" + obj.toString());
		}
		if (obj != null) {
			Iterator<String> ikey = obj.keys();
			while (ikey.hasNext()) {
				String key = ikey.next();
				JSONObject valObj = obj.optJSONObject(key);

				BrandNavObj navObj = new BrandNavObj();
				navObj.brandName = key;
				navObj.appIds = getList(valObj, "ids");
				navObj.appClsInstance = valObj.optString("navtool");
				navObj.arrayIndex = valObj.optInt("level");
				navObjs.add(navObj);
			}
		}

		return navObjs;
	}

	private void tool_init_end(boolean isSuccess, NavThirdApp app) {
		mInitCount++;
		LogUtil.logd("has init count:" + mInitCount);
		final String pkn = app.getPackageName();
		if (isSuccess && app.isReachable()) {
			synchronized (mAppsMap) {
				mAppsMap.put(pkn, app);
				if (DEBUG) {
					String vn = app.getVersionName();
					LogUtil.logd("NavAppManager appMap put:" + pkn + ",vn:" + vn);
				}
			}
		}

		if (TextUtils.isEmpty(pkn)) {
			return;
		}

		replaceMemberObj(pkn);
	}

	/**
	 * 初始化结束后更新内存中导航配置信息
	 * 
	 * @param isInit
	 * @param packageName
	 */
	private void replaceMemberObj(String packageName) {
		synchronized (mTmpBrandNavObjs) {
			for (BrandNavObj obj : mTmpBrandNavObjs) {
				if (obj.hasInited) {
					continue;
				}

				if (packageName.equals(obj.truePkgName)) {
					obj.hasInited = true;
					break;
				}
			}
		}
	}

	/**
	 * 检测是否已经全部初始化完成
	 */
	private void checkInitComplete() {
		if (mInited) {
			return;
		}

		if (mInitCount == mTmpApps.size()) {
			for (BrandNavObj navObj : mTmpBrandNavObjs) {
				if (navObj.hasInited) {
					mAppsLevelList.add(navObj.truePkgName);
				}
			}

			mInited = true;

			synchronized (mDelayTaskList) {
				for (Runnable task : mDelayTaskList) {
					task.run();
				}
			}

			onInitEnd();
			LogUtil.logd("nav apps init end！");
		}
	}

	private void onInitEnd() {
		installTXZCommNav();
		// 开始同步地址
		if (NavManager.getInstance().needSyncAddress()) {
			NavManager.getInstance().applyHcAddressToNav(getInnerNavTool(), true);
		} else {
			// 开始查询家和公司的地址
			AppLogic.removeBackGroundCallback(mQueryAddr);
			AppLogic.runOnBackGround(mQueryAddr, 1000);
		}
		mSetNavType = PreferenceUtil.getInstance().getDefaultNavTool();
		LogUtil.logd("onInitEnd and default nav:" + mSetNavType);

		// 初始化导航拦截Activity焦点变化
		NavInterceptTransponder.getInstance().init();
		HelpGuideManager.getInstance().checkToRegisterGuideCmds();
	}

	Runnable mQueryAddr = new Runnable() {

		@Override
		public void run() {
			NavThirdApp nta = getInnerNavTool();
			if (nta != null) {
				JNIHelper.logd("NavManager queryHomeCompanyAddr");
				nta.queryHomeCompanyAddr();
			}
		}
	};

	/////////////////////////////////////////////////////////////////////

	/**
	 * 优先选取当前被激活的导航（打开后处于后台、前台、导航中都属于激活状态） 
	 * 其次是适配程序设置的导航 
	 * 其次是适配程序指定内部导航
	 * 最后是同行者优先级导航
	 * 
	 * @return
	 */
	public NavThirdApp getCurrNavTool() {
		if (!isInit()) {
			LogUtil.loge("currNav is null,isInit false");
			return null;
		}

		NavThirdApp navApp = getCurrActiveTool();
		do {
			// TODO 根据后台决定要不要将当前激活的工具做为下一次的导航工具
			if (navApp != null && navApp.isReachable() && mUseActiveNav) {
				break;
			}

			navApp = getInnerNavTool();
		} while (false);
		LogUtil.logd("NavThirdApp type:" + (navApp != null ? navApp.getPackageName() : "null"));

		return navApp;
	}

	public NavThirdApp getInnerNavTool() {
		if (!isInit()) {
			LogUtil.loge("currNav is null,isInit false");
			return null;
		}

		NavThirdApp navApp = mRemoteServiceNav;
		do {
			if (navApp != null && navApp.isReachable()) {
				break;
			}

			navApp = getNavToolByName(getNavToolType());
			if (navApp != null && navApp.isReachable()) {
				break;
			}

			for (String packageName : mAppsLevelList) {
				synchronized (mAppsMap) {
					navApp = mAppsMap.get(packageName);
				}

				if (navApp != null && navApp.isReachable()) {
					break;
				}
			}
		} while (false);

		return navApp;
	}

	/**
	 * 获取当前被激活的导航工具
	 * 
	 * @return
	 */
	public NavThirdApp getCurrActiveTool() {
		if (!TextUtils.isEmpty(mCurrActivePkn)) {
			if (hasRemoteNavTool() && mRemoteServiceNav.getPackageName().equals(mCurrActivePkn)) {
				return mRemoteServiceNav;
			}

			synchronized (mAppsMap) {
				return mAppsMap.get(mCurrActivePkn);
			}
		}
		return null;
	}

	public void setRemoteNavService(String serviceName) {
		LogUtil.logd("setRemoteNavService:" + serviceName);
		if (TextUtils.isEmpty(serviceName)) {
			RemoteNavImpl.setRemoteServiceName(null);
			mRemoteServiceNav = null;
		} else {
			RemoteNavImpl.setRemoteServiceName(serviceName);
			mRemoteServiceNav = new RemoteNavImpl();
			mRemoteServiceNav.setNavStatusListener(mStatusListener);
			// 放入Map
			mAppsMap.put(mRemoteServiceNav.getPackageName(), mRemoteServiceNav);
		}
	}
	
	// 找不到默认导航（设置了默认导航，并且后台设置启用的情况下），使用指定的导航
	private String getNavToolType() {
		if (!TextUtils.isEmpty(mSetNavType) && PackageManager.getInstance().checkAppExist(mSetNavType)
				&& NavManager.getInstance().mSupportDefaultNav) {
			return mSetNavType;
		}
		return getNavToolByType(mBrandName);
//		return mLocalNavType;
	}
	
//	public void setNavToolType(String navType) {
//		LogUtil.logd("setNavToolType:" + navType);
//		mLocalNavType = navType;
//	}

	private String mBrandName;

	public void setNavBrandType(String brandName) {
		LogUtil.logd("setNavBrandType:" + brandName);
		mBrandName = brandName;
	}

	public void setDefaultNavType(boolean fromVoice, String navPkn) {
		LogUtil.logd("setDefaultNavType:" + navPkn + ",fromVoice:" + fromVoice);
		mSetNavType = navPkn;
		// 查询该导航的家和公司的地址
		NavThirdApp nta = getInnerNavTool();
		if (nta != null) {
			nta.queryHomeCompanyAddr();
		}
		// 通知适配程序设置了默认导航
		if (fromVoice) {
			invokeRemoteStatus("status.nav.defaultNav", navPkn.getBytes());
		}

		PreferenceUtil.getInstance().setDefaultNavTool(navPkn);
	}

	/**
	 * 获取默认导航
	 * @return
	 */
	public String getDefaultNavTool() {
		return mSetNavType;
	}
	
	public void clearDefaultNav() {
		mSetNavType = null;
		PreferenceUtil.getInstance().setDefaultNavTool("");
	}
	
	public boolean hasDefaultNavTool() {
		return !TextUtils.isEmpty(mSetNavType);
	}

	/**
	 * 返回远程导航
	 * 
	 * @return
	 */
	public NavThirdApp getRemoteNav() {
		return mRemoteServiceNav;
	}

	/**
	 * 根据包名获取对应的导航工具
	 * 
	 * @param packageName
	 * @return
	 */
	public NavThirdApp getNavToolByName(String packageName) {
		LogUtil.logd("getNavToolByName:" + packageName);
		if (TextUtils.isEmpty(packageName)) {
			return null;
		}
		synchronized (mAppsMap) {
			return mAppsMap.get(packageName);
		}
	}

	public void handleSelectNavApp() {
		AppLogic.removeBackGroundCallback(mSelectAppTask);
		AppLogic.runOnBackGround(mSelectAppTask, 0);
	}

	Runnable mSelectAppTask = new Runnable() {

		@Override
		public void run() {
			showNavTool(false, "", null);
		}
	};
	
	public int getNavAppsCount() {
		int size = loadNavApps(false);
		if (size < 1) {
			size = loadNavApps(true);
		}
		LogUtil.logd("getNavAppsCount :" + size);
		return size;
	}
	
	private int loadNavApps(boolean reGet) {
		if (reGet || mNavApps.size() <= 0) {
			mNavApps.clear();
			synchronized (mAppsMap) {
				AppInfoList apps = PackageManager.getInstance().getAppList();
				for (String navKey : mAppsMap.keySet()) {
					if (apps != null && apps.rptMsgApps != null) {
						for (UiApp.AppInfo app : apps.rptMsgApps) {
							if (navKey.equals(app.strPackageName)) {
								NavAppBean bean = new NavAppBean();
								bean.strActivityName = app.strActivityName;
								bean.strAppName = app.strAppName;
								bean.strPackageName = app.strPackageName;
								bean.strParams = app.strParams;
								mNavApps.add(bean);
								break;
							}
						}
					}
				}
			}
			// 处理远程导航的情况
			if (mRemoteServiceNav != null) {
				NavAppBean bean = new NavAppBean();
				bean.navType = NavAppBean.TYPE_REMOTE_NAV;
				bean.strPackageName = mRemoteServiceNav.getPackageName();
				bean.strAppName = "远程导航";

				mNavApps.add(0, bean);
			}
		}

		int nSize = mNavApps.size();
		LogUtil.logd("loadNavApps size:" + nSize);
		return nSize;
	}

	public void showNavTool(boolean reGet, String ttsSpk, OnItemSelectListener<NavAppBean> selectListener) {
		String activeNavPkn = null;
		NavThirdApp activeNav = getCurrActiveTool();
		if (activeNav != null) {
			activeNavPkn = activeNav.getPackageName();
		}

		int nSize = loadNavApps(reGet);

		if (nSize < 1) {
			AsrManager.getInstance().setNeedCloseRecord(false);
			NavManager.getInstance().preInvokeWhenNavNotExists(new Runnable() {
				@Override
				public void run() {
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NO_NAV_TOOL"), null);
				}
			});
			return;
		}

		JSONObject jsonObject = new JSONObject();
		List<NavAppBean> beans = new ArrayList<NavAppBean>();
		try {
			jsonObject.put("type", RecorderWin.SimpleSence);
			JSONArray jNavs = new JSONArray();
			for (int i = 0; i < mNavApps.size(); i++) {
				NavAppBean appInfo = mNavApps.get(i);
//				if (appInfo.strPackageName.equals(activeNavPkn)) {
//					continue;
//				}
				JSONObject obj = new JSONObject();
				obj.put("activityName", appInfo.strActivityName);
				obj.put("appName", appInfo.strAppName);
				obj.put("packageName", appInfo.strPackageName);
				obj.put("params", appInfo.strParams);
				jNavs.put(obj);
				beans.add(appInfo);
			}
			jsonObject.put("count", jNavs.length());
			jsonObject.put("navs", jNavs);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		LogUtil.logd("showNavList:" + jsonObject.toString());

		if (SenceManager.getInstance().noneedProcSence("navChoice", jsonObject.toString().getBytes())) {
			return;
		}

		// SelectorHelper.entryNavToolSelector(beans, ttsSpk, listener);
		ChoiceManager.getInstance().showNavAppsList(beans, ttsSpk, selectListener);
	}

	/**
	 * 返回是否设置了远程工具
	 * 
	 * @return
	 */
	public boolean hasRemoteNavTool() {
		return mRemoteServiceNav != null;
	}

	/**
	 * 检测当前是否已经退出了导航
	 * 
	 * @return
	 */
	public boolean isAlreadyExitNav() {
		NavThirdApp activeNav = getCurrActiveTool();
		if (activeNav != null && activeNav.hasBeenOpen()) {
			return false;
		}

		activeNav = getCurrNavTool();
		if (activeNav != null && activeNav.hasBeenOpen()) {
			return false;
		}
		return true;
	}

	/**
	 * 是否处于焦点
	 * 
	 * @return
	 */
	public boolean isInFocus() {
		NavThirdApp nta = getCurrActiveTool();
		if (nta != null && nta.isReachable()) {
			return nta.isInFocus();
		}
		nta = getCurrNavTool();
		if (nta != null && nta.isReachable()) {
			return nta.isInFocus();
		}
		return false;
	}

	/**
	 * 是否处于路径规划中
	 * 
	 * @return
	 */
	public boolean isInNav() {
		NavThirdApp nta = getCurrActiveTool();
		if (nta != null && nta.isReachable()) {
			return nta.isInNav();
		}

		nta = getCurrNavTool();
		if (nta != null && nta.isReachable()) {
			return nta.isInNav();
		}
		return false;
	}

	public boolean handleSwitchNavApp(NavAppBean bean) {
		NavThirdApp activeNav = getCurrActiveTool();
		if (activeNav == null || !activeNav.isReachable()) {
			if (isAlreadyExitNav()) {
				AsrManager.getInstance().setNeedCloseRecord(false);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ANSWER_OPEN_NAV"), null);
				return false;
			}
		} else {
			if (!activeNav.hasBeenOpen()) {
				AsrManager.getInstance().setNeedCloseRecord(false);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_ANSWER_OPEN_NAV"), null);
				return false;
			}
		}

		NavThirdApp nextNav = getNavToolByName(bean.strPackageName);
		if (bean.navType == NavAppBean.TYPE_REMOTE_NAV) {
			nextNav = mRemoteServiceNav;
		}
		if (nextNav == null) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_NAVTOOL_NULL"), null);
			return false;
		}

		PathInfo pathInfo = null;
		if (activeNav != null && activeNav.isReachable()) {
			pathInfo = activeNav.getCurrentPathInfo();
			// 结束当前导航
			activeNav.exitNav();
		}
		if (pathInfo == null) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(
					NativeData.getResString("RS_VOICE_NAVTOOL_SWITCH").replace("%NAVTOOL%", bean.strAppName),
					new Runnable1<NavThirdApp>(nextNav) {

						@Override
						public void run() {
							mP1.enterNav();
						}
					});
			return true;
		}

		String spk = NativeData.getResString("RS_VOICE_NAVTOOL_SELECT").replace("%NAVTOOL%", bean.strAppName);
		AsrManager.getInstance().setNeedCloseRecord(true);
		RecorderWin.speakTextWithClose(spk, new Runnable2<NavThirdApp, PathInfo>(nextNav, pathInfo) {

			@Override
			public void run() {
				// TODO 导航切换
				// mP1.navigateByPathInfo(mP2);
			}
		});
		LogUtil.logd("switch nav tool:" + (nextNav != null ? nextNav.getPackageName() : ""));
		return true;
	}

	public void notifyStartNav(NavigateInfo info, NavThirdApp nav) {
		setLastVoiceDestination(info);
		
		invokeRemoteStatus("status.nav.enter", nav.getPackageName().getBytes());
		{
			Poi poi = new Poi();
			poi.setLat(info.msgGpsInfo.dblLat);
			poi.setLng(info.msgGpsInfo.dblLng);
			poi.setName(info.strTargetName);
			poi.setGeoinfo(info.strTargetAddress);
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("poi", poi.toString());
			jsonBuilder.put("packageName", nav.getPackageName());
			invokeRemoteStatus("status.nav.beginNav", jsonBuilder.toBytes());
		}
	}

	/**
	 * 向某个远程工具同步当前导航的状态
	 * 
	 * @param packageName
	 */
	public void invokeNavStatusToServiceName(String packageName) {
		NavThirdApp nta = getCurrNavTool();
		if (nta != null) {
			if (nta.isInFocus()) {
				ServiceManager.getInstance().sendInvoke(packageName, "status.nav.foreground",
						nta.getPackageName().getBytes(), null);
			} else {
				ServiceManager.getInstance().sendInvoke(packageName, "status.nav.background",
						nta.getPackageName().getBytes(), null);
			}
			if (nta.isInNav()) {
				ServiceManager.getInstance().sendInvoke(packageName, "status.nav.start",
						nta.getPackageName().getBytes(), null);
			} else {
				ServiceManager.getInstance().sendInvoke(packageName, "status.nav.end", nta.getPackageName().getBytes(),
						null);
			}

			ServiceManager.getInstance().sendInvoke(packageName, "status.nav.defaultNav",
					mSetNavType != null ? mSetNavType.getBytes() : null, null);
			return;
		}
	}

	/**
	 * 判断远程有没有注册了监听
	 * 
	 * @return
	 */
	public boolean hasStatusListener() {
		synchronized (mLockObj) {
			if (mNavToolStatusServiceNames == null || mNavToolStatusServiceNames.size() < 1) {
				return false;
			}
			return true;
		}
	}

	public List<String> getStatusListeners() {
		return mNavToolStatusServiceNames;
	}

	public Object getStatusListenerLock() {
		return mLockObj;
	}
	
	private void onNavForeground(String navPkn) {
		if (!NavManager.getInstance().needSyncAddress()) {
			return;
		}

		NavThirdApp navtool = getInnerNavTool();
		if (navtool != null && navtool.getPackageName().equals(navPkn)) {
			// 默认导航进入前台，将地址覆盖给导航
			NavManager.getInstance().applyHcAddressToNav(navtool, false);
		}
	}

	/**
	 * 将状态广播给远程
	 * 
	 * @param command
	 */
	public void invokeRemoteStatus(String command, byte[] data) {
		if (!hasStatusListener()) {
			return;
		}

		synchronized (mLockObj) {
			for (String serviceName : mNavToolStatusServiceNames) {
				ServiceManager.getInstance().sendInvoke(serviceName, command, data, null);
			}
		}
	}

	ConnectionListener mConnectionListener = new ConnectionListener() {
		@Override
		public void onConnected(String serviceName) {
		}

		@Override
		public void onDisconnected(String serviceName) {
			if (NavAppManager.getInstance().hasStatusListener()) {
				synchronized (NavAppManager.getInstance().getStatusListenerLock()) {
					if (NavAppManager.getInstance().getStatusListeners().contains(serviceName)) {
						invokeNav(null, "setStatusListener", "TXZ".getBytes());
					}
				}
			}
		}
	};

	public void clearHomeLocation() {
		if (hasRemoteNavTool()) {
			mRemoteServiceNav.updateHomeLocation(null);
		}
	}

	public void clearCompanyLocation() {
		if (hasRemoteNavTool()) {
			mRemoteServiceNav.updateCompanyLocation(null);
		}
	}

	public byte[] invokeNav(String packageName, String command, byte[] data) {
		if (!isInit()) {
			synchronized (mDelayTaskList) {
				mDelayTaskList.add(new Runnable3<String, String, byte[]>(packageName, command, data) {

					@Override
					public void run() {
						handleNav(mP1, mP2, mP3);
					}
				});
			}
			return null;
		}
		return handleNav(packageName, command, data);
	}

	private byte[] handleNav(final String packageName, String command, byte[] data) {
		if (command.startsWith("asr.key.")) {
			return invokeNavOption(packageName, command.substring("asr.key.".length()), data);
		}
		if (command.startsWith("remote.")) {
			return invokeNavOption(packageName, command.substring("remote.".length()), data);
		}
		if (command.startsWith("app.")) {
			return invokeNavOption(packageName, command.substring("app.".length()), data);
		}
		if (command.equals("autoNaviDelay")) {
			return invokeNavOption(packageName, command, data);
		}
		if (command.startsWith("enablecmd") || command.equals("enableWakeupExit") || command.equals("forceRegister")
				|| command.equals("enableWakeupNav")) {
			return invokeNavOption(packageName, command, data);
		}
		if (command.equals("setRemoteFlag")) {
			RemoteNavImpl.setRemoteNavFlag(Integer.parseInt(new String(data)));
			return null;
		}
		if (command.equals("notifyNavStatus")) {
			NavThirdApp remoteNav = getRemoteNav();
			if (remoteNav != null) {
				RemoteNavImpl nta = (RemoteNavImpl) remoteNav;
				nta.setRemoteNavToolisInNav(Boolean.parseBoolean(new String(data)));
			}
			return null;
		}
		if (command.equals("notifyIsFocus")) {
			NavThirdApp remoteNav = getRemoteNav();
			if (remoteNav != null) {
				RemoteNavImpl nta = (RemoteNavImpl) remoteNav;
				nta.setRemoteNavToolisInFocus(Boolean.parseBoolean(new String(data)));
			}
			return null;
		}
		if (command.equals("notifyPathInfo")) {
			NavThirdApp navThirdApp = getRemoteNav();
			if (navThirdApp != null) {
				navThirdApp.invokeTXZNav(packageName, command, data);
			}
			return null;
		}
		if (command.equals("notifyJingYous")) {
			NavThirdApp nav = getRemoteNav();
			if (nav != null && nav instanceof RemoteNavImpl) {
				nav.invokeTXZNav(packageName, command, data);
			}
			return null;
		}
		if (command.equals("notifyExitApp")) {
			NavThirdApp remoteNav = getRemoteNav();
			if (remoteNav != null) {
				RemoteNavImpl nta = (RemoteNavImpl) remoteNav;
				nta.setRemoteNavToolisExitApp(Boolean.parseBoolean(new String(data)));
			}
			return null;
		}
		if ("settool".equals(command)) {
			String type = new String(data);
			JNIHelper.logd(packageName + " set nav tool type: " + type);
//			String navtool = getNavToolByType(type);
			if (!TextUtils.isEmpty(type)) {
				setNavBrandType(type);
				return null;
			}
			return null;
		}
		if (command.equals("clearDefaultNav")) {
			JNIHelper.logd(packageName + " clear default nav tool type: " + mSetNavType);
			clearDefaultNav();
			return null;
		}
		if (command.equals("setDefaultNav")) {
			String type = new String(data);
			JNIHelper.logd(packageName + " set default nav tool type: " + type);
			String navtool = getNavToolByType(type);
			if (!TextUtils.isEmpty(navtool)) {
				setDefaultNavType(false, navtool);
			}
			return null;
		}
		if ("useActiveNav".equals(command)) {
			if (data != null) {
				setUseActiveNav(Boolean.parseBoolean(new String(data)));
			}
			return null;
		}
		if (command.equals("setStatusListener")) {
			ServiceManager.getInstance().removeConnectionListener(mConnectionListener);
			ServiceManager.getInstance().addConnectionListener(mConnectionListener);
			ServiceManager.getInstance().sendInvoke(packageName, "", null, new GetDataCallback() {

				@Override
				public void onGetInvokeResponse(ServiceData data) {
					// 记录工具
					if (data != null) {
						synchronized (mLockObj) {
							if (mNavToolStatusServiceNames == null) {
								mNavToolStatusServiceNames = new ArrayList<String>();
							}
							if (mNavToolStatusServiceNames.contains(packageName)) {
								return;
							}
							mNavToolStatusServiceNames.add(packageName);
							LogUtil.logd("setStatusListener invokeNavStatusToServiceName pkg:" + packageName);
							// 设置监听状态的时候返回当前导航的状态
							invokeNavStatusToServiceName(packageName);
						}
					}
				}
			});
			return null;
		}
		return invokeNavOption(packageName, command, data);
	}

	private byte[] invokeNavOption(String packageName, String command, byte[] data) {
		synchronized (mAppsMap) {
			Set<Entry<String, NavThirdApp>> navSets = mAppsMap.entrySet();
			for (Entry<String, NavThirdApp> entry : navSets) {
				entry.getValue().invokeTXZNav(packageName, command, data);
			}
		}
		return mNavCmdRegister != null ? mNavCmdRegister.invokeCmds(packageName, command, data) : null;
	}

	private static JSONObject loadConfigJson() {
		try {
			FileInputStream fis = new FileInputStream(
					GlobalContext.get().getApplicationInfo().dataDir + "/data/nav_tool_map.json");
			FileChannel fileChannel = fis.getChannel();
			if (fileChannel.isOpen()) {
				try {
					long size = fileChannel.size();
					ByteBuffer buffer = ByteBuffer.allocate((int) size);
					fileChannel.read(buffer);
					try {
						return new JSONObject(new String(buffer.array()));
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					try {
						fis.close();
						fileChannel.close();
					} catch (IOException e) {
						LogUtil.loge(e.getMessage());
						e.printStackTrace();
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return null;
	}

	private static List<String> getList(JSONObject targetObj, String key) {
		List<String> idList = new ArrayList<String>();
		JSONArray ids = targetObj.optJSONArray(key);
		if (ids != null) {
			for (int i = 0; i < ids.length(); i++) {
				idList.add(ids.optString(i));
			}
		}
		return idList;
	}

	/**
	 * 获取语音支持的导航包名
	 * @return
	 */
	public Set<String> getSupportNavPkns() {
		synchronized (mAppsMap) {
			return mAppsMap.keySet();
		}
	}

	private String getNavToolByType(String type) {
		String navType = "";
		synchronized (mTmpBrandNavObjs) {
			for (BrandNavObj obj : mTmpBrandNavObjs) {
				if (obj.brandName.equals(type)) {
					navType = obj.truePkgName;
				}
			}
		}

		return navType;
	}
}