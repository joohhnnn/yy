package com.txznet.txz.ui.win.nav;

import java.util.List;

import com.txznet.record.adapter.ChatPoiAdapter.PoiItem;
import com.txznet.record.util.ViewHolder;
import com.txznet.txz.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CityListAdapter extends BaseAdapter {
	List mDisplayList;
	Context mContext;
	public CityListAdapter(Context context, List<String> displayList){
		mContext = context;
		mDisplayList = displayList;
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

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.city_set_item_ly, parent, false);
		}
//		convertView.set
		TextView cityView = ViewHolder.get(convertView, R.id.tv_item_city);
		if(mDisplayList != null){
			cityView.setText((String)mDisplayList.get(position));
		}
		return convertView;
	}

	public void updata( List<String> displayList){
		mDisplayList = displayList;
	}
	
}
