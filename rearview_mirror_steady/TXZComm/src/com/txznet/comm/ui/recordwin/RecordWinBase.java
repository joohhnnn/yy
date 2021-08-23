package com.txznet.comm.ui.recordwin;

import com.txznet.comm.ui.layout.IWinLayout;

import android.content.Context;
import android.view.View;

/**
 * 2.0版本RecordWin基类
 * 
 * @author TerryYang
 *
 */
public abstract class RecordWinBase extends Win2Dialog {

	public RecordWinBase(){
		super();
	}
	
	
	public RecordWinBase(boolean isSystem, boolean isFullScreen){
		super(isSystem,isFullScreen);
	}
	
	public RecordWinBase(boolean isSystem, boolean isFullScreen,IWinLayout winLayout){
		super(isSystem, isFullScreen, winLayout);
	}
	
}
