package com.txznet.music.widget;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.music.R;
import com.txznet.music.utils.FileConfigUtil;
import com.txznet.music.utils.StringUtils;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * Created by brainBear on 2017/7/28.
 */

public class TipsDialog extends WinDialog {

    TextView tvTitle;
    TextView tvContent;
    TextView tvSure;
    TextView tvCancel;
    FrameLayout rlBlank;

    private View.OnClickListener mSureListener;
    private View.OnClickListener mCancelListener;


    public TipsDialog(TipsDialogBuildData data) {
        super(data);
    }

    @Override
    protected View createView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_dialog_tips, null);
        rlBlank = (FrameLayout) view.findViewById(R.id.fl_blank);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvContent = (TextView) view.findViewById(R.id.tv_content);
        tvSure = (TextView) view.findViewById(R.id.tv_sure);
        tvCancel = (TextView) view.findViewById(R.id.tv_cancel);

        TipsDialogBuildData mBuildData = (TipsDialogBuildData) this.mBuildData;
        tvTitle.setText(mBuildData.getTitle());
        if (StringUtils.isEmpty(mBuildData.getContent())) {
            tvContent.setVisibility(View.GONE);
        } else {
            tvContent.setText(mBuildData.getContent());
        }
        tvSure.setText(mBuildData.getSureText());
        tvSure.setTextColor(mBuildData.getSureTextColor());
        tvCancel.setText(mBuildData.getCancelText());
        tvCancel.setTextColor(mBuildData.getCancelTextColor());


        rlBlank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss("blank");
            }
        });

        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss("sure");
                if (null != mSureListener) {
                    mSureListener.onClick(v);
                }
            }
        });

        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss("cancel");
                if (null != mCancelListener) {
                    mCancelListener.onClick(v);
                }
            }
        });

        return view;
    }


    public void setSureListener(View.OnClickListener listener) {
        this.mSureListener = listener;
    }

    public void setCancelListener(View.OnClickListener listener) {
        this.mCancelListener = listener;
    }


    @Override
    public String getReportDialogId() {
        return null;
    }


    public static class TipsDialogBuildData extends DialogBuildData {

        private static final String DEFAULT_TITLE = "提示";
        private static final String DEFAULT_SURE_TEXT = "确定";
        private static final String DEFAULT_CANCEL_TEXT = "取消";

        private String title;
        private String content;
        private String sureText;
        private String cancelText;
        private int sureTextColor;
        private int cancelTextColor;
        private String reportId;

        public TipsDialogBuildData() {
            int screenStyle = FileConfigUtil.getIntegerConfig(TXZFileConfigUtil.KEY_MUSIC_SCREEN_STYLE, 0);
            setFullScreen(screenStyle != 0);
//			setFullScreen(true);
            setWindowType(WindowManager.LayoutParams.TYPE_PRIORITY_PHONE);
        }


        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getSureText() {
            return sureText;
        }

        public void setSureText(String sureText) {
            this.sureText = sureText;
        }

        public String getCancelText() {
            return cancelText;
        }

        public void setCancelText(String cancelText) {
            this.cancelText = cancelText;
        }

        public int getSureTextColor() {
            return sureTextColor;
        }

        public void setSureTextColor(int sureTextColor) {
            this.sureTextColor = sureTextColor;
        }

        public int getCancelTextColor() {
            return cancelTextColor;
        }

        public void setCancelTextColor(int cancelTextColor) {
            this.cancelTextColor = cancelTextColor;
        }

        public String getReportId() {
            return reportId;
        }

        public void setReportId(String reportId) {
            this.reportId = reportId;
        }

        @Override
        public void check() {
            super.check();
            if (TextUtils.isEmpty(getTitle())) {
                setTitle(DEFAULT_TITLE);
            }

            if (TextUtils.isEmpty(getSureText())) {
                setSureText(DEFAULT_SURE_TEXT);
            }

            if (TextUtils.isEmpty(getCancelText())) {
                setCancelText(DEFAULT_CANCEL_TEXT);
            }

            if (getSureTextColor() == 0) {
                setSureTextColor(GlobalContext.get().getResources().getColor(R.color.tips_ok_text));
            }

            if (getCancelTextColor() == 0) {
                setCancelTextColor(GlobalContext.get().getResources().getColor(R.color.tips_cancel_text));
            }

            if (TextUtils.isEmpty(getReportId())) {
                setReportId("MusicTips");
            }
        }
    }

}
