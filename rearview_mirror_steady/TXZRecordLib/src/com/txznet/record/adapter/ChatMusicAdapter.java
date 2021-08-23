package com.txznet.record.adapter;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.StringUtils;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.AudioInfo;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;
import com.txznet.txz.util.LanguageConvertor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ChatMusicAdapter extends ChatDisplayAdapter {

	public static class MusicItem extends DisplayItem<AudioInfo> {

	}

	public ChatMusicAdapter(Context context, List<MusicItem> displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_audio, parent, false);
			prepareSetLayoutParams(convertView);
		}

		final MusicItem musicItem = (MusicItem) getItem(position);
		final AudioInfo audioInfo = musicItem.mItem;

		TextView tvArtist = ViewHolder.get(convertView, R.id.tv_artist);
		TextView tvName = ViewHolder.get(convertView, R.id.tv_audio_name);
		TextView tvOrder = ViewHolder.get(convertView, R.id.tv_order);
		GradientProgressBar mPb = ViewHolder.get(convertView, R.id.my_progress);
		View mDivider = ViewHolder.get(convertView, R.id.divider);
		FrameLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
		ImageView lable1 = ViewHolder.get(convertView,R.id.iv_lable_1);
		ImageView lable2 = ViewHolder.get(convertView,R.id.iv_lable_2);
		ImageView lable3 = ViewHolder.get(convertView,R.id.iv_lable_3);	
		
		TextViewUtil.setTextSize(tvOrder,ViewConfiger.SIZE_AUDIO_INDEX_SIZE1);
		TextViewUtil.setTextColor(tvOrder,ViewConfiger.COLOR_AUDIO_INDEX_COLOR1);
		TextViewUtil.setTextSize(tvName,ViewConfiger.SIZE_AUDIO_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvName,ViewConfiger.COLOR_AUDIO_ITEM_COLOR1);
		TextViewUtil.setTextSize(tvArtist,ViewConfiger.SIZE_AUDIO_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvArtist,ViewConfiger.COLOR_AUDIO_ITEM_COLOR2);
		
		if (StringUtils.isEmpty(audioInfo.text)) {
			tvArtist.setVisibility(View.GONE);
		} else {
			tvArtist.setVisibility(View.VISIBLE);
			tvArtist.setText(LanguageConvertor.toLocale(audioInfo.text));
		}
		tvName.setText(LanguageConvertor.toLocale(audioInfo.title));
		tvOrder.setText(String.valueOf(position + 1));

		mPb.setVisibility(musicItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
		mPb.setProgress(musicItem.shouldWaiting ? musicItem.curPrg : 0);
		convertView.setTag(R.id.key_progress, mPb);

		mDivider.setVisibility(position == (getCount()-1)?View.INVISIBLE:View.VISIBLE);

		lable1.setVisibility(audioInfo.isLatest() ? View.VISIBLE : View.GONE);
		switch (audioInfo.getNovelStatus()) {
			case AudioInfo.NOVEL_STATUS_INVALID:
				lable2.setVisibility(View.GONE);
				break;
			case AudioInfo.NOVEL_STATUS_SERILIZE:
				lable2.setVisibility(View.VISIBLE);
				lable2.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.list_novel_serilize));
				break;
			case AudioInfo.NOVEL_STATUS_END:
				lable2.setVisibility(View.VISIBLE);
				lable2.setImageDrawable(GlobalContext.get().getResources().getDrawable(R.drawable.list_novel_end));
				break;
		}

		lable3.setVisibility(audioInfo.isLastPlay() ? View.VISIBLE : View.GONE);

		if (position == mFocusIndex) {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}
		
		return convertView;
	}
}
