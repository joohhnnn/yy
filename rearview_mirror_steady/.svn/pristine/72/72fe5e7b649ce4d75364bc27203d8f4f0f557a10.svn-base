package com.txznet.record.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.GradientProgressBar;
import com.txznet.record.view.HorizontalProgressBar;

import java.util.List;

/**
 * Created by ASUS User on 2015/7/2.
 */
public class ChatContactListAdapter extends BaseAdapter {
    public static class ContactItem {
        public String main;
        public String province;
        public String city;
        public String isp;
        public boolean shouldWaiting;
        public int curPrg;
    }

    private Context mContext;
    private List<ContactItem> mListItems;

    public void setChatListItem(List<ContactItem> listItems) {
        this.mListItems = listItems;
        notifyDataSetChanged();
    }

    public ChatContactListAdapter(Context context, List<ContactItem> listItems) {
        mContext = context;
        mListItems = listItems;
    }

    @Override
    public int getCount() {
        return mListItems.size();
    }

    @Override
    public ContactItem getItem(int position) {
        return mListItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_list_item_cont, parent, false);
            final View finalConvertView = convertView;
            parent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    ViewGroup.LayoutParams params = finalConvertView.getLayoutParams();
                    if (params == null) {
                        return;
                    }
                    if (finalConvertView.getWidth() != parent.getWidth()) {
                        params.width = parent.getWidth();
                        finalConvertView.setLayoutParams(params);
                    }
                }
            });
        }
        final ContactItem listItem = getItem(position);

        TextView tvIndex = ViewHolder.get(convertView, R.id.txtChat_List_Item_Index);
        TextView tvMain = ViewHolder.get(convertView, R.id.txtChat_List_Item_Main);
        TextView tvProvince = ViewHolder.get(convertView, R.id.txtChat_List_Item_Province);
        TextView tvCity = ViewHolder.get(convertView, R.id.txtChat_List_Item_City);
        TextView tvIsp = ViewHolder.get(convertView, R.id.txtChat_List_Item_Isp);
        GradientProgressBar prgWaiting = ViewHolder.get(convertView, R.id.prgChat_List_Item_Waiting);
        
		TextViewUtil.setTextSize(tvIndex, ViewConfiger.SIZE_CALL_INDEX_SIZE1);
		TextViewUtil.setTextColor(tvIndex, ViewConfiger.COLOR_CALL_INDEX_COLOR1);
		TextViewUtil.setTextSize(tvMain, ViewConfiger.SIZE_CALL_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvMain, ViewConfiger.COLOR_CALL_ITEM_COLOR1);
		TextViewUtil.setTextSize(tvProvince, ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvProvince, ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		TextViewUtil.setTextSize(tvCity, ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvCity, ViewConfiger.COLOR_CALL_ITEM_COLOR2);
		TextViewUtil.setTextSize(tvIsp, ViewConfiger.SIZE_CALL_ITEM_SIZE2);
		TextViewUtil.setTextColor(tvIsp, ViewConfiger.COLOR_CALL_ITEM_COLOR2);
        
        tvIndex.setText("" + (position + 1));
        tvMain.setText(listItem.main == null ? "" : listItem.main);
        tvProvince.setText(listItem.province == null ? "" : listItem.province);
        tvCity.setText(listItem.city == null ? "" : listItem.city);
        tvIsp.setText(listItem.isp == null ? "" : listItem.isp);

        if (listItem.province == null && listItem.city == null && listItem.isp == null) {
            tvProvince.setVisibility(View.GONE);
            tvCity.setVisibility(View.GONE);
        } else {
            tvProvince.setVisibility(View.VISIBLE);
            tvCity.setVisibility(View.VISIBLE);
        }
        tvIsp.setVisibility(listItem.isp == null ? View.GONE : View.VISIBLE);


        prgWaiting.setVisibility(listItem.shouldWaiting ? View.VISIBLE : View.INVISIBLE);
        prgWaiting.setProgress(listItem.shouldWaiting ? listItem.curPrg : 0);
		convertView.setTag(R.id.key_progress, prgWaiting);
        return convertView;
    }
    
}
