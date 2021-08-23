package com.txznet.txz.component.poi.txz;

import java.util.List;

import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;

public class CenterPoiSearchResultListener implements PoiSearchResultListener {
	private NearbyPoiSearchOption mPoiSearchOption;
	private PoiSearchTool mPoiSearchTool;
	private PoiSearchResultListener mPoiSearchResultListener;
	NextStepListener mNextStepListener;

	public interface NextStepListener {
		void onBegin(SearchReq req);
	}

	public CenterPoiSearchResultListener(NearbyPoiSearchOption nextOption,
			TXZPoiSearchManager.PoiSearchTool nextTool,
			PoiSearchResultListener nextListener,
			NextStepListener nextStepListener) {
		this.mPoiSearchOption = nextOption;
		this.mPoiSearchTool = nextTool;
		this.mPoiSearchResultListener = nextListener;
		this.mNextStepListener = nextStepListener;
	}

	@Override
	public void onError(int arg0, String arg1) {
		mPoiSearchResultListener.onError(arg0, arg1);
	}

	@Override
	public void onResult(List<Poi> pois) {
		if (pois == null || pois.isEmpty()) {
			this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			return;
		}
		mPoiSearchOption.setCenterLat(pois.get(0).getLat());
		mPoiSearchOption.setCenterLng(pois.get(0).getLng());
		mPoiSearchOption.setCity(pois.get(0).getCity());
		mNextStepListener.onBegin(mPoiSearchTool.searchNearby(mPoiSearchOption,
				mPoiSearchResultListener));
	}

	@Override
	public void onSuggestion(SearchPoiSuggestion arg0) {
		mPoiSearchResultListener.onSuggestion(arg0);
	}
}
