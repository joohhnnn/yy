package com.txznet.comm.ui.viewfactory.view.defaults;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.AuthorizationViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IAuthorizationView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.util.QRUtil;

public class AuthorizationView extends IAuthorizationView {
    private static AuthorizationView sAuthorizationView = new AuthorizationView();

    private AuthorizationView() {
    }

    public static AuthorizationView getInstance() {
        return sAuthorizationView;
    }

    @Override
    public void init() {
        super.init();
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        ViewFactory.ViewAdapter adapter = new ViewFactory.ViewAdapter();
        adapter.type = data.getType();
        adapter.view = createView2(data);
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
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight()));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextSize(LayouUtil.getDimen("m18"));
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setText(title);

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
        tvTips.setText(tips);

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        tvSubTitle.setTextSize(LayouUtil.getDimen("m16"));
        tvSubTitle.setTextColor(Color.WHITE);
        tvSubTitle.setText(subTitle);

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

    private View createView2(ViewData data) {
        AuthorizationViewData authorizationViewData = (AuthorizationViewData) data;
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
        tvTitle.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ListTitleView.getInstance().getTitleHeight()));
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setTextSize(LayouUtil.getDimen("m18"));
        tvTitle.setTextColor(Color.WHITE);
        tvTitle.setText(authorizationViewData.mTitle);

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
        tvTips.setText(authorizationViewData.mTips);

        TextView tvSubTitle = new TextView(GlobalContext.get());
        tvSubTitle.setGravity(Gravity.CENTER);
        tvSubTitle.setTextSize(LayouUtil.getDimen("m16"));
        tvSubTitle.setTextColor(Color.WHITE);
        tvSubTitle.setText(authorizationViewData.mSubTitle);

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

}
