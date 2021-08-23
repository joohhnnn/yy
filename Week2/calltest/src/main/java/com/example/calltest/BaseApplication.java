package com.example.calltest;

import android.app.Application;

import com.txznet.sdk.TXZConfigManager;

public class BaseApplication extends Application {
    private static BaseApplication baseApplication;
    public static BaseApplication getInstance(){
        return baseApplication;
    }
    @Override
    public void onCreate() {
        super.onCreate();
        baseApplication=this;
        initTXZSDK();

    }
    //初始化core 并建立连接
  private void initTXZSDK(){
        String appId=BaseApplication.getInstance().getResources().getString(R.string.txz_sdk_init_app_id);
        String appToken=BaseApplication.getInstance().getResources().getString(R.string.txz_sdk_init_app_token);
      TXZConfigManager.InitParam initParam=new TXZConfigManager.InitParam(appId,appToken);

        TXZConfigManager.getInstance().initialize(BaseApplication.getInstance(), initParam, new TXZConfigManager.InitListener() {
            @Override
            public void onSuccess() {

            }

            @Override
            public void onError(int i, String s) {

            }
        });

  }
}
