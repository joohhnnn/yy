package com.txznet.music.data.entity;

import java.util.ArrayList;
import java.util.List;

public class SearchResult {


    public static final int SELECTPLAY = 0;// 选择播放
    public static final int GOPLAY = 1;// 直接播放
    public static final int DELAYPLAY = 2;// 延时播放

    public static final int TYPE_MIX = 3;// 复合类型


    public int returnType;// 返回类型，3复合，1audio，2album有值；
    public int playType;// 0 选择，1，直接播放2.延时播放
    public int delayTime;// 服务器协商(ms)
    public int playIndex;// 播放的下标

    //    public List<Album> albums;
//    public List<AudioV5> audios;
    public List<Mix> arrMix;// 混合类型，包括Album，和Audio的混排

    public String keyword; // 搜索关键字


    public SearchResult() {
//        albums = new ArrayList<>();
//        audios = new ArrayList<>();
        arrMix = new ArrayList<>();
    }

    public static class Mix {
        public AudioV5 audio;
        public Album album;
    }

    public Object choice(int index) {
        /*if (albums.size() > 0 && albums.size() > index) {
            return albums.get(index);
        } else if (audios.size() > 0 && audios.size() > index) {
            return audios.get(index);
        } else */
        if (arrMix.size() > 0 && arrMix.size() > index) {
            Mix mix = arrMix.get(index);
            if (mix.audio != null) {
                return mix.audio;
            }
            if (mix.album != null) {
                return mix.album;
            }
        }
        return null;
    }

    public List<AudioV5> convert2AudioList() {
        List<AudioV5> result = new ArrayList<>();
        if (arrMix.size() > 0) {
            for (Mix mix : arrMix) {
                if (mix.audio != null) {
                    result.add(mix.audio);
                }
            }
        }
        return result;
    }

    public List<Album> convert2AlbumList() {
        List<Album> result = new ArrayList<>();
        if (arrMix.size() > 0) {
            for (Mix mix : arrMix) {
                if (mix.album != null) {
                    result.add(mix.album);
                }
            }
        }
        return result;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "returnType=" + returnType +
                ", playType=" + playType +
                ", delayTime=" + delayTime +
                ", playIndex=" + playIndex +
                ", arrMix=" + arrMix +
                ", keyword='" + keyword + '\'' +
                '}';
    }
}
