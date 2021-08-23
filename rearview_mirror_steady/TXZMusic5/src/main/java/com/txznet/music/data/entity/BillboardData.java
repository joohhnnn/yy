package com.txznet.music.data.entity;

import java.util.List;

/**
 * 榜单数据
 *
 * @author zackzhou
 * @date 2019/1/7,15:42
 */

public class BillboardData {

    /**
     * categoryId : 50701
     * sid : 9
     * contentType : 180
     * boardName : 电台榜单
     * posId : 1
     * arrAlbum : [{"sid":3,"id":9723091,"name":"郭德纲21年相声精选","logo":"default","arrArtistName":[],"arrCategoryIds":[],"report":"郭德纲21年相声精选","score":0,"albumType":2,"flag":10,"posId":0,"lastAudioName":"最后一期"}]
     */

    public long categoryId;
    public int sid;
    public int contentType;
    public String boardName;
    public int posId;
    public List<PageItemData> arrAlbum;

    @Override
    public String toString() {
        return "BillboardData{" +
                "categoryId=" + categoryId +
                ", sid=" + sid +
                ", contentType=" + contentType +
                ", boardName='" + boardName + '\'' +
                ", posId=" + posId +
                ", arrAlbum=" + arrAlbum +
                '}';
    }
}
