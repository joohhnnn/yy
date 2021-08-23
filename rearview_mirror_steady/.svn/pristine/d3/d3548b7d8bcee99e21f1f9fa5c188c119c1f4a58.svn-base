package com.txznet.txz.util.focus_supporter;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.txznet.txz.util.focus_supporter.focusfinder.FocusManager;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusOperationPresenter;
import com.txznet.txz.util.focus_supporter.interfaces.IFocusView;
import com.txznet.txz.util.focus_supporter.log.FocusLog;
import com.txznet.txz.util.focus_supporter.strategies.ActivityProxy;
import com.txznet.txz.util.focus_supporter.strategies.DialogProxy;
import com.txznet.txz.util.focus_supporter.strategies.IContextProxy;
import com.txznet.txz.util.focus_supporter.wrappers.IFocusWrapper;

import java.util.List;

/**
 * 方控按键支持入口
 * Created by J on 2016/9/29.
 */

public class FocusSupporter {
    // supported nav buttons
    public static final int NAV_BTN_PREV = 1017;
    public static final int NAV_BTN_NEXT = 1018;

    public static final int NAV_BTN_UP = 1019;
    public static final int NAV_BTN_DOWN = 1020;
    public static final int NAV_BTN_LEFT = 1021;
    public static final int NAV_BTN_RIGHT = 1022;

    public static final int NAV_BTN_CLICK = 1123;
    public static final int NAV_BTN_LONG_CLICK = 1124;
    public static final int NAV_BTN_BACK = 1104;
    public static final int NAV_BTN_NONE = -1000;

    public static final int NAV_MODE_ONE_WAY = 1000;
    public static final int NAV_MODE_TWO_WAY = 1001;

    private IContextProxy mContextProxy;
    private FocusManager mFocusManager;
    private Drawable mIndicatorDrawable;

    // listener
    private OnFocusEventListener mFocusEventListener;

    // focus mode
    private int mNavMode = NAV_MODE_ONE_WAY;

    // inner vars
    //private int mViewCount;
    //private int mCurrentIndex = -1;
    private Object mCurrentFocus; // current view on focus
    private int[] mArrLocationCache = new int[2];
    Rect mVisibleRect = new Rect();

    public static FocusSupporter attach(Activity activity) {
        return attach(activity, NAV_MODE_ONE_WAY);
    }

    public static FocusSupporter attach(Activity activity, int mode) {
        return new FocusSupporter(activity, mode);
    }

    public static FocusSupporter attach(Dialog dialog) {
        return attach(dialog, NAV_MODE_ONE_WAY);
    }

    public static FocusSupporter attach(Dialog dialog, int mode) {
        return new FocusSupporter(dialog, mode);
    }

    public static FocusSupporter attach(IContextProxy proxy) {
        return attach(proxy, NAV_MODE_ONE_WAY);
    }

    public static FocusSupporter attach(IContextProxy proxy, int mode) {
        return new FocusSupporter(proxy, mode);
    }

    private FocusSupporter(Activity activity, int mode) {
        this.mContextProxy = new ActivityProxy(activity);
        init(mode);
    }

    private FocusSupporter(Dialog dialog, int mode) {
        this.mContextProxy = new DialogProxy(dialog);
        init(mode);
    }

    private FocusSupporter(IContextProxy proxy, int mode) {
        this.mContextProxy = proxy;
        init(mode);
    }

    private void init(int mode) {
        mContextProxy.onAttach();
        mFocusManager = new FocusManager(mode);
        mNavMode = mode;
    }

    public void detach() {
        try {
            mContextProxy.onDetach();
            mFocusManager.recycle();
            mContextProxy = null;
            mCurrentFocus = null;
            mFocusManager = null;
        } catch (Exception e) {
            FocusLog.d("detach encountered error: " + e.toString());
        }
    }

    public void setOnFocusEventListener(OnFocusEventListener listener) {
        mFocusEventListener = listener;
    }

    /**
     * 设置自定义的焦点切换规则
     * Note：target 和 next 必须都存在于焦点列表中，否则设置会失败
     *       当焦点list发生变化时，所有自定义规则都会被清空
     *
     * @param target 当前焦点
     * @param next 指定操作方向上的下一个焦点，
     * @param op 对应的操作
     * @return 设置是否成功
     */
    public boolean addRule(Object target, Object next, int op) {
        return mFocusManager.addRule(target, next, op);
    }

    /**
     * 清除自定义焦点切换规则
     *
     * @param target 当前焦点
     * @param op     需要清除的对应操作，不传全清
     */
    public void removeRule(Object target, int... op) {
        mFocusManager.removeRule(target, op);
    }

    /**
     * 设置焦点ViewList
     *
     * @param list
     * @return
     */
    public FocusSupporter setViewList(List list) {
        if (null == list) {
            return this;
        }

        return setViewList(list.toArray());
    }

