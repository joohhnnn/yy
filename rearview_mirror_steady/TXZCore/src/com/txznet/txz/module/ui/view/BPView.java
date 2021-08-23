package com.txznet.txz.module.ui.view;

import com.txznet.txz.INoProguard;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

public abstract class BPView extends LinearLayout implements INoProguard{
	private String mJsonData;

	public BPView(Context context, String jsonData) {
		super(context, null);

		mJsonData = jsonData;
		View view = createView();
		refreshView(view, mJsonData);

		removeAllViewsInLayout();
		LayoutParams lp = getLayoutParams();
		if (lp != null) {
			addView(view, lp);
		} else {
			addView(view);
		}

		requestLayout();
	}

	/**
	 * 生成界面
	 * 
	 * @param jsonData
	 * @return
	 */
	public abstract View createView();

	/**
	 * 获取展示参数
	 */
	public LayoutParams getLayoutParams() {
		return null;
	}

	/**
	 * 刷新界面
	 * 
	 * @param jsonData
	 */
	public abstract void refreshView(View contentView, String jsonData);
}