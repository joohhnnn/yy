package com.txznet.txz.module.feedback;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.view.VoiceWaveView;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;

public class FeedbackWindowManager {

    View rootView;
    private TextView mTvTimeTip;
    private TextView mTvFeedback;
    private LinearLayout mLlFeedback;
    private boolean mShowing;
    private VoiceWaveView mVoiceWaveView;


    public enum DismissReason {
        DISMISS_NORMAL,
        DISMISS_CLICK_CANCEL,
        DISMISS_RECORD_CANCEL
    }

    public FeedbackWindowManager(Context context) {
        WindowManager windowManager = (WindowManager) GlobalContext.get().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(outMetrics);
        int widthPixels = outMetrics.widthPixels;
        int heightPixels = outMetrics.heightPixels;
        if (widthPixels > heightPixels) {
            rootView = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.dialog_feedback, null);
        } else {
            rootView = LayoutInflater.from(GlobalContext.get()).inflate(R.layout.dialog_vertical_feedback, null);
        }

        mTvTimeTip = (TextView) rootView.findViewById(R.id.tv_time_tip);
        mTvFeedback = (TextView) rootView.findViewById(R.id.tv_feedback);
        mLlFeedback = (LinearLayout) rootView.findViewById(R.id.ll_feedback);
        mVoiceWaveView = (VoiceWaveView)rootView.findViewById(R.id.view_voice);
        mTvFeedback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReportUtil.doReport(new ReportUtil.Report.Builder().setType("manualSend")
                        .putExtra("time", System.currentTimeMillis())
                        .buildCommReport());
                dismiss(DismissReason.DISMISS_NORMAL.ordinal());
            }
        });

        rootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (!(event.getRawX() > mLlFeedback.getLeft()
                        && event.getRawX() < mLlFeedback.getRight()
                        && event.getRawY() > mLlFeedback.getTop()
                        && event.getRawY() < mLlFeedback.getBottom())) {
                    dismiss(DismissReason.DISMISS_CLICK_CANCEL.ordinal());
                    ReportUtil.doReport(new ReportUtil.Report.Builder().setType("clickCancel")
                            .putExtra("time", System.currentTimeMillis())
                            .buildCommReport());
                }
                return false;
            }
        });
        rootView.setFocusable(true);
        rootView.setFocusableInTouchMode(true);
        rootView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismiss(DismissReason.DISMISS_CLICK_CANCEL.ordinal());
                }
                return true;
            }
        });
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowLayoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        mWindowLayoutParams.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
        mWindowLayoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED;
        mWindowLayoutParams.format = PixelFormat.RGBA_8888;

    }

    public void startAnimator() {
        if (!isShowing()) {
            return;
        }
        if (mVoiceWaveView != null) {
            mVoiceWaveView.startAnimator();
        }
    }

    public void cancelAnimator() {
        if (!isShowing()) {
            return;
        }
        if (mVoiceWaveView != null) {
            mVoiceWaveView.cancelAnimator();
        }
    }


    public void updateFeedback(final String text) {
        if (!isShowing()) {
            return;
        }
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mTvFeedback != null) {
                    mTvFeedback.setText(text);
                }
            }
        });
    }

    public String getFeedbackText() {
        if (mTvFeedback != null) {
            return mTvFeedback.getText().toString();
        }
        return "";
    }

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;

    public void show() {
        mShowing = true;
        GlobalContext.get().registerReceiver(mHomeListenerReceiver, mHomeFilter);
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                mTvTimeTip.setText("反馈中，请描述您的问题");
                mTvFeedback.setText("发送反馈(3s)");
                mWindowManager.addView(rootView, mWindowLayoutParams);
            }
        });
        AppLogic.removeUiGroundCallback(mUpdateTimeTipRunnable);
        AppLogic.runOnUiGround(mUpdateTimeTipRunnable, 30 * 1000);
    }

    public void dismiss(final int dismissReason) {
        LogUtil.d("Feedback mShowing" + mShowing);
        if (!mShowing) {
            return;
        }
        GlobalContext.get().unregisterReceiver(mHomeListenerReceiver);
        AppLogic.removeUiGroundCallback(mTickRunnable);
        AppLogic.removeUiGroundCallback(mUpdateTimeTipRunnable);
        mShowing = false;
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                mWindowManager.removeViewImmediate(rootView);
                if (mFeedbackWindowDismissListener != null) {
                    mFeedbackWindowDismissListener.onDismiss(dismissReason);
                }
            }
        });
    }

    public boolean isShowing() {
        return mShowing;
    }

    public interface FeedbackWindowDismissListener {
        void onDismiss(int dismissReason);
    }

    private FeedbackWindowDismissListener mFeedbackWindowDismissListener;

    public void setFeedbackWindowDismissListener(FeedbackWindowDismissListener listener) {
        mFeedbackWindowDismissListener = listener;
    }



    private Runnable mTickRunnable = new Runnable() {
        @Override
        public void run() {
            --count;
            if (isShowing() && mTvTimeTip != null) {
                mTvTimeTip.setText("反馈中，剩余时长" + count + "s");
                AppLogic.runOnUiGround(mTickRunnable, 1000);
            }
        }
    };

    private int count = 30;

    private Runnable mUpdateTimeTipRunnable = new Runnable() {
        @Override
        public void run() {
            if (isShowing() && mTvTimeTip != null) {
                mTvTimeTip.setText("反馈中，剩余时长30s");
                AppLogic.removeUiGroundCallback(mTickRunnable);
                count = 30;
                AppLogic.runOnUiGround(mTickRunnable, 1000);
            }
        }
    };

    private IntentFilter mHomeFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);

    private BroadcastReceiver mHomeListenerReceiver = new BroadcastReceiver() {
        final String SYSTEM_DIALOG_REASON_KEY = "reason";
        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);

            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)
                    && reason != null && reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                dismiss(DismissReason.DISMISS_CLICK_CANCEL.ordinal());
            }
        }
    };


}
