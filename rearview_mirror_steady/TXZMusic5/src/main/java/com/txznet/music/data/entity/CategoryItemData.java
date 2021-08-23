package com.txznet.music.data.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

/**
 * 分类数据
 *
 * @author zackzhou
 * @date 2018/12/30,11:21
 */

public class CategoryItemData implements Parcelable {

    /**
     * categoryId : 300000
     * desc : 新闻
     * logo : http://statictest.txzing.com/audio/audio_logo.php?f=fm_home_icon_news.jpg&t=0
     * showStyle : 1
     * arrChild : []
     * posId : 1
     */

    public int sid;
    public long categoryId;
    public String desc;
    public String logo;
    public int showStyle;
    public int posId;
    public ArrayList<CategoryItemData> arrChild;

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.sid);
        dest.writeLong(this.categoryId);
        dest.writeString(this.desc);
        dest.writeString(this.logo);
        dest.writeInt(this.showStyle);
        dest.writeInt(this.posId);
        dest.writeList(this.arrChild);
    }

    public CategoryItemData() {
    }

    protected CategoryItemData(Parcel in) {
        this.sid = in.readInt();
        this.categoryId = in.readLong();
        this.desc = in.readString();
        this.logo = in.readString();
        this.showStyle = in.readInt();
        this.posId = in.readInt();
        this.arrChild = new ArrayList<>();
        in.readList(this.arrChild, CategoryItemData.class.getClassLoader());
    }

    public static final Parcelable.Creator<CategoryItemData> CREATOR = new Parcelable.Creator<CategoryItemData>() {
        @Override
        public CategoryItemData createFromParcel(Parcel source) {
            return new CategoryItemData(source);
        }

        @Override
        public CategoryItemData[] newArray(int size) {
            return new CategoryItemData[size];
        }
    };
}
