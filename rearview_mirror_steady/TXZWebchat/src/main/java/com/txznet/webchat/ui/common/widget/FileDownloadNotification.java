package com.txznet.webchat.ui.common.widget;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.github.lzyzsd.circleprogress.DonutProgress;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.ResourceActionCreator;
import com.txznet.webchat.comm.plugin.model.WxMessage;
import com.txznet.webchat.log.L;
import com.txznet.webchat.stores.WxResourceStore;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 微信文件下载提示窗
 * Created by J on 2017/6/14.
 */

public class FileDownloadNotification extends RelativeLayout {
    @Bind(R.id.rl_file_notification_cancel_container)
    RelativeLayout mRlCancel;
    @Bind(R.id.view_file_notification_progress)
    DonutProgress mViewProgress;

    private static final String LOG_TAG = "FileDownloadNotification";

    // 用于支持拖拽的位置记录
    private int x = 100;
    private int y = 100;
    private float mLastX;
    private float mLastY;
    // 一个触摸流程中down事件位置, 用于检测点击
    private float mTouchX;
    private float mTouchY;

    private boolean bShowing = false;
    private WindowManager mWinManager;
    private boolean bCancelShowing = false;
    private WxMessage mDownloadingMsg;
    private WindowManager.LayoutParams mLayoutParam;

    private static FileDownloadNotification sInstance;

    public static FileDownloadNotification getInstance() {
        if (null == sInstance) {
            synchronized (FileDownloadNotification.class) {
                if (null == sInstance) {
                    sInstance = new FileDownloadNotification(GlobalContext.get());
                }
            }
        }

        return sInstance;
    }

    private FileDownloadNotification(Context context) {
        super(context);
        View v = LayoutInflater.from(getContext()).inflate(R.layout.layout_file_download_notification, this);
        ButterKnife.bind(this, v);

        init();
        initTheme();
    }

    private void init() {
        mWinManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);

        mViewProgress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bCancelShowing) {
                    mRlCancel.setVisibility(GONE);
                    bCancelShowing = false;
                } else {
                    mRlCancel.setVisibility(VISIBLE);
                    bCancelShowing = true;
                }
            }
        });

        mViewProgress.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        mTouchX = mLastX = event.getRawX();
                        mTouchY = mLastY = event.getRawY();
                        break;

                    case MotionEvent.ACTION_MOVE:
                        updateLayoutParam(event.getRawX(), event.getRawY());
                        break;

                    case MotionEvent.ACTION_UP:
                        float dx = event.getRawX() - mTouchX;
                        float dy = event.getRawY() - mTouchY;

                        if (dx < 1.5 && dy < 1.5) {
                            v.performClick();
                        }

                        break;
                }

                return true;
            }
        });

        mRlCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mDownloadingMsg) {
                    ResourceActionCreator.get().cancelDownloadFile(mDownloadingMsg);
                    dismiss();
                }
            }
        });
    }

    private void initTheme() {
        mViewProgress.setFinishedStrokeColor(getResources().getColor(R.color.color_file_notification_pb_finished));
        mViewProgress.setUnfinishedStrokeColor(getResources().getColor(R.color.color_file_notification_pb_unfinished));
    }

    private void updateLayoutParam(float x, float y) {
        int dx = (int) (x - mLastX);
        int dy = (int) (y - mLastY);
        mLastX = x;
        mLastY = y;

        mLayoutParam.x += dx;
        mLayoutParam.y += dy;
        mWinManager.updateViewLayout(this, mLayoutParam);
    }

    public void show(WxMessage msg) {
        if (bShowing) {
            return;
        }

        mDownloadingMsg = msg;
        WxResourceStore.get().register(this);
        bShowing = true;
        mLayoutParam = new WindowManager.LayoutParams();
        mLayoutParam.type = WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
        mLayoutParam.width = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParam.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLayoutParam.gravity = Gravity.LEFT | Gravity.TOP;
        mLayoutParam.x = x;
        mLayoutParam.y = y;
        mLayoutParam.format = PixelFormat.RGBA_8888;
        mLayoutParam.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL;
        mLayoutParam.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        mWinManager.addView(this, mLayoutParam);

        bCancelShowing = false;
        mRlCancel.setVisibility(View.GONE);
        mViewProgress.setProgress(0);
    }

    public void dismiss() {
        if (bShowing) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    bShowing = false;
                    mDownloadingMsg = null;
                    try {
                        mWinManager.removeViewImmediate(FileDownloadNotification.this);
                        WxResourceStore.get().unregister(FileDownloadNotification.this);
                    } catch (Exception e) {
                        L.e(LOG_TAG, "error while dimiss: " + e.toString());
                    }
                }
            }, 0);

        }
    }

    public void updateProgress(final float progress) {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                mViewProgress.setProgress((int) (progress));
            }
        }, 0);
    }
}
