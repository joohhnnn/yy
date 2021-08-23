package com.txznet.txz.module.weather;

import android.text.TextUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.ReportUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZWeatherManager;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.music.MusicManager;
import com.txznet.txz.module.netdata.NetDataManager;
import com.txznet.txz.module.sence.SenceManager;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ASUS User on 2018/8/6.
 */

public class WeatherManager extends IModule {

    private static final String TAG = "weather : ";

    private static WeatherManager sManager = new WeatherManager();
    private JSONObject mAsrWeatherJson;
    private ArrayList<String> mTextWeatherTaskIds = new ArrayList<String>();
    private String mRemoteWeatherTool = null;
    private WeatherResult[] mResult = new WeatherResult[2]; //0存放后台天气数据，1存放SDK天气数据
    private static final long DEF_TIMEOUT = 2000;
    private long mTimeout = DEF_TIMEOUT;
    public boolean mOnFinish = false;

    private WeatherManager() {
        RecorderWin.OBSERVABLE.registerObserver(new RecorderWin.StatusObervable.StatusObserver() {
            @Override
            public void onShow() {

            }

            @Override
            public void onDismiss() {
                mOnFinish = true;
                AppLogic.removeBackGroundCallback(timeoutRunnable);
                mTextWeatherTaskIds.clear();
            }
        });
    }

    public static WeatherManager getInstance() {
        return sManager;
    }

    public void parseWeather(VoiceData.VoiceParseData rawVoice, JSONObject json) {
        mOnFinish = false;
        mAsrWeatherJson = json;
        String city = json.getString("city");
        String date = json.getString("date");
        String taskId = NetDataManager.getInstance().procWeatherBackgroundInner(city, date, rawVoice.strText);
        mResult[0] = new WeatherResult();
        mResult[0].state = WeatherResult.STATE_REQUEST;
        if (mRemoteWeatherTool != null) {
            mResult[1] = new WeatherResult();
            mResult[1].state = WeatherResult.STATE_REQUEST;
            JSONBuilder jsonBuilder = new JSONBuilder();
            jsonBuilder.put("taskid", taskId);
            jsonBuilder.put("city", city);
            jsonBuilder.put("date", date);
            jsonBuilder.put("text", rawVoice.strText);
            ServiceManager.getInstance().sendInvoke(mRemoteWeatherTool, TXZWeatherManager.WEATHER_CMD_PREFIX + TXZWeatherManager.REQUEST_WEATHER, jsonBuilder.toBytes(), null);
            AppLogic.removeBackGroundCallback(timeoutRunnable);
            timeoutRunnable.update(taskId);
            AppLogic.runOnBackGround(timeoutRunnable, mTimeout);
        }

        mTextWeatherTaskIds.add(taskId);
    }

    /**
     * 请求天气出错的情况下使用原始的语义
     */
    public void handleWeatherError() {
        handleWeatherData(mAsrWeatherJson);
    }

    private synchronized void checkResult() {
        if (!mOnFinish) {
            if (mRemoteWeatherTool == null || mResult[1] == null) {
                if (mResult[0].state == WeatherResult.STATE_SUCCESS) {
                    mTextWeatherTaskIds.remove(mResult[0].taskId);
                    handleWeatherData(mResult[0].weatherData);
                } else {
                    //移除前面的
                    mTextWeatherTaskIds.remove(0);
                    handleWeatherError();
                }
            } else {
                switch (mResult[1].state) {
                    case WeatherResult.STATE_REQUEST:
                        //等待
                        break;
                    case WeatherResult.STATE_SUCCESS:
                        mTextWeatherTaskIds.remove(mResult[1].taskId);
                        handleWeatherData(mResult[1].weatherData);
                        break;
                    case WeatherResult.STATE_ERROR:
                        if (mResult[0].state == WeatherResult.STATE_SUCCESS) {
                            mTextWeatherTaskIds.remove(mResult[0].taskId);
                            handleWeatherData(mResult[0].weatherData);
                        } else if (mResult[0].state == WeatherResult.STATE_ERROR) {
                            mTextWeatherTaskIds.remove(mResult[1].taskId);
                            handleWeatherError();
                        }
                        break;
                }
            }
        }
    }

    class WeatherResult {
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
        JSONObject weatherData = null;
    }


    private Runnable1<String> timeoutRunnable = new Runnable1<String>("") {
        @Override
        public void run() {
            mResult[1].taskId = mP1;
            mResult[1].state = WeatherResult.STATE_ERROR;
            checkResult();
        }
    };


