package com.txznet.comm.ui.theme.test.config;

import com.txznet.comm.ui.advertising.BaseAdvertisingControl;

public class AdvertisingControl extends BaseAdvertisingControl {
    private static AdvertisingControl sInstance = new AdvertisingControl();

    public static AdvertisingControl getInstance() {
        return sInstance;
    }

    @Override
    public boolean supportAdvertising() {
        return StyleConfig.getInstance().getSelectStyleIndex() == 1 ? true : false;
    }
}
