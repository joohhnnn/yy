package com.txznet.music.utils;

import android.view.View;

import com.txznet.comm.remote.GlobalContext;

public class ViewUtils {

	public static void setViewBgColor(View view,int colorId){
		view.setBackgroundColor(GlobalContext.get().getResources().getColor(colorId));
	}

	public static void setViewBgDrawable(View view, int drawableId){
		view.setBackground(GlobalContext.get().getResources().getDrawable(drawableId));
	}

}