    public boolean handleWeatherData(UiEquipment.Resp_Weather weatherResult) {
        if (mTextWeatherTaskIds.isEmpty()) {
            return false;
        }
        if (weatherResult == null || weatherResult.weather == null || weatherResult.weather.length == 0) {
            mResult[0].state = WeatherResult.STATE_ERROR;
            checkResult();
            return true;
        }

        JSONObject json = JSONObject.parseObject(new String(weatherResult.weather));
        String taskid = json.getString("taskid");
        String pkg = json.getString("pkg");
        JNIHelper.logd("parseWeather json:" + taskid + json.toString());
        if (TextUtils.equals(pkg, "core_com.txznet.txz")) {

            if (mTextWeatherTaskIds.contains(taskid)) {
                json.remove("taskid");
                json.remove("pkg");
                json.put("text", mAsrWeatherJson.getString("text"));

                mResult[0].taskId = taskid;
                mResult[0].state = WeatherResult.STATE_SUCCESS;
                mResult[0].weatherData = json;
                checkResult();
                return true;
            }
        }
        return false;
    }

    public void handleWeatherData(JSONObject json) {
        mOnFinish = true;
        if (procSenceByRemote("weather", json.toString())) {
            return;
        }
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_WEATHER))
            return;
        if (!json.containsKey("data")) {
            JNIHelper.logd("parseWeather json:" + json);
            speakWords(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), false);
            return;
        }

        if (!json.getJSONObject("data").containsKey("result")) {
            if (!json.getJSONObject("data").containsKey("header")) {
                speakWords(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), false);
                return;
            } else {
                String strHeader = json.getJSONObject("data").getString(
                        "header");
                speakWords(strHeader, false);
                return;
            }
        }
        String strAnswer = json.getJSONObject("data").getString("header");
        String pattern = ".*[0-9]至.*";
        if(strAnswer.matches(pattern)){
            int index = strAnswer.indexOf("至");
            if (index>-1) {
                strAnswer = strAnswer.subSequence(0, index) + "℃"+strAnswer.subSequence(index, strAnswer.length());
            }
        }
        VoiceData.WeatherInfos pbWeatherInfos = new VoiceData.WeatherInfos();
        JSONObject jsonData = json.getJSONObject("data")
                .getJSONObject("result");
        if (jsonData.containsKey("cityName")) {
            pbWeatherInfos.strCityName = jsonData.getString("cityName");
        }else if(json.containsKey("city")){
            pbWeatherInfos.strCityName = json.getString("city");
        }
        if (jsonData.containsKey("focusDateIndex")) {
            pbWeatherInfos.uint32FocusIndex = jsonData
                    .getInteger("focusDateIndex");
        }
        JSONArray jsonWeathers = jsonData.getJSONArray("weatherDays");

        pbWeatherInfos.rptMsgWeather = new VoiceData.WeatherData[jsonWeathers.size()];
        for (int i = 0; i < jsonWeathers.size(); i++) {
            JSONObject jsonWeather = jsonWeathers.getJSONObject(i);
            VoiceData.WeatherData pbWeatherData = new VoiceData.WeatherData();
            if (jsonWeather.containsKey("year")) {
                pbWeatherData.uint32Year = jsonWeather.getInteger("year");
            }
            if (jsonWeather.containsKey("month")) {
                pbWeatherData.uint32Month = jsonWeather.getInteger("month");
            }
            if (jsonWeather.containsKey("day")) {
                pbWeatherData.uint32Day = jsonWeather.getInteger("day");
            }
            if (jsonWeather.containsKey("dayOfWeek")) {
                pbWeatherData.uint32DayOfWeek = jsonWeather
                        .getInteger("dayOfWeek");
            }
            if (jsonWeather.containsKey("currentTemperature")) {
                pbWeatherData.int32CurTemperature = jsonWeather
                        .getInteger("currentTemperature");
            }
            if (jsonWeather.containsKey("highestTemperature")) {
                pbWeatherData.int32HighTemperature = jsonWeather
                        .getInteger("highestTemperature");
            }
            if (jsonWeather.containsKey("lowestTemperature")) {
                pbWeatherData.int32LowTemperature = jsonWeather
                        .getInteger("lowestTemperature");
            }
            if (jsonWeather.containsKey("AQI")) {
                pbWeatherData.int32Pm25 = jsonWeather.getInteger("AQI");//AQI才是实际的空气质量指数
            }
            if (jsonWeather.containsKey("quality")) {
                pbWeatherData.strAirQuality = jsonWeather.getString("quality");
            }
            //if (jsonWeather.containsKey("lowestAQIDesc")) {
            //    if (!TextUtils.isEmpty(jsonWeather.getString("lowestAQIDesc"))) {
            //        pbWeatherData.strAirQuality = jsonWeather.getString("lowestAQIDesc");
            //        if (jsonWeather.containsKey("lowestAQI")) {
            //            pbWeatherData.int32Pm25 = jsonWeather.getInteger("lowestAQI");
            //        }
            //    }
            //}
            if (jsonWeather.containsKey("weather")) {
                pbWeatherData.strWeather = jsonWeather.getString("weather");
            }
            if (jsonWeather.containsKey("wind")) {
                pbWeatherData.strWind = jsonWeather.getString("wind");
            }
            if (jsonWeather.containsKey("carWashIndex")) {
                pbWeatherData.strCarWashIndex = jsonWeather
                        .getString("carWashIndex");
            }
            if (jsonWeather.containsKey("carWashIndexDesc")) {
                pbWeatherData.strCarWashIndexDesc = jsonWeather
                        .getString("carWashIndexDesc");
            }
            if (jsonWeather.containsKey("travelIndex")) {
                pbWeatherData.strTravelIndex = jsonWeather
                        .getString("travelIndex");
            }
            if (jsonWeather.containsKey("travelIndexDesc")) {
                pbWeatherData.strTravelIndexDesc = jsonWeather
                        .getString("travelIndexDesc");
            }
            pbWeatherInfos.rptMsgWeather[i] = pbWeatherData;
        }
        speakWords(strAnswer, false);
        JNIHelper.sendEvent(UiEvent.EVENT_VOICE,
                VoiceData.SUBEVENT_VOICE_SHOW_WEATHER_INFO, pbWeatherInfos);

    }

    public byte[] invokeWeather(final String packageName, String command, final byte[] data) {
        String cmd = command.substring(TXZWeatherManager.WEATHER_INVOKE_PREFIX.length());
        if (TextUtils.equals(cmd, TXZWeatherManager.SET_WEATHER_TOOL)) {
            mRemoteWeatherTool = packageName;
        } else if (TextUtils.equals(cmd, TXZWeatherManager.CLEAR_WEATHER_TOOL)) {
            mRemoteWeatherTool = null;
        } else if (TextUtils.equals(cmd, TXZWeatherManager.SET_TIMEOUT)) {
            mTimeout = new JSONBuilder(data).getVal("timeout", Long.class, DEF_TIMEOUT);
            LogUtil.logd(TAG + " timeout = " + mTimeout);
        } else if (TextUtils.equals(cmd, TXZWeatherManager.RESULT_WEATHER)) {

            JSONObject json = JSONObject.parseObject(new String(data));
            String taskId = json.getString("taskid");
            if (mTextWeatherTaskIds.contains(taskId)) {
                LogUtil.logd(TAG + "RESULT_WEATHER");
                mResult[1].state = WeatherResult.STATE_SUCCESS;
                mResult[1].taskId = taskId;
                mResult[1].weatherData = json;
                checkResult();
                AppLogic.removeBackGroundCallback(timeoutRunnable);
            } else {
                LogUtil.logd(TAG + "RESULT_WEATHER taskId error");
            }
        } else if (TextUtils.equals(cmd, TXZWeatherManager.ERROR_WEATHER)) {
            JSONObject json = JSONObject.parseObject(new String(data));
            String taskId = json.getString("taskid");
            if (mTextWeatherTaskIds.contains(taskId)) {
                LogUtil.logd(TAG + "ERROR_WEATHER");
                mResult[1].state = WeatherResult.STATE_ERROR;
                mResult[1].taskId = taskId;
                checkResult();
                AppLogic.removeBackGroundCallback(timeoutRunnable);
            } else {
                LogUtil.logd(TAG + "ERROR_WEATHER taskId error");
            }
        }
        return null;
    }


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

    private boolean procSenceByRemote(String sence, String data) {
        byte[] b_isProc = SenceManager.getInstance().procSenceByRemote(sence,
                data.getBytes());
        if (b_isProc == null)
            return false;
        boolean isProc = Boolean.parseBoolean(new String(b_isProc));
        LogUtil.logd(MusicManager.TAG + "INTERCEPT:" + sence + "/" + isProc);
        if (isProc) {
            ReportUtil.doReport(new ReportUtil.Report.Builder().setType("procSence").setSessionId()
                    .putExtra("sence", sence).buildCommReport());
        }
        return isProc;
    }
}
