package com.txznet.txz.module.competition;

import android.text.TextUtils;

import com.txz.ui.voice.VoiceData;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean.Team;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.choice.ChoiceManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

import static com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.TYPE_SPORTS_FOOTBALL;
import static com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.TYPE_SPORTS_NBA;
import static com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.TYPE_SPORTS_NONE;

/**
 * Created by Honge on 2019/5/11.
 * 赛事查询的管理类
 */

public class CompetitionManager {
    private static CompetitionManager sInstance;

    public static CompetitionManager getInstance() {
        if (sInstance == null) {
            synchronized (CompetitionManager.class) {
                if (sInstance == null) {
                    sInstance = new CompetitionManager();
                }
            }
        }
        return sInstance;
    }

    /**
     * 处理赛事的语义
     *
     * @param rawVoiceData
     * @return
     */
    public boolean parseCompetition(VoiceData.VoiceParseData rawVoiceData) {
        JSONBuilder jsonBuilder = new JSONBuilder(rawVoiceData.strVoiceData);
        String strAction = jsonBuilder.getVal("action", String.class);
        if (TextUtils.equals("search_schedule", strAction)) {
            CompetitionData competitionData = new CompetitionData();
            competitionData.mAnswer = jsonBuilder.getVal("answer", String.class, "");

            try {
                JSONArray jsonArray = jsonBuilder.getVal("competition_infos", JSONArray.class);
                if (jsonArray != null && jsonArray.length() != 0) {
                    competitionData.mCompetitionBeans = new ArrayList<CompetitionBean>();
                    CompetitionBean competitionBean = null;
                    JSONBuilder competitionJson;
                    JSONBuilder homeTeamJson;
                    JSONBuilder awayTeamJson;
                    long startDate = -1;
                    long endDate = -1;
                    for (int i = 0; i < jsonArray.length(); i++) {
                        competitionJson = new JSONBuilder(jsonArray.getJSONObject(i));
                        competitionBean = new CompetitionBean();
                        competitionBean.mStartTimeStamp = competitionJson.getVal("start_timestamp", Long.class, 0L);
                        //记录可能是多天的比赛
                        if (competitionBean.mStartTimeStamp != 0 && (startDate == -1 || startDate > competitionBean.mStartTimeStamp)) {
                            startDate = competitionBean.mStartTimeStamp;
                        }
                        if ((competitionBean.mStartTimeStamp) != 0 && (endDate < competitionBean.mStartTimeStamp)) {
                            endDate = competitionBean.mStartTimeStamp;
                        }

                        competitionBean.mPeriod = competitionJson.getVal("period", String.class);
                        //除了未开始和已结束的状态，都认为是进行中
                        if (!(TextUtils.equals(competitionBean.mPeriod, "未开始") || TextUtils.equals(competitionBean.mPeriod, "已结束"))) {
                            competitionBean.mPeriod = "进行中";
                        }
                        competitionBean.mRoundType = competitionJson.getVal("round_type", String.class);
                        competitionBean.mSportsType = competitionJson.getVal("sport_type", Integer.class, TYPE_SPORTS_NONE);
                        competitionBean.mCompetition = competitionJson.getVal("competition", String.class, "");

                        homeTeamJson = new JSONBuilder(competitionJson.getVal("home_team", JSONObject.class));
                        competitionBean.mHomeTeam = new Team();
                        competitionBean.mHomeTeam.mName = homeTeamJson.getVal("name", String.class);
                        competitionBean.mHomeTeam.mLogo = homeTeamJson.getVal("logo", String.class);
                        competitionBean.mHomeTeam.mGoal = homeTeamJson.getVal("goal", Integer.class, 0);

                        awayTeamJson = new JSONBuilder(competitionJson.getVal("away_team", JSONObject.class));
                        competitionBean.mAwayTeam = new Team();
                        competitionBean.mAwayTeam.mName = awayTeamJson.getVal("name", String.class);
                        competitionBean.mAwayTeam.mLogo = awayTeamJson.getVal("logo", String.class);
                        competitionBean.mAwayTeam.mGoal = awayTeamJson.getVal("goal", Integer.class, 0);

                        competitionData.mCompetitionBeans.add(competitionBean);
                    }

                    if (startDate == -1 || endDate == -1) {
                        return false;
                    }
                    competitionData.mStartTimeStamp = startDate;
                    competitionData.mDatetime = convertDatetime(startDate, endDate);

                    competitionData.mCompetition = "赛事";
                    competitionData.mSportsType = competitionBean.mSportsType;
                    switch (competitionData.mSportsType) {
                        case TYPE_SPORTS_NBA:
                            competitionData.mCompetition = "NBA赛事";
                            break;
                        case TYPE_SPORTS_FOOTBALL:
                            competitionData.mCompetition = "足球比赛";
                            break;
                        case TYPE_SPORTS_NONE:
                            break;
                    }
                    //大于20个数据时，直接播报反馈语
                    if (competitionData.mCompetitionBeans.size() > 20) {
                        RecorderWin.speakText(competitionData.mAnswer, null);
                    } else if (competitionData.mCompetitionBeans.size() > 1) {
                        //大于一个显示列表，等于一个直接显示
                        Collections.sort(competitionData.mCompetitionBeans, new Comparator<CompetitionBean>() {
                            @Override
                            public int compare(CompetitionBean o1, CompetitionBean o2) {
                                try {
                                    if (o1.mStartTimeStamp > o2.mStartTimeStamp) {
                                        return 1;
                                    } else if (o1.mStartTimeStamp < o2.mStartTimeStamp) {
                                        return -1;
                                    }
                                } catch (Exception e) {

                                }
                                return 0;
                            }
                        });
                        ChoiceManager.getInstance().showCompetition(competitionData);
                    } else {
                        JSONBuilder jsonCompetitionBean = new JSONBuilder()
                                .put("data", CompetitionBean.convToJson(competitionBean))
                                .put("type", RecorderWin.COMPETITION_DETAIL)
                                .put("vTips", getTips());

                        RecorderWin.speakText(competitionData.mAnswer, null);
                        RecorderWin.showData(jsonCompetitionBean.toString());
                    }
                    return true;

                } else {
                    RecorderWin.speakText(competitionData.mAnswer, null);
                    return true;
                }
            } catch (Exception e) {
                LogUtil.loge("parse competition error: " + e.getMessage());
            }

        }
        return false;
    }

