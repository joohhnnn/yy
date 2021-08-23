package com.txznet.record.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.comm.ui.theme.ThemeConfigManager;
import com.txznet.comm.ui.util.ConfigUtil;
import com.txznet.comm.ui.view.RippleView;
import com.txznet.comm.ui.viewfactory.data.QiWuTrainTicketData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultTrainTicketListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.TrainTicketMsg;
import com.txznet.record.view.TitleView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daviddai on 2019/9/20
 */
public class TrainTicketListAdapter extends ChatDisplayAdapter {

    public TrainTicketListAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DefaultTrainTicketListView trainTicketListView = DefaultTrainTicketListView.getInstance();
        trainTicketListView.initAttr();
        RippleView itemView =(RippleView)trainTicketListView
                .createItemView(position, ((TrainTicketItem) (getItem(position))).mItem,
                        false);
        boolean showDivider = (position % ConfigUtil
                .getVisbileCount()) != ConfigUtil.getVisbileCount() - 1;
        View divider = new View(GlobalContext.get());
        divider.setVisibility(View.GONE);
        divider.setBackgroundColor(Color.parseColor("#4c4c4c"));
        int dividerHeight = (int) Math.ceil(ThemeConfigManager.getY(ThemeConfigManager.LIST_ITEM_DIVIDER_HEIGHT));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,dividerHeight);
        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        itemView.addView(divider, layoutParams);
        divider.setVisibility(position != ConfigUtil.getVisbileCount() -1 ? View.VISIBLE : View.INVISIBLE);
        convertView = itemView;
        prepareSetLayoutParams(convertView);
        return convertView;
    }


    public static TrainTicketMsg generateTrainTicketMsg(JSONBuilder jsonBuilder,
                                                        TitleView.Info info) {
        TrainTicketMsg result = new TrainTicketMsg();

        try {
            JSONObject[] jsonObjects = jsonBuilder.getVal("cines", JSONObject[].class);
            List<TrainTicketItem> items = new ArrayList<TrainTicketItem>();
            for (int i = 0; i < jsonObjects.length; i++) {
                JSONObject jsonObject = jsonObjects[i];
                if (jsonObject == null) {
                    continue;
                }
                QiWuTrainTicketData.TrainTicketBean bean =
                        new QiWuTrainTicketData.TrainTicketBean();
                bean.arrivalTime = jsonObject.getString("arrivalTime");
                bean.departureTime = jsonObject.getString("departureTime");
                bean.costTime = jsonObject.getString("costTime");
                bean.endStation = jsonObject.getString("endStation");
                bean.recommendSeat = jsonObject.getString("ticketType");
                bean.station = jsonObject.getString("station");
                bean.trainNo = jsonObject.getString("ticketNo");
                bean.allSeatJSONArray = jsonObject.getJSONArray("allSeatJSONArray");
                bean.recommendPrice = jsonObject.getString("ticketPrice");
                bean.departDate = jsonObject.getString("departDate");
                bean.addDate = jsonObject.getString("addDate");

                TrainTicketItem item = new TrainTicketItem();
                item.curPrg = 0;
                item.mItem = bean;
                items.add(item);
            }
            info.hideDrawable = true;
            result.mTitleInfo = info;
            result.mItemList = items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static class TrainTicketItem extends
            DisplayItem<QiWuTrainTicketData.TrainTicketBean> {

    }

}
