package com.txznet.music.data.http.api.txz.entity;

import java.util.List;

public class TXZAlbum {
    /**
     * sid : 22
     * id : 50199
     * name : 车主播放榜（每天更新）
     * logo : http://static.txzing.com/audio/txzAlbum/1/50199.jpg?t=1528717139?time=1533863870926
     * arrArtistName : []
     * arrCategoryIds : [100037]
     * report : 车主播放榜
     * score : 9999
     * breakpoint : 0
     * flag : 0
     */
    public long categoryId; // 专辑id
    public int sid;// 来源
    public long id; // 专辑id
    public String name; // 专辑名称
    public String logo; // 专辑logo
    public String report;// tts播报用
    public int breakpoint;// 0 播放最新的, 1断点处续播
    /**
     * // 按十进制位设置，
     * // 从右往左，第一位：是否需要播报专辑内音频:1播报 0不播报,
     * // 第二位:表示是否支持订阅(1,不支持订阅,0,支持订阅),
     * // 第三位:表示是否订阅(1,订阅,0,未订阅)
     * //第四位：标识时是否显示（大图）
     * //第五位：标识时是否是当前推荐的专辑（车主FM分时段主题--当前时段）
     */
    public int flag;
    public List<String> arrArtistName;// 艺术家名称
    public List<Long> arrCategoryIds; // 分类ID集合 ,eg:开心的，甜美的


    /**
     * id : 356310
     * arrArtistName : []
     * arrCategoryIds : ["500005"]
     * score : 9458
     * serialize : 2
     * lastListen : 1544508413
     * tips : 历史收听：第1期：藏地密码第1部 第01集（喜欢别忘了打赏哦~）
     * audioType : 4
     */

    public int serialize;// 连载状态0：未知(无效字段，例如音乐、新闻等)， 1：确定连载中，2：确定完结, 3：长时间未更新
    public long lastListen;//// 最后收听时间
    public String tips;// 显示tips,搜索用的
    //public int audioType;//0 音乐   1 直播  2专辑   3 广播   4 智能电台  5 今日头条

    public int albumType; // 专辑类型

    public int pageSize; // 每页推荐请求数量

    @Override
    public String toString() {
        return "TXZAlbum{" +
                "categoryId=" + categoryId +
                ", sid=" + sid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", report='" + report + '\'' +
                ", breakpoint=" + breakpoint +
                ", flag=" + flag +
                ", arrArtistName=" + arrArtistName +
                ", arrCategoryIds=" + arrCategoryIds +
                ", serialize=" + serialize +
                ", lastListen=" + lastListen +
                ", tips='" + tips + '\'' +
                ", albumType=" + albumType +
                '}';
    }
}
