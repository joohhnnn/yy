package com.txznet.music.widget;

import com.txznet.comm.remote.util.LogUtil;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView.Recycler;
import android.support.v7.widget.RecyclerView.State;
import android.util.Log;

public class LinearLayoutManagerWrapper extends LinearLayoutManager{

	private String tag;

	public LinearLayoutManagerWrapper(Context context, String tag) {
		super(context);
		this.tag = tag;
	}
	
	@Override
	public void onLayoutChildren(Recycler arg0, State arg1) {
		try {
			super.onLayoutChildren(arg0, arg1);
		} catch (Exception e) {
			LogUtil.loge("LinearLayoutManager " + tag + " Exception:" + e.toString());
		}
		
	}
	
}
