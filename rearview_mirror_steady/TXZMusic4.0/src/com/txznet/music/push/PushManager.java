package com.txznet.music.push;

import android.content.Intent;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.fm.bean.InfoMessage;
import com.txznet.fm.manager.ObserverManage;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.albumModule.logic.net.response.ResponseAlbumAudio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.INormalCallback;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.config.ConfigManager;
import com.txznet.music.data.http.AlbumRepository;
import com.txznet.music.listener.WinListener;
import com.txznet.music.net.HttpUtils;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestAudioCallBack;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.QuickReportPlayer;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.push.bean.Location;
import com.txznet.music.push.bean.PullData;
import com.txznet.music.push.bean.PullReport;
import com.txznet.music.push.bean.PushResponse;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.ReportManager;
import com.txznet.music.report.bean.PushEvent;
import com.txznet.music.report.bean.ReportRespEvent;
import com.txznet.music.service.MusicInteractionWithCore;
import com.txznet.music.util.TimeUtils;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.Utils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity4;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.bean.LocationData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import io.reactivex.functions.Consumer;

/**
 * Created by ASUS User on 2017/1/3.
 */

public class PushManager {

    public static final int CMD_TYPE_SHORTPLAY = com.txz.push_manager.PushManager.SHORTPLAY;
    public static final int CMD_TYPE_PULL = com.txz.push_manager.PushManager.PULL;
    public static final int CMD_TYPE_AI_PUSH = 3;
    public static final int CMD_TYPE_REPORT = 4;

    private static final String TAG = "Music:PushManager:";
    private static PushManager sInstance;
    private QuickReportPlayer player;

    private PushManager() {
        ObserverManage.getObserver().addObserver(new Observer() {
            @Override
            public void update(Observable o, Object arg) {
                if (arg != null && arg instanceof InfoMessage) {
                    InfoMessage info = (InfoMessage) arg;
                    switch (info.getType()) {
                        case InfoMessage.BE_PLAY_OR_PAUSE:
                        case InfoMessage.BE_PLAY_LAST:
                        case InfoMessage.BE_PLAY_NEXT:
                            releaseDelay();
                            break;
                    }
                }
            }
        });
    }

    public static PushManager getInstance() {
        if (sInstance == null) {
            synchronized (PushManager.class) {
                if (sInstance == null) {
                    sInstance = new PushManager();
                }
            }
        }
        return sInstance;
    }


    /**
     * 测试用
     */
    public void test() {
        LocationData currentLocationInfo = TXZLocationManager.getInstance().getCurrentLocationInfo();
        LogUtil.d(TAG + "location update:refresh gps " + (currentLocationInfo == null ? "null" : currentLocationInfo.toString()));
        if (currentLocationInfo != null) {
            if (currentLocationInfo.dbl_lat != 22.53800115 && currentLocationInfo.dbl_lng != 113.9565695) {
                JSONObject js = new JSONObject();
                try {
                    js.put("lng", currentLocationInfo.dbl_lng);
                    js.put("lat", currentLocationInfo.dbl_lat);
                    MusicInteractionWithCore.requestData("txz.music.dataInterface", Constant.GET_SHORT_PLAY, js.toString().getBytes());
                } catch (JSONException e) {
                    e.printStackTrace();
                    LogUtil.e(TAG + e.toString());
                }
                return;
            }
        }
        ToastUtils.showShort("GPS错误");
    }

    private final List<com.txz.push_manager.PushManager.PushCmd_Audio> mPushList = new ArrayList<>();

