package com.txznet.txz.component.nav.n;

import java.util.List;
import java.util.Set;

public interface INavAsr {

	/**
	 * 设置被禁止的唤醒类型
	 * 
	 * @param cmds
	 */
	public void setBanCmds(String... cmds);

	/**
	 * 获取禁止的类型
	 * 
	 * @return
	 */
	public abstract List<String> getBanCmds();

	/**
	 * 获取所有支持的唤醒类型
	 * 
	 * @return
	 */
	public abstract String[] getSupportCmds();

	/**
	 * 获取支持的命令字类型，要经过禁止和是否导航状态判断
	 * 
	 * @return
	 */
	public Set<String> getCmdTypes();

	/**
	 * 移除禁止的类型
	 * 
	 * @param types
	 */
	public void removeBanCmds(String... types);

	/**
	 * 是否要区分导航和非导航状态来注册类型
	 * 
	 * @return
	 */
	public boolean needSeparateNavStatus();

	/**
	 * 禁止导航任何命令控制
	 * 
	 * @return
	 */
	public boolean banNavWakeup();
}
