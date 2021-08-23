package com.txznet.music.report;

import android.text.TextUtils;

import com.txznet.music.Time.TimeManager;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.playerModule.logic.factory.PlayEngineFactory;
import com.txznet.music.report.bean.ClickAlbum;
import com.txznet.music.report.bean.ClickAudio;
import com.txznet.music.report.bean.ClickBtn;
import com.txznet.music.report.bean.ClickMain;
import com.txznet.music.report.bean.ClickMusicAlbumCategory;
import com.txznet.music.report.bean.ClickMusicCategory;
import com.txznet.music.report.bean.ClickOpenApp;
import com.txznet.music.report.bean.ClickScribeDelete;
import com.txznet.music.report.bean.DragProgressBean;
import com.txznet.music.report.bean.EventBase;
import com.txznet.music.report.bean.ExitEvent;
import com.txznet.music.report.bean.LocalDelete;
import com.txznet.music.report.bean.LocalScan;
import com.txznet.music.report.bean.PlayEvent;
import com.txznet.music.report.bean.PushEvent;

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
    private static long s_pauseStartTime;

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
        if (PlayEngineFactory.getEngine().isPlaying()) {
            s_pauseStartTime = 0;
            return 0;
        } else if (TimeManager.getInstance().getTimeMillis() - s_pauseStartTime < PAUSEMAXTIME) {
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
     * @param albumId
     */
    public static void clickMusicAlbumPlay(long albumId, int position) {
        ClickMusicAlbumCategory clickMusicAlbumCategory = new ClickMusicAlbumCategory(CLICKMUSICALBUM);
        clickMusicAlbumCategory.albumId = albumId;
        clickMusicAlbumCategory.position = position;
        clickMusicAlbumCategory.type = 1;

        ReportManager.getInstance().report(clickMusicAlbumCategory);
    }


    /**
     * 点击音乐专辑图标
     *
     * @param albumId
     * @param position
     * @param exposureIds 曝光的id列表
     */
    public static void clickMusicAlbumIcon(long albumId, int position, List<Integer> exposureIds) {
        ClickMusicAlbumCategory clickMusicAlbumCategory = new ClickMusicAlbumCategory(CLICKMUSICALBUM);
        clickMusicAlbumCategory.albumId = albumId;
        clickMusicAlbumCategory.position = position;
        clickMusicAlbumCategory.type = 2;
        clickMusicAlbumCategory.exposureIds = exposureIds;
        ReportManager.getInstance().report(clickMusicAlbumCategory);
    }
///////////////////////////////////////////////////////

    /**
     * 点击小说分类
     *
     * @param categoryId
     */
    public static void clickRadioCategory(long categoryId) {
        ClickMusicCategory clickMusicCategory = new ClickMusicCategory(CLICKRADIOCATEGORY);
        clickMusicCategory.categoryId = categoryId;
        ReportManager.getInstance().report(clickMusicCategory);
    }

    /**
     * 点击电台分类
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
     * @param albumId
     * @param position
     * @param exposureIds 曝光的id列表
     */
    public static void clickRadioAlbumIcon(long albumId, int position, List<Integer> exposureIds) {
        ClickMusicAlbumCategory clickMusicAlbumCategory = new ClickMusicAlbumCategory(CLICKRADIOALBUM);
        clickMusicAlbumCategory.albumId = albumId;
        clickMusicAlbumCategory.position = position;
        clickMusicAlbumCategory.type = 1;
        clickMusicAlbumCategory.exposureIds = exposureIds;

        ReportManager.getInstance().report(clickMusicAlbumCategory);
    }

    /**
     * 点击音乐专辑图标,直接播放
     *
     * @param albumId
     * @param position
     * @param exposureIds 曝光的id列表
     */
    public static void clickRadioAlbumIconPlay(long albumId, int position, List<Integer> exposureIds) {
        ClickMusicAlbumCategory clickMusicAlbumCategory = new ClickMusicAlbumCategory(CLICKRADIOALBUM);
        clickMusicAlbumCategory.albumId = albumId;
        clickMusicAlbumCategory.position = position;
        clickMusicAlbumCategory.type = 2;
        clickMusicAlbumCategory.exposureIds = exposureIds;
        ReportManager.getInstance().report(clickMusicAlbumCategory);
    }
/////////////////////////////////////////////

    /**
     * 删除本地
     */
    public static void clickLocalDelete(int sid, long audioId, String name) {
        LocalDelete localDelete = new LocalDelete(CLICKLOCALDELETE);
        localDelete.sid = sid;
        if (sid == Constant.LOCAL_MUSIC_TYPE) {
            localDelete.name = name;
        } else {
            localDelete.audioId = audioId;
        }
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickLocalFavour(int sid, long audioId, String name) {
        LocalDelete localDelete = new LocalDelete(CLICKLOCALFAVOURBTN);
        localDelete.name = name;
        localDelete.sid = sid;
        localDelete.audioId = audioId;
        localDelete.type = 1;//表示收藏
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickLocalUnFavour(int sid, long audioId, String name) {
        LocalDelete localDelete = new LocalDelete(CLICKLOCALFAVOURBTN);
        localDelete.name = name;
        localDelete.sid = sid;
        localDelete.audioId = audioId;
        localDelete.type = 2;//表示取消收藏
        ReportManager.getInstance().report(localDelete);
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

    private static void clickAudio(String eventId, int sid, long audioId, String name) {
        ClickAudio clickAudio = new ClickAudio(eventId);
        clickAudio.name = name;
        clickAudio.sid = sid;
        clickAudio.audioId = audioId;
        ReportManager.getInstance().report(clickAudio);
    }

    public static void clickLocalAudio(int sid, long id, String name) {
        clickAudio(CLICKLOCALAUDIO, sid, id, name);
    }
//////////////////////////////////////////////我的

    /**
     * 点击我的界面的哪一个按钮
     *
     * @param type 我的收藏(1)
     *             我的订阅(2)
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
     * 我的模块->我的收藏,点击删除按钮
     */
    public static void clickMineFavourDelete(int sid, long audioId, String name) {
        LocalDelete delete = new LocalDelete(CLICKMINEFAVOURDELETE);
        delete.audioId = audioId;
        delete.sid = sid;
        delete.name = name;
        ReportManager.getInstance().report(delete);
    }


    public static void clickMineFavourAudio(int sid, long audioId, String name) {
        clickAudio(CLICKMINEFAVOURAUDIO, sid, audioId, name);
    }

    public static void clickMineFavourBack() {
        clickBtn(CLICKMINEFAVOUR_BACK);
    }

    ///////////////////////////////////////
    public static void clickMineSubscribeDelete(List<Album> albums) {
        ClickScribeDelete clickScribeDelete = new ClickScribeDelete(CLICKSUBSCRIBEUN);
        clickScribeDelete.albums = albums;
        ReportManager.getInstance().report(clickScribeDelete);
    }

    public static void clickMineSubscribePlay(int sid, long albumId) {
        ClickAlbum clickAlbum = new ClickAlbum(CLICKSUBSCRIBEPLAY);
        clickAlbum.albumId = albumId;
        clickAlbum.sid = sid;
        clickAlbum.type = 1;
        ReportManager.getInstance().report(clickAlbum);
    }

    public static void clickMineSubscribeIcon(int sid, long albumId) {
        ClickAlbum clickAlbum = new ClickAlbum(CLICKSUBSCRIBEPLAY);
        clickAlbum.albumId = albumId;
        clickAlbum.sid = sid;
        clickAlbum.type = 2;
        ReportManager.getInstance().report(clickAlbum);
    }

    public static void clickMineSubscribeBack() {
        clickBtn(CLICKSUBSCRIBEBACK);
    }
    //////////////////////////////// 历史音乐界面

    public static void clickHistoryMusicBack() {
        clickBtn(CLICKHISTORYMUSICBACK);
    }

    public static void clickHistoryMusicDelete(int sid, long audioId, String name) {
        LocalDelete localDelete = new LocalDelete(CLICKHISTORYMUSICDELETE);
        localDelete.sid = sid;
        if (sid == Constant.LOCAL_MUSIC_TYPE) {
            localDelete.name = name;
        } else {
            localDelete.audioId = audioId;
        }
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickHistoryMusicFavour(int sid, long audioId, String name) {
        LocalDelete localDelete = new LocalDelete(CLICKHISTORYMUSICFAVOUR);
        localDelete.name = name;
        localDelete.sid = sid;
        localDelete.audioId = audioId;
        localDelete.type = 1;//表示收藏
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickHistoryMusicUnfavour(int sid, long audioId, String name) {
        LocalDelete localDelete = new LocalDelete(CLICKHISTORYMUSICFAVOUR);
        localDelete.name = name;
        localDelete.sid = sid;
        localDelete.audioId = audioId;
        localDelete.type = 2;//表示取消收藏
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickHistoryMusicPlay(int sid, long audioId, String name) {
        ClickAudio clickAudio = new ClickAudio(CLICKHISTORYMUSICPLAY);
        clickAudio.audioId = audioId;
        clickAudio.sid = sid;
        clickAudio.name = name;
        ReportManager.getInstance().report(clickAudio);
    }


    /////////////////////////// 历史电台

    public static void clickHistoryRadioBack() {
        clickBtn(CLICKHISTORYRADIOBACK);
    }

    public static void clickHistoryRadioDelete(int sid, long albumId, String name) {
        ClickAlbum clickAlbum = new ClickAlbum(CLICKHISTORYRADIODELETE);
        clickAlbum.albumId = albumId;
        clickAlbum.sid = sid;

        ReportManager.getInstance().report(clickAlbum);
    }

    public static void clickHistoryRadioSubscribe(int sid, long albumId, String name) {
        ClickAlbum clickAlbum = new ClickAlbum(CLICKHISTORYRADIOSUBSCRIBE);
        clickAlbum.albumId = albumId;
        clickAlbum.sid = sid;
        clickAlbum.type = 1;
        ReportManager.getInstance().report(clickAlbum);

    }

    public static void clickHistoryRadioUnSubscribe(int sid, long albumId, String name) {
        ClickAlbum clickAlbum = new ClickAlbum(CLICKHISTORYRADIOSUBSCRIBE);
        clickAlbum.albumId = albumId;
        clickAlbum.sid = sid;
        clickAlbum.type = 2;
        ReportManager.getInstance().report(clickAlbum);
    }

    public static void clickHistoryRadioPlay(int sid, long albumId, String name) {
        ClickAlbum clickAlbum = new ClickAlbum(CLICKHISTORYRADIOPLAY);
        clickAlbum.albumId = albumId;
        clickAlbum.sid = sid;
        ReportManager.getInstance().report(clickAlbum);
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
        if (audio == null) {
            return;
        }
        LocalDelete localDelete = new LocalDelete(CLICKBARFAVOUR);
        localDelete.name = audio.getName();
        localDelete.sid = audio.getSid();
        localDelete.audioId = audio.getId();
        localDelete.type = 1;//表示收藏
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickBarMusicUnFavour(Audio audio) {
        if (audio == null) {
            return;
        }
        LocalDelete localDelete = new LocalDelete(CLICKBARFAVOUR);
        localDelete.name = audio.getName();
        localDelete.sid = audio.getSid();
        localDelete.audioId = audio.getId();
        localDelete.type = 2;//表示取消收藏
        ReportManager.getInstance().report(localDelete);
    }


    public static void clickBarRadioSubscribe(Album album) {
        if (album == null) {
            return;
        }
        ClickAlbum clickAlbum = new ClickAlbum(CLICKBARSUBSCRIBE);
        clickAlbum.albumId = album.getId();
        clickAlbum.sid = album.getSid();
        clickAlbum.type = 1;
        ReportManager.getInstance().report(clickAlbum);

    }

    public static void clickBarRadioUnSubscribe(Album album) {
        if (album == null) {
            return;
        }
        ClickAlbum clickAlbum = new ClickAlbum(CLICKBARSUBSCRIBE);
        clickAlbum.albumId = album.getId();
        clickAlbum.sid = album.getSid();
        clickAlbum.type = 2;
        ReportManager.getInstance().report(clickAlbum);
    }

    public static void clickBarPlay() {

        ClickBtn clickBtn = new ClickBtn(CLICKBARPLAYORPAUSE);
        clickBtn.type = 1;
        ReportManager.getInstance().report(clickBtn);

    }

    public static void clickBarPause() {

        ClickBtn clickBtn = new ClickBtn(CLICKBARPLAYORPAUSE);
        clickBtn.type = 2;
        ReportManager.getInstance().report(clickBtn);

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

    public static void clickBarPlaylistItemBtn(int sid, long audioId, String name) {
        clickAudio(CLICKBARPLAYLISTITEM, sid, audioId, name);
    }

    public static void clickBarCoverBtn() {
        clickBtn(CLICKBARCOVER);
    }

    public static void clickBarPrevBtn() {
        clickBtn(CLICKBARPREV);
    }

    public static void clickBarNextBtn() {
        clickBtn(CLICKBARNEXT);
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
        if (audio == null) {
            return;
        }
        LocalDelete localDelete = new LocalDelete(CLICK_PLAYER_FAVOUR);
        localDelete.name = audio.getName();
        localDelete.sid = audio.getSid();
        localDelete.audioId = audio.getId();
        localDelete.type = 1;//表示收藏
        ReportManager.getInstance().report(localDelete);
    }

    public static void clickPlayerPageMusicUnFavour(Audio audio) {
        if (audio == null) {
            return;
        }
        LocalDelete localDelete = new LocalDelete(CLICK_PLAYER_FAVOUR);
        localDelete.name = audio.getName();
        localDelete.sid = audio.getSid();
        localDelete.audioId = audio.getId();
        localDelete.type = 2;//表示取消收藏
        ReportManager.getInstance().report(localDelete);
    }


    public static void clickPlayerPageRadioSubscribe(Album album) {
        if (album == null) {
            return;
        }
        ClickAlbum clickAlbum = new ClickAlbum(CLICK_PLAYER_SUBSCRIBE);
        clickAlbum.albumId = album.getId();
        clickAlbum.sid = album.getSid();
        clickAlbum.type = 1;
        ReportManager.getInstance().report(clickAlbum);

    }

    public static void clickPlayerPageRadioUnSubscribe(Album album) {
        if (album == null) {
            return;
        }
        ClickAlbum clickAlbum = new ClickAlbum(CLICK_PLAYER_SUBSCRIBE);
        clickAlbum.albumId = album.getId();
        clickAlbum.sid = album.getSid();
        clickAlbum.type = 2;
        ReportManager.getInstance().report(clickAlbum);
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


    public static void clickPlayerPagePlaylistItemBtn(int sid, long audioId, String name) {
        clickAudio(CLICK_PLAYER_PLAYLIST_ITEM, sid, audioId, name);
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
    public static void reportPushEvent(String action, String type, int id) {
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


    public static void reportPlayEvent(String action, Audio audio, Album album) {
        if (null == audio) {
            return;
        }
        PlayEvent playEvent = new PlayEvent(action, audio, album);
        ReportManager.getInstance().report(playEvent);
    }


    public static void reportCommEvent(String eventId) {
        EventBase eventBase = new EventBase(eventId);
        ReportManager.getInstance().report(eventBase);
    }
}

