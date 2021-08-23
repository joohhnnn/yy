package com.txznet.music.historyModule.ui;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;

import com.dinuscxj.itemdecoration.LinearOffsetsItemDecoration;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.Logger;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.FavourModule.adapter.ItemAudioAdapter;
import com.txznet.music.R;
import com.txznet.music.baseModule.ui.BaseFragment;
import com.txznet.music.config.ConfigManager;
import com.txznet.music.historyModule.bean.SettingBean;
import com.txznet.music.historyModule.ui.adpter.SettingItemAdapter;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.ui.layout.TXZLinearLayoutManager;
import com.txznet.music.localModule.logic.StorageUtil;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.widget.TipsDialog;
import com.txznet.reserve.activity.ReserveStandardActivity0;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;

import butterknife.Bind;

/**
 * Created by telenewbie on 2017/12/23.
 */

public class SettingFragmentV42 extends BaseFragment {
    private static final String TAG = "SettingFragmentV42:";

    @Bind(R.id.recycler_setting)
    RecyclerView recyclerSetting;
    SettingItemAdapter settingItemAdapter;
    TXZLinearLayoutManager txzLinearLayoutManager;

    List<SettingBean> settingBeans = new ArrayList<>();
    private Runnable mDeleteCacheTask = new Runnable() {
        @Override
        public void run() {
            StorageUtil.deleteCache(1.0f);
            ImageFactory.getInstance().clearDiskCache();
        }
    };

    private RecyclerView.Adapter getAdapter() {
        settingItemAdapter = new SettingItemAdapter(getActivity(), settingBeans);
        settingItemAdapter.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                if (settingBeans.get(position).getListener() != null) {
                    settingBeans.get(position).getListener().onClick(position);
                }
            }
        });
        return settingItemAdapter;
    }

    private void showAboutUs() {
        Intent intent = new Intent(getActivity(), ReserveStandardActivity0.class);
        intent.putExtra("type", 0);
        startActivity(intent);
    }

    private void showHelp() {
        Intent intent = new Intent(getActivity(), ReserveStandardActivity0.class);
        intent.putExtra("type", 1);
        startActivity(intent);
    }

    @Override
    public void reqData() {
        settingBeans.add(new SettingBean("开机新闻推送", ConfigManager.getInstance().isBootRadioEnabled(), new ItemAudioAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                if (ConfigManager.getInstance().isBootRadioEnabled()) {
                    showPushTipsDialog(position);
                } else {
                    ConfigManager.getInstance().switchBootRadio();
                    settingBeans.get(position).setChoice(ConfigManager.getInstance().isBootRadioEnabled());
                    settingItemAdapter.notifyItemChanged(position);
                }
            }
        }));
        if (SharedPreferencesUtils.getNeedAsr()) {
            settingBeans.add(new SettingBean("免唤醒语音指令", ConfigManager.getInstance().isEnableWakeup(), new ItemAudioAdapter.OnItemClickListener() {
                @Override
                public void onClick(int position) {
                    if (ConfigManager.getInstance().isEnableWakeup()) {
                        showWakeupTipsDialog(position);
                    } else {
                        ConfigManager.getInstance().switchWakeup();
                        settingBeans.get(position).setChoice(ConfigManager.getInstance().isEnableWakeup());
                        Logger.d("test:setting", "click:" + position + "," + settingBeans.get(position));
                        settingItemAdapter.notifyItemChanged(position);
                    }
                }
            }));
        }
        settingBeans.add(new SettingBean("当前版本", BuildConfig.VERSION_NAME, null));
        settingBeans.add(new SettingBean("清除缓存", StorageUtil.formatSize(StorageUtil.getCacheSize()), new ItemAudioAdapter.OnItemClickListener() {
            @Override
            public void onClick(final int position) {
//清理缓存
                TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
                tipsDialogBuildData.setContent("缓存清理后，缓存下来的歌曲和图片将会被清理");
                tipsDialogBuildData.setContext(getActivity());
                TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
                tipsDialog.setSureListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AppLogic.removeBackGroundCallback(mDeleteCacheTask);
                        AppLogic.runOnBackGround(mDeleteCacheTask, 500);
                        ImageFactory.getInstance().clearMemory();
                        settingBeans.get(position).setRightText(StorageUtil.formatSize(0));
                        settingItemAdapter.notifyItemChanged(position);
                    }
                });
                tipsDialog.show();
            }
        }));
        settingBeans.add(new SettingBean("帮助", new ItemAudioAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                showHelp();
                ReportEvent.clickSettingPageHelp();
            }
        }));
        settingBeans.add(new SettingBean("关于同听", new ItemAudioAdapter.OnItemClickListener() {
            @Override
            public void onClick(int position) {
                showAboutUs();
                ReportEvent.clickSettingPageAboutUs();
            }
        }));

        settingItemAdapter.notifyDataSetChanged();
    }

    @Override
    public void bindViews() {
//        GridOffsetsItemDecoration
        txzLinearLayoutManager = new TXZLinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
//        recyclerSetting.addItemDecoration(new );

        recyclerSetting.setLayoutManager(txzLinearLayoutManager);
        LinearOffsetsItemDecoration linearOffsetsItemDecoration = new LinearOffsetsItemDecoration(LinearLayoutManager.VERTICAL);
        linearOffsetsItemDecoration.setItemOffsets(getActivity().getResources().getDimensionPixelSize(R.dimen.y13));
        recyclerSetting.addItemDecoration(linearOffsetsItemDecoration);
        recyclerSetting.setAdapter(getAdapter());
    }

    @Override
    public void initListener() {

    }

    @Override
    public void initData() {

    }

    @Override
    public int getLayout() {
        return R.layout.fragment_setting_v42;
    }

    @Override
    public String getFragmentId() {
        return "" + hashCode() + "/设置界面";
    }

    @Override
    public void update(Observable o, Object arg) {

    }


    private void showWakeupTipsDialog(final int position) {
        TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
        tipsDialogBuildData.setContent("关闭后，只有唤醒语音才能控制播放器，直接声控免唤醒操作播放器将不起效果，确定要关闭吗？");
        tipsDialogBuildData.setContext(GlobalContext.get());
        TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
        tipsDialog.setSureListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigManager.getInstance().switchWakeup();
                settingBeans.get(position).setChoice(ConfigManager.getInstance().isEnableWakeup());
                settingItemAdapter.notifyItemChanged(position);
            }
        });
        tipsDialog.show();
    }

    private void showPushTipsDialog(final int position) {
        TipsDialog.TipsDialogBuildData tipsDialogBuildData = new TipsDialog.TipsDialogBuildData();
        tipsDialogBuildData.setContent("关闭推送新闻后，将接收不到新闻头条的通知，确定要关闭吗？");
        tipsDialogBuildData.setContext(GlobalContext.get());
        TipsDialog tipsDialog = new TipsDialog(tipsDialogBuildData);
        tipsDialog.setSureListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ConfigManager.getInstance().switchBootRadio();
                settingBeans.get(position).setChoice(ConfigManager.getInstance().isBootRadioEnabled());
                settingItemAdapter.notifyItemChanged(position);
            }
        });
        tipsDialog.show();

    }

}
