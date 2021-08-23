package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class MovieTimeListViewData extends ListViewData {

    private ArrayList<MovieTimeListViewData.MovieTimeItem> mMovieTimeItemBeans = new ArrayList<MovieTimeListViewData.MovieTimeItem>();

    public ArrayList<MovieTimeListViewData.MovieTimeItem> getData() {
        return mMovieTimeItemBeans;
    }

    public static class MovieTimeItem{
        public String showVersion;
        public String showTime;
        public String closeTime;
        public String hallName;
        public String locationType;
        public String showName;
        public int unitPrice;
    }

    public MovieTimeListViewData() {
        super(TYPE_FULL_MOVIE_TIME_LIST);
    }

    @Override
    public void parseItemData(JSONBuilder data) {
        mMovieTimeItemBeans.clear();
        JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
        MovieTimeItem mt;
        for (int i = 0; i < count; i++) {
            try {
                JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
                mt  = new MovieTimeItem();
                mt.closeTime = cBuilder.getVal("closeTime",String.class);
                mt.showTime = cBuilder.getVal("showTime",String.class);
                mt.hallName = cBuilder.getVal("hallName", String.class);
                mt.showVersion = cBuilder.getVal("showVersion", String.class);
                mt.unitPrice = cBuilder.getVal("unitPrice", Integer.class);
                mt.locationType = cBuilder.getVal("locationType", String.class);
                mt.showName = cBuilder.getVal("showName", String.class);
                mMovieTimeItemBeans.add(mt);
            } catch (JSONException e) {
                e.printStackTrace();
            }

        }

    }
}
