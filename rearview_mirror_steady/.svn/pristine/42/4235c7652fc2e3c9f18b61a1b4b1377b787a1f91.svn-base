package com.txznet.comm.ui.theme.test.view;

import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.test.config.StyleConfig;
import com.txznet.comm.ui.viewfactory.ViewFactory;
import com.txznet.comm.ui.viewfactory.data.ChatSysHighlightViewData;
import com.txznet.comm.ui.viewfactory.data.LogoQrCodeViewData;
import com.txznet.comm.ui.viewfactory.data.OfflinePromoteViewData;
import com.txznet.comm.ui.viewfactory.data.ViewData;
import com.txznet.comm.ui.viewfactory.view.IOfflinePromoteView;

public class OfflinePromoteView extends IOfflinePromoteView {
    private static OfflinePromoteView sInstance = new OfflinePromoteView();

    public static OfflinePromoteView getInstance() {
        return sInstance;
    }

    @Override
    public ViewFactory.ViewAdapter getView(ViewData data) {
        OfflinePromoteViewData offlineData = (OfflinePromoteViewData) data;
        ViewFactory.ViewAdapter adapter = null;
        switch (StyleConfig.getInstance().getSelectStyleIndex()){
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                adapter = createViewFull(offlineData);
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                adapter = createView(offlineData);
            default:

                break;
        }
        return adapter;
    }

    public ViewFactory.ViewAdapter createViewFull(OfflinePromoteViewData offlineData){
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        layout.setOrientation(LinearLayout.VERTICAL);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        ChatSysHighlightViewData chatSysHighlightViewData = new ChatSysHighlightViewData();
        chatSysHighlightViewData.textContent = offlineData.text;
        ViewFactory.ViewAdapter adapter = ChatSysHighlightView.getInstance().getView(chatSysHighlightViewData);
        //高亮Text
        layout.addView(adapter.view);

        LogoQrCodeViewData logoQrCodeViewData = new LogoQrCodeViewData();
        logoQrCodeViewData.qrCode = offlineData.qrCode;
        adapter = LogoQrCodeView.getInstance().getView(logoQrCodeViewData);
        //带LOGO的QRCODE
        layout.addView(adapter.view);
        layout.setTag(ViewData.TYPE_CHAT_OFFLINE_PROMOTE);

        adapter = new ViewFactory.ViewAdapter();
        adapter.type = offlineData.getType();
        adapter.view = layout;
        adapter.object = OfflinePromoteView.getInstance();
        return adapter;
    }

    public ViewFactory.ViewAdapter createView(OfflinePromoteViewData offlineData){
        LinearLayout layout = new LinearLayout(GlobalContext.get());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layout.setLayoutParams(params);

        ChatSysHighlightViewData chatSysHighlightViewData = new ChatSysHighlightViewData();
        chatSysHighlightViewData.textContent = offlineData.text;
        ViewFactory.ViewAdapter adapter = ChatSysHighlightView.getInstance().getView(chatSysHighlightViewData);
        if(adapter.view instanceof LinearLayout){
            ((LinearLayout)adapter.view).getChildAt(0).setBackground(null);
            ((LinearLayout)adapter.view).getChildAt(0).setPadding(30,30,30,7);
        }
        //高亮Text
        layout.addView(adapter.view);

        LogoQrCodeViewData logoQrCodeViewData = new LogoQrCodeViewData();
        logoQrCodeViewData.qrCode = offlineData.qrCode;
        adapter = LogoQrCodeView.getInstance().getView(logoQrCodeViewData);
        //带LOGO的QRCODE
        layout.addView(adapter.view);
        layout.setTag(ViewData.TYPE_CHAT_OFFLINE_PROMOTE);

        adapter = new ViewFactory.ViewAdapter();
        adapter.type = offlineData.getType();
        adapter.view = layout;
        adapter.object = OfflinePromoteView.getInstance();
        return adapter;
    }

    @Override
    public void init() {
        super.init();
    }
}
