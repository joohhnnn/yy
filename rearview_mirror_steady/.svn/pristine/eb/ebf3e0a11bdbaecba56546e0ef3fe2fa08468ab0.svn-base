package com.txznet.comm.ui.theme.test.view;

import android.graphics.Color;
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
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
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


public class HelpTipsView extends IHelpTipsView {
	private static HelpTipsView instance = new HelpTipsView();

	public static HelpTipsView getInstance() {
		return instance;
	}

	private int itemGap;    //列表间隙
	private int mainTitleSize;    //主标题字体大小
	private int mainTitleColor;    //主标题字体颜色
	private int mainIconSide;    //主标题icon大小
	private int mainIconMarginRight;    //主标题icon右边距
    private int minorTitleSize;    //副标题字体大小
    private int minorTitleColor;    //副标题字体颜色
    private int minorIconSide;    //副标题icon大小
    private int minorIconMarginRight;    //副标题icon右边距

	@Override
	public void init() {
        LogUtil.logd(WinLayout.logTag+ "initView: HelpTipsView");
		mFlags = UPDATEABLE;
		super.init();

        mainTitleColor = Color.parseColor(LayouUtil.getString("color_main_title"));
        minorTitleColor = Color.parseColor(LayouUtil.getString("color_vice_title"));
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("vertical_unit");
            itemGap = 2 * unit;
            mainTitleSize = (int)LayouUtil.getDimen("vertical_h2");
            mainIconSide = 8 * unit;
            mainIconMarginRight = 2 * unit;
            minorTitleSize = (int)LayouUtil.getDimen("vertical_h6");
            minorIconSide = 4 * unit;
            minorIconMarginRight = unit;
        }else {
            int unit = (int) LayouUtil.getDimen("unit");
            itemGap = 2 * unit;
            mainTitleSize = (int)LayouUtil.getDimen("h2");
            mainIconSide = 8 * unit;
            mainIconMarginRight = 2 * unit;
            minorTitleSize = (int)LayouUtil.getDimen("h6");
            minorIconSide = 4 * unit;
            minorIconMarginRight = unit;
        }

	}

	private LinearLayout convertView;

	@Override
	public ViewAdapter getView(ViewData data) {
		LogUtil.logd(WinLayout.logTag+ "getView: HelpTipsView");
        //带有~符号的引导语不需要加上““””引号
        HelpTipsViewData helpTipsViewData = (HelpTipsViewData) data;
        WinLayout.getInstance().vTips = helpTipsViewData.mTitle+"~true";
        createView(data);

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = convertView;
		adapter.view.setTag(data.getType());
		adapter.object = HelpTipsView.getInstance();
		return adapter;
	}

	@Override
	public Object updateView(ViewData data) {
        LogUtil.logd(WinLayout.logTag+ "updateView: HelpTipsView");
        createView(data);
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
            if(LayouUtil.getDrawable(bean.resId) == null){
                ImageLoader.getInstance().displayImage(
                        "file://" + bean.resId, new ImageViewAware(icon));
            }else {
                icon.setImageDrawable(LayouUtil.getDrawable(bean.resId));
            }

            if (i == 2 || helpTipsViewData.getData().size() == 2) {
                layoutParams.width = (int)LayouUtil.getDimen("m36");
                layoutParams.height = (int)LayouUtil.getDimen("m36");
                layoutParams.rightMargin = (int)LayouUtil.getDimen("x5");
                icon.setLayoutParams(layoutParams);
                icon.setScaleX(8.0f/9.0f);
                icon.setScaleY(8.0f/9.0f);
                TextViewUtil.setTextSize(title,minorTitleSize + (int)LayouUtil.getDimen("m12"));
                TextViewUtil.setTextColor(title,mainTitleColor);
            }else {
                float alpha = (i + 1) < 3 ? i + 1 : 3 - (i + 1) % 3;
                layoutParams.height = (int)(LayouUtil.getDimen("m36") * (1 - (3 - alpha) / 5));
                layoutParams.width = (int)(LayouUtil.getDimen("m36") * (1 - (3 - alpha) / 5));
                icon.setLayoutParams(layoutParams);
                icon.setScaleX(8.0f/9.0f);
                icon.setScaleY(8.0f/9.0f);
                icon.setAlpha(alpha / 3);
                title.setAlpha(alpha / 3);

                TextViewUtil.setTextSize(title,minorTitleSize + (int)LayouUtil.getDimen("m12") *(alpha / 3));
                TextViewUtil.setTextColor(title,minorTitleColor);
            }
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            params.topMargin = itemGap;
            llContent.addView(view,params);
        }
    }

}
