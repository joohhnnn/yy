package com.txznet.comm.ui.theme.test.config;

import android.util.Log;

import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.theme.test.winlayout.WinLayout;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.sdk.TXZConfigManager;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.TXZFileConfigUtil;

public class SizeConfig {

    private static SizeConfig sInstance = new SizeConfig();

    private SizeConfig(){

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
    //导航地图模式界面的列表个数
    public static int pageMapPoiCount;
    //商圈列表模式界面的列表个数
    public static int pageBusinessPoiCount;
    //商圈地图模式界面的列表个数
    public static int pageBusinessMapPoiCount;
    //导航历史记录列表模式的列表个数
    public static int pagePoiHistoryCount;
    //导航历史记录地图模式的列表个数
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

    //初始化屏幕尺寸参数
    public void initScreenSize(){
        String w = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_SCREEN_WIDTH_DP);
        String h = TXZFileConfigUtil.getSingleConfig(TXZFileConfigUtil.KEY_SCREEN_HEIGHT_DP);

        screenWidth = w != null?Integer.parseInt(w):ScreenUtil.getScreenWidth();
        screenHeight = h != null?Integer.parseInt(h):ScreenUtil.getScreenHeight();
        //WinLayout.isVertScreen = (double)SizeConfig.screenWidth / (double) SizeConfig.screenHeight <= 1.325 && SizeConfig.screenHeight > 600;
        WinLayout.isVertScreen = screenWidth < SizeConfig.screenHeight && screenWidth > 600;
        ViewParamsUtil.getIntance().updateViewParams(WinLayout.isVertScreen);
        LogUtil.logd(WinLayout.logTag+ "initScreen----screenWidth: " + screenWidth+
                "--screenHeight:"+screenHeight+"--isVertScreen:"+ WinLayout.isVertScreen);
        LogUtil.logd(WinLayout.logTag+ "initScreen: maxDimens--"+LayouUtil.getDimen("x800")+"--"+LayouUtil.getDimen("y480"));
    }

    public void init(){
        this.init(4);
    }

    public void init(int count){
        boolean enableMapPoi = true;
        switch (StyleConfig.getInstance().getSelectStyleIndex()) {
            case StyleConfig.STYLE_ROBOT_FULL_SCREES:
                initFull();
                break;
            case StyleConfig.STYLE_ROBOT_HALF_SCREES:
                initHalf();
                break;
            case StyleConfig.STYLE_ROBOT_NONE_SCREES:
                initNone();
                enableMapPoi = false;
                break;
            default:
                break;
        }
        TXZConfigManager.getInstance().setPagingBenchmarkCount(pageCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_FLIGHT_LIST,pageFlightCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_TRAIN_LIST,pageTrainCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_LIST,pagePoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_MAP_LIST,pageMapPoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_LIST,pageBusinessPoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_POI_BUSSINESS_MAP_LIST,pageBusinessMapPoiCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_LIST,pagePoiHistoryCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_NAV_HISTORY_MAP_LIST,pageMapPoiHistoryCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_HELP_LIST,pageHelpCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_HELP_DETAIL_LIST,pageHelpDetailCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_MOVIE_LIST,pageMovieCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_FILM_LIST,pageMovieCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_AUDIO_LIST,pageAudioCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_AUDIO_WITH_TAG,pageAudioTagCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_COMPETITION_LIST,pageCompetitionCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_TICKET_PAY_LIST,1);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_FLIGHT_TICKET_LIST,pageFlightCount);
        TXZConfigManager.getInstance().setPagingBenchmarkCount(TXZConfigManager.PageType.PAGE_TYPE_TRAIN_TICKET_LIST,pageTrainCount);
        //TXZConfigManager.getInstance().setMoviePagingBenchmarkCount(3);
        ServiceManager.getInstance().sendInvoke(ServiceManager.TXZ, "txz.record.win.isSupportMapPoi",
                String.valueOf(enableMapPoi).getBytes(), null);

