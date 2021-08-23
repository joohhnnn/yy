package com.txznet.music.util;

import com.txznet.audio.player.entity.Audio;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.data.entity.PlayConfs;
import com.txznet.music.data.sp.SharedPreferencesUtils;
import com.txznet.proxy.util.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AudioUtils {
    public static final String UNDERLINE = "_";
    public static final String TMD_POSTFIX = ".tmd";

    private AudioUtils() {

    }

    /**
     * 通过sid判断音频是否为音乐
     *
     * @param sid sid
     * @return 如果是音乐则返回true
     */
    public static boolean isSong(int sid) {
        boolean localSong = isLocalSong(sid);
        if (localSong) {
            return true;
        }
        return isNetSong(sid);
    }

    /**
     * 根据sid判断音频是否真实来源于本地
     *
     * @param sid sid
     */
    public static boolean isLocalSong(int sid) {
        return sid == 0;
    }

    /**
     * 根据sid判断音频是否来源于微信推送
     *
     * @param sid sid
     */
    public static boolean isPushItem(int sid) {
        return sid == 24;
    }

    /**
     * 是否存在本地tmd文件
     */
    public static boolean existsLocal(AudioV5 audio) {
        return FileUtils.isExist(getLocalUrl(audio));
    }

    /**
     * 通过sid判断音频是否为音乐
     *
     * @param sid sid
     * @return 如果是音乐则返回true
     */
    public static boolean isNetSong(int sid) {
        if (getConfig() != null && getConfig().arrPlay != null) {
            for (PlayConfs.PlayConf playConf : getConfig().arrPlay) {
                if (playConf.sid == sid) {
                    return playConf.type == PlayConfs.PlayConf.MUSIC_TYPE;
                }
            }
        }
        return false;
    }

    private static PlayConfs mRespCheck;

    public static PlayConfs getConfig() {
        if (mRespCheck == null) {
            mRespCheck = JsonHelper.fromJson(SharedPreferencesUtils.getConfig(), PlayConfs.class);
        }
        return mRespCheck;
    }

    public static void resetPlayConfs(PlayConfs playConfs) {
        mRespCheck = playConfs;
    }

    public static File getAudioTMDFile(Audio audio) {
        if (isSong(audio.sid)) {
            return new File(StorageUtil.getTmdDir(), audio.id + UNDERLINE + audio.sid + TMD_POSTFIX);
        }
        return null;
    }

    public static File getAudioTMDFile(AudioV5 audio) {
        if (isSong(audio.sid)) {
            return new File(StorageUtil.getTmdDir(), audio.id + UNDERLINE + audio.sid + TMD_POSTFIX);
        }
        return null;
    }

    public static String getLocalUrl(AudioV5 audio) {
        if (audio == null) {
            return "";
        }
        if (isLocalSong(audio.sid) && audio.sourceUrl != null && !audio.sourceUrl.startsWith("txz") && !audio.sourceUrl.startsWith("http")) {
            //真实本地音乐，来源于SD卡中的，自带音乐
            return audio.sourceUrl;
        } else {
            if (audio instanceof LocalAudio) {
                if (((LocalAudio) audio).path != null) {
                    return ((LocalAudio) audio).path;
                }
            }
            //来源于缓存的同听音乐
            return StorageUtil.getTmdDir() + File.separator + audio.id + UNDERLINE + audio.sid + TMD_POSTFIX;
        }
    }

    /**
     * 移除列表中的文件不存在的本地音频
     */
    public static void removeLocalNotExists(List<? extends AudioV5> audioList) {
        List<AudioV5> bRemove = new ArrayList<>();
        for (AudioV5 audio : audioList) {
            if (audio.sourceUrl == null) {
                bRemove.add(audio);
                continue;
            }
            if (AudioUtils.isLocalSong(audio.sid) && !FileUtils.isExist(audio.sourceUrl)) {
                bRemove.add(audio);
            }
        }
        audioList.removeAll(bRemove);
    }
}
