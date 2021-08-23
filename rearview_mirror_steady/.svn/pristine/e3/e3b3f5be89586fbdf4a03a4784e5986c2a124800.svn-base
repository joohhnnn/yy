package com.txznet.music.fragment;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.MyselfAdapter;
import com.txznet.music.bean.response.Category;
import com.txznet.music.fragment.base.BaseFragment;

public class MyselfFragment extends BaseFragment {

	private GridView mGridView;
	private MyselfAdapter adapter;

//	private static MyselfFragment instance = new MyselfFragment();
//
//
//	public static MyselfFragment getInstance() {
//		return instance;
//	}

	@Override
	public void bindViews() {
		mGridView = (GridView) findViewById(R.id.gridView1);
	}

	@Override
	public void initListener() {
	}

	@Override
	public void initData() {
		List<Category> data = new ArrayList<Category>();
		Category localCategory = new Category();
		localCategory.setCategoryId(Constant.LOCAL_MUSIC_TYPE);
		localCategory.setDrawableId(R.drawable.fm_me_local_music);
		localCategory.setDesc(getResources().getString(R.string.localMusic));
		data.add(localCategory);

		Category category = new Category();
		category.setCategoryId(Constant.HISTORY_TYPE);
		category.setDrawableId(R.drawable.fm_me_play_history);
		category.setDesc(getResources().getString(R.string.play_history));
		data.add(category);
		adapter = new MyselfAdapter(data, this);
		mGridView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	@Override
	public int getLayout() {
		return R.layout.fragment_myself;
	}

	@Override
	public int getFragmentId() {
		return 0;
	}

	@Override
	public void reqData() {

	}


}
