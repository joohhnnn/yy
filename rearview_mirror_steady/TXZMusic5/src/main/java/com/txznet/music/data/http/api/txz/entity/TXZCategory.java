package com.txznet.music.data.http.api.txz.entity;

import java.util.List;

/**
 * 分类信息
 */
public class TXZCategory {
    public int categoryId;
    public String desc;
    public String logo; // 分类图片
    public int showStyle; // 该分类需要展示的样式 1，表示推荐，2 排行榜 3 表示歌手的样式  4表示分类的样式。参照4.1天之眼的样式
    public List<TXZCategory> arrChild; // 分类的子类

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TXZCategory category = (TXZCategory) o;

        return categoryId == category.categoryId;
    }

    @Override
    public int hashCode() {
        return categoryId;
    }

    @Override
    public String toString() {
        return "TXZCategory{" +
                "categoryId=" + categoryId +
                ", desc='" + desc + '\'' +
                ", logo='" + logo + '\'' +
                ", showStyle=" + showStyle +
                ", arrChild=" + arrChild +
                '}';
    }
}



