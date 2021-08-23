package com.txznet.music.adpter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.base.MyBaseAdpater;
import com.txznet.music.bean.HistoryAudio;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.Utils;

public class SongListHistoryAdapter extends MyBaseAdpater<Audio> {

	private RotateAnimation animation;
	private RotateAnimation backAnimation;

	public SongListHistoryAdapter(List<Audio> data, Context ctx, int type) {
		super(data, ctx);
		// typeFace = Typeface.createFromAsset(ctx.getAssets(),
		// "fonts/DroidSansFallback.ttf");
		animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		backAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setFillAfter(true);
		animation.setDuration(200);
		backAnimation.setFillAfter(true);
		backAnimation.setDuration(200);
	}

	int showPosition = -1;

	// private Typeface typeFace;

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {
		final ViewHolder holder;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(ctx, R.layout.item_song_list, null);
			holder.mTitle = (TextView) convertView.findViewById(R.id.title);
			holder.mTvLocalTag = (TextView) convertView
					.findViewById(R.id.tv_local_tag);
			holder.mArtist = (TextView) convertView.findViewById(R.id.artist);
			holder.mIv_show_delete = (ImageView) convertView
					.findViewById(R.id.iv_show_delete);
			holder.mLl_delete = (LinearLayout) convertView
					.findViewById(R.id.ll_delete);
			holder.mTitle.setTypeface(Constant.typeFace);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Audio song = data.get(position);

		if (song.getSid() == 0) {
			holder.mTvLocalTag.setVisibility(View.VISIBLE);
		} else {
			holder.mTvLocalTag.setVisibility(View.GONE);
		}
		holder.mTitle.setText(Utils.getTitleAndArtists(song.getName(),
				CollectionUtils.toString(song.getArrArtistName())));
		holder.mLl_delete.setVisibility(View.GONE);
		if (showPosition == position) {
			holder.mLl_delete.setVisibility(View.VISIBLE);
			animation.setDuration(0);
			holder.mIv_show_delete.startAnimation(animation);
		} else {
			holder.mLl_delete.setVisibility(View.GONE);
			backAnimation.setDuration(0);
			holder.mIv_show_delete.startAnimation(backAnimation);
		}
		if (MediaPlayerActivityEngine.getInstance().getCurrentAudio() != null
				&& MediaPlayerActivityEngine.getInstance().getCurrentAudio()
						.getSid() == song.getSid()
				&& MediaPlayerActivityEngine.getInstance().getCurrentAudio()
						.getId() == song.getId()) {
			holder.mTitle.setTextColor(Color.parseColor("#1cc859"));
			holder.mTitle.setMarqueeRepeatLimit(-1);
		} else {
			holder.mTitle.setTextColor(Color.parseColor("#FFFFFF"));
			holder.mTitle.setMarqueeRepeatLimit(0);
		}
		// 删除该条记录
		holder.mLl_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showPosition = -1;
				data.remove(song);
//				HistoryAudioDBHelper.getInstance().remove(song.getId(),song.getSid());
				HistoryAudioDBHelper.getInstance().remove(song.getId(),song.getName());
				SongListHistoryAdapter.this.notifyDataSetChanged();// 更新组件
//				if (Constant.HISTORY_TYPE == SharedPreferencesUtils
//						.getAudioSource()) {
//					MediaPlayerActivityEngine.getInstance().removeAudio(song);
//				}
			}
		});
		// 设置为是否可见
		holder.mIv_show_delete.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (holder.mLl_delete.getVisibility() == View.VISIBLE) {
					holder.mLl_delete.setVisibility(View.GONE);
					backAnimation.setDuration(200);
					holder.mIv_show_delete.startAnimation(backAnimation);
					showPosition = -1;
				} else {
					holder.mLl_delete.setVisibility(View.VISIBLE);
					animation.setDuration(200);
					holder.mIv_show_delete.startAnimation(animation);
					showPosition = data.indexOf(song);
					ListView lv = ((ListView) parent);
					if (lv.getLastVisiblePosition() == showPosition
							|| lv.getLastVisiblePosition() == showPosition + 1) {
						// lv.setSelection(lv.getFirstVisiblePosition() + 1);
						lv.setSelection(lv.getLastVisiblePosition() - 1);
					} else {
						lv.setSelection(lv.getFirstVisiblePosition());
					}
					SongListHistoryAdapter.this.notifyDataSetInvalidated();// 更新组件
				}
			}
		});

		return convertView;
	}

	private class ViewHolder {
		private TextView mTitle;
		private TextView mTvLocalTag;
		private TextView mArtist;
		private ImageView mIv_show_delete;
		private LinearLayout mLl_delete;
	}
}
