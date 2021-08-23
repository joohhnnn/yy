package com.txznet.txz.component.music;

import com.txznet.sdk.TXZMusicManager.MusicModel;

public interface IMusic {

	/**
	 * 获取包名
	 * 
	 * @return
	 */
	public String getPackageName();

	/**
	 * 音乐工具状态变化监听器
	 * 
	 * @author txz
	 *
	 */
	public static interface MusicToolStatusListener {
		/**
		 * 音乐状态发生改变，如开始播放、暂停播放(非tts等引发的临时暂停)、曲目变化
		 */
		public void onStatusChange();
	}

	/**
	 * 播放状态获取接口
	 * 
	 * @return 是否正在播放
	 */
	public boolean isPlaying();

	/**
	 * 缓冲状态获取接口
	 * 
	 * @return 是否正在处在缓冲中
	 */
	public boolean isBuffering();

	/**
	 * 开始播放
	 * 
	 */
	public void start();
	
	/**
	 * 停止播放
	 */
	public void pause();

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
	 * 全部循环模式
	 */
	public void switchModeLoopAll();

	/**
	 * 单曲循环模式
	 */
	public void switchModeLoopOne();

	/**
	 * 随机播放模式
	 */
	public void switchModeRandom();

	/**
	 * 切歌
	 */
	public void switchSong();

	/**
	 * 随便听听
	 */
	public void playRandom();

	/**
	 * 播放指定音乐模型
	 * 
	 * @param musicModel
	 */
	public void playMusic(MusicModel musicModel);

	/**
	 * 获取当前正在播放的音乐模型，没有播放返回null
	 */
	public MusicModel getCurrentMusicModel();

	/**
	 * 收藏当前播放的歌曲
	 */
	public void favourMusic();

	/**
	 * 取消收藏当前播放的歌曲
	 */
	public void unfavourMusic();

	/**
	 * 播放收藏歌曲
	 */
	public void playFavourMusic();

	/**
	 * 设置状态监听器
	 */
	public void setStatusListener(MusicToolStatusListener listener);
	
	/**
	 * 取消搜索
	 */
	public void cancelRequest();
}
