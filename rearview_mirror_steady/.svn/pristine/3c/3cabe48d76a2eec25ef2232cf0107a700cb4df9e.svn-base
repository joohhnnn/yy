package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.AuthorizationViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAuthorizationView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.QRUtil;

public class AuthorizationView extends IAuthorizationView {
    private static AuthorizationView sAuthorizationView = new AuthorizationView();

    private int contentHeight;    //内容高度
    private int titleHeight;    //标题高度
    private int titleSize;    //标题字体大小
    private int titleColor;    //标题字体颜色
    private int lineHeight;    //分隔线高度
    private int ivSide;    //二维码边长
    private int tvSubTitleSize;    //二维码下面字体大小
    private int tvSubTitleHeight;    //二维码下面字体行高
    private int tvSubTitleTopMargin;    //二维码下面字体上边距
    private int tvTipsLeftMargin;    //文字内容左边距
    private int tvTipsWidth;    //文字内容宽度
    private int tvTipsSize;    //文字内容字体大小

    private AuthorizationView() {
    }

    public static AuthorizationView getInstance() {
        return sAuthorizationView;
    }

    @Override
    public void init() {
        super.init();

        lineHeight = (int) LayouUtil.getDimen("y1");
        if (lineHeight ==0){
            lineHeight = 1;
        }

        titleColor = Color.parseColor(LayouUtil.getString("color_main_title"));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex){
        int unit = ViewParamsUtil.unit;
        tvSubTitleTopMargin = unit;
        contentHeight = SizeConfig.pageCount * SizeConfig.itemHeight;
        titleHeight = 8 * unit;
        titleSize = ViewParamsUtil.h4;
        ivSide = 20 * unit;
        tvSubTitleSize = ViewParamsUtil.h7;
        tvSubTitleHeight = ViewParamsUtil.h7Height;
        tvTipsLeftMargin = 3 * unit;
        tvTipsWidth = 35 * unit;
        tvTipsSize = ViewParamsUtil.h5;
    }

    @Override
    public ViewAdapter getView(ViewData data) {
        AuthorizationViewData authorizationViewData = (AuthorizationViewData) data;
        WinLayout.getInstance().vTips = authorizationViewData.vTips;
        LogUtil.logd(WinLayout.logTag+ "getView: authorizationViewData:"+authorizationViewData.vTips);

        View view = null;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = WinLayout.isVertScreen?createVerticalViewFull(authorizationViewData):createViewFull(authorizationViewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(authorizationViewData);
                break;
        }

        ViewAdapter adapter = new ViewAdapter();
        adapter.type = data.getType();
        //adapter.view = createView2(data);
        adapter.view = view;
        adapter.object = AuthorizationView.getInstance();
        return adapter;
    }

