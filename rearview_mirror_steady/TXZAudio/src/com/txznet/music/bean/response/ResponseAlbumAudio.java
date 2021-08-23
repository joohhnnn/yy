package com.txznet.music.bean.response;

import java.util.List;

public class ResponseAlbumAudio {
	private int sid; // 原id, 如1：qq音乐等
	private long id; // 专辑id
	private int categoryId; // 专辑id
	private int pageId; // 页码
	private int offset; // 每页多个数量
	private int orderType; // 排序方式
	private int totalNum; // 总数量
	private int totalPage; // 总页数
	private int errCode; // 成功为0，不成功则为错误码
	private List<Audio> arrAudio; // 专辑内音频


	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
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

	public int getTotalNum() {
		return totalNum;
	}

	public void setTotalNum(int totalNum) {
		this.totalNum = totalNum;
	}

	public int getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public List<Audio> getArrAudio() {
		return arrAudio;
	}

	public void setArrAudio(List<Audio> arrAudio) {
		this.arrAudio = arrAudio;
	}

}
