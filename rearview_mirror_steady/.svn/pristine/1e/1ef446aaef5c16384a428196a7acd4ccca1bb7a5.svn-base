package com.txznet.audio.player.audio;

import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.playerModule.bean.PlayItem;
import com.txznet.music.utils.Utils;

public class QQMusicAudio extends NetAudio {

    public String murlHashCode = "";
    private String mFinalUrl;

    public QQMusicAudio(PlayItem playItem, Audio audio) {
        super(playItem, audio);
    }

    /**
     * 获取当前url可以播放的hashcode,由后台返回
     */
//    public String getUrlHashCode() {
//        return murlHashCode;
//    }

//    public void setUrlHashCode(String urlHashCode) {
//        murlHashCode = urlHashCode;
//    }
    @Override
    public boolean needCodecPlayer() {
        return false;
    }

    @Override
    public String getCacheId() {
        StringBuilder sb = new StringBuilder();
        sb.append(mAudio.getUrlType())
                .append(Utils.UNDERLINE)
                .append(mAudio.getSid())
                .append(Utils.UNDERLINE)
                .append(mAudio.getId())
                .append(Utils.UNDERLINE)
                .append(getUrlHashCode());
        return sb.toString();
//        return calCacheId(mAudio.getUrlType() + UNDERLINE + mAudio.getSid() + UNDERLINE + mAudio.getId() + UNDERLINE + murlHashCode);
    }


    private String getUrlHashCode() {
        String flag = "&ttth=";
        int index = mPlayItem.getUrls().get(mUrlIndex).indexOf(flag);
        return mPlayItem.getUrls().get(mUrlIndex).substring(index + flag.length());
    }

    /**
     * 获取音频数据
     */
    public Audio getAudio() {
        return mAudio;
    }

    public String getFinalUrl() {
        return mFinalUrl;
    }

    public void setFinalUrl(String mFinalUrl) {
        this.mFinalUrl = mFinalUrl;
    }

}
