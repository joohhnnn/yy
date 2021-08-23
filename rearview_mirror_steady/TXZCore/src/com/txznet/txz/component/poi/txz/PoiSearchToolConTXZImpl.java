package com.txznet.txz.component.poi.txz;

import java.util.ArrayList;
import java.util.List;

import com.txznet.loader.AppLogic;
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

/**
 * 有优先级的并行搜索工具
 */
public class PoiSearchToolConTXZImpl implements TXZPoiSearchManager.PoiSearchTool {
	private boolean mEnd;
	PoiSearchResultListener mResultListener;
	List<ToolRecord> mToolList = new ArrayList<PoiSearchToolConTXZImpl.ToolRecord>();

	private class ToolRecord {
		int err = TXZPoiSearchManager.ERROR_CODE_EMPTY;
		List<Poi> result;
		public SearchReq searchReq;
		public boolean complete = false;
		public boolean  optional= false;
		public TXZPoiSearchManager.PoiSearchTool tool;

		public ToolRecord(TXZPoiSearchManager.PoiSearchTool tool,boolean o) {
			this.tool = tool;
			this.optional = o;
		}

		PoiSearchResultListener listener = new PoiSearchResultListener() {

			@Override
			public void onSuggestion(SearchPoiSuggestion suggestion) {
				JNIHelper.logd("PoiSearchToolConTXZImpl onSuggestion " + tool.getClass().getName());
				onError(TXZPoiSearchManager.ERROR_CODE_EMPTY, "");
			}

			@Override
			public void onResult(final List<Poi> result) {
				JNIHelper.logd("PoiSearchToolConTXZImpl onResult " + tool.getClass().getName());
				AppLogic.runOnBackGround(new Runnable() {

					@Override
					public void run() {
						if (mEnd) {
							return;
						}
						ToolRecord.this.result = result;
						ToolRecord.this.complete = true;
						PoiSearchToolConTXZImpl.this.checkPoiResult();
					}
				}, 0);
			}

			@Override
			public void onError(final int errCode, String errDesc) {
				JNIHelper.logd("PoiSearchToolConTXZImpl onError " + tool.getClass().getName() + " mEnd:" + mEnd);
				AppLogic.runOnBackGround(new Runnable() {

					@Override
					public void run() {
						if (mEnd) {
							return;
						}
						ToolRecord.this.err = errCode;
						ToolRecord.this.complete = true;
						PoiSearchToolConTXZImpl.this.checkPoiResult();
					}
				}, 0);
			}
		};
	}
	List<Poi> mPoiList= new ArrayList<Poi>();
	private void checkPoiResult() {
		if( !isUseStop && mOption.getSearchInfo().isTxzPoiToolComplete()){
			stopPoiSearchTool(mOption.getSearchInfo().getDisShowEngine());
		}		
		if (mEnd == true) {
			return;
		}
		JNIHelper.logw("checkPoiResult");
		int completeCoumt = 0;
		mPoiList.clear();
		for (ToolRecord tr:mToolList) {				
			if (tr.complete) {
				completeCoumt++;
				if(completeCoumt==mToolList.size()){
					mEnd=true;
				}
				if (tr.result == null || tr.result.size() <= 0) {
					continue;
				}
				mPoiList.addAll(tr.result);
				if(mOption.getSearchInfo().isTxzPoiToolComplete()){
					NavManager.getInstance().filterPoiResult(mOption.getSearchInfo(),mPoiList);
					if(mPoiList.size()==0){
						continue;
					}
				}else{
					continue;
				}
				if(mPoiList.size()>mOption.getNum()){
					mPoiList=mPoiList.subList(0, mOption.getNum()-1);
				}
				// 找到结果
				mEnd = true;
				break;
			} else {
				if(tr.optional){
					continue;
				}else{
					break;
				}	
			}
		}					
		
		if (!mEnd) {
			// 还未结束
			return;
		}

		if (mResultListener == null) {
			return;
		}

		JNIHelper.logd("search return onResult");
		int err = TXZPoiSearchManager.ERROR_CODE_EMPTY;
		if (mPoiList.size() == 0) {
			for (ToolRecord tool : mToolList) {
				if (err == TXZPoiSearchManager.ERROR_CODE_EMPTY) {
					err = tool.err;
				} else if (err == TXZPoiSearchManager.ERROR_CODE_UNKNOW) {
					if (tool.err == TXZPoiSearchManager.ERROR_CODE_TIMEOUT) {
						err = TXZPoiSearchManager.ERROR_CODE_TIMEOUT;
					}else if (tool.err ==TXZPoiSearchManager.ERROR_CODE_NAVICLOSE){
						err = TXZPoiSearchManager.ERROR_CODE_NAVICLOSE;
					}
				}
			}
			mResultListener.onError(err, "");
		} else {
			JNIHelper.logd("POISearchLog: PoiSearchToolConTXZImpl resultList size="+mPoiList.size());
			mResultListener.onResult(mPoiList);
		}
	}

	SearchReq mAllSearchReq = new SearchReq() {

		@Override
		public void cancel() {
			mEnd = true;
			for (ToolRecord tool : mToolList) {
				if (tool != null) {
					tool.searchReq.cancel();
				}
			}
		}
	};

	public PoiSearchToolConTXZImpl() {
	}
	
	public PoiSearchToolConTXZImpl addPoiSearchTool(TXZPoiSearchManager.PoiSearchTool  tool,boolean needComplete) {
		
		if (tool != null) {
			mToolList.add(new ToolRecord(tool,needComplete));
			JNIHelper.logw("add ToolRecord:" + tool.getClass().getName());
		}
		return this;
	}
	
	private PoiSearchOption mOption;
	@Override
	public SearchReq searchInCity(final CityPoiSearchOption option, PoiSearchResultListener listener) {
		mOption=option;
		mResultListener = listener;
		for (ToolRecord pst : mToolList) {
			if (pst != null) {
				JNIHelper.logd("POISearchLog:txz poi search [" + option.getKeywords() + "] in city [" + option.getCity()
						+ "] with tool" + pst.tool.getClass().toString());
				pst.searchReq = pst.tool.searchInCity(option, pst.listener);
			}
		}
		return mAllSearchReq;
	}

	@Override
	public SearchReq searchNearby(final NearbyPoiSearchOption option, PoiSearchResultListener listener) {
		mOption=option;
		mResultListener = listener;
		for (ToolRecord pst : mToolList) {
			if (pst != null) {
				JNIHelper.logd("POISearchLog:txz poi search [" + option.getKeywords() + "] in nearby [" + option.getCity()
						+ "] with tool" + pst.tool.getClass().toString());
				pst.searchReq = pst.tool.searchNearby(option, pst.listener);
			}
		}
		return mAllSearchReq;
	}
	boolean isUseStop = false;
	@Override
	public void stopPoiSearchTool(int disShowPoiType) {
		isUseStop = true;
		for (ToolRecord pst : mToolList) {
			pst.tool.stopPoiSearchTool(disShowPoiType);
		}
	}

	@Override
	public int getPoiSearchType() {
		return 0;
	}

}