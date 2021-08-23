package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.zxing.WriterException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.IMoviePhoneNumQRView;
import com.txznet.comm.ui.viewfactory.data.MoviePhoneNumQRViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.QRUtil;

public class MoviePhoneNumQRView extends IMoviePhoneNumQRView {

    private static MoviePhoneNumQRView sInstance = new MoviePhoneNumQRView();

    private int contentHorPadding;    //内容边距
    private int centerInterval;    //二维码到提示内容边距
    private int qrCodeSide;    //二维码宽高
    private int tvHintSize;    //提示字体大小
    private int tvHintColor;    //提示字体颜色
    private int dividerHeight;

    private MoviePhoneNumQRView(){}

    public static MoviePhoneNumQRView getInstance(){
        return sInstance;
    }

    @Override
    public void init() {
        super.init();
        tvHintColor =  Color.parseColor(LayouUtil.getString("color_main_title"));
        dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {
        switch (styleIndex) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                break;
            default:
                break;
        }
    }

    //全屏布局参数
    private void initFull() {
        if (WinLayout.isVertScreen){
            contentHorPadding = 8 * ViewParamsUtil.unit;
            centerInterval = 2 * ViewParamsUtil.unit;
            qrCodeSide = 20 * ViewParamsUtil.unit;
            tvHintSize = ViewParamsUtil.h5;
        }else {
            contentHorPadding = 8 * ViewParamsUtil.unit;
            centerInterval = 2 * ViewParamsUtil.unit;
            qrCodeSide = 20 * ViewParamsUtil.unit;
            tvHintSize = ViewParamsUtil.h5;
        }
    }

    //半屏布局参数
    private void initHalf() {
        if (WinLayout.isVertScreen){
            contentHorPadding = 8 * ViewParamsUtil.unit;
            centerInterval = 2 * ViewParamsUtil.unit;
            qrCodeSide = 20 * ViewParamsUtil.unit;
            tvHintSize = ViewParamsUtil.h5;
        }else {
            contentHorPadding = 18 * ViewParamsUtil.unit;
            centerInterval = 2 * ViewParamsUtil.unit;
            qrCodeSide = 20 * ViewParamsUtil.unit;
            tvHintSize = ViewParamsUtil.h5;
        }
    }

    //无屏布局参数
    private void initNone() {
        contentHorPadding = (int)(3.5 * ViewParamsUtil.unit);
        centerInterval = 2 * ViewParamsUtil.unit;
        qrCodeSide = 20 * ViewParamsUtil.unit;
        tvHintSize = ViewParamsUtil.h5;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        MoviePhoneNumQRViewData viewData = (MoviePhoneNumQRViewData) data;
        WinLayout.getInstance().vTips = viewData.vTips;
        View view;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                view = createViewFull(viewData);
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            default:
                view = createViewNone(viewData);
                break;
        }

        ViewFactory.ViewAdapter viewAdapter = new ViewFactory.ViewAdapter();
        viewAdapter.type = data.getType();
        viewAdapter.view = view;
        viewAdapter.object = MoviePhoneNumQRView.getInstance();
        return viewAdapter;
    }

    private View createViewFull(MoviePhoneNumQRViewData viewData){
        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setGravity(Gravity.CENTER_VERTICAL);
        lyContents.setOrientation(LinearLayout.VERTICAL);

        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(viewData,
                "movie","手机号验证");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        lyContents.addView(titleViewAdapter.view,layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setPadding(contentHorPadding,0,contentHorPadding,0);
        llContents.setBackground(LayouUtil.getDrawable("movie_list_rang_bg"));
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,SizeConfig.pageCount * SizeConfig.itemHeight);
        lyContents.addView(llContents,layoutParams);

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setBackgroundColor(0xFFFFFFFF);
        int padding = ViewParamsUtil.unit / 2;
        imageView.setPadding(padding, padding, padding, padding);
        int h = qrCodeSide;
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.phoneNumQRUrl, h,0);
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
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContents.addView(imageView,layoutParams);

        TextView tvHint = new TextView(GlobalContext.get());
//        TextViewUtil.setTextSize(tvHint,LayouUtil.getDimen("m21"));
//        tvHint.setTextColor(Color.WHITE);
        tvHint.setSingleLine(false);
        tvHint.setText("注意：填写电话号码过程中请勿关闭此页面！");
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = centerInterval;
        llContents.addView(tvHint,layoutParams);

        TextViewUtil.setTextSize(tvHint,tvHintSize);
        TextViewUtil.setTextColor(tvHint,tvHintColor);

        return lyContents;
    }

    private View createViewNone(MoviePhoneNumQRViewData viewData){
        LinearLayout lyContents = new LinearLayout(GlobalContext.get());
        lyContents.setGravity(Gravity.CENTER_VERTICAL);
        lyContents.setOrientation(LinearLayout.VERTICAL);

        ViewFactory.ViewAdapter titleViewAdapter = com.txznet.comm.ui.theme.test.view.ListTitleView.getInstance().getView(viewData,
                "movie","手机号验证");
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, SizeConfig.titleHeight);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        lyContents.addView(titleViewAdapter.view,layoutParams);

        View divider = new View(GlobalContext.get());
        divider.setBackground(LayouUtil.getDrawable("line"));
        layoutParams = new LinearLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        lyContents.addView(divider, layoutParams);

        LinearLayout llContents = new LinearLayout(GlobalContext.get());
        llContents.setPadding(contentHorPadding,0,contentHorPadding,0);
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,0,1);
        lyContents.addView(llContents,layoutParams);

        final ImageView imageView = new ImageView(GlobalContext.get());
        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
        imageView.setBackgroundColor(0xFFFFFFFF);
        int padding = ViewParamsUtil.unit / 2;
        imageView.setPadding(padding, padding, padding, padding);
        int h = qrCodeSide;
        try {
            final Bitmap bitmap = QRUtil.createQRCodeBitmap(viewData.phoneNumQRUrl, h,0);
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
        layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        llContents.addView(imageView,layoutParams);

        TextView tvHint = new TextView(GlobalContext.get());
//        TextViewUtil.setTextSize(tvHint,LayouUtil.getDimen("m21"));
//        tvHint.setTextColor(Color.WHITE);
        tvHint.setSingleLine(false);
        tvHint.setText("注意：填写电话号码过程中请勿关闭此页面！");
        layoutParams = new LinearLayout.LayoutParams(0,LinearLayout.LayoutParams.WRAP_CONTENT,1);
        layoutParams.gravity = Gravity.CENTER_VERTICAL;
        layoutParams.leftMargin = centerInterval;
        llContents.addView(tvHint,layoutParams);

        TextViewUtil.setTextSize(tvHint,tvHintSize);
        TextViewUtil.setTextColor(tvHint,tvHintColor);

        return lyContents;
    }

}
