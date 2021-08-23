package com.txznet.music.service.impl;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Looper;
import android.text.TextUtils;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.action.AiActionCreator;
import com.txznet.music.action.WxPushActionCreator;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.PlayListDataDao;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.http.api.txz.TXZMusicApi;
import com.txznet.music.data.http.api.txz.TXZMusicApiImpl;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.http.api.txz.entity.req.Location;
import com.txznet.music.data.http.api.txz.entity.resp.AiPullData;
import com.txznet.music.data.http.api.txz.entity.resp.PullData;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.helper.PushLogicHelper;
import com.txznet.music.listener.WinListener;
import com.txznet.music.model.INormalCallback;
import com.txznet.music.model.PushInterceptor;
import com.txznet.music.service.MusicInteractionWithCore;
import com.txznet.music.service.push.AppPushInvoker;
import com.txznet.music.service.push.PushUtils;
import com.txznet.music.util.HttpUtils;
import com.txznet.music.util.JsonHelper;
import com.txznet.music.util.Logger;
import com.txznet.music.util.ToastUtils;
import com.txznet.rxflux.Operation;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.bean.LocationData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Single;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static com.txznet.sdk.music.MusicInvokeConstants.KEY_PUSH_INTERCEPT;
import static com.txznet.sdk.music.MusicInvokeConstants.KEY_PUSH_NEED_MORE_AUDIOS;
import static com.txznet.sdk.music.MusicInvokeConstants.KEY_PUSH_VERSION;

/**
 * 推送解析
 * 搜索日志关键字：telenewbie::|dataPushInterface|CmdInvoke:|sdk.init|pushCommand|TXZNetRequest:
 *
 * @author telen
 * @date 2018/12/20,16:46
 */
public class PushCommand extends BaseCommand {
    public static final String TAG = Constant.LOG_TAG_PUSH;

    private TXZMusicApi mTXZMusicApi = TXZMusicApiImpl.getDefault();


    /**
     * 单例对象
     */
    private volatile static PushCommand singleton;
    private Disposable mShowTaskDisposable;

    public static PushCommand getInstance() {

        if (singleton == null) {
            synchronized (PushCommand.class) {
                if (singleton == null) {
                    singleton = new PushCommand();
                }
            }
        }
        return singleton;
    }


