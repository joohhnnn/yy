package com.txznet.txz.module.offlinepromote;

import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;

import com.nostra13.universalimageloader.core.download.BaseImageDownloader;
import com.txz.equipment_manager.EquipmentManager;
import com.txz.ui.data.UiData;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.innernet.UiInnerNet;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.component.offlinepromote.OfflinePromoteFloatWindow;
import com.txznet.txz.component.offlinepromote.dialog.OfflinePromoteAutoCodeDialog;
import com.txznet.txz.component.offlinepromote.dialog.OfflinePromoteContentDialog;
import com.txznet.txz.component.offlinepromote.dialog.OfflinePromoteHintDialog;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.net.NetworkManager;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.weixin.WeixinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.FileUtil;
import com.txznet.txz.util.MD5Util;
import com.txznet.txz.util.PreferenceUtil;
import com.txznet.txz.util.UrlUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.zip.CRC32;

public class OfflinePromoteManager extends IModule {
    private static final String TAG = "OfflinePromoteManager";
    private static OfflinePromoteManager mInstance = new OfflinePromoteManager();
    private boolean mHasNet = false;

    private static final int RUNNING_TIME = 100 * 60 * 60 * 1000;//100小时 单位ms
    private static final int DAY_TO_MILLISECOND = 1 * 24 * 60 * 60 * 1000;//单位 ms
    private static final int DAY_INTERVAL = 30;//间隔的天数 单位天
    private static final int ACCUMULATED_MILEAGE = 100 * 1000;//累计里程 单位公里

    //初始化离线时长
    private static final int OFFLINE_TIME = 5 * 1000;//默认离线持续5s,单位毫秒

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    //handler what
    private static final int SHOW_OFFLINE_CONTENT = 1;

    private OfflinePromoteContentDialog mDialog;

    //反馈点击类型
    public static final int FEEDBACK_TYPE_CLOSE = 0;//关闭
    public static final int FEEDBACK_TYPE_UNINTERESTED = 1;//不感兴趣
    public static final int FEEDBACK_TYPE_ALREADY_SCAN = 2;//已经关注
    public static final int FEEDBACK_TYPE_SLIP = 3;//滑动，重置显示时长

    //悬浮图标
    private static final int FLOAT_DELAY_SHOW_TIME = 5000;//延时展示时间
    private static final int FLOAT_DELAY_DISMISS_TIME = 30 * 1000;//延时关闭时间30s
    private static final int FLOAT_DELAY_HALF_HOUR_SHOW = 30 * 60 * 1000;//延时展示时间30min
    private static final int MAX_SHOW_NUMBER = 3;//最大显示次数
    private static final int FLOAT_SHOW_INTERVAL_TIME = 6 * 60 * 60 * 1000;//六个小时（单位ms）

    //客诉处理
    private String mAutoCode = "";//验证码
    private static final String ERROR_HINT = "您已经尝试5次，请明天再试";
    private static final int MAX_TRY_INPUT_NUMBER = 5;//最大的输入次数

    //二维码链接
    private static final String URL_FORM_SHOW_CONTENT = "http://weixin.qq.com/q/024ZsRc5xfbsP10000g03N";//内容展示
    private static final String URL_FORM_DEL_CONTENT = "http://weixin.qq.com/q/02jI-Tc3xfbsP10000g03b";//删除本地海报展示


