package com.txznet.txz.component.choice.list;

import android.text.TextUtils;

import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.film.FilmDistanceComparator;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.KeywordsParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MovieTheaterWorkChoice extends WorkChoice<List<MovieTheaterWorkChoice.MovieTheaterItem>, MovieTheaterWorkChoice.MovieTheaterItem> {


    @Override
    protected void onSelectIndex(MovieTheaterItem item, boolean isFromPage, int idx, String fromVoice) {

    }

    @Override
    protected ResourcePage<List<MovieTheaterItem>, MovieTheaterItem> createPage(List<MovieTheaterItem> sources) {
        return  new ResListPage<MovieTheaterWorkChoice.MovieTheaterItem>(sources) {

            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
    }

    public static class MovieTheaterItem{
        public String answer;
        public String cinemaName;
        public String address;
        public String distance;
        public String locationType;
        public String cinemaFlag;
        public List<String> alias = new ArrayList<String>();
    }

    public MovieTheaterWorkChoice(CompentOption<MovieTheaterWorkChoice.MovieTheaterItem> option) {
        super(option);
    }

    @Override
    public void showChoices(List<MovieTheaterWorkChoice.MovieTheaterItem> data) {
        if(data.size() == 1){
            getOption().setCanSure(true);
        }
        super.showChoices(data);
    }

    @Override
    protected void onConvToJson(List<MovieTheaterWorkChoice.MovieTheaterItem> ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type",RecorderWin.FILM_SENCE_MOVICE_THEATER);
        jsonBuilder.put("prefix", "找到以下电影院");
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < ts.size(); i++) {
            MovieTheaterWorkChoice.MovieTheaterItem cb = ts.get(i);
            JSONArray jsonAliasArray = new JSONArray();
            for(int j = 0; j < ts.get(i).alias.size(); j++){
                try {
                    jsonAliasArray.put(j ,ts.get(i).alias.get(j));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            JSONObject obj = new JSONBuilder().put("cinemaName", cb.cinemaName).put("address", cb.address)
                                                .put("distance", cb.distance).put("alias",jsonAliasArray)
                                                  .put("locationType", cb.locationType).put("cinemaFlag", cb.cinemaFlag)
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
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, List<MovieTheaterWorkChoice.MovieTheaterItem> data) {
        super.onAddWakeupAsrCmd(acsc, data);
        List<MovieTheaterWorkChoice.MovieTheaterItem> mTheaterItems = mData;

        acsc.addCommand("PRE_PAGER", "上面那页");
        acsc.addCommand("NEXT_PAGER", "下面那页");
        acsc.addCommand("FILM_DISTANCE_SORT", "距离排序");
        acsc.addCommand("FILM_TICKET_CANCEL",NativeData.getResStringArray("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));

        for(int i = 0; i < mTheaterItems.size(); i++){
            for(int j = 0; j < mTheaterItems.get(i).alias.size(); j++){
                addKeyWord(acsc, i, mTheaterItems.get(i).alias.get(j));
            }
            for(String kw : KeywordsParser.splitAddressKeywords(mTheaterItems.get(i).address)){
                addKeyWord(acsc, i, kw);
            }
            String locationType = mTheaterItems.get(i).locationType.replace("家","家附近")
                                        .replace("目的地","目的地附近");
            addKeyWord(acsc, i, locationType);
        }
    }

    private void addCommand(AsrUtil.AsrComplexSelectCallback acsc, String type, String... cmds){
        acsc.addCommand(type,cmds);
    }

    @Override
    protected boolean onCommandSelect(String type, String speech) {
        final String command = speech;

        if("CHANGE_THE_FILM".equals(type)){
            FilmManager.getInstance().requestTxz(speech, null);
            return true;
        }
        if("FILM_TICKET_CANCEL".equals(type)){
            FilmManager.getInstance().cancel(command);
            return true;
        }if("FILM_DISTANCE_SORT".equals(type)){
            sortFilmDistance(mData, speech);
            return true;
        }

        return super.onCommandSelect(type, command);
    }

    private void sortFilmDistance(List<MovieTheaterWorkChoice.MovieTheaterItem> data, String speech){
        FilmDistanceComparator filmDistanceComparator = new FilmDistanceComparator();
        Collections.sort(data,filmDistanceComparator);
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        //speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
        speakWithTips(sortSpk);
    }

    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
        if (indexs.size() != 1) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    List<MovieTheaterWorkChoice.MovieTheaterItem> data = new ArrayList<MovieTheaterWorkChoice.MovieTheaterItem>();
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


    private void addKeyWord(AsrUtil.AsrComplexSelectCallback acsc, int index, String keywords) {
        if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
            acsc.addIndex(index, keywords);
        }
    }

    @Override
    protected String convItemToString(MovieTheaterWorkChoice.MovieTheaterItem item) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("address", item.address);
        jsonBuilder.put("cinemaName", item.cinemaName);
        jsonBuilder.put("distance", item.distance);
        jsonBuilder.put("locationType",item.locationType);
        jsonBuilder.put("cinemaFlag",item.cinemaFlag);
        return jsonBuilder.toString();
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
    public String getReportId() {
        return "Movie_Theater_Select";
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
