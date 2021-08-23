package com.txznet.txz.component.nav.gaode;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.sence.SenceManager;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.text.TextUtils;

public class NavAmapControl implements IMapInterface {
	public static interface IAmapNavContants{
		public static final String RECV_ACTION = "AUTONAVI_STANDARD_BROADCAST_SEND";
		public static final String SEND_ACTION = "AUTONAVI_STANDARD_BROADCAST_RECV";

		public static final int CREATE_MAP = 1;
		// 运行结束，退出程序
		public static final int EXIT_APP = 2;
		public static final int FRONT = 3;
		public static final int BGROUND = 4;
		public static final int PLAN_START = 5;
		public static final int PLAN_SUC = 6;
		public static final int PLAN_FAIL = 7;
		public static final int START_NAVI = 8;
		public static final int END_NAVI = 9;
		public static final int TTS_START = 13;
		public static final int TTS_END = 14;
		public static final int MAP_ZOOM_IN = 15;//放大地图
		public static final int MAP_ZOOM_OUT = 16;//缩小地图
		public static final int MAP_SIZE_MAX = 22;//地图尺寸达到最大
		public static final int MAP_SIZE_MIN = 23;//地图尺寸到达最小
		public static final int HOME_CHANGE = 26;
		public static final int COMPANY_CHANGE = 27;
		public static final int SENDTOCARD_DISMISS = 30;
		public static final int STOP_CAR_DISMISS = 31;
		public static final int CONTNAVI_DISMISS = 32;
		// 算路失败对话框消失
		public static final int PLANFAIL_DISMISS = 33;
		// 文档说明完全运行结束，退出程序
		public static final int ALL_EXIT_APP = 45;
		// activity获得焦点
		public static final int ACTIVITY_GET_FOCUS = 50;
		// activity失去焦点
		public static final int ACTIVITY_LOSE_FOCUS = 51;
		
		public static final int ROLE_GUOYU_MM = 0;
		public static final int ROLE_GUOYU_GG = 1;
		public static final int ROLE_ZHOUXINGXING  = 2;
		public static final int ROLE_GUANGDONGHUA = 3;
		public static final int ROLE_LINZHILIN = 4;
		public static final int ROLE_GUODEGANG = 5;
		public static final int ROLE_DONGBEIHUA = 6;
		public static final int ROLE_HENANHUA = 7;
		public static final int ROLE_HUNANHUA = 8;
		public static final int ROLE_SICHUANHUA = 9;
		public static final int ROLE_TAIWANHUA = 10;

		public static final String HINT_GUOYU_MM = NativeData.getResString("RS_MAP_HINT_GUOYU_MM");
		public static final String HINT_GUOYU_GG = NativeData.getResString("RS_MAP_HINT_GUOYU_GG");
		public static final String HINT_ZHOUXINGXING = NativeData.getResString("RS_MAP_HINT_ZHOUXINGXING");
		public static final String HINT_GUANGDONGHUA = NativeData.getResString("RS_MAP_HINT_GUANGDONGHUA");
		public static final String HINT_LINZHILIN = NativeData.getResString("RS_MAP_HINT_LINZHILIN");
		public static final String HINT_GUODEGANG = NativeData.getResString("RS_MAP_HINT_GUODEGANG");
		public static final String HINT_DONGBEIHUA = NativeData.getResString("RS_MAP_HINT_DONGBEIHUA");
		public static final String HINT_HENANHUA = NativeData.getResString("RS_MAP_HINT_HENANHUA");
		public static final String HINT_HUNANHUA = NativeData.getResString("RS_MAP_HINT_HUNANHUA");
		public static final String HINT_SICHUANHUA = NativeData.getResString("RS_MAP_HINT_SICHUANHUA");
		public static final String HINT_TAIWANHUA = NativeData.getResString("RS_MAP_HINT_TAIWANHUA");
		
		public static final String HINT_IS_GUOYU_MM = NativeData.getResString("RS_MAP_HINT_IS_GUOYU_MM");
		public static final String HINT_IS_GUOYU_GG = NativeData.getResString("RS_MAP_HINT_IS_GUOYU_GG");
		public static final String HINT_IS_ZHOUXINGXING = NativeData.getResString("RS_MAP_HINT_IS_ZHOUXINGXING");
		public static final String HINT_IS_GUANGDONGHUA = NativeData.getResString("RS_MAP_HINT_IS_GUANGDONGHUA");
		public static final String HINT_IS_LINZHILIN = NativeData.getResString("RS_MAP_HINT_IS_LINZHILIN");
		public static final String HINT_IS_GUODEGANG = NativeData.getResString("RS_MAP_HINT_IS_GUODEGANG");
		public static final String HINT_IS_DONGBEIHUA = NativeData.getResString("RS_MAP_HINT_IS_DONGBEIHUA");
		public static final String HINT_IS_HENANHUA = NativeData.getResString("RS_MAP_HINT_IS_HENANHUA");
		public static final String HINT_IS_HUNANHUA = NativeData.getResString("RS_MAP_HINT_IS_HUNANHUA");
		public static final String HINT_IS_SICHUANHUA = NativeData.getResString("RS_MAP_HINT_IS_SICHUANHUA");
		public static final String HINT_IS_TAIWANHUA = NativeData.getResString("RS_MAP_HINT_IS_TAIWANHUA");
	}

