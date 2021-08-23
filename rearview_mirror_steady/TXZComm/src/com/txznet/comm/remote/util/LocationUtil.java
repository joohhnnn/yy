package com.txznet.comm.remote.util;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.ui.map.UiMap.LocationInfo;
import com.txz.ui.map.UiMap.NavigateInfo;
import com.txz.ui.map.UiMap.NavigateInfoList;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.ServiceManager.GetDataCallback;
import com.txznet.comm.remote.ServiceManager.ServiceData;

public class LocationUtil {
	/*
	 * location获取工具，成员填为null不获取
	 */
	public static abstract class GetLocationCallback {
		public NavigateInfo home;
		public NavigateInfo company;
		public NavigateInfoList historylist;
		public LocationInfo myLocation;

		public abstract void onGet();
	}

	public static void setHome(NavigateInfo home) {
		if (home == null)
			return;
		byte[] bs = NavigateInfo.toByteArray(home);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.location.sethome", bs, null);
	}

    public static void getHome(final GetLocationCallback cb) {
        if (cb == null)
            return;
		GetDataCallback res = new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data != null && data.getBytes() != null) {
					try {
						cb.home = NavigateInfo.parseFrom(data.getBytes());
					} catch (InvalidProtocolBufferNanoException e) {
						LogUtil.loge("parse home NavigateInfo fail!");
					}
				}
				cb.onGet();
			}
		};
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.location.gethome", null, res);
	}

	public static void setCompany(NavigateInfo company) {
		if (company == null)
			return;
		byte[] bs = NavigateInfo.toByteArray(company);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.location.setcompany", bs, null);
	}

    public static void getCompany(final GetLocationCallback cb) {
        if (cb == null)
            return;
		GetDataCallback res = new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data != null && data.getBytes() != null) {
					try {
						cb.company = NavigateInfo.parseFrom(data.getBytes());
					} catch (InvalidProtocolBufferNanoException e) {
						LogUtil.loge("parse company NavigateInfo fail!");
					}
				}
				cb.onGet();
			}
		};
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.location.getcompany", null, res);
	}

	public static void setHisory(NavigateInfo history) {
		if (history == null)
			return;
		byte[] bs = NavigateInfo.toByteArray(history);
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.location.sethistory", bs, null);
	}

    public static void getHistoryList(final GetLocationCallback cb) {
        if (cb == null)
            return;
		GetDataCallback res = new GetDataCallback() {
			@Override
			public void onGetInvokeResponse(ServiceData data) {
				if (data != null && data.getBytes() != null) {
					try {
						cb.historylist = NavigateInfoList.parseFrom(data.getBytes());
					} catch (InvalidProtocolBufferNanoException e) {
						LogUtil.loge("parse historylist NavigateInfoList fail!");
					}
				}
				cb.onGet();
			}
		};
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.location.gethistorylist", null, res);
	}

	private static final double PI = 3.14159265358979324;
	private static double X_PI = PI * 3000.0 / 180.0;

	public static double[] Convert_BD09_To_GCJ02(double lat, double lng) {
		double x = lng - 0.0065, y = lat - 0.006;
		double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * X_PI);
		double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * X_PI);
		lng = z * Math.cos(theta);
		lat = z * Math.sin(theta);
		double[] point = new double[2];
		point[0] = lat;
		point[1] = lng;
		return point;
	}
}
