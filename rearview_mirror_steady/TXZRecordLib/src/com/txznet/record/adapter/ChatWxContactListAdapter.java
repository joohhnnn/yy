package com.txznet.record.adapter;

import java.io.File;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.WxContact;
import com.txznet.record.lib.R;
import com.txznet.record.util.ViewHolder;
import com.txznet.record.view.CircleImageView;
import com.txznet.record.view.RoundImageView;

import android.content.Context;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class ChatWxContactListAdapter extends ChatDisplayAdapter {
    private Context mContext;
    
    public static class WxContactItem extends DisplayItem<WxContact> {

	}

    public ChatWxContactListAdapter(Context context, List<WxContactItem> listItems) {
        super(context, listItems);
        mContext = context;
    }

    @Override
    public View getView(int position, View convertView, final ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(mContext).inflate(R.layout.chat_wxcontact_item, parent, false);
			prepareSetLayoutParams(convertView);
        }
        final WxContactItem item = (WxContactItem) getItem(position);
        final WxContact contact = item.mItem;

        TextView tvIndex = ViewHolder.get(convertView, R.id.txtNum);
        TextView tvMain = ViewHolder.get(convertView, R.id.wxcontact_item_name);
        RoundImageView imgHead = ViewHolder.get(convertView, R.id.wxcontact_item_avatar);
        View mDivider = ViewHolder.get(convertView, R.id.divider);
        FrameLayout layoutItem = ViewHolder.get(convertView, R.id.layout_item);
        
        TextViewUtil.setTextSize(tvIndex,ViewConfiger.SIZE_WX_INDEX_SIZE1);
        TextViewUtil.setTextColor(tvIndex,ViewConfiger.COLOR_WX_INDEX_COLOR1);
        TextViewUtil.setTextSize(tvMain,ViewConfiger.SIZE_WX_ITEM_SIZE1);
		TextViewUtil.setTextColor(tvMain,ViewConfiger.COLOR_WX_ITEM_COLOR1);

        tvIndex.setText("" + (position + 1));
        tvMain.setText(contact.nick == null ? "" : contact.nick);
        File header = new File(Environment.getExternalStorageDirectory() + "/txz/webchat/cache/Head/" + contact.openid);
        if (header.exists()) {
        	ImageLoader.getInstance().displayImage("file://" + header.getAbsolutePath(), new ImageViewAware(imgHead));
        } else {	
            imgHead.setImageResource(R.drawable.default_head);
            refreshWxContact(contact.openid);
        }
        mDivider.setVisibility(position == (getCount()-1)?View.INVISIBLE:View.VISIBLE);
        
        if (position == mFocusIndex) {
        	layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_focused));
		} else {
			layoutItem.setBackgroundColor(GlobalContext.get().getResources().getColor(R.color.bg_ripple_nor));
		}
        
        return convertView;
    }
    
	private void refreshWxContact(String uid) {
		try {
			JSONObject data  = new JSONObject();
			data.put("id", uid);
			ServiceManager.getInstance().sendInvoke(ServiceManager.WEBCHAT, "wx.session.refresh", data.toString().getBytes(), null);
		} catch (Exception e) {

		}
	}
}
