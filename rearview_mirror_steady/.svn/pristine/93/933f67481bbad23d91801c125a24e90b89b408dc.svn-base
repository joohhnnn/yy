package com.txznet.music.data.dao;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.remote.util.Logger;
import com.txznet.music.BuildConfig;
import com.txznet.music.albumModule.bean.Album;
import com.txznet.music.albumModule.bean.AlbumDao;
import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.albumModule.bean.AudioDao;
import com.txznet.music.albumModule.bean.BreakpointAudio;
import com.txznet.music.albumModule.bean.BreakpointAudioDao;
import com.txznet.music.baseModule.bean.PlayListData;
import com.txznet.music.baseModule.bean.PlayListDataDao;
import com.txznet.music.data.entity.Category;
import com.txznet.music.albumModule.bean.DaoMaster;
import com.txznet.music.albumModule.bean.DaoSession;
import com.txznet.music.favor.bean.BeSendBean;
import com.txznet.music.favor.bean.BeSendBeanDao;
import com.txznet.music.favor.bean.FavourBean;
import com.txznet.music.favor.bean.FavourBeanDao;
import com.txznet.music.favor.bean.SubscribeBean;
import com.txznet.music.favor.bean.SubscribeBeanDao;
import com.txznet.music.historyModule.bean.HistoryAudio;
import com.txznet.music.historyModule.bean.HistoryAudioDao;
import com.txznet.music.historyModule.bean.HistoryData;
import com.txznet.music.historyModule.bean.HistoryDataDao;
import com.txznet.music.localModule.bean.LocalAudio;
import com.txznet.music.localModule.bean.LocalAudioDao;
import com.txznet.music.localModule.logic.AlbumUtils;
import com.txznet.music.message.Message;
import com.txznet.music.message.MessageDao;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.playerModule.bean.PlayItemDao;
import com.txznet.music.playerModule.bean.QQTicketTable;
import com.txznet.music.playerModule.bean.QQTicketTableDao;
import com.txznet.music.playerModule.logic.PlayInfoManager;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import org.greenrobot.greendao.query.DeleteQuery;
import org.greenrobot.greendao.query.Join;
import org.greenrobot.greendao.query.Query;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by brainBear on 2017/9/8.
 */

public class DaoManager {
    private static final int HISTORY_MAX_COUNT = 100;
    private static final String TAG = "Music:DB:";
    private static DaoManager sInstance = null;
    private DaoSession mDaoSession;

    public final static String DB_NAME = "txz_music.db";

    //4.1开始使用GreenDao数据库，没有处理好数据库降级的问题，导致热升级回滚的时候一直crash，无法恢复，所以在4.2版本上使用新的表
    public final static String DB_NAME_NEW = "txz_music_new.db";

    private DaoManager() {
        DaoOpenHelper openHelper = new DaoOpenHelper(GlobalContext.get(), DB_NAME_NEW);
        DaoMaster daoMaster = new DaoMaster(openHelper.getWritableDb());
        mDaoSession = daoMaster.newSession();

        if (BuildConfig.DEBUG) {
            QueryBuilder.LOG_SQL = true;
            QueryBuilder.LOG_VALUES = true;
        }
    }

    public static DaoManager getInstance() {

        if (null == sInstance) {
            synchronized (DaoManager.class) {
                if (null == sInstance) {
                    sInstance = new DaoManager();
                }
            }
        }
        return sInstance;
    }


    public void saveCategories(List<Category> arrCategory) {
        LogUtil.d(TAG + "saveCategory size :" + (arrCategory != null ? arrCategory.size() : 0));
        if (arrCategory == null || arrCategory.isEmpty()) {
            return;
        }
        mDaoSession.getCategoryDao().deleteAll();
        mDaoSession.getCategoryDao().insertInTx(arrCategory);
    }

    public List<Category> findAllCategories() {
        return mDaoSession.getCategoryDao().queryBuilder().build().forCurrentThread().list();
    }


    public void saveAlbums(List<Album> albums, long categoryID, int pageID) {
        LogUtil.d(TAG + "saveAlbum size :"
                + (albums != null ? albums.size() : 0) + ",current categoryID:"
                + categoryID);
        if (null == albums || albums.isEmpty()) {
            return;
        }

        AlbumDao albumDao = mDaoSession.getAlbumDao();
        if (pageID == 1) {
            //如果是第一页，则删除相应的数据
//            AlbumDBHelper.getInstance().remove(AlbumDBHelper.TABLE_CATEGORYID + "=?", new String[]{String.valueOf(categoryID)});
            List<Album> list = albumDao.queryBuilder()
                    .where(AlbumDao.Properties.ArrCategoryIds.in(categoryID))
                    .build()
                    .forCurrentThread()
                    .list();
            albumDao.deleteInTx(list);
        }

        List<Album> reducedAlbums = new ArrayList<>();
        for (Album album : albums) {
            if (album == null) {
                continue;
            }
            reducedAlbums.add(album);
        }
        Query<Album> albumQuery = null;
        for (Album album : reducedAlbums) {
//            album.setCategoryId(categoryID);
            AlbumUtils.generateProperties(album);
            if (null == albumQuery) {
                albumQuery = albumDao.queryBuilder()
                        .where(AlbumDao.Properties.Id.eq(album.getId())
                                , AlbumDao.Properties.Sid.eq(album.getSid()))
                        .build()
                        .forCurrentThread();
            } else {
                albumQuery.setParameter(0, album.getId());
                albumQuery.setParameter(1, album.getSid());
            }

            Album localAlbum = albumQuery.unique();
            if (null != localAlbum) {
                if (localAlbum.getLastListen() > album.getLastListen()) {
                    album.setLastListen(localAlbum.getLastListen());
                }
                album.setAlbumDbId(localAlbum.getAlbumDbId());
                albumDao.update(album);
            } else {
                albumDao.insert(album);
            }
        }
    }


