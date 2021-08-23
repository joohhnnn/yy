package com.txznet.music.playerModule.logic.net.request;

/**
 * 请求第三方(音频渠道<假请求>)
 * 
 * @author telenewbie
 * @version 创建时间：2016年4月19日 下午3:19:38
 * 
 */
public class ReqThirdSearch {

	private int sid;
	private long audioId;
	private String strCache;
	private int stepId;//步骤

	private String deviceNum;//
	private long timeStamp;// 后台需求

	public String getDeviceNum() {
		return deviceNum;
	}

	public void setDeviceNum(String deviceNum) {
		this.deviceNum = deviceNum;
	}

	public long getTimeStamp() {
		return timeStamp;
	}

	public void setTimeStamp(long timeStamp) {
		this.timeStamp = timeStamp;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public long getAudioId() {
		return audioId;
	}

	public void setAudioId(long audioId) {
		this.audioId = audioId;
	}

	public String getStrCache() {
		return strCache;
	}

	public void setStrCache(String strCache) {
		this.strCache = strCache;
	}

	public int getStepId() {
		return stepId;
	}

	public void setStepId(int stepId) {
		this.stepId = stepId;
	}

	public ReqThirdSearch(int sid, long audioId, int stepId) {
		super();
		this.sid = sid;
		this.audioId = audioId;
		this.stepId = stepId;
	}

}
