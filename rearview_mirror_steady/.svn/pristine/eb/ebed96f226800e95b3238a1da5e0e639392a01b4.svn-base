package com.txznet.music.adpter;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import android.content.Context;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.R;
import com.txznet.music.bean.response.Category;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.DimenUtils;
import com.txznet.music.widget.NavListener;

public class TypeGridAdapter extends BaseAdapter implements NavListener{
	private static int columNum = 3;
	private Context ctx;
	private List<Category> data = new ArrayList<Category>();
	private static int mHeight = 0;

	public TypeGridAdapter(Context ctx, List<Category> list) {
		super();
		selectedIndex = 0;
		data = list;
		this.ctx = ctx;
		mHeight = ctx.getResources().getDimensionPixelOffset(R.dimen.y96);
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
			convertView = View.inflate(ctx, R.layout.filter_grid_view_layout,
					null);

			holder.name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.bg = convertView.findViewById(R.id.v_bg);
			holder.bottom = convertView
					.findViewById(R.id.recommend_item_bottomline);
			holder.left = convertView
					.findViewById(R.id.recommend_item_leftline);
			holder.right = convertView
					.findViewById(R.id.recommend_item_rightline);
			holder.top = convertView.findViewById(R.id.recommend_item_topline);
			holder.name
					.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
							5) });// 限制为5个字
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		LayoutParams layoutParams = convertView.getLayoutParams();
		if (layoutParams == null) {
			layoutParams = new AbsListView.LayoutParams(
					AbsListView.LayoutParams.MATCH_PARENT, mHeight);
		}
		if (layoutParams.height != mHeight) {
			layoutParams.height = mHeight;
		}
		convertView.setLayoutParams(layoutParams);

		holder.name.setText(data.get(position).getDesc());

		if (position == selectedIndex) {
			holder.name.setBackgroundColor(ctx.getResources().getColor(
					R.color.green));
		} else {
			holder.name.setBackgroundResource(0);
		}
		holder.left.setVisibility(View.GONE);
		holder.right.setVisibility(View.GONE);
		holder.top.setVisibility(View.GONE);
		holder.bottom.setVisibility(View.GONE);
		// 第一列和第二列
		if (position % columNum == 0 || position % columNum == 1) {
			holder.left.setVisibility(View.VISIBLE);
		}
		// 第三列
		if (position % columNum == 2) {
			holder.left.setVisibility(View.VISIBLE);
			holder.right.setVisibility(View.VISIBLE);
		}
		// 第一行
		if (position / columNum == 0) {
			holder.top.setVisibility(View.VISIBLE);
			holder.bottom.setVisibility(View.VISIBLE);
		} else
		// 除第一列
		if (position / columNum >= 1) {
			holder.bottom.setVisibility(View.VISIBLE);
		}

		if (position == data.size() - 1) {
			holder.left.setVisibility(View.VISIBLE);
			holder.right.setVisibility(View.VISIBLE);
			// holder.top.setVisibility(View.VISIBLE);
			holder.bottom.setVisibility(View.VISIBLE);
		}
		
		if(position == mNavFocusIndex){
			holder.bg.setBackgroundResource(R.drawable.shape_focus_rect);
		}else{
			holder.bg.setBackground(null);
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
		public View top, bottom, left, right, bg;
	}

	/**
	 * 设置每一个Item的高度
	 * 
	 * @param i
	 */
	public void setItemHeight(int i) {
		if (i != 0) {
			mHeight = i;
			notifyDataSetInvalidated();
		}
	}

	private int mNavFocusIndex = -1;
	private boolean isFocus = false;
	private OnItemClickListener mItemClickListener;
	
	@Override
	public void setFocus(boolean isFocus) {
		
	}

	@Override
	public int onNext() {
		if(mNavFocusIndex < getCount() - 1){
			mNavFocusIndex++;
//			notifyDataSetChanged();
		}
		return mNavFocusIndex;
	}

	@Override
	public int onPrev() {
		if(mNavFocusIndex > 0){
			mNavFocusIndex--;
//			notifyDataSetChanged();
		}
		return mNavFocusIndex;
	}

	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}
	
	@Override
	public void onClick() {
		if(mNavFocusIndex >= 0 && mNavFocusIndex < getCount() && mItemClickListener!= null){
			mItemClickListener.onItemClick(null, null, mNavFocusIndex, 0);
		}
	}
}
