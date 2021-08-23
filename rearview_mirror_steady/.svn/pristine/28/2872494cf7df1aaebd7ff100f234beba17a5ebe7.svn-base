package com.txznet.record.adapter;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.record.view.DisplayLvRef;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

public abstract class ChatDisplayAdapter extends BaseAdapter {

	List mDisplayList;
	Context mContext;
	DisplayLvRef mDLr;
	protected int mFocusIndex = -1;
	public int lastItemHeight = 0;

	public ChatDisplayAdapter(Context context, List displayList) {
		mContext = context;
		mDisplayList = displayList;
		if (mDisplayList == null) {
			mDisplayList = new ArrayList();
		}
		ScreenUtil.getDisplayLvItemH(true);
	}

	public void attachDisplayLv(DisplayLvRef dlr) {
		this.mDLr = dlr;
	}

	public void setDisplayList(List displayList) {
		this.mDisplayList = displayList;
		notifyDataSetChanged();
	}

	
	
	public void setFocusIndex(int index) {
		this.mFocusIndex = index;
	}

	@Override
	public int getCount() {
		if (mDisplayList != null) {
			return mDisplayList.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		if (mDisplayList != null && mDisplayList.size() > 0) {
			return mDisplayList.get(position);
		}
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	/**
	 * 设置listview item的高度，设置的LayoutParams是AbsListView.LayoutParams
	 * @param view
	 */
	protected void prepareSetLayoutParams(final View view) {
		if (ScreenUtil.getDisplayLvItemH(false) > 0) {
			LayoutParams lp = view.getLayoutParams();
			AbsListView.LayoutParams lParams;
			lastItemHeight = ScreenUtil.getDisplayLvItemH(false);
			if (lp == null) {
				lParams = new AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, lastItemHeight);
			}else {
				lParams = (AbsListView.LayoutParams) lp;
				lParams.width = LayoutParams.MATCH_PARENT;
				lParams.height = lastItemHeight;
			}
			view.setLayoutParams(lParams);
		}
	}
}
