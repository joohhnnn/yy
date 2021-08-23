package com.txznet.comm.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import android.app.Activity;
import android.app.Dialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;

public class ScreenUtils {

	private static int mScreenWidthDp = 0;
	private static int mScreenHeightDp = 0;
	private static int mScreenAdd = 100000; // 如果直接使用的话会导致其他屏幕适配出错，因此添加十万的增量
	
	private static int mLastWidth;
	private static int mLastHeight;

	/**
	 * 加载屏幕长宽dp分辨率
	 */
	public static void loadScreenConfig() {
		List<String> keys = new ArrayList<String>();
		keys.add("screenWidthDp");
		keys.add("screenHeightDp");
		HashMap<String, String> configs = TXZFileConfigUtil.getConfig(keys);
		if (configs.containsKey("screenWidthDp")) {
			try {
				mScreenWidthDp = Integer.parseInt(configs.get("screenWidthDp"));
				mScreenWidthDp = getAddedValue(mScreenWidthDp);
			} catch (Exception e) {
				LogUtil.loge("ScreenUtils", e);
			}
		}
		if (configs.containsKey("screenHeightDp")) {
			try {
				mScreenHeightDp = Integer.parseInt(configs.get("screenHeightDp"));
				mScreenHeightDp = getAddedValue(mScreenHeightDp);
			} catch (Exception e) {
				LogUtil.loge("ScreenUtils", e);
			}
		}

		// 更新GlobalContext相关配置, 被调端已做字段有效性判断, 此处不做检查
		GlobalContext.updateResourceConfig();
	}
	
	public static int getAddedValue(int value) {
		if (value > mScreenAdd) {
			return value;
		}
		return value + mScreenAdd;
	}

	public static int getOriginalValue(int value) {
		if (value < mScreenAdd) {
			return value;
		}
		return value - mScreenAdd;
	}

	private static boolean sFitScreenChange = false;
	/**
	 * 是否自动适配屏幕分辨率变化
	 * @param ifFit
	 */
	public static void setFitScreenChange(boolean ifFit){
		sFitScreenChange = ifFit;
	}
	
	/**
	 * @param manual
	 *            是否手动修改，强制更新分辨率
	 */
	public static void updateScreenSize(int width, int height, boolean manual) {
		LogUtil.logd("SceenSize change:" + width + "," + height + ",mannual:" + manual + ",sFitScreenChange:"
				+ sFitScreenChange);
		if (!manual && ((mLastWidth == 0 || mLastHeight == 0) || (width == mLastWidth && height == mLastHeight))) {
			mLastWidth = width;
			mLastHeight = height;
			return;
		}
		if (!manual && !sFitScreenChange) {
			return;
		}
		mLastWidth = width;
		mLastHeight = height;
		width = getAddedValue(width);
		height = getAddedValue(height);
		Resources resources = GlobalContext.get().getResources();
		Configuration configuration = resources.getConfiguration();
		configuration.screenWidthDp = width;
		configuration.screenHeightDp = height;
		resources.updateConfiguration(configuration, resources.getDisplayMetrics());
		// 更新GlobalContext相关配置, 被调端已做字段有效性判断, 此处不做检查
		GlobalContext.updateResourceConfig();

		synchronized (ScreenUtils.class) {
			for (ScreenSizeChangeListener listener : mScreenSizeChangeListeners) {
				listener.onScreenSizeChange(getOriginalValue(width), getOriginalValue(height));
			}
		}
	}
	
	
	public static interface ScreenSizeChangeListener {
		void onScreenSizeChange(int width, int height);
	}
	
	private static Set<ScreenSizeChangeListener> mScreenSizeChangeListeners = new HashSet<ScreenSizeChangeListener>();// 服务监听队列
	public static void addSceenSizeChangeListener(ScreenSizeChangeListener listener){
		synchronized (ScreenUtils.class) {
			mScreenSizeChangeListeners.add(listener);
		}
	}

	public static void removeScreenSizeChangeListener(ScreenSizeChangeListener listener) {
		synchronized (ScreenUtils.class) {
			mScreenSizeChangeListeners.remove(listener);
		}
	}
	
	public static int getScreenWidthDp() {
		if (mLastWidth > 0) {
			return getAddedValue(mLastWidth);
		}

		return mScreenWidthDp;
	}

	public static int getScreenHeightDp() {
		if (mLastHeight > 0) {
			return getAddedValue(mLastHeight);
		}

		return mScreenHeightDp;
	}
	
	public static boolean isLargeScreen(Activity activity, int border) {
		Rect outRect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		System.out.println("top:" + outRect.top + " ; left: " + outRect.left);
		if (outRect.width() >= border) {
			return true;
		}
		return false;
	}

	public static boolean isLargeScreen(Dialog dialog, int border) {
		Rect outRect = new Rect();
		dialog.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		System.out.println("top:" + outRect.top + " ; left: " + outRect.left);
		if (outRect.width() >= border) {
			return true;
		}
		return false;
	}

	/**
	 * 是否是竖屏
	 * @return
	 */
	public static boolean isVerticalScreen(Activity activity) {
		Rect outRect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		return outRect.width() < outRect.height();
	}

	public static boolean isVerticalScreen(Dialog dialog) {
		Rect outRect = new Rect();
		dialog.getWindow().getDecorView().getWindowVisibleDisplayFrame(outRect);// 应用界面
		return outRect.width() < outRect.height();
	}

}
