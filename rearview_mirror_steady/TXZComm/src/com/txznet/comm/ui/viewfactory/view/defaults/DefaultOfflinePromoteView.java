package com.txznet.comm.ui.viewfactory.view.defaults;

import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.layout.WinLayoutManager;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ChatSysHighlightViewData;
import com.txznet.comm.ui.viewfactory.data.LogoQrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.OfflinePromoteViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IOfflinePromoteView;

public class DefaultOfflinePromoteView extends IOfflinePromoteView {
    private static DefaultOfflinePromoteView sInstance = new DefaultOfflinePromoteView();

    public static DefaultOfflinePromoteView getInstance() {
        return sInstance;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        OfflinePromoteViewData offlineData = (OfflinePromoteViewData) data;

        LinearLayout layout = new LinearLayout(GlobalContext.get());
        layout.setOrientation(LinearLayout.VERTICAL);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        ChatSysHighlightViewData chatSysHighlightViewData = new ChatSysHighlightViewData();
        chatSysHighlightViewData.textContent = offlineData.text;
        ChatSysHighlightView.getInstance().init();
        ViewFactory.ViewAdapter adapter = ChatSysHighlightView.getInstance().getView(chatSysHighlightViewData);
        //高亮Text
        layout.addView(adapter.view);

        LogoQrCodeViewData logoQrCodeViewData = new LogoQrCodeViewData();
        logoQrCodeViewData.qrCode = ((OfflinePromoteViewData)data).qrCode;
        adapter =DefaultLogoQrCodeView.getInstance().getView(logoQrCodeViewData);
        //带LOGO的QRCODE
        layout.addView(adapter.view);

        adapter = new ViewFactory.ViewAdapter();
        adapter.type = offlineData.getType();
        adapter.view = layout;
        adapter.object = DefaultOfflinePromoteView.getInstance();
        return adapter;
    }

    @Override
    public void init() {
        super.init();
    }
}
