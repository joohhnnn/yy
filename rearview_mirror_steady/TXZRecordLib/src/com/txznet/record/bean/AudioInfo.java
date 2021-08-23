package com.txznet.record.bean;

public class AudioInfo {
	public String title;
	public String text;
	public boolean paid;
	public int novelStatus;
	public boolean lastPlay;
	public boolean latest;


	public static final int NOVEL_STATUS_INVALID = 0; // 无效，不存在该标志
	public static final int NOVEL_STATUS_SERILIZE = 1; // 无效，不存在该标志
	public static final int NOVEL_STATUS_END = 2; // 无效，不存在该标志
	
	public AudioInfo(String title,String text){
		this.title = title;
		this.text = text;
	}

	public boolean isLatest() {
		return latest;
	}

	public void setLatest(boolean latest) {
		this.latest = latest;
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



}
