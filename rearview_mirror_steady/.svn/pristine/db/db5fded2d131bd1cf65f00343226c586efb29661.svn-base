package com.txznet.music.ui.webchatpush;

import android.support.annotation.Nullable;
import android.support.v7.util.DiffUtil;
import android.util.Log;

import com.txznet.music.Constant;
import com.txznet.music.data.entity.PushItem;

import java.util.List;

/**
 * 用于做正在播放的数据的更新
 *
 * @author telen
 * @date 2018/12/6,15:30
 */
public class DiffPushItemCallback extends DiffUtil.Callback {

    private List<PushItem> mOldDatas;
    private List<PushItem> mNewDatas;

    //传入旧数据和新数据的集合
    public DiffPushItemCallback(List<PushItem> oldDatas, List<PushItem> newDatas) {
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
        boolean is = mOldDatas.get(oldItemPosition).id == mNewDatas.get(newItemPosition).id;
        Log.d(Constant.LOG_TAG_UI_DEBUG, "areItemsTheSame " + oldItemPosition + " " + newItemPosition + " " + is);
        return is;
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
        Log.d(Constant.LOG_TAG_UI_DEBUG,"areContentsTheSame:");
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

    /**
     * areItemsTheSame()返回true而areContentsTheSame()返回false，也就是说两个对象代表的数据是一条，但是内容更新了。
     *
     * @param oldItemPosition
     * @param newItemPosition
     * @return
     */
    @Nullable
    @Override
    public Object getChangePayload(int oldItemPosition, int newItemPosition) {
        Log.d(Constant.LOG_TAG_UI_DEBUG,"getChangePayload");
//        String oldItem = mOldDatas.get(oldItemPosition).getName();
//        String newItem = mNewDatas.get(newItemPosition).getName();
//        Bundle bundle = new Bundle();
//        if (!oldItem.equals(newItem)) {
//            bundle.putString("name", newItem);
//        }
//
//        if (bundle.size() == 0) {
            return null;
//        }
//        Log.d("lgq", "getChangePayload");
//        return bundle;
    }
}
