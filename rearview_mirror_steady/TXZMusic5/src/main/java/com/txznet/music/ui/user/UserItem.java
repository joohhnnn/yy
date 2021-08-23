package com.txznet.music.ui.user;

import android.view.View;

/**
 * @author telen
 * @date 2019/1/4,10:46
 */
public class UserItem {
    public int iconID;
    public String name;
    View.OnClickListener mOnClickListener;

    public UserItem(int iconID, String name, View.OnClickListener listener) {
        this.iconID = iconID;
        this.name = name;
        mOnClickListener = listener;
    }

    @Override
    public String toString() {
        return "UserItem{" +
                "icon='" + iconID + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
