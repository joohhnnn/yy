package com.txznet.music.albumModule.bean;

import android.text.TextUtils;

import com.txznet.music.baseModule.Constant;
import com.txznet.music.dao.MapConverter;
import com.txznet.music.dao.StringListConverter;
import com.txznet.music.utils.CollectionUtils;
import com.txznet.music.utils.StringUtils;
import com.txznet.music.utils.Utils;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Transient;

import java.io.File;
import java.io.Serializable;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 歌曲 从历史记录中点击先判断 该歌曲所属的次级歌单ID是否有值，如果没有值，则根据专辑ID来所搜
 *
 * @author ASUS User
 */
@Entity
public class Audio implements Serializable, Comparable<Audio> {

    private static final long serialVersionUID = 5944738408389917797L;
    @Transient
    public static final int POS_NEED_REPORT = 0;//第一个位置,是否播放

    @Transient
    public static final int POS_SUPPORT_FAVOUR = 1;//第二个位置,是否支持收藏
    @Transient
    public static final int FLAG_SUPPORT = 1;//支持为1
    @Transient
    public static final int FLAG_UNSUPPORT = 0;//不支持为0

    @Transient
    public static final int POS_FAVOUR = 2;//第三个位置,是否收藏
    @Transient
    public static final int FLAG_FAVOUR = 1;//收藏为1
    @Transient
    public static final int FLAG_UNFAVOUR = 0;//收藏为0

    @Id
    private String audioDbId; //GreenDao不支持双主键，所以新建一个id值作为主键，值固定为sid-id

    private long id;// 歌曲ID
    private String albumId;// 专辑ID

    private int sid;// 来源ID

    private String desc;// 简介

    private String name; // 音频名字

    private String logo;// logo
    private long createTime;// 创建时间

    private long duration;// 曲长(ms)

    private long fileSize;// 歌曲文件大小
    @Convert(columnType = String.class, converter = StringListConverter.class)
    private List<String> arrArtistName;// 歌手

    private int likedNum; // 喜欢的数量

    private long listenNum; // 收听次数

    @Deprecated
    private long clientListenNum; //在客户端收听次数 弃用 因为容易被后台拉回来的数据覆盖
    private String strCategoryId;// 当前歌曲类别
    private String lastPlayTime;// 上一次播放记录的时间，用于历史缓存
    private String currentPlayTime;
    private boolean bNoCache; // 缓存标记位
    private String downloadType; // 1:qq 需要预处理， 2：考拉，可直接用dowloadUrl下载
    private String strDownloadUrl; //
    private String strProcessingUrl; // qq音乐才需要
    private int processIsPost; // 是否需要post请求
    @Convert(columnType = String.class, converter = MapConverter.class)
    private Map<String, Object> processHeader; // post请求的请求头
    private int iExpTime; // 超时时间
    private boolean bShowSource;// 是否显示音乐源
    private String sourceFrom;// 商家来源
    private String albumName; //真实所在专辑的名称！！！！
    private String pinyin;// 汉字，字母，数字，排序
    private String report; // 播报内容
    private int urlType;// 品质类型，高品质1，低品质2
    private int flag;//十进制,标志位，用于控制音频的一些操作（后台控制）,第一位:表示是否播报(1,播报,0,不播报),第二位:表示是否支持收藏(0,不支持收藏,1,支持收藏),第三位:表示是否收藏(1,订阅,0,未订阅)
    private int orderNum;//排序所需
    @Convert(columnType = String.class, converter = StringListConverter.class)
    private List<String> wakeUp; // 支持的唤醒词

    private int score; //打分，后台用于排序 小于1000为已收听，大于1000小于10000为未收听

    private long operTime;//收藏时间,在4.2.0上面增加

    private long srcAlbumId;//区别于albumid,    srcAlbumId 表明这个音频的真实请求专辑的来源，即由哪个专辑请求而来， 而albumId表明音频真正所属的 专辑 ，例如笑话FM，由段子来了+二货一箩筐拼凑而成，笑话FM即是srcAlbumId，而段子来了，二货一箩筐既是albumId（后台传递）

