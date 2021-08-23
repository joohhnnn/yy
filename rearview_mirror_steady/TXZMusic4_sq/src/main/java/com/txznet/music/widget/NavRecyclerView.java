package com.txznet.music.widget;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

public class NavRecyclerView extends RecyclerView {

	public interface EmptyStatusListener {
		void onStatusChange(boolean isEmpty);
	}

	private int mItemCountOffset = 0;
	private EmptyStatusListener mEmptyStatusListener;
	final private AdapterDataObserver mDataObserver = new AdapterDataObserver() {
		@Override
		public void onChanged() {
			checkEmpty();
		}

		@Override
		public void onItemRangeInserted(int positionStart, int itemCount) {
			checkEmpty();
		}

		@Override
		public void onItemRangeRemoved(int positionStart, int itemCount) {
			checkEmpty();
		}
	};

	public NavRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NavRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavRecyclerView(Context context) {
		super(context);
	}

	public void setEmptyStatusListener(EmptyStatusListener l){
		mEmptyStatusListener = l;
	}

	public void checkEmpty() {
		if (mEmptyStatusListener != null && getAdapter() != null) {
			boolean isEmpty = getAdapter().getItemCount() + getItemCountOffset() == 0;
			mEmptyStatusListener.onStatusChange(isEmpty);
		}
	}

	public int getItemCountOffset() {
		return mItemCountOffset;
	}

	public void setItemCountOffset(int offset) {
		this.mItemCountOffset = offset;
	}



	@Override
	public void setAdapter(Adapter adapter) {
		Adapter oldAdapter = getAdapter();
		if (oldAdapter != null) {
			oldAdapter.unregisterAdapterDataObserver(mDataObserver);
		}

		super.setAdapter(adapter);

		if (adapter != null) {
			adapter.registerAdapterDataObserver(mDataObserver);
		}
	}

}
