package com.txznet.txz.component.roadtraffic.gaode;

import android.text.TextUtils;

import com.txz.ui.voice.VoiceData.RoadTrafficQueryInfo;
import com.txznet.comm.util.JSONBuilder;
import com.txznet.loader.AppLogic;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.gaode.NavAmapAutoNavImpl;
import com.txznet.txz.component.nav.gaode.NavAmapValueService;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.Option;
import com.txznet.txz.component.nav.gaode.NavAmapValueService.PoiQuery.PoiQueryResultListener;
import com.txznet.txz.component.roadtraffic.IInquiryRoadTrafficListener;
import com.txznet.txz.component.roadtraffic.IRoadTrafficTool;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.nav.tool.NavAppManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager.SearchReq;

import org.json.JSONException;
import org.json.JSONObject;

public class RoadTrafficGaoDeTool implements IRoadTrafficTool{
	// 高德地图车镜版支持路况查询的最低版本
	public static final int GAODE_SUPPORT_VERSION = 210548;
	// 高德地图车机版支持路况查询的最低版本
	public static final int GAODE_SUPPORT_VERSION_CAR = 554;

	 private  IInquiryRoadTrafficListener mResultListener;
	 SearchReq mSearchReq = new SearchReq() {
		@Override
		public void cancel() {
			JNIHelper.logd("RoadTrafficDebug: GD cancel");
			if(mOption!=null){
				PoiQuery.getInstance().cancel(mOption);
				mOption=null;
			}
			mResultListener=null;
		}
	};
	Runnable mRunnableSearchTimeout = new Runnable() {
		@Override
		public void run() {
			if (mResultListener != null) {
				JNIHelper.logd("RoadTrafficDebug: GD inquiry timeout");
				mResultListener.onError(5, "超时");
				mSearchReq.cancel();
			}
		}
	};
	private Option mOption;
	 
	@Override
	public void init() {
		
	}

	@Override
	public SearchReq inquiryRoadTrafficByPoi(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mResultListener=listener;
		return inquiryRoadTraffic(info);
	}

	@Override
	public SearchReq inquiryRoadTrafficByFront(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mResultListener=listener;
		return inquiryRoadTraffic(info);
	}

	@Override
	public SearchReq inquiryRoadTrafficByNearby(RoadTrafficQueryInfo info,
			IInquiryRoadTrafficListener listener) {
		mResultListener=listener;
		return inquiryRoadTraffic(info);
	}

	private SearchReq inquiryRoadTraffic(RoadTrafficQueryInfo info){
	  	//int mapVersion = NavManager.getInstance().getLocalNavImpl().getMapVersion();
        //if(mapVersion<RoadTrafficManager.GAODE_SUPPORT_VERSION){
        if(!isTrafficQuerySupported()){
        	NavAmapValueService.getInstance().setTrafficSearchEnableAutoPupUp(false);
        	mResultListener.onError(RoadTrafficManager.ERROR_CODE_NULL, "null");
        	return new SearchReq() {
        		@Override
        		public void cancel() {
        			
        		}
        	};
        }
		AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
		AppLogic.runOnBackGround(mRunnableSearchTimeout, 8000);
		mOption = new Option();
		mOption.searchType =12401;
		mOption.traffic=info.strKeywords+"的路况";
		mOption.mResultListener = new PoiQueryResultListener() {
			@Override
			public void onResult(String strData) {
				AppLogic.removeBackGroundCallback(mRunnableSearchTimeout);
				if(TextUtils.isEmpty(strData)){
					NavAmapValueService.getInstance().setTrafficSearchEnableAutoPupUp(true);
					if(mResultListener!=null)
						JNIHelper.logd("RoadTrafficDebug:GD result is null");
						mResultListener.onError(RoadTrafficManager.ERROR_CODE_NULL, "null");
				}
				JSONObject json = new JSONBuilder(strData).build();
				try {
					int code = json.getInt("code");
					String textMessage = json.getString("text");
					JNIHelper.logd("RoadTrafficDebug:GD result code="+code+" text= "+textMessage);
					switch (code) {
					case 1:
						RoadTrafficResult result= new RoadTrafficResult();
						result.setErrorCode(code);
						result.setResultText(textMessage);
						result.setSourceType(RoadTrafficManager.SOURCE_TYPE_GAODE);
						if(mResultListener!=null){
							NavAmapValueService.getInstance().setTrafficSearchEnableAutoPupUp(false);
							mResultListener.onResult(result);
						}else{
						}
						break;
					case 2:case 3:case 4:case 5:
						NavAmapValueService.getInstance().setTrafficSearchEnableAutoPupUp(true);
						if(mResultListener!=null)
							mResultListener.onError(code, textMessage);
						break;
					default:
						break;
					}
				} catch (JSONException e) {
					NavAmapValueService.getInstance().setTrafficSearchEnableAutoPupUp(true);
					if(mResultListener!=null){
						mResultListener.onError(RoadTrafficManager.ERROR_CODE_OTHER, "");
					}
				}
			}
		};
		PoiQuery.getInstance().startQuery(mOption);
		if(NavManager.getInstance().isNavi()&&(!NavManager.getInstance().getLocalNavImpl().isInFocus())){
			NavManager.getInstance().getLocalNavImpl().enterNav();
		}
		return mSearchReq;
	}

	/**
	 * 检查当前安装的高德版本是否支持路况查询
	 *
	 * 当前只检查了公版的高德车镜版和车机版版本号, 包名分别为
	 * com.autonavi.amapauto 车机版
	 * com.autonavi.amapautolite 车镜版
	 *
	 * @return 支持返回true
	 */
	private boolean isTrafficQuerySupported() {
		// 尝试检查车机版版本号
		NavThirdApp gdApp = NavAppManager.getInstance().getNavToolByName(
				NavAmapAutoNavImpl.PACKAGE_NAME);
		if (null != gdApp) {
			int version = gdApp.getMapVersion();
			JNIHelper.logd(String.format("RoadTrafficDebug:GD check version: pkg = %s, version = %s",
					NavAmapAutoNavImpl.PACKAGE_NAME, version));

			return version >= GAODE_SUPPORT_VERSION_CAR;
		}

		// 尝试检查车镜版版本号
		gdApp = NavAppManager.getInstance().getNavToolByName(NavAmapAutoNavImpl.PACKAGE_NAME_LITE);
		if (null != gdApp) {
			int version = gdApp.getMapVersion();
			JNIHelper.logd(String.format("RoadTrafficDebug:GD check version: pkg = %s, version = %s",
					NavAmapAutoNavImpl.PACKAGE_NAME_LITE, version));

			// 这个大于是copy的原逻辑, 未经过具体验证
			return version > GAODE_SUPPORT_VERSION;
		}

		JNIHelper.logd("RoadTrafficDebug:GD app not exist!");
		return false;
	}
}
