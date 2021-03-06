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

    private static final int RUNNING_TIME = 100 * 60 * 60 * 1000;//100?????? ??????ms
    private static final int DAY_TO_MILLISECOND = 1 * 24 * 60 * 60 * 1000;//?????? ms
    private static final int DAY_INTERVAL = 30;//??????????????? ?????????
    private static final int ACCUMULATED_MILEAGE = 100 * 1000;//???????????? ????????????

    //?????????????????????
    private static final int OFFLINE_TIME = 5 * 1000;//??????????????????5s,????????????

    private Handler mHandler;
    private HandlerThread mHandlerThread;

    //handler what
    private static final int SHOW_OFFLINE_CONTENT = 1;

    private OfflinePromoteContentDialog mDialog;

    //??????????????????
    public static final int FEEDBACK_TYPE_CLOSE = 0;//??????
    public static final int FEEDBACK_TYPE_UNINTERESTED = 1;//????????????
    public static final int FEEDBACK_TYPE_ALREADY_SCAN = 2;//????????????
    public static final int FEEDBACK_TYPE_SLIP = 3;//???????????????????????????

    //????????????
    private static final int FLOAT_DELAY_SHOW_TIME = 5000;//??????????????????
    private static final int FLOAT_DELAY_DISMISS_TIME = 30 * 1000;//??????????????????30s
    private static final int FLOAT_DELAY_HALF_HOUR_SHOW = 30 * 60 * 1000;//??????????????????30min
    private static final int MAX_SHOW_NUMBER = 3;//??????????????????
    private static final int FLOAT_SHOW_INTERVAL_TIME = 6 * 60 * 60 * 1000;//?????????????????????ms???

    //????????????
    private String mAutoCode = "";//?????????
    private static final String ERROR_HINT = "???????????????5?????????????????????";
    private static final int MAX_TRY_INPUT_NUMBER = 5;//?????????????????????

    //???????????????
    private static final String URL_FORM_SHOW_CONTENT = "http://weixin.qq.com/q/024ZsRc5xfbsP10000g03N";//????????????
    private static final String URL_FORM_DEL_CONTENT = "http://weixin.qq.com/q/02jI-Tc3xfbsP10000g03b";//????????????????????????


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
     * ????????????????????????
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

        if (needResetAutoCodeState()) {//???????????????????????????????????????????????????
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
            //????????????
            String spk = Html.fromHtml(text).toString().replaceAll("%CODE%", replaceCode);
            speakHighLightText(spk);
            //????????????
            text = text.replaceAll("%CODE%", code);
            JSONBuilder json = new JSONBuilder();
            json.put("type", 26);
            json.put("qrCode", URL_FORM_DEL_CONTENT);
            json.put("text", text);
            RecorderWin.showUserText();
            RecorderWin.showData(json.toString());
            //????????????
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
     * ????????????????????????????????????????????????
     *  1.?????????????????????????????????????????????
     * 2.????????????????????????????????????2??????????????????1?????????????????????????????????
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
     * ????????? ????????????????????????
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

        //????????????????????????
        RecorderWin.OBSERVABLE.registerObserver(mObserver);

        mHasNet = NetworkManager.getInstance().hasNet();
        if(mHasNet){
            LogUtil.d(TAG,"has net.");
            return 0;
        }
        int number = PreferenceUtil.getInstance().getOfflineRecordBootNumber();
        PreferenceUtil.getInstance().setOfflineRecordBootNumber(number + 1);

        initDayData();//?????????mDayJson
        initTimeData();//?????????mTimeArray
        //???????????????????????????????????????????????????????????????
        if (!enableShowContent()) {
            return 0;
        }

        if (uninterestedTimeOut()) {
            mHandler.postDelayed(mShowOfflineContentTask, OFFLINE_TIME);
        } else if (conditionFromDay() || conditionFromHour()) {//????????????
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
     * ???????????????10?????????
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
                    mDayJson.put(getDate(),1);//???????????????????????????
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
     * 1???????????????0????????????
     * @return
     */
    private int getNetStatus() {
        return mHasNet ? 1 : 0;
    }

    /**
     * ??????yyyyMMdd???????????????  20190925
     *
     * @return
     */
    private String getDate(){
        SimpleDateFormat sf = new SimpleDateFormat("yyyyMMdd");
        return sf.format(new Date());
    }

    JSONArray mTimeArray;//????????????30???????????????????????????
    private static final int MAX_TIMES = 30;
    /**
     *  ???????????????30???????????????
     *  //1??????0?????????
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
     * ??????????????????????????????????????????1??????/????????????100??????????????????????????????
     * @return
     */
    public boolean uninterestedTimeOut(){
        //????????????
        long time = PreferenceUtil.getInstance().getOfflineUninterestedTime();
        if (time == 0) {
            return false;
        }
        float interval = DateUtils.getGapMonthWithHalf(new Date(time), new Date());
        LogUtil.d("TAG", "enableShowContent interval:" + interval);
        if (interval >= 1) {//????????????????????????1??????
            return true;
        }

        long recordRunningTime = PreferenceUtil.getInstance().getOfflineRecordRunningTime();
        if(recordRunningTime == 0){
            return false;
        }
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        LogUtil.d("TAG", "enableShowContent RunningTime:" + recordRunningTime);
        if (totalRunningTime - recordRunningTime >= 100 * 60 * 60 * 1000) {//100?????????
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
                        //??????????????????
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
                            //????????????????????????????????????????????????
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
                                //????????????????????????
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
     * ????????????????????????
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
                    if (String.valueOf(crc32.getValue()).equals(strCrc32)) {//????????????????????????????????????????????????
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
     * ??????????????????
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
                if (!mHasNet) {//????????????????????????????????????????????????
                    showOfflineContent();
                }
                break;
            default:
                break;
        }
    }

    /**
     * Desc: ???????????????
     * ???????????????????????????3?????????????????????????????????????????????
     * 1???????????????10??????50%??????????????????1???????????? ??? ????????????3????????????
     * 2????????????????????????100??????
     * 3????????????????????????30???
     *
     * @return
     */
    private boolean conditionFromDay() {
        int recentDrivingDay = MAX_DAYS;
        //????????????????????????10???

        if (mDayJson.length() < recentDrivingDay) {
            LogUtil.d(TAG, "device boot day not enough");
            return false;
        }
        //????????????10?????????50%??????????????????1????????????
        //???????????????3????????????
        if (!((getRecentNetworkDays(recentDrivingDay) < recentDrivingDay / 2 && mDayJson.optInt(getDate()) == 0)
                || getRecentNetworkDays(3) == 0)) {
            LogUtil.d(TAG, "device networking condition not satisfy");
            return false;
        }

        //???????????????????????? 100 ??????
        long totalDistance = PreferenceUtil.getInstance().getTotalDrivingDistance();
        long recordDistance = PreferenceUtil.getInstance().getOfflineRecordDrivingDistance();
        if (totalDistance - recordDistance < ACCUMULATED_MILEAGE) {
            LogUtil.d(TAG, "distance not enough");
            return false;
        }
        //?????????????????????30???
        String activeTime = PreferenceUtil.getInstance().getDeviceActiveTime();
        if (TextUtils.isEmpty(activeTime)) {
            LogUtil.d(TAG, "activeTime is null");
            return false;
        }
        //???????????????????????????????????????????????????30???
        int dayInterval = DateUtils.getGapCount(new Date(Long.parseLong(activeTime)), new Date());
        if (dayInterval < DAY_INTERVAL) {
            LogUtil.d(TAG, "getGapCount:" + dayInterval + ",activeTime:" + activeTime);
            return false;
        }

        return true;
    }

    /**
     * ????????????N??????????????????
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
     * Desc ??????????????????????????????????????????????????????????????????
     * ???????????????????????????????????????????????????????????????
     * 1??????????????????100??????
     * 2????????????????????????100??????
     * 3?????????30???????????????????????????????????????50%?????????????????????3???????????????
     *
     * @return
     */
    private boolean conditionFromHour() {
        //????????????????????????100??????
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        long recordRunningTime = PreferenceUtil.getInstance().getOfflineRecordRunningTime();
        if (totalRunningTime - recordRunningTime < RUNNING_TIME) {
            LogUtil.d(TAG, "running time not enough");
            return false;
        }

        //???????????????????????? 100 ??????
        long totalDistance = PreferenceUtil.getInstance().getTotalDrivingDistance();
        long recordDistance = PreferenceUtil.getInstance().getOfflineRecordDrivingDistance();
        if (totalDistance - recordDistance < ACCUMULATED_MILEAGE) {
            LogUtil.d(TAG, "distance not enough");
            return false;
        }

        int recentRunningNumber = MAX_TIMES;
        //????????????????????????30???
        if (mTimeArray.length() < recentRunningNumber) {
            LogUtil.d(TAG, "device boot number no enough");
            return false;
        }

        //??????30????????????50%??????????????????
        //???????????????3???????????????
        if (getRecentNetWorkNumber(recentRunningNumber) < recentRunningNumber / 2
                || getRecentNetWorkNumber(3) == 0) {
            return true;
        }
        LogUtil.d(TAG, "recent running offline time no enough");

        return false;
    }

    /**
     *  ????????????N??????????????????
     * @param number ??????N???
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
     * ????????????????????????
     */
    public void showOfflineContent() {
        showContentDialog();
        clearAllInfo();//??????????????????????????????????????????
        PreferenceUtil.getInstance().setOfflineContentShowTime(String.valueOf(System.currentTimeMillis()));//???????????????????????????
        PreferenceUtil.getInstance().setOfflineFloatShowNumber(0);//?????????????????????????????????
        mHandler.postDelayed(mShowFloatTask, FLOAT_DELAY_SHOW_TIME);//????????????dialog????????????????????????
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
                                    if(!checkFileIsExist(url)){//?????????????????????????????????????????????????????????????????????
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
     * desc:??????????????????
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
     * ????????????
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
     * ???????????????????????????
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
                    if(RecorderWin.isOpened()){//??????????????????????????????????????????????????????????????????
                        mHasHideFloat = true;
                        return;
                    }
                    mFloatWindow.show();
                    if(isFirstShow){
                        int num = PreferenceUtil.getInstance().getOfflineFloatShowNumber();
                        num++;
                        PreferenceUtil.getInstance().setOfflineFloatShowNumber(num);//?????????????????????
                        isFirstShow = false;
                    }
                    delayDismissFloatWindow();
                }
            });
        }
    };

    /**
     * desc:??????????????????????????????
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

            // TODO ???????????????????????????
            int time = PreferenceUtil.getInstance().getInt("KEY_OFFLINE_FLOAT_DELAY_TIME", 0);//???????????????
            if (time == 0) {
                time = FLOAT_DELAY_HALF_HOUR_SHOW;
            } else{
                time = time *  60 * 1000;
            }
            LogUtil.d(TAG, "KEY_OFFLINE_FLOAT_DELAY_TIME time:" + time);
            isFirstShow = true;
            //?????????????????????????????????
            mHandler.postDelayed(mShowFloatTask, time);
        }
    };

    /**
     * desc????????????????????????????????????
     *
     * @param type 0,1,2
     */
    public void onClickFeedback(int type) {
        LogUtil.d(TAG, "onClickFeedback TYPE:" + type);
        switch (type) {
            case FEEDBACK_TYPE_CLOSE:
                dismissDialog();//??????????????????
                showFloatWindow();
                break;
            case FEEDBACK_TYPE_UNINTERESTED:
                dismissDialog();//??????????????????
                mHandler.removeCallbacks(mShowFloatTask);
                PreferenceUtil.getInstance().setOfflineUninterestedTime(System.currentTimeMillis());
                break;
            case FEEDBACK_TYPE_ALREADY_SCAN:
                dismissDialog();//??????????????????
                mHandler.removeCallbacks(mShowFloatTask);
                PreferenceUtil.getInstance().setOfflineWXIsBind(true);
                break;
            case FEEDBACK_TYPE_SLIP:
                if(isNeedShowTask){
                    mHandler.removeCallbacks(mShowFloatTask);
                    mHandler.postDelayed(mShowFloatTask, FLOAT_DELAY_SHOW_TIME);//????????????dialog????????????????????????
                }
                break;
            default:
                break;
        }
    }

    /**
     * ????????????????????????????????????????????????
     *
     * @return
     */
    public boolean enableShowContent() {
        //??????????????????????????????
        boolean isBind = PreferenceUtil.getInstance().getOfflineWXIsBind();
        if (isBind) {
            LogUtil.d(TAG, "enableShowContent already bind WX");
            return false;
        }
        //??????????????????????????????
        boolean needShow = PreferenceUtil.getInstance().getOfflineNeedShow();
        if (!needShow) {
            LogUtil.d(TAG, "enableShowContent needShow false");
            return false;
        }

        LogUtil.d(TAG, "enableShowContent");

        return true;
    }

    /**
     * ???????????????????????????????????????
     */
    public void onLicenseActive() {
        //????????????????????????
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
     * ?????????????????????????????????????????????
     */
    private void clearAllInfo() {
        PreferenceUtil.getInstance().setOfflineRecentDayData("");
        PreferenceUtil.getInstance().setOfflineRecentTimeData("");
        //????????????
        //total - record = ????????????
        long totalRunningTime = PreferenceUtil.getInstance().getTotalRunningTime();
        PreferenceUtil.getInstance().setOfflineRecordRunningTime(totalRunningTime);
        //total - record = ??????????????????
        long totalDistance = PreferenceUtil.getInstance().getTotalDrivingDistance();
        PreferenceUtil.getInstance().setOfflineRecordDrivingDistance(totalDistance);
    }

    /**
     * md5(del + md5(business + ????????????)), ??????2??????Asc???
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
    //???????????????
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
        mHintDialog = new OfflinePromoteHintDialog("????????????", "???????????????????????????????????????");
        mHintDialog.show();
        setNeedShowOfflinePromote(false);
    }

    public void showErrorHintDialog(){
        mHintDialog = new OfflinePromoteHintDialog("????????????", "???????????????5?????????24???????????????");
        mHintDialog.show();
        PreferenceUtil.getInstance().setOfflineInputErrorTime(System.currentTimeMillis());
        long totalTime = PreferenceUtil.getInstance().getTotalRunningTime();
        PreferenceUtil.getInstance().setOfflineDisableRunningTime(totalTime);
        PreferenceUtil.getInstance().setOfflineRecordBootNumber(0);
    }

}
