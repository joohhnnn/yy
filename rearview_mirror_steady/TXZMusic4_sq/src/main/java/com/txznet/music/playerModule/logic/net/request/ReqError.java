package com.txznet.music.playerModule.logic.net.request;

import com.txznet.fm.bean.Configuration;

/**
 * 错误资源上报 比方说：该歌曲不能播放
 *
 * @author telenewbie
 */
public class ReqError {


    public int sourceId;
    public long albumId;
    public long audioId;
    public String strName;//歌曲名称
    public String strUrl;//请求路径
    public String rawText;//声控内容
    public String artist;//艺术家
    public int errCode;
    public String errString;
    public int version = Configuration.getInstance().getInteger(Configuration.TXZ_Audio_VERSION);

    @Override
    public String toString() {
        return "ReqError{" +
                "sourceId=" + sourceId +
                ", albumId=" + albumId +
                ", audioId=" + audioId +
                ", strName='" + strName + '\'' +
                ", strUrl='" + strUrl + '\'' +
                ", rawText='" + rawText + '\'' +
                ", artist='" + artist + '\'' +
                ", errCode=" + errCode +
                ", version=" + version +
                '}';
    }
}
