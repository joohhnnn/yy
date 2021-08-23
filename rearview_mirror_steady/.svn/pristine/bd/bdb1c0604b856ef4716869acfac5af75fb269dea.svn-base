package com.txznet.music.ui.setting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.util.StringUtils;
import com.txznet.music.R;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author telen
 * @date 2018/12/17,11:42
 */
public class SettingExpendAdapter extends BaseRecyclerViewAdapter<SettingBean, String, BaseViewHolder> {
    LayoutInflater mLayoutInflater;

    public SettingExpendAdapter(Context ctx, List<RecyclerViewData<SettingBean, String>> datas) {
        super(ctx, datas);
        mLayoutInflater = LayoutInflater.from(ctx);

    }

    @Override
    public View getGroupView(ViewGroup parent, int groupType) {
        View view = null;
        if (groupType == SettingBean.STYLE_ARROW) {
            view = mLayoutInflater.inflate(R.layout.setting_item_arrow, parent, false);
        } else if (groupType == SettingBean.STYLE_TWO_LINE_ARROW) {
            view = mLayoutInflater.inflate(R.layout.setting_item_two_line_arrow, parent, false);
        } else if (groupType == SettingBean.STYLE_CHOICE) {
            view = mLayoutInflater.inflate(R.layout.setting_item_choice, parent, false);
        } else if (groupType == SettingBean.STYLE_TEXT) {
            view = mLayoutInflater.inflate(R.layout.setting_item_text, parent, false);
        }
        return view;
    }

    @Override
    public View getChildView(ViewGroup parent, int childType) {
        return View.inflate(parent.getContext(), android.R.layout.simple_list_item_2, null);
    }


    @Override
    public BaseViewHolder createRealViewHolder(Context ctx, View view, int viewType) {
        return getHolder(view, viewType);
    }

    @Override
    public void onBindGroupHolder(BaseViewHolder holder, int groupPos, int position, SettingBean groupData) {
        if (holder instanceof IBindHolder) {
            ((IBindHolder) holder).onBindView(groupData);

        } else {
            throw new RuntimeException("please implements IBindHolder");
        }
    }

    @Override
    public void onBindChildpHolder(BaseViewHolder holder, int groupPos, int childPos, int position, String childData) {
        if (holder instanceof IBindHolder) {
            ((IBindHolder) holder).onBindView(childData);
        } else {
            throw new RuntimeException("please implements IBindHolder");
        }
    }


    public BaseViewHolder getHolder(View view, int viewType) {
        BaseViewHolder viewHolder = null;
        if (isParent(viewType)) {
            if (getDataType(viewType) == SettingBean.STYLE_ARROW) {
                viewHolder = new ItemArrowViewHolder(view, getViewType(viewType));
            } else if (getDataType(viewType) == SettingBean.STYLE_CHOICE) {
                viewHolder = new ItemChoiceViewHolder(view, getViewType(viewType));
            } else if (getDataType(viewType) == SettingBean.STYLE_TWO_LINE_ARROW) {
                viewHolder = new ItemChoiceViewHolder(view, getViewType(viewType));
            } else if (getDataType(viewType) == SettingBean.STYLE_TEXT) {
                viewHolder = new ItemTextViewHolder(view, getViewType(viewType));
            }
        } else if (isChild(viewType)) {
            // TODO: 2018/12/17 自类需要修改.如果可以这里可以进行重构
            viewHolder = new StringChildViewHolder(view, getViewType(viewType));
        }
        return viewHolder;
    }

    public interface IBindHolder<T> {
        void onBindView(T t);
    }

    public static class ItemChoiceViewHolder extends BaseViewHolder implements IBindHolder<SettingBean> {

        @Bind(R.id.tv_left_text)
        TextView tvLeftText;
        @Bind(R.id.tv_sub_text)
        TextView tvSubText;
        @Bind(R.id.iv_choice)
        ImageView ivChoice;

        public ItemChoiceViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBindView(SettingBean settingBean) {
            tvLeftText.setText(settingBean.leftText);
            if (settingBean.style == SettingBean.STYLE_CHOICE) {
                ivChoice.setImageResource(settingBean.choice ? R.drawable.setting_open_btn : R.drawable.setting_close_btn);
            }


            if (StringUtils.isNotEmpty(settingBean.subText)) {
                if (settingBean.choice) {
                    tvSubText.setVisibility(View.VISIBLE);
                    tvSubText.setText("已开启");
                } else {
                    tvSubText.setText(settingBean.subText);
                }
            }
        }
    }

    public static class ItemTextViewHolder extends BaseViewHolder implements IBindHolder<SettingBean> {
        @Bind(R.id.tv_left_text)
        TextView tvLeftText;
        @Bind(R.id.tv_right_text)
        TextView tvRightText;

        public ItemTextViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBindView(SettingBean settingBean) {
            tvLeftText.setText(settingBean.leftText);
            tvRightText.setText(settingBean.rightText);
        }
    }

    public static class ItemArrowViewHolder extends BaseViewHolder implements IBindHolder<SettingBean> {


        @Bind(R.id.tv_left_text)
        TextView tvLeftText;
        @Bind(R.id.iv_right_arrow)
        ImageView ivRightArrow;
        @Bind(R.id.tv_right_text)
        TextView tvRightText;

        public ItemArrowViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBindView(SettingBean settingBean) {
            tvLeftText.setText(settingBean.leftText);
            tvRightText.setText(settingBean.rightText);
        }
    }

    public static class StringChildViewHolder extends BaseViewHolder implements IBindHolder<String> {

        @Bind(android.R.id.text1)
        TextView tvContent;

        public StringChildViewHolder(View itemView, int viewType) {
            super(itemView, viewType);
            ButterKnife.bind(this, itemView);
        }

        @Override
        public void onBindView(String s) {
            tvContent.setText(s);
        }
    }
}
