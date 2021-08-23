package com.txznet.sdk.tongting;

public class TongTingUtils {

    private static int POS_SUPPORT_FAVOUR = 0;
    private static int POS_FAVOUR = 1;
    private static int POS_SUPPORT_SUBSCRIBE = 2;
    private static int POS_SUBSCRIBE = 3;


    /**
     * 获取当前播放的歌曲或电台是否为收藏状态
     * 第一位 表明是否支持收藏
     * 第二位 表明是否收藏
     * 第三位 表明是否支持订阅
     * 第四位 表明是否订阅
     *
     * @return
     */
    public static int getFavourState(
            int support_fav,
            int favour,
            int support_sub,
            int subscribe
    ) {
        int a = support_fav << POS_SUPPORT_FAVOUR;
        int b = favour << POS_FAVOUR;
        int c = support_sub << POS_SUPPORT_SUBSCRIBE;
        int d = subscribe << POS_SUBSCRIBE;

        return a + b + c + d;
    }

    private static int valueAtBit(int num, int bit) {
        return (num >> (bit)) & 1;
    }


    public static boolean supportFavour(int favourState) {
        return valueAtBit(favourState, POS_SUPPORT_FAVOUR) == 1;
    }

    public static boolean supportSubscribe(int favourState) {
        return valueAtBit(favourState, POS_SUPPORT_SUBSCRIBE) == 1;
    }

    public static boolean isFavour(int favourState) {
        return valueAtBit(favourState, POS_FAVOUR) == 1;
    }

    public static boolean isSubscribe(int favourState) {
        return valueAtBit(favourState, POS_SUBSCRIBE) == 1;
    }


}
