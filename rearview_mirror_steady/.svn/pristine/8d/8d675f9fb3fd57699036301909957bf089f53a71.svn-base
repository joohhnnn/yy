package com.txznet.music.data.entity;

import android.arch.persistence.db.SimpleSQLiteQuery;
import android.arch.persistence.db.SupportSQLiteQuery;
import android.arch.persistence.room.Entity;

import com.txznet.comm.util.StringUtils;
import com.txznet.music.Constant;
import com.txznet.music.util.Logger;

/**
 * 本地音频
 *
 * @author zackzhou
 * @date 2018/12/4,16:30
 */
@Entity
public class LocalAudio extends AudioV5 {
    public long createTime; // 文件创建时间，时间戳
    public String path;

    public static SupportSQLiteQuery getSearchQuery(String audioName, String artists, String albumName) {
        StringBuilder sb = new StringBuilder();
//        findBySql
        sb.append("SELECT * FROM LOCALAUDIO where ");
        if (StringUtils.isNotEmpty(artists)
                && StringUtils.isEmpty(audioName)) {
            sb.append("name like '%").append(artists).append("%'").append(" or ").append("artist like '%").append(artists).append("%'");

        } else if (StringUtils.isEmpty(artists)
                && StringUtils.isNotEmpty(audioName)) {
            sb.append("name like '%").append(audioName).append("%'");
        } else if (StringUtils.isNotEmpty(artists)
                && StringUtils.isNotEmpty(audioName)) {
            sb.append("name like '%").append(audioName).append("%'").append(" and ").append("name like '%").append(artists).append("%'");
            sb.append(" or ");
            sb.append("name like '%").append(audioName).append("%'").append(" and ").append("artist like '%").append(artists).append("%'");
        } else if (StringUtils.isEmpty(artists)
                && StringUtils.isEmpty(audioName)
                && StringUtils.isNotEmpty(albumName)) {
            sb.append("name like '%").append(albumName).append("%'");
        } else {
            sb.append("1 < 0");
        }
        Logger.d(Constant.LOG_TAG_DB, "LocalAudio, getSearchQuery:" + sb.toString());

        String query = sb.toString();
        return new SimpleSQLiteQuery(query);

    }
}
