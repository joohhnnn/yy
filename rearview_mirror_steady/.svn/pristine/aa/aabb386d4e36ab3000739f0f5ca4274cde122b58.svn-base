package com.txznet.record.adapter;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.TrainInfo;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;

import java.util.List;

public class ChatTrainAdapter extends ChatDisplayAdapter {

    public ChatTrainAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_train, parent, false);
            prepareSetLayoutParams(convertView);
        }
        TrainItem item = (TrainItem) getItem(position);
        TrainInfo info = item.mItem;
        if (info == null) {
            return convertView;
        }
        TextView tvTrainNo = ViewHolder.get(convertView, R.id.tv_item_train_trainno);
        tvTrainNo.setText(info.trainNo);
        TextView tvTime = ViewHolder.get(convertView, R.id.tv_item_train_time);
        tvTime.setText(info.time);

        TextView tvDepartTime = ViewHolder.get(convertView, R.id.tv_item_train_departtime);
        tvDepartTime.setText(info.departureTime);
        TextView tvArrivalTime = ViewHolder.get(convertView, R.id.tv_item_train_arrivaltime);
        tvArrivalTime.setText(info.arrivalTime);

        TextView tvDepartureStation = ViewHolder.get(convertView, R.id.tv_item_train_departstation);
        SpannableString departureString = new SpannableString("始 " + info.departureStation);
        departureString.setSpan(new RelativeSizeSpan(0.7f), 0 , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        departureString.setSpan(new ForegroundColorSpan(Color.parseColor("#80FFFFFF")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvDepartureStation.setText(departureString);
        TextView tvArrivalStation = ViewHolder.get(convertView, R.id.tv_item_train_arrivalstation);
        SpannableString arrivalString = new SpannableString("终 " + info.arrivalStation);
        arrivalString.setSpan(new RelativeSizeSpan(0.7f), 0 , 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        arrivalString.setSpan(new ForegroundColorSpan(Color.parseColor("#80FFFFFF")), 0, 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvArrivalStation.setText(arrivalString);

        TextView tvPrice = ViewHolder.get(convertView, R.id.tv_item_train_price);
        String price = "¥" + info.minPrice + " 起";
        SpannableString priceString = new SpannableString(price);
        priceString.setSpan(new RelativeSizeSpan(0.6f), price.length() -1 , price.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvPrice.setText(priceString);

        TextView tvSeat0 = ViewHolder.get(convertView, R.id.tv_item_train_seat0);
        tvSeat0.setText(getSeatInfo(0, info));
        TextView tvSeat2 = ViewHolder.get(convertView, R.id.tv_item_train_seat2);
        tvSeat2.setText(getSeatInfo(2, info));
        TextView tvSeat1 = ViewHolder.get(convertView, R.id.tv_item_train_seat1);
        tvSeat1.setText(getSeatInfo(1, info));
        TextView tvSeat3 = ViewHolder.get(convertView, R.id.tv_item_train_seat3);
        tvSeat3.setText(getSeatInfo(3, info));

        View mDivider = ViewHolder.get(convertView, R.id.divider);
        mDivider.setVisibility(position == (getCount() - 1) ? View.INVISIBLE : View.VISIBLE);

        LinearLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
        if (position == mFocusIndex) {
            layoutItem.setBackgroundColor(GlobalContext.get().getResources()
                    .getColor(R.color.bg_ripple_focused));
        } else {
            layoutItem.setBackgroundColor(GlobalContext.get().getResources()
                    .getColor(R.color.bg_ripple_nor));
        }

        return convertView;
    }

    private String getSeatInfo(int index, TrainInfo info) {
        String string = "";
        if (index < info.trainSeats.size()) {
            TrainInfo.TrainSeatsBean seat = info.trainSeats.get(index);
            string = seat.seatName + ":";
            string += seat.ticketsRemainingNumer > 0 ? String.valueOf(seat.ticketsRemainingNumer) : "无";
        }
        return string;
    }

    public static class TrainItem extends DisplayItem<TrainInfo> {

    }

}
