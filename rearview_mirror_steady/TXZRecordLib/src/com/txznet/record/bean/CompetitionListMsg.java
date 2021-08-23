package com.txznet.record.bean;

import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.adapter.CompetitionListAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

import static com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.TYPE_SPORTS_NONE;

public class CompetitionListMsg extends BaseDisplayMsg<CompetitionListAdapter.CompetitionItem> {
    public int count;
    public String mDatetime;
    public int mSportsType;
    public String mCompetition;
    public long mStartTimeStamp;

    public CompetitionListMsg() {
        super(TYPE_FROM_SYS_COMPETITION_LIST);
    }

    public void parseData(JSONBuilder data) {

        count = data.getVal("count", Integer.class, 0);
        ArrayList<CompetitionListAdapter.CompetitionItem> mCompetitionBeans = new ArrayList<CompetitionListAdapter.CompetitionItem>();
        mDatetime = data.getVal("dateTime", String.class);
        mSportsType = data.getVal("sportsType", Integer.class, TYPE_SPORTS_NONE);
        mCompetition = data.getVal("competition", String.class);
        mStartTimeStamp = data.getVal("startTimeStamp", Long.class, System.currentTimeMillis() / 1000L);
        JSONArray obJsonArray = data.getVal("competitions", JSONArray.class);
        if (obJsonArray != null) {
            for (int i = 0; i < count; i++) {
                try {
                    CompetitionListAdapter.CompetitionItem competitionItem = new CompetitionListAdapter.CompetitionItem();
                    competitionItem.mCurTimeStamp = mStartTimeStamp;
                    competitionItem.mItem = CompetitionData.CompetitionBean.parseFromData(obJsonArray.getJSONObject(i));
                    mCompetitionBeans.add(competitionItem);
                } catch (JSONException e) {
                }
            }
        }

        mItemList = mCompetitionBeans;
    }

}