    public Album findAlbumById(long id, int sid) {
//        return AlbumDBHelper.getInstance().findOne(null, AlbumDBHelper.TABLE_ID + " ==? and " + AlbumDBHelper.TABLE_SID + " ==? ", new String[]{"" + id, "" + sid});
        return mDaoSession.getAlbumDao().queryBuilder()
                .where(AlbumDao.Properties.Id.eq(id)
                        , AlbumDao.Properties.Sid.eq(sid))
                .build()
                .forCurrentThread()
                .unique();
    }

    public List<Album> findAlbumsByCategory(int categoryId) {
        return mDaoSession.getAlbumDao().queryBuilder()
                .where(AlbumDao.Properties.ArrCategoryIds.in(categoryId))
                .build()
                .forCurrentThread()
                .list();
    }

    public void saveAlbumNotReplace(List<Album> albums) {
        if (null == albums || albums.isEmpty()) {
            return;
        }
        List<Album> needSave = new ArrayList<>(albums.size());
        for (Album album : albums) {
            if (mDaoSession.getAlbumDao().load(album.getAlbumDbId()) == null) {
                needSave.add(album);
            }
        }
        mDaoSession.getAlbumDao().insertInTx(needSave);


    }


    public void saveAlbum(Album album) {
//        AlbumDBHelper.getInstance().saveOrUpdate(album);
        if (null == album) {
            return;
        }
        Album unique = mDaoSession.getAlbumDao().queryBuilder()
                .where(AlbumDao.Properties.Id.eq(album.getId())
                        , AlbumDao.Properties.Sid.eq(album.getSid()))
                .build()
                .forCurrentThread()
                .unique();
        if (null != unique) {
            if (unique.getLastListen() > album.getLastListen()) {
                album.setLastListen(unique.getLastListen());
            }
            mDaoSession.getAlbumDao().update(album);
        } else {
            mDaoSession.getAlbumDao().insert(album);
        }
    }

    public Audio findAudio(Audio audio) {
        if (null == audio) {
            return null;
        }
        return mDaoSession.getAudioDao().queryBuilder()
                .where(AudioDao.Properties.Id.eq(audio.getId())
                        , AudioDao.Properties.Sid.eq(audio.getSid()))
                .build()
                .forCurrentThread()
                .unique();
    }


    public List<Audio> findAudiosByAlbumId(long albumId, int limit) {
        return mDaoSession.getAudioDao().queryBuilder()
                .where(AudioDao.Properties.AlbumId.eq(albumId))
                .limit(limit)
                .build()
                .forCurrentThread()
                .list();
    }


    public List<Audio> findAudiosByAlbumId(long albumId) {
        return mDaoSession.getAudioDao().queryBuilder()
                .whereOr(AudioDao.Properties.AlbumId.eq(albumId), AudioDao.Properties.SrcAlbumId.eq(albumId))
                .build()
                .forCurrentThread()
                .list();
    }

    public void updateAudioClientListenNum(Audio audio) {
        if (audio == null) {
            return;
        }

        Audio unique = mDaoSession.getAudioDao().queryBuilder()
                .where(AudioDao.Properties.Id.eq(audio.getId())
                        , AudioDao.Properties.Sid.eq(audio.getSid()))
                .build()
                .forCurrentThread()
                .unique();
        if (null == unique) {
            mDaoSession.getAudioDao().insert(audio);
        } else {
            unique.setClientListenNum(audio.getClientListenNum());
            mDaoSession.getAudioDao().update(unique);
        }
    }

    public void updateAudioFavour(Audio audio) {
        if (audio == null) {
            return;
        }

        Audio unique = mDaoSession.getAudioDao().queryBuilder()
                .where(AudioDao.Properties.Id.eq(audio.getId())
                        , AudioDao.Properties.Sid.eq(audio.getSid()))
                .build()
                .forCurrentThread()
                .unique();
        if (null == unique) {
            mDaoSession.getAudioDao().insert(audio);
        } else {
            unique.setFlag(audio.getFlag());
            mDaoSession.getAudioDao().update(unique);
        }
    }

    public void saveAlbumOrReplace(List<Album> albums) {
        if (null == albums || albums.isEmpty()) {
            return;
        }
        mDaoSession.getAlbumDao().insertOrReplaceInTx(albums);
    }

    public void updateAlbumSubscribe(Album album) {
        if (album == null) {
            return;
        }

        Album unique = mDaoSession.getAlbumDao().queryBuilder()
                .where(AlbumDao.Properties.Id.eq(album.getId())
                        , AlbumDao.Properties.Sid.eq(album.getSid()))
                .build()
                .forCurrentThread()
                .unique();
        if (null == unique) {
            mDaoSession.getAlbumDao().insert(album);
        } else {
            unique.setFlag(album.getFlag());
            mDaoSession.getAlbumDao().update(unique);
        }

    }

    public void saveAudio(Audio audio) {
        if (null == audio) {
            return;
        }

        mDaoSession.getAudioDao().insertOrReplace(audio);
    }

    private void saveAudioIfNotExist(Audio audio) {
        if (audio == null) {
            return;
        }

        // TODO: 2018/8/9 是否需要先判断已经存在？
        try {
            mDaoSession.getAudioDao().insert(audio);//如果已经存在，会抛异常
        } catch (Exception e) {

        }
    }


    public void saveAudios(List<Audio> audios) {
        if (null == audios || audios.isEmpty()) {
            return;
        }

        mDaoSession.getAudioDao().insertOrReplaceInTx(audios);
    }

    public void saveAudiosNotReplace(List<Audio> audios) {
        if (null == audios || audios.isEmpty()) {
            return;
        }

        List<Audio> needSave = new ArrayList<>(audios.size());
        for (Audio audio : audios) {
            if (mDaoSession.getAudioDao().load(audio.getAudioDbId()) == null) {
                needSave.add(audio);
            }
        }
        mDaoSession.getAudioDao().insertInTx(needSave);
    }


