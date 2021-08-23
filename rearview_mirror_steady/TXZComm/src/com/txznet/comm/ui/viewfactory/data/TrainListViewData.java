package com.txznet.comm.ui.viewfactory.data;

import android.text.TextUtils;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TrainListViewData extends ListViewData {

    public TrainListViewData() {
        super(TYPE_FULL_LIST_TRAIN);
    }
    /**
     * origin : 乌鲁木齐
     * destination : 齐齐哈尔
     * departDate : 2018-08-15
     * departTime :
     * result : {"ticketList":[{"arrivalStation":"齐齐哈尔","arrivalTime":"18:20","daysApart":3,"departureStation":"乌鲁木齐","departureTime":"22:21","journeyTime":4079,"trainNo":"K1082","trainSeats":[{"isBookable":true,"price":439,"seatName":"硬座","seatType":"HARD_SEAT","ticketsRemainingNumer":99}]}]}
     */

    public String origin;
    public String destination;
    public String departDate;
    public String departTime;
    public List<TicketListBean> ticketList = new ArrayList<TicketListBean>();

    public static class TicketListBean {
        /**
         * arrivalStation : 齐齐哈尔
         * arrivalTime : 18:20
         * daysApart : 3
         * departureStation : 乌鲁木齐
         * departureTime : 22:21
         * journeyTime : 4079
         * trainNo : K1082
         * trainSeats : [{"isBookable":true,"price":439,"seatName":"硬座","seatType":"HARD_SEAT","ticketsRemainingNumer":99}]
         */

        public String arrivalStation;
        public String arrivalTime;
        public int daysApart;
        public String departureStation;
        public String departureTime;
        public int journeyTime;
        public String trainNo;
        public List<TrainSeatsBean> trainSeats = new ArrayList<TrainSeatsBean>();

        /** 耗时 2小时32分 */
        public String time;
        public double minPrice;
        public double maxPrice;

        public static class TrainSeatsBean {

            /**
             * isBookable : true
             * price : 439.5
             * seatName : 硬座
             * seatType : HARD_SEAT
             * ticketsRemainingNumer : 99
             */

            public boolean isBookable;
            public double price;
            public String seatName;
            public String seatType;
            public int ticketsRemainingNumer;

            private boolean checkValidity() {
                if (TextUtils.isEmpty(seatName)) {
                    return false;
                }
                return true;
            }

            public static TrainSeatsBean parseItem(JSONObject jsonObject) {
                if (jsonObject == null) {
                    return null;
                }
                JSONBuilder jsonBuilder = new JSONBuilder(jsonObject);
                TrainSeatsBean seat = new TrainSeatsBean();
                seat.seatName = jsonBuilder.getVal("seatName", String.class);
                seat.seatType = jsonBuilder.getVal("seatType", String.class);
                seat.price = jsonBuilder.getVal("price", Double.class, 0.0);
                seat.ticketsRemainingNumer = jsonBuilder.getVal("ticketsRemainingNumer", Integer.class, 0);
                seat.isBookable = jsonBuilder.getVal("isBookable", Boolean.class, seat.ticketsRemainingNumer > 0);
                if (seat.checkValidity()) {
                    return seat;
                } else {
                    return null;
                }
            }

        }

        private boolean checkValidity() {
            if (TextUtils.isEmpty(this.departureStation)) {
                return false;
            }
            if (TextUtils.isEmpty(this.arrivalStation)) {
                return false;
            }
            return true;
        }

        private void preprocess() {
            int hour = journeyTime / 60;
            int minute = journeyTime % 60;
            String show = "";
            if (hour > 0) {
                show = hour + "时";
            }
            if (minute > 0) {
                show += minute + "分";
            } else {
                if (hour > 0) {
                    show += "整";
                } else {
                    show = "0分";
                }
            }
            this.time = show;

            if (trainSeats.size() == 0) {
                return;
            }
            double minPrice = trainSeats.get(0).price;
            double maxPrice = trainSeats.get(0).price;
            for (int i = 1; i < trainSeats.size(); i++) {
                TrainSeatsBean trainSeat = trainSeats.get(i);
                if (trainSeat.price > maxPrice) {
                    maxPrice = trainSeat.price;
                }
                if (trainSeat.price < minPrice) {
                    minPrice = trainSeat.price;
                }
            }
            this.minPrice = minPrice;
            this.maxPrice = maxPrice;
        }

        public static TicketListBean parseItem(JSONObject jsonObject) {
            if (jsonObject == null) {
                return null;
            }
            JSONBuilder jsonBuilder = new JSONBuilder(jsonObject);
            TicketListBean ticket = new TicketListBean();
            ticket.arrivalStation = jsonBuilder.getVal("arrivalStation", String.class);
            ticket.arrivalTime = jsonBuilder.getVal("arrivalTime", String.class);
            ticket.departureStation = jsonBuilder.getVal("departureStation", String.class);
            ticket.departureTime = jsonBuilder.getVal("departureTime", String.class);
            ticket.trainNo = jsonBuilder.getVal("trainNo", String.class);
            ticket.daysApart = jsonBuilder.getVal("daysApart", Integer.class, 0);
            ticket.journeyTime = jsonBuilder.getVal("journeyTime", Integer.class, 0);
            JSONArray jsonArray = jsonBuilder.getVal("trainSeats", JSONArray.class);
            if (jsonArray != null) {
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject object = jsonArray.optJSONObject(i);
                    TrainSeatsBean trainSeatsBean = TrainSeatsBean.parseItem(object);
                    if (trainSeatsBean != null) {
                        ticket.trainSeats.add(trainSeatsBean);
                    }
                }
            }
            if (ticket.checkValidity()) {
                ticket.preprocess();
                return ticket;
            } else {
                return null;
            }
        }
    }

    @Override
    public void parseData(String data) {
        super.parseData(data);
    }

    @Override
    public void parseItemData(JSONBuilder jsonBuilder) {
        origin = jsonBuilder.getVal("origin", String.class);
        destination = jsonBuilder.getVal("destination", String.class);
        departDate = jsonBuilder.getVal("departDate", String.class);
        departTime = jsonBuilder.getVal("departTime", String.class);
        ticketList.clear();
        JSONObject result = jsonBuilder.getVal("result", JSONObject.class);
        if (result == null) {
            return;
        }
        JSONArray jsonArray = result.optJSONArray("ticketList");
        if (jsonArray == null) {
            return;
        }
        int size = count > jsonArray.length() ? jsonArray.length() : count;
        for (int i = 0; i < size; i++) {
            JSONObject jsonObject = jsonArray.optJSONObject(i);
            TicketListBean ticketListBean = TicketListBean.parseItem(jsonObject);
            if (ticketListBean != null) {
                ticketList.add(ticketListBean);
            }
        }
    }
}
