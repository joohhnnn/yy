package com.txznet.music.report;

import android.text.TextUtils;

import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.report.bean.AddInterestTag;
import com.txznet.music.report.bean.ClickAlbum;
import com.txznet.music.report.bean.ClickAudio;
import com.txznet.music.report.bean.ClickAudioOrAlbum;
import com.txznet.music.report.bean.ClickBtn;
import com.txznet.music.report.bean.ClickCarFM;
import com.txznet.music.report.bean.ClickMain;
import com.txznet.music.report.bean.ClickMusicAlbumCategory;
import com.txznet.music.report.bean.ClickMusicCategory;
import com.txznet.music.report.bean.ClickOpenApp;
import com.txznet.music.report.bean.ClickPersonalizedSkin;
import com.txznet.music.report.bean.ClickScribeDelete;
import com.txznet.music.report.bean.ClickThemePlaylist;
import com.txznet.music.report.bean.ClickTimeOfTheme;
import com.txznet.music.report.bean.DragProgressBean;
import com.txznet.music.report.bean.EventBase;
import com.txznet.music.report.bean.ExitEvent;
import com.txznet.music.report.bean.LocalDelete;
import com.txznet.music.report.bean.LocalScan;
import com.txznet.music.report.bean.PlayEvent;
import com.txznet.music.report.bean.PushEvent;
import com.txznet.music.report.bean.SearchEvent;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/12.
 */

public class ReportEvent implements ReportEventConst {
    public static final int TYPE_MANUAL = 1;
    public static final int TYPE_SOUND = 2;
    public static final int TYPE_SOUND_ASR = 3;
    public static final int TYPE_AUTO = 4;
    private static final long PAUSEMAXTIME = 10 * 1000 * 60;
    private static long s_pauseStartTime = Long.MAX_VALUE;

    /**
     * 记录暂停开始的时间
     */
    public static void updatePauseStartTime(long pauseStartTime) {
        s_pauseStartTime = pauseStartTime;
    }

    /**
     * 获取暂停的时间
     */
    public static long getPauseTime() {
        if (TimeManager.getInstance().getTimeMillis() - s_pauseStartTime < PAUSEMAXTIME) {
            return 0;
        } else {
            return TimeManager.getInstance().getTimeMillis() - s_pauseStartTime;
        }
    }


    private static void clickBtn(String eventId) {
        EventBase eventBase = new ClickAudio(eventId);

        ReportManager.getInstance().report(eventBase);
    }
    //////////////////////////////////////////////

    /**
     * @param type   类别,手动还是声控
     * @param toPage 落地页
     */
    public static void clickOpenActivity(int type, int toPage) {
        ClickOpenApp clickOpenApp = new ClickOpenApp(CLICK_OPEN_ACTIVITY);
        clickOpenApp.toPage = toPage;
        clickOpenApp.type = type;
        ReportManager.getInstance().report(clickOpenApp);
    }


    //////////////////////////////////////////////

    /**
     * 点击一级分类主分类
     */
    public static void clickMainMenu(int type) {
        ClickMain clickMain = new ClickMain(CLICKMAINMENU);
        clickMain.type = type;

        ReportManager.getInstance().report(clickMain);
    }

/////////////////////////////////////////

    /**
     * 点击音乐分类
     *
     * @param categoryId
     */
    public static void clickMusicCategory(long categoryId) {
        ClickMusicCategory clickMusicCategory = new ClickMusicCategory(CLICKMUSICCATEGORY);
        clickMusicCategory.categoryId = categoryId;
        ReportManager.getInstance().report(clickMusicCategory);
    }

    /**
     * 点击音乐专辑播放按钮
     *
     * @param album
     */
    public static void clickMusicAlbumPlay(Album album, int position) {
        reportClickMusicAlbum(CLICKMUSICALBUM, album, position, 1);
    }

    private static void reportClickMusicAlbum(String eventId, Album album, int position, int type) {
        if (album == null) {
            return;
        }
        ClickMusicAlbumCategory clickMusicAlbumCategory = new ClickMusicAlbumCategory(eventId);
        clickMusicAlbumCategory.albumId = album.getId();
        clickMusicAlbumCategory.sid = album.getSid();
        clickMusicAlbumCategory.position = position;
        clickMusicAlbumCategory.type = type;

        ReportManager.getInstance().report(clickMusicAlbumCategory);
    }


