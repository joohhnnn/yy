package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.text.Html;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.txznet.comm.ui.config.ViewConfiger;
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
	private int textColor;
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatSysHighlightViewData viewData = (ChatSysHighlightViewData) data;
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LinearLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
		params.topMargin = (int) LayouUtil.getDimen("y20");
		params.bottomMargin = (int) LayouUtil.getDimen("y20");
		content.setLayoutParams(params);
		content.setMinHeight((int) LayouUtil.getDimen("y70"));
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(Html.fromHtml(viewData.textContent));
		if (viewData.onClickListener!=null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatSysHighlightView.getInstance();
		return adapter;
	}
	
	@Override
	public void init() {
		textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_FROM_SYS);
		textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_FROM_SYS);
	}

}
