package com.txznet.music.historyModule.ui.adpter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.historyModule.bean.SettingBean;
import com.txznet.music.utils.CollectionUtils;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by telenewbie on 2017/12/23.
 */

public class SettingItemAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


    //    private Context ctx;
    private List<SettingBean> settingBeans;
    private AdapterView.OnItemClickListener listener;

    public SettingItemAdapter(Context ctx, List<SettingBean> settingBeans) {
//        this.ctx = ctx;
        this.settingBeans = settingBeans;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        View view = null;
        if (viewType == SettingBean.STYLE_ARROW) {
            view = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.item_setting_arrow, parent, false);
            viewHolder = new ItemArrowViewHolder(view);
        } else if (viewType == SettingBean.STYLE_CHOICE) {
            view = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.item_setting_choice, parent, false);
            viewHolder = new ItemChoiceViewHolder(view);
        } else if (viewType == SettingBean.STYLE_TEXT) {
            view = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.item_setting_text, parent, false);
            viewHolder = new ItemTextViewHolder(view);
        }
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        SettingBean settingBean = settingBeans.get(position);
        if (settingBean.getStyle() == SettingBean.STYLE_ARROW) {
            ItemArrowViewHolder holder1 = ((ItemArrowViewHolder) holder);
            holder1.tvLeftText.setText(settingBean.getLeftText());
        } else if (settingBean.getStyle() == SettingBean.STYLE_CHOICE) {
            ItemChoiceViewHolder holder1 = ((ItemChoiceViewHolder) holder);
            holder1.tvLeftText.setText(settingBean.getLeftText());
            holder1.ivChoice.setImageResource(settingBean.isChoice() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off);
        } else if (settingBean.getStyle() == SettingBean.STYLE_TEXT) {
            ItemTextViewHolder holder1 = ((ItemTextViewHolder) holder);
            holder1.tvLeftText.setText(settingBean.getLeftText());
            holder1.tvRightText.setText(settingBean.getRightText());
        }
        if (null != listener) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(null, holder.itemView, position, getItemId(position));
                }
            });
        }
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener listener) {
        this.listener = listener;
    }

    @Override
    public int getItemCount() {
        if (CollectionUtils.isNotEmpty(settingBeans)) {
            return settingBeans.size();
        }
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        return settingBeans.get(position).getStyle();
    }

    public static class ItemChoiceViewHolder extends RecyclerView.ViewHolder {

        @Bind(R.id.tv_left_text)
        TextView tvLeftText;
        @Bind(R.id.iv_choice)
        ImageView ivChoice;
        @Bind(R.id.layout_push)
        RelativeLayout layoutPush;

        public ItemChoiceViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ItemTextViewHolder extends RecyclerView.ViewHolder {
        @Bind(R.id.tv_left_text)
        TextView tvLeftText;
        @Bind(R.id.tv_right_text)
        TextView tvRightText;
        @Bind(R.id.layout_version)
        RelativeLayout layoutVersion;

        public ItemTextViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    public static class ItemArrowViewHolder extends RecyclerView.ViewHolder {


        @Bind(R.id.tv_left_text)
        TextView tvLeftText;
        @Bind(R.id.iv_right_arrow)
        ImageView ivRightArrow;
        @Bind(R.id.layout_help)
        RelativeLayout layoutHelp;

        public ItemArrowViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
