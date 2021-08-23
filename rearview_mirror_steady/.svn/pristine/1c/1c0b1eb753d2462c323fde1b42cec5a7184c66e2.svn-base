package com.txznet.txz.component.nav.tx.internal;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.tx.NavTXNavImpl;
import com.txznet.txz.jni.JNIHelper;

import android.os.Bundle;

public class TNBroadcastSender {
	private NavTXNavImpl mImpl;

	private static class LayzerHolder {
		private static final TNBroadcastSender sIntance = new TNBroadcastSender();
	}

	private TNBroadcastSender() {
	}

	public static TNBroadcastSender getInstance() {
		return LayzerHolder.sIntance;
	}
	
	public void initParent(NavTXNavImpl navImpl) {
		mImpl = navImpl;
	}

	/**
	 * 对于那些需要反馈的广播指令，可以发送广播让导航取消反馈。
	 * 例如：发起一个检索广播，但因为网络超时原因，导航长时间没有返回，第三方可以发送结束这个检索指令给导航。
	 */
	public void stopBroadcastCommand() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.STOP_COMMAND;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 放大缩小地图
	 * 
	 * @param small
	 *            true是缩小地图，false是放大地图
	 */
	public void zoomMap(boolean small) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.MAP;
		int extra_type = 1;
		int extra_opera = small ? 1 : 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 设置地图车点模式
	 * 
	 * @param mode
	 *            0是2d正北朝上，1是3d车头朝上
	 */
	public void setCar(int mode) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.MAP;
		int extra_type = 2;
		int extra_opera = mode;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 设置地图模式
	 * 
	 * @param day
	 *            true是白天模式，false是黑夜模式
	 */
	public void setStyle(boolean day) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.MAP;
		int extra_type = 3;
		int extra_opera = day ? 0 : 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 设置路况开关
	 * 
	 * @param open
	 *            true是打开路况，false是关闭路况
	 */
	public void setTraffic(boolean open) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.MAP;
		int extra_type = 0;
		int extra_opera = open ? 0 : 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 打开导航app应用
	 */
	public void openNaviApp(boolean needFeedBack) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_OPEN_CLOSE;
		int extra_opera = 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_NEED_FEEDBACK, needFeedBack ? 0 : 1);
		bundle.putBoolean(ExternalDefaultBroadcastKey.KEY.EXTRA_NOTIMEOUT, true);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 退出导航app应用
	 */
	public void exitNaviApp(boolean needFeedBack) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_OPEN_CLOSE;
		int extra_opera = 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_NEED_FEEDBACK, needFeedBack ? 0 : 1);
		bundle.putBoolean(ExternalDefaultBroadcastKey.KEY.EXTRA_NOTIMEOUT, true);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 开始导航
	 */
	public void startNavi() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.ROUTE_PLAN_START_STOP_NAVI;
		int extra_opera = 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		bundle.putBoolean(ExternalDefaultBroadcastKey.KEY.EXTRA_NOTIMEOUT, true);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 结束导航
	 * 
	 * @param value
	 *            0 弹框 1不弹框
	 */
	public void closeNavi(int value, boolean needFeedBack) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.ROUTE_PLAN_START_STOP_NAVI;
		int extra_opera = 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_EXIT_TYPE, value);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_NEED_FEEDBACK, needFeedBack ? 0 : 1);
		bundle.putBoolean(ExternalDefaultBroadcastKey.KEY.EXTRA_NOTIMEOUT, true);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 回家
	 */
	public void goHome() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_TO_HOME_COMPANY;
		int extra_opera = 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 去公司
	 */
	public void goCompany() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_TO_HOME_COMPANY;
		int extra_opera = 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 导航过程中查看或者退出全程概览图
	 * 
	 * @param show
	 *            true查看概览图，false退出概览图
	 */
	public void showOverviewMap(boolean show) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.RG_FULLVIEW;
		int extra_opera = show ? 0 : 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		// 版本号小于33的腾讯导航使用导航的反馈语（由于这些版本没有通知导航状态），其它使用语音的反馈语
		if (mImpl != null && mImpl.getMapVersion() <= 32) {
			bundle.putString(ExternalDefaultBroadcastKey.KEY.TAG,
					ExternalDefaultBroadcastKey.FB_SESSION_DEFAULT.VIEW_ALL_SESSION);
		}
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 选择路线结果
	 * 
	 * @param index
	 *            从0开始，选择的index
	 */
	public void selectRoute(int index) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.ROUTE_PLAN_SELECT;
		int extra_opera = index;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 以当前车点位置为起点，快速发起导航
	 * 
	 * @param poi
	 *            终点位置
	 */
	public void naviTo(TNPoi poi) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_TO_POI;
		String SOURCE_APP = TNBroadcastManager.getInstance().getContext().getPackageName();
		String POINAME = poi.poiName;
		TNLatLng coordinate = (poi.naviCoordinate != null ? poi.naviCoordinate : poi.coordinate);
		double LAT = coordinate.getLatitude();
		double LON = coordinate.getLongitude();
		int COORD = coordinate.getCoordinateSystem();
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putString(ExternalDefaultBroadcastKey.KEY.SOURCE_APP, SOURCE_APP);
		bundle.putString(ExternalDefaultBroadcastKey.KEY.POINAME, POINAME);
		bundle.putDouble(ExternalDefaultBroadcastKey.KEY.LAT, LAT);
		bundle.putDouble(ExternalDefaultBroadcastKey.KEY.LON, LON);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.COORD, COORD);
		JNIHelper.logd("LAT:" + LAT + ",LNG:" + LON + ",COORD:" + COORD);
		sendBroadcast(key_type, bundle);
	}

	public void requestWhereAmI() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.REQ_CUR_ADDRESS;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		sendBroadcast(key_type, bundle);
	}

	public void requestRemianTime() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.REQ_REMAIN_TIME_DISTANCE;
		int opera = 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, opera);
		sendBroadcast(key_type, bundle);
	}

	public void requestRemianDistance() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.REQ_REMAIN_TIME_DISTANCE;
		int opera = 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, opera);
		sendBroadcast(key_type, bundle);
	}

	public void setHomeAddr(String name, double lat, double lng, String addr) {
		setAddrInner(name, lat, lng, addr, 0, 0);
	}

	public void setCompanyAddr(String name, double lat, double lng, String addr) {
		setAddrInner(name, lat, lng, addr, 1, 0);
	}

	/**
	 * 搜索家和公司地址
	 * 
	 * @param type
	 *            0为家 1为公司
	 */
	public void queryAddr() {
		queryHome();
		queryCompany();
	}

	public void queryHome() {
		AppLogic.removeBackGroundCallback(homeRun);
		AppLogic.runOnBackGround(homeRun, 10);
	}

	public void queryCompany() {
		AppLogic.removeBackGroundCallback(companyRun);
		AppLogic.runOnBackGround(companyRun, 10);
	}

	Runnable homeRun = new Runnable() {

		@Override
		public void run() {
			queryAddrInner(0);
		}
	};

	Runnable companyRun = new Runnable() {

		@Override
		public void run() {
			queryAddrInner(1);
		}
	};

	private void queryAddrInner(int type) {
		Bundle bundle = new Bundle();
		bundle.putInt("EXTRA_TYPE", type);
		bundle.putBoolean(ExternalDefaultBroadcastKey.KEY.EXTRA_NOTIMEOUT, true);
		JNIHelper.logd("TNB queryAddrInner:" + type);
		SRActionDispatcher.getInstance().action(ExternalDefaultBroadcastKey.TYPE.NAVI_QUERY_HOME_COMPANY_ADDR, bundle);
	}

	private void setAddrInner(String name, double lat, double lng, String addr, int type, int dev) {
		Bundle bundle = new Bundle();
		bundle.putString(ExternalDefaultBroadcastKey.KEY.POINAME, name);
		bundle.putDouble(ExternalDefaultBroadcastKey.KEY.LAT, lat);
		bundle.putDouble(ExternalDefaultBroadcastKey.KEY.LON, lng);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.COORD, 1);
		bundle.putString(ExternalDefaultBroadcastKey.KEY.ADDRESS, addr);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, type);
		bundle.putBoolean(ExternalDefaultBroadcastKey.KEY.EXTRA_NOTIMEOUT, true);
		JNIHelper.logd("TNB setAddrInner:" + type);

		SRActionDispatcher.getInstance().action(ExternalDefaultBroadcastKey.TYPE.NAVI_SET_HOME_COMPANY_ADDR, bundle);
	}

	/**
	 * 0表示躲避拥堵，1表示不走高速，2表示少收费，3表示高速优先
	 *
	 * @param strategy
	 */
	public void replanNaviStrategy(int strategy) {
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, strategy);
		LogUtil.logd("replanNaviStrategy:" + strategy);
		SRActionDispatcher.getInstance().action(ExternalDefaultBroadcastKey.TYPE.NAVI_REPLAN_ROUTE, bundle);
	}

	/**
	 * 设置自动模式 话术：自动模式
	 */
	public void setAutoStyle() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.MAP;
		int extra_type = 3;
		int extra_opera = 2;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_OPERA, extra_opera);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 设置新手模式 话术：新手模式，详细模式
	 */
	public void setSimpleStyle() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_BROADCAST_SPEECH_MODE;
		int extra_type = 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 设置老手模式 话术：老手模式，简洁模式
	 */
	public void setDetailStyle() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_BROADCAST_SPEECH_MODE;
		int extra_type = 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 设置静音模式 话术：导航静音，关闭导航声音
	 */
	public void closeVoice() {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_SPEECH_MUTE_MODE;
		int extra_type = 0;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		sendBroadcast(key_type, bundle);
	}
	
	/**
	 * 设置对话框自动消失的时间
	 * @param delay
	 */
	public void setConfirmDialogAutoDimissDelay(int s) {
		int key_type = ExternalDefaultBroadcastKey.TYPE.NAVI_PUSH_WXPOI_DIALOG_AUTO_DISMISS;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.TIME, s);
		sendBroadcast(key_type, bundle);
	}

	/**
	 * 恢复导航声音 话术：退出导航静音恢，恢复导航声音，打开导航声音
	 */
	public void resumeVoice() {
		int key_type = 1018;
		int extra_type = 1;
		Bundle bundle = new Bundle();
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.KEY_TYPE, key_type);
		bundle.putInt(ExternalDefaultBroadcastKey.KEY.EXTRA_TYPE, extra_type);
		sendBroadcast(key_type, bundle);
	}

	public void sendBroadcast(int keyType, Bundle extra) {
		SRActionDispatcher.getInstance().action(keyType, extra);
	}
}
