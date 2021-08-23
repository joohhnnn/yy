package com.txznet.music.data.http.api.txz.entity.req;


import com.txznet.music.config.Configuration;
import com.txznet.txz.util.TXZFileConfigUtil;

import java.util.List;

public class TXZReqSearch extends TXZReqBase {

    public String audioName; // audio名称
    public String artist; // audio艺术家
    public String category; // 节目分类
    public String keywords;//关键字
    public String albumName; // 专辑或节目
    public String area; // 地域
    public long beginTime; // 起始时间uint32
    public long endTime; // 结束时间uint32
    public int season; // 期数
    public int version; // 版本(0的版本不会走混排)(1.支持混排)(2走酷我)（3走乐听）（4.走考拉）
    public List<Integer> arrApp; // 安装的音乐源列表
    public int index;//索引
    public int field;// 表示电台，还是歌曲（我要听“逻辑思维”)
    public String text;// 搜索的原文本
    public String subCategory;// 子分类
    public String qualitytype;// 1高音质，0低音质
    public boolean requestHistory;// 是否在请求收听历史
    public String historyType; // 请求的收听历史类型

    public TXZReqSearch() {
        super();
        version = TXZFileConfigUtil.getIntSingleConfig(Configuration.Key.TXZ_SEARCH_VERSION, Configuration.DefVal.SEARCH_VERSION);
    }

//    @Override
//    public String toString() {
//        StringBuffer sBuffer = new StringBuffer();
//        if (StringUtils.isNotEmpty(artist)) {
//            sBuffer.append(artist);
//        }
//        if (StringUtils.isNotEmpty(audioName)) {
//            sBuffer.append(audioName);
//        }
//        if (StringUtils.isNotEmpty(albumName)) {
//            sBuffer.append("专辑：").append(albumName);
//        }
//        if (StringUtils.isNotEmpty(category)) {
//            sBuffer.append("分类：").append(category);
//        }
//        sBuffer.append("的数据");
//        return sBuffer.toString();
//    }

    @Override
    public String toString() {
        return "TXZReqSearch{" +
                "audioName='" + audioName + '\'' +
                ", artist='" + artist + '\'' +
                ", category='" + category + '\'' +
                ", keywords='" + keywords + '\'' +
                ", albumName='" + albumName + '\'' +
                ", area='" + area + '\'' +
                ", beginTime=" + beginTime +
                ", endTime=" + endTime +
                ", season=" + season +
                ", version=" + version +
                ", arrApp=" + arrApp +
                ", index=" + index +
                ", field=" + field +
                ", text='" + text + '\'' +
                ", subCategory='" + subCategory + '\'' +
                ", qualitytype='" + qualitytype + '\'' +
                ", requestHistory=" + requestHistory +
                ", historyType='" + historyType + '\'' +
                '}';
    }
}