    /**
     * 设置焦点ViewList
     *
     * @param list
     * @return
     */
    public FocusSupporter setViewList(Object... list) {
        mFocusManager.setFocusList(list);
        updateFocusRect();

        return this;
    }

    /**
     * 设置焦点到指定View
     *
     * @param newFocus
     * @return
     */
    public boolean setCurrentFocus(Object newFocus) {
        boolean success = mFocusManager.setCurrentFocus(newFocus);
        if (success) {
            mCurrentFocus = mFocusManager.getCurrentFocus();
        }

        updateFocusRect();
        return success;
    }



    /**
     * 获取当前焦点View
     *
     * @return View
     */
    public Object getCurrentFocus() {
        return mFocusManager.getCurrentFocus();
    }

    /**
     * 判断目标是否在方控焦点
     * <p>
     * 会忽略Wrapper带来的影响
     * e.g. 当前焦点为A，isOnFocus(new SimpleDrawableWrapper(A, ....)) 会返回true
     *
     * @param target 目标（可以是View/wrapper等）
     * @return target与当前焦点是否"相同"
     */
    public boolean isOnFocus(Object target) {
        return mFocusManager.isOnFocus(target);
    }

    /**
     * 设置焦点指示框的资源
     *
     * @param drawable
     * @return
     */
    public FocusSupporter setFocusDrawable(Drawable drawable) {
        mIndicatorDrawable = drawable;
        mContextProxy.setIndicatorDrawable(mIndicatorDrawable);

        return this;
    }

    public void performNext() {
        FocusLog.d("performNext");
        performNavOperation(NAV_BTN_NEXT);
    }

    public void performPrev() {
        FocusLog.d("performPrev");
        performNavOperation(NAV_BTN_PREV);
    }

    public void performLeft() {
        if (NAV_MODE_ONE_WAY == mNavMode) {
            performPrev();
            return;
        }

        FocusLog.d("performLeft");
        performNavOperation(NAV_BTN_LEFT);
    }

    public void performRight() {
        if (NAV_MODE_ONE_WAY == mNavMode) {
            performNext();
            return;
        }

        FocusLog.d("performRight");
        performNavOperation(NAV_BTN_RIGHT);
    }

    public void performUp() {
        if (NAV_MODE_ONE_WAY == mNavMode) {
            performPrev();
            return;
        }

        FocusLog.d("performUp");
        performNavOperation(NAV_BTN_UP);
    }

    public void performDown() {
        if (NAV_MODE_ONE_WAY == mNavMode) {
            performNext();
            return;
        }

        FocusLog.d("performDown");
        performNavOperation(NAV_BTN_DOWN);
    }

    private void performNavOperation(int op) {
        // dispatch nav event to focused view first
        if (!dispatchNavOperation(mCurrentFocus, op)) {
            mCurrentFocus = mFocusManager.performOperation(op);
        }

        updateFocusRect();
    }


    public boolean performClick() {
        FocusLog.d("performClick");
        if (null == mCurrentFocus) {
            FocusLog.d("current focus is null, skip");
            return false;
        }

        if (dispatchNavOperation(mCurrentFocus, NAV_BTN_CLICK)) {
            updateFocusRect();
            return true;
        }

        updateFocusRect();

        // 按默认逻辑处理点击事件
        View v = getViewForCurrentFocus();
        if (null != v) {
            return v.performClick();
        }

        return false;
    }

    public boolean performLongClick() {
        FocusLog.d("performLongClick");

        if (null == mCurrentFocus) {
            FocusLog.d("current focus is null, skip");
            return false;
        }

        if (dispatchNavOperation(mCurrentFocus, NAV_BTN_LONG_CLICK)) {
            updateFocusRect();
            return true;
        }

        updateFocusRect();

        // 按默认逻辑处理长按事件
        View v = getViewForCurrentFocus();
        if (null != v) {
            return v.performLongClick();
        }

        return false;
    }

    private View getViewForCurrentFocus() {
        View v = null;
        if (mCurrentFocus instanceof View) {
            v = (View) mCurrentFocus;
        }

        if (mCurrentFocus instanceof IFocusWrapper) {
            Object content = ((IFocusWrapper) mCurrentFocus).getContent();
            if (null != content) {
                v = ((View) content);
            }
        }

        return v;
    }

    public void performBack() {
        if (dispatchNavOperation(mCurrentFocus, NAV_BTN_BACK)) {
            updateFocusRect();
            return;
        }

        updateFocusRect();
        mContextProxy.performBack();
    }

