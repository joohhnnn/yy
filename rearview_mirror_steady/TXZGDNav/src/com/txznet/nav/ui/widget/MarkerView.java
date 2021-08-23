package com.txznet.nav.ui.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.txznet.nav.R;

public class MarkerView extends FrameLayout {

	private View mContentView;
	private ImageView mHeadIv;
	private ImageView mCarIv;
	private String uid;

	private volatile boolean isInit;
	private volatile float mRotate;

	public MarkerView(Context context) {
		this(context, null);
	}

	public MarkerView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public MarkerView(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);
		setDrawingCacheEnabled(true);
		initView();
	}

	private void initView() {
		removeAllViews();
		if (mContentView == null) {
			mContentView = LayoutInflater.from(getContext()).inflate(
					R.layout.marker_layout, null);
			mHeadIv = (ImageView) mContentView.findViewById(R.id.head_iv);
			mCarIv = (ImageView) mContentView.findViewById(R.id.car_iv);
		}

		LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
		if (lp == null) {
			lp = new LayoutParams(LayoutParams.WRAP_CONTENT,
					LayoutParams.WRAP_CONTENT);
		}

		addViewInLayout(mContentView, 0, lp, false);
		isInit = true;
	}

	public void setHeadImageBitmap(Bitmap bm) {
		if (!isInit) {
			return;
		}

		mHeadIv.setImageBitmap(bm);
	}

	public void setHeadImageDrawable(Drawable drawable) {
		if (!isInit) {
			return;
		}

		mHeadIv.setImageDrawable(drawable);
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	@SuppressLint("NewApi")
	public void setCarDirection(float direction) {
		mRotate = direction;
		// MyApplication.getApp().runOnUiGround(new Runnable1<Float>(direction)
		// {
		//
		// @Override
		// public void run() {
		mCarIv.setRotation(direction);
		// }
		// }, 0);
	}

	public float getRotate() {
		return mRotate;
	}
}
