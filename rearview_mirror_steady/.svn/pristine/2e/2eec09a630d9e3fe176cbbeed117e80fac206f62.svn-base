package com.txznet.music.service.push;

import com.txznet.comm.remote.util.TtsUtil;
import com.txznet.music.data.entity.PushItem;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;
import com.txznet.music.data.http.api.txz.entity.resp.PushResponse;
import com.txznet.music.helper.AudioConverts;
import com.txznet.music.util.Logger;
import com.txznet.music.util.Utils;
import com.txznet.proxy.util.StorageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 推送辅助工具
 *
 * @author zackzhou
 * @date 2019/3/26,11:43
 */

public class PushUtils {

    private PushUtils() {
    }

    public static boolean checkTipFileExists(String tip) {
        return getCacheFile(tip).exists();
    }

    public static File getCacheFile(String tip) {
        if (tip == null) {
            return null;
        }
        return new File(StorageUtil.getOtherCacheDir(), String.valueOf(tip.hashCode()));
    }

    public static String getCacheFilePath(String tip) {
        File file = getCacheFile(tip);
        return file == null ? null : file.getAbsolutePath();
    }

    public interface ISpeakTask {
        int doTask();
    }

    private static int mLastTtsId = TtsUtil.INVALID_TTS_TASK_ID;

    public static void speakText(ISpeakTask task) {
        cancelSpeak();
        mLastTtsId = task.doTask();
        Logger.d("PushUtils", "speakText, ttsId=" + mLastTtsId);
    }

    public static void cancelSpeak() {
        Logger.d("PushUtils", "cancelSpeak, ttsId=" + mLastTtsId);
        TtsUtil.cancelSpeak(mLastTtsId);
    }

    public static List<PushItem> getPushItem(PushResponse pushResponse, AudioConverts.Convert<TXZAudio, PushItem> convert) {
        List<PushItem> pushItems = new ArrayList<>();
        if (pushResponse != null && PushResponse.PUSH_SERVICE_AUDIOS.equals(pushResponse.getService())) {
            if (pushResponse.getArrAudio() != null && pushResponse.getArrAudio().size() > 0) {
                for (int size = pushResponse.getArrAudio().size() - 1; size >= 0; size--) {
                    TXZAudio txzAudio = pushResponse.getArrAudio().get(size);
                    pushItems.add(AudioConverts.convert2PushItem(txzAudio, PushItem.STATUS_UNREAD));
                }
            }
        }
        Collections.reverse(pushItems);
        Utils.deleteSameAudiosFromSource(pushItems);
        return pushItems;
    }
}
