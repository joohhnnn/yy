package com.txznet.txz.util.focus_supporter.wrappers;

import android.graphics.drawable.Drawable;
import android.view.View;


/**
 * 提供设置indicator padding的wrapper
 * Created by J on 2017/4/5.
 */
public class SimplePaddingWrapper implements IFocusWrapper {

    private View mContent;
    private Drawable mIndicatorDrawable;
    private int[] mPaddings;

    /**
     * @param view 被包裹的View
     * @param indicator indicator drawable
     * @param padding padding数组，4个元素，顺序： l,t,r,b
     */
    public SimplePaddingWrapper(View view, Drawable indicator, int[] padding) {
        mContent = view;
        mIndicatorDrawable = indicator;
        mPaddings = padding;
    }

    @Override
    public int[] getIndicatorLocation() {
        // 默认返回View在屏幕上的位置
        int[] ret = new int[2];
        mContent.getLocationOnScreen(ret);
        // 计算padding
        ret[0] -= mPaddings[0];
        ret[1] -= mPaddings[1];

        return ret;
    }

    @Override
    public int[] getIndicatorSize() {
        return new int[]{mContent.getWidth() + mPaddings[0] + mPaddings[2], mContent.getHeight() + mPaddings[1] + mPaddings[3]};
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
