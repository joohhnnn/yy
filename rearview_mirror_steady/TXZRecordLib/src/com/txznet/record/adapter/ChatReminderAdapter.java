package com.txznet.record.adapter;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.ReminderInfo;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

public class ChatReminderAdapter extends ChatDisplayAdapter{

	public ChatReminderAdapter(Context context, List displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_reminder, parent, false);
			prepareSetLayoutParams(convertView);
		}
		TextView mTvIndex = ViewHolder.get(convertView, R.id.tv_item_reminder_index);
		TextView mTvContent = ViewHolder.get(convertView, R.id.tv_item_reminder_content);
		TextView mTvTime = ViewHolder.get(convertView, R.id.tv_item_reminder_time);
		TextView mTvPosition = ViewHolder.get(convertView, R.id.tv_item_reminder_position);
		TextView mTvTimeFull = ViewHolder.get(convertView, R.id.tv_reminder_time_full);
		ImageView mIvTime = ViewHolder.get(convertView, R.id.iv_time_icon);
		ImageView mIvPosition = ViewHolder.get(convertView, R.id.iv_position_icon);
		View mDivider = ViewHolder.get(convertView, R.id.divider);
		FrameLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
		
		TextViewUtil.setTextSize(mTvIndex, ViewConfiger.SIZE_REMINDER_INDEX_SIZE1);
		TextViewUtil.setTextColor(mTvIndex, ViewConfiger.COLOR_REMINDER_INDEX_COLOR1);
		TextViewUtil.setTextSize(mTvContent, ViewConfiger.SIZE_REMINDER_ITEM_SIZE1);
		TextViewUtil.setTextColor(mTvContent, ViewConfiger.COLOR_REMINDER_INDEX_COLOR1);
		TextViewUtil.setTextSize(mTvTime, ViewConfiger.SIZE_REMINDER_ITEM_SIZE2);
		TextViewUtil.setTextColor(mTvTime, ViewConfiger.COLOR_REMINDER_ITEM_COLOR2);
		TextViewUtil.setTextSize(mTvTimeFull, ViewConfiger.SIZE_REMINDER_ITEM_SIZE2);
		TextViewUtil.setTextSize(mTvPosition, ViewConfiger.SIZE_REMINDER_ITEM_SIZE3);
		TextViewUtil.setTextColor(mTvPosition, ViewConfiger.COLOR_REMINDER_ITEM_COLOR3);
		
		final ReminderItem item = (ReminderItem) getItem(position);
		final ReminderInfo info = item.mItem;
		
		mTvIndex.setText(position + 1 + "");
		mTvContent.setText(info.mContent);
		if(!TextUtils.isEmpty(info.time)){
			mTvTime.setText(info.time);
			mIvTime.setVisibility(View.VISIBLE);
			mTvTime.setVisibility(View.VISIBLE);
		}else{
			mIvTime.setVisibility(View.INVISIBLE);
			mTvTime.setVisibility(View.INVISIBLE);
		}
		if(!TextUtils.isEmpty(info.position)){
			mTvPosition.setText(info.position);
			mTvPosition.setVisibility(View.VISIBLE);
			mIvPosition.setVisibility(View.VISIBLE);
		}else{
			mIvPosition.setVisibility(View.INVISIBLE);
			mTvPosition.setVisibility(View.INVISIBLE);
		}
		mDivider.setVisibility(position == ConfigUtil.getVisbileCount() - 1 ? View.INVISIBLE : View.VISIBLE);
		if (position == mFocusIndex) {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources()
					.getColor(R.color.bg_ripple_focused));
		} else {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources()
					.getColor(R.color.bg_ripple_nor));
		}
		return convertView;
	}
	
	public static class ReminderItem extends DisplayItem<ReminderInfo>{
		
	}

}