    private void updateFocusRect() {
        mCurrentFocus = mFocusManager.getCurrentFocus();

        if (null == mCurrentFocus) {
            hideFocusView();
            return;
        }

        if (mCurrentFocus instanceof IFocusView) {
            IFocusView v = (IFocusView) mCurrentFocus;
            if (v.showDefaultSelectIndicator()) {
                showFocusView();
            } else {
                hideFocusView();
                return;
            }
        }

        if (mCurrentFocus instanceof IFocusWrapper) {
            final IFocusWrapper wrapper = (IFocusWrapper) mCurrentFocus;

            if (null == wrapper.getIndicatorDrawable()) {
                FocusLog.d("wrapper's indicator drawable is null");
                hideFocusView();
            }

            // update focus indicator
            wrapper.getContent().post(new Runnable() {
                @Override
                public void run() {
                    // 这里做下判断，如果焦点已经不是目标View，则跳过更新焦点指示的逻辑
                    if (null != mFocusManager && mFocusManager.getCurrentFocus() != wrapper) {
                        return;
                    }

                    final int[] indicatorSize = wrapper.getIndicatorSize();
                    if (0 == indicatorSize[0] || 0 == indicatorSize[1]) {
                        FocusLog.d("wrapper's size is 0");
                        hideFocusView();
                    }

                    final int[] indicatorLoc = wrapper.getIndicatorLocation();
                    showFocusView();
                    mContextProxy.setIndicatorDrawable(wrapper.getIndicatorDrawable());
                    mContextProxy.updateFocusIndicator(indicatorLoc[0], indicatorLoc[1], indicatorSize[0], indicatorSize[1]);
                }
            });

            return;
        }

        if (!(mCurrentFocus instanceof View)) {
            FocusLog.d("updating focus rect on something strange: " + mCurrentFocus);
            hideFocusView();
            return;
        }

        final View vCurrent = (View) mCurrentFocus;
        vCurrent.post(new Runnable() {
            @Override
            public void run() {
                try {
                    updateFocusIndicator(vCurrent);
                } catch (Exception e) {

                }
            }
        });

        // 添加LayoutChangeListener未解决View滑动问题，暂时屏蔽
        //vCurrent.addOnLayoutChangeListener(mLayoutChangeListener);
    }

    // 添加LayoutChangeListener未解决View滑动问题，暂时屏蔽
    /*private View.OnLayoutChangeListener mLayoutChangeListener = new View.OnLayoutChangeListener() {
        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
            if (mFocusManager.getCurrentFocus() != v) {
                v.removeOnLayoutChangeListener(this);
                return;
            }

            if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {
                updateFocusIndicator(v);
            }
        }
    };*/

    private void updateFocusIndicator(View v) {
        // updateFocusIndicator可能被post到View的ui线程执行，在此过程中当前焦点可能已经改变
        // 这里做下判断，如果焦点已经不是目标View，则跳过更新焦点指示的逻辑
        if (mFocusManager.getCurrentFocus() != v) {
            return;
        }

        v.getLocationOnScreen(mArrLocationCache);
        // 部分View获取VisibleRect数据有问题(ViewPager等), 暂时屏蔽此处逻辑
        /*v.getLocalVisibleRect(mVisibleRect);

        if (mVisibleRect.right == mVisibleRect.left || mVisibleRect.top == mVisibleRect.bottom) {
            FocusLog.i("target's visible size is 0");
            hideFocusView();
            return;
        }*/

        mContextProxy.setIndicatorDrawable(mIndicatorDrawable);
        showFocusView();
        // 部分View获取VisibleRect数据有问题(ViewPager等), 暂时屏蔽此处逻辑
        //mContextProxy.updateFocusIndicator(mArrLocationCache[0] + mVisibleRect.left, mArrLocationCache[1] + mVisibleRect.top, mVisibleRect.right - mVisibleRect.left, mVisibleRect.bottom - mVisibleRect.top);
        mContextProxy.updateFocusIndicator(mArrLocationCache[0], mArrLocationCache[1], v.getWidth(), v.getHeight());
    }

    private boolean dispatchNavOperation(Object v, int operation) {
        // 如果有设置OnFocusEventListener, 优先交给listener处理
        if (null != mFocusEventListener) {
            if (mFocusEventListener.onFocusOperation(operation)) {
                return true;
            }
        }

        if (null != v && v instanceof IFocusOperationPresenter) {
            return ((IFocusOperationPresenter) v).onNavOperation(operation);
        }

        return false;
    }

    private void hideFocusView() {
        mContextProxy.hideFocusIndicator();
    }

    private void showFocusView() {
        mContextProxy.showFocusIndicator();
    }

    public FocusSupporter setLogEnabled(boolean enable) {
        FocusLog.LOG_ENABLED = enable;
        return this;
    }

    public interface OnFocusEventListener {
        boolean onFocusOperation(int op);
    }
}
