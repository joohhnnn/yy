package com.txznet.sdk.tongting;

import android.support.annotation.NonNull;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

import static com.txznet.sdk.tongting.IConstantData.*;

public class TongTingAudio {
    private long id;
    private int sid;
    private String name;
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

    public int getFlag() {
        return flag;
    }

    public void setFlag(int flag) {
        this.flag = flag;
    }

    public TongTingAudio(long id, int sid, String name, int flag) {
        this.id = id;
        this.sid = sid;
        this.name = name;
        this.flag = flag;
    }


    @NonNull
    public static List<TongTingAudio> createAudios(JSONArray jsonArray) throws JSONException {
        List<TongTingAudio> list = new ArrayList<TongTingAudio>();
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONBuilder jsonBuilder1 = new JSONBuilder(jsonArray.getString(i));
            long id = jsonBuilder1.getVal(KEY_ID, Long.class, 0L);
            int sid = jsonBuilder1.getVal(KEY_SID, Integer.class, 0);
            String name = jsonBuilder1.getVal(KEY_NAME, String.class, "无");
            int flag = jsonBuilder1.getVal(KEY_FLAG, Integer.class, 0);
            TongTingAudio audio = new TongTingAudio(id, sid, name, flag);

            list.add(audio);
        }
        return list;
    }

}
