package com.txznet.txz.component.media;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.txz.ui.event.UiEvent;
import com.txz.ui.music.UiMusic;
import com.txz.ui.radio.UiRadio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.R;
import com.txznet.txz.component.audio.txz.AudioTongTing;
import com.txznet.txz.component.media.base.AbsAudioTool;
import com.txznet.txz.component.media.base.AbsMusicTool;
import com.txznet.txz.component.media.base.IMediaTool;
import com.txznet.txz.component.media.chooser.AbsMediaPriorityChooser;
import com.txznet.txz.component.media.chooser.AudioPriorityChooser;
import com.txznet.txz.component.media.chooser.MusicPriorityChooser;
import com.txznet.txz.component.media.loader.MediaToolManager;
import com.txznet.txz.component.media.model.AudioModel;
import com.txznet.txz.component.media.model.MediaModel;
import com.txznet.txz.component.media.model.MusicModel;
import com.txznet.txz.component.media.remote.RemoteAudioTool;
import com.txznet.txz.component.media.remote.RemoteMusicTool;
import com.txznet.txz.component.music.txz.MusicTongTing;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.app.MyInstallReceiver;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.config.ConfigManager;
import com.txznet.txz.module.mtj.MtjModule;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.SendBroadcast;

/**
 * 媒体工具优先级处理Manager
 *
 * 负责处理声控相关的音乐/电台工具优先级逻辑, 接受音乐/电台相关的操作指令, 自动根据场景和
 * 优先级处理转发给对应的音乐/电台工具
 *
 * 1. 记录最后播放的媒体工具为高优先级工具, 区分电台和音乐分别记录
 * 2. 提供播放器相关控制接口供调用, 根据内部维护的优先级对媒体工具进行选择, 并调用最终选择的媒体工具执行操作
 * 3. 高优先级工具状态需要做持久化存储, Core重启后优先恢复
 *
 * Created by J on 2018/4/25.
 */

public class MediaPriorityManager extends IModule {
    private static final String LOG_TAG = "MediaPriorityManager::";

    /**
     * 媒体工具选择优先级
     * 针对媒体工具的操作需要指定对应的优先级, MediaPriorityManager会依据指定的优先级类型和调用的操作接口
     * 选择合适的媒体工具去执行
     */
    public enum PRIORITY_TYPE {
        /**
         * 不指定, 一般情况下会被当做音乐优先处理
         */
        NONE,
        /**
         * 音乐优先, 音乐工具会被优先选择
         */
        MUSIC,
        /**
         * 电台优先, 电台工具会被优先选择
         */
        AUDIO,
        /**
         * 仅音乐, 即使没有可用音乐工具也不会尝试调用电台工具
         */
        MUSIC_ONLY,
        /**
         * 仅电台, 即使没有可用电台工具也不会尝试调用音乐工具
         */
        AUDIO_ONLY
    }

    /**
     * 当前播放工具
     */
    private IMediaTool mLastMediaTool;

    /**
     * 当前启动了搜索的工具
     *
     * 调用play进行播放时, 会启动播放器的搜索逻辑进行搜索, 播放器内部会根据设定进行播放/展示搜索列表的处理
     * 声控界面关闭时, 需要对启动了搜索的媒体工具进行取消搜索处理, 否则可能导致搜索结果显示时机错误
     */
    private IMediaTool mLastSearchMediaTool;

    /**
     * 上一个播放工具播放状态, 仅用于记录声控界面打开前的播放状态.
     * 因为声控界面打开会导致当前在播放的播放器被抢占焦点而暂停, 所以在声控界面获取当前播放器状态一定是
     * 停止状态, 为了获取当前播放器正确状态, 需要在声控界面打开时(抢占焦点前)对当前播放器状态进行记录
     */
    private IMediaTool.PLAYER_STATUS mLastMediaToolStatus = IMediaTool.PLAYER_STATUS.IDLE;

    /**
     * 用于获取支持搜索的媒体工具的checker
     */
    private AbsMediaPriorityChooser.MediaToolChecker<IMediaTool> mSearchToolChecker =
            new AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>() {
                @Override
                public boolean check(final IMediaTool tool) {
                    return tool.supportSearch();
                }
            };

    /**
     * 用于获取支持播放收藏的媒体工具的checker
     */
    private AbsMediaPriorityChooser.MediaToolChecker<IMediaTool> mPlayCollectionChecker =
            new AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>() {
                @Override
                public boolean check(final IMediaTool tool) {
                    return tool.supportPlayCollection();
                }
            };

    /**
     * 用于获取支持播放订阅的媒体工具的checker
     */
    private AbsMediaPriorityChooser.MediaToolChecker<IMediaTool> mPlaySubscribeChecker =
            new AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>() {
                @Override
                public boolean check(final IMediaTool tool) {
                    return tool.supportPlaySubscribe();
                }
            };

    /**
     * 获取当前媒体工具的播放状态
     *
     * @return
     */
    public IMediaTool.PLAYER_STATUS getCurrentMediaToolStatus() {
        if (null == mLastMediaTool) {
            return IMediaTool.PLAYER_STATUS.IDLE;
        }

        // 声控界面打开后因焦点被占用当前播放器会被强制暂停, 用记录下来的播放状态
        if (RecorderWin.isOpened()) {
            return mLastMediaToolStatus;
        }

        return mLastMediaTool.getStatus();
    }

