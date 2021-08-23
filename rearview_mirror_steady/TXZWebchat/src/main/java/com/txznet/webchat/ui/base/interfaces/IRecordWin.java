package com.txznet.webchat.ui.base.interfaces;

/**
 * 录音窗口
 * Created by J on 2016/10/17.
 */

public interface IRecordWin {
    void updateTargetInfo(String openId);

    void refreshTimeRemain(int seconds);

    boolean isShowing();

    void show();

    void dismiss();
}
