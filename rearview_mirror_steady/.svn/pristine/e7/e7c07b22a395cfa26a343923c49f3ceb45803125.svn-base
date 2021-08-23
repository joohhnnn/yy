package com.txznet.webchat.ui.car.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.txz.util.focus_supporter.FocusSupporter;
import com.txznet.webchat.R;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.adapter.BaseRecyclerViewAdapter;
import com.txznet.webchat.ui.rearview_mirror.widget.IconTextStateBtn;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 车机版主题设置菜单的Adapter
 * Created by J on 2017/5/14.
 */

public class CarSettingsAdapter extends BaseRecyclerViewAdapter {
    private static final int ITEM_TYPE_TITLE = 0;
    private static final int ITEM_TYPE_SWITCH = 1;
    private static final int ITEM_TYPE_BUTTON = 2;

    /**
     * 菜单中的条目
     */
    public enum MENU_ITEM {
        TITLE, // 标题
        AUTO_LOGIN, // 自动登录开关
        AUTO_BROAD, // 消息播报开关
        HELP, // 新手指南
        CLEAR_LOGIN_CACHE, // 清除登录记录
        EXIT, // 退出登录
        BIND, // 关注设备
    }

    private OnMenuItemClickListner mMenuListener;

    private Context mContext;
    private boolean bPortraitTheme;
    private List<MENU_ITEM> mMenuItemList;

    public CarSettingsAdapter(Context context) {
        mContext = context;
        bPortraitTheme = WxThemeStore.get().isPortraitTheme();
    }

    public void setMenuItemList(List<MENU_ITEM> itemList) {
        mMenuItemList = itemList;
    }

