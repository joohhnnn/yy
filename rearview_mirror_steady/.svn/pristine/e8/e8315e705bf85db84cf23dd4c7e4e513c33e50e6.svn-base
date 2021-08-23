package com.txznet.txz.module.qrcode;

import android.text.TextUtils;

import com.txznet.comm.remote.util.LogUtil;
import com.txznet.txz.cfg.ProjectCfg;
import com.txznet.txz.module.IModule;
import com.txznet.txz.module.feedback.FeedbackManager;

import java.util.HashMap;
import java.util.Map;

public class QrCodeManager extends IModule {

    private static final QrCodeManager sQrCodeManager = new QrCodeManager();

    public static QrCodeManager getInstance() {
        return sQrCodeManager;
    }

    private Map<String, String> mOnlineQrCodes = new HashMap<String, String>();
    private Map<String, String> mOfflineQrCodes = new HashMap<String, String>();

    /**
     * 初始化正式网离线二维码
     */
    public void initOfficialOfflineQrCode() {
        if (mOfflineQrCodes != null) {
            mOfflineQrCodes.clear();
        } else {
            mOfflineQrCodes = new HashMap<String, String>();
        }
        //TODO add OfficialOfflineQrCode
        mOfflineQrCodes.put(FeedbackManager.CHANNEL_NO_FAIL_BIND_URL, "http://weixin.qq.com/q/0264mDcXxfbsP10000w03g");
        mOfflineQrCodes.put(FeedbackManager.CHANNEL_NO_SUCCESS_BIND_URL, "http://weixin.qq.com/q/02UPGucFxfbsP10000003A");
    }

    /**
     * 初始化测试网离线二维码
     */
    public void initNoOfficialOfflineQrCode() {
        if (mOfflineQrCodes != null) {
            mOfflineQrCodes.clear();
        } else {
            mOfflineQrCodes = new HashMap<String, String>();
        }
        //TODO add NoOfficialOfflineQrCode
        mOfflineQrCodes.put(FeedbackManager.CHANNEL_NO_FAIL_BIND_URL, "http://weixin.qq.com/q/02xUB3FrFRea4100000037");
        mOfflineQrCodes.put(FeedbackManager.CHANNEL_NO_SUCCESS_BIND_URL, "http://weixin.qq.com/q/02pCLBE4FRea410000g039");
    }

    /**
     * 初始化在线二维码
     */
    public void initOnlineQrCode() {
        if (mOnlineQrCodes != null) {
            mOnlineQrCodes.clear();
        } else {
            mOnlineQrCodes = new HashMap<String, String>();
            //TODO add OnlineQrCode
        }
    }

    public void addOnlineQrCode(String scene, String qrCode) {
        if (TextUtils.isEmpty(scene) || TextUtils.isEmpty(qrCode)) {
            LogUtil.e("params is empty");
            return;
        }
        if (mOnlineQrCodes == null) {
            mOnlineQrCodes = new HashMap<String, String>();
        }
        mOnlineQrCodes.put(scene, qrCode);
    }

    public String getQrCodeByScene(String scene) {
        String result = null;
        if (TextUtils.isEmpty(scene)) {
            throw new IllegalArgumentException("scene can't be null");
        }
        if (mOnlineQrCodes != null) {
            result = mOnlineQrCodes.get(scene);
        }
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        if (mOfflineQrCodes != null) {
            result = mOfflineQrCodes.get(scene);
        }
        if (!TextUtils.isEmpty(result)) {
            return result;
        }
        LogUtil.e("The qrcode of the scene: " + scene + " is empty, please add offlineQrCode to map");
        return "";
    }

}
