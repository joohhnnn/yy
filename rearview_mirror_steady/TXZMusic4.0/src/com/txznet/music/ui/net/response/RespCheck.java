package com.txznet.music.ui.net.response;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.music.ui.bean.FlashPage;
import com.txznet.music.ui.bean.PlayConf;

import java.util.List;

public class RespCheck {
    private long logoTag;
    private List<PlayConf> arrPlay;
    private FlashPage flashPage;

    public long getLogoTag() {
        return logoTag;
    }

    public void setLogoTag(long logoTag) {
        this.logoTag = logoTag;
    }

    public List<PlayConf> getArrPlay() {
        return arrPlay;
    }

    public void setArrPlay(List<PlayConf> arrPlay) {
        this.arrPlay = arrPlay;
    }

    public FlashPage getFlashPage() {
        return flashPage;
    }

    public void setFlashPage(FlashPage flashPage) {
        this.flashPage = flashPage;
    }

    @Override
    public String toString() {
        return "RespCheck{" +
                "logoTag=" + logoTag +
                ", arrPlay=" + CollectionUtils.toString(arrPlay) +
                ", flashPage=" + flashPage +
                '}';
    }
}
