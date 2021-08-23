package com.txznet.txz.component.offlinepromote;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.DeviceUtil;

public abstract class AbsFloatWindow extends LinearLayout implements IFloatWindow {
    private int mWinType = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 7;
    private WindowManager mWinManager;
    protected WindowManager.LayoutParams mLp;
    private boolean mAlreadyOpen;
    protected boolean mResumeOpen;
    protected int mWidth, mHeight;
    protected int rootWidth, rootHeight;
    private int mTouchSlop;

    public AbsFloatWindow(Context context) {
        super(context);
        mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
    }

    protected abstract View dragView();

    protected abstract void onClick(float x);

    @Override
    public boolean isShowing() {
        return mAlreadyOpen;
    }

    @SuppressLint("RtlHardcoded")
    @Override
    public void show() {
        if (mAlreadyOpen) {
            return;
        }
        mLp = new WindowManager.LayoutParams(mWinType, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
                PixelFormat.TRANSLUCENT);
        mLp.type = mWinType;
        mLp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.windowAnimations = 0;
        mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        // mLp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE; /**
        // Window flag: this window can never receive touch events. */
        mLp.format = PixelFormat.RGBA_8888;
        mLp.gravity = Gravity.LEFT | Gravity.TOP | Gravity.START;
        if (historyX == 0 && historyY == 0) {
            historyX = DeviceInfo.getScreenWidth();
            historyY = DeviceInfo.getScreenHeight() / 2;
        }
        mLp.x = historyX;
        mLp.y = historyY;
        mResumeOpen = false;
        LogUtil.logd("help show x:" + mLp.x + ",y:" + mLp.y);
        mWinManager.addView(this, mLp);
        mAlreadyOpen = true;
    }

    @Override
    public void dismiss() {
        if (mAlreadyOpen) {
            LogUtil.logd("help float dismiss");
            mWinManager.removeView(this);
            mAlreadyOpen = false;
        }
    }

    private Rect mVoiceAssistantRect = null;
    protected boolean isTouchVoiceAssistant = false;

    /**
     * 判断点击事件是否点击到指定的view上
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchViewRect(View view, int x, int y) {
        if (view.getVisibility() != View.VISIBLE) {
            return false;
        }
        if (null == mVoiceAssistantRect) {
            mVoiceAssistantRect = new Rect();
        }
        view.getDrawingRect(mVoiceAssistantRect);
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        mVoiceAssistantRect.left = location[0];
        mVoiceAssistantRect.top = location[1];
        mVoiceAssistantRect.right = mVoiceAssistantRect.right + location[0];
        mVoiceAssistantRect.bottom = mVoiceAssistantRect.bottom + location[1];
        return mVoiceAssistantRect.contains(x, y);
    }

    private float downX;
    private float downY;
    private int lastLpX;
    private int lastLpY;
    protected int historyX;
    protected int historyY;
    private boolean shouldMove;
    private Rect mCurVisiableRect = new Rect();

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchVoiceAssistant = false;
                if (isTouchViewRect(dragView(), (int) event.getRawX(), (int) event.getRawY())) {
                    isTouchVoiceAssistant = true;
                    setPressed(true);
                    downX = event.getRawX();
                    downY = event.getRawY();
                    lastLpX = mLp.x;
                    lastLpY = mLp.y;
                    getWindowVisibleDisplayFrame(mCurVisiableRect);
                    rootWidth = mCurVisiableRect.width();
                    rootHeight = mCurVisiableRect.height();
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (isTouchVoiceAssistant) {
                    if (Math.abs(event.getRawX() - downX) >= mTouchSlop
                            || Math.abs(event.getRawY() - downY) >= mTouchSlop) {
                        shouldMove = true;
                    }
                    if (shouldMove) {
//                        mLp.x = (int) (lastLpX + event.getRawX() - downX);
                        mLp.y = (int) (lastLpY + event.getRawY() - downY);

                        if (mLp.y < 0) {
                            mLp.y = 0;
                        } else if (mLp.y > rootHeight - mHeight) {
                            mLp.y = rootHeight - mHeight;
                        }

//                        if (mLp.x > rootWidth - mWidth) {
//                            mLp.x = rootWidth - mWidth;
//                        }
                        LogUtil.logd("mLp x:" + mLp.x + ",mLp y:" + mLp.y);
                        mWinManager.updateViewLayout(this, mLp);
                        historyX = mLp.x;
                        historyY = mLp.y;
                    }
                    return true;
                }
            case MotionEvent.ACTION_UP:
                if (isTouchVoiceAssistant) {
                    setPressed(false);
                    if (!shouldMove) {
                        playSoundEffect(SoundEffectConstants.CLICK);
                        onClick(downX);
                        return true;
                    }
                    shouldMove = false;
                    // 复位贴边
                    if (mLp.x < mWidth) {
                        mLp.x = 0;
                    } else if (mLp.x > rootWidth - mWidth - mWidth) {
                        mLp.x = rootWidth - mWidth;
                    } else {
                        if (mLp.x > rootWidth / 2) {
                            mLp.x = rootWidth - mWidth;
                        } else {
                            mLp.x = 0;
                        }
                    }
                    if (mLp.y < mHeight) {
                        mLp.y = 0;
                    } else if (mLp.y > rootHeight - mHeight - mHeight) {
                        mLp.y = rootHeight - mHeight;
                    }
                    mWinManager.updateViewLayout(this, mLp);
                    historyX = mLp.x;
                    historyY = mLp.y;
                    JNIHelper.logd("HelpFloatView SET x=" + mLp.x + ", y=" + mLp.y + ", w=" + mLp.width + ",h = "
                            + mLp.height);
                    onTouchEventUp(event);
                    return true;
                }
        }
        return super.onTouchEvent(event);
    }

    protected void onTouchEventUp(MotionEvent event) {
    }
}