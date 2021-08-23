package com.txznet.comm.util;


import android.util.TypedValue;
import android.widget.TextView;

import com.txznet.comm.ui.config.ViewConfiger;

/**
 *设置textView字体和颜色的封装
 */
public class TextViewUtil {
	public static void setTextSize(TextView tv,float size) {
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, size);
	}
	
	public static void setTextSize(TextView tv,String key) {
		setTextSize(tv, (Float) ViewConfiger.getInstance().getConfig(key));
	}
	
	public static void setTextColor(TextView tv,int color) {
		tv.setTextColor(color);
	}
	
	public static void setTextColor(TextView tv,String key) {
		setTextColor(tv, (Integer) ViewConfiger.getInstance().getConfig(key));
	}
}
