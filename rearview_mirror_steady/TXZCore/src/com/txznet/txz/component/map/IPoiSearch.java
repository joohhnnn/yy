package com.txznet.txz.component.map;

import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;

/**
 * POI检索组件接口，暂时使用百度的数据结构
 * 
 * @author bihongpi
 *
 */
public interface IPoiSearch {

	/**
	 * 初始化回调
	 */
	public interface IInitCallback {
		public void onInit(boolean bSuccess);
	}

	/**
	 * 检索回调
	 */
	public interface IPoiSearchCallback {
		/**
		 * 搜索结果回调
		 * 
		 * @param result
		 */
		public void onGetPoiResult(PoiResult result);
	}
	
	public interface IPoiSearchTask {
		public void cancel();
	}
	
	/**
	 * 初始化
	 * 
	 * @param oRun
	 * @return
	 */
	public int initialize(final IInitCallback oRun);

	/**
	 * 释放
	 */
	public void release();

	/**
	 * 范围内检索
	 * 
	 * @param option
	 * @return
	 */
	public IPoiSearchTask searchInBound(PoiBoundSearchOption option,
			IPoiSearchCallback callback);

	/**
	 * 城市内检索
	 * 
	 * @param option
	 * @return
	 */
	public IPoiSearchTask searchInCity(PoiCitySearchOption option,
			IPoiSearchCallback callback);

	/**
	 * 周边检索
	 * 
	 * @param option
	 * @return
	 */
	public IPoiSearchTask searchNearby(PoiNearbySearchOption option,
			IPoiSearchCallback callback);

}
