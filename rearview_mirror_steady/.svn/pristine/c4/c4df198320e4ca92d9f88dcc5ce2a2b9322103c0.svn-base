package com.txznet.txz.component.poi.txz;

import java.util.List;

import com.txz.ui.map.UiMap.LocationInfo;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchInfo;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.TXZPoiSearchManager.SearchReq;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.poi.gaode.PoiSearchToolGaodeImpl;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.location.LocationManager;
import com.txznet.txz.module.nav.NavManager;

import android.text.TextUtils;

public class CityPoiSearchToolTXZimpl implements
		TXZPoiSearchManager.PoiSearchTool {
	private TXZPoiSearchManager.PoiSearchTool mPoiSearchToolFirst;
	private TXZPoiSearchManager.PoiSearchTool mPoiSearchToolNext;
	private TXZPoiSearchManager.PoiSearchTool mPoiSearchToolSuggest;
	private PoiSearchResultListener mResultListener;
	private SearchReq mCurSearchReq;
	private boolean mProcSuggestion;

	private PoiSearchResultListener mInnerResultListener = new PoiSearchResultListener() {
		@Override
		public void onSuggestion(SearchPoiSuggestion suggestion) {
			JNIHelper.logd("POISearchLog : CityPoiSearchToolTXZimpl onSuggestion");
			if (mProcSuggestion // 已处理过搜索建议
		     || mCityPoiSearchOption instanceof NearbyPoiSearchOption // 周边搜索暂时不处理搜索建议
			) {
				this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
				return;
			}
			mProcSuggestion = true;
			// TODO 多个搜索建议目前默认采用第一个城市
			if (suggestion.getCity().size() > 0) {
				JNIHelper.logd("POISearchLog :  CityPoiSearchToolTXZimpl onSuggestion city = "+ suggestion.getCity());
				NavManager.getInstance().setNomCityListForString(suggestion.getCity());
				JNIHelper.logd("POISearchLog :  CityPoiSearchToolTXZimpl onSuggestion city = "+ suggestion.getCity().get(0));
				mCityPoiSearchOption.setCity(suggestion.getCity().get(0));
			} else {
				// 替换成当前城市进行搜索
				LocationInfo myLocation = LocationManager.getInstance()
						.getLastLocation();
				if (myLocation != null
						&& myLocation.msgGeoInfo != null
						&& !TextUtils.isEmpty(myLocation.msgGeoInfo.strCity)
						&& !myLocation.msgGeoInfo.strCity
								.equals(mCityPoiSearchOption.getCity())) {
					mCityPoiSearchOption.setCity(myLocation.msgGeoInfo.strCity);
				} else {
					this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
					return;
				}
			}
			PoiSearchInfo info = mCityPoiSearchOption.getSearchInfo();
			boolean isFilter = info.isTxzPoiToolComplete() && ((info.getDisShowEngine() &  1<<(mPoiSearchToolSuggest.getPoiSearchType()-1)) != 0);
			if ( !TextUtils.isEmpty(mCityPoiSearchOption.getCity()) && !isFilter ) {
				JNIHelper.logd("POISearchLog : CityPoiSearchToolTXZimpl onSuggestion txz poi search [" + mCityPoiSearchOption.getKeywords()
						+ "] in city [" + mCityPoiSearchOption.getCity()
						+ "] with tool" + mPoiSearchToolSuggest.getClass().toString());
				mCurrentTool = mPoiSearchToolSuggest;
				mCurSearchReq = mPoiSearchToolSuggest.searchInCity(
						mCityPoiSearchOption, mInnerResultListener);
				return;
			}
			this.onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
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
			mResultListener = null;
		}

		@Override
		public void onError(int code, String desc) {
			PoiSearchInfo info = mCityPoiSearchOption.getSearchInfo();
			boolean isFilter = info.isTxzPoiToolComplete() && ((info.getDisShowEngine() &  1<<(mPoiSearchToolNext.getPoiSearchType()-1)) != 0);
			if( !isFilter && code == TXZPoiSearchManager.ERROR_CODE_EMPTY  ){
				if (bEmtpyCity
						&& !TextUtils.isEmpty(mCityPoiSearchOption.getCity())) {
					mCityPoiSearchOption.setCity(null);
					JNIHelper.logd("txz poi search [" + mCityPoiSearchOption.getKeywords()
							+ "] in city [" + mCityPoiSearchOption.getCity()
							+ "] with tool" + mPoiSearchToolNext.getClass().toString());
					mCurrentTool = mPoiSearchToolNext;
					mCurSearchReq = mPoiSearchToolNext.searchInCity(
							mCityPoiSearchOption, mInnerResultListener);
					return;
				}
			}
			if (mResultListener != null) {
				mResultListener.onError(code, desc);
				mResultListener = null;
			}
		}
	};

	public CityPoiSearchToolTXZimpl(TXZPoiSearchManager.PoiSearchTool first,
			TXZPoiSearchManager.PoiSearchTool next,
			TXZPoiSearchManager.PoiSearchTool suggest) {
		mPoiSearchToolFirst = first;
		mPoiSearchToolNext = next;
		mPoiSearchToolSuggest = suggest;
	}

	SearchReq searchReq = new SearchReq() {
		@Override
		public void cancel() {
			mCurSearchReq.cancel();
			mResultListener = null;
		}
	};

	boolean bEmtpyCity;
	CityPoiSearchOption mCityPoiSearchOption;

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		mCityPoiSearchOption = option;
		mResultListener = listener;
		// 如果搜索城市为空，优先在当前城市进行搜索
		bEmtpyCity = TextUtils.isEmpty(option.getCity());
		if (bEmtpyCity) {
			LocationInfo myLocation = LocationManager.getInstance()
					.getLastLocation();
			if (myLocation != null && myLocation.msgGeoInfo != null) {
				mCityPoiSearchOption.setCity(myLocation.msgGeoInfo.strCity);
			}
		}
		JNIHelper.logd("POISearchLog:txz poi search [" + mCityPoiSearchOption.getKeywords()
				+ "] in city [" + mCityPoiSearchOption.getCity()
				+ "] with tool" + mPoiSearchToolFirst.getClass().toString());
		mCurrentTool = mPoiSearchToolFirst;
		mCurSearchReq = mPoiSearchToolFirst.searchInCity(mCityPoiSearchOption,
				mInnerResultListener);
		return searchReq;
	}

	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		return null;
	}

	private PoiSearchTool mCurrentTool = null;
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if(mCurrentTool != null){
			mCurrentTool.stopPoiSearchTool(disShowPoiType);
		}
	}

	@Override
	public int getPoiSearchType() {
		return 0;
	}
}
