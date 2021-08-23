package com.txznet.txz.component.nav.baidu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;

import com.baidu.che.codriver.sdk.oem.NaviControllerListener;
import com.baidu.che.codriver.sdk.oem.SDKConstants;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.navisdk.hudsdk.client.BNRemoteVistor;
import com.txz.ui.data.UiData.AppInfo;
import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog2.WinConfirmAsr.WinConfirmAsrBuildData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager.AsrKeyType;
import com.txznet.sdk.TXZMediaFocusManager;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.R;
import com.txznet.txz.component.choice.list.PoiWorkChoice.PoisData;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor.ExecuteCallBack;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor.ExecuteReq;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.AsyncExecutor.ExecuteTask;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.DialogRecord;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.NavNotifyQueues;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.NavPathInfo;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.NotifyDialog;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.RequestRecord;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.RouteDetails;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.RunnableCallBack;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.TTSRecord;
import com.txznet.txz.component.nav.baidu.NavBaiduFactory.WayPoiData;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.ConfigFileHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.nav.tool.NavInterceptTransponder;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.version.TXZVersion;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.LocationUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable3;
import com.txznet.txz.util.runnables.Runnable4;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class NavBaiduDeepImpl extends NavThirdComplexApp implements BDConstants {
	boolean mHasInit;
	boolean mClientBind;
	boolean mServiceBind;

	boolean mIsViewAll;
	boolean mIsDogMode;

	//记录小灯是否打开（用户判断切换回自动模式的时候根据小灯的状态判断是否需求显示黑夜模式）
	boolean mIsLightOpen=false;

	RequestRecord mRecord;
	BaiduHudInfo mBaiduHudInfo;
	BaiduConnect mBaiduConnect;
	TTSRecord mTTSRecord = new TTSRecord();

	// 规划路径后获取到的道路信息
	private NavPathInfo mNavPathInfo;
	// 查询得到的导航信息
	private NavPathInfo mNavWayInfo;

	Runnable4<String, Double, Double, Integer> mNaviTask = new Runnable4<String, Double, Double, Integer>("", -1D, -1D,
			-1) {

		@Override
		public void run() {
			if (mP2 == -1 || mP3 == -1) {
				JNIHelper.loge("error GPS");
				return;
			}

			long delay = 0;
			LogUtil.d("foreground--->"+mIsFocus+",InNav--->"+isInNav());
			if (isInNav()) {
				delay = DEFAULT_DELAY_TIME;
				BDHelper.exitNavStatus();
			}

			if (!mIsFocus) {
				delay = DEFAULT_DELAY_TIME;
				BDHelper.startNavApp(GlobalContext.get());
			}
			retryNav = 0;

			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					final String reqId = BDHelper.getRequestId();

					final ExecuteTask eTask = new ExecuteTask() {

						@Override
						public boolean doExecute(ExecuteReq eo) {
							String rId = BDHelper.startNavigate(mP1, mP2, mP3, mP4, reqId);
							return rId.equals(reqId);
						}

						public long getTimeOut() {
							return 30000;
						}
					};
					final ExecuteCallBack callBack = new ExecuteCallBack() {

						@Override
						public void onReceive(boolean bSucc, String params) {
						}

						public void onReceive(int errCode, String params) {
							String disclaimTxt = NativeData.getResString("RS_NAV_BAIDU_DISCLAIMER");
							if (errCode == MAP_CONTROL_STATE_ERROR && params.equals(disclaimTxt)) {
								AsrManager.getInstance().setNeedCloseRecord(true);
								RecorderWin.speakTextWithClose(disclaimTxt, null);
								return;
							}

							if (errCode == MAP_CONTROL_PARAMS_ERROR) {
								mNaviRunnable.update(reqId, eTask, this);
								AppLogic.removeBackGroundCallback(mNaviRunnable);
								AppLogic.runOnBackGround(mNaviRunnable, RETRY_NAV_DELAY);
							}
						}

						public void onError(int error, String des) {
							JNIHelper.loge("start request nav onerror :" + error + ",des:" + des);
						}
					};
					mNaviRunnable.update(reqId, eTask, callBack);
					AppLogic.removeBackGroundCallback(mNaviRunnable);
					AppLogic.runOnBackGround(mNaviRunnable, 10);
				}
			}, delay);
		}
	};

	Runnable4<Poi ,Poi,List<PathInfo.WayInfo> , Integer> mNaviWithPoiTask = new Runnable4<Poi ,Poi,List<PathInfo.WayInfo> , Integer>(null, null,null,1) {

		@Override
		public void run() {
			Poi endPoi = mP2;
			if (endPoi == null || endPoi.getLat() == 0 || endPoi.getLng() == 0) {
				JNIHelper.loge("error GPS");
				return;
			}

			long delay = 0;
			if (isInNav()) {
				delay = DEFAULT_DELAY_TIME;
				BDHelper.exitNavStatus();
			}

			if (!mIsFocus) {
				delay = DEFAULT_DELAY_TIME;
				BDHelper.startNavApp(GlobalContext.get());
			}
			retryNav = 0;

			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					final String reqId = BDHelper.getRequestId();

					final ExecuteTask eTask = new ExecuteTask() {

						@Override
						public boolean doExecute(ExecuteReq eo) {
							String rId = BDHelper.startNavigate(mP1, mP2, mP3, mP4, reqId);
							return rId.equals(reqId);
						}

						public long getTimeOut() {
							return 30000;
						}
					};
					final ExecuteCallBack callBack = new ExecuteCallBack() {

						@Override
						public void onReceive(boolean bSucc, String params) {
						}

						public void onReceive(int errCode, String params) {
							String disclaimTxt = NativeData.getResString("RS_NAV_BAIDU_DISCLAIMER");
							if (errCode == MAP_CONTROL_STATE_ERROR && params.equals(disclaimTxt)) {
								AsrManager.getInstance().setNeedCloseRecord(true);
								RecorderWin.speakTextWithClose(disclaimTxt, null);
								return;
							}

							if (errCode == MAP_CONTROL_PARAMS_ERROR) {
								mNaviRunnable.update(reqId, eTask, this);
								AppLogic.removeBackGroundCallback(mNaviRunnable);
								AppLogic.runOnBackGround(mNaviRunnable, RETRY_NAV_DELAY);
							}
						}

						public void onError(int error, String des) {
							JNIHelper.loge("start request nav onerror :" + error + ",des:" + des);
						}
					};
					mNaviRunnable.update(reqId, eTask, callBack);
					AppLogic.removeBackGroundCallback(mNaviRunnable);
					AppLogic.runOnBackGround(mNaviRunnable, 10);
				}
			}, delay);
		}
	};

	int retryNav = 0;
	int retryMax = 3;

	Runnable3<String, ExecuteTask, ExecuteCallBack> mNaviRunnable = new Runnable3<String, ExecuteTask, ExecuteCallBack>(
			null, null, null) {

		@Override
		public void run() {
			if (retryNav > retryMax || mP1 == null || mP2 == null || mP3 == null) {
				AppLogic.removeBackGroundCallback(this);
				return;
			}

			JNIHelper.logd("retry nav:" + retryNav);

			AsyncExecutor.getInstance().doAsyncExec(mP1, FUN_NAVI_START_TASK, mP2, mP3);

			retryNav++;
		}
	};

	NaviControllerListener mNaviControllerListener = new NaviControllerListener() {

		@Override
		public String onSynchronousCall(String func, String params) {
			return null;
		}

		@Override
		public void onNotification(String func, String params) {
			try {
				Bundle bundle = new Bundle();
				bundle.putString("actionType", "onNotification");
				bundle.putString("func", func);
				bundle.putString("params", params);
				handleBundle(bundle);
			} catch (Exception e) {
				JNIHelper.loge(e.toString());
			}
		}

		@Override
		public void onEvent(int event, String msg) {
			JNIHelper.logd("NaviControllerListener onEvent:" + event + "," + msg);
			try {
				Bundle bundle = new Bundle();
				bundle.putString("actionType", "onEvent");
				bundle.putInt("event", event);
				bundle.putString("msg", msg);
				handleBundle(bundle);
			} catch (Exception e) {
				JNIHelper.loge(e.toString());
			}
		}

		@Override
		public void onAsynchronousCall(String requestId, String func, String params) {
			try {
				Bundle bundle = new Bundle();
				bundle.putString("actionType", "onAsynchronousCall");
				bundle.putString("requestId", requestId);
				bundle.putString("func", func);
				bundle.putString("params", params);
				handleBundle(bundle);
			} catch (Exception e) {
				JNIHelper.loge(e.toString());
			}
		}
	};

	com.baidu.navicontroller.sdk.NaviControllerListener mControllerListener = new com.baidu.navicontroller.sdk.NaviControllerListener() {

		@Override
		public void onReceive(final int type, final int errorNo, final String requestId, final String func,
				final String params) {
			try {
				Bundle bundle = new Bundle();
				bundle.putString("actionType", "onReceive");
				bundle.putInt("type", type);
				bundle.putInt("errorNo", errorNo);
				bundle.putString("requestId", requestId);
				bundle.putString("func", func);
				bundle.putString("params", params);
				handleBundle(bundle);

			} catch (IllegalStateException e){
				JNIHelper.loge("BaiduNavi onReceive IllegalStateException:" + e.toString());
				/* 【时间戳异常导致初始化失败，从而时部分功能无法实现】
				 * 部分车机的刚开机的时候时间会先恢复出厂设置，然后再通过网络请求当前的时间，
				 * 但是因为网络等相关原因时间没有及时更新，导致BaiduSDK初始化因为时间鉴权问题初始化失败，
				 * 最终导致实现部分功能的时候出现被catch异常。
				 * 在这里补充当出现因为SDK未成功初始化问题的时候，播报初始化异常并重新初始化
				 */
				String spkTxt="";
				if (FUN_NAVI_ROUTE_PLAN.equals(func)) {
					spkTxt = NativeData.getResString("RS_MAP_SDK_INITIALIZER_FAIL");
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(spkTxt, null);
				}
				LocationManager.getInstance().checkNet2InitBaiduSDK();
			} catch (Exception e) {
				JNIHelper.loge("BaiduNavi onReceive:" + e.toString());
			}
		}

		@Override
		public void onEvent(int event, final String msg) {
			try {
				Bundle bundle = new Bundle();
				bundle.putString("actionType", "onEvent");
				bundle.putInt("event", event);
				bundle.putString("msg", msg);
				handleBundle(bundle);
			} catch (Exception e) {
			}
		}
	};
	
	@Override
	public void handleBundle(Bundle bundle) {
		String f = bundle.getString("func");
		if (FUN_NAVI_STATUS_SYNC.equals(f)) { // 过滤前后台切换
			String event = null;
			try {
				JSONObject jsonObject = new JSONObject(bundle.getString("params"));
				event = jsonObject.optString("event");
			} catch (Exception e) {
				e.printStackTrace();
			}
			boolean intercept = false;
			if (NAVI_FRONT.equals(event)) {
				intercept = NavInterceptTransponder.getInstance().interceptGroundBundle(this, bundle, true);
			} else if (NAVI_BACKGROUND.equals(event)) {
				intercept = NavInterceptTransponder.getInstance().interceptGroundBundle(this, bundle, false);
			}
			if (intercept) {
				return;
			}
		}
		
		String actionType = bundle.getString("actionType");
		if ("onEvent".equals(actionType)) {
			int event = bundle.getInt("event");
			String msg = bundle.getString("msg");
			JNIHelper.logd("com.baidu.navicontroller.sdk.NaviControllerListener onEvent:" + event + "," + msg);
			doOnEvent(event, msg);
		} else if ("onReceive".equals(actionType)) {
			int type = bundle.getInt("type");
			int errorNo = bundle.getInt("errorNo");
			String requestId = bundle.getString("requestId");
			String func = bundle.getString("func");
			String params = bundle.getString("params");
			JNIHelper.logd("onReceive:" + type + "," + errorNo + "," + requestId + "," + func + "," + params);
			if (AsyncExecutor.getInstance().onNaviReceive(type, errorNo, requestId, func, params)) {
				return;
			}
			if (doOnNotification(func, params)) {
				return;
			}
			if (doOnAsynchronousCall(requestId, func, params)) {
				return;
			}
		} else if ("onAsynchronousCall".equals(actionType)) {
			String requestId = bundle.getString("requestId");
			String func = bundle.getString("func");
			String params = bundle.getString("params");
			JNIHelper.logd("onAsynchronousCall:" + requestId + "," + func + "," + params);
			doOnAsynchronousCall(requestId, func, params);
		} else if ("onNotification".equals(actionType)) {
			String func = bundle.getString("func");
			String params = bundle.getString("params");
			JNIHelper.logd("onNotification:" + func + "," + params);
			doOnNotification(func, params);
		}
	}


	//确定是否是沿途搜
	String[] sArrNavByScene = { "途经", "先去", "经过", "走","顺便","中途","途中","沿途","分享" };
	private boolean isNavWay(String navTarName){
		for(String str:sArrNavByScene){
			if (navTarName.contains(str)){
				return true;
			}
		}
		return false;
	}

	public boolean isWayPoiKeyword(String navTarName){
		if (navTarName.contains("加油站")){
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_GAS, navTarName);
		}else if (navTarName.contains("银行")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_BANK, navTarName);
		}else if (navTarName.contains("厕所")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_TOILET, navTarName);
		}else if (navTarName.contains("景点")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_SPOTS, navTarName);
		}else if (navTarName.contains("餐饮")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_RESTAURANT, navTarName);
		}else if (navTarName.contains("酒店")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_HOTEL, navTarName);
		}else if (navTarName.contains("服务区")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_SERVICE, navTarName);
		}else if (navTarName.contains("停车场")) {
			onWayPoiCommand(AsrKeyType.NAV_WAY_POI_CMD_PARK, navTarName);
		}else {
			return false;
		}
		enterNav();
		return true;
	}

	public boolean onWayPoiCommand(final String cmd, String speech) {
		if (!isInNav()) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			if ("NAV_WAY_POI_CMD_GASTATION".equals(cmd)) {
				NavigateInfo navigateInfo = new NavigateInfo();
				navigateInfo.strTargetName = "加油站";
				NavManager.getInstance().navigateByName(navigateInfo, false, Poi.PoiAction.ACTION_JINGYOU, false, true);
				return false;
			}else if("NAV_WAY_POI_CMD_TOILET".equals(cmd)){
				NavigateInfo navigateInfo = new NavigateInfo();
				navigateInfo.strTargetName = "厕所";
				NavManager.getInstance().navigateByName(navigateInfo, false, Poi.PoiAction.ACTION_JINGYOU, false, true);
				return false;
			}
			return false;
		}

		speech=changeNewKws(speech);
		AsrManager.getInstance().cancel();
		String spt=NativeData.getResString("RS_MAP_SEARCHING").replace("%TARGET%",speech);
		TtsManager.getInstance().speakText(spt);
		if ("NAV_WAY_POI_CMD_GAS".equals(cmd)||"NAV_WAY_POI_CMD_GASTATION".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Gas_Station");
		} else if ("NAV_WAY_POI_CMD_BANK".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Bank");
		} else if ("NAV_WAY_POI_CMD_TOILET".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Toilet");
		} else if ("NAV_WAY_POI_CMD_SPOTS".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Spots");
		} else if ("NAV_WAY_POI_CMD_RESTAURANT".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Restaurant");
		} else if ("NAV_WAY_POI_CMD_HOTEL".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Hotel");
		} else if ("NAV_WAY_POI_CMD_SERVICE".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Service");
		} else if ("NAV_WAY_POI_CMD_PARK".equals(cmd)) {
			query(RequestRecord.TYPE_WAY_POI, "Park");
		}

		if (mRecord != null) {
			mRecord.speech = speech;
		}
		return false;
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		if (!PackageManager.getInstance().checkAppExist(getPackageName())) {
			AppLogic.runOnUiGround(new Runnable() {

				@Override
				public void run() {
					oRun.onInit(false);
				}
			}, 0);
			return 0;
		}

		AppLogic.removeBackGroundCallback(mInitBdSdk);
		AppLogic.runOnBackGround(mInitBdSdk, 1000);

		return super.initialize(oRun);
	}
	
	Runnable mInitBdSdk = new Runnable() {

		@Override
		public void run() {
			try {
				mRetrybind = new Retrybind();
				mRetrybind.check();
			} catch (Exception e) {
				JNIHelper.loge(e.toString());
			}
		}
	};

	private Retrybind mRetrybind;

	private class Retrybind {
		final int DEFAULT_RETRY_DELAY = 3000;
		final int DEFAULT_RETRY_COUNT = 5;
		int retryCount = DEFAULT_RETRY_COUNT;

		public void check() {
			if (BaiduVersion.isSupportProt(true)) {
				checkInner();
			} else {
				JNIHelper.logw("check init sdk version is illegal！");
			}
		}

		private void checkInner() {
			if (isReady() || retryCount < 1) {
				reset();
				return;
			}

			AppLogic.removeBackGroundCallback(oRun);
			AppLogic.runOnBackGround(oRun, 0);
		}

		private void reset() {
			retryCount = DEFAULT_RETRY_COUNT;
			AppLogic.removeBackGroundCallback(oRun);
			AppLogic.removeBackGroundCallback(oRun1);
		}

		Runnable oRun = new Runnable() {

			@Override
			public void run() {
				JNIHelper.logw("RetryBind reInitNav:" + retryCount);
				reInitNav(true, false);
				retryCount--;
				AppLogic.removeBackGroundCallback(oRun1);
				AppLogic.runOnBackGround(oRun1, DEFAULT_RETRY_DELAY);
			}
		};

		Runnable oRun1 = new Runnable() {

			@Override
			public void run() {
				checkInner();
			}
		};
	}

	private void reInitNav(boolean forceInit, boolean openApp) {
		try {
			if (isReady() && !forceInit) {
				JNIHelper.logd("Service has bind");
				return;
			}

			if (BDHelper.isNewSDKVersion()) {
				if (!mClientBind || !BDHelper.isServiceBind()) {
					if (openApp) {
						BDHelper.startNavApp(GlobalContext.get());
					}
					mClientBind = true;
				}

				if (!mServiceBind || !BDHelper.isServiceBind()) {
					BDHelper.initNewSDK(GlobalContext.get(), mControllerListener);
				}
				connectHudSDK(false);
				return;
			}

			if (!mServiceBind) {
				BDHelper.init(GlobalContext.get(), mNaviControllerListener);
			}

			if (!mClientBind) {
				BDHelper.startNavApp(GlobalContext.get());
			}

			connectHudSDK(false);
		} catch (Exception e) {
			JNIHelper.logw("reInitNav error:" + e.toString());
		}
	}

	public void connectHudSDK(boolean isForce) {
		try {
			if ((!BNRemoteVistor.getInstance().isConnect() && !mHasInit) || isForce) {
				mHasInit = true;
				JNIHelper.logd("txz init hud sdk");
				if (mBaiduConnect == null) {
					mBaiduConnect = new BaiduConnect(this);
				}
				if (mBaiduHudInfo == null) {
					mBaiduHudInfo = new BaiduHudInfo(this);
				}
				AppInfo appInfo = PackageManager.getInstance().getAppInfo(getPackageName());
				String vcn = appInfo != null ? appInfo.strVersion : TXZVersion.SVNVERSION + "";
				BNRemoteVistor.getInstance().unInit();
				BNRemoteVistor.getInstance().init(GlobalContext.get(), getPackageName(), vcn, mBaiduHudInfo,
						mBaiduConnect);
			}
		} catch (Exception e) {
			JNIHelper.logw("connectHudSDK error:" + e.toString());
		}
	}

	private void resetUnbind() {
		onEnd(false);
		onPause();
	}

	@Override
	public void onResume() {
		super.onResume();

		NavNotifyQueues.getInstance().onResume();
	}

	@Override
	public void onPause() {
		AppLogic.removeUiGroundCallback(mDismissConfirm);
		AppLogic.runOnUiGround(mDismissConfirm, 0);
		super.onPause();
	}

	Runnable mDismissConfirm = new Runnable() {

		@Override
		public void run() {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				((DialogRecord) mWinConfirmAsr).dismiss();
			}

			NavNotifyQueues.getInstance().onPause();
			mTTSRecord.cancelTts();
		}
	};

	@Override
	public void onNavCommand(final boolean isWakeupResult, String type, final String command) {
		if (AsrKeyType.EXIT_NAV.equals(type) || AsrKeyType.CLOSE_MAP.equals(type)) {
			JSONBuilder json = new JSONBuilder();
			json.put("scene", "nav");
			json.put("text", command);
			json.put("action", "exit");
			if (SenceManager.getInstance().noneedProcSence("nav", json.toBytes())) {
				return;
			}
		}

		JNIHelper
				.logd("onNavCommSelect:[" + isWakeupResult + "," + type + "," + command + "," + getPackageName() + "]");

		if (AsrKeyType.ZOOM_IN.equals(type)) {
			// doQuickResponse("203", "RS_MAP_ZOOMIN");
			doWinConfirmResp(type, command, "RS_MAP_ZOOMIN", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("203", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.ZOOM_OUT.equals(type)) {
			// doQuickResponse("202", "RS_MAP_ZOOMOUT");
			doWinConfirmResp(type, command, "RS_MAP_ZOOMOUT", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("202", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.NIGHT_MODE.equals(type)) {
			// doQuickResponse("231", "RS_MAP_NIGHT_MODE");
			doWinConfirmResp(type, command, "RS_MAP_NIGHT_MODE", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("231", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.LIGHT_MODE.equals(type)) {
			// doQuickResponse("232", "RS_MAP_LIGHT_MODE");
			doWinConfirmResp(type, command, "RS_MAP_LIGHT_MODE", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("232", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.OPEN_TRAFFIC.equals(type)) {
			// doQuickResponse("207", "RS_MAP_OPEN_TRAFFIC");
			doWinConfirmResp(type, command, "RS_MAP_OPEN_TRAFFIC", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("207", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.CLOSE_TRAFFIC.equals(type)) {
			// doQuickResponse("208", "RS_MAP_CLOSE_TRAFFIC");
			doWinConfirmResp(type, command, "RS_MAP_CLOSE_TRAFFIC", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("208", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.CAR_DIRECT.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_CHANGE_CAR_DIRECT", new RunnableCallBack() {
				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("230", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.THREE_MODE.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_CHANGE_CAR_DIRECT", new RunnableCallBack() {
				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("230", getTaskId());
				}
				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.NORTH_DIRECT.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_CHANGE_NORTH_DIRECT", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("229", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.TWO_MODE.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_CHANGE_NORTH_DIRECT", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.notifyMapControl("229", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}

		//前方路况
		if (AsrKeyType.FRONT_TRAFFIC.equals(type)) {
//			final String reqId = BDHelper.getRequestId();
//			ExecuteTask eTask = new ExecuteTask() {
//
//				@Override
//				public boolean doExecute(ExecuteReq eo) {
//					String rId = BDHelper.checkMapCondition( reqId);
//					return rId.equals(reqId);
//				}
//			};
//			ExecuteCallBack callBack = new ExecuteCallBack() {
//				@Override
//				public void onReceive(boolean bSucc, String params) {
//					String spkTxt = params;
//					if (bSucc) {
//						spkTxt = NativeData.getResString("RS_MAP_CHECK_FRONT_TRAFFIC", 0);
//						if (BaiduVersion.getCurPackageName().equals(BaiduVersion.BAIDU_AUTONAVI_PACKAGE)) {
////							spkTxt = NativeData.getResString("RS_VOICE_BD_DOG_NAVI", 1);
//							JSONBuilder jsonBuilder=new JSONBuilder(params);
//							spkTxt = jsonBuilder.getVal("content",String.class);
//						}
//					}else {
//						spkTxt="查询前方路况失败";
//					}
//					AsrManager.getInstance().setNeedCloseRecord(true);
//					RecorderWin.speakTextWithClose(spkTxt, null);
//				}
//
//				@Override
//				public void onError(int error, String des) {
//					String spkTxt="查询前方路况失败";
//					AsrManager.getInstance().setNeedCloseRecord(true);
//					RecorderWin.speakTextWithClose(spkTxt, null);
//				}
//			};
//			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_TRAFFIC_CONDITION, eTask, callBack);
			speakFrontTraffic();
			return;
		}

		//道路限速
		if (AsrKeyType.LIMIT_SPEED.equals(type)) {
			speakLimitSpeech();
			return;
		}

		if (AsrKeyType.MEADWAR_MODE.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_MEADWAR_MODE", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.switchTTSClass("MEADWAR", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.EXPORT_MODE.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_EXPERT_MODE", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.switchTTSClass("EXPERT", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.MUTE_MODE.equals(type)) {
			doWinConfirmResp(type, command, "RS_MAP_MUTE_MODE", new RunnableCallBack() {

				@Override
				public void run() {
					taskId = BDHelper.switchTTSClass("MUTE", getTaskId());
				}

				public String getFunc() {
					return FUN_NAVI_MAP_CONTROL;
				}
			}, false);
			return;
		}
		if (AsrKeyType.VIEW_ALL.equals(type)) {
			doWinConfirmResp(type, NativeData.getResString("RS_MAP_VIEW_ALL").replace("%COMMAND%", command), "",
					new RunnableCallBack() {

						@Override
						public void run() {
							taskId = BDHelper.notifyMapControl("216", getTaskId());
						}

						public String getFunc() {
							return FUN_NAVI_MAP_CONTROL;
						}

						public boolean onResult(boolean bSucc, String params) {
							if (bSucc) {
								AppLogic.runOnBackGround(new Runnable() {

									@Override
									public void run() {
										BDHelper.notifyMapControl("217", BDHelper.getRequestId());
									}
								}, VIEW_TIME_OUT);
							}
							return super.onResult(bSucc, params);
						}
					}, false);
			return;
		}

		if (AsrKeyType.LESS_MONEY.equals(type)) {
			onWakeupReplan(type, command, "RS_MAP_LESS_MONEY", STYLE_SHAOSHOUFEI);
			return;
		}
		if (AsrKeyType.TUIJIANLUXIAN.equals(type)) {
			onWakeupReplan(type, command, "RS_MAP_TUIJIANLUXIAN", STYLE_TUIJIAN);
			return;
		}
		if (AsrKeyType.DUOBIYONGDU.equals(type)) {
			onWakeupReplan(type, command, "RS_MAP_DUOBIYONGDU", STYLE_DUOBIYONGDU);
			return;
		}
		if (AsrKeyType.BUZOUGAOSU.equals(type)) {
			onWakeupReplan(type, command, "RS_MAP_BUZOUGAOSU", STYLE_SHAOZOUGAOSU);
			return;
		}
		if (AsrKeyType.SHIJIANYOUXIAN.equals(type)){
			onWakeupReplan(type,command,"RS_MAP_SHIJIANYOUXIAN",STYLE_SHIJIANYOUXIAN);
			return;
		}
		if (AsrKeyType.GAOSUYOUXIAN.equals(type)) {
			onWakeupReplan(type, command, "RS_MAP_GAOSUYOUXIAN", STYLE_GAOSUYOUXIAN);
			return;
		}
		if (AsrKeyType.HOW_NAVI.equals(type)) {
			speakHowNavi();
//			mNavInfo = mBaiduHudInfo.getCurrNavInfo();
//			speakHowNavi(isWakeupResult);
			return;
		}
		if (AsrKeyType.ASK_REMAIN.equals(type)) {
			speakASkRemain();
//			mNavInfo = mBaiduHudInfo.getCurrNavInfo();
//			speakAskRemain(isWakeupResult);
			return;
		}

		if (AsrKeyType.BACK_NAVI.equals(type)) {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.notifyMapControl("217", reqId);
					return reqId.equals(rId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					if (bSucc) {
						AsrManager.getInstance().setNeedCloseRecord(true);
						String text = NativeData.getResString("RS_VOICE_ALREADY_DO_COMMAND_FOR_NAV_BACK_NAV");
						RecorderWin.speakTextWithClose(text, null);
					} else {
						AsrManager.getInstance().setNeedCloseRecord(true);
						if (!TextUtils.isEmpty(params)) {
							RecorderWin.speakTextWithClose(params, null);
						} else {
							RecorderWin.close();
						}
					}
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_MAP_CONTROL, eTask, callBack);
			return;
		}
		if (AsrKeyType.START_NAVI.equals(type)) {
			startNavByInner();
			RecorderWin.close();
			return;
		}
		if (AsrKeyType.EXIT_NAV.equals(type)) {
			//导航没有打开时不响应该指令
			if (!NavAppManager.getInstance().isAlreadyExitNav()){
				procBackNav(type, command, false, isWakeupResult);
			}
			return;
		}
		if (AsrKeyType.CANCEL_NAV.equals(type)) {
			if (preNavCancelCommand(isWakeupResult, command)) {
				return;
			}
			procBackNav(type, command, true, isWakeupResult);
			return;
		}
		if (AsrKeyType.CLOSE_MAP.equals(type)) {
			if (!isWakeupResult) {
				String text = NativeData.getResString("RS_VOICE_WILL_DO_COMMAND").replace("%CMD%", command);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(text, new Runnable() {

					@Override
					public void run() {
						NavManager.getInstance().exitAllNavTool();
					}
				});
				return;
			}
			doNaviExit(type, command, false);
			return;
		}
		if (AsrKeyType.SWITCH_ROLE.equals(type)) {
			mAttendCount = 3;
			checkTTS(getNextRole(), true);
			return;
		}
		if (AsrKeyType.JINSHA.equals(type)) {
			mRoleIndex = 1;
			checkTTS(sTTSRole[1], false);
			return;
		}
		if (AsrKeyType.MENGMENGDA.equals(type)) {
			mRoleIndex = 0;
			checkTTS(sTTSRole[0], false);
			return;
		}
		if (AsrKeyType.OPEN_DOG.equals(type)) {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.notifyControlDog(true, reqId);
					return rId.equals(reqId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					String spkTxt = params;
					if (bSucc) {
						spkTxt = NativeData.getResString("RS_VOICE_BD_DOG_NAVI", 0);
						if (BaiduVersion.getCurPackageName().equals(BaiduVersion.BAIDU_AUTONAVI_PACKAGE)) {
//							spkTxt = NativeData.getResString("RS_VOICE_BD_DOG_NAVI", 1);
							// 车机版打开巡航模式使用导航发过来的文本
							spkTxt = params;
						}
					}
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(spkTxt, null);
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_CRUISE, eTask, callBack);
			return;
		}
		if (AsrKeyType.CLOSE_DOG.equals(type)) {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.notifyControlDog(false, reqId);
					return rId.equals(reqId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					if (bSucc) {
						JNIHelper.logd("close dog succ");
					}
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(params, null);
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_CRUISE, eTask, callBack);
			return;
		}

//		此处为弹框确定：要先去途径的加油站吗？（走的是POI搜索）
//		现在不要这个方式，统一走沿途搜索
//		if (AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION.equals(type)) {
//			doRePlanWakeup(type, command, "RS_NAV_CMD_NAV_WAY_POI_CMD_GASTATION", new Runnable() {
//				@Override
//				public void run() {
//					switchPlanStyle(IMapInterface.PlanStyle.JIAYOUZHAN);
//					JNIHelper.logd("NavAmapAutoNavImpl start NAV_WAY_POI_CMD_GO_GASTATION");
//				}
//			});
//			return;
//		}

		// 沿途搜索
		if ( AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION.equals(type)||AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET.equals(type)
				||AsrKeyType.NAV_WAY_POI_CMD_BANK.equals(type) || AsrKeyType.NAV_WAY_POI_CMD_GAS.equals(type)
				|| AsrKeyType.NAV_WAY_POI_CMD_HOTEL.equals(type) || AsrKeyType.NAV_WAY_POI_CMD_PARK.equals(type)
				|| AsrKeyType.NAV_WAY_POI_CMD_RESTAURANT.equals(type) || AsrKeyType.NAV_WAY_POI_CMD_SERVICE.equals(type)
				|| AsrKeyType.NAV_WAY_POI_CMD_SPOTS.equals(type) || AsrKeyType.NAV_WAY_POI_CMD_TOILET.equals(type)) {
			onWayPoiCommand(type, command);
			return;
		}
	}

	/**
	 * 此处为原本的沿途搜索，其实际逻辑为通过关键字走POI搜索
	 * 但是因为百度地图的返回结果不同高德，为随机返回，并不是返回最近结果，此处舍弃
	 * @param ps
	 */
	public void switchPlanStyle(IMapInterface.PlanStyle ps) {
		String kw = null;
		if (ps == IMapInterface.PlanStyle.JIAYOUZHAN) {
			kw = "加油站";
		} else if (ps == IMapInterface.PlanStyle.CESUO) {
			kw = "厕所";
		} else if (ps == IMapInterface.PlanStyle.ATM) {
			kw = "ATM";
		} else if (ps == IMapInterface.PlanStyle.WEIXIUZHAN) {
			kw = "维修站";
		}
		if (kw != null) {
			navigateto(kw);
			return;
		}
	}

	private void navigateto(String kw) {
		UiMap.NearbySearchInfo pbneNearbySearchInfo = new UiMap.NearbySearchInfo();
		pbneNearbySearchInfo.strKeywords = kw;
		pbneNearbySearchInfo.strCenterPoi = "ON_WAY";
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, pbneNearbySearchInfo);
	}

	int mRoleIndex;

	String getNextRole() {
		mRoleIndex++;
		int length = sTTSRole.length;
		if (mRoleIndex >= length) {
			mRoleIndex = 0;
		}

		return sTTSRole[mRoleIndex];
	}

	// TODO 切换声音 出现当前已是XXX播报
	private void checkTTS(final String role, final boolean isRandom) {
		try {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.switchTtsRole(reqId, role);
					return reqId.equals(rId);
				}

				@Override
				public long getTimeOut() {
					return TIME_OUT_DELAY;
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					String slot = "";
					String okHint = "";
					if (mRoleIndex == 0) {
						slot = NativeData.getResString("RS_NAV_CMD_ROLE_MM");
					} else if (mRoleIndex == 1) {
						slot = NativeData.getResString("RS_NAV_CMD_ROLE_JS");
					}
					if (bSucc) { // 成功的话播报切换成功
						okHint = NativeData.getResPlaceholderString("RS_NAV_BAIDU_SET_ROLE", "%ROLE%", slot);
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(okHint, null);
						return;
						// } else if (isRandom) {
						//
					}

					// if (isRandom && !bSucc) {
					// JNIHelper.logd("switch tts random fail, next check");
					// checkTTS(getNextRole(), isRandom);
					// return;
					// }
					//
					// if (bSucc) {
					// okHint =
					// NativeData.getResPlaceholderString("RS_NAV_BAIDU_SET_ROLE",
					// "%ROLE%", slot);
					// } else {
					// okHint =
					// NativeData.getResPlaceholderString("RS_NAV_BAIDU_ALREADY_ROLE",
					// "%ROLE%", slot);
					// }
					//
					// AsrManager.getInstance().setNeedCloseRecord(true);
					// RecorderWin.speakTextWithClose(okHint, null);
				}

				@Override
				public void onReceive(int errCode, String params) {
					if (errCode == 1) {
						if (!isRandom) {
							if (params.contains("已经是")) {
								String slot = "";
								if (mRoleIndex == 0) {
									slot = NativeData.getResString("RS_NAV_CMD_ROLE_MM");
								} else if (mRoleIndex == 1) {
									slot = NativeData.getResString("RS_NAV_CMD_ROLE_JS");
								}
								AsrManager.getInstance().setNeedCloseRecord(true);
								String text = NativeData.getResPlaceholderString("RS_NAV_BAIDU_ALREADY_ROLE", "%ROLE%",
										slot);
								RecorderWin.speakTextWithClose(text, null);
								return;
							}
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(params, null);
							return;
						}

						if (mAttendCount > 0) {
							mAttendCount--;
							checkTTS(getNextRole(), true);
						} else {
							String spks = params;
							if (params.contains("已经是")) {
								spks = NativeData.getResString("RS_VOICE_BAIDU_CHECK_TTS_FAIL");
							}
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(spks, null);
						}
					}
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_TTS_CONTROL, eTask, callBack);
		} catch (Exception e) {
		}
	}

	int mAttendCount;

	/**
	 * 没有将为您的场景
	 * 
	 * @param mapOrder
	 * @param hintResId
	 */
	private void doQuickResponse(final String mapOrder, final String hintResId) {
		if (!BDHelper.isNewSDKVersion()) {
			BDHelper.notifyMapControl(mapOrder, "");
			onSelect(true, hintResId, null);
			return;
		}

		final String reqId = com.baidu.navicontroller.sdk.NaviControllerManager.getInstance().getRequestId();
		String func = FUN_NAVI_MAP_CONTROL;
		ExecuteTask task = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String requestId = BDHelper.notifyMapControl(mapOrder, reqId);
				return reqId.equals(requestId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
				if (bSucc) {
					onSelect(true, hintResId, null);
				} else {
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(params, null);
				}
			}

			@Override
			public void onError(int error, String des) {
				JNIHelper.loge("errorNo:" + error);
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_BD_TIME_OUT"), null);
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, func, task, callBack);
	}

	private void doWinConfirmResp(String type, String speech, String hintResId, final RunnableCallBack rcb,
			boolean isNeedConfirm) {
		String aimSlot = NativeData.getResString(hintResId);
		if (TextUtils.isEmpty(aimSlot)) {
			aimSlot = speech;
		}

		if (processRemoteIsConfirm(type)) {
			isNeedConfirm = !isNeedConfirm;
		}

		String spk = NativeData.getResPlaceholderString("RS_MAP_DIALOG_HINT", "%COMMAND%", aimSlot);
		final String okHint = checkHint(type);
		if (isNeedConfirm) {
			String left = NativeData.getResString("RS_MAP_CONFIRM_CANCEL_ASR");
			String right = NativeData.getResString("RS_MAP_CONFIRM_SURE_ASR");
			showWinConfirm("提示", left, right, spk, new Runnable() {

				@Override
				public void run() {
					final String reqId = BDHelper.getRequestId();
					ExecuteTask task = new ExecuteTask() {

						@Override
						public boolean doExecute(ExecuteReq eo) {
							rcb.taskId = reqId;
							rcb.run();
							String rId = rcb.getTaskId();
							return reqId.equals(rId);
						}
					};
					ExecuteCallBack callBack = new ExecuteCallBack() {

						@Override
						public void onReceive(boolean bSucc, String params) {
							if (rcb.onResult(bSucc, params)) {
								return;
							}

							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(okHint, null);
						}
					};
					AsyncExecutor.getInstance().doAsyncExec(reqId, rcb.getFunc(), task, callBack);
				}
			}, null);
		} else {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask task = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					rcb.taskId = reqId;
					rcb.run();
					String rId = rcb.getTaskId();
					return reqId.equals(rId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					bSucc = true;//不播报百度返回的文本，播报默认
					if (bSucc) {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(okHint, null);
					} else {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(params, null);
					}
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, rcb.getFunc(), task, callBack);
		}
	}

	private void showWinConfirm(final String title, final String left, final String right, final String msg,
			final Runnable task, final Runnable cancelRun) {
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
					RecorderWin.close();
					return;
				}

				mWinConfirmAsr = new DialogRecord(new WinConfirmAsrBuildData()) {
					
					@Override
					public String getReportDialogId() {
						return "baidu_nav_virtual_dialog";
					}

					@Override
					public void onClickOk() {
						if (cancelRun != null) {
							cancelRun.run();
							return;
						}

						TtsManager.getInstance().speakText(NativeData.getResString("RS_MAP_CONFIRM_CANCEL_SURE"));
					}

					@Override
					public void onClickCancel() {
						if (task != null) {
							task.run();
						}
					}
				}.setHintTts(msg).setMessage(msg).setSureText(left, new String[] { left }).setCancelText(right,
						new String[] { right });
				if (!TextUtils.isEmpty(title)) {
					((DialogRecord) mWinConfirmAsr).setTitle(title);
				}

				((DialogRecord) mWinConfirmAsr).showVirtual();
			}
		}, 0);
	}

	private void onWakeupReplan(final String nav_type, final String speech, final String resId, final int next) {
		int curStyle = BDHelper.queryCurrentStyle();
		if (curStyle == -1 && BDHelper.isNewSDKVersion()) {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.queryCurrentStyleNewSDK(reqId);
					return reqId.equals(rId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String strData) {
					if (bSucc) {
						if (!TextUtils.isEmpty(strData)) {
							JSONBuilder jb = new JSONBuilder(strData);
							String type = jb.getVal("navi_preference", String.class);
							onReplanRoute(nav_type, Integer.parseInt(type), resId, speech, next, true);
						}
					} else {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose("查询当前路径策略失败", null);
					}
				}

				@Override
				public void onError(int error, String des) {
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(des, null);
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_STATE, eTask, callBack);
			return;
		}

		onReplanRoute(nav_type, curStyle, resId, speech, next, true);
		return;
	}

	private void onReplanRoute(final String type, int cur, final String resId, final String speech, final int next, boolean isSync) {

		if (cur == next) {
			String tts = NativeData.getResString("RS_MAP_NAVI_STYLE_HINT").replace("%COMMAND%",
					NativeData.getResString(resId));
			if (tts.contains("路线")) {
				tts = tts.replace("路线", "");
			}
			if (!tts.contains("路线")) {
				tts = tts + "路线";
			}
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(tts, null);
			return;
		}
		if (processRemoteIsConfirm(type)) {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.rePlanWithStyle(next, reqId);
					return reqId.equals(rId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					if (bSucc) {
						String okHint = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK");
						okHint = okHint.replace("%COMMAND%", NativeData.getResString(resId));
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(okHint, null);
						BDHelper.setDefaultStyle(next);
					} else {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(params, null);
					}
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_SET, eTask, callBack);
			return;
		}

		//取消弹窗，直接执行
		final String reqId = BDHelper.getRequestId();
		final ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.rePlanWithStyle(next, reqId);
				return reqId.equals(rId);
			}
		};

		if(isSync){
			String okHint = NativeData.getResString("RS_VOICE_DOING_COMMAND");
//			okHint = okHint.replace("%COMMAND%", NativeData.getResString(resId));
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(okHint, new Runnable() {
				@Override
				public void run() {
					AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_SET, eTask, null);
				}
			});
			BDHelper.setDefaultStyle(next);
		}else{
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					String okHint = NativeData.getResString("RS_MAP_CONFIRM_SURE_SPK");
					okHint = okHint.replace("%COMMAND%", NativeData.getResString(resId));
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(okHint, null);
					BDHelper.setDefaultStyle(next);
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_SET, eTask, callBack);
		}
	}
	
	private void procBackNav(String type, String speech, boolean isCancelPath, boolean isWakeupResult) {
		if (!isWakeupResult && !(isCancelPath && isInNav())) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_BD_EXIT_NAV_OK");
			RecorderWin.speakTextWithClose(spk, new Runnable() {

				@Override
				public void run() {
					NavManager.getInstance().exitAllNavTool();
				}
			});
			return;
		}

		doNaviExit(type, speech, isCancelPath && isInNav());
	}

	private void doNaviExit(String type, String speech,boolean isCancelPath) {
		final boolean isExitNav = isInNav() && ("结束导航".equals(speech) || "停止导航".equals(speech)) || isCancelPath;
		String okHint = NativeData.getResString("RS_MAP_BD_EXIT_NAV_OK");
		if (!processRemoteIsConfirm(type)) {
			String title = "";
			String msg = NativeData.getResString("RS_MAP_BD_EXIT_APP");
			String right = NativeData.getResString("RS_MAP_BD_EXIT_BTN_OK");
			String left = NativeData.getResString("RS_MAP_BD_EXIT_BTN_CANCEL");
			if (isExitNav) {
				title = "";
				left = "取消";
				right = "确定";
				msg = NativeData.getResString("RS_MAP_BD_EXIT_NAV");
				okHint = NativeData.getResString("RS_MAP_BD_EXIT_NAV_OK");
			}

			final String okSpk = okHint;
			final String cancelSpk = left;
			showWinConfirm(title, left, right, msg, new Runnable() {

				@Override
				public void run() {
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(okSpk, new Runnable() {

						@Override
						public void run() {
							if (isExitNav) {
								BDHelper.exitNavStatus();
							} else {
								NavManager.getInstance().exitAllNavTool();
							}
						}
					});
				}
			}, isExitNav ? null : new Runnable() {

				@Override
				public void run() {
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose("已" + cancelSpk, null);
				}
			});
		} else {
			String spk = NativeData.getResString("RS_MAP_BD_EXIT_NAV_OK");
			AsrManager.getInstance().setNeedCloseRecord(true);
			RecorderWin.speakTextWithClose(spk, new Runnable() {

				@Override
				public void run() {
					if (isExitNav) {
						BDHelper.exitNavStatus();
					} else {
						NavManager.getInstance().exitAllNavTool();
					}
				}
			});
		}
	}

	boolean isReady() {
		return mClientBind && mServiceBind && BDHelper.isServiceBind();
	}

	/**
	 * 进程重启后初始化参数
	 */
	private void reinitTXZProcess() {
		if (!isReady()) {
			JNIHelper.loge("ServiceBind:[client" + mClientBind + ",service:" + mServiceBind);
			return;
		}

		if (BDHelper.isNewSDKVersion()) {
			queryMapState(1);
			queryMapState(2);
			queryIsDogMode();

			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					queryAddressInterval();
				}
			}, DELAY_TIME_TO_QUERY);
		} else {
			boolean isFore = BDHelper.isForground();
			boolean isNavi = BDHelper.isInNavi();
			if (isFore) {
				onResume();
			}

			if (isNavi) {
				onQueryNavi();
			}
			JNIHelper.logd("isForeground:" + isFore + ",isNavi:" + isNavi);

			Poi poi = BDHelper.queryHomeAddress();
			setAddressLoc(true, poi);
			Poi poi2 = BDHelper.queryCompanyAddress();
			setAddressLoc(false, poi2);
		}
	}

	private void queryMapState(final int queryType) {
		final String reqId = BDHelper.getRequestId();
		JNIHelper.loge("queryMapState type:" + queryType + ",reqId:" + reqId);
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = "";
				if (queryType == 1) {
					rId = BDHelper.requestForground(reqId);
				} else if (queryType == 2) {
					rId = BDHelper.requestInNavi(reqId);
				}
				return reqId.equals(rId);
			}
		};

		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
				JNIHelper.loge("queryMapState:" + bSucc + ",params:" + params);
				if (bSucc) {
					if (!TextUtils.isEmpty(params)) {
						JSONBuilder jb = new JSONBuilder(params);
						String val = jb.getVal("foreground", String.class);
						if (val != null) {
							if ("true".equals(val)) {
								onResume();
							}
							return;
						}
						val = jb.getVal("is_innavi", String.class);
						if (val != null) {
							if ("true".equals(val)) {
								onQueryNavi();
							}
							return;
						}
					}
				}
			}

			@Override
			public void onError(int error, String des) {
				JNIHelper.loge("queryMapState onError:" + error + ",des:" + des);
			}
		};
		callBack.mHasTimeOut = false;
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_STATE, eTask, callBack);
	}

	private void queryIsDogMode() {
		final String reqId = BDHelper.getRequestId();
		ExecuteTask et = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.requestIsDog(reqId);
				return reqId.equals(rId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
				JNIHelper.logd("queryDog:" + bSucc + "," + params);
				if (bSucc) {
					JSONBuilder jb = new JSONBuilder(params);
					String is_incruise = jb.getVal("is_incruise", String.class);
					if (is_incruise != null) {
						if ("true".equals(is_incruise)) {
							mIsDogMode = true;
							forceCheckInNav();
						} else {
							mIsDogMode = false;
							forceCheckInNav();
						}
					}
				}
			}
		};
		callBack.mHasTimeOut = false;
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_STATE, et, callBack);
	}

	private void queryAddressRecord(final boolean isHome) {
		final String reqId = BDHelper.getRequestId();
		JNIHelper.loge("queryAddressRecord isHome:" + isHome + ",reqId:" + reqId);
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.queryAddress(isHome, reqId);
				return reqId.equals(rId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
				JNIHelper.loge("queryAddressRecord bSucc:" + bSucc + ",params:" + params);
				if (bSucc) {
					setAddressLoc(isHome, BDHelper.convertPoi(params));
				}
			}

			@Override
			public void onError(int error, String des) {
				JNIHelper.loge("queryAddressRecord onError:" + error + ",des:" + des);
			}
		};
		callBack.mHasTimeOut = false;
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_SYN_ADDRESS, eTask, callBack);
	}

	private void onQueryNavi() {
		onStart();

		// 查询当前导航路线信息
		if (BDHelper.isNewSDKVersion()) {
			final String reqId = BDHelper.getRequestId();
			ExecuteTask eTask = new ExecuteTask() {

				@Override
				public boolean doExecute(ExecuteReq eo) {
					String rId = BDHelper.queryNaviRoadInfoNewSDK(reqId);
					return reqId.equals(rId);
				}
			};
			ExecuteCallBack callBack = new ExecuteCallBack() {

				@Override
				public void onReceive(boolean bSucc, String params) {
					try {
						if (bSucc) {
							mNavWayInfo = NavBaiduFactory.getNavInfoFromQueryResult(params);
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}

				@Override
				public void onError(int error, String des) {
				}
			};
			AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_NAVI_STATE, eTask, callBack);
		} else {
			mNavWayInfo = BDHelper.queryNaviRoadInfo();
		}
	}

	private void setAddressLoc(boolean isHome, Poi poi) {
		JNIHelper.logd("set " + (isHome ? "Home" : "Company") + ":" + (poi != null ? poi.toString() : ""));
		if (poi != null && poi.getLat() != 0 && poi.getLng() != 0) {
			if (isHome) {
				NavManager.getInstance().setHomeLocation(poi.getName(), poi.getGeoinfo(), poi.getLat(), poi.getLng(),
						UiMap.GPS_TYPE_GCJ02, false);
			} else {
				NavManager.getInstance().setCompanyLocation(poi.getName(), poi.getGeoinfo(), poi.getLat(), poi.getLng(),
						UiMap.GPS_TYPE_GCJ02, false);
			}
			return;
		}

		if (poi == null || poi.getLat() == 0 || poi.getLng() == 0) {
			if (isHome) {
				NavManager.getInstance().clearHomeLocation();
			} else {
				NavManager.getInstance().clearCompanyLocation();
			}
		}
	}

	@Override
	public void exitNav() {
		AppLogic.removeBackGroundCallback(mExitNavRunnable);
		AppLogic.runOnBackGround(mExitNavRunnable, 20);
		if (!BaiduVersion.isSupportProt(false)) {
			super.exitNav();
		}
	}

	Runnable mExitNavRunnable = new Runnable() {

		@Override
		public void run() {
			JNIHelper.logd("exitNavStatus ...");
			BDHelper.exitNavStatus();
			AppLogic.runOnBackGround(new Runnable() {

				@Override
				public void run() {
					/* 百度地图的内部语音SDK有一个bug
					 *  内部语音会缓存导航关闭前的最后一个指令并重新运行
					 *  这样的话如果调用关闭导航的指令，
					 *  重新打开的话导航会根据缓存的最后一道关闭导航的指令，再次关闭导航
					 *  修改方式：当导航没有打开的时候不响应关闭导航的指令*/
					if (!NavAppManager.getInstance().isAlreadyExitNav()){
						JNIHelper.logd("exitNavApp ...");
						BDHelper.exitNavApp();
					}
				}
			}, 200);
		}
	};

	@Override
	public String getPackageName() {
		return BaiduVersion.getCurPackageName();
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		try {
			if (!BaiduVersion.isSupportProt(false)) {
				navi(plan, info);
				return true;
			}
			if (!isReady()) {
				mRetrybind.check();
				addBindTask(mNaviTask);
			}
			mNaviTask.update(info.strTargetName, info.msgGpsInfo.dblLat, info.msgGpsInfo.dblLng, -1);
			AppLogic.removeBackGroundCallback(mNaviTask);
			AppLogic.runOnBackGround(mNaviTask, 20);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public boolean isInNav() {
		return mIsStarted;
	}

	@Override
	public boolean showTraffic(final String city, final String addr) {
		if (!BaiduVersion.isSupportProt(false)) {
			return false;
		}

		long delay = 0;
		if (!mIsFocus) {
			delay = DEFAULT_DELAY_TIME;
			BDHelper.startNavApp(GlobalContext.get());
		}

		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				final String reqId = BDHelper.getRequestId();
				ExecuteTask task = new ExecuteTask() {

					@Override
					public boolean doExecute(ExecuteReq eo) {
						String rId = BDHelper.notifyQueryTraffic(city, addr, reqId);
						return reqId.equals(rId);
					}
				};
				ExecuteCallBack callBack = new ExecuteCallBack() {

					@Override
					public void onReceive(boolean bSucc, String params) {
					}

					@Override
					public void onReceive(int errCode, String params) {
						String disclaimTxt = NativeData.getResString("RS_NAV_BAIDU_DISCLAIMER");
						if (errCode == MAP_CONTROL_STATE_ERROR && params.equals(disclaimTxt)) {
							AsrManager.getInstance().setNeedCloseRecord(true);
							RecorderWin.speakTextWithClose(disclaimTxt, null);
							return;
						}
					}
				};
				callBack.mHasTimeOut = false;
				AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_QUERY_TRAFFIC, task, callBack);
			}
		}, delay);
		return true;
	}

	@Override
	public void enterNav() {
		if (!BaiduVersion.isSupportProt(false)) {
			super.enterNav();
			return;
		}

		try {
			BDHelper.startNavApp(GlobalContext.get());
			if (mRetrybind == null) {
				AppLogic.removeBackGroundCallback(mInitBdSdk);
				AppLogic.runOnBackGround(mInitBdSdk, 50);
				return;
			}
			mRetrybind.check();
		} catch (Exception e) {
		}
	}

	@Override
	public void onStart() {
		super.onStart();
		try {
			// 如果HUD没有连上，尝试打开一下
			if (!BNRemoteVistor.getInstance().isConnect()) {
				// BNRemoteVistor.getInstance().open();
				mBaiduConnect.checkToConnect();
			}
		} catch (Exception e) {
		}
	}

	@Override
	public List<String> getBanCmds() {
		List<String> cmds = new ArrayList<String>();
//		cmds.add(AsrKeyType.TWO_MODE);
//		cmds.add(AsrKeyType.THREE_MODE);

		if (!BDHelper.isNewSDKVersion()) {
//			cmds.add(AsrKeyType.FRONT_TRAFFIC);
			cmds.add(AsrKeyType.LIMIT_SPEED);

			cmds.add(AsrKeyType.TWO_MODE);
			cmds.add(AsrKeyType.THREE_MODE);
			cmds.add(AsrKeyType.SWITCH_ROLE);
			cmds.add(AsrKeyType.CAR_DIRECT);
			cmds.add(AsrKeyType.NORTH_DIRECT);
			cmds.add(AsrKeyType.MEADWAR_MODE);
			cmds.add(AsrKeyType.EXPORT_MODE);
			cmds.add(AsrKeyType.MUTE_MODE);
//			cmds.add(AsrKeyType.LIMIT_SPEED);
			cmds.add(AsrKeyType.MENGMENGDA);
			cmds.add(AsrKeyType.JINSHA);
			cmds.add(AsrKeyType.OPEN_DOG);
			cmds.add(AsrKeyType.CLOSE_DOG);
		}
//		if (!isNewVersion()) {
//			cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION);
//			cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET);
//		}
		return cmds;
	}

	@Override
	public String[] getSupportCmds() {
		return new String[] {
				AsrKeyType.FRONT_TRAFFIC,
				AsrKeyType.LIMIT_SPEED,

				AsrKeyType.ZOOM_IN, 
				AsrKeyType.ZOOM_OUT, 
				AsrKeyType.NIGHT_MODE, 
				AsrKeyType.LIGHT_MODE,
				AsrKeyType.EXIT_NAV, 
				AsrKeyType.CANCEL_NAV,
				AsrKeyType.CLOSE_MAP, 
				AsrKeyType.VIEW_ALL, 
				AsrKeyType.TUIJIANLUXIAN,
				AsrKeyType.DUOBIYONGDU, 
				AsrKeyType.BUZOUGAOSU, 
				AsrKeyType.GAOSUYOUXIAN,
				AsrKeyType.SHIJIANYOUXIAN,
				AsrKeyType.LESS_MONEY,
				AsrKeyType.LESS_DISTANCE, 
				AsrKeyType.HOW_NAVI, 
				AsrKeyType.ASK_REMAIN,
//				AsrKeyType.LIMIT_SPEED,
				AsrKeyType.BACK_NAVI,

				//开始导航在下方已经注册了对应的指令内容和事件，此处不再需要重复注册
//				AsrKeyType.START_NAVI,
				
				AsrKeyType.OPEN_TRAFFIC, 
				AsrKeyType.CLOSE_TRAFFIC,
				AsrKeyType.TWO_MODE,
				AsrKeyType.THREE_MODE, 
				AsrKeyType.CAR_DIRECT, 
				AsrKeyType.NORTH_DIRECT,
				AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION,
				AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET,
//				AsrKeyType.NAV_WAY_POI_CMD_BANK,
//				AsrKeyType.NAV_WAY_POI_CMD_GAS, 
//				AsrKeyType.NAV_WAY_POI_CMD_HOTEL,
//				AsrKeyType.NAV_WAY_POI_CMD_PARK, 
//				AsrKeyType.NAV_WAY_POI_CMD_RESTAURANT, 
//				AsrKeyType.BACK_HOME,
//				AsrKeyType.GO_COMPANY, 
//				AsrKeyType.NAV_WAY_POI_CMD_SERVICE, 
//				AsrKeyType.NAV_WAY_POI_CMD_SPOTS,
				AsrKeyType.MEADWAR_MODE, 
				AsrKeyType.EXPORT_MODE, 
				AsrKeyType.MUTE_MODE,
//				AsrKeyType.NAV_WAY_POI_CMD_TOILET, 
//				AsrKeyType.SWITCH_ROLE, 
//				AsrKeyType.MENGMENGDA, 
//				AsrKeyType.JINSHA,
				AsrKeyType.OPEN_DOG, 
				AsrKeyType.CLOSE_DOG
		};
	}

	@Override
	public List<String> getCmdNavOnly() {
		List<String> cmds = new ArrayList<String>();
		cmds.add(AsrKeyType.VIEW_ALL);
		cmds.add(AsrKeyType.TUIJIANLUXIAN);
		cmds.add(AsrKeyType.DUOBIYONGDU);
		cmds.add(AsrKeyType.BUZOUGAOSU);
		cmds.add(AsrKeyType.GAOSUYOUXIAN);
		cmds.add(AsrKeyType.SHIJIANYOUXIAN);
		cmds.add(AsrKeyType.LESS_MONEY);
		cmds.add(AsrKeyType.LESS_DISTANCE);
		cmds.add(AsrKeyType.HOW_NAVI);
		cmds.add(AsrKeyType.ASK_REMAIN);
		cmds.add(AsrKeyType.FRONT_TRAFFIC);
//		cmds.add(AsrKeyType.LIMIT_SPEED);
//		if (!mIsDogMode) {
//			cmds.add(AsrKeyType.LIMIT_SPEED);
//		}
		cmds.add(AsrKeyType.BACK_NAVI);

		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GO_GASTATION);
		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GO_TOILET);

		//新手模式和专家模式和静音模式
