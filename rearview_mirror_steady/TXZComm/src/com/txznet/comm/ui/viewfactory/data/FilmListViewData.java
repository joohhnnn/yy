package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FilmListViewData extends ListViewData {

    private ArrayList<FilmListViewData.FilmBean> mFilmBeans = new ArrayList<FilmListViewData.FilmBean>();

    public ArrayList<FilmListViewData.FilmBean> getData() {
        return mFilmBeans;
    }

    public static class FilmBean {
        public String title;
        public String post;
        public double score;
    }

    public FilmListViewData() {
        super(TYPE_FULL_FILM_LIST);
    }

    @Override
    public void parseItemData(JSONBuilder data) {
        mFilmBeans.clear();
        JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
        FilmListViewData.FilmBean filmBean;
        for (int i = 0; i < count; i++) {
            try {
                JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
                filmBean = new FilmListViewData.FilmBean();
                filmBean.title = cBuilder.getVal("name", String.class);
                filmBean.post = cBuilder.getVal("post", String.class);
                filmBean.score = cBuilder.getVal("score", Double.class);
                mFilmBeans.add(filmBean);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
