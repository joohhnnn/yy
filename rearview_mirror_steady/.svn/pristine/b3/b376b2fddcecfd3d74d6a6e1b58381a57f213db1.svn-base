package com.txznet.comm.ui.theme.test.view;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ChatToSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatToSysView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextUtils;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

@SuppressLint("NewApi")
public class ChatToSysView extends IChatToSysView{
	
	private static ChatToSysView sInstance = new ChatToSysView();

	private int textSize;    //字体大小
	private int textColor;    //字体颜色
	private int lineSpace;    //行高
	private int chatVerPadding;    //字体上下边距
	private int contentMaxWidth;    //对话框最大宽度
	private int horMargin;    //对话框左右间距
	private int verMargin;    //对话框上下间距
	
	private TextView content;
	
	private ChatToSysView(){
	}
	
	public static ChatToSysView getInstance(){
		return sInstance;
	}
	
	
	int i = 0;
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatToSysViewData viewData = (ChatToSysViewData) data;
		WinLayout.getInstance().chatToSysText = viewData.textContent;

		LogUtil.logd(WinLayout.logTag+ "ChatToSysView.getView(ViewData):"
				+ viewData.textContent);
		
		ViewAdapter adapter = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				adapter = createViewFull(viewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                adapter = createViewHalf(viewData);
                break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
				adapter = createViewNone(viewData);
				break;
		}

