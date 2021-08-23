package com.txznet.txz.ui.win.help;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.util.ViewUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogicBase;
import com.txznet.txz.R;
import com.txznet.txz.module.wakeup.WakeupManager;
import com.txznet.txz.util.QRUtil;

import java.lang.reflect.Field;

public class WinHelpGuideQRCodeDialog extends Win2Dialog {
    private View mRootLayout;

    public static final int TYPE_LOW_POWER = 0;
    public static final int TYPE_FULL_SCREEN = 1;
    public static final int TYPE_VERTICAL_SCREEN = 2;
    public static final int TYPE_NONE_SCREEN = 3;

    private static final String TASK_QRCODE_GUIDE = "TASK_QRCODE_GUIDE";

    public WinHelpGuideQRCodeDialog(boolean fullScreen, Object... objects) {
        super(true, fullScreen, objects);
    }

    @Override
    protected View createView(Object... objects) {
        JSONBuilder jsonBuilder = (JSONBuilder) objects[0];
        if (jsonBuilder == null) {
            return null;
        }
        switch (jsonBuilder.getVal("screenType", Integer.class, -1)) {
            case TYPE_LOW_POWER:
                mRootLayout = createViewLowPower(jsonBuilder);
                break;
            case TYPE_FULL_SCREEN:
                mRootLayout = createViewFullScreen(jsonBuilder);
                break;
            case TYPE_VERTICAL_SCREEN:
                mRootLayout = createViewVerticalScreen(jsonBuilder);
                break;
            case TYPE_NONE_SCREEN:
                int locationX = jsonBuilder.getVal("locationX", Integer.class);
                if (locationX > ScreenUtil.getScreenWidth() / 2) {
                    mRootLayout = createViewNoneScreenLeft(jsonBuilder);
                } else {
                    mRootLayout = createViewNoneScreenRight(jsonBuilder);
                }
                break;
            default:
                break;
        }
        return mRootLayout;
    }

    /**
     * 低功耗UI
     *
     * @param jsonBuilder
     * @return
     */
    public View createViewLowPower(JSONBuilder jsonBuilder) {

        String url = jsonBuilder.getVal("qrCodeUrl", String.class);
        String desc = jsonBuilder.getVal("qrCodeGuideDesc", String.class);
        final int locationX = jsonBuilder.getVal("locationX", Integer.class);
        final int locationY = jsonBuilder.getVal("locationY", Integer.class);

        getWindow().getDecorView().setBackgroundColor(Color.parseColor("#CC1A1D23"));

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setLayoutParams(contentParams);
//        llContent.setBackgroundColor(Color.parseColor("#CC1A1D23"));

        //描述、箭头、二维码
        //int height = (int) LayouUtil.getDimen("y368");
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout qrCodeLayout = new LinearLayout(GlobalContext.get());
        qrCodeLayout.setOrientation(LinearLayout.HORIZONTAL);

        llContent.addView(qrCodeLayout);

        int descWidth = (int) LayouUtil.getDimen("x331");
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(descWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = (int) LayouUtil.getDimen("y140");
        descParams.leftMargin = (int) LayouUtil.getDimen("x30");
        TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setText(desc);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setLayoutParams(descParams);
        tvDesc.setGravity(Gravity.RIGHT);

        qrCodeLayout.addView(tvDesc);

        int arrowWidth = (int) LayouUtil.getDimen("x82");
        int arrowHeight = (int) LayouUtil.getDimen("y114");
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(arrowWidth, arrowHeight);
        arrowParams.topMargin = (int) LayouUtil.getDimen("y148");
        arrowParams.leftMargin = (int) LayouUtil.getDimen("x25");
        arrowParams.rightMargin = (int) LayouUtil.getDimen("x13");
        ImageView ivArrow = new ImageView(GlobalContext.get());
        ivArrow.setLayoutParams(arrowParams);
        ivArrow.setImageResource(R.drawable.win_help_guide_arrow);

        qrCodeLayout.addView(ivArrow);

        int qrCodeWidth = (int) LayouUtil.getDimen("m104");
        final LinearLayout.LayoutParams qrCodeParams = new LinearLayout.LayoutParams(qrCodeWidth, qrCodeWidth);
        int barHeight = getStatusBarHeight(getContext());
        LogUtil.d("kevin barHeight :" + barHeight);
        qrCodeParams.topMargin = locationY - barHeight;

        final ImageView qrCodeView = new ImageView(GlobalContext.get());
        try {
            qrCodeView.setImageBitmap(QRUtil.createQRCodeBitmap(url, qrCodeWidth));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        qrCodeView.setLayoutParams(qrCodeParams);
        qrCodeLayout.addView(qrCodeView);

        qrCodeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                qrCodeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] loc = new int[2];
                qrCodeView.getLocationOnScreen(loc);
                LogUtil.d("qrCode view X:" + loc[0]);
                LogUtil.d("qrCode view Y:" + loc[1]);

                if (locationX != loc[0]) {
                    params.leftMargin = (Math.abs(loc[0] - locationX));
                    qrCodeLayout.setLayoutParams(params);
                    qrCodeLayout.requestLayout();
                    LogUtil.d("qrCode Guide real X not equal to locationX.refresh!");
                }
                if (locationY != loc[1]) {
                    qrCodeParams.topMargin = qrCodeParams.topMargin - (loc[1] - locationY);
                    LogUtil.d("qrCode view Y:" + qrCodeParams.topMargin);
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real Y not equal to locationY.refresh!");
                }
            }
        });

