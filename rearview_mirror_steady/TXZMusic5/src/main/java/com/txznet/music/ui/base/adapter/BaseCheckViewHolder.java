package com.txznet.music.ui.base.adapter;

import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.txznet.music.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * @author telen
 * @date 2018/12/26,11:01
 */
public abstract class BaseCheckViewHolder<T> extends BaseViewHolder<T> {

    @Bind(R.id.cb_checked)
    @Nullable
    public CheckBox cbChecked;

    public View.OnClickListener mOnClickListener;

    public BaseCheckViewHolder(ViewGroup parent, int res) {
        super(parent, res);
        ButterKnife.bind(this, itemView);
    }

    @Override
    public void setData(T data) {
        super.setData(data);
        RecyclerView.Adapter ownerAdapter = getOwnerAdapter();
        if (ownerAdapter instanceof BaseCheckPlayerAdapter) {
            if (cbChecked != null) {
                cbChecked.setOnClickListener(v -> {
                    ((BaseCheckPlayerAdapter) ownerAdapter).notifyClickCheck(cbChecked.isChecked(), data);
                });
            }

        }
    }


    public void setItemClickListener(View.OnClickListener listener) {
        mOnClickListener = listener;
    }
}
