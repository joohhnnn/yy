package com.txznet.txz.component.nav.gaode;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.app.PackageManager;

import android.content.Intent;

/*public*/ class NavAmapCarNavImpl extends NavAmapAutoNavImpl {
	public static final String PACKAGE_NAME = "com.autonavi.amapautolite";

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		mNavAmapControl.setForceUseRecvImpl(true);
		return super.NavigateTo(plan, info);
	}

	@Override
	protected void setNavigateInfo(final boolean isHome, final NavigateInfo navigateInfo) {
		JNIHelper.logd("amapauto set " + (isHome ? "home" : "company"));
		PackageManager.getInstance().openApp(getPackageName());
		AppLogic.runOnBackGround(new Runnable() {

			@Override
			public void run() {
				Intent intent = new Intent("AUTONAVI_STANDARD_BROADCAST_RECV");
				intent.addFlags(Intent.FLAG_INCLUDE_STOPPED_PACKAGES);
				intent.putExtra("KEY_TYPE", 10058);
				intent.putExtra("POINAME", navigateInfo.strTargetName);
				intent.putExtra("LON", navigateInfo.msgGpsInfo.dblLng);
				intent.putExtra("LAT", navigateInfo.msgGpsInfo.dblLat);
				intent.putExtra("ADDRESS", navigateInfo.strTargetAddress);
				intent.putExtra("EXTRA_TYPE", isHome ? 1 : 2);
				intent.putExtra("DEV", 0);
				GlobalContext.get().sendBroadcast(intent);
			}
		}, 3000);
	}
}