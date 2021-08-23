package com.txznet.txz.component.poi.txz;

import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.txz.jni.JNIHelper;

public class NearToCityPoiSearchToolTXZimpl implements TXZPoiSearchManager.PoiSearchTool {
	TXZPoiSearchManager.PoiSearchTool mPoiSearchTool;

	public NearToCityPoiSearchToolTXZimpl(TXZPoiSearchManager.PoiSearchTool tool) {
		mPoiSearchTool = tool;
	}

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option, PoiSearchResultListener listener) {
		return null;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option, PoiSearchResultListener listener) {
		JNIHelper.logd("txz poi search [" + option.getKeywords() + "] in nearby to city [" + option.getCity() + "] with tool" + mPoiSearchTool.getClass().toString());
		return mPoiSearchTool.searchInCity(option, listener);
	}
}
