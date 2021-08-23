//package com.txznet.music.historyModule.ui;
//
//import android.content.Intent;
//import android.view.View;
//import android.widget.ImageView;
//import android.widget.LinearLayout;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import com.txznet.comm.remote.util.LogUtil;
//import com.txznet.fm.bean.InfoMessage;
//import com.txznet.loader.AppLogic;
//import com.txznet.music.BuildConfig;
//import com.txznet.music.R;
//import com.txznet.music.baseModule.plugin.CommandString;
//import com.txznet.music.baseModule.ui.BaseFragment;
//import com.txznet.music.config.ConfigManager;
//import com.txznet.music.image.ImageFactory;
//import com.txznet.music.localModule.logic.LocalMusicEngine;
//import com.txznet.music.utils.SDUtils;
//import com.txznet.music.utils.SharedPreferencesUtils;
//import com.txznet.music.utils.ViewUtils;
//import com.txznet.music.widget.TipsDialog;
//import com.txznet.reserve.activity.ReserveStandardActivity0;
//import com.txznet.txz.plugin.PluginManager;
//
//import java.util.Observable;
//
///**
// * Created by Terry on 2017/7/13.
// */
//
//public class SettingFragment extends BaseFragment implements View.OnClickListener {
//
//    private RelativeLayout mLayoutPush;
//    private ImageView ivPush;
//    private RelativeLayout mLayoutVersion;
//    private TextView tvVersion;
//    private RelativeLayout mLayoutHelp;
//    private RelativeLayout mLayoutWakeup;
//    private ImageView ivWakeup;
//    private RelativeLayout mLayoutCache;
//    private TextView tvCache;
//    private RelativeLayout mLayoutAbout;
//    private Runnable mDeleteCacheTask = new Runnable() {
//        @Override
//        public void run() {
//            LocalMusicEngine.getInstance().deleteCache(1.0f);
//            ImageFactory.getInstance().clearDiskCache();
//        }
//    };
//
//    @Override
//    public void reqData() {
//
//    }
//
//    @Override
//    public void bindViews() {
//        mLayoutPush = (RelativeLayout) findViewById(R.id.layout_push);
//        ivPush = (ImageView) findViewById(R.id.iv_push);
//        mLayoutVersion = (RelativeLayout) findViewById(R.id.layout_version);
//        tvVersion = (TextView) findViewById(R.id.tv_version);
//        mLayoutHelp = (RelativeLayout) findViewById(R.id.layout_help);
//        mLayoutWakeup = (RelativeLayout) findViewById(R.id.layout_wakeup);
//        ivWakeup = (ImageView) findViewById(R.id.iv_wakeup);
//        mLayoutCache = (RelativeLayout) findViewById(R.id.layout_cache);
//        tvCache = (TextView) findViewById(R.id.tv_cache);
//        mLayoutAbout = (RelativeLayout) findViewById(R.id.layout_about);
//
//        Object obj = PluginManager.invoke(CommandString.PLUGIN_SETTING_AUDIOTRACK + CommandString.OPENUI);
//        if ((obj != null && obj instanceof Boolean && (boolean) obj)) {
//            LinearLayout llSettingLayout = (LinearLayout) findViewById(R.id.layout_setting);
//            llSettingLayout.addView(llSettingLayout);
//            llSettingLayout.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    PluginManager.invoke(CommandString.PLUGIN_SETTING_AUDIOTRACK + CommandString.OPEN_UI_PROCESS, SettingFragment.this.getActivity());
//                }
//            });
//        }
//
//        tvCache.setText(SDUtils.formatSize(LocalMusicEngine.getInstance().getCacheSize()));
//        tvVersion.setText("V" + BuildConfig.VERSION_NAME);
//    }
//
//    @Override
//    public void initListener() {
//        mLayoutPush.setOnClickListener(this);
//        mLayoutVersion.setOnClickListener(this);
//        mLayoutHelp.setOnClickListener(this);
//        mLayoutWakeup.setOnClickListener(this);
//        mLayoutCache.setOnClickListener(this);
//        mLayoutAbout.setOnClickListener(this);
//
//        if (!SharedPreferencesUtils.getNeedAsr()) {
//            mLayoutWakeup.setVisibility(View.GONE);
//            mLayoutWakeup.setOnClickListener(null);
//        }
//    }
//
//    @Override
//    public void initData() {
//        ivPush.setBackground(getActivity().getResources().getDrawable(ConfigManager.getInstance().isBootRadioEnabled() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off));
//        ivWakeup.setBackground(getActivity().getResources().getDrawable(ConfigManager.getInstance().isEnableWakeup() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off));
//        ViewUtils.setViewBgDrawable(mLayoutPush, R.drawable.bg_item_setting);
//        ViewUtils.setViewBgDrawable(mLayoutVersion, R.drawable.bg_item_setting);
//        ViewUtils.setViewBgDrawable(mLayoutHelp, R.drawable.bg_item_setting);
//        ViewUtils.setViewBgDrawable(mLayoutWakeup, R.drawable.bg_item_setting);
//        ViewUtils.setViewBgDrawable(mLayoutCache, R.drawable.bg_item_setting);
//        ViewUtils.setViewBgDrawable(mLayoutAbout, R.drawable.bg_item_setting);
//
//    }
//
//    @Override
//    public int getLayout() {
//        return R.layout.fragment_setting;
//    }
//
//    @Override
//    public String getFragmentId() {
//        return "SettingFragment#" + this.hashCode() + "/系统设置";
//    }
//
//    @Override
//    public void update(Observable o, Object arg) {
//        if (arg instanceof InfoMessage) {
//            InfoMessage info = (InfoMessage) arg;
//            switch (info.getType()) {
//                case InfoMessage.WAKEUP_ENABLE:
//                    ivWakeup.setBackground(getActivity().getResources().getDrawable(R.drawable.setting_switch_on));
//                    break;
//                case InfoMessage.WAKEUP_DISABLE:
//                    ivWakeup.setBackground(getActivity().getResources().getDrawable(R.drawable.setting_switch_off));
//                    break;
//            }
//        }
//
//    }
//
//    @Override
//    public void onClick(View v) {
//
//        switch (v.getId()) {
//            case R.id.layout_push:
//                LogUtil.logd(TAG + "[" + getFragmentId() + "]onClick:layout_push");
//                if (ConfigManager.getInstance().isBootRadioEnabled()) {
//                    showPushTipsDialog();
//                } else {
//                    ConfigManager.getInstance().switchBootRadio();
//                    ivPush.setBackground(getActivity().getResources().getDrawable(ConfigManager.getInstance().isBootRadioEnabled() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off));
//                }
//                break;
//            case R.id.layout_wakeup:
//                LogUtil.logd(TAG + "[" + getFragmentId() + "]onClick:layout_wakeup");
//                if (ConfigManager.getInstance().isEnableWakeup()) {
//                    showWakeupTipsDialog();
//                } else {
//                    ConfigManager.getInstance().switchWakeup();
//                    ivWakeup.setBackground(getActivity().getResources().getDrawable(ConfigManager.getInstance().isEnableWakeup() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off));
//                }
//                break;
//            case R.id.layout_cache:
//                LogUtil.logd(TAG + "[" + getFragmentId() + "]onClick:layout_cache");
//                showCacheTipsDialog();
//                break;
//            case R.id.layout_help:
//                LogUtil.logd(TAG + "[" + getFragmentId() + "]onClick:layout_help");
//                showHelp();
//                break;
//            case R.id.layout_about:
//                LogUtil.logd(TAG + "[" + getFragmentId() + "]onClick:layout_about");
//                showAboutUs();
//                break;
//
//        }
//    }
//
//    private void showWakeupTipsDialog() {
//        TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
//        tipsDialogBuildData.setContent("关闭后，只有唤醒语音才能控制播放器，直接声控免唤醒操作播放器将不起效果，确定要关闭吗？");
//        tipsDialogBuildData.setContext(getActivity());
//        TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
//        tipsDialog.setSureListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ConfigManager.getInstance().switchWakeup();
//                ivWakeup.setBackground(getActivity().getResources().getDrawable(ConfigManager.getInstance().isEnableWakeup() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off));
//            }
//        });
//        tipsDialog.show();
//    }
//
//    private void showPushTipsDialog() {
//        TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
//        tipsDialogBuildData.setContent("关闭推送新闻后，将接收不到新闻头条的通知，确定要关闭吗？");
//        tipsDialogBuildData.setContext(getActivity());
//        TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
//        tipsDialog.setSureListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                ConfigManager.getInstance().switchBootRadio();
//                ivPush.setBackground(getActivity().getResources().getDrawable(ConfigManager.getInstance().isBootRadioEnabled() ? R.drawable.setting_switch_on : R.drawable.setting_switch_off));
//            }
//        });
//        tipsDialog.show();
//
//    }
//
//    private void showCacheTipsDialog() {
//        TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
//        tipsDialogBuildData.setContent("缓存清理后，缓存下来的歌曲和图片将会被清理");
//        tipsDialogBuildData.setContext(getActivity());
//        TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
//        tipsDialog.setSureListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AppLogic.removeBackGroundCallback(mDeleteCacheTask);
//                AppLogic.runOnBackGround(mDeleteCacheTask, 500);
//                ImageFactory.getInstance().clearMemory();
//                tvCache.setText(SDUtils.formatSize(0));
//            }
//        });
//        tipsDialog.show();
//    }
//
//    private void showAboutUs() {
//        Intent intent = new Intent(getActivity(), ReserveStandardActivity0.class);
//        intent.putExtra("type", 0);
//        startActivity(intent);
//    }
//
//    private void showHelp() {
//        Intent intent = new Intent(getActivity(), ReserveStandardActivity0.class);
//        intent.putExtra("type", 1);
//        startActivity(intent);
//    }
//
//}
