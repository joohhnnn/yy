package com.txznet.launcher.domain.upgrade;

import android.support.annotation.IntDef;

/**
 * Created by daviddai on 2018/9/7
 * 定义处理是否告知用户推送升级的接口。总觉得这个判断逻辑以后要改动
 * 怎么接口只定义是否要升级，怎么升级不关这里的事，也不要将数据的处理放到外面去。
 */
public interface IPushOTAStrategy {

    int NO_UPDATE = 0;
    int FIRST_UPDATE = 1;
    int RESUME_UPDATE = 2;

    @IntDef({NO_UPDATE, FIRST_UPDATE, RESUME_UPDATE})
    @interface UpgradeType {
    }


    /**
     * 接收到升级推送时执行的方法
     *
     * @param version 推送的版本
     */
    void onReceiveOTAPush(String version);

    /**
     * 判断是否要给用户展示升级
     */
    @UpgradeType
    int isNotifyOTAUpgrade();

    /**
     * 升级确认弹出现
     */
    void onShow();

    /**
     * 确认升级的时候调用
     */
    void onSelectDownload();

    /**
     * 升级确认弹框中，用户选择取消升级时调用
     */
    void onCancel();

    /**
     * 正在升级弹框中，升级失败时调用
     */
    void onFailure();
}
