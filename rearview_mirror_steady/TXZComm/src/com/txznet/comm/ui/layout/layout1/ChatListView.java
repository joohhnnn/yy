package com.txznet.comm.ui.layout.layout1;

import com.txznet.comm.ui.adapter.ChatListViewAdapter;
import com.txznet.comm.ui.layout.IContentView;
import com.txznet.comm.ui.layout.IView;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.ListView;

public class ChatListView extends  IContentView{

	private ListView mChatList;
	private ChatListViewAdapter mChatAdapter;
	private Context mContext;
	
	public ChatListView(Context context) {
		mContext = context;
		mChatList = new ListView(context);
		mChatList.setDivider(new ColorDrawable(Color.GRAY));
		mChatList.setSelector(new ColorDrawable(Color.TRANSPARENT));
		mChatList.setVerticalScrollBarEnabled(false);
		mChatAdapter = new ChatListViewAdapter();
		mChatList.setAdapter(mChatAdapter);
	}
	
	@Override
	public void addView(View view) {
		mChatAdapter.addView(view);
	}

	@Override
	public View get() {
		return mChatList;
	}

	@Override
	public int getTXZViewId() {
		return IView.ID_CHAT_CONTENT_LIST;
	}

	public void scrollToEnd(){
		mChatList.postDelayed(new Runnable() {
			@Override
			public void run() {
				mChatList.setSelection(mChatAdapter.getCount() - 1);
				mChatList.requestLayout();
			}
		}, 500);
		// mChatList.scrollTo(0, mChatList.getBottom() - mChatList.getHeight());
	}
	
	
	@Override
	public void reset() {
		mChatAdapter.reset();
		mChatAdapter = new ChatListViewAdapter();
		mChatList.setAdapter(mChatAdapter);
	}

	@Override
	public void removeLastView() {
		mChatAdapter.removeLastView();
	}
}
