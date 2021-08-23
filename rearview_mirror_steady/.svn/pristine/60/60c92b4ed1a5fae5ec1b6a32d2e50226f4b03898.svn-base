package com.txznet.comm.ui.viewfactory.view;

import com.txznet.comm.ui.viewfactory.ViewBase;

public abstract class IFloatView extends ViewBase {

	public static final int STATE_NORMAL = 0; // 正常状态，显示一个录音图标 //TODO 需要判断声控关闭和开启的情况
	public static final int STATE_RECORD_START = 1; // 录音开始，显示一个声纹动画
	public static final int STATE_RECORD_END = 2; // 录音结束，显示一个处理中动画
	public static final int STATE_NORMAL_JOKE = 3; // 正常播报状态，笑话场景
	public static final int STATE_NORMAL_ERROR = 4; // 正常播报状态，错误场景

	/**
	 * 更新录音状态
	 */
	public abstract void updateState(int state);

	public abstract void updateVolume(int volume);
	public abstract void setImageBitmap(final String normal, final String pressed);
}
