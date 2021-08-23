package com.txznet.txz.component.choice.list;

import android.text.TextUtils;

import com.txznet.comm.remote.util.AsrUtil.AsrComplexSelectCallback;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData;
import com.txznet.comm.ui.viewfactory.data.CompetitionViewData.CompetitionData.CompetitionBean;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResCompetitionPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * 赛事的WorkChoice
 */
public class CompetitionWorkChoice extends WorkChoice<CompetitionData, CompetitionBean> {

    private boolean mHasPeriodNotStart;
    private boolean mHasPeriodUnderWay;
    private boolean mHasPeriodEnd;
    private String mCompetition;

    public CompetitionWorkChoice(CompentOption<CompetitionBean> option) {
        super(option);
    }


    @Override
    public void showChoices(CompetitionData data) {
        if (data == null) {
            LogUtil.loge("showChoices Competition isEmpty！");
            return;
        }
        if (data.mCompetitionBeans.size() > 1) {
            getOption().setTtsText(NativeData.getResString("RS_VOICE_COMPETITION_SELECT_LIST_MORE_SPK"));
        } else {
            getOption().setTtsText(NativeData.getResString("RS_VOICE_COMPETITION_SELECT_LIST_SPK"));
        }

        if (!is2_0Version()) {
            getOption().setNumPageSize(data.mCompetitionBeans.size());
        }
        mHasPeriodNotStart = false;
        mHasPeriodUnderWay = false;
        mHasPeriodEnd = false;
        for (int i = 0; i < data.mCompetitionBeans.size(); i++) {
            CompetitionBean competitionBean = data.mCompetitionBeans.get(i);
            if (TextUtils.equals(competitionBean.mPeriod, "未开始")) {
                mHasPeriodNotStart = true;
            } else if (TextUtils.equals(competitionBean.mPeriod, "进行中")) {
                mHasPeriodUnderWay = true;
            } else if (TextUtils.equals(competitionBean.mPeriod, "已结束")) {
                mHasPeriodEnd = true;
            }
            mCompetition = competitionBean.mCompetition;
        }
        super.showChoices(data);
    }


    @Override
    public String getReportId() {
        return "competition";
    }

    @Override
    protected void onConvToJson(CompetitionData competitionData, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.COMPETITION_LIST);
        jsonBuilder.put("count", competitionData.mCompetitionBeans.size());

        List<JSONObject> objs = new ArrayList<JSONObject>();
        for (CompetitionBean competitionBean : competitionData.mCompetitionBeans) {
            JSONObject obj = CompetitionBean.convToJson(competitionBean);
            objs.add(obj);
        }
        String title = String.format("%s %s", competitionData.mCompetition, competitionData.mDatetime);
//        jsonBuilder.put("title", title);

        jsonBuilder.put("competitions", objs.toArray());

        jsonBuilder.put("dateTime", competitionData.mDatetime);
        jsonBuilder.put("sportsType", competitionData.mSportsType);
        jsonBuilder.put("competition", competitionData.mCompetition);
        jsonBuilder.put("startTimeStamp", competitionData.mStartTimeStamp);


