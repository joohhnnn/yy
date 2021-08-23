package com.txznet.music.bean.req;

import java.util.Arrays;

import android.os.Parcel;
import android.os.Parcelable;

import com.txznet.music.Constant;

public class ReqSearchAlbum implements Parcelable {
	private int categoryId;
	private int pageId = 1;
	private int offset;// 一页请求几个数据，服务其默认九个，
	private int orderType = 1;
	private Integer[] arrApp;
	private String version;

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(categoryId);
		out.writeInt(pageId);
		out.writeInt(offset);
		out.writeInt(orderType);
		out.writeArray(arrApp);
		out.writeString(version);
	}

	public static final Parcelable.Creator<ReqSearchAlbum> CREATOR = new Parcelable.Creator<ReqSearchAlbum>() {
		public ReqSearchAlbum[] newArray(int size) {
			return new ReqSearchAlbum[size];
		}

		@Override
		public ReqSearchAlbum createFromParcel(Parcel source) {
			return null;
		}
	};

	private ReqSearchAlbum(Parcel in) {
		categoryId = in.readInt();
		pageId = in.readInt();
		offset = in.readInt();
		orderType = in.readInt();
		arrApp = (Integer[]) in.readArray(Integer.class.getClassLoader());
		version = in.readString();
	}

	public ReqSearchAlbum() {

	}

	public ReqSearchAlbum(int pageId) {
		super();
		this.pageId = pageId;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	public int getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(int categoryId) {
		this.categoryId = categoryId;
	}

	public int getPageId() {
		return pageId;
	}

	public void setPageId(int pageId) {
		this.pageId = pageId;
	}

	public int getOffset() {
		return Constant.PAGECOUNT;
	}

	public int getOrderType() {
		return orderType;
	}

	public void setOrderType(int orderType) {
		this.orderType = orderType;
	}

	public Integer[] getArrApp() {
		return arrApp;
	}

	public void setArrApp(Integer[] arrApp) {
		this.arrApp = arrApp;
	}

	public String getVersion() {
		return Constant.Version;
	}

	public static Parcelable.Creator<ReqSearchAlbum> getCreator() {
		return CREATOR;
	}

	@Override
	public String toString() {
		return "ReqSearchAlbum [categoryId=" + categoryId + ", pageId="
				+ pageId + ", offset=" + offset + ", orderType=" + orderType
				+ ", arrApp=" + Arrays.toString(arrApp) + ", version="
				+ version + "]";
	}

}
