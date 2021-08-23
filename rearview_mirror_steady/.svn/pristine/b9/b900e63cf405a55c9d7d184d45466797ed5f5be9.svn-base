package com.txznet.record.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.viewfactory.data.MovieTimeListViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultMovieTimeListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.MovieTimeMsg;
import com.txznet.record.view.TitleView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChatMovieTimeAdapter extends ChatDisplayAdapter{
    public ChatMovieTimeAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        convertView = DefaultMovieTimeListView.getInstance().createItemView(position, ((MovieTimeItem) getItem(position)).mItem, (position % ConfigUtil.getVisbileCount()) != ConfigUtil.getVisbileCount() - 1);
        prepareSetLayoutParams(convertView);

        return convertView;
    }

    public static MovieTimeMsg generateMovieTheaterBean(JSONBuilder jsonBuilder, TitleView.Info info) {
        JSONObject[] jsonObjects = jsonBuilder.getVal("cines",JSONObject[].class);
        MovieTimeMsg movieTimeMsg = new MovieTimeMsg();
        try {
            List<MovieTimeItem> items = new ArrayList<MovieTimeItem>();
            for (int i = 0; i < jsonObjects.length; i++) {
                JSONObject jsonObject = jsonObjects[i];
                if(jsonObject == null){
                    continue;
                }
                MovieTimeListViewData.MovieTimeItem movieTimeBean = new MovieTimeListViewData.MovieTimeItem();
                movieTimeBean.closeTime = jsonObject.optString("closeTime");
                movieTimeBean.hallName = jsonObject.optString("hallName");
                movieTimeBean.showName = jsonObject.optString("showName");
                movieTimeBean.showTime = jsonObject.optString("showTime");
                movieTimeBean.showVersion = jsonObject.optString("showVersion");
                movieTimeBean.unitPrice = jsonObject.optInt("unitPrice");

                MovieTimeItem movieTimeItem = new MovieTimeItem();
                movieTimeItem.curPrg = 0;
                movieTimeItem.mItem = movieTimeBean;
                items.add(movieTimeItem);
            }
            info.hideDrawable = true;
            movieTimeMsg.mTitleInfo = info;
            movieTimeMsg.mItemList = items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return movieTimeMsg;
    }

    public static class MovieTimeItem extends DisplayItem<MovieTimeListViewData.MovieTimeItem> {

    }
}
