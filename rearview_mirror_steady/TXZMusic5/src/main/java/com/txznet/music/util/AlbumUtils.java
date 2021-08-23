package com.txznet.music.util;

import com.txznet.music.data.entity.Album;

/**
 * 专辑辅助工具
 *
 * @author zackzhou
 * @date 2018/12/12,16:06
 */

public class AlbumUtils {
    private static int[] COUNTS = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};

    private AlbumUtils() {
    }

    /**
     * 得到一个正整形十进制数字某一位的数字，从低位第一位开始
     */
    public static int getNumInPosition(int number, int position) {
        if (position < 0 || position > 10) {
            throw new IllegalStateException("out of range");
        }
        int value = number / COUNTS[position];
        if (value >= 0 && value <= 9) {
            return value;
        }
        return value % 10;
    }

    /**
     * 判断专辑是否脱口秀
     */
    public static boolean isTalkShow(Album album) {
        return Album.ALBUM_TYPE_TALK_SHOW == album.albumType;
    }

    /**
     * 判断专辑是否小说
     */
    public static boolean isNovel(Album album) {
        return Album.ALBUM_TYPE_NOVEL == album.albumType;
    }

    /**
     * 判断专辑是否新闻
     */
    public static boolean isNews(Album album) {
        return Album.ALBUM_TYPE_NEWS == album.albumType || Album.ALBUM_TYPE_RECOMMEND == album.albumType;
    }

    /**
     * 判断专辑是否音乐
     */
    public static boolean isMusic(Album album) {
        return Album.ALBUM_TYPE_MUSIC == album.albumType;
    }

    /**
     * 判断专辑是否AI电台
     */
    public static boolean isAiRadio(Album album) {
        return album != null && (album.id == 1000001 && album.sid == 100);
    }

    /**
     * 判断专辑是否每日推荐20首
     */
    public static boolean isRecommend(Album album) {
        return album != null && album.id == 1000002 && album.sid == 100;
    }

    /**
     * 判断专辑是否歌单、小说、新闻以外的分类
     */
    public static boolean isOther(Album album) {
        return !isMusic(album) && !isNovel(album) && !isNews(album);
    }

    /**
     * 判断专辑是否用户常听电台
     */
    public static boolean isUserRadio(Album album) {
        return album != null && (album.id == 1000004 && album.sid == 100);
    }

    /**
     * 判断专辑是否用户常听音乐
     */
    public static boolean isUserMusic(Album album) {
        return album != null && (album.id == 1000005 && album.sid == 100);
    }
}
