package com.txznet.music.store;

import com.txznet.audio.player.IMediaPlayer;
import com.txznet.audio.player.util.PlayStateUtil;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.action.ActionType;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAlbum;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.helper.PlayHelper;
import com.txznet.music.report.ReportEvent;
import com.txznet.music.report.entity.LocalScanEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.report.entity.SysOpenEvent;
import com.txznet.music.util.AudioUtils;
import com.txznet.music.util.Logger;
import com.txznet.rxflux.Operation;
import com.txznet.rxflux.RxAction;
import com.txznet.rxflux.Store;

import java.util.List;

/**
 * 上报监听
 *
 * @author zackzhou
 * @date 2019/1/20,15:33
 */

public class ReportStore extends Store {
    public static final String TAG = Constant.LOG_TAG_REPORT + ":Store";

    @Override
    protected String[] getActionTypes() {
        return null;
    }

    @Override
    protected void onAction(RxAction action) {
        AppLogic.runOnBackGround(() -> {
            String channel;
            List<? extends AudioV5> audioV5List;
            AudioV5 audioV5;
            Album album;
            int position;
            switch (action.type) {
                case ActionType.ACTION_LOCAL_SORT_BY_NAME: // 排序
                    ReportEvent.reportLocalSortByName();
                    break;
                case ActionType.ACTION_LOCAL_SORT_BY_TIME: // 排序
                    ReportEvent.reportLocalSortByTime();
                    break;
                case ActionType.ACTION_FAVOUR_EVENT_FAVOUR: // 收藏
                    channel = (String) action.data.get(Constant.FavourConstant.KEY_FAVOUR_CHANNEL);
                    audioV5 = (AudioV5) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                    if ("local".equals(channel)) {
                        ReportEvent.reportLocalFavour(audioV5, true);
                    }
                    ReportEvent.reportGlobalFavourOrUnFavour(audioV5, true);
                    break;
                case ActionType.ACTION_FAVOUR_EVENT_UNFAVOUR: // 取消收藏
                    channel = (String) action.data.get(Constant.FavourConstant.KEY_FAVOUR_CHANNEL);
                    audioV5 = (AudioV5) action.data.get(Constant.FavourConstant.KEY_FAVOUR_AUDIO);
                    if ("local".equals(channel)) {
                        ReportEvent.reportLocalFavour(audioV5, false);
                    } else if ("favour".equals(channel)) {
                        ReportEvent.reportUserFavour(audioV5, false);
                    }
                    ReportEvent.reportGlobalFavourOrUnFavour(audioV5, false);
                    break;
                case ActionType.ACTION_PLAYER_PLAY_LOCAL: // 播放本地
                    audioV5List = (List<LocalAudio>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                    position = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    if (position != -1) {
                        ReportEvent.reportLocalListItemClick(audioV5List.get(position));
                    }
                    break;
                case ActionType.ACTION_PLAYER_PLAY_FAVOUR: // 播放收藏
                    audioV5List = (List<FavourAudio>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                    position = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    if (position != -1) {
                        ReportEvent.reportUserFavourItemClick(audioV5List.get(position));
                    }
                    break;
                case ActionType.ACTION_PLAYER_PLAY_HISTORY_MUSIC: // 播放历史音乐
                    audioV5List = (List<HistoryAudio>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                    position = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    if (position != -1) {
                        ReportEvent.reportUserHistoryMusicItemClick(audioV5List.get(position));
                    }
                    break;
                case ActionType.ACTION_PLAYER_PLAY_ALBUM: // 播放历史专辑
                    PlayScene scene = (PlayScene) action.data.get(Constant.PlayConstant.KEY_SCENE);
                    if (PlayScene.HISTORY_ALBUM == scene) {
                        album = (Album) action.data.get(Constant.PlayConstant.KEY_ALBUM);
                        ReportEvent.reportUserHistoryRadioItemClick(album);
                    }
                    break;
                case ActionType.ACTION_SUBSCRIBE_EVENT_SUBSCRIBE: // 订阅
                    channel = (String) action.data.get(Constant.SubscribeConstant.KEY_CHANNEL);
                    album = (Album) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM);
                    ReportEvent.reportGlobalSubscribeOrUnSubscribe(album, true);
                    break;
                case ActionType.ACTION_SUBSCRIBE_EVENT_UNSUBSCRIBE: // 取消订阅
                    channel = (String) action.data.get(Constant.SubscribeConstant.KEY_CHANNEL);
                    album = (Album) action.data.get(Constant.SubscribeConstant.KEY_SUBSCRIBE_ALBUM);
                    if ("subscribe".equals(channel)) {
                        ReportEvent.reportUserSubscribeUnSubscribe(album);
                    }
                    ReportEvent.reportGlobalSubscribeOrUnSubscribe(album, false);
                    break;
                case ActionType.ACTION_COMMAND_FAVOUR_SUBSCRIBE:
                    album = getCurrentAlbum();
                    if (album == null) {
                        audioV5 = PlayHelper.get().getCurrAudio();
                        if (audioV5 != null) {
                            ReportEvent.reportGlobalFavourOrUnFavour(audioV5, true);
                        }
                    } else {
                        ReportEvent.reportGlobalSubscribeOrUnSubscribe(album, true);
                    }
                    break;
                case ActionType.ACTION_COMMAND_UNFAVOUR_UNSUBSCRIBE:
                    album = getCurrentAlbum();
                    if (album == null) {
                        audioV5 = PlayHelper.get().getCurrAudio();
                        if (audioV5 != null) {
                            ReportEvent.reportGlobalFavourOrUnFavour(audioV5, false);
                        }
                    } else {
                        ReportEvent.reportGlobalSubscribeOrUnSubscribe(album, false);
                    }
                    break;
                case ActionType.ACTION_WXPUSH_EVENT_DELETE: // 微信推送删除
                    audioV5List = (List<PushItem>) action.data.get(Constant.WxPushConstant.KEY_AUDIOS);
                    ReportEvent.reportUserWxDelete(audioV5List);
                    break;
                case ActionType.ACTION_PLAYER_PLAY_WX_PUSH: // 播放微信推送
                    audioV5List = (List<? extends AudioV5>) action.data.get(Constant.PlayConstant.KEY_AUDIO_LIST);
                    position = (int) action.data.get(Constant.PlayConstant.KEY_POSITION);
                    if (position != -1) {
                        ReportEvent.reportUserWxListClick(audioV5List.get(position));
                    }
                    break;
                case ActionType.ACTION_PLAYER_PLAY_NEXT:
                    audioV5 = PlayHelper.get().getCurrAudio();
                    if (audioV5 != null) {
                        ReportEvent.reportPlayerAudioSwitch(PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(), audioV5, true);
                    }
                    break;
                case ActionType.ACTION_PLAYER_PLAY_PREV:
                    audioV5 = PlayHelper.get().getCurrAudio();
                    if (audioV5 != null) {
                        ReportEvent.reportPlayerAudioSwitch(PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(), audioV5, false);
                    }
                    break;
                case ActionType.ACTION_PLAYER_ON_STATE_CHANGE:
                    audioV5 = PlayHelper.get().getCurrAudio();
                    int state = PlayHelper.get().getCurrPlayState();
                    Logger.d(TAG, "ACTION_PLAYER_ON_STATE_CHANGE state=" + PlayStateUtil.convert2Str(state));
                    if (IMediaPlayer.STATE_ON_PAUSED == state && audioV5 != null) {
                        @PlayInfoEvent.ExitType int exitType;
                        Operation operation = action.operation;
                        if (Operation.SOUND == operation) {
                            exitType = PlayInfoEvent.EXIT_TYPE_SOUND;
                        } else if (Operation.MANUAL == operation) {
                            exitType = PlayInfoEvent.EXIT_TYPE_MANUAL;
                        } else if (Operation.FOCUS == operation) {
                            exitType = PlayInfoEvent.EXIT_TYPE_AUDIO_FOCUS;
                        } else {
                            exitType = PlayInfoEvent.EXIT_TYPE_OTHER;
                        }
                        ReportEvent.reportAudioPlayPause(audioV5,
                                (Operation.SOUND == operation || Operation.MANUAL == operation) ? PlayInfoEvent.MANUAL_TYPE_MANUAL : PlayInfoEvent.MANUAL_TYPE_AUTO,
                                AudioUtils.isLocalSong(audioV5.sid) ? PlayInfoEvent.ONLINE_TYPE_OFFLINE : PlayInfoEvent.ONLINE_TYPE_ONLINE,
                                PlayHelper.get().getLastDuration(), PlayHelper.get().getLastPosition(), exitType);
                    }
                    break;
                case ActionType.ACTION_AI_RADIO_DELETE:
                    audioV5 = (AudioV5) action.data.get(Constant.AiConstant.KEY_AUDIO);
                    ReportEvent.reportAiModeDelete(audioV5);
                    break;
                case ActionType.ACTION_PLAYER_ON_INFO_CHANGE:
                    audioV5 = (AudioV5) action.data.get(Constant.AiConstant.KEY_AUDIO);
                    if (audioV5 != null) {
                        ReportEvent.reportAudioPlayStart(audioV5);
                    }
                    break;
                case ActionType.ACTION_SEARCH_EVENT_CHOICE_SEARCH_RESULT: // 搜索结果选择
                    ReportEvent.reportEnter(SysOpenEvent.ENTRY_TYPE_VOICE_SEARCH);
                    break;
            }
        });
    }

    private Album getCurrentAlbum() {
        //判断当前播放的是音乐还是电台
        if (PlayHelper.get().getCurrAlbum() == null) {
            return null;
        }

        AudioV5 currentAudio = PlayHelper.get().getCurrAudio();
        if (currentAudio != null) {
            if (!AudioUtils.isSong(currentAudio.sid)) {
                //当前播放的是电台
                return PlayHelper.get().getCurrAlbum();
            }
        }
        return null;
    }

    @Override
    protected void onData(RxAction action) {
        List<? extends AudioV5> audioList;
        boolean enable;
        switch (action.type) {
            case ActionType.ACTION_SCAN:
                long cost = (long) action.data.get(Constant.LocalConstant.KEY_COST_TIME);
                boolean isIntercepted = (boolean) action.data.get(Constant.LocalConstant.KEY_IS_INTERCEPTED);
                audioList = (List<LocalAudio>) action.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO);
                ReportEvent.reportLocalScan(cost, audioList.size(), isIntercepted ? LocalScanEvent.EXIT_TYPE_MANUAL : LocalScanEvent.EXIT_TYPE_NORMAL);
                break;
            case ActionType.ACTION_LOCAL_DELETE:
                audioList = (List<LocalAudio>) action.data.get(Constant.LocalConstant.KEY_LOCAL_MUSIC_AUDIO);
                ReportEvent.reportLocalDelete(audioList);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_MUSIC:
                audioList = (List<HistoryAudio>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_MUSIC_AUDIOS);
                ReportEvent.reportUserHistoryMusicDelete(audioList);
                break;
            case ActionType.ACTION_GET_DEL_ITEM_HISTORY_ALBUM:
                List<HistoryAlbum> historyAlbumList = (List<HistoryAlbum>) action.data.get(Constant.HistoryConstant.KEY_HISTORY_ALBUMS_DELETE);
                ReportEvent.reportUserHistoryRadioDelete(historyAlbumList);
                break;
            case ActionType.ACTION_SETTING_CLICK_BOOT_PLAY: // 设置界面 - 智能点火播放
                enable = (boolean) action.data.get(Constant.SettingConstant.KEY_ENABLE);
                ReportEvent.reportSettingsBootPlay(enable);
                break;
            case ActionType.ACTION_SETTING_CLICK_OPEN_ASR: // 设置界面 - 免唤醒
                enable = (boolean) action.data.get(Constant.SettingConstant.KEY_ENABLE);
                ReportEvent.reportWakeupCmd(enable);
                break;
            case ActionType.ACTION_SETTING_CLICK_CLEAR_MEMORY: // 设置界面 - 清除缓存
                long cacheSize = (long) action.data.get(Constant.SettingConstant.KEY_CACHE_SIZE);
                ReportEvent.reportClearCache(cacheSize);
                break;
        }
    }

    @Override
    protected void onError(RxAction action, Throwable throwable) {
    }
}
