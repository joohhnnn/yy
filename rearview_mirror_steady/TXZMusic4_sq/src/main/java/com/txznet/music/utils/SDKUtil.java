package com.txznet.music.utils;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Looper;
import android.support.v4.content.ContextCompat;
import android.util.ArrayMap;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.AlphaAnimation;

import com.txznet.comm.remote.GlobalContext;

import java.util.HashMap;
import java.util.Map;

/******************************************
 * 类描述： 系统版本工具类 类名称：SDKUtil
 *
 * @version: 1.0
 * @author: shaoningYang
 * @time: 2015-3-20 15:17
 ******************************************/

@SuppressLint("NewApi")
public class SDKUtil {

    private SDKUtil() {
    }

    public static int getSdkInt() {
        if (VERSION.RELEASE.startsWith("1.5")) {
            return 3;
        }

        return HelperInternal.getSdkIntInternal();
    }

    private static class HelperInternal {
        private static int getSdkIntInternal() {
            return VERSION.SDK_INT;
        }
    }

    /**
     * (Android 2.2.x)
     *
     * @return >=API LEVEL 8 true，否则 false
     */
    public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inlined at compile time. This is guaranteed
        // behavior.
        return VERSION.SDK_INT >= VERSION_CODES.FROYO;
    }

    /**
     * (Android 2.3.2
     * Android 2.3.1
     * Android 2.3)
     *
     * @return >=API LEVEL 9 true，否则 false
     */
    public static boolean hasGingerbread() {
        return VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD;
    }

    /**
     * (Android 2.3.4
     * Android 2.3.3)
     *
     * @return >=API LEVEL 10 true，否则 false
     */
    public static boolean hasGingerbread_mr1() {
        return VERSION.SDK_INT >= VERSION_CODES.GINGERBREAD_MR1;
    }

    /**
     * (Android 3.0.x)
     *
     * @return >=API LEVEL 11 true，否则 false
     */
    public static boolean hasHoneycomb() {
        return VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB;
    }

    /**
     * (Android 3.1.x)
     *
     * @return >=API LEVEL 12 true，否则 false
     */
    public static boolean hasHoneycombMR1() {
        return VERSION.SDK_INT >= VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * (Android 4.1, 4.1.1)
     *
     * @return >=API LEVEL 16 true，否则 false
     */
    public static boolean hasJellyBean() {
        return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN;
    }

    /**
     * (Android 2.0)
     *
     * @return >=API LEVEL 5 true，否则 false
     */
    public static boolean hasECLAIR() {
        return VERSION.SDK_INT >= VERSION_CODES.ECLAIR;
    }

    /**
     * (Android 4.0, 4.0.1, 4.0.2)
     *
     * @return >=API LEVEL 14 true，否则 false
     */
    public static boolean hasICE_CREAM_SANDWICH() {
        return VERSION.SDK_INT >= VERSION_CODES.ICE_CREAM_SANDWICH;
    }

    /**
     * (Android 4.2, 4.2.2)
     *
     * @return >=API LEVEL 17 true，否则 false
     */
    public static boolean hasJELLY_BEAN_MR1() {
        return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR1;
    }

    /**
     * (Android 4.3)
     *
     * @return >=API LEVEL 18 true，否则 false
     */
    public static boolean hasJELLY_BEAN_MR2() {
        return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2;
    }

    /**
     * (Android 4.4, KitKat)
     *
     * @return >=API LEVEL 19 true，否则 false
     */
    public static boolean hasKITKAT() {
        return VERSION.SDK_INT >= VERSION_CODES.KITKAT;
    }

    /**
     * (Android 5.0)
     *
     * @return >=API LEVEL 21 true，否则 false
     */
    public static boolean hasLOLLIPOP() {
        return VERSION.SDK_INT > VERSION_CODES.KITKAT_WATCH;
    }

    /**
     * (Android 5.0)
     *
     * @return >=API LEVEL 22 true，否则 false
     */
    public static boolean hasLOLLIPOP_MR1() {
        return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * 是否是 (Android 5.0、5.1)  LOLLIPOP
     *
     * @return
     */
    public static boolean isLOLLIPOP() {
        int currentSDK = getSdkInt();
        int currentSDK20 = VERSION_CODES.KITKAT_WATCH;
        int currentSDK22 = VERSION_CODES.LOLLIPOP_MR1;

        if (currentSDK > currentSDK20 && currentSDK <= currentSDK22) {
            return true;
        }

        return false;
    }

    /**
     * 是否是 (Android 6.0)  Marshmallow
     *
     * @return
     */
    public static boolean hasMarshmallow() {
        return VERSION.SDK_INT > VERSION_CODES.LOLLIPOP_MR1;
    }

    /**
     * 是否支持ART，
     * <p/>
     * 调试应用程序的时候使用
     *
     * @return
     */
    public static boolean isSupportART() {
        String version = System.getProperty("java.vm.version");

        try {
            int vmCode = Integer.valueOf(version.substring(0, version.indexOf(".")));

            if (vmCode >= 2)
                return true;
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 获得设备屏幕宽度
     */
    private static int mScreenWidth;

    public static int getScreenWidth(Context context) {
        if (mScreenWidth > 0 && mScreenHeight > 0) {
            return Math.min(mScreenWidth, mScreenHeight);
        }
        context = context == null ? GlobalContext.get() : context;
        DisplayMetrics metrics = context.getResources()
                .getDisplayMetrics();
        mScreenWidth = metrics.widthPixels;
        if (mScreenHeight == 0) {
            getScreenHeight(context);
        }
        return Math.min(mScreenWidth, mScreenHeight);
    }

    /**
     * 获得设备屏幕高度
     */
    private static int mScreenHeight;

    public static int getScreenHeight(Context context) {
        if (mScreenWidth > 0 && mScreenHeight > 0) {
            return Math.max(mScreenWidth, mScreenHeight);
        }
        context = context == null ? GlobalContext.get() : context;
        DisplayMetrics metrics = context.getResources()
                .getDisplayMetrics();
        mScreenHeight = metrics.heightPixels;
        if (mScreenWidth == 0) {
            getScreenWidth(context);
        }
        return Math.max(mScreenWidth, mScreenHeight);
    }

    /**
     * 为视图设置背景
     *
     * @param view
     */
    @SuppressWarnings("deprecation")
    public static void setBackgroundDrawable(final View view, final Drawable drawable) {
        if (view == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (hasJellyBean()) {
                view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
        } else {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (hasJellyBean()) {
                        view.setBackground(drawable);
                    } else {
                        view.setBackgroundDrawable(drawable);
                    }
                }
            });
        }
    }

    /**
     * 为视图设置背景
     *
     * @param view
     */
    public static void setBackgroundDrawable(final View view, final Drawable drawable, final boolean startAlpha) {
        if (view == null) {
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            if (startAlpha) {
                startAlphaAnimationJavaCode(view);
            }
            if (hasJellyBean()) {
                view.setBackground(drawable);
            } else {
                view.setBackgroundDrawable(drawable);
            }
        } else {
            view.post(new Runnable() {
                @Override
                public void run() {
                    if (startAlpha) {
                        startAlphaAnimationJavaCode(view);
                    }
                    if (hasJellyBean()) {
                        view.setBackground(drawable);
                    } else {
                        view.setBackgroundDrawable(drawable);
                    }
                }
            });
        }
    }

    private static void startAlphaAnimationJavaCode(View view) {
        AlphaAnimation alphaAnim = new AlphaAnimation(0.5f, 1.0f);
        alphaAnim.setDuration(500);
        view.startAnimation(alphaAnim);
    }

    /**
     * 删除视图观察对象
     *
     * @param view
     * @param onGlobalLayoutListener
     */
    @SuppressWarnings("deprecation")
    public static void remove(View view,
                              OnGlobalLayoutListener onGlobalLayoutListener) {
        if (hasJellyBean()) {
            view.getViewTreeObserver().removeOnGlobalLayoutListener(
                    onGlobalLayoutListener);
        } else {
            view.getViewTreeObserver().removeGlobalOnLayoutListener(
                    onGlobalLayoutListener);
        }
    }

    /**
     * 清除View动画
     *
     * @param view
     */
    public static void clearAnimation(View view) {
        if (view == null) {
            return;
        }
        view.clearAnimation();
        if (hasHoneycomb()) {
            view.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }

    /**
     * 根据Android版本创建一个Map对象
     *
     * @return
     */
    public static Map<?, ?> createMapInstance() {
        if (hasKITKAT()) {
            return new ArrayMap<>();
        }
        return new HashMap<>();
    }

    /**
     * 校验当前app是否拥有某项权限（因android6.0以后只有在app运行时才会校验某项权限是否可用）
     *
     * @param context
     * @param permission
     * @return true为拥有该权限，false为否
     */
    @TargetApi(23)
    public static boolean checkSelfPermissionIsGranted(Context context, String permission) {
        if (hasMarshmallow()) {
            return PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(context, permission);
        }
        return true;
    }

    public static void recycleBitmap(Bitmap bitmap) {
        if (hasHoneycomb() || bitmap == null || bitmap.isRecycled()) { // android2.3以后不需要回收bitmap对象 具体参考http://androidweekly.cn/android-dev-weekly-issue-67/
            return;
        }
        bitmap.recycle();
    }

    /**
     * 获取drawable
     *
     * @param context
     * @param drawableRes
     * @param theme
     * @return
     */
    public static Drawable getDrawable(Context context, int drawableRes, Resources.Theme theme) {
        context = context == null ? GlobalContext.get() : context;
        try { // 解决24863问题
            if (hasMarshmallow()) {
                return context.getResources().getDrawable(drawableRes, theme);
            }
            return context.getResources().getDrawable(drawableRes);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return null;
    }

    /**
     * 获取资源色值
     *
     * @param context
     * @param colorRes
     * @return
     */
    public static int getColor(Context context, int colorRes) {
        return getColor(context, colorRes, null);
    }

    /**
     * 获取资源色值
     *
     * @param context
     * @param colorRes
     * @param theme
     * @return
     */
    public static int getColor(Context context, int colorRes, Resources.Theme theme) {
        context = context == null ? GlobalContext.get() : context;
        if (hasMarshmallow()) {
            return context.getResources().getColor(colorRes, theme);
        }
        return context.getResources().getColor(colorRes);
    }

    /**
     * 获取资源多状态色值
     *
     * @param context
     * @param colorsRes
     * @param theme
     * @return
     */
    public static ColorStateList getColorStateList(Context context, int colorsRes, Resources.Theme theme) {
        context = context == null ? GlobalContext.get() : context;
        if (hasMarshmallow()) {
            return context.getResources().getColorStateList(colorsRes, theme);
        }
        return context.getResources().getColorStateList(colorsRes);
    }

    /**
     * 获取当前bitmap所占字节数
     *
     * @param bitmap
     * @return
     */
    public static int getBitmapBytesCount(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        if (hasHoneycombMR1()) {
            return bitmap.getByteCount();
        }
        return bitmap.getRowBytes() * bitmap.getHeight();
    }

    /**
     * 检测当前Activity是否已经销毁
     *
     * @param activity
     * @return true为是，false为否
     */
    public static boolean isActivityDestroyed(Activity activity) {
        boolean isDestroy = false;
        if (activity == null) {
            return isDestroy;
        }
        if (SDKUtil.hasJELLY_BEAN_MR1()) {
            return isDestroy;
        }
        // 解决一个很蛋疼的问题 http://www.umeng.com/apps/28bd206837b042655f921b15/error_types/56af581b498ec9b5125029fa
        // 三星SM-W2014 Android 4.3 OS
        try {
            isDestroy = activity.isDestroyed();
        } catch (NoSuchMethodError nme) {
            nme.printStackTrace();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return isDestroy;
    }

}
