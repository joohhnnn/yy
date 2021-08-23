package com.txznet.launcher.domain.upgrade;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.txznet.comm.remote.util.TtsUtil.PreemptType;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.launcher.R;

public abstract class UpgradeProgressDialog extends WinDialog {
    /**
     * 默认处理进度文本
     */
    public static final String DEFAULT_TEXT_PROGRESS = "正在处理中...";

    /**
     * 进度对话框构建数据
     *
     * @author pppi
     */
    public static class WinProgressBuildData extends WinDialog.DialogBuildData {
        /**
         * 处理中消息文本
         */
        String mMessageText;
        /**
         * 当前进度
         */
        int mProgress;

        /**
         * 最大进度
         */
        int mMaxProgress;

        /**
         * 样式
         */
        int mStyle;

        @Override
        public void check() {
            // 处理框默认优先级是最高的
            this.setHintType(PreemptType.PREEMPT_TYPE_IMMEADIATELY);

            // 默认消息设置
            if (mMessageText == null) {
                mMessageText = DEFAULT_TEXT_PROGRESS;
            }
            super.check();
            addExtraInfo("mMessageText", mMessageText);
            addExtraInfo("dialogType", UpgradeProgressDialog.class.getSimpleName());
        }

        /**
         * 设置消息文本
         *
         * @param text
         * @return
         */
        public WinProgressBuildData setMessageText(String text) {
            this.mMessageText = text;
            return this;
        }

        public WinProgressBuildData setmProgress(int mProgress) {
            this.mProgress = mProgress;
            return this;
        }

        public WinProgressBuildData setMaxProgress(int mMaxProgress) {
            this.mMaxProgress = mMaxProgress;
            return this;
        }

        public WinProgressBuildData setStyle(int mStyle) {
            this.mStyle = mStyle;
            return this;
        }
    }

    /**
     * 处理中文本的TextView
     */
    protected TextView mText;

    protected ProgressBar mProgressBar;

    /**
     * 默认构造
     */
    public UpgradeProgressDialog() {
        this(new WinProgressBuildData());
    }


    public UpgradeProgressDialog(String text) {
        this(new WinProgressBuildData().setMessageText(text));
    }


    public UpgradeProgressDialog(String text, boolean isSystem) {
        this((WinProgressBuildData) new WinProgressBuildData().setMessageText(
                text).setSystemDialog(isSystem));
    }

    /**
     * 特有构建数据
     */
    WinProgressBuildData mWinProgressBuildData;

    /**
     * 通过构建数据构造对话框
     *
     * @param data
     */
    public UpgradeProgressDialog(WinProgressBuildData data) {
        this(data, true);
    }

    /**
     * 通过构造数据构造对话框，用于给派生类构造，构造时先不初始化
     *
     * @param data 构建数据
     * @param init 是否初始化，自己构造时传true，派生类构造时传false
     */
    protected UpgradeProgressDialog(WinProgressBuildData data, boolean init) {
        super(data, false);
        mWinProgressBuildData = data;

        if (init) {
            initDialog();
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected View createView() {
        View context = null;
        if (mWinProgressBuildData.mStyle == 0) {
            context = LayoutInflater.from(getContext()).inflate(
                    R.layout.upgrade_progress, null);
        } else {
            context = LayoutInflater.from(getContext()).inflate(
                    R.layout.upgrade_progress_indeterminate, null);
        }
        mText = (TextView) context.findViewById(R.id.prgProgress_Percent);
        if (this.mWinProgressBuildData.mMessageText != null) {
            mText.setText(this.mWinProgressBuildData.mMessageText);
        }
        mProgressBar = (ProgressBar) context.findViewById(R.id.prgProgress_Progress);
        mProgressBar.setMax(mWinProgressBuildData.mMaxProgress);
        mProgressBar.setProgress(mWinProgressBuildData.mProgress);
        return context;
    }

    /**
     * 更新对话框进度
     */
    public void updateProgress(final int mProgress) {
        this.mWinProgressBuildData.mProgress = mProgress;
        runOnUiGround(new Runnable() {
            @Override
            public void run() {
                mProgressBar.setProgress(mWinProgressBuildData.mProgress);
            }
        }, 0);
    }


    /**
     * 倒计时关闭
     *
     * @param text 处理中格式化文本
     * @param time 倒计时时间，单位：秒
     * @param end  倒计时到达后执行的操作
     */
    public void dismissCountDown(final String text, final int time,
                                 final Runnable end) {
        runOnUiGround(new Runnable() {
            @Override
            public void run() {
                updateCountDown(mText, text, time, new Runnable() {
                    @Override
                    public void run() {
                        if (end != null) {
                            end.run();
                        }
                        UpgradeProgressDialog.this.dismissInner();
                    }
                });
            }
        }, 0);
    }

    /**
     * 获取调试字符串
     *
     * @return
     */
    public String getDebugString() {
        return this.toString() + "[" + this.mWinProgressBuildData.mMessageText
                + "]";
    }
}
