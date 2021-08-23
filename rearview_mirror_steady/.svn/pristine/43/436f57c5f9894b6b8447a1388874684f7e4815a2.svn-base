package com.txznet.music.albumModule.ui.adapter;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.R;
import com.txznet.music.data.entity.Category;
import com.txznet.music.baseModule.ui.BaseFragment;

import java.util.List;

/**
 * Created by Terry on 2017/5/5.
 */

public class MusicFenleiAdapter extends RecyclerView.Adapter {

    private BaseFragment baseFragment;
    private List<Category> categories;
    private int selectedPosition = -1;
    private Resources mRes;

    AdapterView.OnItemClickListener mOnItemClickListener;


    public MusicFenleiAdapter(BaseFragment baseFragment, List<Category> categories) {
        this.baseFragment = baseFragment;
        this.categories = categories;
        this.mRes = baseFragment.getActivity().getResources();
    }

    public void updateCategories(List<Category> categories){
        this.categories = categories;
        LogUtil.logd("categories:" + categories.size());
    }

    public void setSelectedIndex(int position) {
        selectedPosition = position;
    }

    public void setOnItemClickListener(AdapterView.OnItemClickListener itemClickListener){
        this.mOnItemClickListener = itemClickListener;
    }

    public Category getItem(int position) {
        if (categories != null && categories.size() > position) {
            return categories.get(position);
        }
        return null;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music_fenlei, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (categories == null || categories.size() < position - 1) {
            LogUtil.loge("categories is null,return");
            return;
        }
        Category category = categories.get(position);
        ViewHolder viewHolder = (ViewHolder) holder;
        viewHolder.setFenlei(category.getDesc());
        if (position == selectedPosition) {
            viewHolder.setSelect(true);
        }else {
            viewHolder.setSelect(false);
        }
        RelativeLayout layout = viewHolder.getLayout();
        layout.setTag(position);
        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener!=null) {
                    int position = (int) v.getTag();
                    mOnItemClickListener.onItemClick(null,v,position,getItemId(position));
                }
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mTextFenlei;
        private RelativeLayout mLayout;
        private RelativeLayout mLayoutWrapper;

        public ViewHolder(View itemView) {
            super(itemView);
            mTextFenlei = (TextView) itemView.findViewById(R.id.tv_item_music_fenlei);
            mLayout = (RelativeLayout) itemView.findViewById(R.id.layout_fenlei_item);
            mLayoutWrapper = (RelativeLayout) itemView.findViewById(R.id.layout_fenlei_item_wrapper);
        }

        public void setFenlei(String fenlei) {
            mTextFenlei.setText(fenlei);
        }


        public TextView getTextView() {
            return mTextFenlei;
        }

        public RelativeLayout getLayout(){
            return mLayout;
        }

        public void setSelect(boolean selected) {
            if (selected) {
                mLayout.setBackground(mRes.getDrawable(R.drawable.music_fenlei_selected));
                mLayoutWrapper.setBackgroundColor(mRes.getColor(R.color.transparent));
            } else {
                mLayout.setBackgroundColor(mRes.getColor(R.color.transparent));
                mLayoutWrapper.setBackgroundColor(mRes.getColor(R.color.music_fenlei_item_normal));
            }
        }
    }


    @Override
    public int getItemCount() {
        if (categories != null) {
            return categories.size();
        }
        return 0;
    }
}
