package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class HelpTipsViewData extends ViewData {

	public HelpTipsViewData() {
		super(TYPE_CHAT_HELP_TIPS);
	}

	public static class HelpTipBean {
		public int id;
		public String resId;
		public String label;
	}
	public String mTitle;
	public int mCount;
	private ArrayList<HelpTipBean> mHelpTipBeans = new ArrayList<HelpTipsViewData.HelpTipBean>();
	public ArrayList<HelpTipBean> getData() {
		return mHelpTipBeans;
	}

	public void parseItemData(JSONBuilder jsonBuilder) {
		mHelpTipBeans.clear();
		mTitle = jsonBuilder.getVal("title",String.class);
		JSONArray jsonArray = jsonBuilder.getVal("data",JSONArray.class);
		if (jsonArray != null) {
			mCount = jsonArray.length();
			mHelpTipBeans = new ArrayList<HelpTipBean>(mCount);
			JSONBuilder jsonBean;
			HelpTipBean mHelpTipBean;
			for (int i = 0; i < mCount; i++) {
				try {
					jsonBean = new JSONBuilder(jsonArray.getJSONObject(i));
					mHelpTipBean = new HelpTipBean();
					mHelpTipBean.resId = jsonBean.getVal("resId",String.class);
					mHelpTipBean.label = jsonBean.getVal("label",String.class);
					mHelpTipBeans.add(mHelpTipBean);
				} catch (JSONException e) {
					e.printStackTrace();
				}

			}
		}
	}

}
