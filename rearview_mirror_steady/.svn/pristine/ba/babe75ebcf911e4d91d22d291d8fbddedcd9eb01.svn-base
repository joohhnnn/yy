package com.txznet.txz.component.nav.gaode;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NearbySearchInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.util.IntentUtil;
import com.txznet.txz.util.runnables.Runnable1;
import com.txznet.txz.util.runnables.Runnable4;

import android.content.Intent;
import android.text.TextUtils;

public class NavAmapAutoRecv122 implements IMapInterface {
	public static final String RECV_ACTION = "AUTONAVI_STANDARD_BROADCAST_SEND";
	public static final String SEND_ACTION = "AUTONAVI_STANDARD_BROADCAST_RECV";
	private String mPackageName;

	private NavAmapControl mNavImpl;

	public NavAmapAutoRecv122() {
	}

	public NavAmapAutoRecv122(NavAmapControl navImpl) {
		this.mNavImpl = navImpl;
	}

	@Override
	public void initialize() {
		// 初始化
		queryMapStatus();
		queryRoadInfo();
	}
	
	@Override
	public void enterNav() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10034);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void setPackageName(String pkn) {
		this.mPackageName = pkn;
	}

	public String getPackageName() {
		return mPackageName;
	}

	/**
	 * 获取导航状态
	 */
	public void queryMapStatus() {
		JNIHelper.logd("query map status");
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10061);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	/**
	 * 引导信息传递主动查询
	 */
	public void queryRoadInfo() {
		JNIHelper.logd("query roadinfo");
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10062);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void zoomAll(final Runnable run) {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10006);
		intent.putExtra("EXTRA_IS_SHOW", 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		JNIHelper.logd("zoom all enter！");

		AppLogic.removeBackGroundCallback(cRun);
		AppLogic.runOnBackGround(cRun, 50);

		resRun.update(run);
		AppLogic.removeBackGroundCallback(resRun);
		AppLogic.runOnBackGround(resRun, 10000);
	}
	
	Runnable cRun = new Runnable() {

		@Override
		public void run() {
			Intent intent2 = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent2.putExtra("KEY_TYPE", 10053);
			intent2.putExtra("SOURCE_APP", "txz");
			intent2.putExtra("EXTRA_AUTO_BACK_NAVI_DATA", false);
			intent2.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent2);
		}
	};
	
	Runnable1<Runnable> resRun = new Runnable1<Runnable>(null) {

		@Override
		public void run() {
			if (mP1 == null) {
				return;
			}
			JNIHelper.logd("zoom all exist！");

			mP1.run();

			Intent intent = new Intent(SEND_ACTION);
			intent.putExtra("KEY_TYPE", 10006);
			intent.putExtra("EXTRA_IS_SHOW", 1);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent);
		}
	};

	@Override
	public void appExit() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10021);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void naviExit() {
		endNavi();
		// 停止模拟导航
		endMNavi();
	}

	@Override
	public void zoomMap(boolean isZoomin) {
		int opera = 1;
		if (isZoomin) {
			opera = 0;
		}
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 1);
		intent.putExtra("EXTRA_OPERA", opera);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void switchLightNightMode(boolean isLight) {
		boolean useNew = false;
		int vc = mNavImpl != null ? mNavImpl.getMapCode() : -1;
		if (vc >= 143) {
			useNew = true;
		}

		if (vc == -1) {
			if (mNavImpl != null) {
				vc = PackageManager.getInstance().getVerionCode(getPackageName());
				if (vc >= 541) { // 用1.4.3以上版本
					useNew = true;
				}
			}
		}

		if (useNew) {
			int type = isLight ? 1 : 2;
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 10048);
			intent.putExtra("EXTRA_DAY_NIGHT_MODE", type);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent);
			LogUtil.logd("NavAmapAutoRecv122 switchLightNightMode 10048");
			return;
		}

		if (isLight) {
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 10017);
			intent.putExtra("EXTRA_HEADLIGHT_STATE", 1);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent);
			LogUtil.logd("NavAmapAutoRecv122 switchLightNightMode Light");
		} else {
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 10017);
			intent.putExtra("EXTRA_HEADLIGHT_STATE", 0);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent);
			LogUtil.logd("NavAmapAutoRecv122 switchLightNightMode Night");
		}
	}

	@Override
	public void switchTraffic(boolean isShowTraffic) {
		int opera = 0;
		if (!isShowTraffic) {
			opera = 1;
		}
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 0);
		intent.putExtra("EXTRA_OPERA", opera);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void switch23D(boolean is2d, int val) {
		int opera = val;
		if (!is2d) {
			opera = 2;
		}

		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 2);
		intent.putExtra("EXTRA_OPERA", opera);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void switchCarDirection() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 2);
		intent.putExtra("EXTRA_OPERA", 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void switchNorthDirection() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 2);
		intent.putExtra("EXTRA_OPERA", 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	/**
	 * 速度快	0
		费用少	1
		路程短	2
		不走高速	3
		躲避拥堵	4
		不走高速且避免收费	5
		不走高速且躲避拥堵	6
		躲避收费和拥堵	7
		不走高速躲避收费和拥堵	8
		高速优先	20
		躲避拥堵且高速优先	24
	 */
	
	@Override
	public void switchPlanStyle(PlanStyle ps) {
		String kw=null;
		if(ps==PlanStyle.JIAYOUZHAN){
			kw = "加油站";
		}else if(ps==PlanStyle.CESUO){
			kw = "厕所";
		}else if(ps==PlanStyle.ATM){
			kw = "ATM";
		}else if(ps==PlanStyle.WEIXIUZHAN){
			kw = "维修站";
		}
		if(kw!=null){
			navigateto(kw);
			return;
		}
		int type = 7; // 躲避拥堵和收费
		if (ps == PlanStyle.BUZOUGAOSU) {
			type = 3;
		} else if (ps == PlanStyle.DUOBISHOUFEI) {
			type = 1;
		} else if (ps == PlanStyle.DUOBIYONGDU) {
			type = 4;
		} else if (ps == PlanStyle.GAOSUYOUXIAN) {
			type = 20;
		} else if (ps == PlanStyle.MAINROAD) {
			switchRoadStyle(true);
			return;
		} else if(ps == PlanStyle.SIDEROAD){
			switchRoadStyle(false);
			return;
		} else if (ps == PlanStyle.REFRESHPATH) {
			refreshPath();
			return;
		}
		
		NavAmapAutoNavImpl naan = mNavImpl.mImpl;
		// 重新规划路线需要携带途经点信息，避免途经点丢失
		if (naan.mTJPois != null && naan.mTJPois.size() > 0) {
			naan.startNavByWayPois(type);
			return;
		}

		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10005);
		intent.putExtra("NAVI_ROUTE_PREFER", type);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
		JNIHelper.logd("switchPlanStyle:" + ps.name());
	}

	private void navigateto(String kw) {
		UiMap.NearbySearchInfo pbneNearbySearchInfo = new NearbySearchInfo();
		pbneNearbySearchInfo.strKeywords = kw;
		pbneNearbySearchInfo.strCenterPoi = "ON_WAY";
		JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
				UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, pbneNearbySearchInfo);
	}

	@Override
	public void backNavi() {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10006);
		intent.putExtra("EXTRA_IS_SHOW", 1);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	@Override
	public void switchBroadcastRole(int role) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10044);
		intent.putExtra("VOICE_ROLE", role);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}
	
	private boolean hasNavBefore;

	@Override
	public void navigateTo(String name, double lat, double lng, int style) {
		long delay = 0;
		if (!PackageManager.getInstance().isAppRunning(mPackageName)) {
			final Intent intent = GlobalContext.get().getPackageManager().getLaunchIntentForPackage(mPackageName);
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
				GlobalContext.get().startActivity(intent);
			}
			delay = 3000;
			LogUtil.logd("navigateTo delay:" + delay);
		}
		if (/*mNavImpl.mImpl.getMapDetCode() == 2201052 && */!hasNavBefore) {
			hasNavBefore = mNavImpl.mImpl.hasBeenOpen();
			if (!hasNavBefore) {
				hasNavBefore = true;
				delay = 5000; // 延迟5S，防止地图资源没有加载完发不起导航
				final Intent intent = GlobalContext.get().getPackageManager().getLaunchIntentForPackage(mPackageName);
				if (intent != null) {
					intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
					intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
					GlobalContext.get().startActivity(intent);
				}
				LogUtil.logd("hasNavBefore delay:" + delay);
			}
		}
		AppLogic.runOnBackGround(new Runnable4<String, Double, Double, Integer>(name, lat, lng, style) {
			@Override
			public void run() {
				navigateDirect(mP1, mP2, mP3, mP4);
			}
		}, delay);
	}

	@Override
	public void frontTraffic() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10109);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void switchSimpleMode(boolean isOpen) {
		int state = isOpen ? 0 : 1;
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 12107);
		intent.putExtra("HUD_IS_OPEN", state);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void queryCollectionPoint() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 11002);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void intoTeam() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10081);
		intent.putExtra("EXTRA_VIEWTYPE", 4);
		GlobalContext.get().sendBroadcast(intent);
	}

	/**
	 * STYLE (0 速度快; 1 费用少; 2 路程短; 3 不走高速；4 躲避拥堵；5 不走高速且避免收费；6 不走高速且躲避拥堵；7
	 * 躲避收费和拥堵；8 不走高速躲避收费和拥堵)
	 * 
	 * @param name
	 * @param lat
	 * @param lng
	 * @param type
	 */
	public void navigateDirect(String name, double lat, double lng, int type) {
		JNIHelper.logd("navigateDirect:" + name + ",lat:" + lat + ",lng:" + lng + ",type:" + type + " newFlag");
		Intent intent = new Intent(SEND_ACTION);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES|Intent.FLAG_RECEIVER_FOREGROUND);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		int vc = mNavImpl != null ? mNavImpl.getMapCode() : -1;
		if( (vc >= 270) && NavManager.getInstance().getGaoDeAutoPlanningRoute() ){
			intent.putExtra("KEY_TYPE", 10007);
			intent.putExtra("EXTRA_DLAT", lat);
			intent.putExtra("EXTRA_DLON", lng);
			if(TextUtils.isEmpty(name)){
				name = "快速导航";
			}
			intent.putExtra("EXTRA_DNAME", name);
			
			LocationInfo lastLocation = LocationManager.getInstance().getLastLocation();
			intent.putExtra("EXTRA_SNAME", "location");
			if (lastLocation != null && lastLocation.msgGpsInfo != null) {
				intent.putExtra("EXTRA_SLAT", lastLocation.msgGpsInfo.dblLat);
				intent.putExtra("EXTRA_SLON", lastLocation.msgGpsInfo.dblLng);
				JNIHelper.logd("navigateDirect: lastLocation" + "location" + ",lat:" + lastLocation.msgGpsInfo.dblLat
						+ ",lng:" + lastLocation.msgGpsInfo.dblLng + ",type:" + type);
			} else {
				JNIHelper.loge("lastLocation is null！");
			}
			
			intent.putExtra("EXTRA_DEV", 0);
			intent.putExtra("EXTRA_M", type);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent);
		}else{
			intent.putExtra("KEY_TYPE", 10038);
			intent.putExtra("SOURCE_APP", "txz");
			intent.putExtra("POINAME", name);
			intent.putExtra("LAT", lat);
			intent.putExtra("LON", lng);
			intent.putExtra("DEV", 0);
			intent.putExtra("STYLE", type);
			intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
			sendBroadcast(intent);
		}
	}

	// 开始导航，用于全程概览界面，执行点击开始导航动作
	public void startNavi() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10009);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	// 结束引导，通知退出导航状态
	public void endNavi() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10010);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}
	
	// 退出模拟导航
	public void endMNavi() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 12);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	// 重复播报当前的引导语
	public void broadcastAgain() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10003);
		intent.addFlags(Intent.FLAG_ACTIVITY_PREVIOUS_IS_TOP);
		sendBroadcast(intent);
	}

	private void sendBroadcast(Intent intent){
		IntentUtil.getInstance().sendBroadcastFixSetPackage(intent,getPackageName());
	}
	
		/**
	 * 切换道路
	 * @param true 主路 false 辅路
	 */
	public void switchRoadStyle(boolean isMainRoad){
		int type;
		if (isMainRoad) {
			type = 0;
		} else {
			type = 1;
		}
		Intent intent = new Intent();
		intent.setAction(SEND_ACTION);
		intent.putExtra("KEY_TYPE",12013);
		intent.putExtra("EXTRA_TYPE", type);
		GlobalContext.get().sendBroadcast(intent);
	}

	public void refreshPath(){
		Intent intent = new Intent();
		intent.setAction(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 12012);
		GlobalContext.get().sendBroadcast(intent);
	}
}
