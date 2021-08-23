package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatSysHighlightViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatSysHighlightView;
import com.txznet.resholder.R;

/**
 * 2020-09-10 18：16
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatSysHighlightView extends IChatSysHighlightView{
	
	private static ChatSysHighlightView sInstance = new ChatSysHighlightView();
	
	private ChatSysHighlightView() {
	}
	
	public static ChatSysHighlightView getInstance(){
		return sInstance;
	}

	@Override
	public ViewAdapter getView(ViewData data) {
		ChatSysHighlightViewData viewData = (ChatSysHighlightViewData) data;

		View view  = createView(viewData);

		ExtViewAdapter adapter = new ExtViewAdapter();
		adapter.type = data.getType();
		adapter.view = view;
		adapter.view.setTag(adapter);
		adapter.object = ChatSysHighlightView.getInstance();
		return adapter;
	}


	public View createView(ChatSysHighlightViewData data){
		Context context = UIResLoader.getInstance().getModifyContext();
		View view = LayoutInflater.from(context).inflate(R.layout.chat_from_hightlight_view, (ViewGroup)null);
		TextView tvContent = view.findViewById(R.id.tvContent);

		tvContent.setText(Html.fromHtml(data.textContent));
		if (data.onClickListener!=null) {
			view.setOnClickListener(data.onClickListener);
		}

		return view;
	}

	
	@Override
	public void init() {

	}

	//切换模式修改布局参数
	public void onUpdateParams(int styleIndex){

	}

}
