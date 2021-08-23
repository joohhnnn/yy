package com.txznet.record.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.TtsThemeInfo;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;
import com.txznet.txz.util.LanguageConvertor;

public class ChatTtsThemeAdapter extends ChatDisplayAdapter {

	public static class TtsThemeItem extends DisplayItem<TtsThemeInfo> {

	}

	public ChatTtsThemeAdapter(Context context, List<TtsThemeItem> displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_tts_theme, parent, false);
			prepareSetLayoutParams(convertView);
		}

		final TtsThemeItem item = (TtsThemeItem) getItem(position);
		final TtsThemeInfo info = item.mItem;
		
		TextView tvName = ViewHolder.get(convertView, R.id.tv_theme_name);
		TextView tvOrder = ViewHolder.get(convertView, R.id.tv_order);
		GradientProgressBar mPb = ViewHolder.get(convertView, R.id.my_progress);
		View mDivider = ViewHolder.get(convertView, R.id.divider);
		FrameLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
		
		TextViewUtil.setTextColor(tvName,ViewConfiger.COLOR_TTS_ITEM_COLOR1);
		TextViewUtil.setTextSize(tvName,ViewConfiger.SIZE_TTS_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvOrder,ViewConfiger.COLOR_TTS_INDEX_COLOR1);
		TextViewUtil.setTextSize(tvOrder,ViewConfiger.SIZE_TTS_INDEX_SIZE1);

		tvName.setText(LanguageConvertor.toLocale(info.name));
		//更多主题
		if (info.id == -1){
			tvName.setTextColor(Color.BLUE);
		}else{
			tvName.setTextColor(Color.WHITE);
		}
		
		tvOrder.setText(String.valueOf(position + 1));

		mPb.setVisibility(item.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
		mPb.setProgress(item.shouldWaiting ? item.curPrg : 0);
		mDivider.setVisibility(position == ConfigUtil.getVisbileCount() - 1 ?View.INVISIBLE:View.VISIBLE);
		convertView.setTag(R.id.key_progress, mPb);
		
        if (position == mFocusIndex) {
        	layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}
        
		return convertView;
	}
}
