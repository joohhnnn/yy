package com.txznet.music.albumModule.logic.net.request;

import com.txznet.fm.bean.Configuration;
import com.txznet.music.baseModule.Constant;
import com.txznet.music.data.http.req.BaseReq;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;

import java.util.List;

public class ReqAlbumAudio extends BaseReq {
    private int sid; // 原id, 如1：qq音乐等
    private long id; // 专辑id
    private int pageId; // 页码 默认是1
    private int offset = Constant.PAGECOUNT; // 每页多个数量， 默认是10
    private int orderType; // 排序方式 ， 默认按数量
    private long categoryId;// 必须传递，
    private int version; // 版本
    private int type;// 默认0:音频内专辑排序 1:上次收听+专辑内排序


    private long audioId;   //从某个audio开始
    private int audioSid;   //从某个audio开始
    private int chapter;    //章节
    private int up;     //0 向下 1 向上
    private List<ReqChapter> arrMeasure;
    private String svrData; // 回传数据

    public List<ReqChapter> getArrMeasure() {
        return arrMeasure;
    }

    public void setArrMeasure(List<ReqChapter> arrMeasure) {
        this.arrMeasure = arrMeasure;
    }

    public ReqAlbumAudio() {
        super();
        version = Configuration.getInstance().getInteger(Configuration.TXZ_Audio_VERSION);
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public long getAudioId() {
        return audioId;
    }

    public void setAudioId(long audioId) {
        this.audioId = audioId;
    }

    public int getChapter() {
        return chapter;
    }

    public void setChapter(int chapter) {
        this.chapter = chapter;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public int getVersion() {
        return version;
    }

    public int getAudioSid() {
        return audioSid;
    }

    public void setAudioSid(int audioSid) {
        this.audioSid = audioSid;
    }

    public String getSvrData() {
        return svrData;
    }

    public void setSvrData(String svrData) {
        this.svrData = svrData;
    }

    @Override
    public String toString() {
        //声控章节回的时候，会使用到toString的方法，缓存会去获取所有请求对象的toString（）

        return "ReqAlbumAudio{" +
                "sid=" + sid +
                ", id=" + id +
                ", pageId=" + pageId +
                ", offset=" + offset +
                ", orderType=" + orderType +
                ", categoryId=" + categoryId +
                ", version=" + version +
                ", type=" + type +
                ", audioId=" + audioId +
                ", audioSid=" + audioSid +
                ", chapter=" + chapter +
                ", up=" + up +
                ", arrMeasure=" + arrMeasure +
                '}';
    }
}
