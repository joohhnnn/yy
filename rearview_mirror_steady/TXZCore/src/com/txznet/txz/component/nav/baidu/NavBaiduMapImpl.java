package com.txznet.txz.component.nav.baidu;

import android.content.Intent;
import android.net.Uri;

import com.baidu.mapapi.model.LatLng;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.util.LocationUtil;

public class NavBaiduMapImpl extends NavThirdApp {
	public final static String PACKAGE_NAME = "com.baidu.BaiduMap";

	@Override
	public String getPackageName() {
		return PACKAGE_NAME;
	}

	@Override
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info) {
		super.NavigateTo(plan, info);
		try {
			LatLng origin = LocationUtil.getLocation(LocationManager
					.getInstance().getLastLocation().msgGpsInfo);
			LatLng dest = LocationUtil.getLocation(info.msgGpsInfo);

			
			String origin_city = LocationManager.getInstance()
					.getLastLocation().msgGeoInfo.strCity;
			String dest_city = info.strTargetCity;
			if (dest_city == null || dest_city.isEmpty()) {
				dest_city = origin_city;
			}

			Intent intent = new Intent();
			intent.setData(Uri
					.parse("bdapp://map/direction?coord_type=bd09ll&origin=latlng:"
							+ origin.latitude
							+ ","
							+ origin.longitude
							+ "|name:当前位置&destination=latlng:"
							+ dest.latitude
							+ ","
							+ dest.longitude
							+ "|name:"
							+ info.strTargetName
							+ "&mode=driving&origin_region="
							+ origin_city
							+ "&destination_region="
							+ dest_city
							+ "&src=同行者|"
							+ GlobalContext.get().getPackageName()));
			intent.setPackage(getPackageName());
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			GlobalContext.get().startActivity(intent);
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