    public void handleDataPushInterface(byte[] data) {
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
        LogUtil.d(TAG, "data:" + strData);
        LogUtil.d(TAG, "cmd:" + cmd);

        switch (Integer.valueOf(cmd)) {
            case CMD_TYPE_SHORTPLAY:
                //如果，走到这里，记得骂下后台，这是因为，正式环境，不能测试
                throw new RuntimeException("求不吭好吗？？？？");

            case CMD_TYPE_PULL:
                PullData pullData = JsonHelper.toObject(PullData.class, strData);
                LogUtil.d(TAG, pullData.toString());

                ReportEvent.reportPushEvent(PushEvent.ACTION_PRE_PUSH_ARRIVE, PushEvent.getType(pullData), pullData.getId() + "");

                switch (pullData.getType()) {
                    case PullData.TYPE_NEWS:
                        handleShortPlayPullMsg(pullData);
                        break;
                    case PullData.TYPE_AUDIOS:
                        handlePushAudioPullMsg(pullData);
                        break;
                    case PullData.TYPE_UPDATE:
                        handleUpdatePullMsg(pullData);
                        break;
                    default:
                        LogUtil.e(TAG, "can't handle type:" + pullData.getType());
                        break;
                }
                break;
            case CMD_TYPE_AI_PUSH:
            case CMD_TYPE_REPORT:
//                JsonObject jObj = new JsonParser().parse(strData).getAsJsonObject();
//                PullReport pullReport = new PullReport();
//                pullReport.setData(jObj.get("data").getAsString());
//                LogUtil.d(TAG, pullReport.toString());
//                handleReportPullMsg(pullReport);
                break;
            default:
                break;
        }
    }

    private void handleShortPlayPullMsg(PullData pullData) {
//        if (SharedPreferencesUtils.getIsShortPlayNeedTrigger()) {
//            LogUtil.d(TAG + "shortPlay need trigger");
//            return;
//        }
        refreshGps(pullData);
    }


    private void handleUpdatePullMsg(final PullData pullData) {
        NetManager.getInstance().requestPushData(pullData.getService(), null, new RequestCallBack<PushResponse>(PushResponse.class) {
            @Override
            public void onResponse(PushResponse data) {
                ReportEvent.reportPushEvent(PushEvent.ACTION_PUSH_ARRIVE, data.getType() + "", data.getMid());
                LogUtil.d(TAG, "data:" + data.toString());
                handleUpdate(pullData, data);
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.e(TAG, error.toString());
            }
        });
    }

    private void handlePushAudioPullMsg(final PullData pullData) {
        NetManager.getInstance().requestPushData(pullData.getService(), null, new RequestCallBack<PushResponse>(PushResponse.class) {
            @Override
            public void onResponse(PushResponse data) {
                ReportEvent.reportPushEvent(PushEvent.ACTION_PUSH_ARRIVE, data.getType() + "", data.getMid());
                LogUtil.d(TAG, "data:" + data.toString());
                handlePushAudios(pullData, data);
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.e(TAG, error.toString());
            }
        });
    }

    private void handleReportPullMsg(final PullReport pullReport) {
        HttpUtils.HttpCallbackListener listener = new HttpUtils.HttpCallbackListener() {
            @Override
            public void onSuccess(String response) {
                ReportRespEvent event = new ReportRespEvent();
                event.type = "third_report_push";
                event.data = response;
                ReportManager.getInstance().reportImmediate(event);
            }

            @Override
            public void onError(int errorCode) {
                LogUtil.e(TAG, "Error: req report error, code=" + errorCode);
            }
        };
        if (pullReport.getIsPost() == 1) {
            HttpUtils.sendPostRequest(pullReport.getUrl(), pullReport.getHeader(), pullReport.getData(), 5000, listener);
        } else {
            HttpUtils.sendGetRequest(pullReport.getUrl(), pullReport.getHeader(), 5000, listener);
        }
    }

