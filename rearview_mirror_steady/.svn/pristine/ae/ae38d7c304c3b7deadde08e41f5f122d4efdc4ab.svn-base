package com.txznet.txz.component.nav.gaode;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;
import com.txznet.txz.util.runnables.Runnable4;

import android.content.Intent;

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
	}

	@Override
	public void setPackageName(String pkn) {
		this.mPackageName = pkn;
	}

	public String getPackageName() {
		return mPackageName;
	}

	@Override
	public void zoomAll(final Runnable run) {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10006);
		intent.putExtra("EXTRA_IS_SHOW", 0);
		GlobalContext.get().sendBroadcast(intent);

		Intent intent2 = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent2.putExtra("KEY_TYPE", 10053);
		intent2.putExtra("SOURCE_APP", "txz");
		intent2.putExtra("EXTRA_AUTO_BACK_NAVI_DATA", false);
		GlobalContext.get().sendBroadcast(intent2);

		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (run != null) {
					run.run();
				}

				Intent intent = new Intent(SEND_ACTION);
				intent.putExtra("KEY_TYPE", 10006);
				intent.putExtra("EXTRA_IS_SHOW", 1);
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 10000);
	}

	@Override
	public void appExit() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10021);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void naviExit() {
		endNavi();
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
		GlobalContext.get().sendBroadcast(intent);
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
			GlobalContext.get().sendBroadcast(intent);
			return;
		}

		if (isLight) {
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 10017);
			intent.putExtra("EXTRA_HEADLIGHT_STATE", 1);
			GlobalContext.get().sendBroadcast(intent);
		} else {
			Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
			intent.putExtra("KEY_TYPE", 10017);
			intent.putExtra("EXTRA_HEADLIGHT_STATE", 0);
			GlobalContext.get().sendBroadcast(intent);
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
		GlobalContext.get().sendBroadcast(intent);
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
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void switchCarDirection() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 2);
		intent.putExtra("EXTRA_OPERA", 0);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void switchNorthDirection() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10027);
		intent.putExtra("EXTRA_TYPE", 2);
		intent.putExtra("EXTRA_OPERA", 1);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void switchPlanStyle(PlanStyle ps) {
		int type = 7; // 躲避拥堵和收费
		if (ps == PlanStyle.BUZOUGAOSU) {
			type = 3;
		} else if (ps == PlanStyle.DUOBISHOUFEI) {
			type = 1;
		} else if (ps == PlanStyle.DUOBIYONGDU) {
			type = 4;
		} else if (ps == PlanStyle.GAOSUYOUXIAN) {
			type = 20;
		}
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10005);
		intent.putExtra("NAVI_ROUTE_PREFER", type);
		GlobalContext.get().sendBroadcast(intent);
		JNIHelper.logd("switchPlanStyle:" + ps.name());
	}

	@Override
	public void backNavi() {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10006);
		intent.putExtra("EXTRA_IS_SHOW", 1);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void switchBroadcastRole(int role) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10044);
		intent.putExtra("VOICE_ROLE", role);
		GlobalContext.get().sendBroadcast(intent);
	}

	@Override
	public void navigateTo(String name, double lat, double lng, int style) {
		long delay = 0;
		 if (!PackageManager.getInstance().isAppRunning(mPackageName)) {
			final Intent intent = GlobalContext.get().getPackageManager().getLaunchIntentForPackage(mPackageName);
			if (intent != null) {
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				GlobalContext.get().startActivity(intent);
			}
			delay = 2000;
		}
		AppLogic.runOnBackGround(new Runnable4<String, Double, Double, Integer>(name, lat, lng, style) {
			@Override
			public void run() {
				navigateDirect(mP1, mP2, mP3, mP4);
			}
		}, delay);
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
		JNIHelper.logd("navigateDirect:" + name + ",lat:" + lat + ",lng:" + lng + ",type:" + type);
		Intent intent = new Intent(SEND_ACTION);
		intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
		intent.putExtra("KEY_TYPE", 10038);
		intent.putExtra("SOURCE_APP", "txz");
		intent.putExtra("POINAME", name);
		intent.putExtra("LAT", lat);
		intent.putExtra("LON", lng);
		intent.putExtra("DEV", 0);
		intent.putExtra("STYLE", type);
		GlobalContext.get().sendBroadcast(intent);
	}

	// 开始导航，用于全程概览界面，执行点击开始导航动作
	public void startNavi() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10009);
		GlobalContext.get().sendBroadcast(intent);
	}

	// 结束引导，通知退出导航状态
	public void endNavi() {
		Intent intent = new Intent(SEND_ACTION);
		intent.putExtra("KEY_TYPE", 10010);
		GlobalContext.get().sendBroadcast(intent);
	}
}
