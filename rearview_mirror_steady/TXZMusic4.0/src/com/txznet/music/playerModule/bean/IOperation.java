package com.txznet.music.playerModule.bean;

import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.EnumState.AudioType;

/**
 * 针对播放界面的操作 上下拉etc
 */
public interface IOperation {
	/**
	 * 拉取更多数据
	 *
	 * @param isDown 是否向下拉取更多
	 */
	public void searchListData(EnumState.Operation operation, boolean isDown);

	//播放类型音频：播放电台/音频/直播
	public void play(AudioType type);

}
