package com.txznet.music.data.source;

import com.txznet.audio.player.entity.Audio;
import com.txznet.loader.AppLogic;
import com.txznet.music.Constant;
import com.txznet.music.util.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author zackzhou
 * @date 2019/2/12,11:16
 */

public class AiPushDataSource {

    private LinkedList<Audio> mAiAudioList = new LinkedList<>();
    private Audio mLastAudio;

    private static final class Holder {
        private static final AiPushDataSource INSTANCE = new AiPushDataSource();
    }

    public static AiPushDataSource get() {
        return Holder.INSTANCE;
    }

    public synchronized void push(List<Audio> audioList) {
        Logger.d(Constant.LOG_TAG_AI, "ai push " + audioList);
        for (Audio audio : audioList) {
            if (!mAiAudioList.contains(audio)) {
                mAiAudioList.add(audio);
            }
        }
        mLastAudio = mAiAudioList.getLast();
        printList();
    }

    public synchronized void pushFirst(List<Audio> audioList) {
        Logger.d(Constant.LOG_TAG_AI, "ai pushFirst " + audioList);
        Collections.reverse(audioList);
        for (Audio audio : audioList) {
            if (!mAiAudioList.contains(audio)) {
                mAiAudioList.add(0, audio);
            }
        }
        printList();
    }

    public synchronized Audio getLast() {
        return mAiAudioList.size() > 0 ? mAiAudioList.getLast() : mLastAudio;
    }

    public synchronized Audio poll() {
        return mAiAudioList.pollFirst();
    }

    public synchronized void clear() {
        mAiAudioList.clear();
    }

    public synchronized int size() {
        return mAiAudioList.size();
    }

    private synchronized void printList() {
        AppLogic.runOnBackGround(() -> {
            Logger.d(Constant.LOG_TAG_AI, "=== start println ===");
            List<Audio> audioList = new ArrayList<>(mAiAudioList);
            for (Audio audio : audioList) {
                Logger.d(Constant.LOG_TAG_AI, audio);
            }
            Logger.d(Constant.LOG_TAG_AI, "=== end println ===");
        });
    }
}
