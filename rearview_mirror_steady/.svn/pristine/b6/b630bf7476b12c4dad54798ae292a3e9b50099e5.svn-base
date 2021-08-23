package com.txznet.txz.module.film;

import android.graphics.Bitmap;
import android.util.LruCache;
import android.view.View;
import android.widget.ImageView;

import com.alibaba.fastjson.JSONObject;
import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.txz.push_manager.PushManager;
import com.txz.ui.equipment.UiEquipment;
import com.txz.ui.event.UiEvent;
import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.ui.UI2Manager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.util.LayouUtil;
import com.txznet.comm.util.JSONBuilder;

import android.text.TextUtils;

import com.txznet.comm.util.imageloader.ImageLoaderInitialize;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.OnItemSelectListener;
import com.txznet.txz.component.choice.list.FilmWorkChoice;
import com.txznet.txz.component.choice.list.MovieTheaterWorkChoice;
import com.txznet.txz.component.choice.list.MovieTimeWorkChoice;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.comm.ui.dialog.FilmPayResDialog;
import com.txznet.txz.component.film.MoviePhoneNumQRControl;
import com.txznet.txz.component.film.MovieSeatPlanControl;
import com.txznet.txz.component.film.MovieWaitingPayQRControl;
import com.txznet.txz.component.text.IText;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.module.record.Recorder;
import com.txznet.txz.module.sim.SimManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.ticket.QiWuTicketManager;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.TXZFileConfigUtil;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FilmManager extends IModule {

    private FilmManager(){}

    private static FilmManager sIntance = new FilmManager();

    public static FilmManager getInstance() {
        return sIntance;
    }

    public final String WAN_MI_URL = "https://gw.alicdn.com/tfscom/";

    private String mCurrentOrderId = "";

    private static int mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;

    public boolean beInWanMi = false;

    //功能开关。
    private static boolean wanMiControl = true;
    private static boolean newContent = false;

    public void setWanMiControl(boolean wanMiControl){
        FilmManager.wanMiControl = wanMiControl;
    }

    public void setNewContent(boolean newContent){
        FilmManager.newContent = newContent;
    }

    //当前主题皮肤包风格，作为电影票场景的一个开关的标志位。目前只在全屏时打开，之后可继续增加
    private static boolean themeSwitch = true;

    int flag = 0;

    private Long timeOut = TXZFileConfigUtil
                            .getSingleConfig(TXZFileConfigUtil.WAN_MI_FILM_LIST_OUT_TIME_CLEAR,
                                    Long.class,-1L);

    //true表示正在执行一次选择任务，上一个选择任务没有完成，下一次触发的离线唤醒词任务全部丢弃。
    private  boolean beSelecting = false;

    //当值为true表示这次请求需要向后台发送清除玩秘场景。
    private boolean beClearWanMi = true;

    public boolean getBeClearWanMi() {
        boolean tempClear = beClearWanMi;
        beClearWanMi = false;
        return tempClear;
    }

    //设置需要玩秘退出电影场景
    public void setBeClearWanMi(){
        beInWanMi = false;
        beClearWanMi = true;
        beSelecting = false;
        cancelSpeak();
        try {
            RecorderWin.OBSERVABLE.unregisterObserver(statusOb);
        }catch (Exception ignored){
        }
    }


    private static final String TAG = "FilmManager::";

    //窗口监听器
    RecorderWin.StatusObervable.StatusObserver statusOb =  new RecorderWin.StatusObervable.StatusObserver() {
        @Override
        public void onShow() {
            beInWanMi = false;
            beClearWanMi = true;
            beSelecting = false;
            cancelSpeak();
            try {
                RecorderWin.OBSERVABLE.unregisterObserver(statusOb);
            }catch (Exception ignored){
            }
        }

        @Override
        public void onDismiss() {
            beInWanMi = false;
            beClearWanMi = true;
            beSelecting = false;
            //关闭页面的时候检查一下电影页面是否进行了插词，若进行了插词，则反注册插词。
            cancelSpeak();
            try {
                RecorderWin.OBSERVABLE.unregisterObserver(statusOb);
            }catch (Exception ignored){
            }
        }
    };

    @Override
    public int initialize_BeforeStartJni() {
        regEvent(UiEvent.EVENT_ACTION_EQUIPMENT,
                UiEquipment.SUBEVENT_PUSH_TXZ_NLP);
        return super.initialize_BeforeStartJni();
    }

    @Override
    public int onEvent(int eventId, int subEventId, byte[] data) {
        switch (eventId) {
            case UiEvent.EVENT_ACTION_EQUIPMENT:
                switch (subEventId){
                    case UiEquipment.SUBEVENT_PUSH_TXZ_NLP:handleQRResults(data);break;
                }
        }
        return super.onEvent(eventId, subEventId, data);
    }

    public void requestTxz(String text, final RequestCallBack requestCallBack){
        ChoiceManager.getInstance().clearIsSelecting();
        VoiceData.VoiceParseData parseData = new VoiceData.VoiceParseData();
        parseData.strText = text;
        requestTxz(parseData,requestCallBack);
    }

    private void speakText(String spk , final Runnable runnable) {
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
        mSpeechTaskId = TtsManager.getInstance().speakText(spk,new TtsUtil.ITtsCallback(){
            @Override
            public void onSuccess(){
                mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
                if(runnable != null && timeOut > 0){
                    AppLogic.removeBackGroundCallback(runnable);
                    AppLogic.runOnBackGround(runnable,timeOut);
                }

            }

            @Override
            public void onError(int error){
                mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
            }

        });
    }

    public void cancelSpeak(){
        if(mSpeechTaskId != TtsManager.INVALID_TTS_TASK_ID){
            TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        }
    }

    public void requestTxz(VoiceData.VoiceParseData parseData, final RequestCallBack requestCallBack){
        cancelSpeak();
        TextResultHandle.getInstance().parseText(parseData,TextResultHandle.MODULE_TXZ_MASK,new IText.ITextCallBack() {
            @Override
            public void onResult(VoiceData.VoiceParseData dataResult, int priority) {
                beSelecting = false;
                JSONObject json = JSONObject.parseObject(dataResult.strVoiceData);
                LogUtil.logd(TAG + "getTXZjson ="+json);
                String strScene = json.getString("scene");
                String strAction = json.getString("action");
                if(requestCallBack != null){
                    requestCallBack.onResult();
                }
                if("wan_mi".equals(strScene)){
                    parseWanMi(json,strAction);
                }else {
                    LogUtil.logd(TAG+"getTXZjson errorScene="+strScene);
                    AsrManager.getInstance().setNeedCloseRecord(true);
                    RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_IN_NET"), null);
                }
                super.onResult(dataResult, priority);
            }

            @Override
            public void onError(int errorCode) {
                beSelecting =false;
                LogUtil.logd(TAG+ "nlp:errorCode = " + errorCode);
                if(requestCallBack != null){
                    requestCallBack.onError();
                }
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_UNKNOW_LOCAL_IN_NET"), null);
                super.onError(errorCode);
            }
        });
    }

    private boolean showMovieList(JSONObject json) {

        CompentOption<FilmWorkChoice.FilmItem> option = new CompentOption<FilmWorkChoice.FilmItem>();
        option.setCallbackListener(new OnItemSelectListener<FilmWorkChoice.FilmItem>() {

            @Override
            public boolean onItemSelected(boolean isPreSelect, FilmWorkChoice.FilmItem filmItem, boolean fromPage, int idx, String fromVoice) {
                //防止一次选择，触发多次离线词，导致重复发送选择请求给后台。
                if(beSelecting){
                    return true;
                }
                beClearWanMi = false;
                LogUtil.logd(TAG+"onItemSelected filmItem = "+filmItem.title);
                beSelecting = true;
                requestTxz(filmItem.answer,null);
                return true;
            }
        });
        if(timeOut > 0){
            option.setTimeout(timeOut);
        }
        JSONBuilder jBuilder = new JSONBuilder(json.toString());
        String answer = jBuilder.getVal("answer", String.class);
        option.setTtsText(answer);
        ChoiceManager.getInstance().showMovieList(parseText(jBuilder), option);
        return true;
    }

    public boolean parseWanMi(JSONObject json, String strAction) {
//        HelpHitTispUtil.getInstance().hitMovieTips();
        if(!enableFilmFunc()){
            beClearWanMi = true;
            RecorderWin.speakText(NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),null);
            return true;
        }
        beSelecting = false;
        if(!beInWanMi){
            RecorderWin.OBSERVABLE.registerObserver(statusOb);
        }
        beInWanMi = true;
        if (SimManager.getInstance().checkFlowControl(UiEquipment.CONTROL_MOVIE))
            return false;
        ChoiceManager.getInstance().clearIsSelecting();
        beClearWanMi = false;
        if("movie".equals(strAction)){
            return showMovieList(json);
        }else if("cinema".equals(strAction)){
            return  showMovieTheaterList(json);
        }else if("screening".equals(strAction)){
            return showMovieTimesList(json);
        }else if("inquire_ticket_count".equals(strAction)){
            return showSeatPlan(json);
        }else if("inquire_phone_num".equals(strAction)){
            return showInquirePhoneNum(json);
        }else if("waiting_pay".equals(strAction)){
            return showWaitingPay(json);
        }else if("exit".equals(strAction)){
            return exitWanMi(json);
        }
        LogUtil.logd(TAG+"getTXZjson errorAction="+strAction);
        return false;
    }

    private boolean exitWanMi(JSONObject json) {
        int exitType = json.getIntValue("exit_type");
        switch (exitType){
            case 4:
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_TIPS_MOVIE_EXIT_LOCK_TICKET_FAIL"),null);
                break;
            case 5:
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_TIPS_MOVIE_EXIT_UNKNOWN"),null);
                break;
            case 6:
                AsrManager.getInstance().setNeedCloseRecord(true);
                RecorderWin.speakTextWithClose(NativeData.getResString("RS_VOICE_TIPS_MOVIE_EXIT_CINEMA_FAIL"),null);
                break;
                default:
                    if(json.containsKey("answer")){
                        String answer = json.getString("answer");
                        RecorderWin.speakText(answer, null);
                    }
                    break;
        }
        LogUtil.logd(TAG+"getTXZjson exitType="+exitType);
        return true;
    }

    private boolean showWaitingPay(JSONObject json){
        String answer = json.getString("answer");
        JSONObject payInfo = json.getJSONObject("pay_info");
        String WXURL = payInfo.getString("wx");
        String ZFBURL = payInfo.getString("zfb");
        String replacePhoneUrl = json.getString("change_phone_url");
        mCurrentOrderId = json.getString("order_id");
        String phone = json.getString("phone");
        JSONObject attachInfo = json.getJSONObject("attach_info");
        JSONObject selectedSchedule = attachInfo.getJSONObject("selected_schedule");
        String moiveName = selectedSchedule.getString("show_name");
        String cinemaName = selectedSchedule.getString("cinema_name");
        String showTime = selectedSchedule.getString("show_time");
        String showVersion = selectedSchedule.getString("show_version");
        String hallName = selectedSchedule.getString("hall_name");
        com.alibaba.fastjson.JSONArray seats = selectedSchedule.getJSONArray("seats");

        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("WXPayURL",WXURL);
        jsonBuilder.put("ZFBPayURL",ZFBURL);
        jsonBuilder.put("phoneNum",phone);
        jsonBuilder.put("replacePhoneUrl",replacePhoneUrl);
        jsonBuilder.put("type", 21);
        jsonBuilder.put("vTips",NativeData.getResString("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));
        jsonBuilder.put("moiveName",moiveName);
        jsonBuilder.put("cinemaName",cinemaName);
        jsonBuilder.put("showTime",showTime);
        jsonBuilder.put("showVersion",showVersion);
        jsonBuilder.put("seats",seats);
        jsonBuilder.put("hallName",hallName);
        /**
         * 填充无用数据，避免空指针异常
         */
        jsonBuilder.put("count", 0);
        jsonBuilder.put("curPage", 0);
        jsonBuilder.put("maxPage", 0);
        if(TextUtils.isEmpty(WXURL)|| TextUtils.isEmpty(ZFBURL)){
            RecorderWin.open(NativeData.getResString("RS_VOICE_TIPS_MOVIE_QRCODE_LOAD_FAIL"));
            return true;
        }
        speakText(answer,null);
        MovieWaitingPayQRControl.getInstance().show(jsonBuilder);
        return true;
    }

    private boolean showInquirePhoneNum(JSONObject json){
        String answer = json.getString("answer");
        String phoneNumUrl = json.getString("phone_register_url");
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("phoneNumUrl",phoneNumUrl);
        jsonBuilder.put("type", 20);
        jsonBuilder.put("vTips",NativeData.getResString("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));
        /**
         * 填充无用数据，避免空指针异常
         */
        jsonBuilder.put("count", 0);
        jsonBuilder.put("curPage", 0);
        jsonBuilder.put("maxPage", 0);
        speakText(NativeData.getResString("RS_VOICE_TIPS_MOVIE_PHONE_NUM"),null);
        MoviePhoneNumQRControl.getInstance().show(jsonBuilder);
        return true;
    }

    private boolean showSeatPlan(final JSONObject json) {
        JSONObject attach_info = json.getJSONObject("attach_info");
        JSONObject selected_schedule = attach_info.getJSONObject("selected_schedule");
        final String sSeatPlanUrl = selected_schedule.getString("seat_map_img_url_before_ticket_size");
        final JSONBuilder jsonBuilder = new JSONBuilder();
        int ticketCount = json.getJSONArray("options").size();
        jsonBuilder.put("SeatPlanUrl",sSeatPlanUrl);
        jsonBuilder.put("type", 19);
        /**
         * 填充无用数据，避免空指针异常
         */
        jsonBuilder.put("count", 0);
        jsonBuilder.put("curPage", 0);
        jsonBuilder.put("maxPage", 0);
        jsonBuilder.put("vTips",NativeData.getResString("RS_VOICE_TIPS_MOVIE_SEAT"));
        String answer = json.getString("answer");
        //不能使用RecorderWin.speakText
        speakText(answer,MovieSeatPlanControl.getInstance().mTimeoutTask);
        MovieSeatPlanControl.getInstance().show(jsonBuilder,timeOut,ticketCount);
        return true;
    }

    @Override
    public int initialize_AfterInitSuccess() {
        return super.initialize_AfterInitSuccess();
    }

    public boolean showMovieTheaterList(JSONObject json){
        ChoiceManager.getInstance().clearIsSelecting();
        CompentOption<MovieTheaterWorkChoice.MovieTheaterItem> option = new CompentOption<MovieTheaterWorkChoice.MovieTheaterItem>();
        option.setCallbackListener(new OnItemSelectListener<MovieTheaterWorkChoice.MovieTheaterItem>() {

            @Override
            public boolean onItemSelected(boolean isPreSelect, MovieTheaterWorkChoice.MovieTheaterItem movieTheaterItem, boolean fromPage, int idx, String fromVoice) {
                //防止一次选择，触发多次离线词，导致重复发送选择请求给后台。
                if(beSelecting){
                    return true;
                }
                beClearWanMi = false;
                LogUtil.logd(TAG+"onItemSelected TheaterItem = "+movieTheaterItem.cinemaName);
                beSelecting = true;
                requestTxz(movieTheaterItem.answer, null);
                return true;
            }
        });
        JSONBuilder jBuilder = new JSONBuilder(json.toString());
        String answer = jBuilder.getVal("answer", String.class);
        if(timeOut > 0){
            option.setTimeout(timeOut);
        }
        option.setTtsText(answer);
        ChoiceManager.getInstance().showMovieTheatsList(parseMovieTheaterText(jBuilder), option);
        return true;
    }

    public boolean showMovieTimesList(JSONObject json){
        CompentOption<MovieTimeWorkChoice.MovieTimeItem> option = new CompentOption<MovieTimeWorkChoice.MovieTimeItem>();
        option.setCallbackListener(new OnItemSelectListener<MovieTimeWorkChoice.MovieTimeItem>() {

            @Override
            public boolean onItemSelected(boolean isPreSelect, MovieTimeWorkChoice.MovieTimeItem movieTimeItem, boolean fromPage, int idx, String fromVoice) {
                //防止一次选择，触发多次离线词，导致重复发送选择请求给后台。
                if(beSelecting){
                    return true;
                }
                beClearWanMi = false;
                LogUtil.logd(TAG+"onItemSelected TheaterItem = "+movieTimeItem.showTime);
                beSelecting = true;
                requestTxz(movieTimeItem.answer, null);
                return true;
            }
        });
        JSONBuilder jBuilder = new JSONBuilder(json.toString());
        String answer = jBuilder.getVal("answer", String.class);
        if(timeOut > 0){
            option.setTimeout(timeOut);
        }
        option.setTtsText(answer);
        ChoiceManager.getInstance().showMovieTimesList(parseMovieTimesText(jBuilder),option);
        return true;
    }

    private List<FilmWorkChoice.FilmItem> parseText(JSONBuilder jBuilder) {

        JSONArray jsonArray = jBuilder.getVal("options", JSONArray.class);
        List<FilmWorkChoice.FilmItem> items = getFilmFromJSONArray(jsonArray);
       return items;
    }

    private List<MovieTimeWorkChoice.MovieTimeItem> parseMovieTimesText(JSONBuilder jBuilder){
        JSONArray jsonArray = jBuilder.getVal("options", JSONArray.class);
        List<MovieTimeWorkChoice.MovieTimeItem> items = getMovieTimesFromJSONArray(jsonArray);
        return items;
    }

    private List<MovieTimeWorkChoice.MovieTimeItem> getMovieTimesFromJSONArray(JSONArray jsonArray) {
        List<MovieTimeWorkChoice.MovieTimeItem> movieTimesItems = new ArrayList<MovieTimeWorkChoice.MovieTimeItem>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                MovieTimeWorkChoice.MovieTimeItem mt = new MovieTimeWorkChoice.MovieTimeItem();
                org.json.JSONObject jo =(org.json.JSONObject) jsonArray.get(i);
                if (jo.has("show_version")){
                    mt.showVersion = jo.optString("show_version");
                }
                if (jo.has("showT_time")){
                    mt.showTime = jo.optString("showT_time");
                }
                if (jo.has("close_time")){
                    mt.closeTime = jo.optString("close_time");
                }
                if (jo.has("hall_name")){
                    mt.hallName =jo.optString("hall_name");
                }
                if(jo.has("unit_price")){
                    mt.unitPrice = jo.getInt("unit_price");
                }
                if (jo.has("answer")){
                    mt.answer = jo.getString("answer");
                }
                if(jo.has("show_name")){
                    mt.showName = jo.getString("show_name");
                }
                movieTimesItems.add(mt);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return movieTimesItems;
    }

    private List<MovieTheaterWorkChoice.MovieTheaterItem> parseMovieTheaterText(JSONBuilder jBuilder) {
        JSONArray jsonArray = jBuilder.getVal("options", JSONArray.class);
        List<MovieTheaterWorkChoice.MovieTheaterItem> items = getMovieTheaterFromJSONArray(jsonArray);
        return items;
    }

    private List<MovieTheaterWorkChoice.MovieTheaterItem> getMovieTheaterFromJSONArray(JSONArray jsonArray) {
        List<MovieTheaterWorkChoice.MovieTheaterItem> movieTheaterItems = new ArrayList<MovieTheaterWorkChoice.MovieTheaterItem>();
        for(int i = 0; i < jsonArray.length(); i++){
            try {
                MovieTheaterWorkChoice.MovieTheaterItem mi = new MovieTheaterWorkChoice.MovieTheaterItem();
                org.json.JSONObject jo = (org.json.JSONObject) jsonArray.get(i);
                if(jo.has("cinema_name")){
                    mi.cinemaName = jo.optString("cinema_name");
                }
                if(jo.has("address")){
                    mi.address = jo.optString("address");
                }
                if (jo.has("distance")){
                    mi.distance = jo.optString("distance").replace("公里","km")
                                                .replace("米","m");
                }
                if(jo.has("alias")){
                    JSONArray jsonAlias = jo.optJSONArray("alias");
                    for(int j = 0; j < jsonAlias.length(); j++){
                        mi.alias.add(jsonAlias.getString(j));
                    }
                }
                if(jo.has("cinema_flags")){
                    JSONArray cinemaFlags = jo.optJSONArray("cinema_flags");
                    if(cinemaFlags.length() > 0){
                        int cinemaFlag = cinemaFlags.getInt(0);
                        mi.cinemaFlag = NativeData.getResString("RS_STRING_CINEMA_FLAG",cinemaFlag - 1);
                    }
                }
                if (jo.has("answer")){
                    mi.answer = jo.optString("answer");
                }
                if(jo.has("location_type")){
                    int locationType = jo.getInt("location_type");
                     mi.locationType = NativeData.getResString("RS_STRING_LOCATION_TYPE",locationType - 1);
                }
                movieTheaterItems.add(mi);
            } catch (JSONException e) {
                JNIHelper.logw("CinemaQuery parseStrData error:" + e.toString());
            }
        }
        return movieTheaterItems;
    }

    private List<FilmWorkChoice.FilmItem> getFilmFromJSONArray(JSONArray jsonArray) {
        List<FilmWorkChoice.FilmItem> filmList = new ArrayList<FilmWorkChoice.FilmItem>();
        for (int i = 0; i < jsonArray.length(); i++) {
            try {
                FilmWorkChoice.FilmItem fi = new FilmWorkChoice.FilmItem();
                org.json.JSONObject jo = (org.json.JSONObject) jsonArray.get(i);
                if (jo.has("poster")) {
                    fi.postUrl = WAN_MI_URL+jo.optString("poster")+"_"+600+"x"+600+".jpg";;
                }
                if (jo.has("show_name")) {
                    fi.title = jo.optString("show_name");
                }
                if (jo.has("remark")) {
                    fi.score = jo.optDouble("remark");
                }
                if(jo.has("alias")){
                    JSONArray jsonAlias = jo.getJSONArray("alias");
                    for(int j = 0; j < jsonAlias.length(); j++){
                        fi.alias.add(jsonAlias.getString(j));
                    }
                }

                if(jo.has("type")){
                    String[] types = jo.optString("type").split(",");
                    for(int j = 0; j < types.length; j++){
                        fi.types.add(types[j]+"片");
                    }
                }
                if (jo.has("answer")){
                    fi.answer = jo.optString("answer");
                }
                filmList.add(fi);
            } catch (JSONException e) {
                JNIHelper.logw("CinemaQuery parseStrData error:" + e.toString());
            }
        }
        return filmList;
    }

    /**
     * 皮肤包状态回调
     * @param packageName
     * @param command
     * @param data
     * @return
     */
    public byte[] invokeCommand(final String packageName, String command, byte[] data) {
        JSONBuilder dataJson = new JSONBuilder(data);
        String phoneNumUrl = dataJson.getVal("extraString", String.class);
        final JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("phoneNumUrl",phoneNumUrl);
        jsonBuilder.put("type", 20);
        jsonBuilder.put("vTips",NativeData.getResString("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));
        /**
         * 填充无用数据，避免空指针异常
         */
        jsonBuilder.put("count", 0);
        jsonBuilder.put("curPage", 0);
        jsonBuilder.put("maxPage", 0);
        speakText(NativeData.getResString("RS_VOICE_TIPS_MOVIE_CHANGE_PHONE_NUM"),null);
        MoviePhoneNumQRControl.getInstance().show(jsonBuilder);
        return null;
    }

    /*
    * 传入的data中携带的值
    *   表示当前皮肤包的风格。
     *            1 表示全屏
     *            2 表示半屏
     *            3 表示无屏
    * */
    public static byte[] procRemoteResponse(String serviceName, String command,
                                            byte[] data) {

        if ("getTheme".equals(command)) {
            int currentSelectStyleIndex = Integer.parseInt(new String(data));
            switch (currentSelectStyleIndex){
                /*case 1:
                    themeSwitch = true;
                    QiWuTicketManager.themeSwitch = true;
                    break;
               default:
                   //themeSwitch = false;
                   QiWuTicketManager.themeSwitch = false;
                   break;*/
            }
            return null;
        }
        if("enable".equals(command)){
            if(WinManager.getInstance().isSupportNewContent()){
                return null;
            }
            newContent = Boolean.parseBoolean(new String(data));
        }
        return null;
    }

    FilmPayResDialog dialog;
    private void handleQRResults(byte[] data){
        LogUtil.logd(TAG + "handleQRresults");
        if (data == null) {
            LogUtil.logd(TAG + "handleQRresults data = null");
            return;
        }
        PushManager.PushCmd_NLP pushCmdQRResults = null;
        try{
            pushCmdQRResults = PushManager.PushCmd_NLP.parseFrom(data);
        }catch (InvalidProtocolBufferNanoException e){}
        if(pushCmdQRResults == null){
            return;
        }
        LogUtil.logd(TAG + "handleQRResults strJson = "
                + (pushCmdQRResults.strJson == null ? "null" : new String(pushCmdQRResults.strJson)));
        JSONObject resultsJson = null;
        if(pushCmdQRResults.strJson != null){
            resultsJson = JSONObject.parseObject(new String(pushCmdQRResults.strJson));
        }
        int action;
        if(resultsJson != null){
            action = resultsJson.getInteger("action");
        }else {
            return;
        }
        switch (action){
            case 1:
                String resultOrderId = resultsJson.getString("order_id");
                if(!MovieWaitingPayQRControl.getInstance().isSelecting()){
                    break;
                }
                if(TextUtils.isEmpty(mCurrentOrderId) || TextUtils.isEmpty(resultOrderId) ||  !resultOrderId.equals(mCurrentOrderId)){
                    break;
                }

                int payResult = resultsJson.getInteger("pay_result");
                dialog = new FilmPayResDialog() {

                    //重写dismiss方法，目的在于弹窗关闭时同时关闭声控。
                    @Override
                    public void dismiss() {
                        super.dismiss();
                        RecorderWin.close();
                        dialog = null;
                    }
                };
                switch (payResult){
                    case 1:
                    case 3:
                        dialog.setIvResults(LayouUtil.getDrawable("icon_fail"));
                        dialog.setTvResults(NativeData.getResString("RS_DIALOG_TIP_MOVIE_PAY_FAIL"));
                        if(!MovieWaitingPayQRControl.getInstance().isSelecting()){
                            LogUtil.logd(TAG+"payViewIsClose");
                            break;
                        }
                        dialog.show();
                        MovieWaitingPayQRControl.getInstance().clearIsSelecting();
                        break;
                    case 2:
                        dialog.setIvResults(LayouUtil.getDrawable("icon_success"));
                        dialog.setTvResults(NativeData.getResString("RS_DIALOG_TIP_MOVIE_PAY_SUCCESS"));
                        if(!MovieWaitingPayQRControl.getInstance().isSelecting()){
                            LogUtil.logd(TAG+"payViewIsClose");
                            break;
                        }
                        dialog.show();
                        MovieWaitingPayQRControl.getInstance().clearIsSelecting();
                        break;
                }
                break;
            case 2:
                if(!MoviePhoneNumQRControl.getInstance().isSelecting()){
                    break;
                }
                String phoneNum = resultsJson.getString("phone");
                VoiceData.VoiceParseData phoneParseData = new VoiceData.VoiceParseData();
                phoneParseData.strText = phoneNum;
                JSONBuilder jsonBuilder = new JSONBuilder();
                JSONArray jsonArray = new JSONArray();
                org.json.JSONObject paramJson = new org.json.JSONObject();
                try {
                    paramJson.put("key","wan_mi_phone");
                    paramJson.put("value",phoneNum);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                jsonArray.put(paramJson);
                jsonBuilder.put("params",jsonArray);
                phoneParseData.strExtraData = jsonBuilder.build().toString().getBytes();
                requestTxz(phoneParseData,null);
                MoviePhoneNumQRControl.getInstance().clearIsSelecting();
                  break;
            case 3:
                if(!MoviePhoneNumQRControl.getInstance().isSelecting()){
                    break;
                }
                String changePhoneNum = resultsJson.getString("phone");
                VoiceData.VoiceParseData parseData = new VoiceData.VoiceParseData();
                parseData.strText = changePhoneNum;
                JSONBuilder changeJsonBuilder = new JSONBuilder();
                JSONArray changeJsonArray = new JSONArray();
                org.json.JSONObject changeParamJson = new org.json.JSONObject();
//                JSONObject changeParamJson = new JSONObject();
                try {
                    changeParamJson.put("key","wan_mi_replace_phone");
                    changeParamJson.put("value",true);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                changeJsonArray.put(changeParamJson);
                changeJsonBuilder.put("params",changeJsonArray);
                parseData.strExtraData = changeJsonBuilder.build().toString().getBytes();
                requestTxz(parseData,null);
                MoviePhoneNumQRControl.getInstance().clearIsSelecting();
                break;
        }

    }

    public void cancel(String command){
        ChoiceManager.getInstance().clearIsSelecting();
        if(!TextUtils.isEmpty(command)){
            RecorderWin.setLastUserText(command);
            RecorderWin.open(NativeData.getResString("RS_VOICE_FILM_TICKET_CANCEL"));
        }
        beInWanMi = false;
        beClearWanMi = true;
        beSelecting = false;
        try {
            RecorderWin.OBSERVABLE.unregisterObserver(statusOb);
        }catch (Exception ignored){
        }
    }

    private boolean enableFilmFunc(){

        if((wanMiControl || newContent) && themeSwitch){
            LogUtil.logd(TAG + "enableFilmFunc true");
            return true;
        }
        LogUtil.logd(TAG + "enableFilmFunc false");
        return false;
    }

    public static abstract class RequestCallBack {
        public void onResult() {
        }
        public void onError() {
        }
    }

}
