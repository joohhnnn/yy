package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ViewData;

public abstract class IRecordView extends ViewBase {
	
	public static final int STATE_NORMAL = 0; // 正常状态，显示一个录音图标
	public static final int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
	public static final int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画

	public static final int STATE_WIN_OPEN = 3;//窗口打开
	public static final int STATE_WIN_CLOSE = 4;//窗口关闭
	public static final int STATE_SPEAK_START = 5;//TTS播报
	public static final int STATE_SPEAK_END = 6;//TTS播报结束

	/**
	 * 更新录音状态
	 */
	public abstract void updateState(int state);

	public abstract void updateVolume(int volume);
	
}
