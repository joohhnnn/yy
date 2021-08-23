package com.txznet.webchat.ui;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;

import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxLoginStore;
import com.txznet.webchat.stores.WxThemeStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.car.Car_QRCodeActivity;
import com.txznet.webchat.ui.car.t700.Car_QRCodeActivity_T700;
import com.txznet.webchat.ui.rearview_mirror.QRCodeActivity;

/**
 * 启动页面，根据当前主题设置和登录状态进行Activity跳转
 * Created by J on 2016/10/8.
 */

public class AppStartActivity extends AppBaseActivity {
    public static final String INTENT_KEY_TARGET_PAGE = "target_page";

    @Override
    protected int getLayout() {
        return 0;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[0];
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        /*
        * 某些Launcher启动微信时可能发送了不规范的intent, 导致重复启动了
        * AppStartActivity, 所以此处做下判断, 出现这类情况时进行下保护, 直接finish
        * 掉自己, 显示栈顶的Activity
        * */
        if (!this.isTaskRoot() && getIntent() != null) {
            String action = getIntent().getAction();
            if (getIntent().hasCategory(Intent.CATEGORY_LAUNCHER) && Intent.ACTION_MAIN.equals(action)) {
                finish();
            }
        } else {
            // 知豆双屏处理
            Intent intent = new Intent();
            intent.setAction("com.sysom.multidisplay.bind");
            sendBroadcast(intent);

            if (WxLoginStore.get().isLogin()) {
                intentMainActivity();
            } else {
                intentQRActivity();
            }
        }
    }

    @Override
    protected void initFocusViewList() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void intentQRActivity() {
        // 获取intent中的参数
        Intent intent = getIntent();
        String targetPage = intent.getStringExtra(INTENT_KEY_TARGET_PAGE);

        // 若未指定跳转页面, 按默认进入微信登录处理
        if (TextUtils.isEmpty(targetPage)) {
            targetPage = "wechat";
        }

        String theme = WxThemeStore.get().getCurrentTheme();
        // 根据当前主题跳转对应的Activity
        if (WxThemeStore.THEME_CAR.equals(theme)
                || WxThemeStore.THEME_CAR_PORTRAIT.equals(theme)) {
            Car_QRCodeActivity.show(this, targetPage);
        } else if (WxThemeStore.THEME_CAR_PORTRAIT_T700.equals(theme)) {
            Car_QRCodeActivity_T700.show(this, targetPage);
        } else {
            QRCodeActivity.show(this, targetPage);
        }

        finish();
    }

    private void intentMainActivity() {
        Intent intent = new Intent();
        intent.setClass(this, WxThemeStore.get().getClassForMainActivity());
        startActivity(intent);
        this.finish();
    }
}