    /**
     * 点击音乐专辑图标
     *
     * @param album
     * @param position
     * @param exposureIds 曝光的id列表
     */
    public static void clickMusicAlbumIcon(Album album, int position, List<Integer> exposureIds) {
        reportClickMusicAlbum(CLICKMUSICALBUM, album, position, 2);
    }
///////////////////////////////////////////////////////

    /**
     * 点击电台主分类
     *
     * @param categoryId
     */
    public static void clickRadioCategory(long categoryId) {
        ClickMusicCategory clickMusicCategory = new ClickMusicCategory(CLICKRADIOCATEGORY);
        clickMusicCategory.categoryId = categoryId;
        ReportManager.getInstance().report(clickMusicCategory);
    }

    /**
     * 点击小说这样的分类
     *
     * @param categoryId
     */
    public static void clickRadioAlbumCategory(long categoryId) {
        ClickMusicCategory clickMusicCategory = new ClickMusicCategory(CLICKRADIOALBUMCATEGORY);
        clickMusicCategory.categoryId = categoryId;
        ReportManager.getInstance().report(clickMusicCategory);
    }

    /**
     * 点击音乐专辑图标,进入但是不播放
     *
     * @param album
     * @param position
     * @param exposureIds 曝光的id列表
     */
    public static void clickRadioAlbumIcon(Album album, int position, List<Integer> exposureIds) {
        reportClickMusicAlbum(CLICKRADIOALBUM, album, position, 1);
    }

    /**
     * 点击音乐专辑图标,直接播放
     *
     * @param album
     * @param position
     * @param exposureIds 曝光的id列表
     */
    public static void clickRadioAlbumIconPlay(Album album, int position, List<Integer> exposureIds) {
        reportClickMusicAlbum(CLICKRADIOALBUM, album, position, 2);
    }
/////////////////////////////////////////////

    /**
     * 删除本地
     */
    public static void clickLocalDelete(Audio audio, String name) {
        clickAudio(CLICKLOCALDELETE, audio, 0);
    }

    public static void clickLocalFavour(Audio audio, String name) {
        clickAudio(CLICKLOCALFAVOURBTN, audio, 1);
    }