		return adapter;
	}
	

	private ViewAdapter createViewFull(ChatToSysViewData viewData){

		LinearLayout mLayout = (LinearLayout) LayouUtil.getModifyView("chat_to_sys_text");
		content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_to_sys"));
        LinearLayout.LayoutParams layoutParams =(LayoutParams) content.getLayoutParams();
        layoutParams.setMargins(0,verMargin,0,verMargin);
		content.setMaxWidth(contentMaxWidth);
        content.setLineSpacing(lineSpace,0);
        content.setPadding(horMargin,verMargin+chatVerPadding,horMargin,verMargin+chatVerPadding);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
        //content.removeCallbacks(runnable);
        //content.post(runnable);

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	private ViewAdapter createViewHalf(ChatToSysViewData viewData){

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getModifyView("chat_to_sys_text_none");
		content = (TextView) LayouUtil.findViewByName(
				"txtChat_Msg_Text", mLayout);
//		content.setMaxEms(12);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content, textColor);
		//content.setMaxWidth((int) (SizeConfig.screenWidth * 0.8));
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		if (WinLayout.isVertScreen){
			content.setMaxLines(2);
            //content.removeCallbacks(runnable);
			//content.post(runnable);
		}else {
			content.setSingleLine(true);
            content.setMaxWidth(contentMaxWidth);
		}
		content.setEllipsize(TextUtils.TruncateAt.START);
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	private ViewAdapter createViewNone(ChatToSysViewData viewData){

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getModifyView("chat_to_sys_text_none");
		content = (TextView) LayouUtil.findViewByName(
				"txtChat_Msg_Text", mLayout);
//		content.setMaxEms(12);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content, textColor);
		content.setMaxWidth(contentMaxWidth);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		content.setSingleLine(true);
		content.setEllipsize(TextUtils.TruncateAt.START);
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
        textColor = Color.WHITE;
		super.init();
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
//            contentMaxWidth = (int)(SizeConfig.screenWidth * 0.9);
//            horMargin = 3 * unit;    //对话框左右间距
//            verMargin = 2 * unit;    //对话框上下间距
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            textSize = (int) LayouUtil.getDimen("h2");
//            lineSpace = textSize * 3 / 2;
//            chatVerPadding = textSize / 4;
//            contentMaxWidth = (int)((SizeConfig.screenWidth *0.75 - 4 * unit)* 0.9);
//            horMargin = 3 * unit;    //对话框左右间距
//            verMargin = 2 * unit;    //对话框上下间距
//        }
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
//		if (WinLayout.isVertScreen){
//        int unit = (int) LayouUtil.getDimen("vertical_unit");
//        textSize = (int) LayouUtil.getDimen("vertical_h2");
//        lineSpace = textSize * 3 / 2;
//        chatVerPadding = textSize / 4;
//        contentMaxWidth = (int)(SizeConfig.screenWidth * 0.9);
//        horMargin = 3 * unit;    //对话框左右间距
//        verMargin = 2 * unit;    //对话框上下间距
//    }else {
//        int unit = (int) LayouUtil.getDimen("unit");
//        textSize = (int) LayouUtil.getDimen("h2");
//        lineSpace = textSize * 3 / 2;
//        chatVerPadding = textSize / 4;
//        contentMaxWidth = (int)((SizeConfig.screenWidth *0.75 - 4 * unit)* 0.9);
//        horMargin = 3 * unit;    //对话框左右间距
//        verMargin = 2 * unit;    //对话框上下间距
//    }
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
//        if (WinLayout.isVertScreen){
//            textSize = (int) LayouUtil.getDimen("x30");
//            lineSpace = (int) LayouUtil.getDimen("x52");
//            chatVerPadding = (lineSpace - textSize)/ 2;
//            contentMaxWidth = SizeConfig.screenWidth - (int) LayouUtil.getDimen("x271");
//            horMargin = (int) LayouUtil.getDimen("x31");   //对话框左右间距
//            verMargin = (int) LayouUtil.getDimen("x20");   //对话框上下间距
//        }else {
//            int unit = (int) LayouUtil.getDimen("unit");
//            textSize = (int) LayouUtil.getDimen("h3");
//            lineSpace = textSize * 3 / 2;
//            chatVerPadding = textSize / 4;
//            contentMaxWidth = SizeConfig.screenWidth - 26 * unit;
//            horMargin = 3 * unit;    //对话框左右间距
//            verMargin = 2 * unit;    //对话框上下间距
//        }
		int unit = ViewParamsUtil.unit;
		textSize = ViewParamsUtil.h3;
		lineSpace = textSize * 3 / 2;
		chatVerPadding = textSize / 4;
		contentMaxWidth = SizeConfig.screenWidth - 26 * unit;
		horMargin = 3 * unit;    //对话框左右间距
		verMargin = 2 * unit;    //对话框上下间距
    }

    /*public void removeRunnable(){
        // 移除runnable，防止短时间内多次显示内容（反馈语和引导语连续展示）造成死锁
        if (content != null){
            content.removeCallbacks(runnable);
        }
    }*/

    //文本中中文、中文字符、数字、英文混合存在时，android默认换行规则不太美观。因此计算字符宽度，手动换行。
   /* Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (content != null){
                final String rawText = content.getText().toString(); //原始文本
                final Paint paint = content.getPaint(); //paint，包含字体等信息
                final float textWidth = content.getWidth() - content.getPaddingLeft() - content.getPaddingRight(); //控件可用宽度
                //将原始文本按行拆分
                String[] rawTextLines = rawText.replaceAll("\r", "").split("\n");
                StringBuilder newText = new StringBuilder();
                for (String rawTextLine : rawTextLines) {
                    if (paint.measureText(rawTextLine) <= textWidth) {
                        //如果整行宽度在控件可用宽度之内，就不处理了
                        newText.append(rawTextLine);
                    } else {
                        //如果整行宽度超过控件可用宽度，则按字符测量，在超过可用宽度的前一个字符处手动换行
                        float lineWidth = 0;
                        for (int cnt = 0; cnt != rawTextLine.length(); ++cnt) {
                            char ch = rawTextLine.charAt(cnt);
                            lineWidth += paint.measureText(String.valueOf(ch));
                            if (lineWidth <= textWidth) {
                                newText.append(ch);
                            } else {
                                newText.append("\n");
                                lineWidth = 0;
                                --cnt;
                            }
                        }
                    }
                    newText.append("\n");
                }
                //把结尾多余的\n去掉
                if (!rawText.endsWith("\n")) {
                    newText.deleteCharAt(newText.length() - 1);
                }
                content.setText(newText);
            }
            }
    };*/

}
