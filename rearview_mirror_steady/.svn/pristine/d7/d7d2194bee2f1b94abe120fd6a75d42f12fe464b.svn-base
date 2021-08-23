package com.txznet.music.utils;

import android.content.Context;
import android.util.DisplayMetrics;

public class DimenUtils {

	public static int dip2Pixel(Context ctx, int size) {
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		if (dm == null) {
			return size;
		}
		return Math.round(dm.density * size);
	}

	public static float dip2Pixel(Context ctx, float size) {
		DisplayMetrics dm = ctx.getResources().getDisplayMetrics();
		if (dm == null) {
			return size;
		}
		return dm.density * size;
	}
	
	
}
