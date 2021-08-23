package com.txznet.txz.component.nav.n;

import com.txznet.sdk.bean.Poi;

import java.util.List;

public interface INavHighLevel {
	public static final String NAV_APP_ACTION = "com.txznet.txz.NAVI_ACTION";
	public static final String NAVI_INFO_ACTION = "com.txznet.txz.NAVI_INFO";
	public static final String EXTRA_KEY_NAVI_INFO = "KEY_NAVI_INFO";

	/**
	 * 经由地设置
	 * 
	 * @param pois
	 * @return
	 */
	public boolean procJingYouPoi(Poi... pois);

	/**
	 * 刪除途徑點
	 *
	 * @param poi
	 * @return
	 */
	public boolean deleteJingYou(Poi poi);

	/**
	 * 不支持刪除途徑點
	 *
	 * @return
	 */
	public String disableDeleteJingYou();

	/**
	 * 获取途经点
	 * @return
	 */
	public List<Poi> getJingYouPois();

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