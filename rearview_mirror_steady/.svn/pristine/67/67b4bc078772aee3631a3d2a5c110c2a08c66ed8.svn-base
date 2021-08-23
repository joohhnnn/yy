package com.txznet.music.albumModule.logic.net.request;

import com.txznet.fm.bean.Configuration;
import com.txznet.music.baseModule.Constant;

import java.util.Arrays;

/**
 * 请求某一专辑底下音频
 */
public class ReqSearchAlbum {
    private long categoryId;//分类的ID
    private int pageId = 1;//页数
    private int offset;// 一页请求几个数据，服务其默认九个，
    private int orderType = 1;//排序方式
    private Integer[] arrApp;//客户端安装的APPID集合
    private int version;//请求的版本号
    private long AlbumId;
    private int up;

    public long getAlbumId() {
        return AlbumId;
    }

    public void setAlbumId(long albumId) {
        AlbumId = albumId;
    }

    public ReqSearchAlbum() {
        version = Configuration.getInstance().getInteger(Configuration.TXZ_ALBUM_LIST_VERSION);
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public int getPageId() {
        return pageId;
    }

    public void setPageId(int pageId) {
        this.pageId = pageId;
    }

    public int getOffset() {
        return Constant.PAGECOUNT;
    }

    public void setPageCount(int offset) {
        this.offset = offset;
    }

    public int getOrderType() {
        return orderType;
    }

    public void setOrderType(int orderType) {
        this.orderType = orderType;
    }

    public Integer[] getArrApp() {
        return arrApp;
    }

    public void setArrApp(Integer[] arrApp) {
        this.arrApp = arrApp;
    }

    public String getVersion() {
        return Constant.Version;
    }

    public int getUp() {
        return up;
    }

    public void setUp(int up) {
        this.up = up;
    }

    @Override
    public String toString() {
        return "ReqSearchAlbum [categoryId=" + categoryId + ", pageId="
                + pageId + ", offset=" + offset + ", orderType=" + orderType
                + ", arrApp=" + Arrays.toString(arrApp) + ", version="
                + version + "]";
    }

}
