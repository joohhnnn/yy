package com.txznet.record.adapter;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.SimRechargeInfo;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;

public class ChatSimRechargeAdapter extends ChatDisplayAdapter {

	public ChatSimRechargeAdapter(Context context, List displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(null == convertView){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_sim_recharge, parent, false);
			prepareSetLayoutParams(convertView);
		}
		
		// bind views
		TextView mTvIndex = ViewHolder.get(convertView, R.id.tv_item_sim_recharge_index);
		TextView mTvTitle = ViewHolder.get(convertView, R.id.tv_item_sim_recharge_title);
		TextView mTvPrice = ViewHolder.get(convertView, R.id.tv_item_sim_recharge_price);
		TextView mTvPriceRaw = ViewHolder.get(convertView, R.id.tv_item_sim_recharge_price_raw);
		View mDivider = ViewHolder.get(convertView, R.id.divider);
		FrameLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
		
		TextViewUtil.setTextSize(mTvIndex,ViewConfiger.SIZE_SIM_INDEX_SIZE1);
		TextViewUtil.setTextColor(mTvIndex,ViewConfiger.COLOR_SIM_INDEX_COLOR1);
		TextViewUtil.setTextSize(mTvTitle,ViewConfiger.SIZE_SIM_ITEM_SIZE1);
		TextViewUtil.setTextColor(mTvTitle,ViewConfiger.COLOR_SIM_ITEM_COLOR1);
		TextViewUtil.setTextSize(mTvPrice,ViewConfiger.SIZE_SIM_ITEM_SIZE2);
		TextViewUtil.setTextColor(mTvPrice,ViewConfiger.COLOR_SIM_ITEM_COLOR2);
		TextViewUtil.setTextSize(mTvPriceRaw,ViewConfiger.SIZE_SIM_ITEM_SIZE3);
		TextViewUtil.setTextColor(mTvPriceRaw,ViewConfiger.COLOR_SIM_ITEM_COLOR3);
		
		// get data source
		final SimRechargeItem simRechargeItem = (SimRechargeItem) getItem(position);
		final SimRechargeInfo simRechargeInfo = simRechargeItem.mItem;
		
		// bind data
		mTvIndex.setText(position + 1 + "");
		mTvTitle.setText(simRechargeInfo.mName);
		mTvPrice.setText("￥" + simRechargeInfo.mPrice / 100.0);
		mTvPriceRaw.setText("￥" + simRechargeInfo.mPriceRaw / 100.0);
		mDivider.setVisibility(position == (getCount() -1)?View.INVISIBLE:View.VISIBLE);
        if (position == mFocusIndex) {
        	layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}
		return convertView;
	}

	public static class SimRechargeItem extends DisplayItem<SimRechargeInfo> {
		
	}

}
