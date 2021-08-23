package com.txznet.txz.ui.win.help;

import com.txznet.comm.util.JSONBuilder;

import org.json.JSONArray;

import java.util.ArrayList;

public class HelpDetail implements Cloneable{

	public String title;
	public String intro;
	public int iconResId;
	public String[] desps;
	public String iconName;
	
	//新增
	public ArrayList<HelpDetailItem> detailItems;
	public boolean isNew;
	public String time;
	public String id;
	public String name;

	public int openType;//打开类型,0默认到三级指令界面,1到富文本界面,2打开指定app，需设置包名,3打开列表和富文本
	public String strPackage;//应用的包名
	public int order;//排序
	public ArrayList<HelpDetailImg> detailImgs;//图文详情
	public String lastName;//上一次的命名
	public String tool;//

	public String[] intros;

	public boolean hasNet = true;

	public static class HelpDetailImg implements Cloneable {
		public String id;
		public String text;
		public String time;
		public String img;

		@Override
		protected Object clone()  {
			HelpDetailImg helpDetailImg = null;
			try {
				helpDetailImg = (HelpDetailImg) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return helpDetailImg;
		}

		@Override
		public String toString() {
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("id", id);
			jsonBuilder.put("text", text);
			jsonBuilder.put("time", time);
			jsonBuilder.put("img", img);
			return jsonBuilder.toString();
		}

		public static HelpDetailImg fromString(String json) {
			HelpDetailImg img = new HelpDetailImg();
			JSONBuilder jsonBuilder = new JSONBuilder(json);
			img.id = jsonBuilder.getVal("id", String.class, "");
			img.text = jsonBuilder.getVal("text", String.class, "");
			img.time = jsonBuilder.getVal("time", String.class, "");
			img.img = jsonBuilder.getVal("img", String.class, "");
			return img;
		}
	}

	public static class HelpDetailItem implements Cloneable {
		public String id;
		public String name;
		public String time;
		public boolean isNew;
		public int netType;//这条是在线命令还是离线命令，0表示mix，1表示在线，2表示离线
		public int order;
		public String lastName;


		@Override
		protected Object clone() {
			HelpDetailItem helpDetailItem = null;
			try {
				helpDetailItem = (HelpDetailItem) super.clone();
			} catch (CloneNotSupportedException e) {
				e.printStackTrace();
			}
			return helpDetailItem;
		}

		@Override
		public String toString() {
			JSONBuilder jsonBuilder = new JSONBuilder();
			jsonBuilder.put("id", id);
			jsonBuilder.put("name", name);
			jsonBuilder.put("time", time);
			jsonBuilder.put("isNew", isNew);
			jsonBuilder.put("netType", netType);
			jsonBuilder.put("order", order);
			jsonBuilder.put("lastName", lastName);
			return jsonBuilder.toString();
		}

		public static HelpDetailItem fromString(String json) {
			HelpDetailItem item = new HelpDetailItem();
			JSONBuilder jsonBuilder = new JSONBuilder(json);
			item.id = jsonBuilder.getVal("id", String.class, "");
			item.name = jsonBuilder.getVal("name", String.class, "");
			item.time = jsonBuilder.getVal("time", String.class, "");
			item.isNew = jsonBuilder.getVal("isNew", Boolean.class, false);
			item.netType = jsonBuilder.getVal("netType", Integer.class, 0);
			item.order = jsonBuilder.getVal("order", Integer.class, 0);
			item.lastName = jsonBuilder.getVal("lastName", String.class, "");
			return item;
		}
	}