    /**
     * 获取当前高优先级工具包名
     *
     * @return 当前高优先级工具的包名, 高优先级工具不存在时返回null
     */
    public @Nullable String getCurrentMediaToolPackageName() {
        return (null == mLastMediaTool) ? null : mLastMediaTool.getPackageName();
    }

    @Override
    public int initialize_BeforeStartJni() {
        // 搜索歌曲
        regEvent(UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC);
        // 打开音乐
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PAUSE);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_EXIT);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_NEXT);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PREV);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_PLAY_ALL);
        // 当前播放的是什么歌曲
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC, UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO);
        // 下面这两个事件是原来的随便听听， 走playRandom调用的，新版交互中被统一到了打开音乐逻辑中
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
                UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST);
        regEvent(UiEvent.EVENT_SYSTEM_MUSIC,
                UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST);

        // 搜索电台
        regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PLAY);
        // 打开电台
        regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PLAY_ONLY);
        // 暂停电台
        regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PAUSE);
        // 下一个电台
        regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_NEXT);
        // 上一个电台
        regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_PREV);
        // 退出电台
        regEvent(UiEvent.EVENT_CUSTOM_RADIO, UiRadio.SUBEVENT_RADIO_EXIT);

        return super.initialize_BeforeStartJni();
    }

    @Override
    public int initialize_AfterStartJni() {
        // ---------- 媒体控制命令 ----------
        // 继续播放
        regCommandWithResult("MEDIA_CMD_PLAY");
        // 停止播放
        regCustomCommand("MEDIA_CMD_STOP",
                UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_PAUSE);
        // 退出音乐
        regCustomCommand("MEDIA_CMD_EXIT",
                UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_EXIT);
        // 下一首
        regCustomCommand("MEDIA_CMD_NEXT",
                UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_NEXT);
        // 上一首
        regCustomCommand("MEDIA_CMD_PREV",
                UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_PREV);
        // 订阅
        regCommand("MEDIA_CMD_SUBSCRIBE");
        // 取消订阅
        regCommand("MEDIA_CMD_UNSUBSCRIBE");
        // 播放订阅
        regCommand("MEDIA_CMD_PLAY_SUBSCRIBE");
        // 收藏
        regCustomCommand("MEDIA_CMD_COLLECT",
                UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR);
        // 取消收藏
        regCustomCommand("MEDIA_CMD_UNCOLLECT", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR);
        // 播放收藏
        regCustomCommand("MEDIA_CMD_PLAY_COLLECT", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST);
        // 顺序播放模式
        regCommand("MEDIA_CMD_SWITCH_SEQUENTIAL");
        // 单曲循环模式
        regCustomCommand("MEDIA_CMD_SWITCH_SINGLE_LOOP", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE);
        // 列表循环模式
        regCustomCommand("MEDIA_CMD_SWITCH_LIST_LOOP", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL);
        // 随机播放模式
        regCustomCommand("MEDIA_CMD_SWITCH_SHUFFLE", UiEvent.EVENT_SYSTEM_MUSIC_SENCE,
                UiMusic.SUBEVENT_MEDIA_MODE_RANDOM);

        // ---------- 音乐控制命令 ----------
        // 播放音乐
        regCustomCommand("MUSIC_CMD_PLAY",
                UiEvent.EVENT_SYSTEM_MUSIC_SENCE, UiMusic.SUBEVENT_MEDIA_PLAY);
        // 下一首音乐
        regCommand("MUSIC_CMD_NEXT");
        // 上一首音乐
        regCommand("MUSIC_CMD_PREV");

        // ---------- 电台控制命令 ----------
        // 播放电台(打开)
        regCustomCommand("AUDIO_CMD_PLAY", UiEvent.EVENT_CUSTOM_RADIO,
                UiRadio.SUBEVENT_RADIO_PLAY_ONLY);
        // 退出电台
        regCustomCommand("AUDIO_CMD_EXIT", UiEvent.EVENT_CUSTOM_RADIO,
                UiRadio.SUBEVENT_RADIO_EXIT);
        // 下一首电台
        regCustomCommand("AUDIO_CMD_NEXT", UiEvent.EVENT_CUSTOM_RADIO,
                UiRadio.SUBEVENT_RADIO_NEXT);
        // 上一首电台
        regCustomCommand("AUDIO_CMD_PREV", UiEvent.EVENT_CUSTOM_RADIO,
                UiRadio.SUBEVENT_RADIO_PREV);

        return super.initialize_AfterStartJni();
    }

    @Override
    public int onEvent(final int eventId, final int subEventId, final byte[] data) {
        log(String.format("onEvent: %s | %s", eventId, subEventId));
        switch (eventId) {
            case UiEvent.EVENT_REMOTE_PROC_PLAY_MUSIC:
                MtjModule.getInstance().event(MtjModule.EVENTID_MUSIC);
                JSONBuilder json = new JSONBuilder(data);

                MusicModel musicModel = new MusicModel();
                musicModel.setArtists(json.getVal("artist", String[].class));
                musicModel.setAlbum(json.getVal("album", String.class));
                musicModel.setAsrText(json.getVal("text", String.class));
                musicModel.setKeywords(json.getVal("keywords", String[].class));
                musicModel.setTitle(json.getVal("title", String.class));

                if (MediaSceneProcessor.getInstance().procSearchMediaScene(PRIORITY_TYPE.MUSIC,
                        musicModel)) {
                    break;
                }

                play(false, MediaPriorityManager.PRIORITY_TYPE.MUSIC, musicModel);
                break;

            case UiEvent.EVENT_CUSTOM_RADIO:
                switch (subEventId) {
                    case UiRadio.SUBEVENT_RADIO_PLAY:
                        try {
                            UiRadio.RADIOModel radioModel = UiRadio.RADIOModel.parseFrom(data);
                            AudioModel audioModel = new AudioModel();
                            if (null != radioModel.rptStrArtist) {
                                audioModel.setArtists(radioModel.rptStrArtist);
                            }
                            if (null != radioModel.strTitle) {
                                audioModel.setTitle(radioModel.strTitle);
                            }
                            if (null != radioModel.strCategory) {
                                audioModel.setCategory(radioModel.strCategory);
                            }
                            if (null != radioModel.rptStrKeywords) {
                                audioModel.setKeywords(radioModel.rptStrKeywords);
                            }
                            if (null != radioModel.strTag) {
                                audioModel.setTag(radioModel.strTag);
                            }
                            if (null != radioModel.strAlbum) {
                                audioModel.setAlbum(radioModel.strAlbum);
                            }
                            if (null != radioModel.strSubCategory) {
                                audioModel.setSubCategory(radioModel.strSubCategory);
                            }
                            if (null != radioModel.strVoiceText) {
                                audioModel.setAsrText(radioModel.strVoiceText);
                            }
                            if (null != radioModel.rptRadioIndex) {
                                String[] audioIndex = new String[radioModel.rptRadioIndex.length
                                        * 2];
                                int i = 0;
                                for (UiRadio.RADIOIndex index : radioModel.rptRadioIndex) {
                                    audioIndex[i++] = String.valueOf(index.int32Number);
                                    audioIndex[i++] = new String(index.strUnit);
                                }
                                audioModel.setArrAudioIndex(audioIndex);
                            }

                            if (MediaSceneProcessor.getInstance().procSearchMediaScene(
                                    PRIORITY_TYPE.AUDIO, audioModel)) {
                                break;
                            }

                            play(false, PRIORITY_TYPE.AUDIO, audioModel);
                        } catch (Exception e) {

                        }
                        break;

                    case UiRadio.SUBEVENT_RADIO_PLAY_ONLY:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.OPEN, PRIORITY_TYPE.AUDIO)) {
                            break;
                        }

                        open(false, PRIORITY_TYPE.AUDIO, true);
                        break;

                    case UiRadio.SUBEVENT_RADIO_EXIT:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.EXIT, PRIORITY_TYPE.AUDIO)) {
                            break;
                        }

                        exit(false);
                        break;

                    case UiRadio.SUBEVENT_RADIO_NEXT:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.NEXT, PRIORITY_TYPE.AUDIO)) {
                            break;
                        }

                        next(false, PRIORITY_TYPE.AUDIO_ONLY);
                        break;

                    case UiRadio.SUBEVENT_RADIO_PREV:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.PREV, PRIORITY_TYPE.AUDIO)) {
                            break;
                        }

                        prev(false, PRIORITY_TYPE.AUDIO_ONLY);
                        break;
                }
                break;

            case UiEvent.EVENT_SYSTEM_MUSIC:
                switch (subEventId) {
                    case UiMusic.SUBEVENT_MEDIA_PLAY:
                    case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MEDIA_LIST:
                    case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_REFRESH_MUSIC_LIST:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.OPEN, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        open(false, PRIORITY_TYPE.MUSIC, true);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_PAUSE:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.PAUSE, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        pause(false, PRIORITY_TYPE.MUSIC);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_EXIT:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.EXIT, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        exit(false);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_NEXT:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.NEXT, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        next(false, PRIORITY_TYPE.NONE);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_PREV:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.PREV, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        prev(false, PRIORITY_TYPE.NONE);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MODE_LOOP_SINGLE:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SINGLE_LOOP,
                                PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        switchLoopMode(false, IMediaTool.LOOP_MODE.SINGLE_LOOP);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MODE_LOOP_ALL:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_LIST_LOOP,
                                PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        switchLoopMode(false, IMediaTool.LOOP_MODE.LIST_LOOP);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MODE_RANDOM:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SHUFFLE,
                                PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        switchLoopMode(false, IMediaTool.LOOP_MODE.SHUFFLE);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_FAVOURITE_CUR:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.COLLECT, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        collect(false);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_CANCEL_FAVOURITE_CUR:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.UNCOLLECT, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        unCollect(false);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_MUSIC_TOOL_PLAY_FAVOURITE_LIST:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.PLAY_COLLECTION, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        playCollection(false);
                        break;
                    case UiMusic.SUBEVENT_MEDIA_PLAY_ALL:
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.CONTINUE_PLAY, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        continuePlay(false, PRIORITY_TYPE.NONE);
                        break;

                    // 当前播放的是什么歌曲
                    case UiMusic.SUBEVENT_MEDIA_SPEAK_MUSIC_INFO: {
                        if (MediaSceneProcessor.getInstance().procMediaScene(
                                IMediaTool.MEDIA_TOOL_OP.GET_PLAYING_MODEL, PRIORITY_TYPE.NONE)) {
                            break;
                        }

                        speakMusicInfo();
                        break;
                    }
                }
                break;
        }
        return super.onEvent(eventId, subEventId, data);
    }


    @Override
    public int onCommand(final String cmd) {
        log("onCommand: " + cmd);
        if ("MEDIA_CMD_PLAY".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.CONTINUE_PLAY, PRIORITY_TYPE.NONE)) {
                continuePlay(false, PRIORITY_TYPE.NONE);
            }
        } else if ("MEDIA_CMD_SUBSCRIBE".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.SUBSCRIBE, PRIORITY_TYPE.NONE)) {
                subscribe(false);
            }
        } else if ("MEDIA_CMD_UNSUBSCRIBE".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.UNSUBSCRIBE, PRIORITY_TYPE.NONE)) {
                unsubscribe(false);
            }
        } else if ("MEDIA_CMD_PLAY_SUBSCRIBE".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.PLAY_SUBSCRIBE, PRIORITY_TYPE.NONE)) {
                playSubscribe(false);
            }
        } else if ("MEDIA_CMD_SWITCH_SEQUENTIAL".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SEQUENTIAL, PRIORITY_TYPE.NONE)) {
                switchLoopMode(false, IMediaTool.LOOP_MODE.SEQUENTIAL);
            }

        } else if ("MUSIC_CMD_NEXT".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.NEXT, PRIORITY_TYPE.MUSIC_ONLY)) {
                next(false, PRIORITY_TYPE.MUSIC_ONLY);
            }

        } else if ("MUSIC_CMD_PREV".equals(cmd)) {
            if (!MediaSceneProcessor.getInstance().procMediaScene(
                    IMediaTool.MEDIA_TOOL_OP.PREV, PRIORITY_TYPE.MUSIC_ONLY)) {
                prev(false, PRIORITY_TYPE.MUSIC_ONLY);
            }

        }
        return super.onCommand(cmd);
    }

    public void cancelSearchMedia() {
        if (null != mLastSearchMediaTool) {
            mLastSearchMediaTool.cancelRequest();
        }
    }

    /**
     * 主动记录当前播放工具播放状态
     *
     * 因为声控界面打开等场景下Core会抢占焦点, 如果此时当前媒体工具处于播放状态会因焦点丢失而暂停,
     * 导致获取播放状态不正确的问题, 所以提供此接口, 在需要抢占焦点等其他会导致媒体工具临时暂停的
     * 操作前先通知MediaPriorityManager对当前媒体工具状态进行记录.
     */
    public void recordMediaToolStatus() {
        if (null == mLastMediaTool) {
            log("recordMediaToolStatus: media tool is null");
            return;
        }

        mLastMediaToolStatus = mLastMediaTool.getStatus();
        log(String.format("recordMediaToolStatus: status = %s, tool = %s",
                mLastMediaToolStatus.name(), mLastMediaTool.getPackageName()));
    }

    /**
     * 通知MediaPriorityManager远程音乐工具被移除
     *
     * 远程工具被移除时, 如果当前高优先工具是远程音乐工具, 需要对播放状态等进行清理
     */
    public void notifyRemoteMusicToolCleared() {
        if (mLastMediaTool == RemoteMusicTool.getInstance()) {
            mLastMediaTool = null;
            mLastMediaToolStatus = IMediaTool.PLAYER_STATUS.IDLE;
        }

        if (MusicPriorityChooser.getInstance().getHighPriorityTool() ==
                RemoteMusicTool.getInstance()) {
            MusicPriorityChooser.getInstance().updatePriorityTool(null);
        }
    }

    /**
     * 通知MediaPriorityManager远程电台工具被移除
     *
     * 远程工具被移除时, 如果当前高优先工具是远程电台工具, 需要对播放状态等进行清理
     */
    public void notifyRemoteAudioToolCleared() {
        if (mLastMediaTool == RemoteAudioTool.getInstance()) {
            mLastMediaTool = null;
            mLastMediaToolStatus = IMediaTool.PLAYER_STATUS.IDLE;
        }

        if (AudioPriorityChooser.getInstance().getHighPriorityTool() ==
                RemoteAudioTool.getInstance()) {
            AudioPriorityChooser.getInstance().updatePriorityTool(null);
        }
    }

    /**
     * 更新高优先级工具
     *
     * @param mediaTool
     */
    public void notifyPriorityChange(IMediaTool mediaTool) {
        log("media tool priority change: tool = " + mediaTool.getPackageName());
        mLastMediaTool = mediaTool;

        if (mediaTool instanceof MusicTongTing || mediaTool instanceof AudioTongTing) {
            MusicPriorityChooser.getInstance().updatePriorityTool(MusicTongTing.getInstance());
            AudioPriorityChooser.getInstance().updatePriorityTool(AudioTongTing.getInstance());
        } else if (mediaTool instanceof AbsMusicTool) {
            MusicPriorityChooser.getInstance().updatePriorityTool((AbsMusicTool) mediaTool);
        } else if (mediaTool instanceof AbsAudioTool) {
            AudioPriorityChooser.getInstance().updatePriorityTool((AbsAudioTool) mediaTool);
        }
    }

    /**
     * “当前正在播报的是什么节目” 响应
     */
    private void speakMusicInfo() {
        String spk;
        // 判断当前是否有播放歌曲
        if (null == mLastMediaTool) {
            spk = NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE");
        } else if (!isPlaying()) {
            spk = NativeData.getResString("RS_MUSIC_NO_PLAY");
        } else {
            String mediaInfo = getMediaInfo(mLastMediaTool.getPlayingModel());
            if (TextUtils.isEmpty(mediaInfo)) {
                mediaInfo = "未知节目";
            }
            spk = NativeData.getResPlaceholderString("RS_MUSIC_IS_PLAY", "%MUSIC%", mediaInfo);
        }

        AsrManager.getInstance().setNeedCloseRecord(true);
        RecorderWin.speakTextWithClose(spk, null);
    }

    private String getMediaInfo(MediaModel model) {
        if (null == model) {
            return "";
        }

        StringBuilder singer = new StringBuilder();
        String[] artists = model.getArtists();
        if (null != artists && artists.length > 0) {
            for (int i = 0; i < artists.length; ++i) {
                if (!TextUtils.isEmpty(artists[i])) {
                    singer.append(artists[i]);
                    if (i < artists.length - 2) {
                        singer.append("、");
                    } else if (i == artists.length - 2) {
                        singer.append("和");
                    }
                }
            }
        }
        if (singer.length() > 0)
            singer.append("的");
        if (!TextUtils.isEmpty(model.getTitle())) {
            singer.append(model.getTitle());
        } else {
            if (singer.length() <= 0)
                singer.append("未知音乐");
            else
                singer.append("歌");
        }

        return singer.toString();
    }

    /**
     * 当前是否有播放器正在播放
     *
     * @return
     */
    private boolean isPlaying() {
        if (null == mLastMediaTool) {
            return false;
        }

        /*
        * 判断播放状态时需要考虑下当前声控界面的打开情况, 因为可能有sdk发起操作的情况, 此时声控界面并没有打开,
        * 记录的播放状态是没有更新的, 此时应该直接通过媒体工具接口获取
        * */
        IMediaTool.PLAYER_STATUS status;
        if (RecorderWin.isOpened()) {
            status = mLastMediaToolStatus;
        } else {
            status = mLastMediaTool.getStatus();
        }

        /*return (IMediaTool.PLAYER_STATUS.PLAYING == status
                || IMediaTool.PLAYER_STATUS.BUFFERING == status);*/

        // 20181026修改: 判断播放状态时忽略缓冲状态, 只考虑播放中
        return IMediaTool.PLAYER_STATUS.PLAYING == status;
    }

    public void open(boolean fromSDK, final PRIORITY_TYPE type, final boolean play) {
        log("action: open");

        final IMediaTool tool = checkMediaTool(fromSDK, type, false, true);
        if (null == tool) {
            return;
        }

        /*
         * 判断是否要提示已在播放中, 条件:
         * 1. lastMediaTool == tool
         * 2. lastMediaToolStatus是播放状态
         */
        if (tool.equals(mLastMediaTool) && isPlaying()) {
            speakErrorHint(fromSDK, "已在播放中");
            return;
        }
        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.OPEN);

        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.open(play);
                SendBroadcast.sendplay();
                MediaSceneProcessor.getInstance().reportMediaScene(IMediaTool.MEDIA_TOOL_OP.OPEN,
                        type, tool);
            }
        });
    }

    public void play(boolean fromSDK, final PRIORITY_TYPE type, final MediaModel model) {
        log("action: play isNeedBlockSearchTts:" + ConfigManager.getInstance()
                .isNeedBlockSearchTts);
        final IMediaTool tool = checkMediaTool(fromSDK, type, false, mSearchToolChecker,
                NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"), true);
        if (null == tool) {
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.PLAY);
        if (ConfigManager.getInstance().isNeedBlockSearchTts) {
            // 显示用户的话
            RecorderWin.showUserText();
            disableRecordWinControl = true;
        }
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                mLastSearchMediaTool = tool;
                tool.play(model);
                SendBroadcast.sendPlayMusic();
                MediaSceneProcessor.getInstance().reportMediaScene(IMediaTool.MEDIA_TOOL_OP.PLAY,
                        type, tool);
            }
        });
    }

    public void continuePlay(boolean fromSDK, final PRIORITY_TYPE type) {
        log("action: continuePlay");
        // 如果没有高优先级工具, 说明还未启动过任何播放器进行播放, 此时直接走打开逻辑
        if (null == mLastMediaTool) {
            log("continuePlay: lastMediaTool is null, redirect to open");
            open(fromSDK, PRIORITY_TYPE.NONE, true);
            return;
        }

        final IMediaTool tool = checkMediaTool(fromSDK, type, true, false);
        if (null == tool) {
            return;
        }

        if (isPlaying()) {
            speakErrorHint(fromSDK, "已在播放中");
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.CONTINUE_PLAY);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.continuePlay();
                SendBroadcast.sendplay();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.CONTINUE_PLAY, type, tool);
            }
        });
    }

    public void pause(boolean fromSDK, final PRIORITY_TYPE type) {
        log("action: pause");
        final IMediaTool tool = checkMediaTool(fromSDK, type, true, false);
        if (null == tool) {
            return;
        }

        if (!isPlaying()) {
            speakErrorHint(fromSDK, "当前未播放音频");
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.PAUSE);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.pause();
                SendBroadcast.sendPause();
                MediaSceneProcessor.getInstance().reportMediaScene(IMediaTool.MEDIA_TOOL_OP.PAUSE,
                        type, tool);
            }
        });
    }

    public void exit(boolean fromSDK) {
        log("action: exit");
        final IMediaTool tool = checkMediaTool(fromSDK, PRIORITY_TYPE.NONE, true, false);
        if (null == tool) {
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.EXIT);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                // 没有高优先级播放器情况下, 全部关闭
                if (null == mLastMediaTool || getCurrentMediaToolStatus() != IMediaTool.PLAYER_STATUS.PLAYING) {
                    exitAll();
                    return;
                }

                mLastMediaTool.exit();
                SendBroadcast.sendExit();
                MediaSceneProcessor.getInstance().reportMediaScene(IMediaTool.MEDIA_TOOL_OP.EXIT,
                        null, tool);
            }
        });
    }

    private void exitAll() {
        log("action: exitAll");
        // 逐个退出所有播放器
        MusicPriorityChooser.getInstance().getMediaTool(
                new AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>() {
                    @Override
                    public boolean check(final IMediaTool tool) {
                        tool.exit();
                        return false;
                    }
                }
        );

        AudioPriorityChooser.getInstance().getMediaTool(
                new AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>() {
                    @Override
                    public boolean check(final IMediaTool tool) {
                        tool.exit();
                        return false;
                    }
                }
        );

        SendBroadcast.sendExit();
    }

    public void next(boolean fromSDK, final PRIORITY_TYPE type) {
        log("action: next");
        if (null == mLastMediaTool) {
            log("next: lastMediaTool is null, redirect to open");
            open(fromSDK, type, true);
            return;
        }

        // 仅在没有指定优先级时考虑LastMediaTool
        //boolean includeLastMediaTool = PRIORITY_TYPE.NONE == type;

        // 非强制指定工具类型时考虑LastMediaTool
        boolean includeLastMediaTool = (PRIORITY_TYPE.NONE == type||PRIORITY_TYPE.MUSIC == type || PRIORITY_TYPE.AUDIO == type);
        final IMediaTool tool = checkMediaTool(fromSDK, type, includeLastMediaTool, false);
        if (null == tool) {
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.NEXT);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.next();
                SendBroadcast.sendNext();
                MediaSceneProcessor.getInstance().reportMediaScene(IMediaTool.MEDIA_TOOL_OP.NEXT,
                        type, tool);
            }
        });
    }

    public void prev(boolean fromSDK, final PRIORITY_TYPE type) {
        log("action: prev");

        if (null == mLastMediaTool) {
            log("prev: lastMediaTool is null, redirect to open");
            open(fromSDK, type, true);
            return;
        }

        // 仅在没有指定优先级时考虑LastMediaTool
        //boolean includeLastMediaTool = PRIORITY_TYPE.NONE == type;

        // 非强制指定工具类型时考虑LastMediaTool
        boolean includeLastMediaTool = (PRIORITY_TYPE.NONE == type||PRIORITY_TYPE.MUSIC == type || PRIORITY_TYPE.AUDIO == type);
        final IMediaTool tool = checkMediaTool(fromSDK, type, includeLastMediaTool, false);
        if (null == tool) {
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.PREV);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.prev();
                SendBroadcast.sendPre();
                MediaSceneProcessor.getInstance().reportMediaScene(IMediaTool.MEDIA_TOOL_OP.PREV,
                        type, tool);
            }
        });
    }

    public void switchLoopMode(boolean fromSDK, final IMediaTool.LOOP_MODE mode) {
        log("action: switchLoopMode");
        // 没有高优先级播放器情况下, 提示没有打开
        if (null == mLastMediaTool) {
            speakErrorHint(fromSDK, "播放器还没有打开");
            return;
        }

        if (!mLastMediaTool.supportLoopMode(mode)) {
            speakErrorHint(fromSDK, "当前不支持该模式");
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || mLastMediaTool.interceptRecordWinControl(getSwitchModeOP(mode));
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                mLastMediaTool.switchLoopMode(mode);
                sendSwitchLoopModeBroadcast(mode);
                MediaSceneProcessor.getInstance().reportMediaScene(getSwitchModeOP(mode),
                        null, mLastMediaTool);
            }
        });
    }

    private IMediaTool.MEDIA_TOOL_OP getSwitchModeOP(IMediaTool.LOOP_MODE mode) {
        IMediaTool.MEDIA_TOOL_OP ret = IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SEQUENTIAL;
        switch (mode) {
            case SEQUENTIAL:
                ret = IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SEQUENTIAL;
                break;

            case SINGLE_LOOP:
                ret = IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SINGLE_LOOP;
                break;

            case LIST_LOOP:
                ret = IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_LIST_LOOP;
                break;

            case SHUFFLE:
                ret = IMediaTool.MEDIA_TOOL_OP.SWITCH_MODE_SHUFFLE;
                break;
        }

        return ret;
    }

    private void sendSwitchLoopModeBroadcast(IMediaTool.LOOP_MODE mode) {
        switch (mode) {
            case SEQUENTIAL:
            case LIST_LOOP:
                SendBroadcast.sendSwitchModeLoopAll();
                break;

            case SINGLE_LOOP:
                SendBroadcast.sendSwitchModeLoopOne();
                break;

            case SHUFFLE:
                SendBroadcast.sendSwitchModeRandom();
                break;
        }
    }

    public void subscribe(boolean fromSDK) {
        log("action: subscribe");
        if (null == mLastMediaTool
                || !mLastMediaTool.supportSubscribe()
                || !isPlaying()) {
            // 不支持该操作
            speakErrorHint(fromSDK, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || mLastMediaTool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.SUBSCRIBE);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                mLastMediaTool.subscribe();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.SUBSCRIBE, null, mLastMediaTool);
            }
        });
    }

    public void unsubscribe(boolean fromSDK) {
        log("action: unsubscribe");
        if (null == mLastMediaTool
                || !mLastMediaTool.supportSubscribe()
                || !isPlaying()) {
            // 不支持该操作
            speakErrorHint(fromSDK, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || mLastMediaTool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.UNSUBSCRIBE);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                mLastMediaTool.unSubscribe();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.UNSUBSCRIBE, null, mLastMediaTool);
            }
        });
    }

    public void playSubscribe(boolean fromSDK) {
        log("action: playSubscribe");
        final IMediaTool tool = checkMediaTool(fromSDK, PRIORITY_TYPE.AUDIO, true,
                mPlaySubscribeChecker, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),
                false);
        if (null == tool) {
            return;
        }

        /*if (!tool.supportPlaySubscribe()) {
            // 不支持该操作
            speakErrorHint(fromSDK, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
            return;
        }*/

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.PLAY_SUBSCRIBE);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.playSubscribe();
                SendBroadcast.sendplay();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.PLAY_SUBSCRIBE, null, tool);
            }
        });
    }

    public void collect(boolean fromSDK) {
        log("action: collect");
        if (null == mLastMediaTool
                || !mLastMediaTool.supportCollect()
                || !isPlaying()) {
            // 不支持该操作
            speakErrorHint(fromSDK, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || mLastMediaTool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.COLLECT);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                mLastMediaTool.collect();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.COLLECT, null, mLastMediaTool);
            }
        });
    }

    public void unCollect(boolean fromSDK) {
        log("action: unCollect");
        if (null == mLastMediaTool
                || !mLastMediaTool.supportUnCollect()
                || !isPlaying()) {
            // 不支持该操作
            speakErrorHint(fromSDK, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"));
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || mLastMediaTool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.UNCOLLECT);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                mLastMediaTool.unCollect();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.UNCOLLECT, null, mLastMediaTool);
            }
        });
    }

    public void playCollection(boolean fromSDK) {
        log("action: playCollection");
        final IMediaTool tool = checkMediaTool(fromSDK, PRIORITY_TYPE.MUSIC, true,
                mPlayCollectionChecker, NativeData.getResString("RS_VOICE_UNSUPPORT_OPERATE"),
                false);
        if (null == tool) {
            return;
        }

        boolean disableRecordWinControl = fromSDK
                || tool.interceptRecordWinControl(IMediaTool.MEDIA_TOOL_OP.PLAY_COLLECTION);
        executeOperation(disableRecordWinControl, new Runnable() {
            @Override
            public void run() {
                tool.playCollection();
                SendBroadcast.sendplay();
                MediaSceneProcessor.getInstance().reportMediaScene(
                        IMediaTool.MEDIA_TOOL_OP.PLAY_COLLECTION, null, tool);
            }
        });
    }

    private IMediaTool checkMediaTool(boolean disableRecordWinControl, PRIORITY_TYPE type,
                                      boolean includeLastMediaTool, boolean enableIntercept) {
        return checkMediaTool(disableRecordWinControl, type, includeLastMediaTool, null, null,
                enableIntercept);
    }

    /**
     * 检查是否有符合指定PriorityType和指定条件的媒体工具, 并在没有合适工具时进行错误提示
     *
     * @param disableRecordWinControl 是否需要屏蔽声控界面和tts逻辑
     * @param type                    优先级类型
     * @param includeLastMediaTool    是否考虑最后一个媒体工具
     * @param checker                 媒体工具检查器
     * @param errTipText              错误提示文本, 需要注意下传null表示默认提示, 传""表示空提示
     * @param enableIntercept         是否允许MediaChooser对工具选择逻辑进行拦截
     * @return 符合条件的媒体工具, 无可用工具返回null
     */
    private IMediaTool checkMediaTool(boolean disableRecordWinControl, PRIORITY_TYPE type,
                                      boolean includeLastMediaTool,
                                      AbsMediaPriorityChooser.MediaToolChecker<IMediaTool> checker,
                                      String errTipText, boolean enableIntercept) {
        // 如果MediaToolChooser选择拦截, 直接处理
        IMediaTool tool = null;
        if (enableIntercept) {
            tool = dispatchMediaToolSearch(type);

            if (null != tool) {
                return tool;
            }
        }

        if (includeLastMediaTool
                && null != mLastMediaTool) {
            // 如果指定了checker, lastMediaTool也需要符合checker条件才可以
            if (null == checker || checker.check(mLastMediaTool)) {
                tool = mLastMediaTool;
            }
        } else {
            tool = getMediaToolWithPriority(type, checker);
        }

        if (null == tool) {
            String spk = errTipText;
            // 没有指定错误提示文本时, 默认按优先级类型进行提示
            if (null == errTipText) {
                if (PRIORITY_TYPE.NONE == type
                        || PRIORITY_TYPE.MUSIC == type
                        || PRIORITY_TYPE.MUSIC_ONLY == type) {
                    spk = NativeData.getResString("RS_MUSIC_NOT_TOOL");
                } else {
                    spk = NativeData.getResString("RS_AUDIO_NO_AUDIO");
                }
            }

            speakErrorHint(disableRecordWinControl, spk);
        }

        // 持久化媒体工具优先级
        /*if (PRIORITY_TYPE.MUSIC == type || PRIORITY_TYPE.MUSIC_ONLY == type) {
            MediaPrioritySp.getInstance().updatePriorityMusic(tool.getPackageName());
        } else if (PRIORITY_TYPE.AUDIO == type || PRIORITY_TYPE.AUDIO_ONLY == type) {
            MediaPrioritySp.getInstance().updatePriorityAudio(tool.getPackageName());
        }*/

        return tool;
    }

    private IMediaTool dispatchMediaToolSearch(PRIORITY_TYPE type) {
        if (PRIORITY_TYPE.NONE == type
                || PRIORITY_TYPE.MUSIC == type
                || PRIORITY_TYPE.MUSIC_ONLY == type) {
            log("media tool choose dispatch to MusicPriorityChooser");
            return MusicPriorityChooser.getInstance().interceptMediaToolChoose();
        } else {
            log("media tool choose dispatch to AudioPriorityChooser");
            return AudioPriorityChooser.getInstance().interceptMediaToolChoose();
        }
    }

    /**
     * 根据指定优先级类型和条件获取合适的媒体工具
     *
     * @param type    优先级类型
     * @param checker 条件检查器
     * @return
     */
    public IMediaTool getMediaToolWithPriority(PRIORITY_TYPE type,
                                               AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>
                                                       checker) {
        IMediaTool result;
        // 不指定优先级也按音乐优先处理
        if (PRIORITY_TYPE.NONE == type
                || PRIORITY_TYPE.MUSIC == type
                || PRIORITY_TYPE.MUSIC_ONLY == type) {
            result = getMusicToolWithChecker(checker);

            if (null == result && PRIORITY_TYPE.MUSIC_ONLY != type) {
                result = getAudioToolWithChecker(checker);
            }

        } else {
            result = getAudioToolWithChecker(checker);

            if (null == result && PRIORITY_TYPE.AUDIO_ONLY != type) {
                result = getMusicToolWithChecker(checker);
            }
        }

        return result;
    }

    /**
     * 根据传入的Checker获取合适的音乐工具
     *
     * 仅获取音乐工具, 不会尝试获取电台工具
     *
     * @param checker 传null不进行check
     * @return 没有合适的工具返回null
     */
    private IMediaTool getMusicToolWithChecker(AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>
                                                       checker) {
        return MusicPriorityChooser.getInstance().getMediaTool(checker);
    }

    /**
     * 根据传入的Checker获取合适的电台工具
     *
     * 仅获取电台工具, 不会尝试获取音乐工具
     *
     * @param checker 传null不进行check
     * @return 没有合适的工具返回null
     */
    private IMediaTool getAudioToolWithChecker(AbsMediaPriorityChooser.MediaToolChecker<IMediaTool>
                                                       checker) {
        return AudioPriorityChooser.getInstance().getMediaTool(checker);
    }

    /**
     * 执行操作
     *
     * @param disableRecordWinControl 是否需要屏蔽声控界面相关操作(tts/关闭界面操作)
     * @param runnable                需要执行的操作
     */
    private void executeOperation(boolean disableRecordWinControl, Runnable runnable) {
        speakTextAndRun(disableRecordWinControl,
                NativeData.getResString("RS_VOICE_MEDIA_CONTROL_CONFIRM"), runnable);
    }

    /**
     * 进行错误提示
     *
     * @param disableRecordWinControl 是否需要屏蔽声控界面相关操作(tts/关闭界面操作)
     * @param text                    指定的提示文本
     */
    private void speakErrorHint(boolean disableRecordWinControl, String text) {
        speakTextAndRun(disableRecordWinControl, text, null);
    }

    /**
     * 播报tts, 关闭声控界面并执行操作
     *
     * @param disableRecordWinControl 是否需要屏蔽声控界面相关操作(tts/关闭界面操作)
     * @param text
     * @param runnable
     */
    private void speakTextAndRun(boolean disableRecordWinControl, String text, Runnable runnable) {
        // 对于从sdk发起的调用, 不进行播报和声控界面处理
        if (disableRecordWinControl) {
            if (null != runnable) {
                runnable.run();
            }
            return;
        }

        AsrManager.getInstance().setNeedCloseRecord(true);
        RecorderWin.speakTextWithClose(text, false, false, runnable);
    }

    //----------- single instance -----------
    private static volatile MediaPriorityManager sInstance;

    public static MediaPriorityManager getInstance() {
        if (null == sInstance) {
            synchronized (MediaPriorityManager.class) {
                if (null == sInstance) {
                    sInstance = new MediaPriorityManager();
                }
            }
        }

        return sInstance;
    }

    private MediaPriorityManager() {
        // 开始媒体工具装载
        AppLogic.runOnBackGround(new Runnable() {
            @Override
            public void run() {
                MediaToolManager.getInstance().initMediaToolList();
            }
        });

        // 设置卸载监听，避免高优先播放器被卸载导致当前播放状态错误或声控操作播放器不响应
        MyInstallReceiver.SINSTALL_OBSERVABLE.registerObserver(
                new MyInstallReceiver.InstallObservable.InstallObserver() {
                    @Override
                    public void onApkInstall(final String packageName) {

                    }

                    @Override
                    public void onApkUnInstall(final String packageName) {
                        // 如果高优先工具被卸载，需要清空对应的设置
                        if (null != mLastMediaTool && packageName.equals(mLastMediaTool
                                .getPackageName())) {
                            mLastMediaTool = null;
                            mLastMediaToolStatus = IMediaTool.PLAYER_STATUS.IDLE;
                        }

                        // 通知PriorityChooser处理应用卸载情况
                        MusicPriorityChooser.getInstance().onMediaToolUninstalled(packageName);
                        AudioPriorityChooser.getInstance().onMediaToolUninstalled(packageName);
                    }
                });
    }
    //----------- single instance -----------

    // logger
    private void log(String cmd) {
        JNIHelper.logd(LOG_TAG + cmd);
    }
}
