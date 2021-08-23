package com.txznet.music.data.http.req;

import com.txznet.fm.bean.Configuration;

import java.util.ArrayList;
import java.util.List;

public class ReqCategory {
	private int categoryId; // 0：首页类型，其他数字表示首页中各个类型的子类型,默认为0
	private int bAll; // 是否获取该类所有的分类（子类的子类）， 默认0,1表示所有的分类
	private List<Integer> arrApp = new ArrayList<Integer>(); // 安装的音乐源列表,默认所有的。
																// 1表示考拉，2 表示QQ
	private int version;//请求的版本号，用于兼容
	private int logoTag;//1表示电台之家的样子，2表示

	public ReqCategory() {
		super();
		if (Configuration.getInstance().getInteger(Configuration.TXZ_SKIN) == 3) {
			logoTag = 1;
		}
		version = Configuration.getInstance().getInteger(
				Configuration.TXZ_Category_VERSION);

	}

	public int getVersion() {
		return version;
	}

	public int getLogoTag() {
		return logoTag;
	}

	public void setLogoTag(int logoTag) {
		this.logoTag = logoTag;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getbAll() {
		return bAll;
	}

	public void setbAll(int bAll) {
		this.bAll = bAll;
	}

	public List<Integer> getArrApp() {
		return arrApp;
	}

	@Override
	public String toString() {
		return "ReqCategory{" +
				"categoryId=" + categoryId +
				", bAll=" + bAll +
				", arrApp=" + arrApp +
				", version=" + version +
				", logoTag=" + logoTag +
				'}';
	}
}
