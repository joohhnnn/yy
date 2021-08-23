package com.txznet.comm.config;

import com.txznet.comm.remote.ServiceManager;

/**
 * 通用配置项configer基类
 * 
 * @author Terry
 */
public abstract class BaseConfiger {

	public static final String INVOKE_COMM_PREFIX = "comm.configer.";
	public static final String INVOKE_NAV_CONTROL = "navControl.";

	abstract void init();

	abstract byte[] onEvent(String packageName, String command, byte[] data);

	public void sendCommInvoke(String cmd, byte[] data) {
		ServiceManager.getInstance().sendInvoke(ServiceManager.MUSIC, cmd, data, null);
		ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, cmd, data, null);
//		ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, cmd, data, null);
	}
}
