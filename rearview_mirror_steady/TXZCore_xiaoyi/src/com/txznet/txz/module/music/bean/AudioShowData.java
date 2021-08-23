package com.txznet.txz.module.music.bean;

import org.json.JSONObject;

import com.txznet.comm.util.JSONBuilder;

public class AudioShowData {

	private long id;
	private String title;
	private String name;
	
	// 新增字段
	private String albumId;
	private String albumName;
	public String getAlbumName() {
		return albumName;
	}

	public void setAlbumName(String albumName) {
		this.albumName = albumName;
	}

	private String albumIntro; // 专辑简介
	private int albumTrackCount; // 专辑包含的集数

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getAlbumIntro() {
		return albumIntro;
	}

	public void setAlbumIntro(String intro) {
		this.albumIntro = intro;
	}

	public int getAlbumTrackCount() {
		return albumTrackCount;
	}

	public void setAlbumTrackCount(int trackCount) {
		this.albumTrackCount = trackCount;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public JSONObject toJsonObjet() {
		return toJsonObj().build();
	}

	protected JSONBuilder toJsonObj() {
		JSONBuilder json = new JSONBuilder();
		json.put("id", id);
		json.put("title", title);
		json.put("name", name);
		// 兼容旧版本的TXZRecord
		json.put("text", "");
		// 新增的字段
		json.put("albumIntro", albumIntro);
		json.put("albumTrackCount", albumTrackCount);
		json.put("albumId", albumId);
		json.put("albumName", albumName);
		return json;
	}

	public String toString() {
		return toJsonObj().toString();
	}
}
