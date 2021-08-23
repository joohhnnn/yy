package com.txznet.txz.module.music.bean;

import org.json.JSONArray;
import org.json.JSONObject;

import com.txznet.comm.util.JSONBuilder;

public class AudioShowData {

	private long id;
	private String title;
	private String name;
	
	// 新增字段
	private String albumId;
	private String albumName;
	private String report;
	private int novelStatus; // 小说连载状态
	private boolean lastPlay; // 上次收听
	private boolean listened; // 收听过
	private boolean paid; // 是否付费
	private boolean showDetail; // 是否展示详情页
	private String[] wakeUp; // 唤醒词
	private boolean latest; // 最新

	/**
     * 无效字段，例如音乐、新闻等不具有该字段
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


	public String[] getWakeUp() {
		return wakeUp;
	}

	public void setWakeUp(String[] wakeUp) {
		this.wakeUp = wakeUp;
	}
	
	public boolean isShowDetail() {
		return showDetail;
	}

	public void setShowDetail(boolean showDetail) {
		this.showDetail = showDetail;
	}


	public boolean isPaid() {
		return paid;
	}
	
	public void setPaid(boolean paid) {
		this.paid = paid;
	}
	
	public int getNovelStatus() {
		return novelStatus;
	}

	public void setNovelStatus(int novelStatus) {
		this.novelStatus = novelStatus;
	}

	public boolean isLastPlay() {
		return lastPlay;
	}

	public void setLastPlay(boolean lastPlay) {
		this.lastPlay = lastPlay;
	}

	public boolean isListened() {
		return listened;
	}

	public void setListened(boolean listened) {
		this.listened = listened;
	}

	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}

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


	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
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
		// 新增20170705 TXZMusic4.0 terry
		json.put("novelStatus", novelStatus);
		json.put("lastPlay", lastPlay);
		json.put("listened", listened);
		json.put("paid", paid);
		json.put("latest", latest);
		if (wakeUp != null && wakeUp.length > 0) {
			JSONArray jsonArray = new JSONArray();
			for (int i = 0; i < wakeUp.length; i++) {
				jsonArray.put(wakeUp[i]);
			}
			json.put("wakeUp", jsonArray);
		}
		return json;
	}

	public String toString() {
		return toJsonObj().toString();
	}
}
