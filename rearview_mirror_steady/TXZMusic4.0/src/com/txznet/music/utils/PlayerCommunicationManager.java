package com.txznet.music.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.text.TextUtils;

import com.alibaba.fastjson.JSONObject;
import com.txznet.audio.bean.LocalBuffer;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.baseModule.dao.DBManager;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.playerModule.logic.PlayerInfoUpdateListener;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.logic.SoundCommand;
import com.txznet.sdk.TXZRecordWinManager;
import com.txznet.sdk.tongting.TongTingUtils;

import java.io.File;
import java.util.List;

import io.reactivex.schedulers.Schedulers;


/**
 * 通过广播对外发送信息，对第三方提供广播接口控制同听
 */
public class PlayerCommunicationManager {

    public static final String TAG = "music:sendBroadcast:";

    //播放状态改变
    public static final String ACTION_PLAY_STATUS_CHANGE = "com.txznet.music.action.PLAY_STATUS_CHANGE";
    public static final String ACTION_PLAY_UI_STATUS_CHANGE = "com.txznet.music.action.PLAY_UI_STATUS_CHANGE";
    //播放歌曲改变
    public static final String ACTION_PLAY_AUDIO = "com.txznet.music.action.PLAY_AUDIO";
    //播放进度更新
    public static final String ACTION_PLAY_PROGRESS = "com.txznet.music.Action.Progress";
    //音乐相关事件
    public static final String ACTION_MUSIC_EVENT = "com.txznet.music.Action.MUSIC_EVENT";

    public static final String ACTION_LOCAL_SCAN_FINISH = "com.txznet.music.action.SCAN_FINISH";


    //    1缓冲（准备）,2播放,3表示暂停，4表示退出,5表示错误
    public static final int STATE_ON_IDLE = 0;
    public static final int STATE_ON_BUFFERING = 1;
    public static final int STATE_ON_PLAYING = 2;
    public static final int STATE_ON_PAUSED = 3;
    public static final int STATE_ON_EXIT = 4;
    public static final int STATE_ON_FAILED = 5;

    public static final String EXTRA_KEY_STATUS = "status";
    public static final String EXTRA_KEY_SCENE = "scene";//区分播放场景
    public static final String EXTRA_KEY_AUDIO_FLAG = "audioFlag";
    public static final String EXTRA_KEY_ALBUM_FLAG = "albumFlag";
    public static final String EXTRA_KEY_ALBUM_ID_SID = "albumIDSID";
    public static final String EXTRA_KEY_AUDIO_ID_SID = "audioIDSID";
    public static final String EXTRA_KEY_AUDIO_MODE = "audioMode";//播放模式
    public static final String EXTRA_KEY_AUDIO_MODE_SUPPORT = "supportAudioMode";//播放模式
    public static final String EXTRA_KEY_FAVOUR = "favour";//播放模式
    public static final String EXTRA_KEY_ARTISTS = "artists";
    public static final String EXTRA_KEY_TITLE = "title";
    public static final String EXTRA_KEY_SOURCE_FROM = "source";
    public static final String EXTRA_KEY_LOGO = "logo";
    public static final String EXTRA_KEY_PROGRESS = "progress";
    public static final String EXTRA_KEY_DURATION = "duration";
    public static final String EXTRA_KEY_PERCENT = "percent";
    private static final String INTENT_PLAY_ACTION = "com.txznet.music.play";
    private static final String INTENT_PAUSE_ACTION = "com.txznet.music.pause";
    private static final String INTENT_EXIT_ACTION = "com.txznet.music.action.exit";
    private static final String INTENT_NEXT_ACTION = "com.txznet.music.next";
    private static final String INTENT_PREV_ACTION = "com.txznet.music.prev";

