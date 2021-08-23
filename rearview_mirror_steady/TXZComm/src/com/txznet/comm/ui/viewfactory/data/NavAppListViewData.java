package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import com.txznet.comm.util.JSONBuilder;

public class NavAppListViewData extends ListViewData{
	public static class NavAppBean {
		public int id;
		public String navPkn;
		public String title;
	}
	
	private ArrayList<NavAppBean> navAppBeans = new ArrayList<NavAppListViewData.NavAppBean>();

	public NavAppListViewData() {
		super(ViewData.TYPE_FULL_LIST_SIMPLE_LIST);
	}
	
	public ArrayList<NavAppBean> getData(){
		return navAppBeans;
	}

	@Override
	public void parseItemData(JSONBuilder data) {
		navAppBeans.clear();
		String[] beans = data.getVal("beans", String[].class);
		for (int i = 0; i < beans.length; i++) {
			String val = beans[i];
			String[] vals = val.split(":");
			NavAppBean object = new NavAppBean();
			object.title = vals[0];
			object.navPkn = vals[1];
			navAppBeans.add(object);
		}
	}
}
