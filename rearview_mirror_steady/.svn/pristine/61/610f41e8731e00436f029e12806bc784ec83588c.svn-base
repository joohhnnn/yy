package com.txznet.txz.ui.widget;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.ui.GlobalObservableSupport;
import com.txznet.comm.ui.HomeObservable;
import com.txznet.comm.ui.dialog.WinDialog;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.loader.AppLogicBase;

import static com.txznet.comm.ui.util.LayouUtil.getDimen;

/**
 * Created by daviddai on 2019/9/19
 * 齐悟票务的UI。取消订单中或者退票中。
 */
public class QiWuTicketCancellingDialog extends WinDialog {

    private ImageView mIvIcon; // 中间的图标
    private TextView mTvPrompt; // 提示语

    private QiWuTicketCancellingDialog() {
        super(true);
        setCanceledOnTouchOutside(false);
        getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        setOnKeyListener(new OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event)
            {
                if((keyCode == KeyEvent.KEYCODE_BACK) && event.getRepeatCount()==0)
                {
                    if(instance.isShowing()){
                        AppLogicBase.runOnUiGround(new Runnable() {
                            @Override
                            public void run() {
                                instance.dismiss();
                            }
                        });

                        AsrUtil.cancel();
                    }
                }
                return false;
            }
        });
    }

    private HomeObservable.HomeObserver mHomeObserver = new HomeObservable.HomeObserver() {
        @Override
        public void onHomePressed() {
            if(instance.isShowing()){
                AppLogicBase.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        instance.dismiss();
                    }
                });
                AsrUtil.cancel();
            }
        }
    };

    private static QiWuTicketCancellingDialog instance;

    public static QiWuTicketCancellingDialog getInstance(){
            if(instance == null){
                instance = new QiWuTicketCancellingDialog();
            }
            return instance;
    }

    @SuppressLint("NewApi")
    @Override
    protected View createView() {
        // 整个界面，包括了外面的半透明部分
        FrameLayout view = new FrameLayout(getContext());
        view.setBackgroundColor(0xCC1A1D23);

        // dialog布局
        LinearLayout llContent = new LinearLayout(getContext());
        llContent.setOrientation(LinearLayout.VERTICAL);
        // 背景
        GradientDrawable llContentBg = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{0xEF313845, 0xEF313845});
        llContentBg.setCornerRadius(getDimen("m5"));
        llContent.setBackground(llContentBg);
        // 设置成可点击，避免这个view上面的事件都被父类处理了。父类的点击处理是dismiss，如果点击message等地方也dismiss好像不合理。
        llContent.setClickable(true);
        int llContentWidth = (int) getDimen("x313");
        int llContentHeight = ViewGroup.LayoutParams.WRAP_CONTENT;
        FrameLayout.LayoutParams llContentLayoutParams =
                new FrameLayout.LayoutParams(llContentWidth, llContentHeight);
        llContentLayoutParams.gravity = Gravity.CENTER;
        view.addView(llContent, llContentLayoutParams);

        // 图标
        mIvIcon = new ImageView(getContext());
        mIvIcon.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int ivIconDimension = (int) getDimen("m80");
        LinearLayout.LayoutParams ivIconLayoutParams =
                new LinearLayout.LayoutParams(ivIconDimension, ivIconDimension);
        ivIconLayoutParams.leftMargin = ivIconLayoutParams.rightMargin = (int) getDimen("x118");
        ivIconLayoutParams.topMargin = (int) getDimen("y35");
        llContent.addView(mIvIcon, ivIconLayoutParams);

        // 提示文字
        mTvPrompt = new TextView(getContext());
        mTvPrompt.setGravity(Gravity.CENTER);
        mTvPrompt.setSingleLine();
        mTvPrompt.setEllipsize(TextUtils.TruncateAt.END);
        mTvPrompt.setTextColor(0xFFFFFFFF);
        mTvPrompt.setIncludeFontPadding(false);
        TextViewUtil.setTextSize(mTvPrompt, getDimen("m19"));
        LinearLayout.LayoutParams tvPromptLayoutParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvPromptLayoutParams.leftMargin = tvPromptLayoutParams.rightMargin = (int) getDimen("x40");
        tvPromptLayoutParams.topMargin = (int) getDimen("y16");
        tvPromptLayoutParams.bottomMargin = (int) getDimen("y35");
        llContent.addView(mTvPrompt, tvPromptLayoutParams);

        return view;
    }

    @Override
    public void show() {
        super.show();
        GlobalObservableSupport.getHomeObservable().registerObserver(mHomeObserver);
    }

    /**
     * 设置图标
     */
    public void setIcon(@Nullable final Drawable icon) {
        if (mIvIcon != null) {
            AppLogicBase.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    mIvIcon.setImageDrawable(icon);
                }
            });

        }
    }

    /**
     * 设置提示语
     */
    public void setPrompt(@Nullable final CharSequence prompt) {
        if (mTvPrompt != null) {
            AppLogicBase.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    mTvPrompt.setText(prompt);
                }
            });

        }
    }

    @Override
    public void dismiss() {
        if(!isShowing()){
            return;
        }
        try{
            GlobalObservableSupport.getHomeObservable().unregisterObserver(mHomeObserver);
        }catch (Exception ignored){}
        super.dismiss();
    }
}
