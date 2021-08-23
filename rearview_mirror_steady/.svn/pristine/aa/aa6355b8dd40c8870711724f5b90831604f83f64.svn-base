package com.txznet.comm.ui.layout.layout1;

import com.txznet.comm.ui.layout.IContentView;
import com.txznet.comm.ui.layout.IView;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;

public class ChatContentView extends IContentView {

	private LayoutParams mViewParams;
	private LinearLayout mLayout;
	private ChatListView mWrapperView; //用来装载显示聊天信息
	
	public ChatContentView(Context context) {
		mWrapperView = new ChatListView(context);
		mLayout = new LinearLayout(context);
		mLayout.setBackgroundColor(Color.TRANSPARENT);
		mViewParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
		mLayout.addView(mWrapperView.get(), mViewParams);
	}
	
	
	@Override
	public void addView(View view) {
		mWrapperView.addView(view);
		mWrapperView.scrollToEnd();
	}


	@Override
	public View get() {
		return mLayout;
	}


	@Override
	public int getTXZViewId() {
		return IView.ID_CHAT_CONTENT;
	}


	@Override
	public void reset() {
		mWrapperView.reset();
	}

	@Override
	public void removeLastView() {
		mWrapperView.removeLastView();
	}


}
