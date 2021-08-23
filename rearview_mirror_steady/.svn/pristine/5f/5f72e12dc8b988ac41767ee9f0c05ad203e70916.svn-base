package com.txznet.music.widget;

import com.txznet.txz.util.NavBtnSupporter.NavBtnSupporter;
import com.txznet.txz.util.NavBtnSupporter.interfaces.INavFocusable;
import com.txznet.txz.util.NavBtnSupporter.interfaces.INavOperationPresenter;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.BaseAdapter;
import android.widget.GridView;

public class NavGridView extends GridView implements INavFocusable, INavOperationPresenter{

	private boolean isIn = false;
	
	public NavGridView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public NavGridView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NavGridView(Context context) {
		super(context);
	}

	
	public void setNavIn(boolean isIn){
		this.isIn = isIn;
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
				isIn = true;
				mListener.setFocus(true);
			} else {
				mListener.onClick();
			}
			break;
		case NavBtnSupporter.NAV_BTN_NEXT:
			if (isIn) {
				int position = mListener.onNext();
				if (getAdapter() instanceof BaseAdapter) {
					BaseAdapter adapter = (BaseAdapter) getAdapter();
					adapter.notifyDataSetChanged();
					setSelection(position);
				}
			}
			break;
		case NavBtnSupporter.NAV_BTN_PREV:
			if (isIn) {
				int position = mListener.onPrev();
				if (getAdapter() instanceof BaseAdapter) {
					BaseAdapter adapter = (BaseAdapter) getAdapter();
					adapter.notifyDataSetChanged();
					setSelection(position);
				}
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

	private NavListener mListener;

	public void setNavListener(NavListener listener) {
		this.mListener = listener;
	}
}
