package com.txznet.music.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager.BackStackEntry;
import android.content.res.Configuration;
import android.app.FragmentTransaction;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.utils.SharedPreferencesUtils;

/**
 * @author telenewbie
 * @version 创建时间：2016年4月18日 下午5:11:15
 * 
 */
public class BaseActivity extends com.txznet.comm.base.BaseActivity {

	public static final String TAG = "[MUSIC][ACTIVITY] ";
	private TextView mTestMark;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (SharedPreferencesUtils.getFullScreen()) {
			setFullScreen(this);
		}
		int skin = com.txznet.fm.bean.Configuration.getInstance().getInteger(
				com.txznet.fm.bean.Configuration.TXZ_SKIN);
		if (skin == 2) {
			findViewById(android.R.id.content).setBackgroundResource(
					R.drawable.fm_bg_1);
		} else if (skin == 3) {
			findViewById(android.R.id.content).setBackgroundResource(
					R.drawable.fm_bg);
		} else {
			findViewById(android.R.id.content).setBackgroundResource(
					R.drawable.fm_bg_2);
		}
	}

	
	public void setFullScreen(Activity activity){
		//由于全屏会遮挡部分设备的虚拟按键，导致无法返回，但是已经发布过版本，所以仍保留这个接口但是不执行全屏的操作
		activity.getWindow().getDecorView().setSystemUiVisibility(
						View.SYSTEM_UI_FLAG_FULLSCREEN
						);
	}
	
	@Override
	public void onWindowFocusChanged(boolean newFocus) {
		if (newFocus) {
			if (SharedPreferencesUtils.getFullScreen()) {
				setFullScreen(this);
			}
		}
		super.onWindowFocusChanged(newFocus);
	}

	@Override
	protected void onResume() {
		super.onResume();
		Constant.setIsExit(false);
		if (Constant.ISTESTDATA) {
			printTestMark();
		}
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		LogUtil.logd(TAG + "configchaged");
		super.onConfigurationChanged(newConfig);
	}

	@Override
	protected void onPause() {
		if (mTestMark != null) {
			getWindowManager().removeView(mTestMark);
			mTestMark = null;
		}
		super.onPause();
	}

	private void printTestMark() {
		try {
			if (mTestMark != null) {
				return;
			}
			mTestMark = new TextView(this);
			mTestMark.setText("此版本为内部测试版");
			mTestMark.setTextSize(16);
			mTestMark.setTextColor(Color.parseColor("#ccffffff"));
			WindowManager.LayoutParams mLp = new WindowManager.LayoutParams();
			int w = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0,
					View.MeasureSpec.UNSPECIFIED);
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
		}
	}

	public void jumpTypeFragment(int replaceId, Fragment fragment, String tag) {
		Fragment findFragmentByTag = getFragmentManager()
				.findFragmentByTag(tag);
		if (getFragmentManager().getBackStackEntryCount() >= 1) {
			BackStackEntry backStackEntryAt = getFragmentManager()
					.getBackStackEntryAt(
							getFragmentManager().getBackStackEntryCount() - 1);
			String name = backStackEntryAt.getName();
			if (tag != null && tag.equals(name)) {
				return;
			}
		}
		if (findFragmentByTag == null) {
			findFragmentByTag = fragment;
		}
		FragmentTransaction beginTransaction = getFragmentManager()
				.beginTransaction();
		beginTransaction.replace(replaceId, findFragmentByTag, tag);
		beginTransaction.commitAllowingStateLoss();
	}
	
	
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
}
