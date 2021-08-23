package com.txznet.comm.ui.util;

import java.util.concurrent.atomic.AtomicInteger;

import com.txznet.comm.ui.config.ViewConfiger;

import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class ViewUtils {
	/**
     * An {@code int} value that may be updated atomically.
     */
    private static final AtomicInteger sNextGeneratedId = new AtomicInteger(1);

    /**
     * 动态生成View ID
     * API LEVEL 17 以上View.generateViewId()生成
     * API LEVEL 17 以下需要手动生成
     */
    public static int generateViewId() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            for (; ; ) {
                final int result = sNextGeneratedId.get();
                // aapt-generated IDs have the high byte nonzero; clamp to the range under that.
                int newValue = result + 1;
                if (newValue > 0x00FFFFFF) newValue = 1; // Roll over to 1, not 0.
                if (sNextGeneratedId.compareAndSet(result, newValue)) {
                    return result;
                }
            }
        } else {
            return View.generateViewId();
        }
    }

    
    //设置字体大小和颜色的封装
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