	public static final int VERSION_142 = 142;// 142版本
	public static final int VERSION_0708 = 143; // 143版本

	String mVersion;
	boolean mForceUseRecv;
	NavAmapAutoNavImpl mImpl;

	IMapInterface mAmapInterface;

	IMapInterface mRecInterface;
	IMapInterface mActInterface;

	public NavAmapControl(NavAmapAutoNavImpl naani, String version) {
		mImpl = naani;
		mVersion = version;

		initAmapInterface(mForceUseRecv);
	}

	private void initAmapInterface(boolean forceRecv) {
		mRecInterface = new NavAmapAutoRecv122(this);
		mActInterface = new NavAmapAutoAct122();

		if (isRecvVersion() || forceRecv) {
			mAmapInterface = mRecInterface;
		} else {
			mAmapInterface = mActInterface;
		}

		JNIHelper.logd("use interface:" + mAmapInterface.getClass().getName());

		mAmapInterface.setPackageName(mImpl.getPackageName());
	}
	
	boolean hasQueryMapStatus;

	public void queryMapStatus() {
		if (hasQueryMapStatus) {
			return;
		}

		if (mAmapInterface != null) {
			hasQueryMapStatus = true;
			mAmapInterface.initialize();
		}
	}

	public void setForceUseRecvImpl(boolean isForce) {
		mForceUseRecv = isForce;
		initAmapInterface(mForceUseRecv);
	}

	public void updateNavAmapVersion(String version) {
		this.mVersion = version;

		SharedPreferences sp = GlobalContext.get().getSharedPreferences(".AmapAutoVersion", Context.MODE_PRIVATE);
		if (sp != null) {
			Editor editor = sp.edit();
			editor.putString("VERSION_NUM", mVersion);
			editor.commit();
		}

		initAmapInterface(mForceUseRecv);
	}

