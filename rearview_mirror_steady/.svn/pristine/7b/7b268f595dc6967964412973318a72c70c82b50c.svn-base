package com.txznet.txz.component.offlinepromote.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.PreferenceUtil;

public class OfflinePromoteAutoCodeDialog extends Win2Dialog implements View.OnClickListener {
    private static final String TAG = "OfflinePromoteAutoCode";

    private LinearLayout mRootLayout;
    private TextView mTvCodeOne;
    private TextView mTvCodeTwo;
    private TextView mTvCodeThree;
    private TextView mTvCodeFour;
    private TextView mTvTitle;
    private Button mBtnSure;
    //记录点击后的验证码
    private String mCode = "";
    private static final int MAX_NUMBER = 4;
    private static final int MAX_INPUT_NUMBER = 5;//每天最多尝试输入5次
    private static final String SURE_TEXT = "确认";
    private static final String ERROR_TITLE_HINT = "验证码错误，请重试";
    private static final String DEFAULT_TITLE_HINT = "请输入删除验证码";

    public OfflinePromoteAutoCodeDialog() {
        super(true, true);
    }

    @Override
    protected View createView(Object... objects) {
        mRootLayout = new LinearLayout(GlobalContext.get());
        mRootLayout.setOrientation(LinearLayout.VERTICAL);
        mRootLayout.setGravity(Gravity.CENTER);
        mRootLayout.addView(createDeleteCodeView());
        mRootLayout.addView(createKeyboardView());
        return mRootLayout;
    }

    /**
     * 输入二维码的弹窗
     *
     * @return 返回布局
     */
    public View createDeleteCodeView() {
        final LinearLayout rootLayout = new LinearLayout(GlobalContext.get());
        rootLayout.setScaleX(getScale());
        rootLayout.setScaleY(getScale());
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        rootLayout.setBackground(LayouUtil.getDrawable("shape_feedback"));
        int width = 460;
        int height = 220;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width, height);
        if(DeviceInfo.getScreenHeight() < 600){//对小于600的屏幕单独处理
            params.topMargin = DeviceInfo.getScreenHeight() - height - (int) LayouUtil.getDimen("m224");
        }else{
            params.topMargin = (int) LayouUtil.getDimen("m64");
        }
        rootLayout.setLayoutParams(params);

        //标题
        mTvTitle = new TextView(GlobalContext.get());
        mTvTitle.setTextColor(Color.WHITE);
        mTvTitle.setText(DEFAULT_TITLE_HINT);
        int titleSize = 20;
        mTvTitle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = 30;
        mTvTitle.setLayoutParams(titleParams);

        rootLayout.addView(mTvTitle);
        //验证码输入（4个TextView）
        LinearLayout codeLayout = new LinearLayout(GlobalContext.get());
        codeLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams codeLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        codeLayoutParams.topMargin = 20;
        codeLayout.setLayoutParams(codeLayoutParams);

        rootLayout.addView(codeLayout);
        //TextViewOne
        mTvCodeOne = createCodeTextView();
        int tvCodeWidth = 60;
        LinearLayout.LayoutParams tvCodeParams = new LinearLayout.LayoutParams(tvCodeWidth, tvCodeWidth);
        tvCodeParams.rightMargin = 12;
        mTvCodeOne.setLayoutParams(tvCodeParams);

        codeLayout.addView(mTvCodeOne);
        //TextViewTwo
        mTvCodeTwo = createCodeTextView();
        mTvCodeTwo.setLayoutParams(tvCodeParams);

        codeLayout.addView(mTvCodeTwo);
        //TextViewThree
        mTvCodeThree = createCodeTextView();
        mTvCodeThree.setLayoutParams(tvCodeParams);

        codeLayout.addView(mTvCodeThree);
        //TextViewFour
        mTvCodeFour = createCodeTextView();
        LinearLayout.LayoutParams tvCodeFourParams = new LinearLayout.LayoutParams(tvCodeWidth, tvCodeWidth);
        mTvCodeFour.setLayoutParams(tvCodeFourParams);

        codeLayout.addView(mTvCodeFour);