    private String convertDatetime(long startDate, long endDate) {
        String mDatetime;
        Calendar startCalendar = Calendar.getInstance();
        startCalendar.setTimeInMillis(startDate * 1000L);
        int startYear = startCalendar.get(Calendar.YEAR);
        int startMonth = startCalendar.get(Calendar.MONTH) + 1;
        int startDay = startCalendar.get(Calendar.DAY_OF_MONTH);

        Calendar endCalendar = Calendar.getInstance();
        endCalendar.setTimeInMillis(endDate * 1000L);
        int endYear = endCalendar.get(Calendar.YEAR);
        int endMonth = endCalendar.get(Calendar.MONTH) + 1;
        int endDay = endCalendar.get(Calendar.DAY_OF_MONTH);


        if (startYear == endYear) {
            if (startMonth == endMonth) {
                if (startDay == endDay) {
                    mDatetime = String.format("%d年%d月%d日", startYear, startMonth, startDay);
                } else {
                    mDatetime = String.format("%d年%d月%d日-%d日", startYear, startMonth, startDay, endDay);
                }
            } else {
                mDatetime = String.format("%d年%d月%d日-%d月%d日", startYear, startMonth, startDay, endMonth, endDay);
            }
        } else {
            mDatetime = String.format("%d年%d月%d日-%d年%d月%d日", startYear, startMonth, startDay, endYear, endMonth, endDay);
        }
        return mDatetime;
    }

    /**
     * 从res中取两组，每组取1个
     *
     * @return
     */
    private String getTips() {
        String[] tipsArr = NativeData.getResStringArray("RS_TIPS_SPORT_COMPETITION");
        String tipsGroup1 = randomStr(tipsArr);
        String[] tipsGroupArr1 = tipsGroup1.split("#SPLIT#");
        String tips1 = randomStr(tipsGroupArr1);
        String tipsGroup2 = randomStr(tipsArr);
        for (int i = 0; i < 3; i++) {
            if (TextUtils.equals(tipsGroup1, tipsGroup2)) {
                tipsGroup2 = randomStr(tipsArr);
            } else {
                break;
            }
        }
        String[] tipsGroupArr2 = tipsGroup2.split("#SPLIT#");
        String tips2 = randomStr(tipsGroupArr2);
        return tips1 + "；" + tips2;
    }

    private String randomStr(String[] arr) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        Random random = new Random();
        return arr[random.nextInt(arr.length)];
    }

}
