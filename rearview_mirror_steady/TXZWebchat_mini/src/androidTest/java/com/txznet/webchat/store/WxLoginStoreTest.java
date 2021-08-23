/*
package com.txznet.webchat.store;

import android.test.ApplicationTestCase;
import android.util.Log;

import com.squareup.otto.Subscribe;
import com.txznet.comm.base.BaseApplication;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.TXZBindStore;
import com.txznet.webchat.stores.WxContactStore;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxMessageStore;
import com.txznet.webchat.stores.WxQrCodeStore;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.fest.assertions.api.Assertions.assertThat;

public class WxLoginStoreTest extends ApplicationTestCase<BaseApplication> {
    private static final String TAG = WxLoginStoreTest.class.getSimpleName();

    public WxLoginStoreTest() {
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
        WxLoginStore.get().register(new Object() {
            @Subscribe
            public void onStoreChanged(Store.StoreChangeEvent event) {
                if (WxLoginStore.get().isLogin()) {
                    assertThat(WxContactStore.get().getSessionList()).isNotEmpty();
//                    testDone.set(true);
                }
            }
        });
        WxContactStore.get().register(new Object() {
            @Subscribe
            public void onStoreChanged(Store.StoreChangeEvent event) {
                if (event.getType().equals(WxContactStore.EVENT_TYPE_ALL)) {
                    Log.e("WxLoginStore", "##########" + WxContactStore.get().getSessionList());
                }
            }
        });
        while (!testDone.get()) ;
    }

    public void testLogout() {
        final AtomicBoolean testDone = new AtomicBoolean(false);
        WxLoginStore.get().register(new Object() {
            @Subscribe
            public void onStoreChanged(Store.StoreChangeEvent event) {
                if (WxLoginStore.get().isLogin()) {
                    assertThat(WxContactStore.get().getSessionList()).isNotEmpty();
                } else {
                    testDone.set(true);
                }
            }
        });
        LoginActionCreator.get().doLogout(true);
        while (!testDone.get()) ;
    }
}
*/
