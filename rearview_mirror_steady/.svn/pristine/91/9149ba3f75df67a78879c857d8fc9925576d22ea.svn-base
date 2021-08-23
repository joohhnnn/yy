package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class SimListViewData extends ListViewData {

	public SimListViewData() {
		super(TYPE_FULL_LIST_SIM);
	}
	
	public static class SimBean {
		public String title;
		public int price;
		public int rawPrice;
	}
	
	private ArrayList<SimBean> simBeans = new ArrayList<SimListViewData.SimBean>();

	public ArrayList<SimBean> getData() {
		return simBeans;
	}
	
	@Override
	public void parseItemData(JSONBuilder data) {
		simBeans.clear();
		JSONArray obJsonArray = data.getVal("data", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					SimBean simBean = new SimBean();
					simBean.title = objJson.getVal("title", String.class);
					simBean.price = objJson.getVal("price", Integer.class);
					simBean.rawPrice = objJson.getVal("rawPrice", Integer.class);
					simBeans.add(simBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
