package com.txznet.music.bean;

/**
 * 本地音乐
 * 
 * @author telenewbie
 * @version 创建时间：2016年3月25日 下午6:15:24
 * 
 */
public class LocalAudio {
	private long id;
	private String name;
	private String url;
	private String artists;
	private long duration;
	private String pinyin;// 汉字，字母，数字，排序

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public int getSid() {
		return 0;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getArtists() {
		return artists;
	}

	public void setArtists(String artists) {
		this.artists = artists;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getPinyin() {
		return pinyin;
	}

	public void setPinyin(String pinyin) {
		this.pinyin = pinyin;
	}

}