	@Override
	protected HelpDetail clone(){
		HelpDetail helpDetail = null;
		try {
			helpDetail = (HelpDetail) super.clone();
			if (detailItems != null) {
				helpDetail.detailItems = (ArrayList<HelpDetailItem>) detailItems.clone();
			}
			if (detailImgs != null) {
				helpDetail.detailImgs = (ArrayList<HelpDetailImg>) detailImgs.clone();
			}
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return helpDetail;
	}

	@Override
	public String toString() {
		JSONBuilder jsonBuilder = new JSONBuilder();
		jsonBuilder.put("title", title);
		jsonBuilder.put("intro", intro);
		jsonBuilder.put("iconResId", iconResId);
		jsonBuilder.put("desps", desps);
		jsonBuilder.put("iconName", iconName);
		if (detailItems != null && !detailItems.isEmpty()) {
			JSONArray jsonArray = new JSONArray();
			for (HelpDetailItem item : detailItems) {
				jsonArray.put(item.toString());
			}
			jsonBuilder.put("detailItems", jsonArray);
		}
		jsonBuilder.put("isNew", isNew);
		jsonBuilder.put("time", time);
		jsonBuilder.put("id", id);
		jsonBuilder.put("name", name);

		jsonBuilder.put("openType", openType);
		jsonBuilder.put("strPackage", strPackage);
		jsonBuilder.put("order", order);
		if (detailImgs != null && !detailImgs.isEmpty()) {
			JSONArray jsonArray = new JSONArray();
			for (HelpDetailImg img : detailImgs) {
				jsonArray.put(img.toString());
			}
			jsonBuilder.put("detailImgs", jsonArray);
		}
		jsonBuilder.put("lastName", lastName);
		jsonBuilder.put("tool", tool);
		jsonBuilder.put("intros", intros);
		jsonBuilder.put("hasNet", hasNet);
		return jsonBuilder.toString();
	}

	public static HelpDetail fromString(String json) {
		HelpDetail detail = new HelpDetail();
		JSONBuilder jsonBuilder = new JSONBuilder(json);
		detail.title = jsonBuilder.getVal("title", String.class, "");
		detail.intro = jsonBuilder.getVal("intro", String.class, "");
		detail.iconResId = jsonBuilder.getVal("iconResId", Integer.class, 0);
		detail.desps = jsonBuilder.getVal("desps", String[].class, null);
		detail.iconName = jsonBuilder.getVal("iconName", String.class, "");

		JSONArray jsonArray = jsonBuilder.getVal("detailItems", JSONArray.class, null);
		if (jsonArray != null && jsonArray.length() > 0) {
			ArrayList<HelpDetailItem> items = new ArrayList<HelpDetailItem>();
			for (int i = 0; i < jsonArray.length(); i++) {
				items.add(HelpDetailItem.fromString(jsonArray.optString(i)));
			}
			detail.detailItems = items;
		}
		detail.isNew = jsonBuilder.getVal("isNew", Boolean.class, false);
		detail.time = jsonBuilder.getVal("time", String.class, "");
		detail.id = jsonBuilder.getVal("id", String.class, "");
		detail.name = jsonBuilder.getVal("name", String.class, "");
		detail.openType = jsonBuilder.getVal("openType", Integer.class, 0);
		detail.strPackage = jsonBuilder.getVal("strPackage", String.class, "");
		detail.order = jsonBuilder.getVal("order", Integer.class, 0);
		JSONArray jsonArray1 = jsonBuilder.getVal("detailImgs", JSONArray.class, null);
		if (jsonArray1 != null && jsonArray1.length() > 0) {
			ArrayList<HelpDetailImg> items = new ArrayList<HelpDetailImg>();
			for (int i = 0; i < jsonArray1.length(); i++) {
				items.add(HelpDetailImg.fromString(jsonArray1.optString(i)));
			}
			detail.detailImgs = items;
		}
		detail.lastName = jsonBuilder.getVal("lastName", String.class, "");
		detail.tool = jsonBuilder.getVal("tool", String.class, "");
		detail.intros = jsonBuilder.getVal("intros", String[].class, null);
		detail.hasNet = jsonBuilder.getVal("hasNet", Boolean.class, true);
		return detail;
	}
}
