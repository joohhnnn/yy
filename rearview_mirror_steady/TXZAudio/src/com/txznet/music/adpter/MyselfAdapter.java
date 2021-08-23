package com.txznet.music.adpter;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;
import com.txznet.music.adpter.base.MyBaseAdpater;
import com.txznet.music.bean.response.Category;
import com.txznet.music.fragment.LocalMusicFragment;
import com.txznet.music.fragment.SongListFragment;
import com.txznet.music.fragment.base.BaseFragment;
import com.txznet.music.widget.ButtonImageText;

public class MyselfAdapter extends MyBaseAdpater<Category> {

	private BaseFragment baseFragment;

	public MyselfAdapter(List<Category> data, BaseFragment baseFragment) {
		super(data, baseFragment.getActivity());
		this.baseFragment = baseFragment;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		final ViewHolder holder;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(ctx, R.layout.item_myself_layout, null);
			holder.bit = (ButtonImageText) convertView
					.findViewById(R.id.bit_center);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		Category menu = data.get(position);
		holder.bit.setImageResource(menu.getDrawableId());
		holder.bit.setTextImageSpace(ctx.getResources()
				.getDimensionPixelOffset(R.dimen.y30));
		holder.bit.setText(menu.getDesc());
		holder.bit.setTag(menu);
		holder.bit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				LogUtil.logd("title:" + data.get(position).getDesc() + ",tag:"
						+ data.get(position).getCategoryId());
				SongListFragment songListFragment = SongListFragment
						.getInstance();
				int categoryId = data.get(position).getCategoryId();
				if (songListFragment.isAdded()) {
					songListFragment.getArguments().putInt(
							SongListFragment.FRAGMENT_TYPE, categoryId);
				} else {
					Bundle bundle = new Bundle();
					bundle.putInt(SongListFragment.FRAGMENT_TYPE, categoryId);
					songListFragment.setArguments(bundle);
				}
				String title = ctx.getResources()
						.getString(R.string.localMusic);
				if (categoryId == 1) {
					title = ctx.getResources().getString(R.string.play_history);
					// baseFragment.jumpToOtherFragment(title, songListFragment,
					// null);
				} else {
					// baseFragment.jumpToOtherFragment(title,
					// new LocalMusicFragment(), null);
				}

				baseFragment.jumpToOtherFragment(title, songListFragment, null);
			}
		});
		return convertView;
	}

	private class ViewHolder {
		public ButtonImageText bit;
	}
}
