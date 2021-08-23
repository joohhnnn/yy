package com.txznet.music.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 分类信息
 *
 * @author zackzhou
 * @date 2018/12/4,15:38
 */
public class Category implements Parcelable {
    public long categoryId;
    public String name;
    public String logo; // 分类图片

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Category category = (Category) o;

        return categoryId == category.categoryId;
    }

    @Override
    public int hashCode() {
        return (int) (categoryId ^ (categoryId >>> 32));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.categoryId);
        dest.writeString(this.name);
        dest.writeString(this.logo);
    }

    public Category() {
    }

    protected Category(Parcel in) {
        this.categoryId = in.readLong();
        this.name = in.readString();
        this.logo = in.readString();
    }

    public static final Parcelable.Creator<Category> CREATOR = new Parcelable.Creator<Category>() {
        @Override
        public Category createFromParcel(Parcel source) {
            return new Category(source);
        }

        @Override
        public Category[] newArray(int size) {
            return new Category[size];
        }
    };
}



