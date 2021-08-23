package com.txznet.music.helper;

import com.txznet.jni.TXZStrComparator;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.data.entity.LocalAudio;
import com.txznet.music.util.LoadUtils;
import com.txznet.music.util.Logger;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 本地音乐排序工具
 *
 * @author zackzhou
 * @date 2018/12/21,10:11
 */

public class LocalAudioSortUtil {

    private LocalAudioSortUtil() {

    }

    /**
     * 按名称排序
     */
    public static void sortAudiosByName(List<LocalAudio> audios) {
        Logger.d("sortAudios", "sortAudiosByName");
        int retryTime = 0;
        while (true) {
            int loadPinyinFileCode = LoadUtils.initPinyin();
            if (loadPinyinFileCode != 0 && retryTime <= 5) {
                LoadUtils.loadPinyinData();
                retryTime++;
            } else {
                break;
            }
        }
        if (retryTime <= 5) {
            // 排序
            Collections.sort(audios, (Comparator<AudioV5>) (lhs, rhs) -> TXZStrComparator.compareChinese(lhs.name, rhs.name));
            TXZStrComparator.release();
        } else {
            Logger.e("sortAudios", "load pingyin file error");
            Collections.sort(audios, (Comparator<AudioV5>) (lhs, rhs) -> lhs.name.compareTo(rhs.name));
        }
    }

    /**
     * 按扫描时间排序
     */
    public static void sortAudiosByTime(List<LocalAudio> audios) {
        Logger.d("sortAudios", "sortAudiosByTime");
        Collections.sort(audios, (o1, o2) -> {
            if (o1.createTime == o2.createTime) {
                return 0;
            }
            return o1.createTime > o2.createTime ? -1 : 1;
        });
    }
}