        jsonBuilder.put("prefix", title);
        jsonBuilder.put("vTips", getTips());
    }

    private String[] mTips = null;

    /**
     * 从res中取两组，每组取1个
     *
     * @return
     */
    private String getTips() {
        if (mTips == null) {
            String[] tipsArr = null;
            if (mPage.getMaxPage() == 1) {
                tipsArr = NativeData.getResStringArray("RS_TIPS_SPORT_COMPETITION_SELECT_LIST_ONE");
            } else {
                tipsArr = NativeData.getResStringArray("RS_TIPS_SPORT_COMPETITION_SELECT_LIST");
            }
            if (tipsArr == null) {
                return "";
            }
            String mPeriodTips = NativeData.getResString("RS_TIPS_SPORT_COMPETITION_SELECT_LIST_PERIOD");
            if (mPeriodTips == null) {
                mPeriodTips = "";
            }
            if (!mHasPeriodNotStart) {
                mPeriodTips = replacePeriod(mPeriodTips,"未开始的");
            }
            if (!mHasPeriodUnderWay) {
                mPeriodTips = replacePeriod(mPeriodTips,"进行中的");
            }
            if (!mHasPeriodEnd) {
                mPeriodTips = replacePeriod(mPeriodTips,"已结束的");
            }

            if (TextUtils.isEmpty(mPeriodTips)){
                mTips = tipsArr;
            } else {
                mTips = new String[tipsArr.length + 1];
                System.arraycopy(tipsArr, 0, mTips, 0, tipsArr.length);
                mTips[mTips.length - 1] = mPeriodTips;
            }
        }
        
        String tipsGroup1 = randomStr(mTips);
        String[] tipsGroupArr1 = tipsGroup1.split("#SPLIT#");
        String tips1 = randomStr(tipsGroupArr1);
        String tipsGroup2 = randomStr(mTips);
        for (int i = 0; i < 3; i++) {
            if (TextUtils.equals(tipsGroup1, tipsGroup2)) {
                tipsGroup2 = randomStr(mTips);
            } else {
                break;
            }
        }
        String[] tipsGroupArr2 = tipsGroup2.split("#SPLIT#");
        String tips2 = randomStr(tipsGroupArr2);
        return (tips1 + "；" + tips2).replaceAll("%COMPETITION%", mCompetition);
    }

    private String replacePeriod(String tips, String str) {
        return tips.replace(str+"#SPLIT#","").replace("#SPLIT#" + str,"").replace(str,"");
    }

    private String randomStr(String[] arr) {
        if (arr == null || arr.length == 0) {
            return "";
        }
        Random random = new Random();
        return arr[random.nextInt(arr.length)];
    }


    @Override
    protected void onSelectIndex(CompetitionBean item, boolean isFromPage, int idx, String fromVoice) {
        selectItem(item, fromVoice);
    }

    private void selectItem(final CompetitionBean info, String fromVoice) {
        JNIHelper.logd("fromVoice : " + fromVoice);
        String tts = getSpeakText(info);
        speakWithTips(tts);
    }

    private String getSpeakText(CompetitionBean competitionBean) {

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(competitionBean.mStartTimeStamp * 1000L);
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        //老的时间减去今天的时间
        long intervalMilli = calendar.getTimeInMillis() - today.getTimeInMillis();
        int xcts = (int) (intervalMilli / (24 * 60 * 60 * 1000));
        String mDate = "";
        // -2:前天 -1：昨天 0：今天 1：明天 2：后天
        if (xcts >= -1 && xcts <= 1) {
            switch (xcts) {
                case -1:
                    mDate = "昨天";
                    break;
                case 0:
                    mDate = "今天";
                    break;
                case 1:
                    mDate = "明天";
                    break;
            }
        } else {
            mDate = String.format("%d月%d日", calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
        }
        String mTts;
        try {
            if (TextUtils.equals(competitionBean.mPeriod, "未开始")) {
                calendar.setTimeInMillis(competitionBean.mStartTimeStamp * 1000L);
                String time = String.format("%d点%d分", calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE));
                mTts = NativeData.getResString("RS_VOICE_COMPETITION_SELECT_LIST_NOT_STARTED")
                        .replace("%DATE%", mDate)
                        .replace("%TEAM1%", competitionBean.mHomeTeam.mName)
                        .replace("%TEAM2%", competitionBean.mAwayTeam.mName)
                        .replace("%TIME%", time);
            } else {
                CompetitionBean.Team winTeam;
                CompetitionBean.Team loseTeam;
                String result;
                if (TextUtils.equals(competitionBean.mPeriod, "已结束")) {
                    if (competitionBean.mHomeTeam.mGoal > competitionBean.mAwayTeam.mGoal) {
                        winTeam = competitionBean.mHomeTeam;
                        loseTeam = competitionBean.mAwayTeam;
                        result = "战胜";
                    } else if (competitionBean.mHomeTeam.mGoal < competitionBean.mAwayTeam.mGoal) {
                        loseTeam = competitionBean.mHomeTeam;
                        winTeam = competitionBean.mAwayTeam;
                        result = "战胜";
                    } else {
                        winTeam = competitionBean.mHomeTeam;
                        loseTeam = competitionBean.mAwayTeam;
                        result = "战平";
                    }
                } else {
                    if (competitionBean.mHomeTeam.mGoal > competitionBean.mAwayTeam.mGoal) {
                        winTeam = competitionBean.mHomeTeam;
                        loseTeam = competitionBean.mAwayTeam;
                        result = "领先";
                    } else if (competitionBean.mHomeTeam.mGoal < competitionBean.mAwayTeam.mGoal) {
                        loseTeam = competitionBean.mHomeTeam;
                        winTeam = competitionBean.mAwayTeam;
                        result = "领先";
                    } else {
                        result = "持平";
                        winTeam = competitionBean.mHomeTeam;
                        loseTeam = competitionBean.mAwayTeam;
                    }
                }
                mTts = NativeData.getResString("RS_VOICE_COMPETITION_SELECT_LIST_RESULT")
                        .replace("%DATE%", mDate)
                        .replace("%TEAM1%", competitionBean.mHomeTeam.mName)
                        .replace("%TEAM2%", competitionBean.mAwayTeam.mName)
                        .replace("%PERIOD%", competitionBean.mPeriod)
                        .replace("%WIN_TEAM%", winTeam.mName)
                        .replace("%WIN_GOAL%", winTeam.mGoal + "")
                        .replace("%LOSE_GOAL%", loseTeam.mGoal + "")
                        .replace("%RESULT%", result)
                        .replace("%LOSE_TEAM%", loseTeam.mName);

            }
        } catch (Exception e) {
            mTts = "";
        }

        return mTts;
    }

    @Override
    protected ResourcePage<CompetitionData, CompetitionBean> createPage(CompetitionData sources) {
        return new ResCompetitionPage(sources) {
            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
    }

    @Override
    protected void onAddWakeupAsrCmd(AsrComplexSelectCallback acsc, CompetitionData data) {
        super.onAddWakeupAsrCmd(acsc, data);

        final CompetitionData mCompetitionData = mData;

        for (int i = 0; i < mCompetitionData.mCompetitionBeans.size(); i++) {
            CompetitionBean competitionBean = mCompetitionData.mCompetitionBeans.get(i);
            if (TextUtils.equals(competitionBean.mPeriod, "未开始")) {
                addIndex(acsc, i, "未开始");
            } else if (TextUtils.equals(competitionBean.mPeriod, "进行中")) {
                addIndex(acsc, i, "进行中");
            } else if (TextUtils.equals(competitionBean.mPeriod, "已结束")) {
                addIndex(acsc, i, "已结束");
            }

            //只看
            addIndex(acsc, i, "只看" + competitionBean.mCompetition);
        }


        acsc.addCommand("CMD_EARLIEST", "最早的");
        acsc.addCommand("CMD_LATEST", "最晚的");

        acsc.addCommand("CMD_MOST_FRONT", "最上面那个");
        acsc.addCommand("CMD_MOST_BEHIND", "最下面那个");

        getOption().setCanSure(false);
        if (mCompetitionData != null && mCompetitionData.mCompetitionBeans != null && mCompetitionData.mCompetitionBeans.size() == 1) {
            getOption().setCanSure(true);
        }
    }

    @Override
    protected boolean onCommandSelect(String type, String command) {
        if (TextUtils.equals(type, "CMD_EARLIEST")) {
            int page = 1;
            int index = 0;
            selectPage(page, null);
            selectIndex(index, command);
        } else if (TextUtils.equals(type, "CMD_LATEST")) {
            int page = mPage.getMaxPage();
            selectPage(page, null);
            int index = mPage.getCurrPageSize() - 1;
            selectIndex(index, command);
        } else if (TextUtils.equals(type, "CMD_MOST_FRONT")) {
            selectIndex(0, command);
        } else if (TextUtils.equals(type, "CMD_MOST_BEHIND")) {
            selectIndex(mPage.getCurrPageSize() - 1, command);
        }
        return super.onCommandSelect(type, command);
    }

    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
        if (indexs.size() != 1) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    ArrayList<CompetitionBean> competitionBeans = new ArrayList<CompetitionBean>();
                    for (Integer index : indexs) {
                        if (index < mData.mCompetitionBeans.size()) {
                            competitionBeans.add(mData.mCompetitionBeans.get(index));
                        }
                    }
                    mData.mCompetitionBeans = competitionBeans;
                    refreshData(mData);
                    String command2 = command.replace("只看", "");
                    String mLastHintText = NativeData.getResString("RS_VOICE_AREADY_SELECTOR").replace("%CMD%", command2);
                    speakWithTips(mLastHintText);
                }
            });
            return true;
        }

        return super.onIndexSelect(indexs, command);
    }

    private void addIndex(AsrComplexSelectCallback acsc, int index, String keywords) {
        if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
            acsc.addIndex(index, keywords);
        }
    }

    @Override
    protected String convItemToString(CompetitionBean item) {
        JSONObject jsonObject = CompetitionBean.convToJson(item);
        return jsonObject.toString();
    }

    @Override
    protected void onClearSelecting() {
        super.onClearSelecting();
        mTips = null;
    }
}
