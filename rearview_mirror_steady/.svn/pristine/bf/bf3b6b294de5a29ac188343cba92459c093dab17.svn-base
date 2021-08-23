package com.txznet.nav;

import org.json.JSONObject;

import com.amap.api.navi.model.NaviInfo;
import com.google.protobuf.nano.MessageNano;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.DatasUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.nav.manager.NavManager;

public class ServiceRequest {

	public static void sendBeginInvoke(NavigateInfo info) {
		try {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.multi.nav.inner.beginMultiNav",
					MessageNano.toByteArray(info), null);
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
	}

	public static void sendEndInvoke(NavigateInfo info) {
		try {
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.multi.nav.inner.endMultiNav",
					MessageNano.toByteArray(info), null);
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
	}

	public static void sendGetMemberListInfo(long roomId, int type,
			int distance, int time) {
		try {
			byte[] datas = DatasUtil.convertBytes(roomId, type, distance, time);
			ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ,
					"txz.multi.nav.inner.getMultiNavMemList", datas, null);
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
	}

	public static void sendNaviPathToServer() {
		JSONBuilder jb = new JSONBuilder();
	}

	public static void sendNaviInfo(NaviInfo naviInfo) {
		try {
			JSONObject jo = new JSONObject();
			jo.put("CameraDistance", naviInfo.getCameraDistance());
			jo.put("CameraType", naviInfo.getCameraType());
			jo.put("CurLink", naviInfo.getCurLink());
			jo.put("CurPoint", naviInfo.getCurPoint());
			jo.put("CurStep", naviInfo.getCurStep());
			jo.put("CurStepRetainDistance", naviInfo.getCurStepRetainDistance());
			jo.put("CurStepRetainTime", naviInfo.getCurStepRetainTime());
			jo.put("Direction", naviInfo.getDirection());
			jo.put("IconType", naviInfo.getIconType());
			jo.put("LimitSpeed", naviInfo.getLimitSpeed());
			jo.put("NaviType", naviInfo.getNaviType());
			jo.put("PathRetainDistance", naviInfo.getPathRetainDistance());
			jo.put("PathRetainTime", naviInfo.getPathRetainTime());
			jo.put("ServiceAreaDistance", naviInfo.getServiceAreaDistance());
			if (naviInfo.getCameraCoord() != null) {
				jo.put("HasCameraCoord", "true");
				jo.put("CameraCoordLatitude", naviInfo.getCameraCoord()
						.getLatitude());
				jo.put("CameraCoordLongitude", naviInfo.getCameraCoord()
						.getLongitude());
			} else {
				jo.put("HasCameraCoord", "false");
			}
			if (naviInfo.getCoord() != null) {
				jo.put("HasCoord", "true");
				jo.put("CoordLatitude", naviInfo.getCoord().getLatitude());
				jo.put("CoordLongitude", naviInfo.getCoord().getLongitude());
			} else {
				jo.put("HasCoord", "false");
			}
			jo.put("CurrentRoadName", naviInfo.getCurrentRoadName());
			jo.put("NextRoadName", naviInfo.getNextRoadName());
//			ServiceManager.getInstance().sendInvoke(ServiceManager.NAV,
//					"txz.nav.update.naviinfo", jo.toString().getBytes(), null);
			NavManager.getInstance().updateLocalNaviInfo(jo.toString().getBytes());
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}
	}
}
