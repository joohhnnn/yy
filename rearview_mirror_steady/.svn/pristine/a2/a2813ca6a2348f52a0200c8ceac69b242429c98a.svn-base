package com.txznet.txz.module.advertising;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.TextUtils;
import android.view.View;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txz.push_manager.PushManager;
import com.txz.report_manager.ReportManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txz.ui.map.UiMap;
import com.txznet.advertising.base.IBackgroundAdvertisingTool;
import com.txznet.advertising.base.IBannerAdvertisingTool;
import com.txznet.advertising.base.IOpenAdvertisingTool;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.advertising.BackgroundAdvertising;
import com.txznet.txz.component.advertising.BannerAdvertising;
import com.txznet.txz.component.advertising.OpenAdvertising;
import com.txznet.txz.component.advertising.util.AdvertisingUtils;
import com.txznet.txz.component.advertising.view.OpenAdvertisingView;
import com.txznet.txz.component.home.HomeControlManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.call.CallManager;
import com.txznet.txz.module.download.DownloadManager;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.music.focus.MusicFocusManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.news.NewsManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.plugin.PluginManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.DeviceInfo;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.runnables.Runnable2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class AdvertisingManager extends IModule {
    private static AdvertisingManager sInstance = new AdvertisingManager();
    private int mReqAdTimeCount = 0;//重试开屏广告次数
    private int mReqAdType = 0; //请求广告类型，用来判断当前最新请求的广告类型
    private static final int OPEN_AD_TYPE = 1;
    private static final int BANNER_AD_TYPE = 2;
    private static final int BACKGROUND_AD_TYPE = 3;
    private static final int NOW_TIME_AD_TYPE = 4;
    private static final int TTS_TYPE = 4;
    private static final int TTS_TEXT_TYPE = 1;//tts文本类型，可以直接播放
    private static final int TTS_AUDIO_TYPE = 2;//tts音频类型，需要下载
    private static final String FILE_URL_PREFIX = "/sdcard/txz/cache/download/";//广告下载后的目录
    private static final String FILE_NEW_URL_PREFIX = "/sdcard/txz/cache/advertising/";//广告存放目录
    private static final String ACTION_REPORT_START = "action_start";//广告开始展示
    private static final String ACTION_REPORT_SKIP = "action_skip";//触发跳过
    private static final String ACTION_REPORT_FINISH = "action_finish";//广告展示完成
    private static final String ACTION_REPORT_REDIRECT = "redirect";//广告跳转其它页面
	private static final String DOWNLOAD_AD_PARAM = "download.ad";//广告标识
    private static final String ACTION_NOW_TIME = "action_now_time_test";//整点报时广播
    //后台返回的广告数据，暂未展示
    private List<JSONBuilder> mAdvertisingList = new ArrayList<JSONBuilder>();
    //banner广告需要显示的
    private List<JSONBuilder> mAdvertisingBannerList = new ArrayList<JSONBuilder>();
    //整点广告数据
    private JSONBuilder mNowTimeAdData;
    //保存下载任务的开始时间
    Map<String, Long> mDownloadTimeMap = new HashMap<String, Long>();

    private IOpenAdvertisingTool mOpenAdImpl = OpenAdvertising.getInstance();
    private IBannerAdvertisingTool mBannerAdImpl = BannerAdvertising.getInstance();
    private IBackgroundAdvertisingTool mBackgroundAdImpl = BackgroundAdvertising.getInstance();

    private int mSpeechTaskId = 0;

    //开机配置
    JSONObject mOpenAdConfig;
    JSONObject mBannerAdConfig;
    JSONObject mBackgroundAdConfig;
    JSONObject mNowTimeAdConfig;
    private int mBannerInterval = 0;//banner广告间隔的声控次数
    private int mBackgroundInterval = 0;//背景广告间隔的声控次数

    private boolean isFirst = false;//是否首次进入声控界面
    private boolean mCanShowBanner = false;//是否能展示开屏广告
    private int mCurrentShowAd = 0;//当前展示的广告
    private int mBgRepeatNum = 0;//当前背景广告需要上报的次数

    private AlarmManager mAlarmManager;
    private int mAlarmId = -1;//设置不同ID，相同id多个任务会被替换掉
    private int mTestReceiverId = -1;
    private static final int TIMING = 2 * 60 * 1000;
    private static final long DAY = 24 * 60 * 60 * 1000;

    private AdvertisingManager() {
        RecorderWin.OBSERVABLE.registerObserver(mObserver);
    }

    public static AdvertisingManager getInstance() {
        return sInstance;
    }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_NOTIFY_UPDATE_AD_CONFIG);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_GET_AD);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_AD_REPORT);
        regEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterInitSuccess() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_NOW_TIME);
        GlobalContext.get().registerReceiver(mNowTimeReceiver, intentFilter);
