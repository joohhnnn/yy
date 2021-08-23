package com.txznet.txz.component.nav.tx.internal;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;

public class TNPoi implements Parcelable {
	public String poiID;
	public String poiName;
	public String poiType;
	public TNLatLng coordinate = new TNLatLng();
	public TNLatLng naviCoordinate = new TNLatLng();
	public int districtID;
	public String telephone;
	public String address;
	public float distanceToCenter;// 距离中心的距离
	public boolean isHasStreetScape;// 是否有街景
	public ArrayList<TNPoi> childPoiList;
	public int childPoiCount;
	public String poiAliasName; // 别名

	public TNPoi() {
		address = "";
		poiName = "";
	}

	public TNPoi(TNPoi poi) {
		copy(poi);
	}

	private TNPoi(Parcel in) {
		readFromParcel(in);
	}

	private void readFromParcel(Parcel in) {
		this.poiID = in.readString();
		this.poiName = in.readString();
		this.poiType = in.readString();
		coordinate.setLatitude(in.readDouble());
		coordinate.setLongitude(in.readDouble());
		naviCoordinate.setLatitude(in.readDouble());
		naviCoordinate.setLongitude(in.readDouble());
		this.districtID = in.readInt();
		this.telephone = in.readString();
		this.address = in.readString();
		this.distanceToCenter = in.readFloat();
		this.isHasStreetScape = in.readByte() != 0;
	}

	@Override
	public String toString() {
		return "TNPoi{" + "poiID='" + poiID + '\'' + ", poiName='" + poiName + '\'' + ", poiType=" + poiType
				+ ", coordinate=" + coordinate.toString() + ", naviCoordinate=" + naviCoordinate.toString()
				+ ", districtID=" + districtID + ", telephone='" + telephone + '\'' + ", address='" + address + '\''
				+ ", distanceToCenter=" + distanceToCenter + ", isHasStreetScape=" + isHasStreetScape + '}';
	}

	public static final Parcelable.Creator<TNPoi> CREATOR = new Parcelable.Creator<TNPoi>() {
		public TNPoi createFromParcel(Parcel in) {
			return new TNPoi(in);
		}

		public TNPoi[] newArray(int size) {
			return new TNPoi[size];
		}
	};

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(poiID == null ? "" : poiID);
		dest.writeString(poiName == null ? "" : poiName);
		dest.writeString(poiType == null ? "" : poiType);
		dest.writeDouble(coordinate.getLatitude());
		dest.writeDouble(coordinate.getLongitude());
		dest.writeDouble(naviCoordinate.getLatitude());
		dest.writeDouble(naviCoordinate.getLongitude());
		dest.writeInt(districtID);
		dest.writeString(telephone == null ? "" : telephone);
		dest.writeString(address == null ? "" : address);
		dest.writeFloat(distanceToCenter);
		dest.writeByte((byte) (isHasStreetScape ? 1 : 0));
	}

	/**
	 * 拷贝函数
	 *
	 * @param poi
	 */
	public void copy(TNPoi poi) {
		if (poi == null)
			return;

		if (!TextUtils.isEmpty(poi.poiID))
			poiID = new String(poi.poiID);
		else
			poiID = "";

		if (!TextUtils.isEmpty(poi.poiName))
			poiName = new String(poi.poiName);
		else
			poiName = "";

		if (!TextUtils.isEmpty(poi.poiType))
			poiType = new String(poi.poiType);
		else
			poiType = "";

		if (poi.coordinate != null)
			coordinate = new TNLatLng(poi.coordinate);
		else
			coordinate = new TNLatLng();

		if (poi.naviCoordinate != null)
			naviCoordinate = new TNLatLng(poi.naviCoordinate);
		else
			naviCoordinate = new TNLatLng();

		districtID = poi.districtID;

		if (!TextUtils.isEmpty(poi.telephone))
			telephone = new String(poi.telephone);
		else
			telephone = "";

		if (!TextUtils.isEmpty(poi.address))
			address = new String(poi.address);
		else
			address = "";

		distanceToCenter = poi.distanceToCenter;

		isHasStreetScape = poi.isHasStreetScape;

	}
}
