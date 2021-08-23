package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class HelpDetailImageViewData extends ListViewData {

	public HelpDetailImageViewData() {
		super(TYPE_FULL_LIST_HELP_IMAGE_DETAIL);
	}

	public static class HelpDetailBean {
		public String id;
		public String text;
		public String time;
		public String img;
	}

	private ArrayList<HelpDetailBean> helpDetailBeans = new ArrayList<HelpDetailImageViewData.HelpDetailBean>();
	public ArrayList<HelpDetailBean> getData() {
		return helpDetailBeans;
	}
	private String mHelpLabel;
	public boolean isFromFile;
	private String mHelpTitle;
	public String getHelpLabel() {
		return mHelpLabel;
	}

	public String getHelpTitle() {
		return mHelpTitle;
	}

	@Override
	public void parseItemData(JSONBuilder data) {
		helpDetailBeans.clear();
		mHelpLabel = data.getVal("label", String.class);
		mHelpTitle = data.getVal("title",String.class);
		isFromFile = data.getVal("isFromFile", Boolean.class,false);
		JSONArray obJsonArray = data.getVal("helpDetails", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					HelpDetailBean helpBean = new HelpDetailBean();
					helpBean.id = objJson.getVal("id",String.class);
					helpBean.text = objJson.getVal("text", String.class);
					helpBean.time = objJson.getVal("time", String.class);
					helpBean.img = objJson.getVal("img", String.class);
					helpDetailBeans.add(helpBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
