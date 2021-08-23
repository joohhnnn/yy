package com.txznet.txz.component.audio;

public interface IAudio {

	/**
	 * 获取包名
	 * 
	 * @return
	 */
	public String getPackageName();

	/**
	 * 继续播放
	 * 
	 */
	public void start();

	/**
	 * 停止播放
	 */
	public void pause();

	/**
	 * 根据 返回json的数据
	 */
	public void playFm(String jsonData);

	/**
	 * 退出播放器
	 */
	public void exit();

	/**
	 * 下一首
	 */
	public void next();

	/**
	 * 上一首
	 */
	public void prev();

	/**
	 * 获取当前播放的名称
	 * 
	 * @return
	 */
	public String getCurrentFmName();
	
	public void cancelRequest();
}
