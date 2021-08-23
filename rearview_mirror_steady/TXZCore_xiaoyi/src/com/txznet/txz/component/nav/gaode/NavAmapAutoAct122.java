package com.txznet.txz.component.nav.gaode;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.IMapInterface;
import com.txznet.txz.jni.JNIHelper;

import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;

public class NavAmapAutoAct122 implements IMapInterface {

	String mPackageName;

	@Override
	public void initialize() {
	}

	@Override
	public void setPackageName(String pkn) {
		this.mPackageName = pkn;
	}

	private String getPackageName() {
		return mPackageName;
	}

	@Override
	public void zoomAll(final Runnable run) {
		Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
		intent.putExtra("KEY_TYPE", 10006);
		intent.putExtra("EXTRA_IS_SHOW", 0);
		GlobalContext.get().sendBroadcast(intent);
		AppLogic.runOnUiGround(new Runnable() {
			@Override
			public void run() {
				if (run != null) {
					run.run();
				}

				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.putExtra("KEY_TYPE", 10006);
				intent.putExtra("EXTRA_IS_SHOW", 1);
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 10000);
	}

	@Override
	public void appExit() {
		try {
			Intent intent = new Intent();
			intent.setData(Uri.parse("androidauto://appExit?sourceApplication=txz"));
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setAction("android.intent.action.VIEW");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void naviExit() {
		try {
			Intent intent = new Intent();
			intent.setData(Uri.parse("androidauto://naviExit?sourceApplication=txz"));
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setAction("android.intent.action.VIEW");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			android.content.pm.PackageManager pkgManager = GlobalContext.get().getPackageManager();
			List<ResolveInfo> resolveInfo = pkgManager.queryIntentActivities(intent, 0);
			if (resolveInfo != null && resolveInfo.size() > 0) {
				GlobalContext.get().startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void zoomMap(boolean isZoomin) {
		try {
			if (isZoomin) {
				Intent intent = new Intent();
				intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&zoom=0"));
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setAction("android.intent.action.VIEW");
				intent.setPackage(getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				GlobalContext.get().startActivity(intent);
				JNIHelper.logd("NavAmapAutoNavImpl start ZOOM_IN");
			} else {
				Intent intent = new Intent();
				intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&zoom=1"));
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setAction("android.intent.action.VIEW");
				intent.setPackage(getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				GlobalContext.get().startActivity(intent);
				JNIHelper.logd("NavAmapAutoNavImpl start ZOOM_OUT");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void switchLightNightMode(boolean isLight) {
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
		try {
			if (isShowTraffic) {
				Intent intent = new Intent();
				intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&traffic=0"));
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setAction("android.intent.action.VIEW");
				intent.setPackage(getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				GlobalContext.get().startActivity(intent);
			} else {
				Intent intent = new Intent();
				intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&traffic=1"));
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setAction("android.intent.action.VIEW");
				intent.setPackage(getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				GlobalContext.get().startActivity(intent);
				JNIHelper.logd("NavAmapAutoNavImpl start CLOSE_TRAFFIC");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void switch23D(boolean is2d, int val) {
		try {
			if (is2d) {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setPackage(getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&switchView=" + val));
				GlobalContext.get().startActivity(intent);
			} else {
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setPackage(getPackageName());
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&switchView=2"));
				GlobalContext.get().startActivity(intent);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void switchCarDirection() {
		try {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&switchView=0"));
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void switchNorthDirection() {
		try {
			Intent intent = new Intent();
			intent.setAction("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setData(Uri.parse("androidauto://mapOpera?sourceApplication=txz&switchView=1"));
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void switchPlanStyle(PlanStyle ps) {
		if (ps == PlanStyle.BUZOUGAOSU) {
			selectNaviStyle(1, 1, 0, 1);
		}

		if (ps == PlanStyle.DUOBISHOUFEI) {
			selectNaviStyle(1, 0, 1, 1);
		}

		if (ps == PlanStyle.DUOBIYONGDU) {
			selectNaviStyle(0, 1, 1, 1);
		}

		if (ps == PlanStyle.GAOSUYOUXIAN) {
			selectNaviStyle(1, 1, 1, 0);
		}
	}

	/**
	 * isAvoidJam 躲避拥堵 0开； 1关 isVoidCharge 避免收费 0开； 1关 isAvoidHighway 不走高速 0开；
	 * 1关 usingHighway 高速优先 0开； 1关
	 * 
	 * @return
	 */
	private void selectNaviStyle(int isAvoidJam, int isVoidCharge, int isAvoidHighway, int usingHighway) {
		try {
			final Intent intent = new Intent();
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setAction("android.intent.action.VIEW");
			intent.setData(Uri.parse(
					"androidauto://naviRoutePrefer?sourceApplication=txz&isAvoidJam=" + isAvoidJam + "&isVoidCharge="
							+ isVoidCharge + "&isAvoidHighway=" + isAvoidHighway + "&usingHighway=" + usingHighway));
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 官方选路说法
	// 躲避拥堵
	// 避免收费
	// 不走高速
	// 高速优先
	private void changeNaviStyle(int type) {
		/**
		 * 0 速度快; 1 费用少; 2 路程短; 3 不走高速；4 躲避拥堵；5 不走高速且避免收费；6 不走高速且躲避拥堵；7
		 * 躲避收费和拥堵；8 不走高速躲避收费和拥堵
		 */
		final Intent intent = new Intent();
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setData(Uri.parse("androidauto://naviRoutePreferEx?sourceApplication=txz&type=" + type));
		intent.setPackage(getPackageName());
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
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
		try {
			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse("androidauto://navi?sourceApplication=txz&poiname=" + name + "&lat=" + lat + "&lon="
					+ lng + "&dev=0&style=" + style));
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
