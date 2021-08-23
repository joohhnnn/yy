package com.txznet.sdk.bean;

import java.util.LinkedList;

public class FlightBean {

    public String arrivalCity;                  //广州
    public String departureCity;                 //上海
    public String departureDate;                 //2020-03-13

    public LinkedList<PlaneTicket> planeTickets;

   public static class PlaneTicket{

        public String airline;                   //海航
        public String arrivalTime;               //2020-03-13 11:35
        public String arrivalTimeHm;             //11:35
        public long arrivalUnixTimestamp;        //1584070500
        public String departTime;                //2020-03-13 08:55
        public String departTimeHm;              //"08:55"
        public long departUnixTimestamp;         //1584070500
        public int economyCabinPrice;         //150
        public String flightNo;                  //HU7132
        public int ticketCount;                  //10
        public String arrivalAirportName;        //白云
        public String departAirportName;         //浦东
        public String economyCabinDiscount;      //1.0

    }

}
