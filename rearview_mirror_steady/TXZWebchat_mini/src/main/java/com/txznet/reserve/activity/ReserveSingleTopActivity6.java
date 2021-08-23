package com.txznet.reserve.activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.squareup.otto.Subscribe;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.ui.car.t700.Car_QRCodeActivity_T700;

/**
 * 预留的Activity
 * Created by J on 2017/10/11.
 */

public class ReserveSingleTopActivity6 extends Car_QRCodeActivity_T700 {
    @Subscribe
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);
    }
}
