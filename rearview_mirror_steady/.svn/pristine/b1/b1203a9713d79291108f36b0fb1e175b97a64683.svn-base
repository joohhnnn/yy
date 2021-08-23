package com.txznet.record.bean;

import com.txznet.comm.util.JSONBuilder;

public class AudioBean {
	public String mAudioName;
	public String mAuthorName;
	public String mSourceName;
	public boolean mPaid;
	public boolean mLastPlay;
	public int mNovelStatus;

	protected JSONBuilder toJsonObj() {
		JSONBuilder json = new JSONBuilder();
		json.put("mAudioName", this.mAudioName);
		json.put("mAuthorName", this.mAuthorName);
		json.put("mSourceName", this.mSourceName);
		return json;
	}

	public String toString() {
		JSONBuilder json = toJsonObj();
		return json.toString();
	}

	protected void fromJsonObject(JSONBuilder json) {
		this.mAudioName = json.getVal("mAudioName", String.class);
		this.mAuthorName = json.getVal("mAuthorName", String.class);
		this.mSourceName = json.getVal("mSourceName", String.class);
	}

	public static AudioBean fromString(String data) {
		AudioBean p = new AudioBean();
		JSONBuilder json = new JSONBuilder(data);
		p.fromJsonObject(json);
		return p;
	}
}