    public View createView(JSONBuilder jsonBuilder) {
        String url = jsonBuilder.getVal(AuthorizationViewData.KEY_URL, String.class, "");
        String title = jsonBuilder.getVal(AuthorizationViewData.KEY_TITLE, String.class, "");
        String subTitle = jsonBuilder.getVal(AuthorizationViewData.KEY_SUB_TITLE, String.class, "");
        String tips = jsonBuilder.getVal(AuthorizationViewData.KEY_TIPS, String.class, "");

        LinearLayout llMain = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llMainParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y350"));
        llMain.setOrientation(LinearLayout.VERTICAL);
        llMain.setLayoutParams(llMainParams);
        if (Build.VERSION.SDK_INT >= 16) {
            llMain.setBackground(LayouUtil.getDrawable("white_range_layout"));
        } else {
            llMain.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
        }

        RelativeLayout relativeLayout = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(rlLayoutParams);
        relativeLayout.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(GlobalContext.get());
        //tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight()));
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextSize(LayouUtil.getDimen("m18"));
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setText(LanguageConvertor.toLocale(title));

        View lineView = new View(GlobalContext.get());
        LinearLayout.LayoutParams lineViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y1"));
        lineViewParams.leftMargin = (int) LayouUtil.getDimen("x50");
        lineViewParams.rightMargin = (int) LayouUtil.getDimen("x50");
        lineView.setLayoutParams(lineViewParams);
        lineView.setBackgroundColor(Color.parseColor("#4c4c4c"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.setLayoutParams(llContentParams);
        llContent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llLeft = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llLeftParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        llLeftParams.weight = 1;
        llLeft.setGravity(Gravity.CENTER);
        llLeft.setLayoutParams(llLeftParams);
        llLeft.setOrientation(LinearLayout.VERTICAL);

        TextView tvTips = new TextView(GlobalContext.get());
        tvTips.setGravity(Gravity.START);
        tvTips.setTextSize(LayouUtil.getDimen("m16"));
        tvTips.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipsParams.weight = 1;
        tvTips.setLayoutParams(tipsParams);
        tvTips.setText(LanguageConvertor.toLocale(tips));

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        tvSubTitle.setTextSize(LayouUtil.getDimen("m16"));
        tvSubTitle.setTextColor(Color.WHITE);
        tvSubTitle.setText(LanguageConvertor.toLocale(subTitle));

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int h = (int) LayouUtil.getDimen("y150");
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmapNoWhite(url, h);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    imageView.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }

        llLeft.addView(imageView);
        llLeft.addView(tvSubTitle);
        llContent.addView(llLeft);
        llContent.addView(tvTips);
        relativeLayout.addView(llContent);

        llMain.addView(tvTitle);
        llMain.addView(lineView);
        llMain.addView(relativeLayout);
        return llMain;
    }

    private View createView2(AuthorizationViewData authorizationViewData) {
        LinearLayout rootView = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams rootViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        rootView.setLayoutParams(rootViewParams);
        rootView.setOrientation(LinearLayout.VERTICAL);

        LinearLayout.LayoutParams viewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y50"));

        LinearLayout llMain = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llMainParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y368"));
        llMain.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= 16) {
            llMain.setBackground(LayouUtil.getDrawable("white_range_layout"));
        } else {
            llMain.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
        }

        RelativeLayout relativeLayout = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(rlLayoutParams);
        relativeLayout.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(GlobalContext.get());
        //tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight()));
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,SizeConfig.titleHeight));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextSize(LayouUtil.getDimen("m18"));
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mTitle));

        View lineView = new View(GlobalContext.get());
        LinearLayout.LayoutParams lineViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int) LayouUtil.getDimen("y1"));
        lineViewParams.leftMargin = (int) LayouUtil.getDimen("x50");
        lineViewParams.rightMargin = (int) LayouUtil.getDimen("x50");
        lineView.setLayoutParams(lineViewParams);
        lineView.setBackgroundColor(Color.parseColor("#4c4c4c"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.setLayoutParams(llContentParams);
        llContent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llLeft = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llLeftParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        llLeftParams.weight = 1;
        llLeft.setGravity(Gravity.CENTER);
        llLeft.setLayoutParams(llLeftParams);
        llLeft.setOrientation(LinearLayout.VERTICAL);

        TextView tvTips = new TextView(GlobalContext.get());
        tvTips.setGravity(Gravity.START);
        tvTips.setTextSize(LayouUtil.getDimen("m16"));
        tvTips.setTextColor(Color.WHITE);
        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipsParams.weight = 1;
        tvTips.setLayoutParams(tipsParams);
        tvTips.setText(LanguageConvertor.toLocale(authorizationViewData.mTips));

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        tvSubTitle.setTextSize(LayouUtil.getDimen("m16"));
        tvSubTitle.setTextColor(Color.WHITE);
        tvSubTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mSubTitle));

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        int h = (int) LayouUtil.getDimen("y150");
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmapNoWhite(authorizationViewData.mUrl, h);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    imageView.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }

        llLeft.addView(imageView);
        llLeft.addView(tvSubTitle);
        llContent.addView(llLeft);
        llContent.addView(tvTips);
        relativeLayout.addView(llContent);

        llMain.addView(tvTitle);
        llMain.addView(lineView);
        llMain.addView(relativeLayout);
        rootView.addView(new View(GlobalContext.get()), viewLayoutParams);
        rootView.addView(llMain, llMainParams);
        return rootView;
    }

    private View createViewFull(AuthorizationViewData authorizationViewData) {
        LinearLayout llMain = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llMainParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, contentHeight);
        //llMain.setLayoutParams(llMainParams);
        llMain.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= 16) {
            llMain.setBackground(LayouUtil.getDrawable("white_range_layout"));
        } else {
            llMain.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
        }

        RelativeLayout relativeLayout = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(rlLayoutParams);
        relativeLayout.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,titleHeight));
        tvTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTitle,titleSize);
        TextViewUtil.setTextColor(tvTitle,titleColor);
        tvTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mTitle));

        View lineView = new View(GlobalContext.get());
        LinearLayout.LayoutParams lineViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lineHeight);
        lineView.setLayoutParams(lineViewParams);
        lineView.setBackground(LayouUtil.getDrawable("line"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.setLayoutParams(llContentParams);
        llContent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llLeft = new LinearLayout(GlobalContext.get());
        llLeft.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams llLeftParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llLeftParams.gravity = Gravity.CENTER_VERTICAL;
        llLeft.setLayoutParams(llLeftParams);
        llLeft.setOrientation(LinearLayout.VERTICAL);

        TextView tvTips = new TextView(GlobalContext.get());
        tvTips.setGravity(Gravity.START);
        TextViewUtil.setTextSize(tvTips,tvTipsSize);
        TextViewUtil.setTextColor(tvTips,titleColor);
        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipsParams.gravity = Gravity.CENTER_VERTICAL;
        tipsParams.leftMargin = tvTipsLeftMargin;
        tvTips.setLayoutParams(tipsParams);
        tvTips.setText(LanguageConvertor.toLocale(authorizationViewData.mTips));

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvSubTitle,tvSubTitleSize);
        TextViewUtil.setTextColor(tvSubTitle,titleColor);
        LinearLayout.LayoutParams subTitleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvSubTitle.setLayoutParams(subTitleParams);
        tvSubTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mSubTitle));

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //int h = (int) LayouUtil.getDimen("y150");
        int h = ivSide;
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmapNoWhite(authorizationViewData.mUrl, h);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    imageView.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }

        llLeft.addView(imageView);
        llLeft.addView(tvSubTitle);
        llContent.addView(llLeft);
        llContent.addView(tvTips);
        relativeLayout.addView(llContent);

        llMain.addView(tvTitle);
        llMain.addView(lineView);
        llMain.addView(relativeLayout);

        LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(llMain,llMainParams);

        return linearLayout;
    }

    private View createVerticalViewFull(AuthorizationViewData authorizationViewData) {
        LinearLayout llMain = new LinearLayout(GlobalContext.get());
        FrameLayout.LayoutParams llMainParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, contentHeight);
        llMainParams.gravity = Gravity.CENTER;
        //llMain.setLayoutParams(llMainParams);
        llMain.setOrientation(LinearLayout.VERTICAL);
        if (Build.VERSION.SDK_INT >= 16) {
            llMain.setBackground(LayouUtil.getDrawable("white_range_layout"));
        } else {
            llMain.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
        }

        RelativeLayout relativeLayout = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(rlLayoutParams);
        relativeLayout.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,titleHeight));
        tvTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTitle,titleSize);
        TextViewUtil.setTextColor(tvTitle,titleColor);
        tvTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mTitle));

        View lineView = new View(GlobalContext.get());
        LinearLayout.LayoutParams lineViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lineHeight);
        lineView.setLayoutParams(lineViewParams);
        lineView.setBackground(LayouUtil.getDrawable("line"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.setLayoutParams(llContentParams);
        llContent.setOrientation(LinearLayout.VERTICAL);

        LinearLayout llLeft = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llLeftParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llLeftParams.gravity = Gravity.CENTER_HORIZONTAL;
        llLeft.setGravity(Gravity.CENTER);
        llLeft.setLayoutParams(llLeftParams);
        llLeft.setOrientation(LinearLayout.VERTICAL);

        TextView tvTips = new TextView(GlobalContext.get());
        tvTips.setGravity(Gravity.START);
        TextViewUtil.setTextSize(tvTips,tvTipsSize);
        TextViewUtil.setTextColor(tvTips,titleColor);
        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(tvTipsWidth, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipsParams.gravity = Gravity.CENTER_VERTICAL;
        tipsParams.topMargin = tvTipsLeftMargin;
        tvTips.setLayoutParams(tipsParams);
        //按照设计图来换行
        if (authorizationViewData.mTips != null){
            tvTips.setText(LanguageConvertor.toLocale(authorizationViewData.mTips.
                    replace("\n","").replace("。","。\n")));
        }

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvSubTitle,tvSubTitleSize);
        TextViewUtil.setTextColor(tvSubTitle,titleColor);
        LinearLayout.LayoutParams subTitleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvSubTitle.setLayoutParams(subTitleParams);
        tvSubTitle.setText(authorizationViewData.mSubTitle);

        final ImageView imageView = new ImageView(GlobalContext.get());
        LinearLayout.LayoutParams ivLayoutParams = new LinearLayout.LayoutParams(ivSide,ivSide);
        imageView.setLayoutParams(ivLayoutParams);
        //imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //int h = (int) LayouUtil.getDimen("y150");
        int h = ivSide;
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmapNoWhite(authorizationViewData.mUrl, h);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    imageView.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }

        llLeft.addView(imageView);
        llLeft.addView(tvSubTitle);
        llContent.addView(llLeft);
        llContent.addView(tvTips);
        relativeLayout.addView(llContent);

        llMain.addView(tvTitle);
        llMain.addView(lineView);
        llMain.addView(relativeLayout);

        FrameLayout frameLayout = new FrameLayout(GlobalContext.get());
        //frameLayout.setOrientation(LinearLayout.VERTICAL);
        //frameLayout.setGravity(Gravity.CENTER);
        frameLayout.addView(llMain,llMainParams);

        return frameLayout;
    }

    private View createViewNone(AuthorizationViewData authorizationViewData) {
        LinearLayout llMain = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llMainParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        llMain.setLayoutParams(llMainParams);
        llMain.setOrientation(LinearLayout.VERTICAL);
        /*if (Build.VERSION.SDK_INT >= 16) {
            llMain.setBackground(LayouUtil.getDrawable("white_range_layout"));
        } else {
            llMain.setBackgroundDrawable(LayouUtil.getDrawable("white_range_layout"));
        }*/

        RelativeLayout relativeLayout = new RelativeLayout(GlobalContext.get());
        RelativeLayout.LayoutParams rlLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        relativeLayout.setLayoutParams(rlLayoutParams);
        relativeLayout.setGravity(Gravity.CENTER);

        TextView tvTitle = new TextView(GlobalContext.get());
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,titleHeight));
        tvTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvTitle,titleSize);
        TextViewUtil.setTextColor(tvTitle,titleColor);
        tvTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mTitle));

        View lineView = new View(GlobalContext.get());
        LinearLayout.LayoutParams lineViewParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lineHeight);
        lineView.setLayoutParams(lineViewParams);
        lineView.setBackground(LayouUtil.getDrawable("line"));

        LinearLayout llContent = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llContentParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llContent.setLayoutParams(llContentParams);
        llContent.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout llLeft = new LinearLayout(GlobalContext.get());
        LinearLayout.LayoutParams llLeftParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        llLeftParams.gravity = Gravity.CENTER_VERTICAL;
        llLeft.setGravity(Gravity.CENTER);
        llLeft.setLayoutParams(llLeftParams);
        llLeft.setOrientation(LinearLayout.VERTICAL);

        TextView tvTips = new TextView(GlobalContext.get());
        tvTips.setGravity(Gravity.START);
        TextViewUtil.setTextSize(tvTips,tvTipsSize);
        TextViewUtil.setTextColor(tvTips,titleColor);
        LinearLayout.LayoutParams tipsParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tipsParams.leftMargin = tvTipsLeftMargin;
        tipsParams.gravity = Gravity.CENTER_VERTICAL;
        tvTips.setLayoutParams(tipsParams);
        tvTips.setText(LanguageConvertor.toLocale(authorizationViewData.mTips));

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        TextViewUtil.setTextSize(tvSubTitle,tvSubTitleSize);
        TextViewUtil.setTextColor(tvSubTitle,titleColor);
        LinearLayout.LayoutParams subTitleParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvSubTitle.setLayoutParams(subTitleParams);
        tvSubTitle.setText(LanguageConvertor.toLocale(authorizationViewData.mSubTitle));

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        //int h = (int) LayouUtil.getDimen("y150");
        int h = ivSide;
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmapNoWhite(authorizationViewData.mUrl, h);
            UI2Manager.runOnUIThread(new Runnable() {

                @Override
                public void run() {
                    if (bitmap == null) {
                        return;
                    }
                    imageView.setImageBitmap(bitmap);
                }
            }, 0);
        } catch (WriterException ignored) {
        }

        llLeft.addView(imageView);
        llLeft.addView(tvSubTitle);
        llContent.addView(llLeft);
        llContent.addView(tvTips);
        relativeLayout.addView(llContent);

        llMain.addView(tvTitle);
        llMain.addView(lineView);
        llMain.addView(relativeLayout);

        return llMain;
    }

}
