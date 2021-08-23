package com.txznet.record.adapter;

import java.util.List;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.SimpleBean;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;
import com.txznet.record.view.RoundImageView;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ChatSimpleAdapter extends ChatDisplayAdapter {
	
	public static class SimpleItem extends DisplayItem<SimpleBean> {

	}

	public ChatSimpleAdapter(Context context, List displayList) {
		super(context, displayList);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_simple, parent, false);
			prepareSetLayoutParams(convertView);
		}

		final SimpleItem simpleItem = (SimpleItem) getItem(position);
		final SimpleBean bean = simpleItem.mItem;

		TextView num = ViewHolder.get(convertView, R.id.txtNum);
		RoundImageView navIcon = ViewHolder.get(convertView, R.id.nav_item_avatar);
		TextView name = ViewHolder.get(convertView, R.id.name_tv);
		GradientProgressBar mPb = ViewHolder.get(convertView, R.id.my_progress);
		FrameLayout mLayoutItem = ViewHolder.get(convertView, R.id.layout_item);
		View mDivider = ViewHolder.get(convertView, R.id.divider);
		TextView rightTopTv = ViewHolder.get(convertView, R.id.right_top_tv);
		TextView rightBtmTv = ViewHolder.get(convertView, R.id.right_btm_tv);

		num.setText(String.valueOf(position + 1));

		if (bean.title.contains(":")) {
			String[] txtArray = bean.title.split(":");
			name.setText(txtArray[0]);

			Drawable drawable = getDrawableByPkn(txtArray[1]);
			if (drawable != null) {
				navIcon.setImageDrawable(drawable);
				navIcon.setVisibility(View.VISIBLE);
			} else {
				navIcon.setVisibility(View.GONE);
			}
		} else {
			name.setText(bean.title);
			if (navIcon.getVisibility() != View.GONE) {
				navIcon.setVisibility(View.GONE);
			}
		}

		mPb.setVisibility(simpleItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
		mPb.setProgress(simpleItem.shouldWaiting ? simpleItem.curPrg : 0);
		convertView.setTag(R.id.key_progress, mPb);

		mDivider.setVisibility(position == (getCount() - 1) ? View.INVISIBLE : View.VISIBLE);
		if (position == mFocusIndex) {
			mLayoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			mLayoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}
		return convertView;
	}
	
	private Drawable getDrawableByPkn(String navPkn) {
		PackageManager pm = mContext.getPackageManager();
		try {
			return pm.getApplicationIcon(navPkn);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}
}
