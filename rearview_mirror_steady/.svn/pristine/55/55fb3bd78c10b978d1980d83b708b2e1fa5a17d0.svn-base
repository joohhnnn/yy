package com.txznet.comm.ui.theme.test.utils;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.AbsListView;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * function 通用的适配器
 * version 1.0
 * Created by XiaoLin
 * Created at XiaoLin on 2017/10/2.
 */

public abstract class LouAdapter<T> extends BaseAdapter {

    public static final String TAG = "LouAdapter";

    private ArrayList<T> mLists = new ArrayList<>();
    private Context mContext;
    private AbsListView mListView;
    private int mLayoutId;

    public LouAdapter(Context context, AbsListView listView, int layoutId) {
        mContext = context;
        mListView = listView;
        mLayoutId = layoutId;
    }

    @Override
    public int getCount() {
        return mLists.size();
    }

    @Override
    public T getItem(int position) {
        return mLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return updateView(position, convertView);
    }

    public AbsListView getBindListView() {
        return mListView;
    }

    /**
     * contain 来表示是否已经包含元素；（可能需要重写，如果允许重复的话，就不必要重写了）；
     *
     * @param o
     * @return
     */
    protected boolean contain(T o) {
        if (mLists.contains(o)) {
            return true;
        }
        return false;
    }

    /**
     * 用数据来更新视图
     *
     * @param position
     * @param convertView
     * @return
     */
    private View updateView(int position, View convertView) {
        LouHolder holder = LouHolder.getInstance(mContext, mLayoutId, convertView);
        assign(position, holder, getItem(position));
        return holder.getConvertView();
    }

    /**
     * 当更改了某一个Item之后，可以通过updateView(position);的方式只更新这一个Item；
     *
     * @param position
     */
    public void updateView(int position) {
        View convertView = getIndexView(position);
        updateView(position, convertView);
    }

    /**
     * @param position
     * @param holder
     * @param t
     */
    protected abstract void assign(int position, LouHolder holder, T t);


    /**
     * 获取可见元素的View
     *
     * @param position
     * @return
     */
    public View getIndexView(int position) {
        int currentIndex = position - mListView.getFirstVisiblePosition();
        if (currentIndex < 0 || currentIndex >= mLists.size()) {
            return null;
        }
        return mListView.getChildAt(currentIndex);
    }

    public void addItem(T o, boolean filter) {
        if (filter && contain(o)) {
            return;
        }
        mLists.add(o);
        updateChange();
    }

    /**
     * 默认过滤添加的元素；
     *
     * @param o
     */
    public void addItem(T o) {
        addItem(o, true);
    }

    public List<T> getList() {
        return mLists;
    }

    /**
     * 初始化元素
     *
     * @param list
     */
    public void initList(List<T> list) {
        mLists.clear();
        mLists.addAll(list);
        updateChange();
    }

    public void deleteItem(T o) {
        mLists.remove(o);
        updateChange();
    }

    public void deleteItem(int position) {
        mLists.remove(position);
        updateChange();
    }

    /**
     * 高度变为0后删除元素；
     * 有bug
     * @param position
     */
    @Deprecated
    public void deleteItemWithAnim(final int position) {
        final View view = getIndexView(position);
        final int initHeight = view.getMeasuredHeight();
        Animation anim = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    // 高度为0时删除元素，并更新 adapter
                    if (view.getTag() instanceof LouHolder) {
                        ((LouHolder) view.getTag()).mark = LouHolder.MARK.DELETE;
                        deleteItem(position);
                        Log.e(TAG, "删除一行:"+position);
                    }
                } else {
                    // 不断的改变高度，直到高度为0；
                    view.getLayoutParams().height = (int)(initHeight - initHeight * interpolatedTime);
                    view.requestLayout();
                }
            }
        };
        anim.setDuration(300);
        view.startAnimation(anim);
        updateChange();
    }

    public void updateChange() {
        notifyDataSetChanged();
    }

    // -------- 添加 choice调用
    public void clearChoice() {
        mListView.clearChoices();
        updateChange();  // 此句是必须的，否则界面无法更新；
        mListView.post(new Runnable() {
            @Override
            public void run() {
                // 注意需要使用Runnable才能生效；
                // 参考资料[ListView selection remains persistent after exiting choice mode]
                // (http://stackoverflow.com/questions/9754170/listview-selection-remains-persistent-after-exiting-choice-mode)
                mListView.setChoiceMode(AbsListView.CHOICE_MODE_NONE);
            }
        });
    }

    public void setChoiceMode(int mode) {
        mListView.setChoiceMode(mode);
    }

    public int getChoiceMode() {
        return mListView.getChoiceMode();
    }

    public void clear() {
        mLists.clear();
        updateChange();
    }


}