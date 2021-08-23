package com.txznet.txz.component.nav.n;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.sdk.bean.Poi;

public interface INavHighLevel {
	public static final String NAV_APP_ACTION = "com.txznet.txz.NAVI_ACTION";
	public static final String NAVI_INFO_ACTION = "com.txznet.txz.NAVI_INFO";
	public static final String EXTRA_KEY_NAVI_INFO = "KEY_NAVI_INFO";

	/**
	 * 更换导航包名
	 */
	public void setPackageName(String packageName);

	/**
	 * 经由地设置
	 * 
	 * @param pois
	 * @return
	 */
	public boolean procJingYouPoi(Poi... pois);

	/**
	 * 不支持设置经由地原因
	 */
	public String disableProcJingYouPoi();

	/**
	 * 广播导航信息
	 * 
	 * @param navJson
	 */
	public void broadNaviInfo(String navJson);

	/**
	 * 
	 */
	public byte[] invokeTXZNav(String packageName, String command, byte[] data);
}