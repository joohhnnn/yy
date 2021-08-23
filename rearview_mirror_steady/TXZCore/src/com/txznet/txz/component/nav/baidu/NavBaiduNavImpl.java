package com.txznet.txz.component.nav.baidu;

import android.content.Intent;
import android.net.Uri;

import com.baidu.mapapi.model.LatLng;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.R;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.util.LocationUtil;

/*public*/ class NavBaiduNavImpl extends NavThirdApp {
	public final static String PACKAGE_NAME = "com.baidu.navi";

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		try {
			// 10(系统推荐),20(最短时间),30(最短距离),40(最少收费),60(躲避拥堵)
			// bdnavi://plan?coordType=wgs84ll&src=yourAppIdentifier&dest=23.151433,113.252300,广州火车站
			LatLng origin = LocationUtil.getLocation(LocationManager
					.getInstance().getLastLocation().msgGpsInfo);
			LatLng dest = LocationUtil.getLocation(info.msgGpsInfo);
			if (dest == null)
				return false;
			String type = "bd09ll";
			int strategy = 10;
			switch (plan) {
			case NAV_PLAN_TYPE_AVOID_JAMS:
				strategy = 60;
				break;
			case NAV_PLAN_TYPE_LEAST_COST:
				strategy = 40;
				break;
			case NAV_PLAN_TYPE_LEAST_DISTANCE:
				strategy = 30;
				break;
			case NAV_PLAN_TYPE_LEAST_TIME:
				strategy = 20;
				break;
			case NAV_PLAN_TYPE_RECOMMEND:
			default:
				strategy = 10;
				break;
			}
			String url = "bdnavi://plan?coordType=" + type + "&src="
					+ R.string.appid_baidumap + "&dest=" + dest.latitude + ","
					+ dest.longitude + "," + info.strTargetName + "&strategy="
					+ strategy;
			// 起始点
			if (origin != null) {
				url += "&start=" + origin.latitude + "," + origin.longitude;
			}
			Intent intent = new Intent();
			intent.setData(Uri.parse(url));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.setPackage(PACKAGE_NAME);
			GlobalContext.get().startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

}
