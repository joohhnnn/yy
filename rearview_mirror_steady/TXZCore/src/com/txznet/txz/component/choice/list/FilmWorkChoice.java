package com.txznet.txz.component.choice.list;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.remote.util.AsrUtil;
import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.record.helper.ChatMsgFactory;
import com.txznet.record.keyevent.KeyEventManagerUI1;
import com.txznet.record.ui.WinRecord;
import com.txznet.txz.R;
import com.txznet.txz.component.choice.option.CompentOption;
import com.txznet.txz.component.choice.page.ResListPage;
import com.txznet.txz.component.choice.page.ResourcePage;
import com.txznet.txz.component.film.FilmScoreComparator;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.jni.data.NativeData;
import com.txznet.txz.module.asr.AsrManager;
import com.txznet.txz.module.film.FilmManager;
import com.txznet.txz.module.text.TextResultHandle;
import com.txznet.txz.module.tts.TtsManager;
import com.txznet.txz.module.ui.WinManager;
import com.txznet.txz.ui.widget.mov.FilmLayout;
import com.txznet.txz.ui.win.record.RecorderWin;
import com.txznet.txz.util.runnables.Runnable1;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FilmWorkChoice extends WorkChoice<List<FilmWorkChoice.FilmItem>, FilmWorkChoice.FilmItem> {

    // 当前关键字，需要在showChoice前调用
    public static String keywords;

    public FilmWorkChoice(CompentOption<FilmItem> option) {
        super(option);
    }

    public static class FilmItem {
        public String answer;
        public String title;
        public String postUrl;
        public double score;
        public List<String> types = new ArrayList<String>();
        public List<String> alias = new ArrayList<String>();
    }

    @Override
    public void showChoices(List<FilmWorkChoice.FilmItem> data) {
        if(data.size() == 1){
            getOption().setCanSure(true);
        }
        super.showChoices(data);
    }

    @Override
    protected boolean onCommandSelect(String type, String speech) {
        final String command = speech;

        if ("FILM_TICKET_CANCEL".equals(type)) {
            FilmManager.getInstance().cancel(command);
            return true;
        }
        if ("FILM_SCORE_SORT".equals(type)) {
            sortFilmScore(mData, speech);
        }
        if ("MAX_SCORE".equals(type)) {
            int max = mPage.getCurrPage() * getOption().getNumPageSize() + mPage.getCurrPageSize();
            int min = mPage.getCurrPage() * getOption().getNumPageSize();

            if (!(min <= maxScoreIndex && maxScoreIndex <= max)) {
                selectPage(maxScoreIndex / getOption().getNumPageSize() + 1, speech);
            }
            selectAllIndex(maxScoreIndex, speech);
        }

        return super.onCommandSelect(type, command);
    }

    private void addCommand(AsrUtil.AsrComplexSelectCallback acsc, String type, String... cmds) {
        acsc.addCommand(type, cmds);
    }

    @Override
    public void stopTtsAndAsr() {
        JNIHelper.logd("stopTtsAndAsr");
        TtsManager.getInstance().cancelSpeak(mSpeechTaskId);
        mSpeechTaskId = TtsManager.INVALID_TTS_TASK_ID;
        AsrManager.getInstance().cancel();
        if (isCoexistAsrAndWakeup()) {
            TextResultHandle.getInstance().cancel();
        }
    }

    private void sortFilmScore(List<FilmWorkChoice.FilmItem> data, String speech) {
        FilmScoreComparator filmScoreComparator = new FilmScoreComparator();
        Collections.sort(data, filmScoreComparator);
        maxScoreIndex = 0;
        refreshData(data);
        String sortSpk = NativeData.getResString("RS_VOICE_POI_SELECT_SORT_SPK");
        if (!TextUtils.isEmpty(sortSpk)) {
            sortSpk = sortSpk.replace("%SORTSLOT%", speech);
        }
        //speakTtsInChoice(sortSpk + "，" + NativeData.getResString("RS_POI_SELECT_AGAIN_HINT"));
        speakWithTips(sortSpk);
    }

    @Override
    protected void updateDisplay(List<FilmItem> items) {
        // 重写刷新界面
        if (WinManager.getInstance().hasThirdImpl() || WinManager.getInstance().isRecordWin2()) {
            super.updateDisplay(items);
            return;
        }

        if (mPage == null) {
            return;
        }
        String strData = convToJson(mPage.getResource()).toString();
        LogUtil.logd("send data:" + strData);
        AppLogic.runOnUiGround(new Runnable1<String>(strData) {

            @Override
            public void run() {
                WinRecord.getInstance().addMsg(
                        ChatMsgFactory.createContainMsg(mP1, createFilmLayout(convertFilmBeans(mPage.getResource()))));
            }
        });
    }

    private FilmLayout.FilmBean convertFilmBean(FilmItem item) {
        FilmLayout.FilmBean cb = new FilmLayout.FilmBean();
        cb.postUrl = item.postUrl;
        cb.score = item.score;
        cb.title = item.title;
        return cb;
    }


    private List<FilmLayout.FilmBean> convertFilmBeans(List<FilmItem> items) {
        List<FilmLayout.FilmBean> beans = new ArrayList<FilmLayout.FilmBean>();
        for (int i = 0; i < items.size(); i++) {
            FilmItem cb = items.get(i);
            beans.add(convertFilmBean(cb));
        }
        return beans;
    }

    private FilmLayout cLayout;

    private View createFilmLayout(List<FilmLayout.FilmBean> cbs) {
        if (cLayout == null) {
            cLayout = (FilmLayout) View.inflate(GlobalContext.get(), R.layout.view_film_layout, null);
            cLayout.setVisibleCount(getOption().getNumPageSize());
        }
        cLayout.setCineList(cbs);
        cLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {
                if (cLayout == null) {
                    return;
                }
                if (cLayout.getFocusViews() != null && cLayout.getFocusViews().size() > 0) {
                    KeyEventManagerUI1.getInstance().updateFocusViews(cLayout.getFocusViews(),
                            GlobalContext.get().getResources().getDrawable(R.drawable.white_range_layout));
                }
                cLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
        cLayout.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));
        return cLayout;
    }

    @Override
    protected void onConvToJson(List<FilmItem> ts, JSONBuilder jsonBuilder) {
        jsonBuilder.put("type", RecorderWin.FILM_SENCE_FILM);
        jsonBuilder.put("keywords", keywords);
        jsonBuilder.put("prefix", NativeData.getResString("RS_DISPLAY_CINEMA_TITLE"));

        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < ts.size(); i++) {
            FilmWorkChoice.FilmItem cb = ts.get(i);
            JSONObject obj = new JSONBuilder().put("name", cb.title).put("post", cb.postUrl).put("score", cb.score)
                    .getJSONObject();
            jsonArray.put(obj);
        }
        jsonBuilder.put("cines", jsonArray);
        jsonBuilder.put("count", jsonArray.length());
        jsonBuilder.put("vTips", getTips());
    }

    private String getTips() {
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

    private double maxScore = -1;
    private int maxScoreIndex = -1;


    @Override
    protected void onAddWakeupAsrCmd(AsrUtil.AsrComplexSelectCallback acsc, List<FilmWorkChoice.FilmItem> data) {
        super.onAddWakeupAsrCmd(acsc, data);
        List<FilmWorkChoice.FilmItem> mFilmItems = mData;

        acsc.addCommand("FILM_SCORE_SORT", "评分排序");
        acsc.addCommand("PRE_PAGER", "上面那页");
        acsc.addCommand("NEXT_PAGER", "下面那页");
        acsc.addCommand("MAX_SCORE", "评分最高的");
        acsc.addCommand("FILM_TICKET_CANCEL", NativeData.getResStringArray("RS_VOICE_TIPS_FILM_TICKET_CANCEL"));

        for (int i = 0; i < mFilmItems.size(); i++) {
            addKeyWord(acsc, i, mFilmItems.get(i).title);
            if (maxScore < mFilmItems.get(i).score) {
                maxScore = mFilmItems.get(i).score;
                maxScoreIndex = i;
            }
            for (int j = 0; j < mFilmItems.get(i).alias.size(); j++) {
                addKeyWord(acsc, i, mFilmItems.get(i).alias.get(j));
            }
        }

    }

    private void addKeyWord(AsrUtil.AsrComplexSelectCallback acsc, int index, String keywords) {
        if (!TextUtils.isEmpty(keywords) && keywords.length() > 1) {
            acsc.addIndex(index, keywords);
        }
    }

    @Override
    protected String convItemToString(FilmItem item) {
        JSONBuilder jsonBuilder = new JSONBuilder();
        jsonBuilder.put("postUrl", item.postUrl);
        jsonBuilder.put("score", item.score);
        jsonBuilder.put("title", item.title);
        return jsonBuilder.toString();
    }

    @Override
    protected void onSelectIndex(FilmItem item, boolean isFromPage,
                                 int idx, String fromVoice) {

    }


    @Override
    protected boolean onIndexSelect(final List<Integer> indexs, final String command) {
        if (indexs.size() != 1) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    List<FilmWorkChoice.FilmItem> data = new ArrayList<FilmWorkChoice.FilmItem>();
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
    protected ResourcePage<List<FilmItem>, FilmItem> createPage(List<FilmItem> sources) {
        return new ResListPage<FilmWorkChoice.FilmItem>(sources) {

            @Override
            protected int numOfPageSize() {
                return getOption().getNumPageSize();
            }
        };
    }

    @Override
    public String getReportId() {
        return "Film_Select";
    }

    @Override
    protected void onTimeout() {
        super.onTimeout();
        FilmManager.getInstance().cancel(null);
    }

    @Override
    protected void onClearSelecting() {
        super.onClearSelecting();
    }

}