    private static final String INTENT_SWITCHMODELOOPALL_ACTION = "com.txznet.music.action.switchModeLoopAll";
    private static final String INTENT_SWITCHMODE = "com.txznet.music.action.switchMode";
    private static final String INTENT_SWITCHMODELOOPONE_ACTION = "com.txznet.music.action.switchModeLoopOne";
    private static final String INTENT_SWITCHMODERANDOM_ACTION = "com.txznet.music.action.switchModeRandom";
    private static final String INTENT_PLAYMUSIC_ACTION = "com.txznet.music.action.playMusic";
    private static final String INTENT_PLAYFAVOURMUSIC_ACTION = "com.txznet.music.action.playFavourMusic";
    private static final String INTENT_FAVOURMUSIC_ACTION = "com.txznet.music.action.favourMusic";
    private static final String INTENT_UNFAVOURMUSIC_ACTION = "com.txznet.music.action.unfavourMusic";
    private static final String INTENT_SEARCHMUSIC_ACTION = "com.txznet.music.search.audio";
    private static final String INTENT_LOCAL_FIRST_ACTION = "com.txznet.music.getFirstLocalAudio";//获取本地第一首歌曲的信息
    private static final String INTENT_PLAY_LOCAL_AUDIO_ACTION = "com.txznet.music.playLocalAudio";//播放本地音乐
    private static final String INTENT_OPEN_LOCAL_APP_ACTION = "com.txznet.music.openApp.local";//打开本地音乐

    private static final String ACTION_QUERY = "com.txznet.music.query";

    private int mTXZPlayerStatus = 0;

    private static PlayerCommunicationManager sInstance;

    private PlayerCommunicationManager() {
        init();
    }

    public static PlayerCommunicationManager getInstance() {
        if (sInstance == null) {
            synchronized (PlayerCommunicationManager.class) {
                if (sInstance == null) {
                    sInstance = new PlayerCommunicationManager();
                }
            }
        }
        return sInstance;
    }


