package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class ReminderListViewData extends ListViewData {

	public ReminderListViewData() {
		super(TYPE_FULL_LIST_REMINDER);
	}

	public static class ReminderItemBean {
		public String id;
		public String content;
		public String time;
		public String position;
	}

	private ArrayList<ReminderItemBean> reminderItemBeans = new ArrayList<ReminderListViewData.ReminderItemBean>();
	public ArrayList<ReminderItemBean> getData() {
		return reminderItemBeans;
	}
//	private String mHelpLabel;
//	public boolean isFromFile;
//	private String mHelpTitle;
//	public String getHelpLabel() {
//		return mHelpLabel;
//	}
//
//	public String getHelpTitle() {
//		return mHelpTitle;
//	}

	@Override
	public void parseItemData(JSONBuilder data) {
		reminderItemBeans.clear();
//		mHelpLabel = data.getVal("label", String.class);
//		mHelpTitle = data.getVal("title",String.class);
		JSONArray obJsonArray = data.getVal("reminders", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					ReminderItemBean reminderBean = new ReminderItemBean();
					reminderBean.id = objJson.getVal("id",String.class,"");
					reminderBean.content = objJson.getVal("content", String.class,"");
					reminderBean.time = objJson.getVal("time", String.class, "");
					reminderBean.position = objJson.getVal("position", String.class, "");
					reminderItemBeans.add(reminderBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
