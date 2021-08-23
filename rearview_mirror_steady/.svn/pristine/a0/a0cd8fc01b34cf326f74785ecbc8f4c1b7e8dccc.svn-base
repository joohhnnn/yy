package com.txznet.txz.component.nav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.CallSuper;

import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZNavManager.NavStatusListener;
import com.txznet.sdk.TXZNavManager.PathInfo;
import com.txznet.sdk.TXZNavManager.PathInfo.WayInfo;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.app.PackageManager;

import java.util.List;

public abstract class NavThirdApp implements INav {
	public static final String NAV_APP_ACTION = "com.txznet.txz.NAVI_ACTION";
	public static final String CLOSE_WIN_ACTION = "com.txznet.txz.close.win";
	public static final String RECORDER_SHOW_ACTION = "com.txznet.txz.record.show";
	public static final int CMD_CLOSE_WINASR = 1;

	protected boolean mIsFocus;
	protected boolean mIsStarted;
	protected boolean mIsPlaned;
	protected int mPlanStyle;
	protected boolean mEnableSave;
	protected Boolean mHasBeenOpen;
	protected PathInfo mPathInfo;
	protected int autoNavDelay = -1;
	
	protected NavStatusListener mNavStatusListener = null;
	public static int DIALOG_TIME_OUT = -1;
	protected static WinConfirmAsr mWinConfirmAsr = null;
	
