package com.txznet.resholder.theme.ironman.view;

import android.annotation.SuppressLint;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.config.ViewConfiger;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.viewfactory.ViewBase;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatFromSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatFromSysView;
import com.txznet.comm.util.TextViewUtil;
import com.txznet.txz.util.LanguageConvertor;

@SuppressLint("NewApi")
public class ChatFromSysView extends IChatFromSysView{
	
	private static ChatFromSysView sInstance = new ChatFromSysView();
	
	private float textSize;
	private int textColor;
	
	private ChatFromSysView(){
	}
	
	public static ChatFromSysView getInstance(){
		return sInstance;
	}
	
	private LinearLayout mLayout;
	private TextView content;
	
	@Override
	public ViewAdapter getView(ViewData data) {
		ChatFromSysViewData viewData = (ChatFromSysViewData) data;
		mLayout = new LinearLayout(GlobalContext.get());
		content = new TextView(GlobalContext.get());
		content.setBackground(LayouUtil.getDrawable("chat_bg_from_sys"));
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		params.topMargin = (int) LayouUtil.getDimen("y20");
		params.bottomMargin = (int) LayouUtil.getDimen("y20");
		content.setLayoutParams(params);
		content.setMinHeight((int) LayouUtil.getDimen("y70"));
		content.setLineSpacing(0, 1.2f);
		TextViewUtil.setTextSize(content, textSize);
		TextViewUtil.setTextColor(content,textColor);
		mLayout.addView(content);
		content.setText(LanguageConvertor.toLocale(viewData.textContent));
		content.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);
		if (viewData.onClickListener!=null) {
			mLayout.setOnClickListener(viewData.onClickListener);
		}
		
		ViewAdapter adapter = new ViewAdapter();
		adapter.type = data.getType();
		adapter.view = mLayout;
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
		textSize = (Float) ViewConfiger.getInstance().getConfig(ViewConfiger.SIZE_CHAT_FROM_SYS);
		textColor = (Integer) ViewConfiger.getInstance().getConfig(ViewConfiger.COLOR_CHAT_FROM_SYS);
	}
	
}
