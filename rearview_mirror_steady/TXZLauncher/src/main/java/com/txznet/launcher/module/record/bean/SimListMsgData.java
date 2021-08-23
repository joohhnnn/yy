package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 流量充值列表数据
 */

public class SimListMsgData extends BaseListMsgData {
    public SimListMsgData() {
        super(TYPE_FULL_LIST_SIM);
    }

    public class SimData {
        public int id;
        public String title;
        public int price;
        public int rawPrice;
        public String qrcode;
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("data", JSONObject[].class);
        mDatas = new ArrayList<SimData>();
        SimData simData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            simData = new SimData();
            simData.id = jobj.optInt("id");
            simData.title = jobj.optString("title");
            simData.price = jobj.optInt("price");
            simData.rawPrice = jobj.optInt("rawPrice");
            simData.qrcode = jobj.optString("qrcode");
            mDatas.add(simData);
        }
    }
}
