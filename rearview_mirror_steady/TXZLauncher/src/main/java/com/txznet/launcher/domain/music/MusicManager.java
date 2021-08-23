package com.txznet.launcher.domain.music;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.ServiceManager;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.launcher.component.music.MusicComponent;
import com.txznet.launcher.domain.BaseManager;
import com.txznet.launcher.domain.music.bean.DataWrapper;
import com.txznet.launcher.domain.music.bean.PlayInfo;
import com.txznet.launcher.domain.music.bean.ResponseSearch;
import com.txznet.launcher.event.BroadcastingCentre;
import com.txznet.launcher.event.EventTypes;
import com.txznet.launcher.utils.Conditions;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZMusicManager;
import com.txznet.sdk.TXZResourceManager;
import com.txznet.sdkinner.TXZServiceCommandDispatcher;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by brainBear on 2018/3/1.
 * 音乐业务类。
 * 接受音乐相关的数据，然后分发给不同的类
 */

public class MusicManager extends BaseManager {


    private static final String TAG = "MusicManager:";
    private static final String MUSIC_PACKAGE_NAME = "com.txznet.music";
    private static MusicManager sInstance;
    private List<IMusicInfoChangedListener> mListeners = new CopyOnWriteArrayList<>();

    private PlayInfo mCurrentPlayInfo = null;

    @PlayInfo.PlayState
    private int mCurrentPlayState = PlayInfo.PLAY_STATE_PAUSE;


    private MusicManager() {
    }

    public static MusicManager getInstance() {
        if (null == sInstance) {
            synchronized (MusicComponent.class) {
                if (null == sInstance) {
                    sInstance = new MusicManager();
                }
            }
        }
        return sInstance;
    }

    public PlayInfo getCurrentPlayInfo() {
        return mCurrentPlayInfo;
    }

    @PlayInfo.PlayState
    public int getCurrentPlayState() {
        return mCurrentPlayState;
    }


    @Override
    public void init() {
        super.init();
//        setMusicTool();


        // 拦截sendInvoke中所有以data.music开头的command。
        TXZServiceCommandDispatcher.setCommandProcessor("data.music.", new TXZServiceCommandDispatcher.CommandProcessor() {
            @Override
            public byte[] process(String packageName, String command, byte[] data) {
                return handleMusicInvoke(packageName, command, data);
            }
        });

        // 通知音乐那边发送音乐数据出来
        // 这个设置可能会因为music的app没启动而没有执行到。
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.setListener", null, null);


        IntentFilter intentFilter = new IntentFilter();
        // AppLogicBase在onCreate里会发送packageName.onCreateApp的广播，说明该app启动了。所以这里多设置一次music.setListener是为了在music app启动或重启时设置listener
        intentFilter.addAction(MUSIC_PACKAGE_NAME + ".onCreateApp");
        GlobalContext.get().registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                LogUtil.d(TAG, "on receive");
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_EXIT);
                ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.setListener", null, null);
            }
        }, intentFilter);


        // 同听免唤醒
