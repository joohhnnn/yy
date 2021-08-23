package com.txznet.comm.ui.theme.test.config;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.resloader.UIResLoader;
import com.txznet.comm.ui.theme.test.skin.SK;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.Locale;

public class SizeConfig {

    private static final String TAG = "SizeConfig";

    private static SizeConfig sInstance = new SizeConfig();

    private SizeConfig() {

    }

    public static SizeConfig getInstance() {
        return sInstance;
    }

    //public static Boolean isVertScreen;    //当前是否竖屏

    public static int titleHeight;    //标题高度
    public static int itemHeight;    //列表一项的高度
    public static int itemHeightPro;    //票务、商圈、有连载状态的有声书列表一项的高度
    public static int pageWidth;    //翻页键宽度
    public static int pageTextHeight;    //翻页键文本高度
    public static int pageButtonSize;    //翻页键按键大小
    public static int itemHelpHeight;    //帮助列表一项的高度
    public static int itemHelpDetailHeight;    //帮助列表一项的高度

    public static int screenWidth;
    public static int screenHeight;

    //统一设定的列表个数
    public static int pageCount;
    //机票界面的列表个数
    public static int pageFlightCount;
    //火车票界面的列表个数
    public static int pageTrainCount;
    //导航列表模式界面的列表个数
    public static int pagePoiCount;
    //导航地图模式界面的列表个数，无屏模式没有此功能
    public static int pageMapPoiCount;
    //商圈列表模式界面的列表个数
    public static int pageBusinessPoiCount;
    //商圈地图模式界面的列表个数, 无屏模式没有此功能
    public static int pageBusinessMapPoiCount;
    //导航历史记录列表模式的列表个数
    public static int pagePoiHistoryCount;
    //导航历史记录地图模式的列表个数，无屏模式没有此功能
    public static int pageMapPoiHistoryCount;
    //帮助一级界面的列表个数
    public static int pageHelpCount;
    //帮助二级界面的列表个数
    public static int pageHelpDetailCount;
    //电影界面的列表个数
    public static int pageMovieCount;
    //音乐选择界面的列表个数
    public static int pageAudioCount;
    //有连载信息的有声书界面的列表个数
    public static int pageAudioTagCount;
    //赛事页面的列表个数
    public static int pageCompetitionCount;


    /**
     * 填写设计图中屏幕的宽度（px），调用{@link #setCustomDensityByWidth)}会自动将当前屏幕的宽度设置为这么多dp，达到自动适配的目的
     * <b>注意：必须是float</b>
     */
    private static final float DESIGNED_SCREEN_WIDTH_DP = 710;

    /**
     * 填写设计图中屏幕的高度（px），调用{@link #setCustomDensityByHeight}会自动将当前屏幕的高度设置为这么多dp，达到自动适配的目的
     * <b>注意：必须是float</b>
     */
    private static final float DESIGNED_SCREEN_HEIGHT_DP = 600;

