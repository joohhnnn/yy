package com.txznet.txz.module.location;

import com.txz.ui.map.UiMap.LocationInfo;

public interface ILocationClient {
	int LOCATION_DEFAULT_TIME_INTERVAL = 3;
	
	public void quickLocation(boolean bQuick);

	public void setLastLocation(LocationInfo location);

	public LocationInfo getLastLocation();
	
	public void setTimeInterval(int timeInterval);
	
	public void release();
}
