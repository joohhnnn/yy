package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatSysInterruptTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatSysInterruptView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class ChatSysInterruptView extends IChatSysInterruptView{
	
	private static ChatSysInterruptView sInstance = new ChatSysInterruptView();
	
	private ChatSysInterruptView() {
	}
	
	public static ChatSysInterruptView getInstance(){
		return sInstance;
	}

	private int textSize;    //字体大小
	private int textColor;    //字体颜色
	private int lineSpace;    //行高
	private int chatVerPadding;    //字体上下边距
	private int contentMaxWidth;    //对话框最大宽度
	private int horMargin;    //对话框左右间距
	private int verMargin;    //对话框上下间距

	//private int textSizeHalf;    //半屏长文本字体大小
	//private int lineSpaceHalf;    //半屏长文本行高
	//private int chatVerPaddingHalf;    //半屏长文本字体上下边距
	//private int horMarginHalf;    //半屏长文本对话框左右间距
	//private int verMarginHalf;    //半屏长文本对话框上下间距

//	private int textSizeNone;    //无屏长文本字体大小
//	private int lineSpaceNone;    //无屏长文本行高
//	private int chatVerPaddingNone;    //无屏长文本字体上下边距
//	private int horMarginNone;    //无屏长文本对话框左右间距
//	private int verMarginNone;    //无屏长文本对话框上下间距
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatSysInterruptTipsViewData viewData = (ChatSysInterruptTipsViewData) data;
		LogUtil.logd(WinLayout.logTag+ "ChatSysInterruptTipsViewData: get");
		View view = null;
		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				LogUtil.logd(WinLayout.logTag+ "getView: full");
				view = createViewFull(viewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
				view = createViewHalf(viewData);
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				LogUtil.logd(WinLayout.logTag+ "getView: not full");
				view = createViewNone(viewData);
				break;
		}


		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = view;
		adapter.view.setTag(data.getType());
		adapter.object = ChatSysInterruptView.getInstance();
		return adapter;
	}

	private View createViewFull(ChatSysInterruptTipsViewData viewData){
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LinearLayout.LayoutParams layoutParams =(LayoutParams) content.getLayoutParams();
		layoutParams.setMargins(0,verMargin,0,verMargin);
		content.setMaxWidth(contentMaxWidth);
		content.setLineSpacing(lineSpace,0);
		content.setPadding(horMargin,verMargin+chatVerPadding,horMargin,verMargin+chatVerPadding);

		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(viewData.textContent);
		if (viewData.onClickListener!=null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}

		return mLayout;
	}

	private View createViewHalf(ChatSysInterruptTipsViewData viewData){
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		//content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		/*LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text_interrupt");
		TextView content = (TextView) LayouUtil.findViewByName("tv_chat_msg_interrupt", mLayout);
		TextView title = (TextView) LayouUtil.findViewByName("tv_chat_interrupt_tips", mLayout);*/
		//content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LayoutParams params = (LayoutParams) content.getLayoutParams();
		params.gravity = Gravity.CENTER_HORIZONTAL;
		content.setLayoutParams(params);
		content.setMinHeight((int) LayouUtil.getDimen("y70"));
        TextViewUtil.setTextSize(content, textSize);
        TextViewUtil.setTextColor(content,textColor);
        content.setLineSpacing(lineSpace,0);
        content.setPadding(horMargin,verMargin+chatVerPadding,horMargin,verMargin+chatVerPadding);
		content.setText(viewData.textContent);
		int padding = content.getPaddingLeft();
		/*title.setPadding(padding, 0, 0, 0);
		TextViewUtil.setTextSize(title, textSize - 2);
		title.setText(Html.fromHtml(viewData.titleContent));*/
		if (viewData.onClickListener!=null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}

		return mLayout;
	}

	private View createViewNone(ChatSysInterruptTipsViewData viewData){
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text_none");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);

		content.setSingleLine(false);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
        content.setLineSpacing(lineSpace,0);
        content.setPadding(horMargin,verMargin+chatVerPadding,horMargin,verMargin+chatVerPadding);
		content.setText(viewData.textContent);
		if (viewData.onClickListener!=null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}

		return mLayout;
	}

	@Override
	public void init() {
		super.init();
        textColor = Color.WHITE;

	}

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex){
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

    //全屏模式布局
    private void initFull(){
//        if (WinLayout.isVertScreen){
//            int unit = (int) LayouUtil.getDimen("vertical_unit");
//            textSize = (int) LayouUtil.getDimen("vertical_h2");
//            lineSpace = textSize * 3 / 2;
//            chatVerPadding = textSize / 4;
//            contentMaxWidth = (int)(SizeConfig.screenWidth * 0.85);
//            horMargin = 3 * unit;    //对话框左右间距
//            verMargin = 2 * unit;    //对话框上下间距
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            textSize = (int) LayouUtil.getDimen("h2");
//            lineSpace = textSize * 3 / 2;
//            chatVerPadding = textSize / 4;
//            contentMaxWidth = (int)((SizeConfig.screenWidth*0.75 - 4 * unit)* 0.9);
//            horMargin = 3 * unit;    //对话框左右间距
//            verMargin = 2 * unit;    //对话框上下间距
//        }
		int unit = ViewParamsUtil.unit;
		textSize = ViewParamsUtil.h2;
		lineSpace = textSize * 3 / 2;
		chatVerPadding = textSize / 4;
		contentMaxWidth = WinLayout.isVertScreen?(int)(SizeConfig.screenWidth * 0.85):(int)((SizeConfig.screenWidth*0.75 - 4 * unit)* 0.9);
		horMargin = 3 * unit;    //对话框左右间距
		verMargin = 2 * unit;    //对话框上下间距
    }

    //半屏模式布局
    private void initHalf(){
//        if (WinLayout.isVertScreen){
//            int unit = (int) LayouUtil.getDimen("vertical_unit");
//            textSize = (int) LayouUtil.getDimen("vertical_h2");
//            lineSpace = (int) LayouUtil.getDimen("vertical_chat_height_half");
//            chatVerPadding = (textSize - lineSpace) / 2;
//            contentMaxWidth = (int)(SizeConfig.screenWidth * 0.85);
//            horMargin = 5 * unit;    //对话框左右间距
//            verMargin = 3 * unit;    //对话框上下间距
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            textSize = (int) LayouUtil.getDimen("h2");
//            lineSpace = (int) LayouUtil.getDimen("chat_height_half");
//            chatVerPadding =  (textSize - lineSpace) / 2;
//            contentMaxWidth = (int)((SizeConfig.screenWidth*0.75 - 4 * unit)* 0.9);
//            horMargin = 5 * unit;    //对话框左右间距
//            verMargin = 3 * unit;    //对话框上下间距
//        }
		int unit = ViewParamsUtil.unit;
		textSize = ViewParamsUtil.h2;
		lineSpace = ViewParamsUtil.h2;
		chatVerPadding = (lineSpace - textSize)/ 2;
		contentMaxWidth = WinLayout.isVertScreen?(int)(SizeConfig.screenWidth * 0.85):(int)((SizeConfig.screenWidth*0.75 - 4 * unit)* 0.9);
		horMargin = 5 * unit;
		verMargin = 3 * unit;

    }

    //无屏模式布局
    private void initNone(){
//        if (WinLayout.isVertScreen){
//            textSize = (int) LayouUtil.getDimen("x30");
//            lineSpace = (int) LayouUtil.getDimen("x51");
//            chatVerPadding = (textSize - lineSpace) / 2;
//            contentMaxWidth = (int) LayouUtil.getDimen("x603");
//            horMargin = (int) LayouUtil.getDimen("x31");    //对话框左右间距
//            verMargin = (int) LayouUtil.getDimen("x31");    //对话框上下间距
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            textSize = (int) LayouUtil.getDimen("h2_none");
//            lineSpace = (int) LayouUtil.getDimen("chat_height_none");
//            chatVerPadding =  (textSize - lineSpace) / 2;
//            contentMaxWidth = 58 * unit;
//            horMargin = 3 * unit;    //对话框左右间距
//            verMargin = 3 * unit;    //对话框上下间距
//        }
		int unit = ViewParamsUtil.unit;
		textSize = ViewParamsUtil.h3;
		lineSpace = textSize * 25 / 14;
		chatVerPadding = (lineSpace - textSize)/ 2;
		contentMaxWidth = 58 * unit;
		horMargin = 3 * unit;
		verMargin = 3 * unit;
    }

}
