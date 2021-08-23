package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class CinemaListViewData extends ListViewData {

	public CinemaListViewData() {
		super(TYPE_FULL_LIST_CINEMA);
	}
	
	private ArrayList<CinemaBean> cinemaBeans = new ArrayList<CinemaBean>();
	
	public ArrayList<CinemaBean> getData() {
		return cinemaBeans;
	}
	
	public static class CinemaBean {
		public String title;
		public String post;
		public double score;
	}

	@Override
	public void parseItemData(JSONBuilder data) {
		cinemaBeans.clear();
		JSONArray obJsonArray = data.getVal("cines", JSONArray.class);
		CinemaBean cinemaBean;
		for (int i = 0; i < count; i++) {
			try {
				JSONBuilder cBuilder = new JSONBuilder(obJsonArray.getJSONObject(i));
				cinemaBean = new CinemaBean();
				cinemaBean.title = cBuilder.getVal("name", String.class);
				cinemaBean.post = cBuilder.getVal("post", String.class);
				cinemaBean.score = cBuilder.getVal("score", Double.class);
				cinemaBeans.add(cinemaBean);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
