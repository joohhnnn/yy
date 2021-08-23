package com.txznet.comm.ui.theme.test.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.utils.ExtViewAdapter;
import com.txznet.comm.ui.theme.test.widget.EllipsizePrinterTextView;
import com.txznet.comm.ui.theme.test.widget.PrinterTextView;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.viewfactory.ViewFactory.ViewAdapter;
import com.txznet.comm.ui.viewfactory.data.ChatToSysPartViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IChatToSysPartView;
import com.txznet.resholder.R;
import com.txznet.txz.util.LanguageConvertor;

/**
 * 说话的文字
 * <p>
 * 2020-08-19 11:44
 *
 * @author xiaolin
 */
@SuppressLint("NewApi")
public class ChatToSysPartView extends IChatToSysPartView {

    private static ChatToSysPartView sInstance = new ChatToSysPartView();


    private EllipsizePrinterTextView tvContent;

    private ChatToSysPartView() {
    }

    public static ChatToSysPartView getInstance() {
        return sInstance;
    }

    @Override
    public ExtViewAdapter getView(ViewData data) {
        ChatToSysPartViewData viewData = (ChatToSysPartViewData) data;
        LogUtil.logd(WinLayout.logTag + "ChatToSysPartView.getView " + viewData.textContent);

        return createViewNone(viewData);
    }

    private ExtViewAdapter createViewNone(ChatToSysPartViewData data) {
        Context context = UIResLoader.getInstance().getModifyContext();
        View view = LayoutInflater.from(context).inflate(R.layout.chat_to_sys_part_view, (ViewGroup) null);

		tvContent = view.findViewById(R.id.tvContent);
		tvContent.setPrintText(LanguageConvertor.toLocale(data.textContent));

        ExtViewAdapter adapter = new ExtViewAdapter();
        adapter.type = data.getType();
        adapter.view = view;
        adapter.view.setTag(adapter);
        adapter.object = ChatToSysPartView.getInstance();
        return adapter;
    }


    @Override
    public Object updateView(ViewData data) {
        ChatToSysPartViewData viewData = (ChatToSysPartViewData) data;
        LogUtil.logd(WinLayout.logTag + "updateView " + viewData.textContent);
		tvContent.setPrintText(LanguageConvertor.toLocale(viewData.textContent));
        return super.updateView(data);
    }

    @Override
    public void init() {
        super.init();
        mFlags = UPDATEABLE;
    }

    //切换模式修改布局参数
    public void onUpdateParams(int styleIndex) {

    }


}
