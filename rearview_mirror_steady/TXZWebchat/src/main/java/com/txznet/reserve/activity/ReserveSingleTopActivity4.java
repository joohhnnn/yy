package com.txznet.reserve.activity;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.car.Car_MainActivity;

/**
 * Created by J on 16/10/15.
 */

public class ReserveSingleTopActivity4 extends Car_MainActivity {
    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
