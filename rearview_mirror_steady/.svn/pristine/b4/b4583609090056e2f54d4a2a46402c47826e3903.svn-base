package com.txznet.webchat.ui.rearview_mirror;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;

import com.squareup.otto.Subscribe;
import com.txznet.reserve.activity.ReserveStandardActivity3;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.AppStatusActionCreator;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.helper.WxNavBtnHelper;
import com.txznet.webchat.stores.AppStatusStore;
import com.txznet.webchat.stores.Store;
import com.txznet.webchat.stores.WxConfigStore;
import com.txznet.webchat.stores.WxServerConfigStore;
import com.txznet.webchat.ui.base.AppBaseActivity;
import com.txznet.webchat.ui.rearview_mirror.widget.ClearCacheDialog;
import com.txznet.webchat.ui.rearview_mirror.widget.IconTextStateBtn;

import java.util.ArrayList;

import butterknife.Bind;

/**
 * 设置页面
 * Created by J on 2017/4/1.
 */

public class SettingsActivity extends AppBaseActivity {
    @Bind(R.id.rl_setting_notify)
    RelativeLayout mRLNotify;
    @Bind(R.id.rl_setting_auto_login)
    RelativeLayout mRlAutoLogin;
    @Bind(R.id.rl_setting_clear_cache)
    RelativeLayout mRlClearCache;
    @Bind(R.id.itsb_setting_switch_notify)
    IconTextStateBtn mSwitchNotify;
    @Bind(R.id.itsb_setting_switch_auto_login)
    IconTextStateBtn mSwitchAutoLogin;
    @Bind(R.id.btn_setting_back)
    ImageButton mBtnBack;

    private ClearCacheDialog mClearCacheDialog;

    private boolean bNotifyEnabled;
    private boolean bAutoLoginEnabled;

    public static void show(Context context) {
        Intent intent = new Intent();
        intent.setClass(context, ReserveStandardActivity3.class);
        context.startActivity(intent);
    }

    @Override
    protected int getLayout() {
        return R.layout.activity_setting;
    }

    @Override
    protected Store[] getRegisterStores() {
        return new Store[]{
                AppStatusStore.get(),
        };
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        updateSettingItems();

        if (WxConfigStore.getInstance().isBackButtonEnabled()) {
            mBtnBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onBackPressed();
                }
            });
        } else {
            mBtnBack.setVisibility(View.GONE);
        }


        // 根据免扫码开关决定是否需要显示相关按钮
        if (WxServerConfigStore.getInstance().isPushLoginEnabled()) {
            mClearCacheDialog = new ClearCacheDialog(this);

            mClearCacheDialog.setClearCacheDialogListener(new ClearCacheDialog.ClearCacheDialogListener() {
                @Override
                public void onCommit() {
                    LoginActionCreator.get().clearUserCache();
                    mClearCacheDialog.dismiss();
                }

                @Override
                public void onCancel() {
                    mClearCacheDialog.dismiss();
                }
            });

            mRlAutoLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bAutoLoginEnabled = !bAutoLoginEnabled;
                    if (bAutoLoginEnabled) {
                        AppStatusActionCreator.get().enableAutoLogin();
                    } else {
                        AppStatusActionCreator.get().disableAutoLogin();
                    }
                }
            });

            mRlClearCache.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClearCacheDialog.show();
                }
            });
        } else {
            mRlAutoLogin.setVisibility(View.GONE);
            mRlClearCache.setVisibility(View.GONE);
        }

        mRLNotify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bNotifyEnabled = !bNotifyEnabled;
                if (bNotifyEnabled) {
                    AppStatusActionCreator.get().enableAutoSpeak();
                } else {
                    AppStatusActionCreator.get().disableAutoSpeak();
                }
            }
        });
    }

    @Override
    protected void initFocusViewList() {
        if (!isDpadSupportEnabled()) {
            return;
        }

        ArrayList<Object> focusableList = new ArrayList<>();
        focusableList.add(mRLNotify);
        if (WxServerConfigStore.getInstance().isPushLoginEnabled()) {
            focusableList.add(mRlAutoLogin);
            focusableList.add(mRlClearCache);
        }
        getNavBtnSupporter().setViewList(focusableList);

        // 设置默认焦点
        if (WxNavBtnHelper.getInstance().isNavBtnTriggered()) {
            getNavBtnSupporter().setCurrentFocus(mRLNotify);
        }
    }

    @Subscribe
    @Override
    public void onStoreChange(Store.StoreChangeEvent event) {
        super.onStoreChange(event);

        switch (event.getType()) {
            case AppStatusStore.EVENT_TYPE_ALL:
                updateSettingItems();
                break;
        }
    }

    private void updateSettingItems() {
        // 更新消息播报开关
        bNotifyEnabled = AppStatusStore.get().isAutoBroadEnabled();
        mSwitchNotify.setEnabled(bNotifyEnabled);

        if (WxServerConfigStore.getInstance().isPushLoginEnabled()) {
            // 更新自动登录开关
            bAutoLoginEnabled = AppStatusStore.get().isAutoLoginEnabled();
            mSwitchAutoLogin.setEnabled(bAutoLoginEnabled);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSettingItems();
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (null != mClearCacheDialog && mClearCacheDialog.isShowing()) {
            mClearCacheDialog.dismiss();
        }
    }
}
