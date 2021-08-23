package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class WeChatListViewData extends ListViewData {

	public WeChatListViewData() {
		super(TYPE_FULL_LIST_WECHAT);
	}

	public static class WeChatBean {
		public String id;
		public String name;
	}
	
	private ArrayList<WeChatBean> weChatBeans = new ArrayList<WeChatListViewData.WeChatBean>();
	
	public ArrayList<WeChatBean> getData() {
		return weChatBeans;
	}
	
	@Override
	public void parseItemData(JSONBuilder data) {
		weChatBeans.clear();
		JSONArray obJsonArray = data.getVal("contacts", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					WeChatBean weChatBean = new WeChatBean();
					weChatBean.name = objJson.getVal("name", String.class);
					weChatBean.id = objJson.getVal("id", String.class);
					weChatBeans.add(weChatBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
