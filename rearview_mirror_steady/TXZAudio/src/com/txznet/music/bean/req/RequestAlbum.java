package com.txznet.music.bean.req;

public class RequestAlbum {

	private int sourceId; // 1:考拉 2:qq
	private String albumId; // 专辑id
	private String categoryId; // 专辑类型
	private long audioId; // 断点audioId,,id

	public int getSourceId() {
		return sourceId;
	}

	public void setSourceId(int sourceId) {
		this.sourceId = sourceId;
	}

	public String getAlbumId() {
		return albumId;
	}

	public void setAlbumId(String albumId) {
		this.albumId = albumId;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public long getAudioId() {
		return audioId;
	}

	public void setAudioId(long audioId) {
		this.audioId = audioId;
	}

}
