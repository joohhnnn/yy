/*
package com.txznet.webchat.store;

import android.test.ApplicationTestCase;

import com.squareup.otto.Subscribe;
import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxQrCodeStore;
import com.txznet.webchat.stores.WxContactStore;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.fest.assertions.api.Assertions.assertThat;

public class WxQrCodeStoreTest extends ApplicationTestCase<BaseApplication> {
    public WxQrCodeStoreTest() {
        super(BaseApplication.class);
    }

    @Override
    public void setUp() {
        GlobalContext.set(getContext());
        AppStatusStore.get();
        TXZBindStore.get();
        WxMessageStore.get();
        WxQrCodeStore.get();
        WxLoginStore.get();
        WxContactStore.get();
    }

    public void testGetQrCode() {
        final AtomicBoolean testDone = new AtomicBoolean(false);
        WxQrCodeStore.get().register(new Object() {
            @Subscribe
            public void onStoreChanged(Store.StoreChangeEvent event) {
                if (!WxQrCodeStore.get().isQrCodeInvalid()) {
                    assertThat(WxQrCodeStore.get().getQrCode()).isNotEmpty();
                }
                if (WxQrCodeStore.get().isScanned()) {
                    assertThat(WxQrCodeStore.get().getScannerPicStr()).isNotEmpty();
                }
                testDone.set(true);
            }
        });
        while (!testDone.get()) ;
    }
}
*/