        //initMovieSize();
    }

    //全屏界面布局
    private void initFull(){
        //int unit = (int) LayouUtil.getDimen("unit");
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            pageCount = (screenHeight - screenWidth/4 - screenWidth/20 - screenWidth / 10) / (screenWidth/5) ;
            titleHeight = screenWidth / 10;
            itemHeight = 2 * titleHeight;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            itemHelpHeight = 2 * titleHeight;
            pageWidth = 0;

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            pageMapPoiCount = pageCount / 2;
            pageBusinessPoiCount = pageCount;
            pageBusinessMapPoiCount = pageCount / 2;
            pagePoiHistoryCount = pageCount;
            pageMapPoiHistoryCount = pageCount / 2;
            pageHelpCount = (int) ((screenHeight - screenWidth * 0.15 - screenWidth * 0.1 - screenWidth * 0.1) / itemHelpHeight);
            pageHelpDetailCount = pageHelpCount + 1;
            itemHelpDetailHeight = pageHelpCount * itemHelpHeight / pageHelpDetailCount;
            if ((screenHeight - screenWidth * 0.25 - pageHelpCount * itemHelpHeight - itemHelpHeight) < 0){//剩余空间不够放下二维码
                pageHelpCount--;
                pageHelpDetailCount = pageHelpCount + 2;
            }
            pageMovieCount = 3;    //竖屏电影列表固定三个
            //pageMovieCount = (screenWidth * 3/4 - pageWidth - 4 * unit) / movieWidth;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount;
            pageCompetitionCount = pageCount;
        }else {
            if (screenHeight >= 480){
                pageCount = 4;
            }else if(screenHeight >= 384){
                pageCount = 3;
            }else {
                pageCount = 2;
            }
            int movieWidth = ViewParamsUtil.billWidth;
            titleHeight = 6 * unit;
            itemHeight = 12 * unit;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            pageWidth = 10 * unit;
            pageTextHeight = 8 * unit;
            pageButtonSize = 4 * unit;
            itemHelpHeight = 10 * unit;

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            pageMapPoiCount = pageCount / 2;
            pageBusinessPoiCount = pageCount;
            pageBusinessMapPoiCount = pageCount / 2;
            pagePoiHistoryCount = pageCount;
            pageMapPoiHistoryCount = pageCount / 2;
            pageHelpCount = pageCount;
            pageHelpDetailCount = pageCount + 1;
            itemHelpDetailHeight = pageHelpCount  * itemHelpHeight / pageHelpDetailCount;
            //pageMovieCount = 3;
            pageMovieCount = (double)(screenWidth * 3/4 - pageWidth - 4 * unit) / 3  >= movieWidth * 1.34 ?4:3;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount;
            pageCompetitionCount = pageCount;
        }
    }

    //半屏界面布局
    private void initHalf(){
        int unit = ViewParamsUtil.unit;
        if (WinLayout.isVertScreen){
            pageCount = (screenHeight - screenWidth/4 - screenWidth / 10) / (screenWidth/5) ;
            titleHeight = screenWidth / 10;
            itemHeight = 2 * titleHeight;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            itemHelpHeight = 2 * titleHeight;
            pageWidth = 0;

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            pageMapPoiCount = pageCount / 2;
            pageBusinessPoiCount = pageCount;
            pageBusinessMapPoiCount = pageCount / 2;
            pagePoiHistoryCount = pageCount;
            pageMapPoiHistoryCount = pageCount / 2;
            pageHelpCount = (int) ((screenHeight - screenWidth * 0.15 - screenWidth * 0.1 - screenWidth * 0.1) / itemHelpHeight);
            pageHelpDetailCount = pageHelpCount + 1;
            itemHelpDetailHeight = pageHelpCount  * itemHelpHeight / pageHelpDetailCount;
            if ((screenHeight - screenWidth * 0.25 - pageHelpCount * itemHelpHeight - itemHelpHeight) < 0){//剩余空间不够放下二维码
                pageHelpCount--;
                pageHelpDetailCount = pageHelpCount + 2;
            }
            pageMovieCount = 3;    //竖屏电影列表固定三个
            //pageMovieCount = (screenWidth * 3/4 - pageWidth - 4 * unit) / movieWidth;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount;
            pageCompetitionCount = pageCount;
        }else {
            if (screenHeight >= 480){
                pageCount = 4;
            }else if(screenHeight >= 400){
                pageCount = 3;
            }else {
                pageCount = 2;
            }
            int movieWidth = ViewParamsUtil.billWidthHalf;
            titleHeight = 6 * unit;
            itemHeight = 10 * unit;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            pageWidth = 10 * unit;
            pageTextHeight = 8 * unit;
            pageButtonSize = 4 * unit;
            itemHelpHeight = 10 * unit;

//            pageFlightCount = (screenWidth - pageWidth - 10 * unit) / (SizeConfig.pageCount * SizeConfig.itemHeight) + 1;
//            pageTrainCount = (screenWidth - pageWidth - 10 * unit) / (SizeConfig.pageCount * SizeConfig.itemHeight) + 1;
            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            pageMapPoiCount = pageCount;
            pageBusinessPoiCount = pageCount;
            pageBusinessMapPoiCount =  pageCount - 1;
            pagePoiHistoryCount = pageCount;
            pageMapPoiHistoryCount = pageCount;
            pageHelpCount = pageCount;
            pageHelpDetailCount = pageCount + 1;
            itemHelpDetailHeight = pageHelpCount  * itemHelpHeight / pageHelpDetailCount;
            //pageMovieCount = (screenWidth - pageWidth - 10 * unit) / movieWidth;
            pageMovieCount = (double)(screenWidth - pageWidth - 10 * unit) / 4  >= movieWidth * 1.34 ?5:4;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount;
            pageCompetitionCount = pageCount;
        }
    }

    //无屏界面布局
    private void initNone(){
        if (screenHeight >= 384){
            pageCount = 3;
        }else if(screenHeight >= 304){
            pageCount = 2;
        }else {
            pageCount = 2;
        }
        if (WinLayout.isVertScreen){
            int unit = (int) LayouUtil.getDimen("x375")/(2 * pageCount + 1);    //无屏内容高度固定
            int movieWidth = ViewParamsUtil.billWidthNone;
            titleHeight = unit;
            itemHeight = 2 * unit;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            itemHelpHeight = 2 * unit;
            pageWidth =  (int) LayouUtil.getDimen("x103");
            pageTextHeight = (int) LayouUtil.getDimen("x83");
            pageButtonSize = (int) LayouUtil.getDimen("x41");

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            //pageMapPoiCount = pageCount / 2;
            pageBusinessPoiCount = pageCount - 1;
            //pageBusinessMapPoiCount = pageCount / 2;
            pagePoiHistoryCount = pageCount;
            //pageMapPoiHistoryCount = pageCount;
            pageHelpCount = pageCount;
            pageHelpDetailCount = pageCount + 1;
            pageMovieCount = (int)(LayouUtil.getDimen("x666") - pageWidth - LayouUtil.getDimen("x25")) / movieWidth;
            //pageMovieCount = (screenWidth * 3/4 - pageWidth - 4 * unit) / movieWidth;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount - 1;
            pageCompetitionCount = pageCount -1;
        }else {
            int unit = ViewParamsUtil.unit;
            int movieWidth = ViewParamsUtil.billWidthNone;
            titleHeight = 6 * unit;
            itemHeight = 30/pageCount * unit;
            itemHeightPro = itemHeight * pageCount / (pageCount - 1);
            pageWidth = 10 * unit;
            pageTextHeight = 8 * unit;
            pageButtonSize = 4 * unit;
            itemHelpHeight = itemHeight;

            pageFlightCount = pageCount - 1;
            pageTrainCount = pageCount - 1;
            pagePoiCount = pageCount;
            //pageMapPoiCount = pageCount / 2;
            pageBusinessPoiCount = pageCount - 1;
            //pageBusinessMapPoiCount = pageCount / 2;
            pagePoiHistoryCount = pageCount;
            //pageMapPoiHistoryCount = pageCount;
            pageHelpCount = pageCount;
            pageHelpDetailCount = pageCount + 1;
            //pageMovieCount = 3;
            pageMovieCount = (64 * unit - pageWidth - 2 * unit) / movieWidth;
            pageAudioCount = pageCount;
            pageAudioTagCount = pageCount - 1;
            pageCompetitionCount = pageCount -1;
        }
    }

}

