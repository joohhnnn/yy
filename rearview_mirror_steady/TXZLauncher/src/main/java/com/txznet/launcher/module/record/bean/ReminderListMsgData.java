package com.txznet.launcher.module.record.bean;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 提醒列表数据
 */
public class ReminderListMsgData extends BaseListMsgData {

    public ReminderListMsgData() {
        super(TYPE_FULL_LIST_REMINDER);
    }

    public class ReminderData {
        public String content;
        public String time;
        public String position;
    }

    @Override
    public void parseData(JSONBuilder jsData) {
        parseTitle(jsData);
        JSONObject[] arrayObjs = jsData.getVal("reminders", JSONObject[].class);
        mDatas = new ArrayList<ReminderData>();
        ReminderData reminderData;
        for (int i = 0; i < mTitleInfo.count; i++) {
            JSONObject jobj = arrayObjs[i];
            reminderData = new ReminderData();
            reminderData.content = jobj.optString("content");
            reminderData.time = jobj.optString("time");
            reminderData.position = jobj.optString("position");
            mDatas.add(reminderData);
        }
    }
}
