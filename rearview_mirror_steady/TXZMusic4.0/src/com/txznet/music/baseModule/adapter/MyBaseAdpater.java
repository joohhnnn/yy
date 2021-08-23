package com.txznet.music.baseModule.adapter;

import android.content.Context;
import android.widget.BaseAdapter;

import com.txznet.comm.util.CollectionUtils;

import java.util.List;


public abstract class MyBaseAdpater<T> extends BaseAdapter {

	public List<T> data;
	public Context ctx;

	public MyBaseAdpater(List<T> data, Context ctx) {
		super();
		this.data = data;
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		if (!CollectionUtils.isEmpty(data)) {
			return data.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
}
