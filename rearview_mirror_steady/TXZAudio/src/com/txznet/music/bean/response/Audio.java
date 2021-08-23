package com.txznet.music.bean.response;

import java.io.Serializable;
import java.util.List;

import com.txznet.music.Constant;
import com.txznet.music.utils.StringUtils;

/**
 * 歌曲 从历史记录中点击先判断 该歌曲所属的次级歌单ID是否有值，如果没有值，则根据专辑ID来所搜
 * 
 * @author ASUS User
 *
 */
public class Audio implements Serializable, Comparable<Audio> {

	// private static final long serialVersionUID = 5944738408389917797L;

	private long id;// 歌曲ID
	private String albumId;// 专辑ID

	private int sid;// 来源ID

	private String desc;// 简介

	private String name; // 音频名字

	private String logo;// logo
	private long createTime;// 创建时间

	private long duration;// 曲长(ms)

	private long fileSize;// 歌曲文件大小
	private List<String> arrArtistName;// 歌手，。。因为ORMLite不支持集合数据

	private int likedNum; // 喜欢的数量

	private long listenNum; // 收听次数

	private String strCategoryId;// 当前歌曲类别

	private String lastPlayTime;// 上一次播放记录的时间，用于历史缓存

	private String currentPlayTime;

	private boolean bNoCache; // 缓存标记位
	private String downloadType; // 1:qq 需要预处理， 2：考拉，可直接用dowloadUrl下载
	private String strDownloadUrl; //
	private String strProcessingUrl; // qq音乐才需要
	private int iExpTime; // 超时时间
	private boolean bShowSource;// 是否显示音乐源
	private String sourceFrom;// 商家来源
	private String albumName;

	private String pinyin;// 汉字，字母，数字，排序

	private String report; // 播报内容

	private int urlType;// 品质类型，高品质1，低品质2

	public int getUrlType() {
		return urlType;
	}

	public void setUrlType(int urlType) {
		this.urlType = urlType;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

	public String getSourceFrom() {
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

	public void setBShowSource(boolean bShowSource) {
		this.bShowSource = bShowSource;
	}

	public boolean isbNoCache() {
		return bNoCache;
	}

	public void setBNoCache(boolean bNoCache) {
		this.bNoCache = bNoCache;
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

	public int getiExpTime() {
		return iExpTime;
	}

	public void setIExpTime(int iExpTime) {
		this.iExpTime = iExpTime;
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

	// public String getArtistNames() {
	// if (StringUtils.isEmpty(artistNames)) {
	// return "";
	// }
	// return artistNames.replaceAll("&#39;", "'");
	// }
	//
	// public void setArtistNames(String artistNames) {
	// this.artistNames = artistNames;
	// }

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

	// public String getUrl() {
	// return url;
	// }
	//
	// public void setUrl(String url) {
	// this.url = url;
	// }

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

	public void setLastPlayTime(String lastPlayTime) {
		// LogUtil.logd("setLastPlayTime::" + lastPlayTime);
		this.lastPlayTime = lastPlayTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
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
		if (id != other.id)
			return false;
		if (sid != other.sid)
			return false;
		// if (!getStrDownloadUrl().equals(other.getStrDownloadUrl())) {
		// return false;
		// }
		return true;
	}

	@Override
	public String toString() {

		if (Constant.ISTEST) {
			return "Audio [id=" + id + ", albumId=" + albumId + ", sid=" + sid
					+ ", desc=" + desc + ", name=" + name + ", logo=" + logo
					+ ", createTime=" + createTime + ", duration=" + duration
					+ ", fileSize=" + fileSize + ", arrArtistName="
					+ arrArtistName + ", likedNum=" + likedNum + ", listenNum="
					+ listenNum + ", strCategoryId=" + strCategoryId
					+ ", lastPlayTime=" + lastPlayTime + ", currentPlayTime="
					+ currentPlayTime + ", bNoCache=" + bNoCache
					+ ", downloadType=" + downloadType + ", strDownloadUrl="
					+ strDownloadUrl + ", strProcessingUrl=" + strProcessingUrl
					+ ", iExpTime=" + iExpTime + ", bShowSource=" + bShowSource
					+ ", sourceFrom=" + sourceFrom + ", albumName=" + albumName
					+ ", pinyin=" + pinyin + ", report=" + report
					+ ", urlType=" + urlType + "]";
		} else {
			return "Audio [id=" + id + ", albumId=" + albumId + ", sid=" + sid
					+ ", name=" + name + ", logo=" + logo + ", duration="
					+ duration + ", arrArtistName=" + arrArtistName
					+ ", strCategoryId=" + strCategoryId + ", currentPlayTime="
					+ currentPlayTime + ", bNoCache=" + bNoCache + ", albumName="
					+ albumName + ", report=" + report + ", urlType=" + urlType
					+ "]";
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

}
