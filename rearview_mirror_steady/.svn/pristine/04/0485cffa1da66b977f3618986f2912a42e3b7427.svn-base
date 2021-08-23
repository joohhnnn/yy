package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class FlightListViewData extends ListViewData {

	public FlightListViewData() {
		super(TYPE_FULL_LIST_FLIGHT);
	}

	public static class FlightItemBean {
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
		public String addDate;//跨天数
	}

	private ArrayList<FlightItemBean> flightItemBeans = new ArrayList<FlightItemBean>();
	public String mDepartCity, mArrivalCity, mDate;
	public ArrayList<FlightItemBean> getData() {
		return flightItemBeans;
	}
	
	@Override
	public void parseData(String data) {
		super.parseData(data);
		JSONBuilder jsonBuilder = new JSONBuilder(data);
		mDepartCity = jsonBuilder.getVal("departCity", String.class, "未知");
		mArrivalCity = jsonBuilder.getVal("arrivalCity", String.class, "未知");
		mDate = jsonBuilder.getVal("date", String.class, "未知日期");
	}
	
	@Override
	public void parseItemData(JSONBuilder data) {
		flightItemBeans.clear();
		JSONArray obJsonArray = data.getVal("flights", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					FlightItemBean itemBean = new FlightItemBean();
					itemBean.airline = objJson.getVal("airline", String.class, "");
					itemBean.flightNo = objJson.getVal("flightNo", String.class, "");
					itemBean.departAirportName = objJson.getVal("departAirportName", String.class, "");
					itemBean.departTimeHm = objJson.getVal("departTimeHm", String.class, "");
					itemBean.departTime = objJson.getVal("departTime", String.class, "");
					itemBean.departTimestamp = objJson.getVal("departTimestamp", Long.class, 0l);
					itemBean.arrivalAirportName = objJson.getVal("arrivalAirportName", String.class, "");
					itemBean.arrivalTimeHm = objJson.getVal("arrivalTimeHm", String.class, "");
					itemBean.arrivalTime = objJson.getVal("arrivalTime", String.class, "");
					itemBean.arrivalTimestamp = objJson.getVal("arrivalTimestamp", Long.class, 0l);
					itemBean.economyCabinPrice = objJson.getVal("economyCabinPrice", Integer.class, 0);
					itemBean.economyCabinDiscount = objJson.getVal("economyCabinDiscount", String.class, "");
					itemBean.ticketCount = objJson.getVal("ticketCount", Integer.class, 0);
					itemBean.addDate = objJson.getVal("addDate", String.class, "");
					flightItemBeans.add(itemBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
