package com.txznet.comm.ui.viewfactory.view.defaults;

import android.annotation.SuppressLint;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatToSysPartViewData;
import com.txznet.comm.ui.viewfactory.data.ChatToSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatToSysPartView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class DefaultChatToSysPartView extends IChatToSysPartView{

	private static DefaultChatToSysPartView sInstance = new DefaultChatToSysPartView();

	private float textSize;
	private int textColor;

	private DefaultChatToSysPartView(){
	}

	public static DefaultChatToSysPartView getInstance(){
		return sInstance;
	}


	int i = 0;

	@Override
	public ViewAdapter getView(ViewData data) {
		ChatToSysPartViewData viewData = (ChatToSysPartViewData) data;
		LinearLayout mLayout = (LinearLayout) LayouUtil.getView("chat_to_sys_text");
		TextView content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_to_sys"));
		LayoutParams params = (LayoutParams) content.getLayoutParams();
		params.topMargin = (int) LayouUtil.getDimen("y20");
		params.bottomMargin = (int) LayouUtil.getDimen("y20");
		content.setLayoutParams(params);
		content.setMinHeight((int) LayouUtil.getDimen("y70"));
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = DefaultChatToSysPartView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
		super.init();
		mFlags = UPDATEABLE;
		textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_TO_SYS_PART);
		textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_TO_SYS_PART);
	}
	
}
