package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class TtsListViewData extends ListViewData {

	public TtsListViewData() {
		super(TYPE_FULL_LIST_TTS);
	}

	public static class TtsBean {
		public int id;
		public String name;
	}
	
	private ArrayList<TtsBean> ttsBeans = new ArrayList<TtsListViewData.TtsBean>();
	
	public ArrayList<TtsBean> getData() {
		return ttsBeans;
	}
	
	@Override
	public void parseItemData(JSONBuilder data) {
		ttsBeans.clear();
		JSONArray obJsonArray = data.getVal("themes", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					TtsBean ttsBean  = new TtsBean();
					ttsBean.id = objJson.getVal("id", Integer.class);
					ttsBean.name = objJson.getVal("name", String.class);
					ttsBeans.add(ttsBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
