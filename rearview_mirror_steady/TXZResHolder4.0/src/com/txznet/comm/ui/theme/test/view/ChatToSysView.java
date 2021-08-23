package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.widget.PrinterTextView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatToSysViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatToSysView;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;

/**
 * 说完话之后的文字
 * <p>
 * 2020-08-19 11:44
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatToSysView extends IChatToSysView{
	
	private static ChatToSysView sInstance = new ChatToSysView();

	private ChatToSysView(){
	}
	
	public static ChatToSysView getInstance(){
		return sInstance;
	}

	@Override
	public ExtViewAdapter getView(ViewData data) {
		ChatToSysViewData viewData = (ChatToSysViewData) data;
		WinLayout.getInstance().chatToSysText = viewData.textContent;
		LogUtil.logd(WinLayout.logTag+ "ChatToSysView.getView(ViewData):" + viewData.textContent);

		return createViewNone(viewData);
	}

	private ExtViewAdapter createViewNone(ChatToSysViewData viewData){
		Context context = UIResLoader.getInstance().getModifyContext();
		View view = LayoutInflater.from(context).inflate(R.layout.chat_to_sys_text, (ViewGroup)null);

		TextView tvContent = view.findViewById(R.id.tvContent);
		tvContent.setText(LanguageConvertor.toLocale(viewData.textContent));


		ExtViewAdapter adapter = new ExtViewAdapter();
		adapter.type = viewData.getType();
		adapter.view = view;
		adapter.view.setTag(adapter);
		adapter.object = ChatFromSysView.getInstance();
		return adapter;
	}

	@Override
	public void init() {
		super.init();
	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){

	}

}
