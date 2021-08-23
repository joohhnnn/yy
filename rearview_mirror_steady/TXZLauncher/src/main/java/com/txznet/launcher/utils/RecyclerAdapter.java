package com.txznet.launcher.utils;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by felear on 2017/8/14 0014.
 * RecycelerAdapter的封装。通过匿名内部类重写convert方法，可以在module中写实现视图代码。
 */

public abstract class RecyclerAdapter<T> extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<T> mData;
    protected final int mItemLayoutId;

    public RecyclerAdapter(Context context, List<T> data, int itemLayoutId) {
        mContext = context;
        mData = data;
        mItemLayoutId = itemLayoutId;
    }

    // item第一次创建时调用，复用时不会调用
    // viewType表示当前item类型，没有使用多布局是此值无效
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // 布局加载 参数2:null时根据item内容自动分配宽高，parent时才会根据父容器属性设置宽高
        View view = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false);

        return new ViewHolder(view);
    }

    // 每个item出来时都会调用
    @Override
    public void onBindViewHolder(RecyclerAdapter.ViewHolder holder, int position) {
        convert(holder, position, mData.get(position));
    }

    public abstract void convert(RecyclerAdapter.ViewHolder helper, int position, T item);

    // 返回item数量
    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }


    /**
     * 替换元素并刷新
     *
     * @param mDatas
     */
    public void refresh(List<T> mDatas) {
        this.mData = mDatas;
        this.notifyDataSetChanged();
    }

    // 内部类存储item中的控件
    public class ViewHolder extends RecyclerView.ViewHolder {

        // 避免重复findViewByid遍历。
        private final SparseArray<View> mViews = new SparseArray<View>();
        private View mConvertView;

        public ViewHolder(View convertView) {
            super(convertView);
            mConvertView = convertView;
        }

        public <T extends View> T getView(int viewId) {
            View view = mViews.get(viewId);
            if (view == null) {
                view = mConvertView.findViewById(viewId);
                mViews.put(viewId, view);
            }
            return (T) view;
        }

        public View getConvertView() {
            return mConvertView;
        }
    }

}
