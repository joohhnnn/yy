package com.txznet.webchat.stores;

import android.os.Bundle;
import android.text.TextUtils;

import com.txznet.comm.remote.GlobalContext;
import com.txznet.loader.AppLogic;
import com.txznet.webchat.R;
import com.txznet.webchat.actions.Action;
import com.txznet.webchat.actions.ActionType;
import com.txznet.webchat.actions.LoginActionCreator;
import com.txznet.webchat.actions.TtsActionCreator;
import com.txznet.webchat.comm.plugin.model.WxUserCache;
import com.txznet.webchat.dispatcher.Dispatcher;
import com.txznet.webchat.helper.TXZSyncHelper;
import com.txznet.webchat.ui.base.widgets.PushLoginNotificationDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * 微信登录二维码显示相关Store
 */
public class WxQrCodeStore extends Store {
    private static WxQrCodeStore sInstance = new WxQrCodeStore(Dispatcher.get());

    WxQrCodeStore(Dispatcher dispatcher) {
        super(dispatcher);
    }

    public static WxQrCodeStore get() {
        return sInstance;
    }

    private String qrCode;
    private boolean isScanned;
    private String picStr;
    private boolean isQrCodeInvalid;
    private boolean mIsRetrievingQR = false; // 正在获取二维码标志

    // pushLogin相关
    private boolean bPushLoginReq; // 正在pushLogin
    private boolean bPushLoginSuccess; // 已经pushLogin
    private List<WxUserCache> mUserCacheList; // 本地缓存用户列表
    private WxUserCache mCurrentUser; // 当前选中用户

    /**
     * 获取本地缓存用户列表
     *
     * @return
     */
    public List<WxUserCache> getUserCacheList() {
        return mUserCacheList;
    }

    /**
     * 获取当前选中的缓存
     *
     * @return
     */
    public WxUserCache getCurrentUserCache() {
        return mCurrentUser;
    }

    /**
     * 是否正在进行pushLogin
     *
     * @return
     */
    public boolean isPushLoginRequesting() {
        return bPushLoginReq;
    }

    /**
     * pushLogin是否成功
     *
     * @return
     */
    public boolean isPushLoginSuccess() {
        return bPushLoginSuccess;
    }

    /**
     * 获取二维码
     */
    public String getQrCode() {
        return qrCode;
    }

    /**
     * 是否被扫描
     */
    public boolean isScanned() {
        return isScanned;
    }

    /**
     * 获取扫描者头像
     */
    public String getScannerPicStr() {
        return picStr;
    }

    /**
     * 二维码是否已经失效
     */
    public boolean isQrCodeInvalid() {
        return isQrCodeInvalid;
    }

    /**
     * 是否正在获取二维码
     */
    public boolean isRetrieving() {
        return mIsRetrievingQR;
    }

    @Override
    public void onDispatch(Action action) {
        boolean changed = false;
        switch (action.getType()) {
            case ActionType.WX_PLUGIN_LOGIC_RESET:
                changed = true;
                reset();
                break;

            case ActionType.WX_PLUGIN_PUSH_LOGIN_SYNC_CONTACT:
                changed = doSyncUserCacheList((List<WxUserCache>) action.getData());
                break;

            case ActionType.WX_PLUGIN_PUSH_LOGIN_SWITCH_CONTACT:
                changed = doSwitchUser((String) action.getData());
                break;

            case ActionType.WX_PLUGIN_AUTO_LOGIN_REQUEST:
                showPushLoginNotification((Bundle) action.getData());
                break;

            case ActionType.WX_PLUGIN_PUSH_LOGIN_REQUEST:
                isScanned = true;
                bPushLoginReq = true;
                bPushLoginSuccess = false;
                picStr = (String) action.getData();
                changed = true;
                break;

            case ActionType.WX_PLUGIN_PUSH_LOGIN_FAILED:
                changed = doPushLoginFailed();
                break;

            case ActionType.WX_PLUGIN_PUSH_LOGIN_SUCCESS:
                isScanned = true;
                bPushLoginSuccess = true;
                bPushLoginReq = false;
                picStr = (String) action.getData();
                changed = true;
                break;

            case ActionType.WX_QRCODE_REQUEST:
                changed = doQRCodeRequest();
                break;

            case ActionType.WX_QRCODE_SUCCESS:
                bPushLoginSuccess = false;
                bPushLoginReq = false;
                mIsRetrievingQR = false;
                changed = doGetQrCodeSuccess(action);
                break;
            case ActionType.WX_QRCODE_FAIL:
                changed = doGetQrCodeFail(action);
                break;
            case ActionType.WX_QRCODE_SCAN:
                changed = doQrCodeScan(action);
                break;
        }
        if (changed) {
            emitChange(new StoreChangeEvent(EVENT_TYPE_ALL));
        }
    }

    private void showPushLoginNotification(Bundle bundle) {
        PushLoginNotificationDialog.getInstance().show(bundle.getString("avatar"));
        TtsActionCreator.get().insertTts("请在手机上确认登录微信助手", true, null);
    }

    private boolean doPushLoginFailed() {
        TtsActionCreator.get().insertTts(GlobalContext.get().getResources().getString(R.string.tip_push_login_failed), true, null);

        if (PushLoginNotificationDialog.getInstance().isShowing()) {
            AppLogic.runOnUiGround(new Runnable() {
                @Override
                public void run() {
                    PushLoginNotificationDialog.getInstance().dismiss();
                }
            }, 4000);
        }

        return false;
    }

    private boolean doQRCodeRequest() {
        mIsRetrievingQR = true;
        bPushLoginReq = false;
        bPushLoginSuccess = false;
        bPushLoginReq = false;
        isScanned = false;
        // 清空当前用户
        mCurrentUser = null;

        return true;
    }

    private boolean doSyncUserCacheList(List<WxUserCache> list) {
        // 若本地缓存为空, 直接刷新二维码进入扫码逻辑
        if (null == list || list.isEmpty()) {
            mUserCacheList = new ArrayList<>();
            mCurrentUser = null;
            LoginActionCreator.get().refreshQRCode();
        } else {
            mUserCacheList = list;
            mCurrentUser = mUserCacheList.get(0);
        }

        return true;
    }

    private boolean doSwitchUser(String uid) {
        if (!TextUtils.isEmpty(uid)) {
            for (WxUserCache cache : mUserCacheList) {
                if (cache.getUin().equals(uid)) {
                    mCurrentUser = cache;
                }
            }
        } else {
            mCurrentUser = null;
        }

        return true;
    }

    private boolean doQrCodeScan(Action<Bundle> action) {
        Bundle bundle = action.getData();
        isScanned = true;
        picStr = bundle.getString("userAvatar");
        return true;
    }

    private boolean doGetQrCodeFail(Action<Bundle> action) {
        isQrCodeInvalid = true;
        mIsRetrievingQR = false;
        return true;
    }


    private boolean doGetQrCodeSuccess(Action<Bundle> action) {
        Bundle bundle = action.getData();
        qrCode = bundle.getString("qr_code");
        isQrCodeInvalid = false;
        TXZSyncHelper.getInstance().reportLoginStatus();
        return true;
    }

    private void reset() {
        mIsRetrievingQR = false;
        isQrCodeInvalid = true;
        isScanned = false;
        bPushLoginReq = false;
        bPushLoginSuccess = false;
        qrCode = "";
        picStr = "";
        //mUserCacheList = null;
        mCurrentUser = null;


        TXZSyncHelper.getInstance().reset();
        TXZSyncHelper.getInstance().reportLoginStatus();
    }


    public static final String EVENT_TYPE_ALL = "wx_qrcode_store";
}
