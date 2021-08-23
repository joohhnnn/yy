package com.txznet.nav.offline;

import java.util.List;

import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapManager;
import com.amap.api.maps.offlinemap.OfflineMapProvince;

public interface IOffline {

	public void initialize();

	public List<OfflineMapProvince> getProvinces();

	public List<OfflineMapCity> getOfflineMapCities(int groupPos);

	public void downloadMapCity(int groupPos, String cityName,
			DownloadListener listener);

	public void downloadMapProvince(int groupPos, String provName,
			DownloadListener listener);

	public void pause();

	public void goon();

	public OfflineMapManager getOfflineMapManager();

	public static abstract class DownloadListener {
		public abstract void onCityStatus(int status, int completeCode,
				String name);
	}
}