    private PushCommand() {
        addCmd("dataPushInterface", (pkgName, cmd, data) -> {
            handleDataPushInterface(data);
            return new byte[0];
        });

        addCmd("push.tool.set", (pkgName, cmd, data) -> {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            int version = jsonBuilder.getVal(KEY_PUSH_VERSION, int.class, 0);
            if (version == 1) {
                int intercept = jsonBuilder.getVal(KEY_PUSH_INTERCEPT, int.class, 0);
                PushInterceptor.getInstance().setListener(pkgName, intercept);
            }
            return new byte[0];
        });

        addCmd("push.tool.resume.play", (pkgName, cmd, data) -> {
            PushInterceptor.getInstance().resumeDelayTask();
            return new byte[0];
        });

        addCmd("push.tool.clear", (pkgName, cmd, data) -> {
            PushInterceptor.getInstance().clearListener();
            return new byte[0];
        });

        addCmd("push.tool.need.audios", (pkgName, cmd, data) -> {
            JSONBuilder jsonBuilder = new JSONBuilder(data);
            boolean isNeed = jsonBuilder.getVal(KEY_PUSH_NEED_MORE_AUDIOS, boolean.class, false);
            PushInterceptor.getInstance().setNeedAudios(isNeed);
            return new byte[0];
        });

        addCmd("push.click.continue", (pkgName, cmd, data) -> {
            PushInterceptor.getInstance().clickContinue();
            return new byte[0];
        });

        addCmd("push.click.cancel", (pkgName, cmd, data) -> {
            PushInterceptor.getInstance().clickCancel();
            return new byte[0];
        });


        if (BuildConfig.DEBUG) {
            GlobalContext.get().registerReceiver(new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    LocationData currentLocationInfo = TXZLocationManager.getInstance().getCurrentLocationInfo();
                    Logger.d(TAG, " location update:refresh gps " + (currentLocationInfo == null ? "null" : currentLocationInfo.toString()));
                    if (currentLocationInfo != null) {
                        if (currentLocationInfo.dbl_lat != 22.53800115 && currentLocationInfo.dbl_lng != 113.9565695) {
                            JSONObject js = new JSONObject();
                            try {
                                js.put("lng", currentLocationInfo.dbl_lng);
                                js.put("lat", currentLocationInfo.dbl_lat);
                                MusicInteractionWithCore.requestData("txz.music.dataInterface", "/text/pushShortPlay", js.toString().getBytes());
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Logger.e(TAG, e.toString());
                            }
                            return;
                        }
                    }
                    ToastUtils.showShortOnUI("GPS错误");
                }
            }, new IntentFilter("test"));
        }
    }

    private final List<com.txz.push_manager.PushManager.PushCmd_Audio> mPushList = new ArrayList<>();

    /**
     * 解析数据
     *
     * @param data
     */
    private void handleDataPushInterface(byte[] data) {
        com.txz.push_manager.PushManager.PushCmd_Audio pushAudio = null;
        try {
            pushAudio = com.txz.push_manager.PushManager.PushCmd_Audio.parseFrom(data);
        } catch (InvalidProtocolBufferNanoException e) {
            e.printStackTrace();
        }

        if (pushAudio == null) {
            return;
        }
        synchronized (mPushList) {
            mPushList.add(pushAudio);
        }
        AppLogic.runOnBackGround(mProcessPushListTask);
    }

    private Runnable mProcessPushListTask = this::processPushList;

    public void processPushList() {
        if (WinListener.isShowSoundUI) {
            LogUtil.d(TAG, "processPushList delay, isShowSoundUI=true");
            AppLogic.runOnBackGround(mProcessPushListTask, 1000 * 5);
            return;
        } else {
            LogUtil.d(TAG, "processPushList now");
        }
        AppLogic.removeBackGroundCallback(mProcessPushListTask);
        synchronized (mPushList) {
            if (mPushList.isEmpty()) {
                LogUtil.d(TAG, "processPushList complete");
                return;
            }
            com.txz.push_manager.PushManager.PushCmd_Audio audio = mPushList.remove(0);
            handleDataPushInterfaceInner(audio);
            if (mPushList.isEmpty()) {
                LogUtil.d(TAG, "processPushList complete");
            } else {
                AppLogic.runOnBackGround(mProcessPushListTask, 1000 * 5);
            }
        }
    }

    public void processPushListDelay() {
        AppLogic.removeBackGroundCallback(mProcessPushListTask);
        AppLogic.runOnBackGround(mProcessPushListTask, 1000 * 5);
    }

    private void handleDataPushInterfaceInner(com.txz.push_manager.PushManager.PushCmd_Audio pushAudio) {
        final String strData = new String(pushAudio.strData);
        String cmd = new String(pushAudio.strCmd);
        Logger.d(TAG, "data:" + strData);
        Logger.d(TAG, "cmd:" + cmd);

        switch (Integer.valueOf(cmd)) {
            case com.txz.push_manager.PushManager.SHORTPLAY:
                throw new RuntimeException("求不吭好吗？？？？");
            case com.txz.push_manager.PushManager.PULL:
                PullData pullData = JsonHelper.fromJson(strData, PullData.class);
                Logger.d(TAG, pullData.toString());

                // TODO: 2018/12/20 上报
                // ReportEventProtocol.reportPushEvent(PushEvent.ACTION_PRE_PUSH_ARRIVE, PushEvent.getType(pullData), pullData.getId() + "");

                switch (pullData.getType()) {
                    case PullData.TYPE_NEWS:
                        //热点推送
                        getGps(pullData, new INormalCallback<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                reqData(pullData, location);
                            }

                            @Override
                            public void onError() {
                                Logger.d(Constant.LOG_TAG_PUSH, "onError:" + pullData.toString());
                            }
                        });
                        break;
                    case PullData.TYPE_AUDIOS:
                    case PullData.TYPE_UPDATE:
                        //音频列表
                        reqData(pullData, null);
                        break;
                    default:
                        Logger.e(TAG, "can't handle type:" + pullData.getType());
                        break;
                }
                break;
            case 3: // AI电台
                AiPullData aiPullData = JsonHelper.fromJson(strData, AiPullData.class);
                AiActionCreator.get().pushAi(aiPullData);
                break;
            default:
                break;
        }
    }


    //最大请求次数
    final int MAX_REQ_COUNT = 5;
    int gpsReqCount = 0;

    /**
     * 同步Gps的信息给到后台
     *
     * @param pullData
     */
    private void getGps(final PullData pullData, INormalCallback<Location> callback) {
        LocationData currentLocationInfo = TXZLocationManager.getInstance().getCurrentLocationInfo();
        Logger.d(TAG, " location update:refresh gps " + (currentLocationInfo == null ? "null" : currentLocationInfo.toString()));
        if (currentLocationInfo != null) {
            if (currentLocationInfo.dbl_lat != 22.53800115 && currentLocationInfo.dbl_lng != 113.9565695) {
                Location location = new Location(currentLocationInfo.dbl_lng, currentLocationInfo.dbl_lat, pullData.getFrom());
                callback.onSuccess(location);
                return;
            }
        }
        if (++gpsReqCount > MAX_REQ_COUNT) {
            gpsReqCount = 0;
            callback.onError();
            return;
        }

        AppLogic.runOnBackGround(() -> {
            //如果请求太多次,则不请求了
            getGps(pullData, callback);
        }, 10 * 1000);

    }

    private void reqData(PullData pullData, Location location) {
        Disposable disposable = Single.create((SingleOnSubscribe<PushResponse>) emitter -> {
            try {
                emitter.onSuccess(mTXZMusicApi.getPushData(pullData.getService(), location).blockingFirst());
            } catch (Exception e) {
                emitter.onError(e);
            }
        }).subscribeOn(Schedulers.single()).observeOn(Schedulers.single()).subscribe(pushResponse -> {
            handlePushAudios(pullData, pushResponse);
        }, throwable -> {
        });
    }


    private void handlePushAudios(final PullData pullData, final PushResponse pushResponse) {
        if (WinListener.isShowSoundUI) {
            LogUtil.d(TAG + "push block, record win is showing");
            return;
        }

        // 检测响应合法性
        if (checkPushResponseVerify(pushResponse)) {
            stopShowTask();
        } else {
            return;
        }

        if (!SharedPreferencesUtils.isOpenPush()) {
            // 非微信推送
            if (!PushResponse.PUSH_SERVICE_AUDIOS.equals(pushResponse.getService())) {
                Logger.d(TAG, " push is closed:" + pullData.toString());
                return;
            }
        }

        PushInterceptor.getInstance().showData(pullData.getType(), pushResponse, new INormalCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    // TODO: 2018/8/15 声控界面再的时候，应该怎么处理，以及【开机，多个应用多条TTS同时播报】
                    //https://www.tapd.cn/21709951/bugtrace/bugs/view?bug_id=1121709951001003370
//                    player = new QuickReportPlayer(pullData, pushResponse);
//                    player.start();
                    //响应事件
                    doPushResponse(pushResponse);
                }
            }

            @Override
            public void onError() {

            }
        });
    }

    private boolean checkPushResponseVerify(PushResponse pushResponse) {
        if (pushResponse == null) {
            return false;
        }

        return pushResponse.getPreAction() != PushResponse.PRE_ACTION_PLAY_URL_OR_TTS
                || !TextUtils.isEmpty(pushResponse.getTts())
                || !TextUtils.isEmpty(pushResponse.getMp3());
    }

    /**
     * 开始响应数据
     *
     * @param pushResponse
     */
    private void doPushResponse(PushResponse pushResponse) {
        if (!checkExistsTips(pushResponse)) {
            Logger.d(TAG, "is not valid tips:" + pushResponse.getTip() + ", end tips:" + pushResponse.getEndTip());
            return;
        }
        int preAction = pushResponse.getPreAction();
        if (preAction == PushResponse.PRE_ACTION_FORCE_PLAY) {
            List<TXZAudio> audioList = pushResponse.getArrAudio();
            if (audioList == null || audioList.size() == 0) {
                Logger.e(TAG, " !!!!!!!!!! push audio list is null or empty !!!!!!!!!! ");
                return;
            }
            TXZAudio audio = audioList.get(0);
            readyShow(PushUtils.getCacheFilePath(pushResponse.getTip()), audio, pushResponse);
        } else if (preAction == PushResponse.PRE_ACTION_PLAY_URL_OR_TTS) {
            String tip = pushResponse.getTip();
            String tts = pushResponse.getTts();
            String mp3 = pushResponse.getMp3();
            Logger.d(TAG, "tip:" + tip + " TTS:" + tts + " mp3:" + mp3);
            startPlay(pushResponse);
        }
    }

    private void startPlay(PushResponse pushResponse) {
        String tip = PushUtils.getCacheFilePath(pushResponse.getTip());
        String tts = pushResponse.getTts();
        String mp3 = pushResponse.getMp3();
        if (!TextUtils.isEmpty(mp3)) {
            TXZAudio audio = new TXZAudio();
            audio.sid = 9;
            audio.id = 9;
            audio.downloadType = TXZAudio.DOWNLOADTYPE_DIRECT;
            audio.strDownloadUrl = mp3;
            readyShow(tip, audio, pushResponse);
        } else if (!TextUtils.isEmpty(tts)) {
            readyShow(tip, tts, pushResponse);
        }
    }

    /**
     * 准备好可以展示
     */
    private void readyShow(String tip, TXZAudio audio, PushResponse pushResponse) {
        // 小程序的推送逻辑,和此处的不一致,所以,到时候,新开个类,来处理那边来的内容
        // TODO: 2018/12/20 如果是小程序的话,则交给Core进行展示,如果是公版的程序,则交给tts来进行播报
        if (isProgram()) {

        } else {
            playTTsForResponse(tip, "", audio, pushResponse);
        }
    }

    /**
     * 准备好可以展示
     */
    private void readyShow(String tip, String tts, PushResponse pushResponse) {
        // TODO: 2018/12/27  如果是小程序的话,则交给Core进行展示,如果是公版的程序,则交给tts来进行播报
        if (isProgram()) {

        } else {
            playTTsForResponse(tip, tts, null, pushResponse);
        }
    }

    /**
     * 是不是小程序
     *
     * @return
     */
    private boolean isProgram() {
        return false;
    }

    private boolean isPlayTtsInvoking; // 是否在执行推送处理，从TIP->TTS/AUDIO->UI展示

    // 具体推送处理
    private void playTTsForResponse(String tip, String tts, TXZAudio audio, PushResponse pushResponse) {
        Logger.d(TAG, "playTTsForResponse isOpenPush:" + SharedPreferencesUtils.isOpenPush() + ", isShowViewed:" + SharedPreferencesUtils.getShowWindowView() + ", isAppOpened:" + PushLogicHelper.getInstance().isAppOpened() + ", currentService:" + pushResponse.getService());
        // 校验推送内容
        if (!pushResponse.checkValidKeys()) {
            Logger.w(TAG, "playTTsForResponse check failed");
            return;
        }

        // 非微信推送
        if (!PushResponse.PUSH_SERVICE_AUDIOS.equals(pushResponse.getService())) {
            // 受点火智能播放开关影响
            if (!SharedPreferencesUtils.isOpenPush()) {
                Logger.w(TAG, "playTTsForResponse pass, isOpenPush=false");
                return;
            }
            // 受是否展示过横幅影响
            if (SharedPreferencesUtils.getShowWindowView()) {
                Logger.w(TAG, "playTTsForResponse pass, already show");
                return;
            }
            // 打开过同听
            if (PushLogicHelper.getInstance().isAppOpened()) {
                Logger.w(TAG, "playTTsForResponse pass, app opened");
                return;
            }
            // 执行中
            if (isPlayTtsInvoking) {
                Logger.w(TAG, "playTTsForResponse pass, play tts invoking");
                return;
            }
        }

        do {
            // 微信推送音频特殊处理
            if (PushResponse.PUSH_SERVICE_AUDIOS.equals(pushResponse.getService())) {
                List<PushItem> pushItemList = PushUtils.getPushItem(pushResponse, txzAudio -> AudioConverts.convert2PushItem(txzAudio, PushItem.STATUS_UNREAD));
                WxPushActionCreator.getInstance().saveWxPushData(pushItemList);
                // TTS播报中
                // 正在弹窗
                if (isPlayTtsInvoking
                        || AppPushInvoker.getInstance().isShowing()) {
                    // 插入到数据库
                    Logger.w(TAG, "playTTsForResponse pass, insert wx push");
                    return;
                } else {
                    // 非弹窗状态
                    // 没有打开点火智能播放开关
                    // 展示过弹窗
                    if (!SharedPreferencesUtils.isOpenPush()
                            || SharedPreferencesUtils.getShowWindowView()) {
                        //直接播放
                        Logger.w(TAG, "playTTsForResponse play wx push now");
                        PushUtils.speakText(() -> TtsUtil.speakVoice(tip, new TtsUtil.ITtsCallback() {
                            @Override
                            public void onSuccess() {
                                AppPushInvoker.getInstance().initData(pushResponse, null);
                                AppPushInvoker.getInstance().onClickFirstItem(Operation.AUTO);
                            }
                        }));
                        return;
                    }
                }
            } else {
                //可以弹框
                break;
            }
        } while (false);

        isPlayTtsInvoking = true;

        PushUtils.speakText(() -> TtsUtil.speakVoice(tip, new TtsUtil.ITtsCallback() {
            @Override
            public void onSuccess() {
                // 打开过同听
                if (PushLogicHelper.getInstance().isAppOpened()) {
                    Logger.w(TAG, "playTTsForResponse pass, app opened");
                    return;
                }

                PushUtils.speakText(() -> TtsUtil.speakText(tts, new TtsUtil.ITtsCallback() {
                    @Override
                    public void onBegin() {
                        super.onBegin();
                        Logger.d(TAG, "playTTS:onBegin()");

                        // 打开过同听
                        if (PushLogicHelper.getInstance().isAppOpened()) {
                            Logger.w(TAG, "playTTsForResponse pass, app opened");
                            PushUtils.cancelSpeak();
                            return;
                        }

                        PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
                        PlayListData playListData = playListDataDao.getPlayListData();
                        AppPushInvoker.getInstance().initData(pushResponse, playListData);
                        if (audio == null) {
                            //展示UI
                            showNotificationOnUI(pushResponse);
                        }
                    }

                    @Override
                    public void onError(int iError) {
                        super.onError(iError);
                        Logger.d(TAG, "playTTS:onError()" + iError);
                    }

                    @Override
                    public void onSuccess() {
                        Logger.d(TAG, "playTTS:onEnd()");

                        // 打开过同听
                        if (PushLogicHelper.getInstance().isAppOpened()) {
                            Logger.w(TAG, "playTTsForResponse pass, app opened");
                            return;
                        }

                        if (audio == null) {
                            AppPushInvoker.getInstance().onPlayEnd();
                        } else {
                            //展示UI
                            showNotificationOnUI(pushResponse);
                            AppPushInvoker.getInstance().onPlayBegin(AudioConverts.convert2Audio(audio));
                        }
                    }
                }));
            }
        }));
    }

    private void showNotificationOnUI(PushResponse pushResponse) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            AppLogic.runOnUiGround(() -> {
                showNotificationOnUI(pushResponse);
            });
        } else {
            SharedPreferencesUtils.setShowWindowView(true);
            AppPushInvoker.getInstance().onShowView();
            isPlayTtsInvoking = false;
        }
    }


    /**
     * 查看tips是否有效,存在,不为空.
     *
     * @param pushResponse 响应体
     * @return true 有效
     */
    private boolean checkExistsTips(PushResponse pushResponse) {
        if (!TextUtils.isEmpty(pushResponse.getTip())) {
            if (!PushUtils.checkTipFileExists(pushResponse.getTip())) {
                downloadTip(pushResponse);
                return false;
            }
        }

        if (!TextUtils.isEmpty(pushResponse.getEndTip())) {
            if (!PushUtils.checkTipFileExists(pushResponse.getEndTip())) {
                downloadEndTip(pushResponse);
                return false;
            }
        }
        return true;
    }

    /**
     * 下载tip 数据
     *
     * @param pushResponse
     */
    private void downloadTip(PushResponse pushResponse) {
        Disposable disposable = Observable.just(pushResponse.getTip())
                .map(s -> {
                    int i = HttpUtils.downloadFile(s, PushUtils.getCacheFilePath(s));
                    return i >= 0;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Logger.d(TAG, "download tip:" + aBoolean);
                    if (aBoolean) {
                        doPushResponse(pushResponse);
                    }
                });
    }

    private void downloadEndTip(PushResponse pushResponse) {
        Disposable disposable = Observable.just(pushResponse.getEndTip())
                .map(s -> {
                    int i = HttpUtils.downloadFile(s, PushUtils.getCacheFilePath(s));
                    return i >= 0;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aBoolean -> {
                    Logger.d(TAG, "download end tip:" + aBoolean);
                    if (aBoolean) {
                        doPushResponse(pushResponse);
                    }
                });
    }


    /**
     * 开启任务，用于展示
     */
    public void startShowTask() {
        /*
         * 倒计时内没有收到推送，展示本地音乐
         * 若上次播放的是本地音乐，首栏信息："今日推荐：${上次播放的音频名称}"
         * 若上次播放的是不是本地音乐，首栏信息："今日推荐：本地音乐随机一首"
         */
        mShowTaskDisposable = Observable.create((ObservableOnSubscribe<Integer>) emitter -> {
            //展示本地数据
            int localCount = DBUtils.getDatabase(GlobalContext.get()).getLocalAudioDao().getCount();
            emitter.onNext(localCount);
            emitter.onComplete();

        })
                .delay(60, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(count -> {
                    String lastTimeAudioName = null;
                    PlayListData listData = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao().getPlayListData();
                    if (listData != null && PlayScene.LOCAL_MUSIC == listData.scene && listData.audio != null) {
                        lastTimeAudioName = listData.audio.name;
                    }
                    Logger.d(Constant.LOG_TAG_PUSH, "startShowTask: localAudio size=" + count);
                    if (count > 0) {
                        PushResponse pushResponse = new PushResponse();
                        if (lastTimeAudioName == null) {
                            pushResponse.setTitle("今日推荐：本地音乐随机一首");
                        } else {
                            pushResponse.setTitle("今日推荐：" + lastTimeAudioName);
                        }
                        pushResponse.setService(PushResponse.PUSH_LOCAL_COMMAND);
                        PushResponse.Key keyConfirm = new PushResponse.Key();
                        keyConfirm.setArrCms(Collections.singletonList("取消"));
                        keyConfirm.setText("取消");
                        PushResponse.Key keyCancel = new PushResponse.Key();
                        keyCancel.setArrCms(Collections.singletonList("取消"));
                        keyCancel.setText("取消");
                        pushResponse.setArrKeys(Arrays.asList(keyConfirm, keyCancel));
                        pushResponse.setDefUserChoose(1);
                        pushResponse.setTts("即将为你播放本地音乐");

//                        readyShow("即将为你播放本地音乐", pushResponse);
                        playTTsForResponse(null, "即将为你播放本地音乐", null, pushResponse);
                    } else {
                        //不弹
                    }
                });
    }

    /**
     * 取消展示的任务
     */
    public void stopShowTask() {
        Logger.d(Constant.LOG_TAG_PUSH, "stopShowTask:");
        PushLogicHelper.getInstance().setShowViewExecutable(true);
        if (mShowTaskDisposable != null && !mShowTaskDisposable.isDisposed()) {
            mShowTaskDisposable.dispose();
        }
    }

}
