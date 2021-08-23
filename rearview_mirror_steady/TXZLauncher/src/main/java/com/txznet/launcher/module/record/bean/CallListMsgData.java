package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 电话列表数据
 */
public class CallListMsgData extends BaseListMsgData<CallListMsgData.CallData> {
    public CallListMsgData() {
        super(TYPE_FULL_LIST_CALL);
    }

    public class CallData {
        public String name;
        public String number;
        public String province;
        public String city;
        public String isp;
    }

    public void parseData(JSONBuilder jsData){
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("contacts", JSONObject[].class);
        mTitleInfo.count = arrayObjs.length;
        mDatas = new ArrayList<CallData>();
        CallData callData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            callData = new CallData();
            callData.name = jobj.optString("name");
            callData.number = jobj.optString("number");
            callData.province = jobj.optString("province");
            callData.city = jobj.optString("city");
            callData.isp = jobj.optString("isp");
            mDatas.add(callData);
        }
    }
}