    private void refreshGps(final PullData pullData) {
        LocationData currentLocationInfo = TXZLocationManager.getInstance().getCurrentLocationInfo();
        LogUtil.d(TAG + "location update:refresh gps " + (currentLocationInfo == null ? "null" : currentLocationInfo.toString()));
        if (currentLocationInfo != null) {
            if (currentLocationInfo.dbl_lat != 22.53800115 && currentLocationInfo.dbl_lng != 113.9565695) {
                Location location = new Location(currentLocationInfo.dbl_lng, currentLocationInfo.dbl_lat, pullData.getFrom());
                NetManager.getInstance().requestPushData(pullData.getService(), location, new RequestCallBack<PushResponse>(PushResponse.class) {
                    @Override
                    public void onResponse(PushResponse data) {
                        ReportEvent.reportPushEvent(PushEvent.ACTION_PUSH_ARRIVE, data.getType() + "", data.getMid());
                        handlePushShortPlay(pullData, data);
                    }

                    @Override
                    public void onError(String cmd, Error error) {
                        LogUtil.e(TAG, "Error:" + error.toString());
                    }
                });
                return;
            }
        }

        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                refreshGps(pullData);
            }
        }, 10 * 1000);

    }

    boolean isNeedJump;

    public void playDetail(final EnumState.Operation operation, final PushResponse pushResponse) {
        if (pushResponse == null) {
            return;
        }
        isNeedJump = true;
        switch (pushResponse.getPostAction()) {
            case PushResponse.POST_ACTION_PLAY_ALBUM:
//                PlayerControlManager.getInstance().playAlbum(mPushResponse.getPostFlag());

                final long sid = pushResponse.getSid();
                final long id = Long.parseLong(pushResponse.getPostFlag());

                AppLogic.runOnBackGround(new Runnable() {
                    @Override
                    public void run() {
                        Album album = DBManager.getInstance().findAlbumById(id, (int) sid);
                        if (null != album) {
                            AlbumEngine.getInstance().playAlbum(pushResponse.getType(), operation, album, album.getCategoryId(), null);
                        } else {
                            AlbumRepository.getInstance().getAlbumInfoFromNet(id, (int) sid)
                                    .subscribe(new Consumer<Album>() {
                                        @Override
                                        public void accept(Album album) throws Exception {
                                            AlbumEngine.getInstance().playAlbum(pushResponse.getType(), operation, album, album.getCategoryId(), null);
                                            DBManager.getInstance().saveAlbum(album);
                                        }
                                    }, throwable -> {
                                        ToastUtils.showShortOnUI(Constant.RS_VOICE_SPEAK_NETNOTCON_TIPS);
                                    });
                        }
                    }
                });
                break;
            case PushResponse.POST_ACTION_PLAY_NEW:
                break;
            case PushResponse.POST_ACTION_PLAY_PRIVATE_MUSIC:
                break;
            case PushResponse.POST_ACTION_PLAY_MUSIC_LIST:
                List<Audio> audios = pushResponse.getArrAudio();
                if (null == audios || audios.isEmpty()) {
                    LogUtil.e(TAG, "audios is empty!");
                    return;
                }
                PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, null, 0, pushResponse.getType());
                PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.manual);
                break;
            case PushResponse.POST_ACTION_PLAY_ALBUMS:
                playAlbumWrappers(null, operation, pushResponse);
                break;
            case PushResponse.POST_ACTION_NO_PROMPT:
                if (ConfigManager.getInstance().isBootRadioEnabled()) {
                    ConfigManager.getInstance().switchBootRadio();
                }
                break;
            default:
                LogUtil.d(TAG, "can't handle post action:" + pushResponse.getPostAction());
                break;
        }
        if (isNeedJump) {
            Utils.jumpTOMediaPlayerAct(false);
        }
    }

    public void playAlbumWrappers(QuickReportPlayer player, EnumState.Operation operation, PushResponse pushResponse) {
        List<PushResponse.AlbumWrapper> arrAlbumWrappers = pushResponse.getArrAlbumWrappers();
        if (null == arrAlbumWrappers || arrAlbumWrappers.isEmpty()) {
            LogUtil.e(TAG, "arrAlbumWrappers is empty!");
            return;
        }

        if (arrAlbumWrappers.size() == 1) {
            Album album = arrAlbumWrappers.get(0).getAlbum();
            AlbumEngine.getInstance().playAlbum(pushResponse.getType(), operation, album, album.getCategoryId(), false, new RequestAudioCallBack(album) {
                @Override
                public void onResponse(List<Audio> audios, Album album, ResponseAlbumAudio responseAlbumAudio) {
                    if (player != null && player.isUserCancelBeforePlay()) {
                        return;
                    }
                    TimeUtils.endTime(Constant.SPEND_TAG + "quick:http:request:album");
                    if (null != audios && !audios.isEmpty()) {
                        PlayEngineFactory.getEngine().setAudios(operation, audios, album, 0, PlayInfoManager.DATA_ALBUM);
                        PlayEngineFactory.getEngine().play(operation);
                    } else {
                        TtsUtil.speakText(Constant.RS_VOICE_SPEAK_NOAUDIOS_TIPS);
                        AlbumEngine.getInstance().handleAlbumAudioError(Error.ERROR_CLIENT_NET_EMPTY_DATA);
                    }
                }
            });
        } else {
            Intent intent = new Intent(GlobalContext.get(), ReserveConfigSingleTaskActivity4.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            GlobalContext.get().startActivity(intent);
        }
    }


    private void handlePushShortPlay(final PullData pullData, final PushResponse pushResponse) {
        if (WinListener.isShowSoundUI) {
            LogUtil.d(TAG + "push block, record win is showing");
            return;
        }

        if (!SharedPreferencesUtils.isOpenPush()) {
            LogUtil.d(TAG + "push is closed");
            return;
        }
        // FIXME 弹出过不再弹出
        if (SharedPreferencesUtils.canNotShowPushWin()) {
            LogUtil.d(TAG + "push has been shown");
            return;
        }

        if (PushResponse.PUSH_SERVICE_SHORT_PLAY.equals(pushResponse.getService())) {
            SharedPreferencesUtils.setCanNotShowPushWin(true);
        }

        PushIntercepter.getInstance().showData(PushIntercepter.INTERCEPT_NEWS, pushResponse, new INormalCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                LogUtil.d(TAG + "push is intercept:" + aBoolean);
                if (!aBoolean) {
                    AppLogic.runOnUiGround(() -> {
                        player = new QuickReportPlayer(pullData, pushResponse);
                        player.start();
                    });
                }
            }

            @Override
            public void onError() {
                LogUtil.d(TAG + "push is intercept:onerror");
            }
        });
    }


    private void handleUpdate(final PullData pullData, final PushResponse pushResponse) {
        if (!SharedPreferencesUtils.isOpenPush()) {
            LogUtil.d(TAG + "push is closed");
            return;
        }
        PushIntercepter.getInstance().showData(PushIntercepter.INTERCEPT_UPDATE, pushResponse, new INormalCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                LogUtil.d(TAG + "push is intercept:" + aBoolean);
                if (!aBoolean) {
                    player = new QuickReportPlayer(pullData, pushResponse);
                    player.start();
                }
            }

            @Override
            public void onError() {
                LogUtil.d(TAG + "push is intercept:onerror");
            }
        });


    }


    private void handlePushAudios(final PullData pullData, final PushResponse pushResponse) {
        PushIntercepter.getInstance().showData(PushIntercepter.INTERCEPT_AUDIO, pushResponse, new INormalCallback<Boolean>() {
            @Override
            public void onSuccess(Boolean aBoolean) {
                if (!aBoolean) {
                    // TODO: 2018/8/15 声控界面再的时候，应该怎么处理，以及【开机，多个应用多条TTS同时播报】
                    //https://www.tapd.cn/21709951/bugtrace/bugs/view?bug_id=1121709951001003370
                    player = new QuickReportPlayer(pullData, pushResponse);
                    player.start();
                }
            }

            @Override
            public void onError() {

            }
        });
    }


    private void handleForcePlay(PushResponse pushResponse) {
        playDetail(EnumState.Operation.auto, pushResponse);
    }

    public void clickContinue() {
        if (player != null) {
            player.clickContinue();
        }
    }

    public void clickCancel() {
        if (player != null) {
            player.clickCancel();
        }
    }

    public void release() {
        if (player != null) {
            player.release();
        }
    }

    public void releaseDelay() {
        if (player != null) {
            player.releaseDelay();
        }
    }
}
