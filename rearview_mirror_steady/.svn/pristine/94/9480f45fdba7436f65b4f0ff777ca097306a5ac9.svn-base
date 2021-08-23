//package com.txznet.webchat.store;
//
//import android.os.Build;
//
//import com.squareup.otto.Subscribe;
//import com.txznet.comm.base.BaseApplication;
//import com.txznet.webchat.BuildConfig;
//import com.txznet.webchat.actions.LoginActionCreator;
//import com.txznet.webchat.stores.Store;
//import com.txznet.webchat.stores.WxQrCodeStore;
//
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.robolectric.RobolectricGradleTestRunner;
//import org.robolectric.annotation.Config;
//
//import java.util.concurrent.atomic.AtomicBoolean;
//
//import static org.fest.assertions.api.Assertions.assertThat;
//
//@RunWith(RobolectricGradleTestRunner.class)
//@Config(sdk = Build.VERSION_CODES.JELLY_BEAN, application = BaseApplication.class, constants = BuildConfig.class)
//public class WxQrCodeStoreTest {
//    @Test
//    @Config
//    public void testGetQrCode() {
//        final AtomicBoolean testDone = new AtomicBoolean(false);
//        WxQrCodeStore.get().register(new Object() {
//            @Subscribe
//            public void onStoreChanged(Store.StoreChangeEvent event) {
//                System.out.println("onStoreChanged, event=" + event.getType());
//                if (!WxQrCodeStore.get().isQrCodeInvalid()) {
//                    assertThat(WxQrCodeStore.get().getQrCode()).isNotEmpty();
//                    assertThat(WxQrCodeStore.get().getScannerPicUrl()).isNotEmpty();
//                }
//                if (!WxQrCodeStore.get().isScanned()) {
//                    assertThat(WxQrCodeStore.get().getScannerPicUrl()).isNotEmpty();
//                }
//                testDone.set(true);
//            }
//        });
//        LoginActionCreator.get().getQrCode();
//        System.out.println("end");
//        while (!testDone.get()) ;
//    }
//}
