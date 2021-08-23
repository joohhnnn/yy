package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 电影列表数据
 */

public class CinemaListMsgData extends BaseListMsgData<CinemaListMsgData.CinemaData> {
    public CinemaListMsgData() {
        super(TYPE_FULL_LIST_CINEMA);
    }

    public class CinemaData{
        public String name;
        public String post;
        public double score;
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("cines", JSONObject[].class);
        mDatas = new ArrayList<CinemaData>();
        CinemaData cinemaData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            cinemaData = new CinemaData();
            cinemaData.name = jobj.optString("name");
            cinemaData.post = jobj.optString("post");
            cinemaData.score = jobj.optDouble("score",0);
            mDatas.add(cinemaData);
        }
    }
}
