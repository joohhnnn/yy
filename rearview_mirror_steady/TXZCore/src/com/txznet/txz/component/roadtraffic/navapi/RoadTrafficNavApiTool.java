package com.txznet.txz.component.roadtraffic.navapi;

import android.text.TextUtils;

import com.txz.ui.voice.VoiceData;
import com.txznet.txz.component.nav.INavInquiryRoadTraffic;
import com.txznet.txz.component.nav.NavThirdApp;
import com.txznet.txz.component.nav.txz.NavApiImpl;
import com.txznet.txz.component.roadtraffic.IInquiryRoadTrafficListener;
import com.txznet.txz.component.roadtraffic.IRoadTrafficTool;
import com.txznet.txz.component.roadtraffic.RoadTrafficResult;
import com.txznet.txz.jni.JNIHelper;
import com.txznet.txz.module.nav.NavManager;
import com.txznet.txz.module.roadtraffic.RoadTrafficManager;

/**
 * 基于导航工具NavApiImpl实现的路况查询工具
 */
public class RoadTrafficNavApiTool implements IRoadTrafficTool, NavApiImpl.OnInquiryRoadTrafficResultListener {

    private INavInquiryRoadTraffic navApiImpl;
    private IInquiryRoadTrafficListener listener;

    public RoadTrafficNavApiTool() {
        NavThirdApp localNavImpl = NavManager.getInstance().getLocalNavImpl();
        if (localNavImpl instanceof INavInquiryRoadTraffic) {
            navApiImpl = (INavInquiryRoadTraffic) localNavImpl;
            navApiImpl.registerInquiryRoadTrafficResultListener(this);
        }
    }

    RoadTrafficManager.SearchReq searchReq = new RoadTrafficManager.SearchReq() {
        @Override
        public void cancel() {
            JNIHelper.logd("RoadTrafficDebug:NavApi cancel inqury");
            listener = null;
            if (navApiImpl != null) {
                navApiImpl.unregisterInquiryRoadTrafficResultListener(RoadTrafficNavApiTool.this);
            }
        }
    };

    RoadTrafficManager.SearchReq emptyReq = new RoadTrafficManager.SearchReq() {
        @Override
        public void cancel() {
            if (navApiImpl != null) {
                navApiImpl.unregisterInquiryRoadTrafficResultListener(RoadTrafficNavApiTool.this);
            }
        }
    };

    // 这个外部没调用方
    @Override
    public void init() {
    }

    @Override
    public RoadTrafficManager.SearchReq inquiryRoadTrafficByPoi(final VoiceData.RoadTrafficQueryInfo info, IInquiryRoadTrafficListener listener) {
        return inquiryRoadTraffic(info, listener, new Runnable() {
            @Override
            public void run() {
                navApiImpl.inquiryRoadTrafficByPoi(info.strCity, info.strKeywords);
            }
        });
    }

    @Override
    public RoadTrafficManager.SearchReq inquiryRoadTrafficByFront(VoiceData.RoadTrafficQueryInfo info, IInquiryRoadTrafficListener listener) {
        return inquiryRoadTraffic(info, listener, new Runnable() {
            @Override
            public void run() {
                navApiImpl.inquiryRoadTrafficByFront();
            }
        });
    }

    @Override
    public RoadTrafficManager.SearchReq inquiryRoadTrafficByNearby(final VoiceData.RoadTrafficQueryInfo info, IInquiryRoadTrafficListener listener) {
        return inquiryRoadTraffic(info, listener, new Runnable() {
            @Override
            public void run() {
                navApiImpl.inquiryRoadTrafficByNearby(info.strCity, info.strKeywords);
            }
        });
    }

    private RoadTrafficManager.SearchReq inquiryRoadTraffic(VoiceData.RoadTrafficQueryInfo info, IInquiryRoadTrafficListener listener, Runnable doneBlock) {
        JNIHelper.logd("RoadTrafficDebug:NavApi inquiryRoadTraffic" + (info == null ? "" : (info.strCity + ":" + info.strKeywords + ":" + info.strDirection)));
        if (!isInquiryTrafficSupported()) {
            listener.onError(RoadTrafficManager.ERROR_CODE_NULL, "null");
            return emptyReq;
        }
        this.listener = listener;
        if (doneBlock != null) {
            doneBlock.run();
        }
        return searchReq;
    }

    private boolean isInquiryTrafficSupported() {
        if (navApiImpl == null) {
            return false;
        }
        return navApiImpl.isInquiryRoadTrafficSupported();
    }

    @Override
    public void onInquiryRoadTrafficResult(int result, String message) {
        if (listener == null) {
            return;
        }
        if (result == 1 && !TextUtils.isEmpty(message)) {
            RoadTrafficResult roadTrafficResult = new RoadTrafficResult();
            roadTrafficResult.setErrorCode(result);
            roadTrafficResult.setResultText(message);
            roadTrafficResult.setSourceType(RoadTrafficManager.SOURCE_TYPE_NAV_API);
            listener.onResult(roadTrafficResult);
            JNIHelper.logd("RoadTrafficDebug:NavApi onResult text=" + message);
        } else {
            // RoadTrafficToolConImp错误码是相互覆盖的，其实也没用
            if (result == 2) { // 网络异常
                listener.onError(RoadTrafficManager.ERROR_CODE_TIMEOUT, "");
            } else if (result == 3) { // 没有路况信息
                listener.onError(RoadTrafficManager.ERROR_CODE_OTHER, "");
            } else if (result == 4) {
                listener.onError(4, "");
            } else if (result == 5) { //
                listener.onError(-1, "");
            } else {
                listener.onError(-1, "");
            }
            JNIHelper.logd("RoadTrafficDebug:NavApi onError");
        }
    }

    @Override
    public void onInquiryRoadTrafficByFrontResult(String info) {
        if (listener == null) {
            return;
        }
        if (TextUtils.isEmpty(info)) {
            listener.onError(RoadTrafficManager.ERROR_CODE_OTHER, "");
        } else {
            RoadTrafficResult roadTrafficResult = new RoadTrafficResult();
            roadTrafficResult.setErrorCode(1);
            roadTrafficResult.setResultText(info);
            roadTrafficResult.setSourceType(RoadTrafficManager.SOURCE_TYPE_NAV_API);
            listener.onResult(roadTrafficResult);
        }
    }
}