    public void setOnMenuItemClickListener(OnMenuItemClickListner listener) {
        mMenuListener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        MENU_ITEM item = mMenuItemList.get(position);

        switch (item) {
            case TITLE:
                return ITEM_TYPE_TITLE;

            case AUTO_LOGIN:
            case AUTO_BROAD:
                return ITEM_TYPE_SWITCH;

            default:
                return ITEM_TYPE_BUTTON;
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v;

        if (bPortraitTheme) {
            switch (viewType) {
                case ITEM_TYPE_TITLE:
                    v = LayoutInflater.from(mContext)
                            .inflate(R.layout.item_car_main_menu_title_portrait, null);
                    return new TitleViewHolder(v);

                case ITEM_TYPE_SWITCH:
                    v = LayoutInflater.from(mContext)
                            .inflate(R.layout.item_car_main_menu_switch_portrait, null);
                    return new SwitchViewHolder(v);

                case ITEM_TYPE_BUTTON:
                    v = LayoutInflater.from(mContext)
                            .inflate(R.layout.item_car_main_menu_button_portrait, null);
                    return new ButtonViewHolder(v);
            }
        } else {
            switch (viewType) {
                case ITEM_TYPE_TITLE:
                    v = LayoutInflater.from(mContext)
                            .inflate(R.layout.item_car_main_menu_title, null);
                    return new TitleViewHolder(v);

                case ITEM_TYPE_SWITCH:
                    v = LayoutInflater.from(mContext)
                            .inflate(R.layout.item_car_main_menu_switch, null);
                    return new SwitchViewHolder(v);

                case ITEM_TYPE_BUTTON:
                    v = LayoutInflater.from(mContext)
                            .inflate(R.layout.item_car_main_menu_button, null);
                    return new ButtonViewHolder(v);
            }
        }


        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MENU_ITEM item = mMenuItemList.get(position);

        switch (item) {
            case TITLE:
                bindTitleHolder((TitleViewHolder) holder, position);
                break;

            case AUTO_LOGIN:
            case AUTO_BROAD:
                bindSwitchHolder((SwitchViewHolder) holder, position);
                break;

            default:
                bindButtonHolder((ButtonViewHolder) holder, position);
                break;
        }
    }

    private void bindTitleHolder(TitleViewHolder holder, int position) {
        // 根据当前主题设置颜色
        holder.mTvTitle.setTextColor(mContext.getResources().getColor(R.color.color_text_accent));
    }

    private void bindSwitchHolder(SwitchViewHolder holder, int position) {
        holder.mViewFocusIndicator.setVisibility(isOnFocus(position) ? View.VISIBLE : View.GONE);
        holder.position = position;

        MENU_ITEM item = mMenuItemList.get(position);

        if (MENU_ITEM.AUTO_LOGIN == item) {
            // 开机自动登录
            holder.mIvIcon.setImageResource(R.drawable.src_car_main_menu_icon_auto_login);
            holder.mTvTitle.setText(R.string.lb_car_main_menu_auto_login);
            holder.mViewSwitch.setEnabled(AppStatusStore.get().isAutoLoginEnabled());
        } else if (MENU_ITEM.AUTO_BROAD == item) {
            // 自动播报
            holder.mIvIcon.setImageResource(R.drawable.src_car_main_menu_icon_notify);
            holder.mTvTitle.setText(R.string.lb_car_main_menu_notify);
            holder.mViewSwitch.setEnabled(AppStatusStore.get().isAutoBroadEnabled());
        }

    }

    private void bindButtonHolder(ButtonViewHolder holder, int position) {
        holder.mViewFocusIndicator.setVisibility(isOnFocus(position) ? View.VISIBLE : View.GONE);
        holder.position = position;
        MENU_ITEM item = mMenuItemList.get(position);

        if (MENU_ITEM.HELP == item) {
            // 新手指南
            holder.mIvIcon.setImageResource(R.drawable.src_car_main_menu_icon_help);
            holder.mTvTitle.setText(R.string.lb_car_main_menu_help);
            holder.mIvArrow.setVisibility(View.VISIBLE);
        } else if (MENU_ITEM.BIND == item) {
            // 关注设备
            holder.mIvIcon.setImageResource(R.drawable.src_car_main_menu_icon_bind);
            holder.mTvTitle.setText(R.string.lb_car_main_menu_bind);
            holder.mIvArrow.setVisibility(View.VISIBLE);
        } else if (MENU_ITEM.CLEAR_LOGIN_CACHE == item) {
            // 清除缓存
            holder.mIvIcon.setImageResource(R.drawable.src_car_main_menu_icon_clear_cache);
            holder.mTvTitle.setText(R.string.lb_car_main_menu_clear_cache);
            holder.mIvArrow.setVisibility(View.GONE);
        } else if (MENU_ITEM.EXIT == item) {
            // 退出微信
            holder.mIvIcon.setImageResource(R.drawable.src_car_main_menu_icon_exit);
            holder.mTvTitle.setText(R.string.lb_car_main_menu_exit);
            holder.mIvArrow.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return mMenuItemList.size();
    }

    // 菜单标题
    class TitleViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_title)
        TextView mTvTitle;

        public TitleViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            // init theme
            mTvTitle.setTextColor(mContext.getResources().getColor(R.color.color_text_accent));
        }
    }

    // 带开关的菜单项
    class SwitchViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_icon)
        ImageView mIvIcon;
        @Bind(R.id.tv_title)
        TextView mTvTitle;
        @Bind(R.id.view_switch)
        IconTextStateBtn mViewSwitch;
        @Bind(R.id.view_focus_indicator)
        View mViewFocusIndicator;

        int position;

        public SwitchViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            // init theme
            mTvTitle.setTextColor(mContext.getResources().getColor(R.color.color_text_primary));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }
    }

    // 带向右箭头的菜单项
    class ButtonViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_icon)
        ImageView mIvIcon;
        @Bind(R.id.tv_title)
        TextView mTvTitle;
        @Bind(R.id.iv_arrow)
        ImageView mIvArrow;
        @Bind(R.id.view_focus_indicator)
        View mViewFocusIndicator;

        int position;

        public ButtonViewHolder(View v) {
            super(v);
            ButterKnife.bind(this, v);

            // init theme
            mTvTitle.setTextColor(mContext.getResources().getColor(R.color.color_text_primary));

            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick(position);
                }
            });
        }
    }

    @Override
    protected void onItemClick(int position) {
        if (null == mMenuListener) {
            return;
        }

        mMenuListener.onMenuItemClick(mMenuItemList.get(position));
    }

    @Override
    protected void onItemLongClick(int position) {

    }

    @Override
    public boolean onNavOperation(int operation) {
        // 第一个Item不能获取方控焦点，此处需要特殊处理下
        if (1 == getCurrentFocusPosition()
                && (FocusSupporter.NAV_BTN_UP == operation
                || FocusSupporter.NAV_BTN_PREV == operation)) {
            return false;
        }

        return super.onNavOperation(operation);
    }

    // 上个焦点期间最后获取焦点的item index
    private int mLastFocusIndex = 0;
    @Override
    public void onNavGainFocus(Object rawFocus, int operation) {
        super.onNavGainFocus(rawFocus, operation);

        // 恢复焦点index
        if (mLastFocusIndex > 0) {
            setCurrentFocusPosition(mLastFocusIndex);
        }

        if (0 == getCurrentFocusPosition()) {
            setCurrentFocusPosition(1);
        }
    }

    @Override
    public void onNavLoseFocus(Object newFocus, int operation) {
        // ListFocusHelper默认失焦时清空焦点index, 此处做下保存, 用于重新获取焦点时的index恢复
        mLastFocusIndex = getCurrentFocusPosition();
        super.onNavLoseFocus(newFocus, operation);
    }

    public interface OnMenuItemClickListner {
        void onMenuItemClick(MENU_ITEM item);
    }

}
