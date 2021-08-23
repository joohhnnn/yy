package com.txznet.music.bean.response;

import java.util.List;

public class Category {
	// "desc":"\u6700\u7231",
	// "logo":"http:\/\/www.txzing.com\/audio\/audio_logo.php?f=fm_home_icon_bisexual.jpg",
	// "arrChild":null}
	private String desc;// 类别名称
	private int categoryId;
	private String logo;
	private List<Category> arrChild;
	private int drawableId;// 用于本地图片Id

	public Category() {
		super();
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Category(int categoryId, int drawableId, String desc) {
		super();
		this.categoryId = categoryId;
		this.drawableId = drawableId;
		this.desc = desc;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public List<Category> getArrChild() {
		return arrChild;
	}

	public void setArrChild(List<Category> arrChild) {
		this.arrChild = arrChild;
	}

	public int getDrawableId() {
		return drawableId;
	}

	public void setDrawableId(int drawableId) {
		this.drawableId = drawableId;
	}

	@Override
	public String toString() {
		return "Category [categoryId=" + categoryId + ", logo=" + logo
				+ ", arrChild=" + arrChild + "]";
	}

}
