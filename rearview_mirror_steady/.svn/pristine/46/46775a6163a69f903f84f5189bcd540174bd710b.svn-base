package com.txznet.txz.ui.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.sp.CommonSp;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.txz.R;
import com.txznet.txz.jni.JNIHelper;

public class UpgradeFloatView extends LinearLayout {

    private WindowManager mWinManager;
    private WindowManager.LayoutParams mLp;
    private boolean mIsOpening;
    public boolean mDismiss = false;
    private boolean mIsInited = false;
    private int mWidth, mHeight;
    private int historyX = -1, historyY = -1;

    private int mTouchSlop;
    private int mRootWidth;
    private int mRootHeight;
    private int defaultX = TXZConfigManager.FT_POSITION_RIGHT;//默认x
    private int defaultY = TXZConfigManager.FT_POSITION_TOP;//默认y
    private int mType = 10;


    CircleProgressView vUpgradeProgress;
    TextView tvUpgradeProgress;
    TextView tvUpgradeName;
    FrameLayout flUpgrade;

    public UpgradeFloatView(final Context context) {
        super(context);
        mWinManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        initView(context);
    }

    private Rect mCurVisiableRect = new Rect();
    private boolean mAutoAdjust;

    public void enableAutoAdjust() {
        mAutoAdjust = true;
    }

    public void disableAutoAdjust() {
        mAutoAdjust = false;
    }

    private void initView(Context context) {
        LayoutInflater.from(context).inflate(R.layout.win_upgrade_float_view, this);
        flUpgrade = (FrameLayout) findViewById(R.id.fl_upgrade);
        vUpgradeProgress = (CircleProgressView) findViewById(R.id.v_upgrade_progress);
        tvUpgradeProgress = (TextView) findViewById(R.id.tv_upgrade_progress);
        tvUpgradeName = (TextView) findViewById(R.id.tv_upgrade_name);
        mWidth = flUpgrade.getLayoutParams().width;
        mHeight = flUpgrade.getLayoutParams().height;
        enableAutoAdjust();
        mIsInited = true;
    }

    /**
     * 设置浮动按钮的位置
     *
     * @param x
     * @param y
     */
    public void setFloatViewPosition(int x, int y) {
        defaultX = x;
        defaultY = y;
    }

