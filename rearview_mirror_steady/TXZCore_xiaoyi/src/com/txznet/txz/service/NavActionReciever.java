package com.txznet.txz.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.txz.ui.event.UiEvent;
import com.txz.ui.map.UiMap;
import com.txz.ui.map.UiMap.GpsInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NearbySearchInfo;
import com.txznet.comm.util.ProtoBufferUtil;
import com.txznet.sdk.bean.Poi.PoiAction;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;

public class NavActionReciever extends BroadcastReceiver {

	public static NavigateInfo readNavigateInfo(Intent intent) {
		NavigateInfo navigateInfo = new NavigateInfo();
		if (intent.hasExtra("country"))
			navigateInfo.strCountry = intent.getStringExtra("country");
		if (intent.hasExtra("province"))
			navigateInfo.strProvince = intent.getStringExtra("province");
		if (intent.hasExtra("area"))
			navigateInfo.strArea = intent.getStringExtra("area");
		if (intent.hasExtra("region"))
			navigateInfo.strRegion = intent.getStringExtra("region");
		if (intent.hasExtra("city"))
			navigateInfo.strTargetCity = intent.getStringExtra("city");
		if (intent.hasExtra("name"))
			navigateInfo.strTargetName = intent.getStringExtra("name");

		if (intent.hasExtra("poi.lat") && intent.hasExtra("poi.lng")) {
			navigateInfo.msgGpsInfo = new GpsInfo();
			navigateInfo.msgGpsInfo.dblLat = intent.getDoubleExtra("poi.lat",
					.0F);
			navigateInfo.msgGpsInfo.dblLng = intent.getDoubleExtra("poi.lng",
					.0F);
			if (intent.getStringExtra("poi.type").equalsIgnoreCase("wgs84")) {
				navigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_WGS84;
			} else if (intent.getStringExtra("poi.type").equalsIgnoreCase(
					"bd09")) {
				navigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_BD09;
			} else {
				navigateInfo.msgGpsInfo.uint32GpsType = UiMap.GPS_TYPE_GCJ02;
			}
		}

		return navigateInfo;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();

		JNIHelper.logd("recive action: " + action);

		if (action.equals("com.txznet.txz.nav.search.poi")) {
			NavigateInfo navigateInfo = readNavigateInfo(intent);
			if (navigateInfo != null
					&& ProtoBufferUtil
							.isStringEmpty(navigateInfo.strTargetName) == false) {
				NavManager.getInstance().navigateByName(navigateInfo, true,
						PoiAction.ACTION_NAVI, false);
			}
			return;
		}

		if (action.equals("com.txznet.txz.nav.search.nearby")) {
			NavigateInfo navigateInfo = readNavigateInfo(intent);
			if (navigateInfo != null
					&& ProtoBufferUtil
							.isStringEmpty(navigateInfo.strTargetName) == false) {
				NearbySearchInfo info = new NearbySearchInfo();
				info.strKeywords = navigateInfo.strTargetName;
				JNIHelper.sendEvent(UiEvent.EVENT_SYSTEM_MAP,
						UiMap.SUBEVENT_MAP_NAVIGATE_NEARBY, info);
			}
			return;
		}
	}
}
