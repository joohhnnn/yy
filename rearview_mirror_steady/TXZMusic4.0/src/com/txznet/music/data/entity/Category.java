package com.txznet.music.data.entity;

import com.txznet.music.dao.CategotyListConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Unique;

import java.io.Serializable;
import java.util.List;

@Entity
public class Category implements Serializable {

    public static final long serialVersionUID = 0;

    private String desc;// 类别名称

    @Unique
    private long categoryId;
    private String logo;

    //1，表示推荐，2 排行榜 3 表示歌手的样式  4 表示 分类的样式。参照4.1天之眼的样式
    private int showStyle;

    @Convert(columnType = String.class, converter = CategotyListConverter.class)
    private List<Category> arrChild;

    public Category() {
        super();
    }

    @Generated(hash = 1516832533)
    public Category(String desc, long categoryId, String logo, int showStyle,
            List<Category> arrChild) {
        this.desc = desc;
        this.categoryId = categoryId;
        this.logo = logo;
        this.showStyle = showStyle;
        this.arrChild = arrChild;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public List<Category> getArrChild() {
        return arrChild;
    }

    public void setArrChild(List<Category> arrChild) {
        this.arrChild = arrChild;
    }

    public int getShowStyle() {
        return showStyle;
    }

    public void setShowStyle(int showStyle) {
        this.showStyle = showStyle;
    }

    @Override
    public String toString() {
        return "Category{" +
                "desc='" + desc + '\'' +
                ", categoryId=" + categoryId +
                ", logo='" + logo + '\'' +
                ", arrChild=" + arrChild +
                '}';
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (!(obj instanceof Category)) {
            return false;
        }
        Category other = (Category) obj;
        return categoryId == other.categoryId;
    }
}
