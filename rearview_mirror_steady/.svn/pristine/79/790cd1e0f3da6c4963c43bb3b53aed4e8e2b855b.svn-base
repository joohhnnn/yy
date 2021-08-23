package com.txznet.music.widget;

import android.support.v7.util.DiffUtil;

import java.util.List;

/**
 * 用于做正在播放的数据的更新
 *
 * @author telen
 * @date 2018/12/6,15:30
 */
public class DiffCallback<T> extends DiffUtil.Callback {

    private List<T> mOldDatas;
    private List<T> mNewDatas;

    //传入旧数据和新数据的集合
    public DiffCallback(List<T> oldDatas, List<T> newDatas) {
        this.mOldDatas = oldDatas;
        this.mNewDatas = newDatas;
    }

    @Override
    public int getOldListSize() {
        return mOldDatas != null ? mOldDatas.size() : 0;
    }

    @Override
    public int getNewListSize() {
        return mNewDatas != null ? mNewDatas.size() : 0;
    }

    /**
     * 被DiffUtil调用，用来判断 两个对象是否是相同的Item。
     * 例如，如果你的Item有唯一的id字段，这个方法就 判断id是否相等。
     * 本例判断id字段是否一致
     */
    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        T oldT = mOldDatas.get(oldItemPosition);
        T newT = mNewDatas.get(newItemPosition);
        if (oldT == null && newT == null) {
            return true;
        } else if (oldT != null && newT != null) {
            return oldT.equals(newT);
        }
        return false;
    }

    /*
     * 被DiffUtil调用，用来检查 两个item是否含有相同的数据
     * 这个方法仅仅在areItemsTheSame()返回true时，才调用。
     * @param oldItemPosition The position of the item in the old list
     * @param newItemPosition The position of the item in the new list which replaces the
     *                        oldItem
     * @return True if the contents of the items are the same or false if they are different.
     */
    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
//        Log.d("telenewbie::", "areContentsTheSame:");
        //判断是否在播放中
//        String oldName = mOldDatas.get(oldItemPosition).getName();
//        String newName = mNewDatas.get(newItemPosition).getName();
//        Log.d("lgq", "areContentsTheSame"
//                + " " + oldName + " " + newName);
//        if (!oldName.equals(newName)) {
//            Log.d("lgq", "false");
//            return false;
//        }
        return true;
    }
}
