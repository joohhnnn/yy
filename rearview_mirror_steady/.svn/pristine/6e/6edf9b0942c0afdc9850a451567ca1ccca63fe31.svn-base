package com.txznet.comm.ui.layout.layout1;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.layout.IContentView;
import com.txznet.comm.ui.layout.IView;
import android.content.Context;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class FullContentView extends IContentView {

	private RelativeLayout mLayout;
	private LayoutParams mChildParams;
	private Context mContext;
	
	public FullContentView(Context context) {
		mLayout = new RelativeLayout(context);
		this.mContext = context;
		mChildParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
	}
	
	 boolean isAdded = false;
	
	@Override
	public void addView(View view) {
		// if (!isAdded) {
		mLayout.addView(view, mChildParams);
		// isAdded = true;
		// }
	}


	@Override
	public View get() {
		return mLayout;
	}


	@Override
	public int getTXZViewId() {
		return IView.ID_FULL_CONTENT;
	}


	// private Field mVerticalViews;
	// private Field mHorizontalViews;
	
	@Override
	public void reset() {
		LogUtil.logd("removeAllViews");
		mLayout.removeAllViews();
		mLayout.requestLayout();
		// if (mVerticalViews == null || mHorizontalViews == null) {
		// try {
		// mVerticalViews =
		// mLayout.getClass().getDeclaredField("mSortedVerticalChildren");
		// mVerticalViews.setAccessible(true);
		// mHorizontalViews =
		// mLayout.getClass().getDeclaredField("mSortedHorizontalChildren");
		// mHorizontalViews.setAccessible(true);
		// } catch (NoSuchFieldException e) {
		// e.printStackTrace();
		// }
		// }
		// try {
		// mVerticalViews.set(mLayout, null);
		// mHorizontalViews.set(mLayout, null);
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}

	@Override
	public void removeLastView() {

	}

}
