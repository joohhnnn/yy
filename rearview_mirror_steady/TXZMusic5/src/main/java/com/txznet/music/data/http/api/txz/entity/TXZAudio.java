package com.txznet.music.data.http.api.txz.entity;

import java.util.List;
import java.util.Map;

public class TXZAudio {

    public static final String DOWNLOADTYPE_DIRECT = "2";//路径不转变,直接使用该路径
    public static final String DOWNLOADTYPE_PROXY = "1";//路径转变,变化的形式为:TXZUri


    public static final String splitChar = "-";//主要用于在字段中间进行拼接，在此处声明，有助于，同行者的Audio全部使用该关键字进行拼接，以便后期可能的改动

    /**
     * sid : 3
     * id : 104402033
     * name : 【非常溜佳期】管管孩子，救救游戏！【荣耀play酷玩特约播出】
     * albumId : 1000527
     * arrArtistName : []
     * artist_ids : ["3945648"]
     * strCategoryId : 0
     * bShowSource : true
     * bNoCache : false
     * downloadType : 0
     * strDownloadUrl : http://audio.xmcdn.com/group46/M08/A9/76/wKgKj1tqdo6zFeekAKyt_-bCBvk941.mp3
     * strProcessingUrl : http://audio.xmcdn.com/group46/M08/A9/76/wKgKj1tqdo6zFeekAKyt_-bCBvk941.mp3
     * iExpTime : 1542669614
     * albumName : 非常溜佳期
     * report : 【非常溜佳期】管管孩子，救救游戏！【荣耀play酷玩特约播出】
     * sourceFrom : 喜马拉雅
     * score : 1000
     * flag : 1
     * wakeUp : []
     */

    public int sid;
    public long id;
    public String name;
    /**
     * 为string的原因是：在album/audio的时候返回了long型，再text/search的时候返回了string，
     */
    public String albumId;
    public int albumSid;
    public String albumPic; // 专辑图片
    public String strCategoryId;// 当前歌曲类别
    public boolean bShowSource;// 是否显示音乐源
    public boolean bNoCache;// 缓存标记位
    public String downloadType;// 1:qq 需要预处理， 2：考拉，可直接用dowloadUrl下载
    public String strDownloadUrl;
    public String strProcessingUrl;// qq音乐才需要
    public int processIsPost; // 是否需要post请求
    public Map<String, Object> processHeader; // post请求的请求头
    public long iExpTime;// 超时时间
    public String albumName;//真实所在专辑的名称！！！！
    public String report;// tts播报的内容
    public String sourceFrom;// 音频内容来源
    public int flag;//十进制,标志位，用于控制音频的一些操作（后台控制）,第一位:表示是否播报(1,播报,0,不播报),第二位:表示是否支持收藏(0,不支持收藏,1,支持收藏),第三位:表示是否收藏(1,订阅,0,未订阅)
    public List<String> arrArtistName;// 歌手
    public List<String> wakeUp; // 支持的唤醒词

    public int score; //打分，后台用于排序 小于1000为已收听，大于1000小于10000为未收听

    public String svrData;

    @Override
    public String toString() {
        return "TXZAudio{" +
                "sid=" + sid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", albumId='" + albumId + '\'' +
                ", albumSid=" + albumSid +
                '}';
    }
}
