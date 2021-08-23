package com.txznet.fm.bean;

/**
 * 版本控制用于区别不同的事项
 */
public class VersionInfo {

	public final static int ONE = 1;
	public final static int TWO = 2;
	public final static int THREE = 3;

	/**
	 * 从配置文件中读取第几个选项
	 * 
	 * @return
	 */
	public static int getVersion() {
		return Configuration.getInstance()
				.getInteger(Configuration.TXZ_VERSION);
	}
}
