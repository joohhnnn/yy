package com.txznet.music.localModule.logic;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.albumModule.bean.Album;

/**
 * Created by brainBear on 2017/9/11.
 */

public class AlbumUtils {

    private static int[] counts = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000, 1000000000};
    private static final int PROPERTY_NOVEL_STATUS = 0;

    /**
     * 得到一个正整形十进制数字某一位的数字，从低位第一位开始
     *
     * @param number
     * @param position
     */
    public static int getNumInPosition(int number, int position) {
        if (position < 0 || position > 10) {
            throw new IllegalStateException("out of range");
        }
        int value = number / counts[position];
        if (value >= 0 && value <= 9) {
            return value;
        }
        return value % 10;
    }


    /**
     * 是否展示详情页
     *
     * @return
     */
    public static boolean isShowDetail(Album album) {
        return getNumInPosition(album.getFlag(), 0) == 1;
    }



    public static boolean isTalkShow(Album album) {
        if (album.getCategoryId() >= 1000000 && album.getCategoryId() < 1100000) {
            return true;
        }
        if (CollectionUtils.isEmpty(album.getArrCategoryIds())) {
            return false;
        }
        for (long i : album.getArrCategoryIds()) {
            if (i >= 1000000 && i < 1100000) {
                return true;
            }
        }
        return false;
    }

    public static boolean isNovel(Album album) {
        if (album.getCategoryId() >= 500000 && album.getCategoryId() < 600000) {
            return true;
        }
        if (CollectionUtils.isEmpty(album.getArrCategoryIds())) {
            return false;
        }
        for (long i : album.getArrCategoryIds()) {
            if (i >= 500000 && i < 600000) {
                return true;
            }
        }
        return false;
    }

    /**
     * 根据现有的字段生成properties
     */
    public static void generateProperties(Album album) {
        int property = 0;
        if (album.getSerialize() != 0) {
            property += album.getSerialize() * counts[PROPERTY_NOVEL_STATUS];
        }
        album.setProperties(property);
    }


    /**
     * 根据已有properties得到对应的属性值
     */
    public static void analysisProperties(Album album) {
        album.setSerialize(AlbumUtils.getNumInPosition(album.getProperties(), PROPERTY_NOVEL_STATUS));
    }
}
