package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class AudioListViewData extends ListViewData {

    public AudioListViewData() {
        super(TYPE_FULL_LIST_AUDIO);
    }
    public boolean isMusic;
    public static class AudioBean {
        /**
         * 无效状态
         */
        public static final int NOVEL_STATUS_INVALID = 0;
        /**
         * 正在连载状态
         */
        public static final int NOVEL_STATUS_SERILIZE = 1;
        /**
         * 完结状态
         */
        public static final int NOVEL_STATUS_END = 2;

        public int id;
        public int albumTrackCount;
        public String text;
        public String title;
        public String name;
        /**
         * 连载状态
         * {@link AudioBean#NOVEL_STATUS_INVALID}  无效状态<br>
         * {@link AudioBean#NOVEL_STATUS_SERILIZE} 正在连载状态<br>
         * {@link AudioBean#NOVEL_STATUS_END}  完结状态<br>
         */
        public int novelStatus;


        /**
         * 上次收听
         */
        public boolean lastPlay;
        /**
         * 付费
         */
        public boolean paid;
        /**
         * 最新
         */
        public boolean latest;
    }

    private ArrayList<AudioBean> audioBeans = new ArrayList<AudioListViewData.AudioBean>();

    public ArrayList<AudioBean> getData() {
        return audioBeans;
    }

    @Override
    public void parseData(String data) {
        super.parseData(data);
        isMusic = new JSONBuilder(data).getVal("isMusic", Boolean.class);
    }

    @Override
    public void parseItemData(JSONBuilder data) {
        audioBeans.clear();
        JSONArray obJsonArray = data.getVal("audios", JSONArray.class);
        if (obJsonArray != null) {
            for (int i = 0; i < count; i++) {
                try {
                    JSONBuilder objJson = new JSONBuilder(obJsonArray.getJSONObject(i));
                    AudioBean audioBean = new AudioBean();
                    audioBean.id = objJson.getVal("id", Integer.class, 0);
                    audioBean.albumTrackCount = objJson.getVal("albumTrackCount", Integer.class, 0);
                    audioBean.text = objJson.getVal("text", String.class);
                    audioBean.title = objJson.getVal("title", String.class);
                    audioBean.name = objJson.getVal("name", String.class);
                    audioBean.novelStatus = objJson.getVal("novelStatus", Integer.class, AudioBean.NOVEL_STATUS_INVALID);

                    audioBean.lastPlay = objJson.getVal("listened", Boolean.class, false);
                    audioBean.paid = objJson.getVal("paid", Boolean.class, false);
                    audioBean.latest = objJson.getVal("latest", Boolean.class, false);

                    audioBeans.add(audioBean);
                } catch (JSONException e) {
                }
            }
        }
    }
}
