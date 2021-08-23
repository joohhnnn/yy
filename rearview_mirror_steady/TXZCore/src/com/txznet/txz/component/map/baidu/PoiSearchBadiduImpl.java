package com.txznet.txz.component.map.baidu;

import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiBoundSearchOption;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiNearbySearchOption;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.txznet.txz.component.map.IPoiSearch;
import com.txznet.txz.jni.JNIHelper;

public class PoiSearchBadiduImpl implements IPoiSearch {
	
	class TXZGetPoiSearchResultListener implements OnGetPoiSearchResultListener {
		IPoiSearchCallback mCallback;

		public TXZGetPoiSearchResultListener(IPoiSearchCallback cb) {
			mCallback = cb;
		}

		@Override
		public void onGetPoiDetailResult(PoiDetailResult result) {
		}

		@Override
		public void onGetPoiResult(PoiResult result) {
			if (mCallback != null) {
				mCallback.onGetPoiResult(result);
			}
		}

		@Override
		public void onGetPoiIndoorResult(PoiIndoorResult arg0) {
			// TODO Auto-generated method stub
			
		}
	}

	class PoiSearchTaskBaidu implements IPoiSearchTask {
		PoiSearch mInstance;
		OnGetPoiSearchResultListener mListener;

		public PoiSearchTaskBaidu(IPoiSearchCallback callback) {
			mInstance = PoiSearch.newInstance();
			mListener = new TXZGetPoiSearchResultListener(callback);
			mInstance.setOnGetPoiSearchResultListener(mListener);
		}

		@Override
		public void cancel() {
			mInstance.destroy();
		}
	}

	@Override
	public int initialize(IInitCallback oRun) {
		// TODO 百度地图模块初始化
		return 0;
	}

	@Override
	public void release() {
		// TODO Auto-generated method stub
	}

	@Override
	public IPoiSearchTask searchInBound(PoiBoundSearchOption option,
			IPoiSearchCallback callback) {
		try {
			PoiSearchTaskBaidu t = new PoiSearchTaskBaidu(callback);
			if (t.mInstance.searchInBound(option))
				return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		JNIHelper.loge("poi search error");
		return null;
	}

	@Override
	public IPoiSearchTask searchInCity(PoiCitySearchOption option,
			IPoiSearchCallback callback) {
		try {
			PoiSearchTaskBaidu t = new PoiSearchTaskBaidu(callback);
			if (t.mInstance.searchInCity(option))
				return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		JNIHelper.loge("poi search error");
		return null;
	}

	@Override
	public IPoiSearchTask searchNearby(PoiNearbySearchOption option,
			IPoiSearchCallback callback) {
		try {
			PoiSearchTaskBaidu t = new PoiSearchTaskBaidu(callback);
			if (t.mInstance.searchNearby(option))
				return t;
		} catch (Exception e) {
			e.printStackTrace();
		}
		JNIHelper.loge("poi search error");
		return null;
	}

}