//        params.leftMargin = locationX - descWidth - arrowWidth - arrowParams.leftMargin - arrowParams.rightMargin;
        qrCodeLayout.setLayoutParams(params);

        //按钮：我知道了
        int btnWidth = (int) LayouUtil.getDimen("x124");
        int btnHeight = (int) LayouUtil.getDimen("y47");
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnWidth, btnHeight);
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
        btnParams.topMargin = (int) LayouUtil.getDimen("y45");
        ImageView ivBtn = new ImageView(GlobalContext.get());
        ivBtn.setImageResource(R.drawable.win_help_guide_btn);
        ivBtn.setLayoutParams(btnParams);
        ivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        llContent.addView(ivBtn);

        return llContent;
    }

    /**
     * UI3.0界面
     *
     * @param jsonBuilder
     * @return
     */
    public View createViewFullScreen(JSONBuilder jsonBuilder) {

        String url = jsonBuilder.getVal("qrCodeUrl", String.class);
        String desc = jsonBuilder.getVal("qrCodeGuideDesc", String.class);
        final int locationX = jsonBuilder.getVal("locationX", Integer.class);//二维码的x
        final int locationY = jsonBuilder.getVal("locationY", Integer.class);//二维码的y
        int qrCodeWidth = (int) LayouUtil.getDimen("m104");
        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setLayoutParams(contentParams);

        llContent.setBackgroundColor(Color.parseColor("#CC1A1D23"));

        //描述、箭头、二维码
        int height = (int) LayouUtil.getDimen("y368");
        height = height >= (locationY + qrCodeWidth) ? height : (locationY + qrCodeWidth);
        final LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height);
        final LinearLayout qrCodeLayout = new LinearLayout(GlobalContext.get());
        qrCodeLayout.setOrientation(LinearLayout.HORIZONTAL);

        llContent.addView(qrCodeLayout);

        int descWidth = (int) LayouUtil.getDimen("x331");
        int descMarginLeft = (int) LayouUtil.getDimen("x202");
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(descWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = (int) LayouUtil.getDimen("y103");
        descParams.leftMargin = descMarginLeft;
        LinearLayout descLayout = new LinearLayout(GlobalContext.get());
        descLayout.setGravity(Gravity.RIGHT);
        descLayout.setLayoutParams(descParams);

        TextView tvDesc = new TextView(GlobalContext.get());
        descParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        tvDesc.setText(desc);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setLayoutParams(descParams);
        tvDesc.setGravity(Gravity.CENTER);

        descLayout.addView(tvDesc);

        qrCodeLayout.addView(descLayout);

        int arrowWidth = (int) LayouUtil.getDimen("x82");
        int arrowHeight = (int) LayouUtil.getDimen("y114");
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(arrowWidth, arrowHeight);
        arrowParams.topMargin = (int) LayouUtil.getDimen("y148");
        arrowParams.leftMargin = (int) LayouUtil.getDimen("x15");
        arrowParams.rightMargin = (int) LayouUtil.getDimen("x16");
        ImageView ivArrow = new ImageView(GlobalContext.get());
        ivArrow.setLayoutParams(arrowParams);
        ivArrow.setImageResource(R.drawable.win_help_guide_arrow);

        qrCodeLayout.addView(ivArrow);

        final LinearLayout.LayoutParams qrCodeParams = new LinearLayout.LayoutParams(qrCodeWidth, qrCodeWidth);
        qrCodeParams.topMargin = locationY;

        final ImageView qrCodeView = new ImageView(GlobalContext.get());
        try {
            qrCodeView.setImageBitmap(QRUtil.createQRCodeBitmap(url, qrCodeWidth));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        qrCodeView.setLayoutParams(qrCodeParams);
        qrCodeLayout.addView(qrCodeView);

        qrCodeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                qrCodeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] loc = new int[2];
                qrCodeView.getLocationOnScreen(loc);
                LogUtil.d("qrCode view X:" + loc[0]);
                LogUtil.d("qrCode view Y:" + loc[1]);

                if (locationX != loc[0]) {
                    params.leftMargin = params.leftMargin - (loc[0] - locationX);
                    qrCodeLayout.setLayoutParams(params);
                    qrCodeLayout.requestLayout();
                    LogUtil.d("qrCode Guide real X not equal to locationX.refresh!");
                }
                if (locationY != loc[1]) {
                    qrCodeParams.topMargin = qrCodeParams.topMargin - (loc[1] - locationY);
                    LogUtil.d("qrCode view Y:" + qrCodeParams.topMargin);
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real Y not equal to locationY.refresh!");
                }
            }
        });

        //内容和箭头、二维码间距固定，所以根据动态获取到的locatoinX计算Layout的leftMargin
        params.leftMargin = locationX - descWidth - descMarginLeft - arrowWidth - arrowParams.leftMargin - arrowParams.rightMargin;
        qrCodeLayout.setLayoutParams(params);

        //按钮：我知道了
        int btnWidth = (int) LayouUtil.getDimen("x124");
        int btnHeight = (int) LayouUtil.getDimen("y47");
        LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnWidth, btnHeight);
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
        ImageView ivBtn = new ImageView(GlobalContext.get());
        ivBtn.setImageResource(R.drawable.win_help_guide_btn);
        ivBtn.setLayoutParams(btnParams);
        ivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        llContent.addView(ivBtn);
        return llContent;
    }

    /**
     * 竖屏
     *
     * @param jsonBuilder
     * @return
     */
    public View createViewVerticalScreen(JSONBuilder jsonBuilder) {

        String url = jsonBuilder.getVal("qrCodeUrl", String.class);
        String desc = jsonBuilder.getVal("qrCodeGuideDesc", String.class);
        final int locationX = jsonBuilder.getVal("locationX", Integer.class);
        final int locationY = jsonBuilder.getVal("locationY", Integer.class);

        LinearLayout.LayoutParams contentParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        llContent.setOrientation(LinearLayout.VERTICAL);
        llContent.setLayoutParams(contentParams);

        llContent.setBackgroundColor(Color.parseColor("#CC1A1D23"));
        //按钮：我知道了
        int btnWidth = (int) LayouUtil.getDimen("x167");
        int btnHeight = (int) LayouUtil.getDimen("y28");
        final LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(btnWidth, btnHeight);
        btnParams.gravity = Gravity.CENTER_HORIZONTAL;
//        btnParams.topMargin = (int) LayouUtil.getDimen("y260");
        final ImageView ivBtn = new ImageView(GlobalContext.get());
        ivBtn.setImageResource(R.drawable.win_help_guide_btn);
        ivBtn.setLayoutParams(btnParams);
        ivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        llContent.addView(ivBtn);
        //描述
        int descWidth = (int) LayouUtil.getDimen("y265");
        LinearLayout.LayoutParams descParams = new LinearLayout.LayoutParams(descWidth, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = (int) LayouUtil.getDimen("y20");
        descParams.gravity = Gravity.CENTER_HORIZONTAL;
        TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setText(desc);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setLayoutParams(descParams);
        tvDesc.setGravity(Gravity.CENTER);

        llContent.addView(tvDesc);

        //箭头
        int arrowWidth = (int) LayouUtil.getDimen("x70");
        int arrowHeight = (int) LayouUtil.getDimen("y43");
        int qrCodeWidth = (int) LayouUtil.getDimen("m104");
        LinearLayout.LayoutParams arrowParams = new LinearLayout.LayoutParams(arrowWidth, arrowHeight);
        arrowParams.topMargin = (int) LayouUtil.getDimen("y10");
        LogUtil.d("kevin left margin " + (qrCodeWidth - arrowWidth) / 2);
        arrowParams.leftMargin = locationX + (qrCodeWidth - arrowWidth) / 2;//箭头在二维码正中
        ImageView ivArrow = new ImageView(GlobalContext.get());
        ivArrow.setLayoutParams(arrowParams);
        ivArrow.setImageResource(R.drawable.win_help_guide_arrow2);

        llContent.addView(ivArrow);
        //二维码
        final LinearLayout.LayoutParams qrCodeParams = new LinearLayout.LayoutParams(qrCodeWidth, qrCodeWidth);
        //locationY = 上层view相加
        qrCodeParams.topMargin = (int) LayouUtil.getDimen("y16");
        qrCodeParams.leftMargin = locationX;

        final ImageView qrCodeView = new ImageView(GlobalContext.get());
        try {
            qrCodeView.setImageBitmap(QRUtil.createQRCodeBitmap(url, qrCodeWidth));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        qrCodeView.setLayoutParams(qrCodeParams);
        llContent.addView(qrCodeView);

        qrCodeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                qrCodeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] loc = new int[2];
                qrCodeView.getLocationOnScreen(loc);
                LogUtil.d("qrCode view X:" + loc[0]);
                LogUtil.d("qrCode view Y:" + loc[1]);

                if (locationX != loc[0]) {
                    qrCodeParams.leftMargin = qrCodeParams.leftMargin - (Math.abs(loc[0] - locationX));
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real X not equal to locationX.refresh!");
                }
                if (locationY != loc[1]) {
                    btnParams.topMargin = btnParams.topMargin + (Math.abs(loc[1] - locationY));
                    ivBtn.setLayoutParams(btnParams);
                    ivBtn.requestLayout();
                    LogUtil.d("qrCode Guide real Y not equal to locationY.refresh!");
                }
            }
        });

        return llContent;
    }

    /**
     * 无屏 语音图标在右边的时候  二维码在左，文字靠右
     *
     * @param jsonBuilder
     * @return
     */
    public View createViewNoneScreenRight(JSONBuilder jsonBuilder) {
        String url = jsonBuilder.getVal("qrCodeUrl", String.class);
        String desc = jsonBuilder.getVal("qrCodeGuideDesc", String.class);
        final int locationX = jsonBuilder.getVal("locationX", Integer.class, 0);
        final int locationY = jsonBuilder.getVal("locationY", Integer.class, 0);

        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout rlContent = new RelativeLayout(GlobalContext.get());
        rlContent.setLayoutParams(contentParams);

        rlContent.setBackgroundColor(Color.parseColor("#CC1A1D23"));
        //描述、二维码
        int qrCodeWidth = (int) LayouUtil.getDimen("m104");
        final RelativeLayout.LayoutParams qrCodeParams = new RelativeLayout.LayoutParams(qrCodeWidth, qrCodeWidth);
        qrCodeParams.topMargin = locationY;
        qrCodeParams.leftMargin = locationX;

        final ImageView qrCodeView = new ImageView(GlobalContext.get());
        qrCodeView.setId(ViewUtils.generateViewId());
        try {
            qrCodeView.setImageBitmap(QRUtil.createQRCodeBitmap(url, qrCodeWidth));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        qrCodeView.setLayoutParams(qrCodeParams);
        rlContent.addView(qrCodeView);

        qrCodeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                qrCodeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] loc = new int[2];
                qrCodeView.getLocationOnScreen(loc);
                LogUtil.d("qrCode view X:" + loc[0]);
                LogUtil.d("qrCode view Y:" + loc[1]);

                if (locationX != loc[0]) {
                    qrCodeParams.leftMargin = qrCodeParams.leftMargin - (Math.abs(loc[0] - locationX));
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real X not equal to locationX.refresh!");
                }
                if (locationY != loc[1]) {
                    qrCodeParams.topMargin = qrCodeParams.topMargin - (Math.abs(loc[1] - locationY));
                    LogUtil.d("qrCode view Y:" + qrCodeParams.topMargin);
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real Y not equal to locationY.refresh!");
                }
            }
        });

        RelativeLayout.LayoutParams descParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = (int) LayouUtil.getDimen("m25") + locationY;//等于二维码 marginTop + 距离二维码上边距的31px
        descParams.leftMargin = (int) LayouUtil.getDimen("x16");
        descParams.addRule(RelativeLayout.RIGHT_OF, qrCodeView.getId());

        TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setText(desc);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setLayoutParams(descParams);
        tvDesc.setGravity(Gravity.LEFT);

        rlContent.addView(tvDesc);


        //按钮：我知道了
        int btnWidth = (int) LayouUtil.getDimen("x124");
        int btnHeight = (int) LayouUtil.getDimen("y47");
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(btnWidth, btnHeight);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        int btnBottomMargin = (int) LayouUtil.getDimen("y65");
        btnParams.bottomMargin = btnBottomMargin;
        int btnBottom = locationY + qrCodeWidth + btnHeight;
        //判断二维码的bottom+btnHeight+margin是否超出屏幕
        if (ScreenUtil.getScreenHeight() < btnBottomMargin + btnBottom) {
            btnParams.bottomMargin = (ScreenUtil.getScreenHeight() - btnBottom) / 2;
        }
        ImageView ivBtn = new ImageView(GlobalContext.get());
        ivBtn.setImageResource(R.drawable.win_help_guide_btn);
        ivBtn.setLayoutParams(btnParams);
        ivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        rlContent.addView(ivBtn);

        return rlContent;
    }

    /**
     * 声控图标在左边    二维码右 文字左
     *
     * @param jsonBuilder
     * @return
     */
    public View createViewNoneScreenLeft(JSONBuilder jsonBuilder) {
        String url = jsonBuilder.getVal("qrCodeUrl", String.class);
        String desc = jsonBuilder.getVal("qrCodeGuideDesc", String.class);
        final int locationX = jsonBuilder.getVal("locationX", Integer.class, 0);//从底部页面获取到二维码的x
        final int locationY = jsonBuilder.getVal("locationY", Integer.class, 0);//从底部页面获取到二维码的y

        RelativeLayout.LayoutParams contentParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        RelativeLayout rlContent = new RelativeLayout(GlobalContext.get());
        rlContent.setLayoutParams(contentParams);

        rlContent.setBackgroundColor(Color.parseColor("#CC1A1D23"));
        //描述、二维码

        int qrCodeWidth = (int) LayouUtil.getDimen("m104");
        final RelativeLayout.LayoutParams qrCodeParams = new RelativeLayout.LayoutParams(qrCodeWidth, qrCodeWidth);
        qrCodeParams.topMargin = locationY;
        qrCodeParams.rightMargin = ScreenUtil.getScreenWidth() - locationX - qrCodeWidth;
        qrCodeParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

        final ImageView qrCodeView = new ImageView(GlobalContext.get());
        qrCodeView.setId(ViewUtils.generateViewId());
        try {
            qrCodeView.setImageBitmap(QRUtil.createQRCodeBitmap(url, qrCodeWidth));
        } catch (WriterException e) {
            e.printStackTrace();
        }
        qrCodeView.setLayoutParams(qrCodeParams);
        rlContent.addView(qrCodeView);

        qrCodeView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                qrCodeView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                int[] loc = new int[2];
                qrCodeView.getLocationOnScreen(loc);
                LogUtil.d("qrCode view X:" + loc[0]);
                LogUtil.d("qrCode view Y:" + loc[1]);

                if (locationX != loc[0]) {
                    qrCodeParams.leftMargin = qrCodeParams.leftMargin - (Math.abs(loc[0] - locationX));
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real X not equal to locationX.refresh!");
                }
                if (locationY != loc[1]) {
                    qrCodeParams.topMargin = qrCodeParams.topMargin - (Math.abs(loc[1] - locationY));
                    LogUtil.d("qrCode view Y:" + qrCodeParams.topMargin);
                    qrCodeView.setLayoutParams(qrCodeParams);
                    qrCodeView.requestLayout();
                    LogUtil.d("qrCode Guide real Y not equal to locationY.refresh!");
                }
            }
        });

        RelativeLayout.LayoutParams descParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        descParams.topMargin = (int) LayouUtil.getDimen("m25") + locationY;//等于二维码 marginTop + 距离二维码上边距的31px
        descParams.rightMargin = (int) LayouUtil.getDimen("x16");
        descParams.addRule(RelativeLayout.LEFT_OF, qrCodeView.getId());

        TextView tvDesc = new TextView(GlobalContext.get());
        tvDesc.setText(desc);
        tvDesc.setTextSize(TypedValue.COMPLEX_UNIT_PX, LayouUtil.getDimen("m19"));
        tvDesc.setTextColor(Color.WHITE);
        tvDesc.setLayoutParams(descParams);
        tvDesc.setGravity(Gravity.RIGHT);

        rlContent.addView(tvDesc);

        //按钮：我知道了
        int btnWidth = (int) LayouUtil.getDimen("x124");
        int btnHeight = (int) LayouUtil.getDimen("y47");
        RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(btnWidth, btnHeight);
        btnParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
        btnParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        int btnBottomMargin = (int) LayouUtil.getDimen("y65");
        btnParams.bottomMargin = btnBottomMargin;
        int btnBottom = locationY + qrCodeWidth + btnHeight;
        //判断二维码的bottom+btnHeight+margin是否超出屏幕
        if (ScreenUtil.getScreenHeight() < btnBottomMargin + btnBottom) {
            btnParams.bottomMargin = (ScreenUtil.getScreenHeight() - btnBottom) / 2;
        }
        ImageView ivBtn = new ImageView(GlobalContext.get());
        ivBtn.setImageResource(R.drawable.win_help_guide_btn);
        ivBtn.setLayoutParams(btnParams);
        ivBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        rlContent.addView(ivBtn);

        return rlContent;
    }

    @Override
    public void show() {
        super.show();
        reportShow();
        useAsrWakeup();
        IntentFilter intentFilter = new IntentFilter(
                Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        GlobalContext.get().registerReceiver(mHomeReceiver, intentFilter);
    }

    public void useAsrWakeup() {
        AsrUtil.AsrComplexSelectCallback callback = new AsrUtil.AsrComplexSelectCallback() {
            @Override
            public boolean needAsrState() {
                return false;
            }

            @Override
            public String getTaskId() {
                return TASK_QRCODE_GUIDE;
            }

            @Override
            public void onCommandSelected(String type, String command) {
                if ("CMD_QRCODE_SURE".equals(type)) {
                    dismiss();
                }
            }
        };
        callback.addCommand("CMD_QRCODE_SURE", "我知道了");
        WakeupManager.getInstance().useWakeupAsAsr(callback);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        WakeupManager.getInstance().recoverWakeupFromAsr(TASK_QRCODE_GUIDE);
        reportDismiss();
        GlobalContext.get().unregisterReceiver(mHomeReceiver);
    }

    public void reportShow() {
        ReportUtil.doReport(new ReportUtil.Report.Builder()
                .setType("helpGuideQRCode")
                .setAction("show")
                .buildCommReport());
    }

    public void reportDismiss() {
        ReportUtil.doReport(new ReportUtil.Report.Builder()
                .setType("helpGuideQRCode")
                .setAction("dismiss")
                .buildCommReport());
    }

    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

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
                }
            }
        }
    };

    private int getStatusBarHeight(Context context) {
        Class<?> c = null;
        Object obj = null;
        Field field = null;
        int x = 0, statusBarHeight = 0;
        try {
            c = Class.forName("com.android.internal.R$dimen");
            obj = c.newInstance();
            field = c.getField("status_bar_height");
            x = Integer.parseInt(field.get(obj).toString());
            statusBarHeight = context.getResources().getDimensionPixelSize(x);
        } catch (Exception e1) {
            e1.printStackTrace();
        }
        return statusBarHeight;
    }

    public int getNavigationBarHeight() {
        Resources resources = GlobalContext.get().getResources();
        int resourceId = resources.getIdentifier("config_showNavigationBar", "bool", "android");
        if (resourceId > 0) {
            if (resources.getBoolean(resourceId)) {//导航栏是否显示
                if (resourceId > 0) {
                    return resources.getDimensionPixelSize(resourceId);//获取高度
                }
            }
        }
        return 0;
    }
}
