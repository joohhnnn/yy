package com.txznet.reserve.activity;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.rearview_mirror.HelpActivity;

/**
 * 帮助页面
 * theme: 后视镜
 * Created by J on 2016/10/14.
 */

public class ReserveStandardActivity1 extends HelpActivity{
    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
