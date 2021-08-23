package com.txznet.txz.module.ticket;

import android.text.TextUtils;

import com.txznet.comm.util.JSONBuilder;
import com.txznet.txz.jni.JNIHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TrainTicketData {
	
	private static final String TAG = "TrainTicketData";

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
    public ResultBean result;

    public static TrainTicketData objectFromData(String str) {
    	if (TextUtils.isEmpty(str)) {
			return null;
		}
    	try {
            JSONObject jsonObject = new JSONObject(str);
            return parseItem(jsonObject);
        } catch (JSONException e) {
            e.printStackTrace();
            JNIHelper.logw(TAG + "parse data error: " + e.toString());
        }
        return null;
    }
    
    private static TrainTicketData parseItem(JSONObject jsonObject) {
    	if (jsonObject == null) {
            return null;
        }
        JSONBuilder jsonBuilder = new JSONBuilder(jsonObject);
        TrainTicketData trainTicketData = new TrainTicketData();
        trainTicketData.origin = jsonBuilder.getVal("origin", String.class);
        trainTicketData.destination = jsonBuilder.getVal("destination", String.class);
        trainTicketData.departDate = jsonBuilder.getVal("departDate", String.class);
        trainTicketData.departTime = jsonBuilder.getVal("departTime", String.class);
        trainTicketData.result = ResultBean.parseItem(jsonBuilder.getVal("result", JSONObject.class));
    	return trainTicketData;
    }

    public String getShowDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String show = "";
        try {
            Date date = simpleDateFormat.parse(this.departDate);
            show += (date.getMonth() + 1) + "月";
            show += date.getDate() + "日";
        } catch (Exception e) {
            show = this.departDate;
        }
        return show;
    }

    public void preprocess() {
        if (result == null || result.ticketList == null) {
            return;
        }

        for (int i = 0; i < result.ticketList.size(); i++) {
            ResultBean.TicketListBean ticket = result.ticketList.get(i);
            if (ticket == null || ticket.trainSeats == null || TextUtils.isEmpty(ticket.arrivalStation) ||
                    TextUtils.isEmpty(ticket.departureStation) || TextUtils.isEmpty(ticket.trainNo)) {
                result.ticketList.remove(i);
                i--;
                continue;
            }

            // 需要处理掉卧铺上下票合并为同一类型票
            ResultBean.TicketListBean.TrainSeatsBean softBerth = null;
            ResultBean.TicketListBean.TrainSeatsBean hardBerth = null;
            for (int j = 0; j < ticket.trainSeats.size(); j++) {
                ResultBean.TicketListBean.TrainSeatsBean seat = ticket.trainSeats.get(j);
                if (TextUtils.isEmpty(seat.seatName)) {
                    ticket.trainSeats.remove(j);
                    j--;
                    continue;
                }
                if (seat.seatName.startsWith("硬卧")) {
                    if (hardBerth == null) {
                        hardBerth = seat;
                        hardBerth.seatName = "硬卧";
                        hardBerth.seatType = "HARD_BERTH";
                    } else {
                        hardBerth.isBookable |= seat.isBookable;
                        // 取价格较小的那个
                        if (hardBerth.price > seat.price) {
                            hardBerth.price = seat.price;
                        }
                        hardBerth.ticketsRemainingNumer += seat.ticketsRemainingNumer;
                    }
                    ticket.trainSeats.remove(j);
                    j--;
                    continue;
                }
                if (seat.seatName.startsWith("软卧")) {
                    if (softBerth == null) {
                        softBerth = seat;
                        softBerth.seatName = "软卧";
                        softBerth.seatType = "SOFT_BERTH";
                    } else {
                        softBerth.isBookable |= seat.isBookable;
                        // 取价格较小的那个
                        if (softBerth.price > seat.price) {
                            softBerth.price = seat.price;
                        }
                        softBerth.ticketsRemainingNumer += seat.ticketsRemainingNumer;
                    }
                    ticket.trainSeats.remove(j);
                    j--;
                    continue;
                }
            }
            if (softBerth != null) {
                ticket.trainSeats.add(softBerth);
            }
            if (hardBerth != null) {
                ticket.trainSeats.add(hardBerth);
            }
        }
    }

    public static class ResultBean {
        public List<TicketListBean> ticketList;

        public static ResultBean parseItem(JSONObject jsonObject) {
        	if (jsonObject == null) {
                return null;
            }
            ResultBean resultBean = new ResultBean();
            JSONArray jsonArray = jsonObject.optJSONArray("ticketList");
            if (jsonArray == null) {
                return resultBean;
            }
            int size = jsonArray.length();
            resultBean.ticketList = new ArrayList<TrainTicketData.ResultBean.TicketListBean>(size);
        	for (int i = 0; i < size; i++) {
                TicketListBean ticketListBean = TicketListBean.parseItem(jsonArray.optJSONObject(i));
                if (ticketListBean != null) {
                	resultBean.ticketList.add(ticketListBean);
                }
			}
            return resultBean;
        }

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
            public List<TrainSeatsBean> trainSeats;
            
            private boolean checkValidity() {
                if (TextUtils.isEmpty(this.departureStation)) {
                    return false;
                }
                if (TextUtils.isEmpty(this.arrivalStation)) {
                    return false;
                }
                return true;
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
                	ticket.trainSeats = new ArrayList<TrainTicketData.ResultBean.TicketListBean.TrainSeatsBean>();
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject object = jsonArray.optJSONObject(i);
                        TrainSeatsBean trainSeatsBean = TrainSeatsBean.parseItem(object);
                        if (trainSeatsBean != null) {
                            ticket.trainSeats.add(trainSeatsBean);
                        }
                    }
                }
                if (ticket.checkValidity()) {
                    return ticket;
                } else {
                    return null;
                }
            }

            public static class TrainSeatsBean {
                /**
                 * isBookable : true
                 * price : 439
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
                    seat.seatName = jsonBuilder.getVal("seatName", String.class, "");
                    seat.seatType = jsonBuilder.getVal("seatType", String.class, "");
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
        }
    }
}
