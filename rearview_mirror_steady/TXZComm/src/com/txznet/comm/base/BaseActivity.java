package com.txznet.comm.base;

import java.lang.reflect.Field;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.ScreenUtils;
import com.txznet.comm.version.TXZVersion;
import com.txznet.loader.AppLogicBase;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class BaseActivity extends StackActivity {

    @Override
	protected void attachBaseContext(final Context newBase) {
        /*
        * 通过替换Resource强制指定适配分辨率的框架在某些设备上有偶现失效的情况, 所以在Activity attachContext
        * 时根据设置的适配分辨率重新更新下context的相关配置, 规避失效问题
        * */
		Configuration con = newBase.getResources().getConfiguration();
		con.screenWidthDp = ScreenUtils.getScreenWidthDp();
		con.screenHeightDp = ScreenUtils.getScreenHeightDp();
		newBase.getResources().updateConfiguration(con, newBase.getResources().getDisplayMetrics());
		super.attachBaseContext(newBase);
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		CrashCommonHandler.getInstance().setAgain();
		return super.dispatchTouchEvent(ev);
	}

	/**
	 * 获取焦点时回调
	 */
	public void onGetFocus() {
	}

	/**
	 * 丢失焦点时回调，录音窗口挡住不会触发
	 */
	public void onLoseFocus() {
	}

	private boolean mHasFocus = false;

	/**
	 * 是否有焦点
	 */
	public boolean hasFocus() {
		return mHasFocus;
	}

	@Override
	public void onWindowFocusChanged(boolean newFocus) {
		LogUtil.logd(this.toString() + " onWindowFocusChanged: from "
				+ mHasFocus + " to " + newFocus);
		if (mHasFocus != newFocus) {
			mHasFocus = newFocus;
			if (mHasFocus)
				onGetFocus();
			else
				onLoseFocus();
		}

		super.onWindowFocusChanged(newFocus);
	}

	@Override
	public Resources getResources() {
		Application app = AppLogicBase.getApp();

		if (app != null) {
			try {
				Field f = app.getClass().getDeclaredField("mResources");
				f.setAccessible(true);
				Resources r = (Resources) f.get(null);
				if (r != null)
					return r;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return super.getResources();
	}

	/*
	 * 是否启用测试遮罩
	 */
	public static boolean enableTestMask() {
		if ("REL".equals(TXZVersion.BRANCH)) {
			return false;
		}
		return true;
	}
	
	public boolean enableMaskFromChild(){
		return true;
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (enableTestMask()&&enableMaskFromChild()) {
			printTestMark();
		}
	}

	@Override
	protected void onPause() {
		if (mTestMark != null) {
			getWindowManager().removeView(mTestMark);
			mTestMark = null;
		}
		super.onPause();
	}

	private TextView mTestMark;

	private void printTestMark() {
		try {
			if (mTestMark != null) {
				return;
			}
			mTestMark = new TextView(this);
			PackageInfo info = this.getPackageManager().getPackageInfo(this.getPackageName(), 0);
			String verInfo = "未知版本";
			if ("DEV".equals(TXZVersion.BRANCH)) {
				verInfo = "开发版本";
			}
			else if ("NEW".equals(TXZVersion.BRANCH)) {
			verInfo = "演示版本";
			}
			mTestMark.setText("此版本为" + verInfo + (info == null ? "" : info.versionName));
			mTestMark.setTextSize(16);
			mTestMark.setTextColor(Color.parseColor("#ccffffff"));
			WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();
			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			mTestMark.measure(w, h);
			mLp.width = mTestMark.getMeasuredWidth();
			mLp.height = mTestMark.getMeasuredHeight();
			mLp.flags = 40;
			mLp.format = PixelFormat.RGBA_8888;
			mLp.gravity = Gravity.LEFT | Gravity.TOP;
			mLp.x = 10;
			mLp.y = 10;
			getWindowManager().addView(mTestMark, mLp);
		} catch (Exception e) {
			mTestMark=null;
		}
	}
}
