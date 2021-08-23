package com.txznet.record.bean;

import java.util.List;

import com.txznet.record.view.TitleView.Info;

import android.view.View.OnTouchListener;
import android.widget.AdapterView.OnItemClickListener;

public abstract class BaseDisplayMsg<T> extends ChatMessage {
	public BaseDisplayMsg(int type) {
		super(type);
	}

	public List<T> mItemList;

	public String mKeywords;

	// public String mTopkeywords;
	public Info mTitleInfo;

	public boolean mNeedNotify;

	public String action;

	public OnItemClickListener mOnItemClickListener;

	public OnTouchListener mOnTouchListener;
	
	// 当前页
	public int curPage;
	// 最大页
	public int maxPage;
}
