package com.txznet.comm.ui.keyevent;

import com.txznet.comm.ui.viewfactory.ViewBase;

import android.view.KeyEvent;

public abstract class KeyEventDispatcherBase {

	public static final int KEYCODE_LEFT = KeyEvent.KEYCODE_DPAD_LEFT;
	public static final int KEYCODE_RIGHT = KeyEvent.KEYCODE_DPAD_RIGHT;
	public static final int KEYCODE_UP = KeyEvent.KEYCODE_DPAD_UP;
	public static final int KEYCODE_DOWN = KeyEvent.KEYCODE_DPAD_DOWN;
	public static final int KEYCODE_OK = KeyEvent.KEYCODE_DPAD_CENTER;
	public static final int KEYCODE_BACK = KeyEvent.KEYCODE_BACK;
	public static final int KEYCODE_HOME = KeyEvent.KEYCODE_HOME;
	public static final int KEYCODE_VOICE = 1001; // android默认KeyCode到224
	
	
	public static final int MODE_NORMAL = 1; // 普通模式
	public static final int MODE_LIST = 2; // 列表模式，例如POI列表等。
	
	/**
	 * 接收到按键时，NOTE:如果自己处理的话返回true，不处理时返回false
	 */
	public abstract boolean onKeyEvent(int keyEvent);
	
	/**
	 * 内容信息发生变化
	 * @param viewBase
	 * 			新插入的内容
	 */
	public abstract void onChatViewChange(ViewBase viewBase);

	/**
	 * 返回当前显示的模式
	 * @return
	 */
	public abstract int getCurMode();
	
	/**
	 * 进度条发生变化
	 * @param selection
	 * @param value
	 */
	public void onUpdateProgress(int selection,int value){
	}
}
