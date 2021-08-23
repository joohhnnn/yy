package com.txznet.txz.util.focus_supporter.wrappers;

import android.graphics.drawable.Drawable;
import android.view.View;

/**
 * 提供自定义indicator drawable的wrapper
 *
 * Created by J on 2017/4/6.
 */

public class SimpleDrawableWrapper implements IFocusWrapper {
    private View mContent;
    private Drawable mIndicatorDrawable;

    /**
     * @param view 被包裹的View
     * @param indicator indicator drawable
     */
    public SimpleDrawableWrapper(View view, Drawable indicator) {
        mContent = view;
        mIndicatorDrawable = indicator;
    }

    @Override
    public int[] getIndicatorLocation() {
        // 默认返回View在屏幕上的位置
        int[] ret = new int[2];
        mContent.getLocationOnScreen(ret);

        return ret;
    }

    @Override
    public int[] getIndicatorSize() {
        return new int[]{mContent.getWidth(), mContent.getHeight()};
    }

    @Override
    public Drawable getIndicatorDrawable() {
        return mIndicatorDrawable;
    }

    @Override
    public View getContent() {
        return mContent;
    }
}
