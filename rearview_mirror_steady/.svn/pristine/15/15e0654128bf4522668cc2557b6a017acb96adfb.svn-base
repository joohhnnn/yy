package com.txznet.txz.component.nav.qihoo;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.IMapInterface;

import android.content.Intent;
import android.net.Uri;

public class QihooMapInterface implements IMapInterface {

	private String mPackageName;

	@Override
	public void initialize() {
	}

	@Override
	public void setPackageName(String pkn) {
		mPackageName = pkn;
	}

	@Override
	public void zoomAll(final Runnable run) {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setPackage(mPackageName);
		intent.setData(Uri.parse("androidauto://routeoverview?source=txz&switch=0"));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
		AppLogic.runOnUiGround(new Runnable() {

			@Override
			public void run() {
				if (run != null) {
					run.run();
				}
				Intent intent = new Intent("android.intent.action.VIEW");
				intent.addCategory("android.intent.category.DEFAULT");
				intent.setPackage(mPackageName);
				intent.setData(Uri.parse("androidauto://routeoverview?source=txz&switch=1"));
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				GlobalContext.get().startActivity(intent);
			}
		}, 10000);
	}

	@Override
	public void appExit() {
	}

	@Override
	public void naviExit() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setPackage(mPackageName);
		intent.setData(Uri.parse("androidauto://navistop?source=txz"));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

	@Override
	public void zoomMap(boolean isZoomin) {
		if (isZoomin) {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://mapopera?source=txz&zoom=1"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} else {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://mapopera?source=txz&zoom=0"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		}
	}

	@Override
	public void switchLightNightMode(boolean isLight) {
	}

	@Override
	public void switchTraffic(boolean isShowTraffic) {
		if (isShowTraffic) {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://maptraffic?source=txz&switch=1"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} else {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://maptraffic?source=txz&switch=0"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		}
	}

	@Override
	public void switch23D(boolean is2d, int val) {
		if (is2d) {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://mapopera?source=txz&view=0"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} else {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://mapopera?source=txz&view=1"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		}
	}

	@Override
	public void switchCarDirection() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setPackage(mPackageName);
		intent.setData(Uri.parse("androidauto://mapopera?source=txz&north=0"));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

	@Override
	public void switchNorthDirection() {
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setPackage(mPackageName);
		intent.setData(Uri.parse("androidauto://mapopera?source=txz&north=1"));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

	@Override
	public void switchPlanStyle(PlanStyle ps) {
		int mode = 1;
		if (ps == PlanStyle.BUZOUGAOSU) {
			mode = 2;
		} else if (ps == PlanStyle.DUOBIYONGDU) {
			mode = 1;
		} else if (ps == PlanStyle.DUOBISHOUFEI) {
			mode = 3;
		} else if (ps == PlanStyle.GAOSUYOUXIAN) {
			mode = 0;
		}
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setPackage(mPackageName);
		intent.setData(Uri.parse("androidauto://navirouteplan?source=txz&mode=" + mode));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

	@Override
	public void backNavi() {
	}

	@Override
	public void switchBroadcastRole(int role) {
	}

	@Override
	public void navigateTo(String name, double lat, double lng,int style) {
		// 0 速度快 1 躲避拥堵 2 不走高速 3 少收费 4 躲避拥堵+不走高速 5 躲避拥堵+少收费 6 不走高速+少收费 7
		// 躲避拥堵+不走高速+少收费
		// 躲避拥堵+不走高速+少收费
		Intent intent = new Intent("android.intent.action.VIEW");
		intent.addCategory("android.intent.category.DEFAULT");
		intent.setPackage(mPackageName);
		intent.setData(Uri.parse(
				"androidauto://navi?source=txz&poiname=" + name + "&lat=" + lat + "&lon=" + lng + "&coord=0&mode=1"));
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		GlobalContext.get().startActivity(intent);
	}

/*	@Override
	public void onNavCommand(boolean isWakeupResult, String type, String speech) {
	}

	@Deprecated
	@Override
	public void addCommand(AsrComplexSelectCallback acsc, boolean mIsStarted, boolean mTraffic, boolean m2dView,
			boolean mCarDirect, boolean mEnableRole) {
		JNIHelper.logd("start addAsr");
		AsrSources as = AsrKeySourceManager.getInstance().getQihooAsrKeys();
		if (as != null && as.getAsrKeySources() != null) {
			for (AsrKeySource aks : as.getAsrKeySources()) {
				if (!mIsStarted) {
					if (AsrKeySourceManager.getInstance().mNaviTypes.contains(aks.getKeyType())) {
						continue;
					}
				}
				if (!mTraffic) {
					if (AsrKeySourceManager.getInstance().mTrafficTypes.contains(aks.getKeyType())) {
						continue;
					}
				}
				if (!m2dView) {
					if (AsrKeySourceManager.getInstance().m23DTypes.contains(aks.getKeyType())) {
						continue;
					}
				}
				if (!mCarDirect) {
					if (AsrKeySourceManager.getInstance().mCarDirTypes.contains(aks.getKeyType())) {
						continue;
					}
				}
				if (!mEnableRole) {
					if (AsrKeySourceManager.getInstance().mEnableRoleTypes.contains(aks.getKeyType())) {
						continue;
					}
				}
				acsc.addCommand(aks.getKeyType(), aks.getKeyCmds());
			}
		}
		JNIHelper.logd("end addAsr");
	}
*/
	public void onSpeechRemain() {
		try {
			Intent intent = new Intent("android.intent.action.VIEW");
			intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(mPackageName);
			intent.setData(Uri.parse("androidauto://palyrestinfo?source=txz"));
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
		} catch (Exception e) {
		}
	}

	public void updateHomeLocation(String name, double lat, double lng) {
		try {
			Intent mInten = new Intent("android.intent.action.VIEW",
					android.net.Uri.parse("androidauto://setcommonaddress?source=txz&commonaddress=0&commonname=" + name
							+ "&lat=" + lat + "&lon=" + lng + "&coord=0"));
			mInten.addCategory("android.intent.category.DEFAULT");
			mInten.setPackage(mPackageName);
			mInten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(mInten);
		} catch (Exception e) {
		}
	}

	public void updateCompanyLocation(String name, double lat, double lng) {
		try {
			Intent mInten = new Intent("android.intent.action.VIEW",
					android.net.Uri.parse("androidauto://setcommonaddress?source=txz&commonaddress=1&commonname=" + name
							+ "&lat=" + lat + "&lon=" + lng + "&coord=0"));
			mInten.addCategory("android.intent.category.DEFAULT");
			mInten.setPackage(mPackageName);
			mInten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(mInten);
		} catch (Exception e) {
		}
	}
	
	public void playforwardvideo(boolean bOpen) {
		try {
			Intent mInten = new Intent("android.intent.action.VIEW", android.net.Uri
					.parse("androidauto://playforwardvideo?source=appname&status=" + (bOpen ? "1" : "0")));
			mInten.addCategory("android.intent.category.DEFAULT");
			mInten.setPackage(mPackageName);
			mInten.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(mInten);
		} catch (Exception e) {
		}
	}
}
