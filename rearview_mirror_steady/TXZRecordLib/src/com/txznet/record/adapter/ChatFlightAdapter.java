package com.txznet.record.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.TextUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.FlightInfo;
import com.txznet.record.bean.ReminderInfo;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;

import java.util.List;

public class ChatFlightAdapter extends ChatDisplayAdapter{

	public ChatFlightAdapter(Context context, List displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if(convertView == null){
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_flight, parent, false);
			prepareSetLayoutParams(convertView);
		}
		TextView tvAirLine = ViewHolder.get(convertView, R.id.tv_item_flight_airline);
		TextView tvFlightNo = ViewHolder.get(convertView, R.id.tv_item_flight_flightno);
		TextView tvDepartName = ViewHolder.get(convertView, R.id.tv_item_flight_departName);
		TextView tvArrivalName = ViewHolder.get(convertView, R.id.tv_item_flight_arrivalName);
		TextView tvDepartTime = ViewHolder.get(convertView, R.id.tv_item_flight_departTime);
		TextView tvArrivalTime = ViewHolder.get(convertView, R.id.tv_item_flight_arrivalTime);
		TextView tvTicketCount = ViewHolder.get(convertView, R.id.tv_item_flight_count);
		TextView tvDiscount = ViewHolder.get(convertView, R.id.tv_item_flight_discount);
		TextView tvTicketPrice = ViewHolder.get(convertView, R.id.tv_item_flight_price);
		TextView tvSeat = ViewHolder.get(convertView, R.id.tv_item_flight_seat);
		TextView tvAddDate = ViewHolder.get(convertView, R.id.tv_item_flight_adddate);

		View mDivider = ViewHolder.get(convertView, R.id.divider);
		FrameLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
		
		final FlightItem item = (FlightItem) getItem(position);
		final FlightInfo info = item.mItem;
		if(info != null){
			if(!TextUtils.isEmpty(info.airline)){
				tvAirLine.setText(info.airline);
			}
			if(!TextUtils.isEmpty(info.flightNo)){
				tvFlightNo.setText(info.flightNo);
			}
			if(!TextUtils.isEmpty(info.departAirportName)){
				tvDepartName.setText(info.departAirportName);
			}
			if(!TextUtils.isEmpty(info.departTimeHm)){
				tvDepartTime.setText(info.departTimeHm);
			}
			if(!TextUtils.isEmpty(info.arrivalAirportName)){
				tvArrivalName.setText(info.arrivalAirportName);
			}
			if(!TextUtils.isEmpty(info.arrivalTimeHm)){
				tvArrivalTime.setText(info.arrivalTimeHm);
			}
			if(!TextUtils.isEmpty(info.economyCabinDiscount)){
				tvDiscount.setText(info.economyCabinDiscount + "折");
			}
			tvTicketPrice.setText("¥" + info.economyCabinPrice);
			tvTicketCount.setText(info.ticketCount + " 张");
			tvAddDate.setText(info.addDate);
		}

		mDivider.setVisibility(position == (getCount() - 1) ? View.INVISIBLE : View.VISIBLE);
		if (position == mFocusIndex) {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources()
					.getColor(R.color.bg_ripple_focused));
		} else {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources()
					.getColor(R.color.bg_ripple_nor));
		}
		return convertView;
	}
	
	public static class FlightItem extends DisplayItem<FlightInfo>{
		
	}

}
