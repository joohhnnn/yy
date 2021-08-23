package com.txznet.txz.ui.widget;

import com.txznet.comm.base.BaseActivity;
import com.txznet.comm.version.TXZVersion;
import com.txznet.loader.AppLogic;
import com.txznet.txz.ui.win.record.RecorderWin;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.TextView;

public class MarkFloatView extends TextView {
	private WindowManager mWindowManager;
	private WindowManager.LayoutParams mLp;

	private boolean mIsOpening;
	private static MarkFloatView sFloatView = null;

	private MarkFloatView(Context context) {
		super(context);

		mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {

			@Override
			public void onShow() {
				AppLogic.removeUiGroundCallback(showRun);
				AppLogic.runOnUiGround(showRun, 10);
			}

			@Override
			public void onDismiss() {
				AppLogic.removeUiGroundCallback(dismissRun);
				AppLogic.runOnUiGround(dismissRun, 10);
			}
		});
	}
	
	Runnable showRun = new Runnable() {

		@Override
		public void run() {
			show();
		}
	};

	Runnable dismissRun = new Runnable() {

		@Override
		public void run() {
			dismiss();
		}
	};

	public static MarkFloatView getInstance(Context context) {
		if (sFloatView == null) {
			synchronized (MarkFloatView.class) {
				if (sFloatView == null) {
					sFloatView = new MarkFloatView(context);
				}
			}
		}
		return sFloatView;
	}
	
	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
			
			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				if (mIsOpening) {
					mWindowManager.updateViewLayout(MarkFloatView.this, mLp);
				}
				return false;
			}
		});
	}
	
	public void show() {
		if (mIsOpening || !BaseActivity.enableTestMask()) {
			return;
		}

		try {
			PackageInfo info = getContext().getPackageManager().getPackageInfo(getContext().getPackageName(), 0);
			String verInfo = "未知版本";
			if ("DEV".equals(TXZVersion.BRANCH)) {
				verInfo = "开发版本";
			} else if ("NEW".equals(TXZVersion.BRANCH)) {
				verInfo = "演示版本";
			}
			this.setText("此版本为" + verInfo + (info == null ? "" : info.versionName));
			this.setTextSize(16);
			this.setTextColor(Color.parseColor("#ccffffff"));
			if (mLp == null) {
				mLp = new WindowManager.LayoutParams();
			}

			int w = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			int h = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
			this.measure(w, h);
			mLp.width = this.getMeasuredWidth();
			mLp.height = this.getMeasuredHeight();
			mLp.flags = 40;
			mLp.format = PixelFormat.RGBA_8888;
			mLp.gravity = Gravity.LEFT | Gravity.TOP;
			mLp.x = 10;
			mLp.y = 10;
			mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
			mWindowManager.addView(this, mLp);
			mIsOpening = true;
		} catch (Exception e) {
		}
	}

	public void dismiss() {
		if (mIsOpening && BaseActivity.enableTestMask()) {
			mIsOpening = false;
			mWindowManager.removeView(this);
		}
	}
}