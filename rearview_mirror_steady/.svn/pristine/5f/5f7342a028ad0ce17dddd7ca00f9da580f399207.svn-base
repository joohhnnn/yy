package com.txznet.sdk.tongting;

import android.support.annotation.NonNull;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.txznet.sdk.tongting.IConstantData.*;

public class TongTingAlbum {
    private long id;
    private int sid;
    private String name;
    private String logo;
    private String desc;
    private long categoryId;
    private int flag;

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

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
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

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public TongTingAlbum(long id, int sid, String name, String logo, String desc, long categoryId, int flag) {
        this.id = id;
        this.sid = sid;
        this.name = name;
        this.logo = logo;
        this.desc = desc;
        this.categoryId = categoryId;
        this.flag = flag;
    }


    @NonNull
    public static List<TongTingAlbum> createAlbums(JSONArray jsonArray) throws JSONException {
        List<TongTingAlbum> list = new ArrayList<TongTingAlbum>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONBuilder jsonBuilder1 = new JSONBuilder(jsonArray.getString(i));

            long id = jsonBuilder1.getVal(KEY_ID, Long.class, 0L);
            int sid = jsonBuilder1.getVal(KEY_SID, Integer.class, 0);
            String name = jsonBuilder1.getVal(KEY_NAME, String.class, "");
            String logo = jsonBuilder1.getVal(KEY_LOGO, String.class, "");
            String desc = jsonBuilder1.getVal(KEY_DESC, String.class, "");
            Long categoryId = jsonBuilder1.getVal(KEY_CATEGORYID, Long.class, 0L);
            int isSubscribe = jsonBuilder1.getVal(KEY_ISSUBSCRIBE, Integer.class, 0);
            TongTingAlbum album = new TongTingAlbum(id, sid, name, logo, desc, categoryId, isSubscribe);
            list.add(album);
        }
        return list;
    }


}
