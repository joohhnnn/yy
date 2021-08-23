package com.txznet.txz.component.nav;

import com.txz.ui.map.UiMap.NavigateInfo;
import com.txznet.sdk.TXZNavManager.NavStatusListener;

public interface INav {

	public static enum NavPlanType {
		/**
		 * 推荐线路
		 */
		NAV_PLAN_TYPE_RECOMMEND,
		/**
		 * 最短时间
		 */
		NAV_PLAN_TYPE_LEAST_TIME,
		/**
		 * 最短距离
		 */
		NAV_PLAN_TYPE_LEAST_DISTANCE,
		/**
		 * 最少收费
		 */
		NAV_PLAN_TYPE_LEAST_COST,
		/**
		 * 躲避拥堵
		 */
		NAV_PLAN_TYPE_AVOID_JAMS,
	}

	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	public int initialize(final IInitCallback oRun);

	public void release();

	/**
	 * 路径规划到目的地，返回是否支持
	 * 
	 * @param info
	 * @return
	 */
	public boolean NavigateTo(NavPlanType plan, NavigateInfo info);
	
	/**
	 * 是否正在导航中
	 */
	public boolean isInNav();

	/**
	 * 进入导航
	 */
	public void enterNav();

	/**
	 * 退出导航
	 */
	public void exitNav();
	
	/**
	 * 设置导航状态器
	 * @param listener
	 */
	public void setNavStatusListener(NavStatusListener listener);
}
