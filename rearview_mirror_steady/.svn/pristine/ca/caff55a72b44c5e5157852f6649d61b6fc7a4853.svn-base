package com.txznet.webchat.ui.base;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.reserve.activity.ReserveSingleTopActivity0;
import com.txznet.reserve.activity.ReserveSingleTopActivity1;
import com.txznet.webchat.stores.WxLoginStore;

/**
 * Created by J on 2016/10/8.
 */

public class LauncherActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (WxLoginStore.get().isLogin()) {
            intentMainActivity();
        } else {
            intentQRActivity();
        }


    }

    private void intentQRActivity() {
        // 根据当前主题跳转对应的Activity
        Intent intent = new Intent();
        intent.setClass(this, ReserveSingleTopActivity0.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        GlobalContext.get().startActivity(intent);
        this.finish();
    }

    private void intentMainActivity() {
        Intent intent = new Intent();
        intent.setClass(this, ReserveSingleTopActivity1.class);
        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        GlobalContext.get().startActivity(intent);
        this.finish();
    }
}