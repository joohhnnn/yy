package com.txznet.txz.component.poi.txz;

import java.util.List;

import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.jni.JNIHelper;

public class PoiSearchToolTXZimpl implements TXZPoiSearchManager.PoiSearchTool {
	private TXZPoiSearchManager.PoiSearchTool[] mPoiSearchTools;
	private PoiSearchOption mSearchOption;
	private PoiSearchResultListener mResultListener;
	private int mIndex;
	private SearchReq mCurSearchReq;
	private String mCurAction;

	public PoiSearchToolTXZimpl(TXZPoiSearchManager.PoiSearchTool... tools) {
		mPoiSearchTools = tools;
	}

	private PoiSearchResultListener mInnerResultListener = new PoiSearchResultListener() {
		@Override
		public void onSuggestion(SearchPoiSuggestion suggestion) {
			mResultListener.onSuggestion(suggestion);
		}

		@Override
		public void onResult(List<Poi> pois) {
			if (pois == null || pois.isEmpty()) {
				this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				return;
			}
			if (mResultListener != null) {
				mResultListener.onResult(pois);
			}
		}

		@Override
		public void onError(int code, String desc) {
			if (mIndex > mPoiSearchTools.length - 1) {
				if (mResultListener != null) {
					mResultListener.onError(code, desc);
					mResultListener = null;
				}
				return;
			}
			// 最后一次搜索用城市搜索
			// if (mIndex == mPoiSearchTools.length - 1) {
			// searchInCity((CityPoiSearchOption) mSearchOption,
			// mInnerResultListener);
			// return;
			// }

			MonitorUtil.monitorCumulant(MonitorUtil.POISEARCH_ENTER_BACKUP);

			if ("near".equals(mCurAction)) {
				searchNearby((NearbyPoiSearchOption) mSearchOption,
						mResultListener);
			} else if ("city".equals(mCurAction)) {
				searchInCity((CityPoiSearchOption) mSearchOption,
						mResultListener);
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
		mSearchOption = option;
		mResultListener = listener;
		mCurAction = "city";
		TXZPoiSearchManager.PoiSearchTool tool = mPoiSearchTools[mIndex++];
		JNIHelper.logd("txz poi search [" + option.getKeywords()
				+ "]  in city[" + option.getCity() + "] with tool"
				+ tool.getClass().toString());
		mCurSearchReq = tool.searchInCity(option, mInnerResultListener);
		return searchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		mSearchOption = option;
		mResultListener = listener;
		mCurAction = "near";
		TXZPoiSearchManager.PoiSearchTool tool = mPoiSearchTools[mIndex++];
		JNIHelper.logd("txz poi search [" + option.getKeywords()
				+ "] in nearby [" + option.getCity() + "] with tool"
				+ tool.getClass().toString());
		mCurSearchReq = tool.searchNearby(option, mInnerResultListener);
		return searchReq;
	}
}
