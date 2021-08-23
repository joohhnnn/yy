package com.txznet.txz.component.nav.gaode;

import android.content.Intent;
import android.net.Uri;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.util.LocationUtil;

public class NavAutoNavImpl extends NavThirdApp {
	public final static String PACKAGE_NAME = "com.autonavi.xmgd.navigator";
	
	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		try {
			double[] origin = LocationUtil.getGCJ02(LocationManager
					.getInstance().getLastLocation().msgGpsInfo);
			double[] dest = LocationUtil.getGCJ02(info.msgGpsInfo);

			int strategy = 0;
			switch (plan) {
			case NAV_PLAN_TYPE_AVOID_JAMS:
				strategy = 4;
				break;
			case NAV_PLAN_TYPE_LEAST_COST:
				strategy = 1;
				break;
			case NAV_PLAN_TYPE_LEAST_DISTANCE:
				strategy = 2;
				break;
			case NAV_PLAN_TYPE_LEAST_TIME:
				strategy = 0;
				break;
			case NAV_PLAN_TYPE_RECOMMEND:
			default:
				strategy = 0;
				break;
			}

			Intent intent = new Intent("com.autonavi.xmgd.action.NAVIGATOR");
			intent.setData(Uri.parse("NAVI:" + dest[1] + "," + dest[0]));
			// intent.setAction("android.intent.action.VIEW");
			// intent.addCategory("android.intent.category.DEFAULT");
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
