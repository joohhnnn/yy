package com.txznet.sdk.bean;

import java.io.Serializable;

/**
 * 流量信息 
 */
public class FlowInfo implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -6864281721476975707L;

	//流量套餐类型
	public static final int DATA_PLAN_TYPE_MONTH = 0;     // 月付套餐类型
	public static final int DATA_PLAN_TYPE_NORMAL = 2;    // 普通套餐，比如10G/3个月有效这种
	public static final int DATA_PLAN_TYPE_ACTIVE = 3;    // 激活套餐，赠送的套餐
	public static final int DATA_PLAN_TYPE_INFI_EX_VIDEO = 4;     // 无限流量不含视频
	public static final int DATA_PLAN_TYPE_INFI_INC_VIDEO = 5;    // 无限流量包含视频
	public static final int DATA_PLAN_TYPE_REFUL = 6;             // 加油包

	//错误码
	public final static int EC_FLOW_OK = 7301;	//查询成功
	public final static int EC_FLOW_NOT_PARTNER = 7302;	//不是流量合作商
	public final static int EC_FLOW_NOT_AGENT = 7303;	//不是代理商
	public final static int EC_FLOW_NO_CURRENT_PLAN = 7304;	//当前没有生效套餐
	/**
	 * 套餐名称
	 */
	public String planName;
	/**
	 * 总流量
	 */
	public int totalFlow;
	/**
	 * 已使用流量
	 */
	public int useData;
	/**
	 * 剩余流量
	 */
	public int remainData;
	/**
	 * Sim卡ICCID
	 */
	public String iccid;
	/**
	 * 流量过期时间
	 */
	public long outtime;
	/**
	 * 流量剩余使用天数
	 */
	public int remainDay;
	/**
	 * 流量套餐的类型
	 */
	public int planType;
}
