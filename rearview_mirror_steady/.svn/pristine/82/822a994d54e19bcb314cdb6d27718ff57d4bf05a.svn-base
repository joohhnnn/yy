package com.txznet.txz.component.media.util;

import com.txznet.txz.component.media.loader.MediaToolLoadInfo;

import java.util.List;

/**
 * 内部加载媒体工具选择
 * Created by Jun on 2021/01/26.
 */
public class MediaToolChoose {

    /**
     * 比较两个适配版本哪个更高targetVersion更高返回true否则返回false
     *
     * @param targetVersion
     * @param compareVersion
     * @return
     */
    private static boolean isVersionMax(String targetVersion, String compareVersion) {
        String[] targetVer = targetVersion.split("\\.");
        String[] compareVer = compareVersion.split("\\.");
        int target = 0;
        int compare = 0;
        int len = Math.min(targetVer.length, compareVer.length);
        for (int i = 0; i < len; i++) {
            target = Integer.parseInt(targetVer[i]) + target * 100;
            compare = Integer.parseInt(compareVer[i]) + compare * 100;
        }
        if (target > compare) {
            return true;
        }
        return false;
    }

    /**
     * 校验app版本是否属于内置适配的最低到最高之间（包含最高最低）
     *
     * @param versionMin
     * @param versionMax
     * @param appVersion
     * @return
     */
    private static boolean compare(String versionMin, String versionMax, String appVersion) {
        String[] verMin = versionMin.split("\\.");
        String[] verMax = versionMax.split("\\.");
        String[] appVer = appVersion.split("\\.");
        int min = 0;
        int max = 0;
        int app = 0;
        int len = Math.min(verMin.length, appVer.length);
        for (int i = 0; i < len; i++) {
            min = Integer.parseInt(verMin[i]) + min * 100;
            max = Integer.parseInt(verMax[i]) + max * 100;
            app = Integer.parseInt(appVer[i]) + app * 100;
        }
        if (min <= app && app < max) {
            return true;
        }
        return false;
    }


    /**
     * 选择与app版本对应的sdk
     *
     * @param mediaToolList
     * @param version
     * @return
     */
    public static MediaToolLoadInfo mediaToolChoose(List<MediaToolLoadInfo> mediaToolList, String version) {
        //记录最高与最低版本
        MediaToolLoadInfo mediaToolVersionMin = null;
        MediaToolLoadInfo mediaToolVersionMax = null;
        for (int i = mediaToolList.size() - 1; i >= 0; i--) {
            if (compare(mediaToolList.get(i).targetVersionMin, mediaToolList.get(i).targetVersionMax, version)) {
                return mediaToolList.get(i);
            }
        }
        //没匹配到相对应的适配则返回最高或最低的适配,正常情况下不会走下面代码
        for (MediaToolLoadInfo info : mediaToolList) {
            if (mediaToolVersionMax == null && mediaToolVersionMin == null) {
                mediaToolVersionMax = info;
                mediaToolVersionMin = info;
                continue;
            }
            //记录最大版本
            if (!isVersionMax(mediaToolVersionMax.targetVersionName, info.targetVersionName)) {
                mediaToolVersionMax = info;
            }
            //记录最小版本
            if (isVersionMax(mediaToolVersionMin.targetVersionName, info.targetVersionName)) {
                mediaToolVersionMin = info;
            }
        }
        if (isVersionMax(mediaToolVersionMax.targetVersionMax, version)) {
            return mediaToolVersionMin;
        } else {
            return mediaToolVersionMax;
        }
    }

}