        View line = new View(GlobalContext.get());
        int lineHeight = 1;
        line.setBackgroundColor(Color.parseColor("#21FFFFFF"));
        LinearLayout.LayoutParams lineParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lineHeight);
        lineParams.topMargin = 30;
        line.setLayoutParams(lineParams);
        rootLayout.addView(line);

        TextView tvCancel = new TextView(GlobalContext.get());
        tvCancel.setGravity(Gravity.CENTER);
        tvCancel.setText("取消");
        tvCancel.setTextColor(Color.WHITE);
        tvCancel.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        LinearLayout.LayoutParams tvCancelParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        tvCancel.setLayoutParams(tvCancelParams);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
            }
        });

        rootLayout.addView(tvCancel);
        return rootLayout;
    }

    /**
     * 键盘布局
     *
     * @return
     */
    public View createKeyboardView() {

        LinearLayout rootLayout = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        rootLayout.setGravity(Gravity.BOTTOM);
        rootLayout.setLayoutParams(params);

        LinearLayout layout = new LinearLayout(GlobalContext.get());
        layout.setBackgroundColor(Color.parseColor("#282C34"));
        layout.setGravity(Gravity.CENTER);
        layout.setOrientation(LinearLayout.VERTICAL);
        params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("m224"));
        params.topMargin = (int) LayouUtil.getDimen("m16");
        layout.setLayoutParams(params);
        rootLayout.addView(layout);

        //第一行按键
        LinearLayout oneLayout = new LinearLayout(GlobalContext.get());
        oneLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams oneLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        oneLayout.setLayoutParams(oneLayoutParams);

        layout.addView(oneLayout);

        //oneBtn
        Button btnOne = createBtn("1");
        btnOne.setOnClickListener(this);
        oneLayout.addView(btnOne);
        //twoBtn
        Button btnTwo = createBtn("2");
        btnTwo.setOnClickListener(this);
        oneLayout.addView(btnTwo);
        //ThreeBtn
        Button btnThree = createBtn("3");
        btnThree.setOnClickListener(this);
        oneLayout.addView(btnThree);
        //FourBtn
        Button btnFour = createBtn("4");
        btnFour.setOnClickListener(this);
        oneLayout.addView(btnFour);
        //FiveBtn
        Button btnFive = createBtn("5");
        btnFive.setOnClickListener(this);
        oneLayout.addView(btnFive);
        //DeleteBtn
        ImageButton btnDelete = new ImageButton(GlobalContext.get());
        btnDelete.setOnClickListener(this);
        btnDelete.setBackground(LayouUtil.getDrawable("shape_offline_promote_number_bg"));

        btnDelete.setImageDrawable(LayouUtil.getDrawable("offline_promote_delete_icon"));
        int btnWidth = (int) LayouUtil.getDimen("x128");
        int btnHeight = (int) LayouUtil.getDimen("m80");
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnWidth, btnHeight);
        btnDelete.setLayoutParams(btnParams);

        oneLayout.addView(btnDelete);

        //TwoLayout
        LinearLayout twoLayout = new LinearLayout(GlobalContext.get());
        twoLayout.setBaselineAligned(false);
        twoLayout.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams twoLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, btnHeight);

        twoLayoutParams.topMargin = (int) LayouUtil.getDimen("m16");
        twoLayout.setLayoutParams(twoLayoutParams);

        layout.addView(twoLayout);
        //SixBtn
        Button btnSix = createBtn("6");
        btnSix.setOnClickListener(this);
        twoLayout.addView(btnSix);
        //SevenBtn
        Button btnSeven = createBtn("7");
        btnSeven.setOnClickListener(this);
        twoLayout.addView(btnSeven);
        //EightBtn
        Button btnEight = createBtn("8");
        btnEight.setOnClickListener(this);
        twoLayout.addView(btnEight);
        //NineBtn
        Button btnNine = createBtn("9");
        btnNine.setOnClickListener(this);
        twoLayout.addView(btnNine);
        //ZeroBtn
        Button btnZero = createBtn("0");
        btnZero.setOnClickListener(this);
        twoLayout.addView(btnZero);
        //SureBtn
        mBtnSure = new Button(GlobalContext.get());
        mBtnSure.setText("确认");
