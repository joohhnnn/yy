package com.txznet.webchat.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 微信界面显示参数
 * Created by J on 2017/5/26.
 */

public class WxUIConfig implements Parcelable{
    public int x;
    public int y;
    public int width;
    public int height;
    public int gravity;

    public WxUIConfig() {}

    @Override
    public boolean equals(Object obj) {
        if (null == obj) {
            return false;
        }

        if (!(obj instanceof WxUIConfig)) {
            return false;
        }

        WxUIConfig target = (WxUIConfig) obj;
        if (x == target.x && y == target.y && width == target.width && height == target.height && gravity == target.gravity) {
            return true;
        }

        return false;
    }

    public boolean isInValid() {
        return width > 0 && height > 0;
    }

    @Override
    public String toString() {
        return "WxUIConfig { " + x + ", " + y + ", " + width + ", " + height + ", " + gravity + "}";
    }

    protected WxUIConfig(Parcel in) {
        x = in.readInt();
        y = in.readInt();
        width = in.readInt();
        height = in.readInt();
        gravity = in.readInt();
    }

    public static final Creator<WxUIConfig> CREATOR = new Creator<WxUIConfig>() {
        @Override
        public WxUIConfig createFromParcel(Parcel in) {
            return new WxUIConfig(in);
        }

        @Override
        public WxUIConfig[] newArray(int size) {
            return new WxUIConfig[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(x);
        dest.writeInt(y);
        dest.writeInt(width);
        dest.writeInt(height);
        dest.writeInt(gravity);
    }
}