//		cmds.add(AsrKeyType.MEADWAR_MODE);
//		cmds.add(AsrKeyType.EXPORT_MODE);
//		cmds.add(AsrKeyType.MUTE_MODE);
		// 车方位模式
//		cmds.add(AsrKeyType.CAR_DIRECT);
//		cmds.add(AsrKeyType.NORTH_DIRECT);

//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_BANK);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_GAS);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_HOTEL);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_PARK);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_RESTAURANT);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_SERVICE);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_SPOTS);
//		cmds.add(AsrKeyType.NAV_WAY_POI_CMD_TOILET);

//		cmds.add(AsrKeyType.SWITCH_ROLE);
//		cmds.add(AsrKeyType.MENGMENGDA);
//		cmds.add(AsrKeyType.JINSHA);
		return cmds;
	}

	@Override
	public int getMapVersion() {
		return BDHelper.getVersionNum();
	}
	
	@Override
	public void queryHomeCompanyAddr() {
		queryAddr();
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		BDHelper.updatePointLocation(navigateInfo, "home");
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		BDHelper.updatePointLocation(navigateInfo, "office");
	}

	private boolean isNewVersion(){
		return BaiduVersion.BAIDU_AUTONAVI_PACKAGE.equals(BaiduVersion.getCurPackageName())&&
				BDHelper.isNewSDKVersion();
	}

	@Override
	public String disableNavWithWayPoi() {
		if (isNewVersion()) {
			return "";
		}
		return super.disableNavWithWayPoi();
	}

	@Override
	public boolean navigateWithWayPois(final Poi startPoi, final Poi endPoi,
			final List<PathInfo.WayInfo> pois) {
		try {
			if (!isReady()) {
				mRetrybind.check();
				addBindTask(mNaviWithPoiTask);
			}
			mNaviWithPoiTask.update(startPoi, endPoi, pois, -1);
			AppLogic.removeBackGroundCallback(mNaviWithPoiTask);
			AppLogic.runOnBackGround(mNaviWithPoiTask, 20);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public String disableProcJingYouPoi() {
		if (BaiduVersion.BAIDU_NAVI_PACKAGE.equals(BaiduVersion.getCurPackageName())) {
			return "";
		}

		if (isNewVersion()) {
			return "";
		}

		return super.disableProcJingYouPoi();
	}

	@Override
	public boolean procJingYouPoi(final Poi... pois) {
		super.procJingYouPoi(pois);
		// 非定制版不支持
		if (!BaiduVersion.isSupportProt(false)) {
			return false;
		}
		// 汽车版不支持途经点
		if (!isNewVersion()) {
			return false;
		}

		final String reqId = BDHelper.getRequestId();
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				BDHelper.notifyInsertPoint(pois[0], reqId);
				return true;
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
			}

			@Override
			public void onError(int error, String des) {
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_ADD_VIA_POINT, eTask, callBack);
		return true;
	}

	@Override
	public boolean deleteJingYou(final Poi poi) {
		return true;
	}

	@Override
	protected void queryLimitSpeed() {
		if (!isInNav()) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_LIMITSPEED_NONAV");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}
		
		final String reqId = BDHelper.getRequestId();
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.queryLimitSpeed(reqId);
				return reqId.equals(rId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {

			@Override
			public void onReceive(boolean bSucc, String params) {
				JNIHelper.logd("Limit bSucc:" + bSucc + ",params:" + params);
				JSONBuilder jb = new JSONBuilder(params);
				Integer limit = jb.getVal("limsp", Integer.class);
				if (bSucc) {
					if (limit != null) {
						AsrManager.getInstance().setNeedCloseRecord(true);
						String spk = NativeData.getResPlaceholderString("RS_MAP_SPEEDLIMIT", "%SPEED%", limit + "");
						RecorderWin.speakTextWithClose(spk, null);
					} else {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(NativeData.getResString("RS_MAP_NO_SPEEDLIMIT"), null);
					}
				} else {
					if (limit != null) {
						AsrManager.getInstance().setNeedCloseRecord(true);
						RecorderWin.speakTextWithClose(NativeData.getResString("RS_MAP_NO_SPEEDLIMIT"), null);
					}
				}
			}

			@Override
			public void onError(int error, String des) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_MAP_NO_SPEEDLIMIT"), null);
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_LIMIT_SPEED, eTask, callBack);
	}
	
	@Override
	public boolean speakLimitSpeech() {
		queryLimitSpeed();
		return true;
	}

	protected void queryFrontTraffic(){
		if (!isInNav()) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_FRONTTRAFFIC_NONAV");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}

		final String reqId = BDHelper.getRequestId();
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.checkMapCondition( reqId);
				return rId.equals(reqId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {
			@Override
			public void onReceive(boolean bSucc, String params) {
				String spkTxt = params;
				if (bSucc) {
					spkTxt = NativeData.getResString("RS_MAP_CHECK_FRONT_TRAFFIC", 0);
					if (BaiduVersion.getCurPackageName().equals(BaiduVersion.BAIDU_AUTONAVI_PACKAGE)) {
						JSONBuilder jsonBuilder=new JSONBuilder(params);
						spkTxt = jsonBuilder.getVal("content",String.class);
					}
				}else {
					spkTxt=NativeData.getResString("RS_MAP_NO_FRONTTRAFFIC");
				}
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spkTxt, null);
			}

			@Override
			public void onError(int error, String des) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_MAP_NO_FRONTTRAFFIC"), null);
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_TRAFFIC_CONDITION, eTask, callBack);
	}
	public boolean speakFrontTraffic(){
		queryFrontTraffic();
		return true;
	}

	protected void queryHowNavi(){
		if (!isInNav()) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_HOWNAVI_NONAV");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}

		final String reqId = BDHelper.getRequestId();
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.checkHowNavi( reqId);
				return rId.equals(reqId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {
			@Override
			public void onReceive(boolean bSucc, String params) {
				String spkTxt = params;
				if (bSucc) {
					spkTxt = NativeData.getResString("RS_MAP_CHECK_HOW_NAVI", 0);
					if (BaiduVersion.getCurPackageName().equals(BaiduVersion.BAIDU_AUTONAVI_PACKAGE)) {
						JSONBuilder jsonBuilder=new JSONBuilder(params);
						spkTxt = jsonBuilder.getVal("content",String.class);
					}
				}else {
					spkTxt=NativeData.getResString("RS_MAP_NO_HOW_NAVI");
				}
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spkTxt, null);
			}

			@Override
			public void onError(int error, String des) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_MAP_NO_HOW_NAVI"), null);
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_ASK_FORWARD, eTask, callBack);
	}

	public boolean speakHowNavi(){
		queryHowNavi();
		return true;
	}

	protected void queryASkRemain(){
		if (!isInNav()) {
			AsrManager.getInstance().setNeedCloseRecord(true);
			String spk = NativeData.getResString("RS_MAP_RESTINFO_NONAV");
			RecorderWin.speakTextWithClose(spk, null);
			return;
		}

		final String reqId = BDHelper.getRequestId();
		ExecuteTask eTask = new ExecuteTask() {

			@Override
			public boolean doExecute(ExecuteReq eo) {
				String rId = BDHelper.checkRestInfo( reqId);
				return rId.equals(reqId);
			}
		};
		ExecuteCallBack callBack = new ExecuteCallBack() {
			@Override
			public void onReceive(boolean bSucc, String params) {
				String spkTxt = params;
				if (bSucc) {
					spkTxt = NativeData.getResString("RS_MAP_CHECK_REST_INFO", 0);
					if (BaiduVersion.getCurPackageName().equals(BaiduVersion.BAIDU_AUTONAVI_PACKAGE)) {
						JSONBuilder jsonBuilder=new JSONBuilder(params);

						Long remainTime = jsonBuilder.getVal("rest_time",Long.class);
						Long remainDistance = jsonBuilder.getVal("rest_distance",Long.class);

						String rt=getRemainTime(remainTime);
						String rd=getRemainDistance(remainDistance);
						if (!TextUtils.isEmpty(rt) && !TextUtils.isEmpty(rd)) {
							spkTxt = NativeData.getResString("RS_MAP_DESTINATION_ABOUT").replace("%DISTANCE%", rd).replace("%TIME%", rt);
						} else if (!TextUtils.isEmpty(rd)) {
							spkTxt = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_DIS", "%DISTANCE%", rd);
						} else if (!TextUtils.isEmpty(rt)) {
							spkTxt = NativeData.getResPlaceholderString("RS_MAP_DESTINATION_TIME", "%TIME%", rt);
						}
					}
				}else {
					spkTxt=NativeData.getResString("RS_MAP_NO_REST_INFO");
				}
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(spkTxt, null);
			}

			@Override
			public void onError(int error, String des) {
				AsrManager.getInstance().setNeedCloseRecord(true);
				RecorderWin.speakTextWithClose(NativeData.getResString("RS_MAP_NO_HOW_NAVI"), null);
			}
		};
		AsyncExecutor.getInstance().doAsyncExec(reqId, FUN_NAVI_REST_INFO, eTask, callBack);
	}

	public boolean speakASkRemain(){
		queryASkRemain();
		return true;
	}

	private void doOnEvent(int event, String msg) {
		JNIHelper.logd("doOnEvent:" + event + ",msg:" + msg);
		switch (event) {
		case SDKConstants.EVENT_CLIENT_CONNECTED:
			mClientBind = true;
			break;

		case SDKConstants.EVENT_SERVICE_CONNECTED:
			broadcastSDKAidlEvent(true);
			mServiceBind = true;
			break;

		case SDKConstants.EVENT_CLIENT_DISCONNECTED:
			mClientBind = false;
			break;

		case SDKConstants.EVENT_SERVICE_DISCONNECTED:
			broadcastSDKAidlEvent(false);
			mServiceBind = false;
			break;

		case SDKConstants.EVENT_SERVICE_CONNECT_FAIL:
			mServiceBind = false;
			break;
		}

		if (!isReady()) {
			resetUnbind();
		}

		try {
//			reInitNav(false, false);
			mRetrybind.check();
			reinitTXZProcess();
		} catch (Exception e) {
			JNIHelper.loge("NaviControl onEvent Error:" + e.toString());
		}

		procBindTask();
	}
	
	private void broadcastSDKAidlEvent(boolean isConnected) {
		Intent intent = new Intent("com.txznet.txz.bdnav");
		intent.putExtra("isConnect", isConnected);
		GlobalContext.get().sendBroadcast(intent);
	}

	List<Runnable> mBindTasks = new ArrayList<Runnable>();

	private void procBindTask() {
		synchronized (mBindTasks) {
			if (mBindTasks.size() > 0) {
				for (Runnable task : mBindTasks) {
					if (task != null) {
						task.run();
						mBindTasks.remove(task);
					}
				}
			}
		}
	}

	private void addBindTask(Runnable task) {
		synchronized (mBindTasks) {
			mBindTasks.add(task);
		}
	}

	private boolean doOnNotification(String func, String params) {
		if (FUN_NAVI_PARK_REC.equals(func)) {
			int show = 0;
			try {
				JSONObject jsonObject = new JSONObject(params);
				show = jsonObject.optInt("show");
			} catch (JSONException e) {
				e.printStackTrace();
			}
			switch (show) {
			case 1:
				// 停车场出现
				NotifyDialog nd = NavNotifyQueues.getInstance().createDialog(FUN_NAVI_PARK_REC);
				nd.addCmds("CTRL$PARK_HERE", new Runnable() {

					@Override
					public void run() {
						BDHelper.parkHere();
					}
				}, "停这里", "导航过去");
				nd.build();
				break;

			case 0:
				// 停车场消失
				NavNotifyQueues.getInstance().removeDialog(FUN_NAVI_PARK_REC);
				break;

			default:
				break;
			}
			return true;
		}

		if (FUN_NAVI_TTS.equals(func)) {
			String event = null;
			try {
				JSONObject jsonObject = new JSONObject(params);
				event = jsonObject.optString("event");
			} catch (JSONException e) {
				e.printStackTrace();
			}

			if (NAVI_TTS_START.equals(event)) {
				checkTtsStartOrEnd(true);
				TXZMediaFocusManager.getInstance().requestFocus();
			} else if (NAVI_TTS_END.equals(event)) {
				checkTtsStartOrEnd(false);
				TXZMediaFocusManager.getInstance().releaseFocus();
			}
			return true;
		}

		if (FUN_NAVI_STATUS_SYNC.equals(func)) {
			String event = null;
			try {
				JSONObject jsonObject = new JSONObject(params);
				event = jsonObject.optString("event");
			} catch (Exception e) {
				event = params;
				e.printStackTrace();
			}
			if (NAVI_APP_LAUNCH.equals(event)) {
				queryAddressInterval();

				/*  在导航起来之后判断小灯的状态，并实现对应的地图状态
				 */
				if(mIsLightOpen){
					setNightMode();
				}else {
					cancelNightMode();
				}
			} else if (NAVI_APP_EXIT.equals(event)) {
				/* 	这里为导航已经关闭后，通知接口返回的导航已经关闭了
				 *	现在存在一个情况，外部全局关闭导航后，导航会直接关闭，无法及时收到该通知类
				 * 	但是在导航重新打开后又会收到该通知，导致出现导航闪退的现象	*/
				onExitApp();
			} else if (NAVI_START.equals(event)) {
				onStart();
			} else if (NAVI_END.equals(event)) {
				onEnd(false);
			} else if (NAVI_FRONT.equals(event)) {
				onResume();
			} else if (NAVI_BACKGROUND.equals(event)) {
				onPause();
			} else if (CRUISE_START.equals(event)) {
				mIsDogMode = true;
				forceCheckInNav();
			} else if (CRUISE_END.equals(event)) {
				mIsDogMode = false;
				forceCheckInNav();
			}
			return true;
		}

		if (FUN_NAVI_SYN_ADDRESS.equals(func)) {
			JSONBuilder jb = new JSONBuilder(BDHelper.getStrDataFromKey(params, "data"));
			String name = jb.getVal("name", String.class);
			String address = jb.getVal("address", String.class);
			int lat = jb.getVal("lat", Integer.class);
			int lng = jb.getVal("lng", Integer.class);
			String type = jb.getVal("type", String.class);
			if ("home".equals(type)) {
				if (lat == 0 || lng == 0) {
					NavManager.getInstance().clearHomeLocation();
					return true;
				}
				NavManager.getInstance().setHomeLocation(name, address, BDHelper.convertDouble(lat),
						BDHelper.convertDouble(lng), UiMap.GPS_TYPE_GCJ02, false);
			} else if ("office".equals(type)) {
				if (lat == 0 || lng == 0) {
					NavManager.getInstance().clearCompanyLocation();
					return true;
				}
				NavManager.getInstance().setCompanyLocation(name, address, BDHelper.convertDouble(lat),
						BDHelper.convertDouble(lng), UiMap.GPS_TYPE_GCJ02, false);
			}
			return true;
		}

		if (FUN_NAVI_ROUTE_PLAN.equals(func)) {
			JSONBuilder jb = new JSONBuilder(params);
			String event = jb.getVal("event", String.class);
			LogUtil.d("event-->"+event);
			if ("close".equals(event)) {
				// 路径规划界面关闭
				NavNotifyQueues.getInstance().removeDialog(FUN_NAVI_ROUTE_PLAN);
				if (isAutoNavi()) {
					cancelAutoNavi();
				}
				return true;
			}

			JSONObject data = jb.getVal("data", JSONObject.class);
			if (data == null) {
				return true;
			}
			mNavPathInfo = NavBaiduFactory.getNavPathInfoFromJson(data.toString());
			getToPoiCity();
			NotifyDialog mNotifyDialog = NavNotifyQueues.getInstance().createDialog(FUN_NAVI_ROUTE_PLAN);
			RouteDetails[] rds = mNavPathInfo.rds;
			if (rds != null) {
				for (int i = 0; i < rds.length; i++) {
					final String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
					mNotifyDialog.addCmds("CTRL$PLAN_SELECT_" + strIndex, new Runnable1<Integer>(i + 1) {
						@Override
						public void run() {
							BDHelper.selectRoute(mP1);
							String sText = NativeData.getResString("RS_BAIDU_SELECT_ROUTE_HINT");
							sText = sText.replace("%INDEX%", "第" + strIndex + "个");
							TtsManager.getInstance().speakText(sText);
						}
					}, "第" + strIndex + "个", "第" + strIndex + "条", "方案" + strIndex);
				}
				mNotifyDialog.addCmds("CTRL$USER_CANCEL", new Runnable() {

					@Override
					public void run() {
						BDHelper.selectPlanCancel();
					}
				}, "取消", "关闭");
				mNotifyDialog.addCmds("CTRL$START_NAVI", new Runnable() {

					@Override
					public void run() {
						BDHelper.selectStartNavi();
					}
				}, "开始导航", "立即导航");
				mNotifyDialog.build();

				if (isAutoNavi()) {
					startAutoNavi();
				}
			}
			return true;
		}

		//设置小灯显示黑夜主图和地图内选自动模式时切换的白天模式冲突了
		//todo 实际操作起来比较像bug，暂时不上
		//新增切换自动、白天、黑夜模式时，操作数据的返回接口
//		if(FUN_THEME_SYNC.equals(func)){
//			JSONBuilder jb = new JSONBuilder(params);
//			final int mode = jb.getVal("mode", Integer.class);
//			switch (mode){
//				case 1:		//开启自动模式
//					if(mIsLightOpen){
//						try {
//							Thread.sleep(50);
//						} catch (InterruptedException e) {
//							e.printStackTrace();
//						}
//						setNightMode();
//					}
//					break;
//				case 2:	//开启白天模式
//					break;
//				case 3:	//开启黑夜模式
//					break;
//				default:
//					break;
//			}
//			return true;
//		}
		return false;
	}

	private boolean isAutoNavi() {
		return autoNavDelay >= 0;
	}

	private void startAutoNavi() {
		AppLogic.removeBackGroundCallback(autoStartTask);
		if (autoNavDelay >= 0) {
			AppLogic.runOnBackGround(autoStartTask, autoNavDelay);
		}
	}

	private void cancelAutoNavi() {
		AppLogic.removeBackGroundCallback(autoStartTask);
	}
	
	Runnable autoStartTask = new Runnable() {

		@Override
		public void run() {
			BDHelper.selectStartNavi();
		}
	};
	
	@Override
	public void onExitApp() {
		onPause();
		super.onExitApp();
	}
	
	private void getToPoiCity() {
		final GeoCoder geoCoder = GeoCoder.newInstance();
		geoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener() {
			@Override
			public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
				geoCoder.destroy();
				if (result != null && result.getAddressDetail() != null) {
					mNavPathInfo.endNode.city = result.getAddressDetail().city;
				}
			}

			@Override
			public void onGetGeoCodeResult(GeoCodeResult result) {
			}
		});
		geoCoder.reverseGeoCode(new ReverseGeoCodeOption().location(new LatLng(
				BDHelper.convertDouble(mNavPathInfo.endNode.lat), BDHelper.convertDouble(mNavPathInfo.endNode.lng))));
	}
	/**
	 * 间隔发送查询家和公司地址的请求，防止同时设置时时序上的问题只能设置一个
	 */
	private void queryAddressInterval() {
		NavThirdApp nta = NavManager.getInstance().getLocalNavImpl();
		if (nta == null || !(nta instanceof NavBaiduDeepImpl)) {
			return;
		}

		queryAddr();
	}
	
	private void queryAddr() {
		queryAddressRecord(true);
		Runnable reHome = new Runnable() {
			@Override
			public void run() {
				queryAddressRecord(true);
			}
		};
		Runnable reCompany = new Runnable() {
			public void run() {
				queryAddressRecord(false);
			};
		};
		AppLogic.runOnBackGround(reCompany, 1000);
		AppLogic.runOnBackGround(reHome, 10000);
		AppLogic.runOnBackGround(reCompany, 15000);
		
	}

	private boolean doOnAsynchronousCall(final String requestId, String func, String params) {
		if (FUN_NAVI_DIALOG_NOTIFY.equals(func)) {
			JSONBuilder jb = new JSONBuilder(params);
			final String id = jb.getVal("dialogid", String.class);
			String type = jb.getVal("type", String.class);
			JSONObject msg = jb.getVal("value", JSONObject.class);
			if ("message".equals(type)) {
				String title=msg.optString("title");
				//在后续百度地图的版本中，百度已有自己的免责声明，不再需要单独的免责声明弹框
				if (title.equals("免责声明")){
					return true;
				}
				String dialogId = requestId;
				if (BDHelper.isNewSDKVersion()) {
					dialogId = id;
				}

				NotifyDialog mNotifyDialog = NavNotifyQueues.getInstance()
						.createDialog(FUN_NAVI_DIALOG_NOTIFY + dialogId);
				String sureStr = msg.optString("firstbtn");
				String cancelStr = msg.optString("secondbtn");
				mNotifyDialog.addCmds("CTRL$SURE", new Runnable() {

					@Override
					public void run() {
						BDHelper.doWakeupControlDialog(requestId, id, CoDriver_Dialog_FIRST_BTN);
					}
				}, sureStr);
				mNotifyDialog.addCmds("CTRL$CANCEL", new Runnable() {

					@Override
					public void run() {
						BDHelper.doWakeupControlDialog(requestId, id, CoDriver_Dialog_SECOND_BTN);
					}
				}, cancelStr);
				if (mTTSRecord != null) {
					mTTSRecord.cancelTts();
				}
				mNotifyDialog.build();
				mTTSRecord.sSpeechId = TtsManager.getInstance().speakVoice(msg.optString("content"),
						TtsManager.BEEP_VOICE_URL);
			} else if ("list".equals(type)) {
			}
			return true;
		}
		if (FUN_NAVI_DIALOG_RESPONS.equals(func)) {
			if (mWinConfirmAsr != null && mWinConfirmAsr instanceof DialogRecord) {
				String dialogId = requestId;
				if (BDHelper.isNewSDKVersion()) {
					JSONBuilder jb = new JSONBuilder(params);
					dialogId = jb.getVal("dialogid", String.class);
				}
				((DialogRecord) mWinConfirmAsr).onDialog(dialogId, params);
			}
			return true;
		}
		if (FUN_NAVI_DIALOG_CANCEL.equals(func)) {
			String dialogId = requestId;
			if (!TextUtils.isEmpty(params)) {
				if (BDHelper.isNewSDKVersion()) {
					JSONBuilder jb = new JSONBuilder(params);
					try {
						dialogId = jb.getVal("dialogid", String.class);
					} catch (Exception e) {
						try {
							dialogId = String.valueOf(jb.getVal("dialogid", Integer.class));
						} catch (Exception e1) {
						}
					}
				}
				if (mWinConfirmAsr != null && mWinConfirmAsr instanceof DialogRecord) {
					boolean bSucc = ((DialogRecord) mWinConfirmAsr).onDialogCancel(dialogId, params);
					if (bSucc) {
						return true;
					}
				}
			}

			if (mTTSRecord != null) {
				mTTSRecord.cancelTts();
			}

			NavNotifyQueues.getInstance().removeDialog(FUN_NAVI_DIALOG_NOTIFY + dialogId);
			return true;
		}

		if (FUN_NAVI_VIA_SEARCH.equals(func)) {
			if (mRecord != null) {
				WayPoiData wpd = (WayPoiData) mRecord.onParseStrData(requestId, params);
				if (wpd == null || wpd.getWayPois() == null || wpd.getWayPois().size() < 1) {
					AsrManager.getInstance().setNeedCloseRecord(true);
					RecorderWin.speakTextWithClose(NativeData.getResString("RS_NAV_WAY_POI_FAIL"), null);
				} else {
					String kws = mRecord.keywords;
					if (!TextUtils.isEmpty(kws)) {
						PoisData poisData=new PoisData();
						poisData.keywords=kws;
						poisData.action=Poi.PoiAction.ACTION_JINGYOU;
						List<Poi> pois=new ArrayList<Poi>();
						float GCJ_BD_RATE = 100000.0f;
						for (WayPoiData.WayPoi wayPoi:wpd.getWayPois()){
							Poi poi=new Poi();
							poi.setGeoinfo(wayPoi.address);
							poi.setDistance(wayPoi.distance);
							poi.setName(wayPoi.name);
							poi.setLat(wayPoi.lat/GCJ_BD_RATE);
							poi.setLng(wayPoi.lng/GCJ_BD_RATE);

							pois.add(poi);
						}
						poisData.mPois=pois;
						poisData.city="沿途";
						NavManager.getInstance().setTipCityStr(poisData);
						ChoiceManager.getInstance().showPoiList(poisData,null);

//						RecorderWin.speakTextWithClose(
//								NativeData.getResString("RS_BAIDU_WAYPOI_RESULT").replace("%SLOT%", speakText), null);
//						initSearchCommand();
					}
				}
			}
			return true;
		}

		return false;
	}

	private String changeNewKws(String kws){
		if (kws.contains("加油")){
			return "加油站";
		}else if (kws.contains("银行")) {
			return "银行";
		}else if (kws.contains("厕所")) {
			return "厕所";
		}else if (kws.contains("景点")) {
			return "景点";
		}else if (kws.contains("餐饮")) {
			return "餐饮";
		}else if (kws.contains("酒店")) {
			return "酒店";
		}else if (kws.contains("服务区")) {
			return "服务区";
		}else if (kws.contains("停车场")) {
			return "停车场";
		}
		return kws;
	}

	//百度沿途搜索返回结构后注册免唤醒指令,通过过去返回数据的数量注册对应的指令内容
	//修改后使用列表显示，并且相应列表中对应的指令，不再需要这里的免唤醒指令