    @Deprecated
    private boolean isLocal = false;//是否是本地的 弃用 因为容易被后台拉回来的数据覆盖

    //    @Transient
    //2018年8月24日11点27分，新增需求：
    //【【同听4.4.1】【本地音乐】正在播放本地音乐“给你的歌”，此时声控我要听“尘埃”，点击本地TAB，此时音乐切歌播放“从此以后”】
    //https://www.tapd.cn/21711881/bugtrace/bugs/view?bug_id=1121711881001004256
    //使用上这个字段，但是由于没有存放到数据库中，这种方案不可行，否则，打开音乐，再切换本地的tab，会导致切歌（播放的是搜索的歌曲）
    //所以将@Transient删掉，并且在数据库中升级中增加这个字段
    private int isInsert = 0;//用于标记该音频是否是由声控搜索插入到播放列表的，主要用于数据上报 1,表示是由外部插入。可以拓展的字段,
    @Transient
    private Album album;//表明该音频所属的专辑，车主FM新加

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    @Generated(hash = 1642629471)
    public Audio() {
    }

    @Generated(hash = 625168200)
    public Audio(String audioDbId, long id, String albumId, int sid, String desc, String name, String logo, long createTime, long duration, long fileSize, List<String> arrArtistName, int likedNum, long listenNum,
            long clientListenNum, String strCategoryId, String lastPlayTime, String currentPlayTime, boolean bNoCache, String downloadType, String strDownloadUrl, String strProcessingUrl, int processIsPost,
            Map<String, Object> processHeader, int iExpTime, boolean bShowSource, String sourceFrom, String albumName, String pinyin, String report, int urlType, int flag, int orderNum, List<String> wakeUp,
            int score, long operTime, long srcAlbumId, boolean isLocal, int isInsert) {
        this.audioDbId = audioDbId;
        this.id = id;
        this.albumId = albumId;
        this.sid = sid;
        this.desc = desc;
        this.name = name;
        this.logo = logo;
        this.createTime = createTime;
        this.duration = duration;
        this.fileSize = fileSize;
        this.arrArtistName = arrArtistName;
        this.likedNum = likedNum;
        this.listenNum = listenNum;
        this.clientListenNum = clientListenNum;
        this.strCategoryId = strCategoryId;
        this.lastPlayTime = lastPlayTime;
        this.currentPlayTime = currentPlayTime;
        this.bNoCache = bNoCache;
        this.downloadType = downloadType;
        this.strDownloadUrl = strDownloadUrl;
        this.strProcessingUrl = strProcessingUrl;
        this.processIsPost = processIsPost;
        this.processHeader = processHeader;
        this.iExpTime = iExpTime;
        this.bShowSource = bShowSource;
        this.sourceFrom = sourceFrom;
        this.albumName = albumName;
        this.pinyin = pinyin;
        this.report = report;
        this.urlType = urlType;
        this.flag = flag;
        this.orderNum = orderNum;
        this.wakeUp = wakeUp;
        this.score = score;
        this.operTime = operTime;
        this.srcAlbumId = srcAlbumId;
        this.isLocal = isLocal;
        this.isInsert = isInsert;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public List<String> getWakeUp() {
        return wakeUp;
    }

    public void setWakeUp(List<String> wakeUp) {
        this.wakeUp = wakeUp;
    }

    public int getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(int orderNum) {
        this.orderNum = orderNum;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    @Deprecated
    public long getClientListenNum() {
        return clientListenNum;
    }

    @Deprecated
    public void setClientListenNum(long clientListenNum) {
        this.clientListenNum = clientListenNum;
    }

    public int getUrlType() {
        return urlType;
    }

    public void setUrlType(int urlType) {
        this.urlType = urlType;
    }

    public String getReport() {
        if (StringUtils.isNotEmpty(report)) {
            return report;
        }
        return name;
    }

    public boolean isLocal() {
        //如果真的存在的话
//        if (isLocal) {
        File file = new File(getStrDownloadUrl());
        if (file.exists()) {
            return true;
        }
//        }
        return false;
    }

    @Deprecated
    public void setLocal(boolean local) {
        isLocal = local;
    }

    public void setReport(String report) {
        this.report = report;
    }

    public String getSourceFrom() {
        if (StringUtils.isEmpty(sourceFrom)) {
            if (sid == Constant.QQINT) {
                sourceFrom = "QQ音乐";
            } else if (sid == Constant.KAOLAINT) {
                sourceFrom = "考拉";
            } else if (sid == 0) {
                sourceFrom = "未知";
            } else if (sid == Constant.XMLY) {
                sourceFrom = "喜马拉雅";
            }
        }

        return sourceFrom;
    }

    public void setSourceFrom(String sourceFrom) {
        this.sourceFrom = sourceFrom;
    }

    public String getPinyin() {
        return pinyin;
    }

    public void setPinyin(String pinyin) {
        this.pinyin = pinyin;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public boolean isbShowSource() {
        return bShowSource;
    }

    public boolean isbNoCache() {
        return bNoCache;
    }

    public String getDownloadType() {
        return downloadType;
    }

    public void setDownloadType(String downloadType) {
        this.downloadType = downloadType;
    }

    // strDownloadUrl=/mnt/extsd/music/BIGBANG - Let&#39;s not fall in love - 副本
    // (2).mp3,
    public String getStrDownloadUrl() {
        if (StringUtils.isEmpty(strDownloadUrl)) {
            return "";
        }
        return strDownloadUrl.replaceAll("&#39;", "'");
    }

    public void setStrDownloadUrl(String strDownloadUrl) {
        this.strDownloadUrl = strDownloadUrl;
    }

    public String getStrProcessingUrl() {
        return strProcessingUrl;
    }

    public void setStrProcessingUrl(String strProcessingUrl) {
        this.strProcessingUrl = strProcessingUrl;
    }


    public String getCurrentPlayTime() {
        return currentPlayTime;
    }

    public void setCurrentPlayTime(String currentPlayTime) {
        this.currentPlayTime = currentPlayTime;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getAlbumId() {
        return albumId;
    }

    public void setAlbumId(String albumId) {
        this.albumId = albumId;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getName() {
        if (StringUtils.isEmpty(name)) {
            return "";
        }
        return name.replaceAll("&#39;", "'");
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(long createTime) {
        this.createTime = createTime;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public List<String> getArrArtistName() {
        return arrArtistName;
    }

    public void setArrArtistName(List<String> arrArtistName) {
        this.arrArtistName = arrArtistName;
    }

    public int getLikedNum() {
        return likedNum;
    }

    public void setLikedNum(int likedNum) {
        this.likedNum = likedNum;
    }

    public long getListenNum() {
        return listenNum;
    }


    public void setListenNum(long listenNum) {
        this.listenNum = listenNum;
    }

    public String getStrCategoryId() {
        return strCategoryId;
    }

    public void setStrCategoryId(String strCategoryId) {
        this.strCategoryId = strCategoryId;
    }

    public String getLastPlayTime() {
        // LogUtil.logd("getLastPlayTime::" + lastPlayTime);
        return lastPlayTime;
    }

    // public String getUrl() {
    // return url;
    // }
    //
    // public void setUrl(String url) {
    // this.url = url;
    // }

    public void setLastPlayTime(String lastPlayTime) {
        // LogUtil.logd("setLastPlayTime::" + lastPlayTime);
        this.lastPlayTime = lastPlayTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        if (sid != 0) {
            result = prime * result + (int) (id ^ (id >>> 32));
        }
        result = prime * result + sid;
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
        Audio other = (Audio) obj;
        //如果是本地的话，则判断是否名称和艺术家相同，如果相同则视作同一个歌曲，详情查看：TXZ-9315
        if (0 == sid && other.sid == sid) {
            if (CollectionUtils.isNotEmpty(other.arrArtistName) && TextUtils.equals(other.name, name) && TextUtils.equals(StringUtils.toString(other.arrArtistName), StringUtils.toString(arrArtistName))) {
                return true;
            }
        }
        if (id != other.id)
            return false;
//		if (sid != other.sid)
//			return false;
        // if (!getStrDownloadUrl().equals(other.getStrDownloadUrl())) {
        // return false;
        // }
        return true;
    }

    @Override
    public String toString() {
        if (Constant.ISTEST) {
            return "Audio{" +
                    "id=" + id +
                    ", sid=" + sid +
                    ", name=" + name +
                    ", duration=" + duration +
                    ", arrArtistName=" + arrArtistName +
                    ", bNoCache=" + bNoCache +
                    ", downloadType=" + downloadType +
                    ", report=" + report +
                    ", urlType=" + urlType +
                    ", flag=" + flag +
                    ", orderNum=" + orderNum +
                    ", albumId=" + albumId +
                    ", strDownloadUrl=" + strDownloadUrl +
                    ", strProcessingUrl=" + strProcessingUrl +
                    ", bShowSource=" + bShowSource +
                    ", sourceFrom=" + sourceFrom +
                    ", operTime=" + operTime +
                    ", srcAlbumId=" + srcAlbumId +
                    "}";
        } else {
            return "Audio{" +
                    "id=" + id +
                    ", sid=" + sid +
                    ", name=" + name +
                    ", duration=" + duration +
                    ", arrArtistName=" + arrArtistName +
                    ", bNoCache=" + bNoCache +
                    ", downloadType=" + downloadType +
                    ", report=" + report +
                    ", urlType=" + urlType +
                    ", flag=" + flag +
                    ", orderNum=" + orderNum +
                    ", albumId=" + albumId +
                    ", bShowSource=" + bShowSource +
                    ", sourceFrom=" + sourceFrom +
                    ", operTime=" + operTime +
                    ", srcAlbumId=" + srcAlbumId +
                    "}";
        }
    }


    @Override
    public int compareTo(Audio another) {
        // 通过路径过滤
        try {
            return another.getStrDownloadUrl().compareTo(getStrDownloadUrl());
        } catch (Exception e) {
            return 0;
        }
    }


    public boolean getBNoCache() {
        return this.bNoCache;
    }

    public void setBNoCache(boolean bNoCache) {
        this.bNoCache = bNoCache;
    }

    public int getIExpTime() {
        return this.iExpTime;
    }

    public void setIExpTime(int iExpTime) {
        this.iExpTime = iExpTime;
    }

    public boolean getBShowSource() {
        return this.bShowSource;
    }

    public void setBShowSource(boolean bShowSource) {
        this.bShowSource = bShowSource;
    }

    public String getAudioDbId() {
        return String.format(Locale.getDefault(), "%d-%d", sid, id);
    }

    public void setAudioDbId(String audioDbId) {
        this.audioDbId = audioDbId;
    }

    public boolean getIsLocal() {
        return this.isLocal;
    }

    public void setIsLocal(boolean isLocal) {
        this.isLocal = isLocal;
    }

    public long getOperTime() {
        return this.operTime;
    }

    public void setOperTime(long operTime) {
        this.operTime = operTime;
    }

    public int getIsInsert() {
        return isInsert;
    }

    public void setIsInsert(int isInsert) {
        this.isInsert = isInsert;
    }

    public long getSrcAlbumId() {
        return srcAlbumId;
    }

    public void setSrcAlbumId(long srcAlbumId) {
        this.srcAlbumId = srcAlbumId;
    }

    public int isSupportFavour() {
        return Utils.getDataWithPosition(getFlag(), Audio.POS_SUPPORT_FAVOUR);
    }

    public int isFavour() {
        return Utils.getDataWithPosition(getFlag(), Audio.POS_FAVOUR);
    }

    public int getProcessIsPost() {
        return processIsPost;
    }

    public void setProcessIsPost(int processIsPost) {
        this.processIsPost = processIsPost;
    }

    public Map<String, Object> getProcessHeader() {
        return processHeader;
    }

    public void setProcessHeader(Map<String, Object> processHeader) {
        this.processHeader = processHeader;
    }
}
