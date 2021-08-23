package com.txznet.music.widget;

import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.music.R;

/**
 * 加载提示
 * 
 * @author ASUS User
 *
 */
public class LoadingUtils {

	public static class LoadingDialog extends Dialog {
		View view;
		ImageView iv_loading;
		TextView tv_loading;

		public LoadingDialog(Context context) {
			super(context, R.style.gt_dialog);
			view = View.inflate(context, R.layout.dialog_loading, null);
			tv_loading = (TextView) view.findViewById(R.id.tv_loading);
			iv_loading = (ImageView) view.findViewById(R.id.iv_loading);
			setContentView(view);
		}

		public LoadingDialog setLoadingText(String text) {
			tv_loading.setText(text);
			return this;
		}

		public LoadingDialog setLoadingResourceId(int drawableId) {
			iv_loading.setImageResource(drawableId);
			RotateAnimation animation = new RotateAnimation(0, 360, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
			animation.setRepeatCount(Animation.INFINITE);
			animation.setDuration(1000);
			animation.setFillAfter(true);
			animation.setInterpolator(new LinearInterpolator());// 匀速
			iv_loading.startAnimation(animation);
			return this;
		}
	}

	public static Dialog dialog;

	public static void showLoadingDialog(Context ctx) {
		dialog = new LoadingDialog(ctx).setLoadingResourceId(R.drawable.fm_loading).setLoadingText("正在加载...");
		dialog.show();
	}

	public static void dismissDialog() {
		if (null != dialog) {
			dialog.dismiss();
			dialog = null;
		}
	}
}
