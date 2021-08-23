package com.txznet.txz.component.nav.tx.internal;

import android.os.Parcel;
import android.os.Parcelable;

public class TNLatLng implements Parcelable {
	public static final int COORDINATE_SYSTEM_WGS84 = 0;
	public static final int COORDINATE_SYSTEM_GCJ02 = 1;
	public final static double INVALID_VALUE = Double.MIN_VALUE;
	/**
	 * 纬度，gcj02
	 */
	private double mLatitude;
	/**
	 * 经度，gcj02
	 */
	private double mLongitude;

	private int mCoordinateSystem = COORDINATE_SYSTEM_WGS84;

	public TNLatLng() {
		mLatitude = INVALID_VALUE;
		mLongitude = INVALID_VALUE;
	}

	public TNLatLng(TNLatLng in) {
		this.mLatitude = in.mLatitude;
		this.mLongitude = in.mLongitude;
	}

	/**
	 * 用给定的xy坐标构造一个GeoPoint
	 *
	 * @param latitude
	 *            纬度，gcj02
	 * @param longitude
	 *            经度，gcj02
	 */
	public TNLatLng(int latitude, int longitude) {
		this.mLatitude = latitude;
		this.mLongitude = longitude;
	}

	public TNLatLng(double latitude, double longitude) {
		this.mLatitude = latitude;
		this.mLongitude = longitude;
	}

	public double getLatitude() {
		return mLatitude;
	}

	public double getLongitude() {
		return mLongitude;
	}

	public void setLatitude(double latitude) {
		mLatitude = latitude;
	}

	public void setLongitude(double longitude) {
		mLongitude = longitude;
	}

	public void setCoordinateSystem(int coordinateSystem) {
		mCoordinateSystem = coordinateSystem;
	}

	public int getCoordinateSystem() {
		return mCoordinateSystem;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null)
			return false;

		return obj.getClass() == getClass() && (Math.abs(mLatitude - ((TNLatLng) obj).mLatitude) <= 1E-6)
				&& (Math.abs(mLongitude - ((TNLatLng) obj).mLongitude) <= 1E-6);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		double result = 1;
		result = prime * result + mLatitude;
		result = prime * result + mLongitude;
		return (int) result;
	}

	@Override
	public String toString() {
		return (mLongitude) + "," + (mLatitude);
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeDouble(mLatitude);
		out.writeDouble(mLongitude);
	}

	public static final Parcelable.Creator<TNLatLng> CREATOR = new Parcelable.Creator<TNLatLng>() {
		public TNLatLng createFromParcel(Parcel in) {
			return new TNLatLng(in);
		}

		public TNLatLng[] newArray(int size) {
			return new TNLatLng[size];
		}
	};

	private TNLatLng(Parcel in) {
		mLatitude = in.readDouble();
		mLongitude = in.readDouble();
	}

	public boolean isValid() {
		if (mLatitude > 1.0 && mLatitude < 180.0 && mLongitude > 1.0 && mLongitude < 180.0) {
			return true;
		} else {
			return false;
		}
	}
}
