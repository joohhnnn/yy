package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 简单的列表数据
 */

public class SimpleListMsgData extends BaseListMsgData<SimpleListMsgData.SimpleData> {

    public SimpleListMsgData() {
        super(TYPE_FULL_LIST_SIMPLE_LIST);
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        String[] jsonObjects = jsData.getVal("beans", String[].class);
        mDatas = new ArrayList<SimpleData>();
        SimpleData simpleData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            simpleData = new SimpleData();
            simpleData.title = jsonObjects[i];
            mDatas.add(simpleData);
        }
    }

    public class SimpleData{
        public String title;
    }
}
