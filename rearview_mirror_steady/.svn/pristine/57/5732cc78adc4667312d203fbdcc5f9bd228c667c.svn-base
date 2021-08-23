package com.txznet.music.bean.req;

import com.txznet.fm.bean.Configuration;
import com.txznet.music.Constant;

public class ReqAlbumAudio {
	private int sid; // 原id, 如1：qq音乐等
	private long id; // 专辑id
	private int pageId; // 页码 默认是1
	private int offset=Constant.PAGECOUNT; // 每页多个数量， 默认是10
	private int orderType; // 排序方式 ， 默认按数量
	private long categoryId;// 必须传递，
	private int version; // 版本

	public ReqAlbumAudio(int sid, long id, int pageId) {
		this();
		this.sid = sid;
		this.id = id;
		this.pageId = pageId;
		
	}

	public ReqAlbumAudio() {
		super();
		version=Configuration.getInstance().getInteger(Configuration.TXZ_Audio_VERSION);
	}


	public long getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(long categoryId) {
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

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public int getVersion() {
		return version;
	}


	@Override
	public String toString() {
		return "ReqAlbumAudio [sid=" + sid + ", id=" + id + ", pageId=" + pageId + ", offset=" + offset + ", orderType=" + orderType + ", categoryId=" + categoryId + ", version=" + version + "]";
	}

}
