package com.txznet.music.adpter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.dao.LocalAudioDBHelper;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.base.MyBaseAdpater;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.FileUtils;
import com.txznet.music.utils.Utils;

public class SongListAdapter extends MyBaseAdpater<Audio> {

	private RotateAnimation animation;
	private RotateAnimation backAnimation;
	int showPosition = -1;

	public SongListAdapter(List<Audio> data, Context ctx) {
		super(data, ctx);
		animation = new RotateAnimation(0, 180, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		backAnimation = new RotateAnimation(180, 0, Animation.RELATIVE_TO_SELF,
				0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
		animation.setFillAfter(true);
		animation.setDuration(200);
		backAnimation.setFillAfter(true);
		backAnimation.setDuration(200);
	}

	@Override
	public View getView(final int position, View convertView,
			final ViewGroup parent) {

		final ViewHolder holder;
		if (null == convertView) {
			holder = new ViewHolder();
			convertView = View.inflate(ctx, R.layout.item_song_list, null);
			holder.mTitle = (TextView) convertView.findViewById(R.id.title);
			holder.mArtist = (TextView) convertView.findViewById(R.id.artist);
			holder.mIv_show_delete = (ImageView) convertView
					.findViewById(R.id.iv_show_delete);
			holder.mLl_delete = (LinearLayout) convertView
					.findViewById(R.id.ll_delete);
			holder.tvDelete = (TextView) convertView
					.findViewById(R.id.tv_delete);
			holder.tvDelete.setText("删除音乐");
			holder.mTitle.setTypeface(Constant.typeFace);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		final Audio song = data.get(position);
		holder.mTitle.setText(Utils.getTitleAndArtists(song.getName(),
				CollectionUtils.toString(song.getArrArtistName())));
		if (showPosition == position) {
			holder.mLl_delete.setVisibility(View.VISIBLE);
			animation.setDuration(0);
			holder.mIv_show_delete.startAnimation(animation);
		} else {
			holder.mLl_delete.setVisibility(View.GONE);
			backAnimation.setDuration(0);
			holder.mIv_show_delete.startAnimation(backAnimation);
		}
		// SpannableString ss=new SpannableString(song.getName());

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
				data.remove(song);
				showPosition = -1;
				LogUtil.logd("currentAudio::"
						+ MediaPlayerActivityEngine.getInstance()
								.getCurrentAudio() + ",song::" + song);
				MediaPlayerActivityEngine.getInstance().removeAudio(song);
				ObserverManage.getObserver().send(
						InfoMessage.DELETE_LOCAL_MUSIC);
				SongListAdapter.this.notifyDataSetChanged();// 更新组件
				AppLogic.runOnSlowGround(new Runnable() {

					@Override
					public void run() {
						LogUtil.logd("delete::url::" + song.getStrDownloadUrl());
						try {
							if (song.getStrDownloadUrl().endsWith(".tmd")) {
								LocalAudioDBHelper.getInstance().remove(
										song.getStrDownloadUrl());
							} else {
								ctx.getContentResolver()
										.delete(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
												MediaStore.Audio.Media._ID
														+ "=" + song.getId(),
												null);
							}
						} catch (Exception e) {
							LogUtil.loge("delete opration have error ,this song is "
									+ song.toString());
						}
						FileUtils.removeDir(song.getStrDownloadUrl());
						LocalAudioDBHelper.getInstance().remove(
								song.getStrDownloadUrl().replaceAll("'",
										"&#39;"));
					}
				}, 0);
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
					if (lv.getLastVisiblePosition() == showPosition + 1
							|| lv.getLastVisiblePosition() == showPosition + 2) {
						// lv.setSelection(lv.getFirstVisiblePosition() + 1);
						lv.setSelection(lv.getLastVisiblePosition() - 1);
					} else {
						lv.setSelection(lv.getFirstVisiblePosition());
					}
					SongListAdapter.this.notifyDataSetInvalidated();// 更新组件
				}
			}
		});
		return convertView;
	}

	private class ViewHolder {
		private TextView mTitle;
		private TextView mArtist;
		private ImageView mIv_show_delete;
		private LinearLayout mLl_delete;
		private TextView tvDelete;
	}
}
