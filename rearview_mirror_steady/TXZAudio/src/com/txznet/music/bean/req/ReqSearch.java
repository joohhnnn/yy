package com.txznet.music.bean.req;

import java.util.List;

import com.txznet.comm.util.StringUtils;
import com.txznet.fm.bean.Configuration;

public class ReqSearch {

	private String audioName; // audio名称
	private String artist; // audio艺术家
	private String category; // 节目分类
	private String albumName; // 专辑或节目
	private String area; // 地域
	private long beginTime; // 起始时间uint32
	private long endTime; // 结束时间uint32
	private int season; // 期数
	private String version; // 版本(0的版本不会走混排)(1.支持混排)(2走酷我)
	private List<Integer> arrApp; // 安装的音乐源列表
	private int index;
	private int field;// 表示电台，还是歌曲（我要听“逻辑思维”)
	private String text;// 搜索的原文本
	private String subCategory;// 子分类

	public ReqSearch() {
		super();
		version = String.valueOf(Configuration.getInstance().getInteger(
				Configuration.TXZ_Search_VERSION));
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(String subCategory) {
		this.subCategory = subCategory;
	}

	public int getField() {
		return field;
	}

	public void setField(int field) {
		this.field = field;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getAudioName() {
		return audioName;
	}

	public void setAudioName(String audioName) {
		this.audioName = audioName;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public long getBeginTime() {
		return beginTime;
	}

	public void setBeginTime(long beginTime) {
		this.beginTime = beginTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public int getSeason() {
		return season;
	}

	public void setSeason(int season) {
		this.season = season;
	}

	public String getVersion() {
		return version;
	}

	public List<Integer> getArrApp() {
		return arrApp;
	}

	public void setArrApp(List<Integer> arrApp) {
		this.arrApp = arrApp;
	}

	@Override
	public String toString() {
		StringBuffer sBuffer = new StringBuffer();
		if (StringUtils.isNotEmpty(artist)) {
			sBuffer.append(artist);
		}
		if (StringUtils.isNotEmpty(audioName)) {
			sBuffer.append(audioName);
		}
		if (StringUtils.isNotEmpty(albumName)) {
			sBuffer.append("专辑：").append(albumName);
		}
		if (StringUtils.isNotEmpty(category)) {
			sBuffer.append("分类：").append(category);
		}
		sBuffer.append("的数据");
		return sBuffer.toString();
	}

}
