package com.txznet.txz.component.nav;

public interface INavHighLevelInterface {
	// 添加状态监听器，会在构造时调用
	public void addStatusListener();

	// 导航开始
	public void onStart();

	// 导航结束，是否到达终点而结束
	public void onEnd(boolean arrive);

	// 导航规划完成
	public void onPlanComplete();

	// 导航规划完成
	public void onPlanError(int errCode, String errDesc);

	// 恢复焦点
	public void onResume();

	// 暂停
	public void onPause();
	
	// 内部指令处理
	public void onNavCommand(boolean fromWakeup, String cmd, String speech);
}