    private boolean isReadyAttached;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        JNIHelper.logd("UpgradeFloatView onAttachedToWindow");
        getViewTreeObserver().addOnPreDrawListener(new OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                JNIHelper.logd("UpgradeFloatView onPreDraw");
                getViewTreeObserver().removeOnPreDrawListener(this);
                try {
                    if (!mMeasureReady) {
                        mMeasureReady = true;
                        mRootWidth = getRootView().getWidth();
                        mRootHeight = getRootView().getHeight();
                    }
                    mLp.width = mWidth;
                    mLp.height = mHeight;

                    // int cacheX = PointCache.getInstance(getContext()).getX(
                    // mRootWidth - mWidth);
                    // int cacheY = PointCache.getInstance(getContext()).getY(
                    // (mRootHeight - mHeight) / 2);

                    int cacheX = PointCache.getInstance(getContext()).getX(-1);
                    int cacheY = PointCache.getInstance(getContext()).getY(-1);
                    if (cacheX == -1 || cacheY == -1) {
                        checkPosition();
                        cacheX = defaultX;
                        cacheY = defaultY;
                    }

                    // 越界保护
                    if (cacheX < 0) {
                        cacheX = 0;
                    } else if (cacheX > mRootWidth - mWidth) {
                        cacheX = mRootWidth - mWidth;
                    }
                    if (cacheY < 0) {
                        cacheY = 0;
                    } else if (cacheY > mRootHeight - mHeight) {
                        cacheY = mRootHeight - mHeight;
                    }
                    mLp.x = cacheX;
                    mLp.y = cacheY;
                    mWinManager.updateViewLayout(UpgradeFloatView.this, mLp);
                    historyX = mLp.x;
                    historyY = mLp.y;
                    isReadyAttached = true;
                    PointCache.getInstance(getContext()).setX(historyX);
                    PointCache.getInstance(getContext()).setY(historyY);
                    JNIHelper.logd("UpgradeFloatView onPreDraw finish x=" + mLp.x
                            + ", y=" + mLp.y + ", w=" + mLp.width + ",h = "
                            + mLp.height);
                } catch (Exception e) {
                    JNIHelper
                            .loge("UpgradeFloatView onAttachedToWindow[onPreDraw] error, desc="
                                    + e.getClass()
                                    + "::"
                                    + e.getMessage()
                                    + ", isOpening=" + mIsOpening);
                }
                return false;
            }


        });
    }

    private void checkPosition() {
        if (TXZConfigManager.FT_POSITION_LEFT == defaultX) {
            defaultX = 0;
        } else if (TXZConfigManager.FT_POSITION_MIDDLE == defaultX) {
            defaultX = PointCache.getInstance(getContext()).getX(
                    (mRootWidth - mWidth) / 2);
        } else if (TXZConfigManager.FT_POSITION_RIGHT == defaultX) {
            defaultX = PointCache.getInstance(getContext()).getX(
                    mRootWidth - mWidth);
        }
        if (TXZConfigManager.FT_POSITION_TOP == defaultY) {
            defaultY = 0;
        } else if (TXZConfigManager.FT_POSITION_MIDDLE == defaultY) {
            defaultY = PointCache.getInstance(getContext()).getY(
                    (mRootHeight - mHeight) / 2);
        } else if (TXZConfigManager.FT_POSITION_BOTTOM == defaultY) {
            defaultY = PointCache.getInstance(getContext()).getY(
                    mRootHeight - mHeight);
        }
    }

    public void open() {
        if (mDismiss) {//彻底不显示
            return;
        }
        if (mIsOpening) {
            return;
        }
        isReadyAttached = false;
        if (!mMeasureReady) {
            mLp = new WindowManager.LayoutParams();
            mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + mType;
            mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
            mLp.height = WindowManager.LayoutParams.MATCH_PARENT;
            mLp.flags = 40;
            mLp.format = PixelFormat.RGBA_8888;
            mLp.gravity = Gravity.LEFT | Gravity.TOP;
            historyX = 0;
            historyY = 0;
        }
        mLp.x = historyX;
        mLp.y = historyY;
        LogUtil.logd("open");
        mWinManager.addView(this, mLp);
        mIsOpening = true;
        JNIHelper.logd("UpgradeFloatView OPEN x=" + mLp.x + ", y=" + mLp.y + ", w="
                + mLp.width + ",h = " + mLp.height);
    }

    private boolean mMeasureReady = false; // 第一次打开时先测量出可用区域

    public void close() {
        if (mIsOpening) {
            mWinManager.removeView(this);
            mIsOpening = false;
        }
    }


    private float downX, downY;
    private int lastLpX, lastLpY;
    private boolean shouldMove;
    private long lastLaunch = 0;
    private static long CLICK_INTERVAL_LIMIT = -1;

    public void setClickInteval(long interval) {
        CLICK_INTERVAL_LIMIT = interval;
    }

    public void setWinType(int type) {
        LogUtil.logd("setWinType :" + type);
        mType = type;
        if (mLp != null && mWinManager != null) {
            mWinManager.removeView(this);
            mLp.type = mType;
            mWinManager.addView(this, mLp);
        }
    }

    private Rect mVoiceAssistantRect = null;
    private boolean isTouchVoiceAssistant = false;

    /**
     * 判断点击事件是否点击到指定的view上
     *
     * @param view
     * @param x
     * @param y
     * @return
     */
    private boolean isTouchViewRect(View view, int x, int y) {
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


    public void notifyProgressChanged(int progress,String name){
        this.mProgress = progress;
        this.mShowName = name;
        if (Looper.myLooper() == Looper.getMainLooper()) {
            upgradeProgress.run();
        } else {
            removeCallbacks(upgradeProgress);
            post(upgradeProgress);
        }
    }

    int mProgress = 0;
    String mShowName = "";

    private Runnable upgradeProgress = new Runnable() {
        @Override
        public void run() {
            if (tvUpgradeProgress != null && vUpgradeProgress != null && tvUpgradeName != null) {
                tvUpgradeProgress.setText(""+mProgress);
                vUpgradeProgress.setProgress(mProgress);
                tvUpgradeName.setText(mShowName);
            }
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!isReadyAttached) {
            return super.onTouchEvent(event);
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isTouchVoiceAssistant = false;
                if (isTouchViewRect(flUpgrade, (int) event.getRawX(), (int) event.getRawY())) {
                    isTouchVoiceAssistant = true;
                    setPressed(true);
                    downX = event.getRawX();
                    downY = event.getRawY();
                    lastLpX = mLp.x;
                    lastLpY = mLp.y;
                    if (mAutoAdjust) {
                        getWindowVisibleDisplayFrame(mCurVisiableRect);
                        mRootWidth = mCurVisiableRect.width();
                        mRootHeight = mCurVisiableRect.height();
                    }
                    return true;
                }
            case MotionEvent.ACTION_MOVE:
                if (isTouchVoiceAssistant) {
                    if (Math.abs(event.getRawX() - downX) >= mTouchSlop
                            || Math.abs(event.getRawY() - downY) >= mTouchSlop) {
                        shouldMove = true;
                    }
                    if (shouldMove) {
                        mLp.x = (int) (lastLpX + event.getRawX() - downX);
                        mLp.y = (int) (lastLpY + event.getRawY() - downY);

                        if (mLp.x < 0) {
                            mLp.x = 0;
                        } else if (mLp.x > mRootWidth - mWidth) {
                            mLp.x = mRootWidth - mWidth;
                        }
                        if (mLp.y < 0) {
                            mLp.y = 0;
                        } else if (mLp.y > mRootHeight - mHeight) {
                            mLp.y = mRootHeight - mHeight;
                        }
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
                        long now = SystemClock.elapsedRealtime();
                        if (now - lastLaunch < CLICK_INTERVAL_LIMIT) {
                            return true;
                        }
//		            playSoundEffect(SoundEffectConstants.CLICK);
                        JNIHelper.logd("UpgradeFloatView doLaunch");
//		            LaunchManager.getInstance().launchWithRecord();
                        lastLaunch = now;
                        return true;
                    }
                    shouldMove = false;
                    // 复位贴边
                    if (mLp.x < mWidth / 2) {
                        mLp.x = 0;
                    } else if (mLp.x > mRootWidth - mWidth - mWidth / 2) {
                        mLp.x = mRootWidth - mWidth;
                    }
                    if (mLp.y < mHeight / 2) {
                        mLp.y = 0;
                    } else if (mLp.y > mRootHeight - mHeight - mHeight / 2) {
                        mLp.y = mRootHeight - mHeight;
                    }
                    mWinManager.updateViewLayout(this, mLp);
                    historyX = mLp.x;
                    historyY = mLp.y;
                    PointCache.getInstance(getContext()).setX(historyX);
                    PointCache.getInstance(getContext()).setY(historyY);
                    JNIHelper.logd("UpgradeFloatView SET x=" + mLp.x + ", y=" + mLp.y
                            + ", w=" + mLp.width + ",h = " + mLp.height);
                    return true;
                }
        }
        return super.onTouchEvent(event);
    }


    private static class PointCache extends CommonSp {
        private static final String SP_NAME = "float_view_point_cache_upgrade";
        private static PointCache sInstance;

        protected PointCache(Context context) {
            super(context, SP_NAME);
        }

        public static PointCache getInstance(Context context) {
            if (sInstance == null) {
                synchronized (PointCache.class) {
                    if (sInstance == null) {
                        sInstance = new PointCache(context);
                    }
                }
            }
            return sInstance;
        }

        private static final String KEY_X = "x";
        private static final String KEY_Y = "y";

        public int getX(int defVal) {
            return getValue(KEY_X, defVal);
        }

        public void setX(int x) {
            setValue(KEY_X, x);
        }

        public int getY(int defVal) {
            return getValue(KEY_Y, defVal);
        }

        public void setY(int y) {
            setValue(KEY_Y, y);
        }
    }
}
