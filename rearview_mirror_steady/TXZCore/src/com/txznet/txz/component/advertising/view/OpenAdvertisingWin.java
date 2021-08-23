package com.txznet.txz.component.advertising.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.txz.module.advertising.AdvertisingManager;

public class OpenAdvertisingWin extends Win2Dialog implements IAdvertisingWin {
    private LinearLayout mWindowsView;

    public OpenAdvertisingWin() {
        super(true, true);
    }

    public OpenAdvertisingWin(boolean isSystem, boolean isFullScreen) {
        super(isSystem, isFullScreen);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        mWindowsView.removeAllViews();
        GlobalContext.get().unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
        LogUtil.d("open ad click onBackPressed.");
        AdvertisingManager.getInstance().closeOpenAd();
    }

    @Override
    public void show() {
        if (!isShowing()) {
            super.show();
            IntentFilter intentFilter = new IntentFilter(
                    Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            GlobalContext.get().registerReceiver(mReceiver, intentFilter);
        }
    }


    @Override
    public void setOpenAdvertisingView(View view) {
        mWindowsView.addView(view);
    }

    private void log(String log) {
        LogUtil.d("open advertising " + log);
    }

    @Override
    protected View createView(Object... objects) {
        if (mWindowsView == null) {
            mWindowsView = new LinearLayout(GlobalContext.get());
            mWindowsView.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            mWindowsView.setLayoutParams(params);
        }
        return mWindowsView;
    }

    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
            if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                // 短按Home键
                LogUtil.d("open ad click HOME_KEY.");
                AdvertisingManager.getInstance().closeOpenAd();
            }
        }
    };
}
