package com.txznet.webchat.ui.rearview_mirror.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.webchat.R;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.ui.rearview_mirror.widget.BadgeView;
import com.txznet.webchat.util.SmileyParser;
import com.txznet.webchat.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 会话列表adapter
 * Created by J on 2016/3/24.
 */
public class SessionListAdapter extends RecyclerView.Adapter {
    private List<String> mSessionList = new ArrayList<>();
    private Context mContext;

    private int mSelectedIndex = -1;

    // item click listener
    private OnItemClickListener mItemClickListener;

    public SessionListAdapter(Context context) {
        this.mContext = context;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.item_chat_session_list, null);

        return new ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WxContact contact = WxContactStore.getInstance().getContact(mSessionList.get(position));
        ContactViewHolder vh = (ContactViewHolder) holder;

        if (null != contact) {
            CharSequence nick = SmileyParser.getInstance(mContext).parser(contact.getRawDisplayName());
            // vh.mTvUsername.setText(StringUtil.handleLength(nick, 33, "..."));
            vh.mTvUsername.setText(nick);


            // show session icon if needed
            /*if (!TextUtils.isEmpty(contact.mUserOpenId)) {
                String imageStr = WxResourceStore.get().getContactHeadImage(contact.mUserOpenId);
                if (!StringUtils.isEmpty(imageStr)) {
                    ImageLoader.getInstance().displayImage("file:/" + imageStr, vh.mIvUsericon);
                } else {
                    vh.mIvUsericon.setImageResource(R.drawable.default_headimage);
                }
            }*/

            WxImageLoader.loadHead(mContext, contact, vh.mIvUsericon);

            //set unread msg count
            int unReadCount = WxMessageStore.getInstance().getUnreadMsgCount(contact.mUserOpenId);
            if (unReadCount == 0) {
                vh.mViewBadge.hide();
            } else {
                vh.mViewBadge.show();
                vh.mViewBadge.setText(unReadCount > 99 ? "..." : unReadCount + "");
            }

            //set notify icon
            if (WxContactStore.getInstance().isContactBlocked(contact.mUserOpenId)) {
                vh.mIvDenyIcon.setVisibility(View.VISIBLE);
            } else if (!AppStatusStore.get().isGroupMsgBroadEnabled() && WxContact.isGroupOpenId(contact.mUserOpenId)) {
                vh.mIvDenyIcon.setVisibility(View.VISIBLE);
            } else {
                vh.mIvDenyIcon.setVisibility(View.INVISIBLE);
            }
        } else {
            vh.mTvUsername.setText("正在同步联系人...");
        }

        // set indicator
        if(position == mSelectedIndex) {
            vh.mIvIndicator.setVisibility(View.VISIBLE);
        }else{
            vh.mIvIndicator.setVisibility(View.GONE);
        }

        vh.index = position;
    }

    @Override
    public int getItemCount() {
        return mSessionList.size();
    }

    public void setContactList(List<String> list) {
        this.mSessionList = list;
    }

    public WxContact getItem(int position) {
        return WxContactStore.getInstance().getContact(mSessionList.get(position));
    }

    class ContactViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.view_chat_session_usericon)
        ImageView mIvUsericon;
        @Bind(R.id.view_chat_session_badge)
        BadgeView mViewBadge;
        @Bind(R.id.tv_chat_session_username)
        TextView mTvUsername;
        @Bind(R.id.iv_chat_session_notify)
        ImageView mIvDenyIcon;
        @Bind(R.id.iv_chat_session_indicator)
        ImageView mIvIndicator;

        int index;

        public ContactViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(v, index);
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(View v, int index);
    }

    public int performNext() {
        mSelectedIndex++;

        if (mSelectedIndex >= mSessionList.size()) {
            mSelectedIndex = mSessionList.size() - 1;
        }

        notifyDataSetChanged();

        return mSelectedIndex;
    }

    public int performPrev() {
        mSelectedIndex--;

        if (mSelectedIndex < 0) {
            mSelectedIndex = 0;
        }

        notifyDataSetChanged();

        return mSelectedIndex;
    }

    public void performClick() {
        if (mItemClickListener != null) {
            mItemClickListener.onItemClicked(null, mSelectedIndex);
        }
    }

    public void setFocusIndex(int index) {
        mSelectedIndex = index;
        notifyDataSetChanged();
    }


}
