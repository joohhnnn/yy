package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.comm.util.DateUtils;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.sdk.bean.TicketBean;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class QiWuTrainTicketData extends ListViewData {

    public ArrayList<TrainTicketBean> mTrainTicketBeans = new ArrayList<TrainTicketBean>();
    public String departureCity;
    public String arrivalCity;
    public String date;


    public static class TrainTicketBean extends TicketBean {
        public String trainNo;
        public String arrivalTime;
        public String station;
        public String endStation;
        public String costTime;//车程时间，分钟
        public JSONArray allSeatJSONArray;//本车次的所有座位信息。
        public String departDate;
        public String addDate;//跨天数
        public String queryKey;//车次key

    }

    public QiWuTrainTicketData() {
        super(TYPE_FULL_LIST_TRAIN_TICKET);
    }

    @Override
    public void parseItemData(JSONBuilder data) {
        mTrainTicketBeans.clear();
        JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
        departureCity = data.getVal("departureCity",String.class);
        arrivalCity = data.getVal("arrivalCity",String.class);
        date = data.getVal("date",String.class);
        for (int i = 0; i < count; i++) {
            try {
                JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
                TrainTicketBean tb = new TrainTicketBean();
                tb.trainNo = cBuilder.getVal("ticketNo", String.class);
                tb.departureTime = cBuilder.getVal("departureTime",String.class);
                tb.arrivalTime = cBuilder.getVal("arrivalTime", String.class);
                tb.station = cBuilder.getVal("station", String.class);
                tb.endStation = cBuilder.getVal("endStation", String.class);
                tb.departDate = cBuilder.getVal("departDate", String.class);
                tb.costTime = cBuilder.getVal("costTime", String.class);
                tb.allSeatJSONArray = cBuilder.getVal("allSeatJSONArray", JSONArray.class);
                tb.recommendPrice = cBuilder.getVal("ticketPrice", String.class);
                tb.recommendSeat = cBuilder.getVal("ticketType", String.class);
                tb.addDate =  cBuilder.getVal("addDate", String.class);
                tb.queryKey = cBuilder.getVal("queryKey", String.class);
                mTrainTicketBeans.add(tb);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }



    public String getShowDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String show = "";
        try {
            Date date = simpleDateFormat.parse(this.date);
            show += (date.getMonth() + 1) + "月";
            show += date.getDate()+"日";
        } catch (Exception e) {
            show = this.date;
        }
        return show;
    }

    public static QiWuTrainTicketData objectFromData(JSONObject jsonObject) {
        if(jsonObject == null){
            return null;
        }
        return parseItem(jsonObject);
    }

    private static QiWuTrainTicketData parseItem(JSONObject jsonObject) {
        QiWuTrainTicketData trainTicketData = new QiWuTrainTicketData();
        try {
            jsonObject = jsonObject.getJSONObject("data");
            trainTicketData.mTrainTicketBeans.clear();
            trainTicketData.arrivalCity = jsonObject.getString("end_city");
            trainTicketData.departureCity = jsonObject.getString("start_city");

            JSONArray jsonArray = jsonObject.getJSONArray("trains");
            trainTicketData.date = jsonArray.getJSONObject(0).getString("departuretime").split("T")[0];
            for(int i = 0; i < jsonArray.length(); i++){
                JSONObject ticket = jsonArray.getJSONObject(i);
                int bookState = ticket.getInt("bookState");
                if(bookState != 1 ){
                    continue;
                }
                QiWuTrainTicketData.TrainTicketBean tb = new QiWuTrainTicketData.TrainTicketBean();
                tb.queryKey = ticket.getString("queryKey");
                tb.arrivalTime = ticket.getString("arrivaltime").split("T")[1];
                String arrivalTime = ticket.getString("arrivaltime").split("T")[0];
                Date arrivalTimeDate = new Date();
                arrivalTimeDate.setYear(Integer.valueOf(arrivalTime.substring(0, 4)));
                arrivalTimeDate.setMonth(Integer.valueOf(arrivalTime.substring(5, 7)) - 1);
                arrivalTimeDate.setDate(Integer.valueOf(arrivalTime.substring(8, 10)));

                tb.departureTime = ticket.getString("departuretime").split("T")[1];
                String departure = ticket.getString("departuretime").split("T")[0];
                Date departureDate = new Date();
                departureDate.setYear(Integer.valueOf(departure.substring(0, 4)));
                departureDate.setMonth(Integer.valueOf(departure.substring(5, 7)) - 1);
                departureDate.setDate(Integer.valueOf(departure.substring(8, 10)));
                tb.addDate = String.valueOf(DateUtils.getGapCount(departureDate, arrivalTimeDate));
                tb.departDate =  ticket.getString("departuretime").split("T")[0];
                tb.costTime = ticket.getString("costtime");
                tb.station = ticket.getString("station");
                tb.endStation = ticket.getString("endstation");
                tb.trainNo = ticket.getString("trainno");
                tb.allSeatJSONArray = ticket.getJSONArray("allSeats");
                JSONObject recommend = ticket.getJSONObject("trainTicket");
                tb.recommendPrice = recommend.getString("ticketPrice");
                tb.recommendSeat = recommend.getString("ticketType");
                trainTicketData.mTrainTicketBeans.add(tb);
            }
        } catch (Exception e) {
            LogUtil.logd("QiWuTrainTicketData is Exception");
            return null;
        }
        return trainTicketData;
    }

}
