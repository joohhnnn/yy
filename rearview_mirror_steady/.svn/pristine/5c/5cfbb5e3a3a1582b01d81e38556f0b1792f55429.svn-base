package com.txznet.txz.module.constellation;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.NetworkUtil;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.asr.IAsr;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.service.TXZPowerControl;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.PreferenceUtil;

import org.json.JSONException;

import java.util.Random;

import static com.txz.ui.equipment.UiEquipment.SUBEVENT_REQ_HOROSCOPE_FORECAST_SWITCH;
import static com.txz.ui.equipment.UiEquipment.SUBEVENT_REQ_INIT_SUCCESS;
import static com.txz.ui.equipment.UiEquipment.SUBEVENT_RESP_GET_HOROSCOPE_FORECAST;
import static com.txz.ui.equipment.UiEquipment.SUBEVENT_RESP_HOROSCOPE_FORECAST_SWITCH;
import static com.txz.ui.equipment.UiEquipment.SUBEVENT_RESP_SET_CONSTELLATION;
import static com.txz.ui.event.UiEvent.EVENT_ACTION_EQUIPMENT;

public class ConstellationManager extends IModule {
    private static ConstellationManager sConstellationManager = new ConstellationManager();
    private int mTtsTaskId;

    private ConstellationManager() {
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {

            @Override
            public void onShow() {
                if (mConstellationViewManager != null) {
                    mConstellationViewManager.dismiss();
                }
            }

            @Override
            public void onDismiss() {
                mIsInAsr = false;
                TtsManager.getInstance().cancelSpeak(mTtsTaskId);
            }
        });
    }

    public static ConstellationManager getInstance() {
        return sConstellationManager;
    }

    @Override
    public int initialize_AfterInitSuccess() {
        regCommand("CMD_OPEN_CONSTELLATION_FORTUNE_PUSH");
        regCommand("CMD_CLOSE_CONSTELLATION_FORTUNE_PUSH");
        regEvent(EVENT_ACTION_EQUIPMENT, SUBEVENT_RESP_GET_HOROSCOPE_FORECAST);
        regEvent(EVENT_ACTION_EQUIPMENT, SUBEVENT_RESP_SET_CONSTELLATION);
        regEvent(EVENT_ACTION_EQUIPMENT, SUBEVENT_REQ_INIT_SUCCESS);
        regEvent(EVENT_ACTION_EQUIPMENT, SUBEVENT_RESP_HOROSCOPE_FORECAST_SWITCH);
        return super.initialize_AfterInitSuccess();
    }


    private Runnable mNetworkTimeoutRunnable = new Runnable() {
        @Override
        public void run() {
            if (RecorderWin.isOpened()) {
                String tts =
                        NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW");
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(tts, null);
            }
        }
    };

    @Override
    public int onCommand(String cmd) {
        if ("CMD_OPEN_CONSTELLATION_FORTUNE_PUSH".equals(cmd)) {
            // 用户是否已经开启星座推送
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                mCurrentAction = ACTION_JUDGE_CONSTELLATION_PUSH_STATE;
                getHoroscopeForecast();
                AppLogic.runOnUiGround(mNetworkTimeoutRunnable, 10000);
            } else {
                String tts =
                        NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW");
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(tts, null);
            }

        } else if ("CMD_CLOSE_CONSTELLATION_FORTUNE_PUSH".equals(cmd)) {
            if (NetworkUtil.isNetworkAvailable(GlobalContext.get())) {
                mCurrentAction = ACTION_CLOSE_CONSTELLATION_PUSH;
                setConstellationPushState(FLAG_CLOSE);
                AppLogic.runOnUiGround(mNetworkTimeoutRunnable, 10000);
            } else {
                String tts =
                        NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_NEW");
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(tts, null);
            }
        }
        return super.onCommand(cmd);
    }

    private void setConstellationPushState(int state) {
        UiEquipment.Req_HoroscopeForecastCfg reqHoroscopeForecastCfg =
                new UiEquipment.Req_HoroscopeForecastCfg();
        org.json.JSONObject jsonObject = new org.json.JSONObject();
        try {
            jsonObject.put("horoscope_forecast_switch", state);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        reqHoroscopeForecastCfg.strJson = jsonObject.toString().getBytes();
        JNIHelper.sendEvent(EVENT_ACTION_EQUIPMENT, SUBEVENT_REQ_HOROSCOPE_FORECAST_SWITCH,
                reqHoroscopeForecastCfg);
    }

    private boolean mIsInAsr;

    public boolean isInAsr() {
        return mIsInAsr;
    }

    public boolean handleAsrResult(String strRawText) {
        mIsInAsr = false;
        boolean flag = false;
        LogUtil.e("skyward strText" + strRawText);
        for (int i = 0; i < CONSTELLATION_ARRAYS.length; i++) {
            if (CONSTELLATION_ARRAYS[i].equals(strRawText)) {
                flag = true;
                break;
            }
        }
        if (flag) {
            UiEquipment.Req_Constellation reqConstellation = new UiEquipment.Req_Constellation();
            org.json.JSONObject param = new org.json.JSONObject();
            try {
                param.put("constellation", strRawText);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            reqConstellation.strJson = param.toString().getBytes();
            mConstellation = strRawText;
            mCurrentAction = ACTION_SET_CONSTELLATION;
            JNIHelper.sendEvent(EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_SET_CONSTELLATION,
                    reqConstellation);
        }
        return flag;
    }


    private String mConstellation = PreferenceUtil.getInstance().getString(KEY_CONSTELLATION, "");

    private int mCurrentAction = -1;

    /**
     * 关闭星座运势推送
     */
    private static final int ACTION_CLOSE_CONSTELLATION_PUSH = 1;
    /**
     * 开启星座运势推送
     */
    private static final int ACTION_OPEN_CONSTELLATION_PUSH = 2;
    /**
     * 设置星座
     */
    private static final int ACTION_SET_CONSTELLATION = 3;
    /**
     * 判断星座运势推送是否开启
     */
    private static final int ACTION_JUDGE_CONSTELLATION_PUSH_STATE = 4;
    /**
     * 判断用户是否设置星座
     */
    private static final int ACTION_JUDGE_HAD_SET_CONSTELLATION = 5;

    /**
     * 完成
     */
    private static final int STATE_FINISH = 0;
    /**
     * 没有用户星座信息
     */
    private static final int STATE_NO_CONSTELLATION_USER_DATA = 1;
    /**
     * 今天已经推送过
     */
    private static final int STATE_HAD_PUSH = 2;
    /**
     * 没有星座运势数据
     */
    private static final int STATE_NO_CONSTELLATION_DATA = 3;
    /**
     * 用户没有开启星座推送
     */
    private static final int STATE_CONSTELLATION_PUSH_CLOSE = 4;

    private static final int FLAG_OPEN = 1;
    private static final int FLAG_CLOSE = 0;
    private static final String KEY_CONSTELLATION = "key_constellation";

    public void saveConstellation(String constellation) {
        mConstellation = constellation;
        PreferenceUtil.getInstance().setString(KEY_CONSTELLATION, mConstellation);
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        if (eventId == UiEvent.EVENT_ACTION_EQUIPMENT) {
            if (subEventId == SUBEVENT_RESP_SET_CONSTELLATION) {
                AppLogic.removeUiGroundCallback(mNetworkTimeoutRunnable);
                try {
                    UiEquipment.Resp_Constellation respConstellation =
                            UiEquipment.Resp_Constellation.parseFrom(data);
                    LogUtil.e("skyward uint32ErrCode" + respConstellation.uint32ErrCode);
                    LogUtil.e("skyward mCurrentAction" + mCurrentAction);
                    PreferenceUtil.getInstance().setString(KEY_CONSTELLATION, mConstellation);
                    if (respConstellation.uint32ErrCode == 0 &&
                            mCurrentAction == ACTION_SET_CONSTELLATION) {
                        mCurrentAction = ACTION_OPEN_CONSTELLATION_PUSH;
                        setConstellationPushState(FLAG_OPEN);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (subEventId == SUBEVENT_REQ_INIT_SUCCESS) {
                getHoroscopeForecast();
            } else if (subEventId == SUBEVENT_RESP_GET_HOROSCOPE_FORECAST) {
                AppLogic.removeUiGroundCallback(mNetworkTimeoutRunnable);
                try {
                    UiEquipment.Resp_HoroscopeForecast respHoroscopeForecast =
                            UiEquipment.Resp_HoroscopeForecast.parseFrom(data);
                    LogUtil.d("skyward uint32ErrCode" + respHoroscopeForecast.uint32ErrCode);
                    if (respHoroscopeForecast.uint32ErrCode != 0 ||
                            respHoroscopeForecast.strJson == null ||
                            respHoroscopeForecast.strJson.length == 0) {
                        AppLogic.runOnUiGround(mNetworkTimeoutRunnable);
                        return super.onEvent(eventId, subEventId, data);
                    }
                    LogUtil.d("skyward strJson" + new String(respHoroscopeForecast.strJson));
                    LogUtil.d("skyward mCurrentAction" + mCurrentAction);
                    org.json.JSONObject jsonObject =
                            new org.json.JSONObject(new String(respHoroscopeForecast.strJson));
                    int status = -1;
                    String name = "";
                    String desc = "";
                    String type = "";
                    int level = 0;
                    if (jsonObject.has("status")) {
                        status = jsonObject.getInt("status");
                    }
                    if (jsonObject.has("name")) {
                        name = jsonObject.getString("name");
                    }
                    if (jsonObject.has("desc")) {
                        desc = jsonObject.getString("desc");
                    }
                    if (jsonObject.has("level")) {
                        level = jsonObject.getInt("level");
                    }
                    if (jsonObject.has("type")) {
                        type = jsonObject.getString("type");
                    }
                    switch (mCurrentAction) {
                        case ACTION_JUDGE_CONSTELLATION_PUSH_STATE:
                            if (status == STATE_CONSTELLATION_PUSH_CLOSE) {
                                mCurrentAction = ACTION_JUDGE_HAD_SET_CONSTELLATION;
                                setConstellationPushState(FLAG_OPEN);
                            } else if (status == STATE_NO_CONSTELLATION_USER_DATA) {
                                setConstellation();
                            } else {
                                AsrManager.getInstance().setNeedCloseRecord(true);
                                RecorderWin.speakTextWithClose(
                                        "已开启星座运势推送，我将为您每天推送" + mConstellation + "运势", null);
                            }
                            break;
                        case ACTION_JUDGE_HAD_SET_CONSTELLATION:
                            if (status == STATE_NO_CONSTELLATION_USER_DATA) {
                                setConstellation();
                            } else if (status == STATE_HAD_PUSH || status == STATE_FINISH) {
                                AsrManager.getInstance().setNeedCloseRecord(true);
                                RecorderWin.speakTextWithClose(
                                        "已开启星座运势推送，我将为您每天推送" + mConstellation + "运势", null);
                            }
                            break;
                        default:
                            if (status == STATE_FINISH &&
                                    respHoroscopeForecast.uint32ErrCode == 0) {
                                boolean isEnterReverse = TXZPowerControl.isEnterReverse();
                                LogUtil.d("isEnterReverse:" + isEnterReverse);
                                if(!TXZPowerControl.isEnterReverse()) {// 倒车判断
                                    showTodayFortune(name, level, desc, type);
                                }
                            }
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (subEventId == SUBEVENT_RESP_HOROSCOPE_FORECAST_SWITCH) {
                try {
                    UiEquipment.Resp_HoroscopeForecastCfg respHoroscopeForecastCfg =
                            UiEquipment.Resp_HoroscopeForecastCfg.parseFrom(data);
                    LogUtil.e("skyward uint32ErrCode" + respHoroscopeForecastCfg.uint32ErrCode);
                    LogUtil.e("skyward mCurrentAction" + mCurrentAction);
                    AppLogic.removeUiGroundCallback(mNetworkTimeoutRunnable);
                    switch (mCurrentAction) {
                        case ACTION_OPEN_CONSTELLATION_PUSH: {
                            String tts;
                            if (respHoroscopeForecastCfg.uint32ErrCode == 0) {
                                tts = "已开启星座运势推送，我将为您每天推送" + mConstellation + "运势";
                            } else {
                                tts = "开启星座运势推送发生异常，请检查网络是否正常";
                            }
                            AsrManager.getInstance().setNeedCloseRecord(true);
                            RecorderWin.speakTextWithClose(tts, null);
                            break;
                        }
                        case ACTION_CLOSE_CONSTELLATION_PUSH: {
                            String tts;
                            if (respHoroscopeForecastCfg.uint32ErrCode == 0) {
                                tts = "已关闭星座运势推送";
                            } else {
                                tts = "关闭星座运势推送发生异常，请检查网络是否正常";
                            }
                            AsrManager.getInstance().setNeedCloseRecord(true);
                            RecorderWin.speakTextWithClose(tts, null);
                            break;
                        }
                        case ACTION_JUDGE_HAD_SET_CONSTELLATION: {
                            String tts;
                            if (respHoroscopeForecastCfg.uint32ErrCode == 0) {
                                getHoroscopeForecast();
                            } else {
                                tts = "开启星座运势推送发生异常，请检查网络是否正常";
                                AsrManager.getInstance().setNeedCloseRecord(true);
                                RecorderWin.speakTextWithClose(tts, null);
                            }

                            break;
                        }
                        default:
                            LogUtil.e("skyward " + mCurrentAction);
                            break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    private void getHoroscopeForecast() {
        JNIHelper
                .sendEvent(EVENT_ACTION_EQUIPMENT, UiEquipment.SUBEVENT_REQ_GET_HOROSCOPE_FORECAST);
    }

    private void setConstellation() {
        AsrManager.getInstance().setNeedCloseRecord(false);
        RecorderWin.speakTextWithClose("请问您是什么星座", false, new Runnable() {
            @Override
            public void run() {
                mIsInAsr = true;
                AsrManager.getInstance().start(new IAsr.IAsrCallback() {
                    @Override
                    public void onCancel(IAsr.AsrOption option) {
                        mIsInAsr = false;
                        super.onCancel(option);
                    }

                    @Override
                    public void onError(IAsr.AsrOption option, int error, String desc,
                            String speech, int error2) {
                        mIsInAsr = false;
                        super.onError(option, error, desc, speech, error2);
                    }
                });
            }
        });
    }

    public boolean handleResult(JSONObject json) {
        String strAction = json.getString("action");
        if ("scope".equals(strAction)) {
            String name = json.getString("name");
            String fortuneType = json.getString("fortune_type");
            int level = json.getIntValue("level");
            String desc = json.getString("desc");
            AsrManager.getInstance().setNeedCloseRecord(false);
            mTtsTaskId = TtsManager.getInstance().speakText(desc, new TtsUtil.ITtsCallback() {
                @Override
                public void onSuccess() {
                    super.onSuccess();
                    AsrManager.getInstance().start();
                }

                @Override
                public boolean isNeedStartAsr() {
                    return true;
                }
            });
            showConstellationFortune(level, fortuneType, name, desc);
        } else if ("fast_fate".equals(strAction)) {
            String matchName = json.getString("match_name");
            String name = json.getString("name");
            int level = json.getIntValue("level");
            String desc = json.getString("desc");
            String answer = json.getString("answer");
            AsrManager.getInstance().setNeedCloseRecord(false);
            if (TextUtils.isEmpty(matchName)) {
                RecorderWin.speakText(answer, null);
            } else {
                mTtsTaskId = TtsManager.getInstance().speakText(desc, new TtsUtil.ITtsCallback() {
                    @Override
                    public void onSuccess() {
                        super.onSuccess();
                        AsrManager.getInstance().start();
                    }

                    @Override
                    public boolean isNeedStartAsr() {
                        return true;
                    }
                });
                showConstellationMatching(matchName, name, level, desc);
            }
        } else if ("info".equals(strAction)) {
            AsrManager.getInstance().setNeedCloseRecord(false);
            String answer = json.getString("answer");
            RecorderWin.speakTextWithClose(answer, null);
        }
        return true;
    }

    Random mRandom = new Random();

    private void showConstellationFortune(int level, String fortuneType, String name, String desc) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("type", 10);
            json.put("level", level);
            json.put("fortuneType", fortuneType);
            json.put("name", name);
            json.put("desc", desc);
            String[] tipsConstellationFortunes =
                    NativeData.getResStringArray("RS_TIPS_CONSTELLATION");
            json.put("vTips", tipsConstellationFortunes[Math.abs(mRandom.nextInt()) %
                    (tipsConstellationFortunes.length - 1)]);
            RecorderWin.showConstellationFortune(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void showConstellationMatching(String matchName, String name, int level, String desc) {
        try {
            org.json.JSONObject json = new org.json.JSONObject();
            json.put("type", 11);
            json.put("level", level);
            json.put("matchName", matchName);
            json.put("name", name);
            json.put("desc", desc);
            String[] tipsConstellationMatching =
                    NativeData.getResStringArray("RS_TIPS_CONSTELLATION");
            json.put("vTips", tipsConstellationMatching[Math.abs(mRandom.nextInt()) %
                    (tipsConstellationMatching.length - 1)]);
            RecorderWin.showConstellationMatching(json.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    public static final String[] CONSTELLATION_ARRAYS = new String[]{
            "白羊座", "金牛座", "双子座", "巨蟹座", "处女座", "狮子座",
            "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座"
    };

    private ConstellationViewManager mConstellationViewManager;


    private int mShowTodayFortuneTtsTaskId;

    public void showTodayFortune(final String name, final int level, final String desc,
            final String type) {
        if (!TextUtils.isEmpty(mConstellationTool)) {
            org.json.JSONObject jsonObject = new org.json.JSONObject();
            try {
                jsonObject.put("level", level);
                jsonObject.put("name", name);
                jsonObject.put("desc", desc);
                jsonObject.put("type", type);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            ServiceManager.getInstance().sendInvoke(mConstellationTool,
                    "tool.constellation.push.show", jsonObject.toString().getBytes(), null);
            return;
        }
        if (mConstellationViewManager == null) {
            mConstellationViewManager = new ConstellationViewManager(GlobalContext.get());
        }


        mShowTodayFortuneTtsTaskId =
                TtsManager.getInstance().speakText(desc, new TtsUtil.ITtsCallback() {
                    @Override
                    public void onBegin() {
                        super.onBegin();
                        mConstellationViewManager
                                .updateView(name, level, type);
                        LogUtil.d(TAG, "mShowTodayFortuneTtsTaskId " + mShowTodayFortuneTtsTaskId);
                        RecorderWin.close();
                        mConstellationViewManager.show();
                    }

                    @Override
                    public void onEnd() {
                        super.onEnd();
                        mConstellationViewManager.dismiss();
                    }
                });
        LogUtil.d(TAG, "mShowTodayFortuneTtsTaskId " + mShowTodayFortuneTtsTaskId);
        mConstellationViewManager.setOnDismissListener(
                new ConstellationViewManager.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        LogUtil.d(TAG, "mShowTodayFortuneTtsTaskId " + mShowTodayFortuneTtsTaskId);
                        TtsManager.getInstance().cancelSpeak(mShowTodayFortuneTtsTaskId);
                    }
                });

    }

    private static final String TAG = "ConstellationManager";
    private String mConstellationTool = "";

    public byte[] invokeConstellation(final String packageName, String command, byte[] data) {
        if (command.equals("txz.constellation.tool.set")) {
            mConstellationTool = packageName;
        } else if (command.equals("txz.constellation.tool.clear")) {
            mConstellationTool = "";
        } else if (command.equals("txz.constellation.cancel")) {
            if (mConstellationViewManager != null) {
                mConstellationViewManager.dismiss();
            }
        }
        return null;
    }


}
