package com.txznet.txz.component.nav;

public interface IMapInterface {

	public void initialize();
	
	public void enterNav();

	public void setPackageName(String pkn);

	public void zoomAll(Runnable run);

	public void appExit();

	public void naviExit();

	public void zoomMap(boolean isZoomin);

	public void switchLightNightMode(boolean isLight);

	public void switchTraffic(boolean isShowTraffic);

	public void switch23D(boolean is2D, int val);

	public void switchCarDirection();

	public void switchNorthDirection();

	public void switchPlanStyle(PlanStyle ps);

	public void backNavi();

	public void switchBroadcastRole(int role);

	public void navigateTo(String name, double lat, double lng, int planStyle);

	void frontTraffic();

	void switchSimpleMode(boolean isOpen);

	void queryCollectionPoint();

	void intoTeam();

	public static enum PlanStyle {
		DUOBISHOUFEI, DUOBIYONGDU, BUZOUGAOSU, GAOSUYOUXIAN,
		JIAYOUZHAN,ATM,CESUO,WEIXIUZHAN,TUIJIAN,MAINROAD,SIDEROAD,REFRESHPATH
	}
}
