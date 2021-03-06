package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by daviddai on 2019/9/9
 */
public class QiwuTrainTicketPayViewData extends ListViewData {


    public ArrayList<TicketPayBean> mTicketBeans = new ArrayList<TicketPayBean>();
    public String title;

    @Override
    public void parseItemData(JSONBuilder data) {
        mTicketBeans.clear();
        JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
        for(int i = 0; i < count; i++){
            try {
                JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
                String type = cBuilder.getVal("ticketType", String.class);

                if(type.contains("flight")){
                    FlightTicketPayBean fp = new FlightTicketPayBean();
                    fp.payType = type;
                    fp.passengerName = cBuilder.getVal("passengerName", String.class);
                    fp.ticketNo = cBuilder.getVal("flightNo", String.class);
                    fp.station = cBuilder.getVal("station", String.class);
                    fp.endStation = cBuilder.getVal("endStation", String.class);
                    fp.departureDate = cBuilder.getVal("departureDate", String.class);
                    fp.price = cBuilder.getVal("price", String.class);
                    fp.fuelSurcharge = cBuilder.getVal("fuelSurcharge", String.class);
                    fp.payUrlZFB = cBuilder.getVal("payUrlZFB", String.class);
                    fp.parUrlWX = cBuilder.getVal("parUrlWX", String.class);
                    fp.expirationTime = cBuilder.getVal("expirationTime", String.class);
                    fp.departureTime = cBuilder.getVal("departureTime", String.class);
                    fp.orderId = cBuilder.getVal("orderId", String.class);
                    fp.sonAccount = cBuilder.getVal("sonAccount", String.class);
                    fp.phoneNum = cBuilder.getVal("phoneNum", String.class);
                    fp.idNumber = cBuilder.getVal("idNumber", String.class);
                    fp.orderUniqueId = cBuilder.getVal("orderUniqueId", String.class);
                    fp.canRefund = cBuilder.getVal("canRefund", boolean.class);
                    mTicketBeans.add(fp);
                }else if(type.contains("train")) {
                    TrainTicketPayBean tb = new TrainTicketPayBean();
                    tb.payType = type;
                    tb.departureTime = cBuilder.getVal("departureTime", String.class);
                    tb.passengerName = cBuilder.getVal("passengerName",String.class);
                    tb.ticketNo = cBuilder.getVal("ticketNo",String.class);
                    tb.station = cBuilder.getVal("station",String.class);
                    tb.endStation = cBuilder.getVal("endStation",String.class);
                    tb.price = cBuilder.getVal("price",String.class);
                    tb.seat = cBuilder.getVal("seat",String.class);
                    tb.departureDate = cBuilder.getVal("departureDate",String.class);
                    tb.payUrlZFB = cBuilder.getVal("payUrlZFB",String.class);
                    tb.parUrlWX = cBuilder.getVal("parUrlWX",String.class);
                    tb.expirationTime = cBuilder.getVal("expirationTime", String.class);
                    tb.costTime = cBuilder.getVal("costTime", String.class);
                    tb.orderId = cBuilder.getVal("orderId", String.class);
                    tb.sonAccount = cBuilder.getVal("sonAccount", String.class);
                    tb.passengeId = cBuilder.getVal("passengeId", String.class);
                    tb.phoneNum = cBuilder.getVal("phoneNum", String.class);
                    tb.idNumber = cBuilder.getVal("idNumber", String.class);
                    tb.orderUniqueId = cBuilder.getVal("orderUniqueId", String.class);
                    tb.canRefund = cBuilder.getVal("canRefund", boolean.class);
                    mTicketBeans.add(tb);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static class TicketPayBean {
        public String payType;
        public String orderUniqueId;//??????????????????
        public String payUrlZFB;
        public String parUrlWX;
        public String expirationTime;//?????????????????????????????????
        public String passengerName; // ????????????
        public String ticketNo;  // ??????
        public String station;  // ?????????????????????
        public String endStation;   // ?????????????????????
        public String departureDate; // ??????????????????10-24
        public String price;
        public String orderId;  //?????????
        public String sonAccount;   //?????????
        public String phoneNum;    //?????????
        public String idNumber;     //????????????
        public boolean canRefund = true; //??????????????????
        public String orderTime; //????????????
    }

    public static class FlightTicketPayBean extends TicketPayBean {

        public String departureTime; //????????????
        public String fuelSurcharge;

    }

    public static class TrainTicketPayBean extends TicketPayBean {
        public String departureTime; //????????????
        public String costTime; //?????????????????????
        public String seat;
        public String passengeId;

    }




    public QiwuTrainTicketPayViewData() {
        super(TYPE_FULL_TICKET_PAY);
    }
}