    public List<Audio> findHistoryDBHistoryByLimit(int limit) {
        List<HistoryAudio> list = mDaoSession.getHistoryAudioDao().queryBuilder()
                .limit(limit)
                .offset(0)
                .orderDesc(HistoryAudioDao.Properties.DbId)
                .build()
                .forCurrentThread()
                .list();
        List<Audio> audios = new ArrayList<>();

        for (HistoryAudio historyAudio : list) {
            audios.add(historyAudio.getAudio());
        }
        return audios;
    }


    public boolean removeHistoryAudio(Audio audio) {
        if (null == audio) {
            return false;
        }
        QueryBuilder<HistoryAudio> queryBuilder = mDaoSession.getHistoryAudioDao().queryBuilder();
        queryBuilder.join(HistoryAudioDao.Properties.AudioDbId,
                Audio.class, AudioDao.Properties.AudioDbId)
                .where(AudioDao.Properties.Id.eq(audio.getId())
                        , AudioDao.Properties.Sid.eq(audio.getSid()));
        HistoryAudio historyAudio = queryBuilder.build().forCurrentThread().unique();

        if (historyAudio != null) {
            mDaoSession.getHistoryAudioDao().delete(historyAudio);
        }
        return true;
    }


    public List<Audio> findHistoryAudioBySid(List<Integer> sids) {
        if (null == sids || sids.isEmpty()) {
            return null;
        }

        QueryBuilder<HistoryAudio> queryBuilder = mDaoSession.getHistoryAudioDao().queryBuilder();
        queryBuilder.join(HistoryAudioDao.Properties.AudioDbId, Audio.class, AudioDao.Properties.AudioDbId)
                .where(AudioDao.Properties.Sid.in(sids));

        List<HistoryAudio> historyAudios = queryBuilder.orderDesc(HistoryAudioDao.Properties.DbId)
                .build()
                .forCurrentThread()
                .list();

        List<Audio> audios = new ArrayList<>();
        for (HistoryAudio historyAudio : historyAudios) {
            audios.add(historyAudio.getAudio());
        }

        return audios;
    }


    public void saveToHistory(final Audio audio, final Album currentAlbum) {
        if (null == audio) {
            return;
        }

        QueryBuilder<HistoryAudio> queryBuilder = mDaoSession.getHistoryAudioDao().queryBuilder();
        Join<HistoryAudio, Audio> join = queryBuilder.join(HistoryAudioDao.Properties.AudioDbId,
                Audio.class, AudioDao.Properties.AudioDbId);
        if (Utils.isSong(audio.getSid())) {
            join.where(AudioDao.Properties.Id.eq(audio.getId()));
        } else {
            if (null == currentAlbum) {
                return;
            }
            audio.setAlbumId(String.valueOf(currentAlbum.getId()));
            audio.setAlbumName(currentAlbum.getName());
            if (StringUtils.isNotEmpty(currentAlbum.getLogo())) {
                audio.setLogo(currentAlbum.getLogo());
            }
            join.where(AudioDao.Properties.AlbumId.eq(audio.getAlbumId()));
        }
//        HistoryAudio sameHistoryAudio = queryBuilder.build().unique();
//        if (null != sameHistoryAudio) {
//            mDaoSession.getHistoryAudioDao().delete(sameHistoryAudio);
//        }
        List<HistoryAudio> list = queryBuilder.build().forCurrentThread().list();
        mDaoSession.getHistoryAudioDao().deleteInTx(list);


        Audio unique = mDaoSession.getAudioDao().queryBuilder()
                .where(AudioDao.Properties.Id.eq(audio.getId())
                        , AudioDao.Properties.Sid.eq(audio.getSid()))
                .build()
                .forCurrentThread()
                .unique();
        if (null == unique) {
            mDaoSession.getAudioDao().insertOrReplace(audio);
        }

        HistoryAudio historyAudio = new HistoryAudio();
        historyAudio.setAudioDbId(audio.getAudioDbId());

        mDaoSession.getHistoryAudioDao().insert(historyAudio);

        QueryBuilder<HistoryAudio> builder = mDaoSession.getHistoryAudioDao().queryBuilder();
        long count = builder.buildCount().forCurrentThread().count();
        if (count > HISTORY_MAX_COUNT) {
            HistoryAudio oldestHistory = builder.orderAsc(HistoryAudioDao.Properties.DbId)
                    .offset(0)
                    .limit(1)
                    .build()
                    .forCurrentThread()
                    .unique();

            if (null != oldestHistory) {
                mDaoSession.getHistoryAudioDao().delete(oldestHistory);
            }
        }
    }


    public List<Audio> findAllLocalAudios() {
        List<LocalAudio> list = mDaoSession.getLocalAudioDao().queryBuilder()
                .build()
                .forCurrentThread()
                .list();

        List<Audio> audios = new ArrayList<>();
        for (LocalAudio localAudio : list) {
            if (localAudio.getAudio() != null) {
                audios.add(localAudio.getAudio());
            }
        }
        return audios;

    }

    /**
     * 是否有本地音乐数据
     *
     * @return
     */
    public boolean hasLocalAudio() {
        return mDaoSession.getLocalAudioDao().count() > 0;
    }


    public void removeAllLocalAudios() {
        mDaoSession.getLocalAudioDao().deleteAll();
//        mDaoSession.getLocalAudioDao().queryBuilder()
//                .buildDelete()
//                .forCurrentThread()
//                .executeDeleteWithoutDetachingEntities();
    }


