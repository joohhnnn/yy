package com.txznet.music.view;

import java.util.List;

import com.txznet.audio.bean.LocalBuffer;
import com.txznet.music.bean.response.Audio;

public interface IMediaPlayer {

	/**
	 * 展示停止图标
	 */
	public void showPause();

	/**
	 * 展示播放图标
	 */
	public void showPlay();

	/**
	 * 当切换歌曲，或者又进行任何的修改的时候，进行更新界面的操作
	 * 
	 * @param info
	 * @param isLoading 是否加载
	 */
	public void notifyMusicInfo(Audio info,boolean isLoading);

	/**
	 * 更新播放列表数据
	 * 
	 * @param infos
	 */
	public void notifyMusicListInfo(List<Audio> infos,boolean isAdd);

	/**
	 * 设置播放的模式；顺序，随机，循环
	 * 
	 * @param mode
	 */
	public void showSequenceMode();

	/**
	 * 设置播放的模式；顺序，随机，循环
	 * 
	 * @param mode
	 */
	public void showRandomMode();

	/**
	 * 设置播放的模式；顺序，随机，循环
	 * 
	 * @param mode
	 */
	public void showSingleCircleMode();

	/**
	 * 刷新进度条
	 * 
	 * @param value
	 *            当前进度相对于总进度的比例
	 * @param currentTime
	 *            当前进度的时间值
	 */
	public void setFinishedProgress(float value, long currentTime, long endTime);

	/**
	 * 刷新缓冲进度条
	 * 
	 * @param value
	 */
	public void setBufferProgress(final List<LocalBuffer> value);

	/**
	 * 定位位置
	 * 
	 * @param location
	 */
	public void notifyAndLocation(int location, boolean showCurrent);

	public void showBufferButton();

	public void closeBufferButton();

	/**
	 * 加载中的视图
	 */
	public void showLoadingView();

	/**
	 * 隐藏循环模式
	 * 
	 * @param b
	 */
	public void hiddenMode(boolean b);

	/**
	 * 关闭改界面
	 */
	public void dismiss();

	/**
	 * 播放列表刷新不出数据
	 * 
	 * @param ttsText
	 */
	public void notifyMusicListInfo(String ttsText);

	/**
	 * 加载超时
	 */
	public void showTimeOutView();

	/**
	 * 展示当前音量
	 */
	public void showSoundValueView();

	/**
	 * 设置Seekbar的状态
	 * 
	 * @param value
	 */
	public void setSeekBarEnable(boolean value);


	
	/**
	 * 显示或隐藏弹出框
	 * @param show
	 */
	public void  showPop(boolean show);
}
