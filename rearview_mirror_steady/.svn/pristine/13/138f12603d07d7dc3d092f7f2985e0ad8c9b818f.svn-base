package com.txznet.music.playerModule.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.txznet.music.dao.StringListConverter;

import org.greenrobot.greendao.annotation.Convert;
import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Index;

import java.util.List;
import org.greenrobot.greendao.annotation.Generated;

/**
 * Created by brainBear on 2017/11/6.
 */
@Entity(
    indexes = {
            @Index(value = "id,sid", unique = true)
    }
)
public class PlayItem implements Parcelable {

    public static final int TYPE_FILE = 1;
    public static final int TYPE_NET = 2;
    public static final int TYPE_QQ = 3;

    @Id(autoincrement = true)
    private Long dbId;

    private long id;

    private int sid;

    private String name;

    @Convert(columnType = String.class, converter = StringListConverter.class)
    private List<String> urls;

    private int type;

    private long expTime;


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "PlayItem{" +
                "id=" + id +
                ", sid=" + sid +
                ", name='" + name + '\'' +
                ", urls=" + urls +
                ", type=" + type +
                '}';
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(this.id);
        dest.writeInt(this.sid);
        dest.writeString(this.name);
        dest.writeStringList(this.urls);
        dest.writeInt(this.type);
    }

    public Long getDbId() {
        return this.dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public long getExpTime() {
        return this.expTime;
    }

    public void setExpTime(long expTime) {
        this.expTime = expTime;
    }

    public PlayItem() {
    }

    protected PlayItem(Parcel in) {
        this.id = in.readLong();
        this.sid = in.readInt();
        this.name = in.readString();
        this.urls = in.createStringArrayList();
        this.type = in.readInt();
    }

    @Generated(hash = 208731790)
    public PlayItem(Long dbId, long id, int sid, String name, List<String> urls,
            int type, long expTime) {
        this.dbId = dbId;
        this.id = id;
        this.sid = sid;
        this.name = name;
        this.urls = urls;
        this.type = type;
        this.expTime = expTime;
    }

    public static final Creator<PlayItem> CREATOR = new Creator<PlayItem>() {
        @Override
        public PlayItem createFromParcel(Parcel source) {
            return new PlayItem(source);
        }

        @Override
        public PlayItem[] newArray(int size) {
            return new PlayItem[size];
        }
    };
}
