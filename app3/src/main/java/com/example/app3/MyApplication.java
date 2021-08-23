package com.example.app3;

import android.app.Application;
import android.util.Log;

import com.example.app3.service.MessageProcess;
import com.txznet.adapter.aidl.TXZAIDLManager;

public class MyApplication extends Application {
    private final String TAG = this.getClass().getSimpleName();
    private static MyApplication instance;

    public static MyApplication getInstance(){
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;

        init();
    }

    private void init() {
        TXZAIDLManager.getInstance().initService(this, new TXZAIDLManager.TXZCommandListener() {
            @Override
            public void onBindSuccess() {
                Log.d(TAG, "======BindSuccess=====");
            }

            @Override
            public byte[] onCommandReceive(int keyType, String s, byte[] bytes) {
                return MessageProcess.getInstance().processMessage(keyType, s, bytes);
            }
        });
    }
}
