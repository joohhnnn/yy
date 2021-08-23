package com.txznet.txz.component.nav.remote;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.sdk.TXZNavManager;
import com.txznet.sdk.TXZNavManager.NavToolStatusHighListener;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.component.nav.NavThirdComplexApp;
import com.txznet.txz.jni.JNIHelper;

import android.os.Build;
import android.text.TextUtils;

public class RemoteNavImpl extends NavThirdComplexApp {
	// 自定义导航工具配置项
	private static int mRemoteNavFlag;
	public static String sNavAction = null;

	private static String mRemoteServiceName;

	public static void setRemoteNavFlag(int remoteFlag) {
		mRemoteNavFlag = remoteFlag;
		JNIHelper.logd("setRemoteFlag:" + mRemoteNavFlag);
	}

	public void setRemoteNavToolisInFocus(boolean isInFocus) {
		if (isInFocus) {
			onResume();
		} else {
			onPause();
		}
		JNIHelper.logd("mRemoteNavToolisInFocus:" + mIsFocus);
	}
	
	public void setRemoteNavToolisInNav(boolean isInNav) {
		if (isInNav) {
			onStart();
		} else {
			onEnd(false);
		}
		JNIHelper.logd("mRemoteNavToolisInNav:" + mIsStarted);
	}
	
	public void setRemoteNavToolisExitApp(boolean isExitApp) {
		if (isExitApp) {
			onExitApp();
		}
		JNIHelper.logd("notifyExitApp:" + isExitApp);
	}
	
	public static void setRemoteServiceName(String serviceName) {
		mRemoteServiceName = serviceName;
	}