//	private ViaSearchCommand viaSearchCommand=new ViaSearchCommand(FUN_NAVI_ADD_VIA_POINT);
//	//存储传输过来的沿途搜索信息
//	private WayPoiData wpd=null;
//	public void initSearchCommand(){
//		for(int i=0;i<wpd.getWayPois().size();i++){
//			final int finalI = i;
//			final String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
//			viaSearchCommand.addCmds("viaSearch_"+strIndex, new Runnable() {
//				@Override
//				public void run() {
//					Poi poi=new Poi();
//					poi.setLat(wpd.getWayPois().get(finalI).lat);
//					poi.setLng(wpd.getWayPois().get(finalI).lng);
//					BDHelper.notifyInsertPoint(poi,BDHelper.getRequestId());
//					stopTiming();
//				}
//			},"第" + strIndex+ "个","地点"+strIndex,"第"+strIndex);
//
//		}
//		viaSearchCommand.build();
//		startTimimg();
//	}
//	/**
//	 * 时间到后处理的事件
//	 * 清楚对应指令与途径点位置信息
//	 */
//	private Runnable timeOutCallback=new Runnable() {
//		@Override
//		public void run() {
//			JNIHelper.logd("time out and cancel the commands");
//			viaSearchCommand.destory();
//			wpd=null;
//		}
//	};
//
//	/**
//	 * 开始计时
//	 */
//	public void startTimimg(){
//		JNIHelper.logd("fun_navi_via_search,startTiming");
//		AppLogic.removeBackGroundCallback(timeOutCallback);
//		long TIME_OUT = 40 * 1000;
//		AppLogic.runOnBackGround(timeOutCallback, TIME_OUT);
//	}
//
//	/**
//	 * 结束计时
//	 */
//	public void stopTiming(){
//		JNIHelper.logd("fun_navi_via_search,stopTiming");
//		AppLogic.removeBackGroundCallback(timeOutCallback);
//		wpd=null;
//	}

	public void query(int type, String params) {
		if (mRecord == null) {
			mRecord = new RequestRecord();
		}

		mRecord.requestId = "";
		mRecord.execQuery(type, params);
	}

	private void navi(NavPlanType plan, NavigateInfo info) {
		LatLng origin = LocationUtil.getLocation(LocationManager.getInstance().getLastLocation().msgGpsInfo);
		LatLng dest = LocationUtil.getLocation(info.msgGpsInfo);
		if (dest == null)
			return;
		String type = "bd09ll";
		int strategy = 10;
		switch (plan) {
		case NAV_PLAN_TYPE_AVOID_JAMS:
			strategy = 60;
			break;
		case NAV_PLAN_TYPE_LEAST_COST:
			strategy = 40;
			break;
		case NAV_PLAN_TYPE_LEAST_DISTANCE:
			strategy = 30;
			break;
		case NAV_PLAN_TYPE_LEAST_TIME:
			strategy = 20;
			break;
		case NAV_PLAN_TYPE_RECOMMEND:
		default:
			strategy = 10;
			break;
		}
		String url = "bdnavi://plan?coordType=" + type + "&src=" + R.string.appid_baidumap + "&dest=" + dest.latitude
				+ "," + dest.longitude + "," + info.strTargetName + "&strategy=" + strategy;
		// 起始点
		if (origin != null) {
			url += "&start=" + origin.latitude + "," + origin.longitude;
		}
		Intent intent = new Intent();
		intent.setData(Uri.parse(url));
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setPackage(BaiduVersion.BAIDU_NAVI_PACKAGE);
		GlobalContext.get().startActivity(intent);
	}
	
	@Override
	public PathInfo getCurrentPathInfo() {
		if (mPathInfo == null) {
			mPathInfo = new PathInfo();
		}

		if (mNavPathInfo != null) {
			copyPathInfo(mPathInfo, mNavPathInfo);
		} else if (mNavWayInfo != null) {
			copyPathInfo(mPathInfo, mNavWayInfo);
		}

		return mPathInfo;
	}
	
	private void copyPathInfo(PathInfo pathInfo, NavPathInfo info) {
		if (info.startNode != null) {
			pathInfo.fromPoiLat = BDHelper.convertDouble(info.startNode.lat);
			pathInfo.fromPoiLng = BDHelper.convertDouble(info.startNode.lng);
			pathInfo.fromPoiAddr = info.startNode.address;
			pathInfo.fromPoiName = info.startNode.name;
		}

		if (info.endNode != null) {
			pathInfo.toPoiLat = BDHelper.convertDouble(info.endNode.lat);
			pathInfo.toPoiLng = BDHelper.convertDouble(info.endNode.lng);
			pathInfo.toPoiAddr = info.endNode.address;
			pathInfo.toPoiName = info.endNode.name;
			pathInfo.toCity = info.endNode.city;
		}
	}

	@Override
	public double[] getDestinationLatlng() {
		NavPathInfo info = null;
		if (mNavPathInfo != null) {
			info = mNavPathInfo;
		} else {
			info = mNavWayInfo;
		}
		if (info != null && info.endNode != null) {
			double[] latlng = { BDHelper.convertDouble(info.endNode.lat),  BDHelper.convertDouble(info.endNode.lng) };
			return latlng;
		}
		return null;
	}

	@Override
	public String getDestinationCity() {
		NavPathInfo info = null;
		if (mNavPathInfo != null) {
			info = mNavPathInfo;
		} else {
			info = mNavWayInfo;
		}
		if (info != null && info.endNode != null) {
			return info.endNode.city;
		}
		return null;
	}

	@Override
	public  int getOnWaySearchToolCode(String keyword) {
		String str = ConfigFileHelper.getInstance(GlobalContext.get()).ConfigValue(
				ConfigFileHelper.ONWAY_SEARCH, ConfigFileHelper.NAV_IMP_BAIDU, keyword, getMapVersion(),"");
		return TextUtils.isEmpty(str)?-1:ConfigFileHelper.POI_SEARCH_CODE_BAIDU;
	}

	public String checkHint(String type){
		if(AsrKeyType.VIEW_ALL.equals(type)){
			return NativeData.getResString("RS_MAP_VIEW_ALL");
		}else if(AsrKeyType.ZOOM_IN.equals(type)){
			return NativeData.getResString("RS_MAP_ZOOMIN");
		}else if(AsrKeyType.ZOOM_OUT.equals(type)){
			return NativeData.getResString("RS_MAP_ZOOMOUT");
		}else if(AsrKeyType.ZOOM_OUT.equals(type)){
			return NativeData.getResString("RS_MAP_ZOOMOUT");
		}else if(AsrKeyType.NIGHT_MODE.equals(type)){
			return NativeData.getResString("RS_MAP_NIGHT_MODE");
		}else if(AsrKeyType.LIGHT_MODE.equals(type)){
			return NativeData.getResString("RS_MAP_LIGHT_MODE");
		}else if(AsrKeyType.MUTE_MODE.equals(type)){
			return NativeData.getResString("RS_MAP_CHANGE_MUTE_MODE");
		}else if(AsrKeyType.NORTH_DIRECT.equals(type)){
			return NativeData.getResString("RS_MAP_NORTH_DIRECT");
		}else if(AsrKeyType.CAR_DIRECT.equals(type)){
			return NativeData.getResString("RS_MAP_NORTH_DIRECT");
		}else if(AsrKeyType.CLOSE_TRAFFIC.equals(type)){
			return NativeData.getResString("RS_MAP_CLOSE_TRAFFIC");
		}else if(AsrKeyType.OPEN_TRAFFIC.equals(type)){
			return NativeData.getResString("RS_MAP_OPEN_TRAFFIC");
		}
		return NativeData.getResString("RS_MAP_CHANGE_MEADWAR_OR_EXPERT_MODE");
	}

	public void setNightMode(){
		mIsLightOpen=true;
		if(!NavAppManager.getInstance().isAlreadyExitNav()){
			BDHelper.notifyMapControl("235", BDHelper.getRequestId());
		}
	}

	public void cancelNightMode(){
		mIsLightOpen=false;
		if (!NavAppManager.getInstance().isAlreadyExitNav()){
			BDHelper.notifyMapControl("236", BDHelper.getRequestId());
		}
	}
}