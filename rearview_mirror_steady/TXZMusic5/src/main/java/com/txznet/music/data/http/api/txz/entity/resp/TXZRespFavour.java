package com.txznet.music.data.http.api.txz.entity.resp;

import com.txznet.music.data.http.api.txz.entity.TXZAlbum;
import com.txznet.music.data.http.api.txz.entity.TXZAudio;

import java.util.List;

/**
 * Created by telenewbie on 2017/12/8.
 */

public class TXZRespFavour {

    public static class FavourBean {
        public long operTime;   //收藏时间
        public TXZAlbum album;
        public TXZAudio audio;
    }

    public long operTime;//请求的收藏时间戳,作用:用于获取本地(仅限sd卡中的数据)startTime到endtime(endtime=audio里面最后一条的时间戳中间的数据.如果没有数据,则endtime=0)
    public List<FavourBean> arrAudioStore;


}
