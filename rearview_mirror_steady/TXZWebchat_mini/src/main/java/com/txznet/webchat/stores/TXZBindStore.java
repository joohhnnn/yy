package com.txznet.webchat.stores;

import android.app.Activity;
import android.os.Bundle;

import com.txznet.comm.base.ActivityStack;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.TXZBindActionCreator;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.log.L;
import com.txznet.webchat.model.TXZBindInfo;

public class TXZBindStore extends Store {
    private static final String DEFAULT_BIND_URL = "http://weixin.qq.com/r/N0jTywbExlu5rUKo9x0l";
    private static TXZBindStore sInstance = new TXZBindStore(Dispatcher.get());
    private TXZBindInfo mBindInfo;
    private int mBindReqTime = 0;
    private int mBindRespSuccTime = 0;
    private int mBindRespErrorTime = 0;
    private boolean mWaitingResp;
    private int mLastRespType = 0; // 0 失败 1 成功

    private static final int DELAY_WHEN_SYNC_SUCC = 1000 * 60 * 25;
    private static final int DELAY_WHEN_SYNC_ERROR = 1000 * 6;

    TXZBindStore(Dispatcher dispatcher) {
        super(dispatcher);
        mBindInfo = new TXZBindInfo();
        mBindInfo.bindUrl = DEFAULT_BIND_URL;
    }

    public static TXZBindStore get() {
        return sInstance;
    }

    public boolean hasBind() {
        return mBindInfo.isBind;
    }

    public String getBindUrl() {
        return mBindInfo.bindUrl;
    }

    public String getBindNick() {
        return mBindInfo.nick;
    }

    public boolean isWaitingResp() {
        return mWaitingResp;
    }

    public boolean isException() {
        return mLastRespType == 0;
    }

    public boolean isFirstTimeLoading() {
//        LogUtil.logd("mWaitingResp = " + mWaitingResp + ", mBIndReqTime = " + mBindReqTime);
//        return mWaitingResp && (mBindReqTime == 1);
        return false;
    }

//    private boolean mHasReadException; // 是否读取过错误消息

    public boolean isFirstTimeException() {
//        if (mHasReadException) {
//            return false;
//        }
//        mHasReadException = true;
//        return isException() && mBindRespErrorTime == 1;
        return false;
    }

    public int getLastRespType() {
        return mLastRespType;
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.TXZ_BIND_INFO_REQ:
                changed = doBindInfoReq(action);
                break;
            case ActionType.TXZ_BIND_INFO_RESP:
                changed = doBindInfoResp(action);
                break;
            case ActionType.TXZ_BIND_INFO_RESP_ERROR:
                changed = doBindInfoRespError(action);
                break;
            case ActionType.TXZ_BIND_INFO_RESP_ONLY_NICK:
                changed = doBindInfoRespOnlyNick(action);
                break;
        }
        if (changed) {
            emitChange(EVENT_TYPE_ALL);
        }
        // 如果第一次获取二维码，则开启一个延迟刷新任务
        if (TXZBindStore.get().isFirstTimeLoading()) {
            AppLogic.removeUiGroundCallback(mRefreshBindTask);
            AppLogic.runOnUiGround(mRefreshBindTask, DELAY_WHEN_SYNC_SUCC);
        } else {
            AppLogic.removeUiGroundCallback(mRefreshBindTask);
            switch (TXZBindStore.get().getLastRespType()) {
                case -1:
                    break;
                case 0: // 刷新失败
                    mLastSyncStatus = -1;
                    mSyncDelay = DELAY_WHEN_SYNC_ERROR;
                    AppLogic.runOnUiGround(mRefreshBindTask, mSyncDelay);
                    break;
                case 1: // 刷新成功
                    mLastSyncStatus = 1;
                    mSyncDelay = DELAY_WHEN_SYNC_SUCC;
                    AppLogic.runOnUiGround(mRefreshBindTask, mSyncDelay);
                    break;
            }
        }
    }

    private boolean doBindInfoReq(Action action) {
        mWaitingResp = true;
        mBindReqTime++;
        return true;
    }

    private boolean doBindInfoResp(Action<Bundle> action) {
        L.d("on event: TXZ_BIND_INFO_RESP");
        Bundle data = action.getData();
        mWaitingResp = false;
        mBindRespSuccTime++;
        boolean isBind = data.getBoolean("isBind", false);
        L.d("is bind: " + isBind);
        mBindInfo.isBind = isBind;
        mBindInfo.bindUrl = data.getString("bindUrl");
        if (isBind) {
            mBindInfo.nick = data.getString("nick");
        }
        mLastRespType = 1;
        return true;
    }

    private boolean doBindInfoRespError(Action action) {
        mWaitingResp = false;
        mBindRespErrorTime++;
        mLastRespType = 0;
        return true;
    }

    private boolean doBindInfoRespOnlyNick(Action<Bundle> action) {
        Bundle data = action.getData();
        mBindRespSuccTime++;
        mBindInfo.nick = data.getString("nick");
        mBindInfo.isBind = true;
        mLastRespType = 1;
        return true;
    }

    public static final String EVENT_TYPE_ALL = "txz_bind_store";

    private int mLastSyncStatus = 0;
    private int mSyncDelay = DELAY_WHEN_SYNC_SUCC;

    Runnable mRefreshBindTask = new Runnable() {
        @Override
        public void run() {
            Activity act = ActivityStack.getInstance().currentActivity();
            if (!(act == null && mLastSyncStatus == -1)) {
//                if (!Config.FORCE_WECHAT_MODE) {
                TXZBindActionCreator.get().subscribeBindInfo();
                mBindReqTime++;
//                }
            }
            AppLogic.removeUiGroundCallback(mRefreshBindTask);
            AppLogic.runOnUiGround(mRefreshBindTask, mSyncDelay);
        }
    };
}
