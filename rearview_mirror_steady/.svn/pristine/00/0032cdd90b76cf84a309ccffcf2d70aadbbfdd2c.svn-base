package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatSysHighlightViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatSysHighlightView;
import com.txznet.comm.util.TextViewUtil;

@SuppressLint("NewApi")
public class ChatSysHighlightView extends IChatSysHighlightView{
	
	private static ChatSysHighlightView sInstance = new ChatSysHighlightView();
	
	private ChatSysHighlightView() {
	}
	
	public static ChatSysHighlightView getInstance(){
		return sInstance;
	}

	private int textSize;    //字体大小
	private int textColor;    //字体颜色
	private int lineSpace;    //行高
	private int chatVerPadding;    //字体上下边距
	private int contentMaxWidth;    //对话框最大宽度
	private int horMargin;    //对话框左右间距
	private int verMargin;    //对话框上下间距

	private int textSizeHalf;    //半屏长文本字体大小
	private int textChatSizeHalf;    //半屏对话字体大小
	private int lineSpaceHalf;    //半屏长文本行高
	private int chatVerPaddingHalf;    //半屏长文本字体上下边距
	private int horMarginHalf;    //半屏长文本对话框左右间距
	private int verMarginHalf;    //半屏长文本对话框上下间距

	private int textSizeNone;    //无屏长文本字体大小
	private int lineSpaceNone;    //无屏长文本行高
	private int chatVerPaddingNone;    //无屏长文本字体上下边距
	private int horMarginNone;    //无屏长文本对话框左右间距
	private int verMarginNone;    //无屏长文本对话框上下间距

	@Override
	public ViewAdapter getView(ViewData data) {

		ChatSysHighlightViewData viewData = (ChatSysHighlightViewData) data;

		ViewAdapter adapter = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				adapter = createViewFull(viewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
				adapter = createViewHalf(viewData);
				break;
			default:
				break;
		}
		return adapter;
	}

	public ViewAdapter createViewFull(ChatSysHighlightViewData data){
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setLineSpacing(lineSpace,0);
		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LayoutParams params = (LayoutParams) content.getLayoutParams();
		params.topMargin = (int) LayouUtil.getDimen("m20");
		params.bottomMargin = (int) LayouUtil.getDimen("m20");
		content.setPadding(horMarginHalf,verMarginHalf+chatVerPaddingHalf,horMarginHalf,verMarginHalf+chatVerPaddingHalf);
		content.setLayoutParams(params);
//		content.setMinHeight((int) LayouUtil.getDimen("y70"));
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(Html.fromHtml(data.textContent));
		if (data.onClickListener!=null) {
			mLayout.setOnClickListener(data.onClickListener);
		}
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatSysHighlightView.getInstance();
		return adapter;
	}

	public ViewAdapter createViewHalf(ChatSysHighlightViewData data){
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text_half");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
//		content.setLineSpacing(lineSpace,0);
//		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		content.setTypeface(Typeface.SERIF);
		LayoutParams params = (LayoutParams) content.getLayoutParams();
//		content.setPadding(leftPadding,topPadding,leftPadding,topPadding);
		content.setLayoutParams(params);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(Html.fromHtml(data.textContent));
		if (data.onClickListener!=null) {
			mLayout.setOnClickListener(data.onClickListener);
		}
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatSysHighlightView.getInstance();
		return adapter;
	}

	public ViewAdapter createViewNone(ChatSysHighlightViewData data){
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text_none");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setTypeface(Typeface.SERIF);
//		content.setLineSpacing(lineSpace,0);
//		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LayoutParams params = (LayoutParams) content.getLayoutParams();
//		content.setPadding(leftPadding,topPadding,leftPadding,topPadding);
		content.setLayoutParams(params);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(Html.fromHtml(data.textContent));
		if (data.onClickListener!=null) {
			mLayout.setOnClickListener(data.onClickListener);
		}
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatSysHighlightView.getInstance();
		return adapter;
	}
	
	@Override
	public void init() {
		textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_FROM_SYS);
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
		LogUtil.d("ChatSysHighlightView", "styleIndex:" + styleIndex + ",textSize:" + textSize);
	}

	//全屏布局参数
	private void initFull(){
		int unit = ViewParamsUtil.unit;
		textSize = ViewParamsUtil.h2;
		lineSpace = textSize * 3 / 2;
		chatVerPadding = textSize / 4;
		contentMaxWidth = WinLayout.isVertScreen?(int)(SizeConfig.screenWidth * 0.85):(int)((SizeConfig.screenWidth*0.75 - 4 * unit)* 0.9);
		horMargin = 3 * unit;    //对话框左右间距
		verMargin = 2 * unit;    //对话框上下间距
	}

	//半屏布局参数
	private void initHalf(){
		int unit = ViewParamsUtil.unit;
		textSizeHalf = ViewParamsUtil.h2;
		textChatSizeHalf = ViewParamsUtil.h2;
		lineSpaceHalf = textSizeHalf * 2;
		chatVerPaddingHalf = (lineSpaceHalf - textSizeHalf)/ 2;
		horMarginHalf = 4 * unit;
		verMarginHalf = 3 * unit;
	}

	//无屏布局参数
	private void initNone(){
		int unit = ViewParamsUtil.unit;
		textSizeNone = ViewParamsUtil.h3;
		lineSpaceNone = textSizeNone * 25 / 14;
		contentMaxWidth = SizeConfig.screenWidth - 26 * unit;
		chatVerPaddingNone = (lineSpaceNone - textSizeNone)/ 2;
		horMarginNone = 3 * unit;
		verMarginNone = 3 * unit;
	}

}
