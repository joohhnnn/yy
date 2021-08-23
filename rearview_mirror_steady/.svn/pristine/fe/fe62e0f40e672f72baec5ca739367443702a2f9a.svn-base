package com.txznet.launcher.domain.txz;

import android.content.Intent;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.launcher.BuildConfig;
import com.txznet.launcher.cfg.DebugUtils;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.LaunchManager;
import com.txznet.launcher.domain.fm.FmManager;
import com.txznet.launcher.domain.music.MusicManager;
import com.txznet.launcher.domain.nav.NavManager;
import com.txznet.launcher.domain.settings.SettingsManager;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.widget.IImage;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZAsrKeyManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdk.TXZSceneManager;
import com.txznet.sdk.TXZStatusManager;
import com.txznet.sdkinner.TXZServiceCommandDispatcher;
import com.txznet.txz.util.runnables.Runnable1;

/**
 * Created by TXZ-METEORLUO on 2018/2/24.
 * 处理录音窗口的类，包括了录音窗口出来时小欧要执行的行为，也就是说唤醒小欧后处理就在这个类。
 */

public class RecordWinManager extends BaseManager implements TXZResourceManager.RecordWin {

    private static RecordWinManager instance;
    private boolean isRecordWinClosed;
    //临时关闭显示系统文本，只一次有效
    boolean canShowSysText = true;
    private RecordWinOperateListener mRecordWinOperateListener = null;

    public static RecordWinManager getInstance() {
        if (instance == null) {
            synchronized (RecordWinManager.class) {
                if (instance == null) {
                    instance = new RecordWinManager();
                }
            }
        }
        return instance;
    }

    @Override
    public void init() {
        super.init();
        TXZResourceManager.getInstance().setRecordWin(this);
        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_COMMAND, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                String cmd = jsonBuilder.getVal("cmd", String.class);
//                if (TextUtils.equals(cmd, "wifi_on")) {
//                    TXZResourceManager.getInstance().speakTextOnRecordWin("将为您打开wifi", true, new Runnable() {
//                        @Override
//                        public void run() {
//                            SettingsManager.getInstance().ctrlWifi(true);
//                        }
//                    });
//                    return true;
//                } else if (TextUtils.equals(cmd, "wifi_off")) {
//                    TXZResourceManager.getInstance().speakTextOnRecordWin("将为您关闭wifi", true, new Runnable() {
//                        @Override
//                        public void run() {
//                            SettingsManager.getInstance().ctrlWifi(false);
//                        }
//                    });
//                    return true;
//                }
                if (TextUtils.equals(cmd, "wifi_on") || TextUtils.equals(cmd, "wifi_off")) {
                    if (!BuildConfig.DEBUG) {
                        TXZResourceManager.getInstance().speakTextOnRecordWin("当前不支持该操作", true, null);
                        return true;
                    }
                }
                return false;
            }
        });

        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_ALL, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                LogUtil.logd("process, type=" + type + ", data=" + data);

                JSONBuilder jsonBuilder = new JSONBuilder(data);
                String typeStr = "";
                try {
                    typeStr = jsonBuilder.getVal("type", String.class);
                } catch (Exception e) {
                    try {
                        typeStr = jsonBuilder.getVal("type", Integer.class) + "";
                    } catch (Exception ex) {
                    }
                }
                String scene = jsonBuilder.getVal("scene", String.class);
                String action = jsonBuilder.getVal("action", String.class);
                String text = jsonBuilder.getVal("text", String.class);
//                    Boolean local = jsonBuilder.getVal("local", Boolean.class);
                DebugUtils.sendAsrResult(text);
