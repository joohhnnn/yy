package com.txznet.music.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.ImageView;


/******************************************
 * 类描述： 处理View相关工具类 类名称：ViewUtil
 *
 * @version: 1.0
 * @author: shaoningYang
 * @time: 2015-12-5 14:43
 ******************************************/
public final class ViewUtil {
    private ViewUtil() {
    }

    /**
     * 设置当前是否需要隐藏
     *
     * @param view
     * @param visibility
     */
    public static void setViewVisibility(View view, int visibility) {
        if (view != null && view.getVisibility() != visibility) {
            view.setVisibility(visibility);
        }
    }

    /**
     * 释放View上的相关资源
     *
     * @param view
     */
    public static void releaseViewResource(View view) {
        if (view == null) {
            return;
        }
        Drawable drawable = view.getBackground();
        if (drawable != null) {
            drawable.setCallback(null);
        }
        SDKUtil.setBackgroundDrawable(view, null);
    }

    /**
     * 释放ImageView上的相关资源
     *
     * @param imageView
     */
    public static void releaseImageViewResource(ImageView imageView) {
        if (imageView == null) {
            return;
        }
        Drawable drawable = imageView.getDrawable();
        if (drawable != null) {
            drawable.setCallback(null);
        }
        imageView.setImageDrawable(null);
    }

    /**
     * activity里不同fragment的theme获取的layoutInflater 不影响别的fragment的theme
     * 这个方法在fragmengt的oncreatView里调用
     *
     * @param theme
     * @return
     */
    public static LayoutInflater getThemeLayoutInflater(Activity activity, LayoutInflater inflater, int theme) {

        //使用ContextThemeWrapper通过目标Theme生成一个新的Context
        Context ctxWithTheme = new ContextThemeWrapper(
                activity.getApplicationContext(), theme);

        //通过生成的Context创建一个LayoutInflater
        return inflater.cloneInContext(ctxWithTheme);
    }

    public static boolean isVisible(View view) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 扩大View的触摸和点击响应范围,最大不超过其父View范围
     *
     * @param view
     * @param top
     * @param bottom
     * @param left
     * @param right
     */
    public static void expandViewTouchDelegate(final View view, final int top,
                                               final int bottom, final int left, final int right) {
        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                view.setEnabled(true);
                view.getHitRect(bounds);

                bounds.top -= top;
                bounds.bottom += bottom;
                bounds.left -= left;
                bounds.right += right;

                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }

    /**
     * 还原View的触摸和点击响应范围,最小不小于View自身范围
     *
     * @param view
     */
    public static void restoreViewTouchDelegate(final View view) {

        ((View) view.getParent()).post(new Runnable() {
            @Override
            public void run() {
                Rect bounds = new Rect();
                bounds.setEmpty();
                TouchDelegate touchDelegate = new TouchDelegate(bounds, view);

                if (View.class.isInstance(view.getParent())) {
                    ((View) view.getParent()).setTouchDelegate(touchDelegate);
                }
            }
        });
    }
}
