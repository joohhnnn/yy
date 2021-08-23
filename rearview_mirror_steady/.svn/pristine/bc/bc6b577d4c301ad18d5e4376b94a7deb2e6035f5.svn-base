package com.txznet.music.ui.base.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.jude.easyrecyclerview.adapter.BaseViewHolder;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.ui.base.BasePlayerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author telen
 * @date 2018/12/26,10:17
 */
public abstract class BaseCheckPlayerAdapter<T, VH extends BaseCheckViewHolder> extends BasePlayerAdapter<T, VH> {
    protected boolean isChange = false;
    private boolean isSelectAll = false;

    private OnCheckChangeListener mOnCheckChangeListener;

    private List<T> checkedData = new ArrayList<>();

    public BaseCheckPlayerAdapter(Context context) {
        super(context);
    }

    @Override
    public void OnBindViewHolder(BaseViewHolder holder, int position) {
        super.OnBindViewHolder(holder, position);
        VH holder1 = (VH) holder;
        if (isChange) {
            if (holder1.cbChecked != null) {
                holder1.cbChecked.setChecked(checkedData.contains(getItem(position)));
            }
            holder1.itemView.setOnClickListener(v -> {
                holder1.cbChecked.performClick();
//                holder1.cbChecked.setChecked(!holder1.cbChecked.isChecked());
//                notifyClickCheck(holder1.cbChecked.isChecked(), getItem(position));
            });
        } else {
            holder1.itemView.setOnClickListener(holder1.mOnClickListener);
        }

    }

    public void change2Check(boolean change) {
        isChange = change;
        isSelectAll = false;
        checkedData.clear();
        notifyDataSetChanged();
    }

    public void change2AllSelected(boolean all) {
        isSelectAll = all;
        checkedData.clear();
        if (isSelectAll) {
            checkedData.addAll(mObjects);
        }
        notifyDataSetChanged();
    }

    public List<T> getCheckedData() {
        return new ArrayList<>(checkedData);
    }

    public void setCheckAllChangeListener(OnCheckChangeListener checkAllChangeListener) {
        mOnCheckChangeListener = checkAllChangeListener;
    }

    protected void changeVisible(VH holder, View view) {
        initViewVisible(holder);
        if (isChange) {
            if (holder.cbChecked != null) {
                holder.cbChecked.setVisibility(View.VISIBLE);
            }
        } else {
            view.setVisibility(View.VISIBLE);
        }
    }

    protected abstract void initViewVisible(VH holder);


    public interface OnCheckChangeListener {
        void onCheckAllChange(boolean isCheckAll);
    }

    protected void notifyClickCheck(boolean add, T data) {
        if (add) {
            checkedData.add(data);
        } else {
            checkedData.remove(data);
        }
        notifyCheckAll();
    }

    private void notifyCheckAll() {
        if (mOnCheckChangeListener != null) {

            if (BuildConfig.DEBUG) {
                Log.d(Constant.LOG_TAG_UI_DEBUG, getClass().getSimpleName() + ",notifyClickCheck：" + checkedData.size() + ":" + mObjects.size());
            }
            if (isSelectAll && checkedData.size() != mObjects.size()) {
                isSelectAll = false;
                mOnCheckChangeListener.onCheckAllChange(false);
                return;
            }
            if (!isSelectAll && checkedData.size() == mObjects.size()) {
                mOnCheckChangeListener.onCheckAllChange(true);
                isSelectAll = true;
                return;
            }
        }
    }

    private RecyclerView.AdapterDataObserver dataObserver = new RecyclerView.AdapterDataObserver() {
        @Override
        public void onItemRangeInserted(int positionStart, int itemCount) {
            notifyCheckAll();
        }
    };

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        registerAdapterDataObserver(dataObserver);
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        unregisterAdapterDataObserver(dataObserver);
    }
}
