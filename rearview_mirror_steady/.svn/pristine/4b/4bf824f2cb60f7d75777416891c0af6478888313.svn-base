package com.txznet.music.data.entity;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.txznet.comm.util.CollectionUtils;
import com.txznet.comm.util.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * 旧版的Audio
 * 歌曲 从历史记录中点击先判断 该歌曲所属的次级歌单ID是否有值，如果没有值，则根据专辑ID来所搜
 *
 * @author ASUS User
 */
public class TmdInfo implements Serializable, Comparable<TmdInfo> {

    private String strDownloadUrl;
    private String strProcessingUrl;
    private int sid;// 来源ID
    private String name; // 音频名字
    private List<String> arrArtistName;// 歌手
    private long duration;// 曲长(ms)
    private long id;// 歌曲ID
    private int albumSid; // 专辑sid
    private long albumId; // 音频id
    private String sourceFrom;// 商家来源
    private String downloadType; // 1:qq 需要预处理， 2：考拉，可直接用dowloadUrl下载

    public TmdInfo() {
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

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
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

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public List<String> getArrArtistName() {
        return arrArtistName;
    }

    public void setArrArtistName(List<String> arrArtistName) {
        this.arrArtistName = arrArtistName;
    }

    public int getAlbumSid() {
        return albumSid;
    }

    public void setAlbumSid(int albumSid) {
        this.albumSid = albumSid;
    }

    public long getAlbumId() {
        return albumId;
    }

    public void setAlbumId(long albumId) {
        this.albumId = albumId;
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
        TmdInfo other = (TmdInfo) obj;
        //如果是本地的话，则判断是否名称和艺术家相同，如果相同则视作同一个歌曲，详情查看：TXZ-9315
        if (0 == sid && other.sid == sid) {
            if (CollectionUtils.isNotEmpty(other.arrArtistName) && TextUtils.equals(other.name, name) && TextUtils.equals(StringUtils.toString(other.arrArtistName), StringUtils.toString(arrArtistName))) {
                return true;
            }
        }
        return id == other.id;
    }

    @Override
    public int compareTo(@NonNull TmdInfo another) {
        // 通过路径过滤
        try {
            return another.getStrDownloadUrl().compareTo(getStrDownloadUrl());
        } catch (Exception e) {
            return 0;
        }
    }
}
