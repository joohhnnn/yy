package com.txznet.txz.component.roadtraffic.txz;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

import com.txz.ui.voice.VoiceData.RoadTrafficQueryInfo;
import com.txznet.loader.AppLogic;
import com.txznet.sdk.TXZPoiSearchManager;
import com.txznet.sdk.TXZPoiSearchManager.PoiSearchResultListener;
import com.txznet.sdk.TXZPoiSearchManager.SearchPoiSuggestion;
import com.txznet.sdk.bean.Poi;
import com.txznet.txz.component.poi.txz.PoiSearchToolConTXZImpl;
import com.txznet.txz.component.roadtraffic.IInquiryRoadTrafficListener;
import com.txznet.txz.component.roadtraffic.IRoadTrafficTool;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager.SearchReq;

public class RoadTrafficToolConImp implements IRoadTrafficTool{
	private boolean mEnd=false;
	List<ToolRecord> mToolRecord = new ArrayList<ToolRecord>();
	IInquiryRoadTrafficListener mListener=null;
	
	SearchReq mSearchReq= new SearchReq() {		
		@Override
		public void cancel() {
			for(ToolRecord tool: mToolRecord){
				tool.searchReq.cancel();
				mListener=null;
			}
		}
	};
	
	private class ToolRecord {
		int err =RoadTrafficManager.ERROR_CODE_NULL;
		RoadTrafficResult result;
		public SearchReq searchReq;
		public boolean complete = false;
		public boolean isDity=false;
		public IRoadTrafficTool tool;
		IInquiryRoadTrafficListener listener= new IInquiryRoadTrafficListener() {
			@Override
			public void onResult(final RoadTrafficResult result) {
				AppLogic.runOnBackGround(new Runnable() {
					@Override
					public void run() {
						if (mEnd) {
							return;
						}
						ToolRecord.this.result = result;
						ToolRecord.this.complete = true;
						RoadTrafficToolConImp.this.checkPoiResult();
					}
				}, 0);
			}		
			@Override
			public void onError(int errCode, String errDesc) {
				ToolRecord.this.complete = true;
				ToolRecord.this.err=errCode;
				RoadTrafficToolConImp.this.checkPoiResult();
			}
		};
		public ToolRecord(IRoadTrafficTool tool) {
			this.tool = tool;
		}

	}
	
	public RoadTrafficToolConImp addTools(IRoadTrafficTool tool){
		ToolRecord record= new ToolRecord(tool);
		mToolRecord.add(record);
		return this;
	}
	
	protected void checkPoiResult() {
		JNIHelper.logd("RoadTrafficDebug:checkPoiResult");
		if (mEnd == true) {
			return;
		}

		int count = 0; // 完成任务的工具数
		RoadTrafficResult result = null;
		int err=RoadTrafficManager.ERROR_CODE_NULL;
		for (int index=0;index< mToolRecord.size(); index++) {
			ToolRecord tool = mToolRecord.get(index);
			if(tool.complete){
				count++;
				if(tool.result!=null){
					JNIHelper.logd("RoadTrafficDebug:"+tool.tool.getClass().toString()+" has result");
					mEnd=true; // 存在结果直接处理
					result=tool.result;
					break;
				}else{
					JNIHelper.logd("RoadTrafficDebug:"+tool.tool.getClass().toString()+" is error  err:"+tool.err);
					/* 【ID1038974】【大众朗逸】【语音core】附近路况提示：该地图不支持路况查询。非附近，支持路况查询
					 * 该问题的原因是下面遍历了全部的工具，将每次工具的异常都覆盖上去到
					 */
					 if (tool.err != RoadTrafficManager.ERROR_CODE_NULL) {
						err=tool.err;
					}
				}
			}else{
				continue;
			}	
		}
		if(mListener==null)
			return ;
		if(count == mToolRecord.size()) // 所有工具执行完毕
			mEnd=true;
		if(!mEnd)
			return;
		if(result==null){
			mListener.onError(err, "出错");
			return;
		}
		mListener.onResult(result);
	}

	@Override
	public void init() {
		
	}
	@Override
	public SearchReq inquiryRoadTrafficByPoi(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mListener=listener;
		for(ToolRecord tool: mToolRecord){
			JNIHelper.logd("RoadTrafficDebug:inquiry ["+info.strKeywords+"] roadtraffic by tool "+tool.tool.getClass().toString());
			tool.searchReq = tool.tool.inquiryRoadTrafficByPoi(info, tool.listener);
		}
		return mSearchReq;
	}

	@Override
	public SearchReq inquiryRoadTrafficByFront(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mListener=listener;
		for(ToolRecord tool: mToolRecord){
			JNIHelper.logd("RoadTrafficDebug:inquiry front roadtraffic by tool "+tool.tool.getClass().toString());
			tool.searchReq = tool.tool.inquiryRoadTrafficByFront(info, tool.listener);
		}
		return mSearchReq;
	}

	@Override
	public SearchReq inquiryRoadTrafficByNearby(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mListener=listener;
		for(ToolRecord tool: mToolRecord){
			JNIHelper.logd("RoadTrafficDebug:inquiry nearby roadtraffic by tool "+tool.tool.getClass().toString());
			tool.searchReq = tool.tool.inquiryRoadTrafficByNearby(info, tool.listener);
		}
		return mSearchReq;
	}

}
