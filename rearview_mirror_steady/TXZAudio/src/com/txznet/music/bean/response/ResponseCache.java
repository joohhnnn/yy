package com.txznet.music.bean.response;

/**
 * @author telenewbie
 * @version 创建时间：2016年2月23日 下午5:31:59
 * 
 */
public class ResponseCache {

	private long audioID;
	private String processURL;
	private String downloadURL;
	private int expressTime;

	public long getAudioID() {
		return audioID;
	}

	public void setAudioID(long audioID) {
		this.audioID = audioID;
	}

	public String getProcessURL() {
		return processURL;
	}

	public void setProcessURL(String processURL) {
		this.processURL = processURL;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public int getExpressTime() {
		return expressTime;
	}

	public void setExpressTime(int expressTime) {
		this.expressTime = expressTime;
	}

	@Override
	public String toString() {
		return "ResponseCache [audioID=" + audioID + ", processURL=" + processURL + ", downloadURL=" + downloadURL
				+ ", expressTime=" + expressTime + "]";
	}
}
