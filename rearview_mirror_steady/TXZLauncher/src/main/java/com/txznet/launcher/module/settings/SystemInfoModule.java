package com.txznet.launcher.module.settings;

import android.content.Context;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.utils.DeviceUtils;
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.loader.AppLogic;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 系统信息界面
 */
public class SystemInfoModule extends BaseModule {

    @Bind(R.id.tv_settings_system_info_version)
    TextView tvVersion;
    @Bind(R.id.tv_settings_system_info_imei)
    TextView tvImei;
    @Bind(R.id.tv_settings_system_info_mcu)
    TextView tvMcu;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        parseData(data);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_settings_system_info, null);
        ButterKnife.bind(this, view);
        refreshContainer();
        return view;
    }

    @Override
    public void refreshView(String data) {
        super.refreshView(data);
        parseData(data);
        refreshContainer();
    }

    private void parseData(String data) {

    }

    private void refreshContainer() {
        tvVersion.setText(String.format("系统版本：%s", Build.DISPLAY));
        tvImei.setText(String.format("IMEI号：%s", DeviceUtils.getIMEI()));
        String mcu = SettingsManager.getInstance().getMcuVersion();
        if (TextUtils.isEmpty(mcu)) {
            mcu = "";
            SettingsManager.getInstance().syncMcuVersion();
        }
        tvMcu.setText(String.format("MCU号：%s", mcu));
    }

    private Runnable closeRunnable = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().showAtImageBottom("");
            LaunchManager.getInstance().launchDesktop();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        AppLogic.removeUiGroundCallback(closeRunnable);
        AppLogic.runOnUiGround(closeRunnable, PreferenceUtil.getInstance().getLong(PreferenceUtil.KEY_WECHAT_BIND_QR_TIMEOUT, PreferenceUtil.DEFAULT_WECHAT_BIND_QR_TIMEOUT));
    }

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        AppLogic.removeUiGroundCallback(closeRunnable);
    }
}
