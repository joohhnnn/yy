package com.txznet.music.data.entity;

import java.util.List;

/**
 * tab方块数据
 *
 * @author zackzhou
 * @date 2018/12/30,11:21
 */

public class PageItemData {
    /**
     * sid : 9
     * id : 50101
     * name : 巅峰榜·流行指数
     * logo : top_fashion
     * arrArtistName : []
     * arrCategoryIds : [100001]
     * report : 巅峰榜·流行指数
     * score : 0
     * breakpoint : 0
     * albumType : 2
     * flag : 10
     * posId : 1
     * contentType : 130
     * posI d : 6
     */

    public int sid;
    public long id;
    public String name;
    public String logo;
    public String desc;
    public String report;
    public int score;
    public int breakpoint;
    public int albumType;
    public int flag;
    public int posId;
    public int contentType;
    public List<String> arrArtistName;
    public List<Long> arrCategoryIds;
    public String svrData;
    public Icon icon;

    public static class Icon {
        public String url;
    }
}
