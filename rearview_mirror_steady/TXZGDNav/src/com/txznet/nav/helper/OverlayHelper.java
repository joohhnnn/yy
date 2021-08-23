package com.txznet.nav.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.amap.api.maps.AMap;
import com.amap.api.navi.view.RouteOverLay;
import com.txznet.loader.AppLogic;
import com.txznet.nav.data.NaviPath;

public class OverlayHelper {
	private AMap aMap;
	private Map<String, RouteOverLay> mId2NaviPathMap = new HashMap<String, RouteOverLay>();
	private static OverlayHelper instance = new OverlayHelper();

	public static OverlayHelper getInstance() {
		return instance;
	}

	public void init(AMap aMap) {
		this.aMap = aMap;
		destory();
	}

	public void addOverlay(NaviPath path) {
		if (!isInitOk()) {
			return;
		}

		if (path == null) {
			return;
		}

		if (isExistUid(path.getUid())) {
			if (path.isReplan()) {
				RouteOverLay ro = mId2NaviPathMap.get(path.getUid());
				ro.removeFromMap();
				ro.destroy();
				ro.setRouteInfo(path);
				ro.addToMap();
			}
		} else {
			RouteOverLay ro = createOverlay(path);
			if (ro != null) {
				ro.addToMap();
				mId2NaviPathMap.put(path.getUid(), ro);
			}
		}
	}

	public void removeUid(String uid) {
		if (isExistUid(uid)) {
			RouteOverLay ro = mId2NaviPathMap.get(uid);
			if (ro != null) {
				ro.removeFromMap();
				ro.destroy();
				ro = null;

				mId2NaviPathMap.remove(uid);
			}
		}
	}

	public void destory() {
		Set<String> uids = mId2NaviPathMap.keySet();
		if (uids == null) {
			return;
		}

		for (String uid : uids) {
			removeUid(uid);
		}
	}

	public boolean isExistUid(String uid) {
		if (mId2NaviPathMap.containsKey(uid)) {
			return true;
		}
		return false;
	}

	private boolean isInitOk() {
		return aMap != null;
	}

	private RouteOverLay createOverlay(NaviPath path) {
		RouteOverLay ro = new RouteOverLay(aMap, path, AppLogic.getApp());
		return ro;
	}
}