//        if(mNowTimeAdConfig == null){
//            initNowTimeConfig();
//        }
//        try {
//            if(mNowTimeAdConfig != null || mNowTimeAdConfig.getInt("open") == 1){
//                doNowTimeTask();
//            }
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
        return super.initialize_AfterInitSuccess();
    }

    @Override
    public int initialize_addPluginCommandProcessor() {
        PluginManager.addCommandProcessor("txz.advertising.", new PluginManager.CommandProcessor() {
            @Override
            public Object invoke(String command, Object[] args) {
                LogUtil.d("advertising plugin command:" + command);
                if ("openAdImpl".equals(command)) {
                    if (args[0] instanceof IOpenAdvertisingTool) {
                        mOpenAdImpl = (IOpenAdvertisingTool) args[0];
                    }
                } else if ("bannerAdImpl".equals(command)) {
                    if (args[0] instanceof IBannerAdvertisingTool) {
                        mBannerAdImpl = (IBannerAdvertisingTool) args[0];
                    }
                } else if ("backgroundAdImpl".equals(command)) {
                    if (args[0] instanceof IBackgroundAdvertisingTool) {
                        mBackgroundAdImpl = (IBackgroundAdvertisingTool) args[0];
                    }
                } else if ("releaseAudioFocus".equals(command)) {
                    MusicFocusManager.getInstance().releaseAudioFocusImmediately();
                } else if ("setBannerAdvertisingView".equals(command)) {
                    if (args[0] instanceof View) {
                        WinManager.getInstance().setBannerAdvertisingView((View) args[0]);
                    }
                } else if ("removeBannerAdvertisingView".equals(command)) {
                    WinManager.getInstance().removeBannerAdvertisingView();
                } else if ("setBackground".equals(command)) {
                    if (args == null) {
                        WinManager.getInstance().setBackground(null);
                    } else {
                        if (args[0] instanceof Drawable) {
                            WinManager.getInstance().setBackground((Drawable) args[0]);
                        }
                    }
                }
                return null;
            }
        });
        return super.initialize_addPluginCommandProcessor();
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        if (UiEvent.EVENT_ACTION_EQUIPMENT == eventId) {
            switch (subEventId) {
                case UiEquipment.SUBEVENT_NOTIFY_UPDATE_AD_CONFIG://开机下发配置项
                    PushManager.PushCmd_AdConfig adConfig = null;
                    LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG");
                    try {
                        adConfig = PushManager.PushCmd_AdConfig.parseFrom(data);
                        JSONBuilder jsonBuilder = new JSONBuilder(adConfig.jsonAdConfig);
                        try {
                            mOpenAdConfig = jsonBuilder.getJSONObject().getJSONObject("1");//开屏广告配置
                            mBannerAdConfig = jsonBuilder.getJSONObject().getJSONObject("2");//banner广告配置
                            mBackgroundAdConfig = jsonBuilder.getJSONObject().getJSONObject("3");//背景广告配置
                            mNowTimeAdConfig = jsonBuilder.getJSONObject().getJSONObject("4");//整点报时广告配置
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG mOpenAdConfig:" + mOpenAdConfig.toString());
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG mBannerAdConfig:" + mBannerAdConfig.toString());
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG mBackgroundAdConfig:" + mBackgroundAdConfig.toString());
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG:" + jsonBuilder.toString());
                            //下发配置成功后判断是否可以显示开屏广告
                            if (isShowOpenAdvertising() && mOpenAdImpl.isSupportShow()) {
                                reqAdvertising(OPEN_AD_TYPE, mOpenAdImpl.getWidth(), mOpenAdImpl.getHeight());
                            }
                            if (mNowTimeAdConfig != null && mNowTimeAdConfig.getInt("open") == 1) {
                                mAlarmId = -1;
                                doNowTimeTask();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } catch (InvalidProtocolBufferNanoException e) {
                        e.printStackTrace();
                    }
                    break;
                case UiEquipment.SUBEVENT_RESP_GET_AD:
                    //请求资源发生错误，判断是否需要重试
                    if (data == null || data.length == 0) {
                        LogUtil.d("SUBEVENT_RESP_GET_AD data is null,type:" + mReqAdType);
                        switch (mReqAdType) {
                            case OPEN_AD_TYPE:
                                try {
                                    mReqAdTimeCount++;
                                    if (mReqAdTimeCount > mOpenAdConfig.getInt("error_repeat_num")) {//并且重试次数不大于10次error_repeat_num
                                        mReqAdTimeCount = 0;
                                        return 0;
                                    }
                                    LogUtil.d("open ad retry.");
                                    AppLogic.runOnBackGround(new Runnable() {
                                        @Override
                                        public void run() {
                                            LogUtil.d("open ad retry start.");
                                            LogUtil.d("open ad repeat_num:" + mReqAdTimeCount);
                                            reqAdvertising(OPEN_AD_TYPE, mOpenAdImpl.getWidth(), mOpenAdImpl.getHeight());
                                        }
                                    }, mOpenAdConfig.getInt("error_time_interval") * 1000);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case BANNER_AD_TYPE:
                                try {
                                    mReqAdTimeCount++;
                                    if (mReqAdTimeCount > mBannerAdConfig.getInt("error_repeat_num")) {//并且重试次数不大于10次error_repeat_num
                                        if (mBannerAdImpl.isShowing()) {
                                            mBannerAdImpl.dismiss();
                                        }
                                        mReqAdTimeCount = 0;
                                        return 0;
                                    }
                                    LogUtil.d("banner ad retry.");
                                    AppLogic.runOnBackGround(new Runnable() {
                                        @Override
                                        public void run() {
                                            LogUtil.d("banner ad retry start.");
                                            LogUtil.d("banner ad repeat_num:" + mReqAdTimeCount);
                                            reqAdvertising(BANNER_AD_TYPE, mOpenAdImpl.getWidth(), mOpenAdImpl.getHeight());
                                        }
                                    }, mBannerAdConfig.getInt("error_time_interval") * 1000);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            default:
                                break;
                        }
                        return 0;
                    }
                    UiEquipment.Resp_Ad resp_ad = null;
                    try {
                        resp_ad = UiEquipment.Resp_Ad.parseFrom(data);
                        JSONBuilder jsonBuilder = new JSONBuilder(resp_ad.respJsonData);
                        LogUtil.d("SUBEVENT_RESP_GET_AD:" + jsonBuilder.toString());
                        mAdvertisingList.add(jsonBuilder);
                        int type = jsonBuilder.getVal("ad_type", Integer.class, 0);
                        String url = jsonBuilder.getVal("source_url", String.class);
                        switch (type) {
                            case OPEN_AD_TYPE:
                                String ttsUrl = jsonBuilder.getVal("tts_url", String.class);
                                LogUtil.d("open ad start load.");
                                jsonBuilder.put("start_load", System.currentTimeMillis());//设置开始加载时间
                                if (TextUtils.isEmpty(ttsUrl)) {//判断是否有音频资源，无则直接检查广告图片等资源
                                    if (!checkFileExists(url)) {
                                        LogUtil.d("open ad start download file");
                                        downloadFile(url);
                                    } else {
                                        LogUtil.d("open ad url had cache.");
                                        checkShowOpenAdvertising(jsonBuilder);
                                    }
                                } else {
                                    boolean isShow = true;
                                    if (!TextUtils.isEmpty(url) && !checkFileExists(url)) {
                                        isShow = false;
                                        downloadFile(url);
                                    }
                                    if (!TextUtils.isEmpty(ttsUrl) && !checkFileExists(ttsUrl)) {
                                        isShow = false;
                                        downloadFile(ttsUrl);
                                    }
                                    if (isShow) {
                                        LogUtil.d("open ad url、ttsUrl had cache.");
                                        checkShowOpenAdvertising(jsonBuilder);
                                    }
                                }
                                break;
                            case BANNER_AD_TYPE:
                                if (!mCanShowBanner) {
                                    LogUtil.d("banner ad response is out of date.");
                                    return 0;
                                }
                                try {
                                    JSONArray bannerAds = jsonBuilder.getJSONObject().getJSONArray("list");
                                    for (int i = 0; i < bannerAds.length(); i++) {
                                        JSONBuilder bannerData = new JSONBuilder(bannerAds.get(i).toString());
                                        url = bannerData.getVal("source_url", String.class);
                                        if (!checkFileExists(url)) {
                                            DownloadManager.getInstance().registerDownloadTaskStatusChangeListener(MD5Util.generateMD5(url), mProgressChangeListener);
                                            downloadFile(url);
                                        } else {
                                            LogUtil.d("banner ad url had cache.load end.");
                                            LogUtil.d("banner ad data size:" + mAdvertisingBannerList.size());
                                            mAdvertisingBannerList.add(bannerData);
                                            if(i == 0 || !mBannerAdImpl.isShowing()){//i=0代表新一组轮播广告，如果banner没显示则也展示
                                                checkShowBannerAdvertising(bannerData);
                                            }
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;
                            case BACKGROUND_AD_TYPE:
                                LogUtil.d("background ad start load.");
                                jsonBuilder.put("start_load", System.currentTimeMillis());//设置开始加载时间
                                if (!checkFileExists(url)) {
                                    downloadFile(url);
                                } else {
                                    LogUtil.d("background ad had cache.");
                                    showBackgroundAdvertising(jsonBuilder);
                                }
                                break;
                            case NOW_TIME_AD_TYPE:
                                mNowTimeAdData = jsonBuilder;
                                String noticeUrl = mNowTimeAdData.getVal("notice_url",String.class,"");
                                if(!TextUtils.isEmpty(noticeUrl) && !checkFileExists(mNowTimeAdData.getVal("notice_url",String.class))){
                                    downloadFile(noticeUrl);
                                }
                                if(mNowTimeAdData.getVal("tts_type",Integer.class) == TTS_AUDIO_TYPE){
                                    String nowTimeTtsUrl = mNowTimeAdData.getVal("tts_url",String.class);
                                    if(!checkFileExists(nowTimeTtsUrl)){
                                        downloadFile(nowTimeTtsUrl);
                                    }
                                }
                            default:
                                break;
                        }
                    } catch (InvalidProtocolBufferNanoException e) {
                        e.printStackTrace();
                    }
                    break;
                case UiEquipment.SUBEVENT_RESP_AD_REPORT:
                    break;
            }
        } else if (UiEvent.EVENT_INNER_NET == eventId) {
            switch (subEventId) {
                case UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP:
                    if (data == null || data.length == 0) {
                        LogUtil.d("download file is fail!");
                        break;
                    }
                    try {
                        UiInnerNet.DownloadHttpFileTask task = UiInnerNet.DownloadHttpFileTask.parseFrom(data);
                        //不是广告的下载直接结束
                        if(!DOWNLOAD_AD_PARAM.equals(task.strDefineParam)){
                            break;
                        }

                        if (task.int32ResultCode != UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS) {
                            LogUtil.d("download file is fail!error code:" + task.int32ResultCode + ",taskId :" + task.strTaskId);
                            break;
                        }
                        LogUtil.d("download file is success!taskId:" + task.strTaskId);
                        DownloadManager.getInstance().unregisterDownloadTaskStatusChangeListener(task.strTaskId);
                        //把下载的广告资源拷贝到advertising目录下
                        String oldPath = Environment.getExternalStorageDirectory().getPath() + "/txz/cache/download/";
                        String newPath = Environment.getExternalStorageDirectory().getPath() + "/txz/cache/advertising/";
                        FileUtil.copyFile(oldPath + task.strTaskId, newPath + task.strTaskId);
                        //删除download目录下的广告资源
                        File oldFile = new File(FILE_URL_PREFIX + task.strTaskId);
                        oldFile.delete();
                        //如果是整点报时，直接结束
                        if (mNowTimeAdData != null ) {
                            if (task.strUrl.equals(mNowTimeAdData.getVal("tts_url", String.class, ""))) {
                                break;
                            }
                            if (task.strUrl.equals(mNowTimeAdData.getVal("notice_url", String.class, ""))) {
                                break;
                            }
                        }
                        //资源下载成功后判断是否应该展示
                        checkAdvertisingShow(task.strTaskId);
                    } catch (InvalidProtocolBufferNanoException e) {
                        e.printStackTrace();
                    }
                    break;
                default:

                    break;
            }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    DownloadManager.DownloadTaskProgressChangeListener mProgressChangeListener = new DownloadManager.DownloadTaskProgressChangeListener() {
        @Override
        public void onProgressChange(UiInnerNet.DownloadHttpFileTask task) {
            LogUtil.d("ad Listener Progress:" + task.uint32DlProgress);
            if(mDownloadTimeMap.containsKey(task.strTaskId)){
                return;
            }
            mDownloadTimeMap.put(task.strTaskId,System.currentTimeMillis());
        }
    };

    //窗口监听器
    RecorderWin.StatusObervable.StatusObserver mObserver = new RecorderWin.StatusObervable.StatusObserver() {
        @Override
        public void onShow() {
            isFirst = true;
            if (mOpenAdImpl.isShowing()) {
                closeOpenAd();
            }

            if (mBannerInterval >= 0) {
                mBannerInterval--;
            }
            if (mBackgroundInterval >= 0) {
                mBackgroundInterval--;
            }

            if (isShowBackgroundAdvertising() && mBackgroundAdImpl.isSupportShow()) {
                reqAdvertising(BACKGROUND_AD_TYPE, mBackgroundAdImpl.getWidth(), mBackgroundAdImpl.getWidth());
            }
        }

        @Override
        public void onDismiss() {
            if (mBackgroundAdImpl.isShowing()) {
                mBackgroundAdImpl.dismiss();
                for (JSONBuilder data : mAdvertisingList) {
                    if (data.getVal("ad_type", Integer.class) == BACKGROUND_AD_TYPE) {
                        reportAction(data, ACTION_REPORT_SKIP);
                    }
                }
            }
            if (mBannerAdImpl.isShowing()) {
                closeBannerAd();
            }
            mAdvertisingBannerList.clear();
            mAdvertisingList.clear();
            AppLogic.removeBackGroundCallback(mBannerShowTask);
            mReqAdTimeCount = 0;
            mDownloadTimeMap.clear();
        }
    };

    /**
     * 请求广告资源
     *
     * @param type   广告类型
     * @param width  广告位所占宽度
     * @param height 广告位所占高度
     */
    public void reqAdvertising(int type, int width, int height) {
        mReqAdType = type;
        UiMap.LocationInfo locationInfo = LocationManager.getInstance().getLastLocation();
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("ad_type", type);
        if (locationInfo != null && locationInfo.msgGpsInfo != null) {
            jsonBuilder.put("lng", locationInfo.msgGpsInfo.dblLng);
            jsonBuilder.put("lat", locationInfo.msgGpsInfo.dblLat);
        }
        String userAgent = AdvertisingSp.getInstance().getUserAgent();
        if (TextUtils.isEmpty(userAgent)) {
            try {
                userAgent = DeviceInfo.getDefaultUserAgent(GlobalContext.get());
            }catch (Exception e){
                e.printStackTrace();
            }
        }
        jsonBuilder.put("user_agent", userAgent);
        jsonBuilder.put("ad_width", width);
        jsonBuilder.put("ad_height", height);
        jsonBuilder.put("network_type", NetworkManager.getInstance().getNetType());
        UiEquipment.Req_Ad req_ad = new UiEquipment.Req_Ad();
        req_ad.reqJsonData = jsonBuilder.toBytes();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_AD, req_ad);

    }

    /**
     * 根据url下载文件
     *
     * @param url 资源链接
     * @return
     */
    private String downloadFile(String url) {
        //如果sd卡内存小于200M或者广告目录文件占用内存大于200M    则清空广告目录下文件
        if (AdvertisingUtils.getSDFreeSize() < 200 || AdvertisingUtils.getCacheSize() > 200) {
            AdvertisingUtils.freeCacheFiles(new File(FILE_NEW_URL_PREFIX));
        }
        String taskId = MD5Util.generateMD5(url);
        UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
        task.strUrl = url;
        task.strTaskId = taskId;
        task.strDefineParam = DOWNLOAD_AD_PARAM;
        task.bForbidUseReservedSpace = true; // 禁止使用预留空间
        JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
        return taskId;
    }


    /**
     * 检查广告是否已经下载过
     *
     * @param url
     * @return
     */
    private boolean checkFileExists(String url) {
        File file = new File(FILE_NEW_URL_PREFIX + MD5Util.generateMD5(url));
        return file.exists();
    }

    /**
     * 根据传入的taskI判断是否满足展示广告的条件
     *
     * @param taskId 广告资源的名称
     */
    private void checkAdvertisingShow(String taskId) {
        for (JSONBuilder advertising : mAdvertisingList) {
            LogUtil.d("mAdvertisingList data:" + advertising.toString());
            int adType = advertising.getVal("ad_type", Integer.class);
            String url = advertising.getVal("source_url", String.class);
            String ttsUrl = advertising.getVal("tts_url", String.class);
            switch (adType) {
                case OPEN_AD_TYPE:
                    if (!taskId.equals(MD5Util.generateMD5(url)) && !taskId.equals(MD5Util.generateMD5(ttsUrl))) {
                        LogUtil.d("open ad task id error.");
                        break;
                    }
                    if (!TextUtils.isEmpty(url) && !checkFileExists(url)) {//如果图片、gif、视频资源不存在则返回
                        LogUtil.d("open ad url is null or file no exist");
                        break;
                    }
                    if (!TextUtils.isEmpty(ttsUrl) && !checkFileExists(ttsUrl)) {//如果tts_url不为空（空则表示不需下载，播放文本），且资源未下载则返回
                        LogUtil.d("open ad ttsUrl is null or file no exist");
                        break;
                    }
                    checkShowOpenAdvertising(advertising);
                    break;
                case BANNER_AD_TYPE:
                    JSONArray bannerAds = null;
                    try {
                        bannerAds = advertising.getJSONObject().getJSONArray("list");
                        for (int i = 0; i < bannerAds.length(); i++) {
                            JSONBuilder bannerData = new JSONBuilder(bannerAds.get(i).toString());
                            url = bannerData.getVal("source_url", String.class);
                            if (taskId.equals(MD5Util.generateMD5(url))) {
                                long startTime = 0;
                                if(mDownloadTimeMap.containsKey(taskId)){
                                    startTime = mDownloadTimeMap.get(taskId);
                                }
                                long time = System.currentTimeMillis() - startTime;
                                LogUtil.d("banner ad load end.time:" + time + "ms");
                                LogUtil.d("banner ad json" + bannerData.toString());
                                if (time > mBannerAdConfig.getInt("error_over_time") * 1000) {
                                    LogUtil.d("banner ad load timeout.");
                                    return;
                                }
                                mAdvertisingBannerList.add(bannerData);
                                if(i == 0 || !mBannerAdImpl.isShowing()){//i=0代表新一组轮播广告，如果banner没显示则也展示
                                    checkShowBannerAdvertising(bannerData);
                                }
                                break;
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case BACKGROUND_AD_TYPE:
                    if (!taskId.equals(MD5Util.generateMD5(url))) {
                        break;
                    }
                    if (!checkFileExists(url)) {
                        break;
                    }
                    showBackgroundAdvertising(advertising);
                    break;

            }
        }
    }

    /**
     * 展示开屏广告
     *
     * @param data 请求后台返回的广告内容
     */
    private void checkShowOpenAdvertising(final JSONBuilder data) {
        long time = System.currentTimeMillis() - data.getVal("start_load", Long.class, 0L);
        LogUtil.d("open ad loaded end.spend time:" + time + "ms");
        try {
            if (time > mOpenAdConfig.getInt("error_over_time") * 1000) {//秒转毫秒
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!mOpenAdImpl.isShowing() && mOpenAdImpl.isSupportShow()) {
           TtsManager.getInstance().speakText("", new TtsUtil.ITtsCallback() {
               @Override
               public void onSuccess() {
                   LogUtil.d("open ad tts onSuccess.");
                   showOpenAdvertising(data);
               }

               @Override
               public void onEnd() {
                   LogUtil.d("open ad tts onEnd.");
               }

               @Override
               public void onCancel() {
                   LogUtil.d("open ad tts onCancel.");
               }
           });
        }
    }

    private void showOpenAdvertising(final JSONBuilder data){
        if(RecorderWin.isOpened()){
            LogUtil.d("open ad show fail,recorder win is open.");
            return;
        }
        if (!CallManager.getInstance().isIdle()) {
            LogUtil.d("open ad cant show,because calling.");
            return;
        }
        String url = FILE_NEW_URL_PREFIX + MD5Util.generateMD5(data.getVal("source_url", String.class));
        mOpenAdImpl.setUrl(url);
        mOpenAdImpl.setType(data.getVal("source_type", Integer.class));
        mOpenAdImpl.setRedirectUrl(data.getVal("redirect_url", String.class));
        mOpenAdImpl.setCloseBtnText(data.getVal("close_word", String.class));
        //判断tts类型
        TtsUtil.ITtsCallback callback = new TtsUtil.ITtsCallback() {
            @Override
            public void onBegin() {
                mOpenAdImpl.show();
            }
        };
        if (data.getVal("tts_type", Integer.class, 0) == TTS_AUDIO_TYPE) {
            String voiceUrl = FILE_NEW_URL_PREFIX + MD5Util.generateMD5(data.getVal("tts_url", String.class));
            mSpeechTaskId = TtsManager.getInstance().speakVoice(voiceUrl, callback);
        } else {
            String text = data.getVal("tts", String.class);
            mSpeechTaskId = TtsManager.getInstance().speakText(text, callback);
        }
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                if (mOpenAdImpl.isShowing()) {
                    mOpenAdImpl.dismiss();
                    reportAction(data, ACTION_REPORT_FINISH);
                    mAdvertisingList.remove(data);
                }
            }
        }, data.getVal("show_time", Integer.class) * 1000);//后台下发单位是秒

        //开始展示上报
        reportAction(data, ACTION_REPORT_START);
        //记录打开开屏广告次数
        AdvertisingSp sp = AdvertisingSp.getInstance();
        sp.setOpenShowNum(sp.getOpenShowNum() + 1);
        //记录打开广告时间
        sp.setOpenLastTime(System.currentTimeMillis());
    }



    /**
     * 检查是否要显示广告内容
     *
     * @param data 请求后台返回的广告内容
     */
    private void checkShowBannerAdvertising(final JSONBuilder data) {
        if (!mBannerAdImpl.isShowing() && RecorderWin.isOpened() && mCanShowBanner) {
            showBannerAdvertising(data);
            reportAction(data, ACTION_REPORT_START);//开始展示上报
            AdvertisingSp sp = AdvertisingSp.getInstance();
            sp.setBannerShowNum(sp.getBannerShowNum() + 1);
            LogUtil.d("banner ad data size:" + mAdvertisingBannerList.size());
            AppLogic.removeBackGroundCallback(mBannerShowTask);
            AppLogic.runOnBackGround(mBannerShowTask, data.getVal("show_time", Integer.class) * 1000);
            try {
                //后台下发的间隔数
                int start = mBannerAdConfig.getInt("awake_range_start");
                int end = mBannerAdConfig.getInt("awake_range_end");
                //获取随机间隔数
                mBannerInterval = randomInterval(start, end);
                LogUtil.d("banner ad random mBannerInterval:" + mBannerInterval);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{
            showBannerAdvertising(data);
            reportAction(data, ACTION_REPORT_START);//开始展示上报
            AppLogic.removeBackGroundCallback(mBannerShowTask);
            AppLogic.runOnBackGround(mBannerShowTask, data.getVal("show_time", Integer.class) * 1000);
        }
    }

    private Runnable mBannerShowTask = new Runnable() {
        @Override
        public void run() {
            //展示完成上报
            if (mAdvertisingBannerList.size() != 0) {
                reportAction(mAdvertisingBannerList.get(mCurrentShowAd), ACTION_REPORT_FINISH);
                mAdvertisingBannerList.remove(mCurrentShowAd);
            }

            if (mAdvertisingBannerList.size() != 0) {
                JSONBuilder data = mAdvertisingBannerList.get(mCurrentShowAd);
                showBannerAdvertising(data);//5s后显示下一个成功下载的banner广告
                reportAction(data, ACTION_REPORT_START);//开始展示上报
                AppLogic.runOnBackGround(mBannerShowTask, data.getVal("show_time", Integer.class) * 1000);
                LogUtil.d("banner ad data size:" + mAdvertisingBannerList.size());
                if (mAdvertisingBannerList.size() == 0) {
                    reqAdvertising(BANNER_AD_TYPE, mBannerAdImpl.getWidth(), mBannerAdImpl.getHeight());
                }
            } else {
                LogUtil.d("banner ad data is null need req.");
                reqAdvertising(BANNER_AD_TYPE, mBannerAdImpl.getWidth(), mBannerAdImpl.getHeight());
            }
        }
    };

    public void showBannerAdvertising(JSONBuilder data) {
        String url = FILE_NEW_URL_PREFIX + MD5Util.generateMD5(data.getVal("source_url", String.class));
        mBannerAdImpl.setUrl(url);
        mBannerAdImpl.setType(data.getVal("source_type", Integer.class, OpenAdvertisingView.SOURCE_IMAGE_TYPE));
        LogUtil.d("banner ad url :" + url);
        mBannerAdImpl.show();
    }

    /**
     * 展示背景广告
     *
     * @param data 请求后台返回的广告内容
     */
    private void showBackgroundAdvertising(final JSONBuilder data) {
        long time = System.currentTimeMillis() - data.getVal("start_load", Long.class, 0L);
        LogUtil.d("background ad loaded end.spend time:" + time + "ms");
        try {
            if (time > mBackgroundAdConfig.getInt("error_over_time") * 1000) {
                return;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if (!mBackgroundAdImpl.isShowing() && RecorderWin.isOpened()) {
            String url = FILE_NEW_URL_PREFIX + MD5Util.generateMD5(data.getVal("source_url", String.class));
            mBackgroundAdImpl.setUrl(url);
            mBackgroundAdImpl.setOpacity(data.getVal("opacity", Float.class, 75F));
            mBackgroundAdImpl.show();
            //广告开始展示上报
            reportAction(data, ACTION_REPORT_START);
            //记录展示次数
            AdvertisingSp sp = AdvertisingSp.getInstance();
            sp.setBackgroundShowNum(sp.getBackgroundShowNum() + 1);
            //广告展示完成上报
            long showTime = 0;
            try {
                showTime = data.getJSONObject().getJSONObject("report_info").getInt("bg_rep_int") * 1000;//广告上报间隔，秒转毫秒
                mBgRepeatNum = data.getJSONObject().getJSONObject("report_info").getInt("bg_rep_num");//广告需要上报的次数
                LogUtil.d("background ad showTime:" + showTime + ",mBgRepeatNum:" + mBgRepeatNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mBgRepeatNum != 0) {
                mBackgroundTask.update(data, showTime);
                AppLogic.removeBackGroundCallback(mBackgroundTask);
                AppLogic.runOnBackGround(mBackgroundTask, showTime);
            }
            //后台下发的间隔数
            try {
                int start = mBackgroundAdConfig.getInt("awake_range_start");
                int end = mBackgroundAdConfig.getInt("awake_range_end");
                //获取随机间隔数
                mBackgroundInterval = randomInterval(start, end);
                LogUtil.d("background ad random mBackgroundInterval:" + mBackgroundInterval);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    Runnable2<JSONBuilder,Long> mBackgroundTask = new Runnable2<JSONBuilder,Long>(null,null) {
        @Override
        public void run() {
            if (mBackgroundAdImpl.isShowing()) {
                reportAction(mP1, ACTION_REPORT_FINISH);//展示完成一轮上报
                if (mBgRepeatNum > 0) {
                    mBgRepeatNum--;
                }
                if (mBgRepeatNum == 0) {
                    return;
                }
                AppLogic.runOnBackGround(mBackgroundTask, mP2);
            }
        }
    };

    /**
     * 播报音频文件
     *
     * @param url 音频资源路径
     */
    private int speakVoice(String url) {
        return TtsManager.getInstance().speakVoice(url);
    }

    /**
     * 播报文本内容
     *
     * @param text
     */
    private int speakText(String text) {
        return TtsManager.getInstance().speakText(text);
    }

    /**
     * 跳过开屏广告
     */
    public void closeOpenAd() {
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mOpenAdImpl.dismiss();
        for (JSONBuilder data : mAdvertisingList) {
            if (data.getVal("ad_type", Integer.class, 0) == OPEN_AD_TYPE) {
                reportAction(data, ACTION_REPORT_SKIP);
                mAdvertisingList.remove(data);
            }
        }
    }

    /**
     * 跳过banner广告
     */
    public void closeBannerAd() {
        mCanShowBanner = false;
        mBannerAdImpl.dismiss();
        if (mAdvertisingBannerList.size() == 0) {
            LogUtil.d("mAdvertisingBannerList size:0");
            return;
        }
        JSONBuilder data = mAdvertisingBannerList.get(mCurrentShowAd);
        if (data != null) {
            reportAction(data, ACTION_REPORT_SKIP);//跳过上报
        }
    }

    public void clearAdvertising(){
        if(mBannerAdImpl.isShowing()){
            closeBannerAd();
        }
    }

    /**
     * 关闭开屏广告
     */
    public void clearOpenAdvertising() {
        if (mOpenAdImpl.isShowing()) {
            closeOpenAd();
        }
    }

    public boolean isShowOpenAdvertising() {
        AdvertisingSp sp = AdvertisingSp.getInstance();
        try {
            if (mOpenAdConfig == null) {
                LogUtil.d("open ad Config is null.");
                return false;
            }
            int num = sp.getOpenShowNum();//本地记录的展示次数
            if (num >= mOpenAdConfig.optInt("show_num")) {
                LogUtil.d("open ad already can't show");
                return false;
            }
            if (System.currentTimeMillis() - sp.getOpenLastTime() < mOpenAdConfig.optInt("show_time_interval") * 60 * 1000) {//后台下发的广告间隔是分钟
                LogUtil.d("open ad interval time insufficient can't show");
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isShowBannerAdvertising() {
        AdvertisingSp sp = AdvertisingSp.getInstance();
        try {
            if (mBannerAdConfig == null) {
                LogUtil.d("banner ad config is null.");
                return false;
            }
            int num = sp.getBannerShowNum();//本地记录的展示次数
            if (num >= mBannerAdConfig.optInt("show_num")) {
                LogUtil.d("banner ad already can't show");
                return false;
            }

            if (mBannerInterval >= 0) {
                LogUtil.d("banner ad interval time insufficient can't show,mBannerInterval:" + mBannerInterval);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean isShowBackgroundAdvertising() {
        AdvertisingSp sp = AdvertisingSp.getInstance();
        try {
            if (mBackgroundAdConfig == null) {
                LogUtil.d("background ad config is null.");
                return false;
            }

            int num = sp.getBackgroundShowNum();//本地记录的展示次数
            if (num >= mBackgroundAdConfig.optInt("show_num")) {
                LogUtil.d("background ad already can't show");
                return false;
            }
            if (mBackgroundInterval >= 0) {
                LogUtil.d("background ad interval time insufficient can't show,mBackgroundInterval:" + mBackgroundInterval);
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    public boolean handle(com.alibaba.fastjson.JSONObject data,int score) {
        CheckIsNeedCloseAdvertising(data);//检查一轮场景之后的场景是否需要退出广告
        if (!isFirst) {//不是首次进入声控则不处理
            return false;
        }
        isFirst = false;
        //是否可以显示广告
        if (mBackgroundAdImpl.isShowing()) {
            return false;
        }
        if (!mBannerAdImpl.isSupportShow()) {
            return false;
        }

        if (!isShowBannerAdvertising()) {
            return false;
        }
        //根据当前语义场景判断是否可以展示广告
        String scene = data.getString("scene");
        boolean isShow = false;
        if ("unknown".equals(scene) || "empty".equals(scene)) {//闲聊、empty场景
            String answer = data.getString("answer");
            if (score < 90) {
                if (!TextUtils.isEmpty(answer)) {
                    if (answer.length() > 40
                            && !(TextUtils.equals(data.getString("style"), "joke")
                            || TextUtils.equals(data.getString("style"), "cookbook")
                            || TextUtils.equals(data.getString("style"), "poem"))) {
                        int index = answer.indexOf("。");//最后也会截取句号
                        if (index < 40) {
                            answer = answer.substring(0, index + 1);
                        }
                    }
                }
            }
            if (TextUtils.isEmpty(answer) || answer.length() <= 40) {
                isShow = true;
            }
        }
        if ("command".equals(scene)) {
            String action = data.getString("action");
            if ("exeu".equals(action)) {//日期查询
                isShow = true;
            }
        }

        if (isShow) {
            //请求banner广告
            LogUtil.d("banner ad is send request.");
            mCanShowBanner = true;
            reqAdvertising(BANNER_AD_TYPE, mBannerAdImpl.getWidth(), mBannerAdImpl.getHeight());
        }
        return false;
    }

    public void CheckIsNeedCloseAdvertising(com.alibaba.fastjson.JSONObject data){
        String scene = data.getString("scene");
        String action = data.getString("action");
        if("stock".equals(scene)||"weather".equals(scene)){
            if(mBannerAdImpl.isShowing()){
                closeBannerAd();
            }
        }
        if("home_control".equals(scene)){
            if(HomeControlManager.ACTION_AUTHORIZATION.equals(action)){
                closeBannerAd();
            }
        }
    }

    /**
     * 获取间隔数
     * 传2，5 随机取2，3，4，5
     *
     * @param start
     * @param end
     * @return
     */
    private int randomInterval(int start, int end) {
        if (start >= end) {//当输入不合法的时候，则返回一个小的值
            return end;
        }
        int num = 0;
        do {
            num = new Random().nextInt(end + 1);
        } while (num < start);
        return num;
    }

    private void reportAction(JSONBuilder data, String action) {
        try {
            JSONObject jsonObject = data.getJSONObject().getJSONObject("report_info");
            JSONArray info = jsonObject.getJSONArray(action);
            LogUtil.d("reportInfo reportAction info:" + info.toString());
            JSONObject reportInfo = new JSONObject();
            reportInfo.put(action, info);

            reportInfo(data.getVal("p_id", Integer.class), reportInfo);
        } catch (Exception e) {
            LogUtil.d("Exception json reportAction:" + action);
            e.printStackTrace();
        }
    }

    /**
     * 广告上报
     */
    private void reportInfo(int pid, JSONObject reportInfo) {
        JSONBuilder json = new JSONBuilder();
        json.put("p_id", pid);
        json.put("report_time", System.currentTimeMillis());
        json.put("report_info", reportInfo);
        LogUtil.d("reportInfo ad json:" + json.toString()+",reportInfo:"+reportInfo.toString());
        ReportManager.Req_Ad_Report req_ad_report = new ReportManager.Req_Ad_Report();
        req_ad_report.jsonReportInfo = json.toBytes();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_AD_REPORT, req_ad_report);
    }

    public boolean openAdvertisingIsShow(){
        return mOpenAdImpl.isShowing();
    }

    public void doNowTimeTask(){
        LogUtil.d("nowTime doNowTimeTask time:" + System.currentTimeMillis());
        JSONArray nowTimeArray = null;
        try {
            //获取整点广告的时间点
            if (mNowTimeAdConfig.get("time_list") == null) {
                return;
            }
            nowTimeArray = (JSONArray) mNowTimeAdConfig.get("time_list");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (nowTimeArray == null || nowTimeArray.length() == 0) {
            return;
        }
        long[] MillisecondArray = hour2Millisecond(nowTimeArray);
        long currentTime = System.currentTimeMillis();
        for (int i = 0; i < MillisecondArray.length; i++) {
            //距离整点不到n分钟，立即请求
            if (MillisecondArray[i] - currentTime < TIMING
                    && MillisecondArray[i] - currentTime > 0) {
                //1.发起请求

                reqAdvertising(NOW_TIME_AD_TYPE, 0, 0);
                //2.定义一个延时任务整点后执行，展示。
                AppLogic.removeBackGroundCallback(mNowTimeTask);
                AppLogic.runOnBackGround(mNowTimeTask, MillisecondArray[i] - currentTime);
            }

            if (MillisecondArray[i] - TIMING < currentTime) {
                MillisecondArray[i] = MillisecondArray[i] + DAY;
            }
            //注册一个整点前十分的闹钟
            try {
                LogUtil.d("nowTimeArray time:"+nowTimeArray.getInt(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            registerAlarm(MillisecondArray[i] - TIMING);
        }
    }

    /**
     * 整点数组转化为毫秒数组
     * @param timeArray
     */
    public long[] hour2Millisecond(JSONArray timeArray){
        Date date = new Date();
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        String time = sf.format(date);
        long[] millisecondArray = new long[timeArray.length()];
        Calendar calendar = Calendar.getInstance();
        for (int i = 0; i < timeArray.length(); i++) {
            String strTime = null;
            try {
                int hour = timeArray.getInt(i);
                if (hour < 10) {
                    strTime = time + "0" + timeArray.getInt(i) + "0000";
                } else {
                    strTime = time + timeArray.getInt(i) + "0000";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                calendar.setTime(new SimpleDateFormat("yyyyMMddHHmmss").parse(strTime));
                millisecondArray[i] = calendar.getTimeInMillis();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return millisecondArray;
    }

    /**
     * 注册闹钟
     */
    public void registerAlarm(long time) {
        //获得系统提供的AlarmManager服务的对象
        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) GlobalContext.get().getSystemService(Context.ALARM_SERVICE);
        }
        //Intent设置要启动的组件，这里启动广播
        mAlarmId++;
        Intent intent = new Intent(ACTION_NOW_TIME);
        intent.putExtra("id", mAlarmId);
        LogUtil.d("nowTime time:" + time + ",id:" + mAlarmId);
        //PendingIntent对象设置动作,启动的是Activity还是Service,或广播!
        PendingIntent sender = PendingIntent.getBroadcast(GlobalContext.get(), mAlarmId, intent, 0);
        //注册闹钟
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            mAlarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC, time, sender);
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mAlarmManager.setExact(AlarmManager.RTC, time, sender);
//        } else {
//            mAlarmManager.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, sender);
//        }
        mAlarmManager.setRepeating(AlarmManager.RTC, time, AlarmManager.INTERVAL_DAY, sender);
    }

    private BroadcastReceiver mNowTimeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtil.d("nowTime onReceive id:"+intent.getIntExtra("id",-1));
            LogUtil.d("nowTime onReceive :" + System.currentTimeMillis());
            //1.发起请求
            reqAdvertising(NOW_TIME_AD_TYPE,0,0);
            //2.定义一个延时任务。
            AppLogic.removeBackGroundCallback(mNowTimeTask);
            AppLogic.runOnBackGround(mNowTimeTask, TIMING);
        }
    };

    private Runnable mNowTimeTask = new Runnable() {
        @Override
        public void run() {
            LogUtil.d("nowTime do task.");
            if (RecorderWin.isOpened() || !CallManager.getInstance().isIdle() || TXZPowerControl.isEnterReverse() || NewsManager.getInstance().isPlaying()) {
                LogUtil.d("nowTime can't speak.");
                return;
            }
            if (mNowTimeAdData == null) {
                return;
            }
            final TtsUtil.ITtsCallback callback = new TtsUtil.ITtsCallback() {
                @Override
                public void onBegin() {
                    //开始播报
                    reportAction(mNowTimeAdData, ACTION_REPORT_START);
                }

                @Override
                public void onCancel() {
                    //播报被打断
                    reportAction(mNowTimeAdData, ACTION_REPORT_SKIP);
                }

                @Override
                public void onSuccess() {
                    //播报完成
                    reportAction(mNowTimeAdData, ACTION_REPORT_FINISH);
                }
            };
            //TODO tts播报开始进行上报，被打断上报，结束上报。
            String noticeUrl = mNowTimeAdData.getVal("notice_url", String.class);
            if (!TextUtils.isEmpty(noticeUrl)) {
                File file = new File(FILE_NEW_URL_PREFIX + MD5Util.generateMD5(noticeUrl));
                LogUtil.d("nowTime file path :" + file.getPath());
                if (file.exists()) {
                    noticeUrl = file.getPath();
                }
            }

            TtsManager.getInstance().speakVoice(noticeUrl, new TtsUtil.ITtsCallback() {
                @Override
                public void onEnd() {
                    super.onEnd();
                    int ttsType = mNowTimeAdData.getVal("tts_type", Integer.class, 0);
                    LogUtil.d("nowTime tts_type:" + ttsType);
                    switch (ttsType) {
                        case TTS_TEXT_TYPE:
                            TtsManager.getInstance().speakText(mNowTimeAdData.getVal("tts", String.class), callback);
                            break;
                        case TTS_AUDIO_TYPE:
                            TtsManager.getInstance().speakVoice(mNowTimeAdData.getVal("tts_url", String.class), callback);
                            break;
                        default:
                            LogUtil.d("nowTime tts type is error.");
                            break;
                    }
                }

                @Override
                public void onSuccess() {
                    super.onSuccess();
                }
            });
        }
    };

}
