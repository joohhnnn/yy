package com.txznet.music.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSONObject;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.baseModule.bean.EnumState;
import com.txznet.music.baseModule.bean.PlayerInfo;
import com.txznet.music.image.ImageFactory;
import com.txznet.music.playerModule.logic.PlayHelper;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.search.SearchEngine;
import com.txznet.music.soundControlModule.logic.SoundCommand;
import com.txznet.sdk.TXZRecordWinManager;

import java.io.File;


/**
 * 通过广播对外发送信息，对第三方提供广播接口控制同听
 */
public class PlayerCommunicationManager {

    public static final String TAG = "music:sendBroadcast:";

    //播放状态改变
    public static final String ACTION_PLAY_STATUS_CHANGE = "com.txznet.music.action.PLAY_STATUS_CHANGE";
    //播放歌曲改变
    public static final String ACTION_PLAY_AUDIO = "com.txznet.music.action.PLAY_AUDIO";
    //播放进度更新
    public static final String ACTION_PLAY_PROGRESS = "com.txznet.music.Action.Progress";

    //    1缓冲（准备）,2播放,3表示暂停，4表示退出,5表示错误
    public static final int STATE_ON_IDLE = 0;
    public static final int STATE_ON_BUFFERING = 1;
    public static final int STATE_ON_PLAYING = 2;
    public static final int STATE_ON_PAUSED = 3;
    public static final int STATE_ON_EXIT = 4;
    public static final int STATE_ON_FAILED = 5;
    public static final int STATE_ON_OPEN = 6;

    public static final String EXTRA_KEY_STATUS = "status";

    public static final String EXTRA_KEY_ARTISTS = "artists";
    public static final String EXTRA_KEY_TITLE = "title";
    public static final String EXTRA_KEY_SOURCE_FROM = "source";
    public static final String EXTRA_KEY_LOGO = "logo";
    public static final String EXTRA_KEY_IS_SONG = "isSong";
    public static final String EXTRA_KEY_PROGRESS = "progress";
    public static final String EXTRA_KEY_DURATION = "duration";
    public static final String EXTRA_KEY_PERCENT = "percent";


