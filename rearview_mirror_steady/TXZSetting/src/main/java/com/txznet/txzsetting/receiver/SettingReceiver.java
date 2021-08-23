package com.txznet.txzsetting.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;

import com.txznet.txzsetting.TXZApplication;
import com.txznet.txzsetting.util.JsonIntentUtil;
import com.txznet.txzsetting.util.TxzReportUtil;

/**
 * Created by ASUS User on 2017/8/15.
 */

public class SettingReceiver extends BroadcastReceiver {
    String SYSTEM_REASON = "reason";
    String SYSTEM_HOME_KEY = "homekey";
    String SYSTEM_HOME_KEY_LONG = "recentapps";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (intent.getAction().equals(JsonIntentUtil.USERCONF_CORE_UPDATE_ACTION)) {//core更新了用户配置


        } else if (intent.getAction().equals(JsonIntentUtil.FACTORY_CONF_CORE_UPDATE_ACTION)) {//Core更新出厂配置广播(adapter有改动时发出)


        }else if (intent.getAction().equals("com.txznet.txz.record.show")){
            TXZApplication.getApp().exitActivity();
        }else if (intent.getAction().equals("com.txznet.txz.record.dismiss")){

        }else if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
            String reason = intent.getStringExtra(SYSTEM_REASON);
            if ( SYSTEM_HOME_KEY.equals(reason)) {
                //表示按了home键,程序到了后台
                TxzReportUtil.doReportDestroy(TxzReportUtil.KEY_CODE_HOME);
            }
        }
    }
}
