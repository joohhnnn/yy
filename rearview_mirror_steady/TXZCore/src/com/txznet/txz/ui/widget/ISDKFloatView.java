package com.txznet.txz.ui.widget;

/**
 * Created by ASUS User on 2018/10/8.
 */

public interface ISDKFloatView {
    void open();

    void close();

    void showTestFlag(String text);

    void setImageBitmap(String mImageNormal, String mImagePressed);

    void setFloatViewPosition(int mPositionX, int mPositionY);

    void enableAutoAdjust();

    void disableAutoAdjust();

    void setClickInteval(long mClickInteval);

    void setFloatToolSize(int mFloatToolWidth, int mFloatToolHeight);

    void setWinType(int mWinType);

    void setDismiss(boolean mDismiss);
}
