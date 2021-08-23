package com.txznet.music.data.bean;

import java.util.List;

public class AdapterAlbum {

    public static final int POS_NEED_REPORT = 0;//第一个位置,是否播放

    public static final int POS_SUPPORT_SUBSCRIBE = 1;//第二个位置,是否支持订阅
    public static final int FLAG_SUPPORT = 1;//支持为1
    public static final int FLAG_UNSUPPORT = 0;//不支持为0

    public static final int POS_SUBSCRIBE = 2;//第三个位置,是否收藏
    public static final int FLAG_SUBSCRIBE = 1;//收藏为1
    public static final int FLAG_UNSUBSCRIBE = 0;//收藏为0

    /**
     * 无效字段，例如音乐、新闻等
     */
    public static final int NOVEL_STATUS_INVALID = 0;
    /**
     * 连载中
     */
    public static final int NOVEL_STATUS_SERIALIZE = 1;
    /**
     * 完本
     */
    public static final int NOVEL_STATUS_ENDED = 2;
    /**
     * 长时间未更新
     */
    public static final int NOVEL_STATUS_NOUPDATE = 3;
    public static final int TYPE_UNKNOWN = 0;
    public static final int TYPE_MUSIC = 1; //音乐
    public static final int TYPE_HOT = 2;
    public static final int TYPE_NEWS = 3; // 新闻
    public static final int TYPE_BROADCAST = 4; // 广播
    public static final int TYPE_NOVEL = 5; // 小说
    public static final int TYPE_TALKSHOW = 10; // 脱口秀
    public static final int PROPERTY_NOVEL_STATUS = 0;

    private String albumDbId; //GreenDao不支持双主键，所以新建一个id值作为主键，值为sid-id
    private int sid; // 原id, 如1：qq音乐等
    private long id; // 专辑id
    private String name; // 名字
    private String logo; // 封面
    private String desc; // 描述
    private int likedNum; // 喜欢的数量
    private long listenNum; // 收听的次数
    private List<String> arrArtistName; // 艺术家名称
    private int categoryId;// 当前专辑所属类别ID
    private int properties; // 状态
    private int serialize; // 连载状态
    private long lastUpdate; // 最近更新时间
    private long lastListen; // 最后收听时间
    private boolean paid; // 是否付费
    private String tips; // 显示tips
    private int flag; // 按十进制位设置，从右往左，第一位：是否需要播报专辑内音频:1播报 0不播报,第二位:表示是否支持订阅(1,不支持订阅,0,支持订阅),第三位:表示是否订阅(1,订阅,0,未订阅)
    private List<Integer> arrCategoryIds; // 分类,eg:开心的，甜美的
    private String report;    //播报内容
    private int breakpoint; //0 播放最新的
    private int audioType;

    private long operTime;//订阅时间,在4.2.0上面增加

}
