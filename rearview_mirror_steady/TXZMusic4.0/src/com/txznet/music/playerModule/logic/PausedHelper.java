package com.txznet.music.playerModule.logic;

/**
 * @author zackzhou
 * @date 2019/2/28,15:22
 */

public class PausedHelper {

    private static final class Holder {
        private static final PausedHelper INSTANCE = new PausedHelper();
    }

    public static PausedHelper get() {
        return Holder.INSTANCE;
    }

    private boolean isWait2Play;

    // 准备播放
    public void notifyWait2Play() {
        isWait2Play = true;
    }

    // 播放中
    public void notifyPlaying() {
        isWait2Play = false;
        hasPausedBeforePlay = false;
    }

    // 被暂停
    public void notifyPaused() {
        hasPausedBeforePlay = isWait2Play;
    }

    private boolean hasPausedBeforePlay;

    public boolean hasPausedBeforePlay() {
        return hasPausedBeforePlay;
    }
}
