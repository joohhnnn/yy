package com.txznet.music.widget;

import android.content.Context;
import android.graphics.Point;
import android.util.DisplayMetrics;
import android.view.WindowManager;

public class ScreenUtils {
	private static MechainInfo info = new MechainInfo();

	public static MechainInfo getPoint(Context context) {
		WindowManager wmManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Point outSize = new Point();
		wmManager.getDefaultDisplay().getSize(outSize);
		DisplayMetrics outMetrics = new DisplayMetrics();

		wmManager.getDefaultDisplay().getMetrics(outMetrics);
		info.density = outMetrics.density;
		info.height = outSize.y;
		info.width = outSize.x;
		return info;
	}

	public static class MechainInfo {
		public int width;
		public int height;
		public float density;
	}
}
