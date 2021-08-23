package com.txznet.music.albumModule.bean;

import com.txznet.music.dao.LongListConverter;
import com.txznet.music.dao.StringListConverter;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.Serializable;
import java.util.List;
import java.util.Locale;

@Entity
public class Album implements Serializable {

    private static final long serialVersionUID = 1L;
    @Transient
    public static final int POS_NEED_REPORT = 0;//第一个位置,是否播放

    @Transient
    public static final int POS_SUPPORT_SUBSCRIBE = 1;//第二个位置,是否支持订阅
    @Transient
    public static final int FLAG_SUPPORT = 1;//支持为1
    @Transient
    public static final int FLAG_UNSUPPORT = 0;//不支持为0

    @Transient
    public static final int POS_SUBSCRIBE = 2;//第三个位置,是否收藏
    @Transient
    public static final int FLAG_SUBSCRIBE = 1;//收藏为1
    @Transient
    public static final int FLAG_UNSUBSCRIBE = 0;//收藏为0
    @Transient
    public static final int FLAG_SPECIAL_FM = 3;//是否显示大图
    @Transient
    public static final int FLAG_CURRENT_TIME_ZONE = 4;//标识时是否是当前推荐的专辑（车主FM分时段主题--当前时段）

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

    @Id
    private String albumDbId; //GreenDao不支持双主键，所以新建一个id值作为主键，值为sid-id
    private int sid; // 原id, 如1：qq音乐等
    private long id; // 专辑id
    private String name; // 名字
    private String logo; // 封面
    private String desc; // 描述
    private int likedNum; // 喜欢的数量
    private long listenNum; // 收听的次数
    @Convert(columnType = String.class, converter = StringListConverter.class)
    private List<String> arrArtistName; // 艺术家名称
    @Deprecated
    private int categoryId;// 当前专辑所属类别ID
    private int properties; // 状态
    private int serialize; // 连载状态
    private long lastUpdate; // 最近更新时间
    private long lastListen; // 最后收听时间
    private boolean paid; // 是否付费
    private String tips; // 显示tips
    private int flag; // 按十进制位设置，

    // 从右往左，第一位：是否需要播报专辑内音频:1播报 0不播报,
    // 第二位:表示是否支持订阅(1,不支持订阅,0,支持订阅),
    // 第三位:表示是否订阅(1,订阅,0,未订阅)
    //第四位：标识时是否显示（大图）
    //第五位：标识时是否是当前推荐的专辑（车主FM分时段主题--当前时段）
    @Convert(columnType = String.class, converter = LongListConverter.class)
    private List<Long> arrCategoryIds; // 分类,eg:开心的，甜美的
    private String report;    //播报内容
    private int breakpoint; //0 播放最新的
    private int audioType;

    @Transient
    public static final int ALBUM_TYPE_CAR_FM = 7;//车主fm
    @Transient
    public static final int ALBUM_TYPE_NORMAL_FM = 8;//分类fm
    @Transient
    public static final int ALBUM_TYPE_NORMAL_ALBUM = 0;//普通的专辑


    private int albumType;//专辑类型，7表示车主FM，8表示分类FM

    private long operTime;//订阅时间,在4.2.0上面增加

    @Transient
    private String svrData; // 回传数据，5.0.0+

    @Transient
    private Album parentAlbum;//父的album，用在车主FM上面
    private long pid;//父Album的ID
    private int pSid;//父Album的SID

    @Generated(hash = 1249612549)
    public Album(String albumDbId, int sid, long id, String name, String logo, String desc,
            int likedNum, long listenNum, List<String> arrArtistName, int categoryId, int properties,
            int serialize, long lastUpdate, long lastListen, boolean paid, String tips, int flag,
            List<Long> arrCategoryIds, String report, int breakpoint, int audioType, int albumType,
            long operTime, long pid, int pSid) {
        this.albumDbId = albumDbId;
        this.sid = sid;
        this.id = id;
        this.name = name;
        this.logo = logo;
        this.desc = desc;
        this.likedNum = likedNum;
        this.listenNum = listenNum;
        this.arrArtistName = arrArtistName;
        this.categoryId = categoryId;
        this.properties = properties;
        this.serialize = serialize;
        this.lastUpdate = lastUpdate;
        this.lastListen = lastListen;
        this.paid = paid;
        this.tips = tips;
        this.flag = flag;
        this.arrCategoryIds = arrCategoryIds;
        this.report = report;
        this.breakpoint = breakpoint;
        this.audioType = audioType;
        this.albumType = albumType;
        this.operTime = operTime;
        this.pid = pid;
        this.pSid = pSid;
    }

    @Generated(hash = 1609191978)
    public Album() {
    }

