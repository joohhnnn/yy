package com.txznet.launcher.widget.container;

import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.R;
import com.txznet.launcher.widget.IImage;

import static com.txznet.launcher.widget.container.ViewContainer.VIEW_TYPE_CONTENT;
import static com.txznet.launcher.widget.container.ViewContainer.VIEW_TYPE_IMAGE;

/**
 * 显示对话框模式小欧的界面。
 */
public class DialogRecordWin extends LinearLayout {
    public DialogRecordWin(Context context) {
        this(context, null);
    }

    public DialogRecordWin(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DialogRecordWin(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mWm = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
        initView();
    }

    private int mType = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
    private View mImageView;
    private ViewGroup mImageLayout;
    private View mContentView;
    private ViewGroup mContentLayout;
    // 回调
    private ViewContainer.ContainerCallback mCallback;

    private WindowManager mWm;
    private WindowManager.LayoutParams mParams = null;

    private void initView() {
        View.inflate(getContext(), R.layout.dialog_win_ly, this);
        mImageLayout = (ViewGroup) findViewById(R.id.image_ly);
        mContentLayout = (ViewGroup) findViewById(R.id.content_ly);
    }

    public DialogRecordWin setImageView(View imageView) {
        mImageView = imageView;
        checkAddImageView();
        return this;
    }

    public DialogRecordWin setContentView(View contentView) {
        mContentView = contentView;
        checkUpdateContentView();
        return this;
    }

    public DialogRecordWin setType(int type) {
        this.mType = type;
        return this;
    }

    private void checkAddImageView() {
        if (mImageLayout == null || mImageView == null) {
            return;
        }

        clearImageLayout();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT);
        mImageLayout.addView(mImageView, params);
        if (mCallback != null) {
            mCallback.onViewAdded(VIEW_TYPE_IMAGE, mImageView);
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

    private void clearContentLayout() {
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

    private void checkUpdateContentView() {
        if (mContentLayout == null || mContentView == null) {
            return;
        }
        clearContentLayout();
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mContentLayout.addView(mContentView, params);
        if (mCallback != null) {
            mCallback.onViewAdded(VIEW_TYPE_CONTENT, mContentView);
        }
    }

    public void setContainerCallback(ViewContainer.ContainerCallback callback) {
        mCallback = callback;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                getViewTreeObserver().removeOnPreDrawListener(this);
                return false;
            }
        });
    }

    private boolean mAlreadyShow;

    public void open(String text) {
        LogUtil.logd("dialog isOpen: " + mAlreadyShow);
        if (mAlreadyShow) {
            return;
        }

        mParams = new WindowManager.LayoutParams();
        mParams.type = mType;
        mParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mParams.flags = 40;
        mParams.format = PixelFormat.RGBA_8888;
        mParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;

        mWm.addView(this, mParams);
        mAlreadyShow = true;
    }

    public void dismiss() {
        LogUtil.logd("dialog dismiss: " + mAlreadyShow);
        if (mAlreadyShow) {
            clearContentLayout();
            mWm.removeView(this);
            mAlreadyShow = false;
        }
    }

    public boolean isShowing() {
        return mAlreadyShow;
    }

    /**
     * 更新声控状态
     *
     * @param state
     */
    public void updateState(int state) {
        if (mImageView == null) {
            return;
        }

        if (mImageView instanceof IImage) {
            ((IImage) mImageView).updateState(state);
        }
    }
}