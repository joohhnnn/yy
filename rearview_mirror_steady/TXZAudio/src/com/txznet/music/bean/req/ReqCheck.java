package com.txznet.music.bean.req;

import java.util.ArrayList;
import java.util.List;

import com.txznet.music.utils.QQMusicUtil;

public class ReqCheck {
	private long logoTag; // tag, 客户端tag
	private List<Integer> arrApp = new ArrayList<Integer>(); // 安装的音乐源列表,默认所有的。
	private String version; // 版本

	public long getLogoTag() {
		return logoTag;
	}

	public void setLogoTag(long logoTag) {
		this.logoTag = logoTag;
	}

	public List<Integer> getArrApp() {
		if (QQMusicUtil.checkQQMusicInstalled()) {
			arrApp.add(1);
		}
		return arrApp;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

}
