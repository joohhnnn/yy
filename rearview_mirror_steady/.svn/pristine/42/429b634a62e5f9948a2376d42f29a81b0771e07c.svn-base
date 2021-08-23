package com.txznet.txz.component.music;

/**
 * 支持打开操作
 * 
 * @author telenewbie
 * @version 创建时间：2016年6月21日 上午11:47:20
 * 
 */
public interface ITxzMedia {

	void openApp();

	void openAndPlay();

	/**
	 * 区分播放音乐和接口回调
	 * 该接口由用户主动调用TXZMusicManager.getInstance().play()导致
	 */
	void play();
	
	
	/**
	 * 讨厌这首歌曲
	 */
	void hateAudio();
	
	/**
	 * 列表循环模式，区别于顺序播放
	 */
	void  switchPlayModeToOnce();
	
	/**
	 * 添加快捷方式
	 */
	void  addSubscribe();

	/**
	 * 当前版本是否支持查询历史
	 */
	boolean supportRequestHistory();

	/**
	 * 查询收听历史
	 */
	void requestHistory(String type);
}
