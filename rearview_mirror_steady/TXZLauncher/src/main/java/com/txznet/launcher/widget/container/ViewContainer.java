package com.txznet.launcher.widget.container;

import android.content.Context;
import android.os.Looper;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.util.LogUtil;

/**
 * Created by TXZ-METEORLUO on 2018/3/14.
 * 定义了界面上要有三个view,分别表示内容、形象、状态栏。
 */

public abstract class ViewContainer extends RelativeLayout {
    public static final int VIEW_TYPE_CONTENT = 1;
    public static final int VIEW_TYPE_IMAGE = 2;
    public static final int VIEW_TYPE_TIPS = 3;
    public static final int VIEW_TYPE_STATUS_BAR = 4;

    public interface ContainerCallback {
        void onViewPreRemove(int type, View view);

        void onViewAdded(int type, View view);
    }

    // 回调
    private ContainerCallback mCallback;
    // 内容
    protected View mContentView;
    // 形象
    protected View mImageView;
    //状态栏
    protected View mStatusBar;

    public ViewContainer(Context context) {
        this(context, null);
    }

    public ViewContainer(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ViewContainer(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    protected void init() {
        prepareViewContainer();
    }

    public void rebuildContainer() {
        prepareViewContainer();
        activeView();
    }

    public void setContainerCallback(ContainerCallback callback) {
        mCallback = callback;
    }

    public void setViewGroupBackground(final int resId) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            setBackgroundResource(resId);
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    setBackgroundResource(resId);
                }
            });
        }
    }

    protected void prepareViewContainer() {
        LogUtil.logd("ViewContainer prepareViewContainer");
        removeAllViews();
        prepareStatusBarLayout();
        prepareImageLayout();
        prepareContentLayout();
    }

    private void prepareContentLayout() {
        if (mContentLayout == null) {
            mContentLayout = createContentLayout();
        }
        LayoutParams params = createContentLayoutParams();
        if (params == null) {
            return;
        }
        addView(mContentLayout, params);
    }

    private void prepareImageLayout() {
        if (mImageLayout == null) {
            mImageLayout = createImageLayout();
        }
        LayoutParams params = createImageLayoutParams();
        if (params == null) {
            return;
        }
        addView(mImageLayout, params);
    }

    public void updateContentLayoutParams(int bottomPadding) {
        if (mContentLayout == null) {
            return;
        }
        ViewGroup.LayoutParams params = mContentLayout.getLayoutParams();
        if (params != null && params instanceof LayoutParams) {
            LayoutParams lp = (LayoutParams) params;
            lp.setMargins(lp.leftMargin, lp.topMargin, lp.rightMargin, bottomPadding);
        }
    }

    private void prepareStatusBarLayout(){
        if (mStatusBarLayout == null) {
            mStatusBarLayout = createStatusBarLayout();
        }
        LayoutParams params = createStatusBarLayoutParams();
        if (params == null) {
            return;
        }
        addView(mStatusBarLayout, params);
    }

    /**
     * 清空显示的视图
     */
    public void clearLayout() {
        clearContentLayout();
        clearImageLayout();
    }

    public void activeView() {
        LogUtil.logd("activeView");
        activeContentView();
        activeImageView();
    }

    public ViewContainer setContentView(View view) {
        mContentView = view;
        activeContentView();
        return this;
    }

    public ViewContainer setImageView(View view) {
        mImageView = view;
        activeImageView();
        return this;
    }

    public ViewContainer setStatusBar(View view) {
        mStatusBar = view;
        activeStatusBar();
        return this;
    }

    private void activeContentView() {
        LogUtil.logd("activeContentView");
        if (mContentLayout == null || mContentView == null) {
            return;
        }
        clearContentLayout();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            contentLayoutAddView();
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    contentLayoutAddView();
                }
            });
        }
        if (mCallback != null) {
            mCallback.onViewAdded(VIEW_TYPE_CONTENT, mContentView);
        }
    }

    protected void contentLayoutAddView() {
        ViewGroup.LayoutParams params = mContentView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        ViewParent viewParent = mContentView.getParent();
        if (viewParent != null) {
            if (viewParent instanceof ViewGroup) {
                ((ViewGroup) viewParent).removeView(mContentView);
            }
        }
        mContentLayout.addView(mContentView, params);
    }

    private void activeImageView() {
        LogUtil.logd("activeImageView");
        if (mImageLayout == null || mImageView == null) {
            return;
        }
        clearImageLayout();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            imageLayoutAddView();
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    imageLayoutAddView();
                }
            });
        }
        if (mCallback != null) {
            mCallback.onViewAdded(VIEW_TYPE_IMAGE, mImageView);
        }
    }

    protected void imageLayoutAddView() {
        ViewGroup.LayoutParams params = mImageView.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        mImageLayout.addView(mImageView, params);
    }

    private void activeStatusBar() {
        LogUtil.logd("activeStatusBar");
        if (mStatusBarLayout == null || mStatusBar == null) {
            return;
        }
        clearStatusBarLayout();
        if (Looper.myLooper() == Looper.getMainLooper()) {
            statusBarLayoutAddView();
        } else {
            post(new Runnable() {
                @Override
                public void run() {
                    statusBarLayoutAddView();
                }
            });
        }
        if (mCallback != null) {
            mCallback.onViewAdded(VIEW_TYPE_STATUS_BAR, mStatusBar);
        }
    }

    protected void statusBarLayoutAddView() {
        ViewGroup.LayoutParams params = mStatusBar.getLayoutParams();
        if (params == null) {
            params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        mStatusBarLayout.addView(mStatusBar, params);
    }

    public void clearContentLayout() {
        if (mContentLayout != null && mContentLayout.getChildCount() > 0) {
            View view = mContentLayout.getChildAt(0);
            if (mCallback != null) {
                mCallback.onViewPreRemove(VIEW_TYPE_CONTENT, view);
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mContentLayout.removeAllViews();
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mContentLayout.removeAllViews();
                    }
                });
            }
        }
    }

    private void clearImageLayout() {
        if (mImageLayout != null && mImageLayout.getChildCount() > 0) {
            View view = mImageLayout.getChildAt(0);
            if (mCallback != null) {
                mCallback.onViewPreRemove(VIEW_TYPE_IMAGE, view);
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mImageLayout.removeAllViews();
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mImageLayout.removeAllViews();
                    }
                });
            }
        }
    }

    private void clearStatusBarLayout() {
        if (mStatusBarLayout != null && mStatusBarLayout.getChildCount() > 0) {
            View view = mStatusBarLayout.getChildAt(0);
            if (mCallback != null) {
                mCallback.onViewPreRemove(VIEW_TYPE_STATUS_BAR, view);
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                mStatusBarLayout.removeAllViews();
            } else {
                post(new Runnable() {
                    @Override
                    public void run() {
                        mStatusBarLayout.removeAllViews();
                    }
                });
            }
        }
    }

    public View getChildView(int type) {
        View view = null;
        switch (type) {
            case VIEW_TYPE_CONTENT:
                view = mContentView;
                break;
            case VIEW_TYPE_IMAGE:
                view = mImageView;
                break;
            case VIEW_TYPE_STATUS_BAR:
                view = mStatusBar;
                break;
        }
        return view;
    }

    protected ViewGroup mImageLayout = null;
    protected ViewGroup mContentLayout = null;
    protected ViewGroup mStatusBarLayout = null;

    protected ViewGroup createContentLayout() {
        FrameLayout viewGroup = new FrameLayout(getContext());
        viewGroup.setId(View.generateViewId());
        return viewGroup;
    }

    protected ViewGroup createImageLayout() {
        FrameLayout viewGroup = new FrameLayout(getContext());
        viewGroup.setId(View.generateViewId());
        viewGroup.setClipChildren(false);
        return viewGroup;
    }

    protected ViewGroup createStatusBarLayout() {
        FrameLayout viewGroup = new FrameLayout(getContext());
        viewGroup.setId(View.generateViewId());
        return viewGroup;
    }

    protected abstract LayoutParams createImageLayoutParams();

    protected abstract LayoutParams createContentLayoutParams();

    protected abstract LayoutParams createStatusBarLayoutParams();
}