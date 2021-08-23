package com.txznet.music.baseModule.dao;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.BreakpointAudio;
import com.txznet.music.albumModule.bean.Category;
import com.txznet.music.dao.DaoManager;
import com.txznet.music.favor.bean.BeSendBean;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.message.Message;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.soundControlModule.logic.net.request.ReqSearch;
import com.txznet.music.utils.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * 数据库帮助类
 */
public class DBManager {

    private static final String TAG = "Music:DB:";

    // 单例
    private static DBManager instance;

    private DBManager() {
    }

    public static DBManager getInstance() {
        if (instance == null) {
            synchronized (DBManager.class) {
                if (instance == null) {
                    instance = new DBManager();
                }
            }
        }
        return instance;
    }


    public Audio findAudio(Audio audio) {
        return DaoManager.getInstance().findAudio(audio);
    }

    /**
     * 查10条数据
     *
     * @param albumId
     * @return
     */
    public List<Audio> findAudiosByAlbumId(long albumId, int limit) {
        return DaoManager.getInstance().findAudiosByAlbumId(albumId, limit);
    }

    public List<Audio> findAudiosByAlbumId(long albumId) {
        return DaoManager.getInstance().findAudiosByAlbumId(albumId);
    }

    public void updateAudioClientListenNum(Audio audio) {
        DaoManager.getInstance().updateAudioClientListenNum(audio);
    }

    public void updateAudioFavour(Audio audio) {
        DaoManager.getInstance().updateAudioFavour(audio);
    }

    public void updateAlbumSubscribe(Album album) {
        DaoManager.getInstance().updateAlbumSubscribe(album);
    }

    public void saveAudio(Audio audio) {
        DaoManager.getInstance().saveAudio(audio);
    }

    public void saveAudios(List<Audio> audios) {
        DaoManager.getInstance().saveAudios(audios);
    }

    /**
     * 有则插入,没有则不插入,不会更新原有的audio
     *
     * @param audios
     */
    public void saveAudiosNotReplace(List<Audio> audios) {
        DaoManager.getInstance().saveAudiosNotReplace(audios);
    }


    public List<Album> findAlbumsByCategory(int categoryId) {
        return DaoManager.getInstance().findAlbumsByCategory(categoryId);
    }

    public Album findAlbumById(long id, int sid) {
        return DaoManager.getInstance().findAlbumById(id, sid);
    }


    public void saveAlbum(Album album) {
        DaoManager.getInstance().saveAlbum(album);
    }

    /**
     * 有则插入,没有则不插入,不会更新原有的audio
     *
     * @param albums
     */
    public void saveAlbumNotReplace(List<Album> albums) {
        DaoManager.getInstance().saveAlbumNotReplace(albums);
    }

    public void saveAlbumOrReplace(List<Album> albums) {
        DaoManager.getInstance().saveAlbumOrReplace(albums);
    }

    /**
     * 当album不存在时添加该album。搜索返回结果时的数据有些字段没有，但是又需要将其
     * 保存起来，为避免其覆盖之前完整的数据，添加了该方法
     *
     * @param album
     */
    public void insertAlbumIfNotExist(Album album) {
        if (album == null) {
            return;
        }
        Album currentAlbum = findAlbumById(album.getId(), album.getSid());
        if (currentAlbum != null) {
            return;
        }
        saveAlbum(album);
    }

    /**
     * 保存到数据库
     *
     * @param albums
     * @param categoryID
     * @param
     */
    public void saveToAlbum(List<Album> albums, int categoryID, int pageID) {
        DaoManager.getInstance().saveAlbums(albums, categoryID, pageID);
    }


    public List<Category> findAllCategories() {
        return DaoManager.getInstance().findAllCategories();
    }

    /**
     * 保存到数据库
     *
     * @param arrCategory
     */
    public void saveToCategory(List<Category> arrCategory) {
        DaoManager.getInstance().saveCategories(arrCategory);
    }


//    public List<Audio> findHistoryAudioBySid(List<Integer> sids) {
//        return DaoManager.getInstance().findHistoryAudioBySid(sids);
//    }
//
//    public void saveToHistory(final Audio audio, final Album currentAlbum) {
//        DaoManager.getInstance().saveToHistory(audio, currentAlbum);
//    }
//
//
//    public boolean removeHistoryAudio(Audio audio) {
//        return DaoManager.getInstance().removeHistoryAudio(audio);
//    }


