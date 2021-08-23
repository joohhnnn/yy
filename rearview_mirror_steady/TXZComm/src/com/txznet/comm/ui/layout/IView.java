package com.txznet.comm.ui.layout;

import android.view.View;

public  abstract class IView {

	public static final int ID_CHAT_CONTENT = 1;
	public static final int ID_CHAT_CONTENT_LIST = 11;
	
	public static final int ID_FULL_CONTENT = 2;
	
	/**
	 * 得到当前的VIew
	 */
	public abstract View get();
	
	public abstract int getTXZViewId();
}
