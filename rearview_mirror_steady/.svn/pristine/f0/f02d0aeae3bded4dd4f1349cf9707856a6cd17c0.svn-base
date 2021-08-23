package com.txznet.music.helper;

import com.txznet.audio.player.entity.Audio;
import com.txznet.music.BuildConfig;
import com.txznet.music.Constant;
import com.txznet.music.data.entity.AudioV5;
import com.txznet.music.util.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放队列工具类
 *
 * @author zackzhou
 * @date 2018/12/13,18:04
 */

public class PlayQueueHelper {
    private static final class Holder {
        private static final PlayQueueHelper INSTANCE = new PlayQueueHelper();
    }

    public static PlayQueueHelper get() {
        return PlayQueueHelper.Holder.INSTANCE;
    }

    /**
     * 移除播放队列中已经包含的音频
     */
    public void distinctQueue(List<Audio> queue, List<Audio> bInsert) {
        List<Audio> bRemove = new ArrayList<>();
        for (Audio audio : bInsert) {
            if (queue.contains(audio)) {
                bRemove.add(audio);
            }
        }
        bInsert.removeAll(bRemove);
        if (BuildConfig.DEBUG && !bRemove.isEmpty()) {
            Logger.d(Constant.LOG_TAG_LOGIC, "distinctQueue " + bRemove);
        }
    }


    /**
     * 移除播放队列中已经包含的音频
     */
    public void distinctQueueV5(List<Audio> queue, List<AudioV5> bInsert) {
        List<AudioV5> bRemove = new ArrayList<>();
        for (AudioV5 audioV5 : bInsert) {
            for (Audio audio : queue) {
                if (audio.sid == audioV5.sid && audio.id == audioV5.id) {
                    bRemove.add(audioV5);
                    break;
                }
            }
        }
        bInsert.removeAll(bRemove);
        if (BuildConfig.DEBUG && !bRemove.isEmpty()) {
            Logger.d(Constant.LOG_TAG_LOGIC, "distinctQueue " + bRemove);
        }
    }
}
