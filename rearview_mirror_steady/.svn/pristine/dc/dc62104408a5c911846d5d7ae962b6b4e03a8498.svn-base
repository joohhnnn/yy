package com.txznet.comm.ui.viewfactory.data;

import com.txznet.comm.util.JSONBuilder;

public abstract class ListViewData extends ViewData{

	public ListViewData(int type) {
		super(type);
	}
	
	/**
	 * 列表标题数据
	 */
	public static class TitleInfo {
		public String prefix;
		public String cityfix;
		public String midfix;
		public String titlefix;
		public String aftfix;
		public int curPage;
		public int maxPage;
		public boolean hideDrawable;
	}
	
	public TitleInfo mTitleInfo;
	public int count;
	public String action;
	public int type;
	public String keywords;
	public String vTips;
	
	public void parseData(String data) {
		JSONBuilder jsonBuilder = new JSONBuilder(data);
		type = jsonBuilder.getVal("listType", Integer.class,0);
		count = jsonBuilder.getVal("count", Integer.class,0);
		action = jsonBuilder.getVal("action", String.class);
		keywords = jsonBuilder.getVal("keywords", String.class);
		vTips = jsonBuilder.getVal("vTips", String.class);
		parseTitle(jsonBuilder);
		parseItemData(jsonBuilder);
		
	}
	
	/**
	 * 初始化标题
	 * @param data
	 */
	public void parseTitle(JSONBuilder data) {
		mTitleInfo = new TitleInfo();
		mTitleInfo.curPage = data.getVal("curPage", Integer.class,0);
		mTitleInfo.maxPage = data.getVal("maxPage", Integer.class,0);
		mTitleInfo.prefix = data.getVal("prefix", String.class);
		mTitleInfo.titlefix = data.getVal("titlefix", String.class);
		mTitleInfo.aftfix = data.getVal("aftfix", String.class);
		mTitleInfo.cityfix = data.getVal("city", String.class);
		mTitleInfo.midfix = data.getVal("midfix", String.class);
		mTitleInfo.hideDrawable = data.getVal("hideDrawable", Boolean.class, false);
	}
	
	/**
	 * 初始化item
	 * @param data
	 */
	public abstract void parseItemData(JSONBuilder data);
		
}
