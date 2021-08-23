package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatSysHighlightViewData;
import com.txznet.comm.ui.viewfactory.data.ChatSysInterruptTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatSysHighlightView;
import com.txznet.comm.ui.viewfactory.view.IChatSysInterruptView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.comm.R;

@SuppressLint("NewApi")
public class ChatSysInterruptView extends IChatSysInterruptView{
	
	private static ChatSysInterruptView sInstance = new ChatSysInterruptView();
	
	private ChatSysInterruptView() {
	}
	
	public static ChatSysInterruptView getInstance(){
		return sInstance;
	}
	
	private float textSize;
	private int textColor;
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatSysInterruptTipsViewData viewData = (ChatSysInterruptTipsViewData) data;
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_from_sys_text_interrupt");
		TextView content = (TextView) LayouUtil.findViewByName("tv_chat_msg_interrupt", mLayout);
		TextView title = (TextView) LayouUtil.findViewByName("tv_chat_interrupt_tips", mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LinearLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
		content.setLayoutParams(params);
		content.setMinHeight((int) LayouUtil.getDimen("y70"));
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		content.setText(viewData.textContent);
		int padding = content.getPaddingLeft();
		title.setPadding(padding, 0, 0, 0);
		TextViewUtil.setTextSize(title, textSize - 2);
		title.setText(Html.fromHtml(viewData.titleContent));
		if (viewData.onClickListener!=null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatSysInterruptView.getInstance();
		return adapter;
	}
	
	@Override
	public void init() {
		textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_FROM_SYS);
		textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_FROM_SYS);
	}

}