    private static final String INTENT_PLAY_ACTION = "com.txznet.music.play";
    private static final String INTENT_PAUSE_ACTION = "com.txznet.music.pause";
    private static final String INTENT_EXIT_ACTION = "com.txznet.music.action.exit";
    private static final String INTENT_NEXT_ACTION = "com.txznet.music.next";
    private static final String INTENT_PREV_ACTION = "com.txznet.music.prev";
    private static final String INTENT_SWITCHMODELOOPALL_ACTION = "com.txznet.music.action.switchModeLoopAll";
    private static final String INTENT_SWITCHMODELOOPONE_ACTION = "com.txznet.music.action.switchModeLoopOne";
    private static final String INTENT_SWITCHMODERANDOM_ACTION = "com.txznet.music.action.switchModeRandom";
    private static final String INTENT_PLAYMUSIC_ACTION = "com.txznet.music.action.playMusic";
    private static final String INTENT_PLAYFAVOURMUSIC_ACTION = "com.txznet.music.action.playFavourMusic";
    private static final String INTENT_FAVOURMUSIC_ACTION = "com.txznet.music.action.favourMusic";
    private static final String INTENT_UNFAVOURMUSIC_ACTION = "com.txznet.music.action.unfavourMusic";
    private static final String INTENT_SEARCHMUSIC_ACTION = "com.txznet.music.search.audio";

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
        intentFilter.addAction(INTENT_PLAYMUSIC_ACTION);
        intentFilter.addAction(INTENT_PLAYFAVOURMUSIC_ACTION);
        intentFilter.addAction(INTENT_FAVOURMUSIC_ACTION);
        intentFilter.addAction(INTENT_UNFAVOURMUSIC_ACTION);
        intentFilter.addAction(INTENT_SEARCHMUSIC_ACTION);
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
                        PlayHelper.playMusic(EnumState.Operation.extra);
                    }
                } else if (TextUtils.equals(action, ACTION_QUERY)) {
                    sendCurrentPlayItemInfo();
                } else if (TextUtils.equals(action, INTENT_NEXT_ACTION)) {
                    PlayEngineFactory.getEngine().next(EnumState.Operation.extra);
                } else if (TextUtils.equals(action, INTENT_PREV_ACTION)) {
                    PlayEngineFactory.getEngine().last(EnumState.Operation.extra);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODELOOPALL_ACTION)) {
                    SoundCommand.getInstance().changeSingleMode(PlayerInfo.PLAYER_MODE_SEQUENCE);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODELOOPONE_ACTION)) {
                    SoundCommand.getInstance().changeSingleMode(PlayerInfo.PLAYER_MODE_SINGLE_CIRCLE);
                } else if (TextUtils.equals(action, INTENT_SWITCHMODERANDOM_ACTION)) {
                    SoundCommand.getInstance().changeSingleMode(PlayerInfo.PLAYER_MODE_RANDOM);
                } else if (TextUtils.equals(action, INTENT_PLAYFAVOURMUSIC_ACTION)) {
                    SoundCommand.getInstance().playfavour();
                } else if (TextUtils.equals(action, INTENT_FAVOURMUSIC_ACTION)) {
                    com.alibaba.fastjson.JSONObject jsonObject = new JSONObject();
                    jsonObject.put("favour", true);
                    SoundCommand.getInstance().favour(jsonObject.toJSONString().getBytes());
                } else if (TextUtils.equals(action, INTENT_UNFAVOURMUSIC_ACTION)) {
                    com.alibaba.fastjson.JSONObject jsonObject = new JSONObject();
                    jsonObject.put("favour", false);
                    SoundCommand.getInstance().favour(jsonObject.toJSONString().getBytes());
                } else if (TextUtils.equals(action, INTENT_SEARCHMUSIC_ACTION)) {
                    searchAudio(intent);
                } else if (TextUtils.equals(action, INTENT_EXIT_ACTION)) {
                    UIHelper.exit();
                }
            }
        }, intentFilter);
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
            LogUtil.w("Music:current playItem is null");
            return;
        }
        sendPlayItemChanged(playItem);
    }

    public interface ResultListener {
        //        public void play();
//
//        public void pause();
//
//        public void exit();
//
//        public void error();
        public void onState(Bundle bundle);

        public void onPlayInfo(Bundle bundle);

        public void onProgress(Bundle bundle);
    }

    ResultListener listener = null;

    public void setListener(ResultListener listener) {
        this.listener = listener;
    }

    @IntDef({STATE_ON_EXIT,
            STATE_ON_BUFFERING,
            STATE_ON_PLAYING,
            STATE_ON_FAILED,
            STATE_ON_PAUSED,
            STATE_ON_OPEN
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
        Log.d(TAG, "playerControlMgr : on   " + mTXZPlayerStatus);
//        LogUtil.logd(TAG + "playerControlMgr : on   " + mTXZPlayerStatus);

        bundle.putInt(PlayerCommunicationManager.EXTRA_KEY_STATUS, mTXZPlayerStatus);
        sendBroadcast(PlayerCommunicationManager.ACTION_PLAY_STATUS_CHANGE, bundle);
    }


    public void sendProgressChanged(int progress, int duration) {
        Bundle bundle = new Bundle();
        bundle.putLong(EXTRA_KEY_PROGRESS, progress);
        bundle.putLong(EXTRA_KEY_DURATION, duration);
        bundle.putFloat(EXTRA_KEY_PERCENT, progress / duration);
        sendBroadcast(PlayerCommunicationManager.ACTION_PLAY_PROGRESS, bundle);
    }


    public void sendPlayItemChanged(Audio playItem) {
        LogUtil.logd(TAG + "sendPlayItemChanged:" + (playItem == null ? "null" : playItem.getName()));
        if (playItem == null) {
            LogUtil.w("Music:current playItem is null");
            return;
        }
        Bundle bundle = new Bundle();
        boolean isSong = Utils.isSong(playItem.getSid());
        if (isSong && com.txznet.comm.util.CollectionUtils.isNotEmpty(playItem.getArrArtistName())) {
            bundle.putString(EXTRA_KEY_ARTISTS, playItem.getArrArtistName().get(0));
        } else {
            bundle.putString(EXTRA_KEY_ARTISTS, playItem.getAlbumName());
        }
        bundle.putString(EXTRA_KEY_TITLE, playItem.getName());
        bundle.putString(EXTRA_KEY_SOURCE_FROM, playItem.getSourceFrom());
        bundle.putBoolean(EXTRA_KEY_IS_SONG, isSong);
        File diskCache = ImageFactory.getInstance().getDiskImageFile(playItem.getLogo());
        String logo = "";
        if (diskCache != null) {
            logo = diskCache.getAbsolutePath();
        }
        if (TextUtils.isEmpty(logo)) {
            logo = playItem.getLogo();
        }
        bundle.putString(EXTRA_KEY_LOGO, logo);
        bundle.putInt(EXTRA_KEY_STATUS, mTXZPlayerStatus);

        sendBroadcast(ACTION_PLAY_AUDIO, bundle);
    }

    private void sendBroadcast(String action, Bundle bundle) {
//        Intent intent = new Intent();
//        intent.setAction(action);
//        intent.putExtras(bundle);
//        GlobalContext.get().sendBroadcast(intent);

        if (listener != null) {
            if (action.equals(ACTION_PLAY_STATUS_CHANGE)) {
                listener.onState(bundle);
            } else if (action.equals(ACTION_PLAY_AUDIO)) {
                listener.onPlayInfo(bundle);
            } else if (action.equals(ACTION_PLAY_PROGRESS)) {
                listener.onProgress(bundle);
            }
        }
    }

}
