package com.txznet.comm.ui.recordwin;

import android.graphics.drawable.Drawable;
import com.txznet.comm.ui.WinRecordObserver.WinRecordCycleObserver;
import com.txznet.comm.ui.layout.IWinLayout;

public interface IRecordWin2 {

	void init();
	
	boolean isShowing();
	
	void setIsFullSreenDialog(boolean isFullScreen);
	
	void setWinBgAlpha(Float winBgAlpha);
	
	void updateWinLayout(IWinLayout winLayout);
	
	void show();
	
	void dismiss();
	
	void setWinRecordObserver(WinRecordCycleObserver observer);
	
	void setWinType(int type);
	
	void setSystemUiVisibility(int type);
	
	void setWinFlags(int flags);
	
	void newInstance();
	
	/**
	 * 设置Content显示宽度，主要为了适配异形屏
	 */
	void setContentWidth(int width);
	
	void setIfSetWinBg(boolean ifSet);
	
	/**
	 * 
	 * 设置主窗口与软键盘的交互模式，可以用来避免输入法面板遮挡问题
	 * @param soft
	 */
	void setWinSoft(int soft);
	/**
	 * 更新显示区域位置及大小
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	void updateDisplayArea(int x,int y,int width,int height);
	
	void setDialogCancel(boolean flag);

	void setDialogCanceledOnTouchOutside(boolean cancel);

	void setAllowOutSideClickSentToBehind(boolean allow);

	/**
	 * 更新win背景
	 * @param drawable
	 */
	void updateBackground(Drawable drawable);
}
