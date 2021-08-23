package com.txznet.txz.component.choice.list;


import android.text.TextUtils;
import android.view.animation.Animation;

import com.txz.log_manager.LogManager;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.ui.viewfactory.IViewStateListener;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.film.FilmPriceComparator;
import com.txznet.txz.component.film.FilmTimeComparator;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.win.record.RecorderWin;

import org.json.JSONArray;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Date;

public class MovieTimeWorkChoice  extends  WorkChoice<List<MovieTimeWorkChoice.MovieTimeItem>, MovieTimeWorkChoice.MovieTimeItem>{

    public MovieTimeWorkChoice(CompentOption<MovieTimeItem> option) {
        super(option);
    }

    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static class MovieTimeItem{
        public String answer;
        public String showVersion;
        public String showTime;
        public String closeTime;
        public String hallName;
        public String showName;
        public int unitPrice;
    }


    @Override
    public void showChoices(List<MovieTimeWorkChoice.MovieTimeItem> data) {
        if(data.size() == 1){
            getOption().setCanSure(true);
        }
        super.showChoices(data);
    }

    @Override
    protected void onConvToJson(List<MovieTimeItem> ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type",RecorderWin.FILM_SENCE_MOVICE_TIME);
        jsonBuilder.put("prefix", "找到以下电影院");
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < ts.size(); i++) {
            MovieTimeItem cb = ts.get(i);
            JSONObject obj = new JSONBuilder().put("showVersion", cb.showVersion).put("showTime", cb.showTime)
                    .put("closeTime", cb.closeTime).put("hallName",cb.hallName).put("unitPrice",cb.unitPrice).put("showName", cb.showName)
                    .getJSONObject();
            jsonArray.put(obj);
        }
        jsonBuilder.put("cines", jsonArray);
        jsonBuilder.put("count", jsonArray.length());
        jsonBuilder.put("vTips",getTips());

    }

    private String getTips(){
        String tips = "";
        if (mPage != null) {
            if (mPage.getMaxPage() == (mPage.getCurrPage() + 1)) { //是最后一页或者只有一页
                if (mPage.getMaxPage() == 1) {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_ONE_PAGE");
                } else {
                    tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_LAST");
                }
            } else if ((mPage.getCurrPage() + 1) == 1) {  //第一页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE_FIRST");
            } else { //其他中间页
                tips = NativeData.getResString("RS_VOICE_TIPS_MOVIE");
            }
        }
        return tips;
    }

    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, List<MovieTimeWorkChoice.MovieTimeItem> data) {
        super.onAddWakeupAsrCmd(acsc, data);
        acsc.addCommand("PRE_PAGER", "上面那页");
        acsc.addCommand("NEXT_PAGER", "下面那页");
        acsc.addCommand("FILM_SHOW_TIME_SORT","时间排序");
        acsc.addCommand("FILM_PRICE_SORT","价格排序");
        acsc.addCommand("FILM_TICKET_CANCEL",NativeData.getResStringArray("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));
        List<MovieTimeWorkChoice.MovieTimeItem> mMovieTimeItems = mData;

        final int currPageSize = mPage.getCurrPageSize();
        for (int i = 0; i < currPageSize; i++) {
            String strIndex = NativeData.getResString("RS_VOICE_DIGITS", i + 1);
            if (i == 0) {
                acsc.addCommand("ITEM_INDEX_" + i, "第一场", "滴场");
            } else {
                acsc.addCommand("ITEM_INDEX_" + i, "第" + strIndex + "场");
            }
        }

        Date earliestDate = null;
        int earliestIndex = -1;
        Date lastDate = null;
        int lastIndex = -1;
        for(int i = 0; i < mMovieTimeItems.size(); i++){
            try {
                Date date = sdf.parse(mMovieTimeItems.get(i).showTime);
                if(earliestDate == null || date.before(earliestDate)){
                    earliestDate = date;
                    earliestIndex = i;
                }
                if(lastDate == null || date.after(lastDate)){
                    lastDate = date;
                    lastIndex = i;
                }
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                int hours = calendar.get(Calendar.HOUR_OF_DAY);
                String sHours = NativeData.getResString("RS_VOICE_DIGITS", hours);
                addKeyWord(acsc, i, sHours+"点那场");
                int minute = calendar.get(Calendar.MINUTE);
                String sMinute = NativeData.getResString("RS_VOICE_DIGITS", minute);
                addKeyWord(acsc, i, sHours+"点"+sMinute+"分那场");
                if(minute == 30){
                    addKeyWord(acsc, i, sHours+"点"+"半那场");
                }
                if(minute / 10 != 0 && minute % 10 != 0){
                    addKeyWord(acsc, i, sHours+"点"+"零"+sMinute+"分那场");
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        if(earliestIndex >= 0){
            addKeyWord(acsc, earliestIndex, "最早的");
        }

        if(lastIndex >= 0){
            addKeyWord(acsc, lastIndex, "最晚的");
        }

    }

    private void addKeyWord(AsrUtil.AsrComplexSelectCallback acsc, int index, String keywords) {
        if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
            acsc.addIndex(index, keywords);
        }
    }


    @Override
    protected boolean onCommandSelect(String type, String speech) {
        final String command = speech;

        if("FILM_PRICE_SORT".equals(type)){
            sortFilmPrice(mData, speech);
            return true;
        }
        if("FILM_SHOW_TIME_SORT".equals(type)){
            sortFilmShowTime(mData, speech);;
            return true;
        }
        if("FILM_TICKET_CANCEL".equals(type)){
            FilmManager.getInstance().cancel(command);
            return true;
        }
        return super.onCommandSelect(type, command);
    }

    private void sortFilmShowTime(List<MovieTimeWorkChoice.MovieTimeItem> data, String speech){
        FilmTimeComparator filmTimeComparator = new FilmTimeComparator();
        Collections.sort(data,filmTimeComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        //speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
        speakWithTips(sortSpk);
    }

    private void sortFilmPrice(List<MovieTimeWorkChoice.MovieTimeItem> data, String speech){
        FilmPriceComparator filmPriceComparator = new FilmPriceComparator();
        Collections.sort(data,filmPriceComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        //speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
        speakWithTips(sortSpk);
    }

    @Override
    protected String convItemToString(MovieTimeItem item) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("showVersion", item.showVersion);
        jsonBuilder.put("showTime", item.showTime);
        jsonBuilder.put("closeTime", item.closeTime);
        jsonBuilder.put("hallName", item.hallName);
        jsonBuilder.put("unitPrice", item.unitPrice);
        jsonBuilder.put("showName", item.showName);
        return jsonBuilder.toString();
    }

    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
        if (indexs.size() != 1) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    List<MovieTimeWorkChoice.MovieTimeItem> data = new ArrayList<MovieTimeWorkChoice.MovieTimeItem>();
                    for (Integer idx : indexs) {
                        if (idx < mData.size()) {
                            data.add(mData.get(idx));
                        }
                    }
                    mData = data;
                    refreshData(mData);
                }
            });
            return true;
        }
        int page = indexs.get(0) / mCompentOption.getNumPageSize() + 1;
        selectPage(page, null);
        return false;
    }

    @Override
    protected void onSelectIndex(MovieTimeItem item, boolean isFromPage, int idx, String fromVoice) {

    }

    @Override
    public void stopTtsAndAsr() {
        JNIHelper.logd("stopTtsAndAsr");
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
        AsrManager.getInstance().cancel();
        if(isCoexistAsrAndWakeup()){
            TextResultHandle.getInstance().cancel();
        }
    }

    @Override
    protected ResourcePage<List<MovieTimeItem>, MovieTimeItem> createPage(List<MovieTimeItem> sources) {
        return  new ResListPage<MovieTimeWorkChoice.MovieTimeItem>(sources) {

            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
    }

    @Override
    public String getReportId() {
        return "Movie_Times_Select";
    }

    @Override
    protected void onTimeout(){
        super.onTimeout();
        FilmManager.getInstance().cancel(null);
    }

    @Override
    protected void onClearSelecting() {
        super.onClearSelecting();
    }

}
