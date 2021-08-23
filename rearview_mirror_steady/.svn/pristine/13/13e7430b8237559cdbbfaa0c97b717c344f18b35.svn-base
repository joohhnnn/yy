package com.txznet.txz.component.poi.txz;

import java.util.List;

import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.jni.JNIHelper;

public class MaxRaduisNearPoiSearchToolTXZimpl implements
		TXZPoiSearchManager.PoiSearchTool {
	private TXZPoiSearchManager.PoiSearchTool mPoiSearchToolDefault;
	private TXZPoiSearchManager.PoiSearchTool mPoiSearchToolMax;
	private PoiSearchResultListener mResultListener;
	private SearchReq mCurSearchReq;
	private NearbyPoiSearchOption mSearchOption;
	private int mSearchRadius = -1;

	public final static int MAX_NEARBY_RADIUS = 1000000;

	public MaxRaduisNearPoiSearchToolTXZimpl(
			TXZPoiSearchManager.PoiSearchTool toolDefault,
			TXZPoiSearchManager.PoiSearchTool toolMaxRadius) {
		mPoiSearchToolDefault = toolDefault;
		mPoiSearchToolMax = toolMaxRadius;
	}

	private PoiSearchResultListener mInnerResultListener = new PoiSearchResultListener() {
		@Override
		public void onSuggestion(SearchPoiSuggestion suggestion) {
			mSearchOption.setRadius(mSearchRadius);
			if (mResultListener != null) {
				mResultListener.onSuggestion(suggestion);
			}
		}

		@Override
		public void onResult(List<Poi> pois) {
			if (pois == null || pois.isEmpty()) {
				this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				return;
			}
			if (mSearchOption.getRadius() == MAX_NEARBY_RADIUS) {
				MonitorUtil
						.monitorCumulant(MonitorUtil.POISEARCH_RESULT_RADIUS_MORE);
			}
			mSearchOption.setRadius(mSearchRadius);
			if (mResultListener != null) {
				mResultListener.onResult(pois);
			}
		}

		@Override
		public void onError(int code, String desc) {
			if (mSearchOption.getRadius() <= 0) {
				mSearchOption.setRadius(MAX_NEARBY_RADIUS);
				JNIHelper.logd("txz poi search [" + mSearchOption.getKeywords()
						+ "] in nearby [" + mSearchOption.getRadius()
						+ "] with max radius tool"
						+ mPoiSearchToolMax.getClass().toString());
				mCurSearchReq = mPoiSearchToolMax.searchNearby(mSearchOption,
						mInnerResultListener);

				return;
			}

			mSearchOption.setRadius(mSearchRadius);
			if (mResultListener != null) {
				mResultListener.onError(code, desc);
			}
		}
	};

	SearchReq searchReq = new SearchReq() {
		@Override
		public void cancel() {
			mCurSearchReq.cancel();
			mResultListener = null;
		}
	};

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		return null;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		mSearchRadius = option.getRadius();
		mSearchOption = option;
		mResultListener = listener;
		JNIHelper.logd("txz poi search [" + mSearchOption.getKeywords()
				+ "] in nearby [" + mSearchOption.getRadius() + "] with tool"
				+ mPoiSearchToolDefault.getClass().toString());
		mCurSearchReq = mPoiSearchToolDefault.searchNearby(option,
				mInnerResultListener);
		return searchReq;
	}
}
