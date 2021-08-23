package com.txznet.txz.ui.widget;

import android.annotation.SuppressLint;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txz.ui.data.UiData;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import static com.txznet.comm.ui.util.LayouUtil.getDimen;

/**
 * Created by daviddai on 2019/9/12
 */
public class QiWuTicketConfirmDialog extends WinDialog {


    private TextView mTvTitle;
    private TextView mTvMessage;
    private TextView mBtnPositive; // 确认按钮
    private TextView mBtnNegative; // 取消按钮

    public QiWuTicketConfirmDialog() {
        super(true);
        setCanceledOnTouchOutside(false);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        /*setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if((keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) && event.getRepeatCount()==0)
                {
                    if(instance.isShowing()){
                        instance.dismiss();
                        AsrUtil.cancel();
                    }
                }
                return false;
            }
        });*/
    }

    @Override
    protected View createView() {
        return initView();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressLint("NewApi")
    private View initView() {
        // 整个界面，包括了外面的半透明部分
        FrameLayout view = new FrameLayout(getContext());
        view.setBackgroundColor(0xCC1A1D23);
        /*view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                dismiss();
            }
        });*/

        // dialog布局
        LinearLayout llContent = new LinearLayout(getContext());
        llContent.setOrientation(LinearLayout.VERTICAL);
        // 背景
        GradientDrawable llContentBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xEF313845, 0xEF313845});
        llContentBg.setCornerRadius(getDimen("m5"));
        llContent.setBackground(llContentBg);
        llContent.setClickable(
                true); // 设置成可点击，避免这个view上面的事件都被父类处理了。父类的点击处理是dismiss，如果点击message等地方也dismiss好像不合理。
        int llContentWidth = (int) getDimen("x360");
        int llContentHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        FrameLayout.LayoutParams llContentLayoutParams =
                new FrameLayout.LayoutParams(llContentWidth, llContentHeight);
        llContentLayoutParams.gravity = Gravity.CENTER;
        view.addView(llContent, llContentLayoutParams);

        // title
        mTvTitle = new TextView(getContext());
        mTvTitle.setGravity(Gravity.CENTER);
        mTvTitle.setSingleLine();
        mTvTitle.setEllipsize(TextUtils.TruncateAt.END);
        mTvTitle.setTextColor(0xFFFFFFFF);
        TextViewUtil.setTextSize(mTvTitle, getDimen("m19"));
        LinearLayout.LayoutParams tvTitleLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvTitleLayoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        tvTitleLayoutParams.leftMargin = tvTitleLayoutParams.rightMargin = (int) getDimen("x24");
        tvTitleLayoutParams.topMargin = (int) getDimen("y24");
        llContent.addView(mTvTitle, tvTitleLayoutParams);

        // message
        mTvMessage = new TextView(getContext());
        mTvMessage.setGravity(Gravity.CENTER_HORIZONTAL);
        mTvMessage.setSingleLine();
        mTvMessage.setEllipsize(TextUtils.TruncateAt.END);
        mTvMessage.setTextColor(0xFFFFFFFF);
        TextViewUtil.setTextSize(mTvMessage, getDimen("m16"));
        LinearLayout.LayoutParams tvMessageLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvMessageLayoutParams
                .setMargins((int) getDimen("x24"), (int) getDimen("y16"), (int) getDimen("x24"),
                        (int) getDimen("y22"));
        llContent.addView(mTvMessage, tvMessageLayoutParams);