	static {
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(CLOSE_WIN_ACTION);
		intentFilter.addAction(NAV_APP_ACTION);
		intentFilter.addAction(RECORDER_SHOW_ACTION);
		GlobalContext.get().registerReceiver(new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) {
				dismiss();
			}
		}, intentFilter);
	}

	@Override
	public int initialize(final IInitCallback oRun) {
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				oRun.onInit(true);
			}
		}, 0);
		return 0;
	}

	@Override
	public void release() {
	}

	public void handleIntent(Intent intent) {

	}

	public void handleBundle(Bundle bundle) {

	}
	
	/**
	 * 更新家的地址
	 * 
	 * @param navigateInfo
	 */
	public void updateHomeLocation(NavigateInfo navigateInfo){
	}

	/**
	 * 更新公司的地址
	 * 
	 * @param navigateInfo
	 */
	public void updateCompanyLocation(NavigateInfo navigateInfo){
		
	}
	
	/**
	 * 查询家和公司地址（应在选择导航工具之后操作）
	 */
	public void queryHomeCompanyAddr(){
		
	}
	
	public boolean willNavAfterSet() {
		return true;
	}

	private String navPkg;
	
	/**
	 * 更换导航包名
	 */
	public void setPackageName(String packageName) {
		navPkg = packageName;
	}

	public String getPackageName() {
		return navPkg;
	}

	/**
	 * 导航工具是否可用，默认通过包名判断是否存在
	 * @return
	 */
	public boolean isReachable() {
		return PackageManager.getInstance().checkAppExist(getPackageName());
	}

	public void startNavByInner() {

	}

	@Override
	public boolean isInNav() {
		return mIsStarted /*&& mIsPlaned*/;
	}
	
	public boolean isInFocus(){
		return mIsFocus;
	}

	/**
	 * 导航是否启动过
	 * 
	 * @return
	 */
	public boolean hasBeenOpen() {
		if (isInFocus()) {
			mHasBeenOpen = true;
			return true;
		}
		return mHasBeenOpen != null ? mHasBeenOpen : false;
	}

	/**
	 * 退出了应用
	 */
	public void onExitApp() {
		LogUtil.logd("nav onExitApp");
		mHasBeenOpen = null;
		mIsFocus = false;
		mIsStarted = false;

		if (mNavStatusListener != null) {
			mNavStatusListener.onExit(getPackageName());
		}
	}

	@Override
	public void enterNav() {
		String pkn = getPackageName();
		PackageManager.getInstance().openApp(pkn);

		if (mNavStatusListener != null) {
			mNavStatusListener.onEnter(pkn);
		}
	}

	@Override
	public void exitNav() {
		PackageManager.getInstance().closeApp(getPackageName());
		// 退出了应用
		onExitApp();
	}

	public String disableNavWithWayPoi() {
		return NativeData.getResString("RS_NAV_NOT_SUPPORT_THROUGH");
	}
	
	public boolean navigateWithWayPois(Poi startPoi, Poi endPoi, List<WayInfo> pois) {
		return false;
	}

	public String disableNavWithFromPoi(){
		return NativeData.getResString("RS_NAV_NOT_SUPPORT_FROM_POI");
	}
	
	/**
	 * TODO 获取导航当前的路线信息
	 * 
	 * @return
	 */
	public PathInfo getCurrentPathInfo() {
		if (!isInNav()) {
			return null;
		}
		if (mPathInfo != null) {
			return mPathInfo;
		}
		return null;
	}

	/**
	 * 根据给定路线发起导航
	 * @param info
	 */
	public boolean navigateByPathInfo(PathInfo info) {
		if (info == null) {
			throw new NullPointerException("pathInfo is null！");
		}

		NavigateInfo navigateInfo = new NavigateInfo();
		navigateInfo.msgGpsInfo = new GpsInfo();
		navigateInfo.msgGpsInfo.dblLat = info.toPoiLat;
		navigateInfo.msgGpsInfo.dblLng = info.toPoiLng;
		navigateInfo.strTargetName = info.toPoiName;
		navigateInfo.strTargetAddress = info.toPoiAddr;
		navigateInfo.strTargetCity = info.toCity;

		Poi startPoi = null;
		if (info.fromPoiLat != 0 && info.fromPoiLng != 0) {
			startPoi = new Poi();
			startPoi.setLat(info.fromPoiLat);
			startPoi.setLng(info.fromPoiLng);
			startPoi.setName(info.fromPoiName);
			startPoi.setGeoinfo(info.fromPoiAddr);
		}
		Poi endPoi = null;
		if (info.toPoiLat != 0 && info.toPoiLng != 0) {
			endPoi = new Poi();
			endPoi.setLat(info.toPoiLat);
			endPoi.setLng(info.toPoiLng);
			endPoi.setName(info.toPoiName);
			endPoi.setGeoinfo(info.toPoiAddr);
		}

		if (info.wayInfos == null || info.wayInfos.size() < 1) {
			NavigateTo(null, navigateInfo);
			return true;
		}

		return navigateWithWayPois(startPoi, endPoi, info.wayInfos);
	}

	private NavigateInfo destinationInfo;

	@Override
	@CallSuper
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		destinationInfo = info;
		return false;
	}

	/**
	 * 如果当前导航没开启，则将操作数据缓存，等待触发写入
	 *
	 * @return
	 */
	public boolean enableWorkWithoutResume() {
		return false;
	}
	
	/**
	 * 获取导航的版本号信息
	 * 
	 * @return
	 */
	public int getMapVersion() {
		return PackageManager.getInstance().getVerionCode(getPackageName());
	}
	
	/**
	 * 获取导航的版本信息
	 * @return
	 */
	public String getVersionName() {
		return PackageManager.getInstance().getVersionName(getPackageName());
	}

	@Override
	public void setNavStatusListener(NavStatusListener listener) {
		this.mNavStatusListener = listener;
	}

	static Runnable mDismissRunnable = new Runnable() {

		@Override
		public void run() {
			if (mWinConfirmAsr != null && mWinConfirmAsr.isShowing()) {
				LogUtil.logd("mWinConfirmAsr dismiss");
//				mWinConfirmAsr.dismiss();
				mWinConfirmAsr.dismiss("");
			}
		}
	};

	public boolean showTraffic(double lat, double lng) {
		return false;
	}

	public boolean showTraffic(String city, String addr) {
		return false;
	}

	public static void dismiss() {
		AppLogic.removeUiGroundCallback(mDismissRunnable);
		AppLogic.runOnUiGround(mDismissRunnable, 0);
	}

	public byte[] invokeTXZNav(String packageName, String command, byte[] data) {
		if (command.equals("savePlan")) {
			try {
				mEnableSave = Boolean.parseBoolean(new String(data));
				JNIHelper.logd("mEnableSave:" + mEnableSave);
			} catch (Exception e) {
			}
		}
		
		if (command.equals("autoNaviDelay")) {
			try {
				Integer auto = Integer.parseInt(new String(data));
				if (auto != null) {
					autoNavDelay = auto;
				}
				LogUtil.logd("autoNavDelay:" + autoNavDelay);
			} catch (Exception e) {
			}
		}
		return null;
	}
	
	public String getRemainTime(Integer rt){
		if (rt != null) {
			int rtd = rt;
			return getRemainTime(rtd);
		}
		return "";
	}

	public String getRemainTime(Long rt) {
		if (rt == null) {
			return "";
		}

		if (rt <= 0) {
			return "";
		}

		if (rt > 60) {
			if (rt >= 3600) {
				int r = (int) (rt % 3600);
				int h = (int) (rt / 3600);
				int m = r / 60;
				return h + "小时" + (m > 0 ? m + "分钟" : "");
			} else {
				return (rt / 60) + "分钟";
			}
		} else {
			return rt + "秒";
		}
	}
	
	public String getRemainDistance(Integer distance) {
		if (distance != null) {
			int dd = distance;
			return getRemainDistance(dd);
		}
		return "";
	}

	public String getRemainDistance(Long distance) {
		if (distance == null) {
			return "";
		}
		if (distance <= 0) {
			return "";
		}

		if (distance > 1000) {
			return (Math.round(distance / 100.0) / 10.0) + "公里";
		} else {
			return distance + "米";
		}
	}
	@Override
	public String getDestinationCity() {
		return null;
	}
	@Override
	public double[] getDestinationLatlng() {
		if (destinationInfo != null && destinationInfo.msgGpsInfo != null) {
			double[] gps = new double[2];
			gps[0] = destinationInfo.msgGpsInfo.dblLat;
			gps[1] = destinationInfo.msgGpsInfo.dblLng;
			return gps;
		}
		return null;
	}
	@Override
	public int getOnWaySearchToolCode(String keyword) {
		return -1;
	}
	public boolean reportTraffic(int event){
		return    false;
	}
}
