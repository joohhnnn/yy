package com.txznet.comm.ui.theme.test.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.imageaware.ImageViewAware;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.HelpTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IHelpTipsView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class HelpTipsView extends IHelpTipsView {
    private static HelpTipsView instance = new HelpTipsView();

    public static HelpTipsView getInstance() {
        return instance;
    }

    private int itemGap;    //列表间隙
    private int titleSize;    //主标题字体大小
    private int titleColor;    //主标题字体颜色
    private int iconSide;    //主标题icon大小
    private int iconMarginRight;    //主标题icon右边距

    @Override
    public void init() {
        mFlags = UPDATEABLE;
        super.init();

        int unit = ViewParamsUtil.unit;
        itemGap = 2 * unit;
        titleSize = ViewParamsUtil.h2;
        titleColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        iconSide = (int)(4.5 * unit);
        iconMarginRight = unit;
        //竖屏全屏、半屏列表字体size加2
        if (StyleConfig.getInstance().getSelectStyleIndex() != StyleConfig.STYLE_ROBOT_NONE_SCREES && WinLayout.isVertScreen){
            titleSize = ViewParamsUtil.h2;
        }

    }

    private ViewGroup convertView;

    @Override
    public ExtViewAdapter getView(ViewData data) {
        LogUtil.logd(WinLayout.logTag+ "getView: HelpTipsView");
        //带有~符号的引导语不需要加上““””引号
        HelpTipsViewData helpTipsViewData = (HelpTipsViewData) data;
//        WinLayout.getInstance().vTips = helpTipsViewData.mTitle+"~true";
        //createView(data);

        convertView = new FrameLayout(UIResLoader.getInstance().getModifyContext());

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = convertView;
        adapter.view.setTag(adapter);
        adapter.object = HelpTipsView.getInstance();
        return adapter;
    }

    @Override
    public Object updateView(ViewData data) {
        LogUtil.logd(WinLayout.logTag+ "updateView: HelpTipsView");
        //createView(data);
        return super.updateView(data);
    }

    //构造页面内容
    private void createView(ViewData data){
        HelpTipsViewData helpTipsViewData = (HelpTipsViewData) data;
        if(convertView == null){
            convertView = (LinearLayout) LayouUtil.getView("help_tip_view");
        }
        /*TextView content = (TextView) LayouUtil.findViewByName("tvTitle",convertView);*/
        LinearLayout llContent = (LinearLayout) LayouUtil.findViewByName("llContent",convertView);

        LinearLayout.LayoutParams layoutParams;/* = (LinearLayout.LayoutParams) content.getLayoutParams();
        layoutParams.topMargin = itemGap;
        layoutParams.bottomMargin = itemGap;
        content.setLayoutParams(layoutParams);

        content.setText(helpTipsViewData.mTitle);*/
        llContent.removeAllViews();
        llContent.setGravity(Gravity.CENTER_HORIZONTAL);
       /* TextViewUtil.setTextSize(content,(int)LayouUtil.getDimen("m27"));
        TextViewUtil.setTextColor(content,minorTitleColor);*/

        for (int i = 0 ; i < helpTipsViewData.getData().size() ; i++) {
            HelpTipsViewData.HelpTipBean bean = helpTipsViewData.getData().get(i);
            FrameLayout view = (FrameLayout) LayouUtil.getView("help_tip_view_item");
            ImageView icon = (ImageView) LayouUtil.findViewByName("icon",view);
            TextView title = (TextView) LayouUtil.findViewByName("title",view) ;
            layoutParams = (LinearLayout.LayoutParams)icon.getLayoutParams();
            title.setText(LanguageConvertor.toLocale(bean.label));
            Drawable iconDrawable = LayouUtil.getDrawable(bean.resId);
            if(iconDrawable == null){
                Bitmap bitmap = getLoacalBitmap(bean.resId); //从本地取图片(在cdcard中获取)  //
                icon.setImageBitmap(bitmap); //设置Bitmap
            }else {
                icon.setImageDrawable(iconDrawable);
            }

            if (i == 2 || helpTipsViewData.getData().size() == 2) {
                layoutParams.width = iconSide;
                layoutParams.height = iconSide;
                layoutParams.rightMargin = iconMarginRight;
                icon.setLayoutParams(layoutParams);
                icon.setScaleX(8.0f/9.0f);
                icon.setScaleY(8.0f/9.0f);
                TextViewUtil.setTextSize(title,titleSize);
                TextViewUtil.setTextColor(title,titleColor);
            }else {
                float alpha = (i + 1) < 3 ? i + 1 : 3 - (i + 1) % 3;
                layoutParams.height = (int)(iconSide * (1 - (3 - alpha) / 5));
                layoutParams.width = (int)(iconSide * (1 - (3 - alpha) / 5));
                icon.setLayoutParams(layoutParams);
                icon.setScaleX(8.0f/9.0f);
                icon.setScaleY(8.0f/9.0f);
                icon.setAlpha(alpha / 3);
                title.setAlpha(alpha / 3);

                TextViewUtil.setTextSize(title,(int)(titleSize/2 + titleSize/2 *(alpha / 3)));
                TextViewUtil.setTextColor(title,titleColor);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = itemGap;
            llContent.addView(view,params);
        }
    }

    public static Bitmap getLoacalBitmap(String url) {
        try {
            FileInputStream fis = new FileInputStream(url);
            return BitmapFactory.decodeStream(fis);  ///把流转化为Bitmap图片

        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

}
