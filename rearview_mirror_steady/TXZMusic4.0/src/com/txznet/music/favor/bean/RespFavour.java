package com.txznet.music.favor.bean;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/8.
 */

public class RespFavour {

    private long operTime;//请求的收藏时间戳,作用:用于获取本地(仅限sd卡中的数据)startTime到endtime(endtime=audio里面最后一条的时间戳中间的数据.如果没有数据,则endtime=0)
    private List<ResponseFavourBean> arrAudioStore;

    public List<ResponseFavourBean> getArrAudioStore() {
        return arrAudioStore;
    }

    public void setArrAudioStore(List<ResponseFavourBean> arrAudioStore) {
        this.arrAudioStore = arrAudioStore;
    }

    public long getOperTime() {
        return operTime;
    }

    public void setOperTime(long operTime) {
        this.operTime = operTime;
    }


    @Override
    public String toString() {
        return "RespFavour{" +
                "operTime=" + operTime +
                ", arrAudioStore=" + arrAudioStore +
                '}';
    }
}