        // message和button之间的分割线
        View vDivider = new View(getContext());
        vDivider.setBackgroundColor(0x29FFFFFF);
        llContent.addView(vDivider,
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        Math.max((int) getDimen("y1"), 1)));

        // buttons
        ViewGroup buttons = genButtons();
        llContent
                .addView(buttons, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) getDimen("y48")));

        return view;
    }

    private ViewGroup genButtons() {
        LinearLayout result = new LinearLayout(GlobalContext.get());
        result.setOrientation(LinearLayout.HORIZONTAL);
        // 分割线
        result.setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
        ShapeDrawable divider = new ShapeDrawable(new RectShape());
        // 设置最小值为1，避免在较小的屏幕上x1向下取整后小于0导致不显示分割线。
        divider.setIntrinsicWidth(Math.max((int) getDimen("x1"), 1));
        divider.setIntrinsicHeight(-1);
        divider.getPaint().setColor(0x29FFFFFF);
        result.setDividerDrawable(divider);

        // 确定按钮
        mBtnPositive = new TextView(GlobalContext.get());
        mBtnPositive.setGravity(Gravity.CENTER);
        mBtnPositive.setSingleLine();
        mBtnPositive.setEllipsize(TextUtils.TruncateAt.END);
        mBtnPositive.setTextColor(0xFF369FFF);
        TextViewUtil.setTextSize(mBtnPositive, getDimen("m19"));
        // 默认不可见，由调用方传递参数来决定要不要显示。
        mBtnPositive.setVisibility(View.GONE);
        mBtnPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickPositive();
            }
        });
        LinearLayout.LayoutParams btnPositiveLayoutParams = new LinearLayout.LayoutParams(0,
                ViewGroup.LayoutParams.MATCH_PARENT);
        btnPositiveLayoutParams.weight = 1;
        result.addView(mBtnPositive, btnPositiveLayoutParams);

        // 取消按钮
        mBtnNegative = new TextView(GlobalContext.get());
        mBtnNegative.setGravity(Gravity.CENTER);
        mBtnNegative.setSingleLine();
        mBtnNegative.setEllipsize(TextUtils.TruncateAt.END);
        mBtnNegative.setTextColor(0xCCFFFFFF);
        TextViewUtil.setTextSize(mBtnNegative, getDimen("m19"));
        // 默认不可见，由调用方传递参数来决定要不要显示。
        mBtnNegative.setVisibility(View.GONE);
        mBtnNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                onClickNegative();
            }
        });
        LinearLayout.LayoutParams btnNegativeLayoutParams =
                new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT);
        btnNegativeLayoutParams.weight = 1;
        result.addView(mBtnNegative, btnNegativeLayoutParams);

        return result;
    }


    public void setTitle(CharSequence title) {
        if (mTvTitle != null) {
            mTvTitle.setText(title);
        }
    }

    public void setMessage(CharSequence message) {
        if (mTvMessage != null) {
            mTvMessage.setText(message);
        }
    }

    /**
     * 设置确定按钮的文本
     */
    public void setPositiveText(CharSequence positiveText) {
        if (positiveText != null && positiveText.length() > 0) {
            mBtnPositive.setText(positiveText);
            mBtnPositive.setVisibility(View.VISIBLE);
        } else {
            mBtnPositive.setText("");
            mBtnPositive.setVisibility(View.GONE);
        }
    }

    /**
     * 设置取消按钮的文本
     */
    public void setNegativeText(CharSequence negativeText) {
        if (negativeText != null && negativeText.length() > 0) {
            mBtnNegative.setText(negativeText);
            mBtnNegative.setVisibility(View.VISIBLE);
        } else {
            mBtnNegative.setText("");
            mBtnNegative.setVisibility(View.GONE);
        }
    }

    OnClickTask onClickTask;

    public void setonClickTask(OnClickTask onClickTask){
        this.onClickTask = onClickTask;
    }

    private void onClickPositive() {
        TtsManager.getInstance().cancelSpeak(RecorderWin.mSpeechTaskId);
        if(NetworkManager.getInstance().getNetType() == UiData.NETWORK_STATUS_NONE || NetworkManager.getInstance().getNetType() == UiData.NETWORK_STATUS_FLY){
            RecorderWin.mSpeechTaskId = TtsManager.getInstance().speakText("网络异常，请重试");
            return;
        }
        if(onClickTask != null){
            onClickTask.onClickAction();
        }
    }

    private void onClickNegative() {
        TtsManager.getInstance().cancelSpeak(RecorderWin.mSpeechTaskId);
        dismiss();
    }

    public interface OnClickTask{
        void onClickAction();
    }

    @Override
    public void show() {
        super.show();
        // todo 结束事件是什么？
        GlobalObservableSupport.getHomeObservable().registerObserver(mHomeReceiver);
    }

    @Override
    public void dismiss(){
        try {
            GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeReceiver);
        }catch (Exception ignored){}
        super.dismiss();
        onClickTask = null;
    }

    @Override
    public void checkTimeout() {
        super.checkTimeout();
    }

    private HomeObservable.HomeObserver mHomeReceiver = new HomeObservable.HomeObserver() {
        @Override
        public void onHomePressed() {
            // 短按Home键
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                }
            }, 0);
        }
    };

}