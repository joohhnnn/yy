package com.txznet.webchat.ui.common.widget.user_picker;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.txznet.webchat.R;
import com.txznet.webchat.comm.plugin.model.WxUserCache;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.adapter.BaseRecyclerViewAdapter;
import com.txznet.webchat.util.ImageUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 自动登录用户选择列表
 * Created by J on 2017/3/27.
 */

public class UserPickerAdapter extends BaseRecyclerViewAdapter {

    private List<WxUserCache> mUserList = new ArrayList<>();
    private Context mContext;
    private OnUserPickerListener mListener;

    public UserPickerAdapter(Context context) {
        mContext = context;
    }

    public void setUserList(List<WxUserCache> userList) {
        mUserList = userList;
    }

    public void setUserPickerListener(OnUserPickerListener listener) {
        mListener = listener;
    }

    @Override
    public UserHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = null;
        if (WxThemeStore.THEME_MIRROR.equals(WxThemeStore.get().getCurrentTheme())) {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_user_picker, null);
        } else {
            v = LayoutInflater.from(mContext).inflate(R.layout.item_car_user_picker, null);
        }
        return new UserHolder(v);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final UserHolder userHolder = (UserHolder) holder;
        final boolean isLast = (null == mUserList || mUserList.size() == position);

        // show add icon for last item
        if (isLast) {
            // 根据主题设置添加按钮图片
            if (WxThemeStore.THEME_MIRROR.equals(WxThemeStore.get().getCurrentTheme())) {
                userHolder.mIvIcon.setImageResource(R.drawable.ic_user_picker_add);
            } else {
                userHolder.mIvIcon.setImageResource(R.drawable.ic_user_picker_add_rect);
            }

        } else {
            // show userAvatar
            //userHolder.mIvIcon.setImageBitmap(Base64Converter.string2Bitmap(mUserList.get(position).getUserHead()));
            ImageUtil.showImageString(userHolder.mIvIcon, mUserList.get(position).getUserHead(), R.drawable.default_headimage);
        }

        userHolder.mIvIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    if (isLast) {
                        mListener.onPickAdd();
                    } else {
                        mListener.onPickUser(mUserList.get(userHolder.getAdapterPosition()).getUin());
                    }
                }
            }
        });

        if (isOnFocus(userHolder.getAdapterPosition())) {
            userHolder.mViewIndicator.setVisibility(View.VISIBLE);
        } else {
            userHolder.mViewIndicator.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        if (null == mUserList || mUserList.isEmpty()) {
            return 1;
        }

        return mUserList.size() + 1;
    }

    static class UserHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_user_picker_avatar)
        ImageView mIvIcon;
        @Bind(R.id.view_user_picker_indicator)
        View mViewIndicator;

        UserHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);
        }
    }

    public interface OnUserPickerListener {
        void onPickUser(String uid);

        void onPickAdd();
    }

    @Override
    protected void onItemClick(int position) {
        if (null != mListener && position != -1) {
            if (position == getItemCount() - 1) {
                mListener.onPickAdd();
            } else {
                mListener.onPickUser(mUserList.get(position).getUin());
            }
        }
    }

    @Override
    protected void onItemLongClick(int position) {

    }

    @Override
    protected int getLayoutOrientation() {
        return BaseRecyclerViewAdapter.LAYOUT_ORIENTATION_HORIZONTAL;
    }
}
