package com.txznet.reserve.activity;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.rearview_mirror.MainActivity;

/**
 * 主界面
 * theme：后视镜
 * Created by J on 2016/10/14.
 */

public class ReserveSingleTopActivity1 extends MainActivity{
    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
