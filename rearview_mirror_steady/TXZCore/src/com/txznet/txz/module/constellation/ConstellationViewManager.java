package com.txznet.txz.module.constellation;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.ReverseObservable;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.view.defaults.ConstellationMatchingView;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.module.wakeup.WakeupManager;

import java.util.ArrayList;
import java.util.List;

public class ConstellationViewManager {

    private ImageView mIvName;
    private TextView mTvName;
    private List<ImageView> mScoreImageViews;
    View rootView;

    public void setOnDismissListener(
            final OnDismissListener onDismissListener) {
        mOnDismissListener = onDismissListener;
    }

    private OnDismissListener mOnDismissListener;
    interface OnDismissListener {
        void onDismiss();
    }

    public ConstellationViewManager(Context context) {
        rootView = LayoutInflater.from(context).inflate(R.layout.constellation_fortune, null);
        mIvName = (ImageView)rootView.findViewById(R.id.iv_name);
        mTvName = (TextView)rootView.findViewById(R.id.tv_name);

        mScoreImageViews = new ArrayList<ImageView>();
        mScoreImageViews.add((ImageView) rootView.findViewById(R.id.iv_score_1));
        mScoreImageViews.add((ImageView) rootView.findViewById(R.id.iv_score_2));
        mScoreImageViews.add((ImageView) rootView.findViewById(R.id.iv_score_3));
        mScoreImageViews.add((ImageView) rootView.findViewById(R.id.iv_score_4));
        mScoreImageViews.add((ImageView) rootView.findViewById(R.id.iv_score_5));
        TextView tvClose = (TextView)rootView.findViewById(R.id.tv_close);
        tvClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        mWindowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        mWindowLayoutParams = new WindowManager.LayoutParams();
        mWindowLayoutParams.width = (int) LayouUtil.getDimen("x625");
        mWindowLayoutParams.height = (int) (int)LayouUtil.getDimen("y80");
        mWindowLayoutParams.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 10;
        mWindowLayoutParams.flags = 40;
        mWindowLayoutParams.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mWindowLayoutParams.format = PixelFormat.RGBA_8888;
    }

    public void updateView(String name, int level, String type) {
        mTvName.setText(name + type);
        mIvName.setImageDrawable(LayouUtil.getDrawable(ConstellationMatchingView.getConstellationPictureByName(name)));

        for (int i = 0; i < level; i++) {
            mScoreImageViews.get(i).setImageResource(R.drawable.star_enable);
        }

        for (int i = level; i < mScoreImageViews.size(); i++) {
            mScoreImageViews.get(i).setImageResource(R.drawable.star_disable);
        }
    }

    private WindowManager mWindowManager;
    private WindowManager.LayoutParams mWindowLayoutParams;
    private boolean mShowing = false;

    public void show() {
        mShowing = true;
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                useAsrWakeup();
                mWindowManager.addView(rootView, mWindowLayoutParams);
            }
        });
        GlobalObservableSupport.getRevereObservable().registerObserver(mRevereObserver);
    }

    public void dismiss() {
        LogUtil.d("ConstellationViewManager" + mShowing);
        if (!mShowing) {
            return;
        }
        mShowing = false;
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mOnDismissListener != null) {
                    mOnDismissListener.onDismiss();
                }
                mWindowManager.removeViewImmediate(rootView);
                cancelAsrWakeup();
            }
        });
        GlobalObservableSupport.getRevereObservable().unregisterObserver(mRevereObserver);
    }

    private void useAsrWakeup() {
        AsrUtil.AsrComplexSelectCallback cb = new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public String getTaskId() {
                return TASK_ID;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if ("CMD_CANCEL".equals(type)) {
                    dismiss();
                }
            }

        };
        cb.addCommand("CMD_CANCEL", "关闭");
        WakeupManager.getInstance().useWakeupAsAsr(cb);
    }

    private static final String TASK_ID = ConstellationViewManager.class.getSimpleName();

    private void cancelAsrWakeup() {
        WakeupManager.getInstance().recoverWakeupFromAsr(TASK_ID);
    }

    private ReverseObservable.ReverseObserver mRevereObserver=new ReverseObservable.ReverseObserver(){
        @Override
        public void onReversePressed() {
            dismiss();
        }
    };
}
