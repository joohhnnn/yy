package com.txznet.music.widget;

import com.txznet.txz.util.NavBtnSupporter.NavBtnSupporter;
import com.txznet.txz.util.NavBtnSupporter.interfaces.INavFocusable;
import com.txznet.txz.util.NavBtnSupporter.interfaces.INavOperationPresenter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.AttributeSet;
import android.view.View;

public class NavRecyclerView extends RecyclerView implements INavFocusable, INavOperationPresenter {

	private boolean isIn = false;
	private View mEmptyView = null;

	public NavRecyclerView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NavRecyclerView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavRecyclerView(Context context) {
		super(context);
	}

	public void setEmptyView(View view) {
		this.mEmptyView = view;
	}

	public void checkEmpty() {
		if (mEmptyView != null && getAdapter() != null) {
			// getAdapter().getItemCount()
			boolean isEmpty = getAdapter().getItemCount() == 0;
			mEmptyView.setVisibility(isEmpty ? VISIBLE : GONE);
			setVisibility(isEmpty ? GONE : VISIBLE);
		}
	}

	@Override
	public void setAdapter(Adapter adapter) {
		Adapter oldAdapter = getAdapter();
		if (oldAdapter != null) {
			oldAdapter.unregisterAdapterDataObserver(mDataOberver);
		}

		super.setAdapter(adapter);

		if (adapter != null) {
			adapter.registerAdapterDataObserver(mDataOberver);
		}
	}

	final private AdapterDataObserver mDataOberver = new AdapterDataObserver() {
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
	
	public void setNavIn(boolean isIn){
		this.isIn = isIn;
		mListener.setFocus(true);
	}
	

	@Override
	public void onNavGainFocus() {

	}

	@Override
	public void onNavLoseFocus() {

	}

	@Override
	public boolean showDefaultSelectIndicator() {
		if (isIn) {
			return false;
		}
		return true;
	}

	@Override
	public boolean onNavOperation(int arg0) {

		switch (arg0) {
		case NavBtnSupporter.NAV_BTN_BACK:
			if (isIn) {
				isIn = false;
				mListener.setFocus(false);                                                      
				return true;
			}
			break;
		case NavBtnSupporter.NAV_BTN_CLICK:
			if (!isIn) {
				if(getAdapter() != null && getAdapter().getItemCount() == 0){
					break;
				}
				isIn = true;
				mListener.setFocus(true);
			} else {
				mListener.onClick();
			}
			break;
		case NavBtnSupporter.NAV_BTN_NEXT:
			if (isIn) {
				int position = mListener.onNext();
				smoothScrollToPosition(position);
			}
			break;
		case NavBtnSupporter.NAV_BTN_PREV:
			if (isIn) {
				int position = mListener.onPrev();
				smoothScrollToPosition(position);
			}
			break;
		default:
			break;
		}
		if (isIn) {
			return true;
		}
		
	 
		return false;
		}

	private NavListener mListener;

	public void setNavListener(NavListener listener) {
		this.mListener = listener;
	}

	
	public boolean isIn(){
		return this.isIn;
	}
}
