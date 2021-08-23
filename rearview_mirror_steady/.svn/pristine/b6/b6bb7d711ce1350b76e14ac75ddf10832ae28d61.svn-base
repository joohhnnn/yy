package com.txznet.music.bean.response;

import java.util.List;

public class Homepage<T> {
	private int reqType; // 请求类型
	private int errCode; // 成功为0，不成功则为错误码
	private List<T> arrCategory; // 分类列表

	public int getErrCode() {
		return errCode;
	}

	public void setErrCode(int errCode) {
		this.errCode = errCode;
	}

	public List<T> getArrCategory() {
		return arrCategory;
	}

	public void setArrCategory(List<T> arrCategory) {
		this.arrCategory = arrCategory;
	}

	public int getReqType() {
		return reqType;
	}

	public void setReqType(int reqType) {
		this.reqType = reqType;
	}

	@Override
	public String toString() {
		return "Homepage [reqType=" + reqType + ", errCode=" + errCode
				+ ", arrCategory=" + arrCategory.toString() + "]";
	}

}