    private static void clickAudio(String eventId, Audio audio, int type) {
        if (audio == null) {
            return;
        }
        LocalDelete localDelete = new LocalDelete(eventId);
        localDelete.name = audio.getName();
        localDelete.sid = audio.getSid();
        localDelete.audioId = audio.getId();
        localDelete.type = type;//表示收藏
        localDelete.isInsert = audio.getIsInsert();
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickLocalUnFavour(Audio audio, String name) {
        clickAudio(CLICKLOCALFAVOURBTN, audio, 2);
    }

    /**
     * 开始扫描的时候本地列表的数量
     *
     * @param count
     */
    public static void clickScanBegin(int count) {
        LocalScan localScan = new LocalScan(LOCALSCANBEGIN);
        localScan.count = count;
        ReportManager.getInstance().report(localScan);
    }

    /**
     * 手动点击停止扫描的时候本地列表的数量
     *
     * @param count
     */
    public static void clickScanInterrupt(int count) {
        LocalScan localScan = new LocalScan(CLICKSCANINTERRUPT);
        localScan.count = count;
        ReportManager.getInstance().report(localScan);
    }

    /**
     * 手动点击停止扫描的时候本地列表的数量
     *
     * @param count
     */
    public static void clickScanEnd(int count) {
        LocalScan localScan = new LocalScan(CLICKSCANEND);
        localScan.count = count;
        ReportManager.getInstance().report(localScan);
    }

    private static void clickAudio(String eventId, Audio audio, String name) {
        clickAudio(eventId, audio, 0);
    }

    public static void clickLocalAudio(Audio audio, String name) {
        clickAudio(CLICKLOCALAUDIO, audio, name);
    }
//////////////////////////////////////////////我的

    /**
     * 点击我的界面的哪一个按钮
     *
     * @param type 收藏的音乐(1)
     *             订阅的电台(2)
     *             历史(3)
     *             消息(4)
     *             设置(5)
     */
    public static void clickMineBtn(int type) {
        ClickBtn clickBtn = new ClickBtn(CLICKMINEBTN);
        clickBtn.type = type;
        ReportManager.getInstance().report(clickBtn);
    }


    /**
     * 我的模块->收藏的音乐,点击删除按钮
     */
    public static void clickMineFavourDelete(Audio audio, String name) {
        clickAudio(CLICKMINEFAVOURDELETE, audio, 0);
    }


    public static void clickMineFavourAudio(Audio audio, String name) {
        clickAudio(CLICKMINEFAVOURAUDIO, audio, name);
    }

    public static void clickMineFavourBack() {
        clickBtn(CLICKMINEFAVOUR_BACK);
    }

    ///////////////////////////////////////

    private static void clickAlbum(String eventId, Album album, int type) {
        if (album == null) {
            return;
        }
        ClickAlbum clickAlbum = new ClickAlbum(eventId);
        clickAlbum.albumId = album.getId();
        clickAlbum.sid = album.getSid();
        clickAlbum.type = type;
        ReportManager.getInstance().report(clickAlbum);
    }

    public static void clickMineSubscribeDelete(List<Album> albums) {
        ClickScribeDelete clickScribeDelete = new ClickScribeDelete(CLICKSUBSCRIBEUN);
        clickScribeDelete.albums = albums;
        ReportManager.getInstance().report(clickScribeDelete);
    }

    public static void clickMineSubscribePlay(Album album) {
        clickAlbum(CLICKSUBSCRIBEPLAY, album, 1);
    }

    public static void clickMineSubscribeIcon(Album album) {
        clickAlbum(CLICKSUBSCRIBEPLAY, album, 2);
    }

    public static void clickMineSubscribeBack() {
        clickBtn(CLICKSUBSCRIBEBACK);
    }
    //////////////////////////////// 历史音乐界面

    public static void clickHistoryMusicBack() {
        clickBtn(CLICKHISTORYMUSICBACK);
    }

    public static void clickHistoryMusicDelete(Audio audio, String name) {
        clickAudio(CLICKHISTORYMUSICDELETE, audio, 0);
    }

    public static void clickHistoryMusicFavour(Audio audio, String name) {
        clickAudio(CLICKHISTORYMUSICFAVOUR, audio, 1);
    }

    public static void clickHistoryMusicUnfavour(Audio audio, String name) {
        clickAudio(CLICKHISTORYMUSICFAVOUR, audio, 2);
    }

    public static void clickHistoryMusicPlay(Audio audio, String name) {
        clickAudio(CLICKHISTORYMUSICPLAY, audio, 2);
    }


    /////////////////////////// 历史电台

    public static void clickHistoryRadioBack() {
        clickBtn(CLICKHISTORYRADIOBACK);
    }


    public static void clickHistoryRadioDelete(Album album, String name) {
        clickAlbum(CLICKHISTORYRADIODELETE, album, 0);
    }

    public static void clickHistoryRadioSubscribe(Album album, String name) {
        clickAlbum(CLICKHISTORYRADIOSUBSCRIBE, album, 1);
    }

    public static void clickHistoryRadioUnSubscribe(Album album, String name) {
        clickAlbum(CLICKHISTORYRADIOSUBSCRIBE, album, 2);
    }

    public static void clickHistoryRadioPlay(Album album, String name) {
        clickAlbum(CLICKHISTORYRADIOPLAY, album, 0);
    }

/////////////////////////////////////消息界面,
    ///////////按照产品的意思,只是上报点击的行为,而不用带上数据,即产品只需要了解,是否有人点击了什么按钮,至于点击哪一个都不关心的!!!!表示深深的怀疑

    /**
     * 点击了播放按钮
     */
    public static void clickMessagePagePlay() {
        ClickBtn clickBtn = new ClickBtn(CLICKMESSAGEPLAY);
        clickBtn.type = 1;
        ReportManager.getInstance().report(clickBtn);
    }

    /**
     * 通过弹框点击了播放按钮
     */
    public static void clickMessagePagePlayPop() {
        ClickBtn clickBtn = new ClickBtn(CLICKMESSAGEPLAY);
        clickBtn.type = 2;
        ReportManager.getInstance().report(clickBtn);
    }

    /**
     * 点击了删除按钮
     */
    public static void clickMessagePageDeleteAll() {
        ClickBtn clickBtn = new ClickBtn(CLICKMESSAGEDELETE);
        clickBtn.type = 2;
        ReportManager.getInstance().report(clickBtn);
    }

    /**
     * 点击了删除按钮
     */
    public static void clickMessagePageDeleteOne() {
        ClickBtn clickBtn = new ClickBtn(CLICKMESSAGEDELETE);
        clickBtn.type = 1;
        ReportManager.getInstance().report(clickBtn);
    }

    /**
     * 点击了返回按钮
     */
    public static void clickMessagePageBack() {
        clickBtn(CLICKMESSAGEBACK);
    }
    ///////////////////////////消息界面

    public static void clickSettingPageBack() {
        clickBtn(CLICKSETTINGBACK);
    }

    public static void clickSettingPageWakeUp(int open) {
        ClickBtn clickBtn = new ClickBtn(CLICKSETTINGWAKEUP);
        clickBtn.type = open;
        ReportManager.getInstance().report(clickBtn);
    }

    public static void clickSettingPagePush(int open) {
        ClickBtn clickBtn = new ClickBtn(CLICKSETTINGPUSH);
        clickBtn.type = open;
        ReportManager.getInstance().report(clickBtn);
    }

    public static void clickSettingSkin(int open) {
        ClickPersonalizedSkin clickBtn = new ClickPersonalizedSkin(CLICK_PERSONALIZED_SKIN);
        clickBtn.open = open;
        ReportManager.getInstance().report(clickBtn);
    }

    public static void clickSettingPageHelp() {
        clickBtn(CLICKSETTINGHELP);
    }

    public static void clickSettingPageAboutUs() {
        clickBtn(CLICKSETTINGABOUTUS);
    }


    ////////////////////////////////红点上报
//    CLICKREDDOT

    public static void showReddot(int open) {
        ClickBtn clickBtn = new ClickBtn(CLICKREDDOT);
        clickBtn.type = open;
        ReportManager.getInstance().report(clickBtn);
    }

    ///////////////////////常驻播放栏
//    CLICKBARFAVOUR
    public static void clickBarMusicFavour(Audio audio) {
        clickAudio(CLICKBARFAVOUR, audio, 1);
    }

    public static void clickBarMusicUnFavour(Audio audio) {
        clickAudio(CLICKBARFAVOUR, audio, 2);
    }


    public static void clickBarRadioSubscribe(Album album) {
        clickAlbum(CLICKBARSUBSCRIBE, album, 1);
    }

    public static void clickBarRadioUnSubscribe(Album album) {
        clickAlbum(CLICKBARSUBSCRIBE, album, 2);
    }

    private static void clickBarBtn(String eventId, Audio audio, Album album, int type) {
        ClickAudioOrAlbum clickAudioOrAlbum = new ClickAudioOrAlbum(CLICKBARPLAYORPAUSE);
        if (audio != null) {
            clickAudioOrAlbum.audioId = audio.getId();
            clickAudioOrAlbum.audioSid = audio.getSid();
            clickAudioOrAlbum.name = audio.getName();
        }
        if (album != null) {
            clickAudioOrAlbum.albumId = album.getId();
            clickAudioOrAlbum.albumSid = album.getpSid();
        }
        clickAudioOrAlbum.type = type;
        ReportManager.getInstance().report(clickAudioOrAlbum);
    }


    public static void clickBarPlay(Audio audio, Album album) {
        clickBarBtn(CLICKBARPLAYORPAUSE, audio, album, 1);
    }

    public static void clickBarPause(Audio audio, Album album) {
        clickBarBtn(CLICKBARPLAYORPAUSE, audio, album, 2);
    }

    /**
     * 拖动进度条 单位s
     *
     * @param startTime
     * @param stopTime
     */
    public static void clickBarDragProgress(long startTime, long stopTime) {
        DragProgressBean dragProgressBean = new DragProgressBean(CLICKBARPROGRESS);
        dragProgressBean.startTime = startTime;
        dragProgressBean.stopTime = stopTime;
        ReportManager.getInstance().report(dragProgressBean);
    }

    public static void clickBarPlaylistBtn() {
        clickBtn(CLICKBARPLAYLISTBTN);
    }

    public static void clickBarPlaylistItemBtn(Audio audio, String name) {
        clickAudio(CLICKBARPLAYLISTITEM, audio, name);
    }

    public static void clickBarCoverBtn() {
        clickBtn(CLICKBARCOVER);
    }

    public static void clickBarPrevBtn(Audio audio, Album album) {
        PlayEvent playEvent = new PlayEvent(CLICKBARPREV, "", audio, album);
        ReportManager.getInstance().report(playEvent);
    }

    public static void clickBarNextBtn(Audio audio, Album album) {
        PlayEvent playEvent = new PlayEvent(CLICKBARNEXT, "", audio, album);
        ReportManager.getInstance().report(playEvent);
    }

    /**
     * 顺序(0)
     * 单曲(1)
     * 随机(2)
     *
     * @param mode
     */
    public static void clickBarMode(int mode) {
        ClickBtn clickBtn = new ClickBtn(CLICKBARMODE);
        clickBtn.type = mode;
        ReportManager.getInstance().report(clickBtn);
    }

    ///////////////////////播放界面

    public static void enterPlayerPage() {
        clickBtn(ENTER_PLAYER_PAGE);
    }

    public static void clickPlayerPageMusicFavour(Audio audio) {
        clickAudio(CLICK_PLAYER_FAVOUR, audio, 1);
    }

    public static void clickPlayerPageMusicUnFavour(Audio audio) {
        clickAudio(CLICK_PLAYER_FAVOUR, audio, 2);
    }


    public static void clickPlayerPageRadioSubscribe(Album album) {
        clickAlbum(CLICK_PLAYER_SUBSCRIBE, album, 1);

    }

    public static void clickPlayerPageRadioUnSubscribe(Album album) {
        clickAlbum(CLICK_PLAYER_SUBSCRIBE, album, 2);
    }

    public static void clickPlayerPagePlay() {
        ClickBtn clickBtn = new ClickBtn(CLICK_PLAYER_PLAYORPAUSE);
        clickBtn.type = 1;
        ReportManager.getInstance().report(clickBtn);
    }

    public static void clickPlayerPagePause() {

        ClickBtn clickBtn = new ClickBtn(CLICK_PLAYER_PLAYORPAUSE);
        clickBtn.type = 2;
        ReportManager.getInstance().report(clickBtn);

    }

    /**
     * 拖动进度条 单位s
     *
     * @param startTime
     * @param stopTime
     */
    public static void clickPlayerPageDragProgress(long startTime, long stopTime) {
        DragProgressBean dragProgressBean = new DragProgressBean(CLICK_PLAYER_PROGRESS);
        dragProgressBean.startTime = startTime;
        dragProgressBean.stopTime = stopTime;
        ReportManager.getInstance().report(dragProgressBean);
    }


    public static void clickPlayerPagePlaylistItemBtn(Audio audio, String name) {
        clickAudio(CLICK_PLAYER_PLAYLIST_ITEM, audio, name);
    }

    public static void clickPlayerPageCoverBtn() {
        clickBtn(CLICK_PLAYER_COVER);
    }

    public static void clickPlayerPagePrevBtn() {
        clickBtn(CLICK_PLAYER_PREV);
    }

    public static void clickPlayerPageNextBtn() {
        clickBtn(CLICK_PLAYER_NEXT);
    }

    /**
     * 顺序(0)
     * 单曲(1)
     * 随机(2)
     *
     * @param mode
     */
    public static void clickPlayerPageMode(int mode) {
        ClickBtn clickBtn = new ClickBtn(CLICK_PLAYER_MODE);
        clickBtn.type = mode;
        ReportManager.getInstance().report(clickBtn);
    }

    public static void exitPlayerPage() {
        clickBtn(EXIT_PLAYER_PAGE);
    }

    /////////////////////同听30s


    /**
     * 上报推送事件
     *
     * @param action action
     * @param type   type
     * @param id     id
     */
    public static void reportPushEvent(String action, String type, String id) {
        PushEvent pushEvent = new PushEvent(action, type, id);
        if (TextUtils.equals(action, PushEvent.ACTION_SHOW)) {
            ReportManager.getInstance().reportImmediate(pushEvent);
        } else {
            ReportManager.getInstance().report(pushEvent);
        }
    }


    public static void reportExitEvent(String action) {
        ExitEvent exitEvent = new ExitEvent(action);
        ReportManager.getInstance().report(exitEvent);
    }


    public static void reportPlayEvent(String action, Audio audio, Album album, long duration) {
        if (null == audio) {
            return;
        }
        PlayEvent playEvent = new PlayEvent(ReportEventConst.PLAY_EVENT_ID, action, audio, album);
        playEvent.duration = duration;
        ReportManager.getInstance().report(playEvent);
    }


    public static void reportCommEvent(String eventId) {
        EventBase eventBase = new EventBase(eventId);
        ReportManager.getInstance().report(eventBase);
    }

    /////////////////////////////搜索
    public static void reportClickSearchDataAuto(Audio audio, Album album, String searchJson) {
        reportClickSearchData(audio, album, searchJson, SearchEvent.CHOICE_AUTO);
    }

    public static void reportClickSearchDataUser(Audio audio, Album album, String searchJson) {
        reportClickSearchData(audio, album, searchJson, SearchEvent.CHOICE_USER);
    }

    private static void reportClickSearchData(Audio audio, Album album, String searchJson, int type) {
        SearchEvent searchEvent = new SearchEvent(EVENT_CLICK_SEARCH);
        if (null != audio) {
            searchEvent.audioId = audio.getId();
            searchEvent.audioSid = audio.getSid();
            searchEvent.name = audio.getName();
        }
        if (null != album) {
            searchEvent.albumId = album.getId();
            searchEvent.albumSid = album.getSid();
            searchEvent.name = album.getName();
        }
        searchEvent.type = type;
        searchEvent.json = searchJson;
        ReportManager.getInstance().report(searchEvent);
    }


    /**
     * DESC 上报兴趣标签 4.15
     *
     * @param tagsId 选中了的兴趣标签集合
     * @param show   1表示弹出
     * @param action 1表示提交成功，2表示跳过
     */
    public static void reportAddInterestTag(List<Integer> tagsId, String show, String action) {
        AddInterestTag addInterestTag = new AddInterestTag(tagsId, show, action);
        ReportManager.getInstance().report(addInterestTag);
    }

    /**
     * DESC 打开车主FM 4.16.1
     *
     * @param action 语音打开或者手动打开
     */
    public static void reportClickCarFM(String action) {
        ClickCarFM clickCarFM = new ClickCarFM(action);
        ReportManager.getInstance().report(clickCarFM);
    }

    /**
     * DESC 分时段主题4.16.2
     *
     * @param action  手动点击分时段主题手动点击分时段主题
     * @param themeId 分时段内容主题id
     */
    public static void reportClickTimeOfTheme(String action, String themeId) {
        ClickTimeOfTheme clickTimeOfTheme = new ClickTimeOfTheme(action, themeId);
        ReportManager.getInstance().report(clickTimeOfTheme);
    }

    /**
     * desc 分时段播放列表 （点击分时段音频，记录行为） 4.16.3
     */
    public static void reportClickThemePlayList() {
        ClickThemePlaylist clickThemePlaylist = new ClickThemePlaylist();
        ReportManager.getInstance().report(clickThemePlaylist);
    }
    //////////////////////////免唤醒上报
    // 播放 1 ，暂停 2，上一首 3，下一首 4，快进快进 5 ，后退后退 6 ，(加入收藏 ,加入订阅) 7 ，(取消收藏 ,取消订阅 )8

    /**
     * 免唤醒播放
     */
    public static void clickSoundPlay(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 1);
    }

