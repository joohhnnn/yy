package com.txznet.reserve.activity;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.rearview_mirror.QRCodeActivity;

/**
 * 扫码页面
 * theme：后视镜
 * Created by J on 2016/10/14.
 */

public class ReserveSingleTopActivity0 extends QRCodeActivity {
    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
