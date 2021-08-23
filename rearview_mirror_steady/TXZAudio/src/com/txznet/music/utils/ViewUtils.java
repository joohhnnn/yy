package com.txznet.music.utils;

import java.util.List;

import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;

import com.txznet.music.R;
import com.txznet.music.listener.PageChangeListener;

public class ViewUtils {

	private final static int LENGTH = 3;

	/**
	 * 设置页签
	 * 
	 * @param Length
	 *            一页显示多少个元素，默认为3
	 */
	public static void setIndicator(ViewPager pager, final ViewGroup group,
			List<?> res, int Length) {
		int pageCount = 1;
		if (Length <= 0) {
			Length = 3;
		}
		if (CollectionUtils.isNotEmpty(res)) {
			pageCount = res.size() % Length == 0 ? res.size() / Length : res
					.size() / Length + 1;
		}
		group.removeAllViews();
		if (pageCount <= 1) {
			// 不需要显示分页标签
			return;
		}

		for (int i = 0; i < pageCount; i++) {
			ImageView ivdot = new ImageView(group.getContext());
			ivdot.setImageResource(R.drawable.dot_viewpager_selector);
			LayoutParams layoutParams = new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
			layoutParams.rightMargin = group.getContext().getResources()
					.getDimensionPixelSize(R.dimen.x10);
			ivdot.setLayoutParams(layoutParams);

			if (i == 0) {
				ivdot.setEnabled(true);
			} else {
				ivdot.setEnabled(false);
			}
			group.addView(ivdot);
		}
		pager.addOnPageChangeListener(new PageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				for (int i = 0; i < group.getChildCount(); i++) {
					if (null != group.getChildAt(position)) {
						group.getChildAt(i).setEnabled(false);
					}
				}
				if (null != group.getChildAt(position)) {
					group.getChildAt(position).setEnabled(true);
				}
			}
		});
	}
}
