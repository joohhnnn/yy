package com.txznet.txz.component.poi.txz;

import java.util.ArrayList;
import java.util.Arrays;
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
import com.txznet.txz.module.nav.NavManager;

import android.os.SystemClock;

public class PoiSearchToolTXZimpl implements TXZPoiSearchManager.PoiSearchTool {
	private TXZPoiSearchManager.PoiSearchTool[] mPoiSearchTools;
	private PoiSearchOption mSearchOption;
	private PoiSearchResultListener mResultListener;
	private int mIndex;
	private SearchReq mCurSearchReq;
	private String mCurAction;

	private int mTimeout;
	private long mStartSearchTime;
	private float[] mTimeoutRatios;
	private int[] mIndexTimeout;

	public PoiSearchToolTXZimpl(TXZPoiSearchManager.PoiSearchTool... tools) {
		mPoiSearchTools = tools;

		mTimeoutRatios = new float[mPoiSearchTools.length];
		float num = (mPoiSearchTools.length * (mPoiSearchTools.length + 1)) / 2;
		for (int i = 0; i < mPoiSearchTools.length; i++) {
			mTimeoutRatios[i] = (mPoiSearchTools.length - i) / num;
		}
	}
	
	private void dynamicTimeout(PoiSearchOption option) {
		if (mIndex == 0) {
			mTimeout = option.getTimeout();
			mStartSearchTime = SystemClock.elapsedRealtime();

			mIndexTimeout = new int[mPoiSearchTools.length];
			// 根据比例计算出每个工具的超时时间
			for (int i = 0; i < mPoiSearchTools.length; i++) {
				mIndexTimeout[i] = (int) (mTimeoutRatios[i] * mTimeout);
			}

			JNIHelper.logd("txz poi search begin with timeout:" + mTimeout + ",each:" + Arrays.toString(mIndexTimeout));
		}

		long curRealTime = SystemClock.elapsedRealtime();
		long delay = curRealTime - mStartSearchTime;

		int timeout = mIndexTimeout[mIndex];
		if (mIndex == mPoiSearchTools.length - 1) { // 最后一个
			timeout = (int) (mTimeout - delay);
		}

		option.setTimeout(timeout);
	}
	List<Poi> mPoiList=new ArrayList<Poi>();
	private PoiSearchResultListener mInnerResultListener = new PoiSearchResultListener() {
		@Override
		public void onSuggestion(SearchPoiSuggestion suggestion) {
			mResultListener.onSuggestion(suggestion);
		}

		@Override
		public void onResult(List<Poi> pois) {
			mPoiList.addAll(pois);
			if(mSearchOption.getSearchInfo().isTxzPoiToolComplete()){	
				return;
			}else{
				NavManager.getInstance().filterPoiResult(mSearchOption.getSearchInfo(),mPoiList);
			}
			if (mPoiList == null || mPoiList.isEmpty()) {
				this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				return;
			}
			if (mResultListener != null) {
				if(mPoiList.size()>mSearchOption.getNum()){
					mPoiList.subList(0, mSearchOption.getNum()-1);
				}
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
		dynamicTimeout(option);
		
		mSearchOption = option;
		mResultListener = listener;
		mCurAction = "city";
		TXZPoiSearchManager.PoiSearchTool tool = mPoiSearchTools[mIndex++];
		JNIHelper.logd("POISearchLog:txz poi search [" + option.getKeywords()
				+ "]  in city[" + option.getCity() + "] with tool"
				+ tool.getClass().toString() + " will timeout after:" + option.getTimeout());
		mCurSearchReq = tool.searchInCity(option, mInnerResultListener);
		return searchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		dynamicTimeout(option);
		
		mSearchOption = option;
		mResultListener = listener;
		mCurAction = "near";
		TXZPoiSearchManager.PoiSearchTool tool = mPoiSearchTools[mIndex++];
		JNIHelper.logd("POISearchLog:txz poi search [" + option.getKeywords()
				+ "] in nearby [" + option.getCity() + "] with tool"
				+ tool.getClass().toString() + " will timeout after:" + option.getTimeout());
		mCurSearchReq = tool.searchNearby(option, mInnerResultListener);
		return searchReq;
	}

	@Override
	public void stopPoiSearchTool(int arg0) {
		
	}

	@Override
	public int getPoiSearchType() {
		return 0;
	}
}
