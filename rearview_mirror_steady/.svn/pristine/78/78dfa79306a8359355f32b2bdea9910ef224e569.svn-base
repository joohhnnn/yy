package com.txznet.music.data.http.api.txz.entity.resp;

import com.txznet.music.data.http.api.txz.entity.TXZSearchData;

import java.util.List;

public class TXZRespSearch extends TXZRespBase {

    public static final int SELECTPLAY = 0;// 选择播放
    public static final int GOPLAY = 1;// 直接播放
    public static final int DELAYPLAY = 2;// 延时播放


    public static final int TYPE_AUDIO = 1;// 音频类型
    public static final int TYPE_ALBUM = 2;// 专辑类型
    public static final int TYPE_MIX = 3;// 复合类型


    public int returnType;// 返回类型，3复合，1audio，2album有值；
    public int playType;// 0 选择，1，直接播放2.延时播放
    public int delayTime;// 服务器协商(ms)
    public int playIndex;// 播放的下标
//    public List<TXZAudio> arrAudio;//Audio 集合.废弃
//    public List<TXZAlbum> arrAlbum;//Album集合.废弃

    public List<TXZSearchData> arrMix;// 混合类型，包括Album，和Audio的混排

    @Override
    public String toString() {
        return "TXZRespSearch{" +
                "errCode=" + errCode +
                ", returnType=" + returnType +
                ", playType=" + playType +
                ", delayTime=" + delayTime +
                ", playIndex=" + playIndex +
                ", arrMix=" + arrMix +
                '}';
    }
}
