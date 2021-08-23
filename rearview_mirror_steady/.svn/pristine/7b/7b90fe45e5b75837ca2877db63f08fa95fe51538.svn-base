package com.txznet.music.adpter;

import java.util.List;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.MarqueeText;

public class ItemPlayerPageAdapter extends PagerAdapter {

	private Context ctx;
	private List<Audio> data;
	private View view;
	// private TextView song;
	private com.txznet.music.widget.MarqueeText song;
	private TextView singer;

	public ItemPlayerPageAdapter(Context ctx, List<Audio> songInfos) {
		super();
		this.ctx = ctx;
		this.data = songInfos;
	}

	public ItemPlayerPageAdapter(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		if (null != data) {
			return data.size();
		}
		return 0;
	}

	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}

	public void setData(List<Audio> data) {

		if (null == this.data) {
			this.data = data;
		}
		notifyDataSetChanged();
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		view = View.inflate(ctx, R.layout.item_meida_player, null);
		// song = (TextView) view.findViewById(R.id.tv_song_title);
		song = (MarqueeText) view.findViewById(R.id.tv_song_title);
		singer = (TextView) view.findViewById(R.id.tv_singer);
		Audio songInfo = data.get(position);
		song.setTypeface(Constant.typeFace);
		song.setText(songInfo.getName());
		song.setSpeed(10);
		song.setDelayed(200);
		if (CollectionUtils.isNotEmpty(songInfo.getArrArtistName())) {
			singer.setText(CollectionUtils.toString(songInfo.getArrArtistName()));
		} else if (!Utils.isSong(songInfo.getSid())) {
			/*null != songInfo.getStrCategoryId() && "200000".compareTo(songInfo.getStrCategoryId()) <= 0 && StringUtils.isNotEmpty(MediaPlayerActivityEngine.getInstance().getCurrentAlbumName())*/
			
			singer.setText(MediaPlayerActivityEngine.getInstance().getCurrentAlbumName());
		} else {
//			singer.setText("未知艺术家");
		}
		container.addView(view);
		return view;
	}

	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		container.removeView((View) object);
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	public Object getObjectAtItem(int position) {
		if (data.size() <= position) {
			return data.get(0);
		} else if (position < 0) {
			return data.get(data.size() - 1);
		}
		return data.get(position);
	}

}
