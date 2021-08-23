package com.txznet.record.bean;

import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean;
import com.txznet.comm.util.JSONBuilder;

import org.json.JSONObject;

public class CompetitionMsg extends ChatMessage {
    public CompetitionBean mCompetitionBean;

    public CompetitionMsg() {
        super(TYPE_FROM_SYS_COMPETITION_DETAIL);
    }
    public void parseData(JSONBuilder data){
        mCompetitionBean = CompetitionBean.parseFromData(data.getVal("data", JSONObject.class));
    }
}
