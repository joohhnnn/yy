package com.txznet.music.adpter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.adpter.base.MyBaseAdpater;
import com.txznet.music.bean.HistoryAudio;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.widget.NavListener;
import com.txznet.music.widget.NavListener.OnRefreshListener;

public class HistoryAudioAdapter extends MyBaseAdpater<Audio> implements NavListener{

	private RotateAnimation animation;
	private RotateAnimation backAnimation;
	private OnItemClickListener itemListener;

	public HistoryAudioAdapter(List<Audio> data, Context ctx) {
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
			convertView = View.inflate(ctx, R.layout.item_song_list_1, null);
			holder.mTitle = (TextView) convertView.findViewById(R.id.title);
			holder.mTvLocalTag = (TextView) convertView
					.findViewById(R.id.tv_local_tag);
			holder.mArtist = (TextView) convertView.findViewById(R.id.artist);
			holder.mIv_show_delete = (ImageView) convertView
					.findViewById(R.id.iv_show_delete);
			holder.mLl_delete = (LinearLayout) convertView
					.findViewById(R.id.ll_delete);
			holder.mTitle.setTypeface(Constant.typeFace);
			holder.mLl_item = (LinearLayout) convertView.findViewById(R.id.ll_item);
			holder.mRl_song = (RelativeLayout) convertView.findViewById(R.id.rl_song);
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
		holder.mTitle.setText(song.getName());

		holder.mArtist.setText(StringUtils.toString(song.getArrArtistName()));

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
				
				if (deleteListener != null) {
					//威仕特界面会自行删除数据和执行操作。
					deleteListener.onItemClick(null, v, position,
							holder.mLl_delete.getId());
				} else {
					data.remove(song);
//					HistoryAudioDBHelper.getInstance().remove(song.getId(),song.getSid());
					HistoryAudioDBHelper.getInstance().remove(song.getId(),song.getName());
					// if (Constant.HISTORY_TYPE == SharedPreferencesUtils
					// .getAudioSource()) {
					// MediaPlayerActivityEngine.getInstance().removeAudio(song);
					// }
					HistoryAudioAdapter.this.notifyDataSetChanged();// 更新组件
				}
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
					HistoryAudioAdapter.this.notifyDataSetInvalidated();// 更新组件
				}
			}
		});
		
		if(isFocus && mNavFocusIndex == position){
			holder.mRl_song.setBackgroundResource(R.drawable.shape_focus_rect);
			mHeight = holder.mLl_item.getHeight();
		}else{
			holder.mRl_song.setBackground(null);
		}
		
		holder.mRl_song.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				itemListener.onItemClick(null, null, position, 0);
			}
		});

		return convertView;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.itemListener = listener;
	}
	
	private OnItemClickListener deleteListener;

	public void setOnItemDeleteListener(OnItemClickListener deleteListener) {
		this.deleteListener = deleteListener;
	}

	private class ViewHolder {
		private TextView mTitle;
		private TextView mTvLocalTag;
		private TextView mArtist;
		private ImageView mIv_show_delete;
		private LinearLayout mLl_delete;
		private LinearLayout mLl_item;
		private RelativeLayout mRl_song;
	}

	private int mHeight = 0;
	private int mNavFocusIndex = -1;
	private boolean isFocus = false;
	private OnRefreshListener mRefreshListener = null;
	private int offset = 0;
	
	public void setOffset(int offset){
		this.offset = offset;
	}
	
	@Override
	public int onNext() {
		if(mNavFocusIndex < data.size() - 1){
			mNavFocusIndex++;
//			notifyDataSetChanged();
		}
		if(mNavFocusIndex == data.size() - 1 && mRefreshListener != null){
			mRefreshListener.onRefresh(mNavFocusIndex);
		}
		return mNavFocusIndex + offset;
	}

	@Override
	public int onPrev() {
		if(mNavFocusIndex > 0){
			mNavFocusIndex--;
//			notifyDataSetChanged();
		}
		return mNavFocusIndex + offset;
	}

	@Override
	public void onClick() {
		if(mNavFocusIndex >= 0 && mNavFocusIndex < data.size()){
			itemListener.onItemClick(null, null, mNavFocusIndex, 0);
		}
	}

	@Override
	public void setFocus(boolean isFocus) {
		this.isFocus = isFocus;
		if(isFocus && mNavFocusIndex == -1){
			mNavFocusIndex = 0;
		}
		notifyDataSetChanged();
	}
	
	public void setOnRefreshListener(OnRefreshListener listener){
		this.mRefreshListener = listener;
	}
}
