package com.txznet.comm.ui.viewfactory.data;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;

import com.txznet.comm.util.JSONBuilder;

public class HelpListViewData extends ListViewData {

	public HelpListViewData() {
		super(TYPE_FULL_LIST_HELP);
	}
	
	public static class HelpBean {
		public String iconName;
		public String title;
		public String intro;
		
		public String time;
		public boolean isNew;
		public boolean isFromFile;

	}
	public boolean canOpenDetail;
	public boolean isShowTips;
	public String tips;
	public String qrCodeTitleIcon;
	public String qrCodeTitle;
	public String qrCodeUrl;
	public String qrCodeDesc;
	public String qrCodeGuideDesc;
	public boolean qrCodeNeedShowGuide;
	private ArrayList<HelpBean> helpBeans = new ArrayList<HelpListViewData.HelpBean>();
	public ArrayList<HelpBean> getData() {
		return helpBeans;
	}

	@Override
	public void parseItemData(JSONBuilder data) {
		helpBeans.clear();
		canOpenDetail = data.getVal("canOpenDetail",Boolean.class,true);
		isShowTips = data.getVal("isShowTips",Boolean.class,false);
		tips = data.getVal("tips",String.class,"");
		qrCodeTitleIcon = data.getVal("qrCodeTitleIcon", String.class, "");
		qrCodeTitle = data.getVal("qrCodeTitle", String.class, "");
		qrCodeUrl = data.getVal("qrCodeUrl", String.class, "");
		qrCodeDesc = data.getVal("qrCodeDesc", String.class, "");
		qrCodeGuideDesc = data.getVal("qrCodeGuideDesc", String.class, "");
		qrCodeNeedShowGuide = data.getVal("qrCodeNeedShowGuide", Boolean.class, false);


		JSONArray obJsonArray = data.getVal("helps", JSONArray.class);
		if (obJsonArray != null) {
			for (int i = 0; i < count; i++) {
				try {
					JSONBuilder objJson =new JSONBuilder( obJsonArray.getJSONObject(i));
					HelpBean helpBean = new HelpBean();
					helpBean.iconName = objJson.getVal("iconName",String.class);
					helpBean.title = objJson.getVal("title", String.class);
					helpBean.intro = objJson.getVal("intro", String.class);
					helpBean.isNew = objJson.getVal("isNew", Boolean.class,false);
					helpBean.time = objJson.getVal("time", String.class);
					helpBean.isFromFile = objJson.getVal("isFromFile", Boolean.class,false);
					helpBeans.add(helpBean);
				} catch (JSONException e) {
				}
			}
		}
	}
}
