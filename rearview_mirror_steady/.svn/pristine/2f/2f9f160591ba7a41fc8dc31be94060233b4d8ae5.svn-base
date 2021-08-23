package com.txznet.txz.ui.widget;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.graphics.PixelFormat;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONException;
import org.json.JSONObject;

import static com.txznet.comm.ui.util.LayouUtil.getDimen;
import static com.txznet.comm.ui.util.LayouUtil.getDrawable;

/**
 * Created by daviddai on 2019/9/18
 * 齐悟票务的提醒view
 */
public class QiWuTicketReminderView extends FrameLayout {

    private static QiWuTicketReminderView mInstance;

    public static int mSpeechTaskId;

    private static String ttsTip;

    private static boolean mIsOpening = false;

    private static final String TASK_QiWuTicketREMINDER_KWS = "QiWuTicketReminderKws";

    private static final String TASK_REMINDER_KWS = "QiWiPayReminderKws";

    protected WindowManager.LayoutParams mLp;
    protected WindowManager mWinManager;
    private TextView tvTitle, tvCancel; // title和取消按钮的View
    private int border = (int) getDimen("m2");


    private QiWuTicketReminderView(@NonNull final Context context) {
        super(context);
        mWinManager = (WindowManager) getContext().getSystemService(Service.WINDOW_SERVICE);
        View layout = genLayoutView();
        // 由于边框要占用大小，所以宽度要额外加2个边框大小
        LayoutParams params =
                new LayoutParams((int) getDimen("x625") + border * 2,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        params.topMargin = (int) getDimen("y8");
        addView(layout, params);
    }

    /**
     * 生成提醒的view
     */
    @SuppressLint("NewApi")
    private View genLayoutView() {
        LinearLayout result = new LinearLayout(getContext());
        result.setOrientation(LinearLayout.HORIZONTAL);
        result.setGravity(Gravity.CENTER);
        result.setBackground(getDrawable("reminder_push_bg"));
        result.setPadding(border, border, border, border);

        // 左边部分的view。包含icon和title。图片加文字的组合就直接用textView使用了，减少层次。
        tvTitle = new TextView(getContext());
        tvTitle.setGravity(Gravity.CENTER_VERTICAL);
        tvTitle.setEllipsize(TextUtils.TruncateAt.END);
        tvTitle.setSingleLine();
        tvTitle.setTextColor(0xFFFFFFFF);
        int tvTitleHorizontalPadding = (int) getDimen("x16");
        int tvTitleVerticalPadding = (int) getDimen("y8");
        tvTitle.setPadding(tvTitleHorizontalPadding, tvTitleVerticalPadding,
                tvTitleHorizontalPadding, tvTitleVerticalPadding);
        TextViewUtil.setTextSize(tvTitle, getDimen("m20"));
        // 点击事件
        tvTitle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickLeft();
            }
        });
        LinearLayout.LayoutParams tvTitleLayoutParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTitleLayoutParams.weight = 1;
        result.addView(tvTitle, tvTitleLayoutParams);

        // 中间的分割线
        View vDivider = new View(getContext());
        vDivider.setBackgroundColor(0x7FFFFFFF);
        int vDividerWidth = (int) getDimen("x1");
        int vDividerHeight = (int) getDimen("y48");
        LinearLayout.LayoutParams vDividerLayoutParams =
                new LinearLayout.LayoutParams(vDividerWidth, vDividerHeight);
        vDividerLayoutParams.topMargin = vDividerLayoutParams.bottomMargin = (int) getDimen("y16");
        result.addView(vDivider, vDividerLayoutParams);

        // 右边的取消view
        tvCancel = new TextView(getContext());
        tvCancel.setGravity(Gravity.CENTER);
        tvCancel.setEllipsize(TextUtils.TruncateAt.END);
        tvCancel.setSingleLine();
        tvCancel.setTextColor(0xFFFFFFFF);
        TextViewUtil.setTextSize(tvCancel, getDimen("m19"));
        tvCancel.setIncludeFontPadding(false);
        int tvCancelHorizontalPadding = (int) getDimen("x26");
        int tcCancelVerticalPadding = (int) getDimen("y24");
        tvCancel.setPadding(tvCancelHorizontalPadding, tcCancelVerticalPadding,
                tvCancelHorizontalPadding, tcCancelVerticalPadding);
        // 点击事件
        tvCancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickCancel();
            }
        });
        LinearLayout.LayoutParams tvCancelLayoutParams =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT);
        result.addView(tvCancel, tvCancelLayoutParams);

        return result;
    }

    private OnClickTitle onClickTitle;


    private void setOnClickTitle(OnClickTitle onClickTitle){
        this.onClickTitle = onClickTitle;
    }

    /**
     * 展示提醒view,
     * 超时时间的单位：秒
     */
    public static void showPushView(@NonNull Data data, OnClickTitle onClickTitle, long outTime) {
        if(QiWuTicketManager.mCloseWaitingPayView){
            return;//适配设置此时不打开待支付弹窗
        }
        if (mInstance == null) {
            mInstance = new QiWuTicketReminderView(GlobalContext.get());
        }
        AppLogic.removeUiGroundCallback(outTimeRunnable);
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mInstance.setData(data);
        mInstance.open();
        mInstance.setOnClickTitle(onClickTitle);
        if(outTime > 0){
            AppLogic.runOnUiGround(outTimeRunnable, outTime * 1000);
        }
    }

    private static Runnable outTimeRunnable = new Runnable() {
        @Override
        public void run() {
            needTts = false;
            mInstance.dismiss();
        }
    };



    /**
     * 关闭提醒view
     */
    public static void closePushView() {
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        if (mInstance != null && mInstance.isShowing()) {
            mInstance.dismiss();
        }
    }

    private static boolean needTts = false;

    public static void speakTtsTip(){
        if(needTts){
            needTts = false;
            mInstance.open();
            TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
            mSpeechTaskId = TtsManager.getInstance().speakText(ttsTip+"，请及时支付");
        }
    }

    private void open() {
        if (mIsOpening) {
            return;
        }
        if(ttsTip != null){
            if(RecorderWin.isOpened()){
                needTts = true;
                return;
            }else {
                TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
                mSpeechTaskId = TtsManager.getInstance().speakText(ttsTip+"，请及时支付");
            }
        }
        mIsOpening = true;
        mLp = new WindowManager.LayoutParams();
        mLp.type = WindowManager.LayoutParams.FIRST_SYSTEM_WINDOW + 3;
        mLp.width = WindowManager.LayoutParams.MATCH_PARENT;
        mLp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        mLp.flags = 40;
        mLp.format = PixelFormat.RGBA_8888;
        mLp.gravity = Gravity.CENTER_HORIZONTAL | Gravity.TOP;
        mWinManager.addView(this, mLp);
        genAsrTask();
        if(statusOb == null){
            statusOb = new StatusOb(){

                @Override
                public void onShow() {
                    if(mIsOpening){
                        WakeupManager.getInstance().recoverWakeupFromAsr(TASK_REMINDER_KWS);
                    }
                }

                @Override
                public void onDismiss() {
                    if(mIsOpening){
                        genAsrTask();
                    }
                }
            };
        }
        RecorderWin.OBSERVABLE.registerObserver(statusOb);
    }

    private void dismiss() {
        post(new Runnable() {
            @Override
            public void run() {
                if (mIsOpening) {
                    mWinManager.removeView(QiWuTicketReminderView.this);
                    mIsOpening = false;
                    WakeupManager.getInstance().recoverWakeupFromAsr(TASK_REMINDER_KWS);
                    TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
                    AppLogic.removeUiGroundCallback(outTimeRunnable);
                    try {
                        RecorderWin.OBSERVABLE.unregisterObserver(statusOb);
                        WakeupManager.getInstance().recoverWakeupFromAsr(acsc.getTaskId());
                    }catch (Exception ignored){}
                }
            }
        });
    }

    private boolean isShowing() {
        return mIsOpening;
    }

    private void setData(@NonNull Data data) {
        if (tvTitle != null && data.title != null) {
            tvTitle.setText(data.title);
            ttsTip = data.title;
        }
        if (tvCancel != null && data.cancel != null) {
            tvCancel.setText(data.cancel);
        }
        if (tvTitle != null && data.icon != null) {
            tvTitle.setCompoundDrawablesWithIntrinsicBounds(getDrawable(data.icon), null, null,
                    null);
            tvTitle.setCompoundDrawablePadding((int) getDimen("x16"));
        }
    }

    /**
     * 点击分割线左边的view。
     */
    private void onClickLeft() {
        dismiss();
        if(onClickTitle != null){
            onClickTitle.onClickTitle();
        }
    }

    //窗口监听器
    StatusOb statusOb;

    abstract class StatusOb implements RecorderWin.StatusObervable.StatusObserver{

        public boolean isRegister  = false;

    }

    AsrUtil.AsrComplexSelectCallback acsc = new AsrUtil.AsrComplexSelectCallback() {
        @Override
        public boolean needAsrState() {
            return false;
        }

        @Override
        public String getTaskId() {
            return TASK_REMINDER_KWS;
        }

        @Override
        public void onCommandSelected(String type, String command) {
            if("closeReminder".equals(type)){
                if(RecorderWin.isOpened()){
                    return;
                }
                dismiss();
            }
        }
    }.addCommand("closeReminder", new String[]{"关闭"});

    private void genAsrTask(){
        WakeupManager.getInstance().useWakeupAsAsr(acsc);
    }

    /**
     * 点击分割线右边的取消view
     */
    public void onClickCancel() {
        dismiss();
    }

    public interface OnClickTitle{
        void onClickTitle();
    }

    public static class Data {
        private String icon;
        private String title;
        private String cancel;

        public Data(final String icon, final String title, final String cancel) {
            this.icon = icon;
            this.title = title;
            this.cancel = cancel;
        }

        public void setIcon(final String icon) {
            this.icon = icon;
        }

        public void setTitle(final String title) {
            this.title = title;
        }

        public void setCancel(final String cancel) {
            this.cancel = cancel;
        }
    }
}
