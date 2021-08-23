package com.txznet.music.adpter;

import java.util.ArrayList;
import java.util.List;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.Constant;
import com.txznet.music.R;
import com.txznet.music.bean.response.Audio;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;
import com.txznet.music.widget.NavListener;
import com.txznet.music.widget.NavListener.OnRefreshListener;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

public class NavPlayListAdapter extends RecyclerView.Adapter<ViewHolder> implements NavListener{

	private final String TAG = "Music:NavPlayListAdapter:";
	private List<Audio> mPlayList;
	private Context mCtx;
	private int mCurrentPlayPos;
	
	private OnItemClickListener mItemClickListener;

	public NavPlayListAdapter(Context context) {
		this.mCtx = context;
	}
	
	@Override
	public int getItemCount() {
		return mPlayList == null ? 0 : mPlayList.size();
	}

	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int position) {
		if (!(viewHolder instanceof NavPlayListViewHolder))
			return;
		final NavPlayListViewHolder holder = (NavPlayListViewHolder) viewHolder;
		if (mCurrentPlayPos == position) {
			holder.mTvTitle.setSelected(true);
			LogUtil.logd("[MUSIC]current position:" + position);
		} else {
			holder.mTvTitle.setSelected(false);
		}
		Audio audio = mPlayList.get(position);
		if (StringUtils.isEmpty(audio.getName())) {
			audio.setName("无");
		}

		if (Utils.isSong(audio.getSid())) {
			try {
				holder.mTvTitle.setText(Utils.getTitleAndArtists(audio.getName(), CollectionUtils.toString(audio.getArrArtistName())));
			} catch (Exception e) {
				LogUtil.loge(TAG + "mediaplayer::error:" + e.getMessage());
				holder.mTvTitle.setText(audio.getName());
			}
		} else {
			holder.mTvTitle.setText(audio.getName());
		}
		
		if(mNavFocusIndex == position){
			holder.mIvBg.setBackgroundResource(R.drawable.shape_focus_rect);
		}else{
			holder.mIvBg.setBackground(null);
		}
		
		if(mItemClickListener != null){
			holder.itemView.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					mItemClickListener.onItemClick(null, v, position, holder.itemView.getId());
				}
			});
		}
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View itemView = LayoutInflater.from(mCtx).inflate(R.layout.item_player_list, parent, false);
		return new NavPlayListViewHolder(itemView);
	}
	
	public void setCurrentPosition(int location) {
		LogUtil.logd(TAG + "set current position:" + location);
		mCurrentPlayPos = location;
	}
	
	public int getCurrentPosition(){
		return mCurrentPlayPos;
	}

	public void setData(List<Audio> data,boolean isAdd) {
		if (null == data) {
			return;
		}
		if (null == this.mPlayList) {
			mPlayList = new ArrayList<Audio>();
		}
		
		int index = -1;
		if (mNavFocusIndex >= 0 && mNavFocusIndex < mPlayList.size() && mNavFocusIndex < data.size()) {
			Audio audio = mPlayList.get(mNavFocusIndex);
			if (audio.getId() == data.get(mNavFocusIndex).getId()
					&& audio.getSid() == data.get(mNavFocusIndex).getSid()) {
				index = mNavFocusIndex;
			}
		}
		mNavFocusIndex = index;
		//避免java.lang.IndexOutOfBoundsException: Inconsistency detected. Invalid view holder adapter positionViewHolder{1f476bab position=24 id=-1, oldPos=-1, pLpos:-1 no parent}
		//http://www.jianshu.com/p/2eca433869e9
		int previousSize = mPlayList.size();
		if (isAdd) {
			mPlayList.addAll(data);
	        notifyItemRangeInserted(previousSize, data.size());
		}else{
			mPlayList.clear();
	        notifyItemRangeRemoved(0, previousSize);
	        mPlayList.addAll(data);
	        notifyItemRangeInserted(0, mPlayList.size());
		}
	}
	
	public void setOnItemClickListener(OnItemClickListener listener) {
		this.mItemClickListener = listener;
	}
	
	public static class NavPlayListViewHolder extends ViewHolder {

		private TextView mTvTitle;
		private ImageView mIvBg;

		public NavPlayListViewHolder(View itemView) {
			super(itemView);

			mTvTitle = (TextView) itemView.findViewById(R.id.title);
			mIvBg = (ImageView) itemView.findViewById(R.id.iv_bg);
			mTvTitle.setTypeface(Constant.typeFace);
		}

	}

	
	private int mNavFocusIndex = -1;
	private boolean isFocus = false;
	private OnRefreshListener mRefreshListener = null;
	private OnRefreshListener mScanListener = null;

	@Override
	public int onNext() {
		if (mNavFocusIndex < mPlayList.size() - 1) {
			notifyItemChanged(mNavFocusIndex);
			mNavFocusIndex++;
			notifyItemChanged(mNavFocusIndex);
		}
		if (mNavFocusIndex == mPlayList.size() - 1 && mRefreshListener != null) {
			mRefreshListener.onRefresh(mNavFocusIndex);
		}
		return mNavFocusIndex;
	}

	@Override
	public int onPrev() {
		if (mNavFocusIndex > 0) {
			notifyItemChanged(mNavFocusIndex);
			mNavFocusIndex--;
			notifyItemChanged(mNavFocusIndex);
		}
		return mNavFocusIndex;
	}

	@Override
	public void onClick() {
		if(mNavFocusIndex >= 0 && mNavFocusIndex < mPlayList.size()){
			mItemClickListener.onItemClick(null, null, mNavFocusIndex, 0);
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
}
