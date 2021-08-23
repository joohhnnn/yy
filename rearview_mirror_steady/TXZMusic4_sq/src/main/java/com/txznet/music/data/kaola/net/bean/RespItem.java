package com.txznet.music.data.kaola.net.bean;

import java.util.List;

/**
 * Created by telenewbie on 2018/2/7.
 */

public class RespItem<T> {

    /**
     * haveNext : 1
     * nextPage : 2
     * havePre : 0
     * prePage : 1
     * currentPage : 1
     * count : 2579
     * sumPage : 129
     * pageSize : 20
     * dataList : [{"id":1100000000078,"name":"二货<em>一<\/em>箩筐","img":"http://img.kaolafm.net/mz/images/201510/79de75a0-c9fc-4d90-968d-e44acf139a9e/default.jpg","host":[{"name":"小毛驴","des":"我就是集女汉子和软妹子于一身的小毛驴~","img":"http://img.kaolafm.net/mz/images/201411/766d2483-6a9a-44a7-b6b1-06234cdebe9b/default.jpg"},{"name":"王钢蛋","des":"萝莉脸 冷面笑匠，猥琐的内心掩盖不了小清新的内心追求，总幻想着来一场说走就走的旅行，却常常千里送X，落个人财两空。","img":"http://img.kaolafm.net/mz/images/201406/6005d7fb-f2e0-4b12-975d-ff27cbb0d230/default.jpg"}],"type":0,"listenNum":483843569,"albumName":"二货一箩筐","source":1,"sourceName":"考拉FM","playUrl":"","isShowRed":0,"isRequest":0,"duration":0,"originalDuration":0,"oldId":65}]
     */

    private int haveNext;
    private int nextPage;
    private int havePre;
    private int prePage;
    private int currentPage;
    private int count;
    private int sumPage;
    private int pageSize;
    private List<T> dataList;

    public int getHaveNext() {
        return haveNext;
    }

    public void setHaveNext(int haveNext) {
        this.haveNext = haveNext;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    public int getHavePre() {
        return havePre;
    }

    public void setHavePre(int havePre) {
        this.havePre = havePre;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getSumPage() {
        return sumPage;
    }

    public void setSumPage(int sumPage) {
        this.sumPage = sumPage;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public List<T> getDataList() {
        return dataList;
    }

    public void setDataList(List<T> dataList) {
        this.dataList = dataList;
    }

    @Override
    public String toString() {
        return "RespItem{" +
                "haveNext=" + haveNext +
                ", nextPage=" + nextPage +
                ", havePre=" + havePre +
                ", prePage=" + prePage +
                ", currentPage=" + currentPage +
                ", count=" + count +
                ", sumPage=" + sumPage +
                ", pageSize=" + pageSize +
                ", dataList=" + dataList +
                '}';
    }
}