    public void removeLocalAudio(Audio audio) {
        QueryBuilder<LocalAudio> queryBuilder = mDaoSession.getLocalAudioDao().queryBuilder();
        Join<LocalAudio, Audio> join = queryBuilder.join(LocalAudioDao.Properties.AudioDbId
                , Audio.class, AudioDao.Properties.AudioDbId);
        join.where(AudioDao.Properties.Id.eq(audio.getId())
                , AudioDao.Properties.Sid.eq(audio.getSid()));
        LocalAudio unique = queryBuilder.build().forCurrentThread().unique();
        audio.setLocal(false);
        mDaoSession.getAudioDao().update(audio);//更新标志位
        if (null != unique) {
            mDaoSession.getLocalAudioDao().delete(unique);
        }
    }

    public void saveLocalAudio(Audio audio) {
        if (null == audio) {
            return;
        }

        List<Audio> tempList = new ArrayList<>(1);
        tempList.add(audio);
        List<Audio> needSaveAudios = getNeedSaveAudios(tempList);

        saveAudios(needSaveAudios);

        LocalAudio localAudio = new LocalAudio();
        localAudio.setId(audio.getId());
        localAudio.setSid(audio.getSid());
        localAudio.setAudioDbId(audio.getAudioDbId());

        mDaoSession.getLocalAudioDao().insertOrReplaceInTx(localAudio);
    }

    public void saveLocalAudios(List<Audio> audios) {
        if (null == audios || audios.isEmpty()) {
            return;
        }

        //存本地音乐的Audio数据
        List<Audio> needSaveAudios = getNeedSaveAudios(audios);
        saveAudios(needSaveAudios);

        //存放LocalAudio的数据
        List<LocalAudio> localAudios = new ArrayList<>();
        for (Audio audio : audios) {
            LocalAudio localAudio = new LocalAudio();
            localAudio.setId(audio.getId());
            localAudio.setSid(audio.getSid());
            localAudio.setAudioDbId(audio.getAudioDbId());
            localAudios.add(localAudio);
        }
        mDaoSession.getLocalAudioDao().insertOrReplaceInTx(localAudios);
    }

    private List<Audio> getNeedSaveAudios(List<Audio> audios) {

        List<Audio> needSaveAudios = new ArrayList<>();
        Query<Audio> query = null;
        for (Audio audio : audios) {
//            if (audio.getSid() == 0) {
////                needSaveAudios.add(audio);
////                continue;
////            }
//
            if (query == null) {
                query = mDaoSession.getAudioDao()
                        .queryBuilder()
                        .where(AudioDao.Properties.Id.eq(audio.getId()), AudioDao.Properties.Sid.eq(audio.getSid()))
                        .build();
            } else {
                query.setParameter(0, audio.getId());
                query.setParameter(1, audio.getSid());
            }
            Audio unique = query.unique();

            if (null == unique) {
                needSaveAudios.add(audio);
            }

        }

        Logger.i(TAG, "audios size=%d need save audio size=%d", audios.size(), needSaveAudios.size());
        return needSaveAudios;
    }


    public List<Audio> findLocalAudioForSearch(String audioName, String artists, String albumName) {
        QueryBuilder<LocalAudio> queryBuilder = mDaoSession.getLocalAudioDao().queryBuilder();
        Join<LocalAudio, Audio> join = queryBuilder.join(LocalAudioDao.Properties.AudioDbId
                , Audio.class, AudioDao.Properties.AudioDbId);
        if (StringUtils.isNotEmpty(artists)
                && StringUtils.isEmpty(audioName)) {
//            join.where(AudioDao.Properties.Name.like("%" + artists + "%"));
            join.whereOr(AudioDao.Properties.Name.like("%" + artists + "%")
                    , AudioDao.Properties.ArrArtistName.like("%" + artists + "%"));
        } else if (StringUtils.isEmpty(artists)
                && StringUtils.isNotEmpty(audioName)) {
            join.where(AudioDao.Properties.Name.like("%" + audioName + "%"));
        } else if (StringUtils.isNotEmpty(artists)
                && StringUtils.isNotEmpty(audioName)) {
            join.whereOr(join.and(AudioDao.Properties.Name.like("%" + artists + "%"),
                    AudioDao.Properties.Name.like("%" + audioName + "%")), join.and(AudioDao.Properties.Name.like("%" + audioName + "%")
                    , AudioDao.Properties.ArrArtistName.like("%" + artists + "%")));
        } else if (StringUtils.isEmpty(artists)
                && StringUtils.isEmpty(audioName)
                && StringUtils.isNotEmpty(albumName)) {
            join.where(AudioDao.Properties.Name.like("%" + albumName + "%"));
        } else {
            return null;
        }
        List<LocalAudio> localAudios = queryBuilder.build().forCurrentThread().list();
        List<Audio> audios = new ArrayList<>();
        for (LocalAudio localAudio : localAudios) {
            audios.add(localAudio.getAudio());
        }
        return audios;
    }


    public List<BreakpointAudio> findBreakpointAudios(List<Audio> audios) {
        List<BreakpointAudio> breakpointAudios = new ArrayList<>();
        Query<BreakpointAudio> breakpointAudioQuery = null;
        for (Audio audio : audios) {
            if (!Utils.isSong(audio.getSid())) {
                if (null == breakpointAudioQuery) {
                    breakpointAudioQuery = mDaoSession.getBreakpointAudioDao().queryBuilder()
                            .where(BreakpointAudioDao.Properties.Id.eq(audio.getId())
                                    , BreakpointAudioDao.Properties.Sid.eq(audio.getSid()))
                            .build()
                            .forCurrentThread();
                } else {
                    breakpointAudioQuery.setParameter(0, audio.getId());
                    breakpointAudioQuery.setParameter(1, audio.getSid());
                }
                BreakpointAudio unique = breakpointAudioQuery.unique();
                breakpointAudios.add(unique);
            } else {
                breakpointAudios.add(null);
            }
        }
        return breakpointAudios;
    }

