package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import static com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.TYPE_SPORTS_NONE;

/**
 * 赛事查询
 */
public class CompetitionViewData extends ListViewData {

    public CompetitionViewData() {
        super(TYPE_FULL_LIST_COMPETITION);
    }


    public CompetitionData mCompetitionData;

    @Override
    public void parseItemData(JSONBuilder data) {
        mCompetitionData = new CompetitionData();
        mCompetitionData.mCompetitionBeans = new ArrayList<CompetitionData.CompetitionBean>();
        mCompetitionData.mDatetime = data.getVal("dateTime", String.class);
        mCompetitionData.mSportsType = data.getVal("sportsType", Integer.class, TYPE_SPORTS_NONE);
        mCompetitionData.mCompetition = data.getVal("competition", String.class);
        mCompetitionData.mStartTimeStamp = data.getVal("startTimeStamp", Long.class, System.currentTimeMillis() / 1000L);
        JSONArray obJsonArray = data.getVal("competitions", JSONArray.class);
        if (obJsonArray != null) {
            for (int i = 0; i < count; i++) {
                try {
                    CompetitionData.CompetitionBean competitionBean = CompetitionData.CompetitionBean.parseFromData(obJsonArray.getJSONObject(i));
                    mCompetitionData.mCompetitionBeans.add(competitionBean);
                } catch (JSONException e) {
                }
            }
        }
    }


    /**
     * 赛事查询的数据
     */
    public static class CompetitionData {
        public static final int TYPE_SPORTS_NONE = 0;
        public static final int TYPE_SPORTS_NBA = 1;
        public static final int TYPE_SPORTS_FOOTBALL = 2;

        public String mAnswer;

        public int mSportsType = TYPE_SPORTS_NBA;
        public String mCompetition;
        public String mDatetime;
        public ArrayList<CompetitionBean> mCompetitionBeans;
        //起始的时间戳
        public long mStartTimeStamp;

        /**
         * 赛事详情
         */
        public static class CompetitionBean {
            public Team mAwayTeam;//客场队伍
            public Team mHomeTeam;//主场队伍
            public String mPeriod;//当前比赛状态
            public String mRoundType;//赛季类型
            //		public String mStartTime;//开始时间
            public long mStartTimeStamp;//开始时间戳

            public String mCompetition;//
            public int mSportsType;//赛事类型

            public static JSONObject convToJson(CompetitionBean competitionBean) {
                return new JSONBuilder()
                        .put("awayTeam", Team.convToJson(competitionBean.mAwayTeam))
                        .put("homeTeam", Team.convToJson(competitionBean.mHomeTeam))
                        .put("competition", competitionBean.mCompetition)
                        .put("period", competitionBean.mPeriod)
                        .put("roundType", competitionBean.mRoundType)
                        .put("startTimeStamp", competitionBean.mStartTimeStamp)
                        .put("sportsType", competitionBean.mSportsType)
                        .build();
            }

            public static CompetitionBean parseFromData(JSONObject data) {
                if (data == null) {
                    return null;
                }
                try {
                    JSONBuilder jsonBuilder = new JSONBuilder(data);
                    CompetitionBean competitionBean = new CompetitionBean();
                    competitionBean.mAwayTeam = Team.parseFromData(jsonBuilder.getVal("awayTeam", JSONObject.class));
                    competitionBean.mHomeTeam = Team.parseFromData(jsonBuilder.getVal("homeTeam", JSONObject.class));
                    competitionBean.mCompetition = jsonBuilder.getVal("competition", String.class);
                    competitionBean.mPeriod = jsonBuilder.getVal("period", String.class);
                    competitionBean.mRoundType = jsonBuilder.getVal("roundType", String.class);
                    competitionBean.mStartTimeStamp = jsonBuilder.getVal("startTimeStamp", Long.class, 0L);
                    competitionBean.mSportsType = jsonBuilder.getVal("sportsType", Integer.class, TYPE_SPORTS_NONE);
                    return competitionBean;
                } catch (Exception e) {
                    LogUtil.loge("parse CompetitionBean error " + e.getMessage());
                }
                return null;
            }

            /**
             * 比赛队伍
             */
            public static class Team {
                public String mName;
                public String mLogo;
                public int mGoal;//得分

                public static JSONObject convToJson(Team team) {
                    return new JSONBuilder()
                            .put("name", team.mName)
                            .put("logo", team.mLogo)
                            .put("goal", team.mGoal)
                            .build();
                }

                public static Team parseFromData(JSONObject data) {
                    if (data == null) {
                        return null;
                    }
                    try {
                        JSONBuilder jsonBuilder = new JSONBuilder(data);
                        Team team = new Team();
                        team.mName = jsonBuilder.getVal("name", String.class);
                        team.mLogo = jsonBuilder.getVal("logo", String.class);
                        team.mGoal = jsonBuilder.getVal("goal", Integer.class, 0);
                        return team;
                    } catch (Exception e) {
                        LogUtil.loge("parse Team error " + e.getMessage());
                    }
                    return null;
                }
            }
        }
    }
}
