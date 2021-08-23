package com.txznet.sdk.bean;

import java.util.Date;

public class TrafficControlData {
	/**
	 * 城市
	 */
	public String city;
	/**
	 * 本地限行情况
	 */
	public String local;
	/**
	 * 非本地限行情况
	 */
	public String nonlocal;
	/**
	 * 限行详情（为空则不支持该城市查询
	 */
	public TrafficControlInfo[] trafficControlInfos;

	/**
	 * 限行信息
	 */
	public static class TrafficControlInfo {
		/**
		 * 限行尾号
		 */
		public String[] forbiddenTailNumber;
		/**
		 * 星期几
		 */
		public int week;
		/**
		 * 限定日期
		 */
		public Date forbiddenDate;
	}
}