    //初始化屏幕尺寸参数
    public void initScreenSize() {
        String w = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_SCREEN_WIDTH_DP);
        String h = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_SCREEN_HEIGHT_DP);

        screenWidth = w != null ? Integer.parseInt(w) : ScreenUtil.getScreenWidth();
        screenHeight = h != null ? Integer.parseInt(h) : ScreenUtil.getScreenHeight();
        //WinLayout.isVertScreen = (double)SizeConfig.screenWidth / (double) SizeConfig.screenHeight <= 1.325 && SizeConfig.screenHeight > 600;
        WinLayout.isVertScreen = screenWidth < SizeConfig.screenHeight && screenWidth > 600;
        ViewParamsUtil.getIntance().updateViewParams(WinLayout.isVertScreen);
        LogUtil.logd(WinLayout.logTag + "initScreen----screenWidth: " + screenWidth +
                "--screenHeight:" + screenHeight + "--isVertScreen:" + WinLayout.isVertScreen);
        LogUtil.logd(String.format(Locale.getDefault(),
                "initScreen: maxDimen: x800:%f, y480:%f, m480:%f",
                LayouUtil.getDimen("x800"),
                LayouUtil.getDimen("y480"),
                LayouUtil.getDimen("m480")));
        

        if(WinLayout.isVertScreen){
            setCustomDensityByWidth(UIResLoader.getInstance().getModifyContext());
        } else {
            setCustomDensityByHeight(UIResLoader.getInstance().getModifyContext());
        }

        SK.applySkin("/sdcard/txz/skin.apk");
    }

    private static void setDensity(Context context, DisplayMetrics appDisplayMetrics, float targetDensity) {
        final int targetDensityDpi = (int) (160 * targetDensity);
        Log.d(TAG, "Target Density:" + targetDensity);
        Log.d(TAG, "Target Density:" + targetDensityDpi + "dpi");

        appDisplayMetrics.density = appDisplayMetrics.scaledDensity = targetDensity;
        appDisplayMetrics.densityDpi = targetDensityDpi;

        final DisplayMetrics activityDisplayMetrics = context.getResources().getDisplayMetrics();
        activityDisplayMetrics.density = activityDisplayMetrics.scaledDensity = targetDensity;
        activityDisplayMetrics.densityDpi = targetDensityDpi;

        Resources resources = UIResLoader.getInstance().getModifyContext().getResources();
        Configuration configuration = resources.getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.densityDpi = targetDensityDpi;
        }
        resources.updateConfiguration(configuration, appDisplayMetrics);
        Log.d(TAG, "Density Updated");
    }

    // 根据宽度自适应布局
    public static void setCustomDensityByWidth(Context context) {
        final DisplayMetrics appDisplayMetrics = UIResLoader.getInstance().getModifyContext().getResources().getDisplayMetrics();
        Log.d(TAG, "Width: " + appDisplayMetrics.widthPixels + "px");
        Log.d(TAG, "Density: " + appDisplayMetrics.density);
        Log.d(TAG, "Density: " + appDisplayMetrics.densityDpi + "dpi");
        final float targetDensity = appDisplayMetrics.widthPixels / DESIGNED_SCREEN_WIDTH_DP;
        setDensity(context, appDisplayMetrics, targetDensity);
    }

    // 根据高度自适应布局
    public static void setCustomDensityByHeight(Context context) {
        final DisplayMetrics appDisplayMetrics = UIResLoader.getInstance().getModifyContext().getResources().getDisplayMetrics();
        Log.d(TAG, "Height: " + appDisplayMetrics.heightPixels + "px");
        Log.d(TAG, "Density: " + appDisplayMetrics.density);
        Log.d(TAG, "Density: " + appDisplayMetrics.densityDpi + "dpi");
        final float targetDensity = appDisplayMetrics.heightPixels / DESIGNED_SCREEN_HEIGHT_DP;
        setDensity(context, appDisplayMetrics, targetDensity);
    }

    public void init() {
        this.init(4);
    }

    public void init(int count) {
        initNone();
        boolean enableMapPoi = false;

        TXZConfigManager.getInstance().setPagingBenchmarkCount(pageCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_FLIGHT_LIST, pageFlightCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_TRAIN_LIST, pageTrainCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_LIST, pagePoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_MAP_LIST, pageMapPoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_LIST, pageBusinessPoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_MAP_LIST, pageBusinessMapPoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_LIST, pagePoiHistoryCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_MAP_LIST, pageMapPoiHistoryCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_HELP_LIST, pageHelpCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_HELP_DETAIL_LIST, pageHelpDetailCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_MOVIE_LIST, pageMovieCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_FILM_LIST, pageMovieCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_AUDIO_LIST, pageAudioCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_AUDIO_WITH_TAG, pageAudioTagCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_COMPETITION_LIST, pageCompetitionCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_TICKET_PAY_LIST, 1);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_FLIGHT_TICKET_LIST, pageFlightCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_TRAIN_TICKET_LIST, pageTrainCount);
        //TXZConfigManager.getInstance().setMoviePagingBenchmarkCount(3);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.isSupportMapPoi",
                String.valueOf(enableMapPoi).getBytes(), null);

        //initMovieSize();
    }

    //无屏界面布局
    private void initNone() {
        if (screenHeight >= 384) {
            pageCount = 4;
        } else if (screenHeight >= 304) {
            pageCount = 2;
        } else {
            pageCount = 2;
        }
        // TODO 测试用
        pageCount = 4;
        if (WinLayout.isVertScreen) {
            int unit = (int) LayouUtil.getDimen("x375") / (2 * pageCount + 1);    //无屏内容高度固定
            int movieWidth = ViewParamsUtil.billWidthNone;
            titleHeight = unit;
            itemHeight = 2 * unit;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            itemHelpHeight = 2 * unit;
            pageWidth = (int) LayouUtil.getDimen("x103");
            pageTextHeight = (int) LayouUtil.getDimen("x83");
            pageButtonSize = (int) LayouUtil.getDimen("x41");

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            //pageMapPoiCount = pageCount / 2;
            pageBusinessPoiCount = pageCount;
            //pageBusinessMapPoiCount = pageCount / 2;
            pagePoiHistoryCount = pageCount;
            //pageMapPoiHistoryCount = pageCount;
            pageHelpCount = pageCount;
            pageHelpDetailCount = 100;// 帮助二级列表
            pageMovieCount = pageCount - 1;
            //pageMovieCount = (screenWidth * 3/4 - pageWidth - 4 * unit) / movieWidth;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount - 1;
            pageCompetitionCount = pageCount - 1;
        } else {
            int unit = ViewParamsUtil.unit;
            titleHeight = 6 * unit;
            itemHeight = 30 / pageCount * unit;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            pageWidth = 10 * unit;
            pageTextHeight = 8 * unit;
            pageButtonSize = 4 * unit;
            itemHelpHeight = itemHeight;

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            pageMapPoiCount = pageCount;
            pageBusinessPoiCount = pageCount - 1;
            pageBusinessMapPoiCount = pageCount - 1;
            pagePoiHistoryCount = pageCount;
            pageMapPoiHistoryCount = pageCount;
            pageHelpCount = pageCount;
            pageHelpDetailCount = 100;// 帮助二级列表
            pageMovieCount = pageCount - 1;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount - 1;
            pageCompetitionCount = pageCount - 1;
        }
    }

}

