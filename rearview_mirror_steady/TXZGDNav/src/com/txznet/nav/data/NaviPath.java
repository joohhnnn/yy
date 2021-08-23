package com.txznet.nav.data;

import java.lang.reflect.Method;
import java.util.List;

import com.amap.api.maps.model.LatLngBounds;
import com.amap.api.navi.model.AMapNaviPath;
import com.amap.api.navi.model.AMapNaviStep;
import com.amap.api.navi.model.NaviLatLng;
import com.txznet.comm.remote.util.LogUtil;

public class NaviPath extends AMapNaviPath {
	private String uid;
	private boolean isReplanRoute;

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return this.uid;
	}

	public void setReplanRoute(boolean isReplan) {
		this.isReplanRoute = isReplan;
	}

	public boolean isReplan() {
		return isReplanRoute;
	}

	public List<NaviLatLng> getWayPoint() {
		return super.getWayPoint();
	}

	public void setNaviWayPoint(List<NaviLatLng> var1) {
		execField("setWayPoint", List.class, var1);
	}

	public NaviLatLng getStartPoint() {
		return super.getStartPoint();
	}

	public void setNaviStartPoint(NaviLatLng var1) {
		execField("setStartPoint", NaviLatLng.class, var1);
	}

	public NaviLatLng getEndPoint() {
		return super.getEndPoint();
	}

	public void setNaviEndPoint(NaviLatLng var1) {
		execField("setEndPoint", NaviLatLng.class, var1);
	}

	public NaviLatLng getCenterForPath() {
		return super.getCenterForPath();
	}

	public void setNaviCenter(NaviLatLng var1) {
		execField("setCenter", NaviLatLng.class, var1);
	}

	public LatLngBounds getBoundsForPath() {
		return super.getBoundsForPath();
	}

	public void setNaviBounds(LatLngBounds var1) {
		execField("setBounds", LatLngBounds.class, var1);
	}

	public List<AMapNaviStep> getSteps() {
		return super.getSteps();
	}

	public void setNaviListStep(List<AMapNaviStep> var1) {
		execField("setListStep", List.class, var1);
	}

	public List<NaviLatLng> getCoordList() {
		return super.getCoordList();
	}

	public void setNaviList(List<NaviLatLng> var1) {
		execField("setList", List.class, var1);
	}

	public AMapNaviStep getStep(int var1) {
		return null;
	}

	public void setNaviAllLength(int var1) {
		execField("setAllLength", Integer.class, var1);
	}

	public void setNaviStrategy(int var1) {
		execField("setStrategy", Integer.class, var1);
	}

	public void setNaviAllTime(int var1) {
		execField("setAllTime", Integer.class, var1);
	}

	public void setNaviStepsCount(int var1) {
		execField("setStepsCount", Integer.class, var1);
	}

	public void setNaviTollCost(int var1) {
		execField("setTollCost", Integer.class, var1);
	}

	public int getAllLength() {
		return super.getAllLength();
	}

	public int getStrategy() {
		return super.getStrategy();
	}

	public int getAllTime() {
		return super.getAllTime();
	}

	public int getStepsCount() {
		return super.getStepsCount();
	}

	public int getTollCost() {
		return super.getTollCost();
	}

	private Object execField(String name, Class parameterTypes, Object params) {
		try {
			Method method = AMapNaviPath.class.getDeclaredMethod(name,
					parameterTypes);
			if (method != null) {
				method.setAccessible(true);
				return method.invoke(this, params);
			}
		} catch (Exception e) {
			LogUtil.loge(e.toString());
		}

		return null;
	}
}
