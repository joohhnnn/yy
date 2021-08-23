package com.txznet.launcher.module;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by brainBear on 2018/2/5.
 * 最基本的module接口。定义了onCreate、onCreateView等生命周期回调。这些方法有点像activity的onXxx方法，被其他类调用后就形成了生命周期。
 * 总的来说，就是这个接口就是模拟的activity。
 */

public interface IModule {
    int STATUS_FULL = 1;
    int STATUS_HALF = 2;
    int STATUS_THIRD = 3;
    int STATUS_DIALOG = 4;

    void onCreate(String data);

    View onCreateView(Context context, ViewGroup parent, int status);

    void refreshView(String data);

    void onResume();

    void onPreRemove();

    void onDestroy();

    void notifyIsNavInFocusBeforeDisplay(boolean isInFocus);
}
