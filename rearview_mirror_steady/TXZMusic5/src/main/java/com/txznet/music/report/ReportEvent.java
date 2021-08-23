package com.txznet.music.report;

import com.txznet.loader.AppLogic;
import com.txznet.music.BuildConfig;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.helper.AlbumConverts;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.report.entity.AlbumItemEvent;
import com.txznet.music.report.entity.AlbumListEvent;
import com.txznet.music.report.entity.AlbumWithParentItemEvent;
import com.txznet.music.report.entity.AudioItemEvent;
import com.txznet.music.report.entity.AudioListEvent;
import com.txznet.music.report.entity.AudioSwitchEvent;
import com.txznet.music.report.entity.BaseEvent;
import com.txznet.music.report.entity.BillBoardClickEvent;
import com.txznet.music.report.entity.BillBoardContentClickEvent;
import com.txznet.music.report.entity.CacheEvent;
import com.txznet.music.report.entity.CategoryItemEvent;
import com.txznet.music.report.entity.FavourEvent;
import com.txznet.music.report.entity.LocalScanEvent;
import com.txznet.music.report.entity.OpEvent;
import com.txznet.music.report.entity.OpPlayModeEvent;
import com.txznet.music.report.entity.PlayInfoEvent;
import com.txznet.music.report.entity.SeekEvent;
import com.txznet.music.report.entity.SubscribeEvent;
import com.txznet.music.report.entity.SysExitEvent;
import com.txznet.music.report.entity.SysOpenEvent;
import com.txznet.music.report.entity.SysPageAlbumItemClickEvent;
import com.txznet.music.report.entity.SysPageCategoryItemClickEvent;
import com.txznet.music.report.entity.SysPageItemClickEvent;
import com.txznet.music.report.entity.SysPageSlideEvent;
import com.txznet.music.util.TimeManager;

import java.util.List;

/**
 * 上报事件工具
 * 同听5.0，数据报点整理
 *
 * @author zackzhou
 * @date 2018/12/27,14:35
 */

public class ReportEvent implements ReportEventProtocol {
    private static long seqId = 1;

    /**
     * 4.1 打开同听
     */
    public static void reportEnter(@SysOpenEvent.EnterType int enterType) {
        AppLogic.runOnBackGround(() -> doReport(new SysOpenEvent(SYS_OPEN_MUSIC, enterType)));
    }

    /**
     * 4.2 退出同听
     */
    public static void reportExit(@SysExitEvent.ExitType int exitType) {
        AppLogic.runOnBackGround(() -> doReport(new SysExitEvent(SYS_EXIT_MUSIC, exitType)));
    }

    /**
     * 4.3 推荐位点击报点
     */
    public static void reportPageItemClick(@SysPageItemClickEvent.PageType int pageType, int posId) {
        AppLogic.runOnBackGround(() -> doReport(new SysPageItemClickEvent(pageType, posId)));
    }

    /**
     * 4.3 推荐位点击报点
     */
    public static void reportPageItemClick(@SysPageItemClickEvent.PageType int pageType, int posId, Album album) {
        AppLogic.runOnBackGround(() -> doReport(new SysPageAlbumItemClickEvent(pageType, posId, AlbumConverts.convert2Report(album))));
    }

