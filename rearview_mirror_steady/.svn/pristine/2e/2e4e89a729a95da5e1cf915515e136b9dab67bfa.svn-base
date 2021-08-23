package com.txznet.txz.ui.widget;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.ui.dialog.WinDialog;

import android.R;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

public class PhotoFloatView extends WinDialog {
	private ImageView mShow;
	private int mShowW;
	private int mShowH;

	public PhotoFloatView(String url) {
		super(true);
		initMeasure(url);
		ImageLoader.getInstance().displayImage("file://" + url, mShow);
	}

	@Override
	protected View createView() {
		mShow = new ImageView(getContext());
		mShow.setPadding(10, 10, 10, 10);
		mShow.setBackgroundColor(Color.WHITE);
		mShow.setScaleType(ScaleType.FIT_XY);
		return mShow;
	}

	private void initMeasure(String imgPath) {
		// 获取屏幕大小，可用大小最大80%
		DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
		float allowW = metrics.widthPixels * 0.8f;
		float allowH = metrics.heightPixels * 0.8f;

		// 获取图片的原始大小
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imgPath, options);
		int w = options.outWidth;
		int h = options.outHeight;

		// 计算实际尺寸
		float rateW = w * 1f / allowW;
		float rateH = h * 1f / allowH;
		float maxRate = rateW > rateH ? rateW : rateH;
		int resultW = (int) (w / maxRate);
		int resultH = (int) (h / maxRate);

		mShowW = resultW - 10;
		mShowH = resultH - 10;
	}

	public void setImageResource(String imgPath) {
		initMeasure(imgPath);
		ImageLoader.getInstance().displayImage("file://" + imgPath, mShow);
	}

	@Override
	public void show() {
		getWindow().setLayout(mShowW, mShowH);
		getWindow().setWindowAnimations(R.style.Animation_Translucent);
		super.show();
	}

	@Override
	public void dismiss() {
		super.dismiss();
	}
}
