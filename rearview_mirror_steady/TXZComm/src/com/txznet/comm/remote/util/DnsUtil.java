package com.txznet.comm.remote.util;

import com.txznet.comm.remote.ServiceManager;

public class DnsUtil {
	/*
	 * strJsonData数据格式如下:
	 * {"domains":["www.baidu.com", "www.google.com"]}
	 */
	public static void parse(String strJsonData){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.dns.parse", strJsonData.getBytes(), null);
	}
	
	/*
	 * strJsonData数据格式如下:
	 * {"domains":[  {"domain":"www.baidu.com", "ip": ["111", "222"]}, 
	 *                     {"domain":"www.baidu.com", "ip": ["333", "444"]}
	 *                 ]
	 * }
	 */
	public static void report(String strJsonData){
		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "comm.dns.report", strJsonData.getBytes(), null);
	}

}
