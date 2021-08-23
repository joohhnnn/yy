package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class CallListViewData extends ListViewData {

	public CallListViewData() {
		super(TYPE_FULL_LIST_CALL);
	}

	public static class CallBean {
		public String number;
		public String name;
		public String province;
		public String city;
		public String isp;
	}

	private ArrayList<CallBean> callBeans = new ArrayList<CallListViewData.CallBean>();

	public ArrayList<CallBean> getData() {
		return callBeans;
	}

	public boolean isMultName;

	@Override
	public void parseItemData(JSONBuilder data) {
		callBeans.clear();
		isMultName = data.getVal("isMultiName", Boolean.class);
		JSONArray obJsonArray = data.getVal("contacts", JSONArray.class);
		if (obJsonArray != null) {
			count = obJsonArray.length();
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson = new JSONBuilder(
							obJsonArray.getJSONObject(i));
					CallBean callBean = new CallBean();
					callBean.name = objJson.getVal("name", String.class);
					callBean.number = objJson.getVal("number", String.class);
					callBean.province = objJson
							.getVal("province", String.class);
					callBean.city = objJson.getVal("city", String.class);
					callBean.isp = objJson.getVal("isp", String.class);

					callBeans.add(callBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
