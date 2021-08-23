package com.txznet.music.push.bean;

import java.util.Map;

/**
 * Created by brainBear on 2017/12/11.
 */

public class PullData {


    /**
     * 快报
     */
    public static final int TYPE_NEWS = 0;

    /**
     * 专辑更新
     */
    public static final int TYPE_UPDATE = 2;

    /**
     * 推送音频
     */
    public static final int TYPE_AUDIOS = 1;

    /**
     * 请求链接
     */
    private String service;

    /**
     * 推送id
     */
    private int id;

    /**
     * 业务类型
     */
    private int type;


    private String from;


    public String getService() {
        return service;
    }

    public void setService(String service) {
        this.service = service;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }


    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    @Override
    public String toString() {
        return "PullData{" +
                "service='" + service + '\'' +
                ", id=" + id +
                ", type=" + type +
                ", from='" + from + '\'' +
                '}';
    }
}