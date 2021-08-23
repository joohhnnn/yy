package com.txznet.record.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.viewfactory.data.MovieTheaterListViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieTheaterListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.MovieTheaterMsg;
import com.txznet.record.view.TitleView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatMovieTheaterAdapter  extends ChatDisplayAdapter{
    public ChatMovieTheaterAdapter(Context context, List displayList) {
        super(context, displayList);
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = DefaultMovieTheaterListView.getInstance().createItemView(position, ((MovieTheaterItem)(getItem(position))).mItem, (position % ConfigUtil.getVisbileCount()) != ConfigUtil.getVisbileCount() - 1);
        prepareSetLayoutParams(convertView);

        return convertView;
    }


    public static MovieTheaterMsg generateMovieTheaterBean(JSONBuilder jsonBuilder, TitleView.Info info) {
        JSONObject[] jsonObjects = jsonBuilder.getVal("cines",JSONObject[].class);
        MovieTheaterMsg movieTheaterMsg = new MovieTheaterMsg();
        try {
            List<MovieTheaterItem> items = new ArrayList<MovieTheaterItem>();
            for (int i = 0; i < jsonObjects.length; i++) {
                JSONObject jsonObject = jsonObjects[i];
                if(jsonObject == null){
                    continue;
                }
                MovieTheaterListViewData.MovieTheaterBean movieTheaterBean = new MovieTheaterListViewData.MovieTheaterBean();
                movieTheaterBean.address = jsonObject.optString("address");
                JSONArray aliasArrays = jsonObject.getJSONArray("alias");
                List alias = new ArrayList<String>();
                if(aliasArrays != null) {
                    for (int j = 0; j < aliasArrays.length(); j++) {
                        alias.add(aliasArrays.getString(j));
                    }
                }
                movieTheaterBean.alias = alias;
                movieTheaterBean.cinemaName = jsonObject.optString("cinemaName");
                movieTheaterBean.distance = jsonObject.optString("distance");
                movieTheaterBean.locationType = jsonObject.optString("locationType");
                movieTheaterBean.cinemaFlag = jsonObject.optString("cinemaFlag");

                MovieTheaterItem movieTheaterItem = new MovieTheaterItem();
                movieTheaterItem.curPrg = 0;
                movieTheaterItem.mItem = movieTheaterBean;
                items.add(movieTheaterItem);
            }
            info.hideDrawable = true;
            movieTheaterMsg.mTitleInfo = info;
            movieTheaterMsg.mItemList = items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieTheaterMsg;
    }

    public static class MovieTheaterItem extends DisplayItem<MovieTheaterListViewData.MovieTheaterBean> {

    }
}
