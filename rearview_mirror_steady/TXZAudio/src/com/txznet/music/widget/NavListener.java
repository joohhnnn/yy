package com.txznet.music.widget;

public interface NavListener {
	interface OnRefreshListener{
		void onRefresh(int position);
	}
	
	void setFocus(boolean isFocus);

	int onNext();

	int onPrev();

	void onClick();
	
}
