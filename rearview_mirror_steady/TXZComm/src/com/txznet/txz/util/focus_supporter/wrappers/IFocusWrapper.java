package com.txznet.txz.util.focus_supporter.wrappers;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 导航按键Wrapper接口
 * Wrapper提供对默认焦点显示的自定义
 * 如焦点框资源/padding/显示位置等的修改
 *
 * Created by J on 2017/4/5.
 */

public interface IFocusWrapper {
    /**
     * 焦点框的显示位置
     * @return
     */
    int[] getIndicatorLocation();

    /**
     * 焦点框的显示大小
     * @return
     */
    int[] getIndicatorSize();

    /**
     * 焦点框的资源
     * @return
     */
    Drawable getIndicatorDrawable();

    View getContent();
}
