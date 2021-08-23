package com.txznet.music.bean.response;

import java.io.Serializable;
import java.util.List;

public class Album implements Serializable {
	private int sid; // 原id, 如1：qq音乐等
	private long id; // 专辑id
	private String name; // 名字
	private String logo; // 封面
	private String desc; // 描述
	private int likedNum; // 喜欢的数量
	private long listenNum; // 收听的次数
	private List<String> arrArtistName; // 艺术家名称
	private int categoryId;// 当前专辑所属类别ID
	//
	private List<Integer> arrCategoryIds; // 分类,eg:开心的，甜美的
	
	private String report;	//播报内容
	
	
	public String getReport() {
		return report;
	}

	public void setReport(String report) {
		this.report = report;
	}
	
	public int getCategoryID() {
		return categoryId;
	}

	public void setCategoryID(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getSid() {
		return sid;
	}

	public void setSid(int sid) {
		this.sid = sid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLogo() {
		return logo;
	}

	public void setLogo(String logo) {
		this.logo = logo;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public List<String> getArrArtistName() {
		return arrArtistName;
	}

	public void setArrArtistName(List<String> arrArtistName) {
		this.arrArtistName = arrArtistName;
	}

	public int getLikedNum() {
		return likedNum;
	}

	public void setLikedNum(int likedNum) {
		this.likedNum = likedNum;
	}

	public long getListenNum() {
		return listenNum;
	}

	public void setListenNum(long listenNum) {
		this.listenNum = listenNum;
	}

	public List<Integer> getArrCategoryIds() {
		return arrCategoryIds;
	}

	public void setArrCategoryIds(List<Integer> arrCategoryIds) {
		this.arrCategoryIds = arrCategoryIds;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (id ^ (id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Album other = (Album) obj;
		if (id != other.id)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Album [sid=" + sid + ", id=" + id + ", name=" + name + "]";
	}
	
	

}
