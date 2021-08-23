package com.txznet.music.bean.req;

/**
 * 错误资源上报 比方说：该歌曲不能播放
 * 
 * @author telenewbie
 *
 */
public class ReqError {

	
	public int sourceId;
	public long albumId;
	public long audioId;
	public String strName;
	public String strUrl;
	public String rawText;
	public String artist;
	public int errCode;

	
	@Override
	public String toString() {
		return "ReqError [sourceId=" + sourceId + ", albumId=" + albumId + ", audioId=" + audioId + ", strName="
				+ strName + ", strUrl=" + strUrl + ", rawText=" + rawText + ", artist=" + artist + ", errCode="
				+ errCode + "]";
	}

}
