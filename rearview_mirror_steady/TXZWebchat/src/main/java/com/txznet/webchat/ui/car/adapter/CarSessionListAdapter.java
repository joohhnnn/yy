package com.txznet.webchat.ui.car.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.R;
import com.txznet.webchat.comm.plugin.model.WxContact;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.adapter.BaseRecyclerViewAdapter;
import com.txznet.webchat.ui.common.WxImageLoader;
import com.txznet.webchat.ui.rearview_mirror.widget.BadgeView;
import com.txznet.webchat.util.SmileyParser;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by J on 2016/10/12.
 */

public class CarSessionListAdapter extends BaseRecyclerViewAdapter {
    private List<String> mSessionList = new ArrayList<>();
    private Context mContext;
    // item select indicator
    private String mCurrentSelectionId = "";

    private boolean bLoadHeadIcon = true;

    private boolean bFocusGained = false;

    private boolean bPortraitTheme;

    // item click listener
    private CarSessionListAdapter.OnItemClickListener mItemClickListener;

    public CarSessionListAdapter(Context context) {
        this.mContext = context;
        bPortraitTheme = WxThemeStore.get().isPortraitTheme();
    }

    public void setOnItemClickListener(CarSessionListAdapter.OnItemClickListener listener) {
        this.mItemClickListener = listener;
    }

    public void setCurrentSelectionId(String id) {
        mCurrentSelectionId = id;

        if (!TextUtils.isEmpty(id) && bFocusGained) {
            setCurrentFocusPosition(getCurrentSelectionPosition());
        }
    }

    public int getCurrentSelectionPosition() {
        if (TextUtils.isEmpty(mCurrentSelectionId) || null == mSessionList || 0 == mSessionList.size()) {
            return -1;
        }

        for (int i = 0, len = mSessionList.size(); i < len; i++) {
            if (mCurrentSelectionId.equals(mSessionList.get(i))) {
                return i;
            }
        }

        return -1;
    }

    public void setLoadHeadIcon(boolean enable) {
        /*if (!bLoadHeadIcon && enable) {
            bLoadHeadIcon = enable;
            notifyDataSetChanged();
        } else {
            bLoadHeadIcon = enable;
        }*/

        if (enable) {
            //Glide.with(mContext).resumeRequests();
        } else {
            //Glide.with(mContext).pauseRequests();
        }
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;
        if (bPortraitTheme) {
            if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(WxThemeStore.get().getCurrentTheme())) {
                v = LayoutInflater.from(mContext).inflate(R.layout.item_car_session_list_portrait_t700, null);
            } else {
                v = LayoutInflater.from(mContext).inflate(R.layout.item_car_session_list_portrait, null);
            }

        } else {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_car_session_list, null);
        }

        return new CarSessionListAdapter.ContactViewHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        WxContact contact = WxContactStore.getInstance().getContact(mSessionList.get(position));
        CarSessionListAdapter.ContactViewHolder vh = (CarSessionListAdapter.ContactViewHolder) holder;

        if (contact != null) {
            CharSequence displayName = SmileyParser.getInstance(mContext).parser(contact.getRawDisplayName());
            //vh.mTvUsername.setText(StringUtil.handleLength(displayName.toString(), 18, "..."));
            vh.mTvUsername.setText(displayName);

            WxImageLoader.loadHead(mContext, contact, vh.mIvUsericon);

            if (!TextUtils.isEmpty(contact.mUserOpenId) && contact.mUserOpenId.equals(mCurrentSelectionId)) {
                //vh.mIvSelectIndicator.setImageDrawable(mContext.getResources().getDrawable(R.drawable.shape_item_car_session_back_selected));
                vh.mIvSelectIndicator.setBackgroundColor(mContext.getResources().getColor(R.color.color_session_car_sel));
                vh.mIvSelectIndicator.setVisibility(View.VISIBLE);
            } else {
                vh.mIvSelectIndicator.setVisibility(View.GONE);
            }

            if (isOnFocus(vh.getAdapterPosition())) {
                vh.mIvFocusIndicator.setVisibility(View.VISIBLE);
            }else {
                vh.mIvFocusIndicator.setVisibility(View.GONE);
            }

            //set unread msg count
            int unReadCount = WxMessageStore.getInstance().getUnreadMsgCount(contact.mUserOpenId);
            if (unReadCount == 0) {
                vh.mViewBadge.hide();
            } else {
                vh.mViewBadge.show();
                vh.mViewBadge.setText(unReadCount > 99 ? "..." : unReadCount + "");
                //vh.mViewBadge.setText("99+");
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
            vh.mTvUsername.setText("?????????????????????...");
        }


        vh.id = contact.mUserOpenId;
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
        @Bind(R.id.iv_item_car_session_avatar)
        RoundedImageView mIvUsericon;
        @Bind(R.id.view_item_car_session_badge)
        BadgeView mViewBadge;
        @Bind(R.id.tv_item_car_session_name)
        TextView mTvUsername;
        @Bind(R.id.iv_item_car_session_notify)
        ImageView mIvDenyIcon;
        @Bind(R.id.iv_item_car_session_select_indicator)
        ImageView mIvSelectIndicator;
        @Bind(R.id.iv_item_car_session_focus_indicator)
        ImageView mIvFocusIndicator;

        String id;

        public ContactViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            // init theme
            mTvUsername.setTextColor(mContext.getResources().getColor(R.color.color_text_primary));
            mIvSelectIndicator.setBackground(mContext.getResources().getDrawable(R.drawable.shape_car_session_list_bg_selected));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mItemClickListener != null) {
                        mItemClickListener.onItemClicked(v, id);
                        mCurrentSelectionId = id;
                        notifyDataSetChanged();
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(View v, String id);
    }

    @Override
    protected void onItemClick(int position) {
        if (mItemClickListener != null) {
            String id = mSessionList.get(position);
            mItemClickListener.onItemClicked(null, id);
            mCurrentSelectionId = id;
            notifyDataSetChanged();
        }
    }

    @Override
    protected void onItemLongClick(int position) {

    }

    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        if (FocusSupporter.NAV_BTN_LEFT == operation) {
            setCurrentFocusPosition(getCurrentSelectionPosition());
            return;
        }

        // ???????????????????????????, ???????????????????????????????????????????????????????????????????????????, ???????????????
        // ?????????????????????
        if (FocusSupporter.NAV_MODE_ONE_WAY == WxNavBtnHelper.getInstance().getNavMode()
                && FocusSupporter.NAV_BTN_NONE == operation) {
            setCurrentFocusPosition(getCurrentSelectionPosition());
            return;
        }

        bFocusGained = true;
        super.onNavGainFocus(rawFocus, operation);
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {
        bFocusGained = false;
        super.onNavLoseFocus(newFocus, operation);
    }
}
