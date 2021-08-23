package com.txznet.comm.ui.layout;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


/**
 * 录音界面布局基类
 * 
 * @author ASUS User
 *
 */
public abstract class IWinLayout {

	protected LinearLayout mRootLayout;

	/**
	 * 添加View到targetView
	 */
	public abstract Object addView(int targetView, View view, ViewGroup.LayoutParams layoutParams);

	/**
	 * 清空并重置界面上所有的View
	 */
	public abstract void reset();

	/**
	 * 释放WinLayout
	 */
	public abstract void release();
	
	/**
	 * 添加录音View
	 */
	public abstract void addRecordView(View recordView);

	
	public Object addView(int targetView,View view){
		return addView(targetView, view, null);
	}

	public abstract Object removeLastView();

	/**
	 * 得到WinLayout的View
	 */
	public View get() {
		return mRootLayout;
	}


	public Object getTrueLayout() {
		return null;
	}
	
	public void init() {
	}
	
	public static final int CONTENT_MODE_CHAT = 1;
	public static final int CONTENT_MODE_FULL = 2;

	/**
	 * 刷新列表显示的模式
	 * @param mode 显示的模式 <br>
	 * {@link IWinLayout#CONTENT_MODE_CHAT} 显示的是聊天界面<br>
	 * {@link IWinLayout#CONTENT_MODE_FULL} 显示的是全屏操作界面<br>
	 */
	public void updateContentMode(int mode){

	}

	public void setBackground(Drawable drawable){

	}

	public void setBannerAdvertisingView(View view){

	}

	public void removeBannerAdvertisingView(){

	}
}
