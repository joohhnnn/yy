package com.txznet.music.adpter;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.txznet.music.R;
import com.txznet.music.bean.response.Category;
import com.txznet.music.fragment.HomepageFragment;
import com.txznet.music.fragment.base.BaseFragment;

public class MyCategoryAdapter extends PagerAdapter {
	private int LENGTH = HomepageFragment.LENGTH;
	List<Category> categorys = new ArrayList<Category>();
	private BaseFragment fragment;
	private BaseAdapter adapter;

	// private int pageSize;

	// private List<View> childs;

	public MyCategoryAdapter(List<Category> categorys, BaseFragment fragment) {
		super();
		this.categorys = categorys;
		this.fragment = fragment;
	}

	@Override
	public int getCount() {
		LENGTH = HomepageFragment.LENGTH;
		return categorys.size() % LENGTH == 0 ? categorys.size() / LENGTH : categorys.size() / LENGTH + 1;
	}

	public void setData(List<Category> categories) {
		this.categorys.clear();
		this.categorys.addAll(categories);
		notifyDataSetChanged();
	}

	@Override
	public Object instantiateItem(ViewGroup container, final int page) {

		View child = View.inflate(fragment.getActivity(), R.layout.item_category, null);
		// 截取的终点索引
		int end = (page + 1) * LENGTH <= categorys.size() ? (page + 1) * LENGTH : categorys.size();

		adapter = new ItemGridViewAdapter(fragment, categorys.subList(page * LENGTH, end));
		GridView mGridView = (GridView) child.findViewById(R.id.gridView1);
		mGridView.setAdapter(adapter);
		mGridView.setNumColumns(HomepageFragment.itemColCount);
		mGridView.setSelector(new ColorDrawable(Color.TRANSPARENT));
		container.addView(child);
		return child;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	/**
	 * 
	 * @author ASUS User
	 *
	 */
	public interface ItemClickListener {
		public void onClick(int id);
	}

	private int mChildCount = 0;

	@Override
	public void notifyDataSetChanged() {

		mChildCount = getCount();

		super.notifyDataSetChanged();

	}

	@Override
	public int getItemPosition(Object object) {

		if (mChildCount > 0) {

			mChildCount--;

			return POSITION_NONE;

		}

		return super.getItemPosition(object);

	}

}
