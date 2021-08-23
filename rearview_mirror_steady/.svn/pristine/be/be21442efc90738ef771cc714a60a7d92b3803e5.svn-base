package com.txznet.txz.component.tmc;

import com.txznet.txz.R;

import android.app.Service;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.LinearLayout;

public abstract class AbsLayout extends LinearLayout {
	protected boolean mIsOpening;

	protected int mWidth;
	protected int mHeight;
	protected View mLayout;
	protected WindowManager mWinManager;
	protected WindowManager.LayoutParams mLp;

	public AbsLayout(Context context) {
		super(context);
		mWinManager = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
		mLayout = getLayoutView();
		LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		params.leftMargin = (int) getResources().getDimension(R.dimen.x50);
		params.rightMargin = (int) getResources().getDimension(R.dimen.x50);
		addView(mLayout, params);
	}

	@Override
	protected void onAttachedToWindow() {
		super.onAttachedToWindow();
		getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {

			@Override
			public boolean onPreDraw() {
				getViewTreeObserver().removeOnPreDrawListener(this);
				mWidth = mLayout.getLayoutParams().width;
				mHeight = mLayout.getLayoutParams().height;
				mLp.width = mWidth;
				mLp.height = mHeight;
				mWinManager.updateViewLayout(AbsLayout.this, mLp);
				return false;
			}
		});
	}

	protected abstract View getLayoutView();

	public void open() {
		if (mIsOpening) {
			return;
		}
		mLp = new WindowManager.LayoutParams();
		mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
		mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
		mLp.height = WindowManager.LayoutParams.MATCH_PARENT;
		mLp.horizontalMargin = getResources().getDimension(R.dimen.x50);
		mLp.flags = 40;
		mLp.format = PixelFormat.RGBA_8888;
		mLp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

		mWinManager.addView(this, mLp);
		mIsOpening = true;
	}

	public boolean isShowing() {
		return this.mIsOpening;
	}

	public void dismiss() {
		post(new Runnable() {
			
			@Override
			public void run() {
				if (mIsOpening) {
					mWinManager.removeView(AbsLayout.this);
					mIsOpening = false;
				}
			}
		});
	}

	public static int getColorByTrafficStatus(String status) {
		// 未知、畅通、缓行、拥堵
		int color = Color.parseColor("#0F89F5");
		if ("畅通".equals(status)) {
			color = Color.parseColor("#33B100");
		} else if ("缓行".equals(status)) {
			color = Color.parseColor("#FFCC00");
		} else if ("拥堵".equals(status) || "严重拥堵".equals(status)) {
			color = Color.parseColor("#DE0000");
		}
		return color;
	}

	public static String getRemainTime(int remainTime) {
		if (remainTime <= 0) {
			return "";
		}

		if (remainTime > 60) {
			if (remainTime >= 3600) {
				int r = (int) (remainTime % 3600);
				int h = (int) (remainTime / 3600);
				int m = r / 60;
				return h + "小时" + (m > 0 ? m + "分钟" : "");
			} else {
				return (remainTime / 60) + "分钟";
			}
		} else {
			return remainTime + "秒";
		}
	}

	public static String getRemainDistance(int remainDistance) {
		if (remainDistance <= 0) {
			return "";
		}

		if (remainDistance > 1000) {
			return (Math.round(remainDistance / 100.0) / 10.0) + "公里";
		} else {
			return remainDistance + "米";
		}
	}
}