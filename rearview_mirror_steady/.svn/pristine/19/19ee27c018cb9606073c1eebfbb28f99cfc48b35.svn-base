package com.txznet.music.data.http.api.txz.entity.resp;

import com.txznet.music.data.entity.Album;

import java.util.List;

public class TXZRespAlbum extends TXZRespBase {
    public String categoryId; // 类型 1,2,,3,4
    public int pageId; // 页面
    public int offset; // 第几页多少个
    public int orderType; // 排序方式，
    @Deprecated
    public int totalNum; // 总数量 后台返回可能为null
    @Deprecated
    public int totalPage; // 总页数 后台返回可能为null
    public List<Album> arrAlbum;

    @Override
    public String toString() {
        return "TXZRespAlbum{" +
                "categoryId='" + categoryId + '\'' +
                ", pageId=" + pageId +
                ", offset=" + offset +
                ", orderType=" + orderType +
                ", totalNum=" + totalNum +
                ", totalPage=" + totalPage +
                ", arrAlbum=" + arrAlbum +
                '}';
    }
}
