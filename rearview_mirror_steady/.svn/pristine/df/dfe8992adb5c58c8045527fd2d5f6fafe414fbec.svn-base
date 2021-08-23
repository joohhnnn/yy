package com.txznet.record.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.txznet.comm.ui.util.ScreenUtil;
import com.txznet.comm.ui.viewfactory.data.QiwuTrainTicketPayViewData;
import com.txznet.comm.ui.viewfactory.view.defaults.DefaultQiWuTicketPayView;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.record.bean.DisplayItem;
import com.txznet.record.bean.QiWuTickectPayMsg;
import com.txznet.record.view.TitleView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QiWuTicketPayAdapter extends ChatDisplayAdapter {
    public QiWuTicketPayAdapter(Context context, List displayList) {
        super(context, displayList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view =new DefaultQiWuTicketPayView().create(((QiWuTicketPayItem)getItem(position)).mItem);
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        AbsListView.LayoutParams lParams;
        lastItemHeight = ScreenUtil.getDisplayLvItemH(false);
        if (lp == null) {
            lParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }else {
            lParams = (AbsListView.LayoutParams) lp;
            lParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            lParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        }
        parent.setBackground(null);
        view.setLayoutParams(lParams);
        return view;
    }

    public static QiWuTickectPayMsg generateQiWuTickectPay(JSONBuilder jsonBuilder, TitleView.Info info) {
        JSONObject[] jsonObjects = jsonBuilder.getVal("cines",JSONObject[].class);
        QiWuTickectPayMsg qiWuTickectPayMsg = new QiWuTickectPayMsg();
        List<QiWuTicketPayItem> items = new ArrayList<QiWuTicketPayItem>();
        try {
            for (int i = 0; i < jsonObjects.length; i++) {
                JSONObject jsonObject = jsonObjects[i];
                if(jsonObject == null){
                    continue;
                }
                String type = jsonObject.getString("ticketType");
                QiwuTrainTicketPayViewData.TicketPayBean bean = null;
                if(type.contains("train")){
                    QiwuTrainTicketPayViewData.TrainTicketPayBean fb = new QiwuTrainTicketPayViewData.TrainTicketPayBean();
                    fb.passengerName = jsonObject.getString("passengerName");
                    fb.ticketNo = jsonObject.getString("ticketNo");
                    fb.costTime = jsonObject.getString("costTime");
                    fb.sonAccount = jsonObject.getString("sonAccount");
                    fb.station = jsonObject.getString("station");
                    fb.payType = jsonObject.getString("ticketType");
                    fb.endStation = jsonObject.getString("endStation");
                    fb.departureDate = jsonObject.getString("departureDate");
                    fb.orderUniqueId = jsonObject.getString("orderUniqueId");
                    fb.expirationTime = jsonObject.getString("expirationTime");
                    fb.orderId = jsonObject.getString("orderId");
                    fb.price = jsonObject.getString("price");
                    fb.phoneNum = jsonObject.getString("phoneNum");
                    fb.idNumber = jsonObject.getString("idNumber");
                    fb.seat = jsonObject.getString("seat");
                    fb.passengeId = jsonObject.getString("passengeId");
                    fb.payUrlZFB = jsonObject.getString("payUrlZFB");
                    fb.parUrlWX = jsonObject.getString("parUrlWX");
                    fb.departureTime = jsonObject.getString("departureTime");
                    bean = fb;
                }else if(type.contains("flight")){
                    QiwuTrainTicketPayViewData.FlightTicketPayBean fb = new QiwuTrainTicketPayViewData.FlightTicketPayBean();
                    fb.passengerName = jsonObject.getString("passengerName");
                    fb.ticketNo = jsonObject.getString("flightNo");
                    fb.departureTime = jsonObject.getString("departureTime");
                    fb.sonAccount = jsonObject.getString("sonAccount");
                    fb.station = jsonObject.getString("station");
                    fb.payType = jsonObject.getString("ticketType");
                    fb.endStation = jsonObject.getString("endStation");
                    fb.departureDate = jsonObject.getString("departureDate");
                    fb.orderUniqueId = jsonObject.getString("orderUniqueId");
                    fb.expirationTime = jsonObject.getString("expirationTime");
                    fb.orderId = jsonObject.getString("orderId");
                    fb.price = jsonObject.getString("price");
                    fb.phoneNum = jsonObject.getString("phoneNum");
                    fb.idNumber = jsonObject.getString("idNumber");
                    fb.fuelSurcharge = jsonObject.getString("fuelSurcharge");
                    fb.payUrlZFB = jsonObject.getString("payUrlZFB");
                    fb.parUrlWX = jsonObject.getString("parUrlWX");
                    bean = fb;
                }
                QiWuTicketPayItem qiWuTicketPayItem = new QiWuTicketPayItem();
                qiWuTicketPayItem.curPrg = 0;
                qiWuTicketPayItem.mItem = bean;
                items.add(qiWuTicketPayItem);
            }
            info.hideDrawable = true;
            qiWuTickectPayMsg.mTitleInfo = info;
            qiWuTickectPayMsg.mItemList = items;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return qiWuTickectPayMsg;
    }

    public static class QiWuTicketPayItem extends DisplayItem<QiwuTrainTicketPayViewData.TicketPayBean> {

    }

}
