package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
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

	private float textSize;
	private float lineSpace;
	private int textColor;
	private int leftPadding;
	private int topPadding;

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
		content.setPadding(leftPadding,topPadding,leftPadding,topPadding);
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
		textSize =(int) LayouUtil.getDimen("h2");
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
	}

	public void initFull(){
		if(WinLayout.isVertScreen){
			textSize = (int) LayouUtil.getDimen("vertical_h2");
			int unit = (int) LayouUtil.getDimen("vertical_unit");
			leftPadding = 3 * unit;
			topPadding = (int) (2 * unit + textSize / 4);
			lineSpace = textSize * 3 / 2;
		}else {
			textSize = (int) LayouUtil.getDimen("h2");
			int unit = (int) LayouUtil.getDimen("unit");
			leftPadding = 3 * unit;
			topPadding = (int) (2 * unit + textSize / 4);
			lineSpace = textSize * 3 / 2;
		}

	}

	public void initHalf(){
		if(WinLayout.isVertScreen){
			textSize = (int) LayouUtil.getDimen("vertical_h2");
			int unit = (int) LayouUtil.getDimen("vertical_unit");
			leftPadding = 4 * unit;
			topPadding = (int) (3 * unit + (textSize) / 2);
			lineSpace =  LayouUtil.getDimen("chat_height_half");
		}else {
			textSize = (int) LayouUtil.getDimen("h2");
			int unit = (int) LayouUtil.getDimen("unit");
			leftPadding = 4 * unit;
			topPadding = (int) (3 * unit + (textSize) / 2);
			lineSpace =  LayouUtil.getDimen("chat_height_half");
		}
	}

	public void initNone(){
		if(WinLayout.isVertScreen){
			textSize = (int) LayouUtil.getDimen("x29");
		}else {
			textSize = (int) LayouUtil.getDimen("h2");
		}
		int unit = (int) LayouUtil.getDimen("unit");
		int textSizeNone =(int) LayouUtil.getDimen("h3");
		int lineSpaceNone = textSizeNone * 25 / 14;
		int chatVerPaddingNone = (lineSpaceNone - textSizeNone)/ 2;
		leftPadding = 3 * unit;
		topPadding = 3 * unit + chatVerPaddingNone;
//		textSize = textSizeNone;
		lineSpace = (int) LayouUtil.getDimen("chat_height_half");
	}

}
