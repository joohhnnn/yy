package com.txznet.launcher.module.notification;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.txznet.launcher.R;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.notification.TodayNoticeManager;
import com.txznet.launcher.domain.notification.data.INoticeData;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.module.BaseModule;
import com.txznet.launcher.module.IModule;
import com.txznet.launcher.module.record.ChatWeatherModule;
import com.txznet.loader.AppLogic;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by TXZ-METEORLUO on 2018/2/26.
 * 今日贴士的界面。
 */

public class TodayNoticeModule extends BaseModule {
    @Bind(R.id.vg_notice_content)
    ViewGroup vg_notice_content;

    private List<INoticeModule> mNoticeCardList;

    @Override
    public View onCreateView(Context context, ViewGroup parent, int status) {
        View contentView = LayoutInflater.from(context).inflate(R.layout.module_today_notice, parent, false);
        ButterKnife.bind(this, contentView);
        initialView(context);
        return contentView;
    }

    private void initialView(Context context) {
        // 生成卡片
        // 今日贴士有天气、限行、违章三种类型，所有用列表来展示卡片。
        mNoticeCardList = createNoticeModuleList();
        for (INoticeModule noticeCard : mNoticeCardList) {
            View view;
            int status;
            if (mNoticeCardList.size() == 3) {
                status = IModule.STATUS_THIRD;
            } else if (mNoticeCardList.size() == 2) {
                status = IModule.STATUS_HALF;
            } else {
                status = IModule.STATUS_FULL;
            }
            view = noticeCard.onCreateView(context, null, status);
            ViewGroup.MarginLayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1);
            layoutParams.topMargin = context.getResources().getDimensionPixelSize(R.dimen.dimen_notice_card_offset);
            vg_notice_content.addView(view, layoutParams);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // 播报信息
        mCurPlayTtsIndex = 0;
        playNoticeTts(mNoticeCardList);
    }

    private int mCurPlayTtsIndex;
    private INoticeModule mCurNoticeModule;

    private void playNoticeTts(final List<INoticeModule> noticeModuleList) {
        if (mCurPlayTtsIndex <= noticeModuleList.size() - 1) {
            mCurNoticeModule = noticeModuleList.get(mCurPlayTtsIndex);
            mCurNoticeModule.playNotice(mCurPlayTtsIndex, new Runnable() {
                @Override
                public void run() {
                    mCurPlayTtsIndex++;
                    playNoticeTts(noticeModuleList);
                }
            });
        } else {
            AppLogic.runOnUiGround(mLauncherDesktopTask, 3000);
        }
    }

    private Runnable mLauncherDesktopTask = new Runnable() {
        @Override
        public void run() {
            LaunchManager.getInstance().launchDesktop(); // 返回桌面
        }
    };

    @Override
    public void onPreRemove() {
        super.onPreRemove();
        mCurPlayTtsIndex = 0;
        if (mCurNoticeModule != null) {
            mCurNoticeModule.onPreRemove();
        }
        for (INoticeModule module : mNoticeCardList) {
            if (module != mCurNoticeModule) {
                module.onPreRemove();
            }
        }
        AppLogic.removeUiGroundCallback(mLauncherDesktopTask);
        // 执行结束的任务
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_BOOT_TODAY_NOTICE_COMPLETE);
        TodayNoticeManager.getInstance().release();
    }

    private List<INoticeModule> createNoticeModuleList() {
        // 卡片生成策略
        // 当前是否登录了安吉星账号
        // 当天是否账号生日
        // 当天是否节假日
        // 违章
        // 限行

        List<INoticeModule> results = new ArrayList<>();
        List<INoticeData> dataList = TodayNoticeManager.getInstance().getLoadedData();
        for (INoticeData data : dataList) {
            INoticeModule module = getNoticeModuleImpl(data);
            if (module != null) {
                results.add(module);
            }
        }
        return results;
    }

    // 根据类型获取默认实现类
    private INoticeModule getNoticeModuleImpl(INoticeData data) {
        INoticeModule module = null;
        switch (data.getType()) {
            case INoticeData.DATA_TYPE_WEATHER:
                module = new ChatWeatherModule();
                module.onCreate(data.getData());
                break;
        }
        return module;
    }
}