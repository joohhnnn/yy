package com.txznet.txz.component.offlinepromote.dialog;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.recordwin.Win2Dialog;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.module.offlinepromote.OfflinePromoteManager;
import com.txznet.txz.util.DeviceInfo;

import java.util.Objects;

public class OfflinePromoteHintDialog extends Win2Dialog {

    public OfflinePromoteHintDialog(String title, String content) {
        super(true, false, title, content);
    }

    @Override
    protected View createView(Object... objects) {
        String title = (String) objects[0];
        String content = (String) objects[1];

        LinearLayout linearLayout = new LinearLayout(GlobalContext.get());
        linearLayout.setGravity(Gravity.CENTER);
        linearLayout.addView(createView(title,content));
        return linearLayout;
    }

    public View createView(String title, String content) {
        LinearLayout rootLayout = new LinearLayout(GlobalContext.get());
        rootLayout.setScaleX(getScale());
        rootLayout.setScaleY(getScale());
        int width = 460;
        int height = 200;
        LinearLayout.LayoutParams rootParams = new LinearLayout.LayoutParams(width, height);
        rootLayout.setLayoutParams(rootParams);
        rootLayout.setBackground(LayouUtil.getDrawable("shape_feedback"));
        rootLayout.setOrientation(LinearLayout.VERTICAL);
        rootLayout.setGravity(Gravity.CENTER_HORIZONTAL);

        //标题文本
        TextView tvTittle = new TextView(GlobalContext.get());
        tvTittle.setText(title);
        tvTittle.setTextColor(Color.WHITE);
        int titleSize = 24;
        tvTittle.setTextSize(TypedValue.COMPLEX_UNIT_PX, titleSize);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 30;
        tvTittle.setLayoutParams(params);
        rootLayout.addView(tvTittle);

        //文本内容
        TextView tvContent = new TextView(GlobalContext.get());
        tvContent.setTextColor(Color.WHITE);
        tvContent.setText(content);
        int contentSize = 20;
        tvContent.setTextSize(TypedValue.COMPLEX_UNIT_PX, contentSize);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.topMargin = 20;
        tvContent.setLayoutParams(params);
        rootLayout.addView(tvContent);
        //分割线
        View line = new View(GlobalContext.get());
        line.setBackgroundColor(Color.parseColor("#21FFFFFF"));
        int lineHeight = 1;
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, lineHeight);
        params.topMargin = 38;
        line.setLayoutParams(params);
        rootLayout.addView(line);

        //btn
        TextView tvSure = new TextView(GlobalContext.get());
        tvSure.setTextColor(Color.parseColor("#44A9FF"));
        tvSure.setGravity(Gravity.CENTER);
        tvSure.setText("我知道了");
        tvSure.setTextSize(TypedValue.COMPLEX_UNIT_PX, 20);
        params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
        tvSure.setLayoutParams(params);
        rootLayout.addView(tvSure);
        tvSure.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        dismiss();
                    }
                });
            }
        });

        return rootLayout;
    }

    @Override
    public void show() {
        super.show();
        GlobalContext.get().registerReceiver(mHomeReceiver,new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
    }

    @Override
    public void dismiss() {
        super.dismiss();
        GlobalContext.get().unregisterReceiver(mHomeReceiver);
    }

    public float getScale() {
        return Math.min((float) DeviceInfo.getScreenWidth() / 1024, (float) DeviceInfo.getScreenHeight() / 600);
    }

    private BroadcastReceiver mHomeReceiver = new BroadcastReceiver() {
        private static final String LOG_TAG = "HomeReceiver";
        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
        private static final String SYSTEM_DIALOG_REASON_LOCK = "lock";
        private static final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            LogUtil.logd("onReceive: action: " + action);
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                // android.intent.action.CLOSE_SYSTEM_DIALOGS
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (SYSTEM_DIALOG_REASON_HOME_KEY.equals(reason)) {
                    // 短按Home键
                    dismiss();
                } else if (SYSTEM_DIALOG_REASON_RECENT_APPS.equals(reason)) {
                    // 长按Home键 或者 activity切换键
                } else if (SYSTEM_DIALOG_REASON_LOCK.equals(reason)) {
                    // 锁屏
                } else if (SYSTEM_DIALOG_REASON_ASSIST.equals(reason)) {
                    // samsung 长按Home键
                }
            }
        }
    };
}
