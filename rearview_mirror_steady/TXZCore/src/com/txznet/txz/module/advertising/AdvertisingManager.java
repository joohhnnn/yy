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
    private int mReqAdTimeCount = 0;//????????????????????????
    private int mReqAdType = 0; //??????????????????????????????????????????????????????????????????
    private static final int OPEN_AD_TYPE = 1;
    private static final int BANNER_AD_TYPE = 2;
    private static final int BACKGROUND_AD_TYPE = 3;
    private static final int NOW_TIME_AD_TYPE = 4;
    private static final int TTS_TYPE = 4;
    private static final int TTS_TEXT_TYPE = 1;//tts?????????????????????????????????
    private static final int TTS_AUDIO_TYPE = 2;//tts???????????????????????????
    private static final String FILE_URL_PREFIX = "/sdcard/txz/cache/download/";//????????????????????????
    private static final String FILE_NEW_URL_PREFIX = "/sdcard/txz/cache/advertising/";//??????????????????
    private static final String ACTION_REPORT_START = "action_start";//??????????????????
    private static final String ACTION_REPORT_SKIP = "action_skip";//????????????
    private static final String ACTION_REPORT_FINISH = "action_finish";//??????????????????
    private static final String ACTION_REPORT_REDIRECT = "redirect";//????????????????????????
	private static final String DOWNLOAD_AD_PARAM = "download.ad";//????????????
    private static final String ACTION_NOW_TIME = "action_now_time_test";//??????????????????
    //??????????????????????????????????????????
    private List<JSONBuilder> mAdvertisingList = new ArrayList<JSONBuilder>();
    //banner?????????????????????
    private List<JSONBuilder> mAdvertisingBannerList = new ArrayList<JSONBuilder>();
    //??????????????????
    private JSONBuilder mNowTimeAdData;
    //?????????????????????????????????
    Map<String, Long> mDownloadTimeMap = new HashMap<String, Long>();

    private IOpenAdvertisingTool mOpenAdImpl = OpenAdvertising.getInstance();
    private IBannerAdvertisingTool mBannerAdImpl = BannerAdvertising.getInstance();
    private IBackgroundAdvertisingTool mBackgroundAdImpl = BackgroundAdvertising.getInstance();

    private int mSpeechTaskId = 0;

    //????????????
    JSONObject mOpenAdConfig;
    JSONObject mBannerAdConfig;
    JSONObject mBackgroundAdConfig;
    JSONObject mNowTimeAdConfig;
    private int mBannerInterval = 0;//banner???????????????????????????
    private int mBackgroundInterval = 0;//?????????????????????????????????

    private boolean isFirst = false;//??????????????????????????????
    private boolean mCanShowBanner = false;//???????????????????????????
    private int mCurrentShowAd = 0;//?????????????????????
    private int mBgRepeatNum = 0;//???????????????????????????????????????

    private AlarmManager mAlarmManager;
    private int mAlarmId = -1;//????????????ID?????????id???????????????????????????
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
                case UiEquipment.SUBEVENT_NOTIFY_UPDATE_AD_CONFIG://?????????????????????
                    PushManager.PushCmd_AdConfig adConfig = null;
                    LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG");
                    try {
                        adConfig = PushManager.PushCmd_AdConfig.parseFrom(data);
                        JSONBuilder jsonBuilder = new JSONBuilder(adConfig.jsonAdConfig);
                        try {
                            mOpenAdConfig = jsonBuilder.getJSONObject().getJSONObject("1");//??????????????????
                            mBannerAdConfig = jsonBuilder.getJSONObject().getJSONObject("2");//banner????????????
                            mBackgroundAdConfig = jsonBuilder.getJSONObject().getJSONObject("3");//??????????????????
                            mNowTimeAdConfig = jsonBuilder.getJSONObject().getJSONObject("4");//????????????????????????
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG mOpenAdConfig:" + mOpenAdConfig.toString());
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG mBannerAdConfig:" + mBannerAdConfig.toString());
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG mBackgroundAdConfig:" + mBackgroundAdConfig.toString());
                            LogUtil.d("SUBEVENT_NOTIFY_UPDATE_AD_CONFIG:" + jsonBuilder.toString());
                            //?????????????????????????????????????????????????????????
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
                    //???????????????????????????????????????????????????
                    if (data == null || data.length == 0) {
                        LogUtil.d("SUBEVENT_RESP_GET_AD data is null,type:" + mReqAdType);
                        switch (mReqAdType) {
                            case OPEN_AD_TYPE:
                                try {
                                    mReqAdTimeCount++;
                                    if (mReqAdTimeCount > mOpenAdConfig.getInt("error_repeat_num")) {//???????????????????????????10???error_repeat_num
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
                                    if (mReqAdTimeCount > mBannerAdConfig.getInt("error_repeat_num")) {//???????????????????????????10???error_repeat_num
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
                                jsonBuilder.put("start_load", System.currentTimeMillis());//????????????????????????
                                if (TextUtils.isEmpty(ttsUrl)) {//?????????????????????????????????????????????????????????????????????
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
                                        LogUtil.d("open ad url???ttsUrl had cache.");
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
                                            if(i == 0 || !mBannerAdImpl.isShowing()){//i=0????????????????????????????????????banner?????????????????????
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
                                jsonBuilder.put("start_load", System.currentTimeMillis());//????????????????????????
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
                        //?????????????????????????????????
                        if(!DOWNLOAD_AD_PARAM.equals(task.strDefineParam)){
                            break;
                        }

                        if (task.int32ResultCode != UiInnerNet.DOWNLOAD_HTTP_FILE_TASK_RESULT_CODE_SUCCESS) {
                            LogUtil.d("download file is fail!error code:" + task.int32ResultCode + ",taskId :" + task.strTaskId);
                            break;
                        }
                        LogUtil.d("download file is success!taskId:" + task.strTaskId);
                        DownloadManager.getInstance().unregisterDownloadTaskStatusChangeListener(task.strTaskId);
                        //?????????????????????????????????advertising?????????
                        String oldPath = Environment.getExternalStorageDirectory().getPath() + "/txz/cache/download/";
                        String newPath = Environment.getExternalStorageDirectory().getPath() + "/txz/cache/advertising/";
                        FileUtil.copyFile(oldPath + task.strTaskId, newPath + task.strTaskId);
                        //??????download????????????????????????
                        File oldFile = new File(FILE_URL_PREFIX + task.strTaskId);
                        oldFile.delete();
                        //????????????????????????????????????
                        if (mNowTimeAdData != null ) {
                            if (task.strUrl.equals(mNowTimeAdData.getVal("tts_url", String.class, ""))) {
                                break;
                            }
                            if (task.strUrl.equals(mNowTimeAdData.getVal("notice_url", String.class, ""))) {
                                break;
                            }
                        }
                        //?????????????????????????????????????????????
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

    //???????????????
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
     * ??????????????????
     *
     * @param type   ????????????
     * @param width  ?????????????????????
     * @param height ?????????????????????
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
     * ??????url????????????
     *
     * @param url ????????????
     * @return
     */
    private String downloadFile(String url) {
        //??????sd???????????????200M??????????????????????????????????????????200M    ??????????????????????????????
        if (AdvertisingUtils.getSDFreeSize() < 200 || AdvertisingUtils.getCacheSize() > 200) {
            AdvertisingUtils.freeCacheFiles(new File(FILE_NEW_URL_PREFIX));
        }
        String taskId = MD5Util.generateMD5(url);
        UiInnerNet.DownloadHttpFileTask task = new UiInnerNet.DownloadHttpFileTask();
        task.strUrl = url;
        task.strTaskId = taskId;
        task.strDefineParam = DOWNLOAD_AD_PARAM;
        task.bForbidUseReservedSpace = true; // ????????????????????????
        JNIHelper.sendEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_REQ, task);
        return taskId;
    }


    /**
     * ?????????????????????????????????
     *
     * @param url
     * @return
     */
    private boolean checkFileExists(String url) {
        File file = new File(FILE_NEW_URL_PREFIX + MD5Util.generateMD5(url));
        return file.exists();
    }

    /**
     * ???????????????taskI???????????????????????????????????????
     *
     * @param taskId ?????????????????????
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
                    if (!TextUtils.isEmpty(url) && !checkFileExists(url)) {//???????????????gif?????????????????????????????????
                        LogUtil.d("open ad url is null or file no exist");
                        break;
                    }
                    if (!TextUtils.isEmpty(ttsUrl) && !checkFileExists(ttsUrl)) {//??????tts_url????????????????????????????????????????????????????????????????????????????????????
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
                                if(i == 0 || !mBannerAdImpl.isShowing()){//i=0????????????????????????????????????banner?????????????????????
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
     * ??????????????????
     *
     * @param data ?????????????????????????????????
     */
    private void checkShowOpenAdvertising(final JSONBuilder data) {
        long time = System.currentTimeMillis() - data.getVal("start_load", Long.class, 0L);
        LogUtil.d("open ad loaded end.spend time:" + time + "ms");
        try {
            if (time > mOpenAdConfig.getInt("error_over_time") * 1000) {//????????????
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
        //??????tts??????
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
        }, data.getVal("show_time", Integer.class) * 1000);//????????????????????????

        //??????????????????
        reportAction(data, ACTION_REPORT_START);
        //??????????????????????????????
        AdvertisingSp sp = AdvertisingSp.getInstance();
        sp.setOpenShowNum(sp.getOpenShowNum() + 1);
        //????????????????????????
        sp.setOpenLastTime(System.currentTimeMillis());
    }



    /**
     * ?????????????????????????????????
     *
     * @param data ?????????????????????????????????
     */
    private void checkShowBannerAdvertising(final JSONBuilder data) {
        if (!mBannerAdImpl.isShowing() && RecorderWin.isOpened() && mCanShowBanner) {
            showBannerAdvertising(data);
            reportAction(data, ACTION_REPORT_START);//??????????????????
            AdvertisingSp sp = AdvertisingSp.getInstance();
            sp.setBannerShowNum(sp.getBannerShowNum() + 1);
            LogUtil.d("banner ad data size:" + mAdvertisingBannerList.size());
            AppLogic.removeBackGroundCallback(mBannerShowTask);
            AppLogic.runOnBackGround(mBannerShowTask, data.getVal("show_time", Integer.class) * 1000);
            try {
                //????????????????????????
                int start = mBannerAdConfig.getInt("awake_range_start");
                int end = mBannerAdConfig.getInt("awake_range_end");
                //?????????????????????
                mBannerInterval = randomInterval(start, end);
                LogUtil.d("banner ad random mBannerInterval:" + mBannerInterval);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } else{
            showBannerAdvertising(data);
            reportAction(data, ACTION_REPORT_START);//??????????????????
            AppLogic.removeBackGroundCallback(mBannerShowTask);
            AppLogic.runOnBackGround(mBannerShowTask, data.getVal("show_time", Integer.class) * 1000);
        }
    }

    private Runnable mBannerShowTask = new Runnable() {
        @Override
        public void run() {
            //??????????????????
            if (mAdvertisingBannerList.size() != 0) {
                reportAction(mAdvertisingBannerList.get(mCurrentShowAd), ACTION_REPORT_FINISH);
                mAdvertisingBannerList.remove(mCurrentShowAd);
            }

            if (mAdvertisingBannerList.size() != 0) {
                JSONBuilder data = mAdvertisingBannerList.get(mCurrentShowAd);
                showBannerAdvertising(data);//5s?????????????????????????????????banner??????
                reportAction(data, ACTION_REPORT_START);//??????????????????
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
     * ??????????????????
     *
     * @param data ?????????????????????????????????
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
            //????????????????????????
            reportAction(data, ACTION_REPORT_START);
            //??????????????????
            AdvertisingSp sp = AdvertisingSp.getInstance();
            sp.setBackgroundShowNum(sp.getBackgroundShowNum() + 1);
            //????????????????????????
            long showTime = 0;
            try {
                showTime = data.getJSONObject().getJSONObject("report_info").getInt("bg_rep_int") * 1000;//?????????????????????????????????
                mBgRepeatNum = data.getJSONObject().getJSONObject("report_info").getInt("bg_rep_num");//???????????????????????????
                LogUtil.d("background ad showTime:" + showTime + ",mBgRepeatNum:" + mBgRepeatNum);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (mBgRepeatNum != 0) {
                mBackgroundTask.update(data, showTime);
                AppLogic.removeBackGroundCallback(mBackgroundTask);
                AppLogic.runOnBackGround(mBackgroundTask, showTime);
            }
            //????????????????????????
            try {
                int start = mBackgroundAdConfig.getInt("awake_range_start");
                int end = mBackgroundAdConfig.getInt("awake_range_end");
                //?????????????????????
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
                reportAction(mP1, ACTION_REPORT_FINISH);//????????????????????????
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
     * ??????????????????
     *
     * @param url ??????????????????
     */
    private int speakVoice(String url) {
        return TtsManager.getInstance().speakVoice(url);
    }

    /**
     * ??????????????????
     *
     * @param text
     */
    private int speakText(String text) {
        return TtsManager.getInstance().speakText(text);
    }

    /**
     * ??????????????????
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
     * ??????banner??????
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
            reportAction(data, ACTION_REPORT_SKIP);//????????????
        }
    }

    public void clearAdvertising(){
        if(mBannerAdImpl.isShowing()){
            closeBannerAd();
        }
    }

    /**
     * ??????????????????
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
            int num = sp.getOpenShowNum();//???????????????????????????
            if (num >= mOpenAdConfig.optInt("show_num")) {
                LogUtil.d("open ad already can't show");
                return false;
            }
            if (System.currentTimeMillis() - sp.getOpenLastTime() < mOpenAdConfig.optInt("show_time_interval") * 60 * 1000) {//????????????????????????????????????
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
            int num = sp.getBannerShowNum();//???????????????????????????
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

            int num = sp.getBackgroundShowNum();//???????????????????????????
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
        CheckIsNeedCloseAdvertising(data);//?????????????????????????????????????????????????????????
        if (!isFirst) {//????????????????????????????????????
            return false;
        }
        isFirst = false;
        //????????????????????????
        if (mBackgroundAdImpl.isShowing()) {
            return false;
        }
        if (!mBannerAdImpl.isSupportShow()) {
            return false;
        }

        if (!isShowBannerAdvertising()) {
            return false;
        }
        //??????????????????????????????????????????????????????
        String scene = data.getString("scene");
        boolean isShow = false;
        if ("unknown".equals(scene) || "empty".equals(scene)) {//?????????empty??????
            String answer = data.getString("answer");
            if (score < 90) {
                if (!TextUtils.isEmpty(answer)) {
                    if (answer.length() > 40
                            && !(TextUtils.equals(data.getString("style"), "joke")
                            || TextUtils.equals(data.getString("style"), "cookbook")
                            || TextUtils.equals(data.getString("style"), "poem"))) {
                        int index = answer.indexOf("???");//????????????????????????
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
            if ("exeu".equals(action)) {//????????????
                isShow = true;
            }
        }

        if (isShow) {
            //??????banner??????
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
     * ???????????????
     * ???2???5 ?????????2???3???4???5
     *
     * @param start
     * @param end
     * @return
     */
    private int randomInterval(int start, int end) {
        if (start >= end) {//??????????????????????????????????????????????????????
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
     * ????????????
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
            //??????????????????????????????
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
            //??????????????????n?????????????????????
            if (MillisecondArray[i] - currentTime < TIMING
                    && MillisecondArray[i] - currentTime > 0) {
                //1.????????????

                reqAdvertising(NOW_TIME_AD_TYPE, 0, 0);
                //2.???????????????????????????????????????????????????
                AppLogic.removeBackGroundCallback(mNowTimeTask);
                AppLogic.runOnBackGround(mNowTimeTask, MillisecondArray[i] - currentTime);
            }

            if (MillisecondArray[i] - TIMING < currentTime) {
                MillisecondArray[i] = MillisecondArray[i] + DAY;
            }
            //????????????????????????????????????
            try {
                LogUtil.d("nowTimeArray time:"+nowTimeArray.getInt(i));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            registerAlarm(MillisecondArray[i] - TIMING);
        }
    }

    /**
     * ?????????????????????????????????
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
     * ????????????
     */
    public void registerAlarm(long time) {
        //?????????????????????AlarmManager???????????????
        if (mAlarmManager == null) {
            mAlarmManager = (AlarmManager) GlobalContext.get().getSystemService(Context.ALARM_SERVICE);
        }
        //Intent?????????????????????????????????????????????
        mAlarmId++;
        Intent intent = new Intent(ACTION_NOW_TIME);
        intent.putExtra("id", mAlarmId);
        LogUtil.d("nowTime time:" + time + ",id:" + mAlarmId);
        //PendingIntent??????????????????,????????????Activity??????Service,?????????!
        PendingIntent sender = PendingIntent.getBroadcast(GlobalContext.get(), mAlarmId, intent, 0);
        //????????????
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
            //1.????????????
            reqAdvertising(NOW_TIME_AD_TYPE,0,0);
            //2.???????????????????????????
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
                    //????????????
                    reportAction(mNowTimeAdData, ACTION_REPORT_START);
                }

                @Override
                public void onCancel() {
                    //???????????????
                    reportAction(mNowTimeAdData, ACTION_REPORT_SKIP);
                }

                @Override
                public void onSuccess() {
                    //????????????
                    reportAction(mNowTimeAdData, ACTION_REPORT_FINISH);
                }
            };
            //TODO tts????????????????????????????????????????????????????????????
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
