package com.txznet.music.report;

import com.txznet.fm.bean.Configuration;

/**
 * Created by telenewbie on 2017/7/11.
 */

public class ReportHistory {

    /**
     * 开始播放
     */
    public static final int TYPE_START = 0;

    /**
     * 播放结束
     */
    public static final int TYPE_END = 1;

    /**
     * 切歌
     */
    public static final int TYPE_SITCH = 2;


    public int sid;      //version>=10: 用这个
    public String albumId; //专辑id
    public String categoryId; //专辑类型
    public long audioId; //断点audioId
    public int version = Configuration.getInstance().getInteger(Configuration.TXZ_Audio_VERSION);
    public int type; //状态值 0 开始播放 1 播放结束 2 切歌

    @Override
    public String toString() {
        return "ReportHistory{" +
                "sid=" + sid +
                ", albumId='" + albumId + '\'' +
                ", categoryId='" + categoryId + '\'' +
                ", audioId=" + audioId +
                ", version=" + version +
                '}';
    }
}
