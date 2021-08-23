package com.txznet.comm.ui.theme.test.config;

import android.content.Context;
import android.content.SharedPreferences;

import com.txznet.comm.remote.GlobalContext;

public class FloatPointSP {

	private SharedPreferences mSharePre;

	/* 悬浮图标位置 */
	private static final String SP_NAME = "float_view_point_cache";
	private static FloatPointSP sInstance;

	protected FloatPointSP() {
		mSharePre = GlobalContext.get().getSharedPreferences(SP_NAME,
				Context.MODE_PRIVATE);
	}

	public static FloatPointSP getInstance() {
		if (sInstance == null) {
			synchronized (FloatPointSP.class) {
				if (sInstance == null) {
					sInstance = new FloatPointSP();
				}
			}
		}
		return sInstance;
	}

	public int getX() {
		return mSharePre.getInt("x", 0);
	}

	public int getY() {
		return mSharePre.getInt("y", 0);
	}
}