    private void init() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_QUERY);
        intentFilter.addAction(INTENT_PLAY_ACTION);
        intentFilter.addAction(INTENT_PAUSE_ACTION);
        intentFilter.addAction(INTENT_EXIT_ACTION);
        intentFilter.addAction(INTENT_NEXT_ACTION);
        intentFilter.addAction(INTENT_PREV_ACTION);
        intentFilter.addAction(INTENT_SWITCHMODELOOPALL_ACTION);
        intentFilter.addAction(INTENT_SWITCHMODELOOPONE_ACTION);
        intentFilter.addAction(INTENT_SWITCHMODERANDOM_ACTION);
        intentFilter.addAction(INTENT_SWITCHMODE);
        intentFilter.addAction(INTENT_PLAYMUSIC_ACTION);
        intentFilter.addAction(INTENT_PLAYFAVOURMUSIC_ACTION);
        intentFilter.addAction(INTENT_FAVOURMUSIC_ACTION);
        intentFilter.addAction(INTENT_UNFAVOURMUSIC_ACTION);
        intentFilter.addAction(INTENT_SEARCHMUSIC_ACTION);
        intentFilter.addAction(INTENT_LOCAL_FIRST_ACTION);
        intentFilter.addAction(INTENT_PLAY_LOCAL_AUDIO_ACTION);
        intentFilter.addAction(INTENT_OPEN_LOCAL_APP_ACTION);
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                LogUtil.d("Music:receive player control broadcast, action:" + action);
                if (TextUtils.equals(action, INTENT_PAUSE_ACTION)) {
                    PlayEngineFactory.getEngine().pause(EnumState.Operation.extra);
                } else if (TextUtils.equals(action, INTENT_PLAY_ACTION)) {

                    if (PlayEngineFactory.getEngine().getCurrentAudio() != null) {
                        if (!PlayEngineFactory.getEngine().isPlaying()) {
                            PlayEngineFactory.getEngine().play(EnumState.Operation.extra);     //此处单调用play()可能会造成没有触发过startnewplay()造成无法播放的情况
                        }
                    } else {
                        PlayHelper.playMusic(PlayInfoManager.DATA_ALBUM, EnumState.Operation.extra);
                    }
                } else if (TextUtils.equals(action, ACTION_QUERY)) {
                    sendCurrentPlayItemInfo();
                } else if (TextUtils.equals(action, INTENT_NEXT_ACTION)) {
                    PlayEngineFactory.getEngine().next(EnumState.Operation.extra);
                } else if (TextUtils.equals(action, INTENT_PREV_ACTION)) {
                    PlayEngineFactory.getEngine().last(EnumState.Operation.extra);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODELOOPALL_ACTION)) {
                    SoundCommand.getInstance().changeSingleMode(PlayerInfo.PLAYER_MODE_SEQUENCE);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODE)) {
                    SoundCommand.getInstance().changeMode(EnumState.Operation.extra);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODELOOPONE_ACTION)) {
                    SoundCommand.getInstance().changeSingleMode(PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODERANDOM_ACTION)) {
                    SoundCommand.getInstance().changeSingleMode(PlayerInfo.PLAYER_MODE_RANDOM);
                } else if (TextUtils.equals(action, INTENT_PLAYFAVOURMUSIC_ACTION)) {
                    SoundCommand.getInstance().playfavour();
                } else if (TextUtils.equals(action, INTENT_FAVOURMUSIC_ACTION)) {
                    com.alibaba.fastjson.JSONObject jsonObject = new JSONObject();
                    jsonObject.put("favour", "true");
                    SoundCommand.getInstance().favour(jsonObject.toJSONString().getBytes());
                } else if (TextUtils.equals(action, INTENT_UNFAVOURMUSIC_ACTION)) {
                    com.alibaba.fastjson.JSONObject jsonObject = new JSONObject();
                    jsonObject.put("favour", "false");
                    SoundCommand.getInstance().favour(jsonObject.toJSONString().getBytes());
                } else if (TextUtils.equals(action, INTENT_SEARCHMUSIC_ACTION)) {
                    searchAudio(intent);
                } else if (TextUtils.equals(action, INTENT_EXIT_ACTION)) {
                    UIHelper.exit();
                } else if (TextUtils.equals(action, INTENT_PLAY_LOCAL_AUDIO_ACTION)) {
                    SoundCommand.getInstance().playLocalAudios();
                } else if (TextUtils.equals(action, INTENT_OPEN_LOCAL_APP_ACTION)) {
                    SoundCommand.getInstance().openLocal();
                } else if (TextUtils.equals(action, INTENT_LOCAL_FIRST_ACTION)) {
                    Audio firstLocalAudio = getFirstLocalAudio();
                    sendPlayItemChanged(firstLocalAudio);
//                    if (firstLocalAudio != null) {
//                        sendPlayItemChanged(firstLocalAudio);
//                    }
                }
            }
        }, intentFilter);


        PlayInfoManager.getInstance().addPlayerInfoUpdateListener(new PlayerInfoUpdateListener() {
            @Override
            public void onPlayInfoUpdated(Audio audio, Album album) {

            }

            @Override
            public void onProgressUpdated(long position, long duration) {

            }

            @Override
            public void onPlayerModeUpdated(int mode) {
                //发送广播
                sendPlayModeChange(mode);
            }

            @Override
            public void onPlayerStatusUpdated(int status) {

            }

            @Override
            public void onBufferProgressUpdated(List<LocalBuffer> buffers) {

            }

            @Override
            public void onFavourStatusUpdated(int favour) {
                if (TongTingUtils.isFavour(favour) || TongTingUtils.isSubscribe(favour)) {
                    sendFavourStatusChange(true);
                } else {
                    sendFavourStatusChange(false);
                }
            }
        });
    }

    /**
     * 获取本地的第一首歌曲
     *
     * @return
     */
    public Audio getFirstLocalAudio() {
        List<Audio> allLocalAudios = DBManager.getInstance().findAllLocalAudios();
        if (allLocalAudios.size() > 0) {
            return allLocalAudios.get(0);
        }
        return null;
    }

    public void searchAudio(Intent intent) {
        String content = intent.getExtras().getString("content");
        if (TextUtils.isEmpty(content)) {
            LogUtil.logw(TAG + "传入的参数有问题：应该传入key为content的json串");
            return;
        }
        TXZRecordWinManager.getInstance().openAndShowText(Constant.RS_VOICE_SPEAK_SEARCHDATA_TIPS);

        SearchEngine.getInstance().doSoundFind(-2, content);
    }


    private void sendCurrentPlayItemInfo() {
        Audio playItem = PlayEngineFactory.getEngine().getCurrentAudio();
        if (playItem == null) {
            Schedulers.io().scheduleDirect(() -> {
                HistoryData newestHistory = DBManager.getInstance().findNewestHistory();
                if (newestHistory == null || newestHistory.getAudio() == null) {
                    LogUtil.w("Music:current playItem is null");
                    sendEmptyStatesChange();
                } else {
                    Audio item = newestHistory.getAudio();
                    if (item.getLogo() == null) {
                        item.setLogo(newestHistory.getAlbum() == null ? null : newestHistory.getAlbum().getLogo());
                    }
                    LogUtil.w("Music:current playItem is " + item);
                    sendPlayItemChanged(item);
                }
            });
            return;
        }
        LogUtil.w("Music:current playItem is " + playItem);
        sendPlayItemChanged(playItem);
    }

    @IntDef({STATE_ON_EXIT,
            STATE_ON_BUFFERING,
            STATE_ON_PLAYING,
            STATE_ON_FAILED,
            STATE_ON_PAUSED
    })
    public @interface SEND_STATE {
    }

    public int getCurrentPlayerStatus() {
        return mTXZPlayerStatus;
    }

    /**
     * 1缓冲（准备）,2播放,3表示暂停，4表示退出,5表示错误
     *
     * @param status
     */
    public void sendPlayStatusChanged(@SEND_STATE int status) {
        mTXZPlayerStatus = status;
        Bundle bundle = new Bundle();
        Logger.d(TAG, "playerControlMgr : on   " + mTXZPlayerStatus);

        bundle.putInt(PlayerCommunicationManager.EXTRA_KEY_STATUS, mTXZPlayerStatus);
        sendBroadcast(PlayerCommunicationManager.ACTION_PLAY_STATUS_CHANGE, bundle);
    }

    /**
     *    public static final int PLAYER_UI_STATUS_RELEASE = 0;
     *     public static final int PLAYER_UI_STATUS_BUFFER = 1;
     *     public static final int PLAYER_UI_STATUS_PLAYING = 2;
     *     public static final int PLAYER_UI_STATUS_PAUSE = 3;
     * {PLAYER_UI_STATUS_RELEASE, PLAYER_UI_STATUS_BUFFER, PLAYER_UI_STATUS_PLAYING, PLAYER_UI_STATUS_PAUSE}
     */
    public void sendPlayerUIStatus(@PlayerInfo.PlayerUIStatus int status) {
        Bundle bundle = new Bundle();
        Logger.d(TAG, "sendPlayerUIStatus : on   " + status);

        bundle.putInt(PlayerCommunicationManager.EXTRA_KEY_STATUS, status);
        sendBroadcast(PlayerCommunicationManager.ACTION_PLAY_UI_STATUS_CHANGE, bundle);
    }


    public void sendProgressChanged(int progress, int duration) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_KEY_PROGRESS, progress);
        bundle.putLong(EXTRA_KEY_DURATION, duration);
        bundle.putFloat(EXTRA_KEY_PERCENT, progress / duration);
        sendBroadcast(PlayerCommunicationManager.ACTION_PLAY_PROGRESS, bundle);
    }

    public void sendScanFinish() {
        sendBroadcast(PlayerCommunicationManager.ACTION_LOCAL_SCAN_FINISH, null);
    }

    public void sendPlayItemChanged(Audio playItem) {
        LogUtil.logd(TAG + "sendPlayItemChanged:" + (playItem == null ? "null" : playItem.getName()));
        if (playItem == null) {
            LogUtil.w("Music:current playItem is null");
            sendEmptyStatesChange();
            return;
        }
        Bundle bundle = new Bundle();
        if (Utils.isSong(playItem.getSid()) && com.txznet.comm.util.CollectionUtils.isNotEmpty(playItem.getArrArtistName())) {
            bundle.putString(EXTRA_KEY_ARTISTS, playItem.getArrArtistName().get(0));
        } else {
            bundle.putString(EXTRA_KEY_ARTISTS, playItem.getAlbumName());
        }
        bundle.putString(EXTRA_KEY_TITLE, playItem.getName());
        bundle.putString(EXTRA_KEY_SOURCE_FROM, playItem.getSourceFrom());
        File diskCache = ImageFactory.getInstance().getDiskImageFile(playItem.getLogo());
        String logo = "";
        if (diskCache != null) {
            logo = diskCache.getAbsolutePath();
        }
        if (TextUtils.isEmpty(logo)) {
            logo = playItem.getLogo();
        }
        Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
        if (TextUtils.isEmpty(logo) && currentAlbum != null) {
            logo = currentAlbum.getLogo();
        }
        bundle.putString(EXTRA_KEY_LOGO, logo);
        bundle.putInt(EXTRA_KEY_STATUS, mTXZPlayerStatus);
        bundle.putInt(EXTRA_KEY_SCENE, PlayInfoManager.getInstance().getCurrentScene());
        bundle.putString(EXTRA_KEY_AUDIO_ID_SID, playItem.getAudioDbId());
        bundle.putBoolean(EXTRA_KEY_AUDIO_MODE_SUPPORT, Utils.isSong(playItem.getSid()));
        if (Utils.isSong(playItem.getSid())) {
            //【【易图launcher】点击首页音乐按钮，播放电台的情况下，音乐播放设置点击播放模式按钮，点击无响应】
            //https://www.tapd.cn/21865291/bugtrace/bugs/view/1121865291001008787
            //修改的方式为:只有音乐的情况才会传递该字段.
            bundle.putInt(EXTRA_KEY_AUDIO_MODE, PlayInfoManager.getInstance().getCurrentPlayMode());
            bundle.putInt(EXTRA_KEY_AUDIO_FLAG, playItem.getFlag());
        } else {
            if (currentAlbum != null) {
                bundle.putInt(EXTRA_KEY_ALBUM_FLAG, currentAlbum.getFlag());
                bundle.putString(EXTRA_KEY_ALBUM_ID_SID, currentAlbum.getAlbumDbId());
            }
        }


        sendBroadcast(ACTION_PLAY_AUDIO, bundle);
    }


    public void sendEmptyStatesChange() {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_KEY_ARTISTS, "");
        bundle.putString(EXTRA_KEY_TITLE, "");
        bundle.putString(EXTRA_KEY_SOURCE_FROM, "");
        bundle.putString(EXTRA_KEY_LOGO, "");
        bundle.putInt(EXTRA_KEY_STATUS, 0);
        bundle.putInt(EXTRA_KEY_SCENE, 0);
        bundle.putString(EXTRA_KEY_AUDIO_ID_SID, "");
        bundle.putBoolean(EXTRA_KEY_AUDIO_MODE_SUPPORT, false);
        bundle.putInt(EXTRA_KEY_AUDIO_MODE, 0);
        bundle.putInt(EXTRA_KEY_AUDIO_FLAG, 0);
        sendBroadcast(ACTION_PLAY_AUDIO, bundle);
    }

    /**
     * 发送收藏的事件
     *
     * @param isFavour
     */
    public void sendFavourStatusChange(boolean isFavour) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(EXTRA_KEY_FAVOUR, isFavour);
        sendBroadcastWithAction("playFavourStatus", bundle);
    }

    public void sendPlayModeChange(int mode) {
        Bundle bundle = new Bundle();
        bundle.putInt(EXTRA_KEY_AUDIO_MODE, mode);
        sendBroadcastWithAction("playModeChange", bundle);
    }

    private void sendBroadcastWithAction(String action, Bundle bundle) {
        if (bundle == null) {
            bundle = new Bundle();
        }
        bundle.putString("action", action);

        sendBroadcast(ACTION_MUSIC_EVENT, bundle);

    }

    private void sendBroadcast(String action, Bundle bundle) {
        Intent intent = new Intent();
        intent.setAction(action);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        Logger.d(TAG, "sendBroadcast action=" + action);
        GlobalContext.get().sendBroadcast(intent);

    }

}
