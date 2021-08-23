package com.txznet.music.adpter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.req.ReqCategory;
import com.txznet.music.bean.req.ReqSearchAlbum;
import com.txznet.music.bean.response.Category;
import com.txznet.music.fragment.HomepageFragment;
import com.txznet.music.fragment.MusicFragment;
import com.txznet.music.fragment.MyselfFragment;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.ImageUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.NetHelp;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.widget.ShadeImageView;

public class ItemGridViewAdapter extends BaseAdapter {

	private boolean isClicked = false;
	private BaseFragment baseFragment;
	private int width = HomepageFragment.imageWidth_Height - 7;

	private List<Category> menus = new ArrayList<Category>();

	public ItemGridViewAdapter(BaseFragment baseFragment) {
		super();
		this.baseFragment = baseFragment;
	}

	public ItemGridViewAdapter(BaseFragment baseFragment, List<Category> menus) {
		super();
		this.baseFragment = baseFragment;
		this.menus = menus;
	}

	@Override
	public int getCount() {
		if (null != menus && menus.size() > 0) {
			return menus.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return menus.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@SuppressLint("ViewHolder")
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ShadeImageView shadeImageView = new ShadeImageView(
				baseFragment.getActivity());
		Category category = menus.get(position);
		AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(
				width, width);
		shadeImageView.setLayoutParams(layoutParams);
		if (StringUtils.isNotEmpty(category.getLogo())) {
			int londingDrawable;
			Category cat = Constant.defaultCategorys.get(category.getCategoryId());
			if (cat != null) {
				londingDrawable = cat.getDrawableId();
			} else {
				londingDrawable = R.drawable.fm_item_default;
			}
			DisplayImageOptions imageOptions = ImageUtils.initDefault(R.drawable.fm_item_default, londingDrawable,
					londingDrawable, 10);
			ImageLoader.getInstance().displayImage(category.getLogo(), (ImageView) shadeImageView, imageOptions);
		} else {
			if (category.getDrawableId() == 0) {
				category.setDrawableId(R.drawable.fm_home_icon_cars);
			}
			shadeImageView.setImageResource(category.getDrawableId());
		}

		shadeImageView.setName(category.getDesc());
		shadeImageView.setTag(category);

		shadeImageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				if(isClicked)
					return;
				
				Category category = (Category) shadeImageView.getTag();

				Intent intent = new Intent();
				String json = JsonHelper.toJson(category);
				intent.putExtra(Constant.INTENT_CATEGORY, json);
				LogUtil.logd("send_Bundle=" + json);
				if (1 == category.getCategoryId()) {
					baseFragment.jumpToOtherFragment(category.getDesc(),
							new MyselfFragment(), null);
				} else {
					baseFragment.jumpToOtherFragment(shadeImageView.getName(),
							new MusicFragment(), intent);
				}
				
				isClicked = true;
				AppLogic.runOnUiGround(new Runnable() {
					
					@Override
					public void run() {
						isClicked = false;
					}
				}, 500);
			}
		});

		convertView = shadeImageView;
		return convertView;
	}

}
