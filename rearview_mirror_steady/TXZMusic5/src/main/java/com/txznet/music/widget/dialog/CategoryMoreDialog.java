package com.txznet.music.widget.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.music.R;
import com.txznet.music.data.entity.CategoryItemData;
import com.txznet.music.util.adapter.RecyclerAdapter;
import com.txznet.music.widget.GridDividerItemDecoration;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 更多分类对话框
 *
 * @author zackzhou
 * @date 2018/12/25,14:28
 */

public class CategoryMoreDialog extends Dialog {

    @Bind(R.id.rv_category)
    RecyclerView mRecyclerView;
    @Bind(R.id.fl_content)
    FrameLayout flContent;

    List<CategoryItemData> mData;
    CategoryItemData mCurCategoryItemData;

    public CategoryMoreDialog setData(List<CategoryItemData> data) {
        mData = data;
        if (mData != null && mData.size() > 0) {
            //数据最多展示9个
            if (mData.size() > 9) {
                int size = mData.size();
                mData.subList(9, size).clear();
            }
            setCurCategory(mData.get(0));
        }
        return this;
    }

    private void setCurCategory(CategoryItemData curCategory) {
        mCurCategoryItemData = curCategory;
    }

    public interface OnItemClickCallback {
        /**
         * 点击确定键
         */
        void onConfirm(CategoryItemData t);

    }

    OnItemClickCallback callback;

    public CategoryMoreDialog(@NonNull Context context, OnItemClickCallback callback) {
        super(context, R.style.TXZ_Dialog_Style_Full);
        this.callback = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_dialog_more_category);
        ButterKnife.bind(this);

        initAdapter();

        flContent.setOnClickListener(v -> {
            CategoryMoreDialog.this.dismiss();
        });
    }

    @Override
    public void show() {
        super.show();
        mRecyclerView.getAdapter().notifyDataSetChanged();
    }

    private void initAdapter() {

        mRecyclerView.setLayoutManager(new GridLayoutManager(GlobalContext.get(), 3));
        GridDividerItemDecoration gridDividerItemDecoration = new GridDividerItemDecoration(1, getContext().getResources().getColor(R.color.white_10));
        mRecyclerView.addItemDecoration(gridDividerItemDecoration);
        mRecyclerView.setAdapter(new RecyclerAdapter<CategoryItemData>(getContext(), mData, R.layout.category_more_item_view) {
            @Override
            public void convert(RecyclerAdapter.ViewHolder holder, int position, CategoryItemData item) {
                TextView tvCategory = (TextView) holder.getView(R.id.tv_category);
                CategoryItemData obj = mData.get(position);
                tvCategory.setText(obj.desc);
                if (mCurCategoryItemData != null && mCurCategoryItemData.equals(obj)) {
                    tvCategory.setTextColor(getContext().getResources().getColor(R.color.red));
                } else {
                    tvCategory.setTextColor(getContext().getResources().getColor(R.color.white));
                }

                holder.itemView.setOnClickListener(v -> {
                    if (callback != null) {
                        callback.onConfirm(obj);
                        setCurCategory(obj);
                    }
                    CategoryMoreDialog.this.dismiss();
                });
            }
        });
        mRecyclerView.setOverScrollMode(RecyclerView.OVER_SCROLL_NEVER);

    }


}