    public void saveBreakpoint(Audio audio, int breakpoint, boolean playEnd) {
        if (null == audio || audio.isLocal()) {
            return;
        }

        BreakpointAudio newestBreakpoint = findNewestBreakpoint();
        BreakpointAudio breakpointAudio = findBreakpoint(audio);

        long index = 0;
        if (null != newestBreakpoint) {
            if (null != breakpointAudio
                    && breakpointAudio.getSid() == newestBreakpoint.getSid()
                    && breakpointAudio.getId() == newestBreakpoint.getId()) {
                index = newestBreakpoint.getIndex();
            } else {
                index = newestBreakpoint.getIndex() + 1;
            }
        }

        int playEndCount = 0;
        if (null != breakpointAudio) {
            playEndCount = breakpointAudio.getPlayEndCount();
        } else {
            breakpointAudio = new BreakpointAudio();
        }

        breakpointAudio.setAudio(audio);
        breakpointAudio.setId(audio.getId());
        breakpointAudio.setSid(audio.getSid());
        breakpointAudio.setDuration((int) audio.getDuration());
        breakpointAudio.setIndex(index);
        breakpointAudio.setBreakpoint(breakpoint);
        if (!Utils.isSong(audio.getSid())) {
            Album currentAlbum = PlayInfoManager.getInstance().getCurrentAlbum();
            if (currentAlbum != null) {
                breakpointAudio.setAlbumId(currentAlbum.getId(), currentAlbum.getSid());
            }
        }

        if (playEnd) {
            playEndCount++;
        }
        breakpointAudio.setPlayEndCount(playEndCount);

        //这里不能使用save 因为使用save 在判断是否有key的时候，会一直返回true，所以这里需要注意一下
        mDaoSession.getBreakpointAudioDao().insertOrReplace(breakpointAudio);
    }


    public BreakpointAudio findNewestBreakpoint() {
        return mDaoSession.getBreakpointAudioDao().queryBuilder()
                .orderDesc(BreakpointAudioDao.Properties.Index)
                .offset(0)
                .limit(1)
                .build()
                .unique();
    }


    public BreakpointAudio findBreakpoint(Audio audio) {
        if (null == audio) {
            return null;
        }
        return mDaoSession.getBreakpointAudioDao().queryBuilder()
                .where(BreakpointAudioDao.Properties.Id.eq(audio.getId())
                        , BreakpointAudioDao.Properties.Sid.eq(audio.getSid()))
                .build()
                .forCurrentThread()
                .unique();
    }

    public BreakpointAudio findBreakpoint(Album album, Audio audio) {
        if (null == audio) {
            return null;
        }
        if (null == album) {
            return null;
        }
        return mDaoSession.getBreakpointAudioDao().queryBuilder()
                .where(BreakpointAudioDao.Properties.Id.eq(audio.getId())
                        , BreakpointAudioDao.Properties.Sid.eq(audio.getSid()), BreakpointAudioDao.Properties.AlbumId.eq(album.getSid() + "-" + album.getId()))
                .build()
                .forCurrentThread()
                .unique();
    }

    public List<BreakpointAudio> findBreakpoint(Album album) {
        if (null == album) {
            return null;
        }
        Query<BreakpointAudio> breakpointAudioQuery = mDaoSession.getBreakpointAudioDao().queryBuilder()
                .where(BreakpointAudioDao.Properties.AlbumId.eq(album.getSid() + "-" + album.getId()))
                .build()
                .forCurrentThread();
        return breakpointAudioQuery.list();
    }


    public List<Audio> findBreakpointByAlbumId(long albumId) {
        QueryBuilder<BreakpointAudio> queryBuilder = mDaoSession.getBreakpointAudioDao().queryBuilder();
        Join<BreakpointAudio, Audio> join = queryBuilder.join(BreakpointAudioDao.Properties.AudioDbId
                , Audio.class, AudioDao.Properties.AudioDbId);
        join.where(AudioDao.Properties.AlbumId.eq(albumId));
        List<BreakpointAudio> list = queryBuilder.orderDesc(BreakpointAudioDao.Properties.Index).build().forCurrentThread().list();
        List<Audio> audios = new ArrayList<>();
        for (BreakpointAudio breakpointAudio : list) {
            audios.add(breakpointAudio.getAudio());
        }
        return audios;
    }

    public QQTicketTable findTicketUrl(long id, int sid) {
        QueryBuilder<QQTicketTable> queryBuilder = mDaoSession.getQQTicketTableDao().queryBuilder();
        queryBuilder.join(QQTicketTableDao.Properties.Id, QQTicketTable.class).where(QQTicketTableDao.Properties.Id.eq(id));
        List<QQTicketTable> list = queryBuilder.build().list();


        if (CollectionUtils.isNotEmpty(list)) {
            return list.get(0);
        }

        return null;

    }

    public long saveTicketUrl(QQTicketTable ticketTable) {
        return mDaoSession.getQQTicketTableDao().insertOrReplace(ticketTable);
    }

    public void removeTicketUrl(long id, int sid) {
        mDaoSession.getQQTicketTableDao().deleteByKey(id);
    }

    public void savePlayItem(PlayItem playItem) {
        mDaoSession.getPlayItemDao().insertOrReplace(playItem);
    }

    public void removePlayItem(Audio audio) {
        if (audio == null) {
            return;
        }
        mDaoSession.getPlayItemDao().queryBuilder()
                .where(PlayItemDao.Properties.Id.eq(audio.getId())
                        , PlayItemDao.Properties.Sid.eq(audio.getSid()))
                .buildDelete().executeDeleteWithoutDetachingEntities();
    }

    public PlayItem findPlayItem(Audio audio) {
        return mDaoSession.getPlayItemDao().queryBuilder()
                .where(PlayItemDao.Properties.Id.eq(audio.getId())
                        , PlayItemDao.Properties.Sid.eq(audio.getSid()))
                .build()
                .unique();
    }

    /////////////收藏与订阅,2017年11月29日14:59:15----4.2版本新增内容
    public List<FavourBean> findAllFavorMusic() {
        Query<FavourBean> build = mDaoSession.getFavourBeanDao().queryBuilder().build();
        return build.list();
    }

