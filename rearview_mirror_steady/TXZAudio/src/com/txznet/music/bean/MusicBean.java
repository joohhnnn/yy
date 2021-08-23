package com.txznet.music.bean;

import java.util.List;

import com.txznet.music.bean.response.Audio;

import android.view.View.OnClickListener;

public class MusicBean {

	private int id;// 用于设置控件的id
	private String name;
	private String url;
	private OnClickListener clickListener;
	List<Audio> songInfo;

	public MusicBean() {
		super();
	}

	public MusicBean(int id, String name, String url, List<Audio> songInfo,
			OnClickListener clickListener) {
		super();
		this.id = id;
		this.name = name;
		this.url = url;
		this.songInfo = songInfo;
		this.clickListener = clickListener;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public OnClickListener getClickListener() {
		return clickListener;
	}

	public void setClickListener(OnClickListener clickListener) {
		this.clickListener = clickListener;
	}

}
