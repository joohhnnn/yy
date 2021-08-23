package com.txznet.resholder.wave.view;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatToSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatToSysView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

import android.annotation.SuppressLint;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

@SuppressLint("NewApi")
public class ChatToSysView extends IChatToSysView{
	
	private static ChatToSysView sInstance = new ChatToSysView();
	
	private float textSize;
	private int textColor;
	
	private ChatToSysView(){
	}
	
	public static ChatToSysView getInstance(){
		return sInstance;
	}
	
	private LinearLayout mLayout;
	private TextView content;
	
	int i = 0;
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatToSysViewData viewData = (ChatToSysViewData) data;
		mLayout = (LinearLayout) LayouUtil.getView("chat_to_sys_text");
		content = (TextView) LayouUtil.findViewByName("txtChat_Msg_Text", mLayout);
		content.setBackground(LayouUtil.getDrawable("chat_bg_to_sys"));
		LinearLayout.LayoutParams params = (LayoutParams) content.getLayoutParams();
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
		adapter.object = ChatToSysView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
		textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_TO_SYS);
		textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_TO_SYS);
	}
	
}
