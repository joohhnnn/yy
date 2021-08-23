package com.txznet.music.widget;

import android.graphics.drawable.Drawable;
import android.support.v7.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.dialog2.WinDialog;
import com.txznet.music.R;
import com.txznet.music.utils.StringUtils;

/**
 * Created by brainBear on 2017/7/28.
 */

public class TipsCheckDialog extends WinDialog {

    public static final String TAG = "TipsCheckDialog";

    TextView tvTitle;
    TextView tvContent;
    TextView tvSure;
    TextView tvCancel;
    FrameLayout rlBlank;
    AppCompatCheckBox checkBox;

    private View.OnClickListener mSureListener;
    private View.OnClickListener mCancelListener;


    public TipsCheckDialog(TipsDialogBuildData data) {
        super(data);
    }

    @Override
    protected View createView() {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.layout_check_dialog_tips, null);
        rlBlank = (FrameLayout) view.findViewById(R.id.fl_blank);
        tvTitle = (TextView) view.findViewById(R.id.tv_title);
        tvContent = (TextView) view.findViewById(R.id.tv_content);
        tvSure = (TextView) view.findViewById(R.id.tv_sure);
        tvCancel = (TextView) view.findViewById(R.id.tv_cancel);
        checkBox = (AppCompatCheckBox) view.findViewById(R.id.checkbox1);

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

        //设置CheckBox的图片大小
        //创建Drawable对象
        final Drawable drawable = getContext().getResources().getDrawable(R.drawable.message_selector);
//        //设置drawable的位置,宽高
        drawable.setBounds(0, 0, 20, 20);
        checkBox.setCompoundDrawables(drawable, null, null, null);
        checkBox.setCompoundDrawablePadding(5);

        checkBox.setText(mBuildData.getCheckBoxTv());

        rlBlank.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss("blank");
            }
        });

        view.findViewById(R.id.ll_content).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //禁掉区域的点击事件
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
        checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //CheckBox的状态如果是false，调用了performClick后，就相当于你用手点击了CheckBox，当然其状态就变成了true，所以也就不需要setChecked方法了。
            }
        });


        return view;
    }

    /**
     * 是否为选中的状态
     *
     * @return
     */
    public boolean isCheck() {
        return checkBox.isChecked();
    }

    public void changeCheck() {
        LogUtil.d(TAG, "click:" + checkBox.isChecked());
        checkBox.setChecked(!checkBox.isChecked());
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
        private String checkBoxTv;

        public TipsDialogBuildData() {
            setFullScreen(true);
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

        public String getCheckBoxTv() {
            return checkBoxTv;
        }

        public void setCheckBoxTv(String checkBoxTv) {
            this.checkBoxTv = checkBoxTv;
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
