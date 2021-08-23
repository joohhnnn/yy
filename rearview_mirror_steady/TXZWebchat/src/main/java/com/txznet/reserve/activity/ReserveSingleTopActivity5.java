package com.txznet.reserve.activity;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.car.t700.Car_MainActivity_T700;

/**
 * 预留的Activity
 * Created by J on 2017/10/11.
 */

public class ReserveSingleTopActivity5 extends Car_MainActivity_T700 {
    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
