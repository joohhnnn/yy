package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 主题选择列表数据
 */
public class TtsListMsgData extends BaseListMsgData<TtsListMsgData.TtsData> {

    public TtsListMsgData() {
        super(TYPE_FULL_LIST_TTS);
    }

    public class TtsData {
        public int id;
        public String name;
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("themes", JSONObject[].class);
        mDatas = new ArrayList<TtsData>();
        TtsData ttsData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            ttsData = new TtsData();
            ttsData.id = jobj.optInt("id");
            ttsData.name = jobj.optString("name");
            mDatas.add(ttsData);
        }
    }
}
