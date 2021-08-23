package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.widget.PrinterTextView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatToSysPartViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatToSysPartView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class ChatToSysPartView extends IChatToSysPartView {

	private static ChatToSysPartView sInstance = new ChatToSysPartView();

	private int textSize;    //字体大小
	private int textColor;    //字体颜色
	private int lineSpace;    //行高
	private int chatVerPadding;    //字体上下边距
	private int contentMaxWidth;    //对话框最大宽度
	private int horMargin;    //对话框左右间距
	private int verMargin;    //对话框上下间距

	private PrinterTextView content;

	private ChatToSysPartView() {
	}

	public static ChatToSysPartView getInstance() {
		return sInstance;
	}

	int i = 0;

	@Override
	public ViewAdapter getView(ViewData data) {
		ChatToSysPartViewData viewData = (ChatToSysPartViewData) data;

		LogUtil.logd(WinLayout.logTag+ "ChatToSysPartView.getView "+ viewData.textContent);

		ViewAdapter adapter = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
		case StyleConfig.STYLE_ROBOT_FULL_SCREES:
			adapter = createViewFull(viewData);
			break;
		case StyleConfig.STYLE_ROBOT_NONE_SCREES:
            //adapter = createViewNone(viewData);
            break;
		case StyleConfig.STYLE_ROBOT_HALF_SCREES:
		default:
			adapter = createViewHalf(viewData);
			break;
		}

		return adapter;
	}

	private ViewAdapter createViewFull(ChatToSysPartViewData viewData) {

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getModifyView("chat_to_sys_text");
		content = (PrinterTextView) LayouUtil.findViewByName("txtChat_Msg_Text",
				mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_to_sys"));
		LinearLayout.LayoutParams layoutParams =(LayoutParams) content.getLayoutParams();
		layoutParams.setMargins(0,verMargin,0,verMargin);
		content.setMaxWidth(contentMaxWidth);
		content.setLineSpacing(lineSpace,0);
		content.setPadding(horMargin,verMargin+chatVerPadding,horMargin,verMargin+chatVerPadding);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatToSysPartView.getInstance();
		return adapter;
	}
	
	private ViewAdapter createViewHalf(ChatToSysPartViewData viewData) {

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getModifyView("chat_to_sys_text_none");
		content = (PrinterTextView) LayouUtil.findViewByName(
				"txtChat_Msg_Text", mLayout);
//		content.setMaxWidth(textMaxWidth = (ScreenUtil.getScreenWidth()));
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content, textColor);
		String str = viewData.textContent;
		/*while(str.length() > 12) {
			str =  str.substring(1, str.length());
		}*/
		if (WinLayout.isVertScreen){
			content.setMaxLines(2);
		}else {
			content.setSingleLine(true);
		}
		content.setEllipsize(TextUtils.TruncateAt.START);
		content.setText(LanguageConvertor.toLocale(str));
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatToSysPartView.getInstance();
		return adapter;
	}

    private ViewAdapter createViewNone(ChatToSysPartViewData viewData) {

        LinearLayout mLayout = (LinearLayout) LayouUtil
                .getModifyView("chat_to_sys_text_none");
        content = (PrinterTextView) LayouUtil.findViewByName(
                "txtChat_Msg_Text", mLayout);
//		content.setMaxWidth(textMaxWidth = (ScreenUtil.getScreenWidth()));
        TextViewUtil.setTextSize(content, textSize);
        TextViewUtil.setTextColor(content, textColor);
        String str = viewData.textContent;
		/*while(str.length() > 12) {
			str =  str.substring(1, str.length());
		}*/
            content.setSingleLine(true);
        //content.setSingleLine(true);
        content.setEllipsize(TextUtils.TruncateAt.START);
        content.setText(LanguageConvertor.toLocale(str));
        ViewAdapter adapter = new ViewAdapter();
        adapter.type = viewData.getType();
        adapter.view = mLayout;
        adapter.object = ChatToSysPartView.getInstance();
        return adapter;
    }
	

	@Override
	public Object updateView(ViewData data) {
		ChatToSysPartViewData viewData = (ChatToSysPartViewData) data;
		LogUtil.logd(WinLayout.logTag+ "updateView " + viewData.textContent);
//		content.setPrintText(LanguageConvertor.toLocale(viewData.textContent));
		// FIXME: 2020/9/25 蓝鲸项目发版本，临时从svn稳定分支撤回打字效果
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		return super.updateView(data);
	}

	@Override
	public void init() {
		super.init();
        mFlags = UPDATEABLE;
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
        int unit = ViewParamsUtil.unit;
        textSize = ViewParamsUtil.h2;
        lineSpace = textSize * 3 / 2;
        chatVerPadding = textSize / 4;
        contentMaxWidth = WinLayout.isVertScreen?(int)(SizeConfig.screenWidth * 0.9):(int)((SizeConfig.screenWidth *0.75 - 4 * unit)* 0.9);
        horMargin = 3 * unit;    //对话框左右间距
        verMargin = 2 * unit;    //对话框上下间距
    }

    //全屏模式布局
    private void initHalf(){
        int unit = ViewParamsUtil.unit;
        textSize = ViewParamsUtil.h2;
        lineSpace = textSize * 3 / 2;
        chatVerPadding = textSize / 4;
        contentMaxWidth = WinLayout.isVertScreen?(int)(SizeConfig.screenWidth * 0.9):(int)((SizeConfig.screenWidth *0.75 - 4 * unit)* 0.9);
        horMargin = 3 * unit;    //对话框左右间距
        verMargin = 2 * unit;    //对话框上下间距

    }

    //全屏模式布局
    private void initNone(){
        int unit = ViewParamsUtil.unit;
        textSize = ViewParamsUtil.h3;
        lineSpace = textSize * 3 / 2;
        chatVerPadding = textSize / 4;
        contentMaxWidth = SizeConfig.screenWidth - 26 * unit;
        horMargin = 3 * unit;    //对话框左右间距
        verMargin = 2 * unit;    //对话框上下间距
    }

}
