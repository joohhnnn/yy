package com.txznet.comm.ui.viewfactory.data;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean;

import org.json.JSONObject;


/**
 * 赛事查询
 */
public class CompetitionDetailViewData extends ViewData {

    public CompetitionDetailViewData() {
        super(TYPE_CHAT_COMPETITION_DETAIL);
    }


    public CompetitionBean mCompetitionBean;
    public String vTips;

    public void parseItemData(JSONBuilder data) {
        vTips = data.getVal("vTips", String.class);
        mCompetitionBean = CompetitionBean.parseFromData(data.getVal("data", JSONObject.class));
    }

}
