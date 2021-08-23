package com.txznet.launcher.module.settings;

import android.content.Context;
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
import com.txznet.launcher.utils.PreferenceUtil;
import com.txznet.launcher.utils.StringUtils;
import com.txznet.loader.AppLogic;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * 打开wifi是展示的界面。
 * ap 应该是Access Point 接入点。好像路由器就可以说是一个ap，这里应该就是用来指代无线网络吧。
 */
public class APModule extends BaseModule {

    @Bind(R.id.iv_settings_ap_status)
    ImageView ivSettingsApStatus;
    @Bind(R.id.tv_settings_ap_status)
    TextView tvSettingsApStatus;
    @Bind(R.id.tv_settings_ap_name)
    TextView tvSettingsApName;
    @Bind(R.id.tv_settings_ap_password)
    TextView tvSettingsApPassword;

    private int mCurrentType = SettingsManager.TYPE_SETTINGS_AP_CTRL_OPEN;

    @Override
    public void onCreate(String data) {
        super.onCreate(data);
        parseData(data);
    }

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View view = View.inflate(context, R.layout.module_settings_ap, null);
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
        JSONBuilder jsonBuilder = new JSONBuilder(data);
        String cmd = jsonBuilder.getVal("cmd", String.class);
        int type = jsonBuilder.getVal("type", Integer.class, 0);
        if (TextUtils.equals(cmd, "ctrl") && type != 0) {
            mCurrentType = type;
        }
    }

    private void refreshContainer() {
        tvSettingsApName.setText(StringUtils.getWifiName());
        tvSettingsApPassword.setText(PreferenceUtil.getInstance().getString(PreferenceUtil.KEY_WIFI_AP_PSD,PreferenceUtil.DEFAULT_WIFI_AP_PSD));
        switch (mCurrentType) {
            case SettingsManager.TYPE_SETTINGS_AP_CTRL_OPEN:
                ivSettingsApStatus.setImageResource(R.drawable.ic_settings_ap_on);
                tvSettingsApStatus.setTextColor(tvSettingsApStatus.getResources().getColor(R.color.color_settings_ap_status_on));
                tvSettingsApStatus.setText("已开启");
                break;
            case SettingsManager.TYPE_SETTINGS_AP_CTRL_CLOSE:
                ivSettingsApStatus.setImageResource(R.drawable.ic_settings_ap_off);
                tvSettingsApStatus.setTextColor(tvSettingsApStatus.getResources().getColor(R.color.color_settings_ap_status_off));
                tvSettingsApStatus.setText("已关闭");
                break;
            case SettingsManager.TYPE_SETTINGS_AP_CTRL_REFRESH:
                break;
            case SettingsManager.TYPE_SETTINGS_AP_CTRL_SHOW:
                break;
        }
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
