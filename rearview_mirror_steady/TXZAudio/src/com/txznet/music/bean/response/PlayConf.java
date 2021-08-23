package com.txznet.music.bean.response;

public class PlayConf {
	public static final int MUSIC_TYPE = 1;
	public static final int RADIO_TYPE = 2;
	public static final int NEWS_TYPE = 3;
	private int sid;
	private int play; // 1： 同行者播放， 0：原应用app播放
	private int type;// 1,music,2.电台，3 新闻
	private String logo;

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public int getPlay() {
		return play;
	}

	public void setPlay(int play) {
		this.play = play;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

}
