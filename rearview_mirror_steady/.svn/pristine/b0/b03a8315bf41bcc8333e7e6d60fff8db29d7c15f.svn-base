package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ASUS User on 2018/3/5.
 * 微信联系人列表数据
 */
public class WechatListMsgData extends BaseListMsgData<WechatListMsgData.WechatData> {

    public WechatListMsgData() {
        super(TYPE_FULL_LIST_WECHAT);
    }

    public class WechatData {
        public String id;
        public String name;
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("contacts", JSONObject[].class);
        mDatas = new ArrayList<WechatData>();
        WechatData wechatData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            wechatData = new WechatData();
            wechatData.id = jobj.optString("id");
            wechatData.name = jobj.optString("name");
            mDatas.add(wechatData);
        }
    }
}