    public List<Audio> findAllLocalAudios() {
        return DaoManager.getInstance().findAllLocalAudios();
    }

    public void removeAllLocalAudios() {
        DaoManager.getInstance().removeAllLocalAudios();
    }


    public void removeLocalAudios(Audio audio) {
        DaoManager.getInstance().removeLocalAudio(audio);
    }

    public void removeLocalAudios(List<Audio> audios) {
        if (CollectionUtils.isNotEmpty(audios)) {
            for (int i = audios.size() - 1; i >= 0; i--) {
                DaoManager.getInstance().removeLocalAudio(audios.get(i));
            }
        }
    }

    public void saveLocalAudio(Audio audio) {
        DaoManager.getInstance().saveLocalAudio(audio);
    }

    public void saveLocalAudios(List<Audio> audios) {
        DaoManager.getInstance().saveLocalAudios(audios);
    }


    public List<Audio> findLocalAudioForSearch(ReqSearch reqData) {
        return DaoManager.getInstance().findLocalAudioForSearch(reqData);
    }


    public void saveBreakpoint(Audio audio, int breakpoint, boolean playEnd) {
        LogUtil.d(TAG, "save breakpoint " + audio.getName() + " " + breakpoint);
        DaoManager.getInstance().saveBreakpoint(audio, breakpoint, playEnd);
    }

    public BreakpointAudio findBreakpoint(Audio audio) {
        return DaoManager.getInstance().findBreakpoint(audio);
    }

    public List<Audio> findBreakpointByAlbumId(long albumId) {
        return DaoManager.getInstance().findBreakpointByAlbumId(albumId);
    }

//    public List<Audio> findHistoryDBHistoryByLimit(int limit) {
//        return DaoManager.getInstance().findHistoryDBHistoryByLimit(limit);
//    }

    public void savePlayItem(PlayItem playItem) {
        DaoManager.getInstance().savePlayItem(playItem);
    }

    public PlayItem findPlayItem(Audio audio) {
        return DaoManager.getInstance().findPlayItem(audio);
    }


    public List<FavourBean> findAllFavorMusic() {

        return DaoManager.getInstance().findAllFavorMusic();
    }

    public List<FavourBean> findFavorMusics(long starttime, long endTime) {
        return DaoManager.getInstance().findFavorMusics(starttime, endTime);
    }

    public FavourBean findFavorMusic(Audio audio) {
        return DaoManager.getInstance().findFavorMusic(audio);
    }

    public void saveFavorMusicItem(FavourBean favourBean) {
        DaoManager.getInstance().saveFavorMusicItem(favourBean);
    }

    public void saveFavorMusicItems(List<FavourBean> favourBeans) {
        DaoManager.getInstance().saveFavorMusicItems(favourBeans);
    }

    public void deleteFavor(FavourBean favourBean) {
        DaoManager.getInstance().deleteFavor(favourBean);
    }

    public void deleteFavors(List<FavourBean> favourBeans) {
        DaoManager.getInstance().deleteFavors(favourBeans);
    }

    /**
     * 删除非sdcard的收藏音乐
     */
    public void deleteNetFavor(long startTime, long endTime) {
        DaoManager.getInstance().deleteNetFavor(startTime, endTime);
    }

    public List<SubscribeBean> findNetSubscribe(long startTime, long endTime) {
        return DaoManager.getInstance().findNetSubscribe(startTime, endTime);
    }

    public SubscribeBean findNetSubscribe(Album album) {
        return DaoManager.getInstance().findSubscribe(album);
    }

    /**
     * 删除sdcard的收藏音乐的数据
     */
    public void deleteSDcardFavor(long startTime, long endTime) {
        DaoManager.getInstance().deleteSDcardFavor(startTime, endTime);
    }

