package com.txznet.launcher.widget.dialog;

import android.support.annotation.DrawableRes;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.dialog2.WinConfirmAsr;
import com.txznet.launcher.R;
import com.txznet.loader.AppLogic;

/**
 * 恢复出厂设置dialog
 */
public abstract class SysConfirmDialog extends WinConfirmAsr {

    private TextView tvContent;
    private ImageView ivIcon;

    private SysDialogBuildData mBuildData;

    public static class SysDialogBuildData extends WinConfirmAsrBuildData {
        /**
         * 标题
         */
        String content;
        /**
         * 图标
         */
        Integer icon;
        /**
         * 是否自动关闭，默认2s
         */
        boolean autoClose;

        @Override
        public void check() {
            super.check();
            // 处理框默认优先级是最高的
            this.setHintType(TtsUtil.PreemptType.PREEMPT_TYPE_IMMEADIATELY);
        }

        public SysConfirmDialog.SysDialogBuildData setContent(String content) {
            this.content = content;
            return this;
        }

        public SysConfirmDialog.SysDialogBuildData setIcon(@DrawableRes int icon) {
            this.icon = icon;
            return this;
        }

        public SysConfirmDialog.SysDialogBuildData setAutoClose(boolean autoClose) {
            this.autoClose = autoClose;
            return this;
        }
    }

    public SysConfirmDialog(SysDialogBuildData buildData) {
        super(buildData, true);
        mBuildData = buildData;
    }

    @Override
    protected View createView() {
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sys, null);
        tvContent = (TextView) contentView.findViewById(R.id.iv_sys_dialog_content);
        ivIcon = (ImageView) contentView.findViewById(R.id.iv_sys_dialog_icon);
        if (mBuildData.content != null) {
            tvContent.setText(mBuildData.content);
        }
        if (mBuildData.icon != null) {
            ivIcon.setVisibility(View.VISIBLE);
            ivIcon.setImageResource(mBuildData.icon);
        } else {
            ivIcon.setVisibility(View.GONE);
        }
        mViewHolder = new ViewHolder();
        mViewHolder.mLeftButton = contentView.findViewById(R.id.btnMessageBox_Button1);
        mViewHolder.mRightButton = contentView.findViewById(R.id.btnMessageBox_Button3);
        return contentView;
    }

    @Override
    protected void onEndTts() {
        super.onEndTts();
        if (mBuildData.autoClose) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    dismissInner();
                }
            }, 2000);
        }
    }
}