//                if (TextUtils.equals(scene,"nav")) {
//                    switch (action) {
//                        case "pass":
//                        case "search":
//                        case "nearby":
//                        case "passAround":
//                        case "delPass":
//                        case "firmSearch":
//                            TXZResourceManager.getInstance().speakTextOnRecordWin("该操作我还没学会!", false, null);
//                            return true;
//
//                    }
//                }

                if ("guideAnim".equals(typeStr) && "isNeedRecorderWin".equals(action)) {
                    LogUtil.logd("guideAnim finish");
                    return true;
                }
                // 屏蔽附近路况的语义
                if ("query".equals(action)) {
                    if ("traffic".equals(jsonBuilder.getVal("scene",String.class,""))) {
                        LogUtil.e("interrupt query traffic");
                        TXZResourceManager.getInstance().speakTextOnRecordWin(AppLogic.UNSUPPORTED_COMMAND, true, null);
                        return true;
                    }
                }
                // 当调频时根据fm频率调整提示语
                //  fm调频返回示例：{"local":true,"text":"调频91","score":70,"scene":"radio","action":"play","waveband":"fm","unit":"MHZ","hz":"91","t":6}
                if ("radio".equals(scene) && "play".equals(action)) {
                    if ("fm".equals(jsonBuilder.getVal("waveband", String.class, ""))) {
                        LogUtil.logd("control fm by ourself");
                        String hzStr = jsonBuilder.getVal("hz", String.class, "");
                        LogUtil.logd("hzStr:"+hzStr);
                        hzStr = hzStr.replace("点", ".");
                        final float hz = Float.parseFloat(hzStr);

                        FmManager.getInstance().toFmFreq(hz);
                        return true;
                    }
                }
                return false;
            }
        });
        // processor是在service处理完指令后才执行的。
        TXZServiceCommandDispatcher.setCommandProcessor("comm.asr.event.onWakeupAsrResult", new TXZServiceCommandDispatcher.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                JSONBuilder json = new JSONBuilder(data);
                String taskId = json.getVal("taskId", String.class);
                String text = json.getVal("text", String.class);
                DebugUtils.sendWakeupResult(text);
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WAKE_UP);
                return null;
            }
        });

        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_NAV, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                LogUtil.logd("process, type=" + type + ", data=" + data);
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                String typeStr = jsonBuilder.getVal("type", String.class);
                if (typeStr != null) {
                    switch (typeStr) {
                        case "GAOSUYOUXIAN":
                        case "DUOBIYONGDU":
                        case "BUZOUGAOSU":
                        case "LESS_MONEY":
                        case TXZAsrKeyManager.AsrKeyType.CANCEL_NAV:
                        case TXZAsrKeyManager.AsrKeyType.EXIT_NAV:
                            SettingsManager.getInstance().ctrlScreen(true);
                            break;

                    }
                }
                // 导航的状态控制有问题。有时当导航已经不在前台的时候状态没有更新，导致只在导航中相应的指令没有被清除，如高速优先。
                // 远峰系统 BUG2018080909471
                // 等core将导航状态的控制完善了再去掉这段代码吧。
                String actionStr = jsonBuilder.getVal("action", String.class);
                String sceneStr = jsonBuilder.getVal("scene", String.class);
                if ("mapVoiceControl".equals(actionStr) && "nav".equals(sceneStr) && LaunchManager.getInstance().isLaunchResume()) {
                    return true;
                }
                return false;
            }
        });

        //暂时不支持电影显示
        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_MOVIE, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                TXZResourceManager.getInstance().speakTextOnRecordWin("该操作我还没学会!", true, null);
                return true;
            }
        });


        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_MUSIC, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                LogUtil.logd("process, type=" + type + ", data=" + data);
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                //{"action":"play","answer":"好的，刘德华的忘情水","fuzzy":false,"id":53,
                // "model":{"album":"","artist":["刘德华"],"keywords":[],"title":"忘情水"},
                // "query_time":397.5830078125,
                // "scene":"music","score":95,"t":98,"text":"我要听刘德华的忘情水",
                // "textValue":95,"type":13}
                String scene = jsonBuilder.getVal("scene", String.class);
                String action = jsonBuilder.getVal("action", String.class);

                if (TextUtils.equals(scene, "music") && TextUtils.equals(action, "play")) {
                    MusicManager.getInstance().setMusicWillBePlay(true);
//                    JSONBuilder jsonModel = new JSONBuilder(jsonBuilder.getVal("model", org.json.JSONObject.class));
//                    if (jsonModel.getJSONObject() != null) {
//                        String tts = "暂不支持搜索歌曲，敬请期待";
//                        TtsUtil.speakTextOnRecordWin(tts, true, null);
//                        return true;
//                    }
                    JSONBuilder jsonModel = new JSONBuilder(jsonBuilder.getVal("model", org.json.JSONObject.class));
                    if (jsonModel.getJSONObject() != null) {
                        String title = jsonModel.getVal("title", String.class);
                        String[] artists = jsonModel.getVal("artist", String[].class);
                        StringBuffer stringBuffer = new StringBuffer("正在为您搜索");
                        if (LaunchManager.getInstance().isDialogWinShow()) {

                        } else {
                            if (TextUtils.isEmpty(title)) {
                                if (artists != null && artists.length != 0) {
                                    stringBuffer.append("<br>");
                                    stringBuffer.append("<center><font size='24' color='#FFFFFF'>");
                                    stringBuffer.append(artists[0]);
                                    stringBuffer.append("</font></center>");
                                }
                            } else {
                                stringBuffer.append("<br>");
                                stringBuffer.append("<center><font size='29' color='#FFFFFF'>");
                                stringBuffer.append(title);
                                stringBuffer.append("</font></center>");
                                if (artists != null && artists.length != 0) {
                                    stringBuffer.append("<center><font size='24' color='#FFFFFF'>");
                                    stringBuffer.append(artists[0]);
                                    stringBuffer.append("</font></center>");
                                }
                            }
                        }
                        showSysText(stringBuffer.toString());
                    }
                } else if (TextUtils.equals(scene, "music") && TextUtils.equals(action, "exit")) {
                    MusicManager.getInstance().pause();
//                    AppLogic.runOnUiGround(new Runnable() {
//                        @Override
//                        public void run() {
//                            BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_EXIT);
//                        }
//                    });

                } else if (TextUtils.equals(scene, "music") && TextUtils.equals(action, "favourMusic")) { // 屏蔽收藏
                    TXZResourceManager.getInstance().speakTextOnRecordWin("RS_VOICE_UNSUPPORT_OPERATE", "当前不支持该操作", true, null);
                    return true;
                } else if (TextUtils.equals(scene, "music") && TextUtils.equals(action, "addSubscribe")) { // 屏蔽收藏
                    TXZResourceManager.getInstance().speakTextOnRecordWin("RS_VOICE_UNSUPPORT_OPERATE", "当前不支持该操作", true, null);
                    return true;
                } else if (TextUtils.equals(scene, "music") && TextUtils.equals(action, "ask")) { // 屏蔽询问当前是什么歌曲电台
                    TXZResourceManager.getInstance().speakTextOnRecordWin("RS_VOICE_UNSUPPORT_OPERATE", "当前不支持该操作", true, null);
                    return true;
                }
                return false;
            }
        });

        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_AUDIO, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                JSONBuilder jsonBuilder = new JSONBuilder(data);
                //{"action":"play","answer":"好，来听听这些内容吧。","fuzzy":false,"id":71,
                // "model":{"album":"罗辑思维","artist":[],"audioIndex":[],"category":"","episode":"",
                // "keywords":["罗辑思维"],"subCategory":"","tag":"","title":""},
                // "query_time":779.4489860534668,"scene":"audio","t":98,"text":"我要听逻辑思维","textValue":95,"type":13}
                String scene = jsonBuilder.getVal("scene", String.class);
                String action = jsonBuilder.getVal("action", String.class);

                if (TextUtils.equals(scene, "audio") && TextUtils.equals(action, "play")) {
                    MusicManager.getInstance().setMusicWillBePlay(false);
                    JSONBuilder jsonModel = new JSONBuilder(jsonBuilder.getVal("model", org.json.JSONObject.class));
                    if (jsonModel.getJSONObject() != null && !TextUtils.equals("SEARCH_RANDOM", jsonBuilder.getVal("code", String.class))) {
                        StringBuffer stringBuffer = new StringBuffer("正在为您搜索");
                        showSysText(stringBuffer.toString());
                    }
                }
                return false;
            }
        });

        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_STOCK, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                JSONObject json = JSONObject.parseObject(data);
                if (!json.containsKey("data")
                        || !json.getJSONObject("data").containsKey("result")) {
                    TXZResourceManager.getInstance().speakTextOnRecordWin("查询股票发生异常!", false, null);
                    return true;
                }
                String strAnswer = json.getJSONObject("data").getString("header");
                LogUtil.logd("parse stock text:" + strAnswer);

                final JSONObject jsonData = json.getJSONObject("data").getJSONObject("result");
                String strName = "";
                if (jsonData.containsKey("mName")) {
                    strName = jsonData.getString("mName");
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
                    strStock = "%NAME%已停牌";
                    if (jsonData.containsKey("mName")) {
                        strStock = strStock.replace("%NAME%", jsonData.getString("mName"));
                    }
                } else {
                    if (price > 1000) {
                        if (pszAmount.charAt(0) != '-') {
                            strStock = "当前%NAME%是%VALUE%点，上涨%AMOUNT%点，涨幅%RATE%%";
                        } else {
                            strStock = "当前%NAME%是%VALUE%点，下跌%AMOUNT%点，跌幅%RATE%%";
                            if (pszAmount.length() > 1) {
                                pszAmount = pszAmount.substring(1);
                            }
                            if (pszRate.length() > 1) {
                                pszRate = pszRate.substring(1);
                            }
                        }
                    } else {
                        if (pszAmount.charAt(0) != '-') {
                            strStock = "%NAME%的当前价格是%VALUE%，上涨%AMOUNT%，涨幅%RATE%%";
                        } else {
                            strStock = "%NAME%的当前价格是%VALUE%，下跌%AMOUNT%，跌幅%RATE%%";
                            if (pszAmount.length() > 1) {
                                pszAmount = pszAmount.substring(1);
                            }
                            if (pszRate.length() > 1) {
                                pszRate = pszRate.substring(1);
                            }
                        }
                    }
                    if (jsonData.containsKey("mName")) {
                        strStock = strStock.replace("%NAME%", jsonData.getString("mName"));
                    }
                    strStock = strStock.replace("%VALUE%", pszPrice)
                            .replace("%AMOUNT%", pszAmount)
                            .replace("%RATE%", pszRate);
                }
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("正在为您查询股票");
                stringBuffer.append("<br>");
                stringBuffer.append("<center><font size='29' color='#FFFFFF'>");
                stringBuffer.append(strName);
                stringBuffer.append("</font></center>");
                showSysText(stringBuffer.toString());
                canShowSysText = false;
                TtsUtil.speakTextOnRecordWin("", "正在为您查询" + strName + "的股票", null, false, false, false, new Runnable1<String>(strStock) {
                    @Override
                    public void run() {
                        //临时将显示系统文本给关了
                        canShowSysText = false;
                        LaunchManager.getInstance().launchStock(jsonData.toString());
                        TXZResourceManager.getInstance().speakTextOnRecordWin(mP1, false, null);
                    }
                });
                return true;
            }
        });


        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_WEATHER, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, final String data) {
                JSONObject json = JSONObject.parseObject(data);
                if (!json.containsKey("data")) {
                    LogUtil.logd("parseWeather json:" + json);
                    TXZResourceManager.getInstance().speakTextOnRecordWin("抱歉，当前不支持该操作", false, null);
                    return true;
                }
                if (!json.getJSONObject("data").containsKey("result")) {
                    if (!json.getJSONObject("data").containsKey("header")) {
                        TXZResourceManager.getInstance().speakTextOnRecordWin("抱歉，当前不支持该操作", false, null);
                        return true;
                    } else {
                        TXZResourceManager.getInstance().speakTextOnRecordWin(json.getJSONObject("data").getString("header"), false, null);
                        return true;
                    }
                }
                String strAnswer = json.getJSONObject("data").getString("header");
                if (!TextUtils.isEmpty(strAnswer)) {
                    strAnswer = strAnswer.replace("℃~", "至");
                }
                final JSONObject jsonData = json.getJSONObject("data")
                        .getJSONObject("result");
                String strCityName = "";
                if (jsonData.containsKey("cityName")) {
                    strCityName = jsonData.getString("cityName");
                }

                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("正在为您查询天气");
                stringBuffer.append("<br>");
                stringBuffer.append("<center><font size='29' color='#FFFFFF'>");
                stringBuffer.append(strCityName);
                stringBuffer.append("</font></center>");
                showSysText(stringBuffer.toString());
                canShowSysText = false;
                TtsUtil.speakTextOnRecordWin("", "正在为您查询" + strCityName + "的天气", null, false, false, false, new Runnable1<String>(strAnswer) {
                    @Override
                    public void run() {
                        canShowSysText = false;
                        LaunchManager.getInstance().launchWeather(jsonData.toString());
                        TXZResourceManager.getInstance().speakTextOnRecordWin(mP1, false, null);
                    }
                });
                return true;
            }
        });

        TXZSceneManager.getInstance().setSceneTool(TXZSceneManager.SceneType.SCENE_TYPE_POI_CHOICE, new TXZSceneManager.SceneTool() {
            @Override
            public boolean process(TXZSceneManager.SceneType type, String data) {
                LogUtil.logd("process, type=" + type + ", data=" + data);
                if (NavManager.getInstance().isGaodeAlongWayError()) {
                    // 通过标志位来拦截这个搜索结果还是有问题。如果进入这个场景的时候刚好结束播报，那这个标志位就没有作用了。所以本质还是要取消掉搜索结果。
                    return true;
                }
                return false;
            }
        });

        TXZStatusManager.getInstance().addStatusListener(new TXZStatusManager.StatusListener() {
            @Override
            public void onBeginAsr() {
                notifyStateChange(IImage.STATE_RECORD_START);
            }

            @Override
            public void onBeginCall() {

            }

            @Override
            public void onBeginTts() {
                notifyStateChange(IImage.STATE_TTS_START);
            }

            @Override
            public void onBeepEnd() {
            }

            @Override
            public void onEndAsr() {
                notifyStateChange(IImage.STATE_RECORD_END);
            }

            @Override
            public void onEndCall() {

            }

            @Override
            public void onEndTts() {
                notifyStateChange(IImage.STATE_TTS_END);
            }

            @Override
            public void onMusicPause() {

            }

            @Override
            public void onMusicPlay() {

            }
        });
    }

    @Override
    public void setOperateListener(RecordWinOperateListener listener) {
        mRecordWinOperateListener = listener;
        listener.useDefaultSelector(true);
    }

    /**
     * 打开录音界面，即小欧在听我们说话的界面。
     * 设置{@link TXZResourceManager#setRecordWin(TXZResourceManager.RecordWin)}后，唤醒词的回调由{@link TXZResourceManager.RecordWin#open()}处理
     */
    @Override
    public void open() {
        isRecordWinClosed = false;
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_VOICE_OPEN);
        LogUtil.logd("open");
        DebugUtils.sendWakeupResult("小O小O");
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_WAKE_UP);
    }

    @Override
    public void close() {
        isRecordWinClosed = true;
        notifyStateChange(IImage.STATE_NORMAL);
        LogUtil.logd("close");
        //关闭声控界面的时候如果在前台才返回launcher
        if (LaunchManager.getInstance().isLaunchResume()
                && !LaunchManager.getInstance().isActiveWechatQr()
                && !LaunchManager.getInstance().isCurrModule(LaunchManager.ViewModuleType.TYPE_LOGIN)) {
            LaunchManager.getInstance().launchBack();
        }
        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_VOICE_DISMISS);
    }

    @Override
    public void onVolumeChange(int volume) {

    }

    @Override
    public void onProgressChanged(int progress) {

    }

    @Override
    public void onStatusChange(RecordStatus status) {
        int state = 0;
        switch (status) {
            case STATUS_IDLE:
                state = IImage.STATE_NORMAL;
                LaunchManager.getInstance().updateState(state);
                break;
            case STATUS_RECORDING:
                state = IImage.STATE_RECORD_START;
                break;
            case STATUS_RECOGONIZING:
                state = IImage.STATE_RECORD_END;
                break;
            default:
                state = IImage.STATE_NORMAL;
                break;
        }
        LogUtil.logd("record state change1, state=" + state);
    }

    private void notifyStateChange(int state) {
        if (!isRecordWinClosed || IImage.STATE_NORMAL == state
                || IImage.STATE_TTS_START == state
                || IImage.STATE_TTS_END == state) {
            LogUtil.logd("record state change2, state=" + state);
            LaunchManager.getInstance().updateState(state);
        }
    }

    @Override
    public void showUsrText(String text) {
        LogUtil.logd("showUsrText:" + text);
    }

    @Override
    public void showUsrPartText(String text) {

    }

    @Override
    public void showSysText(String text) {
        // FIXME 网络异常时候通知远峰，临时这样处理。
        if (text.contains("请检查网络连接")) {
            Intent intent = new Intent("yf.intent.action.ACTION_SOCKET_UNREACHABLE");
            GlobalContext.get().sendBroadcast(intent);
            LogUtil.logd("send broadcast yf.intent.action.ACTION_SOCKET_UNREACHABLE");
        }

        if (canShowSysText) {
            LaunchManager.getInstance().addSystemText(text);
        } else {
            canShowSysText = true;
        }
        LogUtil.logd("showSysText:" + text);
    }

    @Override
    public void showWheatherInfo(String data) {
        LogUtil.logd("showWheatherInfo:" + data);
//        LaunchManager.getInstance().launchWeather(data);
    }

    @Override
    public void showStockInfo(String data) {
        LogUtil.logd("showStockInfo:" + data);
//        LaunchManager.getInstance().launchStock(data);
    }

    @Override
    public void showContactChoice(String data) {
        LogUtil.logd("showContactChoice:" + data);
        LaunchManager.getInstance().launchList(data);
    }

    @Override
    public void showAddressChoice(String data) {
        LogUtil.logd("showAddressChoice:" + data);
        LaunchManager.getInstance().launchList(data);
    }

    @Override
    public void showWxContactChoice(String data) {
        LogUtil.logd("showWxContactChoice:" + data);
        LaunchManager.getInstance().launchList(data);
    }

    @Override
    public void showAudioChoice(String data) {
        LogUtil.logd("showAudioChoice:" + data);
        LaunchManager.getInstance().launchList(data);
    }

    @Override
    public void showListChoice(int type, String data) {
        LogUtil.logd("showListChoice:" + type + "  " + data);
        LaunchManager.getInstance().launchList(data);
    }

    @Override
    public void showData(String data) {
        LogUtil.logd("showData:" + data);
    }

    @Override
    public void snapPager(boolean next) {
        LogUtil.logd("snapPager:" + next);
    }

    public boolean isRecordWinClosed() {
        return isRecordWinClosed;
    }

    public void ctrlRecordWinDismiss() {
        if (LaunchManager.getInstance().isLaunchResume()) {
            LaunchManager.getInstance().launchBack();
        }
        if (mRecordWinOperateListener != null) {
            mRecordWinOperateListener.onClose();
        }
    }
}