    /**
     * 4.3 推荐位点击报点
     */
    public static void reportPageItemClick(@SysPageItemClickEvent.PageType int pageType, int posId, long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new SysPageCategoryItemClickEvent(pageType, posId, categoryId)));
    }

    /**
     * 4.4 Tab滑动报点
     *
     * @param curPageId 当前tab编号
     * @param prePageId 前一个tab编号
     */
    public static void reportPageSlide(@SysPageSlideEvent.PageId int curPageId, @SysPageSlideEvent.PageId int prePageId) {
        AppLogic.runOnBackGround(() -> doReport(new SysPageSlideEvent(SYS_PAGE_SLIDE, curPageId, prePageId)));
    }

    /**
     * 4.5~4.7 tab点击报点
     */
    public static void reportPageClick(int page) {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(page)));
    }

    /**
     * 5.1.2 进入本地音乐
     */
    public static void reportLocalEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(LOCAL_ENTER)));
    }

    /**
     * 5.1.3 本地扫描
     */
    public static void reportLocalScan(long costTime, int audioNum, @LocalScanEvent.ExitType int exitType) {
        AppLogic.runOnBackGround(() -> doReport(new LocalScanEvent(LOCAL_SCAN, costTime, audioNum, exitType)));
    }

    /**
     * 5.1.4 删除管理
     */
    public static void reportLocalDelete(List<? extends AudioV5> audioList) {
        AppLogic.runOnBackGround(() -> doReport(new AudioListEvent(LOCAL_DELETE, AudioConverts.convert2List(audioList, AudioConverts::convert2Report))));
    }

    /**
     * 5.1.5 按添加时间排序
     */
    public static void reportLocalSortByTime() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(LOCAL_SORT_BY_TIME)));

    }

    /**
     * 5.1.6 按歌曲首字母排序
     */
    public static void reportLocalSortByName() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(LOCAL_SORT_BY_NAME)));
    }

    /**
     * 5.1.7 收藏/取消收藏本地歌曲
     */
    public static void reportLocalFavour(AudioV5 audio, boolean isFavour) {
        AppLogic.runOnBackGround(() -> doReport(new FavourEvent(LOCAL_FAVOUR_OR_UNFAVOUR, AudioConverts.convert2Report(audio), isFavour ? FavourEvent.OP_TYPE_FAVOUR : FavourEvent.OP_TYPE_UN_FAVOUR)));
    }

    /**
     * 5.1.8 点击封面播放本地音乐
     */
    public static void reportLocalPlay() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(LOCAL_COVER_CLICK)));
    }

    /**
     * 5.1.9 点击列表播放本地音乐
     */
    public static void reportLocalListItemClick(AudioV5 audioV5) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(LOCAL_LIST_ITEM_CLICK, AudioConverts.convert2Report(audioV5))));
    }

    /**
     * 5.2.2 点击个人中心
     */
    public static void reportUserEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_CLICK)));
    }

    /**
     * 5.2.3.1 进入收藏二级界面
     */
    public static void reportUserFavourEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_FAVOUR_ENTER)));
    }

    /**
     * 5.2.3.2 收藏/取消收藏
     */
    public static void reportUserFavour(AudioV5 audio, boolean isFavour) {
        AppLogic.runOnBackGround(() -> doReport(new FavourEvent(USER_CENTER_ITEM_FAVOUR_FAVOUR_OR_UNFAVOUR, AudioConverts.convert2Report(audio), isFavour ? FavourEvent.OP_TYPE_FAVOUR : FavourEvent.OP_TYPE_UN_FAVOUR)));
    }

    /**
     * 5.2.3.3 点击播放收藏歌曲
     */
    public static void reportUserFavourItemClick(AudioV5 audioV5) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(USER_CENTER_ITEM_FAVOUR_LIST_ITEM_CLICK, AudioConverts.convert2Report(audioV5))));
    }

    /**
     * 5.2.3.4 退出收藏栏
     */
    public static void reportUserFavourExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_FAVOUR_EXIT)));
    }

    /**
     * 5.2.4.1 进入订阅节目
     */
    public static void reportUserSubscribeEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_SUBSCRIBE_ENTER)));
    }

    /**
     * 5.2.4.2 点击播放电台节目
     */
    public static void reportUserSubscribeItemClick(Album album) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumItemEvent(USER_CENTER_ITEM_SUBSCRIBE_LIST_ITEM_CLICK, AlbumConverts.convert2Report(album))));
    }

    /**
     * 5.2.4.3 退出订阅电台节目
     */
    public static void reportUserSubscribeExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_SUBSCRIBE_EXIT)));
    }

    /**
     * 5.2.4.4
     */
    public static void reportUserSubscribeUnSubscribe(Album album) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumItemEvent(USER_CENTER_ITEM_SUBSCRIBE_LIST_ITEM_UNSUBSCRIBE, AlbumConverts.convert2Report(album))));
    }


    public static void reportUserHistoryEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_HISTORY_ENTER)));
    }

    public static void reportUserHistoryExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_HISTORY_EXIT)));
    }

    public static void reportUserHistoryMusicEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_HISTORY_MUSIC_ENTER)));
    }

    public static void reportUserHistoryMusicItemClick(AudioV5 audio) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(USER_CENTER_ITEM_HISTORY_MUSIC_LIST_ITEM_CLICK, AudioConverts.convert2Report(audio))));
    }

    public static void reportUserHistoryMusicDelete(List<? extends AudioV5> audioList) {
        AppLogic.runOnBackGround(() -> doReport(new AudioListEvent(USER_CENTER_ITEM_HISTORY_MUSIC_DELETE, AudioConverts.convert2List(audioList, AudioConverts::convert2Report))));
    }

    public static void reportUserHistoryMusicExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_HISTORY_MUSIC_EXIT)));
    }

    public static void reportUserHistoryRadioEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_HISTORY_RADIO_ENTER)));
    }

    public static void reportUserHistoryRadioItemClick(Album album) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumItemEvent(USER_CENTER_ITEM_HISTORY_RADIO_LIST_ITEM_CLICK, AlbumConverts.convert2Report(album))));
    }

    public static void reportUserHistoryRadioDelete(List<? extends Album> albumList) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumListEvent(USER_CENTER_ITEM_HISTORY_RADIO_DELETE, AlbumConverts.convert2List(albumList, AlbumConverts::convert2Report))));
    }

    public static void reportUserHistoryRadioExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(USER_CENTER_ITEM_HISTORY_RADIO_EXIT)));
    }


    /**
     * 5.2.6.1 进入微信推送
     */
    public static void reportUserWxEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(WX_PUSH_ENTER)));
    }

    /**
     * 5.2.6.2 删除管理
     */
    public static void reportUserWxDelete(List<? extends AudioV5> audioList) {
        AppLogic.runOnBackGround(() -> doReport(new AudioListEvent(WX_PUSH_DELETE, AudioConverts.convert2List(audioList, AudioConverts::convert2Report))));
    }

    /**
     * 5.2.6.3 点击音频
     */
    public static void reportUserWxListClick(AudioV5 audio) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(WX_PUSH_LIST_ITEM_CLICK, AudioConverts.convert2Report(audio))));
    }

    /**
     * 5.2.6.4 退出微信推送
     */
    public static void reportUserWxExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(WX_PUSH_EXIT)));
    }

    /**
     * 5.2.7.1 进入设置
     */
    public static void reportSettingsEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(SETTINGS_ENTER)));
    }

    /**
     * 5.2.7.2 智能点火播放
     */
    public static void reportSettingsBootPlay(boolean enable) {
        AppLogic.runOnBackGround(() -> doReport(new OpEvent(SETTINGS_AI_PLAY, enable ? OpEvent.OP_TYPE_ENABLE : OpEvent.OP_TYPE_DISABLE)));
    }

    /**
     * 5.2.7.4 免唤醒指令
     */
    public static void reportWakeupCmd(boolean enable) {
        AppLogic.runOnBackGround(() -> doReport(new OpEvent(SETTINGS_WAKEUP_CMD, enable ? OpEvent.OP_TYPE_ENABLE : OpEvent.OP_TYPE_DISABLE)));
    }

    /**
     * 5.2.7.5 清除缓存
     */
    public static void reportClearCache(long cacheSize) {
        AppLogic.runOnBackGround(() -> doReport(new CacheEvent(SETTINGS_CLEAR_CACHE, cacheSize)));
    }

    /**
     * 5.2.7.6 退出设置
     */
    public static void reportSettingsExit() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(SETTINGS_EXIT)));
    }

    /**
     * 5.3.1 查看榜单内容
     */
    public static void reportPayListingsClick(@BillBoardClickEvent.ClickPos int clickPos) {
        AppLogic.runOnBackGround(() -> doReport(new BillBoardClickEvent(PAY_LISTINGS_CLICK, clickPos)));
    }

    /**
     * 5.3.2 播放榜单内容
     */
    public static void reportPayListingsContentClick(Album album, @BillBoardContentClickEvent.ClickPos int clickPos) {
        AppLogic.runOnBackGround(() -> doReport(new BillBoardContentClickEvent(PAY_LISTINGS_LIST_ITEM_CLICK, AlbumConverts.convert2Report(album), clickPos)));
    }

    /**
     * 5.4 常听的音乐标签
     */
    public static void reportUserMusicPlay(Album album) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumItemEvent(OFTEN_LISTEN_MUSIC_CLICK, AlbumConverts.convert2Report(album))));
    }

    /**
     * 5.5 常听的电台节目
     */
    public static void reportUserRadioPlay(Album album) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumItemEvent(OFTEN_LISTEN_RADIO_CLICK, AlbumConverts.convert2Report(album))));
    }

    /**
     * 5.6 每日推荐20首
     */
    public static void reportDailyClick() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(DAILY_RECOMMEND_CLICK)));
    }

    /**
     * 5.7 AI电台
     */
    public static void reportAiRadioClick() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(AI_RADIO_CLICK)));
    }

    /**
     * 6.2.2 换一批
     */
    public static void reportMusicChoiceSwitch() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(MUSIC_RECOMMEND_TAB_SWITCH)));
    }

    /**
     * 6.3.2 换一批
     */
    public static void reportMusicCategorySwitch() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(MUSIC_CATEGORY_TAB_SWITCH)));
    }

    /**
     * 7.2.3.1 查看必听榜内容
     */
    public static void reportNeceListingsClick(@BillBoardClickEvent.ClickPos int clickPos) {
        AppLogic.runOnBackGround(() -> doReport(new BillBoardClickEvent(NECE_LISTEN_LISTINGS_CLICK, clickPos)));
    }

    /**
     * 7.2.3.2 播放榜单内容
     */
    public static void reportNeceListingsContentClick(Album album, @BillBoardContentClickEvent.ClickPos int clickPos) {
        AppLogic.runOnBackGround(() -> doReport(new BillBoardContentClickEvent(NECE_LISTEN_LISTINGS_LIST_ITEM_CLICK, AlbumConverts.convert2Report(album), clickPos)));
    }

    /**
     * 7.3.1.1 进入亲子
     */
    public static void reportCategoryPcEnter(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_PC_ENTER, categoryId)));
    }

    /**
     * 7.3.1.2 退出亲子
     */
    public static void reportCategoryPcExit(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_PC_EXIT, categoryId)));
    }

    /**
     * 7.3.1.3 亲子二级目录选择专辑
     */
    public static void reportCategoryPcListItemClick(Album album, long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumWithParentItemEvent(RADIO_CATEGORY_PC_LIST_ITEM_CLICK, categoryId, AlbumConverts.convert2Report(album))));
    }

    /**
     * 7.3.1.4 亲子中点击更多
     */
    public static void reportCategoryPcMoreClick(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_PC_MORE_CLICK, categoryId)));
    }

    /**
     * 7.3.2.1 进入有声书
     */
    public static void reportCategoryAudioBookEnter(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_AUDIO_BOOK_ENTER, categoryId)));
    }

    /**
     * 7.3.2.2 退出有声书
     */
    public static void reportCategoryAudioBookExit(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_AUDIO_BOOK_EXIT, categoryId)));
    }

    /**
     * 7.3.2.3 亲子二级目录选择专辑
     */
    public static void reportCategoryAudioBookListItemClick(Album album, long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumWithParentItemEvent(RADIO_CATEGORY_AUDIO_BOOK_LIST_ITEM_CLICK, categoryId, AlbumConverts.convert2Report(album))));
    }

    /**
     * 7.3.2.4 有声书点击更多
     */
    public static void reportCategoryAudioBookMoreClick(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_AUDIO_BOOK_MORE_CLICK, categoryId)));
    }

    /**
     * 7.3.3.1 进入其他分类
     */
    public static void reportCategoryOtherEnter(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_OTHER_ENTER, categoryId)));
    }

    /**
     * 7.3.3.2 退出其他分类
     */
    public static void reportCategoryOtherExit(long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new CategoryItemEvent(RADIO_CATEGORY_OTHER_EXIT, categoryId)));
    }

    /**
     * 7.3.3.3 其他分类专辑选择
     */
    public static void reportCategoryOtherListItemClick(Album album, long categoryId) {
        AppLogic.runOnBackGround(() -> doReport(new AlbumWithParentItemEvent(RADIO_CATEGORY_OTHER_LIST_ITEM_CLICK, categoryId, AlbumConverts.convert2Report(album))));
    }

    /**
     * 8.2 进入播放页
     */
    public static void reportPlayerEnter() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(PLAYER_ENTER)));
    }

    /**
     * 8.3 收藏/取消收藏
     */
    public static void reportPlayerFavour(AudioV5 audio, boolean isFavour) {
        AppLogic.runOnBackGround(() -> doReport(new FavourEvent(PLAYER_FAVOUR_OR_UNFAVOUR, AudioConverts.convert2Report(audio), isFavour ? FavourEvent.OP_TYPE_FAVOUR : FavourEvent.OP_TYPE_UN_FAVOUR)));
    }

    /**
     * 8.4 订阅/取消订阅专辑
     */
    public static void reportPlayerSubscribe(Album album, boolean isSubscribe) {
        AppLogic.runOnBackGround(() -> doReport(new SubscribeEvent(PLAYER_SUBSCRIBE_OR_UNSUBSCRIBE, AlbumConverts.convert2Report(album), isSubscribe ? SubscribeEvent.OP_TYPE_SUBSCRIBE : SubscribeEvent.OP_TYPE_UN_SUBSCRIBE)));
    }

    /**
     * 8.5 上一首/下一首
     */
    public static void reportPlayerAudioSwitch(long mediaLength, long playLength, AudioV5 audio, boolean isNext) {
        AppLogic.runOnBackGround(() -> {
            AudioSwitchEvent event = new AudioSwitchEvent(isNext ? COMM_PLAYER_NEXT : COMM_PLAYER_PREV, AudioConverts.convert2Report(audio), isNext ? AudioSwitchEvent.OP_TYPE_NEXT : AudioSwitchEvent.OP_TYPE_PREV);
            event.mediaLength = mediaLength;
            event.playLength = playLength;
            doReport(event);
        });
    }

    /**
     * 8.6 进度条操作
     */
    public static void reportPlayerSeekTo(AudioV5 audio, long spos, long epos, long mediaLength) {
        AppLogic.runOnBackGround(() -> {
            SeekEvent seekEvent = new SeekEvent(PLAYER_SEEK_TO, AudioConverts.convert2Report(audio), spos, epos, mediaLength);
            doReport(seekEvent);
        });
    }

    /**
     * 8.7 查看播放列表
     */
    public static void reportPlayListClick() {
        AppLogic.runOnBackGround(() -> doReport(new BaseEvent(PLAYER_PLAY_LIST_CLICK)));
    }

    /**
     * 8.8 切换播放模式
     */
    public static void reportPlayModeClick(@OpPlayModeEvent.OpType int opType) {
        AppLogic.runOnBackGround(() -> doReport(new OpPlayModeEvent(PLAYER_PLAY_LIST_PLAY_MODE_CLICK, opType)));
    }

    /**
     * 8.9 查看歌词
     */
    public static void reportPlayLrcClick(AudioV5 audio) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(PLAYER_PLAY_LRC_CLICK, AudioConverts.convert2Report(audio))));
    }

    /**
     * 8.11 AI模式下删除歌曲
     */
    public static void reportAiModeDelete(AudioV5 audio) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(PLAYER_AI_MODE_DELETE, AudioConverts.convert2Report(audio))));
    }

    /**
     * 8.11 音频开始播放
     */
    public static void reportAudioPlayStart(AudioV5 audioV5) {
        AppLogic.runOnBackGround(() -> {
            AudioItemEvent infoEvent = new AudioItemEvent(PLAYER_PLAY_START, AudioConverts.convert2Report(audioV5));
            doReport(infoEvent);
        });
    }

    /**
     * 8.12 音频结束播放
     */
    public static void reportAudioPlayEnd(AudioV5 audioV5, @PlayInfoEvent.ManualType int manual, @PlayInfoEvent.OnlineType int online, long mediaLen, long playLen, @PlayInfoEvent.ExitType int exitType, boolean isError) {
        AppLogic.runOnBackGround(() -> {
            int eventId;
            if (isError) {
                eventId = PLAYER_PLAY_COMPLETION_ERROR;
            } else {
                eventId = PLAYER_PLAY_COMPLETION;
            }
            PlayInfoEvent infoEvent = new PlayInfoEvent(eventId, AudioConverts.convert2Report(audioV5), manual, online, mediaLen, playLen, exitType);
            doReport(infoEvent);
        });
    }

    /**
     * 8.12 音频暂停播放
     */
    public static void reportAudioPlayPause(AudioV5 audioV5, @PlayInfoEvent.ManualType int manual, @PlayInfoEvent.OnlineType int online, long mediaLen, long playLen, @PlayInfoEvent.ExitType int exitType) {
        AppLogic.runOnBackGround(() -> {
            PlayInfoEvent infoEvent = new PlayInfoEvent(PLAYER_PLAY_PAUSE, AudioConverts.convert2Report(audioV5), manual, online, mediaLen, playLen, exitType);
            doReport(infoEvent);
        });
    }

    // 通用的收藏和取消收藏
    public static void reportGlobalFavourOrUnFavour(AudioV5 audio, boolean isFavour) {
        AppLogic.runOnBackGround(() -> doReport(new FavourEvent(COMM_FAVOUR_OR_UNFAVOUR, AudioConverts.convert2Report(audio), isFavour ? FavourEvent.OP_TYPE_FAVOUR : FavourEvent.OP_TYPE_UN_FAVOUR)));
    }

    // 通用的订阅和取消订阅
    public static void reportGlobalSubscribeOrUnSubscribe(Album album, boolean isSubscribe) {
        AppLogic.runOnBackGround(() -> doReport(new SubscribeEvent(COMM_SUBSCRIBE_OR_UNSUBSCRIBE, AlbumConverts.convert2Report(album), isSubscribe ? SubscribeEvent.OP_TYPE_SUBSCRIBE : SubscribeEvent.OP_TYPE_UN_SUBSCRIBE)));
    }

    /**
     * 播放列表元素被点击
     */
    public static void reportPlayListItemClick(AudioV5 audio) {
        AppLogic.runOnBackGround(() -> doReport(new AudioItemEvent(PLAYER_PLAY_LIST_ITEM_CLICK, AudioConverts.convert2Report(audio))));
    }


    private static void doReport(BaseEvent event) {
        event.seqId = seqId++;
        event.version = calVersion();
        event.tmStamp = TimeManager.getInstance().getTimeMillis();
        ReportManager.getInstance().reportImmediate(event);
    }

    /**
     * x.y.z版本的计算为：x * 10000 + y*100 + z,
     */
    private static int calVersion() {
        try {
            String verName = BuildConfig.VERSION_NAME;
            String[] split = verName.split("\\.");
            int x = Integer.parseInt(split[0]);
            int y = Integer.parseInt(split[1]);
            int z = Integer.parseInt(split[2]);
            return x * 10000 + y * 100 + z;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 50000;
    }
}