//        TXZMusicManager.getInstance().setWakeupDefaultValue(true);
//        TXZMusicManager.getInstance().setWakeupValue(true);

        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_MUSIC_WAKEUP_PLAY_NEXT", "即将播放下一首");
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_MUSIC_WAKEUP_PLAY_PREV", "即将播放上一首");
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_MUSIC_WAKEUP_PLAY", "即将继续播放");
        TXZResourceManager.getInstance().setTextResourceString("RS_VOICE_MUSIC_WAKEUP_PAUSE", "即将暂停播放");
    }

    /**
     * 同步现在的播放信息
     */
    public void syncCurPlayInfo() {
        GlobalContext.get().sendBroadcast(new Intent("com.txznet.music.query"));
        AppLogic.runOnUiGround(new Runnable() {
            @Override
            public void run() {
                if (isPlaying()) {
                    BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_PLAYING);
                    notifyPlayStateChanged(PlayInfo.PLAY_STATE_PLAYING);
                }
            }
        });
    }

    @Override
    public String[] getObserverEventTypes() {
        return new String[]{
                EventTypes.EVENT_TXZ_INIT_SUCCESS,
                EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP,
                EventTypes.EVENT_DEVICE_POWER_WAKEUP
        };
    }

    @Override
    protected void onEvent(String eventType) {
        super.onEvent(eventType);
        switch (eventType) {
            case EventTypes.EVENT_TXZ_INIT_SUCCESS:
                syncCurPlayInfo(); //  同步歌曲 状态，会触发播放器那边发playInfo过来
                break;
            case EventTypes.EVENT_DEVICE_POWER_BEFORE_SLEEP:
                exit();
                break;
            case EventTypes.EVENT_DEVICE_POWER_WAKEUP:
                ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.setListener", null, null);
                break;
        }
    }

    private boolean isMusicWillBePlay;

    public void setMusicWillBePlay(boolean willBePlay) {
        this.isMusicWillBePlay = willBePlay;
    }

    // 当前播放的是否是歌曲
    public boolean isMusicWillBePlay() {
        return isMusicWillBePlay;
    }

    // 通知播放器下一首的封装，一般在语音识别的回调中被调用。
    public void playNext() {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.next", null, null);
    }

    public void playPrevious() {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.prev", null, null);
    }

    public void play() {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.play", null, null);
    }

    public void pause() {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.pause", null, null);
    }

    public void exit() {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.exit", null, null);
    }

    public boolean isPlaying() {
        ServiceManager.ServiceData serviceData = ServiceManager.getInstance().sendInvokeSync(MUSIC_PACKAGE_NAME, "music.isPlaying", null);
        return serviceData == null ? false : serviceData.getBoolean();
    }

    public void search(TXZMusicManager.MusicModel musicModel) {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.search", new Gson().toJson(musicModel).getBytes(), null);
    }

    public void cancelSearch() {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.search.cancel", null, null);
    }

    public void choice(int index) {
        ServiceManager.getInstance().sendInvoke(MUSIC_PACKAGE_NAME, "music.choice", String.valueOf(index).getBytes(), null);
    }

    /**
     * 接受playinfo并做处理
     */
    public byte[] handleMusicInvoke(String packageName, final String command, byte[] data) {
        LogUtil.d(TAG, "package:" + packageName + " command:" + command);

        if (TextUtils.equals(command, "search")) {
            String s = new String(data);
            DataWrapper<ResponseSearch> responseSearch = new Gson().fromJson

                    (s, new TypeToken<DataWrapper<ResponseSearch>>() {
                    }.getType());
            LogUtil.d(TAG, responseSearch);

            if (responseSearch.getData().getArrAlbum() != null && !responseSearch.getData().getArrAlbum().isEmpty()) {
                choice(0);
            }

            if (responseSearch.getData().getArrAudio() != null && !responseSearch.getData().getArrAudio().isEmpty()) {
                choice(0);
            }

        } else if (TextUtils.equals(command, "state")) {
            String s = new String(data);
            final PlayInfo playInfo = new Gson().fromJson(s, PlayInfo.class);
            LogUtil.d(TAG, playInfo);


            if (playInfo.getStatus() == PlayInfo.PLAY_STATE_OPEN) {
                TXZMusicManager.getInstance().pause();
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_OPEN);
            } else if (playInfo.getStatus() == PlayInfo.STATE_ON_PLAYING) {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_PLAYING);
            } else if (playInfo.getStatus() == PlayInfo.STATE_ON_EXIT) {
                AppLogic.runOnUiGround(new Runnable() {
                    @Override
                    public void run() {
                        BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_EXIT);
                        // tapd-1005228
                        // 当音乐退出或出错的时候，清除PlayInfo的缓存。不然下次打开音乐界面的时候，会显示上一次的图片和名字等。
                        mCurrentPlayInfo = null;
                    }
                }, 1000);
            } else if (playInfo.getStatus() == PlayInfo.STATE_ON_FAILED) {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_FAIL);
            } else {
                BroadcastingCentre.getInstance().notifyEvent(EventTypes.EVENT_MUSIC_PAUSE);
            }


            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    notifyPlayStateChanged(convertState(playInfo.getStatus()));
                }
            });

        } else if (TextUtils.equals(command, "playInfo")) {
            String s = new String(data);
            final PlayInfo playInfo = new Gson().fromJson(s, PlayInfo.class);
            LogUtil.d(TAG, playInfo);

            LogUtil.logd("isPlaying = " + isPlaying());
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    notifyPlayInfoChanged(playInfo);
                }
            });


        } else if (TextUtils.equals(command, "progress")) {
            String s = new String(data);
            final PlayInfo playInfo = new Gson().fromJson(s, PlayInfo.class);
            LogUtil.d(TAG, playInfo);

            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {

                    notifyPlayProgressChanged(playInfo.getProgress(), playInfo.getDuration());
                }
            });

        }

        return null;
    }


    /**
     * 为什么要将这个的PlayInfo.STATE_ON_PLAYING改成PlayInfo.PLAY_STATE_PLAYING
     */
    @PlayInfo.PlayState
    private int convertState(int state) {
        if (state == PlayInfo.STATE_ON_PLAYING) {
            return PlayInfo.PLAY_STATE_PLAYING;
        } else if (state == PlayInfo.STATE_ON_FAILED) {
            return PlayInfo.PLAY_STATE_FAILED;
        } else if (state == PlayInfo.PLAY_STATE_OPEN) {
            return PlayInfo.PLAY_STATE_OPEN;
        } else {
            return PlayInfo.PLAY_STATE_PAUSE;
        }
    }

    // 通知listenerPlayInfo变了，其实就是通知MusicPresenter数据变了。
    private void notifyPlayInfoChanged(PlayInfo playInfo) {
        Conditions.assertMainThread("notifyPlayInfoChanged");

        mCurrentPlayInfo = playInfo;

        for (IMusicInfoChangedListener listener : mListeners) {
            listener.onPlayInfoChanged(playInfo);
        }
    }

    private void notifyPlayStateChanged(@PlayInfo.PlayState int state) {
        Conditions.assertMainThread("notifyPlayStateChanged");

        mCurrentPlayState = state;

        for (IMusicInfoChangedListener listener : mListeners) {
            listener.onPlayStateChanged(state);
        }
    }

    private void notifyPlayProgressChanged(long progress, long duration) {
        Conditions.assertMainThread("notifyPlayProgressChanged");

        if (progress > 0 && PlayInfo.PLAY_STATE_PLAYING != mCurrentPlayState) {
            return;
        }

        for (IMusicInfoChangedListener listener : mListeners) {
            listener.onPlayProgressChanged(progress, duration);
        }
    }

    public boolean addMusicInfoChangedListener(IMusicInfoChangedListener listener) {
        Conditions.assertMainThread("addMusicInfoChangedListener");
        if (mListeners.contains(listener)) {
            return false;
        }
        return mListeners.add(listener);
    }

    public boolean removeMusicInfoChangedListener(IMusicInfoChangedListener listener) {
        Conditions.assertMainThread("removeMusicInfoChangedListener");
        if (!mListeners.contains(listener)) {
            return false;
        }
        return mListeners.remove(listener);
    }
}
