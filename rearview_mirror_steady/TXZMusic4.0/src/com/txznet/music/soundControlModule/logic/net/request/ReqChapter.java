package com.txznet.music.soundControlModule.logic.net.request;

/**
 * Created by telenewbie on 2017/7/10.
 */

public class ReqChapter {
    public int num;
    public String unit;

    public ReqChapter(int num, String unit) {
        this.num = num;
        this.unit = unit;
    }

    @Override
    public String toString() {
        return "ReqChapter{" +
                "num=" + num +
                ", unit='" + unit + '\'' +
                '}';
    }
}
