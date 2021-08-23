package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.theme.test.config.SizeConfig;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.theme.test.config.ViewParamsUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.theme.test.winlayout.inner.WinLayoutHalf;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatFromSysView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class ChatFromSysView extends IChatFromSysView{
	
	private static ChatFromSysView sInstance = new ChatFromSysView();
	
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


	private TextView content;
	
	private ChatFromSysView(){
	}
	
	public static ChatFromSysView getInstance(){
		return sInstance;
	}
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatFromSysViewData viewData = (ChatFromSysViewData) data;
		LogUtil.logd(WinLayout.logTag+ "ChatFromSysView.getView(ViewData)"
				+ viewData.textContent);
		//Log.d("jack", "getMaxDimen: "+LayouUtil.getDimen("x800")+"--"+LayouUtil.getDimen("y480"));

		ViewAdapter adapter = null;

		switch (StyleConfig.getInstance().getSelectStyleIndex()) {
			case StyleConfig.STYLE_ROBOT_FULL_SCREES:
				WinLayout.isHideView = false;
				adapter = createViewFull(viewData);
				break;
			case StyleConfig.STYLE_ROBOT_HALF_SCREES:
			    if (WinLayout.isHideView){
                    WinLayout.isHideView = false;
                }else {
                    adapter = createViewHalf(viewData);
                }
				break;
			case StyleConfig.STYLE_ROBOT_NONE_SCREES:
			default:
                if (WinLayout.isHideView){
                    WinLayout.isHideView = false;
                }else {
                    adapter = createViewNone(viewData);
                }
				break;
		}

		return adapter;
	}

	public ViewAdapter getView(ViewData data, SpannableString spannableString) {
		ChatFromSysViewData viewData = (ChatFromSysViewData) data;
		LogUtil.logd(WinLayout.logTag+ "ChatFromSysView.getView(ViewData)"
				+ viewData.textContent+spannableString);

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
				adapter = createViewNone(viewData,spannableString);
				break;
		}

		return adapter;
	}

	public ViewAdapter getViewLine(ViewData data) {
        ChatFromSysViewData viewData = (ChatFromSysViewData) data;

        LinearLayout mLayout = (LinearLayout) LayouUtil
                .getView("chat_from_sys_text_half");
        content = (TextView) LayouUtil.findViewByName(
                "txtChat_Msg_Text", mLayout);
        content.setTypeface(Typeface.SERIF);
        TextViewUtil.setTextSize(content, textSizeHalf);
        TextViewUtil.setTextColor(content, textColor);
        content.setText(LanguageConvertor.toLocale(viewData.textContent));
        content.setSingleLine(true);
        content.setEllipsize(TextUtils.TruncateAt.END);
        /*if (viewData.onClickListener != null) {
            mLayout.setOnClickListener(viewData.onClickListener);
        }*/

        ViewAdapter adapter = new ViewAdapter();
        adapter.type = viewData.getType();
        adapter.view = mLayout;
        adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	private ViewAdapter createViewFull(ChatFromSysViewData viewData){

		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text");
		content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
        content.setTypeface(Typeface.SERIF);
		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
        LinearLayout.LayoutParams layoutParams =(LayoutParams) content.getLayoutParams();
        layoutParams.setMargins(0,verMargin,0,verMargin);
        content.setMaxWidth(contentMaxWidth);
        content.setLineSpacing(lineSpace,0);
        content.setPadding(horMargin,verMargin+chatVerPadding,horMargin,verMargin+chatVerPadding);

		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
        //setTextAutoWrap(content);
        //手动换行
        //content.removeCallbacks(runnable);
        //content.post(runnable);
		if (viewData.onClickListener != null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	private ViewAdapter createViewHalf(ChatFromSysViewData viewData){

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getView("chat_from_sys_text_half");
		content = (TextView) LayouUtil.findViewByName(
				"txtChat_Msg_Text", mLayout);
        content.setTypeface(Typeface.SERIF);
		//content.setMaxWidth(contentMaxWidth);
		// content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));

		// LinearLayout.LayoutParams params = (LayoutParams)
		// content.getLayoutParams();
		// params.topMargin = (int) LayouUtil.getDimen("y20");
		// params.bottomMargin = (int) LayouUtil.getDimen("y20");
		// content.setLayoutParams(params);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		content.setEllipsize(TextUtils.TruncateAt.END);
		if (viewData.onClickListener != null) {
			 mLayout.setOnClickListener(viewData.onClickListener);
		}
        TextViewUtil.setTextSize(content, textChatSizeHalf);
        TextViewUtil.setTextColor(content, textColor);

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		if (viewData.textContent.length() > 20 && !isNotInterrupt(viewData.textContent) && !viewData.textContent.startsWith("你可以说")) {
			adapter.view.setTag(ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT);
			content.setSingleLine(false);
			LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) content.getLayoutParams();
			layoutParams.gravity = Gravity.CENTER;
			content.setLayoutParams(layoutParams);
            TextViewUtil.setTextSize(content, textSizeHalf);
            TextViewUtil.setTextColor(content, textColor);
            content.setLineSpacing(lineSpaceHalf,0);
            content.setPadding(horMarginHalf,verMarginHalf+chatVerPaddingHalf,horMarginHalf,verMarginHalf+chatVerPaddingHalf);
		} else if(WinLayout.isVertScreen){
            content.setMaxLines(2);
            //手动换行
            //content.removeCallbacks(runnable);
            //content.post(runnable);
            //setTextAutoWrap(content);
		}else {
            content.setSingleLine(true);
        }
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	private ViewAdapter createViewNone(ChatFromSysViewData viewData){

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getView("chat_from_sys_text_none");
		content = (TextView) LayouUtil.findViewByName(
				"txtChat_Msg_Text", mLayout);
        content.setTypeface(Typeface.SERIF);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		if (viewData.onClickListener != null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		if (viewData.textContent != null
				&& viewData.textContent.length() > 20
				&& !isNotInterrupt(viewData.textContent)
				&& !viewData.textContent.startsWith("你可以说")) {
			adapter.view.setTag(ViewData.TYPE_CHAT_FROM_SYS_INTERRUPT);
			content.setSingleLine(false);
			/*mLayout.setPadding((int) LayouUtil.getDimen("x20"),
					(int) LayouUtil.getDimen("x20"),
					(int) LayouUtil.getDimen("x20"),
					(int) LayouUtil.getDimen("x20"));*/
            /*TextViewUtil.setTextSize(content, textSizeNone);
            TextViewUtil.setTextColor(content, textColor);*/
            content.setLineSpacing(lineSpaceNone,0);
            content.setPadding(horMarginNone,verMarginNone+chatVerPaddingNone,horMarginNone,verMarginNone+chatVerPaddingNone);
		} else {
            content.setMaxWidth(contentMaxWidth);
			content.setSingleLine(true);
		}
		TextViewUtil.setTextSize(content, textSizeNone);
		TextViewUtil.setTextColor(content, textColor);
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	private ViewAdapter createViewNone(ChatFromSysViewData viewData,SpannableString spannableString){

		LinearLayout mLayout = (LinearLayout) LayouUtil
				.getView("chat_from_sys_text_none");
		content = (TextView) LayouUtil.findViewByName(
				"txtChat_Msg_Text", mLayout);
		content.setMaxWidth(contentMaxWidth);
		TextViewUtil.setTextSize(content, textSizeNone);
		TextViewUtil.setTextColor(content, textColor);
		content.setText(spannableString);
		if (viewData.onClickListener != null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}

		ViewAdapter adapter = new ViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = mLayout;
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
		super.init();
		int unit = (int) LayouUtil.getDimen("unit");
        textColor = Color.WHITE;
	}

	/*//在聊天框显示内容
	public void setText(String text){
		String hint = "你可以说\"" + text + "\"";
		content.setText(LanguageConvertor.toLocale(hint));
	}*/


	//切换模式是更新布局参数
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

	private boolean isNotInterrupt(String text){
		String[] strArr = {"没有找到","信息不完整","点击本消息手动修改","暂时不支持","我还没有名字","有新名字了","我先去学习一下","我的名字叫"
		,"正在为您搜索","请检查网络连接是否正常","的价格是","空气质量指数","将为您"};
		for(String str:strArr){
			if (text.contains(str)){
				return true;
			}
		}

		LogUtil.logd(WinLayout.logTag+ "isNotInterrupt: fasle--"+text);
		return false;
	}

	/*public void removeRunnable(){
        // 移除runnable，防止短时间内多次显示内容（反馈语和引导语连续展示）造成死锁
        if (content != null){
            content.removeCallbacks(runnable);
        }
    }*/

    /**
     * 容易造成死锁，先去掉
     */
	//文本中中文、中文字符、数字、英文混合存在时，android默认换行规则不太美观。因此计算字符宽度，手动换行。
	/*Runnable runnable = new Runnable() {
        @Override
        public void run() {
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
    };*/

    /*private void setTextAutoWrap(@NonNull final TextView view) {
        view.post(runnable);
    }*/

}