    public static OfflinePromoteManager getInstance() {
        return mInstance;
    }

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_NETWORK_CHANGE);
        regEvent(UiEvent.EVENT_INNER_NET, UiInnerNet.SUBEVENT_DOWNLOAD_HTTP_FILE_RESP);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_INIT_SUCCESS);
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_RESP_OFFLINE_PROMOTE_GET_CONTENT);
        WeixinManager.getInstance().addOnQrCodeListener(onQRCodeListener);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        regCommand("CMD_OFFLINE_PROMOTE_DELETE_CODE");
        regCommand("CMD_OFFLINE_PROMOTE_DELETE_CONTENT");
        return super.initialize_AfterStartJni();
    }

    /**
     * 请求离线促活内容
     */
    private void reqContent() {
        LogUtil.d(TAG,"reqContent");
        EquipmentManager.Req_OfflinePromoteGetContent req = new EquipmentManager.Req_OfflinePromoteGetContent();
        JNIHelper.sendEvent(UiEvent.EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_OFFLINE_PROMOTE_GET_CONTENT, req);
    }

    private OfflinePromoteAutoCodeDialog mAutoCodeDialogDialog;

    @Override
    public int onCommand(String cmd) {

        boolean needShow = PreferenceUtil.getInstance().getOfflineNeedShow();
        if (!needShow) {
            String text = NativeData.getResString("RS_VOICE_ALREADY_DELETE_OFFLINE_CONTENT");
            AsrManager.getInstance().setNeedCloseRecord(true);
            RecorderWin.speakTextWithClose(text, null);
            return 0;
        }

        if (needResetAutoCodeState()) {//命中命令字时先判断是否需要重置状态
            PreferenceUtil.getInstance().setOfflineInputErrorNumber(0);
            PreferenceUtil.getInstance().setOfflineRecordBootNumber(0);
            PreferenceUtil.getInstance().setOfflineInputErrorTime(System.currentTimeMillis());
        }
        int number = PreferenceUtil.getInstance().getOfflineInputErrorNumber();
        if (number >= MAX_TRY_INPUT_NUMBER) {
            String text = "<font color='#F54545'>" + ERROR_HINT + "</font>";
            RecorderWin.speakText(text, null);
            return 0;
        }
        if ("CMD_OFFLINE_PROMOTE_DELETE_CODE".equals(cmd)) {
            RecorderWin.dismiss();
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    mAutoCodeDialogDialog = new OfflinePromoteAutoCodeDialog();
                    mAutoCodeDialogDialog.show();
                }
            });
        } else if ("CMD_OFFLINE_PROMOTE_DELETE_CONTENT".equals(cmd)) {
            String code = randomNum();
            String replaceCode = code;
            if(ProjectCfg.YZS_SDK_VERSION >= 3){
                replaceCode = "<tel>"+code+"</tel>";
            }
            String text = NativeData.getResString("RS_VOICE_DELETE_OFFLINE_CONTENT");
            //播报文本
            String spk = Html.fromHtml(text).toString().replaceAll("%CODE%", replaceCode);
            speakHighLightText(spk);
            //展示文本
            text = text.replaceAll("%CODE%", code);
            JSONBuilder json = new JSONBuilder();
            json.put("type", 26);
            json.put("qrCode", URL_FORM_DEL_CONTENT);
            json.put("text", text);
            RecorderWin.showUserText();
            RecorderWin.showData(json.toString());
            //加密规则
            mAutoCode = encrypt(code);
        }
        return super.onCommand(cmd);
    }


    private int mSpeakTaskId;
    private int mDelayDismissWinTime = 5000;
    public void speakHighLightText(String text){
        mSpeakTaskId = TtsManager.getInstance().speakText(text, new TtsUtil.ITtsCallback() {
            @Override
            public void onSuccess() {
                AppLogic.removeBackGroundCallback(mDelayDismissWin);
                AppLogic.runOnBackGround(mDelayDismissWin,mDelayDismissWinTime);
            }
        });
    }

    Runnable mDelayDismissWin = new Runnable() {
        @Override
        public void run() {
            RecorderWin.close();
        }
    };

    /**
     * 判断是否满足间隔，满足会重置状态
     *  1.时间准确的话，第二天才可重试。
     * 2.时间不准，则当开机次数≥2且开机时长≥1小时，才可以再次重试。
     *
     *
     * @return
     */
    public boolean needResetAutoCodeState() {
        int number = PreferenceUtil.getInstance().getOfflineRecordBootNumber();
        long totalTime = PreferenceUtil.getInstance().getTotalRunningTime();
        long recordTime = PreferenceUtil.getInstance().getOfflineDisableRunnigTime();
        if (number >= 2 && totalTime - recordTime >= 1 * 60 * 60 * 1000) {
            return true;
        }
        LogUtil.d(TAG, "needResetAutoCodeState RUNNING TIME no enough.");
        long time = PreferenceUtil.getInstance().getOfflineInputErrorTime();
        if (DateUtils.getGapCount(new Date(time), new Date()) >= 1) {
            return true;
        }
        LogUtil.d(TAG, "needResetAutoCodeState INTERVAL TIME no enough.");
        return false;
    }


    /**
     * 随机数 截取小数点后四位
     * 0.02507059500814135
     * 0250
     *
     * @return
     */
    public String randomNum() {
        String code = String.valueOf(Math.random()).substring(2, 6);
        return code;
    }

    @Override
    public int initialize_AfterInitSuccess() {
        LogUtil.d(TAG, "initialize_AfterInitSuccess");

        mHandlerThread = new HandlerThread("offline_promote_thread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()) {
            @Override
            public void handleMessage(Message msg) {
                handleMsg(msg);
            }
        };

        //监听声控界面启动
        RecorderWin.OBSERVABLE.registerObserver(mObserver);

        mHasNet = NetworkManager.getInstance().hasNet();
        if(mHasNet){
            LogUtil.d(TAG,"has net.");
            return 0;
        }
        int number = PreferenceUtil.getInstance().getOfflineRecordBootNumber();
        PreferenceUtil.getInstance().setOfflineRecordBootNumber(number + 1);

        initDayData();//初始化mDayJson
        initTimeData();//初始化mTimeArray
        //根据反馈的功能判断是否可以显示离线促活内容
        if (!enableShowContent()) {
            return 0;
        }

        if (uninterestedTimeOut()) {
            mHandler.postDelayed(mShowOfflineContentTask, OFFLINE_TIME);
        } else if (conditionFromDay() || conditionFromHour()) {//不在循环
            mHandler.postDelayed(mShowOfflineContentTask, OFFLINE_TIME);
        } else if (PreferenceUtil.getInstance().getOfflineFloatShowNumber() < MAX_SHOW_NUMBER) {
            if (enableShowFloat()) {
                mHandler.post(mShowFloatTask);
            } else {
                PreferenceUtil.getInstance().setOfflineFloatShowNumber(MAX_SHOW_NUMBER);
            }
        }


        return super.initialize_AfterInitSuccess();
    }

    JSONObject mDayJson;
    private static final int MAX_DAYS = 10;
    /**
     * 初始化最近10天数据
     */
    private void initDayData(){
        String data = PreferenceUtil.getInstance().getOfflineRecentDayData();
        LogUtil.d(TAG, "initDayData :" + data);
        if (TextUtils.isEmpty(data)) {
            mDayJson = new JSONObject();
            try {
                mDayJson.put(getDate(), getNetStatus());
                PreferenceUtil.getInstance().setOfflineRecentDayData(mDayJson.toString());
                return;
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }
        try {
            mDayJson = new JSONObject(data);
            if(mDayJson.has(getDate())){
                int netStatus = mDayJson.getInt(getDate());
                if(netStatus != getNetStatus() && netStatus == 1){
                    mDayJson.put(getDate(),1);//无网到有网需要覆盖
                }
                return;
            }
            while (mDayJson.length() >= MAX_DAYS) {
                mDayJson.remove(mDayJson.keys().next());
            }
            mDayJson.put(getDate(), getNetStatus());
            PreferenceUtil.getInstance().setOfflineRecentDayData(mDayJson.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * 1表示联网，0表示无网
     * @return
     */
    private int getNetStatus() {
        return mHasNet ? 1 : 0;
    }

    /**
     * 生成yyyyMMdd格式的时间  20190925
     *
     * @return
     */
    private String getDate(){
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        return sf.format(new Date());
    }

    JSONArray mTimeArray;//保存最近30次运行是否联网记录
    private static final int MAX_TIMES = 30;
    /**
     *  初始化最近30次运行数据
     *  //1联网0不联网
     */
    private void initTimeData(){
        String data = PreferenceUtil.getInstance().getOfflineRecentTimeData();
        LogUtil.d(TAG, "initTimeData :" + data);
        mTimeArray = new JSONArray();
        if (TextUtils.isEmpty(data)) {
            mTimeArray.put(getNetStatus());
            PreferenceUtil.getInstance().setOfflineRecentTimeData(mTimeArray.toString());
            return;
        }

        try {
            int maxLength = MAX_TIMES;
            JSONArray tempArray = new JSONArray(data);
            if (tempArray.length() >= maxLength) {
                for (int i = tempArray.length() - maxLength; i < tempArray.length(); i++) {
                    mTimeArray.put(tempArray.get(i));
                }
            } else {
                mTimeArray = tempArray;
            }
            mTimeArray.put(getNetStatus());
            PreferenceUtil.getInstance().setOfflineRecentTimeData(mTimeArray.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public boolean enableShowFloat() {
        String showTime = PreferenceUtil.getInstance().getOfflineContentShowTime();
        if (!TextUtils.isEmpty(showTime)) {
            long time = Long.valueOf(showTime);
            if (time != 0 && System.currentTimeMillis() - time > DAY_TO_MILLISECOND) {
                return false;
            }
        }
        LogUtil.d(TAG, "no enough one day.");
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        long recordRunningTime = PreferenceUtil.getInstance().getOfflineRecordRunningTime();
        if (totalRunningTime - recordRunningTime < FLOAT_SHOW_INTERVAL_TIME) {
            return true;
        }
        LogUtil.d(TAG, "more than six hour.");
        return false;
    }

    /**
     * 不感兴趣且时间达到一定周期（1个月/开机时长100小时）则立即展示内容
     * @return
     */
    public boolean uninterestedTimeOut(){
        //不感兴趣
        long time = PreferenceUtil.getInstance().getOfflineUninterestedTime();
        if (time == 0) {
            return false;
        }
        float interval = DateUtils.getGapMonthWithHalf(new Date(time), new Date());
        LogUtil.d("TAG", "enableShowContent interval:" + interval);
        if (interval >= 1) {//间隔月数大于等于1个月
            return true;
        }

        long recordRunningTime = PreferenceUtil.getInstance().getOfflineRecordRunningTime();
        if(recordRunningTime == 0){
            return false;
        }
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        LogUtil.d("TAG", "enableShowContent RunningTime:" + recordRunningTime);
        if (totalRunningTime - recordRunningTime >= 100 * 60 * 60 * 1000) {//100个小时
            return true;
        }
        return false;
    }

    private Runnable mShowOfflineContentTask = new Runnable() {
        @Override
        public void run() {
            Message message = new Message();
            message.what = SHOW_OFFLINE_CONTENT;
            mHandler.sendMessage(message);
        }
    };

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        switch (eventId) {
            case UiEvent.EVENT_NETWORK_CHANGE: {
                switch (NetworkManager.getInstance().getNetType()) {
                    case UiData.NETWORK_STATUS_2G:
                    case UiData.NETWORK_STATUS_3G:
                    case UiData.NETWORK_STATUS_4G:
                    case UiData.NETWORK_STATUS_WIFI:
                        LogUtil.d(TAG, "EVENT_NETWORK_CHANGE");
                        resetCondition();
                        //有网需要设置
                        if (!mHasNet) {
                            mHasNet = true;
                            if(mDayJson != null){
                                try {
                                    mDayJson.put(getDate(),1);
                                    PreferenceUtil.getInstance().setOfflineRecentDayData(mDayJson.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            if(mTimeArray != null){
                                try {
                                    mTimeArray.put(mTimeArray.length()-1,1);
                                    PreferenceUtil.getInstance().setOfflineRecentTimeData(mTimeArray.toString());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                }
                break;
            }
            case UiEvent.EVENT_ACTION_EQUIPMENT:
                switch (subEventId){
                    case UiEquipment.SUBEVENT_REQ_INIT_SUCCESS:
                            reqContent();
                        break;
                    case UiEquipment.SUBEVENT_RESP_OFFLINE_PROMOTE_GET_CONTENT:
                        if(data == null || data.length == 0){
                            LogUtil.d(TAG,"resp data is null.");
                            break;
                        }
                        try {
                            EquipmentManager.Resp_OfflinePromoteGetContent resp = EquipmentManager.Resp_OfflinePromoteGetContent.parseFrom(data);
                            String title = new String(resp.strContTitle);
                            JSONObject jsonObject = new JSONObject();
                            jsonObject.put("title",title);
                            JSONArray urlArray = new JSONArray();
                            JSONArray crc32Array = new JSONArray();
                            //拿本地的数据对比看是否已经下载过
                            String oldData = PreferenceUtil.getInstance().getOfflineReqContent();
                            JSONObject oldJson = null;
                            JSONArray oldCrc32 = null;

                            if (!TextUtils.isEmpty(oldData)) {
                                oldJson = new JSONObject(oldData);
                            }
                            if (oldJson != null && oldJson.has("crc32")) {
                                oldCrc32 = oldJson.getJSONArray("crc32");
                            }

                            for (int i = 0; i < resp.pics.length; i++) {
                                String url = new String(resp.pics[i].strHref);
                                String crc32 = new String(resp.pics[i].strCrc32);
                                urlArray.put(url);
                                crc32Array.put(crc32);
                                //判断是否需要下载
                                if (oldCrc32 == null || !oldCrc32.get(i).equals(crc32) || !checkFileIsExist(url)) {
                                    baseDownloadImage(url,crc32);
                                }
                            }
                            jsonObject.put("urls",urlArray);
                            jsonObject.put("crc32",crc32Array);
                            String content = jsonObject.toString();
                            PreferenceUtil.getInstance().setOfflineReqContent(content);
                            LogUtil.d(TAG, "resp ReqContent:" + content);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;

                }

                break;

        }
        return super.onEvent(eventId, subEventId, data);
    }

    /**
     * 校验文件是否存在
     * @param url
     * @return
     */
    public boolean checkFileIsExist(String url){
        File file = new File(mImagePath,MD5Util.generateMD5(url));
        if(file.exists()){
            return true;
        }
        return false;
    }

    private String mImagePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/txz/image/";
    private String mPrefix = "_";
    public void baseDownloadImage(final String imageUri, final String strCrc32){
        LogUtil.d(TAG,"baseDownloadImage url"+imageUri+",strCrc32:"+strCrc32);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream = null;
                FileOutputStream outputStream = null;
                try {
                    BaseImageDownloader baseImageDownloader = new BaseImageDownloader(GlobalContext.get());
                    UrlUtil.ConnectInfo connectInfo = UrlUtil.parseUrl(imageUri);
                    inputStream = baseImageDownloader.getStream(imageUri, connectInfo);
                    String fileName = MD5Util.generateMD5(imageUri);
                    String oldPath = mImagePath + mPrefix + fileName;
                    String path = mImagePath + fileName;
                    File file = new File(oldPath);
                    if (!file.getParentFile().exists()) {
                        file.getParentFile().mkdirs();
                    }
                    if (!file.exists()) {
                        file.createNewFile();
                    }
                    outputStream = new FileOutputStream(file);
                    int read;
                    byte[] bytes = new byte[1024];
                    CRC32 crc32 = new CRC32();
                    while ((read = inputStream.read(bytes)) != -1) {
                        crc32.update(bytes, 0, read);
                        outputStream.write(bytes, 0, read);
                    }

                    LogUtil.d(TAG,"crc32:" + crc32.getValue());
                    if (String.valueOf(crc32.getValue()).equals(strCrc32)) {//值不相等的时候删除，下次继续下载
                        FileUtil.copyFile(oldPath, path);
                        file.delete();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (outputStream != null) {
                            outputStream.close();
                        }
                        if (inputStream != null) {
                            inputStream.close();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 重置触发条件
     */
    public void resetCondition() {
        if (mDialog != null && mDialog.isShowing()) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "resetCondition CONTENT.");
                    mDialog.dismiss();
                    clearAllInfo();
                }
            }, 5000);
        }

        if (mFloatWindow != null && mFloatWindow.isShowing()) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    LogUtil.d(TAG, "resetCondition FLOAT WINDOW.");
                    mFloatWindow.dismiss();
                    clearAllInfo();
                }
            });
        }
    }

    private void handleMsg(Message message) {
        switch (message.what) {
            case SHOW_OFFLINE_CONTENT:
                if(NavManager.getInstance().isNavi() || NavManager.getInstance().isNavFocus()){
                    LogUtil.d(TAG,"nav is running.");
                    break;
                }
                if (!mHasNet) {//如果还是没网，则展示离线促活内容
                    showOfflineContent();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Desc: 触发条件一
     * 若设备同时满足以下3个条件，则触发运营内容的展示：
     * 1）最后开车10天：50%没联网且最后1天没联网 或 最后连续3天没上网
     * 2）累计行驶里程≥100公里
     * 3）距离激活日期≥30天
     *
     * @return
     */
    private boolean conditionFromDay() {
        int recentDrivingDay = MAX_DAYS;
        //开车时间大于等于10天

        if (mDayJson.length() < recentDrivingDay) {
            LogUtil.d(TAG, "device boot day not enough");
            return false;
        }
        //最后开车10天：≥50%没联网且最后1天没联网
        //或最后连续3天没上网
        if (!((getRecentNetworkDays(recentDrivingDay) < recentDrivingDay / 2 && mDayJson.optInt(getDate()) == 0)
                || getRecentNetworkDays(3) == 0)) {
            LogUtil.d(TAG, "device networking condition not satisfy");
            return false;
        }

        //累计行程大于等于 100 公里
        long totalDistance = PreferenceUtil.getInstance().getTotalDrivingDistance();
        long recordDistance = PreferenceUtil.getInstance().getOfflineRecordDrivingDistance();
        if (totalDistance - recordDistance < ACCUMULATED_MILEAGE) {
            LogUtil.d(TAG, "distance not enough");
            return false;
        }
        //距离激活日期≥30天
        String activeTime = PreferenceUtil.getInstance().getDeviceActiveTime();
        if (TextUtils.isEmpty(activeTime)) {
            LogUtil.d(TAG, "activeTime is null");
            return false;
        }
        //激活日期和当前时间判断，看是否超过30天
        int dayInterval = DateUtils.getGapCount(new Date(Long.parseLong(activeTime)), new Date());
        if (dayInterval < DAY_INTERVAL) {
            LogUtil.d(TAG, "getGapCount:" + dayInterval + ",activeTime:" + activeTime);
            return false;
        }

        return true;
    }

    /**
     * 获取最近N天的联网次数
     * @param days <mDayJson
     * @return
     */
    private int getRecentNetworkDays(int days) {
        int number = 0;
        int i = 0;
        int k = mDayJson.length() - days;
        Iterator iterator = mDayJson.keys();
        while (iterator.hasNext()) {
            if(i == mDayJson.length()){
                break;
            }
            if (i < k) {
                i++;
                continue;
            }
            number += mDayJson.optInt((String) iterator.next());
            i++;
        }
        LogUtil.d(TAG,"getRecentNetworkDays :"+number);
        return number;
    }


    /**
     * Desc 触发条件二，设备时间不准的时候会按小时来触发
     * 若设备满足以下条件，则触发运营内容的展示：
     * 1）开机时长≥100小时
     * 2）累计行驶里程≥100公里
     * 3）最近30次运行（开机首次运行？）≥50%次数或最后连续3次没有联网
     *
     * @return
     */
    private boolean conditionFromHour() {
        //运行时间大于等于100小时
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        long recordRunningTime = PreferenceUtil.getInstance().getOfflineRecordRunningTime();
        if (totalRunningTime - recordRunningTime < RUNNING_TIME) {
            LogUtil.d(TAG, "running time not enough");
            return false;
        }

        //累计行程大于等于 100 公里
        long totalDistance = PreferenceUtil.getInstance().getTotalDrivingDistance();
        long recordDistance = PreferenceUtil.getInstance().getOfflineRecordDrivingDistance();
        if (totalDistance - recordDistance < ACCUMULATED_MILEAGE) {
            LogUtil.d(TAG, "distance not enough");
            return false;
        }

        int recentRunningNumber = MAX_TIMES;
        //开机运行次数需≥30次
        if (mTimeArray.length() < recentRunningNumber) {
            LogUtil.d(TAG, "device boot number no enough");
            return false;
        }

        //最近30次运行≥50%次数没有联网
        //或最后连续3次没有联网
        if (getRecentNetWorkNumber(recentRunningNumber) < recentRunningNumber / 2
                || getRecentNetWorkNumber(3) == 0) {
            return true;
        }
        LogUtil.d(TAG, "recent running offline time no enough");

        return false;
    }

    /**
     *  获取最近N次的联网次数
     * @param number 最近N次
     * @return
     */
    private int getRecentNetWorkNumber(int number){
        int len = mTimeArray.length();
        int netCount = 0;
        for (int i = len - number; i < len; i++) {
            netCount += mTimeArray.optInt(i);
        }
        return netCount;
    }

    /**
     * 显示离线促活内容
     */
    public void showOfflineContent() {
        showContentDialog();
        clearAllInfo();//显示后需要重置条件，重新触发
        PreferenceUtil.getInstance().setOfflineContentShowTime(String.valueOf(System.currentTimeMillis()));//记录内容的显示时间
        PreferenceUtil.getInstance().setOfflineFloatShowNumber(0);//重置悬浮图标的显示次数
        mHandler.postDelayed(mShowFloatTask, FLOAT_DELAY_SHOW_TIME);//延时关闭dialog，并显示悬浮图标
    }

    private void showContentDialog() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mDialog == null) {
                    String content = PreferenceUtil.getInstance().getOfflineReqContent();
                    String title = null;
                    List<String> urlList = null;
                    if (!TextUtils.isEmpty(content)) {
                        try {
                            JSONObject json = new JSONObject(content);
                            title = json.optString("title");
                            JSONArray urls = json.optJSONArray("urls");
                            if (urls != null) {
                                urlList = new ArrayList<String>();
                                for (int i = 0; i < urls.length(); i++) {
                                    String url = String.valueOf(urls.get(i));
                                    if(!checkFileIsExist(url)){//文件不存在时清空从后台获取的资源，使用本地资源
                                        urlList = null;
                                        title = null;
                                        break;
                                    }else{
                                        String path = mImagePath + MD5Util.generateMD5(url);
                                        urlList.add(path);
                                    }
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    mDialog = new OfflinePromoteContentDialog.Builder()
                            .setTitle(title)
                            .setQrCodeUrl(URL_FORM_SHOW_CONTENT)
                            .setIvUrlList(urlList)
                            .build();
                }
                mDialog.show();
            }
        });
    }

    private boolean isNeedShowTask = true;
    /**
     * desc:点击悬浮图标
     */
    public void clickFloatWindow() {
        if (mDialog != null && mDialog.isShowing()) {
            return;
        }
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                isNeedShowTask = false;
                mFloatWindow.dismiss();
                showContentDialog();
                mHandler.removeCallbacks(delayDismissFloatTask);
                if(mAutoCodeDialogDialog != null && mAutoCodeDialogDialog.isShowing()){
                    mAutoCodeDialogDialog.dismiss();
                }
                if(mHintDialog != null && mHintDialog.isShowing()){
                    mHintDialog.dismiss();
                }
            }
        });
    }

    /**
     * 点击关闭
     */
    private void dismissDialog() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if(mDialog != null && mDialog.isShowing()){
                    mDialog.dismiss();
                }
            }
        });
    }

    /**
     * 主动关闭后不再展示
     */
    public void manualCloseFloatWindow(){
        PreferenceUtil.getInstance().setOfflineFloatShowNumber(MAX_SHOW_NUMBER);
        dismissFloatWindow();
        dismissDialog();
    }

    public void showFloatWindow() {
        mHandler.removeCallbacks(mShowFloatTask);
        mHandler.postDelayed(mShowFloatTask, 0);
    }

    public void dismissFloatWindow() {
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (mFloatWindow != null) {
                    mFloatWindow.dismiss();
                }
            }
        });
    }

    private OfflinePromoteFloatWindow mFloatWindow;
    private boolean isFirstShow = true;
    Runnable mShowFloatTask = new Runnable() {
        @Override
        public void run() {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    if (mDialog != null) {
                        mDialog.dismiss();
                    }
                    boolean isShow = PreferenceUtil.getInstance().getOfflineNeedShow();
                    if (!isShow) {
                        return;
                    }
                    if (mFloatWindow == null) {
                        mFloatWindow = new OfflinePromoteFloatWindow(GlobalContext.get());
                    }
                    if(RecorderWin.isOpened()){//声控打开时先不打开悬浮图标，等声控关闭后打开
                        mHasHideFloat = true;
                        return;
                    }
                    mFloatWindow.show();
                    if(isFirstShow){
                        int num = PreferenceUtil.getInstance().getOfflineFloatShowNumber();
                        num++;
                        PreferenceUtil.getInstance().setOfflineFloatShowNumber(num);//记录展示的次数
                        isFirstShow = false;
                    }
                    delayDismissFloatWindow();
                }
            });
        }
    };

    /**
     * desc:延时关闭悬浮图标任务
     */
    public void delayDismissFloatWindow() {
        mHandler.removeCallbacks(delayDismissFloatTask);
        mHandler.postDelayed(delayDismissFloatTask, FLOAT_DELAY_DISMISS_TIME);
    }

    Runnable delayDismissFloatTask = new Runnable() {
        @Override
        public void run() {
            dismissFloatWindow();
            int num = PreferenceUtil.getInstance().getOfflineFloatShowNumber();
            if (num >= MAX_SHOW_NUMBER) {
                return;
            }

            // TODO 提供测试验证的接口
            int time = PreferenceUtil.getInstance().getInt("KEY_OFFLINE_FLOAT_DELAY_TIME", 0);//单位是分钟
            if (time == 0) {
                time = FLOAT_DELAY_HALF_HOUR_SHOW;
            } else{
                time = time *  60 * 1000;
            }
            LogUtil.d(TAG, "KEY_OFFLINE_FLOAT_DELAY_TIME time:" + time);
            isFirstShow = true;
            //关闭后半个小时延时显示
            mHandler.postDelayed(mShowFloatTask, time);
        }
    };

    /**
     * desc：处理反馈界面的点击事件
     *
     * @param type 0,1,2
     */
    public void onClickFeedback(int type) {
        LogUtil.d(TAG, "onClickFeedback TYPE:" + type);
        switch (type) {
            case FEEDBACK_TYPE_CLOSE:
                dismissDialog();//关闭海报内容
                showFloatWindow();
                break;
            case FEEDBACK_TYPE_UNINTERESTED:
                dismissDialog();//关闭海报内容
                mHandler.removeCallbacks(mShowFloatTask);
                PreferenceUtil.getInstance().setOfflineUninterestedTime(System.currentTimeMillis());
                break;
            case FEEDBACK_TYPE_ALREADY_SCAN:
                dismissDialog();//关闭海报内容
                mHandler.removeCallbacks(mShowFloatTask);
                PreferenceUtil.getInstance().setOfflineWXIsBind(true);
                break;
            case FEEDBACK_TYPE_SLIP:
                if(isNeedShowTask){
                    mHandler.removeCallbacks(mShowFloatTask);
                    mHandler.postDelayed(mShowFloatTask, FLOAT_DELAY_SHOW_TIME);//延时关闭dialog，并显示悬浮图标
                }
                break;
            default:
                break;
        }
    }

    /**
     * 根据反馈条件判断是否可以显示内容
     *
     * @return
     */
    public boolean enableShowContent() {
        //是否关注了微信公众号
        boolean isBind = PreferenceUtil.getInstance().getOfflineWXIsBind();
        if (isBind) {
            LogUtil.d(TAG, "enableShowContent already bind WX");
            return false;
        }
        //关注公众号删除或适配
        boolean needShow = PreferenceUtil.getInstance().getOfflineNeedShow();
        if (!needShow) {
            LogUtil.d(TAG, "enableShowContent needShow false");
            return false;
        }

        LogUtil.d(TAG, "enableShowContent");

        return true;
    }

    /**
     * 激活时需要设置设备激活时间
     */
    public void onLicenseActive() {
        //保存设备激活时间
        String time = String.valueOf(System.currentTimeMillis());
        PreferenceUtil.getInstance().setDeviceActiveTime(time);
    }

    private WeixinManager.OnQRCodeListener onQRCodeListener = new WeixinManager.OnQRCodeListener() {
        @Override
        public void onGetQrCode(boolean isBind, String url) {
            PreferenceUtil.getInstance().setOfflineWXIsBind(isBind);
        }
    };

    public void setNeedShowOfflinePromote(boolean needShow) {
        LogUtil.d(TAG, "setNeedShowOfflinePromote needShow:" + needShow);
        PreferenceUtil.getInstance().setOfflineNeedShow(needShow);
    }

    public String getAutoCode() {
        return mAutoCode;
    }

    /**
     * 清空信息，重新开始计算触发条件
     */
    private void clearAllInfo() {
        PreferenceUtil.getInstance().setOfflineRecentDayData("");
        PreferenceUtil.getInstance().setOfflineRecentTimeData("");
        //记录信息
        //total - record = 开机时长
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        PreferenceUtil.getInstance().setOfflineRecordRunningTime(totalRunningTime);
        //total - record = 累计行驶里程
        long totalDistance = PreferenceUtil.getInstance().getTotalDrivingDistance();
        PreferenceUtil.getInstance().setOfflineRecordDrivingDistance(totalDistance);
    }

    /**
     * md5(del + md5(business + 显示数字)), 取后2位的Asc码
     *
     * @param password
     * @return
     */
    public String encrypt(String password) {
        String value = MD5Util.generateMD5("del" + MD5Util.generateMD5("business" + password));
        return stringToAscii(value.substring(value.length() - 2, value.length()));
    }

    public String stringToAscii(String value) {
        StringBuffer sb = new StringBuffer();
        char[] chars = value.toUpperCase().toCharArray();
        for (int i = 0; i < chars.length; i++) {
            sb.append((int) chars[i]);
        }
        return sb.toString();
    }

    private boolean mHasHideFloat = false;
    //窗口监听器
    RecorderWin.StatusObervable.StatusObserver mObserver =  new RecorderWin.StatusObervable.StatusObserver() {
        @Override
        public void onShow() {
            if(mDialog != null && mDialog.isShowing()){
                mHandler.removeCallbacks(mShowFloatTask);
                mDialog.dismiss();
                mHasHideFloat = true;
            }

            if(mFloatWindow != null && mFloatWindow.isShowing()){
                mHasHideFloat = true;
                mFloatWindow.dismiss();
                mHandler.removeCallbacks(delayDismissFloatTask);
            }

            if (mAutoCodeDialogDialog != null && mAutoCodeDialogDialog.isShowing()) {
                mAutoCodeDialogDialog.dismiss();
            }

            if(mHintDialog != null && mHintDialog.isShowing()){
                mHintDialog.dismiss();
            }


        }

        @Override
        public void onDismiss() {
            if(mHasHideFloat){
                mHasHideFloat = false;
                showFloatWindow();
            }
            TtsManager.getInstance().cancelSpeak(mSpeakTaskId);
            AppLogic.removeBackGroundCallback(mDelayDismissWin);
        }
    };

    OfflinePromoteHintDialog mHintDialog;

    public void showSuccessHintDialog(){
        dismissFloatWindow();
        dismissDialog();
        mHandler.removeCallbacks(mShowFloatTask);
        mHintDialog = new OfflinePromoteHintDialog("删除成功", "更多最新资讯，关注车车互联");
        mHintDialog.show();
        setNeedShowOfflinePromote(false);
    }

    public void showErrorHintDialog(){
        mHintDialog = new OfflinePromoteHintDialog("温馨提示", "您已经尝试5次，请24小时后再试");
        mHintDialog.show();
        PreferenceUtil.getInstance().setOfflineInputErrorTime(System.currentTimeMillis());
        long totalTime = PreferenceUtil.getInstance().getTotalRunningTime();
        PreferenceUtil.getInstance().setOfflineDisableRunningTime(totalTime);
        PreferenceUtil.getInstance().setOfflineRecordBootNumber(0);
    }

}