    public List<SubscribeBean> findAllSubscribe() {
        return DaoManager.getInstance().findAllSubscribe();
    }


    public void saveSubscribeItem(SubscribeBean subscribeBean) {
        DaoManager.getInstance().saveSubscribeItem(subscribeBean);
    }

    public void saveSubscribeItems(List<SubscribeBean> subscribeBeans) {
        DaoManager.getInstance().saveSubscribeItems(subscribeBeans);
    }

    public void deleteSubscribe(SubscribeBean subscribeBean) {
        DaoManager.getInstance().deleteSubscribe(subscribeBean);
    }

    public void deleteSubscribe(List<SubscribeBean> subscribeBean) {
        DaoManager.getInstance().deleteSubscribe(subscribeBean);
    }

    public void deleteNetSubscribe(long startTime, long endTime) {
        DaoManager.getInstance().deleteNetSubscribe(startTime, endTime);
    }


    /**
     * 获取待发送的条数
     */
    public List<BeSendBean> findToBeSendBean(int count) {
        return DaoManager.getInstance().findToBeSendBean(count);
    }

    public void saveToBeSendBeanItem(BeSendBean beSendBean) {
        DaoManager.getInstance().saveToBeSendBeanItem(beSendBean);
    }

    public void saveToBeSendBeanItem(List<BeSendBean> beSendBean) {
        DaoManager.getInstance().saveToBeSendBeanItem(beSendBean);
    }

    public void deleteToBeSendBean(BeSendBean beSendBean) {
        DaoManager.getInstance().deleteToBeSendBean(beSendBean);
    }

    public void deleteToBeSendBeans(List<BeSendBean> beSendBean) {
        DaoManager.getInstance().deleteToBeSendBeans(beSendBean);
    }


    public void deleteMessage(Message message) {
        DaoManager.getInstance().deleteMessage(message);
    }

    public void deleteMessages(List<Message> messages) {
        DaoManager.getInstance().deleteMessages(messages);
    }

    public void saveMessages(List<Message> messages) {
        DaoManager.getInstance().saveMessages(messages);
    }


    public void saveMessage(Message message) {
        DaoManager.getInstance().saveMessage(message);
    }


    public void clearMessageUnRead() {
        DaoManager.getInstance().clearMessageUnRead();
    }

    public List<Message> getMessages() {
        return DaoManager.getInstance().getMessages();
    }

    public Message findMessage(long id, int sid) {
        return DaoManager.getInstance().findMessage(id, sid);
    }

    public void updateMessageUnRead(long id, int sid) {
        DaoManager.getInstance().updateMessageUnRead(id, sid);
    }

    public boolean checkUnreadMessage() {
        return DaoManager.getInstance().checkUnreadMessage();
    }


    public void saveMusicHistory(Audio audio) {
        DaoManager.getInstance().saveMusicHistory(audio);
    }

    public void saveAlbumHistory(Album album, Audio audio) {
        DaoManager.getInstance().saveAlbumHistory(album, audio);
    }


    public List<HistoryData> findMusicHistory() {
        return DaoManager.getInstance().findMusicHistory();
    }


    public List<HistoryData> findAlbumHistory() {
        return DaoManager.getInstance().findAlbumHistory();
    }


    public List<HistoryData> findHistory() {
        return DaoManager.getInstance().findHistory();
    }


    public HistoryData findNewestHistory() {
        return DaoManager.getInstance().findNewestHistory();
    }


    public void deleteHistory(HistoryData historyData) {
        DaoManager.getInstance().deleteHistory(historyData);
    }


    public List<Audio> convertHistoryDataToAudio(List<HistoryData> historyData) {
        List<Audio> audios = new ArrayList<>();
        for (HistoryData data : historyData) {
            if (data.getType() == HistoryData.TYPE_AUDIO) {
                Audio audio = data.getAudio();
                if (null == audio) {
                    Logger.e(TAG, "history data audio is null, id=%d, sid=%d", data.getId(), data.getSid());
                    continue;
                }
                audios.add(data.getAudio());
            }
        }
        return audios;
    }
}
