package com.txznet.record.bean;

public class FlightInfo {
	public String airline;//航空公司
	public String flightNo;//航班编号
	public String departAirportName;//出发机场
	public String departTimeHm;//出发时间Hm格式
	public String departTime;//出发时间（2018-0814-14 22:10）格式
	public long departTimestamp;//出发时间
	public String arrivalAirportName;//到达机场
	public String arrivalTimeHm;//到达时间Hm格式
	public String arrivalTime;//到达时间（2018-0814-14 22:10）格式
	public long arrivalTimestamp;//到达时间
	public int economyCabinPrice;//经济舱价格
	public String economyCabinDiscount;//经济舱价格折扣
	public int ticketCount;//机票数量
	public String addDate;//跨越天数
}
