package com.txznet.music.util;

import com.txznet.music.data.entity.PlayConfs;

/**
 * 来源
 *
 * @author zackzhou
 * @date 2018/12/25,19:50
 */

public class SourceFromUtils {

    private SourceFromUtils() {
    }

    /**
     * 根据sid获取音频来源
     */
    public static String getSourceFrom(int sid) {
        PlayConfs conf = AudioUtils.getConfig();
        if (conf != null && conf.arrPlay != null) {
            for (PlayConfs.PlayConf playConf : conf.arrPlay) {
                if (playConf != null && playConf.sid == sid && playConf.sourceFrom != null) {
                    return playConf.sourceFrom;
                }
            }
        }
        String sourceFrom;
        switch (sid) {
            case 0:
                sourceFrom = "本地音乐";
                break;
            case 1:
                sourceFrom = "考拉";
                break;
            case 2:
                sourceFrom = "QQ音乐";
                break;
            case 3:
                sourceFrom = "喜马拉雅";
                break;
            case 4:
                sourceFrom = "多听";
                break;
            case 5:
                sourceFrom = "考拉";
                break;
            case 6:
                sourceFrom = "QQ音乐";
                break;
            case 7: //酷我显示QQ音乐
                sourceFrom = "QQ音乐";
                break;
            case 8:
                sourceFrom = "乐听";
                break;
            case 9:
                sourceFrom = "考拉";
                break;
            case 10:
                sourceFrom = "企鹅FM";
                break;
            case 11:
                sourceFrom = "百度排行榜";
                break;
            case 12:
                sourceFrom = "听书网";
                break;
            case 13:
                sourceFrom = "听书网";
                break;
            case 20:
                sourceFrom = "QQ音乐";
                break;
            case 21:
                sourceFrom = "QQ音乐";
                break;
            case 22:
                sourceFrom = "QQ音乐";
                break;
            case 23:
                sourceFrom = "喜马拉雅";
                break;
            case 24:
                sourceFrom = "微信推送";
                break;
            default:
                sourceFrom = "未知";
                break;
        }
        return sourceFrom;
    }
}
