package com.txznet.music.albumModule.logic.net.response;

import com.txznet.music.albumModule.bean.Audio;
import com.txznet.music.localModule.logic.AlbumUtils;
import com.txznet.music.data.http.resp.BaseResponse;
import com.txznet.music.soundControlModule.logic.net.request.ReqChapter;

import java.util.List;

public class ResponseAlbumAudio extends BaseResponse {

    public static final int FIELD_TYPE_UNKNOWN = 0;
    public static final int FIELD_TYPE_MUSIC = 1; //音乐
    public static final int FIELD_TYPE_FM = 2; //FM
    public static final int FIELD_TYPE_NEWS = 3;   //新闻
    public static final int FIELD_TYPE_STORYBOOK = 4; //小说
    public static final int FIELD_TYPE_TALKSHOW = 5; //脱口秀
    public static final int FIELD_TYPE_RECOMMEND = 6; //新闻推荐
    public static final int FIELD_TYPE_CARERFM = 7; //车主FM

    private int sid; // 原id, 如1：qq音乐等
    private long id; // 专辑id
    private long categoryId; // 专辑id
    private int pageId; // 页码
    private int offset; // 每页多个数量
    private int orderType; // 排序方式
    private int totalNum; // 总数量
    private int totalPage; // 总页数
    private List<Audio> arrAudio; // 专辑内音频
    private int up;//上下（方向）
    private int end;//表示是否到头（上拉到头和下拉到头，1，表示到头，0 表示没有）
    private int flag;
    private int field; //专辑的类别
    private List<ReqChapter> arrMeasure;//章节回的说法
    private int errMeasure;//当具体的章节回没有找到的时候，会回调


    public int getField() {
        return field;
    }

    public void setField(int field) {
        this.field = field;
    }

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public int getEnd() {
        return end;
    }

    public void setEnd(int end) {
        this.end = end;
    }

    @Override
    public String toString() {
        return "ResponseAlbumAudio " + hashCode() + "{"
                + "sid:" + sid
                + ",id:" + id
                + ",categoryId:" + categoryId
                + ",pageId:" + pageId
                + ",offset:" + offset
                + ",orderType:" + orderType
                + ",totalNum:" + totalNum
                + ",totalPage:" + totalPage
                + ",errCode:" + errCode
                + ",up:" + up
                + ",end:" + end
                + ",arrAudio.size:" + arrAudio.size()
                + ",flag:" + flag
                + "}";
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    public List<ReqChapter> getArrMeasure() {
        return arrMeasure;
    }

    public void setArrMeasure(List<ReqChapter> arrMeasure) {
        this.arrMeasure = arrMeasure;
    }

    public int getErrMeasure() {
        return errMeasure;
    }

    public void setErrMeasure(int errMeasure) {
        this.errMeasure = errMeasure;
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

    public int getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(int totalNum) {
        this.totalNum = totalNum;
    }

    public int getTotalPage() {
        return totalPage;
    }

    public void setTotalPage(int totalPage) {
        this.totalPage = totalPage;
    }


    public List<Audio> getArrAudio() {
        return arrAudio;
    }

    public void setArrAudio(List<Audio> arrAudio) {
        this.arrAudio = arrAudio;
    }

    /**
     * 是否需要客户端排序
     */
    public boolean needSort() {
        return AlbumUtils.getNumInPosition(flag, 0) == 1;
    }
}
