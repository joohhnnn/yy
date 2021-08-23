package com.txznet.music.utils;

import com.txznet.music.bean.req.ReqDataStats.Action;

import android.app.Activity;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.PopupWindow.OnDismissListener;

public class Tool {

	private static Activity activity;
	private static PopupWindow mPopupWindow = new PopupWindow();

	public static void init(Activity activity1) {
		activity = activity1;
	}

	/**
	 * 设置添加屏幕的背景透明度
	 *
	 * @param bgAlpha
	 */
	public static void backgroundAlpha(float bgAlpha) {
		if (activity == null)
			throw new RuntimeException("没有初始化activity，没有调用init方法");
		setLayoutAlpha(activity, bgAlpha);
	}

	/**
	 * 设置界面的透明度
	 *
	 * @param activity
	 * @param alpha
	 *            0.0-1.0
	 */
	public static void setLayoutAlpha(Activity activity, float alpha) {
		if (null == activity) {
			return;
		}
		WindowManager.LayoutParams lp = activity.getWindow().getAttributes();
		lp.alpha = alpha; // 0.0-1.0
		lp.dimAmount = alpha;
		activity.getWindow().setAttributes(lp);
		activity.getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_DIM_BEHIND);
	}

	/**
	 * 获得正在显示的popupwindow
	 *
	 * @return
	 */
	public static PopupWindow getmPopupWindow() {
		if (mPopupWindow.isShowing()) {
			return mPopupWindow;
		}
		// 您还没有显示pupwindow，
		return null;
	}

	/**
	 * 展示popupwindow,在指定的视图的指定位置显示指定的内容
	 *
	 * @param rl
	 *            指定显示的内容
	 * @param view
	 *            指定显示的区域视图
	 * @param gravity
	 *            指定显示的位置Gravity.center,标示居中
	 */
	public static PopupWindow showPopAtPosition(int widthSpec, int heightSpec,
			final View rl, final View view, int gravity) {
		mPopupWindow.setWindowLayoutMode(widthSpec, heightSpec);
		mPopupWindow.setContentView(rl);
		mPopupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
		mPopupWindow.setBackgroundDrawable(new BitmapDrawable());
		Tool.backgroundAlpha(0.6f);
		// mPopupWindow.setAnimationStyle(R.style.menu_animation);
		mPopupWindow.setFocusable(true);
		mPopupWindow.setTouchable(true);
		mPopupWindow.setOutsideTouchable(true);
		mPopupWindow.showAtLocation(view, gravity, 0, 0);
		/**
		 * 设置dismiss事件，就可以了。
		 */
		mPopupWindow.setOnDismissListener(new OnDismissListener() {

			@Override
			public void onDismiss() {
				Tool.backgroundAlpha(1.0f);
				NetHelp.sendReportData(Action.SHOW_LIST);
				if(sListener != null){
					sListener.onDismiss();
				}
			}
		});

		/********************************** 测试 **********************************************/

		/*
		 * rl.findViewById(R.id.textView1).setOnClickListener(new
		 * OnClickListener() {
		 * 
		 * @Override public void onClick(View arg0) { TextView
		 * tView=(TextView)view.findViewById(R.id.tv_title_center_tri);
		 * tView.setText("客户"+((TextView)arg0).getText()); } });;
		 * if(listener!=null){ listener.onclick(); }
		 */

		/********************************************************************************/

		return mPopupWindow;
	}

	private static OnDismissListener sListener;
	public static void setOnDismissListener(OnDismissListener listener){
		sListener = listener;
	}
	
	/*
	 * private interface MyListener{ void onclick(); } private static MyListener
	 * listener; public void setOnClickListener(MyListener clickListener){
	 * listener=clickListener; }
	 */

	/**
	 * 展示popupwindow,在指定的视图的右下角显示指定的内容
	 *
	 * @param rl
	 *            显示的内容
	 * @param view
	 *            指定显示的区域视图
	 */
	public static PopupWindow showPop(final View rl, View view) {
		return showPopAtPosition(LayoutParams.WRAP_CONTENT,
				LayoutParams.MATCH_PARENT, rl, view, Gravity.RIGHT);
	}

	public static PopupWindow showFillPop(final View rl, View view) {
		return showPopAtPosition(LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT, rl, view, Gravity.CENTER);
	}
	

}
