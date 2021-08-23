package com.txznet.music.adpter;

import java.util.List;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

public class ItemPlayerListAdapter extends BaseAdapter {

	private List<Audio> data;
	private Context ctx;

	public ItemPlayerListAdapter(List<Audio> data, Context ctx) {
		super();
		this.data = data;
		this.ctx = ctx;
	}

	public ItemPlayerListAdapter(Context ctx) {
		super();
		this.ctx = ctx;
	}

	@Override
	public int getCount() {
		if (!CollectionUtils.isEmpty(data)) {
			return data.size();
		}
		return 0;
	}

	public void setData(List<Audio> data,boolean isAdd) {
		if (null == this.data) {
			this.data = data;
		}
		notifyDataSetChanged();
	}

	@Override
	public Object getItem(int position) {
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(ctx, R.layout.item_player_list, null);
			holder.title = (TextView) convertView.findViewById(R.id.title);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, ctx.getResources().getDimensionPixelSize(R.dimen.y90));
			params.leftMargin = ctx.getResources().getDimensionPixelSize(R.dimen.x10);
			params.bottomMargin = ctx.getResources().getDimensionPixelSize(R.dimen.y3);
			params.topMargin = ctx.getResources().getDimensionPixelSize(R.dimen.y3);
			holder.title.setLayoutParams(params);
			holder.title.setTypeface(Constant.typeFace);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		if (currentPosition == position) {
			holder.title.setSelected(true);
			LogUtil.logd("[MUSIC]current position:" + position);
		} else {
			holder.title.setSelected(false);
		}
		Audio audio = data.get(position);
		if (StringUtils.isEmpty(audio.getName())) {
			audio.setName("无");
		}

		if (Utils.isSong(audio.getSid())) {
			try {
				holder.title.setText(Utils.getTitleAndArtists(audio.getName(), CollectionUtils.toString(audio.getArrArtistName())));
			} catch (Exception e) {
				LogUtil.loge("mediaplayer::error:" + e.getMessage());
				holder.title.setText(audio.getName());
			}
		} else {
			holder.title.setText(audio.getName());
		}

		// // 如果是歌曲的话，则增加序号
		// if
		// (StringUtils.isNotEmpty(MusicFragment.getInstance().getCategoryId()))
		// {
		// int category =
		// Integer.parseInt(MusicFragment.getInstance().getCategoryId().split(",")[0]);
		// if (category / 100000 == 1) {// 音乐
		// holder.title.setText((position + 1) + ". " +
		// data.get(position).getName());
		// } else {
		// holder.title.setText(data.get(position).getName());
		// }
		// } else {
		// holder.title.setText((position + 1) + ". " +
		// data.get(position).getName());
		// }

		return convertView;
	}

	private class ViewHolder {
		TextView title;
	}

	private int currentPosition = 0;

	public void setCurrentPosition(int location) {
		LogUtil.logd("[MUSIC]set current position:" + location);
		currentPosition = location;
	}

}
