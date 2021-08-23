package com.txznet.audio.player.audio;

import com.txznet.music.bean.response.Audio;

public class KuWoMusicAudio extends NetAudio {
	public KuWoMusicAudio(Audio audio) {
		super(audio);
	}

	@Override
	public boolean needCodecPlayer() {
		return false;
	}

	@Override
	public String getCacheId() {
		int end = mAudio.getStrDownloadUrl().indexOf("?");
		int start = mAudio.getStrDownloadUrl().indexOf("/", 6);
		if (start < 0) {
			start = 0;
		}
		if (end >= 0) {
			return calCacheId(mAudio.getStrDownloadUrl().substring(start, end));
		}

		return calCacheId(mAudio.getStrDownloadUrl());
	}

	/**
	 * 获取音频数据
	 */
	public Audio getAudio() {
		return mAudio;
	}

	private String mFinalUrl;

	public String getFinalUrl() {
		return mFinalUrl;
	}

	public void setFinalUrl(String mFinalUrl) {
		this.mFinalUrl = mFinalUrl;
	}

}