	@Override
	public void onNavCommand(boolean fromWakeup, String cmd, String speech) {
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		JNIHelper.logd("nav with " + mRemoteServiceName);
		try {
			JSONObject json = new JSONObject();
			json.put("lat", info.msgGpsInfo.dblLat);
			json.put("lng", info.msgGpsInfo.dblLng);
			json.put("city", info.strTargetCity);
			json.put("name", info.strTargetName);
			json.put("extre", info.strExtraInfo);
			if (!TextUtils.isEmpty(sNavAction)) {
				json.put("action", sNavAction);
			}
			if (info.strTargetAddress != null)
				json.put("geo", info.strTargetAddress);
			ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.navTo", json.toString().getBytes(),
					null);
		} catch (Exception e) {
		}
		return true;
	}
	
	@Override
	public PathInfo getCurrentPathInfo() {
		if (!isInNav()) {
			mPathInfo = null;
			return null;
		}

		if (mPathInfo != null) {
			return mPathInfo;
		}

		return super.getCurrentPathInfo();
	}
	
	private PathInfo Convert(NavigateInfo info) {
		PathInfo pathInfo = new PathInfo();
		pathInfo.toCity = info.strTargetCity;
		pathInfo.toPoiAddr = info.strTargetAddress;
		pathInfo.toPoiLat = info.msgGpsInfo.dblLat;
		pathInfo.toPoiLng = info.msgGpsInfo.dblLng;
		pathInfo.toPoiName = info.strTargetName;
		return pathInfo;
	}
	
	@Override
	public boolean banNavWakeup() {
		return true;
	}

	@Override
	public boolean procJingYouPoi(Poi... pois) {
		if (!remoteEnableWayPoi()) {
			return false;
		}
		super.procJingYouPoi(pois);

		final Poi poi = pois[0];
		try {
			JSONObject json = new JSONObject();
			json.put("lat", poi.getLat());
			json.put("lng", poi.getLng());
			json.put("city", poi.getCity());
			json.put("name", poi.getName());
			json.put("action", PoiAction.ACTION_JINGYOU);
			json.put("geo", poi.getGeoinfo());
			json.put("extre", poi.getExtraStr());
			ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.navTo", json.toString().getBytes(),
					null);
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public String disableDeleteJingYou() {
		if (remoteEnableWayPoi()) {
			return "";
		}
		return super.disableDeleteJingYou();
	}

	@Override
	public boolean deleteJingYou(Poi poi) {
		if (!remoteEnableWayPoi()) {
			return false;
		}
		try {
			JSONObject json = new JSONObject();
			json.put("lat", poi.getLat());
			json.put("lng", poi.getLng());
			json.put("city", poi.getCity());
			json.put("name", poi.getName());
			json.put("action", PoiAction.ACTION_DEL_JINGYOU);
			json.put("geo", poi.getGeoinfo());
			json.put("extre", poi.getExtraStr());
			ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.navTo", json.toString().getBytes(),
					null);
			super.deleteJingYou(poi);
		} catch (Exception e) {
		}
		return true;
	}

	@Override
	public boolean speakLimitSpeech() {
		if (!remoteEnableLimitSpeed()) {
			return false;
		}
		ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.speakLimitSpeed", null, null);
		return true;
	}

	@Override
	public void queryHomeCompanyAddr() {
		// TODO 通知第三方导航工具同步家和公司的地址给语音
	}
	
	@Override
	public void enterNav() {
		ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.enterNav", mRemoteServiceName.getBytes(),
				null);
		if (mNavStatusListener != null) {
			mNavStatusListener.onEnter(mRemoteServiceName);
		}
	}

	@Override
	public void exitNav() {
		ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.exitNav", mRemoteServiceName.getBytes(), null);
		onExitApp();
	}
	
	@Override
	public String disableProcJingYouPoi() {
		if (remoteEnableWayPoi()) {
			return "";
		}

		return super.disableProcJingYouPoi();
	}

	@Override
	public String disableNavWithWayPoi() {
		if (remoteEnableNavPassWayPois()) {
			return "";
		}
		return super.disableNavWithWayPoi();
	}

	/**
	 * 远程工具是否支持途经点功能
	 * 
	 * @return
	 */
	public boolean remoteEnableWayPoi() {
		return (mRemoteNavFlag & NavToolStatusHighListener.MARK_WAYPOI) != 0;
	}

	/**
	 * 远程工具是否支持播报当前限速
	 * 
	 * @return
	 */
	public boolean remoteEnableLimitSpeed() {
		return (mRemoteNavFlag & NavToolStatusHighListener.MARK_LIMITSPEED) != 0;
	}

	/**
	 * 远程工具是否支持发起导航携带途经点
	 * @return
	 */
	public boolean remoteEnableNavPassWayPois(){
		return (mRemoteNavFlag & TXZNavManager.NavToolStatusHighListener.MARK_NAV_PASS_WAYPOIS) != 0;
	}

	@Override
	public void updateHomeLocation(NavigateInfo navigateInfo) {
		if (navigateInfo == null) {
			ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.setHomeLoc", null, null);
			return;
		}
		Poi hPoi = new Poi();
		hPoi.setName(navigateInfo.strTargetName);
		hPoi.setGeoinfo(navigateInfo.strTargetAddress);
		hPoi.setLat(navigateInfo.msgGpsInfo.dblLat);
		hPoi.setLng(navigateInfo.msgGpsInfo.dblLng);
		ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.setHomeLoc", hPoi.toString().getBytes(),
				null);
	}

	@Override
	public void updateCompanyLocation(NavigateInfo navigateInfo) {
		if (navigateInfo == null) {
			ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.setCompanyLoc", null, null);
			return;
		}
		Poi hPoi = new Poi();
		hPoi.setName(navigateInfo.strTargetName);
		hPoi.setGeoinfo(navigateInfo.strTargetAddress);
		hPoi.setLat(navigateInfo.msgGpsInfo.dblLat);
		hPoi.setLng(navigateInfo.msgGpsInfo.dblLng);
		ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.setCompanyLoc",
				hPoi.toString().getBytes(), null);
	}

	@Override
	public boolean enableWorkWithoutResume() {
		return true;
	}

	@Override
	public List<String> getBanCmds() {
		return null;
	}

	@Override
	public String[] getSupportCmds() {
		return null;
	}

	@Override
	public String getPackageName() {
		return mRemoteServiceName;
	}

	@Override
	public boolean isReachable() {
		return !TextUtils.isEmpty(mRemoteServiceName);
	}

	@Override
	public void setPackageName(String packageName) {
		mRemoteServiceName = packageName;
	}

	@Override
	public byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if ("notifyPathInfo".equals(command)) {
			if (data != null) {
				mPathInfo = PathInfo.fromString(new String(data));
			} else {
				mPathInfo = null;
			}
			LogUtil.logd("notifyPathInfo:" + mPathInfo);
			return null;
		} else if("notifyJingYous".equals(command)) {
			JSONArray jsonBuilder = null;
			try {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					jsonBuilder = new JSONArray(data);
				}
				LogUtil.logd("notifyJingYous:" + jsonBuilder);
				if (jsonBuilder != null) {
					List<Poi> pois = new ArrayList<Poi>();
					for (int i = 0; i < jsonBuilder.length(); i++) {
						String jsonData = jsonBuilder.optString(i);
						pois.add(Poi.fromString(jsonData));
					}
					if (jyList == null) {
						jyList = new ArrayList<Poi>();
					}
					jyList.clear();
					jyList.addAll(pois);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
		return super.invokeTXZNav(packageName, command, data);
	}


	@Override
	public boolean navigateWithWayPois(Poi startPoi, Poi endPoi, List<PathInfo.WayInfo> pois) {
		PathInfo pathInfo = new PathInfo();
		pathInfo.wayInfos = pois;
		//起点
		pathInfo.fromPoiLat = startPoi.getLat();
		pathInfo.fromPoiLng = startPoi.getLng();
		pathInfo.fromPoiName = startPoi.getName();

		//终点
		pathInfo.toPoiLat = endPoi.getLat();
		pathInfo.toPoiLng = endPoi.getLng();
		pathInfo.toPoiName = endPoi.getName();
		pathInfo.toCity = endPoi.getCity();
		ServiceManager.getInstance().sendInvoke(mRemoteServiceName, "tool.nav.navigateWithWayPois", pathInfo.toString().getBytes(),
				null);
		return true;
	}
}