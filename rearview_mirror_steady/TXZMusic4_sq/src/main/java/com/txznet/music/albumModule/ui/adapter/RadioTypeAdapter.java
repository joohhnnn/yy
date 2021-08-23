package com.txznet.music.albumModule.ui.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.baseModule.ui.BaseFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Terry on 2017/5/5.
 */

public class RadioTypeAdapter extends RecyclerView.Adapter {

    private BaseFragment baseFragment;
    private int selectedPosition = -1;
    private boolean mIsSBState = false;
    AdapterView.OnItemClickListener mOnItemClickListener;
    private List<Category> mCategories;

    public RadioTypeAdapter(BaseFragment baseFragment, List<Category> categories) {
        this.baseFragment = baseFragment;
        mCategories = categories;
    }

    public void updateCategories(List<Category> categories) {
        mCategories.clear();
        mCategories.addAll(categories);
    }

    public void setSelectedIndex(int position) {
        selectedPosition = position;
    }

    public int getSelectedPosition() {
        return selectedPosition;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener) {
        this.mOnItemClickListener = itemClickListener;
    }

    public Category getItem(int position) {
        if (mCategories != null && mCategories.size() > position) {
            return mCategories.get(position);
        }
        return null;
    }

    /**
     * 将item背景变暗
     */
    public void updateItemSBState(boolean isSBState) {
//        mIsSBState = isSBState;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_audio_fenlei, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (mCategories == null || mCategories.size() < position - 1) {
            LogUtil.loge("categories is null,return");
            return;
        }
        Category category = mCategories.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setFenlei(category.getDesc());
        if (position == selectedPosition) {
            viewHolder.mBtnFenlei.setSelected(true);
//            viewHolder.mIvSelect.setSelected(true);
        } else {
            viewHolder.mBtnFenlei.setSelected(false);
//            viewHolder.mIvSelect.setSelected(false);
        }
        viewHolder.mBtnFenlei.setTag(position);
        viewHolder.mBtnFenlei.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnItemClickListener != null) {
                    int position = (int) v.getTag();
                    mOnItemClickListener.onItemClick(null, v, position, getItemId(position));
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public Button mBtnFenlei;
        private RelativeLayout mLayout;
//        public ImageView mIvSelect;

        public ViewHolder(View itemView) {
            super(itemView);
            mBtnFenlei = (Button) itemView.findViewById(R.id.btn_audio_fenlei);
//            mIvSelect = (ImageView) itemView.findViewById(R.id.iv_select);
//            mBtnFenlei.setBackground(baseFragment.getActivity().getResources().getDrawable(R.drawable.music_title_btn_bg));
            mBtnFenlei.setTextColor(itemView.getResources().getColorStateList(R.color.type_text));
//            mIvSelect.setBackground(baseFragment.getActivity().getResources().getDrawable(R.drawable.type_select));
        }

        public void setFenlei(String fenlei) {
            mBtnFenlei.setText(fenlei);
        }
    }

    @Override
    public int getItemCount() {
        if (mCategories != null) {
            return mCategories.size();
        }
        return 0;
    }
}
