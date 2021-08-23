package com.txznet.txz.util.focus_supporter.strategies;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 方控支持的Context代理
 * NavBtnSupporter只负责客户端接口的提供和基本事件的处理中转
 * 具体需要依赖Context的工作由ContextProxy完成，如焦点提示的显示/更新、返回事件
 * 的处理等
 *
 * Created by J on 2016/11/13.
 */

public interface IContextProxy {
    // init & ~init
    void onAttach();
    void onDetach();

    // new interfaces
    void updateFocusIndicator(int l, int t, int r, int b);
    void showFocusIndicator();
    void hideFocusIndicator();

    View getViewById(int id);
    void performBack();

    void setIndicatorDrawable(Drawable drawable);
}
