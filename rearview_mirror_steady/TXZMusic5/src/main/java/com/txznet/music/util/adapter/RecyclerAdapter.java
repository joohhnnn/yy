package com.txznet.music.util.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.music.widget.DiffCallback;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by felear on 2017/8/14 0014.
 */

public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<T> mDatas;
    protected final int mItemLayoutId;

    public RecyclerAdapter(Context context, List<T> datas, int itemLayoutId) {
        mContext = context;
        mDatas = datas;
        mItemLayoutId = itemLayoutId;
        setHasStableIds(true);
    }

    // item第一次创建时调用，复用时不会调用
    // viewType表示当前item类型，没有使用多布局是此值无效
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // 布局加载 参数2:null时根据item内容自动分配宽高，parent时才会根据父容器属性设置宽高
        View view = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false);

        return new ViewHolder(view);
    }

    // 每个item出来时都会调用
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, int position) {
        convert(holder, position, mDatas.get(position));
    }

    public abstract void convert(RecyclerAdapter.ViewHolder holder, int position, T item);

    @Override
    public long getItemId(int position) {
        return position;
    }

    // 返回item数量
    @Override
    public int getItemCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    public List<T> getData() {
        return mDatas;
    }

    public void addData(List<T> datas) {
        //默认是从后面添加的
        addData(datas, -1);
    }


    public void addData(List<T> datas, int index) {
        if (datas == null) {
            return;
        }

        if (this.mDatas == null) {
            this.mDatas = new LinkedList<>();
        }
        int startPosition = this.mDatas.size();
        if (index == -1) {
            this.mDatas.addAll(datas);
        } else {
            this.mDatas.addAll(index, datas);
        }
        notifyItemRangeInserted(startPosition, datas.size());
    }

    public void removeData(T data) {
        if (data == null) {
            return;
        }

        if (this.mDatas == null) {
            return;
        }
        int i = this.mDatas.indexOf(data);
        mDatas.remove(data);
        this.notifyItemRemoved(i);
        notifyItemRangeChanged(i, getItemCount() - i + 1);
    }

    public int getCount() {
        return mDatas == null ? 0 : mDatas.size();
    }

    /**
     * 替换元素并刷新
     *
     * @param mDatas
     */
    public void refresh(List<T> mDatas) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffCallback<>(this.mDatas, mDatas));
        diffResult.dispatchUpdatesTo(this);
        this.mDatas = mDatas;

//        this.notifyDataSetChanged();
    }

    public void notifyDataSetChanged(List<T> mDatas) {
        this.mDatas = mDatas;
        this.notifyDataSetChanged();
    }

    // 内部类存储item中的控件
    public class ViewHolder extends RecyclerView.ViewHolder {

        private final SparseArray<View> mViews = new SparseArray<>();

        public ViewHolder(View convertView) {
            super(convertView);
        }

        public <V extends View> V getView(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = itemView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (V) view;
        }
    }

}