    public List<FavourBean> findFavorMusics(long starttime, long endTime) {
        QueryBuilder<FavourBean> favourBeanQueryBuilder = mDaoSession.getFavourBeanDao().queryBuilder();
        if (starttime == 0) {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.ge(endTime)).orderDesc(FavourBeanDao.Properties.Timestamp);
        } else if (endTime == 0) {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.lt(starttime)).orderDesc(FavourBeanDao.Properties.Timestamp);
        } else {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.ge(endTime), FavourBeanDao.Properties.Timestamp.lt(starttime)).orderDesc(FavourBeanDao.Properties.Timestamp);
        }
        Query<FavourBean> build = favourBeanQueryBuilder.build();
        return build.list();
    }

    public FavourBean findFavorMusic(Audio audio) {
        QueryBuilder<FavourBean> favourBeanQueryBuilder = mDaoSession.getFavourBeanDao().queryBuilder();
        favourBeanQueryBuilder.where(FavourBeanDao.Properties.AudioDbId.eq(audio.getAudioDbId()));
        Query<FavourBean> build = favourBeanQueryBuilder.build();
        return build.unique();
    }

    public SubscribeBean findSubscribe(Album album) {
        QueryBuilder<SubscribeBean> SubscribeBeanQueryBuilder = mDaoSession.getSubscribeBeanDao().queryBuilder();
        SubscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.AlbumDbId.eq(album.getAlbumDbId()));
        Query<SubscribeBean> build = SubscribeBeanQueryBuilder.build();
        return build.unique();
    }

    public void saveFavorMusicItem(FavourBean favourBean) {
        Logger.d(TAG, "save:favourMusic:" + favourBean.toString());
        mDaoSession.getAudioDao().insertOrReplaceInTx(favourBean.getAudio());
        mDaoSession.getFavourBeanDao().insertOrReplace(favourBean);
    }

    public void saveFavorMusicItems(List<FavourBean> favourBeans) {
        if (null == favourBeans || favourBeans.isEmpty()) {
            return;
        }

        List<Audio> audios = new ArrayList<>();
        for (FavourBean favourBean : favourBeans) {
            audios.add(favourBean.getAudio());
        }

        mDaoSession.getAudioDao().insertOrReplaceInTx(audios);
        mDaoSession.getFavourBeanDao().insertOrReplaceInTx(favourBeans);
    }

    public void deleteFavor(FavourBean favourBean) {
        mDaoSession.getFavourBeanDao().delete(favourBean);
    }

    public void deleteFavors(List<FavourBean> favourBeans) {
        mDaoSession.getFavourBeanDao().deleteInTx(favourBeans);
    }

    public void deleteNetFavor(long starttime, long endTime) {
        QueryBuilder<FavourBean> favourBeanQueryBuilder = mDaoSession.getFavourBeanDao().queryBuilder();
        if (starttime == 0) {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.ge(endTime), FavourBeanDao.Properties.Sid.in(Utils.getSongSidFromNet()));
        } else if (endTime == 0) {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.lt(starttime), FavourBeanDao.Properties.Sid.in(Utils.getSongSidFromNet()));
        } else {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.ge(endTime), FavourBeanDao.Properties.Timestamp.lt(starttime), FavourBeanDao.Properties.Sid.in(Utils.getSongSidFromNet()));
        }
        List<FavourBean> list = favourBeanQueryBuilder.build().list();
        LogUtil.d("test:delete:" + starttime + "," + endTime, list);
        mDaoSession.getFavourBeanDao().deleteInTx(list);
    }

    public void deleteSDcardFavor(long startTime, long endTime) {
        QueryBuilder<FavourBean> favourBeanQueryBuilder = mDaoSession.getFavourBeanDao().queryBuilder();
        if (startTime == 0) {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.ge(endTime), FavourBeanDao.Properties.Sid.eq(0));
        } else if (endTime == 0) {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.lt(startTime), FavourBeanDao.Properties.Sid.eq(0));
        } else {
            favourBeanQueryBuilder.where(FavourBeanDao.Properties.Timestamp.ge(endTime), FavourBeanDao.Properties.Timestamp.lt(startTime), FavourBeanDao.Properties.Sid.in(FavourBeanDao.Properties.Sid.eq(0)));
        }
        mDaoSession.getFavourBeanDao().deleteInTx(favourBeanQueryBuilder.build().list());
    }

    public List<SubscribeBean> findAllSubscribe() {
        Query<SubscribeBean> build = mDaoSession.getSubscribeBeanDao().queryBuilder().build();
        return build.list();
    }

    public void saveSubscribeItem(SubscribeBean subscribeBean) {
        mDaoSession.getAlbumDao().insertOrReplace(subscribeBean.getAlbum());
        mDaoSession.getSubscribeBeanDao().insertOrReplace(subscribeBean);
    }

    public void saveSubscribeItems(List<SubscribeBean> subscribeBeans) {
        if (null == subscribeBeans || subscribeBeans.isEmpty()) {
            return;
        }

        List<Album> albums = new ArrayList<>();
        for (SubscribeBean subscribeBean : subscribeBeans) {
            albums.add(subscribeBean.getAlbum());
        }
        mDaoSession.getAlbumDao().insertOrReplaceInTx(albums);
        mDaoSession.getSubscribeBeanDao().insertOrReplaceInTx(subscribeBeans);
    }

    public void deleteSubscribe(SubscribeBean subscribeBean) {
        mDaoSession.getSubscribeBeanDao().delete(subscribeBean);
    }

    public void deleteSubscribe(List<SubscribeBean> subscribeBean) {
        mDaoSession.getSubscribeBeanDao().deleteInTx(subscribeBean);
    }

    public void deleteNetSubscribe(long startTime, long endTime) {
        QueryBuilder<SubscribeBean> subscribeBeanQueryBuilder = mDaoSession.getSubscribeBeanDao().queryBuilder();
        if (startTime == 0) {
            subscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.Timestamp.ge(endTime)).orderDesc(SubscribeBeanDao.Properties.Timestamp);
        } else if (endTime == 0) {
            subscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.Timestamp.lt(startTime)).orderDesc(SubscribeBeanDao.Properties.Timestamp);
        } else {
            subscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.Timestamp.ge(endTime), SubscribeBeanDao.Properties.Timestamp.lt(startTime)).orderDesc(SubscribeBeanDao.Properties.Timestamp);
        }
        mDaoSession.getSubscribeBeanDao().deleteInTx(subscribeBeanQueryBuilder.build().list());
    }

    public List<SubscribeBean> findNetSubscribe(long startTime, long endTime) {
        QueryBuilder<SubscribeBean> subscribeBeanQueryBuilder = mDaoSession.getSubscribeBeanDao().queryBuilder();
        if (startTime == 0) {
            subscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.Timestamp.ge(endTime)).orderDesc(SubscribeBeanDao.Properties.Timestamp);
        } else if (endTime == 0) {
            subscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.Timestamp.lt(startTime)).orderDesc(SubscribeBeanDao.Properties.Timestamp);
        } else {
            subscribeBeanQueryBuilder.where(SubscribeBeanDao.Properties.Timestamp.ge(endTime), SubscribeBeanDao.Properties.Timestamp.lt(startTime)).orderDesc(SubscribeBeanDao.Properties.Timestamp);
        }
        Query<SubscribeBean> build = subscribeBeanQueryBuilder.build();
        return build.list();
    }

    /**
     * 获取待发送的条数
     */
    public List<BeSendBean> findToBeSendBean(int count) {
        QueryBuilder<BeSendBean> limit = mDaoSession.getBeSendBeanDao().queryBuilder().orderAsc(BeSendBeanDao.Properties.Timestamp).limit(count);
        Query<BeSendBean> build = limit.build();
        return build.list();
    }

    public void saveToBeSendBeanItem(BeSendBean beSendBean) {
        mDaoSession.getBeSendBeanDao().insertOrReplace(beSendBean);
    }

    public void saveToBeSendBeanItem(List<BeSendBean> beSendBean) {
        mDaoSession.getBeSendBeanDao().insertOrReplaceInTx(beSendBean);
    }

    public void deleteToBeSendBean(BeSendBean beSendBean) {
        mDaoSession.getBeSendBeanDao().delete(beSendBean);
    }

    public void deleteToBeSendBeans(List<BeSendBean> beSendBean) {
        mDaoSession.getBeSendBeanDao().deleteInTx(beSendBean);
    }

    public void deleteMessage(Message message) {
        mDaoSession.getMessageDao().deleteInTx(message);
    }

    public void deleteMessages(List<Message> messages) {
        mDaoSession.getMessageDao().deleteInTx(messages);
    }

    public void saveMessages(List<Message> messages) {
        QueryBuilder<Message> messageQueryBuilder = mDaoSession.getMessageDao().queryBuilder();

        List<WhereCondition> conditions = new ArrayList<>();
        for (Message msg : messages) {
            if (msg.getType() == Message.TYPE_ALBUM) {
                WhereCondition condition = messageQueryBuilder.and(MessageDao.Properties.Id.eq(msg.getId()), MessageDao.Properties.Sid.eq(msg.getSid()));
                conditions.add(condition);
            }
        }

        DeleteQuery<Message> messageDeleteQuery = null;
        if (conditions.size() > 2) {
            messageDeleteQuery = messageQueryBuilder.whereOr(conditions.get(0), conditions.get(1), conditions.subList(2, conditions.size()).toArray(new WhereCondition[0]))
                    .buildDelete();
        } else if (conditions.size() > 1) {
            messageDeleteQuery = messageQueryBuilder.whereOr(conditions.get(0), conditions.get(1))
                    .buildDelete();
        } else if (conditions.size() > 0) {
            messageDeleteQuery = messageQueryBuilder.where(conditions.get(0)).buildDelete();
        }

        if (messageDeleteQuery != null) {
            messageDeleteQuery.executeDeleteWithoutDetachingEntities();
        }

        mDaoSession.getMessageDao().insertInTx(messages);
    }


    public void saveMessage(Message message) {
        if (message.getType() == Message.TYPE_ALBUM) {
            mDaoSession.getMessageDao()
                    .queryBuilder()
                    .where(MessageDao.Properties.Id.eq(message.getId())
                            , MessageDao.Properties.Sid.eq(message.getSid()))
                    .buildDelete()
                    .executeDeleteWithoutDetachingEntities();
        }
        mDaoSession.getMessageDao().insertInTx(message);
    }


    public void clearMessageUnRead() {
        List<Message> messages = mDaoSession.getMessageDao()
                .queryBuilder()
                .where(MessageDao.Properties.Status.eq(Message.STATUS_UNREAD))
                .build()
                .list();

        for (Message msg : messages) {
            msg.setStatus(Message.STATUS_READ);
        }

        mDaoSession.getMessageDao().updateInTx(messages);
    }

    public void updateMessageUnRead(long id, int sid) {
        QueryBuilder<Message> messageQueryBuilder = mDaoSession.getMessageDao().queryBuilder();
        messageQueryBuilder.where(MessageDao.Properties.Id.eq(id), MessageDao.Properties.Sid.eq(sid));
        Message unique = messageQueryBuilder.build().unique();
        if (null != unique) {
            unique.setStatus(Message.STATUS_READ);
            mDaoSession.getMessageDao().update(unique);
        }
    }


    public boolean checkUnreadMessage() {
        long count = mDaoSession.getMessageDao().queryBuilder()
                .where(MessageDao.Properties.Status.eq(Message.STATUS_UNREAD))
                .buildCount()
                .count();

        return count > 0;
    }

    public List<Message> getMessages() {
        return mDaoSession.getMessageDao()
                .queryBuilder()
                .orderDesc(MessageDao.Properties.Time)
                .list();
    }

    public Message findMessage(long id, int sid) {
        QueryBuilder<Message> messageQueryBuilder = mDaoSession.getMessageDao().queryBuilder();
        messageQueryBuilder.where(MessageDao.Properties.Id.eq(id), MessageDao.Properties.Sid.eq(sid));
        return messageQueryBuilder.build().unique();
    }


    public void saveMusicHistory(Album album, Audio audio) {

        if (null == audio || !Utils.isSong(audio.getSid())) {
            return;
        }

        //防止之前没有存Audio，导致后面从历史表中获取audio时获取失败
        saveAudioIfNotExist(audio);//如果已经存在则不需要存了，否则会影响下次从历史列表中获取数据的顺序

        HistoryData historyData = new HistoryData();
        historyData.setType(HistoryData.TYPE_AUDIO);
        historyData.setAudioRowId(audio.getAudioDbId());
        historyData.setId(audio.getId());
        historyData.setSid(audio.getSid());
        if (null != album) {
            saveAlbum(album);
            historyData.setAlbumRowId(album.getAlbumDbId());
        }


        mDaoSession.getHistoryDataDao().insertOrReplace(historyData);

        long count = mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_AUDIO))
                .buildCount()
                .forCurrentThread()
                .count();
        if (count > HISTORY_MAX_COUNT) {
            mDaoSession.getHistoryDataDao()
                    .queryBuilder()
                    .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_AUDIO))
                    .orderDesc(HistoryDataDao.Properties.Index)
                    .offset(HISTORY_MAX_COUNT)
                    .buildDelete()
                    .forCurrentThread()
                    .executeDeleteWithoutDetachingEntities();

        }
    }

    public void saveAlbumHistory(Album album, Audio audio) {
        if (null == album || Utils.isSong(album.getSid())) {
            return;
        }

        Logger.i(TAG, "save history: album:%s %d %d Audio:%s %d %d", album.getName()
                , album.getId(), album.getSid(), audio.getName(), audio.getId(), audio.getSid());


        //防止之前没有存Audio，导致后面从历史表中获取audio时获取失败
        saveAlbum(album);
        if (audio.getAlbumName() != null) {
            saveAlbum(audio.getAlbum());
        }
        saveAudio(audio);

        HistoryData historyData = new HistoryData();
        historyData.setType(HistoryData.TYPE_ALBUM);
        historyData.setAlbumRowId(album.getAlbumDbId());
        historyData.setAudioRowId(audio.getAudioDbId());
        historyData.setId(album.getId());
        historyData.setSid(album.getSid());

        mDaoSession.getHistoryDataDao().insertOrReplace(historyData);

        long count = mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_ALBUM))
                .buildCount()
                .forCurrentThread()
                .count();
        if (count > HISTORY_MAX_COUNT) {
            mDaoSession.getHistoryDataDao()
                    .queryBuilder()
                    .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_ALBUM))
                    .orderDesc(HistoryDataDao.Properties.Index)
                    .offset(HISTORY_MAX_COUNT)
                    .buildDelete()
                    .forCurrentThread()
                    .executeDeleteWithoutDetachingEntities();
        }
    }


    public List<HistoryData> findMusicHistory() {
        return mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_AUDIO))
                .orderDesc(HistoryDataDao.Properties.Index)
                .build()
                .forCurrentThread()
                .list();
    }

    /**
     * 查找最新的音乐历史记录
     *
     * @return
     */
    public HistoryData findlasterMusicHistory() {
        List<HistoryData> list = mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_AUDIO))
                .orderDesc(HistoryDataDao.Properties.Index)
                .build()
                .forCurrentThread()
                .list();

        if (list != null && !list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }


    public List<HistoryData> findAlbumHistory() {
        return mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .where(HistoryDataDao.Properties.Type.eq(HistoryData.TYPE_ALBUM))
                .orderDesc(HistoryDataDao.Properties.Index)
                .build()
                .forCurrentThread()
                .list();
    }

    public List<HistoryData> findHistory() {
        return mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .orderDesc(HistoryDataDao.Properties.Index)
                .build()
                .forCurrentThread()
                .list();
    }


    public HistoryData findNewestHistory() {
        return mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .orderDesc(HistoryDataDao.Properties.Index)
                .limit(1)
                .unique();
    }


    public void deleteHistory(HistoryData historyData) {
        mDaoSession.getHistoryDataDao()
                .queryBuilder()
                .where(HistoryDataDao.Properties.Id.eq(historyData.getId()), HistoryDataDao.Properties.Sid.eq(historyData.getSid()))
                .buildDelete()
                .forCurrentThread()
                .executeDeleteWithoutDetachingEntities();
    }

    public PlayListData findPlayListData() {
        return mDaoSession.getPlayListDataDao().queryBuilder().build().unique();
    }

    public void updatePlayListData(PlayListData playListData) {
        //删除之前的数据
        mDaoSession.getPlayListDataDao().deleteAll();
        //再保存数据
        mDaoSession.getPlayListDataDao().insert(playListData);
    }

    public void updatePlayListData(Audio audio) {
        //org.greenrobot.greendao.DaoException: Expected unique result, but count was 2
//        PlayListData unique = mDaoSession.getPlayListDataDao().queryBuilder().build().unique();
        PlayListData unique = mDaoSession.getPlayListDataDao().queryBuilder().build().unique();
        if (unique == null) {
            return;
        }
        unique.setAudio(audio);
        //org.greenrobot.greendao.DaoException: com.txznet.music.baseModule.bean.PlayListDataDao@219e8288 (PLAY_LIST_DATA) does not have a single-column primary key
        mDaoSession.getPlayListDataDao().update(unique);
    }

}
