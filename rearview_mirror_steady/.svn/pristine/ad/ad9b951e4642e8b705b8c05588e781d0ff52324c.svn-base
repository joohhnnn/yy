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
import com.txznet.comm.ui.viewfactory.data.QiWuFlightTicketData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultFlightTicketListView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.FlightTicketMsg;
import com.txznet.record.view.TitleView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by daviddai on 2019/9/20
 */
public class FlightTicketListAdapter extends ChatDisplayAdapter {
    public FlightTicketListAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        DefaultFlightTicketListView flightTicketListView = DefaultFlightTicketListView
                .getInstance();
        flightTicketListView.initAttr();
        RippleView itemView = (RippleView)flightTicketListView
                .createItemView(position, ((FlightTicketItem) (getItem(position))).mItem,
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


    public static FlightTicketMsg generateFlightTicketMsg(JSONBuilder jsonBuilder,
                                                          TitleView.Info info) {
        FlightTicketMsg result = new FlightTicketMsg();

        try {
            JSONObject[] jsonObjects = jsonBuilder.getVal("cines", JSONObject[].class);
            List<FlightTicketItem> items = new ArrayList<FlightTicketItem>();
            for (int i = 0; i < jsonObjects.length; i++) {
                JSONObject jsonObject = jsonObjects[i];
                if (jsonObject == null) {
                    continue;
                }
                QiWuFlightTicketData.FlightTicketBean bean =
                        new QiWuFlightTicketData.FlightTicketBean();
                bean.addDate = jsonObject.getString("addDate");
                bean.airline = jsonObject.getString("airline");
                bean.arrivalAirportName = jsonObject.getString("arrivalAirportName");
                bean.arrivalAirportCode = jsonObject.getString("arrivalAirportCode");
                bean.arrivalTime = jsonObject.getString("arrivalTime");
                bean.departureTime = jsonObject.getString("departTime");
                bean.departAirportName = jsonObject.getString("departAirportName");
                bean.departAirportCode = jsonObject.getString("departAirportCode");
                bean.recommendPrice = jsonObject.getString("cabinPrice");
                bean.recommendSeat = jsonObject.getString("cabin");
                bean.seatCode = jsonObject.getString("seatCode");
                bean.departDate = jsonObject.getString("departDate");
                bean.flightNo = jsonObject.getString("number");
                bean.addDate = jsonObject.getString("addDate");

                FlightTicketItem item = new FlightTicketItem();
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

    public static class FlightTicketItem extends
            DisplayItem<QiWuFlightTicketData.FlightTicketBean> {

    }
}
