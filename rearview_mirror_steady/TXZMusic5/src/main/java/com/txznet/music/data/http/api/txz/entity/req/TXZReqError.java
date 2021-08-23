package com.txznet.music.data.http.api.txz.entity.req;

import com.txznet.music.config.Configuration;
import com.txznet.txz.util.TXZFileConfigUtil;

/**
 * @author zackzhou
 * @date 2019/1/26,16:45
 */

public class TXZReqError extends TXZReqBase {
    public int albumSid;
    public long albumId;
    public int sourceId;
    public long audioId;
    public String strName;//歌曲名称
    public String strUrl;//请求路径
    public String rawText;//声控内容
    public String artist;//艺术家
    public int errCode;
    public String errString;
    public int version = TXZFileConfigUtil.getIntSingleConfig(Configuration.Key.TXZ_AUDIO_VERSION, Configuration.DefVal.AUDIO_VERSION);


    @Override
    public String toString() {
        return "TXZReqError{" +
                "sourceId=" + sourceId +
                ", albumId=" + albumId +
                ", audioId=" + audioId +
                ", strName='" + strName + '\'' +
                ", strUrl='" + strUrl + '\'' +
                ", rawText='" + rawText + '\'' +
                ", artist='" + artist + '\'' +
                ", errCode=" + errCode +
                ", errString='" + errString + '\'' +
                ", version=" + version +
                '}';
    }
}
