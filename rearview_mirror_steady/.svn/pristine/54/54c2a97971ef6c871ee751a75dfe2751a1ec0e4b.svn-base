package com.txznet.record.ui;

import android.graphics.drawable.Drawable;
import android.view.View;

import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.record.bean.ChatMessage;

public interface IWinRecord {

	boolean isShowing();

	void setIsFullSreenDialog(boolean isFullScreen);

	void realInit();

	void updateDialogType(int type);

	void addMsg(ChatMessage chatMsg);

	void notifyUpdateVolume(int volume);

	void notifyUpdateLayout(int status);

	void notifyUpdateProgress(int val, int selection);

	void enableAnim(boolean enable);

	void show();

	void dismiss();

	void setWinRecordObserver(WinRecordCycleObserver observer);
	
	void setWinFlags(int flags);
	
	void setWinBgAlpha(Float winBgAlpha);
	
	void setWinType(int type);
	
	void setSystemUiVisibility(int type);
	
	void newWinInstance();
	
	void setDialogCancel(boolean flag);

	void setDialogCanceledOnTouchOutside(boolean cancel);
	
	void setAllowOutSideClickSentToBehind(boolean allow);
	/**
	 * 
	 * 设置主窗口与软键盘的交互模式，可以用来避免输入法面板遮挡问题
	 * @param soft
	 */
	void setWinSoft(int soft);
	
	
	/**
	 * 设置Content的宽度
	 * @param width
	 */
	void setContentWidth(int width);
	
	/**
	 * 是否将背景图片设置为窗口背景图片
	 * @param ifSet
	 */
	void setIfSetWinBg(boolean ifSet);

	/**
	 * 更新显示区域位置及大小
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void updateDisplayArea(int x,int y,int width,int height);

	/**
	 *  更新广告
	 * @param view 广告banner view
	 */
	void setBannerAdvertisingView(View view);

	/**
	 * 移除广告
	 */
	void removeBannerAdvertisingView();

	/**
	 * 更新win背景
	 * @param drawable
	 */
	void setBackground(Drawable drawable);
}
