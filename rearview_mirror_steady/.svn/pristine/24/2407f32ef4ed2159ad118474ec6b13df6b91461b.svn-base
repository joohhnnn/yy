package com.txznet.txz.component.poi.txz;

import java.util.List;

import com.txznet.comm.remote.util.MonitorUtil;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.CityPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.NearbyPoiSearchOption;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchInfo;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchTool;
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
	private final Object mObjectLock = new Object();

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
			JNIHelper.logd("onSuggestion:mSearchOption Radius set:"+mSearchRadius+"uid="+android.os.Process.myPid()+":tid="
					+ android.os.Process.myTid()+"-->"+className);
			mSearchOption.setRadius(mSearchRadius);
			synchronized (mObjectLock) {
                if (mResultListener != null) {
                    mResultListener.onSuggestion(suggestion);
                }
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
			JNIHelper.logd("onResult:mSearchOption Radius set:"+mSearchRadius+"uid="+android.os.Process.myPid()+":tid="
					+ android.os.Process.myTid()+"-->"+className);
			mSearchOption.setRadius(mSearchRadius);
			synchronized (mObjectLock) {
				if (mResultListener != null) {
					JNIHelper.logd("POISearchLog:MaxRaduisNearPoiSearchToolTXZimpl resultList size="+pois.size());
					mResultListener.onResult(pois);
				}
			}
		}

		@Override
		public void onError(int code, String desc) {
			JNIHelper.logd("onError:mSearchOption Radius is:"+mSearchOption.getRadius() +"uid="+android.os.Process.myPid()+":tid="
					+ android.os.Process.myTid()+"-->"+className);
			PoiSearchInfo info = mSearchOption.getSearchInfo();
			// 按位与运算：不等于0表示启用不需要该结果
			boolean isFilter = info.isTxzPoiToolComplete() && ((info.getDisShowEngine() &  1<<(mPoiSearchToolMax.getPoiSearchType()-1)) != 0);
			if (mSearchOption.getRadius() <= 0 && !isFilter) {
				JNIHelper.logd("onError:mSearchOption Radius set:"+MAX_NEARBY_RADIUS+"uid="+android.os.Process.myPid()+":tid="
						+ android.os.Process.myTid()+"-->"+className);
				mSearchRadius = MAX_NEARBY_RADIUS;
				mSearchOption.setRadius(MAX_NEARBY_RADIUS);
				JNIHelper.logd("POISearchLog:txz poi search [" + mSearchOption.getKeywords()
						+ "] in nearby [" + mSearchOption.getRadius()
						+ "] with max radius tool"
						+ mPoiSearchToolMax.getClass().toString());
				mCurrentTool = mPoiSearchToolMax;
				mCurSearchReq = mPoiSearchToolMax.searchNearby(mSearchOption,
						mInnerResultListener);

				return;
			}

			JNIHelper.logd("onError:mSearchOption Radius set:"+mSearchRadius+"uid="+android.os.Process.myPid()+":tid="
					+ android.os.Process.myTid()+"-->"+className);
			mSearchOption.setRadius(mSearchRadius);
			synchronized (mObjectLock) {
                if (mResultListener != null) {
                    mResultListener.onError(code, desc);
                }
            }
		}
	};

	SearchReq searchReq = new SearchReq() {
		@Override
		public void cancel() {
			mCurSearchReq.cancel();
			synchronized (mObjectLock) {
			    mResultListener = null;
			}
		}
	};

	@Override
	public SearchReq searchInCity(CityPoiSearchOption option,
			PoiSearchResultListener listener) {
		return null;
	}
	String className=null;
	@Override
	public SearchReq searchNearby(NearbyPoiSearchOption option,
			PoiSearchResultListener listener) {
		className=mPoiSearchToolDefault.getClass().toString();
		JNIHelper.logd("searchNearby :mSearchRadius set:"+mSearchRadius+"uid="+android.os.Process.myPid()+":tid="
				+ android.os.Process.myTid()+"-->"+className);
		mSearchOption = copyOption(option);
		mSearchRadius = mSearchOption.getRadius();
		mResultListener = listener;
		JNIHelper.logd("POISearchLog:txz poi search [" + mSearchOption.getKeywords()
				+ "] in nearby [" + mSearchOption.getCity() + " Radius="+mSearchOption.getRadius() +"] with tool"
				+ mPoiSearchToolDefault.getClass().toString());
		mCurrentTool = mPoiSearchToolDefault;
		mCurSearchReq = mPoiSearchToolDefault.searchNearby(option,
				mInnerResultListener);
		return searchReq;
	}
	private NearbyPoiSearchOption copyOption(NearbyPoiSearchOption opt){
		if(opt==null)
			return null;
		NearbyPoiSearchOption nearbyPoiSearchOption = new NearbyPoiSearchOption();
		nearbyPoiSearchOption.setCenterLat(opt.getCenterLat());
		nearbyPoiSearchOption.setCenterLng(opt.getCenterLng());
		nearbyPoiSearchOption.setCity(opt.getCity());
		nearbyPoiSearchOption.setKeywords(opt.getKeywords());
		nearbyPoiSearchOption.setNum(opt.getNum());
		nearbyPoiSearchOption.setRadius(opt.getRadius());
		nearbyPoiSearchOption.setRegion(opt.getRegion());
		nearbyPoiSearchOption.setTimeout(opt.getTimeout());
		PoiSearchInfo searchInfo = nearbyPoiSearchOption.getSearchInfo();
		PoiSearchInfo searchInfo2 = opt.getSearchInfo();
		searchInfo.setDisShowEngine(searchInfo2.getDisShowEngine());
		searchInfo.setPoiRetryCount(searchInfo2.getPoiRetryCount());
		searchInfo.setPoiSourceConf(searchInfo2.getPoiSourceConf());
		return nearbyPoiSearchOption;
	}
	private PoiSearchTool mCurrentTool = null;
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		if (mSearchOption != null) {
			mSearchOption.getSearchInfo().setDisShowEngine(disShowPoiType);
		}
		if(mCurrentTool != null){
			mCurrentTool.stopPoiSearchTool(disShowPoiType);
		}
	}
	@Override
	public int getPoiSearchType() {
		return 0;
	}

}
