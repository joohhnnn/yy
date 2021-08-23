package com.txznet.record.adapter;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.PhoneContact;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.TextView;

public class ChatConsAdapter extends ChatDisplayAdapter {
	private Context mContext;
	
	public static class ContactItem extends DisplayItem<PhoneContact> {

	}

	public ChatConsAdapter(Context context, List<ContactItem> displayList) {
		super(context, displayList);
		mContext = context;
	}

	@Override
	public View getView(int position, View convertView, final ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_cont, parent, false);
//			final View finalConvertView = convertView;
//			parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//				@Override
//				public void onGlobalLayout() {
//					ViewGroup.LayoutParams params = finalConvertView.getLayoutParams();
//					if (params == null) {
//						return;
//					}
//					if (finalConvertView.getWidth() != parent.getWidth()) {
//						params.width = parent.getWidth();
//						finalConvertView.setLayoutParams(params);
//					}
//				}
//			});
			prepareSetLayoutParams(convertView);
		}
		final ContactItem ci = (ContactItem) getItem(position);

		TextView tvIndex = ViewHolder.get(convertView, R.id.txtChat_List_Item_Index);
		TextView tvMain = ViewHolder.get(convertView, R.id.txtChat_List_Item_Main);
		TextView tvPhone = ViewHolder.get(convertView, R.id.txtChat_List_Item_Main_);
		TextView tvProvince = ViewHolder.get(convertView, R.id.txtChat_List_Item_Province);
		TextView tvCity = ViewHolder.get(convertView, R.id.txtChat_List_Item_City);
		TextView tvIsp = ViewHolder.get(convertView, R.id.txtChat_List_Item_Isp);
		View divide = ViewHolder.get(convertView, R.id.divider);
		GradientProgressBar prgWaiting = ViewHolder.get(convertView, R.id.prgChat_List_Item_Waiting);

		TextViewUtil.setTextSize(tvIndex, ViewConfiger.SIZE_CALL_INDEX_SIZE1);
		TextViewUtil.setTextColor(tvIndex, ViewConfiger.COLOR_CALL_INDEX_COLOR1);
		TextViewUtil.setTextSize(tvMain, ViewConfiger.SIZE_CALL_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvMain, ViewConfiger.COLOR_CALL_ITEM_COLOR1);
		TextViewUtil.setTextSize(tvPhone, ViewConfiger.SIZE_CALL_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvPhone, ViewConfiger.COLOR_CALL_ITEM_COLOR1);
		TextViewUtil.setTextSize(tvProvince, ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvProvince, ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		TextViewUtil.setTextSize(tvCity, ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvCity, ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		TextViewUtil.setTextSize(tvIsp, ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvIsp, ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		
		final PhoneContact listItem = ci.mItem;

		tvIndex.setText("" + (position + 1));
		
		String main = listItem.main;
		String phone = listItem.phone;
		if (TextUtils.isEmpty(main) && !TextUtils.isEmpty(phone)) {
			tvMain.setText(phone);
			tvPhone.setText("");
		} else {
			tvMain.setText(listItem.main == null ? "" : listItem.main);
			tvPhone.setText(listItem.phone == null ? "" : listItem.phone);
		}
		tvProvince.setText(listItem.province == null ? "" : listItem.province);
		tvCity.setText(listItem.city == null ? "" : listItem.city);
		tvIsp.setText(listItem.isp == null ? "" : listItem.isp);

		if (TextUtils.isEmpty(listItem.province) && TextUtils.isEmpty(listItem.city) && TextUtils.isEmpty(listItem.isp)) {
			tvProvince.setVisibility(View.GONE);
			tvCity.setVisibility(View.GONE);
			tvIsp.setVisibility(View.GONE);
		} else {
			tvProvince.setVisibility(View.VISIBLE);
			tvCity.setVisibility(View.VISIBLE);
			tvIsp.setVisibility(View.VISIBLE);
		}
//		tvIsp.setVisibility(listItem.isp == null ? View.GONE : View.VISIBLE);
		divide.setVisibility(position == (ScreenUtil.getVisbileCount() - 1) ? View.INVISIBLE : View.VISIBLE);

		if (position == mFocusIndex) {
			convertView.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			convertView.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}

		prgWaiting.setVisibility(ci.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
		prgWaiting.setProgress(ci.shouldWaiting ? ci.curPrg : 0);
		convertView.setTag(R.id.key_progress, prgWaiting);
		return convertView;
	}
}