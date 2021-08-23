package com.txznet.music.push;

import android.content.Intent;

import com.google.protobuf.nano.InvalidProtocolBufferNanoException;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.logic.AlbumEngine;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.Error;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.net.NetManager;
import com.txznet.music.net.RequestCallBack;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.QuickReportPlayer;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.push.bean.Location;
import com.txznet.music.push.bean.PullData;
import com.txznet.music.push.bean.PushResponse;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.bean.PushEvent;
import com.txznet.music.service.MusicInteractionWithCore;
import com.txznet.music.utils.JsonHelper;
import com.txznet.music.utils.SharedPreferencesUtils;
import com.txznet.music.utils.ToastUtils;
import com.txznet.music.utils.TtsUtilWrapper;
import com.txznet.music.utils.Utils;
import com.txznet.reserve.activity.ReserveConfigSingleTaskActivity4;
import com.txznet.sdk.TXZLocationManager;
import com.txznet.sdk.bean.LocationData;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by ASUS User on 2017/1/3.
 */

public class PushManager {


    private static final String TAG = "Music:PushManager:";
    private static PushManager sInstance;

    private PushManager() {
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
        final String strData = new String(pushAudio.strData);
        String cmd = new String(pushAudio.strCmd);
        LogUtil.d(TAG, "data:" + strData);
        LogUtil.d(TAG, "cmd:" + cmd);

        switch (Integer.valueOf(cmd)) {
            case com.txz.push_manager.PushManager.SHORTPLAY:

                break;
            case com.txz.push_manager.PushManager.PULL:
                PullData pullData = JsonHelper.toObject(PullData.class, strData);
                LogUtil.d(TAG, pullData.toString());

                ReportEvent.reportPushEvent(PushEvent.ACTION_PUSH_ARRIVE, PushEvent.getType(pullData), pullData.getId());

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
            default:

                break;
        }
    }


    private void handleShortPlayPullMsg(PullData pullData) {
        if (SharedPreferencesUtils.getIsShortPlayNeedTrigger()) {
            LogUtil.d(TAG + "shortPlay need trigger");
            return;
        }
        refreshGps(pullData);
    }


    private void handleUpdatePullMsg(final PullData pullData) {
        NetManager.getInstance().requestPushData(pullData.getService(), null, new RequestCallBack<PushResponse>(PushResponse.class) {
            @Override
            public void onResponse(PushResponse data) {
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
                LogUtil.d(TAG, "data:" + data.toString());
                handlePushAudios(pullData, data);
            }

            @Override
            public void onError(String cmd, Error error) {
                LogUtil.e(TAG, error.toString());
            }
        });
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

    public void playDetail(final EnumState.Operation operation, final PushResponse pushResponse) {
        if (pushResponse == null) {
            return;
        }

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
                            AlbumEngine.getInstance().playAlbum(operation, album, album.getCategoryId(), null);
                        } else {
                            album = new Album();
                            album.setSid((int) pushResponse.getSid());
                            album.setId(Long.parseLong(pushResponse.getPostFlag()));
                            album.setName(pushResponse.getAlbumName());
                            AlbumEngine.getInstance().playAlbum(operation, album, 0, null);
                            DBManager.getInstance().saveAlbum(album);
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

                PlayEngineFactory.getEngine().setAudios(EnumState.Operation.manual, audios, null, 0, PlayInfoManager.DATA_PUSH);
                PlayEngineFactory.getEngine().playOrPause(EnumState.Operation.manual);
                break;

            case PushResponse.POST_ACTION_PLAY_ALBUMS:
                List<PushResponse.AlbumWrapper> arrAlbumWrappers = pushResponse.getArrAlbumWrappers();
                if (null == arrAlbumWrappers || arrAlbumWrappers.isEmpty()) {
                    LogUtil.e(TAG, "arrAlbumWrappers is empty!");
                    return;
                }

                if (arrAlbumWrappers.size() == 1) {
                    Album album = arrAlbumWrappers.get(0).getAlbum();
                    AlbumEngine.getInstance().playAlbum(operation, album, album.getCategoryId(), null);
                } else {
                    Intent intent = new Intent(GlobalContext.get(), ReserveConfigSingleTaskActivity4.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    GlobalContext.get().startActivity(intent);
                }

                break;
            default:
                LogUtil.d(TAG, "can't handle post action:" + pushResponse.getPostAction());
                break;
        }

        Utils.jumpTOMediaPlayerAct(false);
    }


    private void handlePushShortPlay(final PullData pullData, final PushResponse pushResponse) {
        if (!SharedPreferencesUtils.isOpenPush()) {
            LogUtil.d(TAG + "push is closed");
            return;
        }

        TtsUtilWrapper.speakText("", new TtsUtil.ITtsCallback() {
            @Override
            public void onEnd() {
                super.onEnd();
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        final QuickReportPlayer quickReportPlayer = new QuickReportPlayer(pullData, pushResponse);
                        quickReportPlayer.start();
                    }
                });
            }
        });
    }


    private void handleUpdate(PullData pullData, PushResponse pushResponse) {
        if (!SharedPreferencesUtils.isOpenPush()) {
            LogUtil.d(TAG + "push is closed");
            return;
        }

        new QuickReportPlayer(pullData, pushResponse).start();
    }


    private void handlePushAudios(PullData pullData, PushResponse pushResponse) {
        new QuickReportPlayer(pullData, pushResponse).start();
    }


    private void handleForcePlay(PushResponse pushResponse) {
        playDetail(EnumState.Operation.auto, pushResponse);
    }
}