    public String getReport() {
        if (StringUtils.isNotEmpty(report)) {
            return report;
        }
        return name;
    }

    public void setReport(String report) {
        this.report = report;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (id ^ (id >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Album other = (Album) obj;
        if (id != other.id)
            return false;
        return true;
    }


    @Override
    public String toString() {
        return "Album{" +
                "sid=" + sid +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", logo='" + logo + '\'' +
                ", arrArtistName=" + arrArtistName +
                ", properties=" + properties +
                ", paid=" + paid +
                ", tips='" + tips + '\'' +
                ", flag=" + flag +
                ", arrCategoryIds=" + arrCategoryIds +
                ", report='" + report + '\'' +
                ", breakpoint=" + breakpoint +
                ", audioType=" + audioType +
                ", albumType=" + albumType +
                ", operTime=" + operTime +
                ", pid=" + pid +
                ", pSid=" + pSid +
                '}';
    }

    public String getAlbumDbId() {
        return String.format(Locale.getDefault(), "%d-%d", sid, id);
    }

    public void setAlbumDbId(String albumDbId) {
        this.albumDbId = albumDbId;
    }

    public int getSid() {
        return this.sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return this.logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public int getLikedNum() {
        return this.likedNum;
    }

    public void setLikedNum(int likedNum) {
        this.likedNum = likedNum;
    }

    public long getListenNum() {
        return this.listenNum;
    }

    public void setListenNum(long listenNum) {
        this.listenNum = listenNum;
    }

    public List<String> getArrArtistName() {
        return this.arrArtistName;
    }

    public void setArrArtistName(List<String> arrArtistName) {
        this.arrArtistName = arrArtistName;
    }

//    @Deprecated
//    public int getCategoryId() {
//        return this.categoryId;
//    }

    public long getCategoryId() {
        long categoryId = 0;
        if (null != getArrCategoryIds() && !getArrCategoryIds().isEmpty()) {
            categoryId = getArrCategoryIds().get(0);
        }
        return categoryId;
    }


    public int getProperties() {
        return this.properties;
    }

    public void setProperties(int properties) {
        this.properties = properties;
    }

    public int getSerialize() {
        return this.serialize;
    }

    public void setSerialize(int serialize) {
        this.serialize = serialize;
    }

    public long getLastUpdate() {
        return this.lastUpdate;
    }

    public void setLastUpdate(long lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public long getLastListen() {
        return this.lastListen;
    }

    public void setLastListen(long lastListen) {
        this.lastListen = lastListen;
    }

    public boolean getPaid() {
        return this.paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public String getTips() {
        return this.tips;
    }

    public void setTips(String tips) {
        this.tips = tips;
    }

    public int getFlag() {
        return this.flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public List<Long> getArrCategoryIds() {
        return this.arrCategoryIds;
    }

    public void setArrCategoryIds(List<Long> arrCategoryIds) {
        this.arrCategoryIds = arrCategoryIds;
    }

    public int getBreakpoint() {
        return this.breakpoint;
    }

    public void setBreakpoint(int breakpoint) {
        this.breakpoint = breakpoint;
    }

    public int getAudioType() {
        return this.audioType;
    }

    public void setAudioType(int audioType) {
        this.audioType = audioType;
    }

    public long getOperTime() {
        return this.operTime;
    }

    public void setOperTime(long operTime) {
        this.operTime = operTime;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getAlbumType() {
        return albumType;
    }

    public void setAlbumType(int albumType) {
        this.albumType = albumType;
    }

    public Album getParentAlbum() {
        if (pid != 0 && pSid != 0) {
            if (parentAlbum == null) {
                Album album = new Album();
                album.setSid(pSid);
                album.setId(pid);
                return album;
            }
        }
        return parentAlbum;
    }

    public void setParentAlbum(Album parentAlbum) {
        this.parentAlbum = parentAlbum;
    }

    public long getPid() {
        return pid;
    }

    public void setPid(long pid) {
        this.pid = pid;
    }

    public int getpSid() {
        return pSid;
    }

    public void setpSid(int pSid) {
        this.pSid = pSid;
    }

    public int getPSid() {
        return this.pSid;
    }

    public void setPSid(int pSid) {
        this.pSid = pSid;
    }


    public int isSupportSubscribe() {
        return Utils.getDataWithPosition(getFlag(), Album.POS_SUPPORT_SUBSCRIBE);
    }

    public int isSubscribe() {
        return Utils.getDataWithPosition(getFlag(), Album.POS_SUBSCRIBE);
    }

    public String getSvrData() {
        return svrData;
    }

    public void setSvrData(String svrData) {
        this.svrData = svrData;
    }
}
