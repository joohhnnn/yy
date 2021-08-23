package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class StyleListViewData extends ListViewData {

	public StyleListViewData() {
		super(TYPE_FULL_LIST_STYLE);
	}

	public static class StyleBean {
		public String name;
		public String model;
		public String theme;
	}

	private ArrayList<StyleBean> styleBeans = new ArrayList<StyleListViewData.StyleBean>();
	
	public ArrayList<StyleBean> getData() {
		return styleBeans;
	}
	
	@Override
	public void parseItemData(JSONBuilder data) {
		styleBeans.clear();
		JSONArray obJsonArray = data.getVal("themes", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					StyleBean ttsBean  = new StyleBean();
					ttsBean.model = objJson.getVal("model", String.class);
					ttsBean.name = objJson.getVal("name", String.class);
					ttsBean.theme = objJson.getVal("theme", String.class);
					styleBeans.add(ttsBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
