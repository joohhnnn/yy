package com.txznet.record.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.AbsListView;
import android.widget.ListAdapter;

import com.txznet.record.lib.R;

public class DisplayLvEx extends AbsLv {

	private int mVisibleCount;

	public DisplayLvEx(Context context) {
		super(context);
	}
	
	public DisplayLvEx(Context context,AttributeSet attr) {
		super(context,attr);
	}
	
	public DisplayLvEx(Context context,AttributeSet attr,int defValue) {
		super(context,attr,defValue);
	}

	@Override
	public void setAdapter(ListAdapter adapter) {
		setBackground(getResources().getDrawable(R.drawable.white_range_layout));
		super.setAdapter(adapter);
		updateScrollBar(mVisibleCount);
		if (mIndexView != null) {
			mIndexView.destinNum(0);
		}
	}

	public void updateScrollBar(int visibleCount) {
		this.mVisibleCount = visibleCount;
		if (mIndexView == null) {
			return;
		}

		final int childCount = getAdapter() != null ? getAdapter().getCount() : 0;
		if (childCount > visibleCount) {
			mIndexView.setTotalCount(childCount - visibleCount);
			mIndexView.setVisibility(VISIBLE);
		} else {
			mIndexView.setVisibility(INVISIBLE);
		}
	}
	
	public void updateScrollBar(){
		updateScrollBar(mVisibleCount);
	}
	
	@Override
	public void setSelection(int position) {
		super.setSelection(position);
		if(mIndexView != null){
			mIndexView.destinNum(position);
		}
	}

	@Override
	protected void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
		if (isViewTop() || isViewBottom()) {
			return;
		}

		if (mVisibleCount == (visibleItemCount - 1)) {
			if ((firstVisibleItem + visibleItemCount) >= totalItemCount) {
				firstVisibleItem++;
			}
		}

		if (mIndexView != null) {
			mIndexView.destinNum(firstVisibleItem);
		}
	}
}