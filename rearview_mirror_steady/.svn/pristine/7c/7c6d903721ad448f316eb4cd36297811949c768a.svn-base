package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.data.ChatSysInterruptTipsViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatSysInterruptView;
import com.txznet.resholder.R;

/**
 * 2020-08-19 11:56
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatSysInterruptView extends IChatSysInterruptView {

    private static ChatSysInterruptView sInstance = new ChatSysInterruptView();

    private ChatSysInterruptView() {
    }

    public static ChatSysInterruptView getInstance() {
        return sInstance;
    }


    @Override
    public ExtViewAdapter getView(ViewData data) {
        ChatSysInterruptTipsViewData viewData = (ChatSysInterruptTipsViewData) data;
        LogUtil.logd(WinLayout.logTag + "ChatSysInterruptTipsViewData: get");

        View view = createViewNone(viewData);

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = ChatSysInterruptView.getInstance();
        return adapter;
    }

    private View createViewNone(ChatSysInterruptTipsViewData viewData) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.chat_sys_interrup_view, (ViewGroup) null);
        TextView tvContent = view.findViewById(R.id.tvContent);

        tvContent.setText(viewData.textContent);
        if (viewData.onClickListener != null) {
            tvContent.setOnClickListener(viewData.onClickListener);
        }

        return view;
    }

    @Override
    public void init() {
        super.init();
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }

}
