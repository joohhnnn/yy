package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;

public class MovieTheaterListViewData extends ListViewData {

    private ArrayList<MovieTheaterListViewData.MovieTheaterBean> mMovieTheaterBeans = new ArrayList<MovieTheaterListViewData.MovieTheaterBean>();

    public ArrayList<MovieTheaterListViewData.MovieTheaterBean> getData() {
        return mMovieTheaterBeans;
    }

    public static class MovieTheaterBean {
        public String cinemaName;
        public String address;
        public String distance;
        public String locationType;
        public String cinemaFlag;
        public List<String> alias = new ArrayList<String>();
    }

    public MovieTheaterListViewData() {
        super(TYPE_FULL_MOVIE_THEATER_LIST);
    }

    @Override
    public void parseItemData(JSONBuilder data) {
        mMovieTheaterBeans.clear();
        JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
        MovieTheaterBean movieTheaterBean;
        for (int i = 0; i < count; i++) {
            try {
                JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
                movieTheaterBean = new MovieTheaterBean();
                movieTheaterBean.cinemaName = cBuilder.getVal("cinemaName",String.class);
                movieTheaterBean.address = cBuilder.getVal("address",String.class);
                movieTheaterBean.distance = cBuilder.getVal("distance",String.class);
                movieTheaterBean.locationType = cBuilder.getVal("locationType",String.class);
                movieTheaterBean.cinemaFlag = cBuilder.getVal("cinemaFlag",String.class);
                JSONArray jsonAliasArray = cBuilder.getVal("alias",JSONArray.class);
                for(int j = 0; j < jsonAliasArray.length(); j++){
                    movieTheaterBean.alias.add(jsonAliasArray.getString(j));
                }
                mMovieTheaterBeans.add(movieTheaterBean);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