    /**
     * 免唤醒暂停
     */
    public static void clickSoundPause(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 2);
    }

    /**
     * 免唤醒上一首
     */
    public static void clickSoundPrev(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 3);
    }

    /**
     * 免唤醒下一首
     */
    public static void clickSoundNext(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 4);
    }

    /**
     * 免唤醒快进快进
     */
    public static void clickSoundFastForward(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 5);
    }

    /**
     * 免唤醒后退后退
     */
    public static void clickSoundBackForward(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 6);
    }

    /**
     * 免唤醒加入收藏 ,加入订阅
     */
    public static void clickSoundAddFavour(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 7);
    }

    /**
     * 免唤醒取消收藏 ,取消订阅
     */
    public static void clickSoundUnFavour(Audio audio) {
        clickAudio(EVENT_CLICK_SOUND_COMMAND, audio, 8);
    }
    //////////////////////////免唤醒上报


    //////音频焦点变化
    public static void reportFocusChange(Audio audio, int type) {
        clickAudio(EVENT_CLICK_AUDIO_FOCUS_CHANGE, audio, type);
    }

    public static void reportFocusPlay(Audio audio) {
        clickAudio(EVENT_CLICK_AUDIO_FOCUS_PLAYER, audio, 1);
    }

    public static void reportFocusPause(Audio audio) {
        clickAudio(EVENT_CLICK_AUDIO_FOCUS_PLAYER, audio, 2);
    }

    //////音频焦点变化

}

