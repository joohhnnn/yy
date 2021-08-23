package com.txznet.music.adpter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.bean.response.Category;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.DimenUtils;

public class FilterGridViewAdapter1 extends BaseAdapter {
	private static int columNum = 3;
	private Context ctx;
	private List<Category> data;
//	private int itemWidth;
//	private int itemHeight;

	public FilterGridViewAdapter1(Context ctx, List<Category> data) {
		super();
		selectedIndex = 0;
		this.ctx = ctx;
		this.data = data;
//		int remainWidth = DimenUtils.dip2Pixel(ctx, 92);// GridView左右间距32dip
//		itemWidth = (AppLogic.width - remainWidth) / columNum;
//		Log.d("########", "width:" + itemWidth + "height:" + itemHeight);
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

	private List<TextView> tvs = new ArrayList<TextView>();
	private static int selectedIndex;

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(ctx, R.layout.filter_grid_view_layout, null);
			int height = ctx.getResources().getDimensionPixelSize(R.dimen.y94);
			AbsListView.LayoutParams params = new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, height);
			convertView.setLayoutParams(params);
			holder.name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.bottom = convertView.findViewById(R.id.recommend_item_bottomline);
			holder.left = convertView.findViewById(R.id.recommend_item_leftline);
			holder.right = convertView.findViewById(R.id.recommend_item_rightline);
			holder.top = convertView.findViewById(R.id.recommend_item_topline);
			holder.name.setFilters(new InputFilter[] { new InputFilter.LengthFilter(5) });// 限制为5个字
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.name.setText(data.get(position).getDesc());

		if (position == selectedIndex) {
			holder.name.setBackgroundResource(R.drawable.type_select_bg);
			holder.left.setVisibility(View.GONE);
			holder.right.setVisibility(View.GONE);
			holder.top.setVisibility(View.GONE);
			holder.bottom.setVisibility(View.GONE);
		} else {
			holder.name.setBackgroundResource(R.drawable.filter_normal_bg);
			holder.left.setVisibility(View.VISIBLE);
			holder.right.setVisibility(View.VISIBLE);
			holder.top.setVisibility(View.VISIBLE);
			holder.bottom.setVisibility(View.VISIBLE);
		}

		holder.top.setVisibility(View.GONE);
		// 第一列
		if (position % columNum == 0) {
			// holder.left.setVisibility(View.VISIBLE);
			holder.right.setVisibility(View.GONE);
		}
		// 第三列
		if (position % columNum == 2) {
			// holder.right.setVisibility(View.VISIBLE);
			holder.left.setVisibility(View.GONE);
		}
		// 第一行
		if (position / columNum == 0) {
			holder.top.setVisibility(View.VISIBLE);
		}
		if (position== data.size()-1&&position!=selectedIndex) {
			holder.left.setVisibility(View.VISIBLE);
			holder.right.setVisibility(View.VISIBLE);
			holder.top.setVisibility(View.VISIBLE);
			holder.bottom.setVisibility(View.VISIBLE);
		}

		return convertView;
	}

	public TextView getTextView(int position) {
		return tvs.get(position);
	}

	public void setSelectedIndex(int position) {
		selectedIndex = position;
	}

	private class ViewHolder {
		public TextView name;
		public View top, bottom, left, right;
	}
}
