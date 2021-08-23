package com.txznet.record.adapter;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.AudioBean;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ChatAudioAdapter extends ChatDisplayAdapter {

	public static class AudioItem extends DisplayItem<AudioBean> {

	}

	public ChatAudioAdapter(Context context, List<AudioItem> displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.audio_item_ly, parent, false);
			prepareSetLayoutParams(convertView);
		}

		final AudioItem audioItem = (AudioItem) getItem(position);
		final AudioBean music = audioItem.mItem;

		TextView num = ViewHolder.get(convertView, R.id.txtNum);
		TextView mAudioName = ViewHolder.get(convertView, R.id.audio_name_tv);
		TextView mAuthor = ViewHolder.get(convertView, R.id.author_tv);
		TextView mSourceTv = ViewHolder.get(convertView, R.id.source_tv);
		GradientProgressBar mPb = ViewHolder.get(convertView, R.id.my_progress);
		FrameLayout mLayoutItem = ViewHolder.get(convertView, R.id.layout_item);
		
		View mDivider = ViewHolder.get(convertView, R.id.divider); 

		num.setText(String.valueOf(position + 1));
		mAudioName.setText(music.mAudioName);
		mAudioName.setTextColor(Color.WHITE);
		if (TextUtils.isEmpty(music.mAuthorName) || "null".equals(music.mAuthorName)) {
			mAuthor.setVisibility(View.INVISIBLE);
		} else {
			mAuthor.setVisibility(View.VISIBLE);
			mAuthor.setText(music.mAuthorName);
		}
		if (TextUtils.isEmpty(music.mSourceName) || "null".equals(music.mSourceName)) {
			mSourceTv.setVisibility(View.INVISIBLE);
		} else {
			mSourceTv.setVisibility(View.VISIBLE);
			mSourceTv.setText(music.mSourceName);
		}

		mPb.setVisibility(audioItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
		mPb.setProgress(audioItem.shouldWaiting ? audioItem.curPrg : 0);
		convertView.setTag(R.id.key_progress, mPb);

		mDivider.setVisibility(position == (getCount()-1)?View.INVISIBLE:View.VISIBLE);
        if (position == mFocusIndex) {
        	mLayoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			mLayoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}
		return convertView;
	}
}