package com.txznet.txz.module.stock;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZStockManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.ui.win.help.HelpHitTispUtil;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.StringUtils;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;

public class StockManager {

    private static StockManager sIntance = new StockManager();

    public static StockManager getInstance() {
        return sIntance;
    }

    public StockManager(){
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {
            @Override
            public void onShow() {
            }

            @Override
            public void onDismiss() {
                mOnTrainFinish = true;
                AppLogic.removeBackGroundCallback(stockSDKTimeOut);
                mTextStockIds.clear();
            }
        });
    }

    private final static String TAG = "StockManager::";

    private StockResult[] mResult = new StockResult[2]; //0存放云知声数据，1存放SDK数据
    private String mRemoteStockTool = null;
    public boolean mOnTrainFinish = false;
    private static final long DEF_TIMEOUT = 2000;
    private ArrayList<String> mTextStockIds = new ArrayList<String>();
    private int taskId = 0;
    private long mTimeout = DEF_TIMEOUT;

    private Runnable1<String> stockSDKTimeOut =  new Runnable1<String>(""){
        @Override
        public void run() {
            mResult[1].taskId = mP1;
            mResult[1].state = StockResult.STATE_ERROR;
            LogUtil.logd(TAG + " SDKData mFlightTimeout " + stockSDKTimeOut);
            checkStockData();
        }
    };

    public byte[] invoke(final String packageName, String command, final byte[] data){
        String cmd = command.substring(TXZStockManager.STOCK_INVOKE_PREFIX.length());
        if (TextUtils.equals(cmd, TXZStockManager.SET_STOCK_TOOL)) {
            mRemoteStockTool = packageName;
        } else if (TextUtils.equals(cmd, TXZStockManager.CLEAR_STOCK_TOOL)) {
            mRemoteStockTool = null;
        } else if (TextUtils.equals(cmd, TXZStockManager.SET_TIMEOUT)) {
            mTimeout = new JSONBuilder(data).getVal("timeout", Long.class, DEF_TIMEOUT);
            LogUtil.logd(TAG + " stockTimeout = " + mTimeout);
        } else if (TextUtils.equals(cmd, TXZStockManager.RESULT_STOCK)) {
            JSONBuilder json = new JSONBuilder(new String((data)));
            String taskId = json.getVal("taskid", String.class);
            if (mTextStockIds.contains(taskId)) {
                LogUtil.logd(TAG + "RESULT_TRAIN");
                AppLogic.removeBackGroundCallback(stockSDKTimeOut);
                if(!parseSDKStock(json.getJSONObject())){
                   LogUtil.logd(TAG + "ERROR_TRAIN");
                   mResult[1].state = StockResult.STATE_ERROR;
                   mResult[1].taskId = taskId;
                   checkStockData();
                   return null;
               }
            }
        }
        else if (TextUtils.equals(cmd, TXZStockManager.ERROR_STOCK)) {
            org.json.JSONObject json = null;
            try {
                json = new org.json.JSONObject(new String(data));
                String taskId = null;
                try {
                    taskId = json.getString("taskid");
                } catch (org.json.JSONException e) {

                }
                if (mTextStockIds.contains(taskId)) {
                    LogUtil.logd(TAG + "ERROR_TRAIN");
                    mResult[1].state = StockResult.STATE_ERROR;
                    mResult[1].taskId = taskId;
                    checkStockData();
                    AppLogic.removeBackGroundCallback(stockSDKTimeOut);
                }
            } catch (org.json.JSONException e) {
                e.printStackTrace();
            }
        } else {
            LogUtil.logd(TAG + "ERROR_TRAIN taskId error");
        }

        return null;
    }

    public boolean parseStock(JSONObject json) {
        try {
            if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_STOCK))
                return true;
            if (!json.containsKey("data")
                    || !json.getJSONObject("data").containsKey("result")) {
                return false;
            }
            mOnTrainFinish = false;
            HelpHitTispUtil.getInstance().hitStockTips();
            String strAnswer = json.getJSONObject("data").getString("header");
            JNIHelper.logd("parse stock text:" + strAnswer);
            VoiceData.StockInfo pbStockInfo = new VoiceData.StockInfo();
            JSONObject jsonData = json.getJSONObject("data").getJSONObject(
                    "result");
            if (jsonData.containsKey("mName")) {
                pbStockInfo.strName = jsonData.getString("mName");
            }
            if (jsonData.containsKey("mCode")) {
                pbStockInfo.strCode = jsonData.getString("mCode");
            }
            if (jsonData.containsKey("mChartImgUrl")) {
                pbStockInfo.strUrl = jsonData.getString("mChartImgUrl");
            }
            if (jsonData.containsKey("mCurrentPrice")) {
                pbStockInfo.strCurrentPrice = jsonData
                        .getString("mCurrentPrice");
            }
            if (jsonData.containsKey("mChangeAmount")) {
                pbStockInfo.strChangeAmount = jsonData
                        .getString("mChangeAmount");
            }
            if (jsonData.containsKey("mChangeRate")) {
                pbStockInfo.strChangeRate = jsonData.getString("mChangeRate");
            }
            if (jsonData.containsKey("mHighestPrice")) {
                pbStockInfo.strHighestPrice = jsonData
                        .getString("mHighestPrice");
            }
            if (jsonData.containsKey("mLowestPrice")) {
                pbStockInfo.strLowestPrice = jsonData.getString("mLowestPrice");
            }
            if (jsonData.containsKey("mtradingVolume")) {
                pbStockInfo.strTradingVolume = jsonData
                        .getString("mtradingVolume");
            }
            if (jsonData.containsKey("mUpdateTime")) {
                pbStockInfo.strUpdateTime = jsonData.getString("mUpdateTime");
            }
            if (jsonData.containsKey("mYesterdayClosingPrice")) {
                pbStockInfo.strYestodayClosePrice = jsonData
                        .getString("mYesterdayClosingPrice");
            }
            String pszPrice = jsonData.getString("mCurrentPrice");
            double price = 0.0;
            if (pszPrice != null)
                price = Double.parseDouble(pszPrice);
            String pszAmount = jsonData.getString("mChangeAmount");
            String pszRate = jsonData.getString("mChangeRate");
            String strStock = null;
            if (pszAmount == null) {
                pszAmount = "";
            }
            if (pszRate == null) {
                pszRate = "";
            }
            if (price <= 0.0) {
                strStock = NativeData
                        .getResString("RS_VOICE_STOCK_INFO_CLOSED");
                if (jsonData.containsKey("mName")) {
                    strStock = strStock.replace("%NAME%", jsonData.getString("mName"));
                }
            } else {
                if (price > 1000) {
                    if (pszAmount.charAt(0) != '-') {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_INC_2");
                    } else {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_DEC_2");
                        if (pszAmount != null && pszAmount.length() > 1) {
                            pszAmount = pszAmount.substring(1);
                        }
                        if (pszRate != null && pszRate.length() > 1) {
                            pszRate = pszRate.substring(1);
                        }
                    }
                } else {
                    if (pszAmount.charAt(0) != '-') {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_INC_2");
                    } else {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_DEC_2");
                        if (pszAmount != null && pszAmount.length() > 1) {
                            pszAmount = pszAmount.substring(1);
                        }
                        if (pszRate != null && pszRate.length() > 1) {
                            pszRate = pszRate.substring(1);
                        }
                    }
                }
                if (jsonData.containsKey("mName")) {
                    strStock = strStock.replace("%NAME%", jsonData.getString("mName"));
                }
                if (pszPrice != null) {
                    strStock = strStock.replace("%VALUE%", formatStockString(pszPrice));
                }
                if(pszAmount != null){
                    strStock = strStock.replace("%AMOUNT%", formatStockString(pszAmount));
                }
                if(pszRate != null){
                    strStock = strStock.replace("%RATE%", formatStockString(pszRate));
                }

            }
            if (jsonData.containsKey("mTodayOpeningPrice")) {
                pbStockInfo.strTodayOpenPrice = jsonData
                        .getString("mTodayOpeningPrice");
            }
            mResult[0] = new StockResult();
            mResult[0].state = StockResult.STATE_SUCCESS;
            mResult[0].strStock = strStock;
            mResult[0].stockInfo = pbStockInfo;
            mResult[0].taskId = taskId+"";
            if(mRemoteStockTool != null){
                mResult[1] = new StockResult();
                mResult[1].state = StockResult.STATE_REQUEST;
                mResult[1].taskId = taskId+"";
                JSONBuilder jsonBuilder = new JSONBuilder();
                jsonBuilder.put("taskid", taskId+"");
                jsonBuilder.put("stockCode", pbStockInfo.strCode );
                ServiceManager.getInstance().sendInvoke(mRemoteStockTool, TXZStockManager.STOCK_CMD_PREFIX + TXZStockManager.REQUEST_STOCK, jsonBuilder.toBytes(), null);
                AppLogic.removeBackGroundCallback(stockSDKTimeOut);
                stockSDKTimeOut.update(taskId+"");
                AppLogic.runOnBackGround(stockSDKTimeOut, mTimeout);
            }
            mTextStockIds.add(taskId+"");
            taskId++;
            checkStockData();

        } catch (Exception e) {
            JNIHelper.loge(e.getMessage());
            JNIHelper
                    .loge("TextSemanticAnalysis parseStock NumberFormatException");
            MonitorUtil.monitorCumulant("text.parse.E.stock");
            return false;
        }
        return true;
    }

    public boolean parseSDKStock(org.json.JSONObject json) {
        try {
            if (!json.has("data")
                    || !json.getJSONObject("data").has("result")) {
                return false;
            }
            HelpHitTispUtil.getInstance().hitStockTips();
            VoiceData.StockInfo pbStockInfo = new VoiceData.StockInfo();
            org.json.JSONObject jsonData = json.getJSONObject("data").getJSONObject(
                    "result");
            if (jsonData.has("mName")) {
                pbStockInfo.strName = jsonData.getString("mName");
            }
            if (jsonData.has("mCode")) {
                pbStockInfo.strCode = jsonData.getString("mCode");
            }
            if (jsonData.has("mChartImgUrl")) {
                pbStockInfo.strUrl = jsonData.getString("mChartImgUrl");
            }
            if (jsonData.has("mCurrentPrice")) {
                pbStockInfo.strCurrentPrice = jsonData
                        .getString("mCurrentPrice");
            }
            if (jsonData.has("mChangeAmount")) {
                pbStockInfo.strChangeAmount = jsonData
                        .getString("mChangeAmount");
            }
            if (jsonData.has("mChangeRate")) {
                pbStockInfo.strChangeRate = jsonData.getString("mChangeRate");
            }
            if (jsonData.has("mHighestPrice")) {
                pbStockInfo.strHighestPrice = jsonData
                        .getString("mHighestPrice");
            }
            if (jsonData.has("mLowestPrice")) {
                pbStockInfo.strLowestPrice = jsonData.getString("mLowestPrice");
            }
            if (jsonData.has("mtradingVolume")) {
                pbStockInfo.strTradingVolume = jsonData
                        .getString("mtradingVolume");
            }
            if (jsonData.has("mUpdateTime")) {
                pbStockInfo.strUpdateTime = jsonData.getString("mUpdateTime");
            }
            if (jsonData.has("mYesterdayClosingPrice")) {
                pbStockInfo.strYestodayClosePrice = jsonData
                        .getString("mYesterdayClosingPrice");
            }
            String pszPrice = jsonData.getString("mCurrentPrice");
            double price = 0.0;
            if (pszPrice != null)
                price = Double.parseDouble(pszPrice);
            String pszAmount = jsonData.getString("mChangeAmount");
            String pszRate = jsonData.getString("mChangeRate");
            String strStock = null;
            if (pszAmount == null) {
                pszAmount = "";
            }
            if (pszRate == null) {
                pszRate = "";
            }
            if (price <= 0.0) {
                strStock = NativeData
                        .getResString("RS_VOICE_STOCK_INFO_CLOSED");
                if (jsonData.has("mName")) {
                    strStock = strStock.replace("%NAME%", jsonData.getString("mName"));
                }
            } else {
                if (price > 1000) {
                    if (pszAmount.charAt(0) != '-') {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_INC_2");
                    } else {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_DEC_2");
                        if (pszAmount != null && pszAmount.length() > 1) {
                            pszAmount = pszAmount.substring(1);
                        }
                        if (pszRate != null && pszRate.length() > 1) {
                            pszRate = pszRate.substring(1);
                        }
                    }
                } else {
                    if (pszAmount.charAt(0) != '-') {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_INC_2");
                    } else {
                        strStock = NativeData
                                .getResString("RS_VOICE_STOCK_INFO_DEC_2");
                        if (pszAmount != null && pszAmount.length() > 1) {
                            pszAmount = pszAmount.substring(1);
                        }
                        if (pszRate != null && pszRate.length() > 1) {
                            pszRate = pszRate.substring(1);
                        }
                    }
                }
                if (jsonData.has("mName")) {
                    strStock = strStock.replace("%NAME%", jsonData.getString("mName"));
                }
                if (pszPrice != null) {
                    strStock = strStock.replace("%VALUE%", formatStockString(pszPrice));
                }
                if(pszAmount != null){
                    strStock = strStock.replace("%AMOUNT%", formatStockString(pszAmount));
                }
                if(pszRate != null){
                    strStock = strStock.replace("%RATE%", formatStockString(pszRate));
                }

            }
            if (jsonData.has("mTodayOpeningPrice")) {
                pbStockInfo.strTodayOpenPrice = jsonData
                        .getString("mTodayOpeningPrice");
            }
            if(mResult[1] != null){
                mResult[1].state = StockResult.STATE_SUCCESS;
                mResult[1].stockInfo = pbStockInfo;
                mResult[1].strStock = strStock;
                checkStockData();
                return true;
            }

        } catch (Exception e) {
            JNIHelper.loge(e.getMessage());
            JNIHelper
                    .loge("TextSemanticAnalysis parseStock NumberFormatException");
            MonitorUtil.monitorCumulant("text.parse.E.stock");
            return false;
        }
        return false;
    }

    void checkStockData(){
        if (!mOnTrainFinish) {
            if (mRemoteStockTool == null){
                if(mResult[0].state == StockResult.STATE_SUCCESS){
                   sendData(mResult[0].strStock, mResult[0].stockInfo);
                    mTextStockIds.remove(mResult[0].taskId);
                }
            }else {
                switch (mResult[1].state){
                    case StockResult.STATE_REQUEST:break;//等待
                    case StockResult.STATE_SUCCESS:
                        if(!mTextStockIds.contains(mResult[1].taskId)){
                            return;
                        }
                        mTextStockIds.remove((mResult[1].taskId));
                        sendData(mResult[1].strStock, mResult[1].stockInfo);
                        break;
                    case StockResult.STATE_ERROR:
                        if(!mTextStockIds.contains(mResult[1].taskId)){
                            return;
                        }
                        sendData(mResult[0].strStock, mResult[0].stockInfo);
                        mTextStockIds.remove((mResult[0].taskId));
                        break;
                }
            }
        }
    }

    private void sendData(String strStock,VoiceData.StockInfo pbStockInfo){
        speakWords(strStock, false);
        JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
                VoiceData.SUBEVENT_VOICE_SHOW_STOCK_INFO, pbStockInfo);
    }

    private String formatStockString(String price){
        if (TextUtils.isEmpty(price)) {
            return "";
        }
        return StringUtils.subZeroAndDot(String.format("%.2f", Float.parseFloat(price)));
    }

    /**
     /**
     * 语音播报
     *
     * @param strWords
     * @param closeRecord
     */
    private void speakWords(String strWords, boolean closeRecord) {
        if (closeRecord) {
            AsrManager.getInstance().setNeedCloseRecord(true);
        }
        JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
                VoiceData.SUBEVENT_VOICE_SPEAK_WORDS_CLOSE_RECORD, strWords);
    }

    class StockResult {
        /**
         * 默认状态，没有使用
         */
        static final int STATE_NONE = 0;
        /**
         * 请求中状态
         */
        static final int STATE_REQUEST = 1;
        /**
         * 成功状态
         */
        static final int STATE_SUCCESS = 2;
        /**
         * 失败状态
         */
        static final int STATE_ERROR = 3;
        String taskId = null;
        int state = STATE_NONE;
        String strStock = null;
        VoiceData.StockInfo stockInfo = null;
    }

}