	public int getMapCode() {
//		if (TextUtils.isEmpty(mVersion)) {
//			SharedPreferences sp = GlobalContext.get().getSharedPreferences(".AmapAutoVersion", Context.MODE_PRIVATE);
//			if (sp != null) {
//				mVersion = sp.getString("VERSION_NUM", "");
//			}
			if (TextUtils.isEmpty(mVersion)) {
				PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(mImpl.getPackageName());
				if (apkInfo != null) {
					mVersion = apkInfo.versionName;
					if (TextUtils.isEmpty(mVersion)) {
						return -1;
					}
				} else {
					return -1;
				}
			}
//		}

		Pattern pattern = Pattern.compile("([0-9]+)");
		Matcher matcher = pattern.matcher(mVersion);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			sb.append(matcher.group());
		}
		String v = sb.toString().substring(0, 3);
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public int getMapDetCode() {
//		if (TextUtils.isEmpty(mVersion)) {
//			SharedPreferences sp = GlobalContext.get().getSharedPreferences(".AmapAutoVersion", Context.MODE_PRIVATE);
//			if (sp != null) {
//				mVersion = sp.getString("VERSION_NUM", "");
//			}
			if (TextUtils.isEmpty(mVersion)) {
				PackageInfo apkInfo = PackageManager.getInstance().getApkInfo(mImpl.getPackageName());
				if (apkInfo != null) {
					mVersion = apkInfo.versionName;
					if (TextUtils.isEmpty(mVersion)) {
						return -1;
					}
				} else {
					return -1;
				}
			}
//		}

		Pattern pattern = Pattern.compile("([0-9]+)");
		Matcher matcher = pattern.matcher(mVersion);
		StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			sb.append(matcher.group());
		}
		String v = sb.toString();
		try {
			return Integer.parseInt(v);
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	public boolean isRecvVersion() {
		if (mForceUseRecv) {
			return true;
		}

		// 1.4以前的版本都用schema
//		if (getMapDetCode() > 1222224) {
//			return true;
//		}

		int vc = getMapCode();
		if (vc != -1 && vc >= VERSION_142) {
			return true;
		}

		if (vc == -1) {
			int v = PackageManager.getInstance().getVerionCode(mImpl.getPackageName());
			if (v >= 541) {
				return true;
			}
		}
		return false;
	}

	public boolean isSupportTTSRole() {
		if (!"1.2.2.2224".equals(mVersion) || !"1.4.2.2062".equals(mVersion)) {
			if (!isRecvVersion()) {
				return false;
			}
		}

		return true;
	}
	
	public boolean isAmapautoLite() {
		if (NavAmapAutoNavImpl.PACKAGE_NAME_LITE.equals(mImpl.getPackageName())) {
			return true;
		}
		return false;
	}

	@Override
	public void initialize() {
	}
	
	@Override
	public void enterNav() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "enterNav");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.enterNav();
	}

	@Override
	public void setPackageName(String pkn) {
		mAmapInterface.setPackageName(mImpl.getPackageName());
	}

	@Override
	public void zoomAll(Runnable run) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "zoomAll");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.zoomAll(run);
	}

	@Override
	public void appExit() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "appExit");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.appExit();
	}

	@Override
	public void naviExit() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "naviExit");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.naviExit();
	}

	@Override
	public void zoomMap(boolean isZoomin) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "zoomMap");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("isZoomin", isZoomin);
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.zoomMap(isZoomin);
	}

	@Override
	public void switchLightNightMode(boolean isLight) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchLightNightMode");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("isLight", isLight);
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		if (getMapCode() >= VERSION_0708) {
			int type = isLight ? 1 : 2;
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 10048);
			intent.putExtra("EXTRA_DAY_NIGHT_MODE", type);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			GlobalContext.get().sendBroadcast(intent);
			return;
		}
		mAmapInterface.switchLightNightMode(isLight);
	}

	@Override
	public void switchTraffic(boolean isShowTraffic) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchTraffic");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("isShowTraffic", isShowTraffic);
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switchTraffic(isShowTraffic);
	}

	@Override
	public void switch23D(boolean is2d, int val) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switch23D");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("is2d", is2d);
		jsonBuilder.put("val", val);
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switch23D(is2d, val);
	}

	@Override
	public void switchCarDirection() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchCarDirection");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switchCarDirection();
	}

	@Override
	public void switchNorthDirection() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchNorthDirection");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switchNorthDirection();
	}

	@Override
	public void switchPlanStyle(PlanStyle ps) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchPlanStyle");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("planStyle", ps.name());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switchPlanStyle(ps);
	}

	@Override
	public void backNavi() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "backNavi");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.backNavi();
	}

	@Override
	public void switchBroadcastRole(int role) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchBroadcastRole");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switchBroadcastRole(role);
	}

	@Override
	public void navigateTo(String name, double lat, double lng, int planStyle) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "navigateTo");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("name", name);
		jsonBuilder.put("lat", lat);
		jsonBuilder.put("lng", lng);
		jsonBuilder.put("style", planStyle);
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		
		if (isAmapautoLite()) {
			mRecInterface.navigateTo(name, lat, lng, planStyle);
			return;
		}
		mAmapInterface.navigateTo(name, lat, lng, planStyle);
	}
	
	public String getTTS(int role){
		switch (role) {
		case 0:
			return IAmapNavContants.HINT_IS_GUOYU_MM;
		case 1:
			return IAmapNavContants.HINT_IS_GUOYU_GG;
		case 2:
			return IAmapNavContants.HINT_IS_ZHOUXINGXING;
		case 3:
			return IAmapNavContants.HINT_IS_GUANGDONGHUA;
		case 4:
			return IAmapNavContants.HINT_IS_LINZHILIN;
		case 5:
			return IAmapNavContants.HINT_IS_GUODEGANG;
		case 6:
			return IAmapNavContants.HINT_IS_DONGBEIHUA;
		case 7:
			return IAmapNavContants.HINT_IS_HENANHUA;
		case 8:
			return IAmapNavContants.HINT_IS_HUNANHUA;
		case 9:
			return IAmapNavContants.HINT_IS_SICHUANHUA;
		case 10:
			return IAmapNavContants.HINT_IS_TAIWANHUA;
		}
		return "";
	}
	
	public String getSetTTS(int role){
		switch (role) {
		case 0:
			return IAmapNavContants.HINT_GUOYU_MM;
		case 1:
			return IAmapNavContants.HINT_GUOYU_GG;
		case 2:
			return IAmapNavContants.HINT_ZHOUXINGXING;
		case 3:
			return IAmapNavContants.HINT_GUANGDONGHUA;
		case 4:
			return IAmapNavContants.HINT_LINZHILIN;
		case 5:
			return IAmapNavContants.HINT_GUODEGANG;
		case 6:
			return IAmapNavContants.HINT_DONGBEIHUA;
		case 7:
			return IAmapNavContants.HINT_HENANHUA;
		case 8:
			return IAmapNavContants.HINT_HUNANHUA;
		case 9:
			return IAmapNavContants.HINT_SICHUANHUA;
		case 10:
			return IAmapNavContants.HINT_TAIWANHUA;
		}
		return "";
	}

	/**
	 * 前方路况
	 */
	@Override
	public void frontTraffic(){
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "frontTraffic");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.frontTraffic();
	}

	/**
	 * 打开/关闭精简模式
	 * @param isOpen
	 */
	@Override
	public void switchSimpleMode(boolean isOpen) {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "switchSimpleMode");
		jsonBuilder.put("origin", getMapDetCode());
		jsonBuilder.put("isOpen", isOpen);
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.switchSimpleMode(isOpen);
	}

	@Override
	public void queryCollectionPoint() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "queryCollectionPoint");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.queryCollectionPoint();
	}

	@Override
	public void intoTeam() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("action", "intoTeam");
		jsonBuilder.put("origin", getMapDetCode());
		if (SenceManager.getInstance().noneedProcSence("nav", jsonBuilder.toBytes())) {
			return;
		}
		mAmapInterface.intoTeam();
	}

}