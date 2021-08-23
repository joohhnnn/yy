package com.txznet.record.view;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.record.lib.R;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

public class IndexView extends FrameLayout implements OnPagerListener {

	private View mTagView;
	private FrameLayout mLayout;

	private float mTotalCount;
	private float mDestinNum;

	private int mTagViewHeight;
	private int mContainerHeight;
	private int mCanScrollLength;

	private boolean mHasMeasured = false;

	public IndexView(Context context) {
		this(context, null);
	}

	public IndexView(Context context, AttributeSet attr) {
		this(context, attr, 0);
	}

	public IndexView(Context context, AttributeSet attr, int defValue) {
		super(context, attr, defValue);
		attachView();
	}

	private void attachView() {
		removeAllViews();
		setBackgroundColor(Color.TRANSPARENT);
		LayoutInflater.from(getContext()).inflate(R.layout.scroll_index_view, this);
		this.mTagView = findViewById(R.id.scroll_tag_view);
		this.mLayout = (FrameLayout) findViewById(R.id.scroll_container_fl);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
		if (!mHasMeasured) {
			mTagViewHeight = mTagView.getMeasuredHeight();
			mContainerHeight = mLayout.getMeasuredHeight();
			mCanScrollLength = mContainerHeight - mTagViewHeight;
			mHasMeasured = true;
		}
	}

	@Override
	public void setVisibility(int visibility) {
		mHasMeasured = false;
		super.setVisibility(visibility);
	}

	public void setTotalCount(float count) {
		LogUtil.logd("setTotalCount:" + count);
		this.mTotalCount = count;
		this.mDestinNum = -1;
	}

	public void destinNum(float num) {
		LogUtil.logd("destinNum:" + num);
		if (num == mDestinNum) {
			return;
		}
		this.mDestinNum = num;
		refreshScrollPos();
	}

	public void refreshScrollPos() {
		procDestinationPos();
	}

	private void procDestinationPos() {
		float ratio = mDestinNum / mTotalCount;
		int pos = (int) (ratio * mCanScrollLength);
		_refresh(pos);
	}

	/**
	 * 滑动到pos处
	 * 
	 * @param pos
	 */
	private void _refresh(int pos) {
		LogUtil.logd("start setTop pos:" + pos);
		pos = checkOutScrollPos(pos);
		mTagView.setTop(pos);
		mTagView.setBottom(pos + mTagViewHeight);
	}

	/**
	 * 越界检测修正
	 * 
	 * @param pos
	 * @return
	 */
	private int checkOutScrollPos(int pos) {
		if (pos < 0) {
			pos = 0;
		}

		if (pos > mCanScrollLength) {
			pos = mCanScrollLength;
		}
		return pos;
	}

	@Override
	public void onPrePager(int sel) {
		destinNum(sel);

	}

	@Override
	public void onNextPager(int sel) {
		destinNum(sel);
	}
}