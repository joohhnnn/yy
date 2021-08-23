package com.txznet.music.adpter;

import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.fm.dao.HistoryAudioDBHelper;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.engine.MediaPlayerActivityEngine;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.widget.NavListener;
import com.txznet.music.widget.NavListener.OnRefreshListener;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class LocalAudioAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements NavListener {

	private boolean isShowHeaderView = false;
	private List<Audio> mAudios;
	private Context mCtx;
	private int showPosition = -1;
	private static final int ITEM_TYPE_NORMAL = 0;
	private static final int ITEM_TYPE_HEADER = 1;

	public LocalAudioAdapter(List<Audio> audios, Context ctx) {
		this.mAudios = audios;
		this.mCtx = ctx;
	}

	private OnItemClickListener mDeleteListener;
	private OnItemClickListener mItemClickListener;

	public void setOnItemDeleteListener(OnItemClickListener deleteListener) {
		this.mDeleteListener = deleteListener;
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}

	public static class AudioViewHolder extends ViewHolder {
		private TextView mTitle;
		private TextView mTvLocalTag;
		private TextView mArtist;
		private ImageView mIv_show_delete;
		private LinearLayout mLl_delete;
		private RelativeLayout mLl_item;
		private RelativeLayout mRl_song;
		private ImageView mIvBg;

		public AudioViewHolder(View itemView) {
			super(itemView);
			mTitle = (TextView) itemView.findViewById(R.id.title);
			mTvLocalTag = (TextView) itemView.findViewById(R.id.tv_local_tag);
			mArtist = (TextView) itemView.findViewById(R.id.artist);
			mIv_show_delete = (ImageView) itemView.findViewById(R.id.iv_show_delete);
			mLl_delete = (LinearLayout) itemView.findViewById(R.id.ll_delete);
			mTitle.setTypeface(Constant.typeFace);
			mLl_item = (RelativeLayout) itemView.findViewById(R.id.ll_item);
			mRl_song = (RelativeLayout) itemView.findViewById(R.id.rl_song);
			mIvBg = (ImageView) itemView.findViewById(R.id.iv_bg);
		}

	}

	public static class ScanViewHolder extends ViewHolder {
		private TextView mTvRefreshTotal;
		private TextView tvRefresh;

		public ScanViewHolder(View itemView) {
			super(itemView);
			mTvRefreshTotal = (TextView) itemView.findViewById(R.id.refresh_total_tv);
			tvRefresh = (TextView) itemView.findViewById(R.id.refreshTv);
		}

	}

	@Override
	public int getItemCount() {
		int count = isShowHeaderView ? 1 : 0;
		return (mAudios == null || mAudios.isEmpty()) ? 0 : mAudios.size() + count;
	}

	public void showHeadView(boolean isShow) {
		this.isShowHeaderView = isShow;
	}

	private int getRealPosition(ViewHolder holder) {
		int position = holder.getPosition();
		return isShowHeaderView ? position - 1 : position;
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int pos) {
		if (viewHolder instanceof ScanViewHolder) {
			ScanViewHolder scanViewHolder = (ScanViewHolder) viewHolder;
			int size = mAudios == null ? 0 : mAudios.size();
			scanViewHolder.mTvRefreshTotal.setText(mCtx.getString(R.string.total_song, size));

			if (isFocus && mNavFocusIndex == pos) {
				scanViewHolder.tvRefresh.setBackgroundResource(R.drawable.shape_focus_rect);
			} else {
				scanViewHolder.tvRefresh.setBackground(null);
			}

			if (mScanListener != null) {
				scanViewHolder.tvRefresh.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						mScanListener.onRefresh(pos);
					}
				});
			}

		} else {
			final AudioViewHolder holder = (AudioViewHolder) viewHolder;
			final int position = getRealPosition(holder);
			final Audio song = mAudios.get(position);

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
				// animation.setDuration(0);
				// holder.mIv_show_delete.startAnimation(animation);
			} else {
				holder.mLl_delete.setVisibility(View.GONE);
				// backAnimation.setDuration(0);
				// holder.mIv_show_delete.startAnimation(backAnimation);
			}
			if (MediaPlayerActivityEngine.getInstance().getCurrentAudio() != null
					&& MediaPlayerActivityEngine.getInstance().getCurrentAudio().getSid() == song.getSid()
					&& MediaPlayerActivityEngine.getInstance().getCurrentAudio().getId() == song.getId()) {
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

					if (mDeleteListener != null) {
						// 威仕特界面会自行删除数据和执行操作。
						mDeleteListener.onItemClick(null, v, position, holder.mLl_delete.getId());
					} else {
						mAudios.remove(song);
						HistoryAudioDBHelper.getInstance().remove(song.getId(), song.getName());
						notifyDataSetChanged();// 更新组件
					}
				}
			});
			// 设置为是否可见
			holder.mIv_show_delete.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (holder.mLl_delete.getVisibility() == View.VISIBLE) {
						holder.mLl_delete.setVisibility(View.GONE);
						// backAnimation.setDuration(200);
						// holder.mIv_show_delete.startAnimation(backAnimation);
						showPosition = -1;
					} else {
						holder.mLl_delete.setVisibility(View.VISIBLE);
						// animation.setDuration(200);
						// holder.mIv_show_delete.startAnimation(animation);
						showPosition = mAudios.indexOf(song);
						// ListView lv = ((ListView) parent);
						// if (lv.getLastVisiblePosition() == showPosition
						// || lv.getLastVisiblePosition() == showPosition + 1) {
						// // lv.setSelection(lv.getFirstVisiblePosition() + 1);
						// lv.setSelection(lv.getLastVisiblePosition() - 1);
						// } else {
						// lv.setSelection(lv.getFirstVisiblePosition());
						// }
						notifyDataSetChanged();// 更新组件
					}
				}
			});

			if (isFocus && mNavFocusIndex == pos) {
				holder.mIvBg.setBackgroundResource(R.drawable.shape_focus_rect);
			} else {
				holder.mIvBg.setBackground(null);
			}

			holder.mRl_song.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					mItemClickListener.onItemClick(null, null, position, 0);
				}
			});
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		ViewHolder holder = null;
		if (viewType == ITEM_TYPE_HEADER) {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ll_song_local_scan, parent, false);
			holder = new ScanViewHolder(view);
		} else {
			View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song_list_1, parent, false);
			holder = new AudioViewHolder(view);
		}
		return holder;
	}

	@Override
	public int getItemViewType(int position) {
		if (isShowHeaderView && position == 0) {
			return ITEM_TYPE_HEADER;
		}
		return ITEM_TYPE_NORMAL;
	}

	private int mNavFocusIndex = -1;
	private boolean isFocus = false;
	private OnRefreshListener mRefreshListener = null;
	private OnRefreshListener mScanListener = null;

	@Override
	public int onNext() {
		if (mNavFocusIndex < getItemCount() - 1) {
			mNavFocusIndex++;
			notifyDataSetChanged();
		}
		if (mNavFocusIndex == mAudios.size() - 1 && mRefreshListener != null) {
			mRefreshListener.onRefresh(mNavFocusIndex);
		}
		return mNavFocusIndex;
	}

	@Override
	public int onPrev() {
		if (mNavFocusIndex > 0) {
			mNavFocusIndex--;
			notifyDataSetChanged();
		}
		return mNavFocusIndex;
	}

	@Override
	public void onClick() {
		if (mNavFocusIndex >= 0 && mNavFocusIndex < getItemCount()) {
			if (isShowHeaderView && mNavFocusIndex == 0) {
				mScanListener.onRefresh(mNavFocusIndex);
			} else {
				int position = isShowHeaderView ? mNavFocusIndex - 1 : mNavFocusIndex;
				mItemClickListener.onItemClick(null, null, position, 0);
			}
		}
	}

	@Override
	public void setFocus(boolean isFocus) {
		this.isFocus = isFocus;
		if (isFocus && mNavFocusIndex == -1) {
			mNavFocusIndex = 0;
		}
		notifyDataSetChanged();
	}

	public void setOnRefreshListener(OnRefreshListener listener) {
		this.mRefreshListener = listener;
	}

	public void setOnScanListener(OnRefreshListener listener) {
		this.mScanListener = listener;
	}
}
