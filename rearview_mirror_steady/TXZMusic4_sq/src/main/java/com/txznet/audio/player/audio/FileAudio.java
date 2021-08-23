package com.txznet.audio.player.audio;

import java.io.File;
import java.util.Locale;

public class FileAudio extends PlayerAudio {
    private String mPath;

    public FileAudio(String path) {
        mPath = path;
    }

    public String getPath() {
        return mPath;
    }

    @Override
    public boolean needCodecPlayer() {
        return mPath.toLowerCase(Locale.CHINESE).endsWith(".opus");
    }

    @Override
    public String getAudioName() {
        return new File(mPath).getName();
    }

}
