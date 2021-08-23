package com.txznet.music.helper;

import com.txznet.audio.player.entity.Audio;
import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.data.db.DBUtils;
import com.txznet.music.data.db.dao.PlayListDataDao;
import com.txznet.music.data.entity.Album;
import com.txznet.music.data.entity.FavourAudio;
import com.txznet.music.data.entity.HistoryAudio;
import com.txznet.music.data.entity.PlayListData;
import com.txznet.music.data.entity.PlayScene;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.util.FileUtils;
import com.txznet.music.util.Logger;

import java.util.Iterator;
import java.util.List;

/**
 * 播放列表记录辅助工具
 *
 * @author zackzhou
 * @date 2018/12/28,14:42
 */

public class PlayListDataHelper {
    public static final String TAG = Constant.LOG_TAG_LOGIC + ":PlayListData";

    private PlayListDataHelper() {
    }

    /**
     * 保存当前播放音频信息
     */
    public static void updatePlayListAudioAsync(Audio audio) {
        AppLogic.runOnBackGround(() -> {
            Logger.d(TAG, String.format("updatePlayListAudioAsync, audio=%s", audio == null ? "" : audio.name));
            PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
            PlayListData playListData = playListDataDao.getPlayListData();
            if (playListData != null) {
                playListData.audio = audio;
                playListDataDao.saveOrUpdate(playListData);
            }
        });
    }

    /**
     * 保存当前播放队列信息
     */
    public static void updatePlayListAsync(PlayScene scene, Album album, List<Audio> audioList) {
        AppLogic.runOnBackGround(() -> {
            Logger.d(TAG, String.format("updatePlayListAsync, scene=%s, album=%s, audioList=%s", scene, album == null ? "" : album.name, audioList == null ? 0 : audioList.size()));
            PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
            PlayListData playListData = playListDataDao.getPlayListData();
            if (playListData == null) {
                playListData = new PlayListData();
            }
            playListData.scene = scene;
            playListData.album = album;
            playListData.audioList = audioList;
            playListDataDao.saveOrUpdate(playListData);
        });
    }

    /**
     * 获取上次播放的音频
     */
    public static Audio getLastPlayAudio() {
        PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
        PlayListData listData = playListDataDao.getPlayListData();
        if (listData != null) {
            return listData.audio;
        }
        return null;
    }

    /**
     * 获取上次播放的专辑信息
     */
    public static Album getLastPlayAlbum() {
        PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
        PlayListData listData = playListDataDao.getPlayListData();
        if (listData != null) {
            return listData.album;
        }
        return null;
    }

    /**
     * 获取上次播放的信息
     */
    public static PlayListData getPlayListData() {
        // 剔除本地已经不存在的记录
        PlayListDataDao playListDataDao = DBUtils.getDatabase(GlobalContext.get()).getPlayListDataDao();
        PlayListData playListData = playListDataDao.getPlayListData();
        if (playListData != null && playListData.audioList != null) {
            if (PlayScene.LOCAL_MUSIC == playListData.scene
                    || PlayScene.FAVOUR_MUSIC == playListData.scene
                    || PlayScene.HISTORY_MUSIC == playListData.scene) {
                Iterator<Audio> iterator = playListData.audioList.iterator();
                while (iterator.hasNext()) {
                    Audio audio = iterator.next();
                    if (audio != null && audio.sid == 0 && !FileUtils.isExist(audio.sourceUrl)) {
                        if (playListData.audio != null && playListData.audio.equals(audio)) {
                            playListData.audio = null;
                        }
                        iterator.remove();
                    }
                }
            }

            // 如果是历史音乐场景，剔除当前数据库中没有的记录
            if (PlayScene.HISTORY_MUSIC == playListData.scene) {
                List<HistoryAudio> historyAudios = DBUtils.getDatabase(GlobalContext.get()).getHistoryAudioDao().listAll();
                Iterator<Audio> iterator = playListData.audioList.iterator();
                while (iterator.hasNext()) {
                    Audio audio = iterator.next();
                    if (audio != null) {
                        Boolean isFromSound = audio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                        if (isFromSound != null && isFromSound) {
                            continue;
                        }
                        boolean hasFind = false;
                        for (HistoryAudio historyAudio : historyAudios) {
                            if (audio.sid == historyAudio.sid && audio.id == historyAudio.id) {
                                hasFind = true;
                                break;
                            }
                        }
                        if (!hasFind) {
                            if (playListData.audio != null && playListData.audio.equals(audio)) {
                                playListData.audio = null;
                            }
                            iterator.remove();
                        }
                    }
                }
            }

            // 如果是收藏音乐场景，剔除当前数据库中没有的记录
            if (PlayScene.FAVOUR_MUSIC == playListData.scene) {
                List<FavourAudio> favourAudios = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().listAll();
                Iterator<Audio> iterator = playListData.audioList.iterator();
                while (iterator.hasNext()) {
                    Audio audio = iterator.next();
                    if (audio != null) {
                        Boolean isFromSound = audio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                        if (isFromSound != null && isFromSound) {
                            continue;
                        }
                        boolean hasFind = false;
                        for (FavourAudio favourAudio : favourAudios) {
                            if (audio.sid == favourAudio.sid && audio.id == favourAudio.id) {
                                hasFind = true;
                                break;
                            }
                        }
                        if (!hasFind) {
                            if (playListData.audio != null && playListData.audio.equals(audio)) {
                                playListData.audio = null;
                            }
                            iterator.remove();
                        }
                    }
                }
            }

            // 如果是收藏音乐场景，剔除当前数据库中没有的记录
            if (PlayScene.FAVOUR_MUSIC == playListData.scene) {
                List<FavourAudio> favourAudios = DBUtils.getDatabase(GlobalContext.get()).getFavourAudioDao().listAll();
                Iterator<Audio> iterator = playListData.audioList.iterator();
                while (iterator.hasNext()) {
                    Audio audio = iterator.next();
                    if (audio != null) {
                        Boolean isFromSound = audio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                        if (isFromSound != null && isFromSound) {
                            continue;
                        }
                        boolean hasFind = false;
                        for (FavourAudio favourAudio : favourAudios) {
                            if (audio.sid == favourAudio.sid && audio.id == favourAudio.id) {
                                hasFind = true;
                                break;
                            }
                        }
                        if (!hasFind) {
                            if (playListData.audio != null && playListData.audio.equals(audio)) {
                                playListData.audio = null;
                            }
                            iterator.remove();
                        }
                    }
                }
            }

            // 如果是微信推送场景，剔除当前数据库中没有的记录
            if (PlayScene.WECHAT_PUSH == playListData.scene) {
                List<PushItem> pushItems = DBUtils.getDatabase(GlobalContext.get()).getPushItemDao().listAll();
                Iterator<Audio> iterator = playListData.audioList.iterator();
                while (iterator.hasNext()) {
                    Audio audio = iterator.next();
                    if (audio != null) {
                        Boolean isFromSound = audio.getExtraKey(Constant.AudioExtra.FROM_SOUND_CHOICE);
                        if (isFromSound != null && isFromSound) {
                            continue;
                        }
                        boolean hasFind = false;
                        for (PushItem pushItem : pushItems) {
                            if (audio.sid == pushItem.sid && audio.id == pushItem.id) {
                                hasFind = true;
                                break;
                            }
                        }
                        if (!hasFind) {
                            if (playListData.audio != null && playListData.audio.equals(audio)) {
                                playListData.audio = null;
                            }
                            iterator.remove();
                        }
                    }
                }
            }
        }
        return playListData;
    }
}