//        mBtnSure.setBackground(LayouUtil.getDrawable("shape_offline_promote_sure_btn"));
        int textSize = (int) LayouUtil.getDimen("m19");
        mBtnSure.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        mBtnSure.setTextColor(Color.WHITE);
        btnParams = new LinearLayout.LayoutParams(btnWidth,btnHeight);
        mBtnSure.setLayoutParams(btnParams);
        mBtnSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickSure();
            }
        });
        updateSureBtn(false);

        twoLayout.addView(mBtnSure);
        return rootLayout;
    }

    private void updateSureBtn(boolean enableClick){
        if(enableClick){
            mBtnSure.setBackground(LayouUtil.getDrawable("shape_offline_promote_sure_btn"));
            mBtnSure.setClickable(true);
            mBtnSure.requestLayout();
        }else{
            mBtnSure.setBackground(LayouUtil.getDrawable("shape_offline_promote_number_bg"));
            mBtnSure.setClickable(false);
            mBtnSure.requestLayout();
        }
    }

    public Button createBtn(String text) {
        Button btn = new Button(GlobalContext.get());
        btn.setBackground(LayouUtil.getDrawable("shape_offline_promote_number_bg"));
        btn.setText(text);
        int textSize = (int) LayouUtil.getDimen("m29");
        btn.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
        btn.setTextColor(Color.WHITE);

        int btnWidth = (int) LayouUtil.getDimen("x107");
        int btnHeight = (int) LayouUtil.getDimen("m80");
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnWidth, btnHeight);
        btnParams.rightMargin = (int) LayouUtil.getDimen("x16");
        btn.setLayoutParams(btnParams);
        return btn;
    }

    public TextView createCodeTextView() {
        TextView textView = new TextView(GlobalContext.get());
        textView.setIncludeFontPadding(false);
        textView.setGravity(Gravity.CENTER);
        textView.setBackground(LayouUtil.getDrawable("shape_offline_promote_square_bg"));
        int codeSize = (int) LayouUtil.getDimen("m27");
        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, codeSize);
        textView.setTextColor(Color.WHITE);
        return textView;
    }

    @Override
    public void onClick(View v) {
        if (v instanceof ImageButton) {//删除按钮
            deleteCode();
            return;
        }
        String text = ((Button) v).getText().toString();
        inputCode(text);//点击数字输入
    }

    private void onClickSure(){
        String code = OfflinePromoteManager.getInstance().getAutoCode();
        if (!TextUtils.isEmpty(code) && mCode.equals(code)) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    dismiss();
                    OfflinePromoteManager.getInstance().showSuccessHintDialog();
                }
            });
        } else {
            int number = PreferenceUtil.getInstance().getOfflineInputErrorNumber();
            number++;
            PreferenceUtil.getInstance().setOfflineInputErrorNumber(number);
            if (number >= MAX_INPUT_NUMBER) {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                        OfflinePromoteManager.getInstance().showErrorHintDialog();
                    }
                });
            } else {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        mTvTitle.setTextColor(Color.parseColor("#F54545"));
                        mTvTitle.setText(ERROR_TITLE_HINT);
                        mCode = "";
                        mTvCodeFour.setText("");
                        mTvCodeThree.setText("");
                        mTvCodeTwo.setText("");
                        mTvCodeOne.setText("");
                        updateSureBtn(false);
                    }
                });
            }
        }
    }

    public void inputCode(String code) {
        if (mCode.length() == MAX_NUMBER) {
            return;
        }
        mCode = mCode + code;
        addDialogCode();
    }

    public void deleteCode() {
        if (TextUtils.isEmpty(mCode)) {
            return;
        }
        final int length = mCode.length();
        mCode = mCode.substring(0, mCode.length() - 1);
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                switch (length) {
                    case 4:
                        updateSureBtn(false);
                        mTvCodeFour.setText("");
                        break;
                    case 3:
                        mTvCodeThree.setText("");
                        break;
                    case 2:
                        mTvCodeTwo.setText("");
                        break;
                    case 1:
                        mTvCodeOne.setText("");
                        break;
                    default:
                }
            }
        });
    }

    public void addDialogCode() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                switch (mCode.length()) {
                    case 4:
                        updateSureBtn(true);
                        mTvCodeFour.setText(String.valueOf(mCode.charAt(3)));
                    case 3:
                        mTvCodeThree.setText(String.valueOf(mCode.charAt(2)));
                    case 2:
                        mTvCodeTwo.setText(String.valueOf(mCode.charAt(1)));
                    case 1:
                        if (ERROR_TITLE_HINT.equals(mTvTitle.getText().toString())) {
                            AppLogic.runOnUiGround(new Runnable() {
                                @Override
                                public void run() {
                                    mTvTitle.setTextColor(Color.WHITE);
                                    mTvTitle.setText(DEFAULT_TITLE_HINT);
                                }
                            });
                        }
                        mTvCodeOne.setText(String.valueOf(mCode.charAt(0)));
                        break;
                    default:
                }
            }
        });
    }

    public float getScale() {
        return Math.min((float) DeviceInfo.getScreenWidth() / 1024, (float) DeviceInfo.getScreenHeight() / 600);
    }

    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.logd("onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    dismiss();
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                }
            }
        }
    };

    @Override
    public void show() {
        super.show();
        GlobalContext.get().registerReceiver(mHomeReceiver,new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        GlobalContext.get().unregisterReceiver(mHomeReceiver);
    }
}
