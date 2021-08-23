package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 音乐电台列表数据
 */
public class AudioListMsgData extends BaseListMsgData<AudioListMsgData.AudioData> {

    public AudioListMsgData() {
        super(TYPE_FULL_LIST_AUDIO);
    }

    public class AudioData {
        public String title;
        public String name;
        public String listened;
        public String paid;
        public String novelStatus;
        public String latest;
    }

    public void parseData(JSONBuilder jsData){
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("audios", JSONObject[].class);
        mDatas = new ArrayList<AudioData>();
        AudioData audioData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            audioData = new AudioData();
            audioData.title = jobj.optString("title");
            audioData.name = jobj.optString("name");
            audioData.listened = jobj.optString("listened");
            audioData.paid = jobj.optString("paid");
            audioData.novelStatus = jobj.optString("novelStatus");
            audioData.latest = jobj.optString("latest");
            mDatas.add(audioData);
        }
    }
}