package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class HelpDetailListViewData extends ListViewData {

	public HelpDetailListViewData() {
		super(TYPE_FULL_LIST_HELP_DETAIL);
	}
	
	public static class HelpDetailBean {
		public String id;
		public String title;
		public String time;
		public boolean isNew;
		public int netType;
	}
	
	private ArrayList<HelpDetailBean> helpDetailBeans = new ArrayList<HelpDetailListViewData.HelpDetailBean>();
	public ArrayList<HelpDetailBean> getData() {
		return helpDetailBeans;
	}
	private String mHelpLabel;
	public String getHelpLabel() {
		return mHelpLabel;
	}
	public boolean hasNet = true;

	@Override
	public void parseItemData(JSONBuilder data) {
		helpDetailBeans.clear();
		mHelpLabel = data.getVal("label", String.class);
		hasNet = data.getVal("hasNet",Boolean.class,true);
		JSONArray obJsonArray = data.getVal("helpDetails", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					HelpDetailBean helpBean = new HelpDetailBean();
					helpBean.id = objJson.getVal("id",String.class);
					helpBean.title = objJson.getVal("title", String.class);
					helpBean.time = objJson.getVal("time", String.class);
					helpBean.isNew = objJson.getVal("isNew", Boolean.class,false);
					helpBean.netType = objJson.getVal("netType", Integer.class, 0);
					helpDetailBeans.add(helpBